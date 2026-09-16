# This file also keeps the shortcut workflow path filter exercising the root integration contract.
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$service = Get-Content (Join-Path $root 'src/uk/local/eastenders/WatchNowService.java') -Raw
$manifest = Get-Content (Join-Path $root 'AndroidManifest.xml') -Raw
if ($service -notmatch 'APPLICATION_DETAILS_SETTINGS') { throw 'Cold cleanup must use Android TV App info instead of a private ADB approval flow' }
if ($service -notmatch 'Force stop') { throw 'Cleanup must target the visible Android TV Force stop control' }
if ($service -notmatch 'guidedactions_item_title') { throw 'Cleanup must identify the Android TV confirmation action by its stable view id' }
if ($service -match 'PlayerBridgeClient') { throw 'Accessibility cleanup must not depend on the ADB bridge client' }
if ($manifest -match 'PlayerControlService') { throw 'Standalone shortcut must not expose the obsolete ADB cleanup broker' }
if ($manifest -match 'android.permission.INTERNET') { throw 'Settings-based force stop must not need INTERNET permission' }
Write-Host 'PASS: Shield-native Settings force-stop cleanup is wired without ADB approval'
