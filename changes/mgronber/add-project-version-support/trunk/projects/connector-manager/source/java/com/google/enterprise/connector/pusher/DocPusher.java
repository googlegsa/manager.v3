// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.common.Base64FilterInputStream;
import com.google.enterprise.connector.common.UrlEncodedFilterInputStream;
import com.google.enterprise.connector.common.WorkQueue;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to generate xml feed for a document from the Property Map and send
 * it to GSA.
 */

public class DocPusher implements Pusher {

  private static final String XML_LESS_THAN = "&lt;";
  private static final String XML_AMPERSAND = "&amp;";
  private static final String XML_QUOTE = "&quot;";
  private static final String XML_APOSTROPHE = "&apos;";

  private static void XmlEncodeAttrValue(String val, StringBuffer buf) {
    for (int i = 0; i < val.length(); i++) {
      char c = val.charAt(i);
      /**
       * Only these characters need to be encoded, according to
       * http://www.w3.org/TR/REC-xml/#NT-AttValue. Actually, we could only
       * encode one of the quote characters if we knew that that was the
       * one used to wrap the value, but we'll play it safe and encode
       * both. TODO: what happens to white-space?
       */
      switch (c) {
      case '<':
        buf.append(XML_LESS_THAN);
        break;
      case '&':
        buf.append(XML_AMPERSAND);
        break;
      case '"':
        buf.append(XML_QUOTE);
        break;
      case '\'':
        buf.append(XML_APOSTROPHE);
        break;
      default:
        buf.append(c);
        break;
      }
    }
  }

  private static Set propertySkipSet;

  static {
    propertySkipSet = new HashSet();
    propertySkipSet.add(SpiConstants.PROPNAME_CONTENT);
    propertySkipSet.add(SpiConstants.PROPNAME_DOCID);
  }

  private static final Logger LOGGER =
      Logger.getLogger(WorkQueue.class.getName());

  // Strings for XML tags.
  public static final String XML_DEFAULT_ENCODING = "UTF-8";
  private static final String XML_START =
      "<?xml version='1.0' encoding='" + XML_DEFAULT_ENCODING
          + "'?><!DOCTYPE gsafeed PUBLIC"
          + " \"-//Google//DTD GSA Feeds//EN\" \"gsafeed.dtd\">";
  private static final String XML_GSAFEED = "gsafeed";
  private static final String XML_HEADER = "header";
  private static final String XML_DATASOURCE = "datasource";
  private static final String XML_FEEDTYPE = "feedtype";
  private static final String XML_DATA = "data";
  private static final String XML_GROUP = "group";
  private static final String XML_RECORD = "record";
  private static final String XML_METADATA = "metadata";
  private static final String XML_META = "meta";
  private static final String XML_CONTENT = "content";
  // private static final String XML_ACTION = "action";
  private static final String XML_URL = "url";
  private static final String XML_DISPLAY_URL = "displayurl";
  private static final String XML_MIMETYPE = "mimetype";
  private static final String XML_LAST_MODIFIED = "last-modified";
  // private static final String XML_LOCK = "lock";
  private static final String XML_AUTHMETHOD = "authmethod";
  // private static final String XML_NAME = "name";
  private static final String XML_ENCODING = "encoding";

  // private static final String XML_FEED_FULL = "full";
  private static final String XML_FEED_METADATA_AND_URL = "metadata-and-url";
  private static final String XML_FEED_INCREMENTAL = "incremental";
  // private static final String XML_BASE64BINARY = "base64binary";
  // private static final String XML_ADD = "add";
  // private static final String XML_DELETE = "delete";

  private static final String CONNECTOR_AUTHMETHOD = "httpbasic";

  private FeedConnection feedConnection;
  private String gsaResponse;

  private String dataSource;
  private String feedType;
  private InputStream xmlData;

  /**
   * 
   * @param feedConnection a connection
   */
  public DocPusher(FeedConnection feedConnection) {
    this.feedConnection = feedConnection;
  }

  /**
   * Gets the response from GSA when the feed is sent. For testing only.
   * 
   * @return gsaResponse response from GSA.
   */
  protected String getGsaResponse() {
    return gsaResponse;
  }

  /*
   * Wraps an xm tag with < and >.
   */
  private static String xmlWrapStart(String str) {
    StringBuffer buf = new StringBuffer();
    buf.append("<");
    buf.append(str);
    buf.append(">");
    return buf.toString();
  }

