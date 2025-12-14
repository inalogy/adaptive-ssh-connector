# Setting environment dependant variables
. "$PSScriptRoot\envVars.ps1"

# Functions underneath has to be here because they are used in the preload script
# this version of logging isn't thread safe so should be used only for debugging
function Log {
    param (
        [string]$message,
        [ValidateSet("DEBUG", "INFO", "WARN", "ERROR")]
        [string]$level = "INFO",
        [string]$context  # Optional e.g. "updateOp", "createOp"
    )

    $levelOrder = @{
        "DEBUG" = 1
        "INFO"  = 2
        "WARN"  = 3
        "ERROR" = 4
        "NONE" = 5
    }

    if ($levelOrder[$level] -lt $levelOrder[$LogLevel]) {
        return
    }

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss.fff"
    $contextTag = if ($context) { "[$context]" } else { "" }
    Add-Content -Path $LogFile -Value "$timestamp [$level]$contextTag $message"
}

function DisposeSession {
    param (
        [Parameter(Mandatory=$true)] $session
    )

    if ($session) {
        Remove-PSSession $session -ErrorAction SilentlyContinue
        Log "$SCRIPT_NAME session disposed successfully" "DEBUG"
    }
}

$SCRIPT_NAME = "PRELOAD"
$CONNECTION_URI = "$($METHOD)://$EXCHANGE_SERVER_ADDRESS/powershell"
$idmNullValue = "__NULL_VALUE__"      #this match value from dynamicConfiguration.json
$idmGeneralFatalError = "__FATAL_ERROR__" #this match value from dynamicConfiguration.json
$PRELOAD_SCRIPT_SUCCES_RETURN_VALUE = "BASE-OK"
$COMMANDS_TO_IMPORT = @(
    "Get-Recipient"
    "Enable-MailContact"
    "Disable-Mailbox"
    "Disable-MailUser"
    "Set-MailContact"
    "Disable-MailContact"
    "Get-MailContact"
    "Enable-RemoteMailbox"
    "Set-CasMailbox"
    "Disable-RemoteMailbox"
    "Get-CasMailbox"
    "Get-RemoteMailbox"
    "Set-RemoteMailbox"
    "Enable-Mailbox"
    "Set-Mailbox"
    "Get-Mailbox"
    "Enable-MailUser"
    "Set-MailUser"
    "Get-MailUser"
)

# === LOGGING INIT ===
$LogDir = "$PSScriptRoot\log"

if (-not (Test-Path $LogDir)) {
    New-Item -Path $LogDir -ItemType Directory | Out-Null
}

$logDate = Get-Date -Format "yyyyMMdd"
$LogFile = Join-Path $LogDir ("log_{0}_{1}.log" -f "scripts", $logDate)

# === SESSION INIT ===
$maxAttempts = 10

# ====================================
# Internally, a Get-Command call is made inside Export-PSSession, Import-PSSession, Invoke-Command,
# which in the new implementation of OpenSSH for Windows requires an interactive shell.
# The connector cannot use interactive shell due to adding, for example, color codes to stdout
# ====================================
# It randomly throws this error:
#   - Running the Get-Command command in a remote session reported the following error:
#   Win32 internal error "Access is denied" 0x5 occurred while reading the console output buffer.
#   Contact Microsoft Customer Support Services..
# ====================================
# I have tried these alternatives Export-PSSession, Import-PSSession, Invoke-Command.
# So it is worth trying only in a different way.
# Last worked without repeated attempts on Windows OpenSSH version OpenSSH_for_Windows_8.1p1.
# It definitely does not work on version OpenSSH_for_Windows_9.8p1.
for ($i = 1; $i -le $maxAttempts; $i++) {
    try
    {
        $session = New-PSSession -ConfigurationName Microsoft.Exchange -ConnectionUri $CONNECTION_URI -ThrottleLimit 60000 -Authentication Kerberos -AllowRedirection -ErrorAction Stop
        Import-PSSession $session -CommandName $COMMANDS_TO_IMPORT -AllowClobber > $null

        Log "$SCRIPT_NAME session created successfully" "DEBUG"
        Write-Host $PRELOAD_SCRIPT_SUCCES_RETURN_VALUE
        break
    }
    catch {
        DisposeSession $session

        $msg = $_.Exception.Message
        if ($i -eq $maxAttempts) {
            Log "$SCRIPT_NAME session creation failed, max. attempts exceeded: $maxAttempts, ex. message: $msg" "ERROR"
            Write-Host "$idmGeneralFatalError session creation failed, max. attempts exceeded: $maxAttempts, ex. message: $msg"
            exit 1
        } else {
            Log "$SCRIPT_NAME session creation failed, sleep for: $i, ex. message: $msg" "DEBUG"
            Start-Sleep -Seconds 1
        }
    }
}

