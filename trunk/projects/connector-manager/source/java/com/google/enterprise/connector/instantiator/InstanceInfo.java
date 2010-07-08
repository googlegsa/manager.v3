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

import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.Connector;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
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

  /**
   * Constructs a new Connector instance based upon the supplied
   * configuration map.
   *
   * @param connectorName the name of the Connector instance
   * @param connectorDir the Connector's working directory
   * @param typeInfo the Connector's prototype
   * @param config connector Configuration
   * @return new InstanceInfo representing the Connector instance
   * @throws InstanceInfoException
   */
  public InstanceInfo(String connectorName, File connectorDir,
      TypeInfo typeInfo, Configuration config) throws InstanceInfoException {
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
    this.storeContext = new StoreContext(connectorName, connectorDir);
    this.connector = makeConnectorWithSpring(connectorName, typeInfo, config);
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
    Context context = Context.getInstance();
    String name = connectorName;
    Resource prototype = null;
    if (config.getXml() != null) {
      prototype = new ByteArrayResourceHack(config.getXml().getBytes(),
                                            TypeInfo.CONNECTOR_INSTANCE_XML);
    }
    if (prototype == null) {
      prototype = typeInfo.getConnectorInstancePrototype();
    }

    DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
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
  private static Resource getPropertiesResource(String connectorName,
      Map<String, String> configMap) throws InstanceInfoException {
    Properties properties = (configMap == null)
        ? new Properties() : PropertiesUtils.fromMap(configMap);
    try {
      return new ByteArrayResourceHack(
          PropertiesUtils.storeToString(properties, null).getBytes(),
          connectorName + ".properties");
    } catch (PropertiesException e) {
      throw new PropertyProcessingInternalFailureException(e,
          connectorName);
    }
  }

  /* This subclass of ByteArrayResource attempts to circumvent a bug in
   * org.springframework.core.io.support.PropertiesLoaderSupport.loadProperties()
   * that tries to fetch the filename extension of the properties Resource
   * in an attempt to determine whether to parse the properties as XML or
   * traditional syntax.  ByteArrayResource throws an exception when
   * getFilename() is called because there is no associated filename.
   * TODO: Remove this when Spring Framework SPR-5068 gets fixed:
   * http://jira.springframework.org/browse/SPR-5068
   */
  private static class ByteArrayResourceHack extends ByteArrayResource {
    private String filename;
    public ByteArrayResourceHack(byte[] byteArray, String filename) {
      super(byteArray);
      this.filename = filename;
    }
    @Override
    public String getFilename() {
      return filename;
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
   * @param configuation the connector type specific configuration data,
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
   * Sets the schedule for this connector instance.
   * Writes the modified schedule through to the persistent store.
   *
   * @param connectorSchedule String to store or null unset any existing
   * schedule.
   */
  public void setConnectorSchedule(String connectorSchedule) {
    if (connectorSchedule == null) {
      store.removeConnectorSchedule(storeContext);
    } else {
      store.storeConnectorSchedule(storeContext,
          new Schedule(connectorSchedule));
    }
  }

  /**
   * Gets the schedule for this connector instance.
   *
   * @return the schedule String, or null to erase any previously set schedule.
   * for this connector
   */
  public String getConnectorSchedule() {
    Schedule schedule = store.getConnectorSchedule(storeContext);
    return (schedule == null) ? null : schedule.toString();
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
      super("Problem while processing configuration properties for connector "
            + connectorName + " using resource "
            + prototype.getDescription(), cause);
    }
  }
}
