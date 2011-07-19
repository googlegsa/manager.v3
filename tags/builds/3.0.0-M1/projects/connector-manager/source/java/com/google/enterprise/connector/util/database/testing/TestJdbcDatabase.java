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

import com.google.enterprise.connector.util.database.JdbcDatabase;

import org.h2.jdbcx.JdbcDataSource;

/**
 * A {@link JdbcDatabase} implementation that uses an in-memory H2 database for
 * storage. Connector developers may want to use this to implement unit tests.
 *
 * @since 2.8
 */
public class TestJdbcDatabase extends JdbcDatabase {
  /**
   * Constructs a {@link JdbcDatabase} implementation that uses an in-memory
   * H2 database for storage.
   */
  public TestJdbcDatabase() {
    super(buildDataSource());
  }

  private static JdbcDataSource buildDataSource() {
    // Setup in-memory H2 JDBC DataSource;
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:testdb");
    dataSource.setUser("sa");
    dataSource.setPassword("sa");
    return dataSource;
  }
}
