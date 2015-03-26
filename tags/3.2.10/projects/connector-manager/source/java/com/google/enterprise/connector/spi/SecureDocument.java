// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.spi.SpiConstants.AclAccess;
import com.google.enterprise.connector.spi.SpiConstants.AclInheritanceType;
import com.google.enterprise.connector.spi.SpiConstants.AclScope;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the {@link Document} interface for use with
 * ACLs. Instances are created using the static factory methods. There
 * are no public constructors. Implementors may use this directly or
 * for reference.
 *
 * @since 3.0
 */
public class SecureDocument implements Document {
  protected static final List<Value> ACL_VALUE =
      getValueList(DocumentType.ACL.toString());

  protected static final List<Value> RECORD_VALUE =
      getValueList(DocumentType.RECORD.toString());

  protected static List<Value> getValueList(String value) {
    return Collections.singletonList(Value.getStringValue(value));
  }

  /** An empty, immutable document with no properties. */
  protected static class EmptyDocument implements Document {
    @Override
    public Property findProperty(String name) {
      return null;
    }

    @Override
    public Set<String> getPropertyNames() {
      return Collections.emptySet();
    }
  }

  /**
   * Constructs a {@code SecureDocument} representing a stand-alone ACL.
   * Implies a document type of {@code ACL}.
   *
   * @param docId the unique document ID
   * @param searchUrl the repository URL, may be {@code null}
   * @see SpiConstants#PROPNAME_DOCID
   * @see SpiConstants#PROPNAME_SEARCHURL
   */
  public static SecureDocument createAcl(String docId, String searchUrl) {
    Map<String, List<Value>> copy = new HashMap<String, List<Value>>();
    copy.put(SpiConstants.PROPNAME_DOCID, getValueList(docId));
    if (searchUrl != null) {
      copy.put(SpiConstants.PROPNAME_SEARCHURL, getValueList(searchUrl));
    }
    copy.put(SpiConstants.PROPNAME_DOCUMENTTYPE, ACL_VALUE);
    return new SecureDocument(copy, new EmptyDocument());
  }

  /**
   * Constructs a {@code SecureDocument} representing a stand-alone ACL whose
   * metadata consists of the supplied {@code Map} of properties,
   * associating property names with their {@link Value Values}.
   * Implies a document type of {@code ACL}.
   *
   * @param properties a {@code Map} of document metadata
   */
  public static SecureDocument createAcl(Map<String, List<Value>> properties) {
    Map<String, List<Value>> copy =
        new HashMap<String, List<Value>>(properties);
    copy.put(SpiConstants.PROPNAME_DOCUMENTTYPE, ACL_VALUE);
    return new SecureDocument(copy, new EmptyDocument());
  }

  /**
   * Constructs a {@code SecureDocument} representing a document with
   * an ACL whose metadata consists of the properties of the given
   * {@code Document}. The document may represent a stand-alone ACL or
   * an indexable document.
   *
   * @param document a base document whose properties are included in
   *     the return {@code SecureDocument}
   * @throws RepositoryException if an error occurs accessing the
   *     underlying document
   */
  public static SecureDocument createDocumentWithAcl(Document document)
      throws RepositoryException {
    Map<String, List<Value>> empty = new HashMap<String, List<Value>>();
    return new SecureDocument(empty, document);
  }

  /**
   * Constructs a {@code SecureDocument} representing a document with
   * an ACL whose metadata consists of the supplied {@code Map} of
   * properties, associating property names with their {@link Value
   * Values}. Implies a document type of {@code RECORD}.
   *
   * @param properties a {@code Map} of document metadata
   */
  public static SecureDocument createDocumentWithAcl(
      Map<String, List<Value>> properties) {
    Map<String, List<Value>> copy =
        new HashMap<String, List<Value>>(properties);
    copy.put(SpiConstants.PROPNAME_DOCUMENTTYPE, RECORD_VALUE);
    return new SecureDocument(copy, new EmptyDocument());
  }

  /** Base document. */
  protected final Document document;

  /** Additional properties, but not necessarily strictly ACL properties. */
  protected final Map<String, List<Value>> properties;

  /**
   * Constucts a {@code SimpleDocument} whose metadata consists
   * of the supplied {@code Map} of {@code properties}, associating
   * property names with their {@link Value Values}, together with
   * the properties of the base document.
   *
   * @param properties a non-null, mutable {@code Map} of document metadata
   * @param document a non-null underlying document
   * @throws RepositoryException if an error occurs accessing the
   *     underlying document
   */
  protected SecureDocument(Map<String, List<Value>> properties,
      Document document) {
    this.properties = properties;
    this.document = document;
  }

