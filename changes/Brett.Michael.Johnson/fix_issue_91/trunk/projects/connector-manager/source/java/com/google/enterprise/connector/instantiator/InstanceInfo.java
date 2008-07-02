// Copyright 2007 Google Inc.
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
  private static final String GOOGLE_CONNECTOR_WORK_DIR =
      "googleConnectorWorkDir";
  private static final String GOOGLE_WORK_DIR = "googleWorkDir";

  private final TypeInfo typeInfo;
  private final String name;
  private final Connector connector;
  private final Properties properties;
  private final File propertiesFile;
  private final File connectorDir;

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
    return name;
  }

  /**
   * @return the properties
   */
  Properties getProperties() {
    return properties;
  }

  /**
   * @return the propertiesFile
   */
  File getPropertiesFile() {
    return propertiesFile;
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

  private InstanceInfo(final TypeInfo typeInfo, final String name,
      final Connector connector, final Properties properties,
      final File propertiesFile, final File connectorDir) {
    this.typeInfo = typeInfo;
    this.name = name;
    this.connector = connector;
    this.properties = properties;
    this.propertiesFile = propertiesFile;
    this.connectorDir = connectorDir;
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
      File connectorDir, TypeInfo typeInfo)
      throws FactoryCreationFailureException, NoBeansFoundException,
      BeanInstantiationFailureException, NullDirectoryException,
      NullTypeInfoException, NullConnectorNameException,
      PropertyProcessingFailureException {

    if (connectorName == null || connectorName.length() < 1) {
      throw new NullConnectorNameException();
    }

    if (connectorDir == null) {
      throw new NullDirectoryException();
    }

    if (typeInfo == null) {
      throw new NullTypeInfoException();
    }

    Connector connector = null;
    Properties properties = null;

    String propertiesFileName = connectorName + PROPERTIES_SUFFIX;
    File propertiesFile = new File(connectorDir, propertiesFileName);

    properties = initPropertiesFromFile(propertiesFile, propertiesFileName);

    Resource connectorInstancePrototype = null;

    File specialInstancePrototype =
        new File(connectorDir, SPECIAL_INSTANCE_CONFIG_FILE_NAME);
    if (specialInstancePrototype.exists()) {
      // if this file exists, we use this it in preference to the default
      // prototype associated with the type. This allows customers to supply
      // their own per-instance config xml
      connectorInstancePrototype =
          new FileSystemResource(specialInstancePrototype);
      LOGGER.log(Level.INFO,
          "Using connector-specific xml config for connector " + connectorName
              + " at path " + specialInstancePrototype.getPath());
    } else {
      connectorInstancePrototype = typeInfo.getConnectorInstancePrototype();
    }

    connector =
        makeConnectorWithSpring(connector, properties, propertiesFile,
            connectorInstancePrototype, connectorName);

    InstanceInfo result =
        new InstanceInfo(typeInfo, connectorName, connector, properties,
            propertiesFile, connectorDir);
    return result;
  }

  private static Connector makeConnectorWithSpring(Connector connector,
      Properties properties, File propertiesFile,
      Resource connectorInstancePrototype, String connectorName)
      throws FactoryCreationFailureException, NoBeansFoundException,
      BeanInstantiationFailureException, PropertyProcessingFailureException {
    XmlBeanFactory factory;

    try {
      factory = new XmlBeanFactory(connectorInstancePrototype);
    } catch (BeansException e) {
      throw new FactoryCreationFailureException(e, connectorInstancePrototype,
          connectorName);
    }
    
    if (properties != null) {
      EncryptedPropertyPlaceholderConfigurer cfg =
          (EncryptedPropertyPlaceholderConfigurer) getRequiredBean(
              connectorInstancePrototype, connectorName, factory,
              EncryptedPropertyPlaceholderConfigurer.class);
      if (cfg == null) {
        cfg = new EncryptedPropertyPlaceholderConfigurer();
      }      
      cfg.setLocation(new FileSystemResource(propertiesFile));      
      try {
        cfg.postProcessBeanFactory(factory);
      } catch (BeansException e) {        
        throw new PropertyProcessingFailureException(e, propertiesFile,
            connectorInstancePrototype, connectorName);
      }
    }

    connector =
        (Connector) getRequiredBean(connectorInstancePrototype, connectorName,
            factory, Connector.class);
    if (connector == null) {
      throw new NoBeansFoundException(connectorInstancePrototype,
          connectorName, Connector.class);
    }
    return connector;
  }

  private static Object getRequiredBean(Resource connectorInstancePrototype,
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
          connectorInstancePrototype, connectorName, beanName);
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
        LOGGER
            .log(Level.WARNING, "Problem loading properties file "
                + propertiesFileName
                + "; attempting instantiation stand-alone.", e);
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

  public static InstanceInfo fromNewConfig(String connectorName,
      File connectorDir, TypeInfo typeInfo, Map config)
      throws InstantiatorException {
    Properties properties = new Properties();
    properties.putAll(config);
    properties.put(GOOGLE_CONNECTOR_WORK_DIR, connectorDir.getPath());
    properties.put(GOOGLE_WORK_DIR, Context.getInstance().getCommonDirPath());
    String propertiesFileName = connectorName + PROPERTIES_SUFFIX;
    File propertiesFile = new File(connectorDir, propertiesFileName);
    writePropertiesToFile(properties, propertiesFile);
    return fromDirectoryAndThrow(connectorName, connectorDir, typeInfo);
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
    String plainPassword = properties.getProperty("Password");
    if (plainPassword != null) {
      String encryptedPassword = 
          EncryptedPropertyPlaceholderConfigurer.encryptString(plainPassword);
      properties.setProperty("Password", encryptedPassword);
    }
  }

  private static void decryptSensitiveProperties(Properties properties) {
    String encryptedPassword = properties.getProperty("Password");
    if (encryptedPassword != null) {
      String plainPassword =
          EncryptedPropertyPlaceholderConfigurer.decryptString(encryptedPassword);
      properties.setProperty("Password", plainPassword);
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
        Resource connectorInstancePrototype, String connectorName) {
      super("Spring factory creation failure for connector " + connectorName
          + " using resource " + connectorInstancePrototype.getDescription(),
          cause);
    }
  }
  static class BeanListFailureException extends InstanceInfoException {
    BeanListFailureException(Throwable cause,
        Resource connectorInstancePrototype, String connectorName, Class clazz) {
      super("Spring failure while accessing beans of type " + clazz.getName()
          + " for connector " + connectorName + " using resource "
          + connectorInstancePrototype.getDescription(), cause);
    }
  }
  static class NoBeansFoundException extends InstanceInfoException {
    NoBeansFoundException(Resource connectorInstancePrototype,
        String connectorName, Class clazz) {
      super("No beans found of type " + clazz.getName() + " for connector "
          + connectorName + " using resource "
          + connectorInstancePrototype.getDescription());
    }
  }
  static class BeanInstantiationFailureException extends InstanceInfoException {
    BeanInstantiationFailureException(Throwable cause,
        Resource connectorInstancePrototype, String connectorName,
        String beanName) {
      super("Spring failure while instantiating bean " + beanName
          + " for connector " + connectorName + " using resource "
          + connectorInstancePrototype.getDescription(), cause);
    }
  }
  static class PropertyProcessingInternalFailureException extends
      InstanceInfoException {
    PropertyProcessingInternalFailureException(Throwable cause,
        File propertiesFile, Resource connectorInstancePrototype,
        String connectorName) {
      super("Spring internal failure while processing properties file "
          + propertiesFile.getPath() + " for connector " + connectorName
          + " using resource " + connectorInstancePrototype.getDescription(),
          cause);
    }
  }
  static class PropertyProcessingFailureException extends InstanceInfoException {
    PropertyProcessingFailureException(Throwable cause, File propertiesFile,
        Resource connectorInstancePrototype, String connectorName) {
      super("Problem while processing properties file "
          + propertiesFile.getPath() + " for connector " + connectorName
          + " using resource " + connectorInstancePrototype.getDescription(),
          cause);
    }
  }
  static class PropertyFileCreationFailureException extends
      InstanceInfoException {
    PropertyFileCreationFailureException(Throwable cause, File propertiesFile) {
      super("Problem creating property file " + propertiesFile.getPath(), cause);
    }
  }

}
