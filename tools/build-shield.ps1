[CmdletBinding()]
param(
    [switch]$BuildOnly,
    [string]$ReleaseBranch,
    [ValidateRange(1, 120)]
    [int]$WaitTimeoutMinutes = 30
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-CheckedNative {
    param(
        [Parameter(Mandatory = $true)][string]$Executable,
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [Parameter(Mandatory = $true)][string]$FailureMessage
    )

    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$FailureMessage (exit code $LASTEXITCODE)"
    }
}

function Get-Sha256 {
    param([Parameter(Mandatory = $true)][string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Test-ShieldApk {
    param(
        [Parameter(Mandatory = $true)][string]$ApkPath,
        [Parameter(Mandatory = $true)][int]$VersionCode,
        [Parameter(Mandatory = $true)][string]$VersionName,
        [Parameter(Mandatory = $true)][string]$BuildTools,
        [string]$ExpectedCertificateSha256
    )

    $aapt = Join-Path $BuildTools 'aapt.exe'
    $zipalign = Join-Path $BuildTools 'zipalign.exe'
    $apksigner = Join-Path $BuildTools 'apksigner.bat'
    foreach ($tool in @($aapt, $zipalign, $apksigner)) {
        if (-not (Test-Path -LiteralPath $tool -PathType Leaf)) {
            throw "Android build tool is missing: $tool"
        }
    }

    $badging = & $aapt dump badging $ApkPath 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'aapt could not read the local APK.' }
    $badgingText = $badging -join [Environment]::NewLine
    if ($badgingText -notmatch "package: name='com\.boop\.shieldoverlay'.*versionCode='$VersionCode'.*versionName='$([regex]::Escape($VersionName))'") {
        throw "APK package or version metadata does not match com.boop.shieldoverlay $VersionName ($VersionCode)."
    }

    Invoke-CheckedNative -Executable $zipalign -Arguments @('-c', '-v', '4', $ApkPath) -FailureMessage 'APK alignment verification failed.'
    $signature = & $apksigner verify --print-certs $ApkPath 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed.' }
    $signatureText = $signature -join [Environment]::NewLine
    if ($signatureText -notmatch 'Signer #1 certificate SHA-256 digest:') {
        throw 'The APK has no verifiable signer certificate.'
    }
    if ($ExpectedCertificateSha256 -and
        $signatureText -notmatch "Signer #1 certificate SHA-256 digest: $([regex]::Escape($ExpectedCertificateSha256))") {
        throw 'The APK certificate does not match the permanent BOOP signing certificate.'
    }
    return $badgingText
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'
if (-not (Test-Path -LiteralPath $gradleWrapper -PathType Leaf)) {
    throw "Gradle wrapper is missing: $gradleWrapper"
}

$snapshots = @(
    Get-ChildItem -LiteralPath (Join-Path $repoRoot 'release-snapshots') -Directory |
        Where-Object { $_.Name -match '^shield-v\d+$' } |
        Sort-Object { [int]($_.Name -replace '^shield-v', '') }
)
if ($snapshots.Count -eq 0) { throw 'No versioned Shield snapshots were found.' }
$snapshot = $snapshots[-1]
$versionFromDirectory = [int]($snapshot.Name -replace '^shield-v', '')
$snapshotRoot = $snapshot.FullName
$appGradle = Get-Content -LiteralPath (Join-Path $snapshotRoot 'shield-app\build.gradle') -Raw
$codeMatch = [regex]::Match($appGradle, '(?m)^\s*versionCode\s+(\d+)')
$nameMatch = [regex]::Match($appGradle, '(?m)^\s*versionName\s+[''"]([^''"]+)[''"]')
if (-not $codeMatch.Success -or -not $nameMatch.Success) {
    throw "Could not read versionCode/versionName from $($snapshot.Name)\shield-app\build.gradle."
}
$versionCode = [int]$codeMatch.Groups[1].Value
$versionName = $nameMatch.Groups[1].Value
if ($versionCode -ne $versionFromDirectory -or $versionName -ne "1.2.$versionCode-shield") {
    throw "Snapshot identity mismatch: folder $($snapshot.Name), versionCode $versionCode, versionName $versionName."
}

$java = Get-Command java.exe -ErrorAction SilentlyContinue
$javaHome = $env:JAVA_HOME
if (-not $javaHome -or -not (Test-Path -LiteralPath (Join-Path $javaHome 'bin\java.exe'))) {
    if (-not $java) { throw 'Java 17 is required. Install Temurin/Adoptium 17 or set JAVA_HOME.' }
    $javaHome = Split-Path -Parent (Split-Path -Parent $java.Source)
}
if (-not (Test-Path -LiteralPath (Join-Path $javaHome 'bin\java.exe'))) {
    throw "JAVA_HOME does not contain java.exe: $javaHome"
}
$env:JAVA_HOME = $javaHome

$androidRoot = $env:ANDROID_SDK_ROOT
if (-not $androidRoot) { $androidRoot = $env:ANDROID_HOME }
if (-not $androidRoot -and $env:LOCALAPPDATA) {
    $candidate = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
    if (Test-Path -LiteralPath $candidate -PathType Container) { $androidRoot = $candidate }
}
if (-not $androidRoot -or -not (Test-Path -LiteralPath $androidRoot -PathType Container)) {
    throw 'Android SDK not found. Set ANDROID_HOME or ANDROID_SDK_ROOT to an installed SDK 36.'
}
$env:ANDROID_HOME = $androidRoot
$env:ANDROID_SDK_ROOT = $androidRoot
$buildTools = Join-Path $androidRoot 'build-tools\36.0.0'
if (-not (Test-Path -LiteralPath (Join-Path $androidRoot 'platforms\android-36') -PathType Container)) {
    throw "Android SDK platform android-36 is missing under $androidRoot."
}

$wrapperVersion = & $gradleWrapper --version 2>&1
if ($LASTEXITCODE -ne 0 -or ($wrapperVersion -join [Environment]::NewLine) -notmatch 'Gradle 9\.6\.0') {
    throw 'The pinned Gradle 9.6.0 wrapper could not start.'
}
Write-Host (($wrapperVersion | Select-Object -First 8) -join [Environment]::NewLine)

$aar = Join-Path $snapshotRoot 'app\libs\sherpa-onnx-1.13.7.aar'
if (-not (Test-Path -LiteralPath $aar -PathType Leaf)) {
    throw "Bundled Sherpa AAR is missing: $aar"
}
$vendorDirectory = Join-Path $snapshotRoot 'local-maven\com\boop\vendor\sherpa-onnx\1.13.7'
New-Item -ItemType Directory -Path $vendorDirectory -Force | Out-Null
Copy-Item -LiteralPath $aar -Destination (Join-Path $vendorDirectory 'sherpa-onnx-1.13.7.aar') -Force
$pom = '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>com.boop.vendor</groupId><artifactId>sherpa-onnx</artifactId><version>1.13.7</version><packaging>aar</packaging></project>'
[System.IO.File]::WriteAllText(
    (Join-Path $vendorDirectory 'sherpa-onnx-1.13.7.pom'),
    $pom,
    [System.Text.UTF8Encoding]::new($false)
)

$git = 'git'
$checkoutBranch = (& $git -C $repoRoot branch --show-current).Trim()
$commit = (& $git -C $repoRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or -not $checkoutBranch -or $commit -notmatch '^[0-9a-f]{40}$') {
    throw 'Run this command from a named Git branch with a valid HEAD commit.'
}
$branch = if ($ReleaseBranch) { $ReleaseBranch } else { $checkoutBranch }
$outputDirectory = Join-Path $repoRoot "boop-build\shield-v$versionCode\$commit"
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
$localApk = Join-Path $outputDirectory "BOOP-Shield-v$versionCode-local.apk"
$receiptPath = Join-Path $outputDirectory 'build-receipt.json'

$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("boop-shield-sign-" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempRoot -Force | Out-Null
$resolvedTempRoot = [System.IO.Path]::GetFullPath($tempRoot)
$resolvedTempParent = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
if (-not $resolvedTempRoot.StartsWith($resolvedTempParent, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw 'Refusing to use a temporary keystore path outside the system temp directory.'
}
$keystore = Join-Path $resolvedTempRoot 'boop-local-only.jks'
$password = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
$keytool = Join-Path $javaHome 'bin\keytool.exe'
if (-not (Test-Path -LiteralPath $keytool -PathType Leaf)) { throw "keytool is missing under $javaHome." }

$signingNames = @('BOOP_SIGNING_STORE_FILE', 'BOOP_DEV_STORE_PASSWORD', 'BOOP_DEV_KEY_PASSWORD')
$priorSigningEnvironment = @{}
foreach ($name in $signingNames) {
    $priorSigningEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}
$releaseCreated = $false
$releaseTag = $null

try {
    Invoke-CheckedNative -Executable $keytool -Arguments @(
        '-genkeypair', '-noprompt', '-keystore', $keystore, '-storetype', 'JKS',
        '-storepass', $password, '-alias', 'boop-dev', '-keypass', $password,
        '-keyalg', 'RSA', '-keysize', '2048', '-validity', '10000',
        '-dname', 'CN=BOOP disposable local build'
    ) -FailureMessage 'Could not create a temporary local signing key.'
    [Environment]::SetEnvironmentVariable('BOOP_SIGNING_STORE_FILE', $keystore, 'Process')
    [Environment]::SetEnvironmentVariable('BOOP_DEV_STORE_PASSWORD', $password, 'Process')
    [Environment]::SetEnvironmentVariable('BOOP_DEV_KEY_PASSWORD', $password, 'Process')

    Write-Host "Running Shield unit tests locally for $($snapshot.Name)..."
    Invoke-CheckedNative -Executable $gradleWrapper -Arguments @(
        '-p', $snapshotRoot, ':assistant-lib:testDebugUnitTest', '--no-daemon', '--stacktrace'
    ) -FailureMessage 'Local Shield unit tests failed.'

    Write-Host "Building Shield APK locally for $($snapshot.Name)..."
    Invoke-CheckedNative -Executable $gradleWrapper -Arguments @(
        '-p', $snapshotRoot, ':shield-app:assembleDebug', '--no-daemon', '--stacktrace'
    ) -FailureMessage 'Local Shield APK build failed.'

    $builtApk = Join-Path $snapshotRoot 'shield-app\build\outputs\apk\debug\shield-app-debug.apk'
    if (-not (Test-Path -LiteralPath $builtApk -PathType Leaf)) { throw "Gradle did not produce $builtApk." }
    $null = Test-ShieldApk -ApkPath $builtApk -VersionCode $versionCode -VersionName $versionName -BuildTools $buildTools
    Copy-Item -LiteralPath $builtApk -Destination $localApk -Force
    $apkHash = Get-Sha256 -Path $localApk
    $receipt = [ordered]@{
        commit = $commit
        branch = $branch
        package = 'com.boop.shieldoverlay'
        versionCode = $versionCode
        versionName = $versionName
        localApk = [System.IO.Path]::GetFileName($localApk)
        localApkSha256 = $apkHash
        gradleVersion = '9.6.0'
        javaVersion = '17'
        localUnitTests = 'passed'
        createdUtc = [DateTimeOffset]::UtcNow.ToString('o')
    }
    [System.IO.File]::WriteAllText(
        $receiptPath,
        ($receipt | ConvertTo-Json -Depth 4),
        [System.Text.UTF8Encoding]::new($false)
    )
    Write-Host "Local APK verified: $localApk"
    Write-Host "Local APK SHA-256: $apkHash"

    if ($BuildOnly) {
        Write-Host 'BuildOnly requested; no GitHub release or signature request was created.'
        return
    }

    if ($branch -notmatch '^shield-v\d+-live$') {
        throw "GitHub signing is limited to Shield release branches. Current signing branch is $branch."
    }
    $status = & $git -C $repoRoot status --porcelain
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect Git working tree before signing.' }
    if ($status) { throw 'Signing requires a clean Git working tree so GitHub signs this exact committed source.' }
    $remoteRef = & $git -C $repoRoot ls-remote origin "refs/heads/$branch"
    if ($LASTEXITCODE -ne 0) { throw "Could not check origin/$branch." }
    $remoteCommit = (($remoteRef -split '\s+')[0]).Trim()
    if ($remoteCommit -ne $commit) { throw "origin/$branch is at $remoteCommit, while local HEAD is $commit. Push/sync this exact commit before requesting its signature." }

    Invoke-CheckedNative -Executable 'gh' -Arguments @('auth', 'status', '--hostname', 'github.com') -FailureMessage 'GitHub CLI authentication is required to request the permanent signature.'
    $originUrl = (& $git -C $repoRoot remote get-url origin).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Could not read the origin URL.' }
    $repoMatch = [regex]::Match($originUrl, 'github\.com[:/](?<repo>[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+?)(?:\.git)?$')
    if (-not $repoMatch.Success) { throw "Could not determine a GitHub repository from origin: $originUrl" }
    $repo = $repoMatch.Groups['repo'].Value

    $shortCommit = $commit.Substring(0, 8)
    $randomTag = [guid]::NewGuid().ToString('N').Substring(0, 8)
    $releaseTag = "codex-local-sign-v$versionCode-$shortCommit-$randomTag"
    $triggerTag = "codex-sign-trigger-v$versionCode-$shortCommit-$randomTag"
    Write-Host "Uploading the locally built APK to temporary draft $releaseTag for signing..."
    Invoke-CheckedNative -Executable 'gh' -Arguments @(
        'release', 'create', $releaseTag, $localApk, $receiptPath,
        '--repo', $repo, '--draft', '--target', $commit,
        '--title', "Temporary BOOP Shield signer handoff $releaseTag",
        '--notes', 'Temporary handoff of a locally built APK for permanent signing. Delete after successful download.'
    ) -FailureMessage 'Could not create the temporary GitHub draft release.'
    $releaseCreated = $true
    $releaseId = (& gh api "repos/$repo/releases/tags/$releaseTag" --jq '.id').Trim()
    if ($LASTEXITCODE -ne 0 -or -not $releaseId) { throw "Could not resolve the temporary draft release ID for $releaseTag." }

    Invoke-CheckedNative -Executable $git -Arguments @('-C', $repoRoot, 'tag', $triggerTag, $commit) -FailureMessage 'Could not create the temporary signature trigger tag.'
    Invoke-CheckedNative -Executable $git -Arguments @('-C', $repoRoot, 'push', 'origin', "refs/tags/$triggerTag") -FailureMessage 'Could not push the temporary signature trigger tag.'

    $runId = $null
    $deadline = [DateTimeOffset]::UtcNow.AddMinutes($WaitTimeoutMinutes)
    $expectedTitle = "Sign BOOP Shield tag $triggerTag"
    while ([DateTimeOffset]::UtcNow -lt $deadline -and -not $runId) {
        $runsJson = & gh run list --repo $repo --workflow build-wall-shield-split.yml --limit 50 --json databaseId,headSha,event,displayTitle,createdAt
        if ($LASTEXITCODE -ne 0) { throw 'Could not look up the dispatched GitHub signature run.' }
        $runs = @($runsJson | ConvertFrom-Json)
        $match = $runs |
            Where-Object {
                $_.event -eq 'push' -and
                $_.headSha -eq $commit -and
                $_.displayTitle -eq $expectedTitle
            } |
            Select-Object -First 1
        if ($match) { $runId = [string]$match.databaseId; break }
        Start-Sleep -Seconds 8
    }
    if (-not $runId) { throw "No signature run appeared within $WaitTimeoutMinutes minutes. Draft $releaseTag remains available for retry." }

    Write-Host "Watching GitHub signature run $runId..."
    Invoke-CheckedNative -Executable 'gh' -Arguments @('run', 'watch', $runId, '--repo', $repo, '--exit-status') -FailureMessage "GitHub signature run $runId failed. Draft $releaseTag remains available for retry."

    $downloadDirectory = Join-Path $resolvedTempRoot 'signed-artifact'
    New-Item -ItemType Directory -Path $downloadDirectory -Force | Out-Null
    Invoke-CheckedNative -Executable 'gh' -Arguments @(
        'run', 'download', $runId, '--repo', $repo,
        '--name', "BOOP-Shield-v$versionCode-signed", '--dir', $downloadDirectory
    ) -FailureMessage "Could not download the signed APK. Draft $releaseTag remains available for retry."

    $signedApk = Join-Path $downloadDirectory "BOOP-Shield-v$versionCode-signed.apk"
    $signedReceipt = Join-Path $downloadDirectory 'build-receipt.json'
    if (-not (Test-Path -LiteralPath $signedApk -PathType Leaf) -or -not (Test-Path -LiteralPath $signedReceipt -PathType Leaf)) {
        throw "Signed artifact did not contain the expected APK and receipt. Draft $releaseTag remains available for retry."
    }
    $expectedCertificate = (Get-Content -LiteralPath (Join-Path $repoRoot 'shield-overlay\signing\boop-dev-cert-sha256.txt') -Raw).Trim().ToLowerInvariant()
    $null = Test-ShieldApk -ApkPath $signedApk -VersionCode $versionCode -VersionName $versionName -BuildTools $buildTools -ExpectedCertificateSha256 $expectedCertificate
    $signedMetadata = Get-Content -LiteralPath $signedReceipt -Raw | ConvertFrom-Json
    if ($signedMetadata.commit -ne $commit -or
        [int]$signedMetadata.versionCode -ne $versionCode -or
        $signedMetadata.localApkSha256 -ne $apkHash -or
        $signedMetadata.permanentCertificateSha256 -ne $expectedCertificate -or
        $signedMetadata.signedApkSha256 -ne (Get-Sha256 -Path $signedApk)) {
        throw "Downloaded signature receipt does not match the local source/APK. Draft $releaseTag remains available for retry."
    }

    $finalApk = Join-Path $outputDirectory "BOOP-Shield-v$versionCode-signed.apk"
    Copy-Item -LiteralPath $signedApk -Destination $finalApk -Force
    Copy-Item -LiteralPath $signedReceipt -Destination (Join-Path $outputDirectory 'signed-build-receipt.json') -Force
    Write-Host "Permanent signature verified: $finalApk"
    Write-Host "Signed APK SHA-256: $(Get-Sha256 -Path $finalApk)"

    Write-Host "Deleting temporary signature trigger tag $triggerTag..."
    $triggerDeleteResult = & $git -C $repoRoot push origin ":refs/tags/$triggerTag" 2>&1
    if ($LASTEXITCODE -ne 0) {
        $triggerDeleteResult = & gh api --method DELETE "repos/$repo/git/refs/tags/$triggerTag" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "The signed APK is saved, but trigger tag cleanup needs attention: $($triggerDeleteResult -join ' ')"
        }
    }
    $localTagDeleteResult = & $git -C $repoRoot tag -d $triggerTag 2>&1

    Write-Host "Deleting temporary draft release and tag $releaseTag..."
    $deleteResult = & gh release delete $releaseTag --repo $repo --yes --cleanup-tag 2>&1
    if ($LASTEXITCODE -eq 0) {
        $releaseCreated = $false
    } else {
        if ($releaseId) {
            $apiDeleteResult = & gh api --method DELETE "repos/$repo/releases/$releaseId" 2>&1
            if ($LASTEXITCODE -eq 0) {
                $tagDeleteResult = & gh api --method DELETE "repos/$repo/git/refs/tags/$releaseTag" 2>&1
                if ($LASTEXITCODE -eq 0) {
                    $releaseCreated = $false
                }
            }
        }
        if ($releaseCreated) {
            Write-Warning "The signed APK is saved, but draft cleanup needs attention. Release deletion output: $($deleteResult -join ' ')"
        }
    }
}
finally {
    foreach ($name in $signingNames) {
        [Environment]::SetEnvironmentVariable($name, $priorSigningEnvironment[$name], 'Process')
    }
    if ($resolvedTempRoot -and
        $resolvedTempRoot.StartsWith($resolvedTempParent, [System.StringComparison]::OrdinalIgnoreCase) -and
        (Test-Path -LiteralPath $resolvedTempRoot -PathType Container)) {
        Remove-Item -LiteralPath $resolvedTempRoot -Recurse -Force
    }
    if ($releaseCreated) {
        Write-Warning "Temporary draft $releaseTag remains in GitHub because signing or verification did not complete. Trigger tag: $triggerTag. Review the failed Actions run before retrying."
    }
}
