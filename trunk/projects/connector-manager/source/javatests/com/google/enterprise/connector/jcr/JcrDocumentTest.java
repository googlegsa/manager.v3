// Copyright 2006-2009 Google Inc.
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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrNode;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.BinaryValue;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.Node;

public class JcrDocumentTest extends TestCase {
  @Override
  public void tearDown() {
    // Reset the default time zone.
    Value.setFeedTimeZone("");
  }

  public final void testJcrDocument() throws RepositoryException {
    // We're comparing date strings here, so we need a fixed time zone.
    Value.setFeedTimeZone("GMT");

    {
      String json1 =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\","
              + "\"content\":\"now is the time\", "
              + "\"google:displayurl\":\"http://www.sometesturl.com/test\""
              + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
              + "}\r\n" + "";
      String date1 = "1970-01-01T00:00:10.000Z";
      String date2 = "Tue, 15 Nov 1994 12:45:26 GMT";
      Document document = makeDocumentFromJson(json1);

      validateProperty(document, SpiConstants.PROPNAME_LASTMODIFIED, date2);
      validateProperty(document, "jcr:lastModified", date1);
      validateProperty(document, SpiConstants.PROPNAME_CONTENT,
          "now is the time");
      validateProperty(document, SpiConstants.PROPNAME_DISPLAYURL,
          "http://www.sometesturl.com/test");

      int count = countProperties(document);
      assertEquals(5, count);
    }
    {
      String json1 =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\","
              + "\"content\":\"now is the time\", "
              + "\"google:displayurl\":\"http://www.sometesturl.com/test\""
              + "}\r\n" + "";
      String date1 = "1970-01-01T00:00:10.000Z";
      Document document = makeDocumentFromJson(json1);
      validateProperty(document, SpiConstants.PROPNAME_LASTMODIFIED, date1);
      validateProperty(document, SpiConstants.PROPNAME_CONTENT,
          "now is the time");
      validateProperty(document, SpiConstants.PROPNAME_DISPLAYURL,
          "http://www.sometesturl.com/test");

      int count = countProperties(document);
      assertEquals(4, count);
    }
  }

  private void validateProperty(Document document, String name,
      String expectedValue) throws RepositoryException {
    Value v = document.findProperty(name).nextValue();
    if (v instanceof BinaryValue) {
      // Note this won't work for streams that originate as binary files or
      // documents since the call to streamToString() will mangle the
      // characters.  For this test case, all these originate as plain text.
      assertEquals(expectedValue,
          StringUtils.streamToString(((BinaryValue) v).getInputStream()));
    } else {
      assertEquals(expectedValue, v.toString());
    }
  }

