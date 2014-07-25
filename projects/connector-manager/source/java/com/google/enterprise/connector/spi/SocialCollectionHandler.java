// Copyright 2012 Google Inc. 
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

import java.util.logging.Logger;

/**
 * SocialCollectionHandler initializes a social collection, which is a container
 * in GSA for social documents, like user profile documents. These documents are
 * excluded from default_collection. Specific modules interested in this data
 * can query this collection to retrieve all the data.
 * 
 * @author tapasnay
 * @since 3.0
 * @deprecated Use the Admin Console to create the collection for user profiles
 */
@Deprecated
public final class SocialCollectionHandler {
  private static final Logger LOGGER = Logger
      .getLogger(SocialCollectionHandler.class.getName());
  private static final String PREFIX_DOCID = "social:";

  private SocialCollectionHandler() {
    // prevents instantiation
  }

  /**
   * Initialize user profile collection. Since this class was deprecated,
   * this method does not create the collection on the GSA. It only
   * validates the collection name and logs a warning that the collection
   * must be created manually.
   *
   * @param gsaHost host address of GSA
   * @param gsaPort port of GSA for gdata call
   * @param gsaAdmin userId for making gdata call to GSA
   * @param gsaAdminPassword password for making gdata call to GSA
   * @param collectionName name of the collection where user profiles will go.
   * @return {@code true} if social collection has been initialized successfully
   */
  public static boolean initializeSocialCollection(String gsaHost, int gsaPort,
      String gsaAdmin, String gsaAdminPassword, String collectionName) {
    collectionName = getCollectionName(collectionName);
    if (!(validateCollectionName(collectionName))) {
      LOGGER.warning("Invalid collection name [" + collectionName + "]");
      return false;
    }

    LOGGER.warning("Collection could not be created. Please create"
        + " collection [" + collectionName + "] with following definition ["
        + SocialCollectionHandler.getSocialRegexp(collectionName)
        + "] manually");
    return false;
  }

  /**
   * Gets regular expression defining the scope of collection to hold user 
   * profiles. User profiles are fed as content feed.
   * 
   * @param collectionName name of collection to contain the user profiles
   * @return regular expression matching docids for profiles
   */
  private static String getSocialRegexp(String collectionName) {
    return "regexp:googleconnector:.*\\\\?docid="
        + getDocIdPrefix(getCollectionName(collectionName)) + ".*";
  }

  /**
   * Returns prefix to be used for docId generated for user profile documents
   * which are destined to go to the specified collection. DocIds with this
   * prefix match up with the regular expression associated with the collection.
   * If collectionName is null or "" then default collection
   * <code>_google_social_userprofile_collection</code> is used.
   * 
   * @param collectionName name of collection
   * @return DocId prefix
   */
  static String getDocIdPrefix(String collectionName) {
    return PREFIX_DOCID + getCollectionName(collectionName) + ":";
  }

  /**
   * Gets collection name for use from the passed in parameter. If parameter
   * passed is null or "" then default collection name
   * <code>_google_social_userprofile_collection</code> is returned.
   * 
   * @param collectionName name of collection
   * @return actual collection Name to be used
   */
  private static String getCollectionName(String collectionName) {
    if ((collectionName == null) || collectionName.equals("")) {
      return SpiConstants.DEFAULT_USERPROFILE_COLLECTION;
    }
    return collectionName;
  }

  /**
   * Validates name of collection to consist of [a-z][A-Z][0-9]-_
   * 
   * @param collectionName name of collection
   * @return true if valid
   */
  public static boolean validateCollectionName(String collectionName) {
    for (char c : collectionName.toCharArray()) {
      if (!(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))
          || ((c >= '0') && (c <= '9')) || (c == '_') || (c == '-')))
        return false;
    }
    return true;
  }
}
