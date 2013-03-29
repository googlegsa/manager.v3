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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.common.SecurityUtils;
import com.google.enterprise.connector.util.SAXParseErrorHandler;
import com.google.enterprise.connector.util.XmlParseUtil;
import com.google.enterprise.connector.util.XmlParseUtil.LocalEntityResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Servlet constant and utility class.
 * Non-instantiable class that holds constants and XML tag constants,
 * utilities for reading/writing XML string, etc.
 *
 */
public class ServletUtil {

  private static Logger LOGGER =
    Logger.getLogger(ServletUtil.class.getName());

  private ServletUtil() {
  }

  public static final String MANAGER_NAME =
      "Google Search Appliance Connector Manager";

  public static final String MIMETYPE_XML = "text/xml";
  public static final String MIMETYPE_HTML = "text/html";
  public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
  public static final String MIMETYPE_ZIP = "application/zip";

  public static final String PROTOCOL = "googleconnector://";
  public static final String DOCID = "/doc?docid=";
  public static final String QUERY_PARAM_DOCID = "docid";

  public static final String QUERY_PARAM_LANG = "Lang";
  public static final String DEFAULT_LANGUAGE = "en";

  public static final String XMLTAG_RESPONSE_ROOT = "CmResponse";
  /* StatusId is deprecated, replaced by StatusCode. */
  @Deprecated
  public static final String XMLTAG_STATUSID = "StatusId";
  public static final String XMLTAG_STATUS_CODE = "StatusCode";
  public static final String XMLTAG_STATUS_MESSAGE = "StatusMsg";
  public static final String XMLTAG_STATUS_PARAMS = "CMParams";
  public static final String XMLTAG_STATUS_PARAM_ORDER = "Order";
  public static final String XMLTAG_STATUS_PARAM = "CMParam";

  public static final String XMLTAG_CONNECTOR_LOGS = "ConnectorLogs";
  public static final String XMLTAG_FEED_LOGS = "FeedLogs";
  public static final String XMLTAG_TEED_FEED = "TeedFeedFile";
  public static final String XMLTAG_LOG = "Log";
  public static final String XMLTAG_NAME = "Name";
  public static final String XMLTAG_SIZE = "Size";
  public static final String XMLTAG_LAST_MODIFIED = "LastModified";
  public static final String XMLTAG_VERSION = "Version";
  public static final String XMLTAG_INFO = "Info";
  public static final String XMLTAG_LEVEL = "Level";

  public static final String XMLTAG_CONNECTOR_INSTANCES = "ConnectorInstances";
  public static final String XMLTAG_CONNECTOR_INSTANCE = "ConnectorInstance";
  public static final String XMLTAG_CONNECTOR_TYPES = "ConnectorTypes";
  public static final String XMLTAG_CONNECTOR_TYPE = "ConnectorType";
  public static final String XMLTAG_CONNECTOR_STATUS = "ConnectorStatus";
  public static final String XMLTAG_CONNECTOR_NAME = "ConnectorName";
  public static final String XMLTAG_STATUS = "Status";
  public static final String XMLTAG_CONFIG_FORM = "ConfigForm";
  public static final String XMLTAG_CONFIGURE_RESPONSE = "ConfigureResponse";
  public static final String XMLTAG_MESSAGE = "message";
  public static final String XMLTAG_FORM_SNIPPET = "FormSnippet";

  public static final String XMLTAG_MANAGER = "Manager";
  public static final String XMLTAG_MANAGER_CONFIG_XML = "ManagerConfigXml";
  public static final String XMLTAG_MANAGER_CONFIG = "ManagerConfig";
  public static final String XMLTAG_FEEDERGATE = "FeederGate";
  public static final String XMLTAG_FEEDERGATE_PROTOCOL = "protocol";
  public static final String XMLTAG_FEEDERGATE_HOST = "host";
  public static final String XMLTAG_FEEDERGATE_PORT = "port";
  public static final String XMLTAG_FEEDERGATE_SECURE_PORT = "securePort";

  public static final String XMLTAG_CONNECTOR_CONFIG = "ConnectorConfig";
  public static final String XMLTAG_CONNECTOR_CONFIG_XML = "ConnectorConfigXml";
  public static final String XMLTAG_UPDATE_CONNECTOR = "Update";
  public static final String XMLTAG_PARAMETERS = "Param";
  public static final String XMLTAG_GLOBAL_NAMESPACE = "GlobalNamespace";
  public static final String XMLTAG_LOCAL_NAMESPACE = "LocalNamespace";

