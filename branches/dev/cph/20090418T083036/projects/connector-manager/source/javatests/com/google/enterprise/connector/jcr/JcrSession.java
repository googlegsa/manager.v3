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

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import javax.jcr.Workspace;
import javax.jcr.query.QueryManager;

/**
 * Adaptor to JCR class of the same name
 */
public class JcrSession implements Session {

  javax.jcr.Session session;

  public JcrSession(javax.jcr.Session session) {
    this.session = session;
  }

  public TraversalManager getTraversalManager()
      throws RepositoryException {
    Workspace workspace = session.getWorkspace();
    QueryManager queryManager = null;
    try {
      queryManager = workspace.getQueryManager();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return new JcrTraversalManager(queryManager);
  }

  public AuthenticationManager getAuthenticationManager() {
    return new JcrAuthenticationManager(session);
  }

  public AuthorizationManager getAuthorizationManager() {
    return new JcrAuthorizationManager(session);
  }

}
