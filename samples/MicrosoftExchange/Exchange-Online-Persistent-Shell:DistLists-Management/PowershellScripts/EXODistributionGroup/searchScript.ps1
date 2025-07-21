param (
    [string]$name,
    [string]$guid
)

$columnsHeaderDefinition = "name|guid|displayName|alias|description|groupType|members|emailAddresses|primarySmtpAddress"

function Output-Group($g) {
    $name = $g.Name
    $guid = $g.ExternalDirectoryObjectId
    $displayName = $g.DisplayName
    $alias = $g.Alias
    $desc = $g.Description
    $groupType = $g.RecipientTypeDetails
    $emailAddresses = $g.emailAddresses -join " "
    $primarySmtpAddress = $g.primarySmtpAddress
    $members = (Get-DistributionGroupMember -Identity $g.Identity -ResultSize Unlimited -ErrorAction SilentlyContinue | ForEach-Object {
        $_.ExternalDirectoryObjectId
    }) -join " "

    Write-Host "$name|$guid|$displayName|$alias|$desc|$groupType|$members|$emailAddresses|$primarySmtpAddress"
}

try {
    if ($guid) {
        $group = Get-DistributionGroup | Where-Object { $_.ExternalDirectoryObjectId -eq $guid }
        if ($group) {
            Write-Host $columnsHeaderDefinition
            Output-Group $group
        } else {
            Write-Host "UnknownUid"
            exit 0
        }
    }
    elseif ($name) {
        $group = Get-DistributionGroup -Identity $name -ErrorAction SilentlyContinue
        if ($group) {
            Write-Host $columnsHeaderDefinition
            Output-Group $group
        } else {
            Write-Host "UnknownUid"
            exit 0
        }
    }
    else {
        $allGroups = Get-DistributionGroup -ResultSize Unlimited
        if ($allGroups.Count -gt 0) {
            Write-Host $columnsHeaderDefinition
            foreach ($g in $allGroups) {
                Output-Group $g
            }
        }
    }
}
catch {
    $msg = $_.Exception.Message
    Log "Failed during search: $msg" "ERROR" "createOp"
    exit 1
}