  public static final String XMLTAG_AUTHN_REQUEST = "AuthnRequest";
  public static final String XMLTAG_AUTHN_CREDENTIAL = "Credentials";
  public static final String XMLTAG_AUTHN_USERNAME = "Username";
  public static final String XMLTAG_AUTHN_PASSWORD = "Password";
  public static final String XMLTAG_AUTHN_DOMAIN = "Domain";
  public static final String XMLTAG_AUTHN_RESPONSE = "AuthnResponse";
  public static final String XMLTAG_SUCCESS = "Success";
  public static final String XMLTAG_FAILURE = "Failure";
  public static final String XMLTAG_AUTHZ_QUERY = "AuthorizationQuery";
  public static final String XMLTAG_CONNECTOR_QUERY = "ConnectorQuery";
  public static final String XMLTAG_IDENTITY = "Identity";
  public static final String XMLTAG_GROUP = "Group";
  public static final String XMLTAG_DOMAIN_ATTRIBUTE = "domain";
  public static final String XMLTAG_PASSWORD_ATTRIBUTE = "password";
  public static final String XMLTAG_NAMESPACE_ATTRIBUTE = "namespace";
  public static final String XMLTAG_PRINCIPALTYPE_ATTRIBUTE = "principal-type";
  public static final String XMLTAG_CASESENSITIVITYTYPE_ATTRIBUTE =
      "case-sensitivity-type";
  public static final String XMLTAG_RESOURCE = "Resource";
  public static final String XMLTAG_CONNECTOR_NAME_ATTRIBUTE = "connectorname";
  public static final String XMLTAG_AUTHZ_RESPONSE = "AuthorizationResponse";
  public static final String XMLTAG_ANSWER = "Answer";
  public static final String XMLTAG_DECISION = "Decision";

  public static final String XMLTAG_CONNECTOR_SCHEDULES = "ConnectorSchedules";
  @Deprecated
  public static final String XMLTAG_CONNECTOR_SCHEDULE = "ConnectorSchedule";
  public static final String XMLTAG_DISABLED = "disabled";
  public static final String XMLTAG_LOAD = "load";
  public static final String XMLTAG_DELAY = "RetryDelayMillis";
  public static final String XMLTAG_TIME_INTERVALS = "TimeIntervals";

  public static final String XMLTAG_CONNECTOR_CHECKPOINT =
      "ConnectorCheckpoint";

  public static final String XML_CDATA_START = "<![CDATA[";
  public static final String XML_CDATA_END = "]]>";

  public static final String LOG_RESPONSE_EMPTY_REQUEST = "Empty request";
  public static final String LOG_RESPONSE_EMPTY_NODE = "Empty node";
  public static final String LOG_RESPONSE_NULL_CONNECTOR =
      "Null connector name";
  public static final String LOG_RESPONSE_NULL_CONNECTOR_TYPE =
      "Null connector type name";
  public static final String LOG_RESPONSE_NULL_SCHEDULE =
      "Null connector schedule";

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

  public static final String DEFAULT_FORM =
    "<tr><td>Username</td><td>\n" +
    "<input type=\"text\" name=\"Username\" /></td></tr>\n" +
    "<tr><td>Password</td><td>\n" +
    "<input type=\"password\" name=\"Password\" /></td></tr>\n" +
    "<tr><td>Repository</td><td>\n" +
    "<input type=\"text\" name=\"Repository\" /></td></tr>\n";

  public static final String ATTRIBUTE_NAME = "name=\"";
  public static final String ATTRIBUTE_VALUE = " value=\"";
  public static final String ATTRIBUTE_VERSION = "version=\"";
  public static final String ATTRIBUTE_CRYPT = "encryption=\"";
  public static final char QUOTE = '"';

  private static final String[] XMLIndent = {
      "",
      "  ",
      "    ",
      "      ",
      "        ",
      "          ",
      "            ",
      "              "};