  /*
   * Wraps an xml tag with </ and >.
   */
  private static String xmlWrapEnd(String str) {
    StringBuffer buf = new StringBuffer();
    buf.append("</");
    buf.append(str);
    buf.append(">\n");
    return buf.toString();
  }

  private static InputStream stringWrappedInputStream(String prefix,
      InputStream is, String suffix) {
    InputStream result = null;

    try {
      ByteArrayInputStream prefixStream =
          new ByteArrayInputStream(prefix.getBytes(XML_DEFAULT_ENCODING));

      ByteArrayInputStream suffixStream =
          new ByteArrayInputStream(suffix.getBytes(XML_DEFAULT_ENCODING));
      InputStream[] inputStreams;
      if (is != null) {
        inputStreams = new InputStream[] {prefixStream, is, suffixStream};
      } else {
        inputStreams = new InputStream[] {prefixStream, suffixStream};
      }
      Enumeration inputStreamEnum =
          Collections.enumeration(Arrays.asList(inputStreams));
      result = new SequenceInputStream(inputStreamEnum);
    } catch (UnsupportedEncodingException e) {
      LOGGER.log(Level.SEVERE, "Encoding error.", e);
    }
    return result;
  }

  /*
   * Generate the record tag for the xml data.
   */
  private InputStream xmlWrapRecord(String searchUrl, String displayUrl,
      String lastModified, InputStream content, String mimetype, PropertyMap pm) {
    // build prefix
    StringBuffer prefix = new StringBuffer();
    prefix.append("<");
    prefix.append(XML_RECORD);
    prefix.append(" ");
    appendAttrValuePair(XML_URL, searchUrl, prefix);
    if (displayUrl != null && displayUrl.length() > 0) {
      appendAttrValuePair(XML_DISPLAY_URL, displayUrl, prefix);
    }
    if (mimetype != null) {
      appendAttrValuePair(XML_MIMETYPE, mimetype, prefix);
    }
    if (lastModified != null) {
      appendAttrValuePair(XML_LAST_MODIFIED, lastModified, prefix);
    }
    try {
      Property isPublic = pm.getProperty(SpiConstants.PROPNAME_ISPUBLIC);
      if (isPublic != null
          && getBoolean(isPublic.getValue().getString()) == false) {
        appendAttrValuePair(XML_AUTHMETHOD, CONNECTOR_AUTHMETHOD, prefix);
      }
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Problem getting ispublic property.", e);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Illegal value for ispublic property."
          + " Treat as a public doc", e);
    }
    prefix.append(">\n");
    xmlWrapMetadata(prefix, pm);
    if (feedType != XML_FEED_METADATA_AND_URL) {
      prefix.append("<");
      prefix.append(XML_CONTENT);
      prefix.append(" ");
      appendAttrValuePair(XML_ENCODING, "base64binary", prefix);
      prefix.append(">");
    }

    // build suffix
    StringBuffer suffix = new StringBuffer();
    if (feedType != XML_FEED_METADATA_AND_URL) {
      suffix.append(xmlWrapEnd(XML_CONTENT));
    }
    suffix.append(xmlWrapEnd(XML_RECORD));


