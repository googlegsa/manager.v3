// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.jcradaptor.SpiPropertyMapFromJcrTest;
import com.google.enterprise.connector.jcradaptor.SpiTraversalManagerFromJcr;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.PropertyMapList;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.jcr.query.QueryManager;

/**
 * Tests DocPusher.
 */
public class DocPusherTest extends TestCase {

  /**
   * Test Take for a URL/metadata feed when google.searchurl exists.
   * 
   * @throws RepositoryException
   */
  public void testTakeUrlMeta() throws PushException, RepositoryException {
    String[] expectedXml = new String[1];
    String feedType = "metadata-and-url";
    String record = "<record url=\"http://www.sometesturl.com/test\""
        + " mimetype=\"text/html\""
        + " last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\" >\n"
        + "<metadata>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"google:searchurl\" content=\"http://www.sometesturl.com/test\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01T00:00:10.000Z\"/>\n"
        + "</metadata>\n" + "</record>\n";
    
    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog5.txt");
  }
  
  /**
   * Test Take for a URL/metadata feed when google.searchurl exists, and
   * some of the metadata is empty. In this case, the 
   * MockRepositoryEventLog5null.txt file is almost the same as
   * MockRepositoryEventLog5.txt but has a metadata item with empty content
   * in it
   * 
   * @throws RepositoryException
   */
  public void testTakeUrlMetaNulls() throws PushException, RepositoryException {
    String[] expectedXml = new String[1];
    String feedType = "metadata-and-url";
    String record = "<record url=\"http://www.sometesturl.com/test\""
        + " mimetype=\"text/html\""
        + " last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\" >\n"
        + "<metadata>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"google:searchurl\" content=\"http://www.sometesturl.com/test\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01T00:00:10.000Z\"/>\n"
        + "</metadata>\n" + "</record>\n";
    
    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog5null.txt");
  }  
  
  /**
   * Test Take for a content feed.
   * 
   * @throws RepositoryException
   */
  public void testTakeContent() throws PushException, RepositoryException {
    String[] expectedXml = new String[1];
    String feedType = "incremental";
    String record = "<record url=\"" + ServletUtil.PROTOCOL
      + "junit.localhost" + ServletUtil.DOCID + "doc1\""
      + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE
      + "\" last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\" >\n"
      + "<metadata>\n"
      + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
      + "<meta name=\"jcr:lastModified\" content=\"1970-01-01T00:00:10.000Z\"/>\n"
      + "</metadata>\n" + "<content encoding=\"base64binary\" >"
      + "bm93IGlzIHRoZSB0aW1l" + "</content>\n" + "</record>\n";
    
    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog6.txt");
  }

  /**
   * Test Take for isPublic.
   * 
   * @throws RepositoryException
   */
  public void testTakeIsPublic() throws PushException, RepositoryException {
    String[] expectedXml = new String[4];
    String feedType = "incremental";
    
    // case 1: "google:ispublic":"false"
    String record = "<record url=\"" + ServletUtil.PROTOCOL
        + "junit.localhost" + ServletUtil.DOCID + "users\""
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Thu, 01 Jan 1970 00:00:00 GMT\""
        + " authmethod=\"httpbasic\" >\n"
        + "<metadata>\n"
        + "<meta name=\"acl\" content=\"joe, mary, fred, mark, bill, admin\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"false\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01T00:00:00.000Z\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\" >"
        + "VGhpcyBpcyBhIHNlY3VyZSBkb2N1bWVudA==" + "</content>\n" + "</record>\n";
    expectedXml[0] = buildExpectedXML(feedType, record);

    // case 2: "google:ispublic":"true"
    record = "<record url=\"" + ServletUtil.PROTOCOL
        + "junit.localhost" + ServletUtil.DOCID + "doc1\""
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\" >\n"
        + "<metadata>\n"
        + "<meta name=\"acl\" content=\"joe, mary\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01T00:00:10.000Z\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\" >"
        + "VGhpcyBpcyB0aGUgcHVibGljIGRvY3VtZW50Lg==" + "</content>\n" + "</record>\n";
    expectedXml[1] = buildExpectedXML(feedType, record);

    // case 3: "google:ispublic":"public"; the value "public" is illegal value.
    // note also: MockRepositoryEventLog7.txt has a "" in the acl property,
    // which null-handling should drop out, leaving just "joe, mary"
    record = "<record url=\"" + ServletUtil.PROTOCOL
      + "junit.localhost" + ServletUtil.DOCID + "doc2\""
      + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE
      + "\" last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\" >\n"
      + "<metadata>\n"
      + "<meta name=\"acl\" content=\"joe, mary\"/>\n"
      + "<meta name=\"google:ispublic\" content=\"public\"/>\n"
      + "<meta name=\"google:lastmodified\" content=\"1970-01-01T00:00:10.000Z\"/>\n"
      + "</metadata>\n" + "<content encoding=\"base64binary\" >"
      + "VGhpcyBpcyBhIGRvY3VtZW50Lg==" + "</content>\n" + "</record>\n";
    expectedXml[2] = buildExpectedXML(feedType, record);
    
    takeFeed(expectedXml, "MockRepositoryEventLog7.txt");
  }

