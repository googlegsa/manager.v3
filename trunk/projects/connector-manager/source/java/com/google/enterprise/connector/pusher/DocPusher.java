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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.common.Base64Encoder;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.common.WorkQueue;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to generate xml feed for a document from the Property Map and send it
 * to GSA.
 */

public class DocPusher implements Pusher {

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
  private static final String XML_GROUP = "group";
  private static final String XML_RECORD = "record";
  private static final String XML_METADATA = "metadata";
  private static final String XML_META = "meta";
  private static final String XML_CONTENT = "content";
  // private static final String XML_ACTION = "action";
  private static final String XML_URL = "url";
  private static final String XML_MIMETYPE = "mimetype";
  private static final String XML_LAST_MODIFIED = "last-modified";
  // private static final String XML_LOCK = "lock";
  // private static final String XML_AUTHMETHOD = "authmethod";
  // private static final String XML_NAME = "name";
  private static final String XML_ENCODING = "encoding";

  // private static final String XML_FULL = "full";
  // private static final String XML_INCREMENTAL = "incremental";
  // private static final String XML_BASE64BINARY = "base64binary";
  // private static final String XML_ADD = "add";
  // private static final String XML_DELETE = "delete";

  private String dataSource;
  private String feedType;
  private FeedConnection feedConnection;

  private String xmlData;
  private String gsaResponse;

  /**
   * 
   * @param dataSource datasource for the feed
   * @param feedConnection a connection
   */
  public DocPusher(String dataSource, FeedConnection feedConnection) {
    this.dataSource = dataSource;
    this.feedConnection = feedConnection;
    this.feedType = "full";
  }

  /**
   * Retrieves the xml String to be fed into GSA. For testing only.
   * 
   * @return xmlData xml string that can be fed into GSA.
   */
  protected String getXmlData() {
    return xmlData;
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
   * Converts a string to its base64 encoding. @param str the string to be
   * encoded @return the base64 encoding of original input string @throws
   * IOException
   */
  private static String base64Encode(String str) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream(str.getBytes(XML_DEFAULT_ENCODING));
    StringWriter sw = new StringWriter();
    Base64Encoder.encode(inputStream, sw);
    sw.flush();
    return sw.toString();
  }

  /*
   * Wraps an xm tag with < and >.
   */
  private String xmlWrapStart(String str) {
    StringBuffer buf = new StringBuffer();
    buf.append("<");
    buf.append(str);
    buf.append(">");
    return buf.toString();
  }

  /*
   * Wraps an xml tag with </ and >.
   */
  private String xmlWrapEnd(String str) {
    StringBuffer buf = new StringBuffer();
    buf.append("</");
    buf.append(str);
    buf.append(">\n");
    return buf.toString();
  }
  
  /*
   * Generate the record tag for the xml data.
   */
  private String xmlWrapRecord(String searchUrl, String lastModified,
      String content, String mimetype, PropertyMap pm) {
    StringBuffer buf = new StringBuffer();
    buf.append("<");
    buf.append(XML_RECORD);
    buf.append(" ");
    buf.append(XML_URL);
    buf.append("=\"");
    buf.append(searchUrl);
    buf.append("\" ");
    buf.append(XML_MIMETYPE);
    buf.append("=\"");
    buf.append(mimetype);
    buf.append("\"");
    if (lastModified != null) {
      buf.append(" ");
      buf.append(XML_LAST_MODIFIED);
      buf.append("=\"");
      buf.append(lastModified);
      buf.append("\"");
    }
    buf.append(">\n");
    xmlWrapMetadata(buf, pm);
    buf.append("<");
    buf.append(XML_CONTENT);
    buf.append(" ");
    buf.append(XML_ENCODING);
    buf.append("=\"base64binary\">\n");
    buf.append(content);
    buf.append("\n");
    buf.append(xmlWrapEnd(XML_CONTENT));
    buf.append(xmlWrapEnd(XML_RECORD));
    return buf.toString();
  }

