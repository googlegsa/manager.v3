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
  Credentials creds = null;
  MockJcrWorkspace workspace = null;

  public MockJcrSession(MockJcrRepository repo) {
    this.repo = repo;
    workspace = new MockJcrWorkspace(repo, this);
  }
  
  /**
   * Set the credentials used by this session - not yet used by the framework
   * @param creds   a Credentials object
   */
  public void setCreds(Credentials creds) {
    this.creds = creds;
  }

  /**
   * Get the Repository for this session - 
   * in this implementation, there is only one.
   */ 
  public Repository getRepository() {
    if (repo == null) {
      throw new RuntimeException("Session has null repo");
    }
    return repo;
  }

  /**
   * Get the userID for this session - not yet used by the framework
   */
  public String getUserID() {
    if (creds != null) {
      if (creds instanceof SimpleCredentials) {
        SimpleCredentials c = (SimpleCredentials) creds;
        return c.getUserID();
      }
    }
    return "anonymous";
  }

  /**
   * Gets the Workspace associated with this Session - 
   * in this implementation, there is only one.
   */
  public Workspace getWorkspace() {
    return new MockJcrWorkspace(repo, this);
  }

  // The following methods may be needed later but are temporarily
  // unimplemented

  /**
   * Throws UnsupportedOperationException
   */
  public Session impersonate(Credentials arg0) throws LoginException,
      RepositoryException {
    // TODO(ziff): we may need this later for security
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public Node getRootNode() throws RepositoryException {
    // TODO(ziff): we may need this later for tree traversal
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public Node getNodeByUUID(String arg0) throws ItemNotFoundException,
      RepositoryException {
    // TODO(ziff): we may need this later for tree traversal
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public Item getItem(String arg0) throws PathNotFoundException,
      RepositoryException {
    // TODO(ziff): we may need this later for tree traversal
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public boolean itemExists(String arg0) throws RepositoryException {
    // TODO(ziff): we may need this later for tree traversal
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void checkPermission(String arg0, String arg1)
      throws AccessControlException, RepositoryException {
    // TODO(ziff): we may need this later for security
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void logout() {
    // TODO(ziff): we may need this later for security
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public boolean isLive() {
    // TODO(ziff): we may need this later for security
    throw new UnsupportedOperationException();
  }

// The following methods are JCR level 1 - but we do not anticipate using them

  /**
   * Throws UnsupportedOperationException
   */
  public Object getAttribute(String arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public String[] getAttributeNames() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public ValueFactory getValueFactory()
      throws UnsupportedRepositoryOperationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void exportSystemView(String arg0, ContentHandler arg1, boolean arg2,
      boolean arg3) throws PathNotFoundException, SAXException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void exportSystemView(String arg0, OutputStream arg1, boolean arg2,
      boolean arg3) throws IOException, PathNotFoundException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void exportDocumentView(String arg0, ContentHandler arg1,
      boolean arg2, boolean arg3) throws PathNotFoundException, SAXException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void exportDocumentView(String arg0, OutputStream arg1, boolean arg2,
      boolean arg3) throws IOException, PathNotFoundException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void setNamespacePrefix(String arg0, String arg1)
      throws NamespaceException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public String[] getNamespacePrefixes() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public String getNamespaceURI(String arg0) throws NamespaceException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public String getNamespacePrefix(String arg0) throws NamespaceException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

// The following methods are JCR level 2 - these would never be needed

  /**
   * Throws UnsupportedOperationException
   */
  public void addLockToken(String arg0) {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void move(String arg0, String arg1) throws ItemExistsException,
      PathNotFoundException, VersionException, ConstraintViolationException,
      LockException, RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
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
   */
  public void refresh(boolean arg0) throws RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public boolean hasPendingChanges() throws RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public ContentHandler getImportContentHandler(String arg0, int arg1)
      throws PathNotFoundException, ConstraintViolationException,
      VersionException, LockException, RepositoryException {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
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
   */
  public String[] getLockTokens() {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  public void removeLockToken(String arg0) {
    // All side-effecting calls throw an UnsupportedOperationException
    throw new UnsupportedOperationException();
  }
}
