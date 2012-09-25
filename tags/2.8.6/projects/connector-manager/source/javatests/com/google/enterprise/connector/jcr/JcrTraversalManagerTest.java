// Copyright (C) 2006-2009 Google Inc.
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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrNode;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.jcr.query.QueryManager;

public class JcrTraversalManagerTest extends TestCase {
  private static final Logger logger =
      Logger.getLogger(JcrTraversalManagerTest.class.getName());

  @Override
  public void tearDown() {
    // Reset the default time zone.
    Value.setFeedTimeZone("");
  }

  /**
   * Test generating checkpoints.
   *
   * @throws RepositoryException
   * @throws JSONException
   */
  public void testCheckpoint() throws RepositoryException, JSONException {
    // We're comparing date strings here, so we need a fixed time zone.
    Value.setFeedTimeZone("GMT");

    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    MockRepositoryDocument mockDoc = r.getStore().getDocByID("doc1");
    Document doc = new JcrDocument(new MockJcrNode(mockDoc));
    String checkpointString = JcrDocumentList.checkpoint(doc);
    logger.info(checkpointString);

    JSONObject jo = new JSONObject(checkpointString);

    String lastModified = jo.getString("lastModified");
    Assert.assertEquals("1970-01-01T00:00:10.000Z", lastModified);
    String uuid = jo.getString("uuid");
    Assert.assertEquals("doc1", uuid);
  }

  public void testExtractFromCheckpoint() throws JSONException {
    String checkpointString =
        "{\"uuid\":\"doc1\",\"lastModified\":\"1970-01-01T00:00:10.000Z\"}";
    JcrTraversalManager qtm =
        new JcrTraversalManager(null);

    JSONObject jo = new JSONObject(checkpointString);
    Calendar c = qtm.extractCalendarFromCheckpoint(jo, checkpointString);
    long millis = c.getTimeInMillis();
    Assert.assertEquals(millis, 10000);

    String id = qtm.extractDocidFromCheckpoint(jo, checkpointString);
    Assert.assertEquals(id, "doc1");
  }

  public void testResumeTraversal() throws RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);

    {
      MockRepositoryDocument mockDoc = r.getStore().getDocByID("doc2");
      Document doc = new JcrDocument(new MockJcrNode(mockDoc));
      String checkpointString = JcrDocumentList.checkpoint(doc);
      DocumentList documentList = qtm.resumeTraversal(checkpointString);
      int counter = countDocuments(documentList);
      Assert.assertEquals(2, counter);
    }

    {
      MockRepositoryDocument mockDoc = r.getStore().getDocByID("doc4");
      Document doc = new JcrDocument(new MockJcrNode(mockDoc));
      String checkpointString = JcrDocumentList.checkpoint(doc);
      DocumentList documentList = qtm.resumeTraversal(checkpointString);
      int counter = countDocuments(documentList);
      Assert.assertEquals(0, counter);
    }
  }

  public void testStartTraversal() throws RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);
    {
      DocumentList documentList = qtm.startTraversal();
      int counter = countDocuments(documentList);
      Assert.assertEquals(4, counter);
    }
  }

  private int countDocuments(DocumentList documentList)
      throws RepositoryException {
    int counter = 0;
    if (documentList != null) {
      Document document = null;
      while ((document = documentList.nextDocument()) != null) {
        logger.info(Value.getSingleValueString(document,
            SpiConstants.PROPNAME_DOCID));
        counter++;
      }
    }
    return counter;
  }
}
