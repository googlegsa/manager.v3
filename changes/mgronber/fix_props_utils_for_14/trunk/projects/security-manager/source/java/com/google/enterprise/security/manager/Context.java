// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.security.manager;

import com.google.enterprise.saml.server.BackEnd;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

/**
 * Static services for establishing the application context. This consists of
 * configuration, instantiating singletons, start up, etc. This code supports
 * two context types: servlet (as a web application within an application
 * server) and standalone. When we run junit tests, we use a standalone context.
 * Use the methods setStandaloneContext and setServletContext to select the
 * context type.
 */
public class Context {

  public static final String DEFAULT_JUNIT_CONTEXT_LOCATION =
      "testdata/mocktestdata/applicationContext.xml";

  private static final Logger LOGGER = Logger.getLogger(Context.class.getName());

  private static Context INSTANCE = new Context();

  private static ServletContext servletContext;

  // singletons
  private BackEnd backEnd = null;

  private String standaloneContextLocation;

  /**
   * Factory method for a Context. Note, there is no public constructor.
   * 
   * @return a Context instance
   */
  public static Context getInstance() {
    return INSTANCE;
  }

  /**
   * Factory method for a Context. This version should be used by servlets.
   * Note, there is no public constructor.
   * 
   * @param servletContext
   * @return a Context instance
   */
  public static Context getInstance(ServletContext servletContext) {
    Context.servletContext = servletContext;
    return INSTANCE;
  }

  ApplicationContext applicationContext = null;

  private Context() {
    // private to ensure singleton
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

    applicationContext = new FileSystemXmlApplicationContext(standaloneContextLocation);
  }

  /**
   * Establishes that we are operating within the standalone context. In this
   * case, we use a FileSystemApplicationContext.
   * 
   * @param contextLocation the name of the context XML file used for
   *        instantiation. 
   */
  public void setStandaloneContext(String contextLocation) {
    this.standaloneContextLocation = contextLocation;
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
  @SuppressWarnings("unchecked")
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
        throw new IllegalStateException("Spring failure - can't instantiate " + beanName);
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
    LOGGER.warning("Multiple beans found of type " + clazz.getName() + ", but no beans named "
        + beanName + ".  Instantiating bean: " + beanList[0]);
    result = applicationContext.getBean(beanList[0]);
    if (result == null) {
      throw new IllegalStateException("Spring failure - can't instantiate " + beanName);
    }
    return result;
  }

  /**
   * Gets the singleton BackEnd.
   * 
   * @return the BackEnd
   */
  public BackEnd getBackEnd() {
    if (backEnd != null) {
      return backEnd;
    }
    backEnd = (BackEnd) getRequiredBean("BackEnd", BackEnd.class);
    return backEnd;
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
   * Do everything necessary to start up the application.
   */
  public void start() {
    if (applicationContext == null) {
      setServletContext();
    }
  }

  /**
   * Do everything necessary to shut down the application.
   */
  public void shutdown(boolean force) {
    LOGGER.log(Level.INFO, "shutdown");
  }


}
