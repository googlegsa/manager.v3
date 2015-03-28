# Changing the GSA Feed Host for Connector Manager v2.x #

Connector Manager version 1.3 implemented a latch property
that attempts to prevent malicious re-assignments of the GSA
feed host.  Connector Manager version 2.0 adds an HTTP address
filter that restricts communication with the Connector Manager
to either the machine running the Connector Manager (localhost)
or its configured GSA.

When installing a new connector using the Google Connector
Installer v2.0 (GCI) wizard, the GSA Feed Host is automatically
configured to the GSA specified during the installation.

However, associating a Connector Manager with a different
GSA now requires four (4) separate manual configuration steps.
The Connector Manager is now more secure, at the expense of
the Connector Administrator and Support Staff.

If the Administrator wishes to change a Connector Manager
Feed Host, he must do the following:

  1. Shutdown the Connector's Tomcat server.<br><br>
<ol><li>On the Connector Manager server, edit the file:<br><code>$TOMCAT_HOME/conf/server.xml</code><br>Locate the RemoteAddrValve configuration line:<br><code>&lt;Valve className="org.apache.catalina.valves.RemoteAddrValve" allow="127.0.0.1,..." /&gt;</code><br>Either replace the old GSA IP address with the new GSA IP address, or simply add the new GSA IP address to the list of allowed addresses.  The Remote Address Filter configuration allows regular expressions.  For details, see the <a href='http://tomcat.apache.org/tomcat-6.0-doc/config/valve.html'>Apache Tomcat Valve</a> documentation.<br><br>
</li><li>On the Connector Manager server, edit the file:<br><code>$TOMCAT_HOME/webapps/connector-manager/WEB-INF/applicationContext.properties</code><br>Locate the <code>manager.locked</code> property and change its value to <code>false</code>.<br><br>
</li><li>Restart the Connector's Tomcat server.<br><br>
</li><li>On the new GSA Admin UI, go to <b>Crawl and Index > Feeds</b> page and add the IP address of the Connector Manager to the <b>List of Trusted IP Addresses</b>.<br><br>
</li><li>On the new GSA Admin UI, go to <b>Connector Administration > Connector Managers</b> and add the Connector Manager.</li></ol>

<b>Note:</b> <code>$TOMCAT_HOME</code> represents the Apache Tomcat installation directory. For Connectors installed using GCI, this would be the Tomcat directory in the Connector Installation.<br>
<br>
<h2>See Also</h2>

<a href='RemoteAddrValve.md'>Browser Access to Connector Manager v2.x Displays HTTP 403 Error or Blank Page</a> describes how the Tomcat RemoteAddrValve hampers troubleshooting Connector deployments and how to configure administrator access to the Connector Manager.<br>
<br>
<a href='AdvancedConfiguration.md'>Connector Manager Advanced Configuration</a>.<br>
<br>
The <a href='http://tomcat.apache.org/tomcat-6.0-doc/config/valve.html'>Apache Tomcat Valve</a> documentation.