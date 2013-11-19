// Copyright 2013 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Tests UrlConstructor.
 */
public class UrlConstructorTest extends TestCase {
  private static final String CONTENTURL_PREFIX = "http://foo/";
  private static final String DATASOURCE = "junit";
  private static final String DOCID = "doc1";
  private static final String PARENTID = "parent";
  private static final String WEBURL = "http://www.foo.com/bar/doc1.txt";

  // TODO(bmj): GSA 7.0 strips fragments off of URLs in the feed, so we
  // append the fragment as another query parameter until that is fixed.
  private static final String FRAG = "&"; // "#";

  @Override
  protected void setUp() throws Exception {
    Context.getInstance().setContentUrlPrefix(CONTENTURL_PREFIX);
  }

  private Map<String, Object> getDocumentProperties() {
    return getDocumentProperties(DOCID);
  }

  private Map<String, Object> getDocumentProperties(String docid) {
    Map<String, Object> props = 
        ConnectorTestUtils.createSimpleDocumentBasicProperties(docid);
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, PARENTID);
    return props;
  }

  private Document getDocument(Map<String, Object> properties) {
    return ConnectorTestUtils.createSimpleDocument(properties);
  }

  /**
   * Test RepositoryDocumentException is thrown there are insufficient parts
   * available to construct a record URL.
   */
  public void testExceptionIfNoDocidOrSearchUrl() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Map<String, Object> props = getDocumentProperties();
    props.remove(SpiConstants.PROPNAME_DOCID);
    props.remove(SpiConstants.PROPNAME_SEARCHURL);
    Document document = getDocument(props);
    try {
      urlConstructor.getRecordUrl(document, DocumentType.RECORD);
      fail("Expected RepositoryDocumentException but got none.");
    } catch (RepositoryDocumentException expected) {
      // Expected.
    }
  }

  /** Test getRecordUrl() with SearchURL. */ 
  public void testGetRecordUrlWithSearchUrl() throws Exception {
    for (FeedType feedType : FeedType.values()) {
      for (DocumentType documentType : DocumentType.values()) {
        testGetRecordUrlWithSearchUrl(feedType, documentType);
      }
    }
  }

  /**
   * Test getRecordUrl() with SearchURL. If the SearchURL is specified,
   * it should be used, and no URL should be constructed.
   */
  private void testGetRecordUrlWithSearchUrl(FeedType feedType,
      DocumentType documentType) throws Exception {
    UrlConstructor urlConstructor = new UrlConstructor(DATASOURCE, feedType);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_SEARCHURL, WEBURL);
    Document document = getDocument(props);
    assertEquals(WEBURL, urlConstructor.getRecordUrl(document, documentType));
  }

  /** Test getRecordUrl() with FeedType.WEB returns the docid as the URL. */
  public void testGetRecordUrlFeedTypeWeb() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.WEB);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_DOCID, WEBURL);
    Document document = getDocument(props);
    assertEquals(WEBURL,
        urlConstructor.getRecordUrl(document, DocumentType.RECORD));
  }

  /** Test getRecordUrl() with FeedType.CONTENT returns a googleconnector URL */
  public void testGetRecordUrlFeedTypeContent() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Document document = getDocument(getDocumentProperties());
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + DOCID,
        urlConstructor.getRecordUrl(document, DocumentType.RECORD));
  }

  /**
   * Test getRecordUrl() with FeedType.CONTENT does not encode the docid
   * to preserve backward compatibility.  See codesite Issue 214.
   */
  public void testGetRecordUrlFeedTypeContentEvilDocid() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Document document = getDocument(getDocumentProperties("Ben&Jerry"));
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + "Ben&Jerry",
        urlConstructor.getRecordUrl(document, DocumentType.RECORD));
  }

  /**
   * Test getRecordUrl() with FeedType.CONTENT avoid issue 214 if not
   * an ACL record.
   */
  public void testGetRecordUrlFeedTypeContentAndFragmentFails()
      throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_FRAGMENT, "fragment");
    Document document = getDocument(props);
    try {
      urlConstructor.getRecordUrl(document, DocumentType.RECORD);
      fail("Expected IllegalArgumentException, but got none.");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }
  }

  /**
   * Test getRecordUrl() with FeedType.CONTENT, DocumentType.ACL, and a
   * fragment property returns a googleconnector URL with the fragment appended.
   */
  public void testGetRecordUrlFeedTypeContentAndFragment() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_FRAGMENT, "fragment");
    Document document = getDocument(props);
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + DOCID + FRAG + "fragment",
        urlConstructor.getRecordUrl(document, DocumentType.ACL));
  }

  /**
   * Test getRecordUrl() with FeedType.CONTENT, DocumentType.ACL, and a
   * fragment property returns a googleconnector URL with the fragment
   * appended and is encoded.
   */
  public void testGetRecordUrlFeedTypeContentAndEvilFragment()
      throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_FRAGMENT, "slice&dice");
    Document document = getDocument(props);
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + DOCID + FRAG + "slice%26dice",
        urlConstructor.getRecordUrl(document, DocumentType.ACL));
  }

  /** Test getRecordUrl() with FeedType.CONTENTURL returns a retriever URL */
  public void testGetRecordUrlFeedTypeContentUrl() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Document document = getDocument(getDocumentProperties());
    assertEquals(CONTENTURL_PREFIX + "?" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "=" + DATASOURCE + "&" + ServletUtil.QUERY_PARAM_DOCID + "=" + DOCID,
        urlConstructor.getRecordUrl(document, DocumentType.RECORD));
  }

  /** Test getRecordUrl() with FeedType.CONTENTURL encodes the DOCID. */
  public void testGetRecordUrlFeedTypeContentUrlEvilDocid() throws Exception {
    // See Issue 214.
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Document document = getDocument(getDocumentProperties("Ben&Jerry"));
    assertEquals(CONTENTURL_PREFIX + "?" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "=" + DATASOURCE + "&" + ServletUtil.QUERY_PARAM_DOCID + "="
        + "Ben%26Jerry",
        urlConstructor.getRecordUrl(document, DocumentType.RECORD));
  }

  /**
   * Test getRecordUrl() with FeedType.CONTENTURL and a fragment property
   * returns a googleconnector URL with the fragment appended.
   */
  public void testGetRecordUrlFeedTypeContentUrlAndFragment() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_FRAGMENT, "fragment");
    Document document = getDocument(props);
    assertEquals(CONTENTURL_PREFIX + "?" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "=" + DATASOURCE + "&" + ServletUtil.QUERY_PARAM_DOCID + "="
        + DOCID + FRAG + "fragment",
        urlConstructor.getRecordUrl(document, DocumentType.RECORD));
  }

  /**
   * Test getRecordUrl() with FeedType.CONTENTURL and a fragment property 
   * returns a googleconnector URL with the fragment appended and is encoded.
   */
  public void testGetRecordUrlFeedTypeContentUrlAndEvilFragment()
      throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_FRAGMENT, "slice&dice");
    Document document = getDocument(props);
    assertEquals(CONTENTURL_PREFIX + "?" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "=" + DATASOURCE + "&" + ServletUtil.QUERY_PARAM_DOCID + "="
        + DOCID + FRAG + "slice%26dice",
        urlConstructor.getRecordUrl(document, DocumentType.RECORD));
  }

  /**
   * Test getInheritFromUrl() with ACLINHERITFROM property. If that property
   * is specified, it should be used, and no URL should be constructed.
   */
  public void testGetInheritFromUrlWithInheritFromUrl() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.WEB);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM, WEBURL);
    // These should be ignored:
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, PARENTID);
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT, "fragment");
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE,
              FeedType.CONTENT.toString());
    Document document = getDocument(props);
    assertEquals(WEBURL, urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.WEB returns the docid as the URL.
   */
  public void testGetInheritFromUrlFeedTypeWeb() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.WEB);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, WEBURL);
    Document document = getDocument(props);
    assertEquals(WEBURL,
        urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.CONTENT returns a googleconnector:
   * URL.
   */
  public void testGetInheritFromUrlFeedTypeContent() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Document document = getDocument(getDocumentProperties());
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + PARENTID,
        urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.CONTENT does not encode the docid
   * to preserve backward compatibility.  See codesite Issue 214.
   */
  public void testGetInheritFromUrlFeedTypeContentEvilDocid() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, "Ben&Jerry");
    Document document = getDocument(props);
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + "Ben&Jerry",
        urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.CONTENT and a fragment property
   * returns a googleconnector URL with the fragment appended.
   */
  public void testGetInheritFromUrlFeedTypeContentAndFragment()
      throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT, "fragment");
    Document document = getDocument(props);
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + PARENTID + FRAG + "fragment",
        urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.CONTENT and a fragment property
   * returns a googleconnector URL with the fragment appended and is encoded.
   */
  public void testGetInheritFromUrlFeedTypeContentAndEvilFragment()
      throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENT);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT, "slice&dice");
    Document document = getDocument(props);
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + PARENTID + FRAG + "slice%26dice",
        urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.CONTENTURL returns a retriever URL.
   */
  public void testGetInheritFromUrlFeedTypeContentUrl() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Document document = getDocument(getDocumentProperties());
    assertEquals(CONTENTURL_PREFIX + "?" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "=" + DATASOURCE + "&" + ServletUtil.QUERY_PARAM_DOCID + "=" 
        + PARENTID, urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.CONTENTURL encodes the parent DOCID.
   */
  public void testGetInheritFromUrlFeedTypeContentUrlEvilDocid()
      throws Exception {
    // See Issue 214.
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, "Ben&Jerry");
    Document document = getDocument(props);
    assertEquals(CONTENTURL_PREFIX + "?" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "=" + DATASOURCE + "&" + ServletUtil.QUERY_PARAM_DOCID + "="
        + "Ben%26Jerry",
        urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.CONTENTURL and a fragment 
   * property returns a retriever URL with the fragment appended.
   */
  public void testGetInheritFromUrlFeedTypeContentUrlAndFragment()
      throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT, "fragment");
    Document document = getDocument(props);
    assertEquals(CONTENTURL_PREFIX + "?" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "=" + DATASOURCE + "&" + ServletUtil.QUERY_PARAM_DOCID + "="
        + PARENTID + FRAG + "fragment",
        urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with FeedType.CONTENTURL and a fragment property
   * returns a retriever URL with the fragment appended and is encoded.
   */
  public void testGetInheritFromUrlFeedTypeContentUrlAndEvilFragment()
      throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_FRAGMENT, "slice&dice");
    Document document = getDocument(props);
    assertEquals(CONTENTURL_PREFIX + "?" + ServletUtil.XMLTAG_CONNECTOR_NAME
        + "=" + DATASOURCE + "&" + ServletUtil.QUERY_PARAM_DOCID + "="
        + PARENTID + FRAG + "slice%26dice",
        urlConstructor.getInheritFromUrl(document));
  }

  /**
   * Test getInheritFromUrl() with an ACLINHERITFROM_FEEDTYPE that overrides
   * the default FeedType.
   */
  public void testGetInheritFromUrlOverrideFeedType() throws Exception {
    UrlConstructor urlConstructor =
        new UrlConstructor(DATASOURCE, FeedType.CONTENTURL);
    Map<String, Object> props = getDocumentProperties();
    props.put(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE, "CONTENT");
    Document document = getDocument(props);
    assertEquals(ServletUtil.PROTOCOL + DATASOURCE + ".localhost"
        + ServletUtil.DOCID + PARENTID,
        urlConstructor.getInheritFromUrl(document));
  }
}
