// Copyright (C) 2006-2008 Google Inc.
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
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.MergeException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
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
  MockRepositoryDocument doc;
  List propList = null;

  private Property findProperty(String name) throws RepositoryException {
    for (Iterator iter = propList.iterator(); iter.hasNext();) {
      Property p = (Property) iter.next();
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return null;
  }

  private void init() {
    propList = new LinkedList();
    // Convert the special MockRepositoryDocument schema to a JCR property list
    MockRepositoryProperty p = null;
    // content
    try {
      p = new MockRepositoryProperty("jcr:content", doc.getContentStream());
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
          SpiConstants.PROPNAME_ACLUSERS);
      addAclProperty(MockRepositoryProperty.GROUP_SCOPE, aclProp, propList,
          SpiConstants.PROPNAME_ACLGROUPS);
    }
    // Now push all the other properties onto the list
    for (Iterator iter = doc.getProplist().iterator(); iter.hasNext();) {
      MockRepositoryProperty prop = (MockRepositoryProperty) iter.next();
      propList.add(new MockJcrProperty(prop));
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
   */
  private void addAclProperty(String scopeType,
      MockRepositoryProperty repoAclProp, List propList, String propName) {
    String[] values = repoAclProp.getValues();
    List newAclEntries = new ArrayList();
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
      String roles = null;
      if (roleTokPos != -1) {
        scopeId = aclEntry.substring(0, roleTokPos);
        roles = aclEntry.substring(roleTokPos + 1);
      } else {
        scopeId = aclEntry;
      }

      // At this point we have an ACL Entry.  Add it to the list surrounded by
      // double quotes to hide the separator character from the JSON parser and
      // preserve any commas within the list of roles.
      StringBuffer aclEntryBuf = new StringBuffer("\"");
      aclEntryBuf.append(scopeId);
      if (roles != null) {
        aclEntryBuf.append(SpiConstants.SCOPE_ROLE_SEPARATOR).append(roles);
      }
      aclEntryBuf.append("\"");
      newAclEntries.add(aclEntryBuf.toString());
    }

    if (newAclEntries.size() > 0) {
      // The only way to create a multi-value entry is with a JSON object or a
      // JSON string - {type:string, value:[one, two, three]}.
      StringBuffer jsonBuf = new StringBuffer();
      jsonBuf.append("{type:string, value:");
      // List.toString() should do a nice job here.
      jsonBuf.append(newAclEntries.toString());
      jsonBuf.append("}");
      String jsonString = jsonBuf.toString();
      MockRepositoryProperty newAclProp =
          new MockRepositoryProperty(propName, jsonString);
      propList.add(new MockJcrProperty(newAclProp));
    }
  }

  public MockJcrNode(MockRepositoryDocument doc) {
    this.doc = doc;
    init();
  }

  public Property getProperty(String arg0) throws PathNotFoundException,
      RepositoryException {
    return findProperty(arg0);
  }

  public PropertyIterator getProperties() throws RepositoryException {
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

  public boolean hasProperty(String arg0) throws RepositoryException {
    Property p = findProperty(arg0);
    return (p != null);
  }

  // The following methods may be needed later but are temporarily
  // unimplemented

  public Node getNode(String arg0) throws PathNotFoundException,
      RepositoryException {
    // TODO(ziff): needed for tree traversal - possibly add this later
    throw new UnsupportedOperationException();
  }

  // TODO(ziff): needed for tree traversal - possibly add this later
  public NodeIterator getNodes() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  // TODO(ziff): needed for tree traversal - possibly add this later
  public NodeIterator getNodes(String arg0) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  // The following methods are JCR level 1 - but we do not anticipate using them

  public PropertyIterator getProperties(String s) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Item getPrimaryItem() throws ItemNotFoundException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public int getIndex() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public PropertyIterator getReferences() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean hasNode(String arg0) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean hasNodes() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean hasProperties() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public NodeType getPrimaryNodeType() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public NodeType[] getMixinNodeTypes() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean isNodeType(String arg0) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public NodeDefinition getDefinition() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public String getCorrespondingNodePath(String arg0)
      throws ItemNotFoundException, NoSuchWorkspaceException,
      AccessDeniedException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public String getPath() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public String getName() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Item getAncestor(int arg0) throws ItemNotFoundException,
      AccessDeniedException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Node getParent() throws ItemNotFoundException, AccessDeniedException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public int getDepth() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Session getSession() throws RepositoryException {
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

  public boolean isSame(Item arg0) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void accept(ItemVisitor arg0) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  // The following methods are JCR level 2 - these would never be needed

  public void addMixin(String arg0) throws NoSuchNodeTypeException,
      VersionException, ConstraintViolationException, LockException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void removeMixin(String arg0) throws NoSuchNodeTypeException,
      VersionException, ConstraintViolationException, LockException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean canAddMixin(String arg0) throws NoSuchNodeTypeException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Version checkin() throws VersionException,
      UnsupportedRepositoryOperationException, InvalidItemStateException,
      LockException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void checkout() throws UnsupportedRepositoryOperationException,
      LockException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void doneMerge(Version arg0) throws VersionException,
      InvalidItemStateException, UnsupportedRepositoryOperationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void cancelMerge(Version arg0) throws VersionException,
      InvalidItemStateException, UnsupportedRepositoryOperationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void update(String arg0) throws NoSuchWorkspaceException,
      AccessDeniedException, LockException, InvalidItemStateException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public NodeIterator merge(String arg0, boolean arg1)
      throws NoSuchWorkspaceException, AccessDeniedException, MergeException,
      LockException, InvalidItemStateException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean isCheckedOut() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void restore(String arg0, boolean arg1) throws VersionException,
      ItemExistsException, UnsupportedRepositoryOperationException,
      LockException, InvalidItemStateException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void restore(Version arg0, boolean arg1) throws VersionException,
      ItemExistsException, UnsupportedRepositoryOperationException,
      LockException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void restore(Version arg0, String arg1, boolean arg2)
      throws PathNotFoundException, ItemExistsException, VersionException,
      ConstraintViolationException, UnsupportedRepositoryOperationException,
      LockException, InvalidItemStateException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void restoreByLabel(String arg0, boolean arg1)
      throws VersionException, ItemExistsException,
      UnsupportedRepositoryOperationException, LockException,
      InvalidItemStateException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public VersionHistory getVersionHistory()
      throws UnsupportedRepositoryOperationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Version getBaseVersion()
      throws UnsupportedRepositoryOperationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Lock lock(boolean arg0, boolean arg1)
      throws UnsupportedRepositoryOperationException, LockException,
      AccessDeniedException, InvalidItemStateException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Lock getLock() throws UnsupportedRepositoryOperationException,
      LockException, AccessDeniedException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void unlock() throws UnsupportedRepositoryOperationException,
      LockException, AccessDeniedException, InvalidItemStateException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean holdsLock() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean isLocked() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Node addNode(String arg0) throws ItemExistsException,
      PathNotFoundException, VersionException, ConstraintViolationException,
      LockException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Node addNode(String arg0, String arg1) throws ItemExistsException,
      PathNotFoundException, NoSuchNodeTypeException, LockException,
      VersionException, ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void orderBefore(String arg0, String arg1)
      throws UnsupportedRepositoryOperationException, VersionException,
      ConstraintViolationException, ItemNotFoundException, LockException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Value arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Value arg1, int arg2)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Value[] arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Value[] arg1, int arg2)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, String[] arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, String[] arg1, int arg2)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, String arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, String arg1, int arg2)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, InputStream arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, boolean arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, double arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, long arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Calendar arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Property setProperty(String arg0, Node arg1)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void save() throws AccessDeniedException, ItemExistsException,
      ConstraintViolationException, InvalidItemStateException,
      ReferentialIntegrityException, VersionException, LockException,
      NoSuchNodeTypeException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void refresh(boolean arg0) throws InvalidItemStateException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void remove() throws VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

}
