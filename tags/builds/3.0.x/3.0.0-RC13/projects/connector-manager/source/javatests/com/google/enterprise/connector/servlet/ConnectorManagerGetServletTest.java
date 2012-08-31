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
 * Tests ConnectorManagerGetServlet base class.
 */
public class ConnectorManagerGetServletTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorManagerGetServletTest.class.getName());

  private MockHttpServletRequest req;
  private MockHttpServletResponse res;

  @Override
  protected void setUp() throws Exception {
    req = new MockHttpServletRequest("GET","");
    res = new MockHttpServletResponse();
  }

  /** Test null connectorName. */
  public void testNullConnectorName() throws Exception {
    new TestServlet().doGet(req, res);

    String expectedResult = "<CmResponse>\n  <StatusId>"
        + ConnectorMessageCode.RESPONSE_NULL_CONNECTOR
        + "</StatusId>\n</CmResponse>\n";
    assertEquals(expectedResult, res.getContentAsString());
  }

  /** Test empty connectorName. */
  public void testEmptyConnectorName() throws Exception {
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, "");
    new TestServlet().doGet(req, res);

    String expectedResult = "<CmResponse>\n  <StatusId>"
        + ConnectorMessageCode.RESPONSE_NULL_CONNECTOR
        + "</StatusId>\n</CmResponse>\n";
    assertEquals(expectedResult, res.getContentAsString());
  }

  /** Test connectorName, but no language. */
  public void testNoLanguage() throws Exception {
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, "test");
    new TestServlet().doGet(req, res);
    assertEquals("test", res.getContentAsString());
  }

  /** Test connectorName, but empty language. */
  public void testEmptyLanguage() throws Exception {
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, "test");
    req.setParameter(ServletUtil.QUERY_PARAM_LANG, "");
    new TestServlet().doPost(req, res);
    assertEquals("test", res.getContentAsString());
  }

  /** Test connectorName and language. */
  public void testNameLanguage() throws Exception {
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_NAME, "test");
    req.setParameter(ServletUtil.QUERY_PARAM_LANG, "lang");
    new TestServlet().doGet(req, res);
    assertEquals("test, lang", res.getContentAsString());
  }

  /** Subclass of ConnectorManagerGetServlet that prints its parameters out. */
  private static class TestServlet extends ConnectorManagerGetServlet {
    @Override
    protected void processDoGet(
        String connectorName, String lang, Manager manager, PrintWriter out) {
      out.print(connectorName);
      if (lang != null) {
        out.print(", " + lang);
      }
      out.flush();
    }
  }

  /** Test null ConfigureResponse just returns status code. */
  public void testNullConfigureResponse() {
    String expectedResult =
        "<CmResponse>\n  <StatusId>0</StatusId>\n</CmResponse>\n";
    doTest(null, expectedResult);
  }

  /** Test ConfigureResponse with no message, form snippet, map, or xml. */
  public void testEmptyConfigureResponse() {
    String expectedResult = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n  </ConfigureResponse>\n</CmResponse>\n";
    doTest(new ConfigureResponse(null, null), expectedResult);
  }

  /** Test ConfigureResponse with message. */
  public void testMessageConfigureResponse() {
    String message = "Test Message";
    String expectedResult = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n    <message>" + message + "</message>\n"
        + "  </ConfigureResponse>\n</CmResponse>\n";
    doTest(new ConfigureResponse(message, null), expectedResult);
  }

  /** Test ConfigureResponse with form snippet. */
  public void testSnippetConfigureResponse() {
    String formSnippet = "<tr><td>Form</td></tr>";
    String expectedResult = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n"
        + "    <FormSnippet><![CDATA[" + formSnippet + "]]></FormSnippet>\n"
        + "  </ConfigureResponse>\n</CmResponse>\n";
    doTest(new ConfigureResponse(null, formSnippet), expectedResult);
  }

  /** Test ConfigureResponse with a bad form snippet. */
  public void testBadSnippetConfigureResponse() {
    String formSnippet = "<tr><td>Form<";
    String expectedResult = "<CmResponse>\n  <StatusId>"
        + ConnectorMessageCode.ERROR_PARSING_XML_REQUEST
        + "</StatusId>\n</CmResponse>\n";
    doTest(new ConfigureResponse(null, formSnippet), expectedResult);
  }

  /** Test ConfigureResponse with message and form snippet. */
  public void testMessageAndSnippetConfigureResponse() {
    String message = "Test Message";
    String formSnippet = "<tr><td>Form</td></tr>";
    String expectedResult = "<CmResponse>\n  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n"
        + "    <FormSnippet><![CDATA[" + formSnippet + "]]></FormSnippet>\n"
        + "    <message>" + message + "</message>\n"
        + "  </ConfigureResponse>\n</CmResponse>\n";
    doTest(new ConfigureResponse(message, formSnippet), expectedResult);
  }

  /** Test writeConfigureResponse with null ConfigureResponse. */
  public void doTest(ConfigureResponse configureResponse,
                     String expectedResult) {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    ConnectorManagerGetServlet.writeConfigureResponse(out,
        new ConnectorMessageCode(), configureResponse);
    out.flush();
    String result = writer.toString();
    out.close();
    LOGGER.info("Expected Response:\n" + expectedResult);
    LOGGER.info("Actual Response:\n" + result);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 StringUtils.normalizeNewlines(result));
  }
}
