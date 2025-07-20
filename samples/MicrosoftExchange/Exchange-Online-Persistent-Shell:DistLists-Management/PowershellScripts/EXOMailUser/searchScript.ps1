param (
    [Parameter(Mandatory = $false)]
    [Alias("name")]
    [string]$guid
)


# Old method: get groups for one user from Graph directly (fast for single user)
function Get-UserGroupsDirect {
    param ([string]$uid)

    $groups = @()

    try {
        $memberOf = Get-MgUserMemberOf -UserId $uid -All -ErrorAction Stop

        foreach ($entry in $memberOf) {
            $odataType = $entry.AdditionalProperties["@odata.type"]
            if ($odataType -eq "#microsoft.graph.group") {
                $mailEnabled = $entry.AdditionalProperties["mailEnabled"]
                $groupTypes  = $entry.AdditionalProperties["groupTypes"]
                $security    = $entry.AdditionalProperties["securityEnabled"]

                if ($mailEnabled -eq $true -and -not ($groupTypes -contains "Unified")) {
                    $groups += $entry.Id
                }
            }
        }
    } catch {
        Log "Failed to retrieve groups for user [$uid]: $_" "ERROR"
    }

    return $groups
}

# Bulk method: create group -> members map for all groups
function Get-AllGroups {
    $groupMembersMap = @{}
    $allGroups = Get-DistributionGroup -ResultSize Unlimited

    foreach ($g in $allGroups) {
        try {
            $groupId = $g.ExternalDirectoryObjectId
            $memberIds = Get-DistributionGroupMember -Identity $g.Identity -ResultSize Unlimited -ErrorAction SilentlyContinue | ForEach-Object {
                $_.ExternalDirectoryObjectId
            }

            $groupMembersMap[$groupId] = $memberIds
        } catch {
            Log "Failed to fetch members for group $($g.Identity): $_" "ERROR"
        }
    }

    return $groupMembersMap
}

# Bulk method: find user groups from hash map
function Get-UserGroupsFromMap {
    param (
        [string]$uid,
        [hashtable]$groupMembersMap
    )

    $matchingGroups = @()
    foreach ($groupId in $groupMembersMap.Keys) {
        if ($groupMembersMap[$groupId] -contains $uid) {
            $matchingGroups += $groupId
        }
    }
    return $matchingGroups
}

# Main function to get user(s)
function Get-GraphUser {
    param (
        [string]$UserId,
        [hashtable]$GroupMap
    )

    if ($UserId) {
        try {
            Log "Fetching user by ID: $UserId" "INFO"
            $user = Get-MgUser -UserId $UserId -ErrorAction Stop
            # Use direct method for single user
            $groups = Get-UserGroupsDirect -uid $UserId
            $user | Add-Member -MemberType NoteProperty -Name Groups -Value ($groups -join " ")
            Log "User [$UserId] fetched with [$($groups.Count)] group(s)" "INFO"
            return @($user)
        } catch {
            Log "User not found or failed to fetch: $UserId. Error: $_" "ERROR"
            Write-Host "UnknownUid"
            exit 1
        }
    } else {
        $users = @()
        try {
            Log "Fetching all users from Graph" "INFO"
            $allUsers = Get-MgUser -All
            foreach ($u in $allUsers) {
                $groups = Get-UserGroupsFromMap -uid $u.Id -groupMembersMap $GroupMap
                $u | Add-Member -MemberType NoteProperty -Name Groups -Value ($groups -join " ")
                $users += $u
            }
            Log "Fetched [$($users.Count)] users with group info" "INFO"
        } catch {
            $msg = $_.Exception.Message
            Write-Host "$midPointGeneralFatalError during search: $msg"
            Log "Failed to list users: $_" "ERROR"
            exit 1
        }
        return $users
    }
}

# === Main Execution ===
try {
    if (-not $guid) {
        $groupMap = Get-AllGroups
    }

    $users = Get-GraphUser -UserId $guid -GroupMap $groupMap
    $headerPrinted = $false

    foreach ($u in $users) {
        if (-not $headerPrinted) {
            Write-Host "name|guid|memberOf"
            $headerPrinted = $true
        }

        $groupIds = $u.Groups -split " " | Sort-Object -Unique
        if ($null -eq $groupIds -or $groupIds.Count -eq 0) {
            $groupList = $midPointNullValue  # optional fallback
        } else {
            $groupList = ($groupIds -join " ")
        }

        Write-Host "$($u.UserPrincipalName)|$($u.Id)|$groupList"
    }
} catch {
    $msg = $_.Exception.Message
    Write-Host "$midPointGeneralFatalError during search: $msg"
    Log "Failed during search: $msg" "ERROR"
}

