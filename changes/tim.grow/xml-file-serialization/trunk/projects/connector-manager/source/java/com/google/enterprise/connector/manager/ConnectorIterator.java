// Copyright (C) 2007 Google Inc.
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

import java.util.Map;

/**
 * Iterates over connectors and provides access to their metadata.
 */
public interface ConnectorIterator {

  /**
   * Proceed to the next connector.  A <code>ConnectorIterator</code> is
   * initially positioned before the first connector; the first call to the
   * method <code>next</code> makes the first connector the current connector.
   * @return <code>true</code> iff there are more connectors;
   * <code>false</code> if not
   */
  boolean next();

  /**
   * @return the connector's name
   */
  String getName();

  /**
   * @return the connector's ConnectorType's name
   */
  String getType();

  /**
   * @return the connector's ConnectorType-specific configuration data
   */
  Map getConfig();

  /**
   * @return the connector's schedule's load.
   * @see com.google.enterprise.connector.scheduler.Schedule
   */
  int getLoad();

  /**
   * @return the connector's schedule's time intervals (as a string)
   * @see com.google.enterprise.connector.scheduler.Schedule
   */
  String getTimeIntervals();
}
