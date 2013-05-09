// Copyright 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.manager;

import com.google.common.base.Strings;
import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.instantiator.SpringInstantiator;
import com.google.enterprise.connector.instantiator.ThreadPool;
import com.google.enterprise.connector.pusher.DocPusherFactory;
import com.google.enterprise.connector.pusher.GsaFeedConnection;
import com.google.enterprise.connector.scheduler.TraversalScheduler;
import com.google.enterprise.connector.spi.SimpleTraversalContext;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.traversal.ProductionTraversalContext;
import com.google.enterprise.connector.util.database.JdbcDatabase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static services for establishing the application context. This consists of
 * configuration, instantiating singletons, start up, etc.
 * This code supports two context types: servlet (as a web application within
 * an application server) and standalone.
 * When we run junit tests, we use a standalone context.
 * Use the methods setStandaloneContext and setServletContext to select the
 * context type.
 * <p>
 * Also the interface used for event publishing.  Wraps the event publishing
 * functionality of the established context.
 */
// TODO (jlacey): Context and ConnectorCoordinatorImpl are dangerously close
// to encountering deadlock issues, calling each other from synchronized 
// methods.  The most likely scenerio for deadlock would probably be when
// registering the CM with a new GSA.  Be wary when adding addition
// synchronization to these classes.
public class Context {
  public static final String GSA_FEED_PROTOCOL_PROPERTY_KEY =
      "gsa.feed.protocol";
  public static final String GSA_FEED_HOST_PROPERTY_KEY = "gsa.feed.host";
  public static final String GSA_FEED_PORT_PROPERTY_KEY = "gsa.feed.port";
  public static final String GSA_FEED_PORT_DEFAULT = "19900";
  public static final String GSA_FEED_VALIDATE_CERTIFICATE_PROPERTY_KEY =
      "gsa.feed.validateCertificate";
  public static final String GSA_FEED_VALIDATE_CERTIFICATE_DEFAULT = "false";

  /** Indicates that the HTTPS port has not been set or should not be used. */
  public static final int GSA_FEED_SECURE_PORT_INVALID = -1;
  public static final String GSA_FEED_SECURE_PORT_PROPERTY_KEY =
      "gsa.feed.securePort";
  public static final String GSA_FEED_SECURE_PORT_DEFAULT = "19902";

  public static final String GSA_ADMIN_REQUIRES_PREFIX_KEY =
      "gsa.admin.requiresPrefix";
  public static final Boolean GSA_ADMIN_REQUIRES_PREFIX_DEFAULT =
      Boolean.FALSE;

  public static final String TEED_FEED_FILE_PROPERTY_KEY = "teedFeedFile";
  public static final String MANAGER_LOCKED_PROPERTY_KEY = "manager.locked";

  public static final String FEED_CONTENTURL_PREFIX_PROPERTY_KEY =
      "feed.contenturl.prefix";
  public static final String FEED_CONTENTURL_SERVLET = "/getDocumentContent";
  public static final String FEED_DISABLE_INHERITED_ACLS =
      "feed.disable.inherited.acls";

  public static final String DEFAULT_JUNIT_CONTEXT_LOCATION =
      "testdata/mocktestdata/applicationContext.xml";
  public static final String DEFAULT_JUNIT_COMMON_DIR_PATH =
      "testdata/mocktestdata/";

  /**
   * Id of the Spring Bean used to declare the order services are to be loaded.
   */
  public static final String ORDERED_SERVICES_BEAN_NAME = "OrderedServices";
  private static final String APPLICATION_CONTEXT_PROPERTIES_BEAN_NAME =
      "ApplicationContextProperties";

