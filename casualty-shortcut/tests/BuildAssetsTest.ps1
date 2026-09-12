param([string]$Sdk = "$env:LOCALAPPDATA/Android/Sdk")
$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$source = Get-Content (Join-Path $project 'build.ps1') -Raw
$boundary = $source.IndexOf('& javac -d build/tests')
if ($boundary -lt 0) { throw 'Build preparation boundary not found' }
$temp = Join-Path ([IO.Path]::GetTempPath()) ('casualty-assets-' + [guid]::NewGuid())
$previous = Get-Location
try {
    New-Item -ItemType Directory "$temp/res/drawable-nodpi" -Force | Out-Null
    $before = @{}
    foreach ($name in @('banner', 'icon')) {
        Copy-Item "$project/res/drawable-nodpi/$name.png" "$temp/res/drawable-nodpi/$name.png"
        $before[$name] = (Get-FileHash "$temp/res/drawable-nodpi/$name.png" -Algorithm SHA256).Hash
    }
    [IO.File]::WriteAllText("$temp/prepare.ps1", $source.Substring(0, $boundary))
    & "$temp/prepare.ps1" -Sdk $Sdk
    foreach ($name in @('banner', 'icon')) {
        $after = (Get-FileHash "$temp/res/drawable-nodpi/$name.png" -Algorithm SHA256).Hash
        if ($after -ne $before[$name]) { throw "Build overwrote approved $name resource" }
    }
    Write-Output 'PASS: build preparation preserves both supplied artwork resources byte-for-byte'
} finally {
    Set-Location $previous
    if (Test-Path $temp) { Remove-Item $temp -Recurse -Force }
}
