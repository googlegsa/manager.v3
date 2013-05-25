// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import java.util.HashMap;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

/**
 * Base class of a set of adaptor classes that implement the connector spi by
 * using a JCR level 1 implementation. Note: both the JCR repository and the JCR
 * credentials must be supplied externally (to the SPIRepository constructor and
 * login method). All other JCR objects are produced from these.
 */
public class JcrConnector implements Connector, ConnectorShutdownAware {

  javax.jcr.Repository repo;

  public JcrConnector() {
  }

  /**
   * Construct an adaptor for a repository by using a JCR repository.
   *
   * @param repo
   */
  public JcrConnector(javax.jcr.Repository repo) {
    this.repo = repo;
  }

  private String username = "";
  private String password = "";
  private HashMap<Credentials, JcrSession> sessions =
      new HashMap<Credentials, JcrSession>();

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  public Session login() throws RepositoryLoginException,
      RepositoryException {
    try {
      Credentials simpleCredentials =
          new SimpleCredentials(username, password.toCharArray());
      synchronized (sessions) {
        if (!sessions.containsKey(simpleCredentials)) {
          javax.jcr.Session session = repo.login(simpleCredentials);
          sessions.put(simpleCredentials, new JcrSession(session));
        }
      }
      return sessions.get(simpleCredentials);
    } catch (javax.jcr.LoginException e) {
      throw new RepositoryLoginException(e);
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
  }

  public void shutdown() {
    synchronized (sessions) {
      for (JcrSession jcrSession : sessions.values()) {
        jcrSession.session.logout();
      }
      sessions.clear();
    }
  }

  public void delete() {
    return;
  }
}
