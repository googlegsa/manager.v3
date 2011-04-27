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
 * A pool of JDBC database {@link Connection Connections}. In certain
 * JDBC implementations, database Connections may be expensive and
 * time-consuming to open.  This pool maintains a LIFO stack of open
 * Connections in an attempt to re-use existing Connections to the database.
 *
 * @since 2.8
 */
public class DatabaseConnectionPool {
  private final DataSource dataSource;
  private final LinkedList<Connection> connections = new LinkedList<Connection>();

  /**
   * Constructs a pool to hold cached {@link Connection Connections}
   * to the suppied JDBC {@link DataSource}.  The pool is initially empty.
   *
   * @param dataSource a JDBC {@link DataSource}
   */
  public DatabaseConnectionPool(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  protected void finalize() throws Throwable {
    closeConnections();
  }

  /**
   * Returns the JDBC {@link DataSource} that owns these {@code Connections}.
   *
   * @return the connection pool's {@link DataSource}
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Returns a {@link Connection} from the connection pool.
   * If the pool is empty, a new {@code Connection} is
   * obtained from the {@link DataSource}.
   *
   * @return a {@link Connection} to the {@link DataSource}
   * @throws SQLException if a Connection cannot be obtained
   */
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

  /**
   * Releases a {@link Connection}, returning it to the connection pool
   * for later re-use.
   *
   * @param connection a Connection to to return to the pool
   */
  public synchronized void releaseConnection(Connection connection) {
    connections.add(0, connection);
  }

  /**
   * Empties the connection pool, closing all its
   * {@link Connection Connections}.
   */
  public synchronized void closeConnections() {
    for (Connection conn : connections) {
      close(conn);
    }
    connections.clear();
  }

  /**
   * Returns {@code true} if the connection is dead,
   * {@code false} if it appears to be OK.
   */
  private boolean isDead(Connection conn) {
    try {
      conn.getMetaData();
      return false;
    } catch (SQLException e) {
      close(conn);
      return true;
    }
  }

  /** Closes the Connection silently. */
  private void close(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      // Ignored.
    }
  }
}
