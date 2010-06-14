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

/**
 * Accepts change notifications from a {@link ChangeDetector}.
 */
interface ChangeListener {
  void instanceAdded(String instanceName, Configuration configuration);
  void instanceRemoved(String instanceName);

  void checkpointChanged(String instanceName, String checkpoint);
  void configurationChanged(String instanceName, Configuration configuration);
  void scheduleChanged(String instanceName, Schedule schedule);
}
