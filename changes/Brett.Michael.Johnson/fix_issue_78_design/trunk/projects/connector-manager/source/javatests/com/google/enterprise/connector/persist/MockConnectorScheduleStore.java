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

import java.util.HashMap;

/**
 * Mock Schedule Store - doesn't actually persist, just uses memory.
 */
public class MockConnectorScheduleStore extends HashMap
    implements ConnectorScheduleStore {

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorScheduleStore
   * #getConnectorSchedule(StoreContext)
   */
  public String getConnectorSchedule(StoreContext context) {
    return (String) this.get(context.getConnectorName());
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorScheduleStore
   * #storeConnectorSchedule(StoreContext, java.lang.String)
   */
  public void storeConnectorSchedule(StoreContext context,
      String connectorSchedule) {
    this.put(context.getConnectorName(), connectorSchedule);
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorScheduleStore
   * #removeConnectorSchedule(StoreContext)
   */
  public void removeConnectorSchedule(StoreContext context) {
    this.remove(context.getConnectorName());
  }
}
