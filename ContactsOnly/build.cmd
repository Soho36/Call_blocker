@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1" %*
set "BUILD_RESULT=%ERRORLEVEL%"
pause
exit /b %BUILD_RESULT%