  public int countProperties(Document document)
      throws RepositoryException {
    int counter = 0;
    System.out.println();
    for (String name : document.getPropertyNames()) {
      Property property = document.findProperty(name);
      assertNotNull(property);
      Value value = property.nextValue();
      assertNotNull(value);
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
    countProperties(document);
  }

  public void testNoAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "no_acl");
    // Should not have ACL properties.
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLUSERS));
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));
  }

  public void testUserAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "user_acl");
    // ACL=[joe,mary,admin]
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
  }

  public void testUserRoleAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "user_role_acl");
    // ACL=["joe=reader","mary=reader,writer","admin=owner"]
    // joe=reder
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    JcrProperty scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
    // mary=reader,writer
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("mary", property);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary");
    assertContainsRole("writer", scopeRoles);
    // admin=owner
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("admin", property);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "admin");
    assertNotNull(scopeRoles);
    assertContainsRole("owner", scopeRoles);
    // No groups.
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));
  }

  public void testScopedRoleAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "user_scoped_role_acl");
    // ACL=["user:joe=reader","user:mary=reader,writer","user:admin=owner"]
    // user:joe=reader
    JcrProperty property =
      (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    JcrProperty scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
    // user:mary=reader,writer
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("mary", property);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary");
    assertContainsRole("writer", scopeRoles);
    // user:admin=owner
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("admin", property);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "admin");
    assertNotNull(scopeRoles);
    assertContainsRole("owner", scopeRoles);
    // No groups.
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));
  }

  public void testUserGroupAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "user_group_acl");
    // ACL=["user:joe","user:mary","group:eng"]
    JcrProperty property =
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
  }

  public void testUserGroupRoleAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "user_group_role_acl");
    // ACL=["user:joe=reader","user:mary=reader,writer","group:eng=reader"]
    // user:joe=reader
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    JcrProperty scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
    // user:mary=reader,writer
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("mary", property);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary");
    assertContainsRole("writer", scopeRoles);
    // group:eng=reader
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertNotNull(property);
    assertContainsScope("eng", property);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + "eng");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
  }

  public void testUserReaderAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "user_reader_acl");
    // ACL=joe
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));
  }

  public void testUserOwnerAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "user_owner_acl");
    // ACL = joe=owner
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    JcrProperty scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe");
    assertNotNull(scopeRoles);
    assertContainsRole("owner", scopeRoles);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));
  }

  public void testScopedOwnerAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "user_scoped_owner_acl");
    // ACL = user:joe=owner
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    JcrProperty scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe");
    assertNotNull(scopeRoles);
    assertContainsRole("owner", scopeRoles);
    assertNull(document.findProperty(SpiConstants.PROPNAME_ACLGROUPS));
  }

  public void testSameUserGroupAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "same_user_group_acl");
    // ACL = ["user:root=owner","group:root=reader,writer"]
    // user:root=owner
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("root", property);
    JcrProperty scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "root");
    assertNotNull(scopeRoles);
    assertContainsRole("owner", scopeRoles);
    // group:root=reader
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertNotNull(property);
    assertContainsScope("root", property);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + "root");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
  }

  public void testSomeUserRoleAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "some_user_role_acl");
    // ACL = ["user:joe","user:mary=reader,writer","group:eng","group:root"]
    // user:joe
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    assertNull(document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe"));
    // user:mary=reader,writer
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("mary", property);
    JcrProperty scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary");
    assertContainsRole("writer", scopeRoles);
    // group:eng
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertNotNull(property);
    assertContainsScope("eng", property);
    assertNull(document.findProperty(
        SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + "eng"));
    // group:root
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertContainsScope("eng", property);
    assertNull(document.findProperty(
        SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + "root"));
  }

  public void testSomeGroupRoleAcl() throws RepositoryException {
    Document document = extractJcrDocument("MockRepositoryEventLogAcl.txt",
                                           "some_group_role_acl");
    // ACL = ["user:joe","user:mary","group:eng=reader,writer","group:root"]
    // user:joe
    JcrProperty property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertNotNull(property);
    assertContainsScope("joe", property);
    assertNull(document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "joe"));
    // user:mary
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLUSERS);
    assertContainsScope("mary", property);
    assertNull(document.findProperty(
        SpiConstants.USER_ROLES_PROPNAME_PREFIX + "mary"));
    // group:eng
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertNotNull(property);
    assertContainsScope("eng", property);
    JcrProperty scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + "eng");
    assertNotNull(scopeRoles);
    assertContainsRole("reader", scopeRoles);
    scopeRoles = (JcrProperty) document.findProperty(
        SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + "eng");
    assertContainsRole("writer", scopeRoles);
    // group:root
    property =
        (JcrProperty) document.findProperty(SpiConstants.PROPNAME_ACLGROUPS);
    assertContainsScope("eng", property);
    assertNull(document.findProperty(
        SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + "root"));
  }

  private Document extractJcrDocument(String eventLog, String docid) {
    MockRepositoryEventList mrel = new MockRepositoryEventList(eventLog);
    MockRepository r = new MockRepository(mrel);
    MockRepositoryDocument doc = r.getStore().getDocByID(docid);
    Node node = new MockJcrNode(doc);
    return new JcrDocument(node);
  }

  private void assertContainsScope(String scopeId, JcrProperty aclProp)
      throws RepositoryException {
    Value v = null;
    while ((v = aclProp.nextValue()) != null) {
      String aclScopeId = v.toString();
      if (scopeId.equals(aclScopeId)) {
        return;
      }
    }
    fail("aclProp does not contain scope (" + scopeId + ")");
  }

  private void assertContainsRole(String role, JcrProperty rolesProp)
      throws RepositoryException {
    Value v = null;
    // Don't assume any default roles.
    while ((v = rolesProp.nextValue()) != null) {
      String aclRole = v.toString();
      if (role.equals(aclRole)) {
        return;
      }
    }
    fail("rolesProp does not contain role=" + role);
  }
}
