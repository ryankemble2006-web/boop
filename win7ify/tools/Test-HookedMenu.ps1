param([Parameter(Mandatory=$true)][string]$Exe)
$ErrorActionPreference = 'Stop'
if ($env:GITHUB_ACTIONS -ne 'true') { throw 'This test is only for a disposable GitHub runner.' }
$Exe = (Resolve-Path $Exe).Path
$folder = Join-Path $env:LOCALAPPDATA 'BOOP/Win7ify'
$receipt = Join-Path $env:GITHUB_WORKSPACE 'integration-receipts'
New-Item -ItemType Directory -Force $receipt | Out-Null
Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class BoopTaskbarProbe {
 [DllImport("user32.dll", CharSet=CharSet.Unicode)] public static extern IntPtr FindWindow(string cls, string name);
}
'@
if ([BoopTaskbarProbe]::FindWindow('Shell_TrayWnd', $null) -eq [IntPtr]::Zero) {
    Start-Process (Join-Path $env:WINDIR 'explorer.exe') | Out-Null
    for ($i=0; $i -lt 40; $i++) {
        if ([BoopTaskbarProbe]::FindWindow('Shell_TrayWnd', $null) -ne [IntPtr]::Zero) { break }
        Start-Sleep -Milliseconds 250
    }
}
if ([BoopTaskbarProbe]::FindWindow('Shell_TrayWnd', $null) -eq [IntPtr]::Zero) {
    'SKIPPED normal Explorer-hook test: hosted runner does not expose a desktop taskbar. Standalone vendor menu lifecycle was tested separately. Yoga remains unverified.' | Tee-Object -FilePath (Join-Path $receipt 'explorer-hook.txt')
    exit 0
}
function Invoke-MenuCommand([string]$Command) {
    $p = Start-Process $Exe -ArgumentList $Command -PassThru
    if (!$p.WaitForExit(360000)) { throw "Menu test timed out: $Command" }
    $p.Refresh()
    $result = Get-Content (Join-Path $folder 'last-command.json') -Raw | ConvertFrom-Json
    $result | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $receipt ('hook-' + $Command.TrimStart('-') + '-' + [guid]::NewGuid().ToString('N') + '.json'))
    Write-Host $result.Report
    if ($result.ProcessId -ne $p.Id -or $p.ExitCode -ne 0 -or $result.ExitCode -ne 0) { throw "Real hooked-menu command failed: $Command" }
    $p.Dispose()
}
$completed = $false
try {
    Invoke-MenuCommand '--install-start-menu'
    Invoke-MenuCommand '--activate-start-menu'
    Invoke-MenuCommand '--undo-start-menu'
    if (Test-Path (Join-Path $folder 'open-shell-session-v1.json')) { throw 'Recovery journal still present after hooked-menu Undo.' }
    'PASS actual Win7ify install/configure/start, repeated open, graceful stop and owned uninstall through the normal Explorer-hook path on the hosted Windows Server desktop. No screenshot or appearance test. This is not Yoga Windows 11 Insider acceptance.' | Tee-Object -FilePath (Join-Path $receipt 'explorer-hook.txt')
    $completed = $true
}
finally {
    if (!$completed) { try { Invoke-MenuCommand '--undo-start-menu' } catch { Write-Warning "Recovery probe: $_" } }
    if (Test-Path (Join-Path $folder 'logs')) { Copy-Item (Join-Path $folder 'logs') (Join-Path $receipt 'hook-runner-logs') -Recurse -Force }
}
