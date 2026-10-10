param(
    [string]$Branch = "shield-v325-live",
    [string]$ShieldIp = "10.58.1.248:5555",
    [string]$ArtifactName = ""
)

$ErrorActionPreference = "Stop"

$Adb = "C:\Users\ryank\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $Adb)) {
    $Adb = "adb"
}

Write-Host "Looking up latest GitHub Actions run on branch: $Branch..." -ForegroundColor Cyan
$runJson = gh run list -R ryankemble2006-web/boop -b $Branch --limit 1 --json databaseId,status,conclusion | ConvertFrom-Json

if (-not $runJson -or $runJson.Count -eq 0) {
    Write-Error "No runs found for branch $Branch"
    exit 1
}

$runId = $runJson[0].databaseId
Write-Host "Found run ID: $runId (Status: $($runJson[0].status), Conclusion: $($runJson[0].conclusion))" -ForegroundColor Green

$downloadDir = Join-Path $env:TEMP "boop-shield-build-$runId"
if (Test-Path $downloadDir) { Remove-Item -Recurse -Force $downloadDir }
New-Item -ItemType Directory -Path $downloadDir -Force | Out-Null

Write-Host "Downloading signed build artifact from GitHub..." -ForegroundColor Cyan
if ($ArtifactName -ne "") {
    gh run download $runId -n $ArtifactName -D $downloadDir -R ryankemble2006-web/boop
} else {
    gh run download $runId -D $downloadDir -R ryankemble2006-web/boop
}

$apk = Get-ChildItem -Path $downloadDir -Recurse -Filter "*.apk" | Select-Object -First 1
if (-not $apk) {
    Write-Error "No APK file found in downloaded artifacts at $downloadDir"
    exit 1
}

Write-Host "Located APK: $($apk.FullName) ($([math]::Round($apk.Length / 1MB, 2)) MB)" -ForegroundColor Green

Write-Host "Connecting to Shield at $ShieldIp..." -ForegroundColor Cyan
& $Adb connect $ShieldIp

Write-Host "Installing APK on Shield..." -ForegroundColor Cyan
& $Adb -s $ShieldIp install -r $apk.FullName

Write-Host "Verifying running activity on Shield..." -ForegroundColor Cyan
& $Adb -s $ShieldIp shell dumpsys activity activities | Select-String "mResumedActivity"

Write-Host "Deployment complete!" -ForegroundColor Green
