# Publishes the JetBrains plugin to the Marketplace.
# Loads JETBRAINS_MARKETPLACE_TOKEN from .env (Gradle reads it as an env var),
# sets JAVA_HOME, then runs the publishPlugin task.

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

# Load .env into the process environment
$envFile = Join-Path $root ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $name, $value = $line -split "=", 2
            Set-Item -Path "env:$($name.Trim())" -Value $value.Trim()
        }
    }
}

if (-not $env:JETBRAINS_MARKETPLACE_TOKEN -or $env:JETBRAINS_MARKETPLACE_TOKEN -eq "your-token-here") {
    Write-Error "JETBRAINS_MARKETPLACE_TOKEN not set. Add it to .env (see .env.example)."
    exit 1
}

$env:JAVA_HOME = Join-Path $env:USERPROFILE "jdks\jdk-21.0.5+11"
Set-Location (Join-Path $root "jetbrains-plugin")
.\gradlew.bat publishPlugin
