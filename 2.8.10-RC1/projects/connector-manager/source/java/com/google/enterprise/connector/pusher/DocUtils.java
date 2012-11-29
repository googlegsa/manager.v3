// Copyright 2009 Google Inc.
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

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to more easily access Document content and meta-data.
 */
public class DocUtils {
  private static final Logger LOGGER =
      Logger.getLogger(DocUtils.class.getName());

  private DocUtils() {
    // Prevents instantiation.
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
   * @param document the document being processed.
   * @param aclPropName the name of the property being processed.  Should be one
   *        of {@link SpiConstants#PROPNAME_ACLGROUPS} or
   *        {@link SpiConstants#PROPNAME_ACLUSERS}.
   * @return either the original property if no conversion was necessary or a
   *         new converted property containing ACL Entries.
   * @throws RepositoryException if there was a problem extracting properties.
   */
  public static Property processAclProperty(Document document,
      String aclPropName) throws RepositoryException {
    LinkedList<Value> acl = new LinkedList<Value>();
    Property scopeProp = document.findProperty(aclPropName);
    Value scopeVal = null;
    while ((scopeVal = scopeProp.nextValue()) != null) {
      String aclScope = scopeVal.toString().trim();
      if (aclScope.length() == 0)
        continue;
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
          String role = roleVal.toString().trim();
          if (role.length() > 0) {
            acl.add(Value.getStringValue(aclScope + '=' + role));
          } else {
            // XXX: Empty role implies reader?
            acl.add(scopeVal);
          }
        }
      } else {
        // No roles for this scope; just add scope to the list.
        acl.add(scopeVal);
      }
    }
    return new SimpleProperty(acl);
  }

  /**
   * Gets the value for a given property.
   */
  public static ValueImpl getValueAndThrow(Document document, String name)
      throws RepositoryException {
    return (ValueImpl) Value.getSingleValue(document, name);
  }

  /**
   * Gets the Calendar value for a given property.
   */
  public static String getCalendarAndThrow(Document document, String name)
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

  /**
   * Gets the String value for a given property.
   */
  public static String getStringAndThrow(Document document, String name)
      throws RepositoryException {
    String result = null;
    ValueImpl v = getValueAndThrow(document, name);
    if (v == null) {
      return null;
    }
    result = v.toFeedXml();
    return result;
  }

  /**
   * Gets the String value for a given property.
   */
  public static String getOptionalString(Document document, String name)
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

  /**
   * Gets the String value for a given property.
   */
  public static String getRequiredString(Document document, String name)
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

  /**
   * Gets the InputStream value for a given property.
   */
  public static InputStream getStreamAndThrow(Document document, String name)
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
        bytes = s.getBytes(XmlFeed.XML_DEFAULT_ENCODING);
      } catch (UnsupportedEncodingException e) {
        throw new RepositoryDocumentException("Encoding error." , e);
      }
      result = new ByteArrayInputStream(bytes);
    }
    return result;
  }

  /**
   * Gets the value for a given property.
   */
  public static InputStream getOptionalStream(Document document, String name)
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

  /**
   * Gets the boolean value for a given property.
   */
  public static boolean getOptionalBoolean(Document document, String name, boolean defaultBool) {
    ValueImpl v = null;
    try {
      v = getValueAndThrow(document, name);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while accessing " + name,
          e);
      return defaultBool;
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Swallowing exception while accessing " + name,
          e);
      return defaultBool;
    }
    if (v != null) {
      return v.toBoolean();
    }
    return defaultBool;
  }

  /**
   * Return the appropriate feed type for the supplied Document.
   * <p>
   * To support legacy settings without change, the logic goes like this:
   * <ol>
   * <li> If there is no searchurl and no feedtype setting then default to
   *      content feed using a fabricated URL from the docid.</li>
   * <li> If there is a searchurl and no feedtype setting then default to web
   *      feed and use the searchurl as the document URL.</li>
   * <li> Otherwise, the feedtype setting will determine the feed type.  Note,
   *      this means that you can set the feedtype to web feed and use the
   *      fabricated URL's by not setting a searchurl.  This is probably a
   *      recipe for disaster since the fabricated URL will use the
   *      googleconnector:// protocol, however, this might be an easy key to
   *      use on the front-end for transformation.</li>
   * </ol>
   * <p>
   * Illegal values for feed type will be ignored and the default behavior will
   * be used.
   */
  public static String getFeedType(Document document)
      throws RepositoryException {
    FeedType feedType = null;
    String feedTypeValue = getOptionalString(document,
        SpiConstants.PROPNAME_FEEDTYPE);
    if (feedTypeValue != null) {
      try {
        feedType = FeedType.valueOf(feedTypeValue);
      } catch (IllegalArgumentException iae) {
        LOGGER.warning("Illegal value for feedtype property: " + feedTypeValue);
      }
    }
    if (feedType == null) {
      // Have to go with default behavior.
      String searchUrl = getOptionalString(document,
          SpiConstants.PROPNAME_SEARCHURL);
      if (searchUrl == null) {
        return XmlFeed.XML_FEED_INCREMENTAL;
      } else {
        return XmlFeed.XML_FEED_METADATA_AND_URL;
      }
    } else if (FeedType.CONTENT.equals(feedType)) {
      return XmlFeed.XML_FEED_INCREMENTAL;
    } else {
      // Has to be WEB.
      return XmlFeed.XML_FEED_METADATA_AND_URL;
    }
  }
}
