# Introduction #

Here you will find instructions about how to deploy Google Connector Manager in OracleAS 10g and, specifically, in the 10.1.3 version of the standalone JEE server called OC4J. This deployment has been done only for testing and demo purposes and it is not supported in any way.


# Requirements #

In order to successfully complete this deployment tutorial, you will need to download and install the following components:

  1. Google Connector Manager [binaries](http://google-enterprise-connector-manager.googlecode.com/files/connector-manager-1.0.3.zip) and [source code](http://google-enterprise-connector-manager.googlecode.com/files/connector-manager-1.0.3-src.zip)
  1. [Oracle Containers for Java EE (aka OC4J) 10.1.3](http://www.oracle.com/technology/tech/java/oc4j/index.html)
  1. [Oracle JDeveloper 10.1.3](http://www.oracle.com/technology/software/products/jdev/index.html)

All the instructions explained here has been done for and tested in Windows XP Professional operating system, although OC4J is a quite portable JEE engine and you should be able replicate the deployment in Linux or any other operating system supported by OC4J.

JDeveloper and OC4J are really easy to install. Just follow Oracle instructions.

# JDeveloper customization #

In order to successfully compile and deploy the connector manager in OC4J, you need to add  JUnit testing environment to JDeveloper. You can easily download it as a JDeveloper plugin, usint the _**Help->Check for Updates...**_ option of JDeveloper.

# Connector Compilation and deployment #

The easiest way to deploy and test the connector manager in OC4J is to create a war file with:

  * Connector manager and additional needed libraries.
  * Mock connector and third party components needed (JSON libraries, for example).
  * Connector configuration files

You will learn how to easily build that war file using JDeveloper, following the next steps:

  1. Unzip connector war file and extract the libraries included somewhere accesible from JDeveloper.
  1. Create a JDeveloper application with an empty JDeveloper project in it
  1. Add the libraries extracted in the 1st step to the project, right-clicking on the project name and choosing "Project Properties...", selecting libraries and clicking on "Add Jar/Directory..." button.
  1. Add JUnit Runtime, JCR Common Runtime and Servlet Runtime to the project, using the same procedure than in the previous step, but using "Add Library..." button.
  1. Uncompress the connector manager source file and look for the "javatests" folder. Copy all the following folders under your\_jdeveloperproject\src folder:
    * com\google\enterprise\connector\mock
    * com\google\enterprise\connector\jcr
    * com\google\enterprise\connector\pusher
  1. Create the following folders under your\_jdeveloperproject\src folder:
    * config
    * org\json
  1. Download JSON Java classes from [here](http://www.json.org/java/) and copy to the org\json folder created in the previous step.
  1. Find `connectorInstance.xml` and `connectorType.xml` files under `test-connectorA` folder in the connector manager source zip, and copy them under the "config" folder you created in the previous step.
  1. Find `applicationContext.xml` and `applicationContext.properties` under "etc" folder in the connector manager source code zip.
  1. Copy these two files under `your_jdeveloperproject\public_html\WEB-INF` folder. Edit `applicationContext.properties` file and substitute "localhost" by your favorite GSA host name or IP address.
  1. Extract `web.xml` file from the connector manager war file to `your_jdeveloperproject\public_html\WEB-INF` folder.
  1. In this point, you should be able to see all the files in your JDeveloper project. It is time to create a WAR file. Choose "File->New->Deployment Profile" in JDeveloper. Be sure that all the needed libraries are included under "WEB-INF/lib Contributors" section. You should check all of them but the Servlet Runtime.
  1. Deploy to war file, right-clicking on the WAR file deployment profile created in the previous step.

# Deployment #

At this point, you should have an OC4J instance up and running, after having followed Oracle instructions for that. The OC4J admin console should be available in: `http://OC4J_HOST:OC4J_PORT/em/console`. Using it, deploy the WAR file created in the previous section. We will assume that you chose connector-manager as web context for that war.

After that, find `MockRepositoryEventLog1.txt` in the connector manager source zip and copy it under `yourOC4JDirectory\j2ee\home\applications\connector-manager\connector-manager\WEB-INF` directory.

# GSA Configuration #

Simply follow the Connector configuration for the mock connector described in these steps of the Google Connector Develooper guide:

http://code.google.com/apis/searchappliance/documentation/50/connector_dev/cdg_buildtest.html#runcm
http://code.google.com/apis/searchappliance/documentation/50/connector_dev/cdg_buildtest.html#testconn

It should start feeding and serving.

# Even easier #

You can download a JDeveloper project from [this link](http://google-enterprise-connector-manager.googlecode.com/files/jdeveloper_connector_manager.zip) and simply unzip and use. Remember to change `localhost` by your GSA host name in the `applicationContext.properties` file before deploying.