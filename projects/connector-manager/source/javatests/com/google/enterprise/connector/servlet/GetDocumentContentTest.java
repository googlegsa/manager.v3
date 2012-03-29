// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.servlet;

import com.google.common.base.Strings;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.instantiator.ThreadPool;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.manager.ProductionManager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.MockConnector;
import com.google.enterprise.connector.spi.MockRetriever;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.util.SystemClock;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Tests {@link GetDocumentContent} servlet class.
 */
public class GetDocumentContentTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(GetDocumentContentTest.class.getName());

  /** Test basic servlet function against a MockManager. */
  public void testGetDocumentContentMockManager() throws Exception {
    Manager manager = MockManager.getInstance();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int status;

    // Connector1 serves up document content equal to docid.
    status = GetDocumentContent.handleDoGet(manager, "connector1",
                                            "xyzzy", buffer);
    assertEquals(200, status);
    assertEquals("xyzzy", buffer.toString());

    // Connector2 serves up no content at all.
    buffer.reset();
    status = GetDocumentContent.handleDoGet(manager, "connector2",
                                            "xyzzy", buffer);
    assertEquals(204, status);
    assertEquals(0, buffer.size());

    // UnknownConnector does not exist.
    buffer.reset();
    status = GetDocumentContent.handleDoGet(manager, "unknownConnector",
                                            "xyzzy", buffer);
    assertEquals(404, status);
    assertEquals(0, buffer.size());
  }

  /** Test basic servlet function against a MockManager. */
  public void testGetLastModifiedMockManager() throws Exception {
    Manager manager = MockManager.getInstance();
    long lastModified;

    // Connector1 has a lastModifiedDate
    lastModified = GetDocumentContent.handleGetLastModified(
                   manager, "connector1", "xyzzy");
    assertEquals(3600 * 1000, lastModified);

    // Connector2 has no lastModifiedDate
    lastModified = GetDocumentContent.handleGetLastModified(
                   manager, "connector2", "xyzzy");
    assertEquals(-1L, lastModified);

    // UnknownConnector does not exist.
    lastModified = GetDocumentContent.handleGetLastModified(
                   manager, "unknownConnector", "xyzzy");
    assertEquals(-1L, lastModified);
  }

  private String connectorName = MockInstantiator.TRAVERSER_NAME1;
  private String docid = "docid";

  private Manager getProductionManager() throws Exception {
    MockInstantiator instantiator =
        new MockInstantiator(new ThreadPool(5, new SystemClock()));
    instantiator.setupTestTraversers();
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever()));
    ProductionManager manager = new ProductionManager();
    manager.setInstantiator(instantiator);
    return manager;
  }

  /** Test ProductionManager getDocumentContent. */
  private void checkGetDocumentContent(String connectorName, String docid,
      int expectedStatus, String expectedOutput) throws Exception {
    Manager manager = getProductionManager();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int status =
        GetDocumentContent.handleDoGet(manager, connectorName, docid, buffer);

    assertEquals(expectedStatus, status);
    assertEquals(expectedOutput, buffer.toString());
  }

  /** Test ProductionManager getDocumentContent with null connectorName. */
  public void testGetDocumentContentNullConnectorName() throws Exception {
    checkGetDocumentContent(null, docid, 400, "");
  }

  /** Test ProductionManager getDocumentContent with empty connectorName. */
  public void testGetDocumentContentEmptyConnectorName() throws Exception {
    checkGetDocumentContent("", docid, 400, "");
  }

  /** Test ProductionManager getDocumentContent with null docid. */
  public void testGetDocumentContentNullDocid() throws Exception {
    checkGetDocumentContent(connectorName, null, 400, "");
  }

  /** Test ProductionManager getDocumentContent with empty docid. */
  public void testGetDocumentContentEmptyDocid() throws Exception {
    checkGetDocumentContent(connectorName, "", 400, "");
  }

  /** Test ProductionManager getDocumentContent with ConnectorNotFound. */
  public void testGetDocumentContentConnectorNotFound() throws Exception {
    checkGetDocumentContent("unknownConnector", docid, 404, "");
  }

  /** Test ProductionManager getDocumentContent with Document NotFound. */
  public void testGetDocumentContentDocumentNotFound() throws Exception {
    checkGetDocumentContent(connectorName, MockRetriever.DOCID_NOT_FOUND,
                            404, "");
  }

  /** Test ProductionManager getDocumentContent with RepositoryException. */
  public void testGetDocumentContentRepositoryException() throws Exception {
    checkGetDocumentContent(connectorName,
        MockRetriever.DOCID_REPOSITORY_EXCEPTION, 503, "");
  }

  /** Test ProductionManager getDocumentContent where document has no content. */
  public void testGetDocumentContentNoContent() throws Exception {
    // GSA still doesn't handle docs with no content, so the
    // Production Manager substitutes a single space.
    checkGetDocumentContent(connectorName,
        MockRetriever.DOCID_NO_CONTENT, 200, " ");
  }

  /** Test ProductionManager getDocumentContent where document has empty content. */
  public void testGetDocumentContentEmptyContent() throws Exception {
    // GSA still doesn't handle docs with no content, so the
    // Production Manager substitutes a single space.
    checkGetDocumentContent(connectorName,
        MockRetriever.DOCID_EMPTY_CONTENT, 200, " ");
  }

  /** Test ProductionManager getDocumentContent. */
  public void testGetDocumentContent() throws Exception {
    checkGetDocumentContent(connectorName, docid, 200, docid);
  }

  /** Test getLastModified function against a ProductionManager. */
  public void testGetLastModifiedProductionManager() throws Exception {
    Manager manager = getProductionManager();
    long lastModified;

    // Null or empty ConnectorName.
    lastModified = GetDocumentContent.handleGetLastModified(
        manager, null, docid);
    assertEquals(-1L, lastModified);
    lastModified = GetDocumentContent.handleGetLastModified(
        manager, "", docid);
    assertEquals(-1L, lastModified);

    // Null or empty docid.
    lastModified = GetDocumentContent.handleGetLastModified(
        manager, connectorName, null);
    assertEquals(-1L, lastModified);
    lastModified = GetDocumentContent.handleGetLastModified(
        manager, connectorName, "");
    assertEquals(-1L, lastModified);

    // Connector regular docids have lastModified.
    lastModified = GetDocumentContent.handleGetLastModified(
        manager, connectorName, docid);
    assertEquals(3600 * 1000, lastModified);

    // This Document has no lastModifiedDate
    lastModified = GetDocumentContent.handleGetLastModified(
        manager, connectorName, MockRetriever.DOCID_NO_LASTMODIFIED);
    assertEquals(-1L, lastModified);

    // UnknownConnector does not exist.
    lastModified = GetDocumentContent.handleGetLastModified(
        manager, "unknownConnector", docid);
    assertEquals(-1L, lastModified);

    // Unknown document does not exist.
    lastModified = GetDocumentContent.handleGetLastModified(
        manager, connectorName, MockRetriever.DOCID_NOT_FOUND);
    assertEquals(-1L, lastModified);
  }

  private void patchRealProductionManager() throws Exception {
    MockInstantiator instantiator =
        new MockInstantiator(new ThreadPool(5, new SystemClock()));
    instantiator.setupTestTraversers();
    instantiator.addConnector(connectorName,
        new MockConnector(null, null, null, new MockRetriever()));
    assertTrue(Context.getInstance().getManager() instanceof ProductionManager);
    ProductionManager manager =
        (ProductionManager) (Context.getInstance().getManager());
    manager.setInstantiator(instantiator);
  }

  /** Test ProductionManager getDocumentContent should deny SecMgr. */
  public void testGetDocumentContentFromSecMgr() throws Exception {
    patchRealProductionManager();
    MockHttpServletRequest req = new MockHttpServletRequest("GET",
        "/connector-manager/getDocumentContent");
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
    req.setParameter(ServletUtil.QUERY_PARAM_DOCID, docid);
    req.addHeader("User-Agent", "SecMgr");
    MockHttpServletResponse res = new MockHttpServletResponse();
    new GetDocumentContent().doGet(req, res);
    assertEquals(403, res.getStatus());
  }

  /**
   * Test method for the HttpServlet.doGet.
   */
  public void testDoGetNoIfModifiedSince() throws Exception {
    patchRealProductionManager();
    MockHttpServletRequest req = new MockHttpServletRequest("GET",
        "/connector-manager/getDocumentContent");
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
    req.setParameter(ServletUtil.QUERY_PARAM_DOCID, docid);
    MockHttpServletResponse res = new MockHttpServletResponse();
    new GetDocumentContent().doGet(req, res);
    assertEquals(200, res.getStatus());
    assertEquals(docid, res.getContentAsString());
    assertNull(res.getHeader("Content-Encoding"));
  }

  /**
   * Test compression in the Servlet.
   */
  public void testDoGetWithCompression() throws Exception {
    patchRealProductionManager();
    String docid = "NowIsTheTimeForAllGoodMenToComeToTheAidOfTheCountry";
    MockHttpServletRequest req = new MockHttpServletRequest("GET",
        "/connector-manager/getDocumentContent");
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
    req.setParameter(ServletUtil.QUERY_PARAM_DOCID, docid);
    req.addHeader("Accept-Encoding", "gzip");
    GetDocumentContent.setUseCompression(true);
    MockHttpServletResponse res = new MockHttpServletResponse();
    new GetDocumentContent().doGet(req, res);
    assertEquals(200, res.getStatus());
    assertEquals("gzip", res.getHeader("Content-Encoding"));
    assertFalse(docid.equals(res.getContentAsString()));
    assertTrue(docid.equals(StringUtils.streamToString(
        new GZIPInputStream(new ByteArrayInputStream(
        res.getContentAsByteArray())))));
  }

  /**
   * Test IsModifiedSince, where lastModified returns -1.
   */
  public void testDoGetIsModifiedSinceNoLastModified() throws Exception {
    patchRealProductionManager();
    String docid = MockRetriever.DOCID_NO_LASTMODIFIED;
    MockHttpServletRequest req = new MockHttpServletRequest("GET",
        "/connector-manager/getDocumentContent");
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
    req.setParameter(ServletUtil.QUERY_PARAM_DOCID, docid);
    req.addHeader("If-Modified-Since", SystemClock.INSTANCE.getTimeMillis());
    MockHttpServletResponse res = new MockHttpServletResponse();
    new GetDocumentContent().doGet(req, res);
    assertEquals(200, res.getStatus());
    assertEquals(docid, res.getContentAsString());
  }

  /**
   * Test IsModifiedSince, where lastModified newer.
   */
  public void testDoGetIsModifiedSinceLastModified() throws Exception {
    patchRealProductionManager();
    MockHttpServletRequest req = new MockHttpServletRequest("GET",
        "/connector-manager/getDocumentContent");
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
    req.setParameter(ServletUtil.QUERY_PARAM_DOCID, docid);
    req.addHeader("If-Modified-Since", 1);
    MockHttpServletResponse res = new MockHttpServletResponse();
    new GetDocumentContent().doGet(req, res);
    assertEquals(200, res.getStatus());
    assertEquals(docid, res.getContentAsString());
  }


  /**
   * Test IsModifiedSince, where unmodified.
   */
  public void testDoGetUnModifiedSinceLastModified() throws Exception {
    patchRealProductionManager();
    MockHttpServletRequest req = new MockHttpServletRequest("GET",
        "/connector-manager/getDocumentContent");
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, connectorName);
    req.setParameter(ServletUtil.QUERY_PARAM_DOCID, docid);
    req.addHeader("If-Modified-Since", SystemClock.INSTANCE.getTimeMillis());
    MockHttpServletResponse res = new MockHttpServletResponse();
    new GetDocumentContent().doGet(req, res);
    assertEquals(304, res.getStatus());
    assertTrue(Strings.isNullOrEmpty(res.getContentAsString()));
  }
}
