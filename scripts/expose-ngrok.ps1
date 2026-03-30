<#
Run ngrok tcp 5555 and show the public host:port for clients to connect.
This script requires `ngrok` to be available on PATH (or adjust the $ngrokPath variable).

It will start ngrok in the foreground so you can see logs; it also attempts to parse the "Forwarding" line
and copy host:port to clipboard (Windows). To stop ngrok press Ctrl+C.
#>

$ngrokPath = "ngrok"  # change to .\tools\ngrok.exe if using local binary
$localPort = 5555

Write-Host "Starting ngrok tcp $localPort ..."

try {
    # Start ngrok as a job and capture output
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $ngrokPath
    $startInfo.Arguments = "tcp $localPort"
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true

    $proc = New-Object System.Diagnostics.Process
    $proc.StartInfo = $startInfo
    $proc.Start() | Out-Null

    Write-Host "ngrok started. Waiting for 'Forwarding' line..."

    while (-not $proc.HasExited) {
        $line = $proc.StandardOutput.ReadLine()
        if ($null -eq $line) { Start-Sleep -Milliseconds 100; continue }
        Write-Host $line
        if ($line -match "Forwarding\s+tcp://([^:]+):(\d+) ->") {
            $host = $matches[1]
            $port = $matches[2]
            $public = "$host:$port"
            Write-Host "Public endpoint: $public"
            # copy to clipboard if available
            try {
                Set-Clipboard -Value $public -ErrorAction Stop
                Write-Host "(copied to clipboard)"
            } catch {
                Write-Host "Could not copy to clipboard; you can manually copy: $public"
            }
            break
        }
    }

    Write-Host "ngrok running. Press Ctrl+C to stop."
    # forward any remaining stdout/stderr to console
    while (-not $proc.HasExited) {
        $line = $proc.StandardOutput.ReadLine()
        if ($null -ne $line) { Write-Host $line }
        Start-Sleep -Milliseconds 100
    }
} catch {
    Write-Error "Failed to start ngrok: $_"
}
