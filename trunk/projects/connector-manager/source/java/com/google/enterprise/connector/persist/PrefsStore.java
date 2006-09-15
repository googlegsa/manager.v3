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

import java.util.prefs.Preferences;

/**
 * Class to get and set the schedule and state for a named connector. 
 *
 */
public class PrefsStore implements ConnectorScheduleStore, ConnectorStateStore {

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

}
