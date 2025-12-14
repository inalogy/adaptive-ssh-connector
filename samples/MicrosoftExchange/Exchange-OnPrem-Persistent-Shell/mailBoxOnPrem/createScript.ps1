param(
    [string]$name,
    [string]$primarySmtpAddress,
    [string[]]$alias,
    [string]$database,
    [string]$imapEnabled,
    [string]$emailAddressPolicyEnabled
)

$SCRIPT_NAME = "Mailbox-CREATE"
$OPERATION_NAME = "createOp"
$OBJECT_CLASS_NAME = "Mailbox"

$errors = @()

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

$mailboxParams = @{
    Identity = $name
    PrimarySmtpAddress = $primarySmtpAddress
    DomainController = $DOMAIN_CONTROLLER
    Confirm = $false
    WarningAction = 'SilentlyContinue'
    ErrorAction = 'Stop'
}

if ($database) {
    $mailboxParams.Database = $database
}

$mailbox = $null
try {
    try {
        Log "$SCRIPT_NAME Creating $($OBJECT_CLASS_NAME): $name" "INFO" "$OPERATION_NAME"
        $mailbox = Enable-Mailbox @mailboxParams
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailbox) was successfully created" "INFO" "$OPERATION_NAME"
    } catch {
        Log "$SCRIPT_NAME Checking if $($OBJECT_CLASS_NAME) exists: $name" "INFO" "$OPERATION_NAME"

        CheckConflicts -PrimarySmtpAddress $primarySmtpAddress -Name $name -ErrorRecord $_ -ObjectClass $OBJECT_CLASS_NAME
    }

    if ($alias) {
        Log "$SCRIPT_NAME Setting alias for $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailbox)" "INFO" "$OPERATION_NAME"
        Set-Mailbox $mailbox.Identity -DomainController $DOMAIN_CONTROLLER -EmailAddresses $alias -Confirm:$false -WarningAction SilentlyContinue
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailbox) aliases were successfully adjusted" "INFO" "$OPERATION_NAME"
    }

    if ($imapEnabled) {
        Log "$SCRIPT_NAME Setting imapEnabled for $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailbox)" "INFO" "$OPERATION_NAME"
        Set-CasMailbox -Identity $mailbox.Identity -DomainController $DOMAIN_CONTROLLER -ImapEnabled (convertToBoolean -stringValue $imapEnabled) -Confirm:$false -WarningAction SilentlyContinue
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailbox) imapEnabled was successfully adjusted" "INFO" "$OPERATION_NAME"
    }

    if ($emailAddressPolicyEnabled) {
        Log "$SCRIPT_NAME Setting emailAddressPolicyEnabled for $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailbox)" "INFO" "$OPERATION_NAME"
        Set-Mailbox $mailbox.Identity -DomainController $DOMAIN_CONTROLLER -EmailAddressPolicyEnabled (convertToBoolean -stringValue $emailAddressPolicyEnabled) -Confirm:$false -WarningAction SilentlyContinue
        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME): $(GetRecipientSummaryString -Recipient $mailbox) emailAddressPolicyEnabled was successfully adjusted" "INFO" "$OPERATION_NAME"
    }

    Write-Host "guid|name"
    Write-Host "$($mailbox.guid)|$($mailbox.Name)"

} catch {
    $msg = $_.Exception.Message
    Write-Host "$idmGeneralFatalError during create: $msg"
    DisposeSession $session
    Log "$SCRIPT_NAME failed to create $($OBJECT_CLASS_NAME): $_" "ERROR" "$OPERATION_NAME"
    exit 1
}