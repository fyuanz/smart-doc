$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    # Never accept old target files after a failed build.
    & mvn -B clean test
    if ($LASTEXITCODE -ne 0) { throw "Fixture validation failed (exit $LASTEXITCODE); snapshots were not refreshed." }

    $documents = @()
    foreach ($group in @('account', 'business')) {
        $source = Join-Path $PSScriptRoot "target/openapi/$group.json"
        $document = Get-Content -LiteralPath $source -Raw -Encoding UTF8 | ConvertFrom-Json
        if ($document.openapi -ne '3.1.0') { throw "Unexpected OpenAPI version in $group" }
        $operations = 0
        foreach ($path in $document.paths.PSObject.Properties) {
            foreach ($method in $path.Value.PSObject.Properties.Name) {
                if ($method -in @('get','post','put','patch','delete','head','options','trace')) { $operations++ }
            }
        }
        $documents += [ordered]@{
            documentId = $group
            endpoint = "/v3/api-docs/$group"
            file = "$group.json"
            openapi = $document.openapi
            sha256 = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
            operations = $operations
            schemas = @($document.components.schemas.PSObject.Properties).Count
        }
    }
    $sourceHashes = [ordered]@{}
    $sourceFiles = @(Get-Item pom.xml) + @(Get-ChildItem src/main -File -Recurse)
    foreach ($source in ($sourceFiles | Sort-Object FullName)) {
        $relative = $source.FullName.Substring($PSScriptRoot.Length + 1).Replace('\', '/')
        $sourceHashes[$relative] = (Get-FileHash -LiteralPath $source.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    }
    $metadata = [ordered]@{
        serviceId = 'springdoc-multi-package'
        skillName = 'springdoc-multi-package-api'
        java = '17'
        springBoot = '3.5.9'
        springdoc = '2.8.15'
        captureCommand = 'powershell -NoProfile -File testbeds/springdoc-multi-package/refresh-fixtures.ps1'
        capture = 'SpringBootTest random loopback port; HTTP connect 5s/request 20s; test 60s; fork 120s; context closed after test'
        sanitization = 'All data authored as fictional samples; no external API data. Pretty printing only; no semantic rewrite.'
        documents = $documents
        sourceSha256 = $sourceHashes
    }
    New-Item -ItemType Directory -Path fixtures -Force | Out-Null
    foreach ($group in @('account', 'business')) {
        Copy-Item -LiteralPath "target/openapi/$group.json" -Destination "fixtures/$group.json"
    }
    $json = $metadata | ConvertTo-Json -Depth 12
    [System.IO.File]::WriteAllText((Join-Path $PSScriptRoot 'fixtures/metadata.json'), $json + "`n", [System.Text.UTF8Encoding]::new($false))
    Write-Output 'Refreshed both validated fixture documents and metadata.'
} finally {
    Pop-Location
}
