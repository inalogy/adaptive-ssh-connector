param(
    [string]$name,
    [string[]]$type
)

Write-Host "type:"
foreach ($i in $type)
{
    Write-Host $i
}

Write-Host
Write-Host "name:"
Write-Host $name 
