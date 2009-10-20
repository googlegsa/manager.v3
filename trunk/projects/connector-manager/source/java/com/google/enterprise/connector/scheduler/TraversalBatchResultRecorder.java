// Copyright 2009 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.instantiator.BatchResultRecorder;
import com.google.enterprise.connector.instantiator.ConnectorCoordinator;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.logging.Logger;

/**
 * {@link BatchResultRecorder} for recording batch results with a {@link
 * HostLoadManager}.
 */
public class TraversalBatchResultRecorder implements BatchResultRecorder {
  private static final Logger LOGGER =
      Logger.getLogger(TraversalBatchResultRecorder.class.getName());

  private final HostLoadManager hostLoadManager;
  private final ConnectorCoordinator connectorCoordinator;

  TraversalBatchResultRecorder(HostLoadManager hostLoadManager,
      ConnectorCoordinator connectorCoordinator) {
    this.hostLoadManager = hostLoadManager;
    this.connectorCoordinator = connectorCoordinator;
  }

  /**
   * Record the result of running a batch with the {@link HostLoadManager}
   */
  public void recordResult(BatchResult result) {
    String connectorName = connectorCoordinator.getConnectorName();
    int count = result.getCountProcessed();
    if (count > 0) {
      hostLoadManager.updateNumDocsTraversed(connectorName, count);
    }
    switch (result.getDelayPolicy()) {
      case POLL:
        try {
          Schedule schedule =
              new Schedule(connectorCoordinator.getConnectorSchedule());
          int retryDelayMillis = schedule.getRetryDelayMillis();
          hostLoadManager.connectorFinishedTraversal(connectorName,
              retryDelayMillis);
          if (retryDelayMillis == Schedule.POLLING_DISABLED) {
            // We reached then end of the repository, but aren't allowed
            // to poll looking for new content to arrive.  Disable the
            // traversal schedule.
            schedule.setDisabled(true);
            // TODO(strellis): pass in a batch key here
            connectorCoordinator.setConnectorSchedule(schedule.toString());
            LOGGER.info("Traversal complete. Automatically pausing "
                        + "traversal for connector " + connectorName);
          }
        } catch (ConnectorNotFoundException cnfe) {
          // Connector was deleted while processing the batch.
        }
        break;

      case ERROR:
        hostLoadManager.connectorFinishedTraversal(connectorName,
            Traverser.ERROR_WAIT_MILLIS);
        break;

      case IMMEDIATE:
        // This means the batch did not complete so we leave the
        // hostLoadManager in peace.
        break;

      default:
        throw new IllegalArgumentException("result = " + result);
    }
  }
}
