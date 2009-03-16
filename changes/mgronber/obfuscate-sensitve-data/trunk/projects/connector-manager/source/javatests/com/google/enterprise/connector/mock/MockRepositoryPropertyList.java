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

package com.google.enterprise.connector.mock;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

/**
 * Mock property list object.
 * <p>
 * This models the metadata-list for a document.
 */
public class MockRepositoryPropertyList {
  private Map proplist;

  public String toString() {
    return proplist.toString();
  }

  private void init(List l) {
    proplist = new HashMap();
    if (l == null) {
      return;
    }
    for (Iterator iter = l.iterator(); iter.hasNext(); ) {
      MockRepositoryProperty p = (MockRepositoryProperty) iter.next();
      proplist.put(p.getName(), p);
    }
  }

  public MockRepositoryPropertyList() {
    init(null);
  }

  public MockRepositoryPropertyList(MockRepositoryProperty[] l) {
    init(Arrays.asList(l));
  }

  public MockRepositoryPropertyList(List l) {
    init(l);
  }

  public MockRepositoryPropertyList(JSONObject jo) {
    List l = new LinkedList();
    for (Iterator keys = jo.keys(); keys.hasNext(); ) {
      String name = (String) keys.next();
      Object value;
      try {
        value = jo.get(name);
      } catch (JSONException e) {
        throw new IllegalArgumentException("Bad JSON object");
      }
      MockRepositoryProperty p = new MockRepositoryProperty(name, value);
      l.add(p);
    }
    init(l);
  }

  public void setProperty(MockRepositoryProperty p) {
    proplist.put(p.getName(), p);
  }

  /**
   * Takes all the properties in the parameter list and set them
   * in this list, overriding any with the same key
   * @param pl list of new properties
   */
  public void merge(MockRepositoryPropertyList pl) {
    proplist.putAll(pl.proplist);
  }

  public String lookupStringValue(String name) {
    MockRepositoryProperty p = (MockRepositoryProperty) proplist.get(name);
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  public Iterator iterator() {
    return proplist.values().iterator();
  }

  public MockRepositoryProperty getProperty(String name) {
    return (MockRepositoryProperty) proplist.get(name);
  }
}
