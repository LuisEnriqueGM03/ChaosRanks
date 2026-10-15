# Sube el jar del mod a CurseForge.
# Token: variable de entorno CURSEFORGE_TOKEN (de la sesión o guardada con `setx`).
# Uso:  powershell -File tools/upload-curseforge.ps1 [-DryRun]
param(
	[string]$ProjectId = "1729326",
	[ValidateSet("release", "beta", "alpha")]
	[string]$ReleaseType = "release",
	[string]$Loader = "",
	[switch]$DryRun
)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent

$token = $env:CURSEFORGE_TOKEN
if (-not $token) { $token = [Environment]::GetEnvironmentVariable("CURSEFORGE_TOKEN", "User") }
if (-not $token) { throw "Falta CURSEFORGE_TOKEN (setx CURSEFORGE_TOKEN <token>)" }

$version = ((Get-Content "$root\gradle.properties") | Where-Object { $_ -match '^version=' }) -replace '^version=', ''
if (-not $Loader) {
 $Loader = ((Get-Content "$root\gradle.properties") | Where-Object { $_ -match '^default_loader=' }) -replace '^default_loader=', ''
}
if ($Loader -notin @('fabric', 'neoforge', 'forge')) { throw "Loader debe ser fabric, neoforge o forge" }
$loaderLabel = @{fabric='Fabric'; neoforge='NeoForge'; forge='Forge'}[$Loader]
$jar = Get-ChildItem "$root\$Loader\build\libs" -Filter "chaosranks-$Loader-26.3-$version.jar" | Select-Object -First 1
if (-not $jar) { throw "No existe el JAR de $Loader para $version; compila el cargador elegido" }

# Ids de CurseForge para versión de juego, cargador, Java y entorno.
$api = "https://minecraft.curseforge.com/api"
$all = Invoke-RestMethod -Uri "$api/game/versions" -Headers @{ "X-Api-Token" = $token }
$wanted = "26.3", $loaderLabel, "Java 25", "Client", "Server"
$ids = foreach ($name in $wanted) {
	$match = $all | Where-Object { $_.name -eq $name } | Select-Object -First 1
	if (-not $match) { throw "CurseForge no tiene la versión '$name'" }
	Write-Host "  $name -> $($match.id)"
	$match.id
}

$projectRelations = @()
if ($Loader -eq 'fabric') { $projectRelations = @(@{ slug = 'fabric-api'; type = 'requiredDependency' }) }

$metadataData = @{
	# ReadAllText: en PowerShell 5.1, Get-Content -Raw añade propiedades que ConvertTo-Json serializa.
	changelog     = [IO.File]::ReadAllText("$root\tools\changelog.md", [Text.Encoding]::UTF8)
	changelogType = "markdown"
	displayName   = "Chaos Ranks $version - $loaderLabel 26.3"
	gameVersions  = @($ids)
	releaseType   = $ReleaseType
	relations     = @{ projects = $projectRelations }
}
if ($Loader -ne 'fabric') { $metadataData.Remove('relations') }
$metadata = $metadataData | ConvertTo-Json -Depth 5
$metaFile = Join-Path $env:TEMP ("chaosranks-cf-" + [guid]::NewGuid().ToString() + ".json")
[IO.File]::WriteAllText($metaFile, $metadata, (New-Object Text.UTF8Encoding $false))

Write-Host "Proyecto $ProjectId | $($jar.Name) | Chaos - Ranks $version | $ReleaseType"
if ($DryRun) { Write-Host "DryRun: no se sube nada."; Get-Content $metaFile; Remove-Item -LiteralPath $metaFile; exit 0 }

try {
$response = (& curl.exe -s -w "`nHTTP %{http_code}" -H "X-Api-Token: $token" `
	-F "metadata=<$metaFile" -F "file=@$($jar.FullName)" "$api/projects/$ProjectId/upload-file") -join "`n"
} finally { Remove-Item -LiteralPath $metaFile -ErrorAction SilentlyContinue }
Write-Host $response
if ($response -notmatch "HTTP 200") { exit 1 }
