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
import com.google.enterprise.connector.mock.MockRepositoryDocumentStore;
import com.google.enterprise.connector.spi.Value;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.ParsePosition;
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

  private static final String XPATH_QUERY_STRING_UNBOUNDED_DEFAULT =
    "//*[@jcr:primaryType='nt:resource'] order by @jcr:lastModified, @jcr:uuid";

  private static final String XPATH_QUERY_STRING_BOUNDED_DEFAULT =
    "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= " +
    "''{0}''] order by @jcr:lastModified, @jcr:uuid";

  private String xpathUnboundedTraversalQuery;
  private String xpathBoundedTraversalQuery;

  /**
   * @param xpathBoundedTraversalQuery the xpathBoundedTraversalQuery to set
   */
  void setXpathBoundedTraversalQuery(String xpathBoundedTraversalQuery) {
    this.xpathBoundedTraversalQuery = xpathBoundedTraversalQuery;
  }

  /**
   * @param xpathUnboundedTraversalQuery the xpathUnboundedTraversalQuery to set
   */
  void setXpathUnboundedTraversalQuery(String xpathUnboundedTraversalQuery) {
    this.xpathUnboundedTraversalQuery = xpathUnboundedTraversalQuery;
  }

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
    this.xpathUnboundedTraversalQuery = XPATH_QUERY_STRING_UNBOUNDED_DEFAULT;
    this.xpathBoundedTraversalQuery = XPATH_QUERY_STRING_BOUNDED_DEFAULT;
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

  private Query createMockQueryLanguageQuery(String statement) {

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

    if (statement.equals(xpathUnboundedTraversalQuery)) {
      return createXpathQueryNoBound();
    }

    MessageFormat mf = new MessageFormat(xpathBoundedTraversalQuery);
    Object[] objs = mf.parse(statement, new ParsePosition(0));

    if (objs == null || objs.length < 1 || !(objs[0] instanceof String)) {
      throw new InvalidQueryException("Invalid query: \"" + statement + "\" "
          + "does not match format: \"" + xpathBoundedTraversalQuery + "\"");
    }
    String dateString = (String) objs[0];
    return createXpathQueryWithBound(statement, dateString);
  }

  private Query createXpathQueryNoBound() {
    MockRepositoryDateTime from = new MockRepositoryDateTime(0);
    return new MockJcrQuery(from, store);
  }

  private Query createXpathQueryWithBound(String statement, String dateString)
      throws InvalidQueryException {

    Calendar c = null;
    try {
      c = Value.iso8601ToCalendar(dateString);
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
   */
  public String[] getSupportedQueryLanguages() {
    return SUPPORTED_LANGUAGES;
  }

  // The following methods are JCR level 1 - but we do not anticipate using them

  /**
   * Throws UnsupportedOperationException
   *
   * @param arg0
   * @return nothing
   */
  public Query getQuery(Node arg0) {
    throw new UnsupportedOperationException();
  }


}
