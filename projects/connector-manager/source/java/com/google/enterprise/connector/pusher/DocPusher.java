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
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Class to generate xml feed for a document from the Property Map and send it 
 * to GSA.
 */

public class DocPusher implements Pusher {
  
  // Strings for XML tags.
  private static final String XML_DEFAULT_ENCODING = "UTF-8";
  private static final String XML_START = 
    "<?xml version='1.0' encoding='" 
    + XML_DEFAULT_ENCODING
    + "'?><!DOCTYPE gsafeed PUBLIC"
    + " \"-//Google//DTD GSA Feeds//EN\" \"gsafeed.dtd\">";
  private static final String XML_GSAFEED = "gsafeed";
  private static final String XML_HEADER = "header";
  private static final String XML_DATASOURCE = "datasource";
  private static final String XML_FEEDTYPE = "feedtype";
  private static final String XML_GROUP = "group";
  private static final String XML_RECORD = "record";
//  private static final String XML_METADATA = "metadata";
//  private static final String XML_META = "meta";
  private static final String XML_CONTENT = "content";
//  private static final String XML_ACTION = "action";
  private static final String XML_URL = "url";
  private static final String XML_MIMETYPE = "mimetype";
  private static final String XML_LAST_MODIFIED = "last-modified";
//  private static final String XML_LOCK = "lock";
//  private static final String XML_AUTHMETHOD = "authmethod";
//  private static final String XML_NAME = "name";
  private static final String XML_ENCODING = "encoding";
  
//  private static final String XML_FULL = "full";
//  private static final String XML_INCREMENTAL = "incremental";
//  private static final String XML_BASE64BINARY = "base64binary";
//  private static final String XML_ADD = "add";
//  private static final String XML_DELETE = "delete";
  
  private String dataSource;
  private String feedType;
  private String action;
  private FeedConnection feedConnection;
  private String mimetype;
  
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
    this.action = "add";  
    // TODO Remove this once we are able to get the mimetype from the property map.
    this.mimetype = "text/plain";
  }
  
  /**
   * Retrieves the xml String to be fed into GSA.  For testing only.
   * @return xmlData xml string that can be fed into GSA.
   */
  protected String getXmlData() {
    return xmlData;
  }

  /**
   * Gets the response from GSA when the feed is sent.  For testing only.
   * @return gsaResponse response from GSA.
   */
  protected String getGsaResponse() {
    return gsaResponse;
  }

  /*
   * Converts a string to its base64 encoding.
   * @param str the string to be encoded
   * @return the base64 encoding of original input string
   * @throws IOException
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
    buf.append(">");
    return buf.toString();
  }
  
  /*
   * Generate the record tag for the xml data.
   */
  private String xmlWrapRecord(String contentUrl, String lastModified, String content) {
    StringBuffer buf = new StringBuffer();
    buf.append("<");
    buf.append(XML_RECORD);
    buf.append(" ");
    buf.append(XML_URL);
    buf.append("=\"");
    buf. append(contentUrl);
    buf.append("\" ");
    buf.append(XML_MIMETYPE);
    buf.append("=\"");
    buf.append(mimetype);
    buf.append("\" ");
    buf.append(XML_LAST_MODIFIED);
    buf.append("=\"");
    buf.append(lastModified);
    buf.append("\">");
    buf.append("<");
    buf.append(XML_CONTENT);
    buf.append(" ");
    buf.append(XML_ENCODING);
    buf.append("=\"base64binary\">");
    buf.append(content);
    buf.append(xmlWrapEnd(XML_CONTENT));
    buf.append(xmlWrapEnd(XML_RECORD));
    return buf.toString();
  }
  
  /*
   * Gets the value for a given property.
   */
  private String getPropValue(Property p ) {
    try {
      Value v = p.getValue();
      return v.getString();
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }
  
  /*
   * Builds the xml string for a given property map.
   */
  protected String buildXmlData(PropertyMap pm) {
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
    
    // Gets contenturl property from the property map.
    Property contentUrlProp = null;
    try {
      contentUrlProp = pm.getProperty(SpiConstants.PROPNAME_CONTENTURL);
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    if (contentUrlProp == null) {
      throw new IllegalArgumentException(SpiConstants.PROPNAME_CONTENTURL 
        + " is missing");
    }
    String contentUrl = getPropValue(contentUrlProp);
    
    //Gets the content property from the property map.
    Property contentProp = null;
    try {
      contentProp = pm.getProperty(SpiConstants.PROPNAME_CONTENT);
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    if (contentProp == null) {
      throw new IllegalArgumentException(SpiConstants.PROPNAME_CONTENT
        + " is missing");
    }
    String content = null;
    try {
      content = base64Encode(getPropValue(contentProp));
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    //Gets the lastmodify property from the property map.
    Property lastModifiedProp = null;
    try {
      lastModifiedProp = pm.getProperty(SpiConstants.PROPNAME_LASTMODIFY);
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    if (lastModifiedProp == null) {
      throw new IllegalArgumentException(SpiConstants.PROPNAME_LASTMODIFY
        + " is missing");
    }
    String lastModified = getPropValue(lastModifiedProp);
    
    //TODO Get the mimetype property from the property map.
    
    xmlData.append(xmlWrapRecord(contentUrl, lastModified, content));
    
    xmlData.append(xmlWrapEnd(XML_GROUP));
    xmlData.append(xmlWrapEnd(XML_GSAFEED));
    
    return xmlData.toString();
  }
  
  /*
   * Urlencodes the xml string.
   */
  private String encodeXmlData() throws UnsupportedEncodingException {
    String data =
      URLEncoder.encode("datasource", XML_DEFAULT_ENCODING)
      + "=" + URLEncoder.encode(dataSource, XML_DEFAULT_ENCODING);
    data +=
      "&" + URLEncoder.encode("feedtype", XML_DEFAULT_ENCODING)
      + "=" + URLEncoder.encode(feedType, XML_DEFAULT_ENCODING);
    data +=
      "&" + URLEncoder.encode("data", XML_DEFAULT_ENCODING)
      + "=" + URLEncoder.encode(xmlData, XML_DEFAULT_ENCODING);
    return data;
  }
  
  /**
   * Takes a property map and sends a the feed to the GSA.
   * @param pm PropertyMap corresponding to the document.
   */
  public void take(PropertyMap pm) {
    xmlData = buildXmlData(pm);
    URL feedUrl = null;
    try {
      String encodedXmlData = encodeXmlData();
      gsaResponse = feedConnection.sendData(encodedXmlData);      
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
