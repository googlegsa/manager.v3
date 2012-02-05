// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.importexport;

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.util.SAXParseErrorHandler;
import com.google.enterprise.connector.util.XmlParseUtil;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;


/** Tests for {@link ExportManager} */
public class ExportManagerTest extends TestCase {

  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/ExportManagerTest.xml";

  private static final String TEST_PROPERTIES = "testContext.properties";
  private static final String TEST_DIR_NAME =
      "testdata/tmp/ExportManagerTest";
  private final File baseDirectory = new File(TEST_DIR_NAME);

  private final File propFile = new File(baseDirectory, TEST_PROPERTIES);
  private Context context;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    assertTrue(ConnectorTestUtils.mkdirs(baseDirectory));
  }

  @Override
  protected void tearDown() throws Exception {
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    super.tearDown();
  }

  /** Test {@link ExportManager#toXml(PrintWriter, int)}. */
  public void testToXml() throws Exception {
    // Create a Connector Manager Context.
    Properties originalProps = getBaseProperties();
    newContext(originalProps);

    // Generate XML serialization of Connector Manager;
    String managerXml = asXmlString(new ExportManager());

    // Now parse that XML and see if it reflects some tiny bit of reality.
    Document document =
        XmlParseUtil.parse(managerXml, new SAXParseErrorHandler(), null);
    Element managerElement = document.getDocumentElement();

    // Extract the manager configuration properties.
    Properties props = readProperties(managerElement);
    ConnectorTestUtils.compareMaps(originalProps, props);

    // Extract the manager configuration xml.
    String configXml = readContextXml(managerElement);

    // Read in the original application context file.
    String originalConfigXml = StringUtils.normalizeNewlines(
        StringUtils.streamToStringAndThrow(
        new FileInputStream(APPLICATION_CONTEXT)));

    // Now compare extracted XML to the original.
    assertEquals("configXml", originalConfigXml, configXml);
  }

  /** Test that the generated XML does not contain passwords in clear-text. */
  public void testEncryptedPasswords() throws Exception {
    // Create a Connector Manager Context.
    Properties originalProps = getBaseProperties();
    originalProps.setProperty("password", "pwd");
    newContext(originalProps);

    // Generate XML serialization of Connector Manager;
    String managerXml = asXmlString(new ExportManager());

    // Now parse that XML and see if it reflects some tiny bit of reality.
    Document document =
        XmlParseUtil.parse(managerXml, new SAXParseErrorHandler(), null);
    Element managerElement = document.getDocumentElement();

    // Extract the manager configuration properties.
    Properties props = readProperties(managerElement);

    // Now make sure the password value is not in clear-text.
    assertTrue("password key", props.containsKey("password"));
    assertFalse("password cleartext", "pwd".equals(props.getProperty("password")));
  }

  /** Returns a base set of Connector Manager Properties. */
  private Properties getBaseProperties() {
    // Create an original set of properties.
    Properties props = new Properties();
    props.setProperty(Context.GSA_FEED_HOST_PROPERTY_KEY, "fubar");
    props.setProperty(Context.GSA_FEED_PORT_PROPERTY_KEY, "25");
    return props;
  }

  /** Creates a new Context based upon the supplied properties. */
  private void newContext(Properties properties) throws Exception {
    PropertiesUtils.storeToFile(properties, propFile, "Test Props");
    Context.refresh();
    context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    context.setFeeding(false);
  }

  /**
   * Extract the Connector Manager Properties from the managerElement.
   *
   * @param managerElement an ManagerInstance XML Element.
   * @return Connector Manager Properties.
   */
  private Properties readProperties(Element managerElement) {
    Element configElement = (Element) managerElement.getElementsByTagName(
        ServletUtil.XMLTAG_MANAGER_CONFIG).item(0);
    Map<String, String> configMap = XmlParseUtil.getAllAttributes(
        configElement, ServletUtil.XMLTAG_PARAMETERS);
    return PropertiesUtils.fromMap(configMap);
  }

  /**
   * Extract the Connector Manager configuration XML from the managerElement.
   *
   * @param managerElement an ManagerInstance XML Element.
   * @return Connector Manager applicationContext.xml.
   */
  private String readContextXml(Element managerElement) {
    NodeList nodes = managerElement.getElementsByTagName(
        ServletUtil.XMLTAG_MANAGER_CONFIG_XML);
    assertEquals("num configElements", 1, nodes.getLength());

    Element configElement = (Element) nodes.item(0);
    assertNotNull("configElement", configElement);

    String configXml = XmlParseUtil.getCdata(configElement);
    assertNotNull("configXml", configXml);
    configXml = ServletUtil.restoreEndMarkers(configXml);
    return StringUtils.normalizeNewlines(configXml);
  }

  private static String asXmlString(ExportManager manager) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    manager.toXml(pw, 0);
    return sw.toString();
  }
}