  /** This is the comment written to the ApplicationContextProperties file. */
  private static final String CONNECTOR_MANGER_CONFIG_HEADER =
      " Google Search Appliance Connector Manager Configuration\n"
      + "\n"
      + " The 'gsa.feed.protocol' property specifies the URL protocol for\n"
      + " the feed host on the GSA. The supported values are 'http' and\n"
      + " 'https'.\n"
      + " For example:\n"
      + "   gsa.feed.protocol=http\n"
      + " gsa.feed.protocol=http\n"
      + "\n"
      + " The 'gsa.feed.host' property specifies the host name or IP address\n"
      + " for the feed host on the GSA.\n"
      + " For example:\n"
      + "   gsa.feed.host=172.24.2.0\n"
      + "\n"
      + " The 'gsa.feed.port' property specifies the HTTP host port for the\n"
      + " feed host on the GSA.\n"
      + " For example:\n"
      + "   gsa.feed.port=19900\n"
      + "\n"
      + " The 'gsa.feed.securePort' property specifies the HTTPS host port\n"
      + " for the feed host on the GSA. This port will be used if the\n"
      + " 'gsa.feed.protocol property' is set to 'https'.\n"
      + " For example:\n"
      + "   gsa.feed.securePort=19902\n"
      + "\n"
      + " The 'gsa.feed.validateCertificate' property specifies whether to\n"
      + " validate the GSA certificate when sending SSL feeds. If the GSA\n"
      + " certificate is installed in the Tomcat keystore, this should be\n"
      + " set to 'true', otherwise it must be set to 'false'.\n"
      + " For example:\n"
      + "   gsa.feed.validateCertificate=false\n"
      + "\n"
      + " The 'manager.locked' property is used to lock out the Admin Servlet\n"
      + " and prevent it from making changes to this configuration file.\n"
      + " Specifically, the ability to set the FeedConnection properties will\n"
      + " be locked out.  If it is set to 'true' or missing the Servlet will\n"
      + " not be allowed to update this file.\n"
      + " NOTE: This property will automatically be changed to 'true' upon\n"
      + " successful update of the file by the Servlet.  Therefore, once the\n"
      + " FeedConnection properties are successfully updated by the Servlet\n"
      + " subsequent updates will be locked out until the flag is manually\n"
      + " reset to 'false'.\n"
      + " For example:\n"
      + "   manager.locked=false\n"
      + "\n"
      + " The 'feedLoggingLevel' property controls the logging of the feed\n"
      + " record to a log file.  The log record will contain the feed XML\n"
      + " without the content data.  Set this property to 'ALL' to enable feed\n"
      + " logging, 'OFF' to disable.  Customers and developers can use this\n"
      + " functionality to observe the feed record and metadata information\n"
      + " the connector manager sends to the GSA.\n"
      + " For example:\n"
      + "   feedLoggingLevel=OFF\n"
      + "\n"
      + " If you set the 'teedFeedFile' property to the name of an existing\n"
      + " file, whenever the connector manager feeds content to the GSA, it\n"
      + " will write a duplicate copy of the feed XML to the file specified by\n"
      + " the teedFeedFile property.  GSA customers and third-party developers\n"
      + " can use this functionality to observe the content the connector\n"
      + " manager sends to the GSA and reproduce any issue which may arise.\n"
      + " NOTE: The teedFeedFile will contain all feed data sent to the GSA,\n"
      + " including document content and metadata.  The teedFeedFile can\n"
      + " therefore grow quite large very quickly.\n"
      + " For example:\n"
      + "   teedFeedFile=/tmp/CMTeedFeedFile"
      + "\n"
      + " The 'feed.timezone' property defines the default time zone used\n"
      + " for Date metadata values for Documents.  A null or empty string\n"
      + " indicates that the system timezone of the machine running the\n"
      + " Connector Manager should be used.  Standard TimeZone identifiers\n"
      + " may be used.  For example:\n"
      + "   feed.timezone=America/Los_Angeles\n"
      + " If a standard TimeZone identifier is unavailable, then a custom\n"
      + " TimeZone identifier can be constructed as +/-hours[minutes] offset\n"
      + " from GMT.  For example:\n"
      + "   feed.timezone=GMT+10    # GMT + 10 hours\n"
      + "   feed.timezone=GMT+0630  # GMT + 6 hours, 30 minutes\n"
      + "   feed.timezone=GMT-0800  # GMT - 8 hours, 0 minutes\n"
      + "\n"
      + " The 'feed.file.size' property sets the target size, in bytes, of\n"
      + " an accumulated feed file. The Connector Manager tries to collect\n"
      + " many feed Documents into a single feed file to improve the\n"
      + " efficiency of sending feed data to the GSA.  Specifying too small\n"
      + " a value may result in many small feeds which might overrun the\n"
      + " GSA's feed processor.  However, specifying too large a feed size\n"
      + " reduces concurrency and may result in OutOfMemory errors in the\n"
      + " Java VM, especially if using multiple Connector Instances.\n"
      + " The default target feed size is 10MB.\n"
      + " For example:\n"
      + "   feed.file.size=10485760\n"
      + "\n"
      + " The 'feed.document.size.limit' property defines the maximum\n"
      + " allowed size, in bytes, of a Document's content.  Documents whose\n"
      + " content exceeds this size will still have metadata indexed,\n"
      + " however the content itself will not be fed.  The default value\n"
      + " is 30MB, the maximum file size accepted by the GSA.\n"
      + " For example:\n"
      + "   feed.document.size.limit=31457280\n"
      + "\n"
      + " The 'feed.contenturl.prefix' property is used for contentUrl generation.\n"
      + " The prefix should include protocol, host and port, web app,\n"
      + " and servlet to point back at this Connector Manager instance.\n"
      + " For example:\n"
      + " http://localhost:8080/connector-manager/getDocumentContent\n"
      + "\n"
      + " The 'feed.disable.inherited.acls' property is used to explicitly\n"
      + " disable using ACLs with inheritance, even if the GSA appears to\n"
      + " support the feature. This is necessary in some multibox scenarios\n"
      + " where the GSA does not support ACL inheritance. The default is\n"
      + " 'false'.\n"
      + " feed.disable.inherited.acls=false\n"
      + "\n"
      + " The 'retriever.compression' property is used for content URL feed\n"
      + " content retrieval.  If 'true', document content retrieved using the\n"
      + " content URL will be gzip compressed (if the requesting client\n"
      + " supports compression).  If 'false', content is returned\n"
      + " uncompressed.  Compression may benefit architectures with slow\n"
      + " network communications between the GSA and the Connector Manager\n"
      + " (such as a WAN).  However, use of compression may cause excessive\n"
      + " CPU load on both the GSA and the Connector Manager. The default\n"
      + " value is 'false'.\n"
      + " retriever.compression=false\n"
      + "\n"
      + " Whether to use client certificates for authentication instead of\n"
      + " relying on IP addresses. When you enable this option, your servlet\n"
      + " container must be running HTTPS, otherwise there is no way for the\n"
      + " client to provide a client certificate. The default is 'false'.\n"
      + " retriever.useClientCertificateSecurity=false\n"
      + "\n"
      + " This is a comma-delimited list of additional hosts to allow to\n"
      + " retrieve documents. If in client certificate security mode, the\n"
      + " Common Name of the Subject of the provided client certificate is\n"
      + " checked against this list. When not in client certificate security\n"
      + " mode, these hosts are resolved to IPs at startup and the client's\n"
      + " IP is checked against those IPs. gsa.feed.host is implicitly in\n"
      + " the list. The default is empty.\n"
      + " retriever.allowedHosts=\n"
      + "\n"
      + " The 'feed.backlog.*' properties are used to throttle back the\n"
      + " document feed if the GSA has fallen behind processing outstanding\n"
      + " feed items.  The Connector Manager periodically polls the GSA,\n"
      + " fetching the count of unprocessed feed items (the backlog count).\n"
      + " If the backlog count exceeds the ceiling value, feeding is paused.\n"
      + " Once the backlog count drops down below the floor value, feeding\n"
      + " resumes.\n  For example:\n"
      + " Stop feeding the GSA if its backlog exceeds this value.\n"
      + "   feed.backlog.ceiling=1000\n"
      + " Resume feeding the GSA if its backlog falls below this value.\n"
      + "   feed.backlog.floor=200\n"
      + " How often to check for feed backlog (in seconds).\n"
      + "   feed.backlog.interval=120\n"
      + "\n"
      + " The 'traversal.batch.size' property defines the optimal number\n"
      + " of items to return in each repository traversal batch.  The batch\n"
      + " size represents the size of the roll-back that occurs during a\n"
      + " failure condition.  Batch sizes that are too small may incur\n"
      + " excessive processing overhead.  Batch sizes that are too large\n"
      + " may produce OutOfMemory conditions within a Connector or result\n"
      + " in early termination of the batch if processing time exceeds the\n"
      + " traversal.time.limit.   For example:\n"
      + "    traversal.batch.size=1000\n"
      + "\n"
      + " The 'traversal.poll.interval' property defines the number of\n"
      + " seconds to wait after a traversal of the repository finds no new\n"
      + " content before looking again.  Short intervals allow new content\n"
      + " to be readily available for search, at the cost of increased\n"
      + " repository access.  Long intervals add latency before new\n"
      + " content becomes available for search.  By default, the Connector\n"
      + " Manager waits 5 minutes (300 seconds) before retraversing the\n"
      + " repository if no new content was found on the last traversal.\n"
      + " For example:\n"
      + "   traversal.poll.interval=300\n"
      + "\n"
      + " The 'traversal.time.limit' property defines the number of\n"
      + " seconds a traversal batch should run before gracefully exiting.\n"
      + " Traversals that exceed this time period risk cancelation.\n"
      + " The default time limit is 30 minutes (1800 seconds).\n"
      + " For example:\n"
      + "   traversal.time.limit=1800\n"
      + "\n"
      + " The 'traversal.enabled' property is used to enable or disable\n"
      + " Traversals and Feeds for all connector instances in this\n"
      + " Connector Manager.  Disabling Traversal would be desirable if\n"
      + " configuring a Connector Manager deployment that only authorizes\n"
      + " search results.  Traversals are enabled by default.\n"
      + " traversal.enabled=false\n"
      + "\n"
      + " The 'config.change.detect.interval' property specifies how often\n"
      + " (in seconds) to look for asynchronous configuration changes.\n"
      + " Values <= 0 imply never.  For stand-alone deployments, long\n"
      + " intervals or never are probably sufficient.  For clustered\n"
      + " deployments with a shared configuration store, 60 to 300 seconds\n"
      + " is probably sufficient.  The default configuration change\n"
      + " detection interval is 15 minutes (900 seconds).\n"
      + " config.change.detect.interval=900\n"
      + "\n"
      + "The 'jdbc.datasource.*' properties specify JDBC configuration\n"
      + "required to access external databases.  By default, the\n"
      + "Connector Manager uses an embedded H2 database to store\n"
      + "Connector Configurations and Traversal State.  No additional\n"
      + "configuration is need when using the embedded database.\n"
      + "However, customers with High Availability requirements may\n"
      + "desire to use an enterprise-class HA database instead.\n"
      + "\n"
      + "The 'jdbc.datasource.type' property identifies the\n"
      + "database vendor driver to use for the default data store.\n"
      + "The supported values for this property are 'h2', 'mysql',\n"
      + "'oracle', 'sqlserver'.  These match the values of the\n"
      + "SpiConstants.DatabaseType constants.\n"
      + "jdbc.datasource.type=sqlserver\n"
      + "\n"
      + "The 'jdbc.datasource.*.url' property specifies the\n"
      + "DataSource Connection URL for each DataSource.  The\n"
      + "'jdbc.datasource.*.user' and 'jdbc.datasource.*.password'\n"
      + "properties specify the login credentials for that database.\n"
      + "The password value should be encrypted using the Connector\n"
      + "Manager's EncryptPassword utility.\n"
      + "More than one JDBC DataSource may be configured at once,\n"
      + "however only one may be identified as 'jdbc.datasource.type'.\n"
      + "This makes is convenient to migrate configurations from\n"
      + "one database implementation to another using the MigrateStore\n"
      + "utility.  For instance, you could use MigrateStore to move\n"
      + "Connector Configurations from the embedded H2 database to\n"
      + "an external corporate SQL Server database.\n"
      + "\n"
      + "Microsoft SQL Server JDBC DataSource configuration.\n"
      + "jdbc.datasource.sqlserver.url=jdbc:sqlserver://myserver;DatabaseName=google_connectors\n"
      + "jdbc.datasource.sqlserver.user=google_admin\n"
      + "jdbc.datasource.sqlserver.password=\n"
      + "\n"
      + "Oracle JDBC DataSource configuration.\n"
      + "jdbc.datasource.oracle.url=jdbc:oracle:thin:@myserver:1521:myserver\n"
      + "jdbc.datasource.oracle.user=google_admin\n"
      + "jdbc.datasource.oracle.password=\n"
      + "\n"
      + "MySQL JDBC DataSource configuration.\n"
      + "jdbc.datasource.mysql.url=jdbc:mysql://myserver/google_connectors\n"
      + "jdbc.datasource.mysql.user=google_admin\n"
      + "jdbc.datasource.mysql.password=\n"
      + "\n";

