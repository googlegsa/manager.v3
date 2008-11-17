// Copyright (C) 2006-2008 Google Inc.
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
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
        + "</metadata>\n" + "<content encoding=\"base64binary\">"
        + "bm93IGlzIHRoZSB0aW1l" + "</content>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog6.txt");
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
        + "</metadata>\n" + "<content encoding=\"base64binary\">"
        + "VGhpcyBpcyBhIHNlY3VyZSBkb2N1bWVudA==" + "</content>\n"
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
        + "</metadata>\n" + "<content encoding=\"base64binary\">"
        + "VGhpcyBpcyB0aGUgcHVibGljIGRvY3VtZW50Lg==" + "</content>\n"
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
        + "</metadata>\n" + "<content encoding=\"base64binary\">"
        + "VGhpcyBpcyBhIGRvY3VtZW50Lg==" + "</content>\n" + "</record>\n";
    expectedXml[2] = buildExpectedXML(feedType, record);

    takeFeed(expectedXml, "MockRepositoryEventLog7.txt");
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
        + "</metadata>\n" + "<content encoding=\"base64binary\">" + content
        + "</content>\n" + "</record>\n";

    expectedXml[0] = buildExpectedXML(feedType, record);
    takeFeed(expectedXml, "MockRepositoryEventLog8.txt");
  }

  private void takeFeed(String[] expectedXml, String repository)
      throws Exception {
    String gsaExpectedResponse = GsaFeedConnection.SUCCESS_RESPONSE;
    String gsaActualResponse;

    MockRepositoryEventList mrel = new MockRepositoryEventList(repository);
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);

    DocumentList documentList = qtm.startTraversal();

    int i = 0;
    Document document = null;
    while ((document = documentList.nextDocument()) != null) {
      System.out.println("Test " + i + " output");
      Assert.assertFalse(i == expectedXml.length);
      dpusher.take(document, "junit");
      System.out.println("Test " + i + " assertions");
      String resultXML = mockFeedConnection.getFeed();
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
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(document, "junit");
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
    Map props = new HashMap();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(10 * 1000);

    // Test normal document as delete document
    props.put(SpiConstants.PROPNAME_LASTMODIFIED, cal);
    props.put(SpiConstants.PROPNAME_ACTION,
        SpiConstants.ActionType.DELETE.toString());
    props.put(SpiConstants.PROPNAME_DOCID, "doc1");
    props.put(SpiConstants.PROPNAME_CONTENT, "now is the time");
    props.put(SpiConstants.PROPNAME_CONTENTURL,
        "http://www.comtesturl.com/test");
    Document document = createSimpleDocument(props);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    try {
      dpusher.take(document, "junit");
    } catch (Exception e) {
      fail("Full document take");
    }
    String resultXML = mockFeedConnection.getFeed();

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
        + ServletUtil.DOCID + "doc1\"", resultXML);
    assertStringContains("action=\"delete\"", resultXML);
    assertStringNotContains("<content encoding=\"base64binary\">", resultXML);

    // Now document without URL or content
    props.clear();
    props.put(SpiConstants.PROPNAME_LASTMODIFIED, cal);
    props.put(SpiConstants.PROPNAME_ACTION,
        SpiConstants.ActionType.DELETE.toString());
    props.put(SpiConstants.PROPNAME_DOCID, "doc1");
    document = createSimpleDocument(props);

    try {
      dpusher.take(document, "junit");
    } catch (Exception e) {
      fail("No content document take");
    }
    resultXML = mockFeedConnection.getFeed();

    assertStringContains("last-modified=\"Thu, 01 Jan 1970 00:00:10 GMT\"",
        resultXML);
    assertStringContains("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
        + ServletUtil.DOCID + "doc1\"", resultXML);
    assertStringContains("action=\"delete\"", resultXML);

    // Now document without last-modified
    props.clear();
    props.put(SpiConstants.PROPNAME_ACTION,
        SpiConstants.ActionType.DELETE.toString());
    props.put(SpiConstants.PROPNAME_DOCID, "doc1");
    document = createSimpleDocument(props);

    try {
      dpusher.take(document, "junit");
    } catch (Exception e) {
      fail("No last-modified document take");
    }
    resultXML = mockFeedConnection.getFeed();

    assertStringContains("url=\"" + ServletUtil.PROTOCOL + "junit.localhost"
        + ServletUtil.DOCID + "doc1\"", resultXML);
    assertStringContains("action=\"delete\"", resultXML);
    assertStringNotContains("last-modified=", resultXML);
  }

  /**
   * Utility method to convert {@link Map} of Java Objects into a
   * {@link SimpleDocument}.
   */
  private Document createSimpleDocument(Map props) {
    Map spiValues = new HashMap();
    for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
      String key = (String) iter.next();
      Object obj = props.get(key);
      Value val = null;
      if (obj instanceof String) {
        val = Value.getStringValue((String) obj);
      } else if (obj instanceof Calendar) {
        val = Value.getDateValue((Calendar) obj);
      } else if (obj instanceof InputStream) {
        val = Value.getBinaryValue((InputStream) obj);
      } else {
        throw new AssertionError(obj);
      }
      List values = new ArrayList();
      values.add(val);
      spiValues.put(key, values);
    }
    return new SimpleDocument(spiValues);
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
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(document, "junit");
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
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(document, "junit");
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
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(document, "junit");
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
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(document, "junit");
    String resultXML = mockFeedConnection.getFeed();

    assertStringNotContains("action=\"add\"", resultXML);

    String addActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"add\""
      + "}\r\n" + "";

    document = JcrDocumentTest.makeDocumentFromJson(addActionJson);
    dpusher.take(document, "junit");
    resultXML = mockFeedConnection.getFeed();

    assertStringContains("action=\"add\"", resultXML);

    String deleteActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"delete\""
      + "}\r\n" + "";

    document = JcrDocumentTest.makeDocumentFromJson(deleteActionJson);
    dpusher.take(document, "junit");
    resultXML = mockFeedConnection.getFeed();

    assertStringContains("action=\"delete\"", resultXML);

    String bogusActionJson = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:displayurl\":\"http://www.sometesturl.com/test\""
      + ",\"google:action\":\"bogus\""
      + "}\r\n" + "";

    document = JcrDocumentTest.makeDocumentFromJson(bogusActionJson);
    dpusher.take(document, "junit");
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
    Document document = JcrDocumentTest.makeDocumentFromJson(jsonEventString);
    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    dpusher.take(document, "junit");
    return mockFeedConnection.getFeed();
  }

  private static final String TEST_LOG_FILE = "testdata/FeedLogFile";

  /**
   * Test separate feed logging.
   */
  public void testFeedLogging() throws Exception {
    // Delete the Log file it it exists.
    (new File(TEST_LOG_FILE)).delete();

    try {
      // Setup logging on the DocPusher class.
      FileHandler fh = new FileHandler(TEST_LOG_FILE, 10000, 1);
      SimpleFormatter sf = new SimpleFormatter();
      fh.setFormatter(sf);
      DocPusher.getFeedLogger().addHandler(fh);
      DocPusher.getFeedLogger().setLevel(Level.FINER);

      // Setup the DocPusher.
      MockFeedConnection mockFeedConnection = new MockFeedConnection();
      DocPusher dpusher = new DocPusher(mockFeedConnection);
      Document document;
      String resultXML;

      // Test incremental feed with content.
      final String jsonIncremental =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\""
              + ",\"content\":\"now is the time\""
              + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
              + "}\r\n" + "";
      document = JcrDocumentTest.makeDocumentFromJson(jsonIncremental);
      dpusher.take(document, "junit");
      resultXML = mockFeedConnection.getFeed();
      assertFeedInLog(resultXML, TEST_LOG_FILE);

      // Test metadata-url feed with content.
      final String jsonMetaAndUrl =
          "{\"timestamp\":\"10\",\"docid\":\"doc2\""
              + ",\"content\":\"now is the time\""
              + ",\"google:searchurl\":\"http://www.sometesturl.com/test\""
              + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
              + "}\r\n" + "";
      document = JcrDocumentTest.makeDocumentFromJson(jsonMetaAndUrl);
      dpusher.take(document, "junit");
      resultXML = mockFeedConnection.getFeed();
      assertFeedInLog(resultXML, TEST_LOG_FILE);

      // Test MSWord Document.
      final String jsonMsWord =
          "{\"timestamp\":\"10\",\"docid\":\"msword\""
              + ",\"google:mimetype\":\"application/msword\""
              + ",\"contentfile\":\"testdata/mocktestdata/test.doc\""
              + ",\"author\":\"ziff\""
              + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
              + "}\r\n" + "";
      document = JcrDocumentTest.makeDocumentFromJson(jsonMsWord);
      dpusher.take(document, "junit");
      resultXML = mockFeedConnection.getFeed();
      assertFeedInLog(resultXML, TEST_LOG_FILE);
    } finally {
      // Clean up the log file.
      (new File(TEST_LOG_FILE)).delete();
    }
  }

  private void assertFeedInLog(String resultXML, String logFileName)
      throws IOException {
    BufferedReader logIn = new BufferedReader(new FileReader(logFileName));
    BufferedReader xmlIn = new BufferedReader(new StringReader(resultXML));

    xmlIn.mark(resultXML.length());
    String xmlLine = xmlIn.readLine();
    String logLine;
    boolean isMatch = false;
    while ((logLine = logIn.readLine()) != null) {
      if (logLine.indexOf(xmlLine) >= 0) {
        assertEquals(xmlLine, logLine.substring(7));
        // We match the first line - start comparing record
        isMatch = true;
        while ((xmlLine = xmlIn.readLine()) != null) {
          logLine = logIn.readLine();
          if (xmlLine.indexOf("<content") >= 0) {
            if (!"<content encoding=\"base64binary\">...content...</content>".equals(logLine)) {
              isMatch = false;
              break;
            }
          } else if (!xmlLine.equals(logLine)) {
            isMatch = false;
            break;
          }
        }
        if (isMatch) {
          break;
        } else {
          // Need to reset the xmlIn and reload the xmlLine
          xmlIn.reset();
          xmlLine = xmlIn.readLine();
        }
      } else {
        continue;
      }
    }
    assertTrue("Overall match", isMatch);
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
    props.load(inStream);
    String tffName = (String) props.get(Context.TEED_FEED_FILE_PROPERTY_KEY);
    // Make sure the teed feed file does not exist
    (new File(tffName)).delete();

    // Create the Document.
    String json1 = "{\"timestamp\":\"10\",\"docid\":\"doc1\""
      + ",\"content\":\"now is the time\"" + ",\"author\":\"ziff\""
      + ",\"google:contenturl\":\"http://www.sometesturl.com/test\""
      + "}\r\n" + "";
    Document document = JcrDocumentTest.makeDocumentFromJson(json1);

    try {
      // Create DocPusher and send feed.
      MockFeedConnection mockFeedConnection = new MockFeedConnection();
      DocPusher dpusher = new DocPusher(mockFeedConnection);
      dpusher.take(document, "junit");
      String resultXML = mockFeedConnection.getFeed();
      assertFeedTeed(resultXML, tffName);

      // Now send the feed again and compare with existing teed feed file.
      dpusher.take(document, "junit");
      String secondResultXML = mockFeedConnection.getFeed();
      assertFeedTeed(resultXML + secondResultXML, tffName);
    } finally {
      // Clean up teed feed file.
      (new File(tffName)).delete();
    }
  }

  private void assertFeedTeed(String resultXML, String tffName)
      throws IOException {
    BufferedReader tffIn = new BufferedReader(new FileReader(tffName));
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
        + "<!DOCTYPE gsafeed PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"gsafeed.dtd\">"
        + "<gsafeed><header><datasource>junit</datasource>\n" + "<feedtype>"
        + feedType + "</feedtype>\n" + "</header>\n" + "<group>\n" + record
        + "</group>\n" + "</gsafeed>\n";
    return rawData;
  }

  /**
   * Test that lack of a required metadata field, google:docid, throws
   * a RepositoryDocumentException.
   * Regression test for Issue 108.
   */
  public void testNoDocid() throws Exception {
    Map props = new HashMap();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(10 * 1000);

    // Supply lastmodified and content, but no docid
    props.put(SpiConstants.PROPNAME_LASTMODIFIED, cal);
    props.put(SpiConstants.PROPNAME_MIMETYPE, "text/plain");
    props.put(SpiConstants.PROPNAME_CONTENT, "now is the time");
    props.put(SpiConstants.PROPNAME_CONTENTURL,
        "http://www.comtesturl.com/test");
    Document document = createSimpleDocument(props);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    // Lack of required metadata should throw RepositoryDocumentException.
    try {
      dpusher.take(document, "junit");
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException e) {
      // Expected.
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * Test that if DocPusher gets a read error on the document content stream,
   * it throws a RepositoryDocumentException.
   * Regression test for Issue 108.
   */
  /*
   * TODO: MockFeedConnection converts IOExceptions into RuntimeExceptions.
   * This test won't work until that is fixed.
   */
  public void disabled_testContentReadError() throws Exception {
    Map props = new HashMap();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(10 * 1000);

    props.put(SpiConstants.PROPNAME_DOCID, "doc1");
    props.put(SpiConstants.PROPNAME_LASTMODIFIED, cal);
    props.put(SpiConstants.PROPNAME_MIMETYPE, "text/plain");
    props.put(SpiConstants.PROPNAME_CONTENT, new BadInputStream());
    props.put(SpiConstants.PROPNAME_CONTENTURL,
        "http://www.comtesturl.com/test");
    Document document = createSimpleDocument(props);

    MockFeedConnection mockFeedConnection = new MockFeedConnection();
    DocPusher dpusher = new DocPusher(mockFeedConnection);
    // IO error on content should throw RepositoryDocumentException.
    try {
      dpusher.take(document, "junit");
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException e) {
      // Expected.
    } catch (Throwable t) {
      fail("Expected RepositoryDocumentException, but got " + t.toString());
    }
  }

  /**
   * An InputStream that throws IOExceptions when read.
   */
  private class BadInputStream extends InputStream {
    // Make it look like there is something to read.
    public int available() {
      return 69;
    }
    // Override read methods, always throwing IOException.
    public int read() throws IOException {
      throw new IOException("This stream is unreadable");
    }
    public int read(byte[] b) throws IOException {
      throw new IOException("This stream is unreadable");
    }
    public int read(byte[] b, int o, int l) throws IOException {
      throw new IOException("This stream is unreadable");
    }
  }
}
