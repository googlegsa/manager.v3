// Copyright (C) 2009 Google Inc.
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
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
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
          StringBuilder aclEntry = new StringBuilder(aclScope).append("=")
              .append(roleVal.toString());
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
   * Return the appropriate feed type for the supplied Document.
   */
  public static String getFeedType(Document document)
      throws RepositoryException {
    if (getOptionalString(document, SpiConstants.PROPNAME_SEARCHURL) != null) {
      return XmlFeed.XML_FEED_METADATA_AND_URL;
    } else {
      return XmlFeed.XML_FEED_INCREMENTAL;
    }
  }
}
