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

package com.google.enterprise.connector.traversal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MimeTypeMap {

  private Map typeMap;
  private int unknownMimeTypeSupportLevel;
  
  private static final Integer ZERO = new Integer(0);
  private static final Integer ONE = new Integer(1);
  
  public MimeTypeMap() {
    // if no setters are called, then all mime types are supported
    typeMap = new HashMap();
    unknownMimeTypeSupportLevel = 1;
  }
  
  public void setUnknownMimeTypeSupportLevel(int unknownMimeTypeSupportLevel) {
    this.unknownMimeTypeSupportLevel = unknownMimeTypeSupportLevel;
  }

  public void setSupportedMimeTypes(Set mimeTypes) {
    initMimeTypes(mimeTypes, ONE);
  }

  public void setUnsupportedMimeTypes(Set mimeTypes) {
    initMimeTypes(mimeTypes, ZERO);
  }
  
  private void initMimeTypes(Set mimeTypes, Integer supportLevel) {
    Iterator i = mimeTypes.iterator();
    while (i.hasNext()) {
      String mimeType = (String) i.next();
      typeMap.put(mimeType, supportLevel);
    }
  }
  
  public int mimeTypeSupportLevel(String mimeType) {
    Integer result = (Integer) typeMap.get(mimeType);
    if (result == null) {
      return unknownMimeTypeSupportLevel;
    }
    return result.intValue();
  }
}
