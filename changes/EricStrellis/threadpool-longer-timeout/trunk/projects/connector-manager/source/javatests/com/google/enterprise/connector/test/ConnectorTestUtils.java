// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.servlet.ServletUtil;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public class ConnectorTestUtils {

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
   * Compare two maps.  The maps need not be identical, but map1
   * should be a subset of map2.  Note that this is slightly different
   * behavior than earlier versions of compareMaps.
   *
   * @param map1 a Map that should be a subset of map2
   * @param map2 a Map that should be a superset of map1
   */
  public static void compareMaps(Map<Object, Object> map1,
      Map<Object, Object> map2) {
    Set<Object> set1 = map1.keySet();
    Set<Object> set2 = map2.keySet();
    Assert.assertTrue("there is a key in map1 that's not in map2",
        set2.containsAll(set1));

    for (Object key : set1) {
      Assert.assertEquals(map1.get(key), map2.get(key));
    }
  }

  public static boolean deleteAllFiles(File dir) {
    if(!dir.exists()) {
        return true;
    }
    boolean res = true;
    if(dir.isDirectory()) {
        File[] files = dir.listFiles();
        for(int i = 0; i < files.length; i++) {
            res &= deleteAllFiles(files[i]);
        }
        res = dir.delete(); // Delete dir itself.
    } else {
        res = dir.delete();
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
}
