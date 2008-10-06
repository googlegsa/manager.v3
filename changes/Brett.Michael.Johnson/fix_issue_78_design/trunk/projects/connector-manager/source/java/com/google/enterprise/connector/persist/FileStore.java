// Copyright 2008 Google Inc.  All Rights Reserved.
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
package com.google.enterprise.connector.persist;

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.instantiator.TypeInfo;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Manage persistence for schedule and state and configuration 
 * for a named connector. The persistent store for these data items
 * are files in the connector's work directory.
 */
public class FileStore implements ConnectorScheduleStore,
    ConnectorStateStore, ConnectorConfigStore {

  private static final Logger LOGGER =
      Logger.getLogger(FileStore.class.getName());

  private HashMap cacheMap = new HashMap();
  private static final String schedName = "_schedule.txt";
  private static final String stateName = "_state.txt";
  private static final String configName = ".properties";

  /**
   * Retrieves connector schedule.
   *
   * @param typeInfo connector type information
   * @param connectorName connector name
   * @return connectorSchedule schedule of the corresponding connector.
   */
  public String getConnectorSchedule(TypeInfo typeInfo, String connectorName) {
    String key = connectorName + schedName;
    if (!cacheMap.containsKey(key)) {
      cacheMap.put(key, readStoreFile(typeInfo, connectorName, schedName));
    }
    return (String) cacheMap.get(key);
  }

  /**
   * Stores connector schedule.
   *
   * @param typeInfo connector type information
   * @param connectorName connector name
   * @param connectorSchedule schedule of the corresponding connector.
   */
  public void storeConnectorSchedule(TypeInfo typeInfo, String connectorName,
      String connectorSchedule)  {
    cacheMap.put(connectorName + schedName, connectorSchedule);
    writeStoreFile(typeInfo, connectorName, schedName, connectorSchedule);
  }

  /**
   * Remove a connector schedule.  If no such connector exists, do nothing.
   *
   * @param typeInfo connector type information
   * @param connectorName name of the connector.
   */
  public void removeConnectorSchedule(TypeInfo typeInfo, String connectorName) {
    cacheMap.remove(connectorName + schedName);
    deleteStoreFile(typeInfo, connectorName, schedName);
  }

  /**
   * Gets the stored state of a named connector.
   *
   * @param typeInfo connector type information
   * @param connectorName connector name
   * @return the state, or null if no state has been stored for this connector
   */
  public String getConnectorState(TypeInfo typeInfo, String connectorName) {
    String key = connectorName + stateName;
    if (!cacheMap.containsKey(key)) {
      cacheMap.put(key, readStoreFile(typeInfo, connectorName, stateName));
    }
    return (String) cacheMap.get(key);
  }

  /**
   * Stores connector state.
   *
   * @param typeInfo connector type information
   * @param connectorName connector name
   * @param connectorState state of the corresponding connector
   */
  public void storeConnectorState(TypeInfo typeInfo, String connectorName, 
      String connectorState) {
    cacheMap.put(connectorName + stateName, connectorState);
    writeStoreFile(typeInfo, connectorName, stateName, connectorState);
  }

  /**
   * Remove connector state.  If no such connector exists, do nothing.
   *
   * @param typeInfo connector type information
   * @param connectorName name of the connector.
   */
  public void removeConnectorState(TypeInfo typeInfo, String connectorName) {
    cacheMap.remove(connectorName + stateName);
    getStoreFile(typeInfo, connectorName, stateName).delete();
  }


  /**
   * Gets the stored configuration of a named connector.
   *
   * @param typeInfo connector type information
   * @param connectorName
   * @return the configuration Properties, or null if no configuration has been stored
   * for this connector
   */
  public Properties getConnectorConfiguration(TypeInfo typeInfo, 
      String connectorName) {
    Properties props = null;
    File propFile = getStoreFile(typeInfo, connectorName, configName);
    if (propFile.exists()) {
      try {
        props = PropertiesUtils.loadFromFile(propFile);
      } catch (PropertiesException e) {
        LOGGER.log(Level.WARNING, "Failed to read connector configuration for "
                   + connectorName, e);
      }
    }
    return props;
  }

  /**
   * Stores the configuration of a named connector.
   * 
   * @param typeInfo connector type information
   * @param connectorName
   * @param configuration Properties to store
   */
  public void storeConnectorConfiguration(TypeInfo typeInfo, 
      String connectorName, Properties configuration) {
    File propFile = getStoreFile(typeInfo, connectorName, configName);
    String header = "Configuration for " + typeInfo.getConnectorTypeName()
        + " Connector " + connectorName;
    try {
      PropertiesUtils.storeToFile(configuration, propFile, header);
    } catch (PropertiesException e) {
      LOGGER.log(Level.WARNING, "Failed to store connector configuration for "
                 + connectorName, e);
    }
  }
  
  /**
   * Remove a stored connector configuration.  If no such connector exists,
   * do nothing.
   *
   * @param typeInfo connector type information
   * @param connectorName name of the connector.
   */
  public void removeConnectorConfiguration(TypeInfo typeInfo,
      String connectorName) {
    deleteStoreFile(typeInfo, connectorName, configName);
  }


  /**
   * Return a File object represting the on-disk store.
   *
   * @param typeInfo
   * @param connectorName
   * @param suffix
   */
  private File getStoreFile(TypeInfo typeInfo, String connectorName, 
      String suffix) {
    File connectorDir = new File(typeInfo.getConnectorTypeDir(), connectorName);
    return new File(connectorDir, connectorName + suffix);
  }

  /**
   * Delete a store file.
   * @param typeInfo
   * @param connectorName
   * @param suffix
   */
  private void deleteStoreFile(TypeInfo typeInfo, String connectorName, 
      String suffix) {
    getStoreFile(typeInfo, connectorName, suffix).delete();
  }

  /**
   * Write the data to a store file.
   *
   * @param typeInfo
   * @param connectorName
   * @param suffix
   * @param data
   */
  private void writeStoreFile(TypeInfo typeInfo, String connectorName,
      String suffix, String data) {
    FileOutputStream fos = null;
    File storeFile = null;
    try {
      storeFile = getStoreFile(typeInfo, connectorName, suffix);
      fos = new FileOutputStream(storeFile);
      fos.write(data.getBytes());
      fos.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Cannot write store file "
          + storeFile + " for connector " + connectorName, e);
      try { if (fos != null) fos.close(); } catch (IOException e1) {}
    }
  }

  /**
   * Read a store file, returning a String containing the contents.
   *
   * @param typeInfo
   * @param connectorName
   * @param suffix
   * @return String containing store file contents or null if none exists
   */
  private String readStoreFile(TypeInfo typeInfo, String connectorName,
      String suffix) {
    FileInputStream fis = null;
    File storeFile = null;
    try {
      storeFile = getStoreFile(typeInfo, connectorName, suffix);
      int length = (int) storeFile.length();
      if (length == 0) {
        return (storeFile.exists() ? "" : null);
      }
      byte[] buffer = new byte[length];
      fis = new FileInputStream(storeFile);
      fis.read();
      return new String(buffer);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Cannot read store file "
          + storeFile + " for connector " + connectorName, e);
      return null;
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e1) {
          LOGGER.log(Level.WARNING, "Error closing store file "
              + storeFile + " for connector " + connectorName, e1);
        }
      }
    }
  }
}
