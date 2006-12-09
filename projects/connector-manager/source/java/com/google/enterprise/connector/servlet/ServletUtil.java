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


/**
 *
 * Servlet utility class.
 *
 */
public class ServletUtil {

  private static Logger LOGGER =
    Logger.getLogger(ServletUtil.class.getName());

  public static final String MIMETYPE_XML = "text/xml";
  public static final String MIMETYPE_HTML = "text/html";

  public static final String PROTOCOL = "googleconnector://";
  public static final String DOCID = "/doc?docid=";

  public static final String QUERY_PARAM_LANG = "Lang";
  public static final String DEFAULT_LANGUAGE = "en";

  public static final String XMLTAG_RESPONSE_ROOT = "CmResponse";
  public static final String XMLTAG_STATUSID = "StatusId";
  public static final String XMLTAG_STATUS_MESSAGE = "StatusMsg";
  public static final String XMLTAG_STATUS_PARAMS = "CMParams";
  public static final String XMLTAG_STATUS_PARAM_ORDER = "Order";
  public static final String XMLTAG_STATUS_PARAM = "CMParam";
	  
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

  public static final String LOG_RESPONSE_EMPTY_REQUEST = "Empty request";
  public static final String LOG_RESPONSE_EMPTY_NODE = "Empty node";
  public static final String LOG_RESPONSE_NULL_CONNECTOR =
      "Null connector name";
  public static final String LOG_RESPONSE_NULL_CONNECTOR_TYPE =
      "Null connector type name";

  public static final String LOG_EXCEPTION_CONNECTOR_TYPE_NOT_FOUND =
      "Exception: the connector type is not found";
  public static final String LOG_EXCEPTION_CONNECTOR_NOT_FOUND =
      "Exception: the connector is not found";
  public static final String LOG_EXCEPTION_CONNECTOR_EXISTS =
      "Exception: the connector exists";
  public static final String LOG_EXCEPTION_INSTANTIATOR =
      "Exception: instantiator";
  public static final String LOG_EXCEPTION_PERSISTENT_STORE =
      "Exception: persistent store";
  public static final String LOG_EXCEPTION_THROWABLE =
      "Exception: throwable";
  public static final String LOG_EXCEPTION_CONNECTOR_MANAGER =
      "Exception: general";

  public static final String XML_SIMPLE_RESPONSE =
      "<CmResponse>\n" + "  <StatusId>0</StatusId>\n" + "</CmResponse>\n";

  public static final String DEFAULT_FORM =
    "<tr><td>Username</td><td>\n" + 
    "<input type=\"text\" name=\"Username\" /></td></tr>\n" + 
    "<tr><td>Password</td><td>\n" + 
    "<input type=\"password\" name=\"Password\" /></td></tr>\n" + 
    "<tr><td>Repository</td><td>\n" + 
    "<input type=\"text\" name=\"Repository\" /></td></tr>\n";

