# Changelog

All notable changes to this project will be documented in this file.


## [1.2.4] - 2024-05-24
### Added
- Validation of configuration Properties
- PrivateKeyFilePath configuration property
- Private/pubKey Authentication

### Fixed
- Reverted accidental deletion of UnknownUid & AlreadyExists exception
- proper Message properties for configuration properties

### Removed
- unused BouncyCastle dependency

## [1.2.3] - 2024-05-15
### Added

- SSH_RESPONSE_TIMEOUT as configuration property
- proper Message properties for configuration properties

## [1.2.2] - 2024-04-01
### Fixed

- nullValue replace  based on connectorConfig 'scriptEmptyAttribute' for singleValue attributes


## [1.2.1] - 2024-02-27
### Fixed

- incorrect null value handling in executeQuery for multiValued attributes.


## [1.2.0] - 2024-01-09

### Added

- public release

[1.2.4]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.4
[1.2.3]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.3
[1.2.2]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.2
[1.2.1]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.1
[1.2.0]: https://github.com/inalogy/ssh-connector/releases/tag/v1.2.0
