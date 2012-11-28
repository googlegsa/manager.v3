// Copyright 2010 Google Inc.
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

import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.scheduler.Schedule;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Accepts change notifications from a {@link ChangeDetector}, and
 * calls the change handlers in ConnectorCoordinator.
 */
class ChangeListenerImpl implements ChangeListener {
  private static final Logger LOGGER =
      Logger.getLogger(ChangeListenerImpl.class.getName());

  private final TypeMap typeMap;
  private final ConnectorCoordinatorMap coordinatorMap;

  ChangeListenerImpl(TypeMap typeMap, ConnectorCoordinatorMap coordinatorMap) {
    this.typeMap = typeMap;
    this.coordinatorMap = coordinatorMap;
  }

  /* @Override */
  public void connectorAdded(String instanceName, Configuration configuration)
      throws InstantiatorException {
    LOGGER.config("Add connector " + instanceName + " of type "
                  + configuration.getTypeName());
    try {
      ChangeHandler handler = coordinatorMap.getChangeHandler(instanceName);
      TypeInfo type = typeMap.getTypeInfo(configuration.getTypeName());
      handler.connectorAdded(type, configuration);
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING, "Failed to handle addition of new connector "
                 + instanceName, e);
      // Propagate InstantiatorException, so ChangeDetector can retry later.
      throw e;
    } catch (ConnectorTypeNotFoundException e) {
      LOGGER.log(Level.WARNING, "Failed to handle addition of new connector "
                 + instanceName, e);
    }
  }

  /* @Override */
  public void connectorRemoved(String instanceName) {
    LOGGER.config("Remove connector " + instanceName);
    try {
      coordinatorMap.getChangeHandler(instanceName).connectorRemoved();
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING,
          "Failed to handle removal of connector " + instanceName, e);
    }
  }

  /* @Override */
  public void connectorCheckpointChanged(String instanceName,
      String checkpoint) {
    LOGGER.finest("Checkpoint changed for connector " + instanceName);
    try {
      coordinatorMap.getChangeHandler(instanceName)
          .connectorCheckpointChanged(checkpoint);
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING, "Failed to handle checkpoint change for "
                 + "connector " + instanceName, e);
    }
  }

  /* @Override */
  public void connectorScheduleChanged(String instanceName, Schedule schedule) {
    LOGGER.config("Schedule changed for connector " + instanceName + ": "
                  + schedule);
    try {
      coordinatorMap.getChangeHandler(instanceName)
          .connectorScheduleChanged(schedule);
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING, "Failed to handle schedule change for "
                 + "connector " + instanceName, e);
    }
  }

  /* @Override */
  public void connectorConfigurationChanged(String instanceName,
      Configuration configuration) throws InstantiatorException {
    LOGGER.config("Configuration changed for connector " + instanceName);
    try {
      ChangeHandler handler = coordinatorMap.getChangeHandler(instanceName);
      TypeInfo type = typeMap.getTypeInfo(configuration.getTypeName());
      handler.connectorConfigurationChanged(type, configuration);
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING, "Failed to handle configuration change for "
                 + "connector " + instanceName, e);
      // Propagate InstantiatorException, so ChangeDetector can retry later.
      throw e;
    } catch (ConnectorTypeNotFoundException e) {
      LOGGER.log(Level.WARNING, "Failed to handle configuration change for "
                 + "connector " + instanceName, e);
    }
  }
}
