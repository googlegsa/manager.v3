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
 *
 * @deprecated  Replaced by FileStore
 */
public class PrefsStore implements ConnectorScheduleStore, ConnectorStateStore,
    ConnectorConfigStore {

  private static final Logger LOGGER =
      Logger.getLogger(PrefsStore.class.getName());

  private Preferences prefs = null;
  private String prefsPrefix = null;
  private Preferences prefsSchedule = null;
  private Preferences prefsState = null;
  private Preferences prefsConfig = null;
  private boolean useUserRoot = true;

  public PrefsStore() {
    this(true, null);
  }

  public PrefsStore(boolean useUserRoot) {
    this(useUserRoot, null);
  }

  public PrefsStore(boolean useUserRoot, String prefix) {
    this.useUserRoot = useUserRoot;
    this.prefsPrefix = prefix;
  }

  private synchronized void initPrefs() {
    if (prefs == null) {
      if (useUserRoot) {
        prefs = Preferences.userNodeForPackage(PrefsStore.class);
      } else {
        prefs = Preferences.systemNodeForPackage(PrefsStore.class);
      }
      if (prefsPrefix != null) {
        prefs = prefs.node(prefsPrefix);
      }
    }
  }

  private synchronized void initPrefsSchedule() {
    if (prefsSchedule == null) {
      initPrefs();
      prefsSchedule = prefs.node("schedule");
    }
  }

  private synchronized void initPrefsState() {
    if (prefsState == null) {
      initPrefs();
      prefsState = prefs.node("state");
    }
  }

  private synchronized void initPrefsConfig() {
    if (prefsConfig == null) {
      initPrefs();
      prefsConfig = prefs.node("configuration");
    }
  }

  /**
   * Retrieves connector schedule.
   * @param context a StoreContext
   * @return connectorSchedule schedule of the corresponding connector.
   */
  public String getConnectorSchedule(StoreContext context) {
    initPrefsSchedule();
    String connectorSchedule;
    connectorSchedule = prefsSchedule.get(context.getConnectorName(), null);
    return connectorSchedule;
  }

  /**
   * Stores connector schedule.
   * @param context a StoreContext
   * @param connectorSchedule schedule of the corresponding connector.
   */
  public void storeConnectorSchedule(StoreContext context,
      String connectorSchedule)  {
    initPrefsSchedule();
    prefsSchedule.put(context.getConnectorName(), connectorSchedule);
  }

  /**
   * Remove a connector schedule.  If no such connector exists, do nothing.
   * @param context a StoreContext
   */
  public void removeConnectorSchedule(StoreContext context) {
    initPrefsSchedule();
    prefsSchedule.remove(context.getConnectorName());
    flush();
  }

  /**
   * Gets the stored state of a named connector.
   * @param context a StoreContext
   * @return the state, or null if no state has been stored for this connector
   */
  public String getConnectorState(StoreContext context) {
    initPrefsState();
    return prefsState.get(context.getConnectorName(), null);
  }

  /**
   * Stores connector state.
   * @param context a StoreContext
   * @param connectorState state of the corresponding connector
   */
  public void storeConnectorState(StoreContext context,
      String connectorState) {
    initPrefsState();
    prefsState.put(context.getConnectorName(), connectorState);
  }

  /**
   * Remove connector state.  If no such connector exists, do nothing.
   * @param context a StoreContext
   */
  public void removeConnectorState(StoreContext context) {
    initPrefsState();
    prefsState.remove(context.getConnectorName());
    flush();
  }

  /**
   * Gets the stored configuration of a named connector.
   *
   * @param context a StoreContext
   * @return the configuration Properties, or null if no configuration
   *         has been stored for this connector.
   */
  public Properties getConnectorConfiguration(StoreContext context) {
    initPrefsConfig();
    try {
      String propStr = prefsConfig.get(context.getConnectorName(), null);
      return PropertiesUtils.loadFromString(propStr);
    } catch (PropertiesException e) {
      LOGGER.log(Level.WARNING, "Failed to read connector configuration for "
                 + context.getConnectorName(), e);
    }
    return null;
  }

  /**
   * Stores the configuration of a named connector.
   *
   * @param context a StoreContext
   * @param configuration Properties to store
   */
  public void storeConnectorConfiguration(StoreContext context,
      Properties configuration) {
    initPrefsConfig();
    try {
      String header = "Configuration for Connector "
          + context.getConnectorName();
      String propStr = PropertiesUtils.storeToString(configuration, header);
      prefsConfig.put(context.getConnectorName(), propStr);
    } catch (PropertiesException e) {
      LOGGER.log(Level.WARNING, "Failed to store connector configuration for "
                 + context.getConnectorName(), e);
    }
  }

  /**
   * Remove a stored connector configuration.  If no such connector exists,
   * do nothing.
   *
   * @param context a StoreContext
   */
  public void removeConnectorConfiguration(StoreContext context) {
    initPrefsConfig();
    prefsConfig.remove(context.getConnectorName());
  }

  /**
   * Clear out all persistent state (schedules and state).
   * @return true if successful
   */
  public boolean clear() {
    boolean result = true;
    if (prefs != null) {
      try {
        prefs.removeNode();
      } catch (BackingStoreException e) {
        LOGGER.log(Level.WARNING, "Could not clear preferences store.", e);
        result = false;
      }
      prefs = null;
      prefsConfig = null;
      prefsState = null;
      prefsSchedule = null;
    }
    return result;
  }

  /**
   * Forces any changes in the contents of this preference node and its
   * descendants to the persistent store. Once this method returns successfully,
   * it is safe to assume that all changes made in the subtree rooted at this
   * node prior to the method invocation have become permanent.
   *
   * @return true if successful.
   */
  public boolean flush() {
    boolean result = true;
    if (prefsState != null) {
      try {
        prefsState.flush();
      } catch (BackingStoreException e) {
        LOGGER.log(Level.WARNING,
                   "Could not flush contents to persistent store.", e);
        result = false;
      }
    }
    return result;
  }
}
