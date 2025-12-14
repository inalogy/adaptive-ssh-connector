param(
    [string]$guid,
    [string]$name
)

$SCRIPT_NAME = "MailUser-SEARCH"
$OPERATION_NAME = "searchOp"
$OBJECT_CLASS_NAME = "MailUser"

$HEADER = "guid|name|primarySmtpAddress|alias|externalEmailAddress|emailAddressPolicyEnabled"

try {
    #if alreadyExistsException occurs midPoint send $name instead, for us it doesn't matter since both are valid unique identifiers
    $guid = if ([string]::IsNullOrEmpty($guid) -and $name -ne $null) { $name } else { $guid }

    if ($guid) {
        Log "$SCRIPT_NAME Searching $($OBJECT_CLASS_NAME): $guid" "INFO" "$OPERATION_NAME"
        try {
            $mailUser = Get-MailUser -Identity $guid -DomainController $DOMAIN_CONTROLLER -ErrorAction Stop -WarningAction SilentlyContinue
            Log "$SCRIPT_NAME $($OBJECT_CLASS_NAME) was successfully searched: $(GetRecipientSummaryString -Recipient $mailUser)" "INFO" "$OPERATION_NAME"
            Write-Host $HEADER
            Write-Host "$($mailUser.Guid)|$($mailUser.Name)|$($mailUser.PrimarySmtpAddress)|$($mailUser.EmailAddresses)|$($mailUser.ExternalEmailAddress)|$($mailUser.EmailAddressPolicyEnabled)"
        }
        catch {
            HandleManagementObjectNotFoundException -ErrorRecord $_ -Identifier $guid -Operation $OPERATION_NAME
        }
    } else {
        Log "$SCRIPT_NAME Searching all $($OBJECT_CLASS_NAME) in Exchange" "INFO" "$OPERATION_NAME"
        $mailUsers = Get-MailUser -DomainController $DOMAIN_CONTROLLER -ResultSize Unlimited -ErrorAction Stop -WarningAction SilentlyContinue
        Write-Host $HEADER

        foreach ($mailUser in $mailUsers) {

            Write-Host "$($mailUser.Guid)|$($mailUser.Name)|$($mailUser.PrimarySmtpAddress)|$($mailUser.EmailAddresses)|$($mailUser.ExternalEmailAddress)|$($mailUser.EmailAddressPolicyEnabled)"
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