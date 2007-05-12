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

import java.util.List;

/**
 * Authorization Manager. All calls related to authorizing particular users
 * to see particular documents pass through this interface.
 */
public interface AuthorizationManager {

  /**
   * Gets authorization from the repository for a set of documents by ID.
   * 
   * @param docidList The document set represented as a list of Strings:
   *        the docid for each document. The docid strings which the
   *        connector manager will pass in should have come from the search
   *        appliance, from documents that this connector submitted for
   *        indexing via traversal. Thus the docids should have started as
   *        {@link SpiConstants}.PROPNAME_DOCID properties that were part
   *        of an {@link ResultSet} returned from a
   *        {@link QueryTraversalManager}.startTraversal or
   *        {@link QueryTraversalManager}.resumeTraversal call.
   * @param username The username as a string
   * @return A {@link ResultSet} where each {@link PropertyMap} has a
   *         String property {@link SpiConstants}.PROPNAME_DOCID with a
   *         document ID from the list, and a boolean property
   *         {@link SpiConstants}.PROPNAME_AUTH_VIEWPERMIT that indicates
   *         whether this user can view this document. This result set
   *         should contain an element for each document id supplied, but
   *         does not need to be in the same order as the docidList.
   * @throws RepositoryException
   */
  ResultSet authorizeDocids(List docidList, String username)
      throws RepositoryException;

}
