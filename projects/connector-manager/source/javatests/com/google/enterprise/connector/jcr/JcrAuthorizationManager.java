// Copyright 2006-2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.LinkedList;
import java.util.Collection;

import javax.jcr.Credentials;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * Simple JCR implementation of the spi.AuthorizationManager. This may not
 * be the best performing implementation for some JCR-compliant
 * repositories.
 */
public class JcrAuthorizationManager implements AuthorizationManager {

  private Session sess;

  public JcrAuthorizationManager(Session sess) {
    this.sess = sess;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.spi.AuthorizationManager
   *      #authorizeDocids(java.util.Collection, AuthenticationIdentity)
   */
  public Collection<AuthorizationResponse> authorizeDocids(
      Collection<String> docids, AuthenticationIdentity identity)
      throws RepositoryException {
    // we rely on the ability of the current session to impersonate any
    // other user
    Credentials creds =
        new SimpleCredentials(identity.getUsername(), new char[] {});
    Session userSession;
    try {
      userSession = sess.impersonate(creds);
    } catch (LoginException e) {
      throw new RepositoryException(e);
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }

    try {
      // iterate through the docids, try to fetch each one, and determine
      // this user's access by whether the fetch succeeds
      LinkedList<AuthorizationResponse> result =
          new LinkedList<AuthorizationResponse>();
      for (String uuid : docids) {
        boolean readPrivilege = false;
        try {
          userSession.getNodeByUUID(uuid);
          readPrivilege = true;
        } catch (ItemNotFoundException e) {
          // Normal behavior if the user does not have privileges for this item.
          readPrivilege = false;
        } catch (javax.jcr.RepositoryException e) {
          throw new RepositoryException(e);
        }
        AuthorizationResponse response =
            new AuthorizationResponse(readPrivilege, uuid);
        result.add(response);
      }
      return result;
    } finally {
      userSession.logout();
    }
  }
}
