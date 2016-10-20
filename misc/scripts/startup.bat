@echo off
@echo GDI-BY Downloadclient
@echo ===============================================
@echo Setting up an environment.
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

start javaw -jar downloadclient.jar -config=config

pause
