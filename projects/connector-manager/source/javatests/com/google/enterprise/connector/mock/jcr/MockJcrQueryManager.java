// Copyright (C) 2006 Google Inc.
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
import com.google.enterprise.connector.mock.MockRepositoryDocumentStore;
import com.google.enterprise.connector.test.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

/**
 * MockJcrQuery implements the corresponding JCR interface. This implementation
 * temporarily violates the explicit semantics of JCR by changing the query
 * language semantics to specify only a date range.
 */
public class MockJcrQueryManager implements QueryManager {

  MockRepositoryDocumentStore store;
  static final String[] SUPPORTED_LANGUAGES = new String[] {
      "mockQueryLanguage", Query.XPATH};

  /**
   * Creates a MockJcrQueryManager from a MockRepositoryDocumentStore
   * 
   * @param store
   */
  public MockJcrQueryManager(MockRepositoryDocumentStore store) {
    this.store = store;
  }

  /**
   * Creates a query object. This class does not fully support JCR, because only
   * the pseudo-query-language "mockQueryLanguage" is supported. 
   * @param statement
   * @param language
   * @return MockJcrQuery
   * @throws InvalidQueryException
   * @throws RepositoryException
   */
  public Query createQuery(String statement, String language)
      throws InvalidQueryException, RepositoryException {

    if (statement == null || statement.length() == 0) {
      throw new InvalidQueryException("Invalid query: " + statement);
    }
    if (language == null) {
      throw new IllegalArgumentException("Null language");
    }
    if (language.equals("mockQueryLanguage")) {
      return createMockQueryLanguageQuery(statement);
    }
    if (language.equals(Query.XPATH)) {
      return createXpathQuery(statement);
    }
    throw new IllegalArgumentException("Unsupported language: " + language);
  }

  private Query createMockQueryLanguageQuery(String statement)
      throws InvalidQueryException, RepositoryException {

    JSONObject jo;
    try {
      jo = new JSONObject(statement);
    } catch (JSONException e) {
      throw new IllegalArgumentException("query statement can not be parsed");
    }

    int fromInt;
    int toInt;

    try {
      fromInt = jo.getInt("from");
      toInt = jo.getInt("to");
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }

    MockRepositoryDateTime from = new MockRepositoryDateTime(fromInt);
    MockRepositoryDateTime to = new MockRepositoryDateTime(toInt);

    return new MockJcrQuery(from, to, store);
  }

  private Query createXpathQuery(String statement)
      throws InvalidQueryException, RepositoryException {

    String queryStringNoBound = 
      "//element(*, nt:resource) order by jcr:lastModified, jcr:uuid";

    if (statement.equals(queryStringNoBound)) {
      return createXpathQueryNoBound();
    }

    String queryPrefix = "//element(*, nt:resource)[@jcr:lastModified >= "
        + "xs:dateTime(\"";

    if (!statement.startsWith(queryPrefix)) {
      throw new InvalidQueryException("Invalid query: " + statement + ".  "
          + "Query must begin with: " + queryPrefix);
    }

    return createXpathQueryWithBound(statement, queryPrefix);
  }

  private Query createXpathQueryNoBound() {
    MockRepositoryDateTime from = new MockRepositoryDateTime(0);
    return new MockJcrQuery(from, store);
  }

  private Query createXpathQueryWithBound(String statement, String queryPrefix)
      throws InvalidQueryException {
    String statementRemainder = statement.substring(queryPrefix.length());

    Calendar c = null;
    try {
      c = TimeUtils.iso8601ToCalendar(statementRemainder);
    } catch (ParseException e) {
      throw new InvalidQueryException("Invalid query: " + statement + ".  "
          + "Can't parse date.", e);
    }

    long millis = c.getTimeInMillis();
    int fromInt = (int) (millis / 1000);

    MockRepositoryDateTime from = new MockRepositoryDateTime(fromInt);

    return new MockJcrQuery(from, store);
  }

  /**
   * Returns the query languages supported - in this case, only the mock query
   * language "mockQueryLanguage".
   * 
   * @return {"mockQueryLanguage"}
   * @throws RepositoryException
   */
  public String[] getSupportedQueryLanguages() throws RepositoryException {
    return SUPPORTED_LANGUAGES;
  }

  // The following methods are JCR level 1 - but we do not anticipate using them

  /**
   * Throws UnsupportedOperationException
   * 
   * @param arg0
   * @return nothing
   * @throws InvalidQueryException
   * @throws RepositoryException
   */
  public Query getQuery(Node arg0) throws InvalidQueryException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }


}
