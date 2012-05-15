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

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.spi.SpiConstants.AclAccess;
import com.google.enterprise.connector.spi.SpiConstants.AclInheritanceType;
import com.google.enterprise.connector.spi.SpiConstants.AclScope;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;
import com.google.enterprise.connector.spi.SpiConstants.FeedType;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SecureDocumentTest extends TestCase {
  private String expectedContent;

  private Map<String, List<Value>> properties;

  @Override
  protected void setUp() throws Exception {
    Map<String, Object> basicProperties =
        ConnectorTestUtils.createSimpleDocumentBasicProperties(getName());
    basicProperties.put(SpiConstants.PROPNAME_FEEDTYPE,
        FeedType.CONTENT.toString());
    expectedContent =
        (String) basicProperties.get(SpiConstants.PROPNAME_CONTENT);
    properties = ConnectorTestUtils.createSpiProperties(basicProperties);
  }

  /** Checks a single-valued String property. */
  private void assertPropertyEquals(String expected, Property property)
      throws RepositoryException {
    assertPropertyEquals(new String[] { expected }, property);
  }

  /** Checks a possibly multiple-valued String property. */
  private void assertPropertyEquals(String[] expecteds, Property property)
      throws RepositoryException {
    assertNotNull(property);
    for (String expected : expecteds) {
      Value value = property.nextValue();
      assertNotNull(value);
      assertEquals(expected, value.toString());
    }
    assertNull(property.nextValue());
  }

  /** Tests a basic ACL document. */
  public void testCreateAcl() throws RepositoryException {
    SecureDocument doc = SecureDocument.createAcl(getName(), null);

    Set<String> expectedNames = ImmutableSet.of(SpiConstants.PROPNAME_DOCID,
        SpiConstants.PROPNAME_DOCUMENTTYPE);
    assertEquals(expectedNames, doc.getPropertyNames());
    assertPropertyEquals(getName(),
        doc.findProperty(SpiConstants.PROPNAME_DOCID));
    assertPropertyEquals(DocumentType.ACL.toString(),
        doc.findProperty(SpiConstants.PROPNAME_DOCUMENTTYPE));
  }

  /** Tests an ACL with default properties. */
  public void testCreateAclFromProperties() throws RepositoryException {
    SecureDocument doc = SecureDocument.createAcl(properties);

    assertPropertyEquals(DocumentType.ACL.toString(),
        doc.findProperty(SpiConstants.PROPNAME_DOCUMENTTYPE));
    assertPropertyEquals(expectedContent,
        doc.findProperty(SpiConstants.PROPNAME_CONTENT));
  }

  /** Tests a document with its own feed type and properties. */
  public void testCreateDocumentWithAcl() throws RepositoryException {
    Document baseDoc = new SimpleDocument(properties);
    SecureDocument doc = SecureDocument.createDocumentWithAcl(baseDoc);

    assertPropertyEquals(FeedType.CONTENT.toString(),
        doc.findProperty(SpiConstants.PROPNAME_FEEDTYPE));
    assertPropertyEquals(expectedContent,
        doc.findProperty(SpiConstants.PROPNAME_CONTENT));
  }

  /** Tests a document with default properties. */
  public void testCreateDocumentWithAclFromProperties()
      throws RepositoryException {
    SecureDocument doc = SecureDocument.createDocumentWithAcl(properties);

    assertPropertyEquals(FeedType.CONTENT.toString(),
        doc.findProperty(SpiConstants.PROPNAME_FEEDTYPE));
    assertPropertyEquals(expectedContent,
        doc.findProperty(SpiConstants.PROPNAME_CONTENT));
  }

  /**
   * Tests setting the ACL inheritance, and setting the first property
   * on a secure document created without any.
   */
  public void testSetInheritFromUrl() throws RepositoryException {
    // Use a Document to test the modifying a SecureDocument with no
    // local properties (i.e., make sure the empty properties are mutable).
    Document baseDoc = new SimpleDocument(properties);
    SecureDocument doc = SecureDocument.createDocumentWithAcl(baseDoc);

    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM));
    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));
    doc.setInheritFrom("something");

    assertPropertyEquals("something",
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM));
    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));

    // Test a property from the document.
    assertPropertyEquals(getName(),
        doc.findProperty(SpiConstants.PROPNAME_DOCID));
  }

  /**
   * Tests setting the ACL inheritance, and setting the first property
   * on a secure document created without any.
   */
  public void testSetInheritFromDocid() throws RepositoryException {
    // Use a Document to test the modifying a SecureDocument with no
    // local properties (i.e., make sure the empty properties are mutable).
    Document baseDoc = new SimpleDocument(properties);
    SecureDocument doc = SecureDocument.createDocumentWithAcl(baseDoc);

    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));
    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM));
    doc.setInheritFrom("something", FeedType.CONTENTURL);

    assertPropertyEquals("something",
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));
    assertPropertyEquals(FeedType.CONTENTURL.toString(),
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE));

    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM));

    // Test a property from the document.
    assertPropertyEquals(getName(),
        doc.findProperty(SpiConstants.PROPNAME_DOCID));
  }

  /**
   * Tests setting the ACL inheritance two different ways.  Each should
   * override the other.
   */
  public void testSetInheritFromUrlAndDocid() throws RepositoryException {
    // Use a Document to test the modifying a SecureDocument with no
    // local properties (i.e., make sure the empty properties are mutable).
    Document baseDoc = new SimpleDocument(properties);
    SecureDocument doc = SecureDocument.createDocumentWithAcl(baseDoc);

    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM));
    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));
    doc.setInheritFrom("something");

    assertPropertyEquals("something",
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM));
    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));

    doc.setInheritFrom("somedocid", FeedType.CONTENTURL);
    assertPropertyEquals("somedocid",
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));
    assertPropertyEquals(FeedType.CONTENTURL.toString(),
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE));
    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM));

    doc.setInheritFrom("something");
    assertPropertyEquals("something",
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM));
    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID));
    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE));

    // Test a property from the document.
    assertPropertyEquals(getName(),
        doc.findProperty(SpiConstants.PROPNAME_DOCID));
  }

  /**
   * Tests setting the inheritance type, and the searchUrl parameter
   * to createAcl.
   */
  public void testSetInheritanceType() throws RepositoryException {
    // Use a plain ACL with a search URL to get a different code path.
    SecureDocument doc = SecureDocument.createAcl(getName(), "aclUrl");

    assertNull(doc.findProperty(SpiConstants.PROPNAME_ACLINHERITANCETYPE));
    doc.setInheritanceType(AclInheritanceType.PARENT_OVERRIDES);

    assertPropertyEquals(AclInheritanceType.PARENT_OVERRIDES.toString(),
        doc.findProperty(SpiConstants.PROPNAME_ACLINHERITANCETYPE));
    assertPropertyEquals("aclUrl",
        doc.findProperty(SpiConstants.PROPNAME_SEARCHURL));
  }

  /** Tests adding all kinds of principals. */
  public void testAddPrincipal() throws RepositoryException {
    SecureDocument doc = SecureDocument.createAcl(getName(), null);

    for (String propertyName : doc.getPropertyNames()) {
      assertFalse(propertyName, propertyName.startsWith("google:acl"));
    }

    // Loop over all possible scope and access values to prove the
    // impossibility of hitting the else clause.
    int i = 0;
    for (AclScope scope : AclScope.values()) {
      for (AclAccess access : AclAccess.values()) {
        doc.addPrincipal("user" + i++, scope, access);
      }
    }

    // Make sure that we wrote into all the ACL properties.
    for (String propertyName : doc.getPropertyNames()) {
      if (propertyName.startsWith("google:acl")) {
        i--;
      }
    }
    assertEquals(doc.getPropertyNames().toString(), 0, i);
  }

  /** Tests multiple principal values of the same kind. */
  public void testAddPrincipalMultipleValues() throws RepositoryException {
    SecureDocument doc = SecureDocument.createAcl(getName(), null);
    doc.addPrincipal("alice", AclScope.USER, AclAccess.PERMIT);
    doc.addPrincipal("bob", AclScope.USER, AclAccess.PERMIT);

    Property property = doc.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertPropertyEquals(new String[] { "alice", "bob" }, property);
  }

  /**
   * Tests adding a principal to an existing document property.
   */
  public void testAddPrincipalProperty() throws RepositoryException {
    Map<String, Object> basicProperties =
        ConnectorTestUtils.createSimpleDocumentBasicProperties(getName());
    basicProperties.put(SpiConstants.PROPNAME_ACLUSERS, "alice");
    Document baseDoc = ConnectorTestUtils.createSimpleDocument(basicProperties);
    SecureDocument doc = SecureDocument.createDocumentWithAcl(baseDoc);
    doc.addPrincipal("bob", AclScope.USER, AclAccess.PERMIT);

    Property property = doc.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertPropertyEquals(new String[] { "alice", "bob" }, property);
  }
}
