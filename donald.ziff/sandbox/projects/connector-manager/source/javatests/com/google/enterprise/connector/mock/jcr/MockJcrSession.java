// Copyright (C) 2006 Google Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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

  MockJcrRepository repo = null;
  SimpleCredentials creds = null;
  MockJcrWorkspace workspace = null;

  public MockJcrSession(MockJcrRepository repo) {
    this.repo = repo;
    workspace = new MockJcrWorkspace(repo, this);
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

  public Session impersonate(Credentials creds) throws LoginException,
      RepositoryException {
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
    MockRepositoryProperty property = doc.getProplist().getProperty("acl");
    if (property == null) {
      return result; 
    }
    String[] values = property.getValues();
    for (int i=0; i<values.length; i++) {
      if (values[i].equals(userID)) {
        return result;
      }
    }
    throw new ItemNotFoundException();
  }

  public void logout() {
    ;
  }

  // The following methods may be needed later but are temporarily
  // unimplemented

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   * @throws RepositoryException 
   */
  public Node getRootNode() throws RepositoryException {
    // TODO(ziff): we may need this later for tree traversal
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @return nothing
   * @throws PathNotFoundException 
   * @throws RepositoryException
   */
  public Item getItem(String arg0) throws PathNotFoundException,
      RepositoryException {
    // TODO(ziff): we may need this later for tree traversal
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @return nothing
   * @throws RepositoryException 
   */
  public boolean itemExists(String arg0) throws RepositoryException {
    // TODO(ziff): we may need this later for tree traversal
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @throws AccessControlException 
   * @throws RepositoryException 
   */
  public void checkPermission(String arg0, String arg1)
      throws AccessControlException, RepositoryException {
    // TODO(ziff): we may need this later for security
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public boolean isLive() {
    // TODO(ziff): we may need this later for security
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
   * @throws UnsupportedRepositoryOperationException 
   * @throws RepositoryException 
   */
  public ValueFactory getValueFactory()
      throws UnsupportedRepositoryOperationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @param arg2 
   * @param arg3 
   * @throws PathNotFoundException 
   * @throws SAXException 
   * @throws RepositoryException 
   */
  public void exportSystemView(String arg0, ContentHandler arg1, boolean arg2,
      boolean arg3) throws PathNotFoundException, SAXException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @param arg2 
   * @param arg3 
   * @throws IOException 
   * @throws PathNotFoundException 
   * @throws RepositoryException 
   */
  public void exportSystemView(String arg0, OutputStream arg1, boolean arg2,
      boolean arg3) throws IOException, PathNotFoundException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @param arg2 
   * @param arg3 
   * @throws PathNotFoundException 
   * @throws SAXException 
   * @throws RepositoryException 
   */
  public void exportDocumentView(String arg0, ContentHandler arg1,
      boolean arg2, boolean arg3) throws PathNotFoundException, SAXException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @param arg2 
   * @param arg3 
   * @throws IOException 
   * @throws PathNotFoundException 
   * @throws RepositoryException 
   */
  public void exportDocumentView(String arg0, OutputStream arg1, boolean arg2,
      boolean arg3) throws IOException, PathNotFoundException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @throws NamespaceException 
   * @throws RepositoryException 
   */
  public void setNamespacePrefix(String arg0, String arg1)
      throws NamespaceException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   * @throws RepositoryException 
   */
  public String[] getNamespacePrefixes() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @return nothing
   * @throws NamespaceException 
   * @throws RepositoryException 
   */
  public String getNamespaceURI(String arg0) throws NamespaceException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @return nothing
   * @throws NamespaceException 
   * @throws RepositoryException 
   */
  public String getNamespacePrefix(String arg0) throws NamespaceException,
      RepositoryException {
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
   * @throws ItemExistsException 
   * @throws PathNotFoundException 
   * @throws VersionException 
   * @throws ConstraintViolationException 
   * @throws LockException 
   * @throws RepositoryException 
   */
  public void move(String arg0, String arg1) throws ItemExistsException,
      PathNotFoundException, VersionException, ConstraintViolationException,
      LockException, RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @throws AccessDeniedException 
   * @throws ItemExistsException 
   * @throws ConstraintViolationException 
   * @throws InvalidItemStateException 
   * @throws VersionException 
   * @throws LockException 
   * @throws NoSuchNodeTypeException 
   * @throws RepositoryException 
   */
  public void save() throws AccessDeniedException, ItemExistsException,
      ConstraintViolationException, InvalidItemStateException,
      VersionException, LockException, NoSuchNodeTypeException,
      RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @throws RepositoryException 
   */
  public void refresh(boolean arg0) throws RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   * @throws RepositoryException 
   */
  public boolean hasPendingChanges() throws RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @return nothing
   * @throws PathNotFoundException 
   * @throws ConstraintViolationException 
   * @throws VersionException 
   * @throws LockException 
   * @throws RepositoryException 
   */
  public ContentHandler getImportContentHandler(String arg0, int arg1)
      throws PathNotFoundException, ConstraintViolationException,
      VersionException, LockException, RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @param arg2 
   * @throws IOException 
   * @throws PathNotFoundException 
   * @throws ItemExistsException 
   * @throws ConstraintViolationException 
   * @throws VersionException 
   * @throws InvalidSerializedDataException 
   * @throws LockException 
   * @throws RepositoryException 
   */
  public void importXML(String arg0, InputStream arg1, int arg2)
      throws IOException, PathNotFoundException, ItemExistsException,
      ConstraintViolationException, VersionException,
      InvalidSerializedDataException, LockException, RepositoryException {
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
