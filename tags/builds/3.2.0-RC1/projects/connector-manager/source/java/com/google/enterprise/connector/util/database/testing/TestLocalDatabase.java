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
import com.google.enterprise.connector.spi.LocalDatabase;
import com.google.enterprise.connector.util.database.LocalDatabaseImpl;

import java.io.File;

/**
 * A {@link LocalDatabase} implementation that uses an in-memory H2
 * database for storage and looks for {@link DatabaseResourceBundle}s
 * relative to the current directory or specified resource directory.
 * Connector developers may want to use this to implement unit tests.
 *
 * @since 2.8
 */
public class TestLocalDatabase extends LocalDatabaseImpl {
  /**
   * Constructs a {@code TestLocalDatabase} for the named connector type.
   *
   * @param connectorTypeName name will be used to identify resource bundles.
   * @param resourceDir directory that is the root of resources.
   *         If {@code null}, the current directory is used.
   */
  public TestLocalDatabase(String connectorTypeName, File resourceDir) {
    super(new TestJdbcDatabase(), connectorTypeName,
          new TestResourceClassLoader(resourceDir));
  }
}
