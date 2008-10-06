// Copyright 2006 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.instantiator.TypeInfo;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.PropertiesException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.Properties;

/**
 * Manage persistence for schedule and state and configuration 
 * for a named connector. The persistent store for these data items
 * is via Java Preferences.
 */
public class PrefsStore implements ConnectorScheduleStore, ConnectorStateStore,
    ConnectorConfigStore {

  private static final Logger LOGGER =
      Logger.getLogger(PrefsStore.class.getName());

  private static Preferences prefs;
  private static Preferences prefsSchedule;
  private static Preferences prefsState;
  private static Preferences prefsConfig;

  public PrefsStore() {
    this(true);
  }

  public PrefsStore(boolean useUserRoot) {
    if (useUserRoot) {
      prefs = Preferences.userNodeForPackage(PrefsStore.class);
    } else {
      prefs = Preferences.systemNodeForPackage(PrefsStore.class);
    }
    prefsSchedule = prefs.node("schedule");
    prefsState = prefs.node("state");
    prefsConfig = prefs.node("configuration");
  }

  /**
   * Retrieves connector schedule.
   * @param connectorName connector name
   * @return connectorSchedule schedule of the corresponding connector.
   */
  public String getConnectorSchedule(TypeInfo typeInfo, String connectorName) {
    String connectorSchedule;
    connectorSchedule = prefsSchedule.get(connectorName, null);
    return connectorSchedule;
  }

  /**
   * Stores connector schedule.
   * @param connectorName connector name
   * @param connectorSchedule schedule of the corresponding connector.
   */
  public void storeConnectorSchedule(TypeInfo typeInfo, String connectorName,
      String connectorSchedule)  {
    prefsSchedule.put(connectorName, connectorSchedule);
  }

  /**
   * Remove a connector schedule.  If no such connector exists, do nothing.
   * @param connectorName name of the connector.
   */
  public void removeConnectorSchedule(TypeInfo typeInfo, String connectorName) {
    prefsSchedule.remove(connectorName);
    flush();
  }

  /**
   * Gets the stored state of a named connector.
   * @param connectorName connector name
   * @return the state, or null if no state has been stored for this connector
   */
  public String getConnectorState(TypeInfo typeInfo, String connectorName) {
    return prefsState.get(connectorName, null);
  }

  /**
   * Stores connector state.
   * @param connectorName connector name
   * @param connectorState state of the corresponding connector
   */
  public void storeConnectorState(TypeInfo typeInfo, String connectorName, 
      String connectorState) {
    prefsState.put(connectorName, connectorState);
  }

  /**
   * Remove connector state.  If no such connector exists, do nothing.
   * @param connectorName name of the connector.
   */
  public void removeConnectorState(TypeInfo typeInfo, String connectorName) {
    prefsState.remove(connectorName);
    flush();
  }

  /**
   * Gets the stored configuration of a named connector.
   *
   * @param typeInfo connector type information
   * @param connectorName
   * @return the configuration Properties, or null if no configuration has 
   * been stored for this connector
   */
  public Properties getConnectorConfiguration(TypeInfo typeInfo, 
      String connectorName) {
    try {
      String propStr = prefsConfig.get(connectorName, null);
      return PropertiesUtils.loadFromString(propStr);
    } catch (PropertiesException e) {
      LOGGER.log(Level.WARNING, "Failed to read connector configuration for "
                 + connectorName, e);
    }
    return null;
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
    try {
      String header = "Configuration for " + typeInfo.getConnectorTypeName()
          + " Connector " + connectorName;
      String propStr = PropertiesUtils.storeToString(configuration, header);
      prefsConfig.put(connectorName, propStr);
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
    prefsConfig.remove(connectorName);
  }

  /**
   * Clear out all persistent state (schedules and state).
   * @return true if successful
   */
  public boolean clear() {
    boolean result = true;
    try {
      prefsSchedule.clear();
    } catch (BackingStoreException e) {
      LOGGER.log(Level.WARNING, "Could not clear schedule store.", e);
      result = false;
    }
    try {
      prefsState.clear();
    } catch (BackingStoreException e) {
      LOGGER.log(Level.WARNING, "Could not clear state store.", e);
      result = false;
    }
    try {
      prefsConfig.clear();
    } catch (BackingStoreException e) {
      LOGGER.log(Level.WARNING, "Could not clear configuration store.", e);
      result = false;
    }
    if (result)
      result = flush();
    return result;
  }

  /**
   * Forces any changes in the contents of this preference node and its
   * descendants to the persistent store. Once this method returns successfully,
   * it is safe to assume that all changes made in the subtree rooted at this
   * node prior to the method invocation have become permanent.
   * @return true if successful.
   */
  public boolean flush() {
    boolean result = true;
    try {
      prefsState.flush();
    } catch (BackingStoreException e) {
      LOGGER.log(Level.WARNING,
          "Could not flush contents to persistent store.", e);
      result = false;
    }
    return result;
  }
}
