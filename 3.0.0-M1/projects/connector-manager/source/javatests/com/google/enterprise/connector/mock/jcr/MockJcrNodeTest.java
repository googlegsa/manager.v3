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
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Unit tests for MockJCRNode
 */
public class MockJcrNodeTest extends TestCase {
  private static final Logger logger =
      Logger.getLogger(MockJcrNodeTest.class.getName());

  /**
   * Sanity test
   * @throws RepositoryException
   */
  public void testSimpleDoc() throws RepositoryException {
    MockRepositoryDocument mrd = new MockRepositoryDocument(
        new MockRepositoryDateTime(50000), "docid2", "now is the time",
        new MockRepositoryPropertyList());
    Node n = new MockJcrNode(mrd);
    Property p = n.getProperty("jcr:content");
    Assert.assertNotNull(p);
    String content = p.getString();
    logger.info("Content is \"" + content + "\"");

    p = n.getProperty("jcr:uuid");
    Assert.assertNotNull(p);
    String uuid = p.getString();
    logger.info("uuid is \"" + uuid + "\"");

    p = n.getProperty("jcr:lastModified");
    Assert.assertNotNull(p);
    Calendar modifyDate = p.getDate();
    logger.info("modify date is \"" + modifyDate.getTime() + "\"");
  }

  /**
   * Another simple test, based on the Json Constructor
   * @throws RepositoryException
   */
  public void testJsonDoc() throws RepositoryException {
    String in = "{timestamp:10, docid:xyzzy, "
        + "content:\"Now is the time\", name:John}";
    JSONObject jo;
    try {
      jo = new JSONObject(in);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    MockRepositoryDocument document = new MockRepositoryDocument(jo);
    MockJcrNode node = new MockJcrNode(document);

    {
      Property p = node.getProperty("jcr:content");
      Assert.assertNotNull(p);
      String value = p.getString();
      Assert.assertEquals(value, "Now is the time");
    }

    {
      Property p = node.getProperty("jcr:uuid");
      Assert.assertNotNull(p);
      String value = p.getString();
      Assert.assertEquals(value, "xyzzy");
    }

    {
      Property p = node.getProperty("name");
      Assert.assertNotNull(p);
      String value = p.getString();
      Assert.assertEquals(value, "John");
    }

  }

}
