param(
    [string]$guid
)

$SCRIPT_NAME = "MailUser-DELETE"
$OPERATION_NAME = "deleteOp"
$OBJECT_CLASS_NAME = "MailUser"

try {
    Log "$SCRIPT_NAME Disabling $($OBJECT_CLASS_NAME): $guid" "INFO" "$OPERATION_NAME"
    Disable-MailUser -Identity $guid -DomainController $DOMAIN_CONTROLLER -Confirm:$false -ErrorAction Stop -WarningAction SilentlyContinue
    Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $guid was successfully disabled" "INFO" "$OPERATION_NAME"
} catch {
    Log "$SCRIPT_NAME Checking if $($OBJECT_CLASS_NAME) exists: $name" "WARN" "$OPERATION_NAME"
    try {
        Get-MailUser -Identity $guid -DomainController $DOMAIN_CONTROLLER -ErrorAction Stop -WarningAction SilentlyContinue
    }
    catch {
        HandleManagementObjectNotFoundException -ErrorRecord $_ -Identifier $guid -Operation $OPERATION_NAME
    }
}