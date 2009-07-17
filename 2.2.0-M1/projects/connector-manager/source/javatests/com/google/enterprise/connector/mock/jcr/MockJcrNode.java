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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.SpiConstants;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jcr.Item;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

/**
 * MockJcrNode implements the corresponding JCR interface, with these
 * limitations:
 * <ul>
 * <li> This is a "level 1" (read-only) implementation. All level 2
 * (side-effecting) calls throw UnsupportedOperation exceptions. These are
 * grouped at the bottom of the class implementation.
 * <li> Some level 1 calls are not implemented because they will never be used
 * by our connector infrastructure. Eventually, these will be documented as part
 * of framework documentation. In this implementation, they also throw
 * UnsupportedOperation exceptions. These are grouped above the level 2 calls.
 * <li> Some level 1 calls are not currently needed by our implementation, but
 * may be soon. These are marked with todos and throw UnsupportedOperation
 * exceptions.
 * </ul>
 */
public class MockJcrNode implements Node {
  private static Set<String> propertySkipSet;

  private static Logger LOGGER =
    Logger.getLogger(MockJcrNode.class.getName());

  static {
    propertySkipSet = new HashSet<String>();
    propertySkipSet.add("acl");
  }

  private MockRepositoryDocument doc;
  private List<MockJcrProperty> propList = null;

  private Property findProperty(String name) {
    for (MockJcrProperty prop : propList) {
      if (prop.getName().equals(name)) {
        return prop;
      }
    }
    return null;
  }

  private void init() {
    propList = new LinkedList<MockJcrProperty>();
    // Convert the special MockRepositoryDocument schema to a JCR property list
    MockRepositoryProperty p = null;
    // content
    try {
      p = new MockRepositoryProperty("jcr:content", doc.getContentStream());
    } catch (FileNotFoundException e) {
      LOGGER.severe(e.toString());
    }
    if (p != null) {
      propList.add(new MockJcrProperty(p));
    }
    // modified date
    p = new MockRepositoryProperty("jcr:lastModified",
        MockRepositoryProperty.PropertyType.DATE, Integer.toString(doc
            .getTimeStamp().getTicks()));
    propList.add(new MockJcrProperty(p));
    // docid
    p = new MockRepositoryProperty("jcr:uuid", doc.getDocID());
    propList.add(new MockJcrProperty(p));
    // acl
    MockRepositoryProperty aclProp = doc.getProplist().getProperty("acl");
    if (aclProp != null) {
      addAclProperty(MockRepositoryProperty.USER_SCOPE, aclProp, propList,
          SpiConstants.PROPNAME_ACLUSERS,
          SpiConstants.USER_ROLES_PROPNAME_PREFIX);
      addAclProperty(MockRepositoryProperty.GROUP_SCOPE, aclProp, propList,
          SpiConstants.PROPNAME_ACLGROUPS,
          SpiConstants.GROUP_ROLES_PROPNAME_PREFIX);
    }
    // Now push all the other properties onto the list.
    for (MockRepositoryProperty prop : doc.getProplist()) {
      // Don't pass on the some of the special MockRepoDocument properties so
      // they don't clutter the meta-data.  Shouldn't delete them from the
      // MockRepoDocument because they could be used by the MockRepo.
      if (!propertySkipSet.contains(prop.getName())) {
        propList.add(new MockJcrProperty(prop));
      }
    }
  }

