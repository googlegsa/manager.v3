// Copyright (C) 2006 Google Inc.
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

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.WorkQueue;
import com.google.enterprise.connector.instantiator.InstantiatorException;
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
import org.springframework.web.context.support.XmlWebApplicationContext;

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

import javax.servlet.ServletContext;

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

  private static final String CONNECTOR_MANGER_CONFIG_HEADER =
      " Google Enterprise Connector Manager Configuration\n"
      + "\n"
      + " Specifies the host IP for the feed host on the GSA.\n"
      + " Example:\n"
      + "     gsa.feed.host=172.24.2.0\n"
      + "\n"
      + " Specifies the host port for the feed host on the GSA.\n"
      + " Example:\n"
      + "     gsa.feed.port=19900\n"
      + "\n"
      + " This property is used to lock out the Admin Servlet and prevent it\n"
      + " from making changes to this configuration file.  Specifically, the\n"
      + " ability to set the FeedConnection properties will be locked out.  If\n"
      + " it is set to 'true' or missing the Servlet will not be allowed to\n"
      + " update this file. \n"
      + " NOTE: This property will automatically be changed to 'true' upon\n"
      + " successful update of the file by the Servlet.  Therefore, once the\n"
      + " FeedConnection properties are successfully updated by the Servlet\n"
      + " subsequent updates will be locked out until the flag is manually\n"
      + " reset to false.\n"
      + " Example:\n"
      + "     manager.locked=false\n"
      + "\n"
      + " This property controls the logging of the feed record to a log file.\n"
      + " The log record will contain the feed XML without the content data.\n"
      + " Set this property to ALL to enable feed logging, OFF to disable.\n"
      + " Customers and developers can use this functionality to observe the\n"
      + " feed record and metadata information the connector manager sends to\n"
      + " the GSA.\n"
      + " Example:\n"
      + "     feedLoggingLevel=OFF\n"
      + "\n"
      + " If you set teedFeedFile to the name of an existing file, whenever the\n"
      + " connector manager feeds content to the GSA, it will write a duplicate\n"
      + " copy of the feed XML to the file specified by teedFeedFile. GSA\n"
      + " customers and third-party developers can use this functionality to\n"
      + " observe the content the connector manager sends to the GSA and\n"
      + " reproduce any issue which may arise.\n"
      + " Example:\n"
      + "     teedFeedFile=/tmp/CMTeedFeedFile"
      + "\n";

  private static final Logger LOGGER =
      Logger.getLogger(Context.class.getName());

  private static final GenericApplicationContext genericApplicationContext =
      new GenericApplicationContext();

  private static Context INSTANCE = new Context();

  private static ServletContext servletContext;

  private boolean started = false;

  private boolean isServletContext = false;

  // singletons
  private Manager manager = null;
  private TraversalScheduler traversalScheduler = null;
  private Thread schedulerThread = null;
  private TraversalContext traversalContext = null;

  // control variables for turning off normal functionality - testing only
  private boolean isFeeding = true;

  private String standaloneContextLocation;
  private String standaloneCommonDirPath;

  private Boolean gsaAdminRequiresPrefix = null;

  private boolean isTeedFeedFileInitialized = false;
  private String teedFeedFile = null;

  private boolean isGsaFeedHostInitialized = false;
  private String gsaFeedHost = null;

  /**
   * @param feeding to feed or not to feed
   */
  public void setFeeding(boolean feeding) {
    this.isFeeding = feeding;
  }

  public static Context getInstance() {
    return INSTANCE;
  }

  public static Context getInstance(ServletContext servletContext) {
    Context.servletContext = servletContext;
    return INSTANCE;
  }

  ApplicationContext applicationContext = null;

  private Context() {
    // private to insure singleton
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

    if (standaloneCommonDirPath == null) {
      standaloneCommonDirPath = DEFAULT_JUNIT_COMMON_DIR_PATH;
    }
    LOGGER.info("context file: " + standaloneContextLocation);
    LOGGER.info("common dir path: " + standaloneCommonDirPath);

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
    this.standaloneCommonDirPath = commonDirPath;
    initializeStandaloneApplicationContext();
  }

  /**
   * Establishes that we are operating from a servlet context. In this case, we
   * use an XmlWebApplicationContext, which finds its config from the servlet
   * context - WEB-INF/applicationContext.xml.
   *
   */
  public void setServletContext() {
    if (applicationContext != null) {
      // too late - someone else already established a context.
      // This is normal. Either: another servlet got there first, or this is
      // actually a test and the junit test case got there first and established
      // a standalone context, but then a servlet came along and called this
      // method
      return;
    }
    applicationContext = genericApplicationContext; // avoid recursion

    // Note: default context location is /WEB-INF/applicationContext.xml
    LOGGER.info("Making an XmlWebApplicationContext");
    XmlWebApplicationContext ac = new XmlWebApplicationContext();
    ac.setServletContext(servletContext);
    ac.refresh();
    applicationContext = ac;
    isServletContext = true;
  }

  /*
   * Choose a default context, if it wasn't specified in any other way. For now,
   * we choose servlet context by default.
   */
  private void initApplicationContext() {
    if (applicationContext == null) {
      if (servletContext != null) {
        setServletContext();
      } else {
        initializeStandaloneApplicationContext();
      }
    }
    if (applicationContext == null) {
      throw new IllegalStateException("Spring failure - no application context");
    }
  }

  /**
   * Start up the scheduler.
   *
   */
  private void startScheduler() {
    if (traversalScheduler != null) {
      return;
    }
    traversalScheduler =
        (TraversalScheduler) getRequiredBean("TraversalScheduler",
            TraversalScheduler.class);
    traversalScheduler.init();
    schedulerThread = new Thread(traversalScheduler, "TraversalScheduler");
    schedulerThread.start();
  }

  /**
   * Do everything necessary to start up the application.
   */
  public void start() {
    if (started) {
      return;
    }
    if (applicationContext == null) {
      setServletContext();
    }
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
    initApplicationContext();
    List services = getServices();
    for (Iterator iter = services.iterator(); iter.hasNext(); ) {
      ContextService service = (ContextService) iter.next();
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
  public List getServices() {
    Map orderedServices = (Map) getBean(ORDERED_SERVICES_BEAN_NAME, null);
    Map services = applicationContext.getBeansOfType(ContextService.class);
    List result = new ArrayList();

    if (orderedServices != null) {
      for (Iterator iter = orderedServices.keySet().iterator();
          iter.hasNext(); ) {
        ContextService service =
            (ContextService) orderedServices.get(iter.next());
        result.add(service);
      }
    }
    for (Iterator iter = services.values().iterator(); iter.hasNext(); ) {
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
  public Object getRequiredBean(String beanName, Class clazz) {
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
   * @param beanName the name of the bean we're looking for. Typically, the same
   *        as its most general interface.
   * @param clazz the class of the bean we're looking for.
   * @return if there is a single bean of the required type, we return it,
   *         regardless of name. If there are multiple beans of the required
   *         type, we return the one with the required name, if present, or the
   *         first one we find, if there is none of the right name.  Returns
   *         null if no bean of the appropriate name or type is found.
   * @throws BeansException if there is an instantiation problem.
   */
  public Object getBean(String beanName, Class clazz) throws BeansException {
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
      Class clazz) throws BeansException {
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
      StringBuffer buf = new StringBuffer();
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
   * Gets the singleton Manager.
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

  public void shutdown(boolean force) {
    LOGGER.log(Level.INFO, "shutdown");
    stopServices(force);
    if (!isFeeding) {
      started = false;
    } else if (null != traversalScheduler) {
      traversalScheduler.shutdown(force, WorkQueue.DEFAULT_SHUTDOWN_TIMEOUT);
      started = false;
    }
  }

  /**
   * Stops any services declared as part of the application.
   */
  private void stopServices(boolean force) {
    initApplicationContext();
    List services = getServices();
    Collections.reverse(services);
    for (Iterator iter = services.iterator(); iter.hasNext(); ) {
      ContextService service = (ContextService) iter.next();
      service.stop(force);
    }
  }

  /**
   * Retrieves the prefix for the Common directory file depending on whether its
   * standalone context or servlet context.
   *
   * @return prefix for the Repository file.
   */
  public String getCommonDirPath() {
    initApplicationContext();
    if (isServletContext) {
      return servletContext.getRealPath("/WEB-INF");
    } else {
      return standaloneCommonDirPath;
    }
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
          props.getProperty(GSA_FEED_PORT_PROPERTY_KEY));
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
    return gsaAdminRequiresPrefix.booleanValue();
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
    String isManagerLocked = getProperty(MANAGER_LOCKED_PROPERTY_KEY,
        Boolean.TRUE.toString());
    return Boolean.valueOf(isManagerLocked).booleanValue();
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
