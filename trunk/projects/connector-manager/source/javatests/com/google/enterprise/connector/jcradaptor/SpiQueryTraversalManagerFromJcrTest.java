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

package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.MockRepositoryPropertyTest;
import com.google.enterprise.connector.mock.jcr.MockJcrNode;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.jcr.LoginException;
import javax.jcr.query.QueryManager;

public class SpiQueryTraversalManagerFromJcrTest extends TestCase {
  private static final Logger logger =
      Logger.getLogger(MockRepositoryPropertyTest.class.getName());

  /**
   * Test generating checkpoints
   * 
   * @throws RepositoryException
   * @throws javax.jcr.RepositoryException
   * @throws LoginException
   * @throws JSONException
   */
  public void testCheckpoint() throws RepositoryException, LoginException,
      javax.jcr.RepositoryException, JSONException {

    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    QueryTraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);

    {
      MockRepositoryDocument doc = r.getStore().getDocByID("doc1");
      PropertyMap pm = new SpiPropertyMapFromJcr(new MockJcrNode(doc));

      String checkpointString = qtm.checkpoint(pm);
      logger.info(checkpointString);

      JSONObject jo = new JSONObject(checkpointString);

      String lastModified = jo.getString("lastModified");
      Assert.assertEquals("1970-01-01T00:00:10.000Z", lastModified);
      String uuid = jo.getString("uuid");
      Assert.assertEquals("doc1", uuid);
    }
  }

  public void testExtractFromCheckpoint() throws JSONException {
    String checkpointString =
        "{\"uuid\":\"doc1\","
            + "\"lastModified\":\"1970-01-01T00:00:10.000Z\"}";
    SpiQueryTraversalManagerFromJcr qtm =
        new SpiQueryTraversalManagerFromJcr(null);

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
    QueryTraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);

    {
      MockRepositoryDocument doc = r.getStore().getDocByID("doc2");
      PropertyMap pm = new SpiPropertyMapFromJcr(new MockJcrNode(doc));

      String checkpointString = qtm.checkpoint(pm);

      ResultSet resultSet = qtm.resumeTraversal(checkpointString);

      int counter = 0;
      for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
        PropertyMap propertyMap = (PropertyMap) iter.next();
        logger.info(propertyMap.getProperty(SpiConstants.PROPNAME_DOCID)
            .getValue().getString());
        counter++;
      }
      Assert.assertEquals(2, counter);
    }

    {
      MockRepositoryDocument doc = r.getStore().getDocByID("doc4");
      PropertyMap pm = new SpiPropertyMapFromJcr(new MockJcrNode(doc));

      String checkpointString = qtm.checkpoint(pm);

      ResultSet resultSet = qtm.resumeTraversal(checkpointString);

      int counter = 0;
      for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
        PropertyMap propertyMap = (PropertyMap) iter.next();
        logger.info(propertyMap.getProperty(SpiConstants.PROPNAME_DOCID)
            .getValue().getString());
        counter++;
      }
      Assert.assertEquals(0, counter);
    }
  }

  public void testStartTraversal() throws RepositoryException {

    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    QueryTraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);

    {
      MockRepositoryDocument doc = r.getStore().getDocByID("doc4");

      ResultSet resultSet = qtm.startTraversal();

      int counter = 0;
      for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
        PropertyMap propertyMap = (PropertyMap) iter.next();
        logger.info(propertyMap.getProperty(SpiConstants.PROPNAME_DOCID)
            .getValue().getString());
        counter++;
      }
      Assert.assertEquals(4, counter);
    }
  }

}
