// Copyright (C) 2006-2009 Google Inc.
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
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.XmlUtils;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;
import com.google.enterprise.connector.traversal.FileSizeLimitInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
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
   * This is used to build up a multi-record feed.  Documents are added
   * to the feed until the size of the feed exceeds the maxFeedSize
   * or we are finished with the batch of documents. The feed is then
   * submitted to the feed connection.
   */
  private final ThreadLocal<XmlFeed> xmlFeed = new ThreadLocal<XmlFeed>();

  /**
   * Once the accumulated feed exceeds this value, close it and sumbit.
   */
  // Default value is smallish 1MB, for the convenience of unit testing.
  private static int maxFeedSize = 1024 * 1024;

  /**
   * Configured maximum file size supported.
   */
  private FileSizeLimitInfo fileSizeLimit = new FileSizeLimitInfo();

  /**
   * This field is used to construct a feed record in parallel to the main feed
   * InputStream construction.  It is only used if the feed logging level is set
   * to the appropriate level.  It only exists during the time the main feed is
   * being constructed.  Once sufficient information has been appended to this
   * buffer its contents will be logged and it will be nulled.
   */
  private final ThreadLocal<StringBuilder> feedLog =
      new ThreadLocal<StringBuilder>();

  private static Set<String> propertySkipSet;
  static {
    propertySkipSet = new HashSet<String>();
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

  private final FeedConnection feedConnection;
  private String gsaResponse;

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
   * Set the maximum size of an accumulated feed file.
   * The pusher might actually exceed this maximum by
   * the size of a single document's feed record, so
   * don't get too close to the GSA's absolute maximum
   * size of 1GB.
   *
   * @param maximumFeedSize maximum size to accumulated in
   *        feed, before sending the feed on to the GSA.
   */
  public void setMaximumFeedSize(int maximumFeedSize) {
    if (maxFeedSize > (900 * 1024 * 1024)) {
      // Don't exceed the GSA maximum feed size of 1GB.
      maxFeedSize = 900 * 1024 * 1024;
    } else {
      maxFeedSize = maximumFeedSize;
    }
  }

  /**
   * Set the maximum size of the document content.
   */
  public void setFileSizeLimitInfo(FileSizeLimitInfo fileSizeLimitInfo) {
    this.fileSizeLimit = fileSizeLimitInfo;
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
      InputStream is, String suffix)  throws RepositoryException {
    InputStream result = null;
    try {
      if (is == null) {
        return new ByteArrayInputStream((prefix + suffix)
            .getBytes(XML_DEFAULT_ENCODING));
      }
      ByteArrayInputStream prefixStream = new ByteArrayInputStream(prefix
          .getBytes(XML_DEFAULT_ENCODING));

      ByteArrayInputStream suffixStream = new ByteArrayInputStream(suffix
          .getBytes(XML_DEFAULT_ENCODING));

      InputStream[] inputStreams;
      inputStreams = new InputStream[] {prefixStream, is, suffixStream};

      Enumeration<InputStream> inputStreamEnum = Collections.enumeration(
          Arrays.asList(inputStreams));
      result = new SequenceInputStream(inputStreamEnum);
    } catch (UnsupportedEncodingException e) {
      LOGGER.log(Level.SEVERE, "Encoding error.", e);
      throw new RepositoryDocumentException("Encoding error.", e);
    }
    return result;
  }

  /*
   * Generate the record tag for the xml data.
   */
  private InputStream xmlWrapRecord(String searchUrl, String displayUrl,
      String lastModified, InputStream content, String mimetype,
      ActionType actionType, Document document, String feedType)
      throws RepositoryException {
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
          // TODO(martyg): When the GSA is ready to take ACLUSERS and ACLGROUPS,
          // this is the place where those properties should be pulled out of
          // meta data and into the proper ACL Entry element.
          prefix.append(" ");
          XmlUtils.xmlAppendAttrValuePair(XML_AUTHMETHOD, CONNECTOR_AUTHMETHOD,
              prefix);
        }
      }
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Illegal value for ispublic property."
          + " Treat as a public doc", e);
    }
    prefix.append(">\n");
    if (metadataAllowed) {
      xmlWrapMetadata(prefix, document);
    }
    if (!feedType.equals(XML_FEED_METADATA_AND_URL)  && contentAllowed) {
      prefix.append("<");
      prefix.append(XML_CONTENT);
      prefix.append(" ");
      XmlUtils.xmlAppendAttrValuePair(XML_ENCODING, "base64binary", prefix);
      prefix.append(">\n");
    }

    // build suffix
    StringBuffer suffix = new StringBuffer();
    if (feedType != XML_FEED_METADATA_AND_URL && contentAllowed) {
      suffix.append('\n').append(XmlUtils.xmlWrapEnd(XML_CONTENT));
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
      StringBuilder log = feedLog.get();
      log.append(prefix);
      if (contentAllowed && content != null) {
        log.append("...content...");
      }
      log.append(suffix);
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
  private static void xmlWrapMetadata(StringBuffer buf, Document document)
      throws RepositoryException {
    Set<String> propertyNames = document.getPropertyNames();
    if (propertyNames == null) {
      LOGGER.log(Level.WARNING, "Property names set is empty");
      return;
    }
    if (propertyNames.isEmpty()) {
      return;
    }

    buf.append(XmlUtils.xmlWrapStart(XML_METADATA));
    buf.append("\n");

    for (String name : propertyNames) {
      Property property = null;
      if (propertySkipSet.contains(name) ||
          name.startsWith(SpiConstants.USER_ROLES_PROPNAME_PREFIX) ||
          name.startsWith(SpiConstants.GROUP_ROLES_PROPNAME_PREFIX)) {
        continue;
      }
      if (SpiConstants.PROPNAME_ACLGROUPS.equals(name) ||
          SpiConstants.PROPNAME_ACLUSERS.equals(name)) {
        property = processAclProperty(name, document);
      } else {
        property = document.findProperty(name);
      }
      if (property != null) {
        wrapOneProperty(buf, name, property);
      }
    }
    buf.append(XmlUtils.xmlWrapEnd(XML_METADATA));
  }

  /**
   * Utility function to convert a set of document properties that look like:
   * <pre>
   *   google:aclusers=[joe, mary, admin]
   *   google:user:roles:joe=[reader]
   *   google:user:roles:mary=[reader, writer]
   *   google:user:roles:admin=[owner]
   * </pre>
   * into one property that looks like:
   * <pre>
   *   google:aclusers=[joe=reader, mary=reader, mary=writer, admin=owner]
   * </pre>
   *
   * @param aclPropName the name of the property being processed.  Should be one
   *        of {@link SpiConstants#PROPNAME_ACLGROUPS} or
   *        {@link SpiConstants#PROPNAME_ACLUSERS}.
   * @param document the document being processed.
   * @return either the original property if no conversion was necessary or a
   *         new converted property containing ACL Entries.
   * @throws RepositoryException if there was a problem extracting properties.
   */
  private static Property processAclProperty(String aclPropName,
      Document document) throws RepositoryException {
    Property scopeProp = document.findProperty(aclPropName);
    List<Value> aclEntryList = new ArrayList<Value>();
    boolean aclPropWasModified = false;
    Value scopeVal = null;
    while ((scopeVal = scopeProp.nextValue()) != null) {
      String aclScope = scopeVal.toString();
      Property scopeRoleProp = null;
      if (SpiConstants.PROPNAME_ACLGROUPS.equals(aclPropName)) {
        scopeRoleProp = document.findProperty(
            SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + aclScope);
      } else if (SpiConstants.PROPNAME_ACLUSERS.equals(aclPropName)) {
        scopeRoleProp = document.findProperty(
            SpiConstants.USER_ROLES_PROPNAME_PREFIX + aclScope);
      }
      if (scopeRoleProp != null) {
        // Add ACL Entry (scope=role pair) to the list.
        Value roleVal = null;
        while ((roleVal = scopeRoleProp.nextValue()) != null) {
          String aclRole = roleVal.toString();
          StringBuffer aclEntry = new StringBuffer(aclScope).append("=").
              append(aclRole);
          aclEntryList.add(Value.getStringValue(aclEntry.toString()));
          aclPropWasModified = true;
        }
      } else {
        // Just add scope to the list.
        aclEntryList.add(Value.getStringValue(aclScope));
      }
    }

    if (aclPropWasModified) {
      // Need to create a new Property.
      return new SimpleProperty(aclEntryList);
    } else {
      // Have to return a fresh property so next values can be retrieved.
      return document.findProperty(aclPropName);
    }
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
      Property property) throws RepositoryException {
    // In case there are only null values, we want to "roll back" the
    // XML_META tag. So save our current length:
    int indexMetaStart = buf.length();

    buf.append("<");
    buf.append(XML_META);
    buf.append(" ");
    XmlUtils.xmlAppendAttrValuePair("name", name, buf);
    buf.append(" content=\"");

    // Mark the beginning of the values:
    int indexValuesStart = buf.length();
    String delimiter = "";
    ValueImpl value = null;
    while ((value = (ValueImpl) property.nextValue()) != null) {
      wrapOneValue(buf, value, delimiter);
      delimiter = ", ";
    }

    // If there were no additions to buf (because of empty values),
    // roll back to before the XML_META tag.
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
    String result;
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
      throws RepositoryException {
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
        throw new RepositoryDocumentException("Encoding error." , e);
      }
      result = new ByteArrayInputStream(bytes);
    }
    return result;
  }

  private static ValueImpl getValueAndThrow(Document document, String name)
      throws RepositoryException {
    return (ValueImpl) Value.getSingleValue(document, name);
  }

  /*
   * Gets the value for a given property.
   */
  private static String getOptionalString(Document document, String name)
      throws RepositoryException {
    String result = null;
    try {
      result = getStringAndThrow(document, name);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while accessing " + name,
          e);
    }
    return result;
  }

  /*
   * Gets the value for a given property.
   */
  private static String getRequiredString(Document document, String name)
      throws RepositoryException {
    String result = null;
    result = getStringAndThrow(document, name);
    if (result == null) {
      LOGGER.log(Level.WARNING, "Document missing required property " + name);
      throw new RepositoryDocumentException(
          "Document missing required property " + name);
    }
    return result;
  }

  /*
   * Gets the value for a given property.
   */
  private static InputStream getOptionalStream(Document document, String name)
      throws RepositoryException {
    InputStream result = null;
    try {
      result = getStreamAndThrow(document, name);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while accessing " + name,
          e);
    }
    return result;
  }

  /*
   * Builds the xml string for a given document.
   */
  protected InputStream buildXmlData(Document document, String connectorName,
      String feedType, boolean loggingContent) throws RepositoryException {
    // Build an XML feed record for the document.
    String searchurl = null;
    if (feedType.equals(XML_FEED_METADATA_AND_URL)) {
      searchurl = getOptionalString(document, SpiConstants.PROPNAME_SEARCHURL);
      // check that this looks like a URL
      try {
        // The GSA supports SMB URLs, but Java does not.
        if (searchurl != null && searchurl.startsWith("smb:")) {
          new URL(null, searchurl, SmbURLStreamHandler.getInstance());
        } else {
          new URL(searchurl);
        }
      } catch (MalformedURLException e) {
        throw new RepositoryDocumentException(
            "Supplied search url " + searchurl + " is malformed.", e);
      }
    } else {
      String docid = getRequiredString(document, SpiConstants.PROPNAME_DOCID);
      searchurl = constructGoogleConnectorUrl(connectorName, docid);
    }

    InputStream contentStream = null;
    if (!feedType.equals(XML_FEED_METADATA_AND_URL)) {
      InputStream encodedContentStream =
          new Base64FilterInputStream(
              new BigEmptyDocumentFilterInputStream(
                   getOptionalStream(document, SpiConstants.PROPNAME_CONTENT),
                   fileSizeLimit.maxDocumentSize()),
              loggingContent);

      InputStream encodedAlternateStream =
          new Base64FilterInputStream(getAlternateContent(
              getOptionalString(document, SpiConstants.PROPNAME_TITLE)), false);

      contentStream = new AlternateContentFilterInputStream(
          encodedContentStream, encodedAlternateStream, xmlFeed.get());
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

    return xmlWrapRecord(searchurl, displayUrl, lastModified,
        contentStream, mimetype, actionType, document, feedType);
  }

  /**
   * Construct the alternate content data for a feed item.  If the feed item
   * has null or empty content, or if the feed item has excessively large
   * content, substitute this data which will insure that the feed item gets
   * indexed by the GSA. The alternate content consists of the item's title,
   * or a single space, if it lacks a title.
   *
   * @param title from the feed item
   * @return an InputStream containing the alternate content
   * @throws RepositoryDocumentException if the alternate content string
   *         cannot be UTF-8-encoded into a ByteArrayInputStream.
   */
  private InputStream getAlternateContent(String title)
      throws RepositoryException {
    try {
      byte[] bytes = null;
      // Alternate content is a string that is substituted for null or empty
      // content streams, in order to make sure the GSA indexes the feed item.
      // If the feed item supplied a title property, we build an HTML fragment
      // containing that title.  This provides better looking search result
      // entries.
      if (title != null && title.trim().length() > 0) {
        try {
          String t = "<html><title>" + title.trim() + "</title></html>";
          bytes = t.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
          // Don't be fancy.  Try the single space content.
        }
      }
      // If no title is available, we supply a single space as the content.
      if (bytes == null) {
        bytes = " ".getBytes("UTF-8");
      }
      return new ByteArrayInputStream(bytes);
    } catch (IOException e) {
      throw new RepositoryDocumentException(
          "Failed to create alternate content stream: " + e.toString());
    }
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

  private String getFeedType(Document document) throws RepositoryException {
    if (getOptionalString(document, SpiConstants.PROPNAME_SEARCHURL) != null) {
      return XML_FEED_METADATA_AND_URL;
    } else {
      return XML_FEED_INCREMENTAL;
    }
  }

  /**
   * Takes a Document and sends a the feed to the GSA.
   *
   * @param document Document corresponding to the document.
   * @param connectorName The connector name that fed this document
   * @throws PushException if Pusher problem
   * @throws FeedException if transient Feed problem
   * @throws RepositoryDocumentException if fatal Document problem
   * @throws RepositoryException if transient Repository problem
   */
  public void take(Document document, String connectorName)
      throws PushException, FeedException, RepositoryException {
    String feedType;
    try {
      feedType = getFeedType(document);
    } catch (RuntimeException e) {
      LOGGER.log(Level.WARNING,
          "Rethrowing RuntimeException as RepositoryDocumentException", e);
      throw new RepositoryDocumentException(e);
    }

    // All feeds in a feed file must be of the same type.
    // If the feed would change type, or if the feed file is full,
    // send the feed off to the GSA.
    XmlFeed feed = xmlFeed.get();
    if (feed != null) {
      if (feedType != feed.getFeedType()) {
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("A new feedType, " + feedType
              + ", requires a new feed for " + connectorName
              + ". Closing feed and sending to GSA.");
        }
        submitFeed();
      } else if (feed.size() > ((maxFeedSize / 10) * 8)) {
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("Feed for " + connectorName + " has grown to "
              + feed.size() + " bytes. Closing feed and sending to GSA.");
        }
        submitFeed();
      }
    }

    if ((feed = xmlFeed.get()) == null) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Creating new " + feedType + " feed for " + connectorName);
      }
      try {
        feed = new XmlFeed(connectorName, feedType, maxFeedSize);
        xmlFeed.set(feed);
        if (FEED_LOGGER.isLoggable(FEED_LOG_LEVEL)) {
          StringBuilder log = new StringBuilder(256 * 1024);
          log.append("Records generated for ").append(feedType);
          log.append(" feed of ").append(connectorName).append(":\n");
          feedLog.set(log);
        }
      } catch (OutOfMemoryError me) {
        throw new PushException("Unable to allocate feed buffer.  Try reducing"
                                + " the maximumFeedSize setting.", me);
      } catch (IOException ioe) {
        throw new PushException("Error creating feed", ioe);
      }
    }

    InputStream xmlData = null;
    try {
      xmlData = buildXmlData(document, connectorName, feedType,
          (Context.getInstance().getTeedFeedFile() != null));
    } catch (RuntimeException e) {
      LOGGER.log(Level.WARNING,
          "Rethrowing RuntimeException as RepositoryDocumentException", e);
      throw new RepositoryDocumentException(e);
    }
    if (xmlData == null) {
      LOGGER.warning("Skipped this document for feeding, continuing");
      return;
    }

    boolean isThrowing = false;
    int resetPoint = feed.size();
    try {
      feed.readFrom(xmlData);
      feed.incrementRecordCount();
      if (LOGGER.isLoggable(Level.FINER)) {
        LOGGER.finer("Document "
            + getRequiredString(document, SpiConstants.PROPNAME_DOCID)
            + " from connector " + connectorName + " added to feed.");
      }
    } catch (OutOfMemoryError me) {
      feed.reset(resetPoint);
      throw new PushException("Out of memory building feed, retrying.", me);
    } catch (RepositoryDocumentException rde) {
      // Skipping this document, remove it from the feed.
      feed.reset(resetPoint);
      throw rde;
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "IOException while reading: skipping", ioe);
      feed.reset(resetPoint);
      Throwable t = ioe.getCause();
      isThrowing = true;
      if (t != null && (t instanceof RepositoryException)) {
        throw (RepositoryException) t;
      } else {
        throw new RepositoryDocumentException("I/O error reading data", ioe);
      }
    } finally {
      try {
        xmlData.close();
      } catch (IOException e) {
        if (!isThrowing) {
          LOGGER.log(Level.WARNING,
              "Rethrowing IOException as PushException", e);
          throw new PushException("IOException: " + e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Finish a feed.  No more documents are anticipated.
   * If there is an outstanding feed file, submit it to the GSA.
   *
   * @throws PushException if Pusher problem
   * @throws FeedException if transient Feed problem
   * @throws RepositoryException
   */
  public void flush() throws PushException, FeedException, RepositoryException {
    LOGGER.fine("Flushing accumulated feed to GSA");
    submitFeed();
  }

  /**
   * Cancels any feed being constructed.  Any accumulated feed data is lost.
   */
  public void cancel() {
    XmlFeed feed = xmlFeed.get();
    if (feed != null) {
      LOGGER.fine("Discarding accumulated feed for " + feed.dataSource);
      xmlFeed.remove();
    }
    if (feedLog.get() != null) {
      feedLog.remove();
    }
  }

  /**
   * Takes the XmlFeed and sends the feed to the GSA.
   *
   * @throws PushException if Pusher problem
   * @throws FeedException if transient Feed problem
   * @throws RepositoryException
   */
  private void submitFeed()
      throws PushException, FeedException, RepositoryException {
    XmlFeed feed = xmlFeed.get();
    if (feed == null) {
      return;
    }

    String feedType = feed.getFeedType();
    String connectorName = feed.getDataSource();
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("Submitting " + feedType + " feed for " + connectorName
          + " to the GSA. " + feed.getRecordCount() + " records totaling "
          + feed.size() + " bytes.");
    }

    xmlFeed.remove();
    try {
      feed.close();
    } catch (IOException ioe) {
      throw new PushException("Error closing feed", ioe);
    }

    // Write the generated feedLog to the feed logger.
    if (FEED_LOGGER.isLoggable(FEED_LOG_LEVEL)) {
      FEED_LOGGER.log(FEED_LOG_LEVEL, feedLog.get().toString());
      feedLog.remove();
    }

    // Write the Feed to the TeedFeedFile, if one was specified.
    String teedFeedFilename = Context.getInstance().getTeedFeedFile();
    if (teedFeedFilename != null) {
      boolean isThrowing = false;
      OutputStream os = null;
      try {
        os = new FileOutputStream(teedFeedFilename, true);
        feed.writeTo(os);
      } catch (IOException e) {
        isThrowing = true;
        throw new FeedException("Cannot write to file: " + teedFeedFilename, e);
      } finally {
        if (os != null) {
          try {
            os.close();
          } catch (IOException e) {
            if (!isThrowing) {
              throw new FeedException(
                   "Cannot write to file: " + teedFeedFilename, e);
            }
          }
        }
      }
    }

    GsaFeedData feedData = new GsaFeedData(feedType, feed);
    gsaResponse = feedConnection.sendData(connectorName, feedData);
    if (!gsaResponse.equals(GsaFeedConnection.SUCCESS_RESPONSE)) {
      String eMessage = gsaResponse;
      if (GsaFeedConnection.UNAUTHORIZED_RESPONSE.equals(gsaResponse)) {
        eMessage += ": Client is not authorized to send feeds. Make "
            + "sure the GSA is configured to trust feeds from your host.";
      }
      if (GsaFeedConnection.INTERNAL_ERROR_RESPONSE.equals(gsaResponse)) {
        eMessage += ": Check GSA status or feed format.";
      }
      throw new PushException(eMessage);
    }
  }

  private static class XmlFeed extends ByteArrayOutputStream {
    private final String dataSource;
    private final String feedType;
    private boolean isClosed;
    private int recordCount;

    public XmlFeed(String dataSource, String feedType, int feedSize)
        throws IOException {
      super(feedSize);
      this.dataSource = dataSource;
      this.feedType = feedType;
      this.recordCount = 0;
      this.isClosed = false;
      String prefix = xmlFeedPrefix(dataSource, feedType);
      write(prefix.getBytes(XML_DEFAULT_ENCODING));
    }

    /**
     * Construct the XML header for a feed file.
     *
     * @param dataSource The dataSource for the feed.
     * @param feedType The type of feed.
     * @return XML feed header string.
     */
    private String xmlFeedPrefix(String dataSource, String feedType) {
      // Build prefix.
      StringBuffer prefix = new StringBuffer();
      prefix.append(XML_START).append('\n');
      prefix.append(XmlUtils.xmlWrapStart(XML_GSAFEED)).append('\n');
      prefix.append(XmlUtils.xmlWrapStart(XML_HEADER)).append('\n');
      prefix.append(XmlUtils.xmlWrapStart(XML_DATASOURCE));
      prefix.append(dataSource);
      prefix.append(XmlUtils.xmlWrapEnd(XML_DATASOURCE));
      prefix.append(XmlUtils.xmlWrapStart(XML_FEEDTYPE));
      prefix.append(feedType);
      prefix.append(XmlUtils.xmlWrapEnd(XML_FEEDTYPE));
      prefix.append(XmlUtils.xmlWrapEnd(XML_HEADER));
      prefix.append(XmlUtils.xmlWrapStart(XML_GROUP)).append('\n');
      return prefix.toString();
    }

    /**
     * Construct the XML footer for a feed file.
     *
     * @return XML feed suffix string.
     */
    private String xmlFeedSuffix() {
      // Build suffix.
      StringBuffer suffix = new StringBuffer();
      suffix.append(XmlUtils.xmlWrapEnd(XML_GROUP));
      suffix.append(XmlUtils.xmlWrapEnd(XML_GSAFEED));
      return suffix.toString();
    }

    public String getDataSource() {
      return dataSource;
    }

    public String getFeedType() {
      return feedType;
    }

    /**
     * Bumps the count of records stored in this feed.
     */
    public synchronized void incrementRecordCount() {
      recordCount++;
    }

    /**
     * Return the count of records.
     */
    public synchronized int getRecordCount() {
      return recordCount;
    }

    /**
     * Resets the size of this ByteArrayOutputStream to the
     * specified {@code size}, effectively discarding any
     * data that may have been written passed that point.
     * Like {@code reset()}, this method retains the previously
     * allocated buffer.
     * <p>
     * This method may be used to reduce the size of the data stored,
     * but not to increase it.  In other words, the specified {@code size}
     * cannot be greater than the current size.
     *
     * @param size new data size.
     */
    public synchronized void reset(int size) {
      if (size < 0 || size > count) {
        throw new IllegalArgumentException(
            "New size must not be negative or greater than the current size.");
      }
      count = size;
    }

    /**
     * Reads the complete contents of the supplied InputStream
     * directly into buffer of this ByteArrayOutputStream.
     * This avoids the data copy that would occur if using
     * {@code InputStream.read(byte[], int, int)}, followed by
     * {@code ByteArrayOutputStream.write(byte[], int, int)}.
     *
     * @param in the InputStream from which to read the data.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void readFrom(InputStream in) throws IOException {
      int bytes = 0;
      do {
        count += bytes;
        if (count >= buf.length) {
          // Need to grow buffer.
          int incr = Math.min(buf.length, 8 * 1024 * 1024);
          byte[] newbuf = new byte[buf.length + incr];
          System.arraycopy(buf, 0, newbuf, 0, buf.length);
          buf = newbuf;
        }
        bytes = in.read(buf, count, buf.length - count);
      } while (bytes != -1);
    }

    @Override
    public synchronized void close() throws IOException {
      if (!isClosed) {
        isClosed = true;
        String suffix = xmlFeedSuffix();
        write(suffix.getBytes(XML_DEFAULT_ENCODING));
      }
    }
  }

  /**
   * A FilterInput stream that protects against large documents and empty
   * documents.  If we have read more than FileSizeLimitInfo.maxDocumentSize
   * bytes from the input, we reset the feed to before we started reading
   * content, then provide the alternate content.  Similarly, if we get EOF
   * after reading zero bytes, we provide the alternate content.
   */
  private static class AlternateContentFilterInputStream
      extends FilterInputStream {
    private boolean useAlternate;
    private InputStream alternate;
    private final XmlFeed feed;
    private int resetPoint;

    /**
     * @param in InputStream containing raw document content
     * @param alternate InputStream containing alternate content to provide
     * @param feed XmlFeed under constructions (used for reseting size)
     */
    public AlternateContentFilterInputStream(InputStream in,
        InputStream alternate, XmlFeed feed) {
      super(in);
      this.useAlternate = false;
      this.alternate = alternate;
      this.feed = feed;
      this.resetPoint = -1;
    }

    // Reset the feed to its position when we started reading this stream,
    // and start reading from the alternate input.
    // TODO: WARNING: this strategy will not work if using chunked HTTP transfer.
    private void switchToAlternate() {
      feed.reset(resetPoint);
      useAlternate = true;
    }

    @Override
    public int read() throws IOException {
      if (resetPoint == -1) {
        // If I have read nothing yet, remember the reset point in the feed.
        resetPoint = feed.size();
      }
      if (!useAlternate) {
        try {
          return super.read();
        } catch (EmptyDocumentException e) {
          switchToAlternate();
        } catch (BigDocumentException e) {
          LOGGER.finer("Document content exceeds the maximum configured "
                       + "document size, discarding content.");
          switchToAlternate();
        }
      }
      return alternate.read();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
      if (resetPoint == -1) {
        // If I have read nothing yet, remember the reset point in the feed.
        resetPoint = feed.size();
      }
      if (!useAlternate) {
        try {
          return super.read(b, off, len);
        } catch (EmptyDocumentException e) {
          switchToAlternate();
        } catch (BigDocumentException e) {
          LOGGER.finer("Document content exceeds the maximum configured "
                       + "document size, discarding content.");
          switchToAlternate();
        }
      }
      return alternate.read(b, off, len);
    }

    @Override
    public boolean markSupported() {
      return false;
    }

    @Override
    public void close() throws IOException {
      super.close();
      alternate.close();
    }
  }

  /**
   * A FilterInput stream that protects against large documents and empty
   * documents.  If we have read more than FileSizeLimitInfo.maxDocumentSize
   * bytes from the input, or if we get EOF after reading zero bytes, we
   * throw a subclass of IOException that signals DocPusher to use alternate
   * content.
   */
  private static class BigEmptyDocumentFilterInputStream
      extends FilterInputStream {
    private final long maxDocumentSize;
    private long currentDocumentSize;

    /**
     * @param in InputStream containing raw document content
     * @param maxDocumentSize maximum allowed size in bytes of data read from in
     */
    public BigEmptyDocumentFilterInputStream(InputStream in,
                                             long maxDocumentSize) {
      super(in);
      this.maxDocumentSize = maxDocumentSize;
      this.currentDocumentSize = 0;
    }

    @Override
    public int read() throws IOException {
      if (in == null) {
        throw new EmptyDocumentException();
      }
      int val = super.read();
      if (val == -1) {
        if (currentDocumentSize == 0) {
          throw new EmptyDocumentException();
        }
      } else if (++currentDocumentSize > maxDocumentSize) {
        throw new BigDocumentException();
      }
      return val;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
      if (in == null) {
        throw new EmptyDocumentException();
      }
      int bytesRead = super.read(b, off,
          (int) Math.min(len, maxDocumentSize - currentDocumentSize + 1));
      if (bytesRead == -1) {
        if (currentDocumentSize == 0) {
          throw new EmptyDocumentException();
        }
      } else if ((currentDocumentSize += bytesRead) > maxDocumentSize) {
        throw new BigDocumentException();
      }
      return bytesRead;
    }

    @Override
    public boolean markSupported() {
      return false;
    }

    @Override
    public void close() throws IOException {
      if (in != null) {
        super.close();
      }
    }
  }

  /**
   * Subclass of IOException that is thrown when maximumDocumentSize
   * is exceeded.
   */
  private static class BigDocumentException extends IOException {
    public BigDocumentException() {
      super("Maximum Document size exceeded.");
    }
  }

  /**
   * Subclass of IOException that is thrown when the document has
   * no content.
   */
  private static class EmptyDocumentException extends IOException {
    public EmptyDocumentException() {
      super("Document has no content.");
    }
  }
}
