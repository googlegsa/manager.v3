// Copyright (C) 2008-2009 Google Inc.
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
import java.io.InputStream;
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
  public static String getJarVersion(Class<?> clazz) {
    String classPath = "/" + clazz.getName().replace('.', '/') + ".class";
    URL classUrl = clazz.getResource(classPath);
    if (classUrl == null) {
      LOGGER.warning("Error accessing Jar Manifest for " + clazz);
      return "";
    }

    // The classUrl may not be a jar: URL, depending on the servlet
    // container, so we can't just open it and assume we'll get a
    // JarURLConnection to get the manifest from. So we construct an
    // URL directly to the manifest we want.
    String classUrlRep = classUrl.toString();
    String path = classUrlRep.replace(classPath, "/META-INF/MANIFEST.MF");
    if (path.equals(classUrlRep)) {
      // The replace failed; we don't have an URL to read the manifest from.
      LOGGER.warning("Error accessing Jar Manifest for " + classUrlRep);
      return "";
    }
    try {
      URL url = new URL(path);
      InputStream in = url.openStream();
      Manifest manifest = new Manifest(in);
      Attributes attrs = manifest.getMainAttributes();
      String version = attrs.getValue("Implementation-Version");
      return (version == null) ? "" : version;
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error accessing Jar Manifest for " + path, e);
    }
    return "";  // Can't get version string.
  }
}
