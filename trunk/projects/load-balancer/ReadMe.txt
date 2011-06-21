Introduction
------------
This is a quick and dirty load-balancer emulation for
the Connector Manager.  It is intended for use by
Connector Manager and Connector developers to simulate
a pair of Connector Managers running in a high-availability
environment. This deployment is not really highly available
at all.  It is, however, easy to set up and maintain as it
uses a single Tomcat installation and requires no external
load balancer (such as Apache HTTPD with mod-proxy-balancer).

Architecturally, this deployment consists of a single
Apache Tomcat instance running two distinct Connector
Manager web applications: connector-manager-1 and
connector-manager-2.  Although they are separate web apps,
they share a common keystore and connector configuration
database.  The shared files are kept in the directory
${catalina.base}/shared/connector-manager.

The two Connector Managers are configured slightly
differently: connector-manager-1 is configured to be a
traversing Connector Manager, whereas connector-manager-2
has traversing disabled.

This installation also deploys a LoadBalancerValve
into Tomcat.  The filter is configured to take incoming
requests to 'connector-manager' and forward them to
either 'connector-manager-1' or 'connector-manager-2',
in a round-robin fashion.


Quick Start
-----------
This deployment is intended to be laid down on top
of a fresh Tomcat 6 installation (I used v6.0.32 to
develop this).  You may download Tomcat from:
http://tomcat.apache.org/download-60.cgi

*DO NOT* deploy this on top of a Google Connectors
Installer (CGI) Tomcat installation.

Deployment is done via an ant task from this directory.
It requires ant v1.8.0 or newer, available from:
http://ant.apache.org/bindownload.cgi

You must supply two properties specific to your
installation:

catalina.base - the base of the Tomcat installation
gsa.feed.host - the hostname or IP addr of the feed host

For example:
ant -Dgsa.feed.host=10.1.1.196 -Dcatalina.base=/path/to/apache-tomcat-6.0.32


Warning: Configuring GSA Host
-----------------------------
Unlike Connector configurations, Connector Manager
configuration is *not* shared or replicated between
the two Connector Managers.  You must manually set
the gsa.feed.host property in each Connector Manager's
applicationContext.properties file.  If you register
the Connector Manager via the GSA, only one of the
Connector Manager instances will get reconfigured.


Warning: Running Command Line Apps
----------------------------------
The Connector Manager command line applications
(invoke by the Manager or Manager.bat scripts),
make an assumption as to the name of the Connector
Manager web application.  You must therefore
specify the Connector Manager web application
location to the Manager script:

Manager -Dmanager.dir=/path/to/tomcat/webapps/connector-manager-1 ...


Separate Logging Configurations
-------------------------------
The separate Connector Managers also have distinct logging
configurations, directing their output to separate logs.
Connector logs will be written to
${catalina.base}/logs/google-connectors-1.*.log
and ${catalina.base}/logs/google-connectors-2.*.log.
Similarly, feed logs will be written to
${catalina.base}/logs/google-connectors-1.feed*.log and
${catalina.base}/logs/google-connectors-2.feed*.log.

Also be aware that in order to support separate logging
configurations, this deployment does not hack the Tomcat
startup to use a single java.util.logging.LogManager
(as the GCI deployments do).  This deployment installs
a logging.properties that is largely a superset of the
individual logging.properties installed by GCI.

Note that each Connector Manager web application has
its own /lib directory containing the connector jar
files.  This is done to facility separate logging.
However, it requires the developer to keep them in
sync.  If you use a script to deploy freshly built
jar files to your development Tomcat installation,
it should be altered to copy the jar files to both
Connector Manager deployments.


Installation Details
--------------------
The deployment modifies the Tomcat installation as
follows:

Tomcat configuration:
 - Adds a setenv.sh to ${catalina.base}/bin.
   This includes a couple of Connector-specific
   configurations.  Developers may wish to modify this -
   specifically to set JAVA_HOME or add external
   jars to the classpath (as is done with Documentum).

 - Adds a shared directory for use by the Connector
   Managers: ${catalina.base}/shared/connector-manager

LoadBalancerValve configuration :
 - Adds balancer-valve.jar to ${catalina.base}/lib
 - Injects LoadBalancerValve configuration into
   ${catalina.base}/conf/server.xml
   It is unlikely that you have modified this,
   but be aware.
 - Adds "crossContext=true" attibute to the Context
   element in ${catalina.base}/conf/context.xml
   It is unlikely that you have modified this,
   but be aware.

Connector Manager configuration:
 - Creates ${catalina.base}/shared/connector-manager directory
 - Adds CM third part jars, particularly h2.jar to
   ${catalina.base}/lib
 - Adds connector-logging.jar to ${catalina.base}/lib
 - Deploys Connector Managers as
   ${catalina.base}/webapps/connector-manager-1 and
   ${catalina.base}/webapps/connector-manager-2

 - This *does not* configure a RemoteAddrValve.
   If you wish to replicate a GCI install, you may
   wish to configure a RemoteAddrValve.

The deployment modifies these Connector Manager files
from their default values.  Extreme care should be
taken when installing modified versions of these files
so as to avoid losing these configuration changes:

 - ${catalina.base}/webapps/connector-manager-1/WEB-INF/web.xml
 - ${catalina.base}/webapps/connector-manager-2/WEB-INF/web.xml
    - Sets keystore-file location to shared directory.
      This change is identical for each web app.

 - ${catalina.base}/webapps/connector-manager-1/WEB-INF/applicationContext.xml
 - ${catalina.base}/webapps/connector-manager-2/WEB-INF/applicationContext.xml
    - Feed logging configuration. This is different
      for each web app.

 - ${catalina.base}/webapps/connector-manager-1/WEB-INF/applicationContext.properties
 - ${catalina.base}/webapps/connector-manager-2/WEB-INF/applicationContext.properties
    - Sets the specified gsa.feed.host.
    - Sets H2 JDBC Database location to shared directory.
    - Sets config.change.detect.interval to 60 seconds.
    - Sets traversal.enabled to true for connector-manager-1
      and false for connector-manager-2.

The deployment adds a logging.properties to each
deployed Connector Manager.  This logging.properties
is configured uniquely for each Connector Manager,
so take care if modifying or replacing it.

 - ${catalina.base}/webapps/connector-manager-1/WEB-INF/classes/logging.properties
 - ${catalina.base}/webapps/connector-manager-2/WEB-INF/classes/logging.properties

