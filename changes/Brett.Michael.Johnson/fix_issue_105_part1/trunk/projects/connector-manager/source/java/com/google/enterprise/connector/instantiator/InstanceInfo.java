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

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.spi.Connector;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
  private static final String GOOGLE_CONNECTOR_WORK_DIR =
      "googleConnectorWorkDir";
  private static final String GOOGLE_WORK_DIR = "googleWorkDir";
  public static final String GOOGLE_PROPERTIES_VERSION = 
      "googlePropertiesVersion";
  public static final int GOOGLE_PROPERTIES_VERSION_NUMBER = 1;

  private final Configuration configuration;
  private final Connector connector;

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
    return configuration.getConnectorName();
  }

  /**
   * @return the properties
   */
  Properties getProperties() {
    try {
      return configuration.getProperties();
    } catch (InstanceInfoException e) {
      LOGGER.log(Level.WARNING, "Failed to get configuration Properties "
          + "for connector " + getName(), e);
      return new Properties();
    }
  }

  /**
   * @return the propertiesFile
   */
  File getPropertiesFile() {
    return new File(getConnectorDir(), getName() + PROPERTIES_SUFFIX);
  }

  /**
   * @return the typeInfo
   */
  TypeInfo getTypeInfo() {
    return configuration.getTypeInfo();
  }

  /**
   * @return the connectorDir
   */
  File getConnectorDir() {
    return configuration.getConnectorDir();
  }

  private InstanceInfo(Connector connector, Configuration config) {
    this.connector = connector;
    this.configuration = config;
  }

  public static InstanceInfo fromDirectory(String connectorName,
      File connectorDir, TypeInfo typeInfo) {
    InstanceInfo result = null;
    try {
      result = fromDirectoryAndThrow(connectorName, connectorDir, typeInfo);
    } catch (InstanceInfoException e) {
      LOGGER.log(Level.WARNING, "Problem creating connector info", e);
    }
    return result;
  }

  public static InstanceInfo fromDirectoryAndThrow(String connectorName,
      File connectorDir, TypeInfo typeInfo) throws InstanceInfoException {
    return fromConfiguration(
        new FileConfiguration(connectorName, connectorDir, typeInfo));
  }

  public static InstanceInfo fromNewConfig(String connectorName,
      File connectorDir, TypeInfo typeInfo, Map configMap)
      throws InstanceInfoException {
    return fromConfiguration(
        new MapConfiguration(connectorName, connectorDir, typeInfo, configMap));
  }

  private static InstanceInfo fromConfiguration(Configuration config)
      throws InstanceInfoException {
    if (config.connectorName == null || config.connectorName.length() < 1) {
      throw new NullConnectorNameException();
    }
    if (config.connectorDir == null) {
      throw new NullDirectoryException();
    }
    if (config.typeInfo == null) {
      throw new NullTypeInfoException();
    }

    Resource prototype = null;
    File specialInstancePrototype =
        new File(config.connectorDir, SPECIAL_INSTANCE_CONFIG_FILE_NAME);
    if (specialInstancePrototype.exists()) {
      // If this file exists, we use this it in preference to the default
      // prototype associated with the type. This allows customers to supply
      // their own per-instance config xml.
      prototype = new FileSystemResource(specialInstancePrototype);
      LOGGER.log(Level.INFO,
          "Using connector-specific xml config for connector "
          + config.connectorName + " at path "
          + specialInstancePrototype.getPath());
    } else {
      prototype = config.typeInfo.getConnectorInstancePrototype();
    }

    Connector connector = makeConnectorWithSpring(prototype, config);
    return new InstanceInfo(connector, config);
  }

  private static Connector makeConnectorWithSpring(Resource prototype,
      Configuration config) throws InstanceInfoException {
    XmlBeanFactory factory;
    try {
      factory = new XmlBeanFactory(prototype);
    } catch (BeansException e) {
      throw new FactoryCreationFailureException(e, prototype,
          config.connectorName);
    }

    EncryptedPropertyPlaceholderConfigurer cfg =
        (EncryptedPropertyPlaceholderConfigurer) getRequiredBean(
            prototype, config.connectorName, factory,
            EncryptedPropertyPlaceholderConfigurer.class);
    if (cfg == null) {
      cfg = new EncryptedPropertyPlaceholderConfigurer();
    }

    try {
      cfg.setLocation(config.getResource());
      cfg.postProcessBeanFactory(factory);
    } catch (BeansException e) {
      throw new PropertyProcessingFailureException(e, prototype, config);
    }

    Connector connector = (Connector) getRequiredBean(prototype,
        config.connectorName, factory, Connector.class);

    if (connector == null) {
      throw new NoBeansFoundException(prototype,
          config.connectorName, Connector.class);
    }
    return connector;
  }

  private static Object getRequiredBean(Resource prototype,
      String connectorName, XmlBeanFactory factory, Class clazz)
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
      LOGGER.log(Level.WARNING, "Resource contains multiple " + clazz.getName()
          + " definitions.  " + "Using the first: " + beanName + ".  Skipping:"
          + buf);
    }
    return result;
  }

  public static Properties initPropertiesFromFile(File propertiesFile,
      String propertiesFileName) {
    Properties properties = null;
    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(propertiesFile);
    } catch (FileNotFoundException e) {
      LOGGER.warning("No properties file " + propertiesFileName
          + "; attempting instantiation stand-alone.");
      fileInputStream = null;
    }
    if (fileInputStream != null) {
      properties = new Properties();
      try {
        properties.load(fileInputStream);
        decryptSensitiveProperties(properties);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Problem loading properties file " +
            propertiesFileName + "; attempting instantiation stand-alone.", e);
        fileInputStream = null;
        properties = null;
      } finally {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
          LOGGER.log(Level.WARNING,
              "Problem closing properties file input stream "
                  + propertiesFileName, e);
        }
      }
    }
    if (null != properties) {
      properties.remove(GOOGLE_WORK_DIR);
      properties.remove(GOOGLE_CONNECTOR_WORK_DIR);
    }
    return properties;
  }

  public static void writePropertiesToFile(Properties properties,
      File propertiesFile) throws PropertyFileCreationFailureException {
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(propertiesFile);
    } catch (FileNotFoundException e) {
      throw new PropertyFileCreationFailureException(e, propertiesFile);
    }
    try {
      encryptSensitiveProperties(properties);
      properties.store(fos, null);
    } catch (IOException e) {
      throw new PropertyFileCreationFailureException(e, propertiesFile);
    } finally {
      try {
        fos.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Trouble closing properties file stream", e);
      }
    }
  }

  private static void encryptSensitiveProperties(Properties properties) {
    EncryptedPropertyPlaceholderConfigurer.encryptSensitiveProperties(properties);
  }

  private static void decryptSensitiveProperties(Properties properties) {
    EncryptedPropertyPlaceholderConfigurer.decryptSensitiveProperties(properties);
  }

  private static abstract class Configuration {
    protected String connectorName;
    protected File connectorDir;
    protected TypeInfo typeInfo;

    public Configuration(String connectorName, File connectorDir,
        TypeInfo typeInfo) {
      this.connectorName = connectorName;
      this.connectorDir = connectorDir;
      this.typeInfo = typeInfo;
    }
    public String getConnectorName() { return connectorName; }  
    public File getConnectorDir() { return connectorDir; }
    public TypeInfo getTypeInfo() { return typeInfo; }
    public abstract Properties getProperties() throws InstanceInfoException;
    public abstract Resource getResource() throws InstanceInfoException;
    public abstract String getDescription();
  }

  private static class MapConfiguration extends Configuration {
    private Map config;

    public MapConfiguration(String connectorName, File connectorDir,
        TypeInfo typeInfo, Map config) {
      super(connectorName, connectorDir, typeInfo);
      this.config = config;
    }

    public Properties getProperties() throws InstanceInfoException {
      Properties properties = new Properties();
      properties.putAll(config);
      properties.put(GOOGLE_CONNECTOR_WORK_DIR, connectorDir.getPath());
      properties.put(GOOGLE_WORK_DIR, Context.getInstance().getCommonDirPath());
      return properties;
    }

    public Resource getResource() throws InstanceInfoException {
      try {
        Properties properties = getProperties();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        encryptSensitiveProperties(properties);
        properties.store(baos, null);
        return new ByteArrayResourceHack(baos.toByteArray());
      } catch (IOException e) {
        throw new PropertyProcessingInternalFailureException(e, this);
      } catch (ClassCastException e) {
        throw new PropertyProcessingInternalFailureException(e, this);
      }
    }

    public String getDescription() {
      try {
        Properties properties = getProperties();
        StringWriter sw = new StringWriter();
        properties.list(new PrintWriter(sw));
        return "{ " + sw.toString() + " }";
      } catch (Exception e) {
        // This is called when throwing exceptions. Don't throw another.
        return "[ " + e.toString() + " ]";
      }
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


  private static class FileConfiguration extends Configuration {
    private File configFile;
    private Properties properties;

    public FileConfiguration(String connectorName, File connectorDir, 
        TypeInfo typeInfo) {
      super(connectorName, connectorDir, typeInfo);
      this.configFile =
          new File(connectorDir, connectorName + PROPERTIES_SUFFIX);
      this.properties = null;
    }

    public Properties getProperties() throws InstanceInfoException {
      if (properties == null) {
        properties = initPropertiesFromFile(configFile, configFile.getName());
      }
      return properties;
    }

    public Resource getResource() throws InstanceInfoException {
      return new FileSystemResource(configFile);
    }

    public String getDescription() {
      return "file " + configFile.getPath();
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
        Configuration config) {
      super("Spring internal failure while processing properties "
            + config.getDescription() + " for connector "
            + config.connectorName, cause);
    }
  }
  static class PropertyProcessingFailureException extends InstanceInfoException {
    PropertyProcessingFailureException(Throwable cause, Resource prototype,
        Configuration config) {
      super("Problem while processing properties " + config.getDescription()
            + " for connector " + config.connectorName + " using resource "
            + prototype.getDescription(), cause);
    }
  }
  static class PropertyFileCreationFailureException extends
      InstanceInfoException {
    PropertyFileCreationFailureException(Throwable cause, File propertiesFile) {
      super("Problem creating property file " + propertiesFile.getPath(), cause);
    }
  }
}