  /**
   * Write an XML response with only StatusId (int) to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param statusId int
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
   */
  public static void writeRootTag(PrintWriter out, boolean endingTag) {
    writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, endingTag);
  }

  /**
   * Write Connector Manager, OS, JVM version information.
   *
   * @param out where PrintWriter to be written to
   */
  public static void writeManagerSplash(PrintWriter out) {
    writeManagerSplash(out, 1);
  }

  /**
   * Write Connector Manager, OS, JVM version information.
   *
   * @param out where PrintWriter to be written to
   * @param indent indent level for writing the INFO tag.
   */
  public static void writeManagerSplash(PrintWriter out, int indent) {
    writeXMLElement(out, indent, ServletUtil.XMLTAG_INFO, getManagerSplash());
  }

  /**
   * Get Connector Manager, OS, JVM version information.
   */
  public static String getManagerSplash() {
    return ServletUtil.MANAGER_NAME + " "
      + JarUtils.getJarVersion(ServletUtil.class) + "; "
      + System.getProperty("java.vendor") + " "
      + System.getProperty("java.vm.name") + " "
      + System.getProperty("java.version") + "; "
      + System.getProperty("os.name") + " "
      + System.getProperty("os.version") + " ("
      + System.getProperty("os.arch") + ")";
  }

  /**
   * Write a statusId response to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param statusId int
   */
  @SuppressWarnings("deprecation")
  public static void writeStatusId(PrintWriter out, int statusId) {
    writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID,
        Integer.toString(statusId));
  }

  /**
   * Write a StatusCode response to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param statusId int
   */
  // TODO: Merge this method with writeStatusId (requires test fixes
  // and much better GSA response handling tests).
  @SuppressWarnings("deprecation")
  public static void writeStatusCode(PrintWriter out, int statusCode) {
    writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUS_CODE,
        Integer.toString(statusCode));

    // TODO: Remove this when XMLTAG_STATUSID is fully deprecated.
    writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID, Integer.toString(
        (ConnectorMessageCode.isSuccessMessage(statusCode)) ?
        ConnectorMessageCode.SUCCESS : statusCode));
  }

  /**
   * Write a partial XML status response to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param status ConnectorMessageCode
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
   * Write a name value pair as an XML element to a StringBuilder.
   *
   * @param out where StringBuilder to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param elemValue element value
   */
  public static void writeXMLElement(StringBuilder out, int indentLevel,
                                     String elemName, String elemValue) {
    out.append(indentStr(indentLevel)).append("<").append(elemName).append(">");
    out.append(elemValue).append("</").append(elemName).append(">");
  }

  /**
   * Write a name as an empty XML element to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   */
  public static void writeEmptyXMLElement(PrintWriter out, int indentLevel,
                                          String elemName) {
    out.println(indentStr(indentLevel)
        + "<" + elemName + "></" + elemName + ">");
  }

  /**
   * Write an XML tag with attributes out to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param attributes attributes
   * @param closeTag if true, close the tag with '/>'
   */
  public static void writeXMLTagWithAttrs(PrintWriter out, int indentLevel,
      String elemName, String attributes, boolean closeTag) {
    out.println(indentStr(indentLevel)
        + "<" + elemName + " " + attributes + ((closeTag)? "/>" : ">"));
  }

  /**
   * Write an XML tag with attributes out to a StringBuilder.
   *
   * @param out where StringBuilder to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param attributes attributes
   * @param closeTag if true, close the tag with '/>'
   */
  public static void writeXMLTagWithAttrs(StringBuilder out, int indentLevel,
      String elemName, String attributes, boolean closeTag) {
    out.append(indentStr(indentLevel)).append("<").append(elemName);
    out.append(" ").append(attributes).append((closeTag)? "/>" : ">");
  }

  /** Write an XML tag to a PrintWriter
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation
   * @param tagName String name of the XML tag to be added
   * @param endingTag String write a beginning tag if true or
   *        an ending tag if false
   */
  public static void writeXMLTag(PrintWriter out, int indentLevel,
                                 String tagName, boolean endingTag) {
    out.println(indentStr(indentLevel)
        + (endingTag ? "</" : "<") + (tagName) + ">");
  }

  /** Write an XML tag to a StringBuilder
   *
   * @param out where StringBuilder to be written to
   * @param indentLevel the depth of indentation
   * @param tagName String name of the XML tag to be added
   * @param endingTag String write a beginning tag if true or
   *        an ending tag if false
   */
  public static void writeXMLTag(StringBuilder out, int indentLevel,
                                 String tagName, boolean endingTag) {
    out.append(indentStr(indentLevel)).append(endingTag ? "</" : "<");
    out.append(tagName).append(">");
  }

  // A helper method to ident output string.
  public static String indentStr(int level) {
    if (level < XMLIndent.length) {
      return XMLIndent[level];
    } else {
      return XMLIndent[XMLIndent.length - 1]
          + indentStr(level + 1 - XMLIndent.length);
    }
  }

  /**
   * Percent-encode the {@code kay, value} pair into the supplied StringBuilder.
   */
  public static void percentEncode(StringBuilder sb, String key, String value) {
    percentEncode(sb, key);
    sb.append('=');
    percentEncode(sb, value);    
  }

  private static final String HEX = "0123456789ABCDEF";
  private static final String PERCENT = "-.%0123456789%%%%%%"
      + "%ABCDEFGHIJKLMNOPQRSTUVWXYZ%%%%_"
      + "%abcdefghijklmnopqrstuvwxyz%%%~";

  /**
   * Percent-encode {@code text} as described in
   * <a href="http://tools.ietf.org/html/rfc3986#section-2">RFC 3986</a> and
   * using UTF-8. This is the most common form of percent encoding. 
   * The characters A-Z, a-z, 0-9, '-', '_', '.', and '~' are left as-is;
   * the rest are percent encoded.
   *
   * @param sb StringBuilder in which to encode the text
   * @param text some plain text
   */
  public static void percentEncode(StringBuilder sb, String text) {
    byte[] bytes;
    try {
      bytes = text.getBytes("UTF-8");
    } catch (java.io.UnsupportedEncodingException e) {
      // Not going to happen with built-in encoding.
      throw new AssertionError(e);
    }
    for (byte b : bytes) {
      if (b >= '-' && b <= '~' && PERCENT.charAt(b - '-') != '%') {
        sb.append((char) b);
      } else {
        sb.append('%');
        sb.append(HEX.charAt((b >>> 4) & 0xF));
        sb.append(HEX.charAt(b & 0xF));
      }
    }
  }

  /**
   * Append a query parameter to a URL.
   *
   * @param url an Appendable with URL under contruction
   * @param paramName the name of the query parameter
   * @param paramValue the value of the query parameter
   */
  public static void appendQueryParam(StringBuilder url, String paramName,
                                      String paramValue) {
    // TODO: Use java.net.URI instead of URLEncoder. Better we should write our
    // own RFC 3986 compliant encoder instead.
    if (!Strings.isNullOrEmpty(paramValue)) {
      try {
        url.append(((url.indexOf("?") == -1) ? '?' : '&'));
        url.append(URLEncoder.encode(paramName, "UTF-8")).append('=');
        url.append(URLEncoder.encode(paramValue, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // Can't happen with UTF-8.
        throw new AssertionError(e);
      }
    }
  }

  /**
   * Append a fragment to a URL.
   *
   * @param url an Appendable with URL under contruction
   * @param fragment the fragment to append to the URL.
   */
  public static void appendFragment(StringBuilder url, String fragment) {
    if (!Strings.isNullOrEmpty(fragment)) {
      try {
        url.append('#').append(URLEncoder.encode(fragment, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // Can't happen with UTF-8.
        throw new AssertionError(e);
      }
    }
  }

  /**
   * Parse an un-decoded query string into its parts, correctly taking into
   * account {@code charset}. {@code queryString} should be commonly obtained
   * from {@code HttpServletRequest.getQueryString}.
   *
   * @param queryString encoded parameter string
   * @return fully-decoded parameter values
   */
  public static Map<String, List<String>> parseQueryString(String queryString) {
    if (Strings.isNullOrEmpty(queryString)) {
      return Collections.emptyMap();
    }
    Map<String, List<String>> parsedParams
        = new HashMap<String, List<String>>();
    for (String param : queryString.split("&")) {
      String[] parts = param.split("=", 2);
      String key = parts[0];
      String value = parts.length == 2 ? parts[1] : "";
      try {
        key = URLDecoder.decode(key, "UTF-8");
        value = URLDecoder.decode(value, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        // UTF-8 is always supported.
        throw new AssertionError(ex);
      }
      List<String> values = parsedParams.get(key);
      if (values == null) {
        values = new ArrayList<String>();
        parsedParams.put(key, values);
      }
      values.add(value);
    }
    return parsedParams;
  }

  /**
   * Get the first parameter in the list, or {@code null} if there are no values
   * for the parameter.
   *
   * @param params parameters returned from {@link #parseQueryString}
   * @param param parameter to retrieve first value
   * @return first value of {@code param}, or {@code null}
   */
  public static String getFirstParameter(Map<String, List<String>> params,
      String param) {
    List<String> values = params.get(param);
    return values == null ? null : values.get(0);
  }

  /**
   * Tries to normalize a pathname, as if relative to the context.
   * Absolute paths are allowed (unlike traditional web-app behaviour).
   * file: URLs are allowed as well and are treated like absolute paths.
   * All relative paths are made relative the the web-app WEB-INF directory.
   * Attempts are made to recognize paths that are already relative to
   * WEB-INF (they begin with WEB-INF or /WEB-INF).
   *
   * @param name the file name
   * @param toAbsolute Function to map a relative path to an absolute one.
   */
  public static String getRealPath(String name,
      Function<String, String> toAbsolute) throws IOException {
    Preconditions.checkNotNull(name);

    // The administrator can override any of this manipulation by specifying a
    // file: URI, similar to the way Spring allows file: URIs to specify true
    // absolute resource locations.
    if (name.toLowerCase().startsWith("file:")) {
      try {
        return new File(new URI(name).getPath()).getAbsolutePath();
      } catch (URISyntaxException urie) {
        throw new IOException("Invalid file: URI: " + name);
      }
    }

    // Web-app style "/WEB-INF/..." is still relative, even though it looks
    // absolute. Strip redundant attempts to make the path relative to WEB-INF.
    name = webAppRelative(name);

    // Absolute paths are allowed to point outside of the web application.
    if (new File(name).isAbsolute()) {
      return name;
    }

    // Force relative paths to be relative to WEB-INF.
    return toAbsolute.apply(name);
  }

  // If the supplied pathname starts with WEB-INF (either relative or
  // absolute), return a pathname that is truly relative to WEB-INF.
  // Otherwise return the original pathname.
  // Note: Technically, one could argue that "/WEB-INF" or "WEB-INF"
  // should return the empty string.  This, however returns the supplied path.
  private static String webAppRelative(String name) {
    String pathname = new File(name).getPath();
    String webInf = "WEB-INF" + File.separator;
    int index = pathname.indexOf(webInf);
    if (index < 0) {
      return name;
    }

    // Look for leading "/WEB-INF/..." or "WEB-INF/..."
    File webInfFile = new File(pathname.substring(0, index + webInf.length()));
    File parent = webInfFile.getParentFile();
    if ("WEB-INF".equals(webInfFile.getName()) && (parent == null ||
        parent.getPath().equals(File.separator) ||
        (webInfFile.isAbsolute() && parent.getParent() == null))) {
      // Return the portion relative to WEB-INF.
      return pathname.substring(index + webInf.length());
    }

    // It doesn't look like it is trying to be WEB-INF, so return the original.
    return name;
  }

  private static final Pattern PREPEND_CM_PATTERN =
      Pattern.compile("<[^>]+\\bname\\s*=\\s*[\"']");

  private static final Pattern STRIP_CM_PATTERN =
      Pattern.compile("(<[^>]+\\bname\\s*=\\s*[\"'])CM_");

  // The matching of '/*' is to remove any comments preceeding the CDATA start
  // and end, as is commonly done in XHTML to remain compatible with old HTML
  // browsers. There is no need to actually remove the comments, but this regex
  // replaced code introduced in r1630 that did do the replacements, so the
  // behavior is maintained here.
  private static final Pattern CDATA_PATTERN =
      Pattern.compile("/*<!\\[CDATA\\[(.*?)/*\\]\\]>", Pattern.DOTALL);

  /**
   * A highly-unlikely string to find in real data, used to mark the location
   * that a CDATA section or other section was removed. It currently includes a
   * UUID generated on startup, to be a highly-random string.
   */
  private static final String TEMPORARY_REPLACEMENT =
      "#Replacement-" + UUID.randomUUID() + "#";

  private static final Pattern TEMPORARY_REPLACEMENT_PATTERN =
      Pattern.compile(Pattern.quote(TEMPORARY_REPLACEMENT));

  /**
   * Given a String such as:
   * <Param name="CM_Color" value="a"/> <Param name="CM_Password" value="a"/>
   *
   * Return a String such as:
   * <Param name="Color" value="a"/> <Param name="Password" value="a"/>
   *
   * @param str String an XML string with PREFIX_CM as above
   * @return a result XML string without PREFIX_CM as above
   */
  // TODO(ejona): Remove this method. It is not executed.
  public static String stripCmPrefix(String str) {
    // TODO(ejona): Remove the temporary replacements. This code is only run
    // after any CDATA sections have been removed.
    List<String> saved = new ArrayList<String>();
    str = temporaryReplace(CDATA_PATTERN, str, saved);
    Matcher matcher = STRIP_CM_PATTERN.matcher(str);
    String result = matcher.replaceAll("$1");
    result = undoTemporaryReplace(result, saved);
    return result;
  }

  /**
   * Inverse operation for stripCmPrefix.
   *
   * @param str String an XML string without PREFIX_CM as above
   * @return a result XML string with PREFIX_CM as above
   */
  // TODO(ejona): Move this method onboard the GSA and remove it here, since it
  // is never executed by the connector manager (there is a call to it, but that
  // call isn't executed).
  public static String prependCmPrefix(String str) {
    // TODO(ejona): Remove the temporary replacements after moving to GSA. This
    // code is only run after any CDATA sections have been removed, but it is
    // hard to tell due to calls to prependCmPrefix in
    // ConnectorManagerGetServlet that are never executed.
    List<String> saved = new ArrayList<String>();
    str = temporaryReplace(CDATA_PATTERN, str, saved);
    Matcher matcher = PREPEND_CM_PATTERN.matcher(str);
    String result = matcher.replaceAll("$0CM_");
    result = undoTemporaryReplace(result, saved);
    return result;
  }

  /**
   * Temporarily remove strings matching {@code pattern} from {@code source},
   * saving them in {@code saved}; a placeholder is put in {@code source} where
   * each string used to be and then returned for later reversal. Only one
   * temporary replacement can be occurring within {@code source} at a time, due
   * to reusing the same placeholder; you must always {@link
   * #undoTemporaryReplace} on the string before performing a different
   * {@link #temporaryReplace}.
   */
  private static String temporaryReplace(Pattern pattern, String source,
      List<String> saved) {
    StringBuffer sb = new StringBuffer(source.length());
    Matcher m = pattern.matcher(source);
    String replacement = Matcher.quoteReplacement(TEMPORARY_REPLACEMENT);
    while (m.find()) {
      saved.add(m.group());
      m.appendReplacement(sb, replacement);
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private static String undoTemporaryReplace(String source,
      List<String> previouslySaved) {
    StringBuffer sb = new StringBuffer(source.length() * 2);
    Matcher m = TEMPORARY_REPLACEMENT_PATTERN.matcher(source);
    int i;
    for (i = 0; m.find(); i++) {
      m.appendReplacement(sb, Matcher.quoteReplacement(previouslySaved.get(i)));
    }
    m.appendTail(sb);
    if (previouslySaved.size() != i) {
      LOGGER.warning("Unexpected number of replacements. Bug!");
    }
    return sb.toString();
  }

  private static final String XML_GREATER_THAN = "&gt;";
  // TODO(ejona): remove '/*', since it does nothing and is left over from old
  // code.
  private static final Pattern CDATA_END_PATTERN =
      Pattern.compile("/*\\Q]]>\\E");
  // TODO(ejona): remove '/*', since it does nothing and is left over from old
  // code.
  private static final Pattern ESCAPED_CDATA_END_PATTERN =
      Pattern.compile("/*\\Q]]&gt;\\E");

  private static final Pattern SCRIPT_PATTERN =
      Pattern.compile("<script\\b.*?</script>", Pattern.DOTALL);

  /**
   * Replaces any CDATA sections with the equivalent PCDATA section, properly
   * handling escapes. This is helpful as Xalan incorrectly produces HTML with
   * CDATA sections, which is mostly invalid for HTML documents.
   *
   * @param formSnippet snippet of a form that may have CDATA in it
   * @return the given formSnippet without CDATA sections
   */
  public static String removeNestedMarkers(String formSnippet) {
    List<String> saved = new ArrayList<String>();
    formSnippet = temporaryReplace(SCRIPT_PATTERN, formSnippet, saved);
    for (int i = 0; i < saved.size(); i++) {
      // Script sections should not have special characters escaped as entities,
      // due to the history of HTML. Also note that we have the entire <script>
      // snippet here, so escaping entities would completely trash the tags.
      saved.set(i, removeCdata(saved.get(i), false));
    }
    String result = removeCdata(formSnippet, true);
    result = undoTemporaryReplace(result, saved);
    return result;
  }

  private static String removeCdata(String formSnippet, boolean escapeSpecial) {
    StringBuffer sb = new StringBuffer(formSnippet.length());
    Matcher m = CDATA_PATTERN.matcher(formSnippet);
    while (m.find()) {
      String cdataContent = m.group(1);
      String pcdata;
      if (escapeSpecial) {
        pcdata = cdataContent.replace("&", "&amp;").replace("<", "&lt;");
      } else {
        pcdata = cdataContent;
      }
      m.appendReplacement(sb, Matcher.quoteReplacement(pcdata));
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * Escapes any end markers from the given snippet.
   *
   * @param formSnippet snippet of a form that may have end markers in it.
   * @return the given formSnippet with all end markers escaped.
   */
  public static String escapeEndMarkers(String formSnippet) {
    StringBuffer result = new StringBuffer();
    Matcher endMatcher = CDATA_END_PATTERN.matcher(formSnippet);
    while (endMatcher.find()) {
      String escapedMarker = escapeEndMarker(endMatcher.group());
      endMatcher.appendReplacement(result, escapedMarker);
    }
    endMatcher.appendTail(result);
    return result.toString();
  }

  private static String escapeEndMarker(String endMarker) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < endMarker.length(); i++) {
      char c = endMarker.charAt(i);
      switch (c) {
        case '>':
          buf.append(XML_GREATER_THAN);
          break;
        default:
          buf.append(c);
          break;
      }
    }
    return buf.toString();
  }

  /**
   * UnEscapes any end markers from the given snippet.
   *
   * @param formSnippet snippet of a form that may have escaped end markers
   *        in it.
   * @return the given formSnippet with all end markers restored.
   */
  public static String restoreEndMarkers(String formSnippet) {
    StringBuffer result = new StringBuffer();
    Matcher endMatcher = ESCAPED_CDATA_END_PATTERN.matcher(formSnippet);
    while (endMatcher.find()) {
      endMatcher.appendReplacement(result,
          endMatcher.group().replace("]]&gt;", XML_CDATA_END));
    }
    endMatcher.appendTail(result);
    return result.toString();
  }

  private static final String TEMP_ROOT_BEGIN_ELEMENT = "<filtered_root>";
  private static final String TEMP_ROOT_END_ELEMENT = "</filtered_root>";

  private static final String DOCTYPE = "<!DOCTYPE html PUBLIC "
      + "\"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
      + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";

  /**
   * Utility function to scan the form snippet for any sensitive values and
   * replace them with obfuscated values.
   *
   * @param formSnippet the form snippet to scan.  Expected to be a collection
   *        of table rows (&lt;TR&gt;) containing input elements used within an
   *        HTML FORM.  Should not contain the actual HTML FORM element.
   * @return given formSnippet will all the sensitive values obfuscated.
   *         Returns null on error.  Therefore caller should check result before
   *         using.
   */
  public static String filterSensitiveData(String formSnippet) {
    boolean valueObfuscated = false;
    // Wrap the given form in a temporary root.
    String rootSnippet = DOCTYPE
        + TEMP_ROOT_BEGIN_ELEMENT + formSnippet + TEMP_ROOT_END_ELEMENT;

    // Convert to DOM tree and obfuscated values if needed.
    Document document =
      XmlParseUtil.parse(rootSnippet, new SAXParseErrorHandler(),
            new LocalEntityResolver());
    if (document == null) {
      LOGGER.log(Level.WARNING, "XML parsing exception!");
      return null;
    }
    NodeList nodeList = document.getElementsByTagName("input");
    int length = nodeList.getLength();
    for (int n = 0; n < length; ++n) {
      // Find any names that are sensitive and obfuscate their values.
      Element element = (Element) nodeList.item(n);
      if (SecurityUtils.isKeySensitive(element.getAttribute("name")) &&
          element.hasAttribute("value") &&
          element.getAttribute("type").equalsIgnoreCase("password")) {
        element.setAttribute("value",
            obfuscateValue(element.getAttribute("value")));
        valueObfuscated = true;
      }
    }

    if (!valueObfuscated) {
      // Form snippet was not touched - just return it.
      return formSnippet;
    } else {
      // Part of form snippet was obfuscated.  Transform the DOM tree back into
      // a string.
      String filteredSnippet;
      try {
        TransformerFactory transformerFactory =
            TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.VERSION, "4.0");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        DOMSource source = new DOMSource(document);
        StreamResult result =  new StreamResult(new StringWriter());
        transformer.transform(source, result);
        filteredSnippet = result.getWriter().toString();
      } catch (TransformerException unexpected) {
        LOGGER.log(Level.WARNING, "XML transformation error!", unexpected);
        return null;
      }

      // Remove the temporary root element.
      filteredSnippet = filteredSnippet.substring(
          TEMP_ROOT_BEGIN_ELEMENT.length(),
          filteredSnippet.length() - TEMP_ROOT_END_ELEMENT.length());

      return filteredSnippet;
    }
  }

  /**
   * Replace obfuscated values in configData with clear values.
   *
   * <p> When a user edits a configuration we populate the the edit form with
   * values from configData. First we obfuscate sensitive data such as passwords
   * in configData to avoid displaying the values. We retain the clear
   * values in previousConfigData. After the user edits the form this
   * function replaces any still obfuscated values that the user did not change
   * with the saved clear values from previousConfigData. Values the user did
   * change will no longer be obfuscated and are left as the user entered
   * them.
   *
   * <p> This function employs the following heuristic to determine if a value
   * is obfuscated:
   * <OL>
   * <LI>{@link SecurityUtils#isKeySensitive(String)} returns true.
   * <LI>The value is sequence of '*' characters (isObfuscated() returns true).
   * <LI>The returned value length equals the clear value length.
   * </OL>
   *
   * <p> Below are some illustrative examples:
   * <OL>
   * <LI>Obfuscated value that will be replaced by a clear value: key is
   * sensitive, clear value = 'dog', configData value = "***".
   * <LI>Clear value that will <b>not</b> be replaced: key is <b>not</b>
   * sensitive, clear value = 'cat', configData value = "***".
   * <LI>Clear value that will <b>not</b> be replaced: key is sensitive, clear
   * value = "fish", configDataValue = "*". Here the length of the configData
   * value does <b>not</b> match the length of the clear value.
   * <LI>Clear value that will <b>not</b> be replaced: the key is sensitive,
   * clear value = "oops", configData value = "****" after the user entered
   * "****" in the form. Here {@link ServletUtil#replaceSensitiveData(Map, Map)}
   * will assume the value is obfuscated though it is not. This confusion only
   * occurs if the user enters a sequence of stars with the same length as the
   * original clear value for an obfuscated one.
   * </OL>
   *
   * @param configData the updated properties that may still include some
   *        obfuscated values.
   * @param previousConfigData the current or previous set of properties that
   *        have all the values in the clear.
   */
  public static void replaceSensitiveData(Map<String, String> configData,
      Map<String, String> previousConfigData) {
    for (Map.Entry<String, String> entry : configData.entrySet()) {
      if (SecurityUtils.isKeySensitive(entry.getKey())
          && isObfuscated(entry.getValue())) {
        String clearValue = previousConfigData.get(entry.getKey());
        if (entry.getValue().length() == clearValue.length()) {
          configData.put(entry.getKey(), clearValue);
        }
      }
    }
  }

  public static String obfuscateValue(String value) {
    return value.replaceAll(".", "*");
  }

  // Entire string of one or more '*' characters.
  private static final Pattern OBFUSCATED_PATTERN = Pattern.compile("^\\*+$");

  /**
   * @param value the value to be checked.
   * @return true if the given value is obfuscated.
   */
  public static boolean isObfuscated(String value) {
    Matcher matcher = OBFUSCATED_PATTERN.matcher(value);
    return matcher.matches();
  }
}
