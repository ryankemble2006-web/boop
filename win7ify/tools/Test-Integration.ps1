param([Parameter(Mandatory=$true)][string]$Exe)
$ErrorActionPreference = 'Stop'
if ($env:GITHUB_ACTIONS -ne 'true') { throw 'This destructive installation cycle is restricted to a fresh, disposable GitHub runner. It must not run on a personal desktop.' }
$Exe = (Resolve-Path $Exe).Path
$folder = Join-Path $env:LOCALAPPDATA 'BOOP/Win7ify'
$owned = Join-Path $env:ProgramFiles 'BOOP Win7ify Open-Shell'
$receipt = Join-Path $env:GITHUB_WORKSPACE 'integration-receipts'
New-Item -ItemType Directory -Force $receipt | Out-Null
if (Test-Path 'HKLM:/Software/OpenShell/OpenShell') { throw 'Pre-existing Open-Shell detected. Disposable test refuses to modify it.' }
if (Test-Path (Join-Path $folder 'open-shell-session-v1.json')) { throw 'Pre-existing BOOP journal detected. Refusing integration test.' }
function Invoke-Boop([string]$Command, [int]$Expected=0) {
    $p = Start-Process $Exe -ArgumentList $Command -PassThru
    if (!$p.WaitForExit(360000)) { throw "Timed out: $Command. The installer was not force-killed." }
    $p.Refresh()
    $result = Get-Content (Join-Path $folder 'last-command.json') -Raw | ConvertFrom-Json
    if ($result.ProcessId -ne $p.Id -or $result.Command -ne $Command) { throw 'Missing or stale diagnostic receipt.' }
    $safeName = $Command.TrimStart('-') + '-' + [guid]::NewGuid().ToString('N')
    $result | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $receipt ($safeName + '.json'))
    Write-Host $result.Report
    if ($p.ExitCode -ne $Expected -or $result.ExitCode -ne $Expected) { throw "$Command returned $($p.ExitCode), expected $Expected" }
    $p.Dispose()
}
Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class BoopMenuProbe {
  [DllImport("user32.dll", CharSet=CharSet.Unicode)] public static extern IntPtr FindWindow(string cls, string name);
  [DllImport("user32.dll")] public static extern bool IsWindowVisible(IntPtr window);
  [DllImport("user32.dll")] public static extern uint GetWindowThreadProcessId(IntPtr window, out uint process);
  [DllImport("user32.dll")] public static extern bool PostMessage(IntPtr window, uint message, IntPtr wp, IntPtr lp);
}
'@
$menuProcess = $null
$success = $false
try {
    Invoke-Boop '--verify-payload'
    Invoke-Boop '--diagnostics-selftest' 1
    if ((Get-Content (Join-Path $folder 'last-command.txt') -Raw) -notmatch 'BOOP E298') { throw 'Diagnostic error code was not recorded.' }
    Invoke-Boop '--setup-start-menu'
    if (!(Test-Path (Join-Path $owned 'StartMenu.exe'))) { throw 'Official menu did not install at the owned location.' }
    if (!(Test-Path (Join-Path $owned 'Skins/Windows Aero.skin7'))) { throw 'Windows 7 menu skin is absent.' }
    foreach ($file in @('ClassicExplorer32.dll','ClassicExplorer64.dll','ClassicIE_32.exe','ClassicIE_64.exe','Update.exe')) {
        if (Test-Path (Join-Path $owned $file)) { throw "Unrequested component installed: $file" }
    }
    $original = (Get-FileHash (Join-Path $folder 'open-shell-profile-v1.json')).Hash
    Invoke-Boop '--setup-start-menu'
    if ((Get-FileHash (Join-Path $folder 'open-shell-profile-v1.json')).Hash -ne $original) { throw 'Repeated setup overwrote the original profile backup.' }
    # A hosted server is not the user's Windows 11 desktop. Exercise the real menu
    # out of process, without claiming Explorer-hook or appearance acceptance.
    $menuProcess = Start-Process (Join-Path $owned 'StartMenu.exe') -ArgumentList '-nohook' -PassThru
    $window = [IntPtr]::Zero
    for ($i=0; $i -lt 100; $i++) {
        $candidate = [BoopMenuProbe]::FindWindow('OpenShell.CMenuContainer', $null)
        [uint32]$ownerProcess = 0
        if ($candidate -ne [IntPtr]::Zero) { [void][BoopMenuProbe]::GetWindowThreadProcessId($candidate, [ref]$ownerProcess) }
        if ($ownerProcess -eq $menuProcess.Id -and [BoopMenuProbe]::IsWindowVisible($candidate)) { $window = $candidate; break }
        Start-Sleep -Milliseconds 200
    }
    if ($window -eq [IntPtr]::Zero) { throw 'Installed Open-Shell did not create its standalone menu window.' }
    'PASS real vendor menu window created in standalone/nohook mode. No image or appearance comparison. Explorer-hook behavior on Yoga remains unverified.' | Tee-Object -FilePath (Join-Path $receipt 'menu-runtime.txt')
    [void][BoopMenuProbe]::PostMessage($window, 16, [IntPtr]::Zero, [IntPtr]::Zero)
    if (!$menuProcess.WaitForExit(15000)) { throw 'Standalone menu did not close cleanly.' }
    if ($menuProcess.ExitCode -ne 0) { throw 'Standalone menu exited abnormally.' }
    $menuProcess.Dispose(); $menuProcess = $null
    Invoke-Boop '--undo-start-menu'
    if (Test-Path 'HKLM:/Software/OpenShell/OpenShell') {
        $registered = Get-ItemPropertyValue 'HKLM:/Software/OpenShell/OpenShell' 'Path' -ErrorAction SilentlyContinue
        if ($registered) { throw 'Owned installation is still registered after Undo.' }
    }
    if (Test-Path (Join-Path $folder 'open-shell-session-v1.json')) { throw 'Completed recovery journal was not cleared.' }
    if (Test-Path (Join-Path $folder 'open-shell-profile-v1.json')) { throw 'Completed profile backup was not cleared.' }
    Invoke-Boop '--undo-start-menu'
    'PASS pinned installer, effective profile export, repeated setup, standalone menu lifecycle, owned uninstall, repeat undo and diagnostic failure receipt.' | Set-Content (Join-Path $receipt 'integration.txt')
    $success = $true
}
finally {
    if ($null -ne $menuProcess) {
        $window = [BoopMenuProbe]::FindWindow('OpenShell.CMenuContainer', $null)
        [uint32]$ownerProcess = 0
        if ($window -ne [IntPtr]::Zero) { [void][BoopMenuProbe]::GetWindowThreadProcessId($window, [ref]$ownerProcess) }
        if ($ownerProcess -eq $menuProcess.Id) { [void][BoopMenuProbe]::PostMessage($window, 16, [IntPtr]::Zero, [IntPtr]::Zero); [void]$menuProcess.WaitForExit(5000) }
        $menuProcess.Dispose()
    }
    if (!$success -and (Test-Path (Join-Path $folder 'open-shell-session-v1.json'))) {
        try { Invoke-Boop '--undo-start-menu' } catch { Write-Warning "Cleanup also reported: $_" }
    }
    if (Test-Path (Join-Path $folder 'logs')) { Copy-Item (Join-Path $folder 'logs') (Join-Path $receipt 'runner-logs') -Recurse -Force }
}
