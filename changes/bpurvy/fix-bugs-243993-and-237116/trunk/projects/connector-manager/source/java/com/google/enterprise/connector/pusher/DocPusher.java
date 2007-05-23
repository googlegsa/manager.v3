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
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SimplePropertyMap;
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

  private void appendAttrValuePair(String attrName, String value,
      StringBuffer buf) {
    buf.append(attrName);
    buf.append("=\"");
    XmlEncodeAttrValue(value, buf);
    buf.append("\" ");
  }

  /**
   * Wrap the metadata and append it to the string buffer. Empty metadata
   * properties are not appended. Returns an indication of whether there
   * was any metadata.
   * @param buf string buffer
   * @param pm property map
   * @return true if any metadata was appended; false if not.
   */
  private boolean xmlWrapMetadata(StringBuffer buf, PropertyMap pm) {
    Iterator i;
    try {
      i = pm.getProperties();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "xmlWrapMetadata",
          "Swallowing exception while scanning properties", e);
      return false;
    }
    if (!i.hasNext()) {
      return false;
    }
    /**
     *  accumulate metadata in a separate buffer, so we can avoid changing
     *  'buf' if all properties were null.
     */
    StringBuffer metadataBuf = new StringBuffer();
    metadataBuf.append(xmlWrapStart(XML_METADATA));
    metadataBuf.append("\n");
    boolean dataWasAppended = false;
    while (i.hasNext()) {
      Property p = (Property) i.next();
      String name;
      try {
        name = p.getName();
      } catch (RepositoryException e) {
        LOGGER.logp(Level.WARNING, this.getClass().getName(),
            "xmlWrapMetadata",
            "Swallowing exception while scanning properties", e);
        continue;
      }
      if (!propertySkipSet.contains(name)) {
        dataWasAppended |= wrapOneProperty(metadataBuf, p);
      }
    }
    if (dataWasAppended) {
      metadataBuf.append(xmlWrapEnd(XML_METADATA));
      buf.append(metadataBuf);
      return true;
    } 
    return false;
  }

  /**
   * Wrap a single Property and append to string buffer. Does nothing if
   * the Property's value is null or zero-length. Returns an indication of
   * whether any data was appended.
   * @param buf string buffer
   * @param p Property
   * @return true if any data was appended; false if not.
   */
  private boolean wrapOneProperty(StringBuffer buf, Property p) {
    String name;
    boolean dataWasAppended = false;
    try {
      name = p.getName();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "xmlWrapMetadata",
          "Swallowing exception while scanning values", e);
      return dataWasAppended;
    }
    Iterator values;
    try {
      values = p.getValues();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "xmlWrapMetadata",
          "Swallowing exception while scanning values", e);
      return dataWasAppended;
    }
    // if property is null, don't encode it; GSA won't process 
    if (!values.hasNext()) return dataWasAppended;
    StringBuffer metadataBuf = new StringBuffer();
    metadataBuf.append("<");
    metadataBuf.append(XML_META);
    metadataBuf.append(" ");
    appendAttrValuePair("name", name, metadataBuf);
    metadataBuf.append("content=\"");
    String delimiter = "";
    
    while (values.hasNext()) {
      Value value = (Value) values.next();
      String valString = "";
      try {
        valString = value.getString();
      } catch (IllegalArgumentException e) {
        LOGGER.logp(Level.WARNING, this.getClass().getName(),
            "xmlWrapMetadata", "Swallowing exception while accessing property "
                + name, e);
        continue;
      } catch (RepositoryException e) {
        LOGGER.logp(Level.WARNING, this.getClass().getName(),
            "xmlWrapMetadata", "Swallowing exception while accessing property "
                + name, e);
        continue;
      }
      if (valString.length() < 1) {
        continue;
      }
      metadataBuf.append(delimiter);
      XmlEncodeAttrValue(valString, metadataBuf);
      dataWasAppended = true;
      delimiter = ", ";
    }
    /* If there were no additions to metadataBuf (because of empty values), 
     * don't append it to the StringBuffer passed in.
     */
    if (dataWasAppended) {
      metadataBuf.append("\"/>\n");
      buf.append(metadataBuf);
    }
    return dataWasAppended;
  }

  /*
   * Gets the Calendar value for a given property.
   */
  private Calendar getCalendarAndThrow(PropertyMap pm, String name)
      throws IllegalArgumentException, RepositoryException {
    Calendar result = null;
    Value v = getValueAndThrow(pm, name);
    result = v.getDate();
    return result;
  }

  /*
   * Gets the String value for a given property.
   */
  private String getStringAndThrow(PropertyMap pm, String name)
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
  private InputStream getStreamAndThrow(PropertyMap pm, String name)
      throws RepositoryException {
    InputStream result = null;
    Value v = getValueAndThrow(pm, name);
    if (v == null) {
      return null;
    }
    result = v.getStream();
    return result;
  }

  private Value getValueAndThrow(PropertyMap pm, String name)
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
  private String getOptionalString(PropertyMap pm, String name) {
    String result = null;
    try {
      result = getStringAndThrow(pm, name);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(),
          "getOptionalString", "Swallowing exception while accessing " + name,
          e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(),
          "getOptionalString", "Swallowing exception while accessing " + name,
          e);
    }
    return result;
  }

  /*
   * Gets the value for a given property.
   */
  private String getRequiredString(PropertyMap pm, String name) {
    String result = null;
    try {
      result = getStringAndThrow(pm, name);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(),
          "getRequiredString",
          "Catching exception, rethrowing as RuntimeException", e);
      throw new RuntimeException(e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(),
          "getRequiredString",
          "Catching exception, rethrowing as RuntimeException", e);
      throw new RuntimeException(e);
    }
    return result;
  }

  /*
   * Gets the value for a given property.
   */
  private InputStream getOptionalStream(PropertyMap pm, String name) {
    InputStream result = null;
    try {
      result = getStreamAndThrow(pm, name);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(),
          "getOptionalStream", "Swallowing exception while accessing " + name,
          e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(),
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
    prefix.append(dataSource);
    prefix.append(xmlWrapEnd(XML_DATASOURCE));
    prefix.append(xmlWrapStart(XML_FEEDTYPE));
    prefix.append(feedType);
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
      InputStream contentStream = new GoogleContentStream(
          getOptionalStream(pm, SpiConstants.PROPNAME_CONTENT));
      if (null != contentStream) {
        encodedContentStream = new Base64FilterInputStream(contentStream);
      }
    }

    Calendar lastModified = null;
    try {
      lastModified = getCalendarAndThrow(pm, SpiConstants.PROPNAME_LASTMODIFIED);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "buildXmlData",
          "Swallowing exception while getting "
              + SpiConstants.PROPNAME_LASTMODIFIED, e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "buildXmlData",
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
   * Form a Google connector URL.
   * 
   * @param connectorName
   * @param docid
   * @return
   */
  private String constructGoogleConnectorUrl(String connectorName, String docid) {
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

  private boolean getBoolean(String stringValue)
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
  public void take(PropertyMap pm, String connectorName) {
    this.dataSource = connectorName;
    setFeedType(pm);
    this.xmlData = buildXmlData(pm, connectorName);
    if (this.xmlData == null) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "take",
          "Skipped this document for feeding, continuing");
      return;
    }
    InputStream message = null;
    try {
      message = encodeXmlData();
      gsaResponse = feedConnection.sendData(message);
    } catch (MalformedURLException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "take",
          "Received exception while feeding, continuing", e);
    } catch (UnsupportedEncodingException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "take",
          "Received exception while feeding, continuing", e);
    } catch (IOException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "take",
          "Received exception while feeding, continuing", e);
    } finally {
      if (message != null) {
        try {
          message.close();
        } catch (IOException e) {
          LOGGER.logp(Level.WARNING, this.getClass().getName(), "take",
              "Received exception while closing input stream, continuing", e);
        }
      }
    }
  }
}
