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

package com.google.enterprise.connector.test;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Wraps a JSONObject as an unmodifiable Map, so it can be easily passed in to
 * things that want maps. This implementation could be enhanced to be
 * modifiable, but we don't need it for our testing.
 */
public class JsonObjectAsMap extends AbstractMap {

  final JSONObject jobj;

  /**
   *
   *
   */
  public JsonObjectAsMap() {
    jobj = new JSONObject();
  }

  /**
   * The javadoc for AbstractMap specifies that you should provide a constructor
   * that takes a map. But one isn't needed here - as far as we know. This
   * implementation is here to let us know otherwise very loudly.
   *
   * @param m a Map
   */
  public JsonObjectAsMap(Map m) {
    throw new IllegalArgumentException();
  }

  /**
   * The primary constructor for this class
   * @param jobj
   *
   */
  public JsonObjectAsMap(JSONObject jobj) {
    this.jobj = jobj;
  }

  public Object get(Object key) {
    try {
      return jobj.get((String) key);
    } catch (JSONException e) {
      // this means the key wasn't found
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.util.AbstractMap#entrySet()
   */
  public Set entrySet() {
    return new AbstractSet() {

      public Iterator iterator() {
        final Iterator i = jobj.keys();
        return new Iterator() {
          public boolean hasNext() {
            return i.hasNext();
          }

          public Object next() {
            try {
              final String key = (String) i.next();
              final Object val = jobj.get(key);
              Entry e = new Entry() {
                public Object getKey() {
                  return key;
                }

                public Object getValue() {
                  return val;
                }

                public Object setValue(Object value) {
                  throw new UnsupportedOperationException();
                }
              };
              return e;
            } catch (JSONException e) {
              throw new IllegalArgumentException();
            }
          }

          // no implementation needed here because we're only implementing
          // unmodifiable maps
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

      public int size() {
        return jobj.length();
      }

    };

  }
}
