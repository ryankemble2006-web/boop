# Source contract for the standalone local-ADB cleanup path.
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$service = Get-Content (Join-Path $root 'src/uk/local/eastenders/WatchNowService.java') -Raw
$manifest = Get-Content (Join-Path $root 'AndroidManifest.xml') -Raw
$bridge = Get-Content (Join-Path $root 'src/uk/local/eastenders/PlayerBridgeClient.java') -Raw
$control = Get-Content (Join-Path $root 'src/uk/local/eastenders/PlayerControlService.java') -Raw
if ($service -notmatch 'PlayerBridgeClient') { throw 'Accessibility cleanup must use the app-local bridge' }
if ($service -match 'APPLICATION_DETAILS_SETTINGS') { throw 'Cleanup must not open Android TV App info' }
if ($manifest -notmatch 'android.permission.INTERNET') { throw 'Loopback ADB requires INTERNET socket permission' }
if ($manifest -notmatch 'PlayerControlService') { throw 'Standalone APK must package its private cleanup controller' }
if ($bridge -notmatch 'uk.local.eastenders.PlayerControlService') { throw 'EastEnders bridge must bind to its own APK' }
if ($control -notmatch 'iplayer-local-adb.key') { throw 'Cleanup must use the app-private ADB identity' }
Write-Host 'PASS: standalone EastEnders local-ADB cleanup is wired'
