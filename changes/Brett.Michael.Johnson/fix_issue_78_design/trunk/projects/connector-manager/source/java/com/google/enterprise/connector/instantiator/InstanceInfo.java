// Copyright 2007-2008 Google Inc.
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

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.persist.ConnectorConfigStore;
import com.google.enterprise.connector.persist.ConnectorScheduleStore;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.GenerationalStateStore;
import com.google.enterprise.connector.persist.StoreContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Container for info about a Connector Instance. Instantiable only through a
 * static factory that uses Spring.
 */
public final class InstanceInfo {

  private static final Logger LOGGER =
      Logger.getLogger(InstanceInfo.class.getName());

  private static final String SPECIAL_INSTANCE_CONFIG_FILE_NAME =
      "connectorInstance.xml";

  private final TypeInfo typeInfo;
  private final File connectorDir;
  private final String connectorName;
  private final StoreContext storeContext;

  private final ConnectorConfigStore configStore;
  private final ConnectorScheduleStore schedStore;
  private final ConnectorStateStore stateStore;

  private final Collection legacyConfigStores;
  private final Collection legacyScheduleStores;
  private final Collection legacyStateStores;

  private Properties properties;
  private Connector connector;

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

  private InstanceInfo(String connectorName, File connectorDir, 
      TypeInfo typeInfo) throws InstanceInfoException {
    if (connectorName == null || connectorName.length() < 1) {
      throw new NullConnectorNameException();
    }
    if (connectorDir == null || connectorDir.length() < 1) {
      throw new NullDirectoryException();
    }
    if (typeInfo == null) {
      throw new NullTypeInfoException();
    }

    this.connectorName = connectorName;
    this.connectorDir = connectorDir;
    this.typeInfo = typeInfo;
    this.storeContext = new StoreContext(connectorName, connectorDir);

    Context context = Context.getInstance();
    this.configStore = (ConnectorConfigStore) context.getRequiredBean(
        "ConnectorConfigStore", ConnectorConfigStore.class);
    this.schedStore = (ConnectorScheduleStore) context.getRequiredBean(
        "ConnectorScheduleStore", ConnectorScheduleStore.class);
    this.stateStore = new GenerationalStateStore(
        (ConnectorStateStore) context.getRequiredBean(
        "ConnectorStateStore", ConnectorStateStore.class));

    this.legacyConfigStores = getLegacyStores("LegacyConnectorConfigStores",
                                              ConnectorConfigStore.class);
    this.legacyScheduleStores = getLegacyStores("LegacyConnectorScheduleStores",
                                                ConnectorScheduleStore.class);
    this.legacyStateStores = getLegacyStores("LegacyConnectorStateStores",
                                             ConnectorStateStore.class);
  }

  public void removeConnector() {
    // Side effect of GenerationalStateStore.removeConnectorState() 
    // is a new generation.
    stateStore.removeConnectorState(storeContext);
    schedStore.removeConnectorSchedule(storeContext);
    configStore.removeConnectorConfiguration(storeContext);
  }

  public static InstanceInfo fromDirectory(String connectorName,
      File connectorDir, TypeInfo typeInfo) throws InstanceInfoException {
    InstanceInfo info = new InstanceInfo(connectorName, connectorDir, typeInfo);
    info.properties =
        info.configStore.getConnectorConfiguration(info.storeContext);

    // Upgrade from Legacy Configuration Data Stores. This method is
    // called to instantiate Connectors that were created by some 
    // other (possibly older) instance of the Connector Manager.
    // If the various stored instance data is not found in the
    // expected locations, the connector may have been previously
    // created by an older version of the Connector Manager and may 
    // have its instance data stored in the older legacy locations.
    // Move the data from the legacy stores to the expected locations
    // before launching the connector instance.
    if (info.properties == null) {
      upgradeConfigStore(info);
    }
    if (info.schedStore.getConnectorSchedule(info.storeContext) == null) {
      upgradeScheduleStore(info);
    }
    if (info.stateStore.getConnectorState(info.storeContext) == null) {
      upgradeStateStore(info);
    }

    info.connector = makeConnectorWithSpring(info);
    return info;
  }

  public static InstanceInfo fromNewConfig(String connectorName,
      File connectorDir, TypeInfo typeInfo, Map configMap)
      throws InstanceInfoException {
    InstanceInfo info = new InstanceInfo(connectorName, connectorDir, typeInfo);
    info.properties = PropertiesUtils.fromMap(configMap);
    info.properties.put(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR,
                        connectorDir.getPath());
    info.properties.put(PropertiesUtils.GOOGLE_WORK_DIR,
                        Context.getInstance().getCommonDirPath());
    // Don't write properties file to disk yet.
    info.connector = makeConnectorWithSpring(info);
    return info;
  }

