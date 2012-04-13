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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Interface for a feed connection. This takes in a data source name and a data
 * source object that contains the data to be sent.  The actual connection to
 * the feed server should be established by the implementation during
 * construction or initialization.
 */
public interface FeedConnection {

  /**
   * Sends data contained in the given data object identified as the given data
   * source name.
   *
   * @param feedData an object that encapsulates the feed data that needs to be
   *        sent by the <code>FeedConnection</code>.
   * @return response from the feed server.
   * @throws FeedException if problem extracting the data or sending it.
   * @throws RepositoryException if problem retrieving data from the Connector.
   */
  public String sendData(FeedData feedData)
      throws FeedException, RepositoryException;

  /**
   * Returns true if the Feed host has large number of unprocessed Feed items.
   * The Feed host may temporarily stop processing Feed items during periodic
   * maintenance, when resetting the index, during system configuration, or
   * due to certain error conditions. If backlogged, the Feed client may choose
   * to throttle back its feeds until the backlog clears.
   *
   * @return true if the Feed host is known to be backlogged processing feeds,
   *         false otherwise.
   */
  public boolean isBacklogged();

  /**
   * Return a String consisting of a comma-separated list supported content
   * encodings.  For instance: "base64binary, base64compressed".
   *
   * @return supported content encodings.
   */
  public String getContentEncodings();

  /**
   * Returns {@code true} if Documents may include full ACL support,
   * specifically DENY users or groups, ACL inheritance, and ACL-only
   * Documents.  Some earlier Search Appliance implementations do not
   * support these features.
   *
   * @return {@code true} if Documents may include enhanced ACL support
   *
   * @see SecureDocument
   * @see SpiConstants.AclInheritanceType
   * @see SpiConstants.FeedType
   * @see SpiConstants#PROPNAME_ACLINHERITFROM
   * @see SpiConstants#PROPNAME_ACLINHERITANCETYPE
   * @see SpiConstants#PROPNAME_ACLDENYGROUPS
   * @see SpiConstants#PROPNAME_ACLDENYUSERS
   *
   * @since 3.0
   */
  boolean supportsInheritedAcls();
}
