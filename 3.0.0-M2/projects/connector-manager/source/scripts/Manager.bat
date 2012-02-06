@echo off
goto start
rem  Copyright 2010 Google Inc.
rem
rem  Licensed under the Apache License, Version 2.0 (the "License");
rem  you may not use this file except in compliance with the License.
rem  You may obtain a copy of the License at
rem
rem  http://www.apache.org/licenses/LICENSE-2.0
rem
rem  Unless required by applicable law or agreed to in writing, software
rem  distributed under the License is distributed on an "AS IS" BASIS,
rem  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem  See the License for the specific language governing permissions and
rem  limitations under the License.

rem  This helper script is used to access several Connector Manager functions
rem  from the command line.  This script sets up the Java classpath needed
rem  by the Connector Manager, then invokes the Connector Manager command
rem  processor.
rem
rem  Usage: Manager [-?] [-v] [command] [options] [arguments]
rem         -?, --help    Display the set of available commands
rem         -v, --version Display the Connector Manager version
rem
rem  To get help for a command, specify the command along with -? or --help
rem  For instance:
rem    Manager MigrateStore --help

:start
setlocal

rem The Connector Manager main class.
set classname=com.google.enterprise.connector.manager.ManagerMain

set pwd=%cd%
rem The Windows equivalent of dirname and basename only work in FOR and CALL.
for %%i in ( "%pwd%" ) do set pwdname=%%~nxi
for %%i in ( "%pwd%" ) do set parentdir=%%~dpi
if "%parentdir:~-1%" == "\" set parentdir=%parentdir:~0,-1%

rem  If there is a setenv file in the same directory as my script, then invoke
rem  it to set certain environment variables appropriate to this installation.
rem  For instance, JAVA_HOME or CLASSPATH.
rem  If the Connector requires JAR files not included in the Connector
rem  installation (for instance Documentum DFC JAR files), then you should
rem  specify those JAR files in the CLASSPATH environment variable.
rem  For instance:
rem    set CLASSPATH="%CLASSPATH%";"%DOCUMENTUM_SHARED%"\dfc\dfc.jar;"%DOCUMENTUM_SHARED%"\config
for %%i in ( "%0" ) do set scriptdir=%%~dpi
if exist "%scriptdir%\setenv.bat" (
  call "%scriptdir%\setenv.bat"
)

rem  Locate a Java runtime. The Connector Manager requires Java 5 or better.
if "%JAVA_HOME%" == "" (
  if "%JRE_HOME%" == "" (
    set java=java
  ) else (
    set java=%JRE_HOME%\bin\java
  )
) else (
  set java=%JAVA_HOME%\bin\java
)

rem  Locate the Tomcat 6 installation.  Note that this code requires a
rem  Tomcat 6 directory layout, which is simplified over previous versions.
if "%CATALINA_HOME%" == "" (
  if "%pwdname%" == "Tomcat" (
    set cathome=%pwd%
  ) else if "%pwdname%" == "Scripts" (
    set cathome=%parentdir%\Tomcat
  ) else (
    set cathome=%pwd%\Tomcat
  )
) else (
  set cathome=%CATALINA_HOME%
)
if not exist "%cathome%" (
  echo Unable to locate the Apache Tomcat web application container.
  echo Please set the CATALINA_HOME and CATALINA_BASE environment
  echo variables appropriately.
  goto end
)
if "%CATALINA_BASE%" == "" (
  set catbase=%cathome%
) else (
  set catbase=%CATALINA_BASE%
)

rem  Locate the Connector Manager's WEB-INF directory, typically found at
rem  'Tomcat\webapps\connector-manager\WEB-INF' in the Connector installation.
set webinf=%catbase%\webapps\connector-manager\WEB-INF
if not exist "%webinf%" (
  rem  Perhaps we are running from the Install dir, or Scripts.
  set webinf=Tomcat\webapps\connector-manager\WEB-INF
  if "%pwdname%" == "Scripts" (
    set webinf=%parentdir%\%webinf%
  )
  if not exist "%webinf%" (
    echo Unable to locate the Connector Manager web application directory.
    goto end
  )
)

rem  Set the classpath to include all jars in the Connector Manager
rem  lib directory.
for /R "%webinf%"\lib %%a in (*.jar) do call :appendClassPath %%a

rem  Set the classpath to include all jars in the Tomcat lib directory.
rem  This is where JDBC drivers should be installed.
for /R "%cathome%"\lib %%a in (*.jar) do call :appendClassPath %%a

rem  The Connector Manager webapp directory is the parent of WEB-INF.
for %%i in ( "%webinf%" ) do set managerdir=%%~dpi
if "%managerdir:~-1%" == "\" set managerdir=%managerdir:~0,-1%

rem  Run the program.
"%java%" -Dmanager.dir="%managerdir%" -Dcatalina.base="%catbase%" %classname% %*

:end
endlocal
goto :eof

:appendClassPath
set CLASSPATH=%*;%CLASSPATH%
goto :eof
