// Copyright 2006 Google Inc.
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

/**
 * The primary access point to information from the repository.
 * In the future, we may add access to the root node of the repository,
 * for crawling, or to a listener-registry for event notification.
 */
public interface Session {

  /**
   * Gets a {@link TraversalManager} to implement query-based traversal.
   *
   * @return a {@link TraversalManager} - should not be {@code null}
   * @throws RepositoryException
   */
  public TraversalManager getTraversalManager()
      throws RepositoryException;

  /**
   * Gets an {@link AuthenticationManager}.  It is permissible to return
   * {@code null}.  A {@code null} return means that this implementation does
   * not support an authentication manager.  This may be for one of these
   * reasons:
   * <ul>
   * <li> Authentication is not needed for this data source</li>
   * <li> Authentication is handled through another Search Appliance-supported
   * mechanism, such as LDAP</li>
   * </ul>
   *
   * @return an {@link AuthenticationManager} - may be {@code null}
   * @throws RepositoryException
   */
  public AuthenticationManager getAuthenticationManager()
      throws RepositoryException;

  /**
   * Gets an {@link AuthorizationManager}.  It is permissible to return
   * {@code null}.  A {@code null} return means that this implementation does
   * not support an authorization manager.  This may be for one of these
   * reasons:
   * <ul>
   * <li> Authorization is not needed for this data source - all documents are
   * public</li>
   * <li> Authorization is handled through another Search Appliance-supported
   * mechanism, such as NTLM or Basic Auth</li>
   * </ul>
   *
   * @return an {@link AuthorizationManager} - may be {@code null}
   * @throws RepositoryException
   */
  public AuthorizationManager getAuthorizationManager()
      throws RepositoryException;
}