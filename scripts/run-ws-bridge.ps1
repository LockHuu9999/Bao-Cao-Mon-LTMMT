<##
Run the WebSocket->TCP bridge (requires Node.js installed).
This script ensures the 'ws' package is installed locally and then starts the bridge.

Usage:
  .\run-ws-bridge.ps1          # uses defaults: WS port 8080 -> TCP localhost:5555
  .\run-ws-bridge.ps1 9090    # use 9090 as WS port
>##>

<##
Run the WebSocket->TCP bridge (requires Node.js installed).
This script ensures the 'ws' package is installed locally and then starts the bridge.

Usage:
  .\run-ws-bridge.ps1          # uses defaults: WS port 8080 -> TCP localhost:5555
  .\run-ws-bridge.ps1 9090    # use 9090 as WS port
>##>

param(
  [int]$WsPort = 8080,
  [string]$TcpHost = '127.0.0.1',
  [int]$TcpPort = 5555
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
  Write-Error "Node.js is not installed or not on PATH. Please install Node.js from https://nodejs.org and rerun this script."
  exit 1
}

# ensure node_modules/ws exists
if (-not (Test-Path .\node_modules\ws)) {
  Write-Host "Installing required npm packages (ws)..."
  npm install ws | Out-Null
}

Write-Host "Starting HTTP+WS bridge (serves ws-test.html) on http://localhost:$($WsPort) -> TCP $($TcpHost):$($TcpPort)"
# start ws-bridge-server.js via node
$serverPath = Join-Path $scriptDir 'ws-bridge-server.js'
Start-Process -FilePath node -ArgumentList "$serverPath $WsPort $TcpHost $TcpPort" -NoNewWindow
Write-Host "Bridge server started (background). Open http://localhost:$WsPort in browser or run ngrok http $WsPort to expose it." 
