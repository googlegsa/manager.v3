// Copyright (C) 2006 Google Inc.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.enterprise.connector.spi.ConfigureResponse;

/**
 *
 * Servlet utility class.
 *
 */
public class ServletUtil {
  public static final String MIMETYPE_XML = "text/xml";
  public static final String MIMETYPE_HTML = "text/html";

  public static final String PROTOCOL = "googleconnector://";
  public static final String DOCID = "/doc?docid=";

  public static final String QUERY_PARAM_LANG = "Lang";
  public static final String DEFAULT_LANGUAGE = "en";

  public static final String XMLTAG_RESPONSE_ROOT = "CmResponse";
  public static final String XMLTAG_STATUSID = "StatusId";
  public static final String XMLTAG_CONNECTOR_TYPES = "ConnectorTypes";
  public static final String XMLTAG_CONNECTOR_TYPE = "ConnectorType";
  public static final String XMLTAG_CONNECTOR_STATUS = "ConnectorStatus";
  public static final String XMLTAG_CONNECTOR_NAME = "ConnectorName";
  public static final String XMLTAG_STATUS = "Status";
  public static final String XMLTAG_CONFIG_FORM = "ConfigForm";
  public static final String XMLTAG_CONFIGURE_RESPONSE = "ConfigureResponse";
  public static final String XMLTAG_MESSAGE = "message";
  public static final String XMLTAG_FORM_SNIPPET = "FormSnippet";

  public static final String XMLTAG_MANAGER_CONFIG = "ManagerConfig";
  public static final String XMLTAG_CERT_AUTHN = "CertAuthn";
  public static final String XMLTAG_FEEDERGATE = "FeederGate";
  public static final String XMLTAG_FEEDERGATE_HOST = "host";
  public static final String XMLTAG_FEEDERGATE_PORT = "port";

  public static final String XMLTAG_CONNECTOR_CONFIG = "ConnectorConfig";
  public static final String XMLTAG_UPDATE_CONNECTOR = "Update";
  public static final String XMLTAG_PARAMETERS = "Param";

  public static final String XMLTAG_AUTHN_REQUEST = "AuthnRequest";
  public static final String XMLTAG_AUTHN_CREDENTIAL = "Credentials";
  public static final String XMLTAG_AUTHN_USERNAME = "Username";
  public static final String XMLTAG_AUTHN_PASSWORD = "Password";
  public static final String XMLTAG_AUTHN_RESPONSE = "AuthnResponse";
  public static final String XMLTAG_SUCCESS = "Success";
  public static final String XMLTAG_FAILURE = "Failure";
  public static final String XMLTAG_AUTHZ_QUERY = "AuthorizationQuery";
  public static final String XMLTAG_CONNECTOR_QUERY = "ConnectorQuery";
  public static final String XMLTAG_IDENTITY = "Identity";
  public static final String XMLTAG_RESOURCE = "Resource";
  public static final String XMLTAG_AUTHZ_RESPONSE = "AuthorizationResponse";
  public static final String XMLTAG_ANSWER = "Answer";
  public static final String XMLTAG_DECISION = "Decision";
  
  public static final String XMLTAG_CONNECTOR_SCHEDULES = "ConnectorSchedules";
  public static final String XMLTAG_LOAD = "load";
  public static final String XMLTAG_TIME_INTERVALS = "TimeIntervals";

