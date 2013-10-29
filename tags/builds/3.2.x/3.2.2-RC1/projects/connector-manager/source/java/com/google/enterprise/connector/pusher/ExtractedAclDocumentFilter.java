// Copyright 2013 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.pusher;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.AclInheritanceType;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.util.filter.AbstractDocumentFilter;

import java.util.Set;

/**
 * A DocumentFilter that convertes a non-DocumentType.ACL Document
 * to a DocumentType.ACL Document that can be inherited from by the
 * original.
 *
 * GSA 7.0 does not support case-sensitivity or namespaces in ACLs
 * during crawl-time. So we have to send the ACLs at feed-time.
 * But the crawl-time metadata overwrites the feed-time ACLs.
 * The proposed escape is to send a named resource ACL in the feed for
 * each document, and at crawl-time return an empty ACL that inherits
 * from the corresponding named resource ACL. Then the documents
 * do not have feed-time ACLs, so there's nothing to get overwritten,
 * and the crawl-time ACLs do not have principals, so the missing
 * case-sensitivity and namespace support is moot.
 *
 * @see InheritFromExtractedAclDocumentFilter
 */
public class ExtractedAclDocumentFilter extends AbstractDocumentFilter {
  private static final Set<String> EXTRACTED_ACL_PROPS =
      ImmutableSet.<String>of(SpiConstants.PROPNAME_DOCUMENTTYPE,
                              SpiConstants.PROPNAME_FRAGMENT,
                              SpiConstants.PROPNAME_ACLINHERITANCETYPE);

  public static final String EXTRACTED_ACL_FRAGMENT = "ExtrACL";

  @Override
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    return Sets.union(source.getPropertyNames(), EXTRACTED_ACL_PROPS);
  }

  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    if (SpiConstants.PROPNAME_DOCUMENTTYPE.equals(name)) {
      return new SimpleProperty(Value.getStringValue(
          DocumentType.ACL.toString()));
    } else if (SpiConstants.PROPNAME_FRAGMENT.equals(name)) {
      return new SimpleProperty(Value.getStringValue(EXTRACTED_ACL_FRAGMENT));
    } else if (SpiConstants.PROPNAME_ACLINHERITANCETYPE.equals(name)) {
      String inheritanceType = Value.getSingleValueString(source, name);
      if (AclInheritanceType.AND_BOTH_PERMIT.toString().equals(inheritanceType)) {
        throw new SkippedDocumentException(
            "Extracting 'and-both-permit' ACLs is not supported."); 
      }
      return new SimpleProperty(Value.getStringValue(
          Strings.isNullOrEmpty(inheritanceType) ?
          AclInheritanceType.PARENT_OVERRIDES.toString() : inheritanceType));
    } else {
      return source.findProperty(name);
    }
  }
}
