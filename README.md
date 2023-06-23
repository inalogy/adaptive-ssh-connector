
## Table of Contents
1. [Introduction](#introduction)
2. [Capabilities and Features](#capabilities-and-features)
3. [Dynamic Schema](#dynamic-schema)
4. [Configuration](#configuration)
5. [Script design](#script-design)
6. [Customisation](#customisation)
7. [JavaDoc](#javadoc)
8. [Build](#build)
9. [TODO](#todo)
10. [Special Thanks](#special-thanks)
# Introduction
  ### ssh-connector
  Standalone SSH Connector for midPoint IDM customised for Microsoft Exchange provisioning

version 0.1.0.1
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
because midPoint will send them as for powershell like "$name = value;" and this will result in error in cmd
### PowerShell command:
```powershell
New-ItemProperty -Path "HKLM:\SOFTWARE\OpenSSH" -Name DefaultShell -Value "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe" -PropertyType String -Force
```
More info: https://github.com/PowerShell/Win32-OpenSSH/wiki/DefaultShell

## Dynamic Schema
- Schema is generated from schemaConfig.json
- Schema File should be properly secured and protected with appropriate permissions
- Schema supports arbitrary number of ObjectClasses and their attributes
- **Any modifications of schemaFile need to be carefully planned and tested, before any modification of schemaFile resource should be put into maintenance mode.**
- script Paths containg white space need to be properly escaped 'with single quotes ' otherwise powershell reads them till first white space -> "C:\\Users\\**' PS Scripts'**\searchScript.ps1"
- Every ObjectClass defined in schemaConfig.json must have the following parameters:
```
    {
      "icfsName": "anyicfsName",
      "icfsUid": "anyicfsUid",
      "objectClass": "user",
      "createScript": "path\\to\\script\\createScript.ps1",
      "updateScript": "path\\to\\'script folder'\\updateScript.ps1",
      "deleteScript": "path\\to\'script folder'\\deleteScript.ps1",
      "searchScript": "path\\to\\'script folder'\\searchScript.ps1",
      "attributes": [
        { // secondary attributes are optional
          "anyAttribute": {
            "required": true,
            "creatable":true,
            "updateable": true,
            "dataType": "String",
            "multivalued": false
          },
          "anyAttribute2":{
            ...},
          ...
        }
      ]
    }
```
- icfsName and icfsUid can point to the same value
### Script design
Script return values must follow this convention:
* first line must always have column attributes that match schema -> this applies only for searchScript and createScript
* Script needs to be designed to return Constants.RESPONSE_EMPTY_ATTRIBUTE_SYMBOL -> "null" for empty attribute otherwise separator won't be able to tell if attribute is empty
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
### Configuration
- Set the usual username, password, and host address, also specify absolute file path for the schemaFile
### Connector Operations
* Each operation is designed in a way to work with predefined Script input/output

- ### Search Operation
  - For single account/object Query searchOp needs UID also operation should always return all attributes that are defined in schema for particular object
- ### Create Operation
  - createOp expects attributes provided by midpoint based on mappings in resource, CreateScript should return uniqueID|uniqueName or just uniqueId it depends on script and schemaConfig.json design
  - for mapping special attributes from midpoint for example \_\_NAME\_\_ or \_\_PASSWORD\_\_ to target system need to be specified in Constants and mapped with name that corresponds with expected script input parameter
  - for createOp it is recommended to define Constant that should be present in response when objectAlreadyExists occurs

- ### UpdateDelta operation
  - updateDelta process attributes, and multivalued attributes which are formatted in a way that remote powershell script know how to handle them based on Constant Prefix ADD:somevalue,REMOVE:somevalue2
  - Script is expected to return ""  if execution of script was successful any other output is considered as error message
  
- ### Delete operation
  - deleteOp expects Uid which is then passed into powershell script
  - Script is expected to return "" if execution of script was successful any other output is considered as error message
  - for deleteOp it is recommended to define Constant that should be present in response when objectNotFound occurs

## Customisation
- Ssh Response column separator  and new line separator can be changed in class utils/Constants
- Also, Ssh Response that define successful execution for updateDelta and DeleteOp are defined in utils/Constants -> default empty string
- RESPONSE_COLUMN_SEPARATOR should never be equal to RESPONSE_NEW_LINE_SEPARATOR

## JavaDoc
- JavaDoc can be generated locally by this command:
```bash
mvn clean javadoc:javadoc
```
## Build
```
mvn clean install
```
## Build without Tests
```
mvn clean install -DskipTests=True
```
After successful build, you can find ssh-v1.0-connector.jar in target directory.

## TODO
- Private/public key authentication
- Response handler for different type of script output e.g. json
- Feature that will optionally allow to store schema file within the connector jar
- Test on Unix/Linux based systems
- Script validator that validate SchemaFile/SchemaType obj. with script return values
## Special Thanks
This project is inspired by and owes a debt of gratitude to the [Evolveum SSH Connector](https://github.com/Evolveum/connector-ssh) project.

# Status
Tested only on Microsoft Windows server with powershell version 5.1.17763

Ssh Connector is intended for production use. Tested with MidPoint version 4.6. The connector was introduced as a contribution to midPoint project by Inalogy and is not officially supported by Evolveum. If you need support, please contact info@inalogy.com.