param (
    [string]$name,
    [string]$displayName,
    [string]$alias,
    [string]$primarySmtpAddress,
    [string]$description,
    [string]$groupType  # expects "MailUniversalSecurityGroup" or "MailUniversalDistributionGroup"
)

try {
    Log "Checking if distribution group exists: $name" "INFO" "createOp"

    $existingGroup = Get-DistributionGroup -Identity $name -ErrorAction SilentlyContinue

    if ($existingGroup) {
        Log "Group already exists: $name" "WARN" "createOp"
        Write-Host "ObjectAlreadyExists"
        return
    }

    switch ($groupType) {
        "MailUniversalSecurityGroup"     { $typeValue = "Security" }
        "MailUniversalDistributionGroup" { $typeValue = "Distribution" }
        default {
            $err = "Unsupported groupType: $groupType. Must be 'MailUniversalSecurityGroup' or 'MailUniversalDistributionGroup'"
            Log $err "ERROR" "createOp"
            Write-Host "error:$err"
            exit 1
        }
    }

    Log "Creating group: $name with type: $groupType" "INFO" "createOp"

    $params = @{
        Name                                = $name
        DisplayName                         = $displayName
        Alias                               = $alias
        Type                                = $typeValue
        PrimarySmtpAddress                  = $primarySmtpAddress
        RequireSenderAuthenticationEnabled  = $true
    }

    if ($description) {
        $params["Description"] = $description
    }

    $group = New-DistributionGroup @params

    Log "Successfully created group [$($group.DisplayName)] <$($group.PrimarySmtpAddress)> GUID: $($group.ExternalDirectoryObjectId)" "INFO" "createOp"

    # Output for midPoint
    Write-Host "name|guid"
    Write-Host "$($group.Name)|$($group.ExternalDirectoryObjectId)"
}
catch {
    $msg = $_.Exception.Message
    Log "Failed to create group [$name]: $msg" "ERROR" "createOp"
    Write-Host "error:$msg"
    exit 1
}
