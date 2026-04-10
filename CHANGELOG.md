# Changelog

All notable changes to this project will be documented in this file.

## [1.3.2] - 2026-04-10
### Added
- Comprehensive test framework (143 tests) with embedded Apache MINA SSHD server
  - Unit tests for response parsing, command building, schema loading, config validation
  - Integration tests for exec mode and persistent shell mode (preload, dispose, CRUD cycle)
  - Sample configuration validation for all 4 existing samples
- TestNG groups support: `mvn test` runs all, `mvn test -DtestGroups=unit` for unit only

### Changed
- slf4j version bump from 2.0.5 to 2.0.17
- TestNG dependency scoped to test
- Added test dependencies: Apache MINA SSHD 2.15.0, EdDSA 0.3.0 (test scope only, not shipped in connector JAR)

### Removed
- Legacy test classes (TestClient, TestProcessor, TestSshResponseHandler, TestUniversalSchemaHandler)

## [1.3.1] - 2025-12-14
### Added
- new connectorConfiguration (native) Properties:
    - remoteCharset: enforce different charset encoding for SSH connection (persistent mode)See [RemoteCharset ➜](samples/MicrosoftExchange/Exchange-OnPrem-Persistent-Shell/sshd_config-enforce-encoding.md)
        - with connectorConfiguration **'usePersistentShell'** == true
    - connectTimeout: define initial connect timeout in seconds


- Full configuration Sample for persistentShell Microsoft Exchange onPrem:
    - mailUser
    - mailContact
    - mailBoxCloud
    - mailBoxOnpRem


- added new 'dynamicConfiguration.json' property **disposeScript**
    - used for cleanup/closing opened Exchange sessions if fatal error occurs that result in midPoint dispose of connector See [Base Script DisposeSession Method](samples/MicrosoftExchange/Exchange-OnPrem-Persistent-Shell/baseScript.ps1)

### Fixed
- NonPersistent shell configuration resulted in 'sessionIsUsedUp' error.

## [1.3.0] - 2025-07-01
### Added
- new dynamicConfiguration.json Properties:
  - generalFatalErrorMessage: Usable if scripts throw unexpected error in CRUD operations
  - preloadScript: absolute path to the preloadScript works only in conjunction with resource configuration **'usePersistentShell'** == true
- Persistent shell functionality (Tested only on Windows):
    - by setting resource configuration **'usePersistentShell'** connector will create reusable session, in addition to this if dynamicConfiguration.json has **'preloadScript'** defined, preloadScript get executed each time new session is spawned this results in massive performance boost in Powershell Connect-Exchange, Connect-MsGraph since they're executed only once per persistent session, consecutive requests like executeQuery by id take 200-600ms. See [Exchange Online preload script guide ➜](samples/MicrosoftExchange/Exchange-Online-Persistent-Shell:DistLists-Management/preloadScript-guide.md)
- sshj version bump to 0.40.0

### Fixed
- unknownUid error handling for searchOp

## [1.2.4] - 2025-05-24
### Added
- Validation of configuration Properties
- PrivateKeyFilePath configuration property
- Private/pubKey Authentication

### Fixed
- Reverted accidental deletion of UnknownUid & AlreadyExists exception
- proper Message properties for configuration properties

### Removed
- unused BouncyCastle dependency

## [1.2.3] - 2025-05-15
### Added

- SSH_RESPONSE_TIMEOUT as configuration property
- proper Message properties for configuration properties

## [1.2.2] - 2025-04-01
### Fixed

- nullValue replace  based on connectorConfig 'scriptEmptyAttribute' for singleValue attributes


## [1.2.1] - 2025-02-27
### Fixed

- incorrect null value handling in executeQuery for multiValued attributes.


## [1.2.0] - 2025-01-09

### Added

- public release

[1.3.2]: https://github.com/inalogy/ssh-connector/releases/tag/v1.3.2
[1.3.1]: https://github.com/inalogy/ssh-connector/releases/tag/v1.3.1
[1.3.0]: https://github.com/inalogy/ssh-connector/releases/tag/v1.3.0
[1.2.4]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.4
[1.2.3]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.3
[1.2.2]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.2
[1.2.1]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.1
[1.2.0]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.0
