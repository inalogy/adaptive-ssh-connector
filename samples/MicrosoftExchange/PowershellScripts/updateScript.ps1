 param(
    [string]$ExchangeGuid,
    [string]$Email,
    [string[]]$EmailAddresses
)

$commandsToImport = "Set-Mailbox", "Get-Mailbox"
$Session = New-PSSession -ConfigurationName Microsoft.Exchange -ConnectionUri http://PowershellServerAddr -Authentication Kerberos
Import-PSSession $Session -CommandName $commandsToImport -AllowClobber > $null

# example of updateScript
# .\updateScript.ps1 -ExchangeGuid "a97f7a95-299d-46d0-8687-787fa9298c2d" -email "jon.stark@protonmail.com" -EmailAddresses "ADD:smtp:jon.targaryen@protonmail.com ","REMOVE:smtp:jon.snow@protonmail.com"


# handle multivalued operation (singlevalue replace too if present) if no error occurs this script should return ""
if($EmailAddresses){


    # Split EmailAddresses into an array of operations
    $operations = $EmailAddresses -split ' '

    # Loop through the operations
    foreach($operation in $operations){
        $mailbox = Get-Mailbox -Identity $ExchangeGuid
        # Split each operation into action (ADD or REMOVE) and emailaddress
        $action, $emailaddress = $operation -split ':', 2

        # Check if email is present
        $isPresent = $mailbox.EmailAddresses -contains $emailaddress

        switch($action){

            'ADD' {
                if(!$isPresent){
                    $mailbox.EmailAddresses += $emailaddress
                    Set-Mailbox $mailbox.Identity -EmailAddresses $mailbox.EmailAddresses
                }
            }
            'REMOVE' {
                if($isPresent){
                    $mailbox.EmailAddresses = $mailbox.EmailAddresses -ne $emailaddress
                    Set-Mailbox $mailbox.Identity -EmailAddresses $mailbox.EmailAddresses
                }

            }
            default {
                #Write-Host "ERROR"
                }
            }

        }
        if ($email){
            # handle replace

            # Update the PrimarySmtpAddress
            Set-Mailbox -Identity $ExchangeGuid -PrimarySmtpAddress $email
        }
    }


# handle replace single value if $emailAddresses is not passed as argument
else {

    #check which attribute to update
    if ($email){
        # Update the mailbox name
        Set-Mailbox -Identity $ExchangeGuid -PrimarySmtpAddress $email
    }
}

Remove-PSSession $Session > $null