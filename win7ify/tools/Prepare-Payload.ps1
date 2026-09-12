$ErrorActionPreference = 'Stop'
$destination = Join-Path $PSScriptRoot '../src/Win7ify/Payload'
New-Item -ItemType Directory -Force $destination | Out-Null
$file = Join-Path $destination 'OpenShellSetup_4_4_198.exe'
$expected = 'a4d2d4459de55b5e962ba2a14f7bb794170511649138173dfa72949837b48c3f'
if (!(Test-Path $file)) {
    Invoke-WebRequest 'https://github.com/Open-Shell/Open-Shell-Menu/releases/download/v4.4.198/OpenShellSetup_4_4_198.exe' -OutFile $file
}
if ((Get-Item $file).Length -ne 9924608 -or (Get-FileHash $file -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expected) {
    throw 'Pinned official Open-Shell installer integrity mismatch. Do not build or execute it.'
}
Write-Host "Official Open-Shell 4.4.198 payload verified: $expected"
Write-Host 'This upstream payload is not Authenticode-signed. No Windows trust settings are changed.'
