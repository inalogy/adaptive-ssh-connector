
## Table of Contents
1. [Introduction](#introduction)
2. [Capabilities and Features](#capabilities-and-features)
3. [Dynamic Schema](#dynamic-schema)
4. [Configuration]()
5. [Script design](#script-design)
6. [Build](#build)
# Introduction
  ### ssh-connector
  Standalone SSH Connector for midPoint IDM focused primarily for Microsoft Exchange provisioning
# Capabilities and Features

- Schema: YES - dynamic
- Provisioning: YES
- Live Synchronization: No
- Password: No
- Activation: No
- Script execution: No

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
```
    {
      "icfsName": "anyicfsName",
      "icfsUid": "anyicfsUid",
      "objectClass": "user",
      "createScript": "path\to\script\createScript.ps1",
      "updateScript": "path\to\script\updateScript.ps1",
      "deleteScript": "path\to\script\deleteScript.ps1",
      "searchScript": "path\to\script\searchScript.ps1",
      "attributes": [
        { // secondary attributes are optional
          "anyAttribute": {
            "required": true,
            "creatable":true,
            "updateable": true,
            "dataType": "String",
            "multivalued": false
          }
        },"anyAttribute2":{
            ...},
            ...
        {
        }
      ]
    }
```
- icfsName and icfsUid can point to the same value
### Script design
Script return values must follow this convention:
* first line must always have column attributes that match schema
* Script needs to be designed to return Constants.RESPONSE_EMPTY_ATTRIBUTE_SYMBOL -> "null" for empty attribute otherwise separator wont be able to tell if attribute is empty
* **First Attribute must be icfsUid or icfsName If you need both unique identifiers specified, first must be icfsUid followed by icfsName that match those specified in schemaConfig.json example:**
```
uniqueIdentifier|UniqueName|AnyOtherAttributes
or
UniqueIdentifier|AnyOtherAttributes
```


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
* SearchScript.ps1 output :
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
* SearchScript.ps1 output:
``` 
    smtpGuid |   smtpMail  |mailPrefix|mailBoxNickName
    UniqIdent|uniqesmtpMail|mailPrefix|null
```
* **case2:** icfsName and icfsUid will have different value that corresponds to smtpGuid column and smtpMail
### Powershell Scripts limitations
- Powershell scripts for microsoft exchange use weird UI element when importing remote session in terminal, sshj which is responsible for executing/reading output  crash since by default sshj create connection with -T flag, so it needs to be bypassed
- to bypass this every command should be imported separately
- To test this simply connect to your testing server with ssh -T name@host and execute test script
- example of Powershell script with command import:
``` 
  $commandsToImport = "Set-Mailbox", "Get-Mailbox", "Set-User"
  Import-PSSession $Session -CommandName $commandsToImport -AllowClobber > $null
``` 
### Connector Operations
* Each operation is designed in a way to work with predefined Script input/output

- ### Search Operation
  - For single account/object Query searchOp needs UID also operation should always return all attributes that are defined in schema for particular object
- ### Create Operation
  - createOp expects attributes provided by midpoint based on mappings in resource, CreateScript should return uniqueID|uniqueName or just uniqueId it depends on script and schemaConfig.json design
  - for mapping special attributes from midpoint for example \_\_NAME\_\_ or \_\_PASSWORD\_\_ to target system need to be specified in Constants and mapped with name that corresponds with expected script input parameter

- ### UpdateDelta operation
  - updateDelta process attributes, and multivalued attributes which are formatted in a way that remote powershell script know how to handle them based on Constant Prefix ADD:somevalue,REMOVE:somevalue2
  - Script is expected to return ""  if execution of script was successful any other output is considered as error message

- ### Delete operation
  - deleteOp expects Uid which is then passed into powershell script
  - Script is expected to return "" if execution of script was successful any other output is considered as error message

## Build
```
mvn clean install
```
After successful the build, you can find ssh-v1.0-connector.jar in target directory.