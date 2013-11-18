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

package com.google.enterprise.connector.util;

import com.google.common.collect.ImmutableMap;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility functions for parsing XML.
 *
 * @since 2.8
 */
public class XmlParseUtil {

  private XmlParseUtil() {
    // prevents instantiation
  }

  private static Logger LOGGER =
      Logger.getLogger(XmlParseUtil.class.getName());

  private static final String XHTML_DTD_URL =
      "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd";
  private static final String XHTML_DTD_FILE = "/xhtml1-transitional.dtd";

  private static final String HTML_LAT1_URL =
      "http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent";
  private static final String HTML_LAT1_FILE = "/xhtml-lat1.ent";

  private static final String HTML_SYMBOL_URL =
      "http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent";
  private static final String HTML_SYMBOL_FILE = "/xhtml-symbol.ent";

  private static final String HTML_SPECIAL_URL =
      "http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent";
  private static final String HTML_SPECIAL_FILE = "/xhtml-special.ent";

  private static final String WEBAPP_DTD_URL =
      "http://java.sun.com/dtd/web-app_2_3.dtd";
  private static final String WEBAPP_DTD_FILE = "/web-app_2_3.dtd";

  private static final String XHTML_STRICT_DTD_URL =
      "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
  private static final String XHTML_STRICT_DTD_FILE = "/xhtml1-strict.dtd";

  private static Map<String, String> LOCAL_DTDS =
      ImmutableMap.<String, String> builder().
      put(XHTML_DTD_URL, XHTML_DTD_FILE).
      put(HTML_LAT1_URL, HTML_LAT1_FILE).
      put(HTML_SYMBOL_URL, HTML_SYMBOL_FILE).
      put(HTML_SPECIAL_URL, HTML_SPECIAL_FILE).
      put(WEBAPP_DTD_URL, WEBAPP_DTD_FILE).
      put(XHTML_STRICT_DTD_URL, XHTML_STRICT_DTD_FILE).
      build();

