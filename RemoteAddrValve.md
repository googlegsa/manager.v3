## Browser Access to Connector Manager v2.x Displays HTTP 403 Error or Blank Page ##

Connector Manager version 2.0 adds an HTTP address filter that restricts communication with the Connector Manager to either the machine running the Connector Manager (localhost) or its configured GSA.

When installing a new connector using the Google Connector Installer v2.0 (GCI) wizard, the Tomcat RemoteAddrValve is automatically configured to the GSA specified during the installation, plus localhost.

Attempts to access the Connector Manager from another machine will result in a HTTP 403 (Forbidden) error.  If accessed using a web browser, this error might displayed explicitly or it might be displayed as a blank page, depending upon the browser used.

If following instructions in the existing Connector Documentation that describe manipulating the Connector Manager using a web browser (or command line tools such as wget, curl, or telnet) you are now restricted to performing those actions from the machine running the Connector Manager itself.  The version 2.0 Documentation has not yet been updated to include this critical detail.

For instance, if manually restarting a Connector's traversal of a repository, the existing [documentation](http://code.google.com/apis/searchappliance/documentation/connectors/200/connector_admin/livelink_connector.html#RestartTraversal) mentions:

> To restart the traversal, open a browser and enter a URL in the following format, where `connector_manager_host_address` is the location of the connector manager and `connector_name` is the name of the connector whose traversal you are restarting:

> `http://connector_manager_host_address:8080/connector-manager/restartConnectorTraversal?ConnectorName=connector_name`

> For example, if the host address is `www.example.com` and the connector manager is named `our_connector`:

> `http://www.example.com:8080/connector-manager/restartConnectorTraversal?ConnectorName=our_connector`

With the default Connector Manager v2.x configuration, `connector_manager_host_address` must be `localhost` (or more specifically, 127.0.0.1), and the request must originate from the machine on which the Connector Manager is running<sup>*</sup>.  If direct access to the Connector Manager machine is inconvenient, Connector Administrators may wish to add administration machines to the list of IP addresses allowed by the RemoteAddrValve.

> <sup>*</sup> This is not completely true.  Google support personnel may access the Connector Manager from the GSA that the Connector Manager is configured to feed.


## Configuring the Tomcat RemoteAddrValve to Allow Administrator Access ##

The addition of the remote address filter severely impacts the local administrator's ability to troubleshoot Connector installations.  Connector administrators may wish to reconfigure the Connector's Tomcat RemoteAddrValve, allowing access from the administrator's local machine.

To modify the Tomcat RemoteAddrValve configuration:

  1. Shutdown the Connector's Tomcat server.<br><br>
<ol><li>On the Connector Manager server, edit the file:<br><code>$TOMCAT_HOME/conf/server.xml</code><br><br>
</li><li>Locate the RemoteAddrValve configuration line:<br><code>&lt;Valve className="org.apache.catalina.valves.RemoteAddrValve" allow="127.0.0.1,..." /&gt;</code><br><br>
</li><li>Add the IP addresses of the administrators' machines to the list of allowed addresses.<br>The Remote Address Filter configuration allows regular expressions.<br>For details, see the <a href='http://tomcat.apache.org/tomcat-6.0-doc/config/valve.html'>Apache Tomcat Valve</a> documentation.<br><br>
</li><li>Save the file.<br><br>
</li><li>Restart the Connector's Tomcat server.<br><br></li></ol>

<b>Note:</b> <code>$TOMCAT_HOME</code> represents the Apache Tomcat installation directory. For Connectors installed using GCI, this would be the Tomcat directory in the Connector Installation.<br>
<br>
<h2>See Also</h2>

<a href='ChangeGSA.md'>Changing the GSA Feed Host for Connector Manager v2.x</a> describes reconfiguring the Tomcat RemoteAddrValve when changing  GSA Feed Host.<br>
<br>
The <a href='http://tomcat.apache.org/tomcat-6.0-doc/config/valve.html'>Apache Tomcat Valve</a> documentation.