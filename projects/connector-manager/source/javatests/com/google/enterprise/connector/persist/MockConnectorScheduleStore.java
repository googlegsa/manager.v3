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

import java.util.Iterator;

/**
 * Mock Schedule Store
 */
public class MockConnectorScheduleStore implements ConnectorScheduleStore {

  PrefsStore store;
  
  /**
   * Create a Mock Schedule store with all known connectors to be run 24x7
   * @param configStore ConnectorConfigStore with configured connectors
   */
  public MockConnectorScheduleStore(ConnectorConfigStore configStore) {
    store = new PrefsStore();
    Iterator iter = configStore.getConnectorNames();
    while (iter.hasNext()) {
      String connectorName = (String) iter.next();
      final String allDaySchedule = "0-0";
      String connectorSchedule = connectorName + ":" + allDaySchedule;
      storeConnectorSchedule(connectorName, connectorSchedule);
    }
  }
  
  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorScheduleStore#getConnectorSchedule(java.lang.String)
   */
  public String getConnectorSchedule(String connectorName) {
    return store.getConnectorSchedule(connectorName);
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorScheduleStore#storeConnectorSchedule(java.lang.String, java.lang.String)
   */
  public void storeConnectorSchedule(String connectorName,
      String connectorSchedule) {
    store.storeConnectorSchedule(connectorName, connectorSchedule);
  }

}
