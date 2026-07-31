# run-ai.ps1
Write-Host "Loading environment variables..." -ForegroundColor Cyan

Get-Content ".env" | ForEach-Object {
    $line = $_.Trim()
    if ($line -and $line -notmatch "^\s*#" -and $line -match "^([^=]+)=(.*)$") {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
        Write-Host "  Loaded $key" -ForegroundColor Gray
    }
}

Write-Host "Starting Boardwise AI Gateway..." -ForegroundColor Cyan
Set-Location -Path "ai"

python -m uvicorn app.main:app --reload --port 8000

if ($LASTEXITCODE -ne 0) {
    Write-Host "AI Gateway failed to start." -ForegroundColor Red
    exit 1
}