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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Class to get and set the schedule and state for a named connector. 
 *
 */
public class PrefsStore implements ConnectorScheduleStore, ConnectorStateStore {

  private static final Logger LOGGER =
    Logger.getLogger(PrefsStore.class.getName());

  private static Preferences prefs = 
    Preferences.userNodeForPackage(PrefsStore.class);
  private static Preferences prefsSchedule = prefs.node("schedule");
  private static Preferences prefsState = prefs.node("state");
  
  public PrefsStore() {
  
  }
  
  /**
   * Retrieves connector schedule.
   * @param connectorName connector name
   * @return connectorSchedule schedule of the corresponding connector.
   */
  public String getConnectorSchedule(String connectorName)  {
    String connectorSchedule;
    connectorSchedule = prefsSchedule.get(connectorName, null);
    return connectorSchedule;
  }

  /**
   * Stores connector schedule.
   * @param connectorName connector name
   * @param connectorSchedule schedule of the corresponding connector.
   */
  public void storeConnectorSchedule(String connectorName,
      String connectorSchedule)  {    
    prefsSchedule.put(connectorName, connectorSchedule);
  }
  
  /**
   * Remove a connector schedule.  If no such connector exists, do nothing.
   * @param connectorName name of the connector.
   */
  public void removeConnectorSchedule(String connectorName) {
    prefsSchedule.remove(connectorName);
  }
    
  /**
   * Retrieves connector state
   * @param connectorName connector name
   * @return connectorState state of the corresponding connector.
   */
  public String getConnectorState(String connectorName) {
    String connectorState;
    connectorState = prefsState.get(connectorName, null);
    return connectorState;
  }
  
  /**
   * Stores connector state.
   * @param connectorName connector name
   * @param connectorState state of the corresponding connector.
   */
  public void storeConnectorState(String connectorName, String connectorState) {
    prefsState.put(connectorName, connectorState);    
  }
  
  /**
   * Remove connector state.  If no such connector exists, do nothing.
   * @param connectorName name of the connector.
   */
  public void removeConnectorState(String connectorName) {
    prefsState.remove(connectorName);
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
    return result;
  }
}
