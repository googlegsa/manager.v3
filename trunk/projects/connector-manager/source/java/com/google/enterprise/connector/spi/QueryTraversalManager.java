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

/**
 * Interface for implementing query-based traversal.
 * <p>
 * The property maps returned by the queries defined here represent documents.
 * They must therefore contain special properties according to the following
 * rules:
 * <ul>
 * <li> {@link SpiConstants.PROPNAME_DOCID} This property must be present.
 * <li> {@link SpiConstants.PROPNAME_CONTENTURL} If present, the GSA will pull
 * content using the value of this property. If not present, then the
 * {@link SpiConstants.PROPNAME_CONTENT} property should be present.
 * <li> {@link SpiConstants.PROPNAME_CONTENT} Will not be used if the
 * {@link SpiConstants.PROPNAME_CONTENTURL} is present. If present, the
 * connector framework will base-64 encode the value and present it to the GSA
 * as the primary content to be indexed.
 * <li> {@link SpiConstants.PROPNAME_SECURITYCLASS} If present, this will be
 * persisted in the index and used at serve-time to determine whether a given
 * user is authorized to view this document.
 * <li> {@link SpiConstants.PROPNAME_DISPLAYURL} If present, this will be used
 * as the primary link on a results page
 * </ul>
 */
public interface QueryTraversalManager {

  /**
   * Gets a bootstrap query that is used to start traversal. This query will
   * return objects starting from the very oldest, or with the smallest IDs, or
   * whatever natural order the implementation prefers. After this initial call,
   * the caller should remember the last object returned, use the
   * {@link #getCheckpoint(PropertyMap)} method to obtain a string
   * representation of that document, then get all subsequent queries by calling
   * the {@link #getCheckpointTraversalQuery(String)} method.
   * 
   * @return A Query that returns documents from the repository in natural order
   * @throws RepositoryException if the Repository is unreachable or similar
   *           exceptional condition.
   */
  public Query getInitialTraversalQuery() throws RepositoryException;

  /**
   * Gets a query that continues traversal. The supplied checkPoint parameter
   * should have been created by a call to the
   * {@link #getCheckpoint(PropertyMap)} method. The Query object returned
   * should resume traversal of the repository in natural order starting just
   * after the document that was used to create the checkpoint string.
   * 
   * @param checkPoint String that indicates from where to resume traversal.
   * @return Query object that returns documents starting just after the
   *         checkpoint.
   * @throws RepositoryException
   */
  public Query getCheckpointTraversalQuery(String checkPoint)
      throws RepositoryException;

  /**
   * Gets a checkpoint string from a query result. A {@link Query} object
   * returns an iterator of property maps that represent documents. Any of these
   * could be passed in to the SPI to create a checkpoint string representing
   * that document.
   * 
   * @param pm A property map that should have been obtained from executing a
   *          Query obtained from either {@link #getInitialTraversalQuery()} or
   *          {link {@link #getCheckpointTraversalQuery(String)}.
   * @return A string that can be used by a subsequent call to the
   *         {@link #getCheckpointTraversalQuery(String)} method.
   * @throws RepositoryException
   */
  public String getCheckpoint(PropertyMap pm) throws RepositoryException;
}
