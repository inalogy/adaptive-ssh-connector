# === BEGIN CONFIG ===
$clientId = "EntraID Client id"
$tenantId = "EntraID Tenant id"
$certificateThumbprint = "certificatieTumbprint"
$organization = "test-org.onmicrosoft.com"
$midPointNullValue = "__NULL_VALUE__"
$midPointGeneralFatalError = "__FATAL_ERROR__"
$midPointPreloadScriptSuccessMessage = "__SUCCESS_PRELOAD_SCRIPT__"
$LogLevel = "ERROR"  # Options: DEBUG, INFO, WARN, ERROR

# === INIT MAIN CONNECTIONS ===
$exoCommands = @(
    "Set-DistributionGroup",
    "Get-DistributionGroup",
    "Get-DistributionGroupMember",
    "Set-DistributionGroupMember",
    "New-DistributionGroup",
    "Add-DistributionGroupMember",
    "Remove-DistributionGroupMember",
    "Remove-DistributionGroup",
    "Get-OrganizationConfig"
)

Connect-MgGraph -ClientId $clientId -TenantId $tenantId -CertificateThumbprint $certificateThumbprint -NoWelcome

Connect-ExchangeOnline `
    -AppId $clientId `
    -CertificateThumbprint $certificateThumbprint `
    -Organization $organization `
    -ShowBanner:$false `
    -ShowProgress:$false `
    -LoadCmdletHelp:$false `
    -CommandName $exoCommands


# === LOGGING INIT ===
$SCRIPT_NAME = [System.IO.Path]::GetFileNameWithoutExtension($MyInvocation.InvocationName)
if (-not $SCRIPT_NAME) {
    $SCRIPT_NAME = "script"
}
$LogDir = "$PSScriptRoot\midpoint-script-logs"

if (-not (Test-Path $LogDir)) {
    New-Item -Path $LogDir -ItemType Directory | Out-Null
}

$logDate = Get-Date -Format "yyyyMMdd"
$LogFile = Join-Path $LogDir ("log_{0}_{1}.log" -f $SCRIPT_NAME, $logDate)


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
    }

    if ($levelOrder[$level] -lt $levelOrder[$LogLevel]) {
        return
    }

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss.fff"
    $contextTag = if ($context) { "[$context]" } else { "" }
    Add-Content -Path $LogFile -Value "$timestamp [$level]$contextTag $message"
}

function Test-ExchangeOnlineConnection {
    try {
        Get-OrganizationConfig -ErrorAction Stop | Out-Null
        return $true
    } catch {
        return $false
    }
}

function Test-MgGraphConnection {
    try {
        Get-MgUser -Top 1 -ErrorAction Stop | Out-Null
        return $true
    } catch {
        return $false
    }
}

#main validation is needed for preloadScript functionality
if ((Test-ExchangeOnlineConnection) -and (Test-MgGraphConnection)) {
    Write-Host $midPointPreloadScriptSuccessMessage
} else {
    Write-Host "$midPointGeneralFatalError Occured while test-connection in preloadScript."
}





