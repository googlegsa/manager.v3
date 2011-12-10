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

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryProperty;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

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

  private final MockRepository repo;

  /**
   * Creates a MockJcrRepository from a MockRepository.
   *
   * @param repo
   */
  public MockJcrRepository(MockRepository repo) {
    this.repo = repo;
  }

  /**
   * Gets this MockJcrRepository's MockRepository - only for testing.
   *
   * @return the MockRepository
   */
  public MockRepository getRepo() {
    return repo;
  }

  public Session login(Credentials creds) throws LoginException {
    return makeSession(creds);
  }

//
// The following methods are JCR level 1 - but we do not anticipate using them
//

  public Session login(Credentials arg0, String arg1) {
    throw new UnsupportedOperationException();
  }

  public Session login(String arg0) {
    throw new UnsupportedOperationException();
  }

  public Session login() {
    throw new UnsupportedOperationException();
  }

  public String[] getDescriptorKeys() {
    throw new UnsupportedOperationException();
  }

  public String getDescriptor(String arg0) {
    throw new UnsupportedOperationException();
  }

  private Session makeSession(Credentials creds) throws LoginException {
    if (!(creds instanceof SimpleCredentials)) {
      throw new IllegalArgumentException();
    }
    SimpleCredentials simpleCreds = (SimpleCredentials) creds;
    if (!authenticate(simpleCreds)) {
      throw new LoginException("Given credentials not valid.");
    }
    MockJcrSession session = new MockJcrSession(this);
    session.setCreds(simpleCreds);
    return session;
  }

  private boolean authenticate(SimpleCredentials creds) {
    String userID = creds.getUserID();
    String password = new String (creds.getPassword());
    if (userID == null || userID.length() < 1) {
      return true;
    }
    MockRepositoryDocument doc = repo.getStore().getDocByID("users");
    if (doc == null) {
      return true;
    }
    MockRepositoryProperty property = doc.getProplist().getProperty("acl");
    if (property == null) {
      return true;
    }
    String[] values = property.getValues();
    for (int i=0; i<values.length; i++) {
      if (values[i].equals(userID)) {
        if (userID.equals(password)) {
          return true;
        } else {
          return false;
        }
      }
    }

    return false;
  }
}
