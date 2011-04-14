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

package com.google.enterprise.connector.util.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.sql.DataSource;

/**
 * A pool of JDBC database Connections.
 */
public class DatabaseConnectionPool {
  private final DataSource dataSource;
  private final LinkedList<Connection> connections = new LinkedList<Connection>();

  public DatabaseConnectionPool(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  protected void finalize() throws Throwable {
    closeConnections();
  }


  // Return a Connection from the ConnectionPool.  If the pool is empty,
  // then get a new Connection from the DataSource.
  public synchronized Connection getConnection() throws SQLException {
    Connection conn;
    try {
      // Get a cached connection, but check if it is still functional.
      do {
        conn = connections.remove(0);
      } while (isDead(conn));
    } catch (IndexOutOfBoundsException e) {
      // Pool is empty.  Get a new connection from the dataSource.
      conn = dataSource.getConnection();
    }
    return conn;
  }

  // Release a Connection back to the pool.
  public synchronized void releaseConnection(Connection conn) {
    connections.add(0, conn);
  }

  // Empty the Pool, closing all Connections.
  public synchronized void closeConnections() {
    for (Connection conn : connections) {
      close(conn);
    }
    connections.clear();
  }

  // Returns true if connection is dead, false if it appears to be OK.
  private boolean isDead(Connection conn) {
    try {
      conn.getMetaData();
      return false;
    } catch (SQLException e) {
      close(conn);
      return true;
    }
  }

  // Close the Connection silently.
  private void close(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      // Ignored.
    }
  }
}
