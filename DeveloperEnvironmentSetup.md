# Introduction #

These instructions are intended for developers who want to develop a new connector for Google Search Appliance, using the Connector Manager or who want to work on the Connector Manager itself.

The steps in this document are suggestions, not requirements.  For example, Ant and Eclipse are not required, but that is the only scenario we describe here.  You are free to use whatever development tools you like!

## Prerequisites ##

Install a [Java 6 or 7 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html), [Apache Ant](http://ant.apache.org/) (version 1.8 or newer), [Eclipse](http://www.eclipse.org/), and [Apache Tomcat](http://tomcat.apache.org/) (version 7).  See those websites for download and installation instructions.

Install a subversion (svn) client.  See the [Connector Manager Code Site](http://code.google.com/p/google-enterprise-connector-manager/) (under the "Source" tab) for a list of svn clients.

For building tests, download the JSR-170 api: jcr-1.0.jar.  See the [JSR-170](http://jcp.org/en/jsr/detail?id=170) Site.

Note on the JDK version: the connector manager is designed so that it can be built and run using JDK 1.6.0 (Java 6), but it can also be built and run using Java 7.

## Checking out the Connector Manager Code ##

Create a new empty directory.  You may use any name for this directory, but for clarity we will use "google-enterprise-connector-manager" in the descriptions below.  Checkout the Connector Manager code into the "google-enterprise-connector-manager" directory.  Use the svn client of your choice.  Point the client to the Connector Manager Code Site, and supply your name and password if required.

## Directory Layout ##

Inside the `google-enterprise-connector-manager` directory, you will find the following directories (this list is not complete):

  * `projects/`
    * `connector-manager/`
      * `etc/`
      * `source/`
        * `java/`
        * `javatests/`
      * `testdata/`
      * `third-party/`
        * `cobertura/`
        * `prod/`
        * `tests/`
    * `test-connectorA/`
    * `test-connectorB/`

Those directories are instrumental in building the Connector Manager and the test connectors used by the tests.

There are several other ancillary projects as well:

  * `projects/`
    * `loadbalancer/`
    * `mime-util/`
    * `slf4j-stub/`

You generally will not need to build these and they are not built from the default `build.xml` file in the `projects` directory.  The `loadbalancer` project deploys an already built Connector Manager as 2 separate web applications within Tomcat to emulate a load-balanced environment.  The `mime-util` and `slf4j-stub` projects have patches to the third party Mime-Util package used by the Connector Manager.

## Copy the JCR Jar to the Right Place ##

**IMPORTANT**: Copy the jcr-1.0.jar to `google-enterprise-connector-manager/projects/connector-manager/third-party/tests/`.

## Verify that the Connector Manager Builds ##

Use a shell or command window and change directory into the `google-enterprise-connector-manager/projects` directory.  Run "ant" in that directory.  There should be no errors reported.  A common cause of problems here is not copying the `jcr-1.0.jar` to the proper place, using an old version of `ant` (ant version 1.8 or later is required), or not having a Java 6 or 7 JDK on the path.

Useful build targets are:

  * `clean-all`:  Clean connector-manager and test connector projects.

  * `build-all`:  Builds connector-manager and test connector projects.

  * `install-all`: Copies the built connector-manager and test connector to `dist` directory.

  * `test-all`: Run the JUnit tests using Cobertura code instrumentation.  Output is placed in the `projects/connector-manager/reports` directory.

  * `connector-manager-war-prod`: Builds a connector-manager.war file and places it in the `dist` directory.

  * `download-connector-manager`: Builds full download distribution archives, including war files, javadoc, licenses, release notes, etc. and places the output in the `downloads` directory.  These are the files that are uploaded to the Connector Manager codesite Downloads page.

## Advanced Test Configurations ##

When the tests are run from the `projects` directory, they are run using Cobertura code instrumentation.  This slows down the tests, approximately doubling the time taken to run them.

You can run the tests directly from the `projects/connector-manager` directory using the command "ant run\_tests".

You can run individual test classes by specifying the "test.suite" system property, indicating the test classname (less the "Test" part of the name).  For instance, suppose you wish to run the the `LogLevelTest` class, you would use:

` ant run_tests -Dtest.suite=LogLevel `

The Connector Manager tests have their own `logging.properties` file in `projects/connector-manager/testdata/config`.  The default logging level is `INFO`, but you might wish to increase the logging verbosity when running down test failures.

## Set up Eclipse Projects ##

For clarity, the following directions assume that you will create a new eclipse workspace for this development.  This is not required.  It is possible to use an existing eclipse workspace, but the steps you would then follow would be subtly different, depending on the state of your current workspace.

  1. Start up eclipse.  Point it to a new workspace.  This workspace should be a new directory, independent from the directory into which you checked out the connector-manager svn repository.
  1. Go to `Window->Preferences->Java->Compiler`.  Set Compiler Compliance Level to 1.6.
  1. Go to `Window->Preferences->Java->Installed JREs`.  Make sure you have a 1.6.0 JRE or JDK and make it the default for this workspace.
  1. Create the following three projects, `connector-manager`, `test-connectorA` and `test-connectorB`.  For each one, follow these steps (replace `project-name` appropriately.
    * `File->New->Project`.  Choose `Java Project` and click `Next`
    * Fill in the project-name.
    * Choose `Create project from existing source` and browse to the sub-directory under `projects` in your svn checkout directory whose name corresponds to the project-name.  For example, the directory for the `connector-manager` project is `connector-manager/projects/connector-manager`.
  1. There should be no errors shown although there may be warnings.  It may be necessary to clean the connector-manager project.  To do so, select that project, then choose `Project->Clean`
  1. Verify that all is well by running the unit tests through eclipse.  Right-click on the connector-manager project and choose `Run As->Junit Test`.  All tests should be green.

## Source Code Conventions ##

The coding style guidelines used in the project are based upon the general [Code Conventions for Java](http://java.sun.com/docs/codeconv/html/CodeConvTOC.doc.html), with additional guidelines as detailed below.

However, since development is done on both Microsoft Windows and Unix platforms, two conventions **must** be adhered to in order to avoid massive differences when merging code:
  1. Use spaces, not embedded tabs, for indenting code.
  1. All text files (including source code) added to the repository must include the `svn:eol-style=native` property (see below) to ensure that line-endings are converted properly on checkin and checkout.

Additional Coding Style Guidelines:
  * Indentation: 2 spaces, 4 spaces on a continuation, no tabs.
  * Line length: 80 columns
  * FileHeader: use standard style
  * Imports: Fully qualify imports
  * Imports: static, Google, third party alphabetical, java, javax
  * Javadoc: write it
  * Short methods: don't write giant methods
  * Fields: should either be at the top of the file, or immediately before the methods that use them
  * Local variables: limit the scope
  * Acronyms are words: Treat acronyms as words in names, yielding XmlHttpRequest, getUrl(), etc.
  * TODO style: "TODO(userid): Write this description."
  * Exceptions: Never catch and ignore them without explanation.
  * Exceptions: do not catch generic Exception.
  * Finalizers: generally don't use them
  * Consistency: Look at what's around you!

## Configuring Subversion Automatic Properties ##

To minimize cross platform development pain, all plain text files added to the repository should have a subversion `svn:eol-style=native` property set.  This is easily done with by configuring automatic properties in your subversion configuration file.  To do so:

  1. Edit your Subversion configuration file,  typically `~/.subversion/config`
  1. Enable automatic properties if it is not already enabled:
```
### Set enable-auto-props to 'yes' to enable automatic properties
### for 'svn add' and 'svn import', it defaults to 'no'.
### Automatic properties are defined in the section 'auto-props'.
enable-auto-props = yes
```
  1. Under the `[auto-props]` section of the file, add `svn:eol-style` and `svn:mime-type` properties for the various types of text files in the project:
```
## These are for Google Development
*.java = svn:eol-style=native;svn:mime-type=text/x-java
*.properties = svn:eol-style=native;svn:mime-type=text/plain
*.txt = svn:eol-style=native;svn:mime-type=text/plain
*.xml = svn:eol-style=native;svn:mime-type=text/xml
*.html = svn:eol-style=native;svn:mime-type=text/html
*.css = svn:eol-style=native;svn:mime-type=text/css
*.png = svn:mime-type=image/png
*.jpg = svn:mime-type=image/jpeg
*.gif = svn:mime-type=image/gif
```
  1. Save the configuration file.

## Configuring Tomcat for Connector Manager Deployment ##

The Connector Manager runs as a web application under Tomcat.  For ease of configuration, Tomcat v6.0.26 or newer should be used.  Download a binary distribution of Tomcat from the Apache site and unpack it.  Tomcat must then be configured slightly to enable Connector Manager ContextLogging.  Specifically, the connector-logging.jar file must be on the system classpath.  Tomcat can be configuration can often be altered using optional `$CATALINA_BASE/bin/setenv.sh` or `%CATALINA_BASE%\bin\setenv.bat` files.

Here is a minmal `setenv.sh` script:

```
CLASSPATH="$CLASSPATH":"$CATALINA_BASE/webapps/connector-manager/WEB-INF/lib/connector-logging.jar"
```

Here is an example `setenv.sh` script for my particular environment:

```
# Use Java 1.6 JDK
JAVA_HOME=/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home

# JVM memory config similar to the Google Connectors Installer
JAVA_OPTS="$JAVA_OPTS -Xms256m -Xmx1024m"

# Determine the Connector Manager directory
CONNECTOR_MANAGER=webapps/connector-manager/WEB-INF
if [ -n "$CATALINA_BASE" ] && [ -d "$CATALINA_BASE/$CONNECTOR_MANAGER" ]; then
  CONNECTOR_MANAGER="$CATALINA_BASE/$CONNECTOR_MANAGER"
else
  CONNECTOR_MANAGER="$CATALINA_HOME/$CONNECTOR_MANAGER"
fi

# Google Search Appliance Connector Logging Configuration
CONNECTOR_LOGGING_CONFIG="$CONNECTOR_MANAGER"/classes/logging.properties
if [ -f "$CONNECTOR_LOGGING_CONFIG" ] ; then
  # The Logging config for Tomcat 6.0.20 and later
  LOGGING_MANAGER="-Djava.util.logging.manager=java.util.logging.LogManager"
  LOGGING_CONFIG="-Djava.util.logging.config.file=$CONNECTOR_LOGGING_CONFIG"

  # Google Search Appliance Connector Logging must be on the system CLASSPATH
  # when using java.util.logging.FileHandler
  CONNECTOR_LOGGING="$CONNECTOR_MANAGER"/lib/connector-logging.jar
  if [ -f "$CONNECTOR_LOGGING" ] ; then
    CLASSPATH="$CLASSPATH":"$CONNECTOR_LOGGING"
  fi
fi

# Google Search Appliance Connector for Documentum Additions to CLASSPATH
if [ -n "$DOCUMENTUM_SHARED" ] ; then
  CLASSPATH="$CLASSPATH":"$DOCUMENTUM_SHARED"/dfc/dfc.jar:"$DOCUMENTUM_SHARED"/config
fi
```

The Google Connectors Installer also configures a Tomcat RemoteAddrValve in `$CATALINA_BASE/conf/server.xml` that limits access to the configured GSA and localhost.  I do not recommend this for development, especially when [switching between several GSAs](ChangeGSA.md).

## Deploying the Development Connector Manager under Tomcat ##

As mentioned previously, the `connector-manager-war-prod` ant task builds the Connector Manager WAR file that may be deployed as a Tomcat web application in the traditional manner.  The deployed WAR file will be unpacked into Tomcat with most of the interesting bits found in the `webapps/connector-manager/WEB-INF` directory.

The Connector Manager WAR file contains only the Connector Manager and most of its associated files.  It _does not_ contain any connectors.  Connectors may be manually deployed by installing their connector JAR files (and any required third-party JAR files) in the `webapps/connector-manager/WEB-INF/lib` directory.  There is a [tool](http://code.google.com/p/googlesearchapplianceconnectors/source/browse/#svn%2Ftrunk%2Fprojects%2Fgsa-connectors-war) that can be used to bundle a Connector Manager and several Connectors into a single WAR file for deployment, however its configuration is specific to GSA on-board deployments, rather than external Tomcat deployments.

The Connector Manager WAR file does not include two key components (besides connectors) that are installed by the [Google Connectors Installer](http://code.google.com/p/googlesearchapplianceconnectors/downloads/list):

  * `logging.properties`: A current TODO is to unify Connector logging, including having a single `logging.properties` supplied by the Connector Manager.  Currently, however, each Connector supplies its own `logging.properties` file, slightly different than the others, and the last one installed wins.  My best suggestion is to take a [logging.properties file from a connector](http://code.google.com/p/google-enterprise-connector-file-system/source/browse/trunk/projects/file-system-connector/config/logging.properties) you are using to test your Connector Manager.  The `logging.properties` file should be placed in `webapps/connector-manager/WEB-INF/classes` directory.  You **will** need to restart Tomcat after installing a new `logging.properties`.

  * `Manager` scripts: These are used invoke certain Connector Manager functions when run from the command line. (See EncryptPassword, DumpConnectors, MigrateStore).  These scripts are not necessarily unless you need run to one of these command line utilities.  (They are not needed for the web application to function.)  The `Manager` and `Manager.bat` scripts are found in `projects/connector-manager/source/scripts` directory.  The installer places them in the `Scripts` directory within the Connector installation.  However, you may install them in a convenient location as long as the `CATALINA_HOME` or `CATALINA_BASE` environment variable points to the Tomcat hosting the Connector Manager.

You can test to see if the Connector Manager is running properly by opening a browser and loading http://localhost:8080/connector-manager/testConnectivity (assuming Tomcat is running locally on its default port).  Firefox and Chrome tend to present the returned XML in a prettier fashion than IE or Safari.

## Updating a Connector Manager or Connector Deployment on Tomcat ##

If you are making changes to the Connector Manager source and wish to test them, it is not always necessary (or even desirable) to redeploy a full WAR file.  This runs the risk of overwriting any Connector Manager configuration, connector instances, and customized logging.properties.  It is often more convenient (and quicker) to copy the `connector*.jar` files into `webapps/connector-manager/WEB-INF/lib`.  If you modified `applicationContext.xml` or `web.xml`, you will need to copy those to  `webapps/connector-manager/WEB-INF`, as well.

You will need to shutdown Tomcat, copy over the new files, then restart Tomcat.  However, be aware that Tomcat actually takes some time shutting down, so you will need to wait 15 or 20 seconds after the shutdown request before restarting.  If you don't wait long enough Tomcat will complain about "port already in use" when restarting.

I just have a little script that does the {stop, sleep 15, copy, start} for me.  We should add a `deploy` target to the ant `build.xml` file that does this.

## Shared Packages, Build Files, and Third-party Components ##

Certain portions of the Connector Manager are shared with the Connectors and the GSA, so care should be taken when changing, moving, or removing them.

  * The packages `com.google.enterprise.connector.spi` and `com.google.enterprise.connector.util` are used by Connectors and the GSA.

  * Some classes in package `com.google.enterprise.connector.servlet` are used by the GSA.

  * JAR files in `projects/connector-manager/thirdparty` are used by Connectors.

  * Some ant build files my be included by connectors.  Currently only the `projects/svnbuild.xml` is used.  However, much of the Cobertura targets and config should be extracted as well to reduce the heavy redundancy within all the individual connector build files.

Theoretically, a well-behaved Connector should only need access to the Connector Manager's `connector-spi.jar` (and the third-party jars) to compile an run its tests.  However, not all connectors are so well behaved.  Several of them access internal Connector Manager classes when running tests (bad! _bad!_ connector), so don't be surprised if some random connector unit test breaks after you add a constructor arg to `DocPusher`.