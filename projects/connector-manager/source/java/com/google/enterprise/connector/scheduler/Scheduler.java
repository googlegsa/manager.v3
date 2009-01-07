// Copyright (C) 2006 Google Inc.
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

/**
 * Interface to the Scheduler.
 *
 * Runs the schedule.  The run() method consists of determining the schedule as
 * well as executing the schedule.
 */
public interface Scheduler extends Runnable {
  /**
   * Call this method when a connector is removed.  Assumes ScheduleStore has
   * already been updated to reflect the schedule change.  This causes the
   * scheduler to gracefully interrupt any work that is done on this connector.
   * @param connectorName name of the connector instance
   */
  public void removeConnector(String connectorName);
}
