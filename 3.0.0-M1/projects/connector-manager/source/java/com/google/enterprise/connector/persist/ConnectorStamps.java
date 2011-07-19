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

package com.google.enterprise.connector.persist;

/**
 * Represents the version stamps for the persistent objects of a
 * connector instance.
 */
public class ConnectorStamps {
  private final Stamp checkpointStamp;
  private final Stamp configurationStamp;
  private final Stamp scheduleStamp;

  public ConnectorStamps(Stamp checkpointStamp, Stamp configurationStamp,
      Stamp scheduleStamp) {
    this.checkpointStamp = checkpointStamp;
    this.configurationStamp = configurationStamp;
    this.scheduleStamp = scheduleStamp;
  }

  /**
   * Gets the checkpoint stamp.
   *
   * @return the checkpoint stamp, or {@code null} if there is no checkpoint
   */
  public Stamp getCheckpointStamp() {
    return checkpointStamp;
  }

  /**
   * Gets the configuration stamp.
   *
   * @return the configuration stamp, which must not be {@code null}
   */
  // TODO: Could this stamp reasonably be null?
  public Stamp getConfigurationStamp() {
    return configurationStamp;
  }

  /**
   * Gets the schedule stamp.
   *
   * @return the schedule stamp, or {@code null} if there is no schedule
   */
  public Stamp getScheduleStamp() {
    return scheduleStamp;
  }

  @Override
  public String toString() {
    return "{" + checkpointStamp + ", " + configurationStamp + ", "
        + scheduleStamp + "}";
  }
}
