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
  public static final String INSTANCE_ADDED = "instanceAdded: ";
  public static final String INSTANCE_REMOVED = "instanceRemoved: ";
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
  public void instanceAdded(String instanceName, Configuration configuration) {
    changes.add(INSTANCE_ADDED + instanceName);
  }

  /* @Override */
  public void instanceRemoved(String instanceName) {
    changes.add(INSTANCE_REMOVED + instanceName);
  }

  /* @Override */
  public void checkpointChanged(String instanceName, String checkpoint) {
    changes.add(CHECKPOINT_CHANGED + instanceName);
  }

  /* @Override */
  public void configurationChanged(String instanceName,
      Configuration configuration) {
    changes.add(CONFIGURATION_CHANGED + instanceName);
  }

  /* @Override */
  public void scheduleChanged(String instanceName, Schedule schedule) {
    changes.add(SCHEDULE_CHANGED + instanceName);
  }
}
