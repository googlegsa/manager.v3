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

package com.google.enterprise.connector.spi;

/**
 * Authentication Manager. All calls for end-user query authentication pass
 * through this interface.
 *
 * @since 1.0
 */
public interface AuthenticationManager {

  /**
   * Authenticates against the repository and returns an
   * {@link AuthenticationResponse} indicating whether authentication
   * was successful.
   *
   * If the authentication was successful, the connector may return a
   * {@code Collection} of group names representing any groups for which
   * the user is a member.
   *
   * If the password supplied in the {@link AuthenticationIdentity}
   * is {@code null}, the connector may skip authentication, but still return
   * a valid {@link AuthenticationResponse} containing a {@code Collection}
   * of group names representing any groups for which the user is a member.
   *
   * If an exception is thrown, the implementor should provide
   * an explanatory message.
   *
   * @param  identity an {@link AuthenticationIdentity} object that encapsulates
   *         the user's identity
   * @return an {@link AuthenticationResponse} indicating whether authentication
   *         was successful, or if the identity password was {@code null}, the
   *         {@code Collection} of groups to which the user belongs
   *
   * @throws RepositoryLoginException if there is a credentials-related problem
   *         that prohibits authentication
   * @throws RepositoryException if there is a more general problem, such
   *         as the system is unreachable or down
   */
  public AuthenticationResponse authenticate(AuthenticationIdentity identity)
      throws RepositoryLoginException, RepositoryException;
}
