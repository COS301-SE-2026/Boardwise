# Get-Content ".env" | ForEach-Object {
#     $line = $_.Trim()
#     if ($line -and $line -notmatch "^\s*#" -and $line -match "^([^=]+)=(.*)$") {
#         $key = $matches[1].Trim()
#         $value = $matches[2].Trim()
#         Write-Host "$key = $value"
#     }
# }

# run-backend.ps1
Write-Host "Loading environment variables..." -ForegroundColor Cyan

Get-Content ".env" | ForEach-Object {
    $line = $_.Trim()
    if ($line -and $line -notmatch "^\s*#" -and $line -match "^([^=]+)=(.*)$") {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
        Write-Host "  Set $key" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "Verifying DB_URL..." -ForegroundColor Cyan
Write-Host $env:DB_URL

Write-Host "Starting Boardwise backend..." -ForegroundColor Cyan
Set-Location -Path "backend"

.\mvnw.cmd spring-boot:run

if ($LASTEXITCODE -ne 0) {
    Write-Host "Backend failed to start." -ForegroundColor Red
    exit 1
}