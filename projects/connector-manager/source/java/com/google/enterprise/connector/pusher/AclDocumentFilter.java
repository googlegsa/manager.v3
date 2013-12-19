// Copyright 2012 Google Inc.
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

import com.google.common.base.Preconditions;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Dumb-down ACLs unsupported by current GSA. If an ACL feature is used by the
 * document isn't supported by the GSA we are communicated with, then we remove
 * all the document's ACLs and mark the document private to enable late-binding
 * authz.
 */
public class AclDocumentFilter implements DocumentFilterFactory {
  private static final Set<String> PROPERTIES_UNSUPPORTED;
  private static final Set<String> PROPERTIES_TO_REMOVE;

  static {
    Set<String> properties = new HashSet<String>();
    properties.add(SpiConstants.PROPNAME_ACLINHERITFROM);
    properties.add(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID);
    PROPERTIES_UNSUPPORTED = Collections.unmodifiableSet(properties);

    properties = new HashSet<String>();
    properties.add(SpiConstants.PROPNAME_ACLGROUPS);
    properties.add(SpiConstants.PROPNAME_ACLUSERS);
    properties.add(SpiConstants.PROPNAME_ACLDENYGROUPS);
    properties.add(SpiConstants.PROPNAME_ACLDENYUSERS);
    properties.add(SpiConstants.PROPNAME_ACLINHERITANCETYPE);
    properties.add(SpiConstants.PROPNAME_ACLINHERITFROM);
    properties.add(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID);
    properties.add(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE);
    PROPERTIES_TO_REMOVE = Collections.unmodifiableSet(properties);
  }

  @Override
  public Document newDocumentFilter(Document source)
      throws RepositoryException {
    if (isAclDocument(source)) {
      // Throw-away ACL-only documents if the GSA can't handle them. These
      // documents should only be used with ACL inheritance, and since we
      // fallback to non-ACL solutions when ACL inheritance is in place,
      // things should "just work," even though we are throwing away
      // information here.
      return new SkipDocument(source);
    } else if (requiresDumbingDown(source)) {
      return new NoAclsDocument(source);
    } else {
      return source;
    }
  }

  private boolean isAclDocument(Document source) throws RepositoryException {
    String docType = DocUtils.getOptionalString(source,
        SpiConstants.PROPNAME_DOCUMENTTYPE);
    return docType != null
        && DocumentType.findDocumentType(docType) == DocumentType.ACL;
  }

  private boolean requiresDumbingDown(Document source)
      throws RepositoryException {
    Set<String> propertyNames = source.getPropertyNames();
    for (String unsupported : PROPERTIES_UNSUPPORTED) {
      if (propertyNames.contains(unsupported)) {
        return true;
      }
    }
    return false;
  }

  /**
   * A document that always throws {@link SkippedDocumentException}.
   */
  private static class SkipDocument implements Document {
    private Document source;

    public SkipDocument(Document source) {
      this.source = source;
    }

    /* @Override */
    public Property findProperty(String name) throws RepositoryException {
      if (SpiConstants.PROPNAME_DOCID.equals(name)) {
        return source.findProperty(name);
      }
      throw new SkippedDocumentException("Document was an ACL document, which "
          + "is unsupported on this GSA");
    }

    /* @Override */
    public Set<String> getPropertyNames() throws RepositoryException {
      throw new SkippedDocumentException("Document was an ACL document, which "
          + "is unsupported on this GSA");
    }
  }

  /**
   * A document filter that removes ACLs and marks the document as private.
   */
  private static class NoAclsDocument implements Document {
    private final Document source;

    public NoAclsDocument(Document source) {
      this.source = source;
    }

    /* @Override */
    public Property findProperty(String name)
        throws RepositoryException {
      if (name == SpiConstants.PROPNAME_ISPUBLIC) {
        return new SimpleProperty(Value.getBooleanValue(false));
      } else if (PROPERTIES_TO_REMOVE.contains(name)) {
        return null;
      } else {
        return source.findProperty(name);
      }
    }

    /* @Override */
    public Set<String> getPropertyNames()
        throws RepositoryException {
      Set<String> names = source.getPropertyNames();
      names = new HashSet<String>(names);
      names.removeAll(PROPERTIES_TO_REMOVE);
      names.add(SpiConstants.PROPNAME_ISPUBLIC);
      return names;
    }
  }
}
