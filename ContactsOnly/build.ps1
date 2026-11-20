param([string[]]$Tasks = @('assembleDebug', 'testDebugUnitTest', 'lintDebug'))
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$androidRoot = Split-Path -Parent $projectRoot
$toolsRoot = Join-Path $androidRoot 'tools'
$jdk = Get-ChildItem -LiteralPath $toolsRoot -Directory -Filter 'jdk-17*' | Select-Object -First 1
if (!$jdk) { throw 'Portable JDK 17 not found in the sibling tools folder. Alternatively, open this project in Android Studio.' }
$gradle = Join-Path $toolsRoot 'gradle-8.11.1\bin\gradle.bat'
if (!(Test-Path -LiteralPath $gradle)) { throw 'Portable Gradle 8.11.1 not found in the sibling tools folder.' }
$variables = @{
    JAVA_HOME = $jdk.FullName
    ANDROID_HOME = (Join-Path $toolsRoot 'sdk')
    ANDROID_USER_HOME = (Join-Path $androidRoot 'cache\android')
    ANDROID_EMULATOR_HOME = (Join-Path $androidRoot 'cache\emulator')
    GRADLE_USER_HOME = (Join-Path $androidRoot 'cache\gradle')
    TEMP = (Join-Path $androidRoot 'cache\temp')
    TMP = (Join-Path $androidRoot 'cache\temp')
}
$previous = @{}
try {
    foreach ($name in $variables.Keys) {
        $previous[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
        [Environment]::SetEnvironmentVariable($name, $variables[$name], 'Process')
    }
    foreach ($folder in @($variables.ANDROID_USER_HOME, $variables.ANDROID_EMULATOR_HOME,
                          $variables.GRADLE_USER_HOME, $variables.TEMP)) {
        New-Item -ItemType Directory -Force -Path $folder | Out-Null
    }
    # Use Windows' existing trusted CAs; keep TLS verification enabled.
    & $gradle --no-daemon --console=plain '-Djavax.net.ssl.trustStoreType=Windows-ROOT' '-Djavax.net.ssl.trustStore=NONE' -p $projectRoot @Tasks
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
    $apk = Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk'
    if (Test-Path -LiteralPath $apk) { Write-Host "APK: $apk" }
} finally {
    foreach ($name in $previous.Keys) {
        [Environment]::SetEnvironmentVariable($name, $previous[$name], 'Process')
    }
}
