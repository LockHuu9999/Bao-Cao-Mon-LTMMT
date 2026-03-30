
# PowerShell script to compile and run the RandomMatchServer and two TestClient instances
# Requires JDK (javac/java) installed and in PATH.

$src = Join-Path $PSScriptRoot 'src'
$out = Join-Path $PSScriptRoot 'out'
if (-Not (Test-Path $out)) { New-Item -ItemType Directory -Path $out | Out-Null }

Write-Host "Compiling (UTF-8)..."

# Build a list of .java files (PowerShell expands wildcard differently)
$files = Get-ChildItem -Path $src -Filter *.java | ForEach-Object { $_.FullName }
if ($files.Count -eq 0) { Write-Host "No .java files found in $src"; exit 1 }

# javac option -encoding UTF-8 avoids unmappable character errors on Windows
$javacArgs = @("-encoding", "UTF-8", "-d", $out) + $files

& javac @javacArgs
if ($LASTEXITCODE -ne 0) { Write-Host "Compile failed"; exit 1 }

Write-Host "Starting RandomMatchServer in new window..."
## Before attempting to start processes, ensure javac/java are available
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
	Write-Host "ERROR: 'javac' not found in PATH. Please open a NEW PowerShell after installing JDK, or ensure javac is on PATH.";
	exit 1
}

# open new cmd window for server so it keeps running visibly
$cmdServer = "java -cp `"$out`" RandomMatchServer & pause"
Start-Process -FilePath cmd -ArgumentList "/c", $cmdServer
Start-Sleep -Seconds 1

Write-Host "Starting two TestClient windows..."
$cmdClient = "java -cp `"$out`" TestClient & pause"
Start-Process -FilePath cmd -ArgumentList "/c", $cmdClient
Start-Process -FilePath cmd -ArgumentList "/c", $cmdClient

Write-Host "Started server and 2 TestClient windows. Type MOVE r c in a TestClient to send moves, or type quit to exit a client."