# === FUNCTIONS ===
function HandleManagementObjectNotFoundException {
    param (
        [Parameter(Mandatory)] $ErrorRecord,
        [Parameter(Mandatory)] [string]$Identifier,
        [Parameter()] [string]$Operation = "genericOp"
    )

    if ($ErrorRecord.FullyQualifiedErrorId -like "*ManagementObjectNotFoundException*") {
        Log "Object with identifier '$Identifier' was not found" "WARN" $Operation
        Write-Host "UnknownUid"
        exit 1
    } else {
        $msg = $ErrorRecord.Exception.Message
        Write-Host "$idmGeneralFatalError during $($Operation): $msg"
        DisposeSession $session
        Log "Operation '$Operation' failed with unexpected error: $ErrorRecord" "ERROR" $Operation
        exit 1
    }
}

function CheckConflicts {
    param (
        [Parameter(Mandatory)] [string]$PrimarySmtpAddress,
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] $ErrorRecord,
        [Parameter()] [string]$ObjectClass
    )
    $conflictedObject = GetRecipientByPrimarySmtp -PrimarySmtpAddress $PrimarySmtpAddress

    if ($conflictedObject) {
        if ($conflictedObject.Name -eq $Name) {
            Log "Found already existed object with $(GetRecipientSummaryString -Recipient $conflictedObject)" "WARN" "createOp"
            Write-Host "ObjectAlreadyExists"
            exit 1
        } else {
            Log "Email $PrimarySmtpAddress  is already used by object with $(GetRecipientSummaryString -Recipient $conflictedObject)" "ERROR" "createOp"
            Write-Host "E-mail '$PrimarySmtpAddress' is already used by different object with $(GetRecipientSummaryString -Recipient $conflictedObject)"
            exit 1
        }
    } else {
        $msg = $ErrorRecord.Exception.Message
        Write-Host "$idmGeneralFatalError during create: $msg"
        DisposeSession $session
        Log "Failed to create $($ObjectClass) with name: $Name;PrimarySmtpAddress: $PrimarySmtpAddress; ERROR: $msg" "ERROR"
        exit 1
    }
}

function GetRecipientByPrimarySmtp {
    param (
        [Parameter(Mandatory)] [string]$PrimarySmtpAddress
    )
    # Filter is case insensitive for smtp and email address
    $filter = "EmailAddresses -like 'smtp:$PrimarySmtpAddress'"

    return Get-Recipient -Filter $filter -ResultSize 1
}

function GetRecipientSummaryString {
    param (
        [Parameter(Mandatory = $true)]
        [Object]$Recipient
    )

    $name = $Recipient.Name

    if (-not $Recipient.EmailAddresses) {
        return "name: `"$name`"; emails: """
    }

    $emailList = $Recipient.EmailAddresses | ForEach-Object {
        $_.ToString()
    }

    $emails = $emailList -join ", "
    return "name: `"$name`"; emails: `"$emails`""
}

function Process-Aliases {
    param (
        [string[]]$operations
    )

    $emailAddressesToUpdate = @{
        add = @()
        remove = @()
    }

    foreach ($operation in $operations) {
        $action, $emailAddress = $operation -split ':', 2

        if ($action -eq "ADD") {
            $emailAddressesToUpdate.add += $emailAddress
        } elseif ($action -eq "REMOVE") {
            $emailAddressesToUpdate.remove += $emailAddress
        }
    }

    return $emailAddressesToUpdate
}

function convertToBoolean {
    param (
        [string]$stringValue
    )

    return [System.Convert]::ToBoolean($stringValue)
}