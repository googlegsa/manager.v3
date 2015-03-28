#Quick reference on how to use H2 database embedded in the connector manager.

# Introduction #

This is a set of simple instructions on how to use the H2 database management console to browse the connector manager's built-in database.


# Details #




## Installation ##

Install the software using the Windows Installer (if you did not yet do that).
Download Link - http://www.h2database.com/html/main.html

## Start the Console ##
```
Click [Start], [All Programs], [H2], and [H2 Console (Command Line)]:
```


A new console window appears.

Also, a new browser page should open with the URL http://localhost:8082. You may get a security warning from the firewall. If you don't want other computers in the network to access the database on your machine, you can let the firewall block these connections. Only local connections are required at this time.


## Login ##

#NOTE - You may need to stop the connector service to view the user datastore
  1. Select - Generic H2 (Embedded)
  1. Leave Driver Class at default value  - org.h2.Driver
  1. Set JDBC URL
  * Locate the datastore...
```
/Tomcat/webapps/connector-manager/WEB-INF/connector_manager.dbstore/connector-manager
```
  * You can also find relevant information in the applicationContext.xml file
  * Look for “JDBC DataSource configuration”
```
<prop key="jdbc.datasource.h2.url">jdbc:h2:${catalina.base}/webapps/connector-manager/WEB-INF/connector_manager.dbstore/connector-manager;AUTO_SERVER=TRUE;TRACE_LEVEL_FILE=1;MVCC=TRUE;CACHE_SIZE=131072;MAX_OPERATION_MEMORY=0</prop>
```
  * Specify the URL (this is just an example):
```
jdbc:h2:file:C:/Program Files (x86)/GoogleConnectors/Sharepoint1/Tomcat/webapps/connector-manager/WEB-INF/connector_manager.dbstore/connector-manager;AUTO_SERVER=TRUE;
```

  * By default the Username is “sa” and password is blank

  1. Click Connect
  1. You are now logged in.
  1. Please note that this tool doesn’t complain if it cannot find the datastore you specified. It will not complain. If you don’t see those tables mentioned below, that’s a sign.

## Viewing the User Group Membership Table ##

  1. You should now see the USER\_GROUP\_MEMBERSHIP table
  1. You can click on the table which will generate the following SQL statement “SELECT **FROM USER\_GROUP\_MEMBERSHIPS ” or type it directly in and hit Run
  1. You will see a table.
  1. The table contains the username (SPUSERNAME), group name (SPGROUPNAME) and SharePoint Site (SPSITE)
  1. Also note that the SPUSERNAME contains the domain name to ensure the domain is set when configuring the connector for ACLs change the “Username Format in ACE” to domain\username
TO BE CONFIRMED (use at your own risk for now):**

  1. To delete User data store related tables and index run the following SQL statement

```
TRUNCATE TABLE User_Group_Memberships;
```

# H2 DB browsing #

```
java -cp C:\wherever\tomcat\webapps\connector-manager\WEB-INF\lib\h2.jar org.h2.tools.Console
```

It will start a server on local port 8082 and you can browse the database on http://localhost:8082/

## Using Command line: ##

c:\Program Files (x86)\H2\bin>java -cp h2**.jar org.h2.tools.Shell
You will be prompted with URL, driver user name/password.
Remember to put “;” at the end of each query you issue.**

## Common Queries ##

Find a user

```
select * from user_group_memberships where spusername like '%username%’
```

## Case sensitivity - 2.x (3.x not case sensitive) ##


The lookup of local SharePoint User Group Memberships by the SharePoint connector is case sensitive.

Depending on whether customers have usernames that use a mix of lowercase and uppercase identifiers, this is a problem.  It is also a problem when the authentication mechanisms returns a Domain/Username that is not in the same case as the entry that is stored in the membership table.

In the case where H2 is being used as the database for the group membership table, the following workaround has been implemented by a partner:

Edit the applicationContext.xml file and add ;IGNORECASE=TRUE to the jdbc declaration.

And yes, IGNORECASE=TRUE takes effect only on database creation
and doesn't affect existing databases.

In order to let the connector to recreate the database, please stop the
connector (Tomcat), remove the db file(**), then restart the connector (Tomcat) again.**

(**)
webapps/connector-manager/WEB-INF/connector\_manager.dbstore/connector-manager.h2.db**

Then it IGNORECASE=TRUE will kick in and you are all set.


if you haven't already deleted the DB, there is a way to change the existing DB:

```
ALTER TABLE USER_GROUP_MEMBERSHIPS ALTER COLUMN SPUSERNAME VARCHAR_IGNORECASE(256);
```


## DB Backup ##

Reference: http://www.h2database.com/html/tutorial.html#upgrade_backup_restore

A command like:

```
java -cp "C:\Program Files (x86)\GoogleConnectors_2_8_All\GSAConnectors3\Tomcat\webapps\connector-manager\WEB-INF\lib\h2.jar" org.h2.tools.Shell -url "jdbc:h2:file:C:/Program Files (x86)/GoogleConnectors_2_8_All\GSAConnectors3/Tomcat/webapps/connector-manager/WEB-INF/connector_manager.dbstore/connector-manager;AUTO_SERVER=TRUE" -user sa -sql "BACKUP TO 'c:\backup2.zip'"
```

Will backup the DB in live mode (while it is running) to a zip.  This zip can then restore from scratch an H2 database.

This can be scheduled to run via a scheduled 