// Copyright (C) 2006-2008 Google Inc.
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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * This class keeps track of the installed connector instances, maintains a
 * corresponding directory structure and maintains properties files that
 * implement the instances.
 */
public class InstanceMap extends TreeMap {

  private static final Logger LOGGER = Logger.getLogger(InstanceMap.class
      .getName());

  private TypeMap typeMap;

  public InstanceMap(TypeMap typeMap) {
    if (typeMap == null) {
      throw new IllegalArgumentException();
    }
    this.typeMap = typeMap;
    for (Iterator i = typeMap.keySet().iterator(); i.hasNext();) {
      String typeName = (String) i.next();
      TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
      if (typeInfo == null) {
        LOGGER.log(Level.WARNING, "Skipping " + typeName);
        continue;
      }
      processTypeDirectory(typeInfo);
    }
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
        try {
          InstanceInfo instanceInfo = 
              InstanceInfo.fromDirectory(name, directory, typeInfo);
          if (instanceInfo != null) {
            this.put(name, instanceInfo);
          }
        } catch (InstantiatorException e) {
          LOGGER.log(Level.WARNING, "Problem creating connector instance", e);
        }
      }
    }
  }

  public InstanceInfo getInstanceInfo(String name) {
    return (InstanceInfo) get(name);
  }

  public ConfigureResponse updateConnector(String name, String typeName,
      Map config, Locale locale, boolean update) throws InstantiatorException,
      ConnectorNotFoundException, ConnectorExistsException {
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
      Map config, Locale locale) throws InstantiatorException {
    TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
    if (typeInfo == null) {
      throw new InstantiatorException();
    }
    File connectorDir = makeConnectorDirectory(name, typeInfo);
    return resetConfig(name, connectorDir, typeInfo, config, locale);
  }

  private ConfigureResponse resetConfig(String name, File connectorDir,
      TypeInfo typeInfo, Map config, Locale locale)
      throws InstantiatorException {
    // First, validate the configuration.
    ConfigureResponse response =
        typeInfo.getConnectorType().validateConfig(config, locale,
            new ConnectorInstanceFactory(name, connectorDir, typeInfo, config));

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
        return connectorDir;
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
    return connectorDir;
  }

  public void removeConnector(String name) {
    InstanceInfo instanceInfo = (InstanceInfo) this.remove(name);
    if (instanceInfo == null) {
      return;
    }
    File connectorDir = instanceInfo.getConnectorDir();
    instanceInfo.removeConnector();
    if (connectorDir.exists()) {
      if (!connectorDir.delete()) {
        LOGGER.warning("Can't delete connector directory "
            + connectorDir.getPath()
            + "; this connector may be difficult to delete.");
      }
    }
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
    Map origConfig;

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
        TypeInfo typeInfo, Map config) {
      this.connectorName = connectorName;
      this.connectorDir = connectorDir;
      this.typeInfo = typeInfo;
      this.origConfig = config;
    }

    /**
     * Create an instance of this connector based upon the supplied
     * configuration data.  If the supplied config <code>Map</code> is 
     * <code>null</code>, use the original configuration.
     *
     * @see com.google.enterprise.connector.spi.ConnectorFactory#makeConnector(Map)
     */
    public Connector makeConnector(Map config) throws RepositoryException {
      try {
        InstanceInfo info = InstanceInfo.fromNewConfig(connectorName,
            connectorDir, typeInfo, ((config == null) ? origConfig : config));
        return (info == null) ? null : info.getConnector();
      } catch (InstantiatorException e) {
        throw new RepositoryException(
            "ConnectorFactory failed to make connector.", e);
      }
    }
  }
}
