// Copyright (C) 2006-2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.mock;

import junit.framework.TestCase;


/**
 * Unit tests for Mock Repository
 */
public class MockRepositoryTest extends TestCase {

  /**
   * Simple creation sanity test
   */
  public void testSimpleRepository() {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    MockRepositoryDateTime dateTime = new MockRepositoryDateTime(60);
    assertTrue(r.getCurrentTime().compareTo(dateTime) == 0);
  }

  /**
   * Test advancing repository time
   */
  public void testRepositoryTimes() {
    // TODO(ziff): change this file access to use TestUtil
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel, new MockRepositoryDateTime(0));

    assertEquals(0, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(20));
    assertEquals(2, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(39));
    assertEquals(3, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(40));
    assertEquals(2, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(41));
    assertEquals(2, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(100));
    assertEquals(4, r.getStore().size());
  }

  /**
   * Make sure documents have exactly the attributes they should
   */
  public void testDocumentIntegrity() {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog3.txt");
    MockRepository r = new MockRepository(mrel);
    MockRepositoryDocument doc = r.getStore().getDocByID("doc1");

    System.out.println();
    int counter = 0;
    for (MockRepositoryProperty property : doc.getProplist()) {
      System.out.print(property.toString());
      System.out.println();
      counter++;
    }
    assertEquals(2, counter);
  }

  /**
   * Test documents for ACL properties.
   */
  public void testDocumentAcl() {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLogAcl.txt");
    MockRepository r = new MockRepository(mrel);

    // Get each document by ID and check it's ACL property list.
    // No ACL
    MockRepositoryDocument doc = r.getStore().getDocByID("no_acl");
    MockRepositoryPropertyList proplist = doc.getProplist();
    MockRepositoryProperty aclProp = proplist.getProperty("acl");
    assertNull("no ACL", aclProp);

    // ACL=[joe,mary,admin]
    doc = r.getStore().getDocByID("user_acl");
    proplist = doc.getProplist();
    aclProp = proplist.getProperty("acl");
    assertTrue("is repeating", aclProp.isRepeating());
    assertHasScope(MockRepositoryProperty.USER_SCOPE, "joe", aclProp);
    assertHasScope(MockRepositoryProperty.USER_SCOPE, "mary", aclProp);
    assertHasScope(MockRepositoryProperty.USER_SCOPE, "admin", aclProp);

    // ACL=["joe=reader","mary=reader,writer","admin=owner"]
    doc = r.getStore().getDocByID("user_role_acl");
    proplist = doc.getProplist();
    aclProp = proplist.getProperty("acl");
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "joe", "reader",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "mary", "reader",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "mary", "writer",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "admin", "owner",
        aclProp);

    // ACL=["user:joe=reader","user:mary=reader,writer","user:admin=owner"]
    doc = r.getStore().getDocByID("user_scoped_role_acl");
    proplist = doc.getProplist();
    aclProp = proplist.getProperty("acl");
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "joe", "reader",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "mary", "reader",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "mary", "writer",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "admin", "owner",
        aclProp);

    // ACL=["user:joe","user:mary","group:eng"]
    doc = r.getStore().getDocByID("user_group_acl");
    proplist = doc.getProplist();
    aclProp = proplist.getProperty("acl");
    assertHasScope(MockRepositoryProperty.USER_SCOPE, "joe", aclProp);
    assertHasScope(MockRepositoryProperty.USER_SCOPE, "mary", aclProp);
    assertHasScope(MockRepositoryProperty.GROUP_SCOPE, "eng", aclProp);

    // ACL=["user:joe=reader","user:mary=reader,writer","group:eng=reader"]
    doc = r.getStore().getDocByID("user_group_role_acl");
    proplist = doc.getProplist();
    aclProp = proplist.getProperty("acl");
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "joe", "reader",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "mary", "reader",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "mary", "writer",
        aclProp);
    assertScopeHasRole(MockRepositoryProperty.GROUP_SCOPE, "eng", "reader",
        aclProp);

    // ACL=joe
    doc = r.getStore().getDocByID("user_reader_acl");
    proplist = doc.getProplist();
    aclProp = proplist.getProperty("acl");
    assertHasScope(MockRepositoryProperty.USER_SCOPE, "joe", aclProp);

    // ACL="joe=owner"
    doc = r.getStore().getDocByID("user_owner_acl");
    proplist = doc.getProplist();
    aclProp = proplist.getProperty("acl");
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "joe", "owner",
        aclProp);

    // ACL="user:joe=owner"
    doc = r.getStore().getDocByID("user_scoped_owner_acl");
    proplist = doc.getProplist();
    aclProp = proplist.getProperty("acl");
    assertScopeHasRole(MockRepositoryProperty.USER_SCOPE, "joe", "owner",
        aclProp);
  }

  private void assertScopeHasRole(String scopeType, String scopeId, String role,
      MockRepositoryProperty aclProp) {
    String[] values = aclProp.getValues();
    for (int i = 0; i < values.length; i++) {
      String aclEntry = values[i];
      // Extract the scope type and compare.
      int scopeTokPos = aclEntry.indexOf(MockRepositoryProperty.SCOPE_TYPE_SEP);
      if (scopeTokPos != -1) {
        if (scopeType.equals(aclEntry.substring(0, scopeTokPos))) {
          aclEntry = aclEntry.substring(scopeTokPos + 1);
        } else {
          continue;
        }
      } else {
        // If a scope type is not specified in the aclEntry then it's safe to
        // assume a scope type of "user".
        if (!MockRepositoryProperty.USER_SCOPE.equals(scopeType)) {
          continue;
        }
      }
      // Don't assume any default roles so separate the scope identity from the
      // role list and check.
      int roleTokPos = aclEntry.indexOf(MockRepositoryProperty.SCOPE_ROLE_SEP);
      if (roleTokPos != -1) {
        if (scopeId.equals(aclEntry.substring(0, roleTokPos))) {
          // Assert the role list contains the given role
          String rolesStr = aclEntry.substring(roleTokPos + 1);
          String[] roles = rolesStr.split(",", 0);
          for (int j = 0; j < roles.length; j++) {
            if (role.equals(roles[j])) {
              return;
            }
          }
        }
      }
    }
    fail("aclProp does not contain scope (" + scopeType + ":" + scopeId
        + ") with role=" + role);
  }

  private void assertHasScope(String scopeType, String scopeId,
      MockRepositoryProperty aclProp) {
    String[] values = aclProp.getValues();
    for (int i = 0; i < values.length; i++) {
      String aclEntry = values[i];
      // Extract the scope type and compare.
      int scopeTokPos = aclEntry.indexOf(MockRepositoryProperty.SCOPE_TYPE_SEP);
      if (scopeTokPos != -1) {
        if (scopeType.equals(aclEntry.substring(0, scopeTokPos))) {
          aclEntry = aclEntry.substring(scopeTokPos + 1);
        } else {
          continue;
        }
      } else {
        // If a scope type is not specified in the aclEntry then it's safe to
        // assume a scope type of "user".
        if (!MockRepositoryProperty.USER_SCOPE.equals(scopeType)) {
          continue;
        }
      }
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
    fail("aclProp does not contain scope (" + scopeType + ":" + scopeId + ")");
  }
}
