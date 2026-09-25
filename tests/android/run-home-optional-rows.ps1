param(
    [Parameter(Mandatory=$true)][ValidatePattern('^emulator-[0-9]+$')][string]$Serial,
    [ValidatePattern('^[a-zA-Z][a-zA-Z0-9_.]+$')][string]$Package = 'com.boop.shieldoverlay',
    [string]$Sdk = "$env:LOCALAPPDATA/Android/Sdk",
    [string]$OutputDirectory,
    [string]$EvidenceLabel = 'current'
)
$ErrorActionPreference = 'Stop'
$RepoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if (-not $OutputDirectory) { $OutputDirectory = [IO.Path]::GetFullPath((Join-Path $RepoRoot '../home-row-audit')) }
$Adb = Join-Path $Sdk 'platform-tools/adb.exe'
$BuildTools = Join-Path $Sdk 'build-tools/36.0.0'
$AndroidJar = Join-Path $Sdk 'platforms/android-36/android.jar'
function Invoke-Checked([string]$Tool, [string[]]$Arguments) {
    & $Tool @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Tool exited $LASTEXITCODE" }
}
$Qemu = & $Adb -s $Serial shell getprop ro.kernel.qemu
if ($LASTEXITCODE -ne 0 -or $Qemu.Trim() -ne '1') { throw 'This probe runs only on a verified Android emulator.' }
$Installed = & $Adb -s $Serial shell pm path $Package
if ($LASTEXITCODE -ne 0 -or -not ($Installed -match '^package:')) { throw "Install the BOOP APK on $Serial first." }
New-Item -ItemType Directory -Force -Path $OutputDirectory, "$OutputDirectory/classes", "$OutputDirectory/dex" | Out-Null
$Manifest = (Get-Content -Raw -LiteralPath "$PSScriptRoot/home-optional-rows-manifest.xml").Replace('com.boop.shieldoverlay', $Package)
[IO.File]::WriteAllText("$OutputDirectory/AndroidManifest.xml", $Manifest)
Invoke-Checked 'javac' @('-encoding','UTF-8','-source','17','-target','17','-cp',$AndroidJar,'-d',"$OutputDirectory/classes","$PSScriptRoot/HomeOptionalRowsProbe.java")
Invoke-Checked 'jar' @('cf',"$OutputDirectory/classes.jar",'-C',"$OutputDirectory/classes",'.')
Invoke-Checked 'java' @('-cp',"$BuildTools/lib/d8.jar",'com.android.tools.r8.D8','--lib',$AndroidJar,'--min-api','29','--output',"$OutputDirectory/dex","$OutputDirectory/classes.jar")
Invoke-Checked "$BuildTools/aapt2.exe" @('link','-I',$AndroidJar,'--manifest',"$OutputDirectory/AndroidManifest.xml",'-o',"$OutputDirectory/probe-unsigned.apk")
Invoke-Checked 'jar' @('uf',"$OutputDirectory/probe-unsigned.apk",'-C',"$OutputDirectory/dex",'classes.dex')
if (-not (Test-Path -LiteralPath "$OutputDirectory/probe.jks")) {
    Invoke-Checked 'keytool' @('-genkeypair','-keystore',"$OutputDirectory/probe.jks",'-storepass','android','-keypass','android','-alias','probe','-dname','CN=Emulator test fixture','-keyalg','RSA','-validity','365')
}
Invoke-Checked "$BuildTools/zipalign.exe" @('-f','4',"$OutputDirectory/probe-unsigned.apk","$OutputDirectory/probe.apk")
Invoke-Checked "$BuildTools/apksigner.bat" @('sign','--ks',"$OutputDirectory/probe.jks",'--ks-pass','pass:android','--key-pass','pass:android',"$OutputDirectory/probe.apk")
Invoke-Checked $Adb @('-s',$Serial,'install','-r',"$OutputDirectory/probe.apk")
$Result = & $Adb -s $Serial shell am instrument -w -e target_package $Package 'local.boop.homeaudit/local.boop.homeaudit.HomeOptionalRowsProbe\$ProbeInstrumentation'
$Result | Tee-Object -FilePath "$OutputDirectory/$EvidenceLabel-test.txt"
foreach ($Phase in @('initial','row1','row2','returned','tall-room','tall-no-rows')) {
    Invoke-Checked $Adb @('-s',$Serial,'pull',"/sdcard/Android/data/local.boop.homeaudit/files/$Phase.png","$OutputDirectory/$EvidenceLabel-$Phase.png")
}
if (-not (($Result -join "`n").Contains('PASS optional rows'))) { throw 'Home optional-row navigation regression failed; see captured evidence.' }
