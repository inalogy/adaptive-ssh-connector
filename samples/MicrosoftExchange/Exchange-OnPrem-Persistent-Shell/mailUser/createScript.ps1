param(
    [string]$name,
    [string]$primarySmtpAddress,
    [string[]]$alias,
    [string]$externalEmailAddress,
    [string]$emailAddressPolicyEnabled
)

$SCRIPT_NAME = "MailUser-CREATE"
$OPERATION_NAME = "createOp"
$OBJECT_CLASS_NAME = "MailUser"

$errors = @()

Disable-Mailbox -Identity $name -DomainController $DOMAIN_CONTROLLER -Confirm:$false -ErrorAction SilentlyContinue -WarningAction SilentlyContinue

if ($name -eq $idmNullValue) {
    $errors += "Name cannot be null"
}
if ($primarySmtpAddress -eq $idmNullValue) {
    $errors += "Primary SMTP Address cannot be null"
}
if ($errors.Count -gt 0) {
    Log "$SCRIPT_NAME Failed to $($OPERATION_NAME) $($OBJECT_CLASS_NAME) because of some of the required parameters from IDM were null" "ERROR" "$OPERATION_NAME"
    Write-Host "$idmGeneralFatalError during $($OPERATION_NAME) $($errors -join '; ')"
    DisposeSession $session
    exit 1
}

try {
    $mailUser = $null

    try {
        Log "$SCRIPT_NAME Creating $($OBJECT_CLASS_NAME): $name" "INFO" "$OPERATION_NAME"
        $mailUser = Enable-MailUser -Identity "$name" -DomainController $DOMAIN_CONTROLLER -ExternalEmailAddress $externalEmailAddress -PrimarySmtpAddress $primarySmtpAddress -Confirm:$false -ErrorAction Stop -WarningAction SilentlyContinue
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailUser) was successfully created" "INFO" "$OPERATION_NAME"
    } catch {
        Log "$SCRIPT_NAME Checking if $($OBJECT_CLASS_NAME) exists: $name" "INFO" "$OPERATION_NAME"

        CheckConflicts -PrimarySmtpAddress $primarySmtpAddress -Name $name -ErrorRecord $_ -ObjectClass $OBJECT_CLASS_NAME
    }

    if ($alias) {
        Log "$SCRIPT_NAME Setting alias for $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailUser)" "INFO" "$OPERATION_NAME"
        Set-MailUser -Identity "$name" -DomainController $DOMAIN_CONTROLLER -EmailAddresses $alias -Confirm:$false -ForceUpgrade -WarningAction SilentlyContinue
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailUser) aliases was successfully adjusted" "INFO" "$OPERATION_NAME"
    }

    if ($emailAddressPolicyEnabled) {
        Log "$SCRIPT_NAME Setting emailAddressPolicyEnabled for $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailUser)" "INFO" "$OPERATION_NAME"
        Set-MailUser -Identity "$name" -DomainController $DOMAIN_CONTROLLER -EmailAddressPolicyEnabled (convertToBoolean -stringValue $emailAddressPolicyEnabled) -Confirm:$false -WarningAction SilentlyContinue
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailUser) emailAddressPolicyEnabled was successfully adjusted" "INFO" "$OPERATION_NAME"
    }

    Write-Host "guid|name"
    Write-Host "$($mailUser.guid)|$($mailUser.Name)"
} catch {
    $msg = $_.Exception.Message
    Write-Host "$idmGeneralFatalError during create: $msg"
    DisposeSession $session
    Log "$SCRIPT_NAME failed to create MailUser: $_" "ERROR" "$OPERATION_NAME"
    exit 1
}