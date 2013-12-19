// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ConnectorTestUtils {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorTestUtils.class.getName());

  private ConnectorTestUtils() {
    // prevents instantiation
  }

  /**
   * Removes the connector manager version string from the buffer.
   * This allows the tests that compare actual output to expected
   * output to function across versions, jvms, and platforms.
   */
  public static void removeManagerVersion(StringBuffer buffer) {
    int start = buffer.indexOf("  <" + ServletUtil.XMLTAG_INFO + ">"
                               + ServletUtil.MANAGER_NAME);
    if (start >= 0) {
      buffer.delete(start, buffer.indexOf("\n", start) + 1);
    }
  }

  /**
   * Removes colspan="1" rowspan="1" attributes from processed
   * XML form snippets.  These seem to be added by some Java
   * DOM engines, but not others.
   */
  public static String removeColRowSpan(String str) {
    if (str == null) {
      return null;
    } else {
      return str.replaceAll(" colspan=\"1\"", "")
                .replaceAll(" rowspan=\"1\"", "");
    }
  }

  /**
   * Compare two maps.  The maps need not be identical, but map1
   * should be a subset of map2.  Note that this is slightly different
   * behavior than earlier versions of compareMaps.
   *
   * @param map1 a Map that should be a subset of map2
   * @param map2 a Map that should be a superset of map1
   */
  public static <T, U> void compareMaps(Map<T, U> map1,
      Map<T, U> map2) {
    Set<T> set1 = map1.keySet();
    Set<T> set2 = map2.keySet();
    Assert.assertTrue("there is a key in map1 that's not in map2",
        set2.containsAll(set1));

    for (T key : set1) {
      Assert.assertEquals(map1.get(key), map2.get(key));
    }
  }

  /**
   * Compares two Configurations for equality.
   *
   * @param expected the expected configuration.
   * @param config the configuration that should match expected.
   */
  public static void compareConfigurations(Configuration expected,
                                           Configuration config) {
    Assert.assertNotNull(config);
    Assert.assertEquals(expected.getTypeName(), config.getTypeName());
    Assert.assertEquals(expected.getXml(), config.getXml());
    compareMaps(expected.getMap(), config.getMap());
  }

  public static boolean mkdirs(File file) {
    if (file.exists() && file.isDirectory()) {
      return true;
    }
    boolean res = file.mkdirs();
    if (!res) {
      LOGGER.warning("Failed to create directory " + file.getPath());
    }
    return res;
  }

  public static boolean deleteAllFiles(File file) {
    if (!file.exists()) {
      return true;
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        deleteAllFiles(f);
      }
    }
    boolean res = file.delete();
    if (!res) {
      LOGGER.warning("Failed to delete " + file.getPath());
    }
    return res;
  }

  public static void copyFile(String source, String dest) throws IOException {
    InputStream in = new FileInputStream(new File(source));
    OutputStream out = new FileOutputStream(new File(dest));
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }

  public static void deleteFile(String file) {
    File f = new File(file);
    if (f.exists() && !f.delete()) {
      throw new IllegalStateException("Deletion failed " + file);
    }
  }

  /**
   * Creates a {@link SimpleDocument} with the provided id and a
   * minimal set of additional properties.
   */
  public static SimpleDocument createSimpleDocument(String docId) {
    Map<String, Object> props = createSimpleDocumentBasicProperties(docId);
    return createSimpleDocument(props);
  }

  /**
   * Creates a {@link Map} with basic properties filled for
   * constructing a {@link SimpleDocument}
   */
  public static Map<String, Object> createSimpleDocumentBasicProperties(
      String docId) {
    Map<String, Object> props = new HashMap<String, Object>();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(3600 * 1000);
    props.put(SpiConstants.PROPNAME_LASTMODIFIED, cal);
    props.put(SpiConstants.PROPNAME_DOCID, docId);
    props.put(SpiConstants.PROPNAME_MIMETYPE, "text/plain");
    props.put(SpiConstants.PROPNAME_CONTENT, "now is the time");
    props.put(SpiConstants.PROPNAME_DISPLAYURL,
        "http://www.comtesturl.com/test?" + docId);
    return props;
  }

  /**
   * Creates a {@link SimpleDocument} with the properties in the provided
   * {@link Map}.
   */
  public static SimpleDocument createSimpleDocument(Map<String,
      Object> props) {
    return new SimpleDocument(createSpiProperties(props));
  }

  @SuppressWarnings("unchecked")
  public static void addValueToList(Object obj, List<Value> list) {
    if (obj instanceof List) {
      for (Object listItem : (List<Object>) obj) {
        addValueToList(listItem, list);
      }
    } else if (obj instanceof Value) {
      list.add((Value) obj);
    } else if (obj instanceof String) {
      list.add(Value.getStringValue((String) obj));
    } else if (obj instanceof Calendar) {
      list.add(Value.getDateValue((Calendar) obj));
    } else if (obj instanceof InputStream) {
      list.add(Value.getBinaryValue((InputStream) obj));
    } else if (obj instanceof Boolean) {
      list.add(Value.getBooleanValue((Boolean) obj));
    } else if (obj instanceof Long) {
      list.add(Value.getLongValue((Long) obj));
    } else if (obj instanceof Double) {
      list.add(Value.getDoubleValue((Double) obj));
    } else if (obj instanceof Principal) {
      list.add(Value.getPrincipalValue((Principal) obj));
    } else {
      throw new AssertionError(obj);
    }
  }

  /**
   * Creates a properties map matching the SPI type, mapping String
   * property names to {@code List<Value>}.
   */
  public static Map<String, List<Value>> createSpiProperties(
      Map<String, Object> props) {
    Map<String, List<Value>> spiValues = new HashMap<String, List<Value>>();
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      Object obj = entry.getValue();
      List<Value> values = new ArrayList<Value>();
      addValueToList(obj, values);
      spiValues.put(entry.getKey(), values);
    }
    return spiValues;
  }
}