  public static final String XML_RESPONSE_SUCCESS = "0";
  public static final String XML_RESPONSE_STATUS_EMPTY_REQUEST =
      "Empty request";
  public static final String XML_RESPONSE_STATUS_EMPTY_NODE = "Empty node";
  public static final String XML_RESPONSE_STATUS_PARAM_MISSING =
      "Param missing";
  public static final String XML_RESPONSE_STATUS_NULL_CONNECTOR =
      "Null connector name";
  public static final String XML_RESPONSE_STATUS_NULL_CONNECTOR_TYPE =
      "Null connector type name";
  public static final String XML_RESPONSE_STATUS_NULL_CONNECTOR_STATUS =
      "Null connector status";
  public static final String XML_RESPONSE_STATUS_NULL_FORM_SNIPPET =
	  "Null form snippet or configure response";
  public static final String XML_RESPONSE_STATUS_NULL_DOCID =
      "Null doc ID";
  public static final String XML_RESPONSE_STATUS_EMPTY_CONFIG_DATA =
      "Empty connector configure data";
  public static final String XML_RESPONSE_STATUS_NULL_RESOURCE =
      "Null resource";
  public static final String XML_RESPONSE_AUTHZ_DOCID_MISMATCH =
      "Authorization docid mismatch";
  public static final String XML_SIMPLE_RESPONSE =
      "<CmResponse>\n" + "  <StatusId>0</StatusId>\n" + "</CmResponse>\n";

  public static final String DEFAULT_FORM =
    "<tr><td>Username</td><td>\n" + 
    "<input type=\"text\" name=\"Username\" /></td></tr>\n" + 
    "<tr><td>Password</td><td>\n" + 
    "<input type=\"password\" name=\"Password\" /></td></tr>\n" + 
    "<tr><td>Repository</td><td>\n" + 
    "<input type=\"text\" name=\"Repository\" /></td></tr>\n";

  public static final int HTML_NORMAL = 0;
  public static final int HTML_HEADING = 1;
  public static final int HTML_LINE = 2;
  public static final String HTML_INPUT = "input";
  public static final String HTML_NAME = "name=\"";
  public static final String HTML_VALUE = " value=\"";
  public static final char HTML_QUOTE = '"';

  private static final String[] XMLIndent = { "",
      "  ",
      "    ",
      "      ",
      "        ",
      "          ",
      "            ",
      "              "};

  private static final String PREFIX_CM = " name=\"CM_";
  private static final String PREFIX_NO_CM = " name=\"";

  private static Logger LOG =
	    Logger.getLogger(ServletUtil.class.getName());

  private static DocumentBuilderFactory factory =
	    DocumentBuilderFactory.newInstance();

  private ServletUtil() {
  }

  /**
   * Parse an XML String to a Document.
   *
   * @param fileContent the XML string
   * @param errorHandler The error handle for SAX parser
   * @return A result Document object, null on error
   */
  public static Document parse(String fileContent,
                               SAXParseErrorHandler errorHandler) {
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(errorHandler);
      Document document = builder.parse(
        new ByteArrayInputStream(fileContent.getBytes("UTF-8")));
      return document;
    } catch (ParserConfigurationException pce) {
      LOG.log(Level.SEVERE, "Parse exception", pce);
    } catch (java.io.UnsupportedEncodingException uee) {
      LOG.log(Level.SEVERE, "Really Unexpected", uee);
    } catch (SAXException se) {
      LOG.log(Level.SEVERE, "SAXException", se);
    } catch (IOException ioe) {
      LOG.log(Level.SEVERE, "IOException", ioe);
    }
    return null;
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
  public static String getFirstAttribute(Element elem, String name, String attrName) {
    NodeList nodeList = elem.getElementsByTagName(name);
    if (nodeList.getLength() == 0) {
      return null;
    }

    return (((Element)nodeList.item(0)).getAttribute(attrName));
  }

  /**
   * Get the attribute values of a given name/value pair for
   * the first XML element of given name
   *
   * @param elem Element The parent XML element
   * @param name String name of the child text element
   * @return attribute name and value map of named child element
   * 
   */
  public static Map getAllAttributes(Element elem, String name) {
    Map attributes = new TreeMap();
    NodeList nodeList = elem.getElementsByTagName(name); 
    int length = nodeList.getLength(); 
    for (int n = 0; n < length; ++n) {
      attributes.put(
            ((Element)nodeList.item(n)).getAttribute("name"),
            ((Element)nodeList.item(n)).getAttribute("value"));
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
  public static List getAllElementsByTagName(Element elem, String name) {
    NodeList nodeList = elem.getElementsByTagName(name);
    List result = new ArrayList();
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
   * Write a name value pair as an XML element to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param status String
   *
   */
  public static void writeSimpleResponse(PrintWriter out, String status) {
    writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, false);
    writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID, status);
    writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);
  }

