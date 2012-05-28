// Copyright 2011 Google Inc.  All Rights Reserved.
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
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Tests ConnectorManagerUrlServlet base class.
 */
public class ConnectorManagerUrlServletTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorManagerUrlServletTest.class.getName());

  private MockHttpServletRequest req;
  private MockHttpServletResponse res;

  @Override
  protected void setUp() throws Exception {
    req = new MockHttpServletRequest("POST", "");
    res = new MockHttpServletResponse();
  }

  /** Test null request body. */
  public void testNullBody() throws Exception {
    new TestServlet().doGet(req, res);
    String expectedResult = "<CmResponse>\n  <StatusId>"
        + ConnectorMessageCode.RESPONSE_EMPTY_REQUEST
        + "</StatusId>\n</CmResponse>\n";
    assertEquals(expectedResult, res.getContentAsString());
  }

  /** Test empty request body. */
  public void testEmptyBody() throws Exception {
    req.setContent("".getBytes());
    new TestServlet().doPost(req, res);
    String expectedResult = "<CmResponse>\n  <StatusId>"
        + ConnectorMessageCode.RESPONSE_EMPTY_REQUEST
        + "</StatusId>\n</CmResponse>\n";
    assertEquals(expectedResult, res.getContentAsString());
  }

  /** Test XML request body. */
  public void testXmlBody() throws Exception {
    String expectedResult = "<test>hello</test>";
    req.setContent(expectedResult.getBytes("UTF-8"));
    new TestServlet().doPost(req, res);
    assertEquals(expectedResult, res.getContentAsString());
  }

  /** Test character encoding is set to UTF-8 if unspecified. */
  public void testDefaultCharEncoding() throws Exception {
    String expectedResult = "<test>hello</test>";
    req.setContent(expectedResult.getBytes("UTF-8"));
    assertNull(req.getCharacterEncoding());
    new TestServlet().doPost(req, res);
    assertEquals("UTF-8", req.getCharacterEncoding());
    assertEquals(expectedResult, res.getContentAsString());
  }

  /** Test explicit character encoding. */
  public void testSpecifiedCharEncoding() throws Exception {
    String expectedResult = "<test>hello</test>";
    req.setContent(expectedResult.getBytes("ISO-8859-1"));
    req.setCharacterEncoding("ISO-8859-1");
    new TestServlet().doPost(req, res);
    assertEquals("ISO-8859-1", req.getCharacterEncoding());
    assertEquals(expectedResult, res.getContentAsString());
  }

  /** Subclass of ConnectorManagerUrlServlet that prints its parameters out. */
  private static class TestServlet extends ConnectorManagerUrlServlet {
    @Override
    protected void processDoPost(String connectorManagerUrl,
        String xmlBody, Manager manager, PrintWriter out) {
      out.print(xmlBody);
      out.flush();
    }
  }

  /** Test extracting the Connetor Manager URL. */
  public void testConnectorManagerUrl1() throws Exception {
    testConnectorManagerUrl("/connector-manager", "");
  }

  public void testConnectorManagerUrl2() throws Exception {
    testConnectorManagerUrl("/connector-manager/", "");
  }

  public void testConnectorManagerUrl3() throws Exception {
    testConnectorManagerUrl("/connector-manager", "/testServlet");
  }

  public void testConnectorManagerUrl4() throws Exception {
    testConnectorManagerUrl("/connector-manager/", "/testServlet");
  }

  private void testConnectorManagerUrl(String contextPath, String servletPath)
      throws Exception {
    req = new MockHttpServletRequest("POST", contextPath + servletPath);
    req.setServerName("test");
    req.setServerPort(8080);
    req.setContextPath(contextPath);
    req.setServletPath(servletPath);
    req.setContent("<test>hello</test>".getBytes("UTF-8"));
    new TestUrlServlet().doPost(req, res);
    assertEquals("http://test:8080/connector-manager",
                 res.getContentAsString());
  }

  /** Subclass of ConnectorManagerUrlServlet that prints its CM URL out. */
  private static class TestUrlServlet extends ConnectorManagerUrlServlet {
    @Override
    protected void processDoPost(String connectorManagerUrl,
        String xmlBody, Manager manager, PrintWriter out) {
      out.print(connectorManagerUrl);
      out.flush();
    }
  }
}
