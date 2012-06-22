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

package com.google.enterprise.connector.manager;

/**
 * Short info on a named connector.
 */
public class ConnectorStatus {

  private final String name;
  private final String type;
  private final int status;
  private final String schedule;
  private final String globalNamespace;
  private final String localNamespace;

  /**
   * Primary constructor.
   * @param name the connector's name
   * @param type the connector's type
   * @param status the connector's status
   * @param schedule the connector's schedule
   */
  public ConnectorStatus(String name, String type, int status, String schedule,
                         String globalNamespace, String localNamespace) {
    super();
    this.name = name;
    this.type = type;
    this.status = status;
    this.schedule = (schedule == null) ? null : schedule.trim();
    this.globalNamespace = globalNamespace;
    this.localNamespace = localNamespace;
  }

  /**
   * Gets the name
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the type
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Gets the status
   * @return the status
   */
  public int getStatus() {
    return status;
  }

  /**
   * Gets the schedule
   * @return the schedule
   */
  public String getSchedule() {
    return schedule;
  }

  /**
   * Gets the global namespace
   * @return the global namespace
   */
  public String getGlobalNamespace() {
    return globalNamespace;
  }

  /**
   * Gets the local namespace
   * @return the local namespace
   */
  public String getLocalNamespace() {
    return localNamespace;
  }

  @Override
  public String toString() {
    return "{ connector = " + name + ", type = " + type + ", status = " + status
            + ", schedule = " + schedule + ", globalNamespace = "
            + globalNamespace + ", localNamespace = " + localNamespace + " }";
  }
}
