**Version Info:**
Connector Manager 2.6.9, build 2727: Required for JBoss.
JBoss5.1.0.GA
Open JDK Java 1.6


1) Download JBoss5.1.0.GA

2) Build and deploy JBoss.
When running JBoss, you may have want to use
```
./run.sh -b 0.0.0.0 
```
to start up the jboss if you are planning on connecting to the server remotely and not using http://localhost:port

3) Download CM 2.6.9, build 2727 from:
https://docs.google.com/a/google.com/leaf?id=0Bxidh_UXp6waOGQzN2IzMzMtYzQ0Ny00MWE5LTkyZjktMTA0NWIyYzQ0NTA4&hl=en&authkey=CMqDgtgB

4) Deploy the extracted war file (connector-manager.war)

5) Updated applicationContext.xml to use jboss.server.home.dir and  instead of catalina.base

```
<prop key="feed.logging.FileHandler.pattern">${jboss.server.home.dir}/logs/google-connectors.feed%g.log</prop>


<prop key="jdbc.datasource.url">jdbc:h2:${jboss.server.home.dir}/webapps/connector-manager/WEB-INF/connector_manager.dbstore/connector-manager</prop>
```

Update the log directory in to use **logs** -- default directory in jboss.

6) Connect using the following url: http://servername:port/connector-manager