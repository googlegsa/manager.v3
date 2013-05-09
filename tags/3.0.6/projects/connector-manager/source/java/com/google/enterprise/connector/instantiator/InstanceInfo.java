// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.util.filter.DocumentFilterChain;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Container for info about a Connector Instance. Instantiable only through a
 * static factory that uses Spring.
 */
final class InstanceInfo {
  private static final Logger LOGGER =
      Logger.getLogger(InstanceInfo.class.getName());

  private static PersistentStore store;

  private final TypeInfo typeInfo;
  private final File connectorDir;
  private final String connectorName;
  private final StoreContext storeContext;
  private final Connector connector;
  private final DocumentFilterFactory documentFilterFactory;

  /**
   * Constructs a InstanceInfo with no backing Connector instance.
   *
   * @param connectorName the name of the Connector instance
   * @param connectorDir the Connector's working directory
   * @param typeInfo the Connector's prototype
   * @throws InstanceInfoException
   */
  public InstanceInfo(String connectorName, File connectorDir,
      TypeInfo typeInfo) throws InstanceInfoException {
    this(connectorName, connectorDir, typeInfo, null, false);
  }

  /**
   * Constructs a new Connector instance based upon the supplied
   * configuration map.
   *
   * @param connectorName the name of the Connector instance
   * @param connectorDir the Connector's working directory
   * @param typeInfo the Connector's prototype
   * @param config connector Configuration
   * @throws InstanceInfoException
   */
  public InstanceInfo(String connectorName, File connectorDir,
      TypeInfo typeInfo, Configuration config) throws InstanceInfoException {
    this(connectorName, connectorDir, typeInfo, config, true);
  }

  /**
   * Constructs a new Connector instance based upon the supplied
   * configuration map.
   *
   * @param connectorName the name of the Connector instance
   * @param connectorDir the Connector's working directory
   * @param typeInfo the Connector's prototype
   * @param config connector Configuration
   * @param createConnector if true, create the connector instance
   * @throws InstanceInfoException
   */
  private InstanceInfo(String connectorName, File connectorDir,
      TypeInfo typeInfo, Configuration config, boolean createConnector)
      throws InstanceInfoException {
    if (connectorName == null || connectorName.length() < 1) {
      throw new NullConnectorNameException();
    }
    if (connectorDir == null) {
      throw new NullDirectoryException();
    }
    if (typeInfo == null) {
      throw new NullTypeInfoException();
    }

    this.connectorName = connectorName;
    this.connectorDir = connectorDir;
    this.typeInfo = typeInfo;
    this.storeContext =
        new StoreContext(connectorName, typeInfo.getConnectorTypeName());

    if (createConnector) {
      if (config == null) {
        throw new NullConfigurationException();
      }
      DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
      this.connector = makeConnectorWithSpring(connectorName, typeInfo, config,
                                               beanFactory);
      try {
        this.documentFilterFactory = getDocumentFilterFactory(beanFactory);
        if (this.documentFilterFactory != null) {
          LOGGER.config("Connector " + connectorName + " has document filters: "
                        + this.documentFilterFactory.toString());
        }
      } catch (BeansException e) {
        throw new InstanceInfoException("Failed to load document filters for"
                                        + " connector " + connectorName, e);
      }
    } else {
      this.connector = null;
      this.documentFilterFactory = null;
    }
  }


  /* **** Getters and Setters **** */

  public static void setPersistentStore(PersistentStore store) {
    InstanceInfo.store = store;
  }

  /**
   * @return the connector
   */
  Connector getConnector() {
    return connector;
  }

  /**
   * @return the name
   */
  String getName() {
    return connectorName;
  }

  /**
   * @return the typeInfo
   */
  TypeInfo getTypeInfo() {
    return typeInfo;
  }

  /**
   * @return the connectorDir
   */
  File getConnectorDir() {
    return connectorDir;
  }