  private static final Logger LOGGER =
      Logger.getLogger(Context.class.getName());

  private static final GenericApplicationContext genericApplicationContext =
      new GenericApplicationContext();

  private static Context INSTANCE = new Context();

  private Throwable initFailureCause = null;

  private boolean started = false;

  private boolean isServletContext = false;

  private boolean isFeeding = true;

  private String commonDirPath = null;

  // singletons
  private Manager manager = null;
  private TraversalScheduler traversalScheduler = null;
  private TraversalContext traversalContext = null;
  private SpringInstantiator instantiator = null;

  private String standaloneContextLocation;
  private String standaloneContextBaseDir;

  private Boolean gsaAdminRequiresPrefix = null;

  private boolean isTeedFeedFileInitialized = false;
  private String teedFeedFile = null;

  private boolean isGsaFeedHostInitialized = false;
  private String gsaFeedHost = null;

  /**
   * The prefix that will be used for contentUrl generation.
   * The prefix should include protocol, host and port, web app,
   * and servlet to point back at this Connector Manager instance.
   * For example:
   * {@code http://localhost:8080/connector-manager/getDocumentContent}
   */
  private String contentUrlPrefix = null;

  private int propertiesVersion = 0;

  /**
   * @param feeding to feed or not to feed
   */
  public void setFeeding(boolean feeding) {
    LOGGER.config("Traversal and Feeds are "
        + ((feeding) ? "enabled." : "disabled."));
    this.isFeeding = feeding;
  }

