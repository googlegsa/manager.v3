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
 * Handles change notifications from a {@link ChangeListener}
 * for a specific connector instance.
 */
interface ChangeHandler {
  void connectorAdded(TypeInfo typeInfo, Configuration configuration)
      throws InstantiatorException;

  void connectorRemoved() throws InstantiatorException;

  void connectorCheckpointChanged(String checkpoint)
      throws InstantiatorException;

  void connectorScheduleChanged(Schedule schedule)
      throws InstantiatorException;

  void connectorConfigurationChanged(TypeInfo typeInfo,
      Configuration configuration) throws InstantiatorException;
}
