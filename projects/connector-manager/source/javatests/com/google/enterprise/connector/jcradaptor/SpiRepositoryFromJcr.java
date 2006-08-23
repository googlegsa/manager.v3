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

package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.Repository;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

/**
 * Base class of a set of adaptor classes that implement the connector spi by
 * using a JCR level 1 implementation. Note: both the JCR repository and the JCR
 * credentials must be supplied externally (to the SPIRepository constructor and
 * login method). All other JCR objects are produced from these.
 */
public class SpiRepositoryFromJcr implements Repository {

  javax.jcr.Repository repo;

  /**
   * Construct an adaptor for a repository by using a JCR repository.
   * 
   * @param repo
   */
  public SpiRepositoryFromJcr(javax.jcr.Repository repo) {
    this.repo = repo;
  }

  public Session login(String username, String password) throws LoginException,
      RepositoryException {
    try {
      Credentials simpleCredentials = new SimpleCredentials(username, password
          .toCharArray());
      javax.jcr.Session session = repo.login(simpleCredentials);
      return new SpiSessionFromJcr(session);
    } catch (javax.jcr.LoginException e) {
      throw new LoginException(e);
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
  }
}