  /**
   * @return feeding to feed or not to feed
   */
  public boolean isFeeding() {
    return this.isFeeding;
  }

  public static Context getInstance() {
    return INSTANCE;
  }

  ApplicationContext applicationContext = null;

  private Context() {
    // Private to ensure singleton.
  }

  private void initializeStandaloneApplicationContext() {
    if (applicationContext != null) {
      // too late - someone else already established a context. this might
      // happen with multiple junit tests that each want to establish a context.
      // so long as they use the same context location, it's ok. if they want a
      // different context location, they should refresh() - see below
      return;
    }

    applicationContext = genericApplicationContext; // avoid recursion

    if (standaloneContextLocation == null) {
      standaloneContextLocation = DEFAULT_JUNIT_CONTEXT_LOCATION;
    }

    if (standaloneContextBaseDir == null) {
      standaloneContextBaseDir = System.getProperty("user.dir");
    }

    if (commonDirPath == null) {
      commonDirPath = DEFAULT_JUNIT_COMMON_DIR_PATH;
    }
    LOGGER.info("context file: " + standaloneContextLocation);
    LOGGER.info("context base directory: " + standaloneContextBaseDir);
    LOGGER.info("common dir path: " + commonDirPath);

    try {
      applicationContext = new AnchoredFileSystemXmlApplicationContext(
          standaloneContextBaseDir, standaloneContextLocation);
    } catch (Throwable t) {
      setInitFailureCause(t);
      LOGGER.log(Level.SEVERE, "Connector Manager Startup failed: ", t);
      throw new IllegalStateException("Connector Manager Startup failed", t);
    }
  }

  /**
   * Establishes that we are operating within the standalone context. In
   * this case, we use a FileSystemApplicationContext.
   *
   * @param contextLocation the name of the context XML file used for
   * instantiation.
   * @param commonDirPath the location of the common directory which contains
   * ConnectorType and Connector instantiation configuration data.
   */
  public void setStandaloneContext(String contextLocation,
                                   String commonDirPath) {
    setStandaloneContext(contextLocation, null, commonDirPath);
  }

  /**
   * Establishes that we are operating within the standalone context. In
   * this case, we use a FileSystemApplicationContext.
   *
   * @param contextLocation the name of the context XML file used for
   * instantiation.
   * @param contextBaseDir base directory for relative file paths in
   * the contextLocation.
   * @param commonDirPath the location of the common directory which contains
   * ConnectorType and Connector instantiation configuration data.
   */
  public void setStandaloneContext(String contextLocation,
                                   String contextBaseDir,
                                   String commonDirPath) {
    this.standaloneContextLocation = contextLocation;
    this.standaloneContextBaseDir = contextBaseDir;
    this.commonDirPath = commonDirPath;
    initializeStandaloneApplicationContext();
  }

  /**
   * Establishes that we are operating from a servlet context. In this case, we
   * use an XmlWebApplicationContext, which finds its config from the servlet
   * context - WEB-INF/applicationContext.xml.
   *
   * @param servletApplicationContext the web application servlet context.
   * @param commonDirPath the location of the common directory which contains
   * ConnectorType and Connector instantiation configuration data.
   */
  public void setServletContext(ApplicationContext servletApplicationContext,
                                String commonDirPath) {
    this.applicationContext = servletApplicationContext;
    this.commonDirPath = commonDirPath;
    isServletContext = true;
  }

  /**
   * Saves the cause of a Connector Manager initialization failure.
   * That cause will be rethrown upon further attempts to use the
   * unitialized Context.
   *
   * @param cause the cause of initialization failure
   */
  public void setInitFailureCause(Throwable cause) {
    this.initFailureCause = cause;
  }

  /*
   * Choose a default context, if it wasn't specified in any other way. For now,
   * we choose servlet context by default.
   */
  private synchronized void initApplicationContext() {
    if (applicationContext == null) {
      if (initFailureCause != null) {
        throw new IllegalStateException("Connector Manager Startup failed",
                                        initFailureCause);
      }
      initializeStandaloneApplicationContext();
    }
  }

  /**
   * Start up the Scheduler.
   */
  private void startScheduler() {
    traversalScheduler =
        (TraversalScheduler) getRequiredBean("TraversalScheduler",
            TraversalScheduler.class);
    if (traversalScheduler != null) {
      traversalScheduler.init();
    }
  }

  /**
   * Start up the Instantiator.
   */
  private void startInstantiator() {
    instantiator =
        (SpringInstantiator) getBean("Instantiator", SpringInstantiator.class);
    if (instantiator != null) {
      instantiator.init();
    }
  }

  /**
   * Do everything necessary to start up the application.
   */
  public synchronized void start() {
    if (started) {
      return;
    }
    initApplicationContext();
    try {
      startInstantiator();
      if (isFeeding) {
        startScheduler();
      }
      startServices();
    } finally {
      started = true;
    }
  }

  /**
   * Starts any services declared as part of the application.
   */
  private void startServices() {
    for (ContextService service : getServices()) {
      service.start();
    }
  }