  public static void writeConfigureResponse(
      PrintWriter out, String status, ConfigureResponse configRes) {
    writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, false);
    writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID, status);
    if (configRes != null) {
      writeXMLTag(out, 1, ServletUtil.XMLTAG_CONFIGURE_RESPONSE, false);
      writeXMLElement(
          out, 2, ServletUtil.XMLTAG_MESSAGE, configRes.getMessage());
      if (configRes.getFormSnippet() != null) {
        writeXMLElement(
            out, 2, ServletUtil.XMLTAG_FORM_SNIPPET,
            "<![CDATA[" + prependCmPrefix(configRes.getFormSnippet()) + "]]>");
      }
      writeXMLTag(out, 1, ServletUtil.XMLTAG_CONFIGURE_RESPONSE, true);
    }
    writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);
  }

  /**
   * Write a name value pair as an XML element to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param elemValue element value
   */
  public static void writeXMLElement(PrintWriter out, int indentLevel,
                                     String elemName, String elemValue) {
    out.println(IndentStr(indentLevel)
        + "<" + elemName + ">" + elemValue + "</" + elemName + ">");
  }

  /**
   * Write a name value pair as an XML element to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param attributes attributes
   */
  public static void writeXMLElementWithAttrs(PrintWriter out, int indentLevel,
                                     String elemName, String attributes) {
    
    out.println(IndentStr(indentLevel)
        + "<" + elemName + " " + attributes + ">");
  }

  /** Write an XML tag to a PrintWriter
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation
   * @param tagName String name of the XML tag to be added
   * @param endingTag String write a beginning tag if true or
   * an ending tag if false
   */
  public static void writeXMLTag(PrintWriter out, int indentLevel,
                          String tagName, boolean endingTag) {
    out.println(IndentStr(indentLevel)
        + (endingTag ? "</" : "<") + (tagName) + ">");
  }

  // A helper method to ident output string.
  private static String IndentStr(int level) {
    if (level < XMLIndent.length) {
      return XMLIndent[level];
    } else {
      return XMLIndent[XMLIndent.length - 1]
          + IndentStr(level - XMLIndent.length);
    }
  }

  public static String htmlErrorPage(String status) {
    return "<HTML><BODY>Error: " + status + "</BODY></HTML>";
  }

  public static void htmlHeadWithTitle(PrintWriter out, String title) {
    out.println("<HTML>");
    out.println("<HEAD><TITLE>" + title + "</TITLE></HEAD><BODY>");
  }

  public static void htmlBody(
      PrintWriter out, int style, String text, boolean linebreak) {
    switch (style) {
      case HTML_NORMAL:
        out.println(text);
        break;
      case HTML_HEADING:
        out.println("<H1>" + text + "</H1>");
        break;
      case HTML_LINE:
        out.println("<HR>");
        break;
      default:
        break;
    }
    if (linebreak) {
      out.println("<BR>");
    }
  }

  public static void htmlPage(PrintWriter out) {
    out.println("</BODY></HTML>");
  }

  /*
   * Given a String such as:
   * <Param name="CM_Color" value="a"/> <Param name="CM_Password" value="a"/>
   * 
   * Return a String such as:
   * <Param name="Color" value="a"/> <Param name="Password" value="a"/>
   */
  public static String stripCmPrefix(String str) {
    String result = str.replaceAll(PREFIX_CM, PREFIX_NO_CM);
    return result;
  }
  
  /*
   * Inverse operation for stripCmPrefix.
   */
  public static String prependCmPrefix(String str) {
    String result = str.replaceAll(PREFIX_NO_CM, PREFIX_CM);
    return result;
  }
}
