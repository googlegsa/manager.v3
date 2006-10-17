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


package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.jcradaptor.SpiPropertyMapFromJcrTest;
import com.google.enterprise.connector.jcradaptor.SpiQueryTraversalManagerFromJcr;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Iterator;

import javax.jcr.query.QueryManager;

/**
 * Tests DocPusher.
 */
public class DocPusherTest extends TestCase {

  public void testTake() throws RepositoryException {
    String expectedXml =
        "datasource=junit&feedtype=full&data="
            + "<?xml version=\'1.0\' encoding=\'UTF-8\'?>"
            + "<!DOCTYPE gsafeed PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"gsafeed.dtd\">"
            + "<gsafeed><header><datasource>junit</datasource>\n"
            + "<feedtype>full</feedtype>\n"
            + "</header>\n"
            + "<group>\n"
            + "<record url=\"http://www.sometesturl.com/test\" mimetype=\""
            + SpiConstants.DEFAULT_MIMETYPE
            + "\" last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\" >\n"
            + "<metadata>\n"
            + "<meta name=\"google:lastmodify\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
            + "<meta name=\"google:searchurl\" content=\"http://www.sometesturl.com/test\"/>\n"
            + "<meta name=\"jcr:lastModified\" content=\"1970-01-01T00:00:10.000Z\"/>\n"
            + "</metadata>\n" + "<content encoding=\"base64binary\" >\n"
            + "bm93IGlzIHRoZSB0aW1l\n" + "</content>\n" + "</record>\n"
            + "</group>\n" + "</gsafeed>\n" + "";
    String resultXML;
    String gsaExpectedResponse = "Mock response";
    String gsaActualResponse;

    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog3.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    QueryTraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);

    ResultSet resultSet = qtm.startTraversal();

    for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
      PropertyMap propertyMap = (PropertyMap) iter.next();
      dpusher.take(propertyMap, "junit");
      resultXML = mockFeedConnection.getFeed();
      gsaActualResponse = dpusher.getGsaResponse();
      Assert.assertEquals(expectedXml, resultXML);
      Assert.assertEquals(gsaExpectedResponse, gsaActualResponse);
    }
  }

  /**
   * Tests DocPusher.
   * 
   * @throws RepositoryException
   */
  public void testSimpleDoc() throws RepositoryException {
    String json1 =
        "{\"timestamp\":\"10\",\"docid\":\"doc1\""
            + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
            + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
            + "}\r\n" + "";
    PropertyMap propertyMap =
        SpiPropertyMapFromJcrTest.makePropertyMapFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(propertyMap, "junit");
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"ziff\"/>",
        resultXML);
    assertStringContains("url=\"googleconnector://junit.localhost?docid=doc1\"",
        resultXML);
    
  }

  /**
   * Tests DocPusher.
   * 
   * @throws RepositoryException
   */
  public void testDisplayUrl() throws RepositoryException {
    String json1 =
        "{\"timestamp\":\"10\",\"docid\":\"doc1\""
            + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
            + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
            + "}\r\n" + "";
    PropertyMap propertyMap =
        SpiPropertyMapFromJcrTest.makePropertyMapFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(propertyMap, "junit");
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    
  }

  /**
   * Tests DocPusher.
   * 
   * @throws RepositoryException
   */
  public void testWordDoc() throws RepositoryException {
    String json1 =
      "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"google:mimetype\":\"application/msword\""
      + ",\"contentfile\":\"testdata/mocktestdata/test.doc\"" 
      + ",\"author\":\"ziff\""
      + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
      + "}\r\n" + "";
    PropertyMap propertyMap =
        SpiPropertyMapFromJcrTest.makePropertyMapFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(propertyMap, "junit");
    String resultXML = mockFeedConnection.getFeed();
 
    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"ziff\"/>",
        resultXML);
    assertStringContains("url=\"googleconnector://junit.localhost?docid=doc1\"",
        resultXML);
    
  }

  public static void assertStringContains(String expected, String actual) {
    Assert.assertTrue("Expected:\n" + expected + "\nDid not appear in\n"
        + actual, actual.indexOf(expected) > 0);
  }
}
