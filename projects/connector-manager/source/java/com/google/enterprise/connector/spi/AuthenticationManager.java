// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * Authentication Manager. All calls for end-user query authentication pass
 * through this interface.
 */
public interface AuthenticationManager {

  /**
   * Authenticates against the repository and returns true or false
   * depending on whether authentication was successful. If an exception is
   * thrown, the implementor should provide an explanatory message.
   * 
   * @param identity An AuthenticationIdentity object that encapsulates the
   *        user's identity
   * @return True means that authentication succeeded, false indicates
   *         failure
   * @throws RepositoryLoginException if there is a credentials-related problem that
   *         prohibits authentication
   * @throws RepositoryException if there is a more general problem, such
   *         as the system is unreachable or down
   */
  public AuthenticationResponse authenticate(AuthenticationIdentity identity)
      throws RepositoryLoginException, RepositoryException;
}
