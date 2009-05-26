// Copyright (C) 2006-2008 Google Inc.
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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepositoryDateTime;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryDocumentStore;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * MockJcrQuery implements the corresponding JCR interface.  This implementation
 * temporarily violates the explicit semantics of JCR by only allowing date
 * queries between specified bounds.  This class will need revisiting when we
 * work with a real JCR implementation.
 * <p>
 * TODO(ziff): extend the query semantics as needed
 */
public class MockJcrQuery implements Query {

  private final String statement;

  private final List<MockRepositoryDocument> internalQuery;

  /**
   * Creates a MockJcrQuery object from a date range.  This is intended to be
   * used for query traversal.  We may need to add another constructor, or
   * another type that implements this interface, for other SPI requirements
   * as we go along.
   * @param from    Beginning point of range
   * @param to  End point of range
   * @param store   MockRepositoryDocumentStore from which documents are
   * returned
   */
  public MockJcrQuery(MockRepositoryDateTime from, MockRepositoryDateTime to,
      MockRepositoryDocumentStore store) {
    this.statement = "Query for documents between " + from.toString() + " and "
        + to.toString();
    this.internalQuery = store.dateRange(from, to);
  }

  /**
   * Creates a MockJcrQuery object from a single date.  This is intended to be
   * used for query traversal.
   * @param from    Beginning point of range
   * @param store   MockRepositoryDocumentStore from which documents are
   * returned
   */
  public MockJcrQuery(MockRepositoryDateTime from,
      MockRepositoryDocumentStore store) {
    this.statement = "Query for documents from " + from.toString();
    this.internalQuery = store.dateRange(from);
  }

  /**
   * Returns the result list from executing the query. Clients may consume as
   * much or little of the result as they need.
   * @return MockJcrQueryResult
   */
  public QueryResult execute() {
    return new MockJcrQueryResult(this.internalQuery);
  }

  /**
   * Returns the query statement.  In this implementation, this is
   * just for debugging - it's not an actual query string in a query language.
   * @return the stored statement
   */
  public String getStatement() {
    return statement;
  }

  /**
   * Returns the query language.  TBD(ziff): consider whether this is really
   * needed.
   * @return null
   */
  public String getLanguage() {
    return null;
  }

  //The following methods are JCR level 1 - but we do not anticipate using them

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public String getStoredQueryPath() {
    throw new UnsupportedOperationException();
  }

  //The following methods are JCR level 2 - these would never be needed

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @return nothing
   */
  public Node storeAsNode(String arg0) {
    throw new UnsupportedOperationException();
  }

}
