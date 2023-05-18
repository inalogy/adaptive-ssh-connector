# ssh-connector
Standalone SSH Connector for midPoint IDM

## Set PowerShell as default shell on Windows
Make sure that the powershell is default shell,
otherwise this will not work due to style of argument passing between cmd and powershell
because midPoint will sent them as for powershell like "$name = value;" and this will result in error in cmd
### PowerShell command:
```powershell
New-ItemProperty -Path "HKLM:\SOFTWARE\OpenSSH" -Name DefaultShell -Value "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe" -PropertyType String -Force
```
More info: https://github.com/PowerShell/Win32-OpenSSH/wiki/DefaultShell

## Dynamic Schema
- Schema is generated from schemaConfig.json
- Schema supports arbitrary number of ObjectClasses and their attributes
- Every ObjectClass defined in schemaConfig.json must have the following parameters:
- 
```
    {
      "icfsName": "anyicfsName",
      "icfsUid": "anyicfsUid",
      "objectClass": "user",
      "createScript": "path\to\script\createScript.ps2",
      "updateScript": "path\to\script\updateScript.ps3",
      "deleteScript": "path\to\script\deleteScript.ps4",
      "searchScript": "path\to\script\searchScript.ps1",
      "attributes": [
        {
          "anyAttribute": {
            "required": true,
            "creatable":true,
            "updateable": true,
            "dataType": "String",
            "multivalued": false
          }
        },
        {.....
        }
      ]
    }
```
- icfsName and icfsUid can point to the same value
### Scripts
Script return values must follow this convention:
* Script needs to be designed to return null for empty attribute otherwise separator wont be able to tell if attribute is empty
* first line must have column attributes
* **First Attribute must be icfsUid or icfsName If you need both unique identifiers specified, first must be icfsUid followed by icfsName example:**
```
uniqueIdentifier|UniqueName|AnyOtherAttributes
or
UniqueIdentifier|AnyOtherAttributes
```


* UniqueIdentifier must be first attribute Followed by UniqueName
* if icfsName and icfsUid in schemaConfig.json point to same value, script should return only UniqueName followed by any other attributes defined in schema
example:
* **case1**:
  * schemaConfig.json
```
    "icfsName": "smtpMail",
    "icfsUid": "smtpMail",
    "attributes": [
       "smtpMailbox: {....},
       "mailBoxNickName": {....}
       ]
  ``` 
* Script output:
``` 
    smtpMail|smtpMailbox|mailBoxNickName
    UniqIdent|exampleMail|exampleNick
```
* **case1:** icfsName and icfsUid will have same value that corresponds to smtpMail column
---
* **case2**:
  * schemaConfig.json
```
    "icfsName": "smtpGuid",
    "icfsUid": "smtpMail",
    "attributes": [
       "mailPrefix: {....},
       "mailBoxNickName": {....}
       ]
``` 
* Script output:
``` 
    smtpGuid |smtpMail     |mailPrefix|mailBoxNickName
    UniqIdent|uniqesmtpMail|mailPrefix|examplemailBoxNickName
```
* **case2:** icfsName and icfsUid will have different value that corresponds to smtpGuid column and smtpMail
