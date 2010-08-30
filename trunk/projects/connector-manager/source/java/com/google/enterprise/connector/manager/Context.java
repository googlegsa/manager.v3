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

import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.instantiator.SpringInstantiator;
import com.google.enterprise.connector.instantiator.ThreadPool;
import com.google.enterprise.connector.pusher.GsaFeedConnection;
import com.google.enterprise.connector.scheduler.TraversalScheduler;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.traversal.ProductionTraversalContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
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
public class Context {

  public static final String GSA_FEED_HOST_PROPERTY_KEY = "gsa.feed.host";
  public static final String GSA_FEED_PORT_PROPERTY_KEY = "gsa.feed.port";
  public static final String GSA_FEED_PORT_DEFAULT = "19900";

  public static final String GSA_ADMIN_REQUIRES_PREFIX_KEY =
      "gsa.admin.requiresPrefix";
  public static final String TEED_FEED_FILE_PROPERTY_KEY = "teedFeedFile";
  public static final String MANAGER_LOCKED_PROPERTY_KEY = "manager.locked";

  public static final Boolean GSA_ADMIN_REQUIRES_PREFIX_DEFAULT =
      Boolean.FALSE;

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

  // This is the comment written to the ApplicationContextProperties file.
  private static final String CONNECTOR_MANGER_CONFIG_HEADER =
      " Google Search Appliance Connector Manager Configuration\n"
      + "\n"
      + " The 'gsa.feed.host' property specifies the host name or IP address\n"
      + " for the feed host on the GSA.\n"
      + " For example:\n"
      + "   gsa.feed.host=172.24.2.0\n"
      + "\n"
      + " The 'gsa.feed.port' property specifies the host port for the feed\n"
      + " host on the GSA.\n"
      + " For example:\n"
      + "   gsa.feed.port=19900\n"
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
      + " The 'feed.backlog.*' properties are used to throttle back the\n"
      + " document feed if the GSA has fallen behind processing outstanding\n"
      + " feed items.  The Connector Manager periodically polls the GSA,\n"
      + " fetching the count of unprocessed feed items (the backlog count).\n"
      + " If the backlog count exceeds the ceiling value, feeding is paused.\n"
      + " Once the backlog count drops down below the floor value, feeding\n"
      + " resumes.\n  For example:\n"
      + " Stop feeding the GSA if its backlog exceeds this value.\n"
      + "   feed.backlog.ceiling=10000\n"
      + " Resume feeding the GSA if its backlog falls below this value.\n"
      + "   feed.backlog.floor=1000\n"
      + " How often to check for feed backlog (in seconds).\n"
      + "   feed.backlog.interval=900\n"
      + "\n"
      + " The 'traversal.batch.size' property defines the optimal number\n"
      + " of items to return in each repository traversal batch.  The batch\n"
      + " size represents the size of the roll-back that occurs during a\n"
      + " failure condition.  Batch sizes that are too small may incur\n"
      + " excessive processing overhead.  Batch sizes that are too large\n"
      + " may produce OutOfMemory conditions within a Connector or result\n"
      + " in early termination of the batch if processing time exceeds the\n"
      + " travesal.time.limit.   For example:\n"
      + "    traversal.batch.size=500\n"
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
      + " detection interval is -1 (never).\n"
      + " config.change.detect.interval=60\n"
      + "\n";

  private static final Logger LOGGER =
      Logger.getLogger(Context.class.getName());

  private static final GenericApplicationContext genericApplicationContext =
      new GenericApplicationContext();

  private static Context INSTANCE = new Context();

  private boolean started = false;

  private boolean isServletContext = false;

  private boolean isFeeding = true;

  private String commonDirPath = null;

  // singletons
  private Manager manager = null;
  private TraversalScheduler traversalScheduler = null;
  private TraversalContext traversalContext = null;
  private SpringInstantiator instantiator = null;

  // control variables for turning off normal functionality - testing only
  private String standaloneContextLocation;


  private Boolean gsaAdminRequiresPrefix = null;

  private boolean isTeedFeedFileInitialized = false;
  private String teedFeedFile = null;

  private boolean isGsaFeedHostInitialized = false;
  private String gsaFeedHost = null;

  private int propertiesVersion = 0;

  /**
   * @param feeding to feed or not to feed
   */
  public void setFeeding(boolean feeding) {
    LOGGER.config("Traversal and Feeds are "
        + ((feeding) ? "enabled." : "disabled."));
    this.isFeeding = feeding;
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

    if (commonDirPath == null) {
      commonDirPath = DEFAULT_JUNIT_COMMON_DIR_PATH;
    }
    LOGGER.info("context file: " + standaloneContextLocation);
    LOGGER.info("common dir path: " + commonDirPath);

    applicationContext =
        new FileSystemXmlApplicationContext(standaloneContextLocation);
  }

