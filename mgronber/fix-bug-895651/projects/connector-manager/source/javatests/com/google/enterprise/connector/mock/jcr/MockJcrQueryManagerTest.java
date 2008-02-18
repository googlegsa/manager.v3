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

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDateTime;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.MockRepositoryPropertyTest;

import junit.framework.TestCase;

import java.text.MessageFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * Unit tests for Mock JCR repository.  
 */
public class MockJcrQueryManagerTest extends TestCase {
  private static final Logger logger = Logger
      .getLogger(MockRepositoryPropertyTest.class.getName());

  /**
   * Simple query test
   * @throws RepositoryException
   */
  public void testSimpleQuery() throws RepositoryException {
    MockRepositoryEventList mrel = new MockRepositoryEventList(
        "MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r);

    String statement = "{from:10, to:21}";
    String language = "mockQueryLanguage";
    // Important to do this before the query is created since the constructor
    // can mutate the store.
    List docs = r.getStore().dateRange(new MockRepositoryDateTime(10),
        new MockRepositoryDateTime(21));

    Query query = qm.createQuery(statement, language);
    QueryResult qr = query.execute();

    assertQueryResultContainsOnlyDocs(qr, docs);
  }

  public void testXpathQuery() throws RepositoryException {
    MockRepositoryEventList mrel = new MockRepositoryEventList(
        "MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);

    QueryManager qm = new MockJcrQueryManager(r);

    String messagePattern = 
      "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= " +
      "''{0}''] order by @jcr:lastModified, @jcr:uuid";

    String time = "1970-01-01T00:00:50Z";

    String language = Query.XPATH;

    Object[] arguments = { time };
    String statement = MessageFormat.format(
        messagePattern,
        arguments);
    logger.info(statement);
    List docs = r.getStore().dateRange(new MockRepositoryDateTime(50));

    Query query = qm.createQuery(statement, language);
    QueryResult qr = query.execute();

    assertQueryResultContainsOnlyDocs(qr, docs);
  }

  public void testXpathQueryWithPause() throws RepositoryException {
    MockRepositoryEventList mrel = new MockRepositoryEventList(
        "MockRepositoryEventLog9.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r);
    String messagePattern = 
      "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= " +
      "''{0}''] order by @jcr:lastModified, @jcr:uuid";
    String time = "1970-01-01T00:00:00Z";
    String language = Query.XPATH;

    Object[] arguments = { time };
    String statement = MessageFormat.format(messagePattern, arguments);
    logger.info(statement);
    List docs = r.getStore().dateRange(new MockRepositoryDateTime(0));

    Query query = qm.createQuery(statement, language);
    QueryResult qr = query.execute();

    assertEquals(3, docs.size());
    assertQueryResultContainsOnlyDocs(qr, docs);

    // Try again after pause 
    docs = r.getStore().dateRange(new MockRepositoryDateTime(0));
    query = qm.createQuery(statement, language);
    qr = query.execute();

    assertEquals(2, docs.size());
    assertQueryResultContainsOnlyDocs(qr, docs);

    // And again
    docs = r.getStore().dateRange(new MockRepositoryDateTime(0));
    query = qm.createQuery(statement, language);
    qr = query.execute();

    assertEquals(4, docs.size());
    assertQueryResultContainsOnlyDocs(qr, docs);
  }

  /*
   * Helper method to make sure the given QueryResult only contains docs from
   * the given list of MockRepositoryDocument objects.  Equality is determined
   * by matching the Node "jcr:uuid" property to the MockRepositoryDocument
   * docId.
   */
  private void assertQueryResultContainsOnlyDocs(QueryResult qr, List docs) 
      throws RepositoryException {
    int count = 0;
    int target = docs.size();
    NodeIterator ni = qr.getNodes();

    while (ni.hasNext()) {
      Node n = ni.nextNode();
      for (ListIterator iter = docs.listIterator(); iter.hasNext(); ) {
        MockRepositoryDocument doc = (MockRepositoryDocument) iter.next();
        if (n.getProperty("jcr:uuid").getString().equals(doc.getDocID())) {
          logger.info("Found document where docid=" + doc.getDocID());
          iter.remove();
          count++;
          break;
        }
      }
    }
    assertEquals(target, count);
  }
}
