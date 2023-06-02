 param(
    [string]$name,
    [string]$email,
    [string]$password
)

 # example of powershell script for creating mailbox
$columnsHeaderDefinition = "ExchangeGuid|UserPrincipalName" # this must match the schema! also separator must match Constants.RESPONSE_COLUMN_SEPARATOR
$commandsToImport = "New-Mailbox"

$Session = New-PSSession -ConfigurationName Microsoft.Exchange -ConnectionUri https://ExchangeServerAddr -Authentication Kerberos
Import-PSSession $Session -CommandName $commandsToImport -AllowClobber > $null


$password = "hardcoded password"
$securePassword = ConvertTo-SecureString $password -AsPlainText -Force
# Create mailbox
$mailbox = New-Mailbox -Name $name -UserPrincipalName "$name@example.net" -PrimarySmtpAddress $email -Password $securePassword
$mailboxName = $mailbox.UserPrincipalName
$guid = $mailbox.ExchangeGuid

Write-Host $columnsHeaderDefinition
Write-Host "$guid|$mailboxName"

Remove-PSSession $Session > $null