  /**
   * Utility method to take the given {@link MockRepositoryProperty} and parse
   * and convert it into ACL Entries for the SPI {@link Document} space.
   * @param scopeType the scopeType to be extracted from the given
   *     <code>repoAclProp</code>.  Should be one of
   *     {@link MockRepositoryProperty#USER_SCOPE} or
   *     {@link MockRepositoryProperty#GROUP_SCOPE}.
   * @param repoAclProp the repository property containing general ACL
   *     information.
   * @param propList the property list to store any ACL Entries of the given
   *     <code>scopeType</code> extracted from the given
   *     <code>repoAclProp</code>.
   * @param propName the key or property named to be used to store the ACL
   *     Entries in the given <code>propList</code>.
   * @param rolesPrefix the prefix to use to add a property to define specific
   *     roles for a ACL Entry.
   */
  private void addAclProperty(String scopeType,
      MockRepositoryProperty repoAclProp, List<MockJcrProperty> propList,
      String propName, String rolesPrefix) {
    String[] values = repoAclProp.getValues();
    List<String> newAclScopes = new ArrayList<String>();
    for (int i = 0; i < values.length; i++) {
      String aclEntry = values[i];
      // Strip the scope type if present and continue to process the entries
      // that match the given scopeType.
      int scopeTokPos = aclEntry.indexOf(MockRepositoryProperty.SCOPE_TYPE_SEP);
      if (scopeTokPos != -1) {
        if (scopeType.equals(aclEntry.substring(0, scopeTokPos))) {
          aclEntry = aclEntry.substring(scopeTokPos + 1);
        } else {
          continue;
        }
      } else {
        // If a scope type is not specified in the aclEntry then it's assumed to
        // be of scope type "user".
        if (!MockRepositoryProperty.USER_SCOPE.equals(scopeType)) {
          continue;
        }
      }

      // Separate the scope identity from the role list if present.
      int roleTokPos = aclEntry.indexOf(MockRepositoryProperty.SCOPE_ROLE_SEP);
      String scopeId;
      String rolesStr = null;
      if (roleTokPos != -1) {
        scopeId = aclEntry.substring(0, roleTokPos);
        rolesStr = aclEntry.substring(roleTokPos + 1);
      } else {
        scopeId = aclEntry;
      }

      // At this point we have the scope and list of roles that make up an ACL
      // Entry.  Add the scope to the list and, if there are roles specified,
      // create an associated "<rolesPrefix><scopeId>" property and add it to
      // the propList.
      newAclScopes.add("\"" + scopeId + "\"");
      if (rolesStr != null) {
        // Create a multi-value property for the scope's roles.
        List<String> rolesList = Arrays.asList(rolesStr.split(",", 0));
        MockRepositoryProperty newRolesProp = new MockRepositoryProperty(
            rolesPrefix + scopeId,
            "{type:string, value:" + rolesList.toString() + "}");
        propList.add(new MockJcrProperty(newRolesProp));
      }
    }

    if (newAclScopes.size() > 0) {
      MockRepositoryProperty newAclProp = new MockRepositoryProperty(propName,
          "{type:string, value:" + newAclScopes.toString() + "}");
      propList.add(new MockJcrProperty(newAclProp));
    }
  }

  public MockJcrNode(MockRepositoryDocument doc) {
    this.doc = doc;
    init();
  }

  public Property getProperty(String arg0) {
    return findProperty(arg0);
  }

  public PropertyIterator getProperties() {
    return new MockJcrPropertyIterator(propList);
  }

  public String getUUID() throws UnsupportedRepositoryOperationException,
      RepositoryException {
    Property p = this.getProperty("jcr:uuid");
    if (p == null) {
      throw new IllegalArgumentException();
    }
    return p.getString();
  }

  public boolean hasProperty(String arg0) {
    Property p = findProperty(arg0);
    return (p != null);
  }

  // The following methods may be needed later but are temporarily
  // unimplemented

  public Node getNode(String arg0) {
    throw new UnsupportedOperationException();
  }

  public NodeIterator getNodes() {
    throw new UnsupportedOperationException();
  }

  public NodeIterator getNodes(String arg0) {
    throw new UnsupportedOperationException();
  }

  // The following methods are JCR level 1 - but we do not anticipate using them

  public PropertyIterator getProperties(String s) {
    throw new UnsupportedOperationException();
  }

  public Item getPrimaryItem() {
    throw new UnsupportedOperationException();
  }

  public int getIndex() {
    throw new UnsupportedOperationException();
  }

  public PropertyIterator getReferences() {
    throw new UnsupportedOperationException();
  }

  public boolean hasNode(String arg0) {
    throw new UnsupportedOperationException();
  }

  public boolean hasNodes() {
    throw new UnsupportedOperationException();
  }