  /**
   * {@inheritDoc}
   *
   * @throws RepositoryException if an error occurs accessing the
   *     underlying document
   */
  @Override
  public Property findProperty(String name) throws RepositoryException {
    List<Value> list = properties.get(name);
    return (list == null)
        ? document.findProperty(name) : new SimpleProperty(list);
  }

  /**
   * {@inheritDoc}
   *
   * @throws RepositoryException if an error occurs accessing the
   *     underlying document
   */
  @Override
  public Set<String> getPropertyNames() throws RepositoryException {
    Set<String> combined = new HashSet<String>();
    combined.addAll(properties.keySet());
    combined.addAll(document.getPropertyNames());
    return combined;
  }

  /**
   * Sets the URL for an inherited ACL.
   * <p>
   * Only one of {@link #setInheritFrom(String url)} or
   * {@link #setInheritFrom(String docid, SpiConstants.FeedType feedType)}
   * should be called.  If both are called the {@code inherit-from}
   * {@code docid} and {@code feedType} will be ignored in favor
   * of the {@code inherit-from url}.
   *
   * @param url the URL of the parent ACL
   * @see SpiConstants#PROPNAME_ACLINHERITFROM
   */
  public void setInheritFrom(String url) {
    properties.put(SpiConstants.PROPNAME_ACLINHERITFROM, getValueList(url));
    // Obscure these properties, in case they are specified in the delegate.
    properties.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID, null);
    properties.put(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE, null);
  }

  /**
   * Sets the components needed to construct the inherit-from URL of an ACL
   * that was fed using FeedType.CONTENT, FeedType.CONTENTURL, or FeedType.ACL.
   * <p>
   * Only one of {@link #setInheritFrom(String url)} or
   * {@link #setInheritFrom(String docid, SpiConstants.FeedType feedType)}
   * should be called.  If both are called the {@code inherit-from}
   * {@code docid} and {@code feedType} will be ignored in favor
   * of the {@code inherit-from url}.
   *
   * @param docid the docid of the parent ACL document
   * @param feedType the FeedType of the parent ACL document, or {@code null}
   *        if this document's FeedType should be used.
   * @see SpiConstants#PROPNAME_ACLINHERITFROM
   */
  public void setInheritFrom(String docid, FeedType feedType) {
    properties.put(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID,
                   getValueList(docid));
    if (feedType != null) {
      properties.put(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE,
                     getValueList(feedType.toString()));
    }
    // Obscure this property, in case it is specified in the delegate.
    properties.put(SpiConstants.PROPNAME_ACLINHERITFROM, null);
  }

  /**
   * Sets the inheritance type.
   *
   * @param type one of the {@link AclInheritanceType} values
   * @see SpiConstants#PROPNAME_ACLINHERITANCETYPE
   */
  public void setInheritanceType(AclInheritanceType type) {
    properties.put(SpiConstants.PROPNAME_ACLINHERITANCETYPE,
        getValueList(type.toString()));
  }

  /**
   * Adds a user or group to the ACL. The scope determines whether the
   * principal is a user or group, and the access determines whether
   * the principal is permitted or denied access.
   *
   * @param name the user or group name
   * @param scope whether the principal is a user or group name, one
   *     of the {@link AclScope} values
   * @param access whether access is permitted or denied, one of the
   *     {@link AclAccess} values
   * @throws RepositoryException if an error occurs accessing the
   *     underlying document
   * @see SpiConstants#PROPNAME_ACLGROUPS
   * @see SpiConstants#PROPNAME_ACLUSERS
   * @see SpiConstants#PROPNAME_ACLDENYGROUPS
   * @see SpiConstants#PROPNAME_ACLDENYUSERS
   */
  public void addPrincipal(String name, AclScope scope, AclAccess access)
      throws RepositoryException {
    String propertyName;
    if (scope == AclScope.USER && access == AclAccess.PERMIT) {
      propertyName = SpiConstants.PROPNAME_ACLUSERS;
    } else if (scope == AclScope.GROUP && access == AclAccess.PERMIT) {
      propertyName = SpiConstants.PROPNAME_ACLGROUPS;
    } else if (scope == AclScope.USER && access == AclAccess.DENY) {
      propertyName = SpiConstants.PROPNAME_ACLDENYUSERS;
    } else if (scope == AclScope.GROUP && access == AclAccess.DENY) {
      propertyName = SpiConstants.PROPNAME_ACLDENYGROUPS;
    } else {
      throw new AssertionError("Unknown scope " + scope
          + " or access " + access);
    }
    List<Value> list = properties.get(propertyName);
    if (list == null) {
      list = new ArrayList<Value>();
      properties.put(propertyName, list);

      Property existing = document.findProperty(propertyName);
      if (existing != null) {
        Value value;
        while ((value = existing.nextValue()) != null) {
          list.add(value);
        }
      }
    }
    list.add(Value.getStringValue(name));
  }
}
