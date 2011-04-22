// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.util.database.testing;

import com.google.enterprise.connector.spi.DatabaseResourceBundle;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A {@code ClassLoader} that looks for resources relative to the
 * specified resources directory, or the current working directory,
 * if none is specified.
 * <p/>
 * <strong>Note:</strong> This only overrides the one method used to locate
 * {@link DatabaseResourceBundle DatabaseResourceBundles}, so it is not a
 * good candidate to stand on its own.
 * <p/>
 * Connector developers may want to use this to implement unit tests.
 *
 * @since 2.8
 */
public class TestResourceClassLoader extends ClassLoader {
  private final File resourceDir;

  public TestResourceClassLoader(File resourceDir) {
    this.resourceDir = (resourceDir == null) ?
        new File(System.getProperty("user.dir")) : resourceDir;
  }

  @Override
  public URL getResource(String name) {
    try {
      File file = new File(resourceDir, name);
      if (file.exists() && file.isFile()) {
        return file.toURI().toURL();
      }
    } catch (MalformedURLException e) {
      // Fall through and look on classpath.
    }
    return this.getClass().getClassLoader().getResource(name);
  }
}

