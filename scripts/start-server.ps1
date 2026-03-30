<#
Compile (optional) and start RandomMatchServer in the project root.
Usage:
  .\start-server.ps1            # compile then start server (background)
  .\start-server.ps1 -NoCompile # start server without recompiling
#>
param(
    [switch]$NoCompile
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir
Set-Location ..    # project root

if (-not $NoCompile) {
    Write-Host "Compiling Java sources..."
    & javac -encoding UTF-8 -d build_classes src\*.java
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Compilation failed. Fix errors and try again."
        exit $LASTEXITCODE
    }
}

Write-Host "Starting RandomMatchServer..."
# Start Java process (detached). Adjust -WindowStyle if you want a new visible window.
Start-Process -FilePath "java" -ArgumentList "-cp .\build_classes RandomMatchServer" -WorkingDirectory (Get-Location)
Write-Host "RandomMatchServer started (background). Check logs in the server terminal or process list." 
