# Source contract for the standalone local-ADB cleanup path.
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$service = Get-Content (Join-Path $root 'src/uk/local/casualty/WatchNowService.java') -Raw
$manifest = Get-Content (Join-Path $root 'AndroidManifest.xml') -Raw
$bridge = Get-Content (Join-Path $root 'src/uk/local/casualty/PlayerBridgeClient.java') -Raw
$control = Get-Content (Join-Path $root 'src/uk/local/casualty/PlayerControlService.java') -Raw
if ($service -notmatch 'PlayerBridgeClient') { throw 'Accessibility cleanup must use the app-local bridge' }
if ($service -match 'APPLICATION_DETAILS_SETTINGS') { throw 'Cleanup must not open Android TV App info' }
if ($manifest -notmatch 'android.permission.INTERNET') { throw 'Loopback ADB requires INTERNET socket permission' }
if ($manifest -notmatch 'PlayerControlService') { throw 'Standalone APK must package its private cleanup controller' }
if ($bridge -notmatch 'uk.local.casualty.PlayerControlService') { throw 'Casualty bridge must bind to its own APK' }
if ($bridge -match 'uk.local.eastenders.PlayerControlService') { throw 'Casualty must not bind to EastEnders' }
if ($control -notmatch 'iplayer-local-adb.key') { throw 'Cleanup must use the app-private ADB identity' }
Write-Host 'PASS: standalone Casualty local-ADB cleanup is wired'
