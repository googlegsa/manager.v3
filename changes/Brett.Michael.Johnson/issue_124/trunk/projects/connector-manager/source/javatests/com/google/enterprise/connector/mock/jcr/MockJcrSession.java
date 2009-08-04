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

import org.xml.sax.ContentHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import javax.jcr.Credentials;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

/**
 * MockJcrSession is trivial. Mostly exceptions and boiler plate.
 * <p>
 * This class implements the corresponding JCR interface, with these
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
public class MockJcrSession implements Session {

  private final MockJcrRepository repo;
  private SimpleCredentials creds;

  public MockJcrSession(MockJcrRepository repo) {
    this.repo = repo;
  }

  /**
   * Set the credentials used by this session - not yet used by the framework
   * @param creds   a Credentials object
   */
  public void setCreds(SimpleCredentials creds) {
    this.creds = creds;
  }

  /**
   * Get the Repository for this session -
   * in this implementation, there is only one.
   * @return MockJcrRepository
   */
  public Repository getRepository() {
    if (repo == null) {
      throw new RuntimeException("Session has null repo");
    }
    return repo;
  }

  /**
   * Get the userID for this session - not yet used by the framework
   * @return a guess at the user's name
   */
  public String getUserID() {
    if (creds != null) {
        return creds.getUserID();
      }
    return "admin";
  }

  /**
   * Gets the Workspace associated with this Session -
   * in this implementation, there is only one.
   * @return MockJcrWorkspace
   */
  public Workspace getWorkspace() {
    return new MockJcrWorkspace(repo, this);
  }

  public Session impersonate(Credentials creds) {
    if (!(creds instanceof SimpleCredentials)) {
      throw new IllegalArgumentException();
    }
    SimpleCredentials simpleCreds = (SimpleCredentials) creds;
    MockJcrSession result = new MockJcrSession(this.repo);
    result.setCreds(simpleCreds);
    return result;
  }

  public Node getNodeByUUID(String uuid) throws ItemNotFoundException,
      RepositoryException {
    MockRepositoryDocument doc = repo.getRepo().getStore().getDocByID(uuid);
    if (doc == null) {
      throw new ItemNotFoundException();
    }
    Node result = new MockJcrNode(doc);
    String userID = getUserID();
    if (userID == null) {
      return result;
    }
    if ("admin".equals(userID)) {
      return result;
    }
    try {
      checkPermission(userID, doc);
    } catch (AccessControlException e) {
      throw new ItemNotFoundException();
    }
    return result;
  }

  /**
   * Determines whether the given user has permission to read the specified
   * document.  This method quietly returns if the access request is permitted,
   * or throws a suitable <code>java.security.AccessControlException</code>
   * otherwise.
   */
  private void checkPermission(String userId, MockRepositoryDocument doc)
      throws AccessControlException {
    MockRepositoryProperty property = doc.getProplist().getProperty("acl");
    if (property == null) {
      return;
    }
    String[] values = property.getValues();
    for (int i = 0; i < values.length; i++) {
      String aclEntry = values[i];
      // Extract the scope type and compare.
      int scopeTokPos = aclEntry.indexOf(MockRepositoryProperty.SCOPE_TYPE_SEP);
      if (scopeTokPos != -1) {
        if ("user".equals(aclEntry.substring(0, scopeTokPos))) {
          aclEntry = aclEntry.substring(scopeTokPos + 1);
        } else {
          continue;
        }
      }
      int roleTokPos = aclEntry.indexOf(MockRepositoryProperty.SCOPE_ROLE_SEP);
      if (roleTokPos != -1) {
        if (userId.equals(aclEntry.substring(0, roleTokPos))) {
          return;
        }
      } else {
        if (userId.equals(aclEntry)) {
          return;
        }
      }
    }
    throw new AccessControlException("User(" + userId + ") does not have "
        + "premission to read document(" + doc.getDocID() + ").");
  }

  public void logout() { }

  // The following methods may be needed later but are temporarily
  // unimplemented

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public Node getRootNode() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @return nothing
   */
  public Item getItem(String arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @return nothing
   */
  public boolean itemExists(String arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   */
  public void checkPermission(String arg0, String arg1) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public boolean isLive() {
    throw new UnsupportedOperationException();
  }

// The following methods are JCR level 1 - but we do not anticipate using them

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @return nothing
   */
  public Object getAttribute(String arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public String[] getAttributeNames() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public ValueFactory getValueFactory() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @param arg2
   * @param arg3
   */
  public void exportSystemView(String arg0, ContentHandler arg1, boolean arg2,
      boolean arg3) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @param arg2
   * @param arg3
   */
  public void exportSystemView(String arg0, OutputStream arg1, boolean arg2,
      boolean arg3) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @param arg2
   * @param arg3
   */
  public void exportDocumentView(String arg0, ContentHandler arg1,
      boolean arg2, boolean arg3) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @param arg2
   * @param arg3
   */
  public void exportDocumentView(String arg0, OutputStream arg1, boolean arg2,
      boolean arg3) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   */
  public void setNamespacePrefix(String arg0, String arg1) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public String[] getNamespacePrefixes() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @return nothing
   */
  public String getNamespaceURI(String arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @return nothing
   */
  public String getNamespacePrefix(String arg0) {
    throw new UnsupportedOperationException();
  }

// The following methods are JCR level 2 - these would never be needed

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   */
  public void addLockToken(String arg0) {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   */
  public void move(String arg0, String arg1) {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void save() {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   */
  public void refresh(boolean arg0) {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public boolean hasPendingChanges() {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @return nothing
   */
  public ContentHandler getImportContentHandler(String arg0, int arg1) {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   * @param arg1
   * @param arg2
   */
  public void importXML(String arg0, InputStream arg1, int arg2) {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public String[] getLockTokens() {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0
   */
  public void removeLockToken(String arg0) {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }
}
