// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * Simple JCR implementation of the spi.AuthorizationManager. This may not
 * be the best performing implementation for some JCR-compliant
 * repositories.
 */
public class SpiAuthorizationManagerFromJcr implements AuthorizationManager {

  private Session sess;

  public SpiAuthorizationManagerFromJcr(Session sess) {
    this.sess = sess;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.AuthorizationManager
   *      #authorizeDocids(java.util.List, java.lang.String)
   */
  public List authorizeDocids(List docidList, AuthenticationIdentity identity)
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
      LinkedList result = new LinkedList();
      for (Iterator i = docidList.iterator(); i.hasNext();) {
        String uuid = (String) i.next();
        boolean readPrivilege = false;
        try {
          Node n = userSession.getNodeByUUID(uuid);
          readPrivilege = true;
        } catch (ItemNotFoundException e) {
          // normal behavior if the user does not have privileges for this
          // item
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
