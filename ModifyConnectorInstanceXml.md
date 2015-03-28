## Introduction ##

A Connector's `connectorInstance.xml` configuration file contains
advanced implementation-specific configuration, as well
as Spring Framework bean configuration for a Connector
deployment.

Currently, a Connector's `connectorInstance.xml` configuration file
is not directly modifiable using the Search Appliance Admin Console.
When a connector's configuration is stored in files within the web
application filesystem, manual modifications to these files could
easily be done using any plain text editor.
However, when a connector's configuration is stored in a JDBC
database, modifying that configuration manually becomes non-trivial.

This page discusses the steps needed to export a connector's
configuration from the database, manually edit it, then re-import
the modified configuration into the database.

Doing so requires direct access to the computer hosting a
Google Connector Manager for the deployed Connector, with
sufficient rights to modify files in the Connector Manager
Tomcat web application directory.

## Modifying a Connector's `connectorInstance.xml` Configuration ##

These example makes the following assumptions:
  * The name of the connector deployment is `MyGoogleConnector`, the name of the connector instance is `myconnector`, and that connector is of type `SomeConnectorType`.<br><br>
<ul><li>The default <code>JdbcPersistentStore</code> is used to store connector configuration data.  If you are using a customized external database configuration in <code>applicationContext.xml</code>, for instance <code>MyCompanyOraclePersistentStore</code>, then use that specific PersistentStore in the following instructions.  If you are unsure of the available PersistentStore configurations, run MigrateStore in interactive mode for assistance.<br><br></li></ul>


<ol><li>Disable traversal for the connector:<br>In the GSA Amin UI, go to <b>Connector Administration > Connectors</b>, click the <b>Edit</b> link for the desired connector, check the <b>Disable Traversal</b>  check box, then click the <b>Save Configuration</b> button.<br><br>
</li><li>On the computer that contains the connector deployment, launch a command shell, then change directory to the <code>Scripts</code> directory within the connector deployment:<br><code>$ cd /path/to/MyGoogleConnector/Scripts</code><br><br>
</li><li>Export the connector's configuration to the local file system using the MigrateStore command:<br><code>$ ./Manager MigrateStore --force -connector myconnector JdbcPersistentStore FilePersistentStore</code><br><br>
</li><li>Change directory to the connector's exported configuration:<br><code>$ cd ../Tomcat/webapps/connector-manager/WEB-INF/connectors/SomeConnectorType/myconnector</code><br><br>
</li><li>Edit <code>connectorInstance.xml</code> with a plain text editor.  This file must remain a plain text UTF-8 encoded file.  Do not use a word processor that will convert the file to some proprietary word processing format. Save the file.<br><br>
</li><li>Import the connector's modified configuration to the database using the MigrateStore command:<br><code>$ ./Manager MigrateStore --force -connector myconnector FilePersistentStore JdbcPersistentStore</code><br><br>
</li><li>Reset the index for that connector on the GSA (Optional).  Most changes to advanced connector configuration in <code>connectorInstance.xml</code> will require re-indexing all content for that connector.  If you know this is not necessary, you may skip this step.  If you are unsure, you should reset the index:<br>In the GSA Amin UI, go to <b>Connector Administration > Connectors</b> and click the <b>Reset</b> link for the target connector.<br><br>
</li><li>Re-enable traversal for the connector:<br>In the GSA Amin UI, go to <b>Connector Administration > Connectors</b>, click the <b>Edit</b> link for the desired connector, check the <b>Disable Traversal</b>  uncheck box, then click the <b>Save Configuration</b> button.<br><br></li></ol>

<hr />
<i>Since Connector Manager v3.0</i>