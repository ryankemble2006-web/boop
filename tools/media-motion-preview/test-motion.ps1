param(
    [string]$JavacPath = "javac",
    [string]$JavaPath = "java",
    [Parameter(Mandatory = $true)]
    [string]$JunitPath,
    [Parameter(Mandatory = $true)]
    [string]$HamcrestPath,
    [string]$OutputDirectory = (Join-Path $PSScriptRoot "build")
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

function Invoke-NativeCapture {
    param(
        [string]$Executable,
        [string[]]$Arguments
    )

    $output = & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Native command failed with exit code $LASTEXITCODE`: $Executable"
    }
    return ($output -join [Environment]::NewLine)
}

function Assert-Equal {
    param(
        $Expected,
        $Actual,
        [string]$Label
    )

    if ($Expected -ne $Actual) {
        throw "$Label expected '$Expected' but was '$Actual'"
    }
}

function Assert-FiniteRows {
    param(
        [object[]]$Rows,
        [string]$Label
    )

    for ($rowIndex = 0; $rowIndex -lt $Rows.Count; $rowIndex++) {
        $row = @($Rows[$rowIndex])
        Assert-Equal 4 $row.Count "$Label row $rowIndex width"
        for ($valueIndex = 0; $valueIndex -lt $row.Count; $valueIndex++) {
            $value = [double]$row[$valueIndex]
            if ([double]::IsNaN($value) -or [double]::IsInfinity($value)) {
                throw "$Label row $rowIndex value $valueIndex is not finite"
            }
        }
    }
}

function Assert-SamplesMatch {
    param(
        [object[]]$Rows,
        [string]$Motion,
        [string]$JavaExecutable,
        [string]$ClassPath
    )

    $probeText = Invoke-NativeCapture $JavaExecutable @("-cp", $ClassPath, "MotionProbe", $Motion)
    $probeRows = @($probeText -split "\r?\n")
    Assert-Equal $Rows.Count $probeRows.Count "$Motion probe row count"

    for ($rowIndex = 0; $rowIndex -lt $Rows.Count; $rowIndex++) {
        $probe = @($probeRows[$rowIndex].Split(","))
        $row = @($Rows[$rowIndex])
        Assert-Equal 4 $probe.Count "$Motion probe row $rowIndex width"

        for ($valueIndex = 0; $valueIndex -lt 4; $valueIndex++) {
            $expected = [double]::Parse(
                $probe[$valueIndex],
                [Globalization.CultureInfo]::InvariantCulture
            )
            $actual = [double]$row[$valueIndex]
            if ([Math]::Abs($expected - $actual) -gt 0.0001) {
                throw "$Motion row $rowIndex value $valueIndex expected '$expected' but was '$actual'"
            }
        }
    }
}

$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\.."))
$motionSource = Join-Path $repoRoot "shield-overlay\app\src\main\java\com\boop\shieldoverlay\MediaPuppetMotion.java"
$testSource = Join-Path $repoRoot "shield-overlay\app\src\test\java\com\boop\shieldoverlay\MediaPuppetMotionTest.java"
$exportSource = Join-Path $PSScriptRoot "ExportMotion.java"
$outputPath = [IO.Path]::GetFullPath($OutputDirectory)
$probeSource = Join-Path $outputPath "MotionProbe.java"
$separator = [IO.Path]::PathSeparator
$testClassPath = "$outputPath$separator$JunitPath$separator$HamcrestPath"

New-Item -ItemType Directory -Force -Path $outputPath | Out-Null

@'
import com.boop.shieldoverlay.MediaPuppetMotion;

public final class MotionProbe {
    public static void main(String[] args) {
        String motion = args[0];
        boolean music = "music".equals(motion);
        long periodMs = music
                ? MediaPuppetMotion.MUSIC_PERIOD_MS
                : MediaPuppetMotion.CINEMA_PERIOD_MS;
        for (long elapsedMs = 0; elapsedMs < periodMs; elapsedMs += 40L) {
            MediaPuppetMotion.Pose pose = music
                    ? MediaPuppetMotion.music(elapsedMs)
                    : MediaPuppetMotion.cinema(elapsedMs);
            System.out.println(
                    Float.toString(pose.x) + ","
                            + Float.toString(pose.y) + ","
                            + Float.toString(pose.rotationDegrees) + ","
                            + Float.toString(pose.kernelAlpha));
        }
    }
}
'@ | Set-Content -LiteralPath $probeSource -Encoding utf8NoBOM

Invoke-Native $JavacPath @(
    "-cp", "$JunitPath$separator$HamcrestPath",
    "-d", $outputPath,
    $motionSource,
    $testSource,
    $exportSource,
    $probeSource
)

Invoke-Native $JavaPath @(
    "-cp", $testClassPath,
    "org.junit.runner.JUnitCore",
    "com.boop.shieldoverlay.MediaPuppetMotionTest"
)

$jsonText = Invoke-NativeCapture $JavaPath @("-cp", $outputPath, "ExportMotion")
$document = $jsonText | ConvertFrom-Json

Assert-Equal 40 $document.stepMs "stepMs"
Assert-Equal 3600 $document.musicPeriodMs "musicPeriodMs"
Assert-Equal 12000 $document.cinemaPeriodMs "cinemaPeriodMs"

$musicRows = @($document.music)
$cinemaRows = @($document.cinema)
Assert-Equal 90 $musicRows.Count "music row count"
Assert-Equal 300 $cinemaRows.Count "cinema row count"
Assert-FiniteRows $musicRows "music"
Assert-FiniteRows $cinemaRows "cinema"

Assert-SamplesMatch $musicRows "music" $JavaPath $outputPath
Assert-SamplesMatch $cinemaRows "cinema" $JavaPath $outputPath

Write-Host "Motion checks passed: 7 JUnit tests; 90 music rows; 300 cinema rows."
