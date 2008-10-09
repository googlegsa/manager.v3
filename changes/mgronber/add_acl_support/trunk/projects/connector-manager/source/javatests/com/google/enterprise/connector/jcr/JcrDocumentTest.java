// Copyright 2006-2008 Google Inc.
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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.jcr.MockJcrNode;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Value;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import javax.jcr.Node;

public class JcrDocumentTest extends TestCase {

  public final void testJcrDocument() throws RepositoryException {
    {
      String json1 =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\","
              + "\"content\":\"now is the time\", "
              + "\"google:contenturl\":\"http://www.sometesturl.com/test\""
              + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
              + "}\r\n" + "";
      String date1 = "1970-01-01T00:00:10.000Z";
      String date2 = "Tue, 15 Nov 1994 12:45:26 GMT";
      Document document = makeDocumentFromJson(json1);

      validateProperty(document, SpiConstants.PROPNAME_LASTMODIFIED, date2);
      validateProperty(document, "jcr:lastModified", date1);
      validateProperty(document, SpiConstants.PROPNAME_CONTENT,
          "now is the time");
      validateProperty(document, SpiConstants.PROPNAME_CONTENTURL,
          "http://www.sometesturl.com/test");

      int count = countProperties(document);
      Assert.assertEquals(5, count);
    }
    {
      String json1 =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\","
              + "\"content\":\"now is the time\", "
              + "\"google:contenturl\":\"http://www.sometesturl.com/test\""
              + "}\r\n" + "";
      String date1 = "1970-01-01T00:00:10.000Z";
      Document document = makeDocumentFromJson(json1);
      validateProperty(document, SpiConstants.PROPNAME_LASTMODIFIED, date1);
      validateProperty(document, SpiConstants.PROPNAME_CONTENT,
          "now is the time");
      validateProperty(document, SpiConstants.PROPNAME_CONTENTURL,
          "http://www.sometesturl.com/test");

      int count = countProperties(document);
      Assert.assertEquals(4, count);
    }
  }

  private void validateProperty(Document document, String name,
      String expectedValue) throws RepositoryException {
    Assert.assertEquals(expectedValue, document.findProperty(name).nextValue()
        .toString());
  }

  public int countProperties(Document document)
      throws RepositoryException {
    int counter = 0;
    System.out.println();
    for (Iterator i = document.getPropertyNames().iterator(); i.hasNext();) {
      String name = (String) i.next();
      Property property = document.findProperty(name);
      Assert.assertNotNull(property);
      Value value = property.nextValue();
      Assert.assertNotNull(value);
      System.out.print(name);
      System.out.print("(");
      String type = value.getClass().getName();
      System.out.print(type);
      System.out.print(") ");
      String valueString = value.toString();
      System.out.print(valueString);
      System.out.println();
      counter++;
    }
    return counter;
  }

  public static Document makeDocumentFromJson(String jsonString) {
    JSONObject jo;
    try {
      jo = new JSONObject(jsonString);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    MockRepositoryDocument mockDocument = new MockRepositoryDocument(jo);
    MockJcrNode node = new MockJcrNode(mockDocument);
    Document document = new JcrDocument(node);
    return document;
  }

  public final void testJcrDocumentFromMockRepo() throws RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog3.txt");
    MockRepository r = new MockRepository(mrel);
    MockRepositoryDocument doc = r.getStore().getDocByID("doc1");
    Node node = new MockJcrNode(doc);
    Document document = new JcrDocument(node);
    int count = countProperties(document);
  }

  public void testJcrDocumentWithAcl() throws RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLogAcl.txt");
    MockRepository r = new MockRepository(mrel);