  /**
   * Construct a new Connector Instance based upon the connectorInstance
   * and connectorDefaults bean definitions.
   *
   * @param connectorName the name of the Connector instance.
   * @param typeInfo the Connector's prototype.
   * @param config connector Configuration.
   */
  static Connector makeConnectorWithSpring(String connectorName,
      TypeInfo typeInfo, Configuration config) throws InstanceInfoException {
    return makeConnectorWithSpring(connectorName, typeInfo, config,
                                   new DefaultListableBeanFactory());
  }

  /**
   * Construct a new Connector Instance based upon the connectorInstance
   * and connectorDefaults bean definitions.
   *
   * @param connectorName the name of the Connector instance.
   * @param typeInfo the Connector's prototype.
   * @param config connector Configuration.
   * @param factory DefaultListableBeanFactory used to create the connector.
   */
  private static Connector makeConnectorWithSpring(String connectorName,
      TypeInfo typeInfo, Configuration config,
      DefaultListableBeanFactory factory) throws InstanceInfoException {
    String name = connectorName;
    Resource prototype = null;
    if (config.getXml() != null) {
      prototype = getByteArrayResource(config.getXml(), Charsets.UTF_8.name(),
          TypeInfo.CONNECTOR_INSTANCE_XML);
    }
    if (prototype == null) {
      prototype = typeInfo.getConnectorInstancePrototype();
    }

    XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(factory);
    Resource defaults = typeInfo.getConnectorDefaultPrototype();
    try {
      beanReader.loadBeanDefinitions(prototype);
    } catch (BeansException e) {
      throw new FactoryCreationFailureException(e, prototype, name);
    }
    // Seems non-intuitive to load these in this order, but we want newer
    // versions of the connectors to override any default bean definitions
    // specified in old-style monolithic connectorInstance.xml files.
    if (defaults != null) {
      try {
        beanReader.loadBeanDefinitions(defaults);
      } catch (BeansException e) {
        throw new FactoryCreationFailureException(e, defaults, name);
      }
    }

    Context context = Context.getInstance();
    EncryptedPropertyPlaceholderConfigurer cfg = null;
    try {
        cfg = (EncryptedPropertyPlaceholderConfigurer) context.getBean(
            factory, null, EncryptedPropertyPlaceholderConfigurer.class);
    } catch (BeansException e) {
      throw new BeanInstantiationFailureException(e, prototype, name,
          EncryptedPropertyPlaceholderConfigurer.class.getName());
    }
    if (cfg == null) {
      cfg = new EncryptedPropertyPlaceholderConfigurer();
    }

    try {
      cfg.setLocation(getPropertiesResource(name, config.getMap()));
      cfg.postProcessBeanFactory(factory);
    } catch (BeansException e) {
      throw new PropertyProcessingFailureException(e, prototype, name);
    }

    Connector connector = null;
    try {
      connector = (Connector) context.getBean(factory, null, Connector.class);
    } catch (BeansException e) {
      throw new BeanInstantiationFailureException(e, prototype, name,
          Connector.class.getName());
    }
    if (connector == null) {
      throw new NoBeansFoundException(prototype, name, Connector.class);
    }
    return connector;
  }

  /**
   * Return a Spring Resource containing the InstanceInfo
   * configuration Properties.
   */
  @VisibleForTesting
  static Resource getPropertiesResource(String connectorName,
      Map<String, String> configMap) throws InstanceInfoException {
    Properties properties = (configMap == null)
        ? new Properties() : PropertiesUtils.fromMap(configMap);
    try {
      return getByteArrayResource(
          PropertiesUtils.storeToString(properties, null),
          PropertiesUtils.PROPERTIES_ENCODING, connectorName + ".properties");
    } catch (PropertiesException e) {
      throw new PropertyProcessingInternalFailureException(e,
          connectorName);
    }
  }

