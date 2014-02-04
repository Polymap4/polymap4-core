@echo off
rem -------------------------------------------------------------------------
rem Polymap3 Bootstrap Script for Win32
rem -------------------------------------------------------------------------

set DIRNAME=.\                                                                                                                           
set WORKSPACE=%DIRNAME%/workspace
set PORT=8080
rem set JAVA_HOME=C:\Programme\Java\jdk1.6.0_05

rem if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%                                                                                             
if "%OS%" == "Windows_NT" echo !!!Check DIRNAME on Windows_NT!!!                                                                                             

set LANG=en_US.UTF-8

cd %DIRNAME%/bin
set SUN_VM=-server -XX:MaxPermSize=128M -XX:NewRatio=4 -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=500
set VMARGS=-Xverify:none %SUN_VM% -Xmx512M -Dorg.eclipse.rwt.compression=true
set ARGS=-console -consolelog -registryMultiLanguage
set LOGARGS=-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
rem set PROXY='-Dhttp.proxyHost=someHost -Dhttp.proxyPort=somePort -Dhttp.proxyUser=someUserName -Dhttp.proxyPassword=somePassword'

eclipse.exe %ARGS% -data %WORKSPACE% -vmargs %VMARGS% %PROXY% -Dorg.osgi.service.http.port=%PORT% %LOGARGS%

