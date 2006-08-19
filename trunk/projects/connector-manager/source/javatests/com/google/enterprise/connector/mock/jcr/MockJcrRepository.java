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

import com.google.enterprise.connector.mock.MockRepository;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * MockJcrRepository is the parent of a set of classes that wrap the simple
 * "MockRepository" classes as a JCR implementation.
 * <p>
 * This class implements the corresponding JCR interface, with these
 * limitations:
 * <ul>
 * <li> Some level 1 calls are not implemented because they will never be used
 * by our connector infrastructure. Eventually, these will be documented as part
 * of framework documentation. In this implementation, they also throw
 * UnsupportedOperation exceptions. These are grouped above the level 2 calls.
 * <li> Some level 1 calls are not currently needed by our implementation, but
 * may be soon. These are marked with todos and throw UnsupportedOperation
 * exceptions.
 * </ul>
 */
public class MockJcrRepository implements Repository {

  MockRepository repo;
  MockJcrWorkspace workspace;

  /**
   * Creates a MockJcrRepository from a MockRepository.
   * @param repo
   */
  public MockJcrRepository(MockRepository repo) {
    this.repo = repo;
    workspace = new MockJcrWorkspace(this, null);
  }

  /**
   * Gets this MockJcrRepository's MockRepository - only for testing.
   * @return the MockRepository
   */
  public MockRepository getRepo() {
    return repo;
  }

  //  We will probably not need all the flavors of login
  //  TODO(ziff): decide which ones we really need

  /**
   * Creates a session - at present, no credential checking is done
   * @param arg0 
   * @param arg1 
   * @return a new Session
   * @throws LoginException 
   * @throws NoSuchWorkspaceException 
   * @throws RepositoryException 
   */
  public Session login(Credentials arg0, String arg1) throws LoginException,
      NoSuchWorkspaceException, RepositoryException {
    return makeSession(arg0);
  }

  /**
   * Creates a session - at present, no credential checking is done
   * @param arg0 
   * @return a new Session
   * @throws LoginException 
   * @throws RepositoryException 
   */
  public Session login(Credentials arg0) throws LoginException,
      RepositoryException {
    return makeSession(arg0);
  }

  /**
   * Creates a session - at present, no credential checking is done
   * @param arg0 
   * @return a new Session
   * @throws LoginException 
   * @throws NoSuchWorkspaceException 
   * @throws RepositoryException 
   */
  public Session login(String arg0) throws LoginException,
      NoSuchWorkspaceException, RepositoryException {
    throw new RuntimeException("Unimplemented interface");
  }

  /**
   * Creates a session - at present, no credential checking is done
   * @return a new Session
   * @throws LoginException 
   * @throws RepositoryException 
   */
  public Session login() throws LoginException, RepositoryException {
    return new MockJcrSession(this);
  }

  /**
   * The following methods are JCR level 1 - but we do not anticipate using them
   */

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public String[] getDescriptorKeys() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @param arg0 
   * @return nothing
   */
  public String getDescriptor(String arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   */
  private Session makeSession(Credentials arg0) {
    MockJcrSession session = new MockJcrSession(this);
    session.setCreds(arg0);
    return session;
  }
}
