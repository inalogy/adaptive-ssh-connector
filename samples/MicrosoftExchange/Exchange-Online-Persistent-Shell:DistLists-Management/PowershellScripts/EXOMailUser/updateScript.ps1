param (
    [string]$name,
    [string]$guid,
    [string[]]$memberOf
)

function Update-DistributionGroupMemberships {
    param (
        [string]$userId,
        [string[]]$memberOfOps
    )

    $toUpdate = @{
        add = @()
        remove = @()
    }

    foreach ($entry in $memberOfOps) {
        $action, $group = $entry -split ':', 2
        switch ($action.ToUpper()) {
            "ADD"    { $toUpdate.add += $group }
            "REMOVE" { $toUpdate.remove += $group }
            default  { Log "Ignoring unrecognized operation: $entry" "WARN" "updateOp" }
        }
    }

    Log "Starting group membership update for user GUID: $userId" "INFO" "updateOp"
    Log "Groups to add: $($toUpdate.add -join ', ')" "DEBUG" "updateOp"
    Log "Groups to remove: $($toUpdate.remove -join ', ')" "DEBUG" "updateOp"

    foreach ($group in $toUpdate.add) {
        try {
            Log "Adding user [$userId] to group [$group]" "INFO" "updateOp"
            Add-DistributionGroupMember -Identity $group -Member $userId -Confirm:$false -BypassSecurityGroupManagerCheck -ErrorAction Stop
        } catch {
            $errMsg = $_.Exception.Message
            Log "Failed to add user [$userId] to group [$group]: $errMsg" "ERROR" "updateOp"
        }
    }

    foreach ($group in $toUpdate.remove) {
        try {
            Log "Removing user [$userId] from group [$group]" "INFO" "updateOp"
            Remove-DistributionGroupMember -Identity $group -Member $userId -Confirm:$false -BypassSecurityGroupManagerCheck -ErrorAction Stop
        } catch {
            $errMsg = $_.Exception.Message
            Log "Failed to remove user [$userId] from group [$group]: $errMsg" "ERROR" "updateOp"
        }
    }

    Log "Completed group membership update for user GUID: $userId" "INFO" "updateOp"
}

Update-DistributionGroupMemberships -userId $guid -memberOfOps $memberOf