  /**
   * Gets a service by name.  Returns a matching bean if found or null
   * otherwise.
   *
   * @param serviceName the name of the service to find.
   * @return if there is a single bean with the given name it will be returned.
   *         If there are multiple beans with the same name, the first one found
   *         will be returned.  If there are no beans with the given name, null
   *         will be returned.
   */
  public ContextService findService(String serviceName) {
    return (ContextService) getBean(serviceName, null);
  }

  /**
   * Returns an ordered list of services attached to the context.  Collection is
   * ordered according to the startup order of the services.
   * <p>
   * To get the list in reverse order use {@link Collections#reverse(List)}.
   *
   * @return an ordered list of ContextService objects.  If no services are
   *         registered an empty list will be returned.
   */
  public List<ContextService> getServices() {
    // TODO: Investigate the use of the GenericBeanFactoryAccessor here.
    Map<?, ?> orderedServices = (Map<?, ?>)
        getBean(ORDERED_SERVICES_BEAN_NAME, null);
    Map<?, ?> services = applicationContext.getBeansOfType(ContextService.class);
    List<ContextService> result = new ArrayList<ContextService>();

    if (orderedServices != null) {
      for (Iterator<?> iter = orderedServices.keySet().iterator();
          iter.hasNext(); ) {
        ContextService service =
            (ContextService) orderedServices.get(iter.next());
        result.add(service);
      }
    }
    for (Iterator<?> iter = services.values().iterator(); iter.hasNext(); ) {
        ContextService service = (ContextService) iter.next();
      if (!result.contains(service)) {
        result.add(service);
      }
    }

    return result;
  }

  /**
   * Get a bean from the application context that we MUST have to operate.
   *
   * @param beanName the name of the bean we're looking for. Typically, the same
   *        as its most general interface.
   * @param clazz the class of the bean we're looking for.
   * @return if there is a single bean of the required type, we return it,
   *         regardless of name. If there are multiple beans of the required
   *         type, we return the one with the required name, if present, or the
   *         first one we find, if there is none of the right name.
   * @throws IllegalStateException if there are no beans of the right type, or
   *         if there is an instantiation problem.
   */
  public Object getRequiredBean(String beanName, Class<?> clazz) {
    try {
      Object object = getBean(beanName, clazz);
      if (object != null) {
        return object;
      }
      throw new IllegalStateException("The context has no " + beanName);
    } catch (BeansException e) {
      throw new IllegalStateException("Spring failure - can't instantiate "
          + beanName + ": (" + e.toString() + ")");
    }
  }

  /**
   * Get an optional bean from the application context.
   *
   * @param beanName the name of the bean we're looking for. Typically,
   *        the same as its most general interface.
   * @param clazz the class of the bean we're looking for.
   * @return if there is a single bean of the required type, we return it,
   *         regardless of name. If there are multiple beans of the required
   *         type, we return the one with the required name, if present, or the
   *         first one we find, if there is none of the right name.  Returns
   *         null if no bean of the appropriate name or type is found.
   * @throws BeansException if there is an instantiation problem.
   */
  public Object getBean(String beanName, Class<?> clazz)
      throws BeansException {
    initApplicationContext();
    return getBean(applicationContext, beanName, clazz);
  }

  /**
   * Get a bean from the supplied BeanFactory.  First look for a bean with
   * the given name and type.  If none is found, look for the first bean
   * of the specified type.
   *
   * @param factory a ListableBeanFactory
   * @param beanName the name of the bean we're looking for. Typically, the same
   *        as its most general interface.  If null, return the first bean
   *        of the requested type.
   * @param clazz the class of the bean we're looking for.  If null, return
   *        any bean of the specified name.
   * @return if there is a single bean of the required type, we return it,
   *         regardless of name. If there are multiple beans of the required
   *         type, we return the one with the required name, if present, or the
   *         first one we find, if there is none of the right name.  Returns
   *         null if no bean of the appropriate name or type is found.
   * @throws BeansException if there is an instantiation problem.
   */
  public Object getBean(ListableBeanFactory factory, String beanName,
      Class<?> clazz) throws BeansException {
    Object result = null;

    // First, look for a bean with the specified name and type.
    try {
      if (beanName != null && beanName.length() > 0) {
        result = factory.getBean(beanName, clazz);
        if (result != null) {
          return result;
        }
      }
    } catch (NoSuchBeanDefinitionException e) {
      // Not a problem yet.  Look for any bean of the appropriate type.
    }

    // If no bean type was specified, we are done.
    if (clazz == null) {
      return null;
    }

    // Get the list of beans defined in the bean factory of the required type.
    String[] beanList = factory.getBeanNamesForType(clazz);

    // Make sure there is at least one
    if (beanList.length < 1) {
      return null;
    }

    // If more beans were found issue a warning.
    if (beanList.length > 1) {
      StringBuilder buf = new StringBuilder();
      for (int i = 1; i < beanList.length; i++) {
        buf.append(" ");
        buf.append(beanList[i]);
      }
      LOGGER.warning("Resource contains multiple " + clazz.getName() +
          " definitions. Using the first: " + beanList[0] +
          ". Skipping: " + buf);
    }

    return factory.getBean(beanList[0]);
  }

  /**
   * Gets the singleton {@link Manager}.
   *
   * @return the Manager
   */
  public Manager getManager() {
    if (manager != null) {
      return manager;
    }
    manager = (Manager) getRequiredBean("Manager", Manager.class);
    return manager;
  }

  /**
   * Gets the singleton {@link Instantiator}.
   *
   * @return the Instantiator
   */
  public Instantiator getInstantiator() {
    if (instantiator != null) {
      return instantiator;
    }
    instantiator = (SpringInstantiator)
        getRequiredBean("Instantiator", SpringInstantiator.class);
    return instantiator;
  }

