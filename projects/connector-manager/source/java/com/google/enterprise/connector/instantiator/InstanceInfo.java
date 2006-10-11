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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.spi.Connector;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
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
 * 
 */
public class InstanceInfo {

  private static final Logger LOGGER =
      Logger.getLogger(InstanceInfo.class.getName());

  private static final String PROPERTIES_SUFFIX = ".properties";
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

  /**
   * @param typeInfo
   * @param name
   * @param connector
   * @param properties
   * @param propertiesFile
   */
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
      File connectorDir, TypeInfo typeInfo) throws FactoryCreationFailure,
      BeanListFailure, NoBeansFound, BeanInstantiationFailure,
      NullDirectoryException, NullTypeInfoException,
      NullConnectorNameException, PropertyProcessingInternalFailure,
      PropertyProcessingFailure {

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

    properties =
        initPropertiesFromFile(propertiesFile, connectorName,
            propertiesFileName);

    Resource connectorInstancePrototype =
        typeInfo.getConnectorInstancePrototype();

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
      throws FactoryCreationFailure, BeanListFailure, NoBeansFound,
      BeanInstantiationFailure, PropertyProcessingInternalFailure,
      PropertyProcessingFailure {
    XmlBeanFactory factory;

    try {
      factory = new XmlBeanFactory(connectorInstancePrototype);
    } catch (BeansException e) {
      throw new FactoryCreationFailure(e, connectorInstancePrototype,
          connectorName);
    }

    if (properties != null) {
      PropertyPlaceholderConfigurer cfg;
      try {
        cfg = new PropertyPlaceholderConfigurer();
        cfg.setLocation(new FileSystemResource(propertiesFile));
      } catch (Exception e) {
        throw new PropertyProcessingInternalFailure(e, propertiesFile,
            connectorInstancePrototype, connectorName);
      }
      try {
        cfg.postProcessBeanFactory(factory);
      } catch (Exception e) {
        throw new PropertyProcessingFailure(e, propertiesFile,
            connectorInstancePrototype, connectorName);
      }
    }

    // get the list of Connectors defined in the bean factory
    String beanList[];
    try {
      beanList = factory.getBeanNamesForType(Connector.class);
    } catch (Exception e) {
      throw new BeanListFailure(e, connectorInstancePrototype, connectorName,
          Connector.class);
    }

    // make sure there is at least one Connector Type
    if (beanList.length < 1) {
      throw new NoBeansFound(connectorInstancePrototype, connectorName,
          Connector.class);
    }

    // remember the name of the first one found, and instantiate it
    String beanName = beanList[0];
    try {
      connector = (Connector) factory.getBean(beanName);
    } catch (Exception e) {
      throw new BeanInstantiationFailure(e, connectorInstancePrototype,
          connectorName, beanName);
    }

    // if more Connectors were found issue a warning
    if (beanList.length > 1) {
      StringBuffer buf = new StringBuffer();
      for (int i = 1; i < beanList.length; i++) {
        buf.append(" ");
        buf.append(beanList[i]);
      }
      LOGGER.log(Level.WARNING,
          "Resource contains multiple Connector Type definitions.  Using the first: "
              + beanName + ".  Skipping:" + buf);
    }

    return connector;
  }

  private static Properties initPropertiesFromFile(File propertiesFile,
      String connectorName, String propertiesFileName) {
    Properties properties = null;
    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(propertiesFile);
    } catch (FileNotFoundException e) {
      LOGGER.warning("No properties file for connector " + connectorName
          + "; attempting instantiation stand-alone.");
      fileInputStream = null;
    }
    if (fileInputStream != null) {
      properties = new Properties();
      try {
        properties.load(fileInputStream);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Problem loading properties file "
            + propertiesFileName + " for connector " + connectorName
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
                  + propertiesFileName + " for connector " + connectorName, e);
        }
      }
    }
    properties.remove(GOOGLE_WORK_DIR);
    properties.remove(GOOGLE_CONNECTOR_WORK_DIR);
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
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(propertiesFile);
    } catch (FileNotFoundException e) {
      throw new PropertyFileCreationFailure(e, propertiesFile);
    }
    try {
      properties.store(fos, null);
    } catch (IOException e) {
      throw new PropertyFileCreationFailure(e, propertiesFile);
    } finally {
      try {
        fos.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Trouble closing properties file stream", e);
      }
    }
    return fromDirectoryAndThrow(connectorName, connectorDir, typeInfo);
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
  static class FactoryCreationFailure extends InstanceInfoException {
    FactoryCreationFailure(Throwable cause,
        Resource connectorInstancePrototype, String connectorName) {
      super("Spring factory creation failure for connector " + connectorName
          + " using resource " + connectorInstancePrototype.getDescription(),
          cause);
    }
  }
  static class BeanListFailure extends InstanceInfoException {
    BeanListFailure(Throwable cause, Resource connectorInstancePrototype,
        String connectorName, Class clazz) {
      super("Spring failure while accessing beans of type " + clazz.getName()
          + " for connector " + connectorName + " using resource "
          + connectorInstancePrototype.getDescription(), cause);
    }
  }
  static class NoBeansFound extends InstanceInfoException {
    NoBeansFound(Resource connectorInstancePrototype, String connectorName,
        Class clazz) {
      super("No beans found of type " + clazz.getName() + " for connector "
          + connectorName + " using resource "
          + connectorInstancePrototype.getDescription());
    }
  }
  static class BeanInstantiationFailure extends InstanceInfoException {
    BeanInstantiationFailure(Throwable cause,
        Resource connectorInstancePrototype, String connectorName,
        String beanName) {
      super("Spring failure while instantiating bean " + beanName
          + " for connector " + connectorName + " using resource "
          + connectorInstancePrototype.getDescription(), cause);
    }
  }
  static class PropertyProcessingInternalFailure extends InstanceInfoException {
    PropertyProcessingInternalFailure(Throwable cause, File propertiesFile,
        Resource connectorInstancePrototype, String connectorName) {
      super("Spring internal failure while processing properties file "
          + propertiesFile.getPath() + " for connector " + connectorName
          + " using resource " + connectorInstancePrototype.getDescription(),
          cause);
    }
  }
  static class PropertyProcessingFailure extends InstanceInfoException {
    PropertyProcessingFailure(Throwable cause, File propertiesFile,
        Resource connectorInstancePrototype, String connectorName) {
      super("Problem while processing properties file "
          + propertiesFile.getPath() + " for connector " + connectorName
          + " using resource " + connectorInstancePrototype.getDescription(),
          cause);
    }
  }
  static class PropertyFileCreationFailure extends InstanceInfoException {
    PropertyFileCreationFailure(Throwable cause, File propertiesFile) {
      super("Problem creating property file " + propertiesFile.getPath(), cause);
    }
  }
}
