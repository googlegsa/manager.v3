// Copyright 2007-2009 Google Inc.
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
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.traversal.TraversalStateStore;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
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

  private final TypeInfo typeInfo;
  private final File connectorDir;
  private final String connectorName;
  private final StoreContext storeContext;

  private final ConnectorConfigStore configStore;
  private final ConnectorScheduleStore schedStore;
  private final ConnectorStateStore stateStore;

  private final Collection<ConnectorConfigStore> legacyConfigStores;
  private final Collection<ConnectorScheduleStore> legacyScheduleStores;
  private final Collection<ConnectorStateStore> legacyStateStores;

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

    Context context = Context.getInstance();
    this.configStore = (ConnectorConfigStore) context.getRequiredBean(
        "ConnectorConfigStore", ConnectorConfigStore.class);
    this.schedStore = (ConnectorScheduleStore) context.getRequiredBean(
        "ConnectorScheduleStore", ConnectorScheduleStore.class);
    this.stateStore = new GenerationalStateStore(
        (ConnectorStateStore) context.getRequiredBean(
        "ConnectorStateStore", ConnectorStateStore.class));

    this.legacyConfigStores =
        getLegacyStores("LegacyConnectorConfigStores",
                        ConnectorConfigStore.class);
    this.legacyScheduleStores =
        getLegacyStores("LegacyConnectorScheduleStores",
                        ConnectorScheduleStore.class);
    this.legacyStateStores =
        getLegacyStores("LegacyConnectorStateStores",
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
      if (info.properties == null) {
        throw new InstanceInfoException("Configuration not found for connector "
                                        + connectorName);
      }
    }
    if (info.schedStore.getConnectorSchedule(info.storeContext) == null) {
      upgradeScheduleStore(info);
      if (info.getConnectorSchedule() == null) {
        // If there is no schedule, create a disabled schedule rather than
        // logging "schedule not found" once a second for eternity.
        LOGGER.warning("Traversal Schedule not found for connector "
                       + connectorName + ", disabling traversal.");
        Schedule schedule = new Schedule();
        schedule.setConnectorName(connectorName);
        info.setConnectorSchedule(schedule.toString());
      }
    }
    if (info.stateStore.getConnectorState(info.storeContext) == null) {
      upgradeStateStore(info);
    }

    info.connector = makeConnectorWithSpring(info);
    return info;
  }

  public static InstanceInfo fromNewConfig(String connectorName,
      File connectorDir, TypeInfo typeInfo, Map<String, String> configMap)
      throws InstanceInfoException {
    InstanceInfo info = new InstanceInfo(connectorName, connectorDir, typeInfo);
    info.properties = PropertiesUtils.fromMap(configMap);
    // Don't write properties file to disk yet.
    info.connector = makeConnectorWithSpring(info);
    return info;
  }

  private static Connector makeConnectorWithSpring(InstanceInfo info)
      throws InstanceInfoException {
    Context context = Context.getInstance();
    String name = info.connectorName;
    Resource prototype = null;
    if (info.connectorDir != null) {
      // If this file exists, we use this it in preference to the default
      // prototype associated with the type. This allows customers to supply
      // their own per-instance config xml.
      File customPrototype =
          new File(info.connectorDir, TypeInfo.CONNECTOR_INSTANCE_XML);
      if (customPrototype.exists()) {
        prototype = new FileSystemResource(customPrototype);
        LOGGER.info("Using connector-specific xml config for connector "
            + name + " at path " + customPrototype.getPath());
      }
    }
    if (prototype == null) {
      prototype = info.typeInfo.getConnectorInstancePrototype();
    }

    DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
    XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(factory);
    Resource defaults = info.typeInfo.getConnectorDefaultPrototype();
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
      cfg.setLocation(getPropertiesResource(info));
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
  private static Resource getPropertiesResource(InstanceInfo info)
      throws InstanceInfoException {
    Properties properties =
        (info.properties == null) ? new Properties() : info.properties;
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
    @Override
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
  public Map<String, String> getConnectorConfig() {
    if (properties == null) {
      properties = configStore.getConnectorConfiguration(storeContext);
    }
    return PropertiesUtils.toMap(properties);
  }

  /**
   * Set the configuration data for this connector instance.
   * Writes the supplied configuration through to the persistent store.
   *
   * @param configMap a Map&lt;String, String&gt; of its ConnectorType-specific
   *        configuration data, or null if no configuration is stored.
   */
  public void setConnectorConfig(Map<String, String> configMap) {
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
      schedStore.storeConnectorSchedule(storeContext, connectorSchedule);
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
   * Creates a TraversalStateStore for this connector instance.
   *
   * @return a new TraversalStateStore
   */
  TraversalStateStore getTraversalStateStore() {
    return new ConnectorTraversalStateStore();
  }

  /**
   * TraversalStateStore implementation used by the Traverser to
   * maintain state between batches.
   */
  private class ConnectorTraversalStateStore implements TraversalStateStore {
    private final GenerationalStateStore store;

    public ConnectorTraversalStateStore() {
      this.store = new GenerationalStateStore(stateStore, storeContext);
    }

    /**
     * Store traversal state.
     *
     * @param state a String representation of the state to store.
     *        If null, any previous stored state is discarded.
     * @throws IllegalStateException if the store is no longer valid.
     */
    public void storeTraversalState(String state) {
      if (state == null) {
        store.removeConnectorState(storeContext);
      } else {
        store.storeConnectorState(storeContext, state);
      }
    }

    /**
     * Return a stored traversal state.
     *
     * @returns String representation of the stored state, or
     *          null if no state is stored.
     * @throws IllegalStateException if the store is no longer valid.
     */
    public String getTraversalState() {
      return store.getConnectorState(storeContext);
    }
  }

  /**
   * Retrieve the bean description for legacy connector configuration,
   * schedule, and state stores.  The Legacy store bean may be specified
   * as an individual class of the expected type, or a Collection of
   * legacy stores.
   *
   * @param beanName the name of the legacy store bean.
   * @param clazz the expected Class for the legacy store.
   * @return Collection of legacy stores, empty if none are specified.
   */
  private static <T> Collection<T> getLegacyStores(String beanName,
      Class<T> clazz) {
    ArrayList<T> list = new ArrayList<T>();
    try {
      Object bean = Context.getInstance().getBean(beanName, null);
      if (bean != null) {
        if (clazz.isInstance(bean)) {
          list.add(clazz.cast(bean));
        } else if (bean instanceof Collection) {
          Iterator<?> iter = ((Collection<?>)bean).iterator();
          while (iter.hasNext()) {
            Object store = iter.next();
            if (clazz.isInstance(store)) {
              list.add(clazz.cast(store));
            } else {
              LOGGER.warning("Legacy store " + store.getClass().getName()
                             + " does not implement " + clazz.getName());
            }
          }
        }
      }
    } catch (BeansException e) {
      LOGGER.log(Level.WARNING,
          "Unable to process Legacy Connector Store bean " + beanName, e);
    }
    return list;
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
    for (ConnectorConfigStore legacyStore : info.legacyConfigStores) {
      Properties properties =
          legacyStore.getConnectorConfiguration(info.storeContext);
      if (properties != null) {
        LOGGER.config("Migrating configuration information for connector "
                      + info.connectorName + " from legacy storage "
                      + legacyStore.getClass().getName() + " to "
                      + info.configStore.getClass().getName());
        info.properties = properties;
        info.configStore.storeConnectorConfiguration(info.storeContext,
                                                     properties);
        legacyStore.removeConnectorConfiguration(info.storeContext);
        return;
      }
    }
    LOGGER.config("Connector " + info.connectorName
                  + " lacks saved configuration information, and none was"
                  + " found in any LegacyConnectorConfigStores.");
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
    for (ConnectorScheduleStore legacyStore : info.legacyScheduleStores) {
      String schedule = legacyStore.getConnectorSchedule(info.storeContext);
      if (schedule != null) {
        LOGGER.config("Migrating traversal schedule information for connector "
                      + info.connectorName + " from legacy storage "
                      + legacyStore.getClass().getName() + " to "
                      + info.schedStore.getClass().getName());
        info.schedStore.storeConnectorSchedule(info.storeContext, schedule);
        legacyStore.removeConnectorSchedule(info.storeContext);
        return;
      }
    }
    LOGGER.config("Connector " + info.connectorName
                  + " lacks saved traversal schedule information, and none"
                  + " was found in any LegacyConnectorScheduleStores.");
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
    for (ConnectorStateStore legacyStore : info.legacyStateStores) {
      String state = legacyStore.getConnectorState(info.storeContext);
      if (state != null) {
        LOGGER.config("Migrating traversal state information for connector "
                      + info.connectorName + " from legacy storage "
                      + legacyStore.getClass().getName() + " to "
                      + info.stateStore.getClass().getName());
        info.stateStore.storeConnectorState(info.storeContext, state);
        legacyStore.removeConnectorState(info.storeContext);
        return;
      }
    }
    LOGGER.config("Connector " + info.connectorName
                  + " lacks saved traversal state information, and none was"
                  + " found in any LegacyConnectorStateStores.");
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