  /**
   * Gets the singleton TraversalContext.
   *
   * @return the TraversalContext
   */
  public TraversalContext getTraversalContext() {
    if (traversalContext != null) {
      return traversalContext;
    }
    try {
      traversalContext = (TraversalContext) getRequiredBean("TraversalContext",
          TraversalContext.class);
    } catch (IllegalStateException e) {
      LOGGER.warning("Can't find suitable " + TraversalContext.class.getName()
          + " bean in context, using default.");
      traversalContext = new ProductionTraversalContext();
    }
    // Lazily initialize supportsInheritedAcls and supportsDenyAcls,
    // since they usually require communicating with the GSA.
    if (traversalContext instanceof SimpleTraversalContext) {
      SimpleTraversalContext simpleContext =
          (SimpleTraversalContext) traversalContext;
      Properties props = getConnectorManagerProperties();
      GsaFeedConnection feeder = getGsaFeedConnection();
      initTraversalContext(simpleContext, props, feeder);
    }
    return traversalContext;
  }

  @VisibleForTesting
  void initTraversalContext(SimpleTraversalContext simpleContext,
      Properties props, GsaFeedConnection feeder) {
    if (Boolean.valueOf(props.getProperty(FEED_DISABLE_INHERITED_ACLS))) {
      simpleContext.setSupportsInheritedAcls(false);
    } else if (feeder != null) {
      simpleContext.setSupportsInheritedAcls(feeder.supportsInheritedAcls());
    }
    // N.B.: We are using feeder.supportsInheritedAcls() to
    // determine whether DENY is supported. This is conservative,
    // only claiming support for DENY ACLs on 7.0, and not on 6.14.
    // TODO(jlacey): Check for 6.14 and support DENY there as well.
    if (feeder != null) {
      simpleContext.setSupportsDenyAcls(feeder.supportsInheritedAcls());
    }
  }

  /**
   * Throws out the current context instance and gets another one. For testing
   * only. This could really boolux things up if it were used in production!
   *
   */
  public static void refresh() {
    INSTANCE = new Context();
  }

  /**
   * Gets the applicationContext. For testing only.
   *
   * @return the applicationContext
   */
  public ApplicationContext getApplicationContext() {
    initApplicationContext();
    return applicationContext;
  }

  /**
   * Retrieves the ApplicationContext configLocations,
   * or null if none specified.
   *
   * @return String array of configLocations.
   */
  public String[] getConfigLocations() {
    initApplicationContext();
    if (isServletContext) {
      return ((XmlWebApplicationContext) applicationContext)
          .getConfigLocations();
    } else {
      return ((AnchoredFileSystemXmlApplicationContext) applicationContext)
          .getConfigLocations();
    }
  }

  public synchronized void shutdown(boolean force) {
    if (started) {
      LOGGER.info("Shutdown initiated...");
      stopServices(force);
      if (null != traversalScheduler) {
        traversalScheduler.shutdown();
        traversalScheduler = null;
      }
      if (null != instantiator) {
        instantiator.shutdown(force,
            ThreadPool.DEFAULT_SHUTDOWN_TIMEOUT_MILLIS);
        instantiator = null;
      }
      closeDatabases();
      started = false;
    }
  }

  /**
   * Shuts down any Spring-configured JdbcDatabase instances.
   */
  @SuppressWarnings("unchecked")
  private void closeDatabases() {
    Collection<JdbcDatabase> databases = (Collection<JdbcDatabase>)
        applicationContext.getBeansOfType(JdbcDatabase.class).values();
    for (JdbcDatabase database : databases) {
      try {
        database.shutdown();
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to shut down database connection", e);
      }
    }
  }

  /**
   * Stops any services declared as part of the application.
   */
  private void stopServices(boolean force) {
    initApplicationContext();
    List<ContextService> services = getServices();
    Collections.reverse(services);
    for (ContextService service : services) {
      service.stop(force);
    }
  }

  /**
   * Retrieves the prefix for the Common directory file depending on whether
   * it is a standalone context or servlet context.
   *
   * @return prefix for the Repository file.
   */
  public String getCommonDirPath() {
    initApplicationContext();
    return commonDirPath;
  }

  private String getPropFileName() throws InstantiatorException {
    String propFileName = null;
    try {
      propFileName =
          (String) applicationContext.getBean(
              APPLICATION_CONTEXT_PROPERTIES_BEAN_NAME, java.lang.String.class);
    } catch (BeansException e) {
      throw new InstantiatorException("Spring exception while getting "
          + APPLICATION_CONTEXT_PROPERTIES_BEAN_NAME + " bean", e);
    }
    if (Strings.isNullOrEmpty(propFileName)) {
      throw new InstantiatorException("Null or empty file name returned from "
          + "Spring while getting " + APPLICATION_CONTEXT_PROPERTIES_BEAN_NAME
          + " bean");
    }
    return propFileName;
  }

  private File getPropFile(String propFileName) throws InstantiatorException {
    Resource propResource = applicationContext.getResource(propFileName);
    File propFile;
    try {
      propFile = propResource.getFile();
    } catch (IOException e) {
      throw new InstantiatorException(e);
    }
    return propFile;
  }

  /**
   * Returns the configuration Properties for the Connector Manager.
   */
  public Properties getConnectorManagerProperties() {
    try {
      return loadConnectorManagerProperties();
    } catch (PropertiesException pe) {
      LOGGER.log(Level.WARNING, "Unable to read application context"
                 + " properties.", pe);
      return new Properties();
    }
  }

  /**
   * Loads the configuration Properties for the Connector Manager.
   *
   * @return the configuration Properties for the Connector Manager.
   * @throws PropertiesException if error loading properties
   */
  public synchronized Properties loadConnectorManagerProperties()
      throws PropertiesException {
    initApplicationContext();
    String propFileName = "";
    try {
      // Get the properties out of the CM properties file if present.
      propFileName = getPropFileName();
      File propFile = getPropFile(propFileName);
      Properties props = PropertiesUtils.loadFromFile(propFile);
      propertiesVersion = PropertiesUtils.getPropertiesVersion(props);
      return props;
    } catch (InstantiatorException ie) {
      throw new PropertiesException("Unable to read application context"
          + " properties file " + propFileName, ie);
    }
  }