  private static Connector makeConnectorWithSpring(InstanceInfo info)
      throws InstanceInfoException {
    Context context = Context.getInstance();
    Resource prototype = null;
    if (info.connectorDir != null) {
      // If this file exists, we use this it in preference to the default
      // prototype associated with the type. This allows customers to supply
      // their own per-instance config xml.
      File customPrototype =
          new File(info.connectorDir, SPECIAL_INSTANCE_CONFIG_FILE_NAME);
      if (customPrototype.exists()) {
        prototype = new FileSystemResource(customPrototype);
        LOGGER.info("Using connector-specific xml config for connector "
            + info.connectorName + " at path " + customPrototype.getPath());
      } 
    }
    if (prototype == null) {
      prototype = info.typeInfo.getConnectorInstancePrototype();
    }

    XmlBeanFactory factory;
    try {
      factory = new XmlBeanFactory(prototype);
    } catch (BeansException e) {
      throw new FactoryCreationFailureException(e, prototype, 
          info.connectorName);
    }

    EncryptedPropertyPlaceholderConfigurer cfg = null;
    try {
        cfg = (EncryptedPropertyPlaceholderConfigurer) context.getBean(
            factory, null, EncryptedPropertyPlaceholderConfigurer.class);
    } catch (BeansException e) {
      throw new BeanInstantiationFailureException(e, prototype, 
          info.connectorName, 
          EncryptedPropertyPlaceholderConfigurer.class.getName());
    }
    if (cfg == null) {
      cfg = new EncryptedPropertyPlaceholderConfigurer();
    }

    try {
      cfg.setLocation(getPropertiesResource(info));
      cfg.postProcessBeanFactory(factory);
    } catch (BeansException e) {
      throw new PropertyProcessingFailureException(e, prototype,
          info.connectorName);
    }

    Connector connector = null;
    try {
      connector = (Connector) context.getBean(factory, null, Connector.class);
    } catch (BeansException e) {
      throw new BeanInstantiationFailureException(e, prototype, 
          info.connectorName, Connector.class.getName());
    }
    if (connector == null) {
      throw new NoBeansFoundException(prototype,
          info.connectorName, Connector.class);
    }
    return connector;
  }

  /**
   * Return a Spring Resource containing the InstanceInfo 
   * configuration Properties.
   */
  private static Resource getPropertiesResource(InstanceInfo info)
      throws InstanceInfoException {
    Properties properties =
        (info.properties == null) ? new Properties() : info.properties;
    LOGGER.info("Properties Resource: " + properties.toString());
    try {
      return new ByteArrayResourceHack(
          PropertiesUtils.storeToString(properties, null).getBytes());
    } catch (PropertiesException e) {
      throw new PropertyProcessingInternalFailureException(e,
          info.connectorName);
    }
  }

  /* This subclass of ByteArrayResource attempts to circumvent a bug in
   * org.springframework.core.io.support.PropertiesLoaderSupport.loadProperties()
   * that tries to fetch the filename extension of the properties Resource
   * in an attempt to determine whether to parse the properties as XML or
   * traditional syntax.  ByteArrayResource throws an exception when
   * getFilename() is called because there is no associated filename.
   * This subclass returns a fake filename (without a .xml extension).
   * TODO: Remove this when Spring Framework SPR-5068 gets fixed:
   * http://jira.springframework.org/browse/SPR-5068
   */
  private static class ByteArrayResourceHack extends ByteArrayResource {
    public ByteArrayResourceHack(byte[] byteArray) {
      super(byteArray);
    }
    public String getFilename() {
      return "ByteArrayResourceHasNoFilename";
    }
  }

  /**
   * Get the configuration data for this connector instance.
   *
   * @return a Map&lt;String, String&gt; of its ConnectorType-specific
   * configuration data, or null if no configuration is stored.
   */
  public Map getConnectorConfig() {
    if (properties == null) {
      properties = configStore.getConnectorConfiguration(storeContext);
    }
    return properties;
  }

  /**
   * Set the configuration data for this connector instance.
   * Writes the supplied configuration through to the persistent store.
   *
   * @param configMap a Map&lt;String, String&gt; of its ConnectorType-specific
   *        configuration data, or null if no configuration is stored.
   */
  public void setConnectorConfig(Map configMap) {
    properties = PropertiesUtils.fromMap(configMap);
    if (configMap == null) {
      configStore.removeConnectorConfiguration(storeContext);
    } else {
      configStore.storeConnectorConfiguration(storeContext, properties);
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
      schedStore.removeConnectorSchedule(storeContext);
    } else {
      schedStore.storeConnectorSchedule(storeContext,
                                        connectorSchedule);
    }
  }

