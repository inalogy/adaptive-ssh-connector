param (
    [string]$name,
    [string]$guid,
    [string]$displayName,
    [string]$description,
    [string]$mailNickname,
    [string]$primarySmtpAddress,
    [string[]]$members
)

try {
    # === Group lookup ===
    $group = $null
    if ($guid) {
        Log "Looking up group by GUID: $guid" "DEBUG" "updateOp"
        $group = Get-DistributionGroup | Where-Object { $_.ExternalDirectoryObjectId -eq $guid }
    } elseif ($name) {
        Log "Looking up group by name: $name" "DEBUG" "updateOp"
        $group = Get-DistributionGroup -Identity $name -ErrorAction SilentlyContinue
    }

    if (-not $group) {
        Log "Group not found (name=$name, guid=$guid)" "ERROR" "updateOp"
        Write-Host "UnknownUid"
        exit 1
    }

    Log "Updating group [$($group.DisplayName)] <$($group.PrimarySmtpAddress)> GUID: $($group.ExternalDirectoryObjectId)" "INFO" "updateOp"

    # === Build update payload ===
    $updateParams = @{}

    if ($displayName -and $displayName -ne $group.DisplayName) {
        $updateParams["DisplayName"] = $displayName
    }

    if ($mailNickname -and $mailNickname -ne $group.Alias) {
        $updateParams["Alias"] = $mailNickname
    }

    if ($description -and $description -ne $group.Notes) {
        $updateParams["Description"] = $description
    }

    if ($primarySmtpAddress -and $primarySmtpAddress -ne $group.primarySmtpAddress) {
        $updateParams["primarySmtpAddress"] = $primarySmtpAddress
    }

    if ($updateParams.Count -gt 0) {
        Log "Applying attribute changes: $($updateParams.Keys -join ', ')" "INFO" "updateOp"
        Set-DistributionGroup -Identity $group.Identity @updateParams
        Log "Group attributes updated successfully." "INFO" "updateOp"
    } else {
        Log "No attribute changes detected; skipping update." "INFO" "updateOp"
    }

    # === Handle group Name change separately ===
    if ($name -and $name -ne $group.Name) {
        Log "Changing group Name from '$($group.Name)' to '$name'" "INFO" "updateOp"
        Set-DistributionGroup -Identity $group.Identity -Name $name
        Log "Group Name updated successfully." "INFO" "updateOp"

        # Refresh group object so Identity stays valid
        $group = Get-DistributionGroup -Identity $name
    }

    # === Handle member updates ===
    if ($members) {
        Log "Processing membership changes for group [$($group.Identity)]" "INFO" "updateOp"

        $opsParsed = @{
            add    = @()
            remove = @()
        }

        foreach ($op in $members) {
            $action, $memberGuid = $op -split ":", 2
            switch ($action.ToUpper()) {
                "ADD"    { $opsParsed.add += $memberGuid }
                "REMOVE" { $opsParsed.remove += $memberGuid }
                default  { Log "Ignoring invalid member operation: $op" "WARN" "updateOp" }
            }
        }

        foreach ($memberGuid in $opsParsed.add) {
            try {
                Log "Adding member [$memberGuid] to group [$($group.Identity)]" "INFO" "updateOp"
                Add-DistributionGroupMember -Identity $group.Identity -Member $memberGuid -Confirm:$false -BypassSecurityGroupManagerCheck -ErrorAction Stop
            } catch {
                Log "Failed to add member [$memberGuid] to group [$($group.Identity)]: $($_.Exception.Message)" "ERROR" "updateOp"
            }
        }

        foreach ($memberGuid in $opsParsed.remove) {
            try {
                Log "Removing member [$memberGuid] from group [$($group.Identity)]" "INFO" "updateOp"
                Remove-DistributionGroupMember -Identity $group.Identity -Member $memberGuid -Confirm:$false -BypassSecurityGroupManagerCheck -ErrorAction Stop
            } catch {
                Log "Failed to remove member [$memberGuid] from group [$($group.Identity)]: $($_.Exception.Message)" "ERROR" "updateOp"
            }
        }
    }

    Log "Update operation completed for group GUID: $($group.ExternalDirectoryObjectId)" "INFO" "updateOp"
}
catch {
    $err = $_.Exception.Message
    Log "Exception during group update: $err" "ERROR" "updateOp"
    Write-Host $err
    exit 1
}
