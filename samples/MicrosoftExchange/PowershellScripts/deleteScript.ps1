param(
    [string]$ExchangeGuid
)

# example of delete script
# .\deleteScript.ps1 -ExchangeGuid "a97f7a95-299d-46d0-8687-787fa9298c2d"


$commandsToImport = "Remove-Mailbox"

$Session = New-PSSession -ConfigurationName Microsoft.Exchange -ConnectionUri  https://ExchangeServerAddr -Authentication Kerberos
Import-PSSession $Session -CommandName commandsToImport -AllowClobber > $null

# Remove mailbox imidiatelly from DB
Remove-Mailbox -Identity $ExchangeGuid -Permanent $true -Confirm:$false

Remove-PSSession $Session > $null
