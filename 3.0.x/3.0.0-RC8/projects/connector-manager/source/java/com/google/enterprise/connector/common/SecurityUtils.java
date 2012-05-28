// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Security related utility methods.
 */
public class SecurityUtils {

  /**
   * @param key key name to test.
   * @return true if the given key name describes a sensitive field.
   */
  public static boolean isKeySensitive(String key) {
    return key.toLowerCase().indexOf("password") != -1;
  }

  /**
   * Gets a copy of the map with password property values masked.
   *
   * @param original a property map
   * @return a copy of the map with password property values
   *         replaced by the string "[...]"
   */
  public static Map<String, String> getMaskedMap(Map<String, String> original) {
    if (original == null) {
      return null;
    }
    HashMap<String, String> copy = new HashMap<String, String>();
    for (Map.Entry<String, String> entry : original.entrySet()) {
      String key = entry.getKey();
      copy.put(key, (isKeySensitive(key) ? "[...]" : entry.getValue()));
    }
    return copy;
  }
}
