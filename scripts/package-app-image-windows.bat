@echo off
REM Builds CodeGauge and packages it as a native Windows app-image using
REM jpackage (bundled with the JDK -- no extra download needed).
REM
REM Produces a self-contained folder with a bundled JVM: target\dist\CodeGauge\
REM Run it directly via target\dist\CodeGauge\CodeGauge.exe
REM
REM This does NOT require the WiX Toolset. For a real installer (.exe/.msi
REM with a Start Menu entry and uninstaller), use package-installer-windows.bat
REM instead, which does require WiX.
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

REM Locate jpackage. On Windows, `java` on PATH often resolves through
REM Oracle's "javapath" shim folder, which contains only java.exe -- not
REM the rest of the JDK's bin directory -- so a bare `jpackage` call or
REM JAVA_HOME often fail even when java itself works fine. Ask the running
REM JVM for its own install directory instead; that is always accurate.
set "JPACKAGE_CMD=jpackage"

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe"

if "%JPACKAGE_CMD%"=="jpackage" for /f "tokens=2 delims==" %%A in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /C:"java.home"') do set "DETECTED_JAVA_HOME=%%A"

if defined DETECTED_JAVA_HOME for /f "tokens=* delims= " %%B in ("%DETECTED_JAVA_HOME%") do set "DETECTED_JAVA_HOME=%%B"

if "%JPACKAGE_CMD%"=="jpackage" if defined DETECTED_JAVA_HOME if exist "%DETECTED_JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE_CMD=%DETECTED_JAVA_HOME%\bin\jpackage.exe"

echo Using jpackage: %JPACKAGE_CMD%

call "%JPACKAGE_CMD%" --type app-image --input target\jpackage-input --dest target\dist --name CodeGauge --app-version 1.0.0 --vendor "Prakash Mishra" --main-jar codegauge.jar --win-console --description "Repository quality analyzer"

if errorlevel 1 (
    echo jpackage failed using: %JPACKAGE_CMD%
    echo If this said "not recognized", find your JDK install folder and set JAVA_HOME manually:
    echo   setx JAVA_HOME "C:\Program Files\Java\jdk-21"
    echo Then open a NEW terminal window and retry.
    exit /b 1
)

echo Done. App image is at target\dist\CodeGauge\CodeGauge.exe
echo Example: target\dist\CodeGauge\CodeGauge.exe analyze . --html
endlocal