param(
    [string]$ExchangeGuid)

# searchScript.ps1 must always return all attributes defined in schema for particular objectClass

$columnsHeaderDefinition= "ExchangeGuid||UserPrincipalName||Email||EmailAddresses"
$commandsToImport= "Get-Mailbox"

$Session = New-PSSession -ConfigurationName Microsoft.Exchange -ConnectionUri https://ExchangeServerAddr -Authentication Kerberos
Import-PSSession $Session -CommandName $commandsToImport -AllowClobber > $null


# search for 1 mailbox based on Guid
if ($ExchangeGuid){
    $mailbox = Get-Mailbox -Identity $ExchangeGuid -ErrorAction SilentlyContinue
    if ($mailbox) {
        $email = $mailbox.PrimarySmtpAddress
        $mailboxUpn = $mailbox.UserPrincipalName
        $EmailAddresses = $mailbox.EmailAddresses
        Write-Host $columnsHeaderDefinition
        Write-Host "$ExchangeGuid||$mailboxUpn||$email||$EmailAddresses"
    }
    else {
        Write-Host "Error mailBox notfound"
    }
}

else {
    # Get all mailboxes and output their exchangeGuid and all attributes defined in schema
    $mailboxes = Get-Mailbox -ResultSize Unlimited
    Write-Host $columnsHeaderDefinition
    foreach ($mailbox in $mailboxes)
    {
        $guid = $mailbox.ExchangeGuid
        $email = $mailbox.PrimarySmtpAddress
        $EmailAddresses = $mailbox.EmailAddresses
        $mailboxUpn = $mailbox.UserPrincipalName

        Write-Host "$guid||$mailboxUpn||$email||$EmailAddresses"
    }
}

Remove-PSSession $Session > $null

