// Copyright (C) 2006-2009 Google Inc.
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * This class keeps track of the installed connector instances, maintains a
 * corresponding directory structure and maintains properties files that
 * implement the instances.
 */
public class InstanceMap extends TreeMap<String, InstanceInfo> {

  private static final Logger LOGGER =
      Logger.getLogger(InstanceMap.class.getName());

  private TypeMap typeMap;

  public InstanceMap(TypeMap typeMap) {
    if (typeMap == null) {
      throw new IllegalArgumentException();
    }
    this.typeMap = typeMap;
    for (String typeName : typeMap.keySet()) {
      TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
      if (typeInfo == null) {
        LOGGER.log(Level.WARNING, "Skipping " + typeName);
        continue;
      }
      processTypeDirectory(typeInfo);
    }
  }

  public synchronized void shutdown() {
   for (String connectorName : this.keySet()) {
      Connector connector = getInstanceInfo(connectorName).getConnector();
      if (connector instanceof ConnectorShutdownAware) {
        try {
          ((ConnectorShutdownAware)connector).shutdown();
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Problem shutting down connector "
                     + connectorName, e);
        }
      }
    }
    this.clear();
  }

  private void processTypeDirectory(TypeInfo typeInfo) {
    File typeDirectory = typeInfo.getConnectorTypeDir();

    // find the subdirectories
    FileFilter fileFilter = new FileFilter() {
      public boolean accept(File file) {
        return file.isDirectory();
      }
    };
    File[] directories = typeDirectory.listFiles(fileFilter);

    if (directories == null) {
      // this just means the directory is empty
      return;
    }

    // process each one
    for (int i = 0; i < directories.length; i++) {
      File directory = directories[i];
      String name = directory.getName();
      if (!name.startsWith(".")) {
        NDC.pushAppend(name);
        try {
          InstanceInfo instanceInfo =
              InstanceInfo.fromDirectory(name, directory, typeInfo);
          if (instanceInfo != null) {
            this.put(name, instanceInfo);
          }
        } catch (InstantiatorException e) {
          LOGGER.log(Level.WARNING, "Problem creating connector instance", e);
        } finally {
          NDC.pop();
        }
      }
    }
  }

  public InstanceInfo getInstanceInfo(String name) {
    return get(name);
  }

  public ConfigureResponse updateConnector(String name, String typeName,
      Map<String, String> config, Locale locale, boolean update)
      throws InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException {
    InstanceInfo instanceInfo = getInstanceInfo(name);
    ConfigureResponse response = null;
    if (instanceInfo == null) {
      if (update) {
        throw new ConnectorNotFoundException();
      }
      response = createNewConnector(name, typeName, config, locale);
    } else {
      if (!update) {
        throw new ConnectorExistsException();
      }
      TypeInfo typeInfo = instanceInfo.getTypeInfo();
      String previousTypeName = typeInfo.getConnectorTypeName();
      if (previousTypeName.equals(typeName)) {
        File connectorDir = instanceInfo.getConnectorDir();
        response = resetConfig(name, connectorDir, typeInfo, config, locale);
      } else {
        // an existing connector is being given a new type - drop then add
        removeConnector(name);
        response = createNewConnector(name, typeName, config, locale);
        if (response != null) {
          // TODO: We need to restore original Connector config. This is
          // necessary once we allow update a Connector with new ConnectorType.
          LOGGER.severe("Failed to update Connector config."
              + " Need to restore original Connector config.");
        }
      }
    }
    return response;
  }

  private ConfigureResponse createNewConnector(String name, String typeName,
      Map<String, String> config, Locale locale) throws InstantiatorException {
    TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
    if (typeInfo == null) {
      throw new InstantiatorException();
    }
    File connectorDir = makeConnectorDirectory(name, typeInfo);
    try {
      ConfigureResponse result = null;
      result = resetConfig(name, connectorDir, typeInfo, config, locale);
      if (result != null && result.getMessage() != null) {
        removeConnectorDirectory(name, connectorDir, typeInfo);
      }
      return result;
    } catch (InstantiatorException ie) {
      removeConnectorDirectory(name, connectorDir, typeInfo);
      throw (ie);
    }
  }

  private ConfigureResponse resetConfig(String name, File connectorDir,
      TypeInfo typeInfo, Map<String, String> configMap, Locale locale)
      throws InstantiatorException {

    // Copy the configuration map, adding a couple of additional
    // context properties.  validateConfig() may also alter this map.
    Map<String, String> config = new HashMap<String, String>();
    config.putAll(configMap);
    config.put(PropertiesUtils.GOOGLE_CONNECTOR_NAME, name);
    config.put(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR,
               connectorDir.getPath());
    config.put(PropertiesUtils.GOOGLE_WORK_DIR,
               Context.getInstance().getCommonDirPath());

    // Validate the configuration.
    ConnectorInstanceFactory factory =
        new ConnectorInstanceFactory(name, connectorDir, typeInfo, config);
    ConfigureResponse response;
    try {
      response =
          typeInfo.getConnectorType().validateConfig(config, locale, factory);
    } catch (Exception e) {
      throw new InstantiatorException("Unexpected validateConfig failure.", e);
    } finally {
      factory.shutdown();
    }

    if (response != null) {
      // If validateConfig() returns a non-null response with an error message.
      // or populated config form, then consider it an invalid config that
      // needs to be corrected.  Return the response so that the config form
      // may be redisplayed.
      if ((response.getMessage() != null) ||
          (response.getFormSnippet() != null)) {
        LOGGER.warning("A rejected configuration for connector \"" + name
            + "\" was returned.");
        return response;
      }

      // If validateConfig() returns a response with no message or formSnippet,
      // but does include a configuration Map; then consider it a valid,
      // but possibly altered configuration and use it.
      if (response.getConfigData() != null) {
         LOGGER.config("A modified configuration for connector \"" + name
             + "\" was returned.");
        config = response.getConfigData();
      }
    }

    // We have an apparently valid configuration.  Create a connector instance
    // with that configuration.
    InstanceInfo instanceInfo =
        InstanceInfo.fromNewConfig(name, connectorDir, typeInfo, config);
    if (instanceInfo == null) {
      // We don't expect this, because an InstantiatorException should have
      // been thrown, but just in case.
      throw new InstantiatorException("Failed to create connector " + name);
    }

    // Tell old connector instance to shut down, as it is being replaced.
    if (getInstanceInfo(name) != null) {
      Connector connector = getInstanceInfo(name).getConnector();
      if (connector instanceof ConnectorShutdownAware) {
        try {
          ((ConnectorShutdownAware)connector).shutdown();
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Problem shutting down connector " + name
              + " during configuration update.", e);
        }
      }
    }

    // Only after validateConfig and instantiation succeeds do we
    // save the new configuration to persistent store.
    instanceInfo.setConnectorConfig(config);
    this.put(name, instanceInfo);

    return null;
  }

  private File makeConnectorDirectory(String name, TypeInfo typeInfo)
      throws InstantiatorException {
    File connectorTypeDir = typeInfo.getConnectorTypeDir();
    File connectorDir = new File(connectorTypeDir, name);

    if (connectorDir.exists()) {
      if (connectorDir.isDirectory()) {
        // we don't know why this directory already exists, but we're ok with it
        LOGGER.warning("Connector directory " + connectorDir.getAbsolutePath()
            + "; already exists for connector " + name);
      } else {
        throw new InstantiatorException("Existing file blocks creation of "
            + "connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name);
      }
    } else {
      connectorDir.mkdirs();
      if (!connectorDir.exists()) {
        throw new InstantiatorException("Can't create "
            + "connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name);
      }
    }

    // If connectorInstance.xml file does not exist, copy it out of the
    // Connector's jar file.
    File configXml = new File(connectorDir, TypeInfo.CONNECTOR_INSTANCE_XML);
    if (!configXml.exists()) {
      try {
        InputStream in =
            typeInfo.getConnectorInstancePrototype().getInputStream();
        String config = StringUtils.streamToStringAndThrow(in);
        FileOutputStream out = new FileOutputStream(configXml);
        out.write(config.getBytes("UTF-8"));
        out.close();
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING,"Can't extract connectorInstance.xml "
            + " to connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name, ioe);
      }
    }
    return connectorDir;
  }

  private void removeConnectorDirectory(String name, File connectorDir,
        TypeInfo typeInfo) {
    // Remove the extracted connectorInstance.xml file, but only
    // if it is unmodified.
    // TODO: Remove this when fixing CM Issue 87?
    File configXml = new File(connectorDir, TypeInfo.CONNECTOR_INSTANCE_XML);
    if (configXml.exists()) {
      try {
        InputStream in1 =
            typeInfo.getConnectorInstancePrototype().getInputStream();
        FileInputStream in2 = new FileInputStream(configXml);
        String conf1 = StringUtils.streamToStringAndThrow(in1);
        String conf2 = StringUtils.streamToStringAndThrow(in2);
        if (conf1.equals(conf2)) {
          configXml.delete();
        }
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "Can't delete connectorInstance.xml "
            + " from connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name, ioe);
      }
    }

    if (connectorDir.exists()) {
      if (!connectorDir.delete()) {
        LOGGER.warning("Can't delete connector directory "
            + connectorDir.getPath()
            + "; this connector may be difficult to delete.");
      }
    }
  }

  public void removeConnector(String name) {
    InstanceInfo instanceInfo = this.remove(name);
    if (instanceInfo == null) {
      return;
    }
    Connector connector = instanceInfo.getConnector();
    if (connector instanceof ConnectorShutdownAware) {
      try {
        LOGGER.fine("Shutting down Connector " + name);
        ((ConnectorShutdownAware)connector).shutdown();
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to shutdown connector " + name, e);
      }
      try {
        LOGGER.fine("Removing Connector " + name);
        ((ConnectorShutdownAware)connector).delete();
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to remove connector " + name, e);
      }
    }

    instanceInfo.removeConnector();
    removeConnectorDirectory(name, instanceInfo.getConnectorDir(),
        instanceInfo.getTypeInfo());
  }

  /**
   * {@link ConnectorFactory} implementation that uses
   * {@link InstanceInfo} to create an instance of a connector of the
   * specified type.  Instances created here are not placed in the official
   * <code>InstanceMap</code>.  This factory is supplied to calls to
   * {@link com.google.enterprise.connector.spi.ConnectorType#validateConfig(Map, Locale, ConnectorFactory)}.
   *
   * @see com.google.enterprise.connector.spi.ConnectorFactory
   */
  private class ConnectorInstanceFactory implements ConnectorFactory {
    String connectorName;
    File connectorDir;
    TypeInfo typeInfo;
    Map<String, String> origConfig;
    List<InstanceInfo> connectors;

    /**
     * Constructor takes the items needed by <code>InstanceInfo</code>,
     * but not provided via <code>makeConnector</code>.
     *
     * @param connectorName the name of this connector instance.
     * @param connectorDir the directory containing the connector prototype.
     * @param typeInfo the connector type.
     * @param config the configuration provided to <code>validateConfig</code>.
     */
    public ConnectorInstanceFactory(String connectorName, File connectorDir,
        TypeInfo typeInfo, Map<String, String> config) {
      this.connectorName = connectorName;
      this.connectorDir = connectorDir;
      this.typeInfo = typeInfo;
      this.origConfig = config;
      this.connectors = new ArrayList<InstanceInfo>();
    }

    /**
     * Create an instance of this connector based upon the supplied
     * configuration data.  If the supplied config <code>Map</code> is
     * <code>null</code>, use the original configuration.
     *
     * @see com.google.enterprise.connector.spi.ConnectorFactory#makeConnector(Map)
     */
    public Connector makeConnector(Map<String, String> config)
        throws RepositoryException {
      try {
        InstanceInfo info = InstanceInfo.fromNewConfig(connectorName,
            connectorDir, typeInfo, ((config == null) ? origConfig : config));
        if (info == null) {
          return null;
        }
        connectors.add(info);
        return info.getConnector();
      } catch (InstantiatorException e) {
        throw new RepositoryException(
            "ConnectorFactory failed to make connector.", e);
      }
    }

    /**
     * Shutdown any connector instances created by the factory.
     */
    private void shutdown() {
      List<InstanceInfo> connectorList;
      synchronized(this) {
        connectorList = connectors;
        connectors = null;
      }
      if (connectorList != null) {
        for (InstanceInfo info : connectorList) {
          Connector connector = info.getConnector();
          if (connector instanceof ConnectorShutdownAware) {
            try {
              ((ConnectorShutdownAware)connector).shutdown();
            } catch (Exception e) {
              LOGGER.log(Level.WARNING, "Failed to shutdown connector "
                  + info.getName() + " created by validateConfig", e);
            }
          }
        }
      }
    }
  }
}
