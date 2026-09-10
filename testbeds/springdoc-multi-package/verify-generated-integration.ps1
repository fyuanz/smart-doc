$ErrorActionPreference = "Stop"

$testbedRoot = (Resolve-Path $PSScriptRoot).Path
$repositoryRoot = (Resolve-Path (Join-Path $testbedRoot "../..")).Path
$generatedRoot = Join-Path $testbedRoot "target/generated-openapi"
$outputRoot = Join-Path $testbedRoot "target/smartdoc"
$skillRoot = Join-Path $outputRoot "springdoc-multi-package-api"

function Invoke-CheckedMaven([string] $workingDirectory, [string[]] $arguments) {
    Push-Location $workingDirectory
    try {
        $previousErrorAction = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        $lines = @(& mvn @arguments 2>&1)
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorAction
        Pop-Location
    }
    $lines | ForEach-Object { Write-Host $_ }
    if ($exitCode -ne 0) {
        throw "Maven failed with exit code ${exitCode}: mvn $($arguments -join ' ')"
    }
    return ,$lines
}

function Assert-True([bool] $condition, [string] $message) {
    if (-not $condition) { throw $message }
}

function Get-FreeTcpPort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    try {
        $listener.Start()
        return $listener.LocalEndpoint.Port
    } finally {
        $listener.Stop()
    }
}

function Read-Status {
    $path = Join-Path $outputRoot ".smartdoc/status/springdoc-multi-package.json"
    return Get-Content -LiteralPath $path -Raw | ConvertFrom-Json
}

function Get-TreeDigest([string] $root) {
    $builder = [System.Text.StringBuilder]::new()
    $absoluteRoot = (Resolve-Path -LiteralPath $root).Path.TrimEnd('\') + '\'
    Get-ChildItem -LiteralPath $root -File -Recurse | Sort-Object FullName | ForEach-Object {
        $relative = $_.FullName.Substring($absoluteRoot.Length).Replace('\', '/')
        $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash
        [void] $builder.AppendLine("$relative=$hash")
    }
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($builder.ToString())
    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        return -join ($sha256.ComputeHash($bytes) | ForEach-Object { $_.ToString("x2") })
    } finally {
        $sha256.Dispose()
    }
}

Invoke-CheckedMaven $repositoryRoot @("-B", "-DskipTests", "install") | Out-Null
$successHttpPort = Get-FreeTcpPort
do { $successJmxPort = Get-FreeTcpPort } while ($successJmxPort -eq $successHttpPort)
$successLog = Invoke-CheckedMaven $testbedRoot @("-B", "clean",
        "-Dsmartdoc.application.port=$successHttpPort", "-Dsmartdoc.springdoc.port=$successHttpPort",
        "-Dsmartdoc.jmx.port=$successJmxPort", "verify")

$account = Join-Path $generatedRoot "account.json"
$business = Join-Path $generatedRoot "business.json"
Assert-True (Test-Path -LiteralPath $account -PathType Leaf) "current-build account document missing"
Assert-True (Test-Path -LiteralPath $business -PathType Leaf) "current-build business document missing"
Assert-True (Test-Path -LiteralPath (Join-Path $skillRoot "SKILL.md") -PathType Leaf) "generated-document Skill missing"
Assert-True ((Read-Status).outcome -eq "SUCCESS") "generated-document Skill update failed"
Assert-True ((@($successLog | Where-Object { "$_" -match 'springdoc-openapi.*:generate \(export-account\)' })).Count -eq 1) "account export did not run once"
Assert-True ((@($successLog | Where-Object { "$_" -match 'springdoc-openapi.*:generate \(export-business\)' })).Count -eq 1) "business export did not run once"
Assert-True ((@($successLog | Where-Object { "$_" -match 'SmartDoc \[springdoc-multi-package\] SUCCESS' })).Count -eq 1) "Skill update did not run once"

$skillDigest = Get-TreeDigest $skillRoot
$accountTime = (Get-Item -LiteralPath $account).LastWriteTimeUtc
$businessTime = (Get-Item -LiteralPath $business).LastWriteTimeUtc
$failureHttpPort = Get-FreeTcpPort
do { $failureJmxPort = Get-FreeTcpPort } while ($failureJmxPort -eq $failureHttpPort)
$failureLog = Invoke-CheckedMaven $testbedRoot @("-B", "-Dsmartdoc.application.port=$failureHttpPort",
        "-Dsmartdoc.springdoc.port=1", "-Dsmartdoc.jmx.port=$failureJmxPort", "verify")

Assert-True ((Read-Status).outcome -eq "FAILED") "stale generated documents did not fail the update"
Assert-True ((Get-TreeDigest $skillRoot) -eq $skillDigest) "previous Skill changed after document preparation failure"
Assert-True ((Get-Item -LiteralPath $account).LastWriteTimeUtc -eq $accountTime) "failed producer rewrote account document"
Assert-True ((Get-Item -LiteralPath $business).LastWriteTimeUtc -eq $businessTime) "failed producer rewrote business document"
Assert-True ((@($failureLog | Where-Object { "$_" -match 'SmartDoc \[springdoc-multi-package\] FAILED: account: INPUT: document was not prepared during the current Maven build' })).Count -eq 1) "current-build freshness warning missing"

Write-Host "Generated springdoc integration verification passed."
