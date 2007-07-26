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

import com.google.enterprise.connector.spi.old.TraversalManager;

/**
 * The primary access point to information from the
 * repository.  In the future, we may add access to the root
 * node of the repository, for crawling, or to a listener-registry for event
 * notification.
 */
public interface Session {
  
  /**
   * Gets a TraversalManager to implement query-based traversal.  
   * @return    a TraversalManager - should not be null
   * @throws RepositoryException
   */
  public TraversalManager getTraversalManager() 
      throws RepositoryException;
  
  /**
   * Gets an AuthenticationManager.  It is permissible to return null.  
   * A null return means that this implementation does not support an
   * Authentication Manager.  This may be for one of these reasons:
   * <ul>
   * <li> Authentication is not needed for this data source
   * <li> Authentication is handled through another Search Appliance-supported mechanism,
   * such as LDAP
   * </ul>
   * @return    a AuthenticationManager - may be null
   * @throws RepositoryException
   */
  public AuthenticationManager getAuthenticationManager() 
      throws RepositoryException;
  
  /**
   * Gets an AuthorizationManager.  It is permissible to return null.  
   * A null return means that this implementation does not support an
   * Authorization Manager.  This may be for one of these reasons:
   * <ul>
   * <li> Authorization is not needed for this data source - all documents are
   * public
   * <li> Authorization is handled through another Search Appliance-supported mechanism,
   * such as NTLM or Basic Auth
   * </ul>
   * @return    a AuthorizationManager - may be null
   * @throws RepositoryException
   */
  public AuthorizationManager getAuthorizationManager() 
      throws RepositoryException;
  
}