  public static final String ATTRIBUTE_NAME = "name=\"";
  public static final String ATTRIBUTE_VALUE = " value=\"";
  public static final char QUOTE = '"';

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
      LOGGER.log(Level.SEVERE, "Parse exception", pce);
    } catch (java.io.UnsupportedEncodingException uee) {
      LOGGER.log(Level.SEVERE, "Really Unexpected", uee);
    } catch (SAXException se) {
      LOGGER.log(Level.SEVERE, "SAX Exception", se);
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "IO Exception", ioe);
    }
    return null;
  }

  /**
   * Get a root element from the XML request body.
   * 
   * @param xmlBody String the XML request body
   * @param rootTagName String the root element tag name
   * @return a result Element object if successful, null on error 
   */
  public static Element parseAndGetRootElement(String xmlBody, String rootTagName) {
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(xmlBody, errorHandler);
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
  public static String getFirstAttribute(
      Element elem, String name, String attrName) {
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
      attributes.put(((Element)nodeList.item(n)).getAttribute("name"),
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
   * Write an XML response with only StatusId (int) to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param statusId int
   *
   */
  public static void writeResponse(PrintWriter out, int statusId) {
    writeRootTag(out, false);
    writeStatusId(out, statusId);
    writeRootTag(out, true);
  }

  /**
   * Write an XML response with full status to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param status ConnectorMessageCode
   *
   */
  public static void writeResponse(PrintWriter out,
                                   ConnectorMessageCode status) {
    writeRootTag(out, false);
    writeMessageCode(out, status);
    writeRootTag(out, true);
  }

  /**
   * Write the root XML tag <CMResponse> to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param endingTag boolean true if it is the ending tag
   *
   */
  public static void writeRootTag(PrintWriter out, boolean endingTag) {
    writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, endingTag);
  }

  /**
   * Write a statusId response to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param statusId int
   *
   */
  public static void writeStatusId(PrintWriter out,
                                   int statusId) {
    writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID,
        Integer.toString(statusId));
  }

  /**
   * Write a partial XML status response to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param status ConnectorMessageCode
   *
   */
  public static void writeMessageCode(PrintWriter out,
                                      ConnectorMessageCode status) {
    writeStatusId(out, status.getMessageId());

    if (status.getMessage() != null && status.getMessage().length() > 1) {
      writeXMLElement(
          out, 1, ServletUtil.XMLTAG_STATUS_MESSAGE, status.getMessage());
    }

    if (status.getParams() == null) {
      return;
    }

    for (int i = 0; i < status.getParams().length; ++i) {
      String param = status.getParams()[i].toString();
      if (param == null || param.length() < 1) {
        continue;
      }
      out.println(indentStr(1)
          + "<" + XMLTAG_STATUS_PARAMS
          + " " + XMLTAG_STATUS_PARAM_ORDER + "=\"" + Integer.toString(i) + "\""
          + " " + XMLTAG_STATUS_PARAM + "=\"" + param
          + "\"/>");
    }
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
    out.println(indentStr(indentLevel)
        + "<" + elemName + ">" + elemValue + "</" + elemName + ">");
  }

  /**
   * Write a name value pair as an XML element to a StringBuffer.
   *
   * @param out where StringBuffer to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param elemValue element value
   */
  public static void writeXMLElement(StringBuffer out, int indentLevel,
                                     String elemName, String elemValue) {
    out.append(indentStr(indentLevel)).append("<").append(elemName).append(">");
    out.append(elemValue).append("</").append(elemName).append(">");
  }

  /**
   * Write a name value pair as an XML element to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param attributes attributes
   */
  public static void writeXMLElementWithAttrs(
      PrintWriter out, int indentLevel, String elemName, String attributes) {
    out.println(indentStr(indentLevel)
        + "<" + elemName + " " + attributes + ">");
  }

  /**
   * Write name value pair(s) as an XML element with attributes only to a
   * StringBuffer.
   *
   * @param out where StringBuffer to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param attributes attributes
   */
  public static void writeXMLElementWithAttrs(
      StringBuffer out, int indentLevel, String elemName, String attributes) {
    out.append(indentStr(indentLevel)).append("<").append(elemName);
    out.append(" ").append(attributes).append("/>");
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
    out.println(indentStr(indentLevel)
        + (endingTag ? "</" : "<") + (tagName) + ">");
  }

  /** Write an XML tag to a StringBuffer
  *
  * @param out where StringBuffer to be written to
  * @param indentLevel the depth of indentation
  * @param tagName String name of the XML tag to be added
  * @param endingTag String write a beginning tag if true or
  * an ending tag if false
  */
  public static void writeXMLTag(StringBuffer out, int indentLevel,
                                 String tagName, boolean endingTag) {
    out.append(indentStr(indentLevel)).append(endingTag ? "</" : "<");
    out.append(tagName).append(">");
  }

  // A helper method to ident output string.
  private static String indentStr(int level) {
    if (level < XMLIndent.length) {
      return XMLIndent[level];
    } else {
      return XMLIndent[XMLIndent.length - 1]
          + indentStr(level - XMLIndent.length);
    }
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
