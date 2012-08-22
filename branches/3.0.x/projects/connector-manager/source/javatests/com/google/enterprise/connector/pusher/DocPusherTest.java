// Copyright 2006 Google Inc.
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
import com.google.enterprise.connector.pusher.Pusher.PusherStatus;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SecureDocument;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.traversal.FileSizeLimitInfo;
import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.SAXParseErrorHandler;
import com.google.enterprise.connector.util.SystemClock;
import com.google.enterprise.connector.util.UniqueIdGenerator;
import com.google.enterprise.connector.util.XmlParseUtil;
import com.google.enterprise.connector.util.filter.DocumentFilterChain;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;
import com.google.enterprise.connector.util.filter.ModifyPropertyFilter;

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
import java.util.Collections;
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
  private FileSizeLimitInfo fsli;
  private DocumentFilterChain dfc;
  private String contentUrlPrefix;

  private FeedConnection aclsUnsupportedFeedConnection
      = new MockFeedConnection() {
    public boolean supportsInheritedAcls() {
      return false;
    }
  };

  @Override
  protected void setUp() throws Exception {
    // MockFeedConnection also prints the XML it "sends".
    System.out.println("\nTest Case: " + getName());

    // Set artificially low limits as test env only has 64MB of heap space.
    fsli = new FileSizeLimitInfo();
    fsli.setMaxFeedSize(1024 * 1024);
    fsli.setMaxDocumentSize(1024 * 1024);

    // Set up an empty filter document chain.
    dfc = new DocumentFilterChain();

    // A distinct contentUrlPrefix.
    contentUrlPrefix = "http://contentUrlPrefix";

    // We're comparing date strings here, so we need a fixed time zone.
    Value.setFeedTimeZone("GMT");

    // To ease comarisons against expected output, generate non-unique FeedId.
    XmlFeed.setUniqueIdGenerator(new MockIdGenerator());
  }

  @Override
  public void tearDown() {
    // Reset the default time zone.
    Value.setFeedTimeZone("");
  }

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
   * Test that Take works for a URL/metadata feed when google:docid is missing
   * but google:searchurl is provided.
   */
  public void testTakeUrlMetaNoDocid() throws Exception {
    String feedType = "metadata-and-url";
    String record = "<record url=\"http://www.sometesturl.com/searchurl\""
        + " mimetype=\"text/plain\""
        + " last-modified=\"Thu, 01 Jan 1970 01:00:00 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "<meta name=\"google:mimetype\" content=\"text/plain\"/>\n"
        + "<meta name=\"google:searchurl\" content=\"http://www.sometesturl.com/searchurl\"/>\n"
        + "</metadata>\n" + "</record>\n";

    String expectedXml = buildExpectedXML(feedType, record);

    Map<String, Object> props = getTestDocumentConfig();
    props.put(SpiConstants.PROPNAME_SEARCHURL,
        "http://www.sometesturl.com/searchurl");
    props.remove(SpiConstants.PROPNAME_DOCID);
    props.remove(SpiConstants.PROPNAME_DISPLAYURL);
    Document document = ConnectorTestUtils.createSimpleDocument(props);

    MockFeedConnection feedConnection = new MockFeedConnection();
    DocPusher dpusher =
        new DocPusher(feedConnection, "junit", fsli, dfc, null);
    dpusher.take(document, null);
    dpusher.flush();
    assertEquals(expectedXml, feedConnection.getFeed());
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

  /** Builds googleconnector URL with the supplied docid. */
  private String googleConnectorUrl(String docid) {
    return "\"" + ServletUtil.PROTOCOL + "junit.localhost"
        + ServletUtil.DOCID + docid + "\"";
  }

  /**
   * Test Take for a content feed.
   */
  public void testTakeContent() throws Exception {
    String[] expectedXml = new String[1];
    String feedType = "incremental";
    String record = "<record url=" + googleConnectorUrl("doc1")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\">\n"
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
    String record = "<record url=" + googleConnectorUrl("doc10")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Tue, 15 Nov 1994 12:45:26 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"contentfile\" content=\"testdata/mocktestdata/i18n.html\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"Tue, 15 Nov 1994 12:45:26 GMT\"/>\n"
        + "<meta name=\"google:mimetype\" content=\"text/html\"/>\n"
        + "<meta name=\"jcr:lastModified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64compressed\">\n"
        + "eJyzySjJzbE73Hd449HFh1cWHd54eCmQse7wNhDryJ7D647uQ4jY6INVAwBbqCBF"
        + "\n</content>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog8.txt", true, true);
  }

  /**
   * Test Take for isPublic.
   */
  public void testTakeIsPublic() throws Exception {
    String[] expectedXml = new String[4];
    String feedType = "incremental";

    // case 1: "google:ispublic":"false"
    String record = "<record url=" + googleConnectorUrl("users")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:00 GMT\""
        + " authmethod=\"httpbasic\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"mary\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"fred\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"mark\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"bill\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"admin\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"false\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIHNlY3VyZSBkb2N1bWVudA==" + "\n</content>\n"
        + "</record>\n";
    expectedXml[0] = buildExpectedXML(feedType, record);

    // case 2: "google:ispublic":"true"
    record = "<record url=" + googleConnectorUrl("doc1")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"mary\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyB0aGUgcHVibGljIGRvY3VtZW50Lg==" + "\n</content>\n"
        + "</record>\n";
    expectedXml[1] = buildExpectedXML(feedType, record);

    // case 3: "google:ispublic":"public"; the value "public" is illegal value.
    // note also: MockRepositoryEventLog7.txt has a "" in the acl property,
    // which null-handling should drop out, leaving just "joe, mary"
    record = "<record url=" + googleConnectorUrl("doc2")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"mary\"/>\n"
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
    String records = "<record url=" + googleConnectorUrl("doc1")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:00 GMT\""
        + " authmethod=\"httpbasic\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"mary\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"fred\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"mark\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"bill\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"admin\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"false\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIHNlY3VyZSBkb2N1bWVudA==" + "\n</content>\n"
        + "</record>\n";

    // Doc 2
    records += "<record url=" + googleConnectorUrl("doc2")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"mary\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyB0aGUgcHVibGljIGRvY3VtZW50Lg==" + "\n</content>\n"
        + "</record>\n";

    // Doc 3
    records += "<record url=" + googleConnectorUrl("doc3")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<metadata>\n"
        + "<meta name=\"google:aclusers\" content=\"joe\"/>\n"
        + "<meta name=\"google:aclusers\" content=\"mary\"/>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIGRvY3VtZW50Lg==" + "\n</content>\n" + "</record>\n";

    String expectedXml = buildExpectedXML(feedType, records);
    takeMultiFeed(expectedXml, "MockRepositoryEventLog9.txt", false);
  }

  /**
   * Test for multiple document feed on smart GSA (supportsInheritedAcls).
   */
  public void testMultiRecordFeedSmartGsa() throws Exception {
    String feedType = "incremental";

    // Doc 1.
    String records = "<record url=" + googleConnectorUrl("doc1")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:00 GMT\""
        + " authmethod=\"httpbasic\">\n"
        + "<acl>\n"
        + "<principal scope=\"user\" access=\"permit\">joe</principal>\n"
        + "<principal scope=\"user\" access=\"permit\">mary</principal>\n"
        + "<principal scope=\"user\" access=\"permit\">fred</principal>\n"
        + "<principal scope=\"user\" access=\"permit\">mark</principal>\n"
        + "<principal scope=\"user\" access=\"permit\">bill</principal>\n"
        + "<principal scope=\"user\" access=\"permit\">admin</principal>\n"
        + "</acl>\n"
        + "<metadata>\n"
        + "<meta name=\"google:ispublic\" content=\"false\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIHNlY3VyZSBkb2N1bWVudA==" + "\n</content>\n"
        + "</record>\n";

    // Doc 2
    records += "<record url=" + googleConnectorUrl("doc2")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<acl>\n"
        + "<principal scope=\"user\" access=\"permit\">joe</principal>\n"
        + "<principal scope=\"user\" access=\"permit\">mary</principal>\n"
        + "</acl>\n"
        + "<metadata>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyB0aGUgcHVibGljIGRvY3VtZW50Lg==" + "\n</content>\n"
        + "</record>\n";

    // Doc 3
    records += "<record url=" + googleConnectorUrl("doc3")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
        + " last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\">\n"
        + "<acl>\n"
        + "<principal scope=\"user\" access=\"permit\">joe</principal>\n"
        + "<principal scope=\"user\" access=\"permit\">mary</principal>\n"
        + "</acl>\n"
        + "<metadata>\n"
        + "<meta name=\"google:ispublic\" content=\"true\"/>\n"
        + "<meta name=\"google:lastmodified\" content=\"1970-01-01\"/>\n"
        + "</metadata>\n" + "<content encoding=\"base64binary\">\n"
        + "VGhpcyBpcyBhIGRvY3VtZW50Lg==" + "\n</content>\n" + "</record>\n";

    String expectedXml = buildExpectedXML(feedType, records);
    takeMultiFeed(expectedXml, "MockRepositoryEventLog9.txt", true);
  }

  private void takeMultiFeed(String expectedXml, String repository,
      final boolean supportsInheritedAcls) throws Exception {
    String gsaExpectedResponse = GsaFeedConnection.SUCCESS_RESPONSE;
    String gsaActualResponse;

    MockRepositoryEventList mrel = new MockRepositoryEventList(repository);
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);

    MockFeedConnection feedConnection = new MockFeedConnection() {
      public boolean supportsInheritedAcls() {
        return supportsInheritedAcls;
      }
    };

    DocPusher dpusher = new DocPusher(feedConnection, "junit", fsli, dfc, null);
    DocumentList documentList = qtm.startTraversal();

    Document document = null;
    while ((document = documentList.nextDocument()) != null) {
      assertEquals(PusherStatus.OK, dpusher.take(document, null));
    }
    dpusher.flush();
    String resultXML = feedConnection.getFeed();
    assertEquals(expectedXml, resultXML);
    gsaActualResponse = dpusher.getGsaResponse();
    assertEquals(gsaExpectedResponse, gsaActualResponse);
  }

  /**
   * Test whether MockRepostitoryEventList, MockRepository, and DocPusher can
   * handle I18N'd content.
   */
  public void testI18N() throws Exception {
    String[] expectedXml = new String[1];
    String feedType = "incremental";
    String content = "PGh0bWw+w47DscWjw6lyw7HDpcWjw67DtsOxw6XEvMOuxb7DpcWjw67DtsOxPC9odG1sPg==";
    String record = "<record url=" + googleConnectorUrl("doc10")
        + " mimetype=\"" + SpiConstants.DEFAULT_MIMETYPE + "\""
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

  private void takeFeed(String[] expectedXml, String repository)
      throws Exception {
    takeFeed(expectedXml, repository, false, false);
  }

  private void takeFeed(String[] expectedXml, String repository,
      final boolean useCompression, final boolean supportsInheritedAcls)
      throws Exception {
    String gsaExpectedResponse = GsaFeedConnection.SUCCESS_RESPONSE;
    String gsaActualResponse;

    MockRepositoryEventList mrel = new MockRepositoryEventList(repository);
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);

    MockFeedConnection feedConnection = new MockFeedConnection() {
      public String getContentEncodings() {
        if (useCompression) {
          return super.getContentEncodings() + ", base64compressed";
        } else {
          return super.getContentEncodings();
        }
      }

      public boolean supportsInheritedAcls() {
        return supportsInheritedAcls;
      }
    };

    DocumentList documentList = qtm.startTraversal();

    int i = 0;
    Document document = null;
    while ((document = documentList.nextDocument()) != null) {
      System.out.println("Test " + i + " output");
      assertFalse(i == expectedXml.length);
      DocPusher dpusher =
          new DocPusher(feedConnection, "junit", fsli, dfc, null);
      assertEquals(PusherStatus.OK, dpusher.take(document, null));
      dpusher.flush();
      System.out.println("Test " + i + " assertions");
      String resultXML = feedConnection.getFeed();
      gsaActualResponse = dpusher.getGsaResponse();
      assertEquals(expectedXml[i], resultXML);
      assertEquals(gsaExpectedResponse, gsaActualResponse);
      System.out.println("Test " + i + " done\n");
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
    String resultXML = feedJsonEvent(json1);

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"ziff\"/>", resultXML);
    assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
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
      assertStringContains("last-modified=\"Thu, 01 Jan 1970 01:00:00 GMT\"",
                           resultXML);
      assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
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
      assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
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
      assertStringContains("last-modified=\"Thu, 01 Jan 1970 01:00:00 GMT\"",
                           resultXML);
      assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
      assertStringContains("action=\"delete\"", resultXML);
    } catch (Exception e) {
      fail("No content document take");
    }
  }

  /**
   * Tests that a principal with minimal attributes is written to the
   * feed correctly.
   */
  public void testMinimalPrincipal() throws Exception {
    testPrincipal(
        new Principal(SpiConstants.PrincipalType.UNKNOWN, null, "John Doe"),
        "<principal"
        + " scope=\"user\" access=\"permit\">John Doe</principal>");
  }

  /**
   * Tests that a principal with no domain is written to the feed correctly.
   */
  public void testUnqualifiedPrincipal() throws Exception {
    testPrincipal(new Principal(SpiConstants.PrincipalType.UNQUALIFIED, null,
            "John Doe"),
        "<principal principal-type=\"unqualified\""
        + " scope=\"user\" access=\"permit\">John Doe</principal>");
  }

  /**
   * Tests that the principal namespace is written to the feed correctly.
   */
  public void testPrincipalNamespace() throws Exception {
    testPrincipal(
        new Principal(SpiConstants.PrincipalType.UNKNOWN, "Unknown Persons",
            "John Doe"),
        "<principal namespace=\"Unknown Persons\""
        + " scope=\"user\" access=\"permit\">John Doe</principal>");
  }

  /**
   * Tests that an empty namespace is ignored.
   */
  public void testPrincipalEmptyNamespace() throws Exception {
    testPrincipal(
        new Principal(SpiConstants.PrincipalType.UNKNOWN, "",
            "John Doe"),
        "<principal"
        + " scope=\"user\" access=\"permit\">John Doe</principal>");
  }

  /**
   * Tests that case-insensitivity is written to the feed correctly.
   */
  public void testCaseInsensitivePrincipal() throws Exception {
    testPrincipal(
        new Principal(SpiConstants.PrincipalType.UNKNOWN, null, "John Doe",
            SpiConstants.CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE),
        "<principal case-sensitivity-type=\"everything-case-insensitive\""
        + " scope=\"user\" access=\"permit\">John Doe</principal>");
  }

  /** Tests that a given principal is written to the feed correctly. */
  public void testPrincipal(Principal principal, String expected)
      throws Exception {
    Map<String, Object> props = getTestDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLUSERS, principal);
    Document document = ConnectorTestUtils.createSimpleDocument(props);

    String resultXML = feedDocument(document, true);
    assertStringContains(expected, resultXML);
    assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
  }

  /**
   * Test DocumentFilter is invoked.
   */
  public void testDocumentFilter() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    ModifyPropertyFilter mpf = new ModifyPropertyFilter();
    mpf.setPropertyName("author");
    mpf.setPattern("ziff");
    mpf.setReplacement("johnson");
    mpf.setOverwrite(true);

    String resultXML = feedDocument(document, mpf, false);

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"johnson\"/>",
        resultXML);
    assertStringNotContains("ziff", resultXML);
    assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
  }

  /**
   * Test multi-valued metadata representation.  The multiple values
   * should be fed as individual &lt;meta&gt; elements.
   * Regression test for Issue 220.
   */
  public void testMultiValueMetaDoc() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\""
        + ",\"author\":{type:string, value:[ziff,bjohnson,jlacey]}"
        + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"ziff\"/>",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"bjohnson\"/>",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"jlacey\"/>",
        resultXML);
    assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
  }

  /**
   * Test embedded commas in metadata get fed unmolested.
   * Regression test for Issue 220.
   */
  public void testEmbeddedCommaMetaDoc() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\""
        + ",\"author\":\"Google, Inc.\""
        + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("<meta name=\"author\" content=\"Google, Inc.\"/>",
        resultXML);
    assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
  }

  /**
   * Test default searchurl.
   */
  public void testDefaultSearchUrl() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:searchurl\":\"http://www.sometesturl.com/docid\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringNotContains(ServletUtil.PROTOCOL, resultXML);
    assertStringContains("url=\"http://www.sometesturl.com/docid\"", resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>metadata-and-url</feedtype>", resultXML);
    assertStringNotContains("<content encoding=\"base64binary\">", resultXML);
  }

  /**
   * Test searchurl with feed type set.
   */
  public void testSearchUrl() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:searchurl\":\"http://www.sometesturl.com/docid\""
        + ",\"google:feedtype\":\"WEB\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    String json2 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:searchurl\":\"http://www.sometesturl.com/docid\""
        + ",\"google:feedtype\":\"CONTENT\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    String json3 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:searchurl\":\"http://www.sometesturl.com/docid\""
        + ",\"google:feedtype\":\"CONTENTURL\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";

    // Web feed with searchurl.
    String resultXML = feedJsonEvent(json1);

    assertStringNotContains(ServletUtil.PROTOCOL, resultXML);
    assertStringContains("url=\"http://www.sometesturl.com/docid\"", resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>metadata-and-url</feedtype>", resultXML);
    assertStringNotContains("<content encoding=\"base64binary\">", resultXML);

    // Content feed with searchurl.
    resultXML = feedJsonEvent(json2);

    assertStringNotContains(ServletUtil.PROTOCOL, resultXML);
    assertStringContains("url=\"http://www.sometesturl.com/docid\"", resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>incremental</feedtype>", resultXML);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);

    // ContentURL feed with searchurl.
    resultXML = feedJsonEvent(json3);

    assertStringNotContains(ServletUtil.PROTOCOL, resultXML);
    assertStringContains("url=\"http://www.sometesturl.com/docid\"", resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>metadata-and-url</feedtype>", resultXML);
    assertStringNotContains("<content encoding=\"base64binary\">", resultXML);
  }

  /**
   * Test without searchurl with feed type set.
   */
  public void testNoSearchUrl() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:feedtype\":\"CONTENT\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    String json2 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:feedtype\":\"WEB\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + "}\r\n" + "";
    String json3 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"author\":\"ziff\""
      + ",\"google:feedtype\":\"CONTENT\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + "}\r\n" + "";

    // Content feed without searchurl.
    String resultXML = feedJsonEvent(json1);

    assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>incremental</feedtype>", resultXML);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);

    // Web feed without searchurl.
    resultXML = feedJsonEvent(json2);

    assertStringNotContains(ServletUtil.PROTOCOL, resultXML);
    assertStringContains("url=\"http://www.sometesturl.com/test\"", resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>metadata-and-url</feedtype>", resultXML);
    assertStringNotContains("<content encoding=\"base64binary\">", resultXML);

    // Content feed without searchurl and without content.
    resultXML = feedJsonEvent(json3);

    assertStringContains("url=" + googleConnectorUrl("doc1"), resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>incremental</feedtype>", resultXML);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);
    assertStringContains("IA==", resultXML);
  }

  /**
   * Test contentUrl.
   */
  public void testContentUrl() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"author\":\"ziff\""
      + ",\"google:feedtype\":\"CONTENTURL\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + "}\r\n" + "";

    String json2 = "{\"timestamp\":\"10\",\"docid\":\"doc1&evil/value\""
      + ",\"author\":\"ziff\""
      + ",\"google:feedtype\":\"CONTENTURL\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + "}\r\n" + "";

    // ContentURL feed.
    String resultXML = feedJsonEvent(json1);
    assertStringContains("url=\"" + contentUrlPrefix + "?"
        + ServletUtil.XMLTAG_CONNECTOR_NAME + "=junit&amp;"
        + ServletUtil.QUERY_PARAM_DOCID + "=doc1\"", resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>metadata-and-url</feedtype>", resultXML);
    assertStringNotContains("<content encoding=\"base64binary\">", resultXML);

    // ContentURL feed - docid has special chars (Issue 214 regression).
    resultXML = feedJsonEvent(json2);
    assertStringContains("url=\"" + contentUrlPrefix + "?"
        + ServletUtil.XMLTAG_CONNECTOR_NAME + "=junit&amp;"
        + ServletUtil.QUERY_PARAM_DOCID + "=doc1%26evil%2Fvalue\"", resultXML);
    assertStringContains("displayurl=\"http://www.sometesturl.com/test\"",
        resultXML);
    assertStringContains("<feedtype>metadata-and-url</feedtype>", resultXML);
    assertStringNotContains("<content encoding=\"base64binary\">", resultXML);

    // Test unset contentUrlPrefix.
    contentUrlPrefix = null;
    try {
      resultXML = feedJsonEvent(json1);
      fail("Expected RepositoryDocumentException");
    } catch (RepositoryDocumentException expected) {
      assertEquals("contentUrlPrefix must not be null or empty",
                   expected.getMessage());
    }

    // Test empty contentUrlPrefix.
    contentUrlPrefix = "";
    try {
      resultXML = feedJsonEvent(json1);
      fail("Expected RepositoryDocumentException");
    } catch (RepositoryDocumentException expected) {
      assertEquals("contentUrlPrefix must not be null or empty",
                   expected.getMessage());
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
    String resultXML = feedJsonEvent(json1);

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
    String resultXML = feedJsonEvent(json1);

    assertStringContains("<meta name=\"special\" " +
    // only single escapes here, because this is not a json string
        // but xml-sensitive characters have been replaced with entities
        "content=\"`~!@#$%^&amp;*()_+-={}[]|\\:&quot;;&#39;&lt;>?,./\"/>",
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
    String resultXML = feedDocument(document);

    // Strip off the DOCTYPE so that the document parses, since we
    // don't have the DTD.
    resultXML = resultXML.substring(resultXML.indexOf("<gsafeed>"));
    assertNotNull("Parse error",
        XmlParseUtil.parse(resultXML, new FatalErrorHandler(), null));

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
    String resultXML = feedJsonEvent(json1);

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
    String resultXML = feedJsonEvent(defaultActionJson);

    assertStringNotContains("action=\"add\"", resultXML);

    String addActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"add\""
      + "}\r\n" + "";
    resultXML = feedJsonEvent(addActionJson);

    assertStringContains("action=\"add\"", resultXML);

    String deleteActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"delete\""
      + "}\r\n" + "";
    resultXML = feedJsonEvent(deleteActionJson);

    assertStringContains("action=\"delete\"", resultXML);

    String bogusActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"bogus\""
      + "}\r\n" + "";

    resultXML = feedJsonEvent(bogusActionJson);

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
    String resultXML = feedJsonEvent(userAcl, false);
    assertStringContains("<record url=" + googleConnectorUrl("user_acl"),
                         resultXML);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\" content=\"joe\"/>",
                         resultXML);
    assertStringContains("<meta name=\"google:aclusers\" content=\"mary\"/>",
                         resultXML);
    assertStringContains("<meta name=\"google:aclusers\" content=\"admin\"/>",
                         resultXML);
    assertStringNotContains("<meta name=\"acl\"", resultXML);
    assertStringNotContains("<acl url=", resultXML);
    assertStringNotContains("<principal", resultXML);
  }

  /**
   * Test ACL related properties on GSA with advanced ACL support, separate
   * acl elements should be created.
   */
  public void testUserAclSmartGsa() throws Exception {
    String userAcl = "{\"timestamp\":\"20\""
        + ",\"docid\":\"user_acl\""
        + ",\"content\":\"this document has user only ACL\""
        + ",\"acl\":{type:string, value:[joe,mary,admin]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userAcl, true);
    assertStringContains("<acl", resultXML);
    assertStringContains("<principal scope=\"user\" access=\"permit\">"
                         + "joe</principal>", resultXML);
    assertStringContains("<principal scope=\"user\" access=\"permit\">"
                         + "mary</principal>", resultXML);
    assertStringContains("<principal scope=\"user\" access=\"permit\">"
                         + "admin</principal>", resultXML);
    assertStringContains("</acl>", resultXML);
    assertStringContains("<record url=" + googleConnectorUrl("user_acl"),
                         resultXML);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);

    assertStringNotContains("<meta name=\"google:aclusers\" content=\"joe\"/>",
                            resultXML);
    assertStringNotContains("<meta name=\"google:aclusers\" content=\"mary\"/>",
                            resultXML);
    assertStringNotContains("<meta name=\"google:aclusers\" content=\"admin\"/>",
                            resultXML);
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
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"joe=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary=writer\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"admin=owner\"/>", resultXML);
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
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"joe=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary=writer\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"admin=owner\"/>", resultXML);
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
    String resultXML = feedJsonEvent(userGroupAcl, false);
    assertStringContains("<record url=" + googleConnectorUrl("user_group_acl"),
                         resultXML);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringContains("<meta name=\"google:aclusers\" content=\"joe\"/>",
                         resultXML);
    assertStringContains("<meta name=\"google:aclusers\" content=\"mary\"/>",
                         resultXML);
    assertStringContains("<meta name=\"google:aclgroups\" content=\"eng\"/>",
                         resultXML);
    assertStringNotContains("<acl url=", resultXML);
    assertStringNotContains("<principal", resultXML);
  }

  public void testUserGroupAclSmartGsa() throws Exception {
    String userGroupAcl = "{\"timestamp\":\"50\""
        + ",\"docid\":\"user_group_acl\""
        + ",\"content\":\"this document has scoped user and group ACL\""
        + ",\"acl\":{type:string, value:[\"user:joe\",\"user:mary\""
        + ",\"group:eng\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(userGroupAcl, true);

    assertStringContains("<acl", resultXML);
    assertStringContains("<principal scope=\"user\" access=\"permit\">"
                         + "joe</principal>", resultXML);
    assertStringContains("<principal scope=\"user\" access=\"permit\">"
                         + "mary</principal>", resultXML);
    assertStringContains("<principal scope=\"group\" access=\"permit\">"
                         + "eng</principal>", resultXML);
    assertStringContains("</acl>", resultXML);
    assertStringContains("<record url=" + googleConnectorUrl("user_group_acl"),
                         resultXML);
    assertStringContains("authmethod=\"httpbasic\"", resultXML);
    assertStringNotContains("<meta name=\"google:aclusers\" content=\"joe\"/>",
                            resultXML);
    assertStringNotContains("<meta name=\"google:aclusers\" content=\"mary\"/>",
                            resultXML);
    assertStringNotContains("<meta name=\"google:aclgroups\" content=\"eng\"/>",
                            resultXML);
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
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"joe=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary=writer\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe\"",
        resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary\"",
        resultXML);
    assertStringContains(
        "<meta name=\"google:aclgroups\" content=\"eng=reader\"/>", resultXML);
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
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"root=owner\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "root\"",
        resultXML);
    assertStringContains(
        "<meta name=\"google:aclgroups\" content=\"root=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclgroups\" content=\"root=writer\"/>", resultXML);
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
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"joe\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary=writer\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary\"",
        resultXML);
    assertStringContains(
        "<meta name=\"google:aclgroups\" content=\"eng\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclgroups\" content=\"root\"/>", resultXML);
  }

  public void testSomeGroupRoleAcl() throws Exception {
    String someGroupRoleAcl = "{\"timestamp\":\"120\""
        + ",\"docid\":\"some_group_role_acl\""
        + ",\"content\":\"this document has one group with extra roles\""
        + ",\"acl\":{type:string, value:[\"user:joe\",\"user:mary\",\"group:eng=reader,writer\",\"group:root\"]}"
        + ",\"google:ispublic\":\"false\"}";
    String resultXML = feedJsonEvent(someGroupRoleAcl);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"joe\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclusers\" content=\"mary\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclgroups\" content=\"eng=reader\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclgroups\" content=\"eng=writer\"/>", resultXML);
    assertStringContains(
        "<meta name=\"google:aclgroups\" content=\"root\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"" + SpiConstants.USER_ROLES_PROPNAME_PREFIX + "eng\"",
        resultXML);
  }

  /**
   * Utility method to take the given JSON event string and feed it through a
   * DocPusher and return the resulting XML feed string.
   */
  private String feedJsonEvent(String jsonEventString) throws Exception {
    return feedJsonEvent(jsonEventString, false);
  }

  /**
   * Utility method to take the given JSON event string and feed it through a
   * DocPusher and return the resulting XML feed string.
   */
  private String feedJsonEvent(String jsonEventString,
      boolean supportsInheritedAcls) throws Exception {
    return feedDocument(JcrDocumentTest.makeDocumentFromJson(jsonEventString),
                        supportsInheritedAcls);
  }

  /**
   * Utility method to take the given Document and feed it through a
   * DocPusher and return the resulting XML feed string.
   */
  private String feedDocument(Document document) throws Exception {
    return feedDocument(document, dfc, false);
  }

  /**
   * Utility method to take the given Document and feed it through a
   * DocPusher and return the resulting XML feed string.
   */
  private String feedDocument(Document document, boolean supportsInheritedAcls)
      throws Exception {
    return feedDocument(document, dfc, supportsInheritedAcls);
  }

  /**
   * Utility method to take the given Document and DocumentFilterFactory
   * and feed it through a DocPusher and return the resulting XML feed
   * string.
   */
  private String feedDocument(Document document, DocumentFilterFactory dff,
      final boolean supportsInheritedAcls) throws Exception {
    MockFeedConnection mockFeedConnection = new MockFeedConnection() {
      public boolean supportsInheritedAcls() {
        return supportsInheritedAcls;
      }
    };

    DocPusher dpusher =
        new DocPusher(mockFeedConnection, "junit", fsli, dff, contentUrlPrefix);
    assertEquals(PusherStatus.OK, dpusher.take(document, null));
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

  /** Tests that feeding the given document logs it to the feed log. */
  private void testFeedLogging(Document document) throws Exception {
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
      DocPusher dpusher =
          new DocPusher(mockFeedConnection, "junit", fsli, dfc, null);
      assertEquals(PusherStatus.OK, dpusher.take(document, null));
      dpusher.flush();
      String resultXML = mockFeedConnection.getFeed();
      assertFeedInLog(resultXML, TEST_LOG_FILE);
    } finally {
      if (fh != null) {
        fh.close();
      }
      deleteOldFile(TEST_LOG_FILE);
    }
  }

  /** Tests feed logging with a document created from a JSON string. */
  private void testFeedLogging(String jsonDocument) throws Exception {
    testFeedLogging(JcrDocumentTest.makeDocumentFromJson(jsonDocument));
  }

  public void testFeedLoggingContentFeed() throws Exception {
    final String jsonIncremental =
        "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\""
        + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
        + "}\r\n" + "";
    testFeedLogging(jsonIncremental);
  }

  /** Tests a metadata-and-URL feed with content. */
  public void testFeedLoggingMetadataAndUrlFeed() throws Exception {
    final String jsonMetaAndUrl =
        "{\"timestamp\":\"10\",\"docid\":\"doc2\""
        + ",\"content\":\"now is the time\""
        + ",\"google:searchurl\":\"http://www.sometesturl.com/test\""
        + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
        + "}\r\n" + "";
    testFeedLogging(jsonMetaAndUrl);
  }

  public void testFeedLoggingWordDocument() throws Exception {
    final String jsonMsWord =
        "{\"timestamp\":\"10\",\"docid\":\"msword\""
        + ",\"google:mimetype\":\"application/msword\""
        + ",\"contentfile\":\"testdata/mocktestdata/test.doc\""
        + ",\"author\":\"ziff\""
        + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    testFeedLogging(jsonMsWord);
  }

  public void testFeedLoggingAcl() throws Exception {
    testFeedLogging(SecureDocument.createAcl("acl1", null));
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
      DocPusher dpusher =
          new DocPusher(mockFeedConnection, "junit", fsli, dfc, null);
      assertEquals(PusherStatus.OK, dpusher.take(document, null));
      dpusher.flush();
      String resultXML = mockFeedConnection.getFeed();
      assertFeedTeed(resultXML, tffName);

      // Now send the feed again and compare with existing teed feed file.
      dpusher = new DocPusher(mockFeedConnection, "junit", fsli, dfc, null);
      assertEquals(PusherStatus.OK, dpusher.take(document, null));
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
    assertTrue("Expected:\n" + expected + "\nDid not appear in\n"
        + actual, actual.indexOf(expected) > 0);
  }

  public static void assertStringNotContains(String expected, String actual) {
    assertTrue("Expected:\n" + expected + "\nDid appear in\n" + actual,
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
   * Test Doc with lock unspecified.
   */
  public void testLockUnspecified() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";

    String resultXML = feedJsonEvent(json1);

    assertStringNotContains("lock=\"true\"",
        resultXML);
    // The GSA treats attribute as false if not present in the feed.
    // We prefer to not specify it if the value is false (explicitly or
    // implicitly) to minimize risk in a patch
    // TODO(Max): change this to explicit in the trunk
    assertStringNotContains("lock=\"false\"",
        resultXML);
    assertStringNotContains("lock=",
        resultXML);
    assertStringNotContains("meta name=\"" + SpiConstants.PROPNAME_LOCK + "\"",
        resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK,
        resultXML);
  }

  /**
   * Test Doc with lock specified false.
   */
  public void testLockExplicitFalse() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + ",\"google:lock\":\"false\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringNotContains("lock=\"true\"",
        resultXML);
    assertStringNotContains("lock=\"false\"",
        resultXML);
    assertStringNotContains("lock=",
        resultXML);
    assertStringNotContains("meta name=\"" + SpiConstants.PROPNAME_LOCK + "\"",
        resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK,
        resultXML);
  }

  /**
   * Test Doc with lock specified true.
   */
  public void testLockExplicitTrue() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + ",\"google:lock\":\"true\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringContains("lock=\"true\"",
        resultXML);
    assertStringNotContains("lock=\"false\"",
        resultXML);
    assertStringNotContains("meta name=\"" + SpiConstants.PROPNAME_LOCK + "\"",
        resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK,
        resultXML);
  }

  /**
   * Test Doc with lock specified with illegal value.
   */
  public void testLockIllegalValue() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + ",\"google:lock\":\"xyzzy\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    // should be silently treated as true
    assertStringContains("lock=\"true\"",
        resultXML);
    assertStringNotContains("lock=\"false\"",
        resultXML);
    assertStringNotContains("meta name=\"" + SpiConstants.PROPNAME_LOCK + "\"",
        resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK,
        resultXML);
  }

  /**
   * Test Doc with lock specified with empty value.
   */
  public void testLockEmptyValue() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + ",\"google:lock\":\"\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    // should be silently treated as true
    assertStringContains("lock=\"true\"",
        resultXML);
    assertStringNotContains("lock=\"false\"",
        resultXML);
    assertStringNotContains("meta name=\"" + SpiConstants.PROPNAME_LOCK + "\"",
        resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK,
        resultXML);
  }

  /** Test doc with pagerank unspecified. */
  public void testPagerankUnspecified() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringNotContains("pagerank", resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK, resultXML);
  }

  /** Test doc with valid pagerank. */
  public void testValidPagerank() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + ",\"google:pagerank\":\"97\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringContains("pagerank=\"97\"", resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK, resultXML);
  }

  /** Test doc with invalid pagerank. */
  public void testInvalidPagerank() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + ",\"google:pagerank\":\"abcdef\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringContains("pagerank=\"abcdef\"", resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK, resultXML);
  }

  /** Test doc with empty pagerank. */
  public void testEmptyPagerank() throws Exception {
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
        + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
        + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
        + ",\"google:pagerank\":\"\""
        + "}\r\n" + "";
    String resultXML = feedJsonEvent(json1);

    assertStringNotContains("pagerank", resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_LOCK, resultXML);
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
      assertEquals("Document has neither property "
          + SpiConstants.PROPNAME_DOCID + " nor property "
          + SpiConstants.PROPNAME_SEARCHURL, expected.getMessage());
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
      assertEquals("Document has neither property "
          + SpiConstants.PROPNAME_DOCID + " nor property "
          + SpiConstants.PROPNAME_SEARCHURL, expected.getMessage());
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
   * Test that failure to retrieve a optional metadata field,
   * foo, throws a SkippedDocumentException.
   */
  public void testBadFoo4() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty("foo", SkippedDocumentException.class);

    // Failure to get metadata should throw SkippedDocumentException
    try {
      feedDocument(doc);
      fail("Expected SkippedDocumentException, but got none.");
    } catch (SkippedDocumentException expected) {
      assertEquals("Fail foo", expected.getMessage());
    } catch (Throwable t) {
      fail("Expected SkippedDocumentException, but got " + t.toString());
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
   * Base64 encoding of:
   * <!DOCTYPE html><html><head><meta charset="utf-8"/><title>title</title></html>
   */
  private static String HTML_TITLE_ONLY_BASE64 =
      "PCFET0NUWVBFIGh0bWw+PGh0bWw+PGhlYWQ+PG1ldGEgY2hhcnNl"
      + "dD0idXRmLTgiLz48dGl0bGU+dGl0bGU8L3RpdGxlPjwvaHRtbD4=";

  /**
   * Base64 encoding of a tiny, empty PDF document with a Title property.
   */
  private static String PDF_TITLE_ONLY_BASE64 =
      "JVBERi0xLjEKMSAwIG9iago8PC9UeXBlIC9DYXRhbG9nCi9QYWdlcyAyIDAgUgo+PgplbmR"
      + "vYmoKMiAwIG9iago8PC9UeXBlIC9QYWdlcwovS2lkcyBbMyAwIFJdCi9Db3VudCAxCj4+"
      + "CmVuZG9iagozIDAgb2JqCjw8L1R5cGUgL1BhZ2UKL1BhcmVudCAyIDAgUgovTWVkaWFCb"
      + "3ggWzAgMCA3MiA3Ml0KPj4KZW5kb2JqCjQgMCBvYmoKPDwvVGl0bGUgPEZFRkYwMDc0MD"
      + "A2OTAwNzQwMDZDMDA2NT4KPj4KZW5kb2JqCnhyZWYKMCA1CjAwMDAwMDAwMDAgNjU1MzU"
      + "gZg0KMDAwMDAwMDAwOSAwMDAwMCBuDQowMDAwMDAwMDU3IDAwMDAwIG4NCjAwMDAwMDAx"
      + "MTMgMDAwMDAgbg0KMDAwMDAwMDE4MSAwMDAwMCBuDQp0cmFpbGVyCjw8L1NpemUgNQovU"
      + "m9vdCAxIDAgUgovSW5mbyA0IDAgUgo+PgpzdGFydHhyZWYKMjM1CiUlRU9GCg==";

  /**
   * Base64 encoding of a tiny, empty PDF document with no Title property.
   */
  private static String PDF_NO_TITLE_BASE64 =
      "JVBERi0xLjEKMSAwIG9iago8PC9UeXBlIC9DYXRhbG9nCi9QYWdlcyAyIDAgUgo+PgplbmR"
      + "vYmoKMiAwIG9iago8PC9UeXBlIC9QYWdlcwovS2lkcyBbMyAwIFJdCi9Db3VudCAxCj4+"
      + "CmVuZG9iagozIDAgb2JqCjw8L1R5cGUgL1BhZ2UKL1BhcmVudCAyIDAgUgovTWVkaWFCb"
      + "3ggWzAgMCA3MiA3Ml0KPj4KZW5kb2JqCjQgMCBvYmoKPDwKPj4KZW5kb2JqCnhyZWYKMC"
      + "A1CjAwMDAwMDAwMDAgNjU1MzUgZg0KMDAwMDAwMDAwOSAwMDAwMCBuDQowMDAwMDAwMDU"
      + "3IDAwMDAwIG4NCjAwMDAwMDAxMTMgMDAwMDAgbg0KMDAwMDAwMDE4MSAwMDAwMCBuDQp0"
      + "cmFpbGVyCjw8L1NpemUgNQovUm9vdCAxIDAgUgovSW5mbyA0IDAgUgo+PgpzdGFydHhyZ"
      + "WYKMjAyCiUlRU9GCg==";

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
    assertStringContains(HTML_TITLE_ONLY_BASE64, resultXML);
  }

  /**
   * Test that suppling empty content will force alternate PDF content.
   * Alternate content for PDF must still be a PDF, or the GSA drops the
   * document with a "Conversion Error".  If the google:title property
   * is present, create an empty PDF document with a Title entry in the
   * Document Information Dictionary.
   */
  public void testPdfTitleContent() throws Exception {
    Map<String, Object> config = getTestDocumentConfig();
    config.put(SpiConstants.PROPNAME_CONTENT, "");
    config.put(SpiConstants.PROPNAME_TITLE, "title");
    config.put(SpiConstants.PROPNAME_MIMETYPE, "application/pdf");
    Document doc = ConnectorTestUtils.createSimpleDocument(config);

    // Content is optional, and may be missing.  Missing content is replaced
    // with the default content, the title.
    String resultXML = feedDocument(doc);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);
    assertStringContains(PDF_TITLE_ONLY_BASE64, resultXML);
  }

  /**
   * Test that suppling empty content will force alternate PDF content.
   * Alternate content for PDF must still be a PDF, or the GSA drops the
   * document with a "Conversion Error".  If there is no google:title property
   * property, create an empty PDF document.
   */
  public void testPdfEmptyContent() throws Exception {
    Map<String, Object> config = getTestDocumentConfig();
    config.put(SpiConstants.PROPNAME_CONTENT, "");
    config.put(SpiConstants.PROPNAME_MIMETYPE, "application/pdf");
    Document doc = ConnectorTestUtils.createSimpleDocument(config);

    // Content is optional, and may be missing.  Missing content is replaced
    // with the default content, the title.
    String resultXML = feedDocument(doc);
    assertStringContains("<content encoding=\"base64binary\">", resultXML);
    assertStringContains(PDF_NO_TITLE_BASE64, resultXML);
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
    assertStringContains(HTML_TITLE_ONLY_BASE64, resultXML);
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
    limit.setMaxFeedSize(64 * 1024); // 64 KB
    DocPusher dpusher =
        new DocPusher(mockFeedConnection, "junit", limit, dfc, null);
    assertEquals(PusherStatus.OK, dpusher.take(document, null));
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
   * Test that failure to retrieve the document content property,
   * google:content, throws a SkippedDocumentException.
   * The content is handled specially in DocPusher.take().
   */
  public void testBadContent5() throws Exception {
    BadDocument doc = new BadDocument(getTestDocument());
    doc.failProperty(SpiConstants.PROPNAME_CONTENT,
                     SkippedDocumentException.class);

    // Failure to get metadata should throw SkippedDocumentException
    try {
      feedDocument(doc);
      fail("Expected SkippedDocumentException, but got none.");
    } catch (SkippedDocumentException expected) {
      assertEquals("Fail " + SpiConstants.PROPNAME_CONTENT,
                   expected.getMessage());
    } catch (Throwable t) {
      fail("Expected SkippedDocumentException, but got " + t.toString());
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
      DocPusher dpusher =
          new DocPusher(badFeedConnection, "junit", fsli, dfc, null);
      dpusher.take(document, null);
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
      DocPusher dpusher =
          new DocPusher(badFeedConnection, "junit", fsli, dfc, null);
      dpusher.take(document, null);
      dpusher.flush();
      fail("Expected PushException, but got none.");
    } catch (PushException expected) {
      assertEquals("Bulimic FeedConnection", expected.getMessage());
    } catch (Throwable t) {
      fail("Expected PushException, but got " + t.toString());
    }
  }

  /**
   * Test that if DocPusher appears to be backlogged transmitting feeds,
   * (feeds backed up on this end of the FeedConnection), the DocPusher
   * returns an indication to stop feeding docs.
   */
  public void testProximalFeedBacklog() throws Exception {
    Document document = getTestDocument();
    // Force 1 document per feed by setting a tiny feed size.
    FileSizeLimitInfo limit = new FileSizeLimitInfo();
    limit.setMaxFeedSize(32);
    limit.setMaxDocumentSize(64 * 1024);

    // SlowFeedConnection waits 5 secs before transmission, allowing feeds
    // to back up on this end of the connection.
    SlowFeedConnection slowFeedConnection = new SlowFeedConnection();
    DocPusher dpusher =
        new DocPusher(slowFeedConnection, "junit", limit, dfc, null);
    int count;
    PusherStatus status = PusherStatus.OK;
    for (count = 0; count < 30; count++) {
      status = dpusher.take(document, null);
      if (status != PusherStatus.OK)
        break;
    }
    assertTrue(count >= 10); // Min. 10 feeds must be waiting to be a backlog.
    assertTrue(count < 30);  // But we should have detected the backlog by now.
    assertEquals(PusherStatus.LOCAL_FEED_BACKLOG, status);
    assertEquals(PusherStatus.LOCAL_FEED_BACKLOG, dpusher.getPusherStatus());
    // dpusher.flush();      // Let the sleeping threads lie.
  }

  /**
   * Test that if Feed sink appears to be backlogged processing submitted
   * feeds (feeds backed up on the other end of the FeedConnection),
   * the DocPusher returns an indication to stop feeding docs.
   */
  public void testDistalFeedBacklog() throws Exception {
    Document document = getTestDocument();
    BacklogFeedConnection backlogFeedConnection = new BacklogFeedConnection();
    // Force 1 document per feed by setting a tiny feed size.
    FileSizeLimitInfo limit = new FileSizeLimitInfo();
    limit.setMaxFeedSize(32);
    limit.setMaxDocumentSize(64 * 1024);

    DocPusher dpusher =
        new DocPusher(backlogFeedConnection, "junit", limit, dfc, null);
    assertEquals(PusherStatus.OK, dpusher.take(document, null));
    backlogFeedConnection.setBacklogged(true);
    assertEquals(PusherStatus.GSA_FEED_BACKLOG, dpusher.take(document, null));
    assertEquals(PusherStatus.GSA_FEED_BACKLOG, dpusher.getPusherStatus());
    dpusher.flush();
  }

  /**
   * Test that if DocPusher is not low on memory, it returns an indication
   * to continue feeding docs.
   */
  public void testNotLowMemory() throws Exception {
    Document document = getTestDocument();
    FeedConnection feedConnection = new MockFeedConnection();
    FileSizeLimitInfo limit = new FileSizeLimitInfo();
    limit.setMaxFeedSize(32);
    limit.setMaxDocumentSize(64 * 1024);

    Runtime rt = Runtime.getRuntime();
    rt.gc();

    // If plenty of memory is available, DocPusher should indicate it is
    // OK to feed more (return true).
    DocPusher dpusher =
        new DocPusher(feedConnection, "junit", limit, dfc, null);
    assertEquals(PusherStatus.OK, dpusher.take(document, null));
    dpusher.flush();
  }

  /**
   * Test that if DocPusher runs low on memory, it returns an indication
   * to stop feeding docs.
   */
  public void testLowMemory() throws Exception {
    FeedConnection feedConnection = new MockFeedConnection();

    Runtime rt = Runtime.getRuntime();
    rt.gc();
    long memAvailable = rt.maxMemory() - (rt.totalMemory() - rt.freeMemory());

    FileSizeLimitInfo limit = new FileSizeLimitInfo();
    // With these limits, the largest possible feed will be about 7/12 of
    // available memory - there should not be room for a second one.
    limit.setMaxDocumentSize(memAvailable/4);
    limit.setMaxFeedSize(memAvailable/3);

    DocPusher dpusher =
        new DocPusher(feedConnection, "junit", limit, dfc, null);
    Map<String, Object> config = getTestDocumentConfig();
    config.put(SpiConstants.PROPNAME_CONTENT,
               new HugeInputStream(limit.maxDocumentSize() - 10));
    Document bigDocument = ConnectorTestUtils.createSimpleDocument(config);
    assertEquals(PusherStatus.LOW_MEMORY, dpusher.take(bigDocument, null));
    dpusher.flush();
    assertFalse(feedConnection.isBacklogged());
  }

  /**
   * Tests ACL document with inherit-from URL.
   */
  public void testAclInheritFromUrl() throws Exception {
    String parentUrl = "http://foo/parent-doc";
    Map<String, Object> props = getTestAclDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM, parentUrl);
    testAclInheritFrom(props, parentUrl);
    testDocumentAclInheritFrom(props, parentUrl);
  }

  /**
   * Tests ACL document with inherit-from docid and FeedType.
   */
  public void testAclInheritFromDocidAndFeedType() throws Exception {
    String parentId = "parent-doc";
    Map<String, Object> props = getTestAclDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, parentId);
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE,
              SpiConstants.FeedType.CONTENTURL.toString());
    String parentUrl = contentUrlPrefix + "?"
        + ServletUtil.XMLTAG_CONNECTOR_NAME + "=junit&amp;"
        + ServletUtil.QUERY_PARAM_DOCID + "=" + parentId;
    testAclInheritFrom(props, parentUrl);
    testDocumentAclInheritFrom(props, parentUrl);
  }

  /**
   * Tests ACL document with inherit-from docid.
   */
  public void testAclInheritFromDocid() throws Exception {
    String parentId = "parent-doc";
    Map<String, Object> props = getTestAclDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, parentId);
    String parentUrl = ServletUtil.PROTOCOL + "junit.localhost"
        + ServletUtil.DOCID + parentId;
    testAclInheritFrom(props, parentUrl);
    testDocumentAclInheritFrom(props, parentUrl);
  }

  /**
   * Tests ACL document with inherit-from URL overrides
   * inherit-from docid.
   */
  public void testAclInheritFromUrlAndDocid() throws Exception {
    String parentUrl = "http://foo/parent-doc";
    String parentId = "step-parent-doc";
    Map<String, Object> props = getTestAclDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM, parentUrl);
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, parentId);
    testAclInheritFrom(props, parentUrl);
    testDocumentAclInheritFrom(props, parentUrl);
  }

  /** Returns a document config with some ACL properties. */
  private Map<String, Object> getTestAclDocumentConfig() {
    Map<String, Object> props = getTestDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLINHERITANCETYPE,
        SpiConstants.AclInheritanceType.PARENT_OVERRIDES.toString());
    props.put(SpiConstants.PROPNAME_ACLUSERS, "John Doe");
    props.put(SpiConstants.PROPNAME_ACLUSERS, "John Doe");
    props.put(SpiConstants.PROPNAME_ACLDENYUSERS, "Jason Wang");
    props.put(SpiConstants.PROPNAME_ACLGROUPS, "Engineering");
    return props;
  }

  /**
   * Tests ACL inheritance for ACL documents.
   */
  private void testAclInheritFrom(Map<String, Object> props,
      String expectedParentUrl) throws Exception {
    // Copy the properties so we can make internal changes.
    props = new HashMap<String, Object>(props);

    props.put(SpiConstants.PROPNAME_DOCUMENTTYPE,
        SpiConstants.DocumentType.ACL.toString());
    props.put(SpiConstants.PROPNAME_FEEDTYPE,
        SpiConstants.FeedType.CONTENT.toString());

    Document document = ConnectorTestUtils.createSimpleDocument(props);
    String resultXML = feedDocument(document, true);

    assertStringContains("<acl url=" + googleConnectorUrl("doc1")
        + " inheritance-type=\"parent-overrides\" inherit-from=\""
        + expectedParentUrl + "\">", resultXML);
    assertStringContains(
        "<principal scope=\"user\" access=\"permit\">John Doe</principal>",
        resultXML);
    assertStringContains(
        "<principal scope=\"user\" access=\"deny\">Jason Wang</principal>",
        resultXML);
    assertStringContains(
        "<principal scope=\"group\" access=\"permit\">Engineering</principal>",
        resultXML);
    assertStringContains("</acl>", resultXML);
    assertStringNotContains("<record", resultXML);
  }

  /**
   * Tests ACL inheritance for regular documents that include ACLs.
   */
  private void testDocumentAclInheritFrom(Map<String, Object> props,
      String expectedParentUrl) throws Exception {
    // Copy the properties so we can make internal changes.
    props = new HashMap<String, Object>(props);

    props.put(SpiConstants.PROPNAME_FEEDTYPE,
        SpiConstants.FeedType.CONTENT.toString());

    Document document = ConnectorTestUtils.createSimpleDocument(props);
    String resultXML = feedDocument(document, true);

    // This should be an acl feed record, followed by a regular feed record.
    assertStringContains("<acl inheritance-type=\"parent-overrides\" "
        + "inherit-from=\"" + expectedParentUrl + "\">", resultXML);
    assertStringContains(
        "<principal scope=\"user\" access=\"permit\">John Doe</principal>",
        resultXML);
    assertStringContains(
        "<principal scope=\"user\" access=\"deny\">Jason Wang</principal>",
        resultXML);
    assertStringContains(
        "<principal scope=\"group\" access=\"permit\">Engineering</principal>",
        resultXML);
    assertStringContains("</acl>", resultXML);

    assertStringContains("<record url=" + googleConnectorUrl("doc1"), resultXML);

    assertStringNotContains(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID,
                            resultXML);
    assertStringNotContains(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE,
                            resultXML);

    assertStringNotContains("<meta name=\"google:aclinheritfrom\" content=\""
        + expectedParentUrl + "\"/>", resultXML);
    assertStringNotContains(
        "<meta name=\"google:aclinheritancetype\" content=\"parent-overrides\"/>",
        resultXML);
    assertStringNotContains(
        "<meta name=\"google:acldenyusers\" content=\"Jason Wang\"/>",
        resultXML);
    assertStringNotContains(
        "<meta name=\"google:aclusers\" content=\"John Doe\"/>",
        resultXML);
    assertStringNotContains(
        "<meta name=\"google:aclgroups\" content=\"Engineering\"/>",
        resultXML);
 }

  public void testAclSmartGsa() throws Exception {
    String parentUrl = "http://foo/parent-doc";
    Map<String, Object> props = getTestAclDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM, parentUrl);
    props.put(SpiConstants.PROPNAME_FEEDTYPE,
        SpiConstants.FeedType.CONTENT.toString());
    Document document = ConnectorTestUtils.createSimpleDocument(props);
    dfc = new DocumentFilterChain(Collections.singletonList(
        new AclDocumentFilter(new MockFeedConnection())));
    String resultXML = feedDocument(document);
    assertStringContains("parent-doc", resultXML);
    assertStringNotContains("httpbasic", resultXML);
  }

  public void testAclNoDumbDown() throws Exception {
    Map<String, Object> props = getTestAclDocumentConfig();
    props.put(SpiConstants.PROPNAME_FEEDTYPE,
        SpiConstants.FeedType.CONTENT.toString());
    Document document = ConnectorTestUtils.createSimpleDocument(props);
    dfc = new DocumentFilterChain(Collections.singletonList(
        new AclDocumentFilter(aclsUnsupportedFeedConnection)));
    String resultXML = feedDocument(document);
    assertStringNotContains("httpbasic", resultXML);
  }

  public void testAclDumbDown() throws Exception {
    String parentUrl = "http://foo/parent-doc";
    Map<String, Object> props = getTestAclDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM, parentUrl);
    props.put(SpiConstants.PROPNAME_FEEDTYPE,
        SpiConstants.FeedType.CONTENT.toString());
    Document document = ConnectorTestUtils.createSimpleDocument(props);
    dfc = new DocumentFilterChain(Collections.singletonList(
        new AclDocumentFilter(aclsUnsupportedFeedConnection)));
    String resultXML = feedDocument(document);
    assertStringNotContains("parent-doc", resultXML);
    assertStringContains("httpbasic", resultXML);
  }

  public void testAclSkip() throws Exception {
    String parentUrl = "http://foo/parent-doc";
    Map<String, Object> props = getTestAclDocumentConfig();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM, parentUrl);
    props.put(SpiConstants.PROPNAME_FEEDTYPE,
        SpiConstants.FeedType.CONTENT.toString());
    props.put(SpiConstants.PROPNAME_DOCUMENTTYPE,
        SpiConstants.DocumentType.ACL.toString());
    Document document = ConnectorTestUtils.createSimpleDocument(props);
    dfc = new DocumentFilterChain(Collections.singletonList(
        new AclDocumentFilter(aclsUnsupportedFeedConnection)));
    try {
      feedDocument(document);
      fail("Excepted SkippedDocumentException");
    } catch (SkippedDocumentException ex) {
    }
  }

  private static class MockIdGenerator implements UniqueIdGenerator {
    // Return a predictable non-unique ID to ease expected output comparisons.
    public String uniqueId() {
      return "test";
    }
  }

  /**
   * A FeedConnection that throws FeedException when fed.
   */
  private static class BadFeedConnection1 extends MockFeedConnection {
    @Override
    public String sendData(FeedData feedData) throws FeedException {
      throw new FeedException("Anorexic FeedConnection");
    }
  }

  /**
   * A FeedConnection that returns a bad response when fed.
   */
  private static class BadFeedConnection2 extends MockFeedConnection {
    @Override
    public String sendData(FeedData feedData)
        throws FeedException, RepositoryException {
      super.sendData(feedData);
      return "Bulimic FeedConnection";
    }
  }

  /**
   * A slow FeedConnection.
   */
  private static class SlowFeedConnection extends MockFeedConnection {
    static Clock clock = new SystemClock(); // TODO: rewrite this to use a mock clock.
    static long doneTime = clock.getTimeMillis() + 5000;
    @Override
    public String sendData(FeedData feedData)
        throws FeedException, RepositoryException {
      try {
        while (clock.getTimeMillis() < doneTime) {
          Thread.sleep(250);
        }
      } catch (InterruptedException ie) {
        // Stop waiting.
      }
      return super.sendData(feedData);
    }
  }

  /**
   * A FeedConnection that can be backlogged.
   */
  private class BacklogFeedConnection extends MockFeedConnection {
    private boolean backlogged = false;
    public void setBacklogged(boolean backlogged) {
      this.backlogged = backlogged;
    }
    @Override
    public boolean isBacklogged() {
      return backlogged;
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
