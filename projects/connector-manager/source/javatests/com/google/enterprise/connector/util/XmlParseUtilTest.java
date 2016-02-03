// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.util;

import static com.google.common.base.Charsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Files;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.util.testing.Logging;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class XmlParseUtilTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private File tempFile;

  private ArrayList<String> saxExceptions;

  @Before
  public void setUp() throws IOException {
    tempFile = tempFolder.newFile("test.txt");
    Files.write("hello, world", tempFile, UTF_8);

    saxExceptions = new ArrayList<String>();
    Logging.captureLogMessages(XmlParseUtil.class, "SAX Exception",
        saxExceptions);
  }

  @Test
  public void verifyTempFile() throws IOException {
    assertEquals("hello, world", Files.readFirstLine(tempFile, UTF_8));
  }

  @Test
  public void testValidateXhtml_valid() throws Exception {
    XmlParseUtil.validateXhtml("<tr><td>hello, world</td></tr>");
  }

  @Test
  public void testValidateXhtml_invalid() throws Exception {
    thrown.expect(SAXParseException.class);
    XmlParseUtil.validateXhtml("<tr>hello, world</td>");
  }

  @Test
  public void testParse_success() {
    String xml = "<ConnectorInstances></ConnectorInstances>";
    Document doc = XmlParseUtil.parse(xml, new SAXParseErrorHandler(),
        XmlParseUtil.nonEntityResolver);
    assertTrue(saxExceptions.toString(), saxExceptions.isEmpty());
    assertNotNull(doc);
  }

  @Test
  public void testParse_unknownDoctype_nonResolver() {
    String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD something\" \"\">"
        + "<ConnectorInstances></ConnectorInstances>";
    Document doc = XmlParseUtil.parse(xml, new SAXParseErrorHandler(),
        XmlParseUtil.nonEntityResolver);
    assertFalse(saxExceptions.toString(), saxExceptions.isEmpty());
    assertNull(doc);
  }

  @Test
  public void testParse_unknownDoctype_catalogResolver() {
    String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD something\" \"\">"
        + "<ConnectorInstances></ConnectorInstances>";
    Document doc = XmlParseUtil.parse(xml, new SAXParseErrorHandler(),
        XmlParseUtil.catalogEntityResolver);
    assertFalse(saxExceptions.toString(), saxExceptions.isEmpty());
    assertNull(doc);
  }

  @Test
  public void testParse_xmlEntities() throws Exception {
    String original = "a&lt;b&amp;I say &quot;Ain&#39;t nothing&quot;";
    String expected = "a<b&I say \"Ain't nothing\"";

    String xml = "<Credentials><Username>bar</Username><Password>"
        + original
        + "</Password></Credentials>";

    Document doc = XmlParseUtil.parse(xml, new SAXParseErrorHandler(),
        XmlParseUtil.nonEntityResolver);
    assertTrue(saxExceptions.toString(), saxExceptions.isEmpty());
    assertNotNull(doc);
    Element root = doc.getDocumentElement();
    String actual = XmlParseUtil.getFirstElementByTagName(root, "Password");
    assertEquals(expected, actual);
  }

  @Test
  public void testParse_htmlEntities_nonResolver() throws Exception {
    String original = "H&euml;llo, World&iexcl;";

    String xml = "<?xml version=\"1.0\"?><!DOCTYPE html PUBLIC"
        + " \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"\">"
        + "<html><body>"
        + original
        + "</body></html>";

    Document doc = XmlParseUtil.parse(xml, new SAXParseErrorHandler(),
        XmlParseUtil.nonEntityResolver);
    assertFalse(saxExceptions.toString(), saxExceptions.isEmpty());
    assertNull(doc);
  }

  @Test
  public void testParse_htmlEntities_catalogResolver() throws Exception {
    String original = "H&euml;llo, World&iexcl;";
    String expected = "H\u00ebllo, World\u00a1";

    String xml = "<?xml version=\"1.0\"?><!DOCTYPE html PUBLIC"
        + " \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"\">"
        + "<html><body>"
        + original
        + "</body></html>";

    Document doc = XmlParseUtil.parse(xml, new SAXParseErrorHandler(),
        XmlParseUtil.catalogEntityResolver);
    assertTrue(saxExceptions.toString(), saxExceptions.isEmpty());
    assertNotNull(doc);
    Element root = doc.getDocumentElement();
    String actual = XmlParseUtil.getFirstElementByTagName(root, "body");
    assertEquals(expected, actual);
  }

  @Test
  public void testParseAndGetRootElement_success() {
    String xml = "<AuthnRequest><Credentials>"
        + "<Username>bar</Username><Password>afoo</Password>"
        + "</Credentials></AuthnRequest>";

    Element root = XmlParseUtil.parseAndGetRootElement(xml,
        ServletUtil.XMLTAG_AUTHN_REQUEST);
    assertTrue(saxExceptions.toString(), saxExceptions.isEmpty());
    assertEquals(ServletUtil.XMLTAG_AUTHN_REQUEST, root.getTagName());
  }

  @Test
  public void testParseAndGetRootElement_xxe() {
    String xml = "<!DOCTYPE foo ["
        + "<!ENTITY bar SYSTEM \"file://" + tempFile + "\">"
        + "]>"
        + "<AuthnRequest><Credentials>"
        + "<Username>&bar;</Username><Password>afoo</Password>"
        + "</Credentials></AuthnRequest>";

    Element root = XmlParseUtil.parseAndGetRootElement(xml,
        ServletUtil.XMLTAG_AUTHN_REQUEST);
    assertFalse(saxExceptions.toString(), saxExceptions.isEmpty());
    assertEquals(null, root);
  }

  @Test
  public void testParseAndGetRootElement_xinclude() {
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<AuthnRequest xmlns=\"foo\" "
        + "xmlns:xi=\"http://www.w3.org/2001/XInclude\">\n"
        + "<xi:include href=\"file://" + tempFile + "\" parse=\"text\"/>\n"
        + "</data>\n";

    Element root = XmlParseUtil.parseAndGetRootElement(xml,
        ServletUtil.XMLTAG_AUTHN_REQUEST);
    assertFalse(saxExceptions.toString(), saxExceptions.isEmpty());
    assertEquals(null, root);
  }

  @Test
  public void testParseAndGetRootElement_doctype() throws Exception {
    String xml = "<?xml version=\"1.0\"?><!DOCTYPE html PUBLIC"
        + " \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"\">"
        + "<html><body></body></html>";

    Element root = XmlParseUtil.parseAndGetRootElement(xml, "html");
    assertFalse(saxExceptions.toString(), saxExceptions.isEmpty());
    assertEquals(root, null);
  }

  @Test
  public void testGetOptionalElementByTagName() throws Exception {
    String xml = "<root><foo>blah</foo><bar></bar><baz/><nest><nested>oops"
                 + "</nested></nest></root>";
    Element root = XmlParseUtil.parseAndGetRootElement(xml, "root");
    assertNotNull(root);
    assertEquals("blah", XmlParseUtil.getOptionalElementByTagName(root, "foo"));
    assertEquals("", XmlParseUtil.getOptionalElementByTagName(root, "bar"));
    assertEquals("", XmlParseUtil.getOptionalElementByTagName(root, "baz"));
    assertNull(XmlParseUtil.getOptionalElementByTagName(root, "nonexist"));
    assertEquals("", XmlParseUtil.getOptionalElementByTagName(root, "nest"));
  }
}
