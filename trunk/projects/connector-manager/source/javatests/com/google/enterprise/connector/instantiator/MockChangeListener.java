// Copyright (C) 2010 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.scheduler.Schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Accepts change notifications from a {@link ChangeDetector}.
 */
class MockChangeListener implements ChangeListener{
  public static final String CONNECTOR_ADDED = "connectorAdded: ";
  public static final String CONNECTOR_REMOVED = "connectorRemoved: ";
  public static final String CHECKPOINT_CHANGED = "checkpointChanged: ";
  public static final String CONFIGURATION_CHANGED = "configurationChanged: ";
  public static final String SCHEDULE_CHANGED = "scheduleChanged: ";

  private final List<String> changes = new ArrayList<String>();

  /**
   * Gets the changes that this listener has received.
   *
   * @return the ordered list of changes
   */
  public List<String> getChanges() {
    return changes;
  }

  /* @Override */
  public void connectorAdded(String connectorName,
      Configuration configuration) {
    changes.add(CONNECTOR_ADDED + connectorName);
  }

  /* @Override */
  public void connectorRemoved(String connectorName) {
    changes.add(CONNECTOR_REMOVED + connectorName);
  }

  /* @Override */
  public void connectorCheckpointChanged(String connectorName,
      String checkpoint) {
    changes.add(CHECKPOINT_CHANGED + connectorName);
  }

  /* @Override */
  public void connectorConfigurationChanged(String connectorName,
      Configuration configuration) {
    changes.add(CONFIGURATION_CHANGED + connectorName);
  }

  /* @Override */
  public void connectorScheduleChanged(String connectorName,
      Schedule schedule) {
    changes.add(SCHEDULE_CHANGED + connectorName);
  }
}
