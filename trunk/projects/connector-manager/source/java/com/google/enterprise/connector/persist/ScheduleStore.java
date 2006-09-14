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
 * Class to get and set the schedule for a named connector. 
 *
 */
public class ScheduleStore implements ConnectorScheduleStore {

  private static Preferences prefs = 
    Preferences.userNodeForPackage(ScheduleStore.class);
  
  public ScheduleStore() {
    
  }
  /**
   * @param connectorName connector name
   * @return connectorSchdule schedule of the corresponding connector.
   * @throws PersistentStoreException
   */
  public String getConnectorSchedule(String connectorName) 
      throws PersistentStoreException {
    String connectorSchedule;
    try {
      connectorSchedule = prefs.get(connectorName, null);
    } catch (NullPointerException e) {
      throw new PersistentStoreException(e);
    }
    return connectorSchedule;
  }

  /**
   * @param connectorName connector name
   * @param connectorSchedule schedule of the corresponding connector.
   * @throws PersistentStoreException 
   */
  public void storeConnectorSchedule(String connectorName,
      String connectorSchedule) throws PersistentStoreException {    
    try {
      prefs.put(connectorName, connectorSchedule);
    } catch (NullPointerException e) {
      throw new PersistentStoreException(e);
    } catch (IllegalArgumentException e) {
      throw new PersistentStoreException(e);
    }
    
  }

}