  /*
   * Wraps a string as a Spring resource. This function has two purposes:
   * 1. Convert the string to a byte array using the given encoding.
   * 2. Workaround a bug in Spring where Resource.getFilename() is required.
   *
   * We override ByteArrayResource.getFilename, because
   * org.springframework.core.io.support.PropertiesLoaderSupport.loadProperties()
   * tries to fetch the filename extension of the properties Resource
   * in an attempt to determine whether to parse the properties as XML or
   * traditional syntax.  ByteArrayResource throws an exception when
   * getFilename() is called because there is no associated filename.
   * TODO: Remove this hack when Spring Framework SPR-5068 gets fixed:
   * http://jira.springframework.org/browse/SPR-5068
   */
  private static ByteArrayResource getByteArrayResource(String value,
      String encoding, final String filename) {
    byte[] byteArray;
    try {
      byteArray = value.getBytes(encoding);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return new ByteArrayResource(byteArray) {
      public String getFilename() {
        return filename;
      }
    };
  }

  /**
   * Looks for {@link DocumentFilterFactory} beans in the connector's
   * bean factory. 
   *
   * @param beanFactory DefaultListableBeanFactory used to create the connector.
   * @return {@link DocumentFilterFactory} for the connector, or {@code null}
   *         if the connector does not define a DocumentFilterFactory.
   */
  private static DocumentFilterFactory getDocumentFilterFactory(
      DefaultListableBeanFactory beanFactory) throws BeansException {
    @SuppressWarnings("unchecked") Collection<DocumentFilterFactory> filters =
        beanFactory.getBeansOfType(DocumentFilterFactory.class).values();
    if (filters == null || filters.size() == 0) {
      // No filters defined.
      return null;
    } else if (filters.size() == 1) {
      // If there is just one, return it.
      return filters.iterator().next();
    }

    // More than one filter is defined.  Look for a single DocumentFilterChain,
    // which hopefully encapsulates the rest.
    @SuppressWarnings("unchecked") Collection<DocumentFilterChain> chains =
        beanFactory.getBeansOfType(DocumentFilterChain.class).values();
    if (chains == null || chains.size() == 0) {
      // No chains defined, so I'll make one.  But the order of the filters
      // should be considered random.
      return new DocumentFilterChain(Lists.newArrayList(filters));
    } else if (chains.size() == 1) {
      // If there is just one, return it.
      return chains.iterator().next();
    } else {
      // More than one filter chain is defined???  I will allow it, but...
      return new DocumentFilterChain(Lists.newArrayList(chains));
    }
  }

  /**
   * Returns a connector's {@link DocumentFilterFactory}. Connectors may define
   * a document filter specific to that connector instance.  This filter will
   * be used in conjuction with the Connector Manager's document filter, and
   * will act as the source for the Connector Manager's document filter.
   *
   * @return {@link DocumentFilterFactory} for the connector
   */
  public DocumentFilterFactory getDocumentFilterFactory() {
    return documentFilterFactory;
  }

  /**
   * Sets {@code GData} host for Connectors that want it.
   */
  public void setGDataConfig(Map<String, String> gdataConfig)
      throws PropertyProcessingFailureException {
    try {
      PropertyAccessorFactory.forBeanPropertyAccess(connector)
          .setPropertyValues(new MutablePropertyValues(gdataConfig), true);
    } catch (BeansException be) {
      throw new PropertyProcessingFailureException(be, "GData Host",
                                                   connectorName);
    }
  }

  /* **** Manage the Connector Instance Persistent data store. **** */

  /**
   * Remove this Connector Instance's persistent store state.
   */
  public void removeConnector() {
    store.removeConnectorState(storeContext);
    store.removeConnectorSchedule(storeContext);
    store.removeConnectorConfiguration(storeContext);
  }

  /**
   * Get the configuration data for this connector instance.
   *
   * @return the connector type specific configuration data, or {@code null}
   *         if no configuration is stored
   */
  public Configuration getConnectorConfiguration() {
    return store.getConnectorConfiguration(storeContext);
  }

  /**
   * Set the configuration data for this connector instance.
   * Writes the supplied configuration through to the persistent store.
   *
   * @param configuration the connector type specific configuration data,
   *        or {@code null} to unset any existing configuration.
   */
  public void setConnectorConfiguration(Configuration configuration) {
    if (configuration == null) {
      store.removeConnectorConfiguration(storeContext);
    } else {
      store.storeConnectorConfiguration(storeContext, configuration);
    }
  }

  /**
   * Sets the {@link Schedule} for this connector instance.
   * Writes the modified schedule through to the persistent store.
   *
   * @param connectorSchedule Schedule to store or null unset any existing
   * schedule.
   */
  public void setConnectorSchedule(Schedule connectorSchedule) {
    if (connectorSchedule == null) {
      store.removeConnectorSchedule(storeContext);
    } else {
      store.storeConnectorSchedule(storeContext, connectorSchedule);
    }
  }

  /**
   * Gets the schedule for this connector instance.
   *
   * @return the Schedule, or null if there is no schedule.
   * for this connector
   */
  public Schedule getConnectorSchedule() {
    return store.getConnectorSchedule(storeContext);
  }

  /**
   * Sets the remembered traversal state for this connector instance.
   * Writes the modified state through to the persistent store.
   *
   * @param connectorState String to store or null to erase any previously
   * saved traversal state.
   * @throws IllegalStateException if state store is disabled for this connector
   */
  public void setConnectorState(String connectorState) {
    if (connectorState == null) {
      store.removeConnectorState(storeContext);
    } else {
      store.storeConnectorState(storeContext, connectorState);
    }
  }

  /**
   * Gets the remembered traversal state for this connector instance.
   *
   * @return the state, or null if no state has been stored for this connector
   * @throws IllegalStateException if state store is disabled for this connector
   */
  public String getConnectorState() {
    return store.getConnectorState(storeContext);
  }


  /* **** InstanceInfoExceptions **** */

  static class InstanceInfoException extends InstantiatorException {
    InstanceInfoException(String message, Throwable cause) {
      super(message, cause);
    }
    InstanceInfoException(String message) {
      super(message);
    }
  }

  static class NullConnectorNameException extends InstanceInfoException {
    NullConnectorNameException() {
      super("Attempt to instantiate a connector with a null or empty name");
    }
  }

  static class NullDirectoryException extends InstanceInfoException {
    NullDirectoryException() {
      super("Attempt to instantiate a connector with a null directory");
    }
  }

  static class NullTypeInfoException extends InstanceInfoException {
    NullTypeInfoException() {
      super("Attempt to instantiate a connector with a null TypeInfo");
    }
  }

  static class NullConfigurationException extends InstanceInfoException {
    NullConfigurationException() {
      super("Attempt to instantiate a connector with a null Configuration");
    }
  }

  static class FactoryCreationFailureException extends InstanceInfoException {
    FactoryCreationFailureException(Throwable cause,
        Resource prototype, String connectorName) {
      super("Spring factory creation failure for connector " + connectorName
          + " using resource " + prototype.getDescription(),
          cause);
    }
  }

  static class NoBeansFoundException extends InstanceInfoException {
    NoBeansFoundException(Resource prototype,
        String connectorName, Class<?> clazz) {
      super("No beans found of type " + clazz.getName() + " for connector "
          + connectorName + " using resource "
          + prototype.getDescription());
    }
  }

  static class BeanInstantiationFailureException extends InstanceInfoException {
    BeanInstantiationFailureException(Throwable cause,
        Resource prototype, String connectorName, String beanName) {
      super("Spring failure while instantiating bean " + beanName
          + " for connector " + connectorName + " using resource "
          + prototype.getDescription(), cause);
    }
  }

  static class PropertyProcessingInternalFailureException extends
      InstanceInfoException {
    PropertyProcessingInternalFailureException(Throwable cause,
        String connectorName) {
      super("Spring internal failure while processing configuration properties"
            + " for connector " + connectorName, cause);
    }
  }

  static class PropertyProcessingFailureException extends InstanceInfoException {
    PropertyProcessingFailureException(Throwable cause, Resource prototype,
        String connectorName) {
      this(cause, prototype.getDescription(), connectorName);
    }

    PropertyProcessingFailureException(Throwable cause, String description,
        String connectorName) {
      super("Problem while processing configuration properties for connector "
            + connectorName + " using resource " + description, cause);
    }
  }
}
