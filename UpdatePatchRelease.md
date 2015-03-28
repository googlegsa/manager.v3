## Introduction ##

Occasionally we release a minor updates or patch releases to the Connector Manager.  These releases usually fix critical bugs or customer issues found since the previous release and it is strongly recommended that customers
using the previous release of Connector Manager upgrade as soon as possible.

This document provides instructions for manually applying a new Connector Manager patch over a previously installed
Connector Manager.  This procedure works best when applying patch releases (version changes in the last digit - for instance, v2.0.0 -> v2.0.2).   It is **not** recommended for major upgrades (for instance v1.3.2 -> v2.0.2).


## Prerequisites ##

Before applying an update you should have the following available to you.
  * Access to the computer running the Connector Manager and its Tomcat application server.  You will need sufficient access rights to start and stop Tomcat and modify files in its deployment directory.
  * A binary distribution of a Connector Manager patch release, available on the [Downloads page](http://code.google.com/p/google-enterprise-connector-manager/downloads/list).
  * Familiarity with the command line environment of the deployment computer (cmd.exe on Windows or a shell environment on Unix/Linux).

**Note:** These instructions use environment variable syntax as an allusion. These are not actual environment variables, and you must substitute the actual path when entering the commands.

## Updating a Connector Manager deployed on Windows ##

**Note:** `%CATALINA_HOME%` refers to the Apache Tomcat deployment directory for your Connector installation.  If you installed the Connector using the Google Connector Installer (GCI), then `%CATALINA_HOME%` will be the `Tomcat` directory inside the Connector Installation (for instance: `C:\GoogleConnectors\myConnector\Tomcat`).

You will need to stop and restart the Tomcat web application server during the upgrade.  The instructions here assume that Tomcat is running as a **Windows Service**.  The instructions for doing so differ if Tomcat is running as a console application.  You would typically stop and start the service via the `Windows->Settings->Control Panel->Administrative Tools->Services` control panel.

  1. Download the _Google Connector Manager Binary Distribution_ from the [Downloads page](http://code.google.com/p/google-enterprise-connector-manager/downloads/list), and unzip it.  Note the location of the `connector-manager.war` file that was extracted from the binary distribution.  These notes will use `%WAR_DIR%` to refer to the directory that contains the `connector-manager.war` file.
  1. Shutdown the Tomcat web application server, either via the Services control panel, by using the `Stop_*_Connector_Console` command in the Connector installation directory, or by using Tomcat's `shutdown` or  `stopService` command.
```
cd %CATALINA_HOME%\bin
stopService.bat
```
  1. Make a backup copy of the Connector Manager configuration properties file %CATALINA\_HOME%\webapps\connector-manager\WEB-INF\applicationContext.properties.
```
cd %CATALINA_HOME%\webapps\connector-manager\WEB-INF
copy applicationContext.properties applicationContext.properties.backup
```
  1. Deploy the new `connector-manager.war` over the old installation.  This is done by using the Java `jar` command to unpack the `war` file directly into the Tomcat deployment.  **WARNING:** Some versions of the Google Connector Installer mark the installed connector JAR files as Read-Only.  Write permission for these files must be restored before deploying the patch.
```
cd %CATALINA_HOME%\webapps\connector-manager
attrib -r WEB-INF\lib\*.jar
jar xvf %WAR_DIR%\connector-manager.war
```
  1. Restore the Connector Manager configuration properties file that was backed up earlier.
```
cd %CATALINA_HOME%\webapps\connector-manager\WEB-INF
copy applicationContext.properties.backup applicationContext.properties
```
  1. Restart the Tomcat web application server, either via the Services control panel, by using the `Start_*_Connector_Console` command in the Connector installation directory, or by using Tomcat's `startup` command or `startService` command.
```
cd %CATALINA_HOME%\bin
startService.bat
```

## Updating a Connector Manager deployed on Unix or Linux ##

**Note:** `${CATALINA_HOME}` refers to the Apache Tomcat deployment directory for your Connector installation.  If you installed the Connector using the Google Connector Installer (GCI), then `${CATALINA_HOME}` will be the
`Tomcat` directory inside the Connector Installation (for instance: `~/GoogleConnectors/myConnector/Tomcat`).

The syntax used here is Bash shell syntax.  You may need to adjust the syntax appropriately if you are using a different command shell.

  1. Download the _Google Connector Manager Binary Distribution_ from the [Downloads page](http://code.google.com/p/google-enterprise-connector-manager/downloads/list), and unzip it.  Note the location of the `connector-manager.war` file that was extracted from the binary distribution.  These notes will use `${WAR_DIR}` to refer to the directory that contains the `connector-manager.war` file. GNU tar was used to generate the archive, and should also be used to extract its contents.
```
wget http://google-enterprise-connector-manager.googlecode.com/files/connector-manager-2.0.2.tar.gz
tar xzf connector-manager-2.0.2.tar.gz
WAR_DIR=`pwd`/connector-manager-2.0.2
```
  1. Shutdown the Tomcat web application server either by using the `Stop_*_Connector_Console` command in the Connector installation directory, or by using Tomcat's `shutdown` command.
```
cd ${CATALINA_HOME}/bin
./shutdown.sh
```
  1. Make a backup copy of the Connector Manager configuration properties file ${CATALINA\_HOME}/webapps/connector-manager/WEB-INF/applicationContext.properties.
```
cd ${CATALINA_HOME}/webapps/connector-manager/WEB-INF
cp applicationContext.properties applicationContext.properties.backup
```
  1. Deploy the new `connector-manager.war` over the old installation.  This is done by using the Java `jar` command to unpack the `war` file directly into the Tomcat deployment.  **WARNING:** Some versions of the Google Connector Installer mark the installed connector JAR files as Read-Only.  Write permission for these files must be restored before deploying the patch.
```
cd ${CATALINA_HOME}/webapps/connector-manager
chmod +w WEB-INF/lib/*.jar
jar xvf ${WAR_DIR}/connector-manager.war
```
  1. Restore the Connector Manager configuration properties file that was backed up earlier.
```
cd ${CATALINA_HOME}/webapps/connector-manager/WEB-INF
cp applicationContext.properties.backup applicationContext.properties
```
  1. Restart the Tomcat web application server, either by using the `Start_*_Connector_Console` command in the Connector installation directory, or by using Tomcat's `startup` command.
```
cd ${CATALINA_HOME}/bin
./startup.sh 
```