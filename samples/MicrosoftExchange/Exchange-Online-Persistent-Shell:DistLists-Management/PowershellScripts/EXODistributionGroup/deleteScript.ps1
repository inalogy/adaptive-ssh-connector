param (
    [string]$guid
)

try {
    Log "Attempting to delete distribution group with GUID: $guid" "INFO"

    # Optional: resolve the SMTP or name first for logging/debugging
    $group = Get-DistributionGroup | Where-Object { $_.ExternalDirectoryObjectId -eq $guid }

    if (-not $group) {
        Log "No distribution group found with GUID: $guid" "WARN"
        Write-Host "UnknownUid"
        exit 1
    }

    Log "Found group [$($group.Name)] <$($group.PrimarySmtpAddress)> for deletion." "INFO"

    Remove-DistributionGroup -Identity $group.Identity -Confirm:$false -ErrorAction Stop -BypassSecurityGroupManagerCheck

    Log "Successfully deleted group [$($group.DisplayName)] GUID: [$guid]" "INFO"

}
catch {
    $msg = $_.Exception.Message
    Log "Failed to delete group with GUID [$guid]: $msg" "ERROR"
    Write-Host "FATAL_ERROR: $msg"
}


