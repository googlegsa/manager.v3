// Copyright (C) 2008 Google Inc.
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

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JarUtils {

  private static final Logger LOGGER =
      Logger.getLogger(JarUtils.class.getName());

  private JarUtils() {
    // prevents instantiation
  }

  /**
   * Retrieve the Implementation-Version string from the Jar file
   * that contains the specified Class.
   *
   * @param clazz a Class unique to the jar file we are looking for.
   * @return the version string from the Jar file or the empty string
   * ("") if none found.
   */
  public static String getJarVersion(Class clazz) {
    URL url = null;
    try {
      String resName = "/" + clazz.getName().replace('.', '/') + ".class";
      url = clazz.getResource(resName);
      try {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        Manifest manifest = connection.getManifest();
        Attributes attrs = manifest.getMainAttributes();
        String version = attrs.getValue("Implementation-Version");
        return (version == null) ? "" : version;
      } catch (ClassCastException cce) {
        // It looks like we are running JUnit tests, pulling classes out
        // of classes directory instead of a Jar file.
        LOGGER.warning("Unable to access Jar Manifest for " + url);
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error accessing Jar Manifest for " + url, e);
    }
    return "";  // Can't get version string.
  }
}