  private void xmlWrapMetadata(StringBuffer buf, PropertyMap pm) {
    Iterator i;
    try {
      i = pm.getProperties();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "xmlWrapMetadata",
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
        LOGGER.logp(Level.WARNING, this.getClass().getName(),
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

  private void wrapOneProperty(StringBuffer buf, Property p) {
    String name;
    try {
      name = p.getName();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "xmlWrapMetadata",
          "Swallowing exception while scanning values", e);
      return;
    }
    Iterator values;
    try {
      values = p.getValues();
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "xmlWrapMetadata",
          "Swallowing exception while scanning values", e);
      return;
    }
    buf.append("<");
    buf.append(XML_META);
    buf.append(" name=\"");
    buf.append(name);
    buf.append("\" content=\"");
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
      buf.append(delimiter);
      // TODO: escape quotes
      buf.append(valString);
      delimiter = ", ";
    }
    buf.append("\"/>\n");
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
  protected String buildXmlData(PropertyMap pm, String connectorName) {
    StringBuffer xmlData = new StringBuffer();
    xmlData.append(XML_START);
    xmlData.append(xmlWrapStart(XML_GSAFEED));
    xmlData.append(xmlWrapStart(XML_HEADER));
    xmlData.append(xmlWrapStart(XML_DATASOURCE));
    xmlData.append(dataSource);
    xmlData.append(xmlWrapEnd(XML_DATASOURCE));
    xmlData.append(xmlWrapStart(XML_FEEDTYPE));
    xmlData.append(feedType);
    xmlData.append(xmlWrapEnd(XML_FEEDTYPE));
    xmlData.append(xmlWrapEnd(XML_HEADER));
    xmlData.append(xmlWrapStart(XML_GROUP));
    xmlData.append("\n");

    String searchurl = getOptionalString(pm, SpiConstants.PROPNAME_SEARCHURL);
    if (searchurl != null) {
      // TODO: validate that this looks like a URL
      ;
    } else {
      String docid = getRequiredString(pm, SpiConstants.PROPNAME_DOCID);
      StringBuffer buf = new StringBuffer("googleconnector://");
      buf.append(connectorName);
      buf.append(".localhost?docid=");
      buf.append(docid);
      searchurl = buf.toString();
    }

    String contentUrl = getOptionalString(pm, SpiConstants.PROPNAME_CONTENTURL);

    InputStream contentStream = 
      getOptionalStream(pm, SpiConstants.PROPNAME_CONTENT);
    String content;
    
    try {
      content = StringUtils.streamToString(contentStream);
      content = base64Encode(content);
    } catch (IOException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "buildXmlData",
          "Swallowing exception while base64-encoding.", e);
      content = "";
    }

    Calendar lastModified = null;
    try {
      lastModified = getCalendarAndThrow(pm, SpiConstants.PROPNAME_LASTMODIFY);
    } catch (IllegalArgumentException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "buildXmlData",
          "Swallowing exception while getting "
              + SpiConstants.PROPNAME_LASTMODIFY, e);
    } catch (RepositoryException e) {
      LOGGER.logp(Level.WARNING, this.getClass().getName(), "buildXmlData",
          "Swallowing exception while getting "
              + SpiConstants.PROPNAME_LASTMODIFY, e);
    }
    String lastModifiedString;
    if (lastModified == null) {
      // maybe someone supplied a date as a string in some other format
      lastModifiedString =
          getOptionalString(pm, SpiConstants.PROPNAME_LASTMODIFY);
    } else {
      lastModifiedString = SimpleValue.calendarToRfc822(lastModified);
    }

    String mimetype = getOptionalString(pm, SpiConstants.PROPNAME_MIMETYPE);
    if (mimetype == null) {
      mimetype = SpiConstants.DEFAULT_MIMETYPE;
    }

    xmlData.append(xmlWrapRecord(searchurl, lastModifiedString, content,
        mimetype, pm));

    xmlData.append(xmlWrapEnd(XML_GROUP));
    xmlData.append(xmlWrapEnd(XML_GSAFEED));

    return xmlData.toString();
  }

  /*
   * Composes the final message
   */
  private String composeMessage() {
    StringBuffer buf = new StringBuffer(8192);
    buf.append("datasource=");
    buf.append(dataSource);
    buf.append("&feedtype=");
    buf.append(feedType);
    buf.append("&data=");
    buf.append(xmlData);
    return buf.toString();
  }

  /**
   * Takes a property map and sends a the feed to the GSA.
   * 
   * @param pm PropertyMap corresponding to the document.
   * @param connectorName The connector name that fed this document
   */
  public void take(PropertyMap pm, String connectorName) {
    xmlData = buildXmlData(pm, connectorName);
    URL feedUrl = null;
    try {
      String message = composeMessage();
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
    }
  }
}
