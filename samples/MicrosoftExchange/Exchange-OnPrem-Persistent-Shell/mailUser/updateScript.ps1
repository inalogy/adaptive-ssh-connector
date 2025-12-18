param(
    [string]$guid,
    [string]$name,
    [string]$primarySmtpAddress,
    [string[]]$alias,
    [string]$externalEmailAddress,
    [string]$emailAddressPolicyEnabled
)

$SCRIPT_NAME = "MailUser-UPDATE"
$OPERATION_NAME = "updateOp"
$OBJECT_CLASS_NAME = "MailUser"

$errors = @()

if ($name -eq $idmNullValue) {
    $errors += "Name cannot be updated to null"
}
if ($primarySmtpAddress -eq $idmNullValue) {
    $errors += "Primary SMTP Address cannot be updated to null"
}
if ($externalEmailAddress -eq $idmNullValue) {
    $errors += "External Email Address cannot be updated to null"
}
if ($errors.Count -gt 0) {
    Log "$SCRIPT_NAME Failed to $($OPERATION_NAME) $($OBJECT_CLASS_NAME) because of some of the required parameters from IDM were null" "ERROR" "$OPERATION_NAME"
    Write-Host "$idmGeneralFatalError during $($OPERATION_NAME) $($errors -join '; ')"
    DisposeSession $session
    exit 1
}

$setMailUserParams = @{
    Identity = $guid
    DomainController = $DOMAIN_CONTROLLER
    Confirm = $false
    ForceUpgrade = $true
    WarningAction = 'SilentlyContinue'
}

if ($name) {
    $setMailUserParams.Name = $name
}

if ($primarySmtpAddress) {
    $setMailUserParams.PrimarySmtpAddress = $primarySmtpAddress
}

if ($externalEmailAddress) {
    $setMailUserParams.ExternalEmailAddress = $externalEmailAddress
}

if ($emailAddressPolicyEnabled) {
    $setMailUserParams.EmailAddressPolicyEnabled = (convertToBoolean -stringValue $emailAddressPolicyEnabled)
}

try {
    if ($name -or $primarySmtpAddress -or $externalEmailAddress -or $emailAddressPolicyEnabled) {
        Log "$SCRIPT_NAME Updating $($OBJECT_CLASS_NAME): $guid" "INFO" "$OPERATION_NAME"
        Set-MailUser @setMailUserParams
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME) was updated successfully: $guid" "INFO" "$OPERATION_NAME"
    }

    # alias separately because "You can't use the PrimarySmtpAddress and EmailAddresses parameters at the same time"
    if ($alias) {
        Set-MailUser -Identity $guid -DomainController $DOMAIN_CONTROLLER -EmailAddresses (Process-Aliases -operations $alias) -Confirm:$false -ForceUpgrade -WarningAction SilentlyContinue
    }
} catch {
    HandleManagementObjectNotFoundException -ErrorRecord $_ -Identifier $guid -Operation $OPERATION_NAME
}