param(
    [string]$guid,
    [string]$name
)

# This function has to be there because of DEV and TEST environment which does not have ExchangeOnline
# PROD ENV - synchronization into ExchangeOnline can take some time
function TryGetCasMailbox {
    param (
        [Parameter(Mandatory)]
        [string]$Identity,
        [Parameter(Mandatory)]
        [object]$remoteMailbox
    )

    try {
        return Get-CasMailbox -Identity $guid -DomainController $DOMAIN_CONTROLLER -ErrorAction Stop -WarningAction SilentlyContinue
    }
    catch {
        if ($_.FullyQualifiedErrorId -like "*ManagementObjectNotFoundException*") {
            Log "$SCRIPT_NAME ExchangeOnline mailbox was not created yet for $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $remoteMailbox)" "WARN" "$OPERATION_NAME"
            return $null
        }
        else {
            $msg = $_.Exception.Message
            Log "$SCRIPT_NAME Failed to search $($OBJECT_CLASS_NAME)(s): $_" "ERROR" "$OPERATION_NAME"
            DisposeSession $session
            Write-Host "$idmGeneralFatalError during search: $msg"
            exit 1
        }
    }
}

$SCRIPT_NAME = "RemoteMailbox-SEARCH"
$OPERATION_NAME = "searchOp"
$OBJECT_CLASS_NAME = "RemoteMailbox"

$HEADER = "guid|name|primarySmtpAddress|alias|database|imapEnabled|emailAddressPolicyEnabled|remoteRoutingAddress"

try {
    #if alreadyExistsException occurs midPoint send $name instead, for us it doesn't matter since both are valid unique identifiers
    $guid = if ([string]::IsNullOrEmpty($guid) -and $name -ne $null) { $name } else { $guid }

    if ($guid) {
        Log "$SCRIPT_NAME Searching $($OBJECT_CLASS_NAME): $guid" "INFO" "$OPERATION_NAME"
        $remoteMailbox = $null

        try {
            $remoteMailbox = Get-RemoteMailbox -Identity $guid -DomainController $DOMAIN_CONTROLLER -ErrorAction Stop -WarningAction SilentlyContinue
        } catch {
            HandleManagementObjectNotFoundException -ErrorRecord $_ -Identifier $guid -Operation $OPERATION_NAME
        }

        $casMailbox = TryGetCasMailbox -Identity $guid -RemoteMailbox $remoteMailbox
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME) was successfully searched: $(GetRecipientSummaryString -Recipient $remoteMailbox)" "INFO" "$OPERATION_NAME"

        $guid = $remoteMailbox.Guid
        $name = $remoteMailbox.Name
        $primarySmtpAddress = $remoteMailbox.PrimarySmtpAddress
        $alias = $remoteMailbox.EmailAddresses
        $database = $remoteMailbox.Database
        $imapEnabled = $casMailbox.ImapEnabled
        $emailAddressPolicyEnabled = $remoteMailbox.EmailAddressPolicyEnabled
        $remoteRoutingAddress = $remoteMailbox.RemoteRoutingAddress

        Write-Host $HEADER
        Write-Host "$guid|$name|$primarySmtpAddress|$alias|$database|$imapEnabled|$emailAddressPolicyEnabled|$remoteRoutingAddress"

    } else {
        Log "$SCRIPT_NAME Searching all $($OBJECT_CLASS_NAME) in Exchange" "INFO" "$OPERATION_NAME"
        Write-Host $HEADER

        $remoteMailboxes = Get-RemoteMailbox -DomainController $DOMAIN_CONTROLLER -ResultSize Unlimited -ErrorAction Stop -WarningAction SilentlyContinue

        foreach ($remoteMailbox in $remoteMailboxes) {

            $guid = $remoteMailbox.Guid
            $casMailbox = TryGetCasMailbox -Identity $guid -RemoteMailbox $remoteMailbox

            $name = $remoteMailbox.Name
            $primarySmtpAddress = $remoteMailbox.PrimarySmtpAddress
            $alias = $remoteMailbox.EmailAddresses
            $database = $remoteMailbox.Database
            $imapEnabled = $casMailbox.ImapEnabled
            $emailAddressPolicyEnabled = $remoteMailbox.EmailAddressPolicyEnabled
            $remoteRoutingAddress = $remoteMailbox.RemoteRoutingAddress

            Write-Host "$guid|$name|$primarySmtpAddress|$alias|$database|$imapEnabled|$emailAddressPolicyEnabled|$remoteRoutingAddress"
        }
        Log "$SCRIPT_NAME All $($OBJECT_CLASS_NAME) in Exchange was searched successfully" "INFO" "$OPERATION_NAME"
    }
} catch {
    $msg = $_.Exception.Message
    Log "$SCRIPT_NAME Failed to search $($OBJECT_CLASS_NAME)(s): $_" "ERROR" "$OPERATION_NAME"
    DisposeSession $session
    Write-Host "$idmGeneralFatalError during search: $msg"
    exit 1
}