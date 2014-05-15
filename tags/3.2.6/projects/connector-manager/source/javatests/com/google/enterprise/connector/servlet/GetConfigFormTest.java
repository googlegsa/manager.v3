// Copyright 2006 Google Inc.
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
import com.google.enterprise.connector.instantiator.ExtendedConfigureResponse;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.spi.ConfigureResponse;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Tests {@link GetConfigForm} servlet class.
 */
public class GetConfigFormTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(GetConfigFormTest.class.getName());

  private Manager manager;
  private Instantiator instantiator;

  @Override
  protected void setUp() {
    // Create a stand alone context with real ProductionManager.
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    context.setFeeding(false);
    context.start();
    manager = context.getManager();
    instantiator = context.getInstantiator();
  }

  /** Test null ConfigureResponse just returns status code. */
  public void testNullConfigureResponse() {
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>0</StatusId>\n" +
      "</CmResponse>\n";
    ConfigureResponse configResponse = null;
    doTest(configResponse, expectedResult);
  }

  /** Test ConfigureResponse with no message, form snippet, map, or xml. */
  public void testEmptyConfigureResponse() {
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>0</StatusId>\n" +
      "  <ConfigureResponse>\n" +
      "  </ConfigureResponse>\n" +
      "</CmResponse>\n";
    String message = null;
    String formSnippet = null;
    ConfigureResponse configResponse = new ConfigureResponse(message, formSnippet);
    doTest(configResponse, expectedResult);
  }

  /** Test ConfigureResponse with a message and form snippet. */
  public void testMessageAndFormSnippet() {
    String message = "Sample form";
    String formSnippet =
      "    <tr><td>Repository</td>" +
      "      <td><input type=\"text\" name=\"repository\" value=\"\"/></td>" +
      "    </tr>" +
      "    <tr><td>Username</td>" +
      "      <td><input type=\"text\" name=\"username\" value=\"\"/>" +
      "      </td></tr>" +
      "    <tr><td>Password</td>" +
      "      <td><input type=\"password\" name=\"passwd\" value=\"\"/>" +
      "    </td></tr>" +
      "    <tr><td>Seed URIs</td>" +
      "      <td><textarea name=\"seedUris\"></textarea></td></tr>";
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>0</StatusId>\n" +
      "  <ConfigureResponse>\n" +
      "    <FormSnippet><![CDATA[" + formSnippet + "]]></FormSnippet>\n" +
      "    <message>" + message + "</message>\n" +
      "  </ConfigureResponse>\n" +
      "</CmResponse>\n";
    ConfigureResponse configResponse =
        new ConfigureResponse(message, formSnippet);
    doTest(configResponse, expectedResult);
  }

  /** Test ExtendedConfigureResponse with message, form snippet, configXml. */
  public void testMessageAndFormSnippetAndXml() {
    String message = "Sample form";
    String formSnippet =
      "    <tr><td>Repository</td>" +
      "      <td><input type=\"text\" name=\"repository\" value=\"\"/></td>" +
      "    </tr>" +
      "    <tr><td>Username</td>" +
      "      <td><input type=\"text\" name=\"username\" value=\"\"/>" +
      "      </td></tr>" +
      "    <tr><td>Password</td>" +
      "      <td><input type=\"password\" name=\"passwd\" value=\"\"/>" +
      "    </td></tr>" +
      "    <tr><td>Seed URIs</td>" +
      "      <td><textarea name=\"seedUris\"></textarea></td></tr>";
    // My XML has embedded CDATA to make sure it gets properly escaped.
    String configXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<beans><bean id=\"MagicalFruit\" class=\"None\">" +
      "<![CDATA[xyzzy]]></bean></beans>";
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>0</StatusId>\n" +
      "  <ConfigureResponse>\n" +
      "    <FormSnippet><![CDATA[" + formSnippet + "]]></FormSnippet>\n" +
      "    <ConnectorConfigXml><![CDATA[" + configXml.replace("]]>", "]]&gt;") +
      "]]></ConnectorConfigXml>\n" +
      "    <message>" + message + "</message>\n" +
      "  </ConfigureResponse>\n" +
      "</CmResponse>\n";
    ExtendedConfigureResponse configResponse = new ExtendedConfigureResponse(
        new ConfigureResponse(message, formSnippet), configXml);
    doTest(configResponse, expectedResult);
  }

  private void doTest(ConfigureResponse configResponse, String expectedResult) {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    ConnectorManagerGetServlet.writeConfigureResponse(
        out, new ConnectorMessageCode(), configResponse);
    out.flush();
    String result = writer.toString();
    out.close();
    LOGGER.info("Expected Response:\n" + expectedResult);
    LOGGER.info("Actual Response:\n" + result);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 StringUtils.normalizeNewlines(result));
  }

  /** Test null ConnectorType returns status code. */
  public void testNullConnectorType() throws Exception {
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>5216</StatusId>\n" +
      "</CmResponse>\n";
    doTest((String) null, expectedResult);
  }

  /** Test empty ConnectorType returns status code. */
  public void testEmptyConnectorType() throws Exception {
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>5216</StatusId>\n" +
      "</CmResponse>\n";
    doTest("", expectedResult);
  }

  /** Test ConnectorTypeNotFound returns status code. */
  public void testConnectorTypeNotFound() throws Exception {
    String connectorType = "UnknownConnectorType";
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>5304</StatusId>\n" +
      "  <CMParams Order=\"0\" CMParam=\""+ connectorType + "\"/>\n" +
      "</CmResponse>\n";
    doTest(connectorType, expectedResult);
  }

  /** Test InstantiatorException returns status code. */
  public void testInstantiatorException() throws Exception {
    manager = new ExceptionThrowingManager();
    String connectorType = "UnknownConnectorType";
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>5305</StatusId>\n" +
      "</CmResponse>\n";
    doHandlerTest(connectorType, expectedResult);
  }

  /** A MockManager that throws exception when getting config form. */
  private class ExceptionThrowingManager extends MockManager {
    @Override
    public ConfigureResponse getConfigForm(String connectorTypeName,
        String language) throws InstantiatorException {
      throw new InstantiatorException("getConfigForm: connectorType = "
                                      + connectorTypeName);
    }
  }

  /** Test ProductionManager supplies connectorInstancePrototype in response. */
  public void testProductionManagerGetConfigXml() throws Exception {
    String connectorType = "TestConnectorA";
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n"
        + "    <FormSnippet><![CDATA[<tr>\n"
        + "<td>Username</td>\n"
        + "<td><input type=\"text\" name=\"Username\"/></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Password</td>\n"
        + "<td><input type=\"password\" name=\"Password\"/></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Color</td>\n"
        + "<td><input type=\"text\" name=\"Color\"/></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>RepositoryFile</td>\n"
        + "<td><input type=\"text\" name=\"RepositoryFile\"/></td>\n"
        + "</tr>\n"
        + "]]></FormSnippet>\n"
        + "    <ConnectorConfigXml><![CDATA["
        + instantiator.getConnectorInstancePrototype(connectorType)
        + "]]></ConnectorConfigXml>\n"
        + "  </ConfigureResponse>\n"
        + "</CmResponse>\n";

    doTest(connectorType, expectedResult);
  }

  private void doTest(String connectorType, String expectedResult)
      throws Exception {
    doHandlerTest(connectorType, expectedResult);
    doServletTest(connectorType, expectedResult);
  }

  private void doHandlerTest(String connectorType, String expectedResult) {
    // Use the Servlet to get the unpopulated config form.  Make sure it can be
    // parsed and make sure the reserved XML properties are preserved.
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);

    GetConfigForm.handleDoGet(connectorType, "en", manager, out);

    out.flush();
    String result = writer.toString();
    out.close();
    LOGGER.info("Expected Response:\n" + expectedResult);
    LOGGER.info("Actual Response:\n" + result);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 StringUtils.normalizeNewlines(result));
  }

  private void doServletTest(String connectorType, String expectedResult)
      throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET","");
    req.setParameter(ServletUtil.XMLTAG_CONNECTOR_TYPE, connectorType);
    MockHttpServletResponse res = new MockHttpServletResponse();
    new GetConfigForm().doGet(req, res);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 StringUtils.normalizeNewlines(res.getContentAsString()));
  }
}
