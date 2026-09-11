param([string]$Sdk = "$env:LOCALAPPDATA/Android/Sdk")
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$bt = Join-Path $Sdk 'build-tools/36.0.0'
$platform = Join-Path $Sdk 'platforms/android-36/android.jar'
function Check { if ($LASTEXITCODE -ne 0) { throw "Build command failed: $LASTEXITCODE" } }
New-Item -ItemType Directory -Force build,build/classes,build/dex,build/generated,build/tests,res/drawable-nodpi | Out-Null

# Package the reviewed resources unchanged; never regenerate placeholder artwork.
foreach ($name in @('banner', 'icon')) {
    if (!(Test-Path "res/drawable-nodpi/$name.png" -PathType Leaf)) {
        throw "Missing approved $name artwork resource"
    }
}
& javac -d build/tests src/uk/local/eastenders/Routes.java src/uk/local/eastenders/ClickGate.java src/uk/local/eastenders/UiPolicy.java src/uk/local/eastenders/LaunchPolicy.java tests/RouteTest.java tests/ClickGateTest.java tests/AutoplayPolicyTest.java tests/LaunchPolicyTest.java
Check
& java -cp build/tests uk.local.eastenders.RouteTest
Check
& java -cp build/tests uk.local.eastenders.ClickGateTest
Check
& java -cp build/tests uk.local.eastenders.AutoplayPolicyTest
Check
& java -cp build/tests uk.local.eastenders.LaunchPolicyTest
Check
& "$PSScriptRoot/tests/BuildAssetsTest.ps1" -Sdk $Sdk
& "$bt/aapt2.exe" compile --dir res -o build/resources.zip
Check
& "$bt/aapt2.exe" link -o build/base.apk --manifest AndroidManifest.xml -I $platform --java build/generated build/resources.zip
Check
$sources = @(Get-ChildItem src -Filter '*.java' -Recurse | ForEach-Object FullName)
& javac --release 8 -classpath $platform -d build/classes @sources
Check
$classes = @(Get-ChildItem build/classes -Filter '*.class' -Recurse | ForEach-Object FullName)
& "$bt/d8.bat" --release --min-api 23 --lib $platform --output build/dex @classes
Check
Copy-Item build/base.apk build/unsigned.apk -Force
& jar uf build/unsigned.apk -C build/dex classes.dex
Check
& "$bt/zipalign.exe" -f -p 4 build/unsigned.apk build/aligned.apk
Check

# A new key belongs only to this standalone launcher. Keep it for future updates.
New-Item -ItemType Directory -Force signing | Out-Null
if (!(Test-Path signing/password.txt)) {
    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    [Convert]::ToBase64String($bytes) | Set-Content signing/password.txt -NoNewline
}
$env:EASTENDERS_SIGN_PASSWORD = Get-Content signing/password.txt -Raw
try {
    if (!(Test-Path signing/eastenders.p12)) {
        & keytool -genkeypair -keystore signing/eastenders.p12 -storetype PKCS12 -alias eastenders -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=EastEnders Personal Launcher' -storepass:env EASTENDERS_SIGN_PASSWORD -keypass:env EASTENDERS_SIGN_PASSWORD
        Check
    }
    & "$bt/apksigner.bat" sign --ks signing/eastenders.p12 --ks-key-alias eastenders --ks-pass "file:signing/password.txt" --out build/EastEnders-1.5.apk build/aligned.apk
    Check
} finally { Remove-Item Env:EASTENDERS_SIGN_PASSWORD -ErrorAction SilentlyContinue }
& "$bt/apksigner.bat" verify --verbose --print-certs build/EastEnders-1.5.apk
Check
& "$bt/aapt2.exe" dump badging build/EastEnders-1.5.apk
Check
Get-FileHash build/EastEnders-1.5.apk -Algorithm SHA256
