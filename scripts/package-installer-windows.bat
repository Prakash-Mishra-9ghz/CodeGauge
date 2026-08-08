@echo off
REM Builds CodeGauge and packages it as a real Windows installer (.exe) using
REM jpackage (bundled with the JDK).
REM
REM PREREQUISITE: the WiX Toolset v3.14+ must be installed and on PATH.
REM Download: https://wixtoolset.org/
REM If you don't have WiX yet, use package-app-image-windows.bat first --
REM it needs no extra tools and produces a runnable CodeGauge.exe directly.
REM
REM Produces: target\dist\CodeGauge-1.0.0.exe
setlocal

cd /d "%~dp0.."

echo Building shaded jar...
call mvn -q clean package
if errorlevel 1 (
    echo Maven build failed.
    exit /b 1
)

echo Preparing jpackage input...
if exist target\jpackage-input rmdir /s /q target\jpackage-input
mkdir target\jpackage-input
copy target\codegauge.jar target\jpackage-input\ >nul

set "JPACKAGE_CMD=jpackage"

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe"

if "%JPACKAGE_CMD%"=="jpackage" for /f "tokens=2 delims==" %%A in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /C:"java.home"') do set "DETECTED_JAVA_HOME=%%A"

if defined DETECTED_JAVA_HOME for /f "tokens=* delims= " %%B in ("%DETECTED_JAVA_HOME%") do set "DETECTED_JAVA_HOME=%%B"

if "%JPACKAGE_CMD%"=="jpackage" if defined DETECTED_JAVA_HOME if exist "%DETECTED_JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE_CMD=%DETECTED_JAVA_HOME%\bin\jpackage.exe"

echo Using jpackage: %JPACKAGE_CMD%

call "%JPACKAGE_CMD%" --type exe --input target\jpackage-input --dest target\dist --name CodeGauge --app-version 1.0.0 --vendor "Prakash Mishra" --main-jar codegauge.jar --win-console --win-dir-chooser --win-menu --win-shortcut --description "Repository quality analyzer"

if errorlevel 1 (
    echo jpackage failed using: %JPACKAGE_CMD%
    echo Is the WiX Toolset installed and on PATH? Download: https://wixtoolset.org/
    echo If this said "not recognized" instead, set JAVA_HOME manually:
    echo   setx JAVA_HOME "C:\Program Files\Java\jdk-21"
    echo Then open a NEW terminal window and retry.
    exit /b 1
)

echo Installer created in target\dist\
endlocal