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

package com.google.enterprise.connector.spi;

import java.util.Map;

/**
 * The root of the SPI. We do not specify how an object implementing this
 * interface will be obtained. This is left up to implementors, who will
 * probably provide their own constructors or factories, that get injected by
 * Spring or another mechanism.
 * <p>
 * A repository object is used as something against which to authenticate, via
 * the login method.
 */
public interface Connector {

  /**
   * Sets the configuration for this Connector instance. The configuration is
   * passed as a Map of Strings to Strings. The map passed in was validated by
   * this Connector's associated ConnectorType.
   * 
   * @param configData
   * @throws RepositoryException
   */
  public void setConfig(Map configData) throws RepositoryException;

  /**
   * Authenticates against the repository and returns a session belonging to the
   * named user. If an exception is thrown, the implementor should provide an
   * explanatory message.
   * 
   * @param username A String, the user's name
   * @param password A String, the user's password
   * @return An object implementing the {@link Session} interface
   * @throws LoginException if there is a credentials-related problem
   * @throws RepositoryException if there is a more general problem, such as the
   *         system is unreachable or down
   */
  public Session login(String username, String password) throws LoginException,
      RepositoryException;
}