  /**
   * Stores the configuration Properties for the Connector Manager.
   *
   * @param props  the configuration Properties to store
   * @throws PropertiesException if error storing properties
   */
  public synchronized void storeConnectorManagerProperties(Properties props)
      throws PropertiesException {
    String propFileName = "";
    try {
      // Get the properties out of the CM properties file if present.
      propFileName = getPropFileName();
      File propFile = getPropFile(propFileName);
      PropertiesUtils.storeToFile(props, propFile,
                                  CONNECTOR_MANGER_CONFIG_HEADER);
    } catch (InstantiatorException ie) {
      throw new PropertiesException("Unable to save application context"
          + " properties file " + propFileName, ie);
    }
  }

  /**
   * Returns a Properties containing just the GSA feed URL properties.
   */
  public Properties getConnectorManagerConfig() {
    // Get the properties out of the CM properties file if present.
    Properties props = getConnectorManagerProperties();
    Properties result = new Properties();
    String protocol = props.getProperty(GSA_FEED_PROTOCOL_PROPERTY_KEY);
    if (protocol != null) {
      result.setProperty(GSA_FEED_PROTOCOL_PROPERTY_KEY, protocol);
    }
    result.setProperty(GSA_FEED_HOST_PROPERTY_KEY,
        props.getProperty(GSA_FEED_HOST_PROPERTY_KEY));
    result.setProperty(GSA_FEED_PORT_PROPERTY_KEY,
        props.getProperty(GSA_FEED_PORT_PROPERTY_KEY, GSA_FEED_PORT_DEFAULT));
    String securePort = props.getProperty(GSA_FEED_SECURE_PORT_PROPERTY_KEY);
    if (securePort != null && Integer.parseInt(securePort) >= 0) {
      result.setProperty(GSA_FEED_SECURE_PORT_PROPERTY_KEY, securePort);
    }
    return result;
  }

  public synchronized void setConnectorManagerConfig(String feederGateProtocol,
      String feederGateHost, int feederGatePort, int feederGateSecurePort,
      String connectorManagerUrl) throws InstantiatorException {
    initApplicationContext();
    setConnectorManagerConfig(feederGateProtocol, feederGateHost,
        feederGatePort, feederGateSecurePort, getGsaFeedConnection(),
        connectorManagerUrl);
  }

  private GsaFeedConnection getGsaFeedConnection() {
    try {
      return (GsaFeedConnection)
        applicationContext.getBean("FeedConnection", GsaFeedConnection.class);
    } catch (BeansException be) {
      // The configured FeedConnection isn't a GSA, so it doesn't care
      // about the GSA host and port.
      LOGGER.config("The FeedConnection is not to a GSA: " + be.getMessage());
      return null;
    }
  }

  @VisibleForTesting
  void setConnectorManagerConfig(String feederGateProtocol,
      String feederGateHost, int feederGatePort, int feederGateSecurePort,
      GsaFeedConnection feeder, String connectorManagerUrl)
      throws InstantiatorException {
    // Update the feed host and port in the CM properties file.
    String propFileName = getPropFileName();
    File propFile = getPropFile(propFileName);
    Properties props;
    try {
      props = PropertiesUtils.loadFromFile(propFile);
    } catch (PropertiesException e) {
      LOGGER.log(Level.WARNING, "Unable to read application context properties"
          + " file "+ propFileName + "; attempting instantiation stand-alone.",
          e);
      props = new Properties();
    }
    props.put(GSA_FEED_HOST_PROPERTY_KEY, feederGateHost);
    props.put(GSA_FEED_PORT_PROPERTY_KEY, Integer.toString(feederGatePort));

    // Do not overwrite the protocol or secure port if we didn't get a
    // value from the GSA. This is for backward compatibility when
    // manually setting the securePort or protocol for older GSAs.
    int explicitSecurePort;
    if (feederGateSecurePort < 0) {
      String securePort = props.getProperty(GSA_FEED_SECURE_PORT_PROPERTY_KEY);
      if (Strings.isNullOrEmpty(securePort)) {
        // If no secure port is specified, use the default in case the
        // protocol is set to "https", but do not store this value.
        feederGateSecurePort = Integer.parseInt(GSA_FEED_SECURE_PORT_DEFAULT);
        explicitSecurePort = GSA_FEED_SECURE_PORT_INVALID;
      } else {
        feederGateSecurePort = Integer.parseInt(securePort);
        explicitSecurePort = feederGateSecurePort;
      }
    } else {
      props.put(GSA_FEED_SECURE_PORT_PROPERTY_KEY,
          Integer.toString(feederGateSecurePort));
      explicitSecurePort = feederGateSecurePort;
    }
    if (Strings.isNullOrEmpty(feederGateProtocol)) {
      String protocol = props.getProperty(GSA_FEED_PROTOCOL_PROPERTY_KEY);
      if (Strings.isNullOrEmpty(protocol)) {
        // Pick a protocol based on an explicitly configured secure port.
        feederGateProtocol = (explicitSecurePort < 0) ? "http" : "https";
      } else {
        feederGateProtocol = protocol;
      }
    } else {
      props.put(GSA_FEED_PROTOCOL_PROPERTY_KEY, feederGateProtocol);
    }

    if (!Strings.isNullOrEmpty(connectorManagerUrl)) {
      contentUrlPrefix = connectorManagerUrl + FEED_CONTENTURL_SERVLET;
      props.put(FEED_CONTENTURL_PREFIX_PROPERTY_KEY, contentUrlPrefix);
    } else {
      contentUrlPrefix = null;
    }

    // Lock down the manager at this point.
    props.put(MANAGER_LOCKED_PROPERTY_KEY, Boolean.TRUE.toString());
    try {
      PropertiesUtils.storeToFile(props, propFile,
          CONNECTOR_MANGER_CONFIG_HEADER);
    } catch (PropertiesException e) {
      LOGGER.log(Level.WARNING, "Unable to save application context properties"
          + " file " + propFileName + ". ", e);
      throw new InstantiatorException(e);
    }

    // This property is not overwritten, but is logged.
    boolean validateCertificate = Boolean.parseBoolean(props.getProperty(
            GSA_FEED_VALIDATE_CERTIFICATE_PROPERTY_KEY,
            GSA_FEED_VALIDATE_CERTIFICATE_DEFAULT));
    LOGGER.info("Updated Connector Manager Config: "
        + GSA_FEED_PROTOCOL_PROPERTY_KEY + "=" + feederGateProtocol + "; "
        + GSA_FEED_HOST_PROPERTY_KEY + "=" + feederGateHost + "; "
        + GSA_FEED_PORT_PROPERTY_KEY + "=" + feederGatePort + "; "
        + GSA_FEED_VALIDATE_CERTIFICATE_PROPERTY_KEY + "="
        + validateCertificate + "; "
        + GSA_FEED_SECURE_PORT_PROPERTY_KEY + "=" + feederGateSecurePort + "; "
        + FEED_CONTENTURL_PREFIX_PROPERTY_KEY + "="
        + props.getProperty(FEED_CONTENTURL_PREFIX_PROPERTY_KEY) + "; " 
        + MANAGER_LOCKED_PROPERTY_KEY + "="
        + props.getProperty(MANAGER_LOCKED_PROPERTY_KEY));

    // Update our local cached feed host.
    gsaFeedHost = feederGateHost;
    isGsaFeedHostInitialized = true;

    // TODO: The following should probably be done in ProductionManager.

    // Notify the GsaFeedConnection of new host and port.
    if (feeder != null) {
      try {
        feeder.setFeedHostAndPort(feederGateProtocol, feederGateHost,
            feederGatePort, feederGateSecurePort);

        // Update the validateCertificate flag. We do this here so that
        // the value can be updated without restarting Tomcat.
        feeder.setValidateCertificate(validateCertificate);
      } catch (MalformedURLException e) {
        throw new InstantiatorException("Invalid GSA Feed specification", e);
      }
    }

    // Notify GData aware Connectors.
    if (instantiator != null) {
      instantiator.setGDataConfig();
    }
  }

