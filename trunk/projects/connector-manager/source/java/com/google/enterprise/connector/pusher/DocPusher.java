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
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.XmlUtils;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to generate xml feed for a document from the Document and send it
 * to GSA.
 */
public class DocPusher implements Pusher {
  private static final Logger LOGGER =
      Logger.getLogger(DocPusher.class.getName());
  private static final Logger FEED_WRAPPER_LOGGER =
      Logger.getLogger(LOGGER.getName() + ".FEED_WRAPPER");
  private static final Logger FEED_LOGGER = 
      Logger.getLogger(FEED_WRAPPER_LOGGER.getName() + ".FEED");
  private static final Level FEED_LOG_LEVEL = Level.FINER;

  /**
   * This field is used to construct a feed record in parallel to the main feed
   * InputStream construction.  It is only used if the feed logging level is set
   * to the appropriate level.  It only exists during the time the main feed is
   * being constructed.  Once sufficient information has been appended to this
   * buffer its contents will be logged and it will be nulled.
   */
  private StringBuffer feedLogRecord;

  private static Set propertySkipSet;

  static {
    propertySkipSet = new HashSet();
    propertySkipSet.add(SpiConstants.PROPNAME_CONTENT);
    propertySkipSet.add(SpiConstants.PROPNAME_DOCID);
  }

  // Strings for XML tags.
  public static final String XML_DEFAULT_ENCODING = "UTF-8";
  private static final String XML_START = "<?xml version='1.0' encoding='"
      + XML_DEFAULT_ENCODING + "'?><!DOCTYPE gsafeed PUBLIC"
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
  private static final String XML_ACTION = "action";
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
  private static final String XML_ADD = "add";
  private static final String XML_DELETE = "delete";

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

  public static Logger getFeedLogger() {
    return FEED_WRAPPER_LOGGER;
  }

  /**
   * Gets the response from GSA when the feed is sent. For testing only.
   *
   * @return gsaResponse response from GSA.
   */
  protected String getGsaResponse() {
    return gsaResponse;
  }

