// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.jcr.JcrDocumentTest;
import com.google.enterprise.connector.jcr.JcrTraversalManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.servlet.SAXParseErrorHandler;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.traversal.FileSizeLimitInfo;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.xml.sax.SAXParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javax.jcr.query.QueryManager;

/**
 * Tests DocPusher.
 */
public class DocPusherTest extends TestCase {

  /**
   * Test Take for a URL/metadata feed when google:searchurl exists.
   */
  public void testTakeUrlMeta() throws Exception {
    String[] expectedXml = new String[1];
    String feedType = "metadata-and-url";
    String record = "<record url=\"http://www.sometesturl.com/test\""
        + " mimetype=\"text/html\""
        + " last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"google:searchurl\" content=\"http://www.sometesturl.com/test\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog5.txt");
  }

  /**
   * Test Take for a URL/metadata feed when google:searchurl exists, and some of
   * the metadata is empty. In this case, the MockRepositoryEventLog5null.txt
   * file is almost the same as MockRepositoryEventLog5.txt but has a metadata
   * item with empty content in it
   */
  public void testTakeUrlMetaNulls() throws Exception {
    String[] expectedXml = new String[1];
    String feedType = "metadata-and-url";
    String record = "<record url=\"http://www.sometesturl.com/test\""
        + " mimetype=\"text/html\""
        + " last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"google:searchurl\" content=\"http://www.sometesturl.com/test\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog5null.txt");
  }

  /**
   * Test Take for a URL/metadata feed when google:searchurl exists and
   * is a SMB URL.
   * Regression Test for Connector Manager Issue 100
   */
  public void testTakeSmbUrlMeta() throws Exception {
    String[] expectedXml = new String[1];
    String feedType = "metadata-and-url";
    String record = "<record url=\"smb://localhost/share/test\""
        + " mimetype=\"text/html\""
        + " last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"google:searchurl\" content=\"smb://localhost/share/test\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog5smb.txt");
  }

  /**
   * Test Take for a content feed.
   */
  public void testTakeContent() throws Exception {
    String[] expectedXml = new String[1];
    String feedType = "incremental";
    String record = "<record url=\""
        + ServletUtil.PROTOCOL
        + "junit.localhost"
        + ServletUtil.DOCID
        + "doc1\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "bm93IGlzIHRoZSB0aW1l" + "\n</content>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog6.txt");
  }

  /**
   * Test Take for a compressed content feed.
   */
  public void testTakeCompressedContent() throws Exception {
    String[] expectedXml = new String[1];
    String feedType = "incremental";
    String record = "<record url=\""
        + ServletUtil.PROTOCOL
        + "junit.localhost"
        + ServletUtil.DOCID
        + "doc10\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"contentfile\" content=\"testdata/mocktestdata/i18n.html\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"google:mimetype\" content=\"text/html\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64compressed\">\n"
        + "eJyzySjJzbE73Hd449HFh1cWHd54eCmQse7wNhDryJ7D647uQ4jY6INVAwBbqCBF"
        + "\n</content>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog8.txt", true);
  }


  /**
   * Test Take for isPublic.
   */
  public void testTakeIsPublic() throws Exception {
    String[] expectedXml = new String[4];
    String feedType = "incremental";

    // case 1: "google:ispublic":"false"
    String record = "<record url=\""
        + ServletUtil.PROTOCOL
        + "junit.localhost"
        + ServletUtil.DOCID
        + "users\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Thu, 01 Jan 1970 00:00:00 GMT\""
        + " authmethod=\"httpbasic\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe, mary, fred, mark, bill, admin\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"false\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIHNlY3VyZSBkb2N1bWVudA==" + "\n</content>\n"
        + "</record>\n";
    expectedXml[0] = buildExpectedXML(feedType, record);

    // case 2: "google:ispublic":"true"
    record = "<record url=\""
        + ServletUtil.PROTOCOL
        + "junit.localhost"
        + ServletUtil.DOCID
        + "doc1\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe, mary\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyB0aGUgcHVibGljIGRvY3VtZW50Lg==" + "\n</content>\n"
        + "</record>\n";
    expectedXml[1] = buildExpectedXML(feedType, record);

    // case 3: "google:ispublic":"public"; the value "public" is illegal value.
    // note also: MockRepositoryEventLog7.txt has a "" in the acl property,
    // which null-handling should drop out, leaving just "joe, mary"
    record = "<record url=\""
        + ServletUtil.PROTOCOL
        + "junit.localhost"
        + ServletUtil.DOCID
        + "doc2\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe, mary\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"public\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIGRvY3VtZW50Lg==" + "\n</content>\n" + "</record>\n";
    expectedXml[2] = buildExpectedXML(feedType, record);

    takeFeed(expectedXml, "MockRepositoryEventLog7.txt");
  }

  /**
   * Test for multiple document feed.
   */
  public void testMultiRecordFeed() throws Exception {
    String feedType = "incremental";

    // Doc 1.
    String records = "<record url=\""
        + ServletUtil.PROTOCOL
        + "junit.localhost"
        + ServletUtil.DOCID
        + "doc1\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Thu, 01 Jan 1970 00:00:00 GMT\""
        + " authmethod=\"httpbasic\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe, mary, fred, mark, bill, admin\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"false\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIHNlY3VyZSBkb2N1bWVudA==" + "\n</content>\n"
        + "</record>\n";

    // Doc 2
    records += "<record url=\""
        + ServletUtil.PROTOCOL
        + "junit.localhost"
        + ServletUtil.DOCID
        + "doc2\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe, mary\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyB0aGUgcHVibGljIGRvY3VtZW50Lg==" + "\n</content>\n"
        + "</record>\n";

    // Doc 3
    records += "<record url=\""
        + ServletUtil.PROTOCOL
        + "junit.localhost"
        + ServletUtil.DOCID
        + "doc3\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\" last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe, mary\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIGRvY3VtZW50Lg==" + "\n</content>\n" + "</record>\n";

    String expectedXml = buildExpectedXML(feedType, records);
    takeMultiFeed(expectedXml, "MockRepositoryEventLog9.txt");
  }

  private void takeMultiFeed(String expectedXml, String repository)
      throws Exception {
    String gsaExpectedResponse = GsaFeedConnection.SUCCESS_RESPONSE;
    String gsaActualResponse;

    MockRepositoryEventList mrel = new MockRepositoryEventList(repository);
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);

    MockFeedConnection feedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(feedConnection, "junit");
    DocumentList documentList = qtm.startTraversal();

    Document document = null;
    while ((document = documentList.nextDocument()) != null) {
      dpusher.take(document);
    }
    dpusher.flush();
    String resultXML = feedConnection.getFeed();
    Assert.assertEquals(expectedXml, resultXML);
    gsaActualResponse = dpusher.getGsaResponse();
    Assert.assertEquals(gsaExpectedResponse, gsaActualResponse);
  }

  /**
   * Test whether MockRepostitoryEventList, MockRepository, and DocPusher can
   * handle I18N'd content.
   */
  public void testI18N() throws Exception {
    String[] expectedXml = new String[1];
    String feedType = "incremental";
    String url = ServletUtil.PROTOCOL + "junit.localhost" + ServletUtil.DOCID
        + "doc10";
    String content = "PGh0bWw+w47DscWjw6lyw7HDpcWjw67DtsOxw6XEvMOuxb7DpcWjw67DtsOxPC9odG1sPg==";
    String record = "<record url=\""
        + url
        + "\""
        + " mimetype=\""
        + SpiConstants.DEFAULT_MIMETYPE
        + "\""
        + " last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"contentfile\" content=\"testdata/mocktestdata/i18n.html\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"google:mimetype\" content=\"text/html\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n" + content
        + "\n</content>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog8.txt");
  }

  private class CompressedFeedConnection extends MockFeedConnection {
    @Override
    public String getContentEncodings() {
      return super.getContentEncodings() + ", base64compressed";
    }
  }

  private void takeFeed(String[] expectedXml, String repository)
      throws Exception {
    takeFeed(expectedXml, repository, false);
  }

  private void takeFeed(String[] expectedXml, String repository,
      boolean useCompression) throws Exception {
    String gsaExpectedResponse = GsaFeedConnection.SUCCESS_RESPONSE;
    String gsaActualResponse;

    MockRepositoryEventList mrel = new MockRepositoryEventList(repository);
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);

    MockFeedConnection feedConnection;
    if (useCompression) {
      feedConnection = new CompressedFeedConnection();
    } else {
      feedConnection = new MockFeedConnection();
    }

    DocumentList documentList = qtm.startTraversal();

    int i = 0;
    Document document = null;
    while ((document = documentList.nextDocument()) != null) {
      System.out.println("Test " + i + " output");
      Assert.assertFalse(i == expectedXml.length);
      DocPusher dpusher = new DocPusher(feedConnection, "junit");
      dpusher.take(document);
      dpusher.flush();
      System.out.println("Test " + i + " assertions");
      String resultXML = feedConnection.getFeed();
      gsaActualResponse = dpusher.getGsaResponse();
      Assert.assertEquals(expectedXml[i], resultXML);
      Assert.assertEquals(gsaExpectedResponse, gsaActualResponse);
      System.out.println("Test " + i + " done");
      ++i;
    }
  }

  /**
   * Test basic metadata representation.
   */
  public void testSimpleDoc() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    dpusher.take(document);
    dpusher.flush();
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"ziff\"/>", resultXML);
    assertStringContains("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
        + ServletUtil.DOCID + "doc1\"", resultXML);

  }

  /**
   * Test minimal properties allowed for delete document.
   */
  public void testSimpleDeleteDoc() {
    Map<String, Object> props = getTestDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACTION,
        SpiConstants.ActionType.DELETE.toString());
    Document document = ConnectorTestUtils.createSimpleDocument(props);

    try {
      String resultXML = feedDocument(document);
      assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
                           resultXML);
      assertStringContains("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
                           + ServletUtil.DOCID + "doc1\"", resultXML);
      assertStringContains("action=\"delete\"", resultXML);
      assertStringNotContains("<content encoding=\"base64binary\">", resultXML);
    } catch (Exception e) {
      fail("Full document take");
    }

    // Now document with only DocId and Delete Action.
    Map<String, Object> minProps = new HashMap<String, Object>();
    minProps.put(SpiConstants.PROPNAME_DOCID,
                 props.get(SpiConstants.PROPNAME_DOCID));
    minProps.put(SpiConstants.PROPNAME_ACTION,
                 props.get(SpiConstants.PROPNAME_ACTION));
    document = ConnectorTestUtils.createSimpleDocument(minProps);

    try {
      String resultXML = feedDocument(document);
      assertStringContains("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
                           + ServletUtil.DOCID + "doc1\"", resultXML);
      assertStringContains("action=\"delete\"", resultXML);
      assertStringNotContains("last-modified=", resultXML);
    } catch (Exception e) {
      fail("No last-modified document take");
    }

    // Now include optional last-modified.
    minProps.put(SpiConstants.PROPNAME_LASTMODIFIED,
                 props.get(SpiConstants.PROPNAME_LASTMODIFIED));
    document = ConnectorTestUtils.createSimpleDocument(minProps);

    try {
      String resultXML = feedDocument(document);
      assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
                           resultXML);
      assertStringContains("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
                           + ServletUtil.DOCID + "doc1\"", resultXML);
      assertStringContains("action=\"delete\"", resultXML);
    } catch (Exception e) {
      fail("No content document take");
    }
  }

  /**
   * Test documenturl.
   */
  public void testDocumentUrl() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:documenturl\":\"http://www.sometesturl.com/docid\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    dpusher.take(document);
    dpusher.flush();
    String resultXML = mockFeedConnection.getFeed();

    assertStringNotContains("googleconnector://", resultXML);
    assertStringContains("url=\"http://www.sometesturl.com/docid\"", resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
  }

  /**
   * Test documenturl and searchurl.
   */
  public void testDocumentAndSearchUrl() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:documenturl\":\"http://www.sometesturl.com/docid\""
        + ",\"google:searchurl\":\"http://www.sometesturl.com/searchid\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    try {
      dpusher.take(document);
      fail("Expected exception not thrown.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Illegal Document Property State: Document contains both a "
          + "documentUrl (http://www.sometesturl.com/docid) and a searchUrl "
          + "(http://www.sometesturl.com/searchid)", expected.getMessage());
    }
  }

  /**
   * Test displayurl.
   */
  public void testDisplayUrl() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    dpusher.take(document);
    dpusher.flush();
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
  }

  /**
   * Test special characters in metadata values.
   */
  public void testSpecials() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\""
        // note double escaping in the line below, since this is a json string
        + ",\"special\":\"`~!@#$%^&*()_+-={}[]|\\\\:\\\";'<>?,./\""
        + ",\"japanese\":\"\u5317\u6d77\u9053\""
        + ",\"chinese\":\"\u5317\u4eac\u5e02\"" + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    dpusher.take(document);
    dpusher.flush();
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains("<meta name=\"special\" " +
    // only single escapes here, because this is not a json string
        // but xml-sensitive characters have been replaced with entities
        "content=\"`~!@#$%^&amp;*()_+-={}[]|\\:&quot;;&apos;&lt;>?,./\"/>",
        resultXML);

    assertStringContains("<meta name=\"japanese\" " +
    // only single escapes here, because this is not a json string
        // but xml-sensitive characters have been replaced with entities
        "content=\"\u5317\u6d77\u9053\"/>", resultXML);

    assertStringContains("<meta name=\"chinese\" " +
    // only single escapes here, because this is not a json string
        // but xml-sensitive characters have been replaced with entities
        "content=\"\u5317\u4eac\u5e02\"/>", resultXML);

  }

  /**
   * Test invalid XML characters in metadata values. This is a test
   * that only the control characters we want stripped are stripped.
   */
  public void testInvalidXmlChars() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\""
        // Note double escaping in the lines below, since this is a json string.
        + ",\"control\":\""
        + "\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007"
        + "\\u0008\\u0009\\u000A\\u000B\\u000C\\u000D\\u000E\\u000F"
        + "\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017"
        + "\\u0018\\u0019\\u001A\\u001B\\u001C\\u001D\\u001E\\u001F"
        + "\\uFFFE\\uFFFF"
        + "\"" + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    assertParsedFeedContains(document,
        "<meta name=\"control\" content=\"\t\n\r\"/>");
  }

  /**
   * Test (almost) all XML characters in metadata values. We already
   * tested U+0009, U+000A, and U+000D in testInvalidXmlChars. Here we
   * just test U+0020 to U+FFFD. This tests that we do not need to drop
   * any other characters on the floor for XML parsing.
   */
  public void testValidXmlChars() throws Exception {
    StringBuilder buf = new StringBuilder();
    buf.append("{\"timestamp\":\"10\",\"docid\":\"doc3\""
        + ",\"content\":\"now is the time\""
        // Note double escaping in the lines below, since this is a json string.
        + ",\"control\":\"");
    for (int i = 0x20; i <= 0xFFFD; i++) {
      buf.append("\\u");
      String hex = Integer.toHexString(i);
      for (int j = hex.length(); j < 4; j++) {
        buf.append('0');
      }
      buf.append(hex);
    }
    buf.append("\"" + "}\r\n" + "");
    String json1 = buf.toString();
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    assertParsedFeedContains(document, "<meta name=\"control\" content=\" ");
  }

  /**
   * Pushes the document through {@link DocPusher}, parses the resulting
   * XML feed record to check for invalid characters, and asserts that it
   * contains the given string.
   */
  private void assertParsedFeedContains(Document document, String expected)
      throws Exception {
    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    dpusher.take(document);
    dpusher.flush();
    String resultXML = mockFeedConnection.getFeed();

    // Strip off the DOCTYPE so that the document parses, since we
    // don't have the DTD.
    resultXML = resultXML.substring(resultXML.indexOf("<gsafeed>"));
    assertNotNull("Parse error",
        ServletUtil.parse(resultXML, new FatalErrorHandler(), null));

    // Do this after the XML parsing, since that's the main test.
    assertStringContains(expected, resultXML);
  }

  /**
   * Overrides the production class <code>SAXParseErrorHandler</code>
   * to throw an exception for fatal errors, to make diagnosing test
   * failures easier.
   */
  private static class FatalErrorHandler extends SAXParseErrorHandler {
    @Override
    public void fatalError(SAXParseException e) {
      super.fatalError(e);
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Tests a word document.
   */
  public void testWordDoc() throws Exception {
    final String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"google:mimetype\":\"application/msword\""
        + ",\"contentfile\":\"testdata/mocktestdata/test.doc\""
        + ",\"author\":\"ziff\""
        + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    dpusher.take(document);
    dpusher.flush();
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"ziff\"/>", resultXML);
    assertStringContains("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
        + ServletUtil.DOCID + "doc1\"", resultXML);
  }

  /**
   * Test action.
   */
  public void testAction() throws Exception {
    String defaultActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(defaultActionJson);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    dpusher.take(document);
    dpusher.flush();
    String resultXML = mockFeedConnection.getFeed();

    assertStringNotContains("action=\"add\"", resultXML);

    dpusher = new DocPusher(mockFeedConnection, "junit");
    String addActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"add\""
      + "}\r\n" + "";

    document = JcrDocumentTest.makeDocumentFromJson(addActionJson);
    dpusher.take(document);
    dpusher.flush();
    resultXML = mockFeedConnection.getFeed();

    assertStringContains("action=\"add\"", resultXML);

    dpusher = new DocPusher(mockFeedConnection, "junit");
    String deleteActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"delete\""
      + "}\r\n" + "";

    document = JcrDocumentTest.makeDocumentFromJson(deleteActionJson);
    dpusher.take(document);
    dpusher.flush();
    resultXML = mockFeedConnection.getFeed();

    assertStringContains("action=\"delete\"", resultXML);

    dpusher = new DocPusher(mockFeedConnection, "junit");
    String bogusActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"bogus\""
      + "}\r\n" + "";

    document = JcrDocumentTest.makeDocumentFromJson(bogusActionJson);
    dpusher.take(document);
    dpusher.flush();
    resultXML = mockFeedConnection.getFeed();

    assertStringNotContains("action=", resultXML);
  }

  /**
   * Test ACL related properties.  See the 'content' of the document for details
   * on what is being tested.
   */
  public void testUserAcl() throws Exception {
    String userAcl = "{\"timestamp\":\"20\""
        + ",\"docid\":\"user_acl\""
        + ",\"content\":\"this document has user only ACL\""
        + ",\"acl\":{type:string, value:[joe,mary,admin]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userAcl);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe, mary, admin\"/>", resultXML);
    assertStringNotContains("<meta name=\"acl\"", resultXML);
  }

  public void testUserRoleAcl() throws Exception {
    String userRoleAcl = "{\"timestamp\":\"30\""
        + ",\"docid\":\"user_role_acl\""
        + ",\"content\":\"this document has user with role ACL\""
        + ",\"acl\":{type:string, value:[\"joe=reader\",\"mary=reader,writer\""
        + ",\"admin=owner\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userRoleAcl);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe=reader, mary=reader, mary=writer, admin=owner\"/>",
        resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe\"",
        resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary\"",
        resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "admin\"",
        resultXML);
  }

  public void testUserScopedRoleAcl() throws Exception {
    String userScopedRoleAcl = "{\"timestamp\":\"40\""
        + ",\"docid\":\"user_scoped_role_acl\""
        + ",\"content\":\"this document has scoped user with role ACL\""
        + ",\"acl\":{type:string, value:[\"user:joe=reader\""
        + ",\"user:mary=reader,writer\",\"user:admin=owner\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userScopedRoleAcl);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe=reader, mary=reader, mary=writer, admin=owner\"/>",
        resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe\"",
        resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary\"",
        resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "admin\"",
        resultXML);
  }

  public void testUserGroupAcl() throws Exception {
    String userGroupAcl = "{\"timestamp\":\"50\""
        + ",\"docid\":\"user_group_acl\""
        + ",\"content\":\"this document has scoped user and group ACL\""
        + ",\"acl\":{type:string, value:[\"user:joe\",\"user:mary\""
        + ",\"group:eng\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userGroupAcl);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe, mary\"/>", resultXML);
    assertStringContains("<meta name=\"google:aclgroups\""
        + " content=\"eng\"/>", resultXML);
  }

  public void testUserGroupRoleAcl() throws Exception {
    String userGroupRoleAcl = "{\"timestamp\":\"60\""
        + ",\"docid\":\"user_group_role_acl\""
        + ",\"content\":\"this document has scoped user and group role ACL\""
        + ",\"acl\":{type:string, value:[\"user:joe=reader\""
        + ",\"user:mary=reader,writer\",\"group:eng=reader\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userGroupRoleAcl);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe=reader, mary=reader, mary=writer\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe\"",
        resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary\"",
        resultXML);
    assertStringContains("<meta name=\"google:aclgroups\""
        + " content=\"eng=reader\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "eng\"",
        resultXML);
  }

  public void testUserReaderAcl() throws Exception {
    String userReaderAcl = "{\"timestamp\":\"70\""
        + ",\"docid\":\"user_reader_acl\""
        + ",\"content\":\"this document has one reader\",acl:joe"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userReaderAcl);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe\"/>", resultXML);
  }

  public void testUserOwnerAcl() throws Exception {
    String userOwnerAcl = "{\"timestamp\":\"80\""
        + ",\"docid\":\"user_owner_acl\""
        + ",\"content\":\"this document has one owner\""
        + ",\"acl\":\"joe=owner\""
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userOwnerAcl);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe=owner\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe\"",
        resultXML);
  }

  public void testUserScopedOwnerAcl() throws Exception {
    String userScopedOwnerAcl = "{\"timestamp\":\"90\""
        + ",\"docid\":\"user_scoped_owner_acl\""
        + ",\"content\":\"this document has one owner\""
        + ",\"acl\":\"user:joe=owner\""
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userScopedOwnerAcl);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe=owner\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe\"",
        resultXML);
  }

  public void testSameUserGroupAcl() throws Exception {
    String sameUserGroupAcl = "{\"timestamp\":\"100\""
        + ",\"docid\":\"same_user_group_acl\""
        + ",\"content\":\"this document has a user id and group id the same with different roles\""
        + ",\"acl\":{type:string, value:[\"user:root=owner\",\"group:root=reader,writer\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(sameUserGroupAcl);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"root=owner\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "root\"",
        resultXML);
    assertStringContains("<meta name=\"google:aclgroups\""
        + " content=\"root=reader, root=writer\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "root\"",
        resultXML);
  }

  public void testSomeUserRoleAcl() throws Exception {
    String someUserRoleAcl = "{\"timestamp\":\"110\""
        + ",\"docid\":\"some_user_role_acl\""
        + ",\"content\":\"this document has one user with extra roles\""
        + ",\"acl\":{type:string, value:[\"user:joe\",\"user:mary=reader,writer\",\"group:eng\",\"group:root\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(someUserRoleAcl);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe, mary=reader, mary=writer\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary\"",
        resultXML);
    assertStringContains("<meta name=\"google:aclgroups\""
        + " content=\"eng, root\"/>", resultXML);
  }

  public void testSomeGroupRoleAcl() throws Exception {
    String someGroupRoleAcl = "{\"timestamp\":\"120\""
        + ",\"docid\":\"some_group_role_acl\""
        + ",\"content\":\"this document has one group with extra roles\""
        + ",\"acl\":{type:string, value:[\"user:joe\",\"user:mary\",\"group:eng=reader,writer\",\"group:root\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(someGroupRoleAcl);
    assertStringContains("<meta name=\"google:aclusers\""
        + " content=\"joe, mary\"/>", resultXML);
    assertStringContains("<meta name=\"google:aclgroups\""
        + " content=\"eng=reader, eng=writer, root\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "eng\"",
        resultXML);
  }

  /**
   * Utility method to take the given JSON event string and feed it through a
   * DocPusher and return the resulting XML feed string.
   */
  private String feedJsonEvent(String jsonEventString) throws Exception {
    return feedDocument(JcrDocumentTest.makeDocumentFromJson(jsonEventString));
  }

  /**
   * Utility method to take the given Document and feed it through a
   * DocPusher and return the resulting XML feed string.
   */
  private String feedDocument(Document document) throws Exception {
    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
    dpusher.take(document);
    dpusher.flush();
    return mockFeedConnection.getFeed();
  }

  /**
   * Test separate feed logging.
   */
  private static final String TEST_LOG_FILE = "testdata/FeedLogFile";

  private void deleteOldFile(String path) {
    // Delete the Log file it it exists.
    File logFile = new File(path);
    if (logFile.exists() && !logFile.delete()) {
      fail();
    }
  }

  public void testFeedLogging() throws Exception {
    deleteOldFile(TEST_LOG_FILE);
    FileHandler fh = null;
    try {
      // Setup logging on the DocPusher class.
      fh = new FileHandler(TEST_LOG_FILE, 10000, 1);
      SimpleFormatter sf = new SimpleFormatter();
      fh.setFormatter(sf);
      DocPusher.getFeedLogger().addHandler(fh);
      DocPusher.getFeedLogger().setLevel(Level.FINER);

      // Setup the DocPusher.
      MockFeedConnection mockFeedConnection = new MockFeedConnection();
      DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
      Document document;
      String resultXML;

      // Test incremental feed with content.
      final String jsonIncremental =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\""
              + ",\"content\":\"now is the time\""
              + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
              + "}\r\n" + "";
      document = JcrDocumentTest.makeDocumentFromJson(jsonIncremental);
      dpusher.take(document);
      dpusher.flush();
      resultXML = mockFeedConnection.getFeed();
      assertFeedInLog(resultXML, TEST_LOG_FILE);

      // Test metadata-url feed with content.
      dpusher = new DocPusher(mockFeedConnection, "junit");
      final String jsonMetaAndUrl =
          "{\"timestamp\":\"10\",\"docid\":\"doc2\""
              + ",\"content\":\"now is the time\""
              + ",\"google:searchurl\":\"http://www.sometesturl.com/test\""
              + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
              + "}\r\n" + "";
      document = JcrDocumentTest.makeDocumentFromJson(jsonMetaAndUrl);
      dpusher.take(document);
      dpusher.flush();
      resultXML = mockFeedConnection.getFeed();
      assertFeedInLog(resultXML, TEST_LOG_FILE);

      // Test MSWord Document.
      dpusher = new DocPusher(mockFeedConnection, "junit");
      final String jsonMsWord =
          "{\"timestamp\":\"10\",\"docid\":\"msword\""
              + ",\"google:mimetype\":\"application/msword\""
              + ",\"contentfile\":\"testdata/mocktestdata/test.doc\""
              + ",\"author\":\"ziff\""
              + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
              + "}\r\n" + "";
      document = JcrDocumentTest.makeDocumentFromJson(jsonMsWord);
      dpusher.take(document);
      dpusher.flush();
      resultXML = mockFeedConnection.getFeed();
      assertFeedInLog(resultXML, TEST_LOG_FILE);
    } finally {
      if (fh != null) {
        fh.close();
      }
      deleteOldFile(TEST_LOG_FILE);
    }
  }

  // The feed log doesn't contain the xml feed headers and footers.
  private static final String[] xmlSkip = {
    "<?xml", "<gsafeed>", "<header>", "<datasource>", "<feedtype>", "<group>",
    "</group>", "</header>", "</gsafeed>" };

  // Should we skip this line?
  private boolean shouldSkip(String line) {
    if (line != null) {
      for (String skip : xmlSkip) {
        if (line.startsWith(skip)) {
          return true;
        }
      }
    }
    return false;
  }

  // Read a line from the XML feed, skipping header and footer lines.
  private String xmlReadLine(BufferedReader xmlIn) throws IOException {
    String xmlLine;
    while (shouldSkip(xmlLine = xmlIn.readLine())) {
      // Skip over header and footer lines.
    }
    return xmlLine;
  }

  private void assertFeedInLog(String resultXML, String logFileName)
      throws IOException {
    BufferedReader logIn = new BufferedReader(new FileReader(logFileName));
    try {
      BufferedReader xmlIn = new BufferedReader(new StringReader(resultXML));

      xmlIn.mark(resultXML.length());
      String xmlLine = xmlReadLine(xmlIn);
      String logLine;
      boolean isMatch = false;
      boolean inContent = false;
      while ((logLine = logIn.readLine()) != null) {
        if (logLine.indexOf(xmlLine) >= 0) {
          assertEquals(xmlLine, logLine);
          // We match the first line - start comparing record
          isMatch = true;
          while ((xmlLine = xmlReadLine(xmlIn)) != null) {
            logLine = logIn.readLine();
            if (inContent) {
              inContent = false;
              if (!"...content...".equals(logLine)) {
                isMatch = false;
                break;
              }
            } else {
              if ("...content...".equals(logLine)) {
                // Content outside of <content></content> element?
                isMatch = false;
                break;
              }
              if (xmlLine.indexOf("<content") >= 0) {
                inContent = true;
              }
              if (!xmlLine.equals(logLine)) {
                isMatch = false;
                break;
              }
            }
          }
          if (isMatch) {
            break;
          } else {
            // Need to reset the xmlIn and reload the xmlLine
            xmlIn.reset();
            xmlLine = xmlReadLine(xmlIn);
          }
        } else {
          continue;
        }
      }
      assertTrue("Overall match", isMatch);
    } finally {
      logIn.close();
    }
  }

  private static final String TEST_DIR = "testdata/contextTests/docPusher/";
  private static final String APPLICATION_CONTEXT = "applicationContext.xml";
  private static final String APPLICATION_PROPERTIES =
      "applicationContext.properties";

  /**
   * Test using teed feed file.
   */
  public void testTeedFeed() throws Exception {
    // Setup context where the teedFeedFile is set.
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(TEST_DIR + APPLICATION_CONTEXT,
        "testdata/mocktestdata/");

    // Get properties from file so the teed feed file can be checked.
    String propFileName = TEST_DIR + APPLICATION_PROPERTIES;
    Properties props = new Properties();
    InputStream inStream = new FileInputStream(propFileName);
    try {
      props.load(inStream);
    } finally {
      inStream.close();
    }
    String tffName = (String) props.get(Context.TEED_FEED_FILE_PROPERTY_KEY);
    // Make sure the teed feed file does not exist
    deleteOldFile(tffName);

    // Create the Document.
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
      + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    try {
      // Create DocPusher and send feed.
      MockFeedConnection mockFeedConnection = new MockFeedConnection();
      DocPusher dpusher = new DocPusher(mockFeedConnection, "junit");
      dpusher.take(document);
      dpusher.flush();
      String resultXML = mockFeedConnection.getFeed();
      assertFeedTeed(resultXML, tffName);

      // Now send the feed again and compare with existing teed feed file.
      dpusher = new DocPusher(mockFeedConnection, "junit");
      dpusher.take(document);
      dpusher.flush();
      String secondResultXML = mockFeedConnection.getFeed();
      assertFeedTeed(resultXML + secondResultXML, tffName);
    } finally {
      // Clean up teed feed file.
      (new File(tffName)).deleteOnExit();
    }
  }

  private void assertFeedTeed(String resultXML, String tffName)
      throws IOException {
    BufferedReader tffIn = new BufferedReader(new FileReader(tffName));
    try {
      StringReader xmlIn = new StringReader(resultXML);
      int tffChar;
      int xmlChar;
      while (true) {
        tffChar = tffIn.read();
        xmlChar = xmlIn.read();
        if (tffChar == -1 && xmlChar == -1) {
          return;
        }
        assertEquals(tffChar, xmlChar);
      }
    } finally {
      tffIn.close();
    }
  }

  public static void assertStringContains(String expected, String actual) {
    Assert.assertTrue("Expected:\n" + expected + "\nDid not appear in\n"
        + actual, actual.indexOf(expected) > 0);
  }

  public static void assertStringNotContains(String expected, String actual) {
    Assert.assertTrue("Expected:\n" + expected + "\nDid appear in\n" + actual,
        actual.indexOf(expected) == -1);
  }

  private String buildExpectedXML(String feedType, String record) {
    String rawData = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>"
        + "<!DOCTYPE gsafeed PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"gsafeed.dtd\">\n"
        + "<gsafeed>\n<header>\n<datasource>junit</datasource>\n" + "<feedtype>"
        + feedType + "</feedtype>\n" + "</header>\n" + "<group>\n" + record
        + "</group>\n" + "</gsafeed>\n";
    return rawData;
  }

  private Document getTestDocument() {
    return ConnectorTestUtils.createSimpleDocument(getTestDocumentConfig());
  }

  private Map<String, Object> getTestDocumentConfig() {
    return ConnectorTestUtils.createSimpleDocumentBasicProperties("doc1");
  }

  /**
   * Test that lack of a required metadata field, google:docid, throws
   * a RepositoryDocumentException.
   * Regression test for Issue 108.
   */
  public void testNoDocid() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DOCID, null);

    // Lack of required metadata should throw RepositoryDocumentException.
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Document missing required property "
                   + SpiConstants.PROPNAME_DOCID, expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a required metadata field, google:docid,
   * throws a RepositoryDocumentException.
   * Regression test for Issue 108.
   */
  public void testBadDocid1() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DOCID,
                     IllegalArgumentException.class);

    // Failure to get required metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_DOCID,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a required metadata field, google:docid,
   * throws a RepositoryDocumentException.
   * Regression test for Issue 108.
   */
  public void testBadDocid2() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DOCID, RuntimeException.class);

    // Failure to get required metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_DOCID,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a required metadata field, google:docid,
   * throws a RepositoryDocumentException.
   * Regression test for Issue 108.
   */
  public void testBadDocid3() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DOCID,
                     RepositoryDocumentException.class);

    // Failure to get required metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_DOCID,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a required metadata field, google:docid,
   * propagates a thrown RepositoryException.
   */
  public void testBadDocid4() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DOCID, RepositoryException.class);

    // RepositoryExceptions should be passed through unmolested.
    try {
      feedDocument(doc);
      fail("Expected RepositoryException, but got none.");
    } catch (RepositoryDocumentException e) {
      fail("RepositoryException was replaced with RepositoryDocumentException");
    } catch (RepositoryException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_DOCID,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryException, but got " + t.toString());
    }
  }

  /**
   * Test that lack of a optional metadata field, google:lastmodified,
   * does not throw an Exception, and also does not appear in the feed.
   */
  public void testNoLastModified() throws Exception, Throwable {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_LASTMODIFIED, null);

    // Lack of optional metadata should not throw an Exception.
    try {
      String resultXML = feedDocument(doc);
      assertStringNotContains("last-modified=", resultXML);
    } catch (Throwable t) {
      fail("Missing LastModified threw " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:lastmodified, throws a RepositoryDocumentException.
   */
  public void testBadLastModified2() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_LASTMODIFIED,
                     RuntimeException.class);

    // Failure to get optional metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_LASTMODIFIED,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:lastmodified, throws a RepositoryDocumentException.
   */
  public void testBadLastModified3() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_LASTMODIFIED,
                     RepositoryDocumentException.class);

    // Failure to get optional metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_LASTMODIFIED,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:lastmodified, propagates a thrown RepositoryException.
   */
  public void testBadLastModified4() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_LASTMODIFIED,
                     RepositoryException.class);

    // RepositoryExceptions should be passed through unmolested.
    try {
      feedDocument(doc);
      fail("Expected RepositoryException, but got none.");
    } catch (RepositoryDocumentException e) {
      fail("RepositoryException was replaced with RepositoryDocumentException");
    } catch (RepositoryException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_LASTMODIFIED,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryException, but got " + t.toString());
    }
  }

  /**
   * Test that lack of an arbitrary repository metadata field, foo,
   * does not throw an Exception, and also does not appear in the feed.
   */
  public void testNoFooProperty() throws Exception, Throwable {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty("foo", null);

    // Lack of optional metadata should not throw an Exception.
    try {
      String resultXML = feedDocument(doc);
      assertStringNotContains("\"foo\"", resultXML);
    } catch (Throwable t) {
      fail("Missing foo Property threw " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a repository metadata field,
   * foo, throws a RepositoryDocumentException.
   */
  public void testBadFoo1() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty("foo", RuntimeException.class);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail foo", expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * foo, throws a RepositoryDocumentException.
   */
  public void testBadFoo2() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty("foo", RepositoryDocumentException.class);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail foo", expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * foo, propagates a thrown RepositoryException.
   */
  public void testBadFoo3() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty("foo", RepositoryException.class);

    // RepositoryExceptions should be passed through unmolested.
    try {
      feedDocument(doc);
      fail("Expected RepositoryException, but got none.");
    } catch (RepositoryDocumentException e) {
      fail("RepositoryException was replaced with RepositoryDocumentException");
    } catch (RepositoryException expected) {
      assertEquals("Fail foo", expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to specify a valid URL for
   * google:searchurl, throws a RepositoryDocumentException.
   * The searchUrl is looked at pretty early in DocPusher.take()
   * and handled specially.
   */
  public void testBadSearchUrl1() throws Exception {
    Map<String, Object> config = getTestDocumentConfig();
    config.put(SpiConstants.PROPNAME_SEARCHURL,
               "Not even remotely a \\ valid % URL");
    Document doc = ConnectorTestUtils.createSimpleDocument(config);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertStringContains("malformed", expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:searchurl, throws a RepositoryDocumentException.
   * The searchUrl is looked at pretty early in DocPusher.take()
   * and handled specially.
   */
  public void testBadSearchUrl2() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_SEARCHURL, RuntimeException.class);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_SEARCHURL,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:searchurl, throws a RepositoryDocumentException.
   * The searchUrl is looked at pretty early in DocPusher.take()
   * and handled specially.
   */
  public void testBadSearchUrl3() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_SEARCHURL,
                     RepositoryDocumentException.class);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_SEARCHURL,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:searchurl, propagates a thrown RepositoryException.
   * The searchUrl is looked at pretty early in DocPusher.take()
   * and handled specially.
   */
  public void testBadSearchUrl4() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_SEARCHURL,
                     RepositoryException.class);

    // RepositoryExceptions should be passed through unmolested.
    try {
      feedDocument(doc);
      fail("Expected RepositoryException, but got none.");
    } catch (RepositoryDocumentException e) {
      fail("RepositoryException was replaced with RepositoryDocumentException");
    } catch (RepositoryException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_SEARCHURL,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to specify a Display URL is not fatal.
   * The displayUrl is handled specially in DocPusher.take().
   */
  public void testNoDisplayUrl1() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DISPLAYURL, null);

    // DisplayURL is optional, and may be missing.
    String resultXML = feedDocument(doc);
    assertStringNotContains("\"google:displayurl\"", resultXML);
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:displayurl, throws a RepositoryDocumentException.
   * The displayUrl is handled specially in DocPusher.take().
   */
  public void testBadDisplayUrl2() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DISPLAYURL, RuntimeException.class);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_DISPLAYURL,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:displayurl, throws a RepositoryDocumentException.
   * The displayUrl is handled specially in DocPusher.take().
   */
  public void testBadDisplayUrl3() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DISPLAYURL,
                     RepositoryDocumentException.class);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_DISPLAYURL,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve a optional metadata field,
   * google:displayurl, propagates a thrown RepositoryException.
   * The displayUrl is handled specially in DocPusher.take().
   */
  public void testBadDisplayUrl4() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_DISPLAYURL,
                     RepositoryException.class);

    // RepositoryExceptions should be passed through unmolested.
    try {
      feedDocument(doc);
      fail("Expected RepositoryException, but got none.");
    } catch (RepositoryDocumentException e) {
      fail("RepositoryException was replaced with RepositoryDocumentException");
    } catch (RepositoryException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_DISPLAYURL,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to specify document Content is not fatal.
   * The Content is handled specially in DocPusher.take().
   * The GSA requires some content.  If the Document provides no content,
   * an alternate default content is used - either the document's title,
   * or a single space.
   */
  public void testNoContent() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_CONTENT, null);

    // Content is optional, and may be missing.  Missing content is replaced
    // with the default content, a single space.
    String resultXML = feedDocument(doc);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);
    assertStringContains("IA==", resultXML);  // Base64 encoded space char.
  }

  /**
   * Test that suppling empty content will force alternate content.
   * The Content is handled specially in DocPusher.take().
   * The GSA requires some content.  If the Document provides no content,
   * an alternate default content is used - either the document's title,
   * or a single space.
   */
  public void testEmptyContent() throws Exception {
    Map<String, Object> config = getTestDocumentConfig();
    config.put(SpiConstants.PROPNAME_CONTENT, "");
    Document doc = ConnectorTestUtils.createSimpleDocument(config);

    // Content is optional, and may be missing.  Missing content is replaced
    // with the default content, a single space.
    String resultXML = feedDocument(doc);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);
    assertStringContains("IA==", resultXML);  // Base64 encoded space char.
  }

  /**
   * Test that suppling empty content will force alternate content.
   * The Content is handled specially in DocPusher.take().
   * The GSA requires some content.  If the Document provides no content,
   * an alternate default content is used - either the document's title,
   * or a single space.
   */
  public void testTitleContent() throws Exception {
    Map<String, Object> config = getTestDocumentConfig();
    config.put(SpiConstants.PROPNAME_CONTENT, "");
    config.put(SpiConstants.PROPNAME_TITLE, "title");
    Document doc = ConnectorTestUtils.createSimpleDocument(config);

    // Content is optional, and may be missing.  Missing content is replaced
    // with the default content, the title.
    String resultXML = feedDocument(doc);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);
    assertStringContains("PGh0bWw+PHRpdGxlPnRpdGxlPC90aXRsZT48L2h0bWw+",
                         resultXML);
  }

  /**
   * Test that suppling huge content will force alternate content.
   * The Content is handled specially in DocPusher.take().
   * The GSA can't handle content > 30MB.  If the Document provides larger
   * content, an alternate default content is used - either the document's
   * title, or a single space.
   */
  public void testHugeContent() throws Exception {
    Map<String, Object> config = getTestDocumentConfig();
    config.put(SpiConstants.PROPNAME_CONTENT,
               new HugeInputStream(100 * 1024 * 1024)); // 100MB
    Document doc = ConnectorTestUtils.createSimpleDocument(config);

    // Content is optional, and may be missing.  Missing content is replaced
    // with the default content, a single space.
    String resultXML = feedHugeDocument(doc);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);
    assertStringContains("IA==", resultXML);  // Base64 encoded space char.
  }

  /**
   * Test that suppling huge content will force alternate content.
   * The Content is handled specially in DocPusher.take().
   * The GSA can't handle content > 30MB.  If the Document provides larger
   * content, an alternate default content is used - either the document's
   * title, or a single space.
   */
  public void testHugeContent2() throws Exception {
    Map<String, Object> config = getTestDocumentConfig();
    config.put(SpiConstants.PROPNAME_CONTENT,
               new HugeInputStream(100 * 1024 * 1024)); // 100MB
    config.put(SpiConstants.PROPNAME_TITLE, "title");
    Document doc = ConnectorTestUtils.createSimpleDocument(config);

    // Content is optional, and may be missing.  Missing content is replaced
    // with the default content, the title.
    String resultXML = feedHugeDocument(doc);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);
    assertStringContains("PGh0bWw+PHRpdGxlPnRpdGxlPC90aXRsZT48L2h0bWw+",
                         resultXML);
  }

  /**
   * Utility method to take the given Document with huge content and feed
   * it through a DocPusher and return the resulting XML feed string.
   * In the test context, "huge" is relative.  We set the maxDocSize
   * artificially low to avoid an OutOfMemoryError in the test JVM.
   */
  private String feedHugeDocument(Document document) throws Exception {
    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    FileSizeLimitInfo limit = new FileSizeLimitInfo();
    limit.setMaxDocumentSize(1024 * 1024); // 1 MB
    DocPusher dpusher = new DocPusher(mockFeedConnection, "junit", limit);
    dpusher.take(document);
    dpusher.flush();
    return mockFeedConnection.getFeed();
  }


  /**
   * Test that failure to retrieve the document content property,
   * google:content, throws a RepositoryDocumentException.
   * The content stream is handled specially in DocPusher.take().
   */
  public void testBadContent2() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_CONTENT, RuntimeException.class);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_CONTENT,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve the document content property,
   * google:content, throws a RepositoryDocumentException.
   * The content is handled specially in DocPusher.take().
   */
  public void testBadContent3() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_CONTENT,
                     RepositoryDocumentException.class);

    // Failure to get metadata should throw RepositoryDocumentException
    try {
      feedDocument(doc);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_CONTENT,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that failure to retrieve the document content property,
   * google:content, propagates a thrown RepositoryException.
   * The content is handled specially in DocPusher.take().
   */
  public void testBadContent4() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_CONTENT,
                     RepositoryException.class);

    // RepositoryExceptions should be passed through unmolested.
    try {
      feedDocument(doc);
      fail("Expected RepositoryException, but got none.");
    } catch (RepositoryDocumentException e) {
      fail("RepositoryException was replaced with RepositoryDocumentException");
    } catch (RepositoryException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_CONTENT,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryException, but got " + t.toString());
    }
  }

  /**
   * Test that if DocPusher gets a read error on the document content stream,
   * it throws a RepositoryDocumentException.
   */
  public void testContentReadError() throws Exception {
    Map<String, Object> props = getTestDocumentConfig();
    props.put(SpiConstants.PROPNAME_CONTENT, new BadInputStream());
    Document document = ConnectorTestUtils.createSimpleDocument(props);

    // IO error on content should throw RepositoryDocumentException.
    try {
      feedDocument(document);
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      assertEquals("I/O error reading data: This stream is unreadable",
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that if DocPusher gets a FeedException, it propagates
   * unimpeded.
   */
  public void testBadFeed1() throws Exception {
    Document document = getTestDocument();
    try {
      FeedConnection badFeedConnection = new BadFeedConnection1();
      DocPusher dpusher = new DocPusher(badFeedConnection, "junit");
      dpusher.take(document);
      dpusher.flush();
      fail("Expected FeedException, but got none.");
    } catch (FeedException expected) {
      assertEquals("Anorexic FeedConnection", expected.getMessage());
    } catch (Throwable t) {
      fail("Expected FeedException, but got " + t.toString());
    }
  }

  /**
   * Test that if DocPusher gets a bad response from the
   * feed, it throws a PushException.
   */
  public void testBadFeed2() throws Exception {
    Document document = getTestDocument();
    try {
      FeedConnection badFeedConnection = new BadFeedConnection2();
      DocPusher dpusher = new DocPusher(badFeedConnection, "junit");
      dpusher.take(document);
      dpusher.flush();
      fail("Expected PushException, but got none.");
    } catch (PushException expected) {
      assertEquals("Bulimic FeedConnection", expected.getMessage());
    } catch (Throwable t) {
      fail("Expected PushException, but got " + t.toString());
    }
  }

  /**
   * A FeedConnection that throws FeedException when fed.
   */
  private static class BadFeedConnection1 implements FeedConnection {
    public String sendData(FeedData feedData)
        throws FeedException {
      throw new FeedException("Anorexic FeedConnection");
    }
    public boolean isBacklogged() {
      return false;
    }
    public String getContentEncodings() {
      return "base64binary";
    }
  }

  /**
   * A FeedConnection that returns a bad response.
   */
  private static class BadFeedConnection2 extends MockFeedConnection {
    @Override
    public String sendData(FeedData feedData)
        throws RepositoryException {
      super.sendData(feedData);
      return "Bulimic FeedConnection";
    }
    @Override
    public boolean isBacklogged() {
      return false;
    }
    @Override
    public String getContentEncodings() {
      return "base64binary";
    }
  }

  /**
   * An InputStream that throws IOExceptions when read.
   */
  private static class BadInputStream extends InputStream {
    // Make it look like there is something to read.
    @Override
    public int available() {
      return 69;
    }
    // Override read methods, always throwing IOException.
    @Override
    public int read() throws IOException {
      throw new IOException("This stream is unreadable");
    }
    @Override
    public int read(byte[] b) throws IOException {
      throw new IOException("This stream is unreadable");
    }
    @Override
    public int read(byte[] b, int o, int l) throws IOException {
      throw new IOException("This stream is unreadable");
    }
  }

  /**
   * An InputStream that returns huge amounts of data.
   */
  private static class HugeInputStream extends InputStream {
    private final long hugeLength;
    private long currentLength;

    public HugeInputStream(long length) {
      this.hugeLength = length;
    }

    // Make it look like there is something to read.
    @Override
    public int available() {
      return 69;
    }

    // Don't support mark & reset.
    @Override
    public boolean markSupported() {
      return false;
    }

    // Override read methods, returning at least hugeLength bytes of crap.
    @Override
    public int read() {
      if (currentLength < hugeLength) {
        currentLength++;
        return 'x';
      } else {
        return -1;
      }
    }

    @Override
    public int read(byte[] b, int o, int l) {
      if (currentLength < hugeLength) {
        Arrays.fill(b, o, o + l, (byte)'z');
        currentLength += l;
        return l;
      } else {
        return -1;
      }
    }
  }

  /**
   * A Document with Properties that fail.
   */
  private static class BadDocument implements Document {

    // The wrapped Document.
    private final Document baseDocument;

    // Map of bad Properties.
    private final HashMap<String, Class<? extends Throwable>> badProperties;

    /**
     * Constructor wraps an existing Document.
     */
    public BadDocument(Document document) {
      baseDocument = document;
      badProperties = new HashMap<String, Class<? extends Throwable>>();
    }

    /**
     * Specify a property to fail and how to fail it.
     *
     * @param propertyName name of a Property.
     * @param exception Class indicating which Exception to throw if accessed.
     *        If null, findProperty() will return null rather than throw an
     *        Exception.
     */
    public void failProperty(String propertyName,
                             Class<? extends Throwable> exception) {
      if (exception != null &&
          !(RuntimeException.class.isAssignableFrom(exception) ||
            RepositoryException.class.isAssignableFrom(exception))) {
        throw new IllegalArgumentException("Wrong kind of Exception");
      }
      badProperties.put(propertyName, exception);
    }

    /**
     * Specify a properties to fail and how to fail them.
     *
     * @param propertyNames an Array of Property names.
     * @param exception Class indicating which Exception to throw if accessed.
     *        If null, findProperty() will return null rather than throw an
     *        Exception.
     */
    public void failProperties(String[] propertyNames,
                               Class<? extends Throwable> exception) {
      for (int i = 0; i < propertyNames.length; i++) {
        failProperty(propertyNames[i], exception);
      }
    }

    /**
     * Return the Set of Property names available for this Document.
     */
    public Set<String> getPropertyNames() throws RepositoryException {
      // Get all the property names of the base Document.
      HashSet<String> names =
          new HashSet<String>(baseDocument.getPropertyNames());
      // Add my additional bad properties.
      names.addAll(badProperties.keySet());
      // Return the union.
      return names;
    }

    /**
     * Find the requested Property.  If the requested property
     * is one of our specified fail Properties, then fail in
     * the appropriate manner.
     *
     * @param propertyName a Property name.
     */
    public Property findProperty(String propertyName)
        throws RepositoryException {
      if (badProperties.containsKey(propertyName)) {
        Class<? extends Throwable> throwable = badProperties.get(propertyName);
        if (throwable == null) {
          return null;
        }

        Class<?> [] parameterTypes = { String.class };
        String[] parameters = { "Fail " + propertyName };
        Constructor<? extends Throwable> constructor;
        try {
          constructor = throwable.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
          throw new IllegalArgumentException(e.getMessage());
        }

        if (RuntimeException.class.isAssignableFrom(throwable)) {
          // RuntimeExceptions don't have to be declared.
          try {
            throw (RuntimeException) constructor.newInstance(
                (Object[])parameters);
          } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage());
          } catch (InstantiationException e) {
            throw new IllegalArgumentException(e.getMessage());
          } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getMessage());
          }
        } else if (RepositoryException.class.isAssignableFrom(throwable)) {
          try {
            throw (RepositoryException) constructor.newInstance(
                (Object[])parameters);
          } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage());
          } catch (InstantiationException e) {
            throw new IllegalArgumentException(e.getMessage());
          } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getMessage());
          }
        }
      }
      return baseDocument.findProperty(propertyName);
    }
  }
}