    InputStream is =
        stringWrappedInputStream(prefix.toString(), content, suffix.toString());
    return is;
  }

  private static void appendAttrValuePair(String attrName, String value,
      StringBuffer buf) {
    buf.append(attrName);
    buf.append("=\"");
    XmlEncodeAttrValue(value, buf);
    buf.append("\" ");
  }

  /**
   * Wrap the metadata and append it to the string buffer. Empty metadata
   * properties are not appended. 
   * @param buf string buffer
   * @param pm property map
   */
  private static void xmlWrapMetadata(StringBuffer buf, PropertyMap pm) {
    Iterator i;
    try {
      i = pm.getProperties();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "xmlWrapMetadata",
          "Swallowing exception while scanning properties", e);
      return;
    }
    if (!i.hasNext()) {
      return;
    }

    buf.append(xmlWrapStart(XML_METADATA));
    buf.append("\n");

    while (i.hasNext()) {
      Property p = (Property) i.next();
      String name;
      try {
        name = p.getName();
      } catch (RepositoryException e) {
        LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
            "xmlWrapMetadata",
            "Swallowing exception while scanning properties", e);
        continue;
      }
      if (!propertySkipSet.contains(name)) {
        wrapOneProperty(buf, p);
      }
    }
    buf.append(xmlWrapEnd(XML_METADATA));
  }

  /**
   * Wrap a single Property and append to string buffer. Does nothing if
   * the Property's value is null or zero-length. 
   * @param buf string buffer
   * @param p Property
   */
  private static void wrapOneProperty(StringBuffer buf, Property p) {
    String name;
    try {
      name = p.getName();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "xmlWrapMetadata",
          "Swallowing exception while scanning values", e);
      return;
    }
    Iterator values;
    try {
      values = p.getValues();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "xmlWrapMetadata",
          "Swallowing exception while scanning values", e);
      return;
    }
    // if property is null, don't encode it; GSA won't process 
    if (!values.hasNext()) return;
    
    /* in case there are only null values, we want to "roll back" the
     * XML_META tag. So save our current length:
     */
    int indexMetaStart = buf.length();
    
    buf.append("<");
    buf.append(XML_META);
    buf.append(" ");
    appendAttrValuePair("name", name, buf);
    buf.append("content=\"");
    String delimiter = "";
    
    // mark the beginning of the values:
    int indexValuesStart = buf.length();
    while (values.hasNext()) {
      Value value = (Value) values.next();
      String valString = "";
      try {
        valString = value.getString();
      } catch (IllegalArgumentException e) {
        LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
            "xmlWrapMetadata", "Swallowing exception while accessing property "
                + name, e);
        continue;
      } catch (RepositoryException e) {
        LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
            "xmlWrapMetadata", "Swallowing exception while accessing property "
                + name, e);
        continue;
      }
      if (valString.length() == 0) {
        continue;
      }
      buf.append(delimiter);
      XmlEncodeAttrValue(valString, buf);
      delimiter = ", ";
    }
    /* If there were no additions to buf (because of empty values), 
     * roll back to before the XML_META tag
     */
    if (buf.length() > indexValuesStart) {
      buf.append("\"/>\n");
    } else {
      buf.delete(indexMetaStart, buf.length());
    }
  }

  /*
   * Gets the Calendar value for a given property.
   */
  private static Calendar getCalendarAndThrow(PropertyMap pm, String name)
      throws IllegalArgumentException, RepositoryException {
    Calendar result = null;
    Value v = getValueAndThrow(pm, name);
    result = v.getDate();
    return result;
  }

  /*
   * Gets the String value for a given property.
   */
  private static String getStringAndThrow(PropertyMap pm, String name)
      throws IllegalArgumentException, RepositoryException {
    String result = null;
    Value v = getValueAndThrow(pm, name);
    if (v == null) {
      return null;
    }
    result = v.getString();
    return result;
  }

  /*
   * Gets the InputStream value for a given property.
   */
  private static InputStream getStreamAndThrow(PropertyMap pm, String name)
      throws RepositoryException {
    InputStream result = null;
    Value v = getValueAndThrow(pm, name);
    if (v == null) {
      return null;
    }
    result = v.getStream();
    return result;
  }

  private static Value getValueAndThrow(PropertyMap pm, String name)
      throws RepositoryException {
    Property prop = pm.getProperty(name);
    if (prop == null) {
      return null;
    }
    Value v = prop.getValue();
    return v;
  }

  /*
   * Gets the value for a given property.
   */
  private static String getOptionalString(PropertyMap pm, String name) {
    String result = null;
    try {
      result = getStringAndThrow(pm, name);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
          "getOptionalString", "Swallowing exception while accessing " + name,
          e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
          "getOptionalString", "Swallowing exception while accessing " + name,
          e);
    }
    return result;
  }

  /*
   * Gets the value for a given property.
   */
  private static String getRequiredString(PropertyMap pm, String name) {
    String result = null;
    try {
      result = getStringAndThrow(pm, name);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
          "getRequiredString",
          "Catching exception, rethrowing as RuntimeException", e);
      throw new RuntimeException(e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
          "getRequiredString",
          "Catching exception, rethrowing as RuntimeException", e);
      throw new RuntimeException(e);
    }
    return result;
  }

  /*
   * Gets the value for a given property.
   */
  private static InputStream getOptionalStream(PropertyMap pm, String name) {
    InputStream result = null;
    try {
      result = getStreamAndThrow(pm, name);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
          "getOptionalStream", "Swallowing exception while accessing " + name,
          e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(),
          "getOptionalStream", "Swallowing exception while accessing " + name,
          e);
    }
    return result;
  }

  /*
   * Builds the xml string for a given property map.
   */
  protected InputStream buildXmlData(PropertyMap pm, String connectorName) {
    // build prefix
    StringBuffer prefix = new StringBuffer();
    prefix.append(XML_START);
    prefix.append(xmlWrapStart(XML_GSAFEED));
    prefix.append(xmlWrapStart(XML_HEADER));
    prefix.append(xmlWrapStart(XML_DATASOURCE));
    prefix.append(this.dataSource);
    prefix.append(xmlWrapEnd(XML_DATASOURCE));
    prefix.append(xmlWrapStart(XML_FEEDTYPE));
    prefix.append(this.feedType);
    prefix.append(xmlWrapEnd(XML_FEEDTYPE));
    prefix.append(xmlWrapEnd(XML_HEADER));
    prefix.append(xmlWrapStart(XML_GROUP));
    prefix.append("\n");

    // build suffix
    StringBuffer suffix = new StringBuffer();
    suffix.append(xmlWrapEnd(XML_GROUP));
    suffix.append(xmlWrapEnd(XML_GSAFEED));

    // build record
    String searchurl = null;
    if (this.feedType == XML_FEED_METADATA_AND_URL) {
      searchurl = getOptionalString(pm, SpiConstants.PROPNAME_SEARCHURL);
      // check that this looks like a URL
      try {
        URL url = new URL(searchurl);
      } catch (MalformedURLException e) {
        LOGGER.warning("Supplied search url " + searchurl + " is malformed: "
            + e.getMessage());
        return null;
      }
    } else {
      String docid = getRequiredString(pm, SpiConstants.PROPNAME_DOCID);
      searchurl = constructGoogleConnectorUrl(connectorName, docid);
    }

    InputStream encodedContentStream = null;
    if (this.feedType != XML_FEED_METADATA_AND_URL) {
      InputStream contentStream = getNonNullContentStream(
          getOptionalStream(pm, SpiConstants.PROPNAME_CONTENT));
 
      if (null != contentStream) {
        encodedContentStream = new Base64FilterInputStream(contentStream);
      }
    }

    Calendar lastModified = null;
    try {
      lastModified = getCalendarAndThrow(pm, SpiConstants.PROPNAME_LASTMODIFIED);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "buildXmlData",
          "Swallowing exception while getting "
              + SpiConstants.PROPNAME_LASTMODIFIED, e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "buildXmlData",
          "Swallowing exception while getting "
              + SpiConstants.PROPNAME_LASTMODIFIED, e);
    }
    String lastModifiedString = null;
    if (lastModified == null) {
      // maybe someone supplied a date as a string in some other format
      lastModifiedString =
          getOptionalString(pm, SpiConstants.PROPNAME_LASTMODIFIED);
    } else {
      lastModifiedString = SimpleValue.calendarToRfc822(lastModified);
    }

    String mimetype = getOptionalString(pm, SpiConstants.PROPNAME_MIMETYPE);
    if (mimetype == null) {
      mimetype = SpiConstants.DEFAULT_MIMETYPE;
    }

    String displayUrl = getOptionalString(pm, SpiConstants.PROPNAME_DISPLAYURL);

    InputStream recordInputStream =
        xmlWrapRecord(searchurl, displayUrl, lastModifiedString,
            encodedContentStream, mimetype, pm);

    InputStream is =
        stringWrappedInputStream(prefix.toString(), recordInputStream, suffix
            .toString());

    return is;
  }

  /**
   * DEFAULT_CONTENT is a string that is substituted for null or empty
   * content streams, in order to make sure the GSA indexes the feed item.
   */
  private static final String DEFAULT_CONTENT = " ";
  
  /**
   * Inspect the content stream for a feed item, and if it's null or empty,
   * substitute a string which will insure that the feed items gets indexed
   * by the GSA
   * @param contentStream from the feed item
   * @return an InputStream which is guaranteed to be non-null.
   * @throws RuntimeException if the DEFAULT_CONTENT string above cannot
   * be UTF-8-encoded into a ByteArrayInputStream.
   */
  private static InputStream getNonNullContentStream(
      InputStream contentStream) {
    InputStream output = contentStream;
    try {
      if (contentStream == null) {
        output = 
            new ByteArrayInputStream(DEFAULT_CONTENT.getBytes("UTF-8"));
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "IO error.", e);
      throw new RuntimeException("failed to create default content stream:" + 
          e.toString());
    }
    return output;
  }
  
  /**
   * Form a Google connector URL.
   * 
   * @param connectorName
   * @param docid
   * @return
   */
  private static String constructGoogleConnectorUrl(String connectorName, 
      String docid) {
    String searchurl;
    StringBuffer buf = new StringBuffer(ServletUtil.PROTOCOL);
    buf.append(connectorName);
    buf.append(".localhost/doc?docid=");
    buf.append(docid);
    searchurl = buf.toString();
    return searchurl;
  }

  /*
   * Returns URL Encoded data to be sent to feeder.
   */
  private InputStream encodeXmlData() throws UnsupportedEncodingException {
    String prefix =
        URLEncoder.encode(XML_DATASOURCE, XML_DEFAULT_ENCODING) + "="
            + URLEncoder.encode(dataSource, XML_DEFAULT_ENCODING);
    prefix +=
        "&" + URLEncoder.encode(XML_FEEDTYPE, XML_DEFAULT_ENCODING) + "="
            + URLEncoder.encode(feedType, XML_DEFAULT_ENCODING);
    prefix += "&" + URLEncoder.encode(XML_DATA, XML_DEFAULT_ENCODING) + "=";

    InputStream xmlDataStream = new UrlEncodedFilterInputStream(xmlData);
    String suffix = "";
    InputStream is = stringWrappedInputStream(prefix, xmlDataStream, suffix);
    return is;
  }

  private void setFeedType(PropertyMap pm) {
    if (getOptionalString(pm, SpiConstants.PROPNAME_SEARCHURL) != null) {
      this.feedType = XML_FEED_METADATA_AND_URL;
    } else {
      this.feedType = XML_FEED_INCREMENTAL;
    }
  }

  private static boolean getBoolean(String stringValue)
      throws IllegalArgumentException {
    if (stringValue.equalsIgnoreCase("t")
        || stringValue.equalsIgnoreCase("true")
        || stringValue.equalsIgnoreCase("y")
        || stringValue.equalsIgnoreCase("yes")
        || stringValue.equalsIgnoreCase("ok") || stringValue.equals("1")) {
      return true;
    }
    if (stringValue.equalsIgnoreCase("f")
        || stringValue.equalsIgnoreCase("false")
        || stringValue.equalsIgnoreCase("n")
        || stringValue.equalsIgnoreCase("no") || stringValue.equals("0")) {
      return false;
    }

    throw new IllegalArgumentException();
  }

  /**
   * Takes a property map and sends a the feed to the GSA.
   * 
   * @param pm PropertyMap corresponding to the document.
   * @param connectorName The connector name that fed this document
   */
  public void take(PropertyMap pm, String connectorName) throws PushException {
    this.dataSource = connectorName;
    setFeedType(pm);
    this.xmlData = buildXmlData(pm, connectorName);
    if (this.xmlData == null) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "take",
          "Skipped this document for feeding, continuing");
      return;
    }
    InputStream message = null;
    try {
      message = encodeXmlData();
      gsaResponse = feedConnection.sendData(message);
      if (!gsaResponse.equals(GsaFeedConnection.SUCCESS_RESPONSE)) {
        throw new PushException("gsaResponse=" + gsaResponse);
      }
    } catch (MalformedURLException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "take",
                  "Rethrowing MalformedURLException as PushException", e);
      throw new PushException("MalformedURLException: " + e.getMessage(), e);
    } catch (UnsupportedEncodingException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "take",
                  "Rethrowing UnsupportedEncodingException as PushException",
                  e);
      throw new PushException(
          "UnsupportedEncodingException: " + e.getMessage(), e);
    } catch (IOException e) {
      LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "take",
                  "Rethrowing IOException as PushException", e);
      throw new PushException("IOException: " + e.getMessage(), e);
    } finally {
      if (message != null) {
        try {
          message.close();
        } catch (IOException e) {
          LOGGER.logp(Level.WARNING, DocPusher.class.getName(), "take",
                      "Rethrowing IOException as PushException", e);
          throw new PushException("IOException: " + e.getMessage(), e);
        }
      }
    }
  }
}