  private static InputStream stringWrappedInputStream(String prefix,
      InputStream is, String suffix) {
    InputStream result = null;

    try {
      ByteArrayInputStream prefixStream = new ByteArrayInputStream(prefix
          .getBytes(XML_DEFAULT_ENCODING));

      ByteArrayInputStream suffixStream = new ByteArrayInputStream(suffix
          .getBytes(XML_DEFAULT_ENCODING));
      InputStream[] inputStreams;
      if (is != null) {
        inputStreams = new InputStream[] {prefixStream, is, suffixStream};
      } else {
        inputStreams = new InputStream[] {prefixStream, suffixStream};
      }
      Enumeration inputStreamEnum = Collections.enumeration(Arrays
          .asList(inputStreams));
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
      String lastModified, InputStream content, String mimetype,
      ActionType actionType, Document document) {
    boolean metadataAllowed = true;
    boolean contentAllowed = true;
    // build prefix
    StringBuffer prefix = new StringBuffer();
    prefix.append("<");
    prefix.append(XML_RECORD);
    prefix.append(" ");
    XmlUtils.xmlAppendAttrValuePair(XML_URL, searchUrl, prefix);
    if (displayUrl != null && displayUrl.length() > 0) {
      prefix.append(" ");
      XmlUtils.xmlAppendAttrValuePair(XML_DISPLAY_URL, displayUrl, prefix);
    }
    if (actionType != null) {
      prefix.append(" ");
      if (actionType == ActionType.ADD) {
        XmlUtils.xmlAppendAttrValuePair(XML_ACTION, XML_ADD, prefix);
      } else if (actionType == ActionType.DELETE) {
        XmlUtils.xmlAppendAttrValuePair(XML_ACTION, XML_DELETE, prefix);
        metadataAllowed = false;
        contentAllowed = false;
      }
    }
    if (mimetype != null) {
      prefix.append(" ");
      XmlUtils.xmlAppendAttrValuePair(XML_MIMETYPE, mimetype, prefix);
    }
    if (lastModified != null) {
      prefix.append(" ");
      XmlUtils.xmlAppendAttrValuePair(XML_LAST_MODIFIED, lastModified, prefix);
    }
    try {
      ValueImpl v = (ValueImpl) Value.getSingleValue(document,
          SpiConstants.PROPNAME_ISPUBLIC);
      if (v != null) {
        boolean isPublic = v.toBoolean();
        if (!isPublic) {
          prefix.append(" ");
          XmlUtils.xmlAppendAttrValuePair(XML_AUTHMETHOD, CONNECTOR_AUTHMETHOD,
              prefix);
        }
      }
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Problem getting ispublic property.", e);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Illegal value for ispublic property."
          + " Treat as a public doc", e);
    }
    prefix.append(">\n");
    if (metadataAllowed) {
      xmlWrapMetadata(prefix, document);
    }
    if (feedType != XML_FEED_METADATA_AND_URL  && contentAllowed) {
      prefix.append("<");
      prefix.append(XML_CONTENT);
      prefix.append(" ");
      XmlUtils.xmlAppendAttrValuePair(XML_ENCODING, "base64binary", prefix);
      prefix.append(">");
    }

    // build suffix
    StringBuffer suffix = new StringBuffer();
    if (feedType != XML_FEED_METADATA_AND_URL && contentAllowed) {
      suffix.append(XmlUtils.xmlWrapEnd(XML_CONTENT));
    }
    suffix.append(XmlUtils.xmlWrapEnd(XML_RECORD));

    InputStream is = null;
    if (contentAllowed) {
      is = stringWrappedInputStream(prefix.toString(), content,
          suffix.toString());
    } else {
      is = stringWrappedInputStream(prefix.toString(), null,
          suffix.toString());
    }

    if (FEED_LOGGER.isLoggable(FEED_LOG_LEVEL)) {
      feedLogRecord.append(prefix);
      if (contentAllowed && content != null) {
        feedLogRecord.append("...content...");
      }
      feedLogRecord.append(suffix);
    }

    return is;
  }

  /**
   * Wrap the metadata and append it to the string buffer. Empty metadata
   * properties are not appended.
   *
   * @param buf string buffer
   * @param document Document
   */
  private static void xmlWrapMetadata(StringBuffer buf, Document document) {

    Set propertyNames = null;
    try {
      propertyNames = document.getPropertyNames();
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING,
          "Swallowing exception while starting getting property names", e);
    }

    if (propertyNames == null) {
      LOGGER.log(Level.WARNING, "Property names set is empty");
      return;
    }
    if (propertyNames.isEmpty()) {
      return;
    }

    buf.append(XmlUtils.xmlWrapStart(XML_METADATA));
    buf.append("\n");

