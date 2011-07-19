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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * Simple JCR implementation of the spi.AuthenticationManager. This may not be
 * the best performing implementation for some JCR-compliant repositories.
 */
public class JcrAuthenticationManager implements AuthenticationManager {

  private Session sess;

  JcrAuthenticationManager(Session sess) {
    this.sess = sess;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.spi.AuthenticationManager
   *      #authenticate(java.lang.String,java.lang.String)
   */
  public AuthenticationResponse authenticate(AuthenticationIdentity identity)
      throws RepositoryLoginException, RepositoryException {
    String username = identity.getUsername();
    String password = identity.getPassword();
    if (username == null) {
      username = "";
    }
    if (password == null) {
      password = "";
    }
    boolean valid = false;
    Repository repo = sess.getRepository();
    SimpleCredentials creds =
        new SimpleCredentials(username, password.toCharArray());
    Session userSess = null;
    try {
      userSess = repo.login(creds);
      if (userSess != null) {
        valid = true;
      }
    } catch (javax.jcr.LoginException e) {
      // Login failed.  Just leave result false and continue.
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    } finally {
      if (userSess != null) {
        userSess.logout();
      }
    }
    AuthenticationResponse result = new AuthenticationResponse(valid, null);
    return result;
  }
}
