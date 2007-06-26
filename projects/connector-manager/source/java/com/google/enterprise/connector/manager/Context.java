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

import com.google.enterprise.connector.common.WorkQueue;
import com.google.enterprise.connector.instantiator.InstanceInfo;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.scheduler.TraversalScheduler;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.traversal.ProductionTraversalContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;
import java.io.IOException;
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
 */
public class Context {

  public static final String GSA_FEED_HOST_PROPERTY_KEY = "gsa.feed.host";
  public static final String GSA_FEED_PORT_PROPERTY_KEY = "gsa.feed.port";
  public static final String GSA_ADMIN_REQUIRES_PREFIX_KEY =
      "gsa.admin.requiresPrefix";

  public static final Boolean GSA_ADMIN_REQUIRES_PREFIX_DEFAULT =
      Boolean.FALSE;

  public static final String DEFAULT_JUNIT_CONTEXT_LOCATION =
      "testdata/mocktestdata/applicationContext.xml";
  public static final String DEFAULT_JUNIT_COMMON_DIR_PATH =
      "testdata/mocktestdata/";

  private static final String APPLICATION_CONTEXT_PROPERTIES_BEAN_NAME =
      "ApplicationContextProperties";

  private static final Logger LOGGER =
      Logger.getLogger(Context.class.getName());

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

    if (standaloneContextLocation == null) {
      standaloneContextLocation = DEFAULT_JUNIT_CONTEXT_LOCATION;
    }

    if (standaloneCommonDirPath == null) {
      standaloneCommonDirPath = DEFAULT_JUNIT_COMMON_DIR_PATH;
    }

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
   * we chooseservlet context by default.
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

  private void reInitApplicationContext() {
    applicationContext = null;
    initApplicationContext();
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
   * 
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
    started = true;
  }

  private void restart() {
    started = false;
    start();
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
  private Object getRequiredBean(String beanName, Class clazz) {
    initApplicationContext();
    String beanList[] = applicationContext.getBeanNamesForType(clazz);
    if (beanList.length < 1) {
      throw new IllegalStateException("The context has no " + beanName);
    }
    Object result = null;
    if (beanList.length == 1) {
      // we use this bean, whatever it may be named
      result = applicationContext.getBean(beanList[0]);
      if (result == null) {
        throw new IllegalStateException("Spring failure - can't instantiate "
            + beanName);
      }
      return result;
    }
    // there are multiple beans of this type in the context. this is unexpected
    // but maybe some testing is going on. so try to find one with the right
    // name
    result = applicationContext.getBean(beanName, clazz);
    if (result != null) {
      return result;
    }
    // otherwise, just return the first one found
    LOGGER.warning("Multiple beans found of type " + clazz.getName()
        + ", but no beans named " + beanName + ".  Instantiating bean: "
        + beanList[0]);
    result = applicationContext.getBean(beanList[0]);
    if (result == null) {
      throw new IllegalStateException("Spring failure - can't instantiate "
          + beanName);
    }
    return result;
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
    if (!isFeeding) {
      return;
    }
    if (null != traversalScheduler) {
      traversalScheduler.shutdown(force, WorkQueue.DEFAULT_SHUTDOWN_TIMEOUT);
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
      return servletContext.getRealPath("/") + File.separator + "WEB-INF";
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

  public void setConnectorManagerConfig(String feederGateHost,
      int feederGatePort) throws InstantiatorException {
    initApplicationContext();
    String propFileName = getPropFileName();
    File propFile = getPropFile(propFileName);
    Properties props =
        InstanceInfo.initPropertiesFromFile(propFile, propFileName);
    props.put(GSA_FEED_HOST_PROPERTY_KEY, feederGateHost);
    props.put(GSA_FEED_PORT_PROPERTY_KEY, Integer.toString(feederGatePort));
    InstanceInfo.writePropertiesToFile(props, propFile);
    shutdown(true);  // force shutdown
    reInitApplicationContext();
    restart();
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
      try {
        String propFileName = getPropFileName();
        File propFile = getPropFile(propFileName);
        Properties props =
            InstanceInfo.initPropertiesFromFile(propFile, propFileName);
        String prop = props.getProperty(
            GSA_ADMIN_REQUIRES_PREFIX_KEY,
            GSA_ADMIN_REQUIRES_PREFIX_DEFAULT.toString());
        gsaAdminRequiresPrefix = Boolean.valueOf(prop);
      } catch (InstantiatorException e) {
        LOGGER.log(Level.WARNING,
                   "Unable to read application context properties file.  " +
                   "Using default value for Context.gsaAdminRequiresPrefix().",
                   e);
        gsaAdminRequiresPrefix = GSA_ADMIN_REQUIRES_PREFIX_DEFAULT;
      }
    }
    return gsaAdminRequiresPrefix.booleanValue();
  }
}