    // Should not have ACL properties.
    MockRepositoryDocument doc = r.getStore().getDocByID("no_acl");
    Node node = new MockJcrNode(doc);
    Document document = new JcrDocument(node);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLUSERS));
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));

    // ACL=[joe,mary,admin]
    doc = r.getStore().getDocByID("user_acl");
    node = new MockJcrNode(doc);
    document = new JcrDocument(node);
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    // Note, have to refresh the property each time since it's nextValue()
    // method is stateful.
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("mary", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("admin", property);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));

    // ACL=["joe=reader","mary=reader,writer","admin=owner"]
    doc = r.getStore().getDocByID("user_role_acl");
    node = new MockJcrNode(doc);
    document = new JcrDocument(node);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertScopeHasRole("joe", "reader", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertScopeHasRole("mary", "reader", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertScopeHasRole("mary", "writer", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertScopeHasRole("admin", "owner", property);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));

    // ACL=["user:joe=reader","user:mary=reader,writer","user:admin=owner"]
    doc = r.getStore().getDocByID("user_scoped_role_acl");
    node = new MockJcrNode(doc);
    document = new JcrDocument(node);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertScopeHasRole("joe", "reader", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertScopeHasRole("mary", "reader", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertScopeHasRole("mary", "writer", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertScopeHasRole("admin", "owner", property);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));

    // ACL=["user:joe","user:mary","group:eng"]
    doc = r.getStore().getDocByID("user_group_acl");
    node = new MockJcrNode(doc);
    document = new JcrDocument(node);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("mary", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertNotNull(property);
    assertContainsScope("eng", property);

    // ACL=["user:joe=reader","user:mary=reader,writer","group:eng=reader"]
    doc = r.getStore().getDocByID("user_group_role_acl");
    node = new MockJcrNode(doc);
    document = new JcrDocument(node);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertScopeHasRole("joe", "reader", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertScopeHasRole("mary", "reader", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertScopeHasRole("mary", "writer", property);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertNotNull(property);
    assertScopeHasRole("eng", "reader", property);

    // ACL=joe
    doc = r.getStore().getDocByID("user_reader_acl");
    node = new MockJcrNode(doc);
    document = new JcrDocument(node);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));

    // ACL = joe=owner
    doc = r.getStore().getDocByID("user_owner_acl");
    node = new MockJcrNode(doc);
    document = new JcrDocument(node);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertScopeHasRole("joe", "owner", property);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));

    // ACL = user:joe=owner
    doc = r.getStore().getDocByID("user_scoped_owner_acl");
    node = new MockJcrNode(doc);
    document = new JcrDocument(node);
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertScopeHasRole("joe", "owner", property);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));
  }

  private void assertContainsScope(String scopeId, JcrProperty aclProp)
      throws RepositoryException {
    Value v = null;
    while ((v = aclProp.nextValue()) != null) {
      String aclEntry = v.toString();
      int roleTokPos = aclEntry.indexOf(MockRepositoryProperty.SCOPE_ROLE_SEP);
      if (roleTokPos != -1) {
        if (scopeId.equals(aclEntry.substring(0, roleTokPos))) {
          return;
        }
      } else {
        if (scopeId.equals(aclEntry)) {
          return;
        }
      }
    }
    fail("aclProp does not contain scope (" + scopeId + ")");
  }

  private void assertScopeHasRole(String scopeId, String role,
      JcrProperty aclProp) throws RepositoryException {
    Value v = null;
    while ((v = aclProp.nextValue()) != null) {
      String aclEntry = v.toString();
      // Don't assume any default roles so separate the scope identity from the
      // role list and check.
      int roleTokPos = aclEntry.indexOf(SpiConstants.SCOPE_ROLE_SEPARATOR);
      if (roleTokPos != -1) {
        if (scopeId.equals(aclEntry.substring(0, roleTokPos))) {
          // Assert the role list contains the given role
          String rolesStr = aclEntry.substring(roleTokPos +
              SpiConstants.SCOPE_ROLE_SEPARATOR.length());
          String[] roles = rolesStr.split(",", 0);
          for (int j = 0; j < roles.length; j++) {
            if (role.equals(roles[j])) {
              return;
            }
          }
        }
      }
    }
    fail("aclProp does not contain scope (" + scopeId + ") with role=" + role);
  }
}