    for (Iterator iter = propertyNames.iterator(); iter.hasNext();) {
      Property property = null;
      String name = (String) iter.next();
      if (propertySkipSet.contains(name)) {
        continue;
      }
      try {
        property = document.findProperty(name);
      } catch (RepositoryException e) {
        LOGGER.log(Level.WARNING,
            "Swallowing exception while reading properties", e);
        continue;
      }
      wrapOneProperty(buf, name, property);
    }
    buf.append(XmlUtils.xmlWrapEnd(XML_METADATA));
  }

  /**
   * Wrap a single Property and append to string buffer. Does nothing if the
   * Property's value is null or zero-length.
   *
   * @param buf string buffer
   * @param name the property's name
   * @param property Property
   */
  private static void wrapOneProperty(StringBuffer buf, String name,
      Property property) {
    /*
     * in case there are only null values, we want to "roll back" the XML_META
     * tag. So save our current length:
     */
    int indexMetaStart = buf.length();

    buf.append("<");
    buf.append(XML_META);
    buf.append(" ");
    XmlUtils.xmlAppendAttrValuePair("name", name, buf);
    buf.append(" content=\"");
    String delimiter = "";

    // mark the beginning of the values:
    int indexValuesStart = buf.length();
    ValueImpl value = null;
    while (true) {
      try {
        value = (ValueImpl) property.nextValue();
      } catch (RepositoryException e) {
        LOGGER.log(Level.WARNING,
            "Swallowing exception while scanning values for property " + name,
            e);
        continue;
      }
      if (value == null) {
        break;
      }
      wrapOneValue(buf, value, delimiter);
      delimiter = ", ";
    }
    /*
     * If there were no additions to buf (because of empty values), roll back to
     * before the XML_META tag
     */
    if (buf.length() > indexValuesStart) {
      buf.append("\"/>\n");
    } else {
      buf.delete(indexMetaStart, buf.length());
    }
  }

  private static void wrapOneValue(StringBuffer buf, ValueImpl value,
      String delimiter) {
    String valString = "";
    valString = value.toFeedXml();
    if (valString.length() == 0) {
      return;
    }
    buf.append(delimiter);
    XmlUtils.XmlEncodeAttrValue(valString, buf);
  }

  /*
   * Gets the Calendar value for a given property.
   */
  private static String getCalendarAndThrow(Document document, String name)
      throws IllegalArgumentException, RepositoryException {
    String result = null;
    ValueImpl v = getValueAndThrow(document, name);
    if (v == null) {
      result = null;
    } else if (v instanceof DateValue) {
      result = ((DateValue) v).toRfc822();
    } else {
      result = v.toFeedXml();
    }
    return result;
  }

  /*
   * Gets the String value for a given property.
   */
  private static String getStringAndThrow(Document document, String name)
      throws IllegalArgumentException, RepositoryException {
    String result = null;
    ValueImpl v = getValueAndThrow(document, name);
    if (v == null) {
      return null;
    }
    result = v.toFeedXml();
    return result;
  }

  /*
   * Gets the InputStream value for a given property.
   */
  private static InputStream getStreamAndThrow(Document document, String name)
      throws RepositoryException {
    InputStream result = null;
    ValueImpl v = getValueAndThrow(document, name);
    if (v == null) {
      return null;
    }
    if (v instanceof BinaryValue) {
      result = ((BinaryValue) v).getInputStream();
    } else {
      String s = v.toString();
      byte[] bytes;
      try {
        bytes = s.getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        result = null;
        return result;
      }
      result = new ByteArrayInputStream(bytes);
    }
    return result;
  }

  private static ValueImpl getValueAndThrow(Document document, String name)
      throws RepositoryException {
    ValueImpl v = null;
    v = (ValueImpl) Value.getSingleValue(document, name);
    return v;
  }

  /*
   * Gets the value for a given property.
   */
  private static String getOptionalString(Document document, String name) {
    String result = null;
    try {
      result = getStringAndThrow(document, name);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while accessing " + name,
          e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while accessing " + name,
          e);
    }
    return result;
  }

  /*
   * Gets the value for a given property.
   */
  private static String getRequiredString(Document document, String name) {
    String result = null;
    try {
      result = getStringAndThrow(document, name);
      if (result == null) {
        LOGGER.log(Level.WARNING, "Document missing required property " + name);
        throw new RuntimeException("Document missing required property " 
            + name);
      }
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING,
          "Catching exception, rethrowing as RuntimeException", e);
      throw new RuntimeException(e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING,
          "Catching exception, rethrowing as RuntimeException", e);
      throw new RuntimeException(e);
    }
    return result;
  }

  /*
   * Gets the value for a given property.
   */
  private static InputStream getOptionalStream(Document document, String name) {
    InputStream result = null;
    try {
      result = getStreamAndThrow(document, name);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while accessing " + name,
          e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while accessing " + name,
          e);
    }
    return result;
  }

  /*
   * Builds the xml string for a given document.
   */
  protected InputStream buildXmlData(Document document, String connectorName) {
    // build prefix
    StringBuffer prefix = new StringBuffer();
    prefix.append(XML_START);
    prefix.append(XmlUtils.xmlWrapStart(XML_GSAFEED));
    prefix.append(XmlUtils.xmlWrapStart(XML_HEADER));
    prefix.append(XmlUtils.xmlWrapStart(XML_DATASOURCE));
    prefix.append(this.dataSource);
    prefix.append(XmlUtils.xmlWrapEnd(XML_DATASOURCE));
    prefix.append(XmlUtils.xmlWrapStart(XML_FEEDTYPE));
    prefix.append(this.feedType);
    prefix.append(XmlUtils.xmlWrapEnd(XML_FEEDTYPE));
    prefix.append(XmlUtils.xmlWrapEnd(XML_HEADER));
    prefix.append(XmlUtils.xmlWrapStart(XML_GROUP));
    prefix.append("\n");

    // build suffix
    StringBuffer suffix = new StringBuffer();
    suffix.append(XmlUtils.xmlWrapEnd(XML_GROUP));
    suffix.append(XmlUtils.xmlWrapEnd(XML_GSAFEED));

    // build record
    String searchurl = null;
    if (this.feedType == XML_FEED_METADATA_AND_URL) {
      searchurl = getOptionalString(document, SpiConstants.PROPNAME_SEARCHURL);
      // check that this looks like a URL
      try {
        new URL(searchurl);
      } catch (MalformedURLException e) {
        LOGGER.warning("Supplied search url " + searchurl + " is malformed: "
            + e.getMessage());
        return null;
      }
    } else {
      String docid = getRequiredString(document, SpiConstants.PROPNAME_DOCID);
      searchurl = constructGoogleConnectorUrl(connectorName, docid);
    }

    InputStream encodedContentStream = null;
    if (this.feedType != XML_FEED_METADATA_AND_URL) {
      InputStream contentStream = getNonNullContentStream(getOptionalStream(
          document, SpiConstants.PROPNAME_CONTENT));

      if (null != contentStream) {
        encodedContentStream = new Base64FilterInputStream(contentStream);
      }
    }

    String lastModified = null;
    try {
      lastModified = getCalendarAndThrow(document,
          SpiConstants.PROPNAME_LASTMODIFIED);
      if (lastModified == null) {
        LOGGER.log(Level.FINEST, "Document does not contain "
            + SpiConstants.PROPNAME_LASTMODIFIED);
      }
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while getting "
          + SpiConstants.PROPNAME_LASTMODIFIED, e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while getting "
          + SpiConstants.PROPNAME_LASTMODIFIED, e);
    }

    String mimetype = getOptionalString(document,
        SpiConstants.PROPNAME_MIMETYPE);
    if (mimetype == null) {
      mimetype = SpiConstants.DEFAULT_MIMETYPE;
    }

    String displayUrl = getOptionalString(document,
        SpiConstants.PROPNAME_DISPLAYURL);

    ActionType actionType = null;
    String action = getOptionalString(document, SpiConstants.PROPNAME_ACTION);
    if (action != null) {
      // Compare to legal action types.
      actionType = ActionType.findActionType(action);
      if (actionType == ActionType.ERROR) {
        LOGGER.log(Level.WARNING, "Illegal tag used for ActionType: " + action);
        actionType = null;
      }
    }

    if (FEED_LOGGER.isLoggable(FEED_LOG_LEVEL)) {
      feedLogRecord = new StringBuffer();
      feedLogRecord.append(prefix);
    }

    InputStream recordInputStream = xmlWrapRecord(searchurl, displayUrl,
        lastModified, encodedContentStream, mimetype, actionType, document);

    InputStream is = stringWrappedInputStream(prefix.toString(),
        recordInputStream, suffix.toString());

    if (FEED_LOGGER.isLoggable(FEED_LOG_LEVEL)) {
      feedLogRecord.append(suffix);
      FEED_LOGGER.log(FEED_LOG_LEVEL, feedLogRecord.toString());
      feedLogRecord = null;
    }

    return is;
  }

  /**
   * DEFAULT_CONTENT is a string that is substituted for null or empty content
   * streams, in order to make sure the GSA indexes the feed item.
   */
  private static final String DEFAULT_CONTENT = " ";

  /**
   * Inspect the content stream for a feed item, and if it's null or empty,
   * substitute a string which will insure that the feed items gets indexed by
   * the GSA
   *
   * @param contentStream from the feed item
   * @return an InputStream which is guaranteed to be non-null.
   * @throws RuntimeException if the DEFAULT_CONTENT string above cannot be
   *         UTF-8-encoded into a ByteArrayInputStream.
   */
  private static InputStream getNonNullContentStream(InputStream contentStream) {
    InputStream output = contentStream;
    try {
      if (contentStream == null) {
        output = new ByteArrayInputStream(DEFAULT_CONTENT.getBytes("UTF-8"));
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "IO error.", e);
      throw new RuntimeException("failed to create default content stream:"
          + e.toString());
    }
    return output;
  }

  /**
   * Form a Google connector URL.
   *
   * @param connectorName
   * @param docid
   * @return the connector url
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

  private void setFeedType(Document document) {
    if (getOptionalString(document, SpiConstants.PROPNAME_SEARCHURL) != null) {
      this.feedType = XML_FEED_METADATA_AND_URL;
    } else {
      this.feedType = XML_FEED_INCREMENTAL;
    }
  }

  // FilterInputStream which "tees" all content to an OutputStream.
  // (named after the UNIX 'tee' command)
  private static class TeeInputStream extends FilterInputStream {
    protected OutputStream out;

    public TeeInputStream(InputStream in, OutputStream out) {
      super(in);
      this.out = out;
    }

    public int read() throws IOException {
      int retval = super.read();
      if (retval != -1) {
        out.write(retval);
      }
      return retval;
    }

    public int read(byte[] b, int off, int len) throws IOException {
      int retval = super.read(b, off, len);
      if (retval != -1) {
        out.write(b, off, retval);
      }
      return retval;
    }
  }

  /**
   * Takes a Document and sends a the feed to the GSA.
   *
   * @param document Document corresponding to the document.
   * @param connectorName The connector name that fed this document
   */
  public void take(Document document, String connectorName)
      throws PushException {
    this.dataSource = connectorName;
    setFeedType(document);
    this.xmlData = buildXmlData(document, connectorName);
    if (this.xmlData == null) {
      LOGGER.log(Level.WARNING,
          "Skipped this document for feeding, continuing");
      return;
    }
    // Setup the teedFeedFile if declared
    InputStream is = xmlData;
    String osFilename = Context.getInstance().getTeedFeedFile();
    File osFile = null;
    OutputStream os = null;
    if (osFilename != null) {
      osFile = new File(osFilename);
      try {
        os = new BufferedOutputStream(new FileOutputStream(osFile, true));
        is = new TeeInputStream(xmlData, os);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "cannot write file: " +
            osFile.getAbsolutePath(), e);
      }
    }
    try {
      gsaResponse = feedConnection.sendData(dataSource, feedType, is);
      if (!gsaResponse.equals(GsaFeedConnection.SUCCESS_RESPONSE)) {
        String eMessage = gsaResponse;
        if (GsaFeedConnection.UNAUTHORIZED_RESPONSE.equals(gsaResponse)) {
          eMessage += ": Client is not authorized to send feeds."
              + " Make sure the GSA is configured to trust feeds from your host.";
        } else if (GsaFeedConnection.INTERNAL_ERROR_RESPONSE.equals(gsaResponse)) {
          eMessage += ": Check GSA status or feed format.";
        }
        throw new PushException(eMessage);
      }
      LOGGER.finer("Document (" + document + ") from connector " +
          connectorName + " sent.");
    } catch (MalformedURLException e) {
      LOGGER.log(Level.WARNING,
          "Rethrowing MalformedURLException as PushException", e);
      throw new PushException("MalformedURLException: " + e.getMessage(), e);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Rethrowing IOException as PushException", e);
      throw new PushException("IOException: " + e.getMessage(), e);
    } finally {
      try {
        xmlData.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Rethrowing IOException as PushException", e);
        throw new PushException("IOException: " + e.getMessage(), e);
      }
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "cannot close file: " +
              osFile.getAbsolutePath());
        }
      }
    }
  }
}
