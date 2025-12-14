param(
    [string]$guid,
    [string]$name,
    [string]$primarySmtpAddress,
    [string]$remoteRoutingAddress,
    [string[]]$alias,
    [string]$database,
    [string]$imapEnabled,
    [string]$emailAddressPolicyEnabled
)

$SCRIPT_NAME = "RemoteMailbox-UPDATE"
$OPERATION_NAME = "updateOp"
$OBJECT_CLASS_NAME = "RemoteMailbox"

$errors = @()

if ($name -eq $idmNullValue) {
    $errors += "Name cannot be updated to null"
}
if ($primarySmtpAddress -eq $idmNullValue) {
    $errors += "Primary SMTP Address cannot be updated to null"
}
if ($database -eq $idmNullValue) {
    $errors += "Database cannot be updated to null"
}
if ($errors.Count -gt 0) {
    Log "$SCRIPT_NAME Failed to $($OPERATION_NAME) $($OBJECT_CLASS_NAME) because of some of the required parameters from IDM were null" "ERROR" "$OPERATION_NAME"
    DisposeSession $session
    Write-Host "$idmGeneralFatalError during $($OPERATION_NAME) $($errors -join '; ')"
    exit
}

$setMailboxParams = @{
    Identity = $guid
    DomainController = $DOMAIN_CONTROLLER
    Confirm = $false
    WarningAction = 'SilentlyContinue'
}

if ($name) {
    $setMailboxParams.Name = $name
}

if ($primarySmtpAddress) {
    $setMailboxParams.PrimarySmtpAddress = $primarySmtpAddress
}

if ($remoteRoutingAddress) {
    $setMailboxParams.RemoteRoutingAddress = $remoteRoutingAddress
}

if ($database) {
    $setMailboxParams.Database = $database
}

if ($emailAddressPolicyEnabled) {
    $setMailboxParams.EmailAddressPolicyEnabled = (convertToBoolean -stringValue $emailAddressPolicyEnabled)
}

try {
    if ($name -or $primarySmtpAddress -or $database -or $remoteRoutingAddress -or $emailAddressPolicyEnabled) {
        Log "$SCRIPT_NAME Updating $($OBJECT_CLASS_NAME): $guid" "INFO" "$OPERATION_NAME"
        Set-RemoteMailbox @setMailboxParams
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME) was updated successfully: $guid" "INFO" "$OPERATION_NAME"
    }

    # alias separately because "You can't use the PrimarySmtpAddress and EmailAddresses parameters at the same time"
    if ($alias) {
        Set-RemoteMailbox -Identity $guid -DomainController $DOMAIN_CONTROLLER -EmailAddresses (Process-Aliases -operations $alias) -Confirm:$false -WarningAction SilentlyContinue
    }

    if ($imapEnabled) {
        Set-CasMailbox -Identity $guid -DomainController $DOMAIN_CONTROLLER -ImapEnabled (convertToBoolean -stringValue $imapEnabled) -Confirm:$false -WarningAction SilentlyContinue
    }
} catch {
    HandleManagementObjectNotFoundException -ErrorRecord $_ -Identifier $guid -Operation $OPERATION_NAME
}