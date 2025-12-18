# === FILE HAS TO BE RENAMED TO envVars.ps1 ===

# === VARIABLES VALUES WHICH ARE / CAN BE DIFFERENT PER ENV ===
$LogLevel = "WARN"  # Options: DEBUG, INFO, WARN, ERROR, NONE
$DOMAIN_CONTROLLER = "domain.controller.com"
$EXCHANGE_SERVER_ADDRESS = "exchange.address.com"
# IF HTTPS does not work can be change to http but it is not best practice (http / https)
$METHOD = "http"