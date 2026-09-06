param(
    [string]$JavacPath = "javac",
    [string]$JavaPath = "java",
    [Parameter(Mandatory = $true)]
    [string]$JunitPath,
    [Parameter(Mandatory = $true)]
    [string]$HamcrestPath,
    [string]$OutputDirectory = (Join-Path $PSScriptRoot "build"),
    [string[]]$TestClasses = @(
        "com.boop.shieldoverlay.DeezerPuppetPolicyTest",
        "com.boop.shieldoverlay.MediaPuppetClockTest",
        "com.boop.shieldoverlay.MediaPuppetStateTest",
        "com.boop.shieldoverlay.DeezerSessionObserverTest",
        "com.boop.shieldoverlay.MediaPuppetFrameLoopTest",
        "com.boop.shieldoverlay.HeadphoneGeometryTest",
        "com.boop.shieldoverlay.DeezerPuppetSettingsModelTest"
    )
)

$ErrorActionPreference = "Stop"

function Invoke-Native {
    param(
        [string]$Executable,
        [string[]]$Arguments
    )

    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Native command failed with exit code $LASTEXITCODE`: $Executable"
    }
}

$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$mainRoot = Join-Path $repoRoot "shield-overlay\app\src\main\java\com\boop\shieldoverlay"
$testRoot = Join-Path $repoRoot "shield-overlay\app\src\test\java\com\boop\shieldoverlay"
$outputPath = [IO.Path]::GetFullPath($OutputDirectory)
$separator = [IO.Path]::PathSeparator
$testClassPath = "$outputPath$separator$JunitPath$separator$HamcrestPath"

$sourceAllowlist = @(
    (Join-Path $mainRoot "DeezerPuppetPolicy.java"),
    (Join-Path $mainRoot "MediaPuppetClock.java"),
    (Join-Path $mainRoot "MediaPuppetMotion.java"),
    (Join-Path $mainRoot "MediaPuppetState.java"),
    (Join-Path $mainRoot "DeezerSessionObserver.java"),
    (Join-Path $mainRoot "MediaPuppetFrameLoop.java"),
    (Join-Path $mainRoot "HeadphoneGeometry.java"),
    (Join-Path $mainRoot "DeezerPuppetSettingsModel.java"),
    (Join-Path $testRoot "HeadphoneGeometryTest.java"),
    (Join-Path $testRoot "DeezerPuppetSettingsModelTest.java"),
    (Join-Path $testRoot "MediaPuppetFrameLoopTest.java"),
    (Join-Path $testRoot "DeezerPuppetPolicyTest.java"),
    (Join-Path $testRoot "MediaPuppetClockTest.java"),
    (Join-Path $testRoot "MediaPuppetStateTest.java"),
    (Join-Path $testRoot "DeezerSessionObserverTest.java")
)
$sources = @($sourceAllowlist | Where-Object { Test-Path -LiteralPath $_ })

New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

$compileArguments = @(
    "--release", "17",
    "-cp", "$JunitPath$separator$HamcrestPath",
    "-d", $outputPath
) + $sources
Invoke-Native -Executable $JavacPath -Arguments $compileArguments

$availableTestClasses = @(
    $TestClasses | Where-Object {
        $relativeClassFile = $_.Replace(".", [IO.Path]::DirectorySeparatorChar) + ".class"
        Test-Path -LiteralPath (Join-Path $outputPath $relativeClassFile)
    }
)
if ($availableTestClasses.Count -eq 0) {
    throw "No requested test classes were compiled."
}

$testArguments = @(
    "-cp", $testClassPath,
    "org.junit.runner.JUnitCore"
) + $availableTestClasses
Invoke-Native -Executable $JavaPath -Arguments $testArguments
