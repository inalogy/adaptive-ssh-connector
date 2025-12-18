param(
    [string]$guid,
    [string]$name
)

$SCRIPT_NAME = "Mailbox-SEARCH"
$OPERATION_NAME = "searchOp"
$OBJECT_CLASS_NAME = "Mailbox"

$HEADER = "guid|name|primarySmtpAddress|alias|database|imapEnabled|emailAddressPolicyEnabled"

try {
    #if alreadyExistsException occurs midPoint send $name instead, for us it doesn't matter since both are valid unique identifiers
    $guid = if ([string]::IsNullOrEmpty($guid) -and $name -ne $null) { $name } else { $guid }

    if ($guid) {
        Log "$SCRIPT_NAME Searching $($OBJECT_CLASS_NAME): $guid" "INFO" "$OPERATION_NAME"
        $mailbox = $null
        try {
            $mailbox = Get-Mailbox -Identity $guid -DomainController $DOMAIN_CONTROLLER -ErrorAction Stop -WarningAction SilentlyContinue
        }
        catch {
            HandleManagementObjectNotFoundException -ErrorRecord $_ -Identifier $guid -Operation $OPERATION_NAME
        }
        $casMailbox = Get-CasMailbox -Identity $guid -DomainController $DOMAIN_CONTROLLER -ErrorAction Stop -WarningAction SilentlyContinue

        Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME) was successfully searched: $(GetRecipientSummaryString -Recipient $mailbox)" "INFO" "$OPERATION_NAME"
        Write-Host $HEADER
        Write-Host "$($mailbox.Guid)|$($mailbox.Name)|$($mailbox.PrimarySmtpAddress)|$($mailbox.EmailAddresses)|$($mailbox.Database)|$($casMailbox.ImapEnabled)|$($mailbox.EmailAddressPolicyEnabled)"
    } else {
        Log "$SCRIPT_NAME Searching all $($OBJECT_CLASS_NAME) in Exchange" "INFO" "$OPERATION_NAME"
        Write-Host $HEADER

        $mailboxes = Get-Mailbox -DomainController $DOMAIN_CONTROLLER -ResultSize Unlimited -ErrorAction Stop -WarningAction SilentlyContinue

        foreach ($mailbox in $mailboxes) {
            $guid = $mailbox.Guid
            $casMailbox = Get-CasMailbox -Identity $guid -DomainController $DOMAIN_CONTROLLER -ErrorAction Stop -WarningAction SilentlyContinue

            Write-Host "$($mailbox.Guid)|$($mailbox.Name)|$($mailbox.PrimarySmtpAddress)|$($mailbox.EmailAddresses)|$($mailbox.Database)|$($casMailbox.ImapEnabled)|$($mailbox.EmailAddressPolicyEnabled)"
        }
        Log "$SCRIPT_NAME All $($OBJECT_CLASS_NAME) in Exchange was searched successfully" "INFO" "$OPERATION_NAME"
    }
} catch {
    $msg = $_.Exception.Message
    Write-Host "$idmGeneralFatalError during search: $msg"
    DisposeSession $session
    Log "$SCRIPT_NAME Failed to search $($OBJECT_CLASS_NAME)(s): $_" "ERROR" "$OPERATION_NAME"
    exit 1
}