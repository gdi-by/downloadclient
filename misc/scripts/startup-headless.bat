@echo off
@echo GDI-BY Downloadclient
@echo ===============================================
@echo Setting up an environment.
SET dlc=%~dp0
SET dlc=%dlc:\\=\%
SET "PATH=%dlcbin%;%dlcdll%;%PATH%"
SET dlstep=%~f1

@echo --Setting up GDAL -----------------------------
call %dlc%bin\gisinternals\SDKShell.bat setenv
@echo -----------------------------------------------

@echo -----------------------------------------------
@echo Using "%dlstep%" as Download-Step file
@echo Starting the downloadclient.
@echo -----------------------------------------------

java -jar downloadclient.jar --headless %dlstep% --config=config
