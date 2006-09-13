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

package com.google.enterprise.connector.instantiator;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Utilities for dealing with Spring.
 */
public class SpringUtils {

  private static final int INDENT_SIZE = 3;

  private static void appendSpaces(int n, StringBuffer buf) {
    final String SPACES = "                                            ";
    final int SPACES_LENGTH = SPACES.length();
    if (n < 0) {
      throw new IllegalArgumentException();
    }
    if (n > SPACES_LENGTH) {
      buf.append(SPACES);
    }
    buf.append(SPACES.substring(0, n));
  }

  /**
   * Produces the Spring xml representation of the input map, indented at level
   * zero.
   * 
   * @param mapStringString A Map of String to String
   * @return A spring xml fragment as a String
   */
  public static String mapToSpring(Map mapStringString) {
    return mapToSpring(mapStringString, 0);
  }

  /**
   * Produces the Spring xml representation of the input map, indented at a
   * specified level.
   * 
   * @param mapStringString A Map of String to String
   * @param indentLevel A number of levels to indent each line
   * @return A spring xml fragment as a String
   */
  public static String mapToSpring(Map mapStringString, int indentLevel) {
    SortedMap sortedMap = new TreeMap(mapStringString);
    StringBuffer buf = new StringBuffer(2048);
    appendSpaces(indentLevel * INDENT_SIZE, buf);
    buf.append("<map>\r\n");
    for (Iterator i = sortedMap.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Entry) i.next();
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();

      appendSpaces((indentLevel + 1) * INDENT_SIZE, buf);
      buf.append("<entry>\r\n");

      appendSpaces((indentLevel + 2) * INDENT_SIZE, buf);
      buf.append("<key><value>");
      buf.append(key);
      buf.append("</value></key>\r\n");

      appendSpaces((indentLevel + 2) * INDENT_SIZE, buf);
      buf.append("<value>");
      buf.append(value);
      buf.append("</value>\r\n");

      appendSpaces((indentLevel + 1) * INDENT_SIZE, buf);
      buf.append("</entry>\r\n");
    }

    appendSpaces(indentLevel * INDENT_SIZE, buf);
    buf.append("</map>\r\n");
    return new String(buf);
  }

  /**
   * Reach inside the (assumed) top-level "beans" element and strip off
   * everything so that we just get the content of that element.
   * 
   * @param connectorBeanProto
   * @return the inside of the beans element
   */
  public static String stripBeansElement(String connectorBeanProto) {
    int start = connectorBeanProto.indexOf("<beans");
    if (start < 0) {
      throw new IllegalArgumentException();
    }
    start = connectorBeanProto.indexOf('>', start);
    if (start < 0) {
      throw new IllegalArgumentException();
    }
    start++; // skip the closing >
    int finish = connectorBeanProto.lastIndexOf("</beans");
    if (finish <= start) {
      throw new IllegalArgumentException();
    }
    return connectorBeanProto.substring(start, finish);
  }

  public static String setBeanID(String connectorBeanProto, String newID) {
    String foo = "<bean id=\"";
    int start = connectorBeanProto.indexOf("<bean");
    if (start < 0) {
      throw new IllegalArgumentException();
    }
    start = connectorBeanProto.indexOf("id=\"", start);
    if (start < 0) {
      throw new IllegalArgumentException();
    }
    start += 4; // length of "id=\""
    int finish = connectorBeanProto.indexOf('"', start);
    if (finish <= start) {
      throw new IllegalArgumentException();
    }
    return connectorBeanProto.substring(0, start) + newID
        + connectorBeanProto.substring(finish);
  }
}