  private void takeFeed(String[] expectedXml, String repository)
      throws PushException, RepositoryException {
    String gsaExpectedResponse = GsaFeedConnection.SUCCESS_RESPONSE;
    String gsaActualResponse;

    MockRepositoryEventList mrel = new MockRepositoryEventList(
        repository);
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new SpiTraversalManagerFromJcr(qm);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);

    PropertyMapList propertyMapList = qtm.startTraversal();

    int i = 0;
    for (Iterator iter = propertyMapList.iterator(); iter.hasNext();) {
      Assert.assertFalse(i == expectedXml.length);
      PropertyMap propertyMap = (PropertyMap) iter.next();
      dpusher.take(propertyMap, "junit");
      String resultXML = mockFeedConnection.getFeed();
      gsaActualResponse = dpusher.getGsaResponse();
      Assert.assertEquals(expectedXml[i], resultXML);
      Assert.assertEquals(gsaExpectedResponse, gsaActualResponse);
      ++i;
    }
  }

  /**
   * Test basic metadata representation.
   * 
   * @throws RepositoryException
   */
  public void testSimpleDoc() throws PushException, RepositoryException {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:contenturl\":\"http://www.sometesturl.com/test\"" + "}\r\n"
      + "";
    PropertyMap propertyMap = SpiPropertyMapFromJcrTest
      .makePropertyMapFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(propertyMap, "junit");
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains(
      urlEncode("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\""), resultXML);
    assertStringContains(
      urlEncode("<meta name=\"author\" content=\"ziff\"/>"), resultXML);
    assertStringContains(
      urlEncode("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
                + ServletUtil.DOCID + "doc1\""), resultXML);

  }

  /**
   * Test displayurl.
   * 
   * @throws RepositoryException
   */
  public void testDisplayUrl() throws PushException, RepositoryException {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\"" + "}\r\n"
      + "";
    PropertyMap propertyMap = SpiPropertyMapFromJcrTest
      .makePropertyMapFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(propertyMap, "junit");
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains(
      urlEncode("displayurl=\"http://www.sometesturl.com/test\""), resultXML);

  }

  /**
   * Test special characters in metadata values.
   * 
   * @throws RepositoryException
   */
  public void testSpecials() throws PushException, RepositoryException {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\""
      // note double escaping in the line below, since this is a json string
      + ",\"special\":\"`~!@#$%^&*()_+-={}[]|\\\\:\\\";'<>?,./\""
      + ",\"japanese\":\"\u5317\u6d77\u9053\""
      + ",\"chinese\":\"\u5317\u4eac\u5e02\"" + "}\r\n" + "";
    PropertyMap propertyMap = SpiPropertyMapFromJcrTest
      .makePropertyMapFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(propertyMap, "junit");
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains(urlEncode("<meta name=\"special\" " +
    // only single escapes here, because this is not a json string
      // but xml-sensitive characters have been replaced with entities
      "content=\"`~!@#$%^&amp;*()_+-={}[]|\\:&quot;;&apos;&lt;>?,./\"/>"),
      resultXML);

    assertStringContains(urlEncode("<meta name=\"japanese\" " +
    // only single escapes here, because this is not a json string
      // but xml-sensitive characters have been replaced with entities
      "content=\"\u5317\u6d77\u9053\"/>"), resultXML);

    assertStringContains(urlEncode("<meta name=\"chinese\" " +
    // only single escapes here, because this is not a json string
      // but xml-sensitive characters have been replaced with entities
      "content=\"\u5317\u4eac\u5e02\"/>"), resultXML);

  }

  private static String urlEncode(String str) {
    try {
      return URLEncoder.encode(str, DocPusher.XML_DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      fail();
    }
    return null;
  }

  /**
   * Tests a word document.
   * 
   * @throws RepositoryException
   */
  public void testWordDoc() throws PushException, RepositoryException {
    final String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"google:mimetype\":\"application/msword\""
      + ",\"contentfile\":\"testdata/mocktestdata/test.doc\""
      + ",\"author\":\"ziff\""
      + ",\"google:contenturl\":\"http://www.sometesturl.com/test\"" + "}\r\n"
      + "";
    PropertyMap propertyMap = SpiPropertyMapFromJcrTest
      .makePropertyMapFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(propertyMap, "junit");
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains(
      urlEncode("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\""), resultXML);
    assertStringContains(
      urlEncode("<meta name=\"author\" content=\"ziff\"/>"), resultXML);
    assertStringContains(
      urlEncode("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
                + ServletUtil.DOCID + "doc1\""), resultXML);
  }

  public static void assertStringContains(String expected, String actual) {
    Assert.assertTrue("Expected:\n" + expected + "\nDid not appear in\n"
      + actual, actual.indexOf(expected) > 0);
  }

  
  private String buildExpectedXML(String feedType, String record) {
    String rawData = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>"
        + "<!DOCTYPE gsafeed PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"gsafeed.dtd\">"
        + "<gsafeed><header><datasource>junit</datasource>\n"
        + "<feedtype>" + feedType + "</feedtype>\n"
        + "</header>\n"
        + "<group>\n"
        + record
        + "</group>\n"
        + "</gsafeed>\n";
    return "datasource=junit&feedtype=" + feedType + "&data="
        + urlEncode(rawData);
  }

}
