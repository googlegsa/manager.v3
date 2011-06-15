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

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * Unit tests for Mock JCR repository.
 */
public class MockJcrQueryManagerTest extends TestCase {
  private static final Logger logger =
      Logger.getLogger(MockJcrQueryManagerTest.class.getName());

  /**
   * Simple query test
   * @throws RepositoryException
   */
  public void testSimpleQuery() throws RepositoryException {
    MockRepositoryEventList mrel = new MockRepositoryEventList(
        "MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);

    QueryManager qm = new MockJcrQueryManager(r.getStore());

    String statement = "{from:10, to:21}";
    String language = "mockQueryLanguage";

    Query query = qm.createQuery(statement, language);

    QueryResult qr = query.execute();

    NodeIterator ni = qr.getNodes();

    Node n;
    while (ni.hasNext()) {
      n = ni.nextNode();
      logger.info("docid " + n.getProperty("jcr:uuid").getString());

      Property p;
      PropertyIterator pi = n.getProperties();
      String indent = "  ";
      while (pi.hasNext()) {
        p = pi.nextProperty();
        logger.info(indent + p.getName() + " " + p.getString());
      }
    }
  }

  public void testXpathQuery() throws RepositoryException {
    MockRepositoryEventList mrel = new MockRepositoryEventList(
        "MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);

    QueryManager qm = new MockJcrQueryManager(r.getStore());

    String messagePattern =
      "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= " +
      "''{0}''] order by @jcr:lastModified, @jcr:uuid";

    String time = "1970-01-01T00:00:50Z";

    String language = Query.XPATH;

    Object[] arguments = { time };
    String statement = MessageFormat.format(
        messagePattern,
        arguments);
    System.out.println(statement);

    Query query = qm.createQuery(statement, language);

    QueryResult qr = query.execute();

    NodeIterator ni = qr.getNodes();

    int count = 0;
    while (ni.hasNext()) {
      ni.nextNode();
      count++;
    }
    Assert.assertEquals(2, count);
  }

}
