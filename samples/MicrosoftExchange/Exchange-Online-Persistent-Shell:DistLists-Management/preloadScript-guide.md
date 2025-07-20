# Preload Script Guide for Exchange Online Integration

`preloadScript` Feature in the Adaptive SSH Connector for managing Exchange Online distribution groups (DistributionLists and securityEnabled Dist Lists) and mail users using persistent shell sessions.

## Purpose

When managing objects in Exchange Online via PowerShell (e.g., `EXODistributionGroup`, `EXOMailUser`), establishing connections like `Connect-ExchangeOnline` and `Connect-MgGraph` would result in immense long CRUD operations in span of 15-30seconds rendering connector unusable because default ssh shell execution is one time use. 

To avoid this, the connector supports persistent shell sessions. You can preload environment context and authenticated sessions using a script that runs once, when the shell session is first established. This result in initial slow first request and each subsequent request spans in range of 150-800ms technically only speed of which massively improve timings.



## Configuration Requirements

### 1. Enable persistent shell

In your resource xml `connectorConfiguration`, set:

```xml
<usePersistentShell>true</usePersistentShell>
```

**Make sure you're using latest version of dynamicConfiguration** with preloadScript defined, [see dynamicConfiguration.json](dynamicConfiguration.json)
```json
      "preloadScript": {
        "enabled": true,
        "value": "C:\\Users\\test\\scripts\\exchange\\preloadScript.ps1",
        "successReturnValue": "__SUCCESS_PRELOAD_SCRIPT__"
}
```

### 2. preloadScript content
**preloadScript.ps1** should contain Connect Commands that should be initialized only once like
```powershell
Connect-ExchangeOnline `
    -AppId $clientId `
    -CertificateThumbprint $certificateThumbprint `
    -Organization $organization `
    -ShowBanner:$false `
    -ShowProgress:$false `
    -LoadCmdletHelp:$false `
    -CommandName $commands

Connect-MgGraph -ClientId $clientId -TenantId $tenantId -CertificateThumbprint $certificateThumbprint -NoWelcome
```
#### Important!
* 
Preload script on successful execution must return value defined in dynamicConfiguration held in **successReturnValue** otherwise connector treat it as error and Session get terminated!
*
The connector sources the **preloadScript.ps1** into the persistent PowerShell session using dot-sourcing (. preloadScript.ps1), ensuring that the authenticated context and loaded modules remain available across subsequent script executions.

### 3. Full Configuration Example

See [PowershellScripts](PowershellScripts) for a complete working configuration.

This includes:
- Full CRUD scripts for **Distribution Groups** and **Security-enabled Distribution Groups**
- Membership management logic that uses **Microsoft Graph API** to resolve and associate users
- Integration examples combining `Connect-ExchangeOnline` and `Connect-MgGraph` in persistent sessions

These scripts demonstrate how to provision and manage Exchange Online groups with dynamic user data pulled from Graph.