  /**
   * Gets the schedule for this connector instance.
   * 
   * @return the schedule String, or null to erase any previously set schedule.
   * for this connector
   */
  public String getConnectorSchedule() {
    return schedStore.getConnectorSchedule(storeContext);
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
      stateStore.removeConnectorState(storeContext);
    } else {
      stateStore.storeConnectorState(storeContext, connectorState);
    }
  }

  /**
   * Gets the remembered traversal state for this connector instance.
   *
   * @return the state, or null if no state has been stored for this connector
   * @throws IllegalStateException if state store is disabled for this connector
   */
  public String getConnectorState() {
    return stateStore.getConnectorState(storeContext);
  }

  /**
   * Retrieve the bean description for legacy connector configuration,
   * schedule, and state stores.  The Legacy store bean may be specified
   * as an individual class of the expected type, or a Collection of
   * legacy stores.
   *
   * @param beanName the name of the legacy store bean
   * @param clazz the expected Class for the legacy store
   * @return Collection of legacy stores, or null if none found.
   */
  private static Collection getLegacyStores(String beanName, Class clazz) {
    try {
      Object bean = Context.getInstance().getBean(beanName, null);
      if (bean == null) {
        // These are optional, so they need not be defined.
        return null;
      } else if (clazz.isInstance(bean)) {
        ArrayList list = new ArrayList(1);
        list.add(bean);
        return list;
      } else if (bean instanceof Collection) {
        return (Collection)(((Collection)bean).isEmpty() ? null : bean);
      }
    } catch (BeansException e) {
      LOGGER.log(Level.WARNING, 
          "Unable to process Legacy Connector Store bean " + beanName, e);
    }
    return null;
  }

  /**
   * Upgrade ConnectorConfigStore.  If the ConnectorConfigStore has
   * no stored configuration data for this connector, look in the
   * Legacy stores (those used in earlier versions of the product).
   * If a configuration was found in a Legacy store, move it to the
   * new store.
   *
   * @param info a partially constructed InstanceInfo describing the
   * connector.
   */
  private static void upgradeConfigStore(InstanceInfo info) {
    if (info.legacyConfigStores != null) {
      Iterator iter = info.legacyConfigStores.iterator();
      while (iter.hasNext()) {
        ConnectorConfigStore legacyStore = (ConnectorConfigStore)iter.next();
        Properties properties =
            legacyStore.getConnectorConfiguration(info.storeContext);
        if (properties != null) {
          info.properties = properties;
          info.configStore.storeConnectorConfiguration(info.storeContext,
                                                       properties);
          legacyStore.removeConnectorConfiguration(info.storeContext);
          return;
        }
      }
    }
  }    

  /**
   * Upgrade ConnectorScheduleStore.  If the ConnectorScheduleStore has
   * no stored schedule data for this connector, look in the
   * Legacy stores (those used in earlier versions of the product).
   * If a schedule was found in a Legacy store, move it to the
   * new store.
   *
   * @param info a partially constructed InstanceInfo describing the
   * connector.
   */
  private static void upgradeScheduleStore(InstanceInfo info) {
    if (info.legacyScheduleStores != null) {
      Iterator iter = info.legacyScheduleStores.iterator();
      while (iter.hasNext()) {
        ConnectorScheduleStore legacyStore = (ConnectorScheduleStore)iter.next();
        String schedule = legacyStore.getConnectorSchedule(info.storeContext);
        if (schedule != null) {
          info.schedStore.storeConnectorSchedule(info.storeContext, schedule);
          legacyStore.removeConnectorSchedule(info.storeContext);
          return;
        }
      }
    }
  }    

  /**
   * Upgrade ConnectorStateStore.  If the ConnectorStateStore has
   * no stored traversal state data for this connector, look in the
   * Legacy stores (those used in earlier versions of the product).
   * If a traversal state was found in a Legacy store, move it to the
   * new store.
   *
   * @param info a partially constructed InstanceInfo describing the
   * connector.
   */
  private static void upgradeStateStore(InstanceInfo info) {
    if (info.legacyStateStores != null) {
      Iterator iter = info.legacyStateStores.iterator();
      while (iter.hasNext()) {
        ConnectorStateStore legacyStore = (ConnectorStateStore)iter.next();
        String state = legacyStore.getConnectorState(info.storeContext);
        if (state != null) {
          info.stateStore.storeConnectorState(info.storeContext, state);
          legacyStore.removeConnectorState(info.storeContext);
          return;
        }
      }
    }
  }    

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
        String connectorName, Class clazz) {
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
