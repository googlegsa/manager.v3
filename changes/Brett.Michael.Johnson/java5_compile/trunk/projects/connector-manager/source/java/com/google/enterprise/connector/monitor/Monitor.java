// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.monitor;

import java.util.Map;

/**
 * Monitors variables and responds to requests from the Manager for them.
 * Whether or not this is synchronized is left as an implementation detail.
 */
public interface Monitor {

  /**
   * Update or add variables.  If a variable is already existing, it is
   * overwritten.
   *
   * @param vars the variables and values to set.  The keys must be of type
   *        String and the values must provide a toString() method.  If the
   *        value is null, then the variable is removed.
   */
  public void setVariables(Map<String, ?> vars);

  /**
   * Retrieve all variables and their values.
   *
   * @return a Map object with variable names (String) and values (Object).
   */
  public Map<String, ?> getVariables();
}