  /**
   * Whether or not the GSA requires the connector manager to prepend a
   * connector-manager-specific prefix to connector configuration
   * property names.  Older GSA require the prefix, and newer GSAs do not.
   * This value is read from the <code>gsa.admin.requiresPrefix</code>
   * property in the application context properties file.
   * If the <code>gsa.admin.requiresPrefix</code> property is not defined, the
   * default value is <code>false</code>.
   */
  public synchronized boolean gsaAdminRequiresPrefix() {
    initApplicationContext();
    if (gsaAdminRequiresPrefix == null) {
        String prop = getProperty(
            GSA_ADMIN_REQUIRES_PREFIX_KEY,
            GSA_ADMIN_REQUIRES_PREFIX_DEFAULT.toString());
        gsaAdminRequiresPrefix = Boolean.valueOf(prop);
    }
    return gsaAdminRequiresPrefix;
  }

  /**
   * Reads <code>teedFeedFile</code> from the application context properties file.
   * See google-enterprise-connector-manager/projects/connector-manager/etc/applicationContext.properties
   * for additional documentation.
   */
  public synchronized String getTeedFeedFile() {
    initApplicationContext();
    if (!isTeedFeedFileInitialized) {
      teedFeedFile = getProperty(TEED_FEED_FILE_PROPERTY_KEY, null);
      isTeedFeedFileInitialized = true;
    }
    return teedFeedFile;
  }

  /**
   * Reads <code>gsa.feed.host</code> from the application context properties file.
   * See google-enterprise-connector-manager/projects/connector-manager/etc/applicationContext.properties
   * for additional documentation.
   */
  public synchronized String getGsaFeedHost() {
    initApplicationContext();
    if (!isGsaFeedHostInitialized) {
      gsaFeedHost = getProperty(GSA_FEED_HOST_PROPERTY_KEY, null);
      isGsaFeedHostInitialized = true;
    }
    return gsaFeedHost;
  }

  /**
   * Reads <code>feed.contenturl.prefix</code> from the application context
   * properties file.
   * See google-enterprise-connector-manager/projects/connector-manager/etc/applicationContext.properties
   * for additional documentation.
   */
  public synchronized String getContentUrlPrefix() {
    initApplicationContext();
    if (contentUrlPrefix == null) {
      contentUrlPrefix = getProperty(FEED_CONTENTURL_PREFIX_PROPERTY_KEY, null);
    }
    return contentUrlPrefix;
  }

  @VisibleForTesting
  public synchronized void setContentUrlPrefix(String contentUrlPrefix) {
    this.contentUrlPrefix = contentUrlPrefix;
  }

  /**
   * Reads <code>manager.locked</code> property from the application context
   * properties file.
   *
   * @return true if the property does not exist.  Returns true if the property
   *         is set to 'true', ignoring case.  Returns false otherwise.
   */
  public synchronized boolean getIsManagerLocked() {
    initApplicationContext();
    String isManagerLocked = getProperty(MANAGER_LOCKED_PROPERTY_KEY, null);
    if (isManagerLocked != null) {
      return Boolean.valueOf(isManagerLocked).booleanValue();
    }
    // Consider older, but uninitialized properties files to be unlocked.
    if (propertiesVersion < 2 && "localhost".equals(getGsaFeedHost())) {
      return false;
    }
    return true;
  }

  /**
   * Reads a property from the application context properties file.
   *
   * @param key the property name
   * @param defaultValue if property does not exist
   */
  private String getProperty(String key, String defaultValue) {
    Properties props = getConnectorManagerProperties();
    return props.getProperty(key, defaultValue);
  }

  /**
   * Notify all listeners registered with this context of an application event.
   * Events may be framework events or application-specific events.
   *
   * @param event the event to publish.
   */
  public void publishEvent(ApplicationEvent event) {
    initApplicationContext();
    applicationContext.publishEvent(event);
  }
}
