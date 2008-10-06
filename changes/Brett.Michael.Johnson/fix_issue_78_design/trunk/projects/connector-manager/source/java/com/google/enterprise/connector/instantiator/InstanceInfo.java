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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

  private static final String PROPERTIES_SUFFIX = ".properties";
  private static final String SPECIAL_INSTANCE_CONFIG_FILE_NAME =
      "connectorInstance.xml";

  private final TypeInfo typeInfo;
  private final File connectorDir;
  private final String connectorName;

  private final ConnectorConfigStore configStore;
  private final ConnectorScheduleStore schedStore;
  private final ConnectorStateStore stateStore;

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
   * @return the connector configuration Map.
   */
  Map getConfigMap() {
    if (properties == null) {
      properties = 
          configStore.getConnectorConfiguration(typeInfo, connectorName);
    }
    return properties;
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
      TypeInfo typeInfo) {
    this.connectorName = connectorName;
    this.connectorDir = connectorDir;
    this.typeInfo = typeInfo;
    Context context = Context.getInstance();
    this.configStore = (ConnectorConfigStore) context.getRequiredBean(
        "ConnectorConfigStore", ConnectorConfigStore.class);
    this.schedStore = (ConnectorScheduleStore) context.getRequiredBean(
        "ConnectorScheduleStore", ConnectorScheduleStore.class);
    this.stateStore = new GenerationalStateStore(
        (ConnectorStateStore) context.getRequiredBean(
        "ConnectorStateStore", ConnectorStateStore.class));
  }

  public void removeConnector() {
    // Side effect of GenerationalStateStore.removeConnectorState() 
    // is a new generation.
    stateStore.removeConnectorState(typeInfo, connectorName);
    schedStore.removeConnectorSchedule(typeInfo, connectorName);
    configStore.removeConnectorConfiguration(typeInfo, connectorName);
  }

  public static InstanceInfo fromDirectory(String connectorName,
      File connectorDir, TypeInfo typeInfo) throws InstanceInfoException {
    InstanceInfo info = new InstanceInfo(connectorName, connectorDir, typeInfo);
    // TODO: [bmj] upgrade legacy stores here.
    info.properties = info.configStore.getConnectorConfiguration(typeInfo,
                                                                 connectorName);
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
    info.connector = makeConnectorWithSpring(info);
    return info;
  }

  private static Connector makeConnectorWithSpring(InstanceInfo info)
      throws InstanceInfoException {
    if (info.connectorName == null || info.connectorName.length() < 1) {
      throw new NullConnectorNameException();
    }
    if (info.typeInfo == null) {
      throw new NullTypeInfoException();
    }

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
    EncryptedPropertyPlaceholderConfigurer cfg =
        (EncryptedPropertyPlaceholderConfigurer) getBean(
            prototype, info.connectorName, factory,
            EncryptedPropertyPlaceholderConfigurer.class);
    if (cfg == null) {
      cfg = new EncryptedPropertyPlaceholderConfigurer();
    }

    try {
      cfg.setLocation(getPropertiesResource(info));
      cfg.postProcessBeanFactory(factory);
    } catch (BeansException e) {
      throw new PropertyProcessingFailureException(e, prototype, info.connectorName);
    }

    Connector connector = (Connector) getBean(prototype,
        info.connectorName, factory, Connector.class);
    if (connector == null) {
      throw new NoBeansFoundException(prototype,
          info.connectorName, Connector.class);
    }
    return connector;
  }

  private static Object getBean(Resource prototype, String connectorName, 
      XmlBeanFactory factory, Class clazz)
      throws BeanInstantiationFailureException {
    Object result;
    // get the list of beans defined in the bean factory of the required type
    String[] beanList;
    beanList = factory.getBeanNamesForType(clazz);

    // make sure there is at least one
    if (beanList.length < 1) {
      return null;
    }

    // remember the name of the first one found, and instantiate it
    String beanName = beanList[0];
    try {
      result = factory.getBean(beanName);
    } catch (BeansException e) {
      throw new BeanInstantiationFailureException(e,
          prototype, connectorName, beanName);
    }

    // if more beans were found issue a warning
    if (beanList.length > 1) {
      StringBuffer buf = new StringBuffer();
      for (int i = 1; i < beanList.length; i++) {
        buf.append(" ");
        buf.append(beanList[i]);
      }
      LOGGER.warning("Resource contains multiple " + clazz.getName() +
          " definitions. Using the first: " + beanName + ". Skipping:" + buf);
    }
    return result;
  }

  /**
   * Return a Spring Resource containing the InstanceInfo 
   * configuration Properties.
   */
  private static Resource getPropertiesResource(InstanceInfo info)
      throws InstanceInfoException {
    try {
      return new ByteArrayResourceHack(
          PropertiesUtils.storeToString(info.properties, null).getBytes());
    } catch (PropertiesException e) {
      throw new PropertyProcessingInternalFailureException(e, info.connectorName);
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
    return configStore.getConnectorConfiguration(typeInfo, connectorName);
  }

  public void setConnectorConfig(Map configMap) {
    if (configMap == null) {
      configStore.removeConnectorConfiguration(typeInfo, connectorName);
    } else {
      configStore.storeConnectorConfiguration(typeInfo, connectorName,
          PropertiesUtils.fromMap(configMap));
    }
  }

  /**
   * Sets the schedule for this connector instance.
   * 
   * @param connectorSchedule String to store or null unset any existing
   * schedule.
   */
  public void setConnectorSchedule(String connectorSchedule) {
    if (connectorSchedule == null) {
      schedStore.removeConnectorSchedule(typeInfo, connectorName);
    } else {
      schedStore.storeConnectorSchedule(typeInfo, connectorName,
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
    return schedStore.getConnectorSchedule(typeInfo, connectorName);
  }

  /**
   * Sets the remembered traversal state for this connector instance.
   *
   * @param connectorState String to store or null to erase any previously
   * saved traversal state.
   * @throws IllegalStateException if state store is disabled for this connector
   */
  public void setConnectorState(String connectorState) {
    if (connectorState == null) {
      stateStore.removeConnectorState(typeInfo, connectorName);
    } else {
      stateStore.storeConnectorState(typeInfo, connectorName, connectorState);
    }
  }

  /**
   * Gets the remembered traversal state for this connector instance.
   *
   * @return the state, or null if no state has been stored for this connector
   * @throws IllegalStateException if state store is disabled for this connector
   */
  public String getConnectorState() {
    return stateStore.getConnectorState(typeInfo, connectorName);
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
