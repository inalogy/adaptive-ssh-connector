## Enforcing encoding on Windows Servers


##### Warning: Using `remoteCharset` works only with `PERSISTENT Shell mode!` In `nonPersistent` mode `ForceCommand` with `-NoExit` breaks connector functionality


#### In persistent shell mode its possible to encounter issue where encoding from ssh connector doesn't match with encoding on Windows server, to enforce this setting u need to configure sshd config with this variable use encodingCode (1250) based on your needs:


Edit
` C:\ProgramData\ssh\sshd_config`
```
ForceCommand powershell.exe -NoLogo -NoExit -Command "[Console]::OutputEncoding = [Console]::InputEncoding = [System.Text.Encoding]::GetEncoding(1250)"
```


in connectorConfig.xml match this with:
```xml
<gen629:remoteCharset>Windows-1250</gen629:remoteCharset>
```