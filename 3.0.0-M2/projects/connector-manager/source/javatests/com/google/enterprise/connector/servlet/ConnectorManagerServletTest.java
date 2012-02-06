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
 * Tests ConnectorManagerServlet base class.
 */
public class ConnectorManagerServletTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorManagerServletTest.class.getName());

  private MockHttpServletRequest req;
  private MockHttpServletResponse res;

  @Override
  protected void setUp() throws Exception {
    req = new MockHttpServletRequest("POST","");
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
    System.out.println("char enc = " + req.getCharacterEncoding());
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

  /** Subclass of ConnectorManagerServlet that prints its parameters out. */
  private static class TestServlet extends ConnectorManagerServlet {
    @Override
    protected void processDoPost(
        String xmlBody, Manager manager, PrintWriter out) {
      out.print(xmlBody);
      out.flush();
    }
  }
}
