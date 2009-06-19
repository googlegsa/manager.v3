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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the Monitor interface using a Properties object.  This class is
 * thread-safe.
 */
public class HashMapMonitor implements Monitor {

  private final Map<String, Object> vars;

  public HashMapMonitor() {
    vars = Collections.synchronizedMap(new HashMap<String, Object>());
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.monitor.Monitor#getVariables()
   * @return a read-only Map of the variables
   */
  public Map<String, ?> getVariables() {
    synchronized (vars) {
      return Collections.unmodifiableMap(vars);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.enterprise.connector.monitor.Monitor#setVariables(java.util.Map)
   */
  public void setVariables(Map<String, ?> props) {
    synchronized (vars) {
      for (Map.Entry<String, ?> entry : props.entrySet()) {
        if (null == entry.getValue()) {
          vars.remove(entry.getKey());
        } else {
          vars.put(entry.getKey(), entry.getValue());
        }
      }
    }
  }
}
