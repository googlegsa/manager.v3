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

  public static class LocalEntityResolver implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId) {
      URL url;
      if (XHTML_DTD_URL.equals(systemId)) {
        LOGGER.fine("publicId=" + publicId + "; systemId=" + systemId);
        url = getClass().getResource(XHTML_DTD_FILE);
        if (url != null) {
          // Go with local resource.
          LOGGER.fine("Resolving " + XHTML_DTD_URL + " to local entity");
          return new InputSource(url.toString());
        } else {
          // Go with the HTTP URL.
          LOGGER.fine("Unable to resolve " + XHTML_DTD_URL + " to local entity");
          return null;
        }
      } else if (HTML_LAT1_URL.equals(systemId)) {
        url = getClass().getResource(HTML_LAT1_FILE);
        if (url != null) {
          return new InputSource(url.toString());
        } else {
          return null;
        }
      } else if (HTML_SYMBOL_URL.equals(systemId)) {
        url = getClass().getResource(HTML_SYMBOL_FILE);
        if (url != null) {
          return new InputSource(url.toString());
        } else {
          return null;
        }
      } else if (HTML_SPECIAL_URL.equals(systemId)) {
        url = getClass().getResource(HTML_SPECIAL_FILE);
        if (url != null) {
          return new InputSource(url.toString());
        } else {
          return null;
        }
      } else if (WEBAPP_DTD_URL.equals(systemId)) {
        url = getClass().getResource(WEBAPP_DTD_FILE);
        if (url != null) {
          return new InputSource(url.toString());
        } else {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  private static final String HTML_PREFIX =
      "<!DOCTYPE html PUBLIC "
      + "\"-//W3C//DTD XHTML 1.0 Strict//EN\" "
      + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
      + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
      + "<head><title/></head><body><table>";

  private static final String HTML_SUFFIX = "</table></body></html>";

  /**
   * A simple <code>ErrorHandler</code> implementation that always
   * throws the <code>SAXParseException</code>.
   */
  public static class ThrowingErrorHandler implements ErrorHandler {
    public void error(SAXParseException exception) throws SAXException {
      throw exception;
    }

    public void fatalError(SAXParseException exception)
        throws SAXException {
      throw exception;
    }

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
    String html = HTML_PREFIX + formSnippet + HTML_SUFFIX;
    builder.parse(new ByteArrayInputStream(html.getBytes("UTF-8")));
  }

  private static DocumentBuilderFactory factory =
      DocumentBuilderFactory.newInstance();

  /**
   * Parse an XML String to a Document.
   *
   * @param fileContent the XML string
   * @param errorHandler The error handle for SAX parser
   * @param entityResolver The entity resolver to use
   * @return A result Document object, null on error
   */
  public static Document parse(String fileContent,
      SAXParseErrorHandler errorHandler,
      EntityResolver entityResolver) {
    InputStream in = stringToInputStream(fileContent);
    return (in == null) ? null : parse(in, errorHandler, entityResolver);
  }

  /**
   * Get a root element from the XML request body.
   *
   * @param xmlBody String the XML request body
   * @param rootTagName String the root element tag name
   * @return a result Element object if successful, null on error
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
   * Parse an input stream to a Document.
   *
   * @param in the input stream
   * @param errorHandler The error handle for SAX parser
   * @param entityResolver The entity resolver to use
   * @return A result Document object, null on error
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
   * Get a root element from an XML input stream.
   *
   * @param in the input stream
   * @param rootTagName String the root element tag name
   * @return a result Element object if successful, null on error
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
   * the first XML element of given name
   *
   * @param elem Element The parent XML element
   * @param name String name of the child text element
   * @param attrName String Attribute name
   * @return String attribute value of named child element
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
   * the first XML element of given name
   *
   * @param elem Element The parent XML element
   * @param name String name of the child text element
   * @return attribute name and value map of named child element
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
   * Get text data of first XML element of given name
   *
   * @param elem Element The parent XML element
   * @param name String name of the child text element
   * @return text data of named child element
   */
  public static String getFirstElementByTagName(Element elem, String name) {
    NodeList nodeList = elem.getElementsByTagName(name);
    if (nodeList.getLength() == 0) {
      return null;
    }

    NodeList children = nodeList.item(0).getChildNodes();
    if (children.getLength() == 0 ||
        children.item(0).getNodeType() != Node.TEXT_NODE) {
      return null;
    }
    return children.item(0).getNodeValue();
  }

  /**
   * Get a list of all child text element of given name directly
   * under a given element
   *
   * @param elem the parent element
   * @param name the given name of searched child elements
   * @return a list of values of those child text elements
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
   * Extracts the first CDATA child from the given element.
   *
   * @param elem the parent element
   * @return the String value of the CDATA section, or null if none found.
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