  /**
   * An {@link EntityResolver} implementation that resolves
   * entities using a selection of locally stored DTDs.
   */
  public static class LocalEntityResolver implements EntityResolver {
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
      String filename = LOCAL_DTDS.get(systemId);
      if (filename == null) {
        return null;
      }
      URL url = getClass().getResource(filename);
      if (url != null) {
        return new InputSource(url.toString());
      }
      return null;
    }
  }

  private static final String STRICT_HTML_PREFIX =
      "<!DOCTYPE html PUBLIC "
      + "\"-//W3C//DTD XHTML 1.0 Strict//EN\" \""
      + XHTML_STRICT_DTD_URL
      + "\">"
      + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
      + "<head><title/></head><body><table>";

  private static final String HTML_SUFFIX = "</table></body></html>";

  /**
   * A simple {@link ErrorHandler} implementation that always
   * throws the {@link SAXParseException}.
   */
  public static class ThrowingErrorHandler implements ErrorHandler {
    @Override
    public void error(SAXParseException exception) throws SAXException {
      throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      throw exception;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      throw exception;
    }
  }

  /**
   * Parses a form snippet using the XHTML Strict DTD, the
   * appropriate HTML context, and a validating parser.
   *
   * @param formSnippet the form snippet
   * @throws Exception if an unexpected error occrs
   */
  public static void validateXhtml(String formSnippet) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setErrorHandler(new ThrowingErrorHandler());
    builder.setEntityResolver(new LocalEntityResolver());

    System.out.println(formSnippet);
    String html = STRICT_HTML_PREFIX + formSnippet + HTML_SUFFIX;
    builder.parse(new ByteArrayInputStream(html.getBytes("UTF-8")));
  }

  private static DocumentBuilderFactory factory =
      DocumentBuilderFactory.newInstance();

  /**
   * Parse an XML String to a {@code org.w3c.dom.Document}.
   *
   * @param fileContent the XML string
   * @param errorHandler the error handle for SAX parser
   * @param entityResolver the entity resolver to use
   * @return A result Document object, {@code null} on error
   */
  public static Document parse(String fileContent,
      SAXParseErrorHandler errorHandler,
      EntityResolver entityResolver) {
    InputStream in = stringToInputStream(fileContent);
    return (in == null) ? null : parse(in, errorHandler, entityResolver);
  }

  /**
   * Get a root {@code org.w3c.dom.Element} from the XML request body.
   *
   * @param xmlBody the XML request body as a String
   * @param rootTagName the root Element tag name
   * @return a result Element object if successful, {@code null} on error
   */
  public static Element parseAndGetRootElement(String xmlBody,
      String rootTagName) {
    InputStream in = stringToInputStream(xmlBody);
    return (in == null) ? null : parseAndGetRootElement(in, rootTagName);
  }

  private static InputStream stringToInputStream(String fileContent) {
    try {
      return new ByteArrayInputStream(fileContent.getBytes("UTF-8"));
    } catch (java.io.UnsupportedEncodingException uee) {
      LOGGER.log(Level.SEVERE, "Really Unexpected", uee);
      return null;
    }
  }

  /**
   * Parse an input stream to a {@code org.w3c.dom.Document}.
   *
   * @param in the input stream
   * @param errorHandler the error handle for SAX parser
   * @param entityResolver the entity resolver to use
   * @return a result Document object, {@code null} on error
   */
  public static Document parse(InputStream in,
      SAXParseErrorHandler errorHandler,
      EntityResolver entityResolver) {
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(errorHandler);
      builder.setEntityResolver(entityResolver);
      Document document = builder.parse(in);
      return document;
    } catch (ParserConfigurationException pce) {
      LOGGER.log(Level.SEVERE, "Parse exception", pce);
    } catch (SAXException se) {
      LOGGER.log(Level.SEVERE, "SAX Exception", se);
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "IO Exception", ioe);
    }
    return null;
  }

  /**
   * Get a root {@code org.w3c.dom.Element} from an XML input stream.
   *
   * @param in the input stream
   * @param rootTagName the root Element tag name
   * @return a result Element object if successful, {@code null} on error
   */
  public static Element parseAndGetRootElement(InputStream in,
      String rootTagName) {
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = parse(in, errorHandler, null);
    if (document == null) {
      LOGGER.log(Level.WARNING, "XML parsing exception!");
      return null;
    }

    NodeList nodeList =
        document.getElementsByTagName(rootTagName);
    if (nodeList == null || nodeList.getLength() == 0) {
      LOGGER.log(Level.WARNING, "Empty node: " + rootTagName);
      return null;
    }

    return (Element) nodeList.item(0);
  }

  /**
   * Get the attribute value of a given attribute name for
   * the first XML {@code org.w3c.dom.Element} of given name.
   *
   * @param elem the parent XML Element
   * @param name the name of the child text Element
   * @param attrName the attribute name
   * @return attribute value of named child Element
   */
  public static String getFirstAttribute(Element elem, String name,
      String attrName) {
    NodeList nodeList = elem.getElementsByTagName(name);
    if (nodeList.getLength() == 0) {
      return null;
    }

    return (((Element) nodeList.item(0)).getAttribute(attrName));
  }

  /**
   * Get the attribute values of a given name/value pair for
   * the first XML {@code org.w3c.dom.Element} of given name.
   *
   * @param elem the parent XML Element
   * @param name the name of the child text Element
   * @return attribute name and value Map of named child Element
   */
  public static Map<String, String> getAllAttributes(Element elem,
      String name) {
    Map<String, String> attributes = new TreeMap<String, String>();
    NodeList nodeList = elem.getElementsByTagName(name);
    int length = nodeList.getLength();
    for (int n = 0; n < length; ++n) {
      attributes.put(((Element) nodeList.item(n)).getAttribute("name"),
          ((Element) nodeList.item(n)).getAttribute("value"));
    }
    return attributes;
  }

  /**
   * Get text data of an optional XML {@code org.w3c.dom.Element} of given name.
   * <p/>
   * Note that this differs from {@link #getFirstElementByTagName} in how it
   * handles missing elements vs. empty elements. Specifically, if the named
   * element does not exist, this returns {@code null}.  However, if the named
   * element does exist, but is empty (<tag></tag> or <tag/>), this returns
   * the empty string.  In both cases {@code getFirstElementByTagName} would
   * return {@code null}.
   *
   * @param elem the parent XML Element
   * @param name the name of the child text Element
   * @return text data of the named child element.
   *         Returns {@code null} if the named element does not exist.
   *         Returns the empty string if the named element exists, but is empty.
   */
  public static String getOptionalElementByTagName(Element elem, String name) {
    return getElementByTagName(elem, name, "");
  }

  /**
   * Get text data of first XML {@code org.w3c.dom.Element} of given name.
   *
   * @param elem the parent XML Element
   * @param name the name of the child text Element
   * @return text data of named child Element
   */
  public static String getFirstElementByTagName(Element elem, String name) {
    return getElementByTagName(elem, name, null);
  }

  /**
   * Get text data of first XML {@code org.w3c.dom.Element} of given name.
   *
   * @param elem the parent XML Element
   * @param name the name of the child text Element
   * @param emptyValue value to return if element exists, but is empty
   * @return text data of named child Element
   */
  private static String getElementByTagName(Element elem, String name,
                                           String emptyValue) {
    NodeList nodeList = elem.getElementsByTagName(name);
    if (nodeList.getLength() == 0) {
      return null;
    }

    NodeList children = nodeList.item(0).getChildNodes();
    if (children.getLength() == 0 ||
        children.item(0).getNodeType() != Node.TEXT_NODE) {
      return emptyValue;
    }
    return children.item(0).getNodeValue();
  }

  /**
   * Get a list of all child text Elements of given name directly
   * under a given {@code org.w3c.dom.Element}.
   *
   * @param elem the parent Element
   * @param name the given name of searched child Elements
   * @return a List of values of those child text Elements
   */
  public static List<String> getAllElementsByTagName(Element elem, String name) {
    NodeList nodeList = elem.getElementsByTagName(name);
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < nodeList.getLength(); ++i) {
      NodeList children = nodeList.item(i).getChildNodes();
      if (children.getLength() == 0 ||
          children.item(0).getNodeType() != Node.TEXT_NODE) {
        continue;
      }
      result.add(children.item(0).getNodeValue());
    }
    return result;
  }

  /**
   * Extracts the first CDATA child from the given {@code org.w3c.dom.Element}.
   *
   * @param elem the parent Element
   * @return the String value of the CDATA section, or {@code null} if none
   *         found
   */
  public static String getCdata(Element elem) {
    NodeList nodes = elem.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
        CharacterData cdataNode = (CharacterData) node;
        return cdataNode.getData();
      }
    }
    return null;
  }
}