  /**
   * Establishes that we are operating within the standalone context. In
   * this case, we use a FileSystemApplicationContext.
   * @param contextLocation the name of the context XML file used for
   * instantiation.
   * @param commonDirPath the location of the common directory which contains
   * ConnectorType and Connector instantiation configuration data.
   */
  public void setStandaloneContext(String contextLocation,
                                   String commonDirPath) {
    this.standaloneContextLocation = contextLocation;
    this.commonDirPath = commonDirPath;
    initializeStandaloneApplicationContext();
  }

  /**
   * Establishes that we are operating from a servlet context. In this case, we
   * use an XmlWebApplicationContext, which finds its config from the servlet
   * context - WEB-INF/applicationContext.xml.
   */
  public void setServletContext(ApplicationContext servletApplicationContext,
                                String commonDirPath) {
    this.applicationContext = servletApplicationContext;
    this.commonDirPath = commonDirPath;
    isServletContext = true;
  }

  /*
   * Choose a default context, if it wasn't specified in any other way. For now,
   * we choose servlet context by default.
   */
  private void initApplicationContext() {
    if (applicationContext == null) {
      initializeStandaloneApplicationContext();
    }
    if (applicationContext == null) {
      throw new IllegalStateException("Spring failure - no application context");
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
  public void start() {
    if (started) {
      return;
    }
    initApplicationContext();
    startInstantiator();
    if (isFeeding) {
      startScheduler();
    }
    startServices();
    started = true;
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
    return traversalContext;
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

  public synchronized void shutdown(boolean force) {
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
    started = false;
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
    if (propFileName == null || propFileName.length() < 1) {
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

  public Properties getConnectorManagerConfig() throws InstantiatorException {
    initApplicationContext();
    Properties result = new Properties();
    // Get the properties out of the CM properties file if present.
    String propFileName = getPropFileName();
    File propFile = getPropFile(propFileName);
    try {
      Properties props = PropertiesUtils.loadFromFile(propFile);
      result.setProperty(GSA_FEED_HOST_PROPERTY_KEY,
          props.getProperty(GSA_FEED_HOST_PROPERTY_KEY));
      result.setProperty(GSA_FEED_PORT_PROPERTY_KEY,
          props.getProperty(GSA_FEED_PORT_PROPERTY_KEY, GSA_FEED_PORT_DEFAULT));
    } catch (PropertiesException e) {
      LOGGER.log(Level.WARNING, "Unable to read application context properties"
          + " file " + propFileName,
          e);
    }
    return result;
  }

  public void setConnectorManagerConfig(String feederGateHost,
      int feederGatePort) throws InstantiatorException {
    initApplicationContext();

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
    LOGGER.info("Updated Connector Manager Config: "
        + GSA_FEED_HOST_PROPERTY_KEY + "=" + feederGateHost + "; "
        + GSA_FEED_PORT_PROPERTY_KEY + "=" + feederGatePort + ";"
        + MANAGER_LOCKED_PROPERTY_KEY + "="
        + props.getProperty(MANAGER_LOCKED_PROPERTY_KEY));

    // Update our local cached feed host.
    gsaFeedHost = feederGateHost;
    isGsaFeedHostInitialized = true;

    // Notify the GsaFeedConnection of new host and port.
    try {
      GsaFeedConnection feeder = (GsaFeedConnection)
        applicationContext.getBean("FeedConnection", GsaFeedConnection.class);
      feeder.setFeedHostAndPort(feederGateHost, feederGatePort);
    } catch (BeansException be) {
      // The configured FeedConnection isn't a GSA, so it doesn't care
      // about the GSA host and port.
    } catch (MalformedURLException e) {
      throw new InstantiatorException("Invalid GSA Feed specification", e);
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
  public boolean gsaAdminRequiresPrefix() {
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
  public String getTeedFeedFile() {
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
  public String getGsaFeedHost() {
    initApplicationContext();
    if (!isGsaFeedHostInitialized) {
      gsaFeedHost = getProperty(GSA_FEED_HOST_PROPERTY_KEY, null);
      isGsaFeedHostInitialized = true;
    }
    return gsaFeedHost;
  }

  /**
   * Reads <code>manager.locked</code> property from the application context
   * properties file.
   *
   * @return true if the property does not exist.  Returns true if the property
   *         is set to 'true', ignoring case.  Returns false otherwise.
   */
  public boolean getIsManagerLocked() {
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
    try {
      String propFileName = getPropFileName();
      File propFile = getPropFile(propFileName);
      try {
        Properties props = PropertiesUtils.loadFromFile(propFile);
        propertiesVersion = PropertiesUtils.getPropertiesVersion(props);
        return props.getProperty(key, defaultValue);
      } catch (PropertiesException e) {
        LOGGER.log(Level.WARNING, "Unable to read application context "
                   + "properties file " + propFileName, e);
      }
    } catch (InstantiatorException ie) {
      LOGGER.log(Level.WARNING, "Unable to read application context "
                 + "properties file.", ie);
    }
    return defaultValue;
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
