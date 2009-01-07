// Copyright 2006- 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.importexport;

import java.util.Map;

/**
 * An encapsulation of all the information we export/import per connector
 * instance.
 */
public class ImportExportConnector {
  private String name;
  private String type;
  private String scheduleString;
  private Map config;

  /**
   * @param name the connector's name
   * @param type the connector's ConnectorType's name
   * @param scheduleString the connector's schedule as a String
   * @param config the connector's ConnectorType-specific configuration data
   */
  public ImportExportConnector(
      String name, String type, String scheduleString, Map config) {
    this.name = name;
    this.type = type;
    this.scheduleString = scheduleString;
    this.config = config;
  }

  /**
   * @return the connector's name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the connector's ConnectorType's name
   */
  public String getType() {
    return type;
  }

  /**
   * @return the connector's schedule as a String
   * @see com.google.enterprise.connector.scheduler.Schedule
   */
  public String getScheduleString() {
    return scheduleString;
  }

  /**
   * @return the connector's ConnectorType-specific configuration data
   */
  public Map getConfig() {
    return config;
  }
}
