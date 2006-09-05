// Copyright 2006 Google Inc.  All Rights Reserved.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implements the Monitor interface using a Properties object.
 */
public class HashMapMonitor implements Monitor {

  private Map vars;
  
  public HashMapMonitor() {
    vars = new HashMap();
  }
  
  /* (non-Javadoc)
   * @see com.google.enterprise.connector.monitor.Monitor#getVariables()
   */
  public Map getVariables() {
    return vars;
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.monitor.Monitor#setVariables(java.util.Properties)
   */
  public void setVariables(Map props) {
    Set entrySet = props.entrySet();
    Iterator iter = entrySet.iterator();
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      String key = (String) entry.getKey();
      Object value = entry.getValue();
      if (null == value) {
        vars.remove(key);
      } else {
        vars.put(key, value);
      }
    }
  }

}
