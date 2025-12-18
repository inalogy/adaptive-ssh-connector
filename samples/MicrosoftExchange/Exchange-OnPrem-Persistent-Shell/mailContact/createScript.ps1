param(
    [string]$name,
    [string]$primarySmtpAddress,
    [string[]]$alias,
    [string]$externalEmailAddress
)

$SCRIPT_NAME = "MailContact-CREATE"
$OPERATION_NAME = "createOp"
$OBJECT_CLASS_NAME = "MailContact"

try {
    $mailContact = $null

    Disable-Mailbox -Identity $name -DomainController $DOMAIN_CONTROLLER -Confirm:$false -ErrorAction SilentlyContinue -WarningAction SilentlyContinue
    Disable-MailUser -Identity $name -DomainController $DOMAIN_CONTROLLER -Confirm:$false -ErrorAction SilentlyContinue -WarningAction SilentlyContinue

    try {
        Log "$SCRIPT_NAME Creating $($OBJECT_CLASS_NAME): $name" "INFO" "$OPERATION_NAME"
        $mailContact = Enable-MailContact -Identity $name -DomainController $DOMAIN_CONTROLLER -ExternalEmailAddress $externalEmailAddress -PrimarySmtpAddress $primarySmtpAddress -Confirm:$false -ErrorAction Stop -WarningAction SilentlyContinue
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailContact) was successfully created" "INFO" "$OPERATION_NAME"
    } catch {
        Log "$SCRIPT_NAME Checking if $($OBJECT_CLASS_NAME) exists: $name" "INFO" "$OPERATION_NAME"

        CheckConflicts -PrimarySmtpAddress $primarySmtpAddress -Name $name -ErrorRecord $_ -ObjectClass $OBJECT_CLASS_NAME
    }

    if ($alias) {
        Log "$SCRIPT_NAME Setting alias for $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailContact)" "INFO" "$OPERATION_NAME"
        Set-MailContact $mailContact.Identity -DomainController $DOMAIN_CONTROLLER -EmailAddresses $alias -Confirm:$false -ForceUpgrade -WarningAction SilentlyContinue
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailContact) aliases were successfully adjusted" "INFO" "$OPERATION_NAME"
    }

    Write-Host "guid|name"
    Write-Host "$($mailContact.guid)|$($mailContact.Name)"

} catch {
    $msg = $_.Exception.Message
    Write-Host "$idmGeneralFatalError during create: $msg"
    DisposeSession $session
    Log "$SCRIPT_NAME failed to create $($OBJECT_CLASS_NAME): $_" "ERROR" "$OPERATION_NAME"
    exit 1
}