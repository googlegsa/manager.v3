// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.database;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * This is a {@link DataSource} implementation, as Spring does not allow
 * factory methods to return {@code null} instances.  This implementation
 * does nothing, but is sufficient to trigger the
 * {@link com.google.enterprise.connector.persist.JdbcStore} built on
 * top of it to consider itself disabled.
 */
public class FakeDataSource implements DataSource {
  private final String description;

  public FakeDataSource(String description) {
    this.description = description;
  }

  private String message() {
    return description + " JDBC DataSource has not been configured.";
  }

  public String getDescription() {
    return "Disabled stub for " + description + " JDBC DataSource.";
  }

  /* Setter injectors for the benefit of Spring */
  public void setURL(String ignored) {
    // Do nothing.
  }

  public void setUser(String ignored) {
    // Do nothing.
  }

  public void setPassword(String ignored) {
    // Do nothing.
  }

  @Override
  public Connection getConnection() throws SQLException {
    throw new SQLException(message());
  }

  @Override
  public Connection getConnection(String username, String password)
      throws SQLException {
    throw new SQLException(message());
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new SQLException(message());
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new SQLException(message());
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new SQLException(message());
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new SQLException(message());
  }

  /* @Override TODO(jlacey): This @Override requires Java 7. */
  public Logger getParentLogger() {
    return Logger.getLogger("com.google.enterprise.connector");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException(message());
  }
}