  public boolean hasProperties() {
    throw new UnsupportedOperationException();
  }

  public NodeType getPrimaryNodeType() {
    throw new UnsupportedOperationException();
  }

  public NodeType[] getMixinNodeTypes() {
    throw new UnsupportedOperationException();
  }

  public boolean isNodeType(String arg0) {
    throw new UnsupportedOperationException();
  }

  public NodeDefinition getDefinition() {
    throw new UnsupportedOperationException();
  }

  public String getCorrespondingNodePath(String arg0) {
    throw new UnsupportedOperationException();
  }

  public String getPath() {
    throw new UnsupportedOperationException();
  }

  public String getName() {
    throw new UnsupportedOperationException();
  }

  public Item getAncestor(int arg0) {
    throw new UnsupportedOperationException();
  }

  public Node getParent() {
    throw new UnsupportedOperationException();
  }

  public int getDepth() {
    throw new UnsupportedOperationException();
  }

  public Session getSession() {
    throw new UnsupportedOperationException();
  }

  public boolean isNode() {
    throw new UnsupportedOperationException();
  }

  public boolean isNew() {
    throw new UnsupportedOperationException();
  }

  public boolean isModified() {
    throw new UnsupportedOperationException();
  }

  public boolean isSame(Item arg0) {
    throw new UnsupportedOperationException();
  }

  public void accept(ItemVisitor arg0) {
    throw new UnsupportedOperationException();
  }

  // The following methods are JCR level 2 - these would never be needed

  public void addMixin(String arg0) {
    throw new UnsupportedOperationException();
  }

  public void removeMixin(String arg0) {
    throw new UnsupportedOperationException();
  }

  public boolean canAddMixin(String arg0) {
    throw new UnsupportedOperationException();
  }

  public Version checkin() {
    throw new UnsupportedOperationException();
  }

  public void checkout() {
    throw new UnsupportedOperationException();
  }

  public void doneMerge(Version arg0) {
    throw new UnsupportedOperationException();
  }

  public void cancelMerge(Version arg0) {
    throw new UnsupportedOperationException();
  }

  public void update(String arg0) {
    throw new UnsupportedOperationException();
  }

  public NodeIterator merge(String arg0, boolean arg1) {
    throw new UnsupportedOperationException();
  }

  public boolean isCheckedOut() {
    throw new UnsupportedOperationException();
  }

  public void restore(String arg0, boolean arg1) {
    throw new UnsupportedOperationException();
  }

  public void restore(Version arg0, boolean arg1) {
    throw new UnsupportedOperationException();
  }

  public void restore(Version arg0, String arg1, boolean arg2) {
    throw new UnsupportedOperationException();
  }

  public void restoreByLabel(String arg0, boolean arg1) {
    throw new UnsupportedOperationException();
  }

  public VersionHistory getVersionHistory() {
    throw new UnsupportedOperationException();
  }

  public Version getBaseVersion() {
    throw new UnsupportedOperationException();
  }

  public Lock lock(boolean arg0, boolean arg1) {
    throw new UnsupportedOperationException();
  }

  public Lock getLock() {
    throw new UnsupportedOperationException();
  }

  public void unlock() {
    throw new UnsupportedOperationException();
  }

  public boolean holdsLock() {
    throw new UnsupportedOperationException();
  }

  public boolean isLocked() {
    throw new UnsupportedOperationException();
  }

  public Node addNode(String arg0) {
    throw new UnsupportedOperationException();
  }

  public Node addNode(String arg0, String arg1) {
    throw new UnsupportedOperationException();
  }

  public void orderBefore(String arg0, String arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Value arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Value arg1, int arg2) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Value[] arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Value[] arg1, int arg2) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, String[] arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, String[] arg1, int arg2) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, String arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, String arg1, int arg2) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, InputStream arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, boolean arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, double arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, long arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Calendar arg1) {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Node arg1) {
    throw new UnsupportedOperationException();
  }

  public void save() {
    throw new UnsupportedOperationException();
  }

  public void refresh(boolean arg0) {
    throw new UnsupportedOperationException();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

}
