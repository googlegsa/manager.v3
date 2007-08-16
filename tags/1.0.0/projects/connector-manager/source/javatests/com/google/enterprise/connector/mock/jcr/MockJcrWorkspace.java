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

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.xml.sax.ContentHandler;

/**
 * MockJcrWorkspace implements the corresponding JCR interface, with these
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
public class MockJcrWorkspace implements Workspace {

  MockJcrRepository repo;
  MockJcrSession session;

  /**
   * Create a Workspace object from a 
   * @param repo
   * @param session
   */
  public MockJcrWorkspace(MockJcrRepository repo, MockJcrSession session) {
    this.repo = repo;
    this.session = session;
  }

  /**
   * Returns the Session with which this was created
   * @return Session
   */
  public Session getSession() {
    return session;
  }

  /**
   * Returns a name for this Workspace
   * @return The constant "MockJcrWorkspace"
   */
  public String getName() {
    return "MockJcrWorkspace";
  }
  
  /**
   * Gets a QueryManager for this Workspace
   * @return MockJcrQueryManager
   * @throws RepositoryException 
   */
  public QueryManager getQueryManager() throws RepositoryException {
    return new MockJcrQueryManager(repo.getRepo().getStore());
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   * @throws UnsupportedRepositoryOperationException 
   * @throws RepositoryException 
   */
  public ObservationManager getObservationManager()
      throws UnsupportedRepositoryOperationException, RepositoryException {
    // TODO(ziff): this will be needed later
    throw new UnsupportedOperationException();
  }

  /**
   * The following methods are JCR level 1 - but we do not anticipate using them
   */

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   * @throws RepositoryException 
   */
  public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   * @throws RepositoryException 
   */
  public NodeTypeManager getNodeTypeManager() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   * @throws RepositoryException 
   */
  public String[] getAccessibleWorkspaceNames() throws RepositoryException {
    throw new UnsupportedOperationException();
  }


   // The following methods are JCR level 2 - these would never be needed

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @throws ConstraintViolationException 
   * @throws VersionException 
   * @throws AccessDeniedException 
   * @throws PathNotFoundException 
   * @throws ItemExistsException 
   * @throws LockException 
   * @throws RepositoryException 
   */
  public void copy(String arg0, String arg1)
      throws ConstraintViolationException, VersionException,
      AccessDeniedException, PathNotFoundException, ItemExistsException,
      LockException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @param arg2 
   * @throws NoSuchWorkspaceException 
   * @throws ConstraintViolationException 
   * @throws VersionException 
   * @throws AccessDeniedException 
   * @throws PathNotFoundException 
   * @throws ItemExistsException 
   * @throws LockException 
   * @throws RepositoryException 
   */
  public void copy(String arg0, String arg1, String arg2)
      throws NoSuchWorkspaceException, ConstraintViolationException,
      VersionException, AccessDeniedException, PathNotFoundException,
      ItemExistsException, LockException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @param arg2 
   * @param arg3 
   * @throws NoSuchWorkspaceException 
   * @throws ConstraintViolationException 
   * @throws VersionException 
   * @throws AccessDeniedException 
   * @throws PathNotFoundException 
   * @throws ItemExistsException 
   * @throws LockException 
   * @throws RepositoryException 
   */
  public void clone(String arg0, String arg1, String arg2, boolean arg3)
      throws NoSuchWorkspaceException, ConstraintViolationException,
      VersionException, AccessDeniedException, PathNotFoundException,
      ItemExistsException, LockException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @throws ConstraintViolationException 
   * @throws VersionException 
   * @throws AccessDeniedException 
   * @throws PathNotFoundException 
   * @throws ItemExistsException 
   * @throws LockException 
   * @throws RepositoryException 
   */
  public void move(String arg0, String arg1)
      throws ConstraintViolationException, VersionException,
      AccessDeniedException, PathNotFoundException, ItemExistsException,
      LockException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @param arg1 
   * @throws ItemExistsException 
   * @throws UnsupportedRepositoryOperationException 
   * @throws VersionException 
   * @throws LockException 
   * @throws InvalidItemStateException 
   * @throws RepositoryException 
   */
  public void restore(Version[] arg0, boolean arg1) throws ItemExistsException,
      UnsupportedRepositoryOperationException, VersionException, LockException,
      InvalidItemStateException, RepositoryException {
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
   * @throws AccessDeniedException 
   * @throws RepositoryException 
   */
  public ContentHandler getImportContentHandler(String arg0, int arg1)
      throws PathNotFoundException, ConstraintViolationException,
      VersionException, LockException, AccessDeniedException,
      RepositoryException {
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
   * @throws InvalidSerializedDataException 
   * @throws LockException 
   * @throws AccessDeniedException 
   * @throws RepositoryException 
   */
  public void importXML(String arg0, InputStream arg1, int arg2)
      throws IOException, PathNotFoundException, ItemExistsException,
      ConstraintViolationException, InvalidSerializedDataException,
      LockException, AccessDeniedException, RepositoryException {
    throw new UnsupportedOperationException();
  }
}
