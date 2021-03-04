@echo off
@echo GDI-BY Downloadclient
@echo ===============================================
@echo Setting up an environment.
@setlocal EnableDelayedExpansion

SET "SYS_PATH=%PATH%"
SET dlc=%~dp0
SET dlc=%dlc:\\=\%
SET "PATH=%dlcbin%;%dlcdll%;%PATH%"

@echo --Setting up GDAL -----------------------------
call %dlc%bin\gisinternals\SDKShell.bat setenv
@echo -----------------------------------------------

@echo -----------------------------------------------
@echo Starting the downloadclient.
@echo This may take a while!
@echo -----------------------------------------------

where java >nul 2>nul
if %errorlevel%==1 (
    @echo "No Java Runtime (JRE/JDK) installed! Please read the documentation for further information."
    goto exit
)
  for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER=%%g
  )
  set JAVAVER=%JAVAVER:"=%
  @echo Java version=%JAVAVER%

  IF "%JAVAVER:~0,3%"=="1.8" (
    @echo "Starting GDI-BY Downloadclient using Java version 1.8"
    start javaw -jar downloadclient.jar --config=config
    goto exit
  ) else (
    IF "%JAVAVER:~0,4%"=="11.0" (
      IF "%JAVAFX_HOME%"=="" (
        @echo "JAVAFX_HOME is not set. Please read the documentation for further information."
        goto exit
      ) else (
        @echo "Starting GDI-BY Downloadclient using Java version 11"
        start javaw --module-path %JAVAFX_HOME%\lib --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.web --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=javafx.graphics/javafx.application=ALL-UNNAMED --add-opens=javafx.graphics/javafx.geometry=ALL-UNNAMED --add-opens=javafx.web/javafx.scene.web=ALL-UNNAMED --add-opens=javafx.web/com.sun.webkit=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.util=ALL-UNNAMED --add-exports javafx.base/com.sun.javafx.logging=ALL-UNNAMED --add-exports javafx.graphics/com.sun.prism=ALL-UNNAMED --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.geom.transform=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED --add-exports javafx.graphics/com.sun.glass.utils=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.font=ALL-UNNAMED --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports javafx.graphics/com.sun.prism.paint=ALL-UNNAMED --add-exports javafx.graphics/com.sun.scenario.effect=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.text=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.iio=ALL-UNNAMED -jar downloadclient.jar --config=config
        goto exit
      )
    ) else (
      @echo "No suitable Java version found. Please read the documentation for further information."
      goto exit
    )
  )

:exit
@set "PATH=%SYS_PATH%"
@set SYS_PATH=
@set _path=
pause
