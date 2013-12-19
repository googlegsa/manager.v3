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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
   * Return the appropriate {@link FeedType} for the supplied {@link Document}.
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
  public static FeedType getFeedType(Document document)
      throws RepositoryException {
    FeedType feedType = null;
    String feedTypeValue =
        getOptionalString(document, SpiConstants.PROPNAME_FEEDTYPE);
    if (feedTypeValue != null) {
      try {
        return FeedType.valueOf(feedTypeValue.toUpperCase());
      } catch (IllegalArgumentException iae) {
        LOGGER.warning("Illegal value for feedtype property: " + feedTypeValue);
      }
    }
    // Have to go with default behavior.
    String searchUrl =
        getOptionalString(document, SpiConstants.PROPNAME_SEARCHURL);
    return (searchUrl == null) ? FeedType.CONTENT : FeedType.WEB;
  }

  public static Predicate<String> aclPredicate = new Predicate<String>() {
    @SuppressWarnings("deprecation")
    public boolean apply(String input) {
      return (input.startsWith(SpiConstants.ACL_PROPNAME_PREFIX) ||
              input.startsWith(SpiConstants.GROUP_ROLES_PROPNAME_PREFIX) ||
              input.startsWith(SpiConstants.USER_ROLES_PROPNAME_PREFIX));
    }
  };
  
  /**
   * Returns true if the document exposes any acl properties, false otherwise.
   */
  public static boolean hasAclProperties(Document document)
      throws RepositoryException {
    return Iterables.any(document.getPropertyNames(), aclPredicate);
  }
}
