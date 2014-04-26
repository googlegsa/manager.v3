// Copyright 2014 Google Inc.
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

package com.google.enterprise.connector.util.database;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.h2.jdbcx.JdbcDataSource;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DatabaseConnectionPoolTest extends TestCase {  

  public void testConstructor() {
    DataSource ds = createMock(DataSource.class);
    new DatabaseConnectionPool(ds);
  }

  public void testGetConnection() throws SQLException {
    DataSource ds = createMock(DataSource.class);
    Connection c1 = createMock(Connection.class);
    Connection c2 = createMock(Connection.class);
    Connection c3 = createMock(Connection.class);
    expect(ds.getConnection()).andReturn(c1).andReturn(c2).andReturn(c3);
    replay(ds, c1, c2, c3);

    DatabaseConnectionPool pool = new DatabaseConnectionPool(ds);
    assertEquals(c1, pool.getConnection());
    assertEquals(c2, pool.getConnection());
    assertEquals(c3, pool.getConnection());

    verify(ds, c1, c2, c3);
  }
  
  public void testReleaseConnection() throws SQLException {
    DataSource ds = createMock(DataSource.class);
    Connection c1 = createMock(Connection.class);
    Connection c2 = createMock(Connection.class);
    Connection c3 = createMock(Connection.class);
    Connection c4 = createMock(Connection.class);
    expect(ds.getConnection()).andReturn(c1).andReturn(c2).andReturn(c3)
        .andReturn(c4);
    expect(c2.isValid(1)).andReturn(true);
    expect(c3.isValid(1)).andReturn(true);
    replay(ds, c1, c2, c3, c4);

    DatabaseConnectionPool pool = new DatabaseConnectionPool(ds);
    assertEquals(c1, pool.getConnection());
    assertEquals(c2, pool.getConnection());
    assertEquals(c3, pool.getConnection());

    pool.releaseConnection(c3);
    pool.releaseConnection(c2);

    // Test LIFO    
    assertEquals(c2, pool.getConnection());
    assertEquals(c3, pool.getConnection());
    // This should fetch new connection
    assertEquals(c4, pool.getConnection());
    verify(ds, c1, c2, c3, c4);
  }

  public void testCloseConnections() throws SQLException {
    DataSource ds = createMock(DataSource.class);
    Connection c1 = createMock(Connection.class);
    Connection c2 = createMock(Connection.class);
    Connection c3 = createMock(Connection.class);  
    expect(ds.getConnection()).andReturn(c1).andReturn(c2).andReturn(c3);
    c1.close();
    c2.close();
    replay(ds, c1, c2, c3);

    DatabaseConnectionPool pool = new DatabaseConnectionPool(ds);
    assertEquals(c1, pool.getConnection());
    assertEquals(c2, pool.getConnection());
    assertEquals(c3, pool.getConnection());
    // Only c1 and c2 are released back to pool. close() should be executed
    // only on c1 and c2
    pool.releaseConnection(c1);
    pool.releaseConnection(c2);

    pool.closeConnections();  
    verify(ds, c1, c2, c3);
  }

  public void testValidConnection() throws SQLException {    
    DataSource ds = createMock(DataSource.class);
    Connection alive = createMock(Connection.class);
    Connection deadAfterSomeTime = createMock(Connection.class);    
    expect(ds.getConnection()).andReturn(alive).andReturn(deadAfterSomeTime);    
    expect(alive.isValid(1)).andReturn(true);
    expect(deadAfterSomeTime.isValid(1)).andReturn(false);
    deadAfterSomeTime.close();
    replay(ds, alive, deadAfterSomeTime);
    
    DatabaseConnectionPool pool = new DatabaseConnectionPool(ds);
    assertEquals(alive, pool.getConnection());
    assertEquals(deadAfterSomeTime, pool.getConnection());
   
    pool.releaseConnection(alive);
    // release dead connection to connection pool.
    pool.releaseConnection(deadAfterSomeTime);    
    
    // Pool should return alive connection and close dead connection.
    assertEquals(alive, pool.getConnection());
    verify(ds, alive, deadAfterSomeTime);
  }

  public void testConnectionPoolwithH2() throws SQLException {
    // Setup in-memory H2 JDBC DataSource;
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:testdb");
    ds.setUser("sa");
    ds.setPassword("sa");
    
    DatabaseConnectionPool pool = new DatabaseConnectionPool(ds);
    //Test getConnection
    Connection c = pool.getConnection();
    assertTrue(c.isValid(1));
    
    //Test release connection
    pool.releaseConnection(c);    
    Connection newOne = pool.getConnection();
    assertTrue(newOne.isValid(1));
    // verify same released connection is returned.
    assertEquals(c, newOne);

    //verify close connection.
    pool.releaseConnection(newOne);
    pool.closeConnections();    
    assertTrue(newOne.isClosed());
  }
}
