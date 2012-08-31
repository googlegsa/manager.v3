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

import com.google.enterprise.apis.client.GsaClient;
import com.google.enterprise.apis.client.GsaEntry;
import com.google.enterprise.connector.util.SslUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * SocialCollectionHandler initializes a social collection, which is a container
 * in GSA for social documents, like user profile documents. These documents are
 * excluded from default_collection. Specific modules interested in this data
 * can query this collection to retrieve all the data.
 * 
 * @author tapasnay
 * @since 3.0
 */
public final class SocialCollectionHandler {
  private static final Logger LOGGER = Logger
      .getLogger(SocialCollectionHandler.class.getName());
  private static final String URLLIST_SEPARATOR = "\n";
  private static final String DEFAULT_COLLECTION = "default_collection";
  private static final String PREFIX_DOCID = "social:";

  private SocialCollectionHandler() {
    // prevents instantiation
  }

  /**
   * Ensures the required collection exists and configured. If the collection
   * already exists and configured, nothing happens here. If not then this one
   * creates and configures the collection.
   * 
   * @param collectionName name of collection for user profiles
   * @param urlFollowPattern urlFollowPatterns for the collection
   * @param gsahostAddr host address of the GSA server
   * @param port enterpriseController port of GSA server
   * @param user user id for gdata call
   * @param password password for gdata call
   * @return name of the collection
   * @throws RepositoryException
   */
  private static String ensureUserProfileCollection(String collectionName,
      String urlFollowPattern, String gsahostAddr, int port, String user,
      String password) throws RepositoryException {
    // add the url pattern to social_userprofile_collection and add it to
    // doNotFollowUrl in default_collection
    SSLSocketFactory originalSocketFactory = null;
    HostnameVerifier originalHostnameVerifier = null;
    try {
      GsaClient client = null;
      try {
        client = new GsaClient(
            "https", gsahostAddr, port == 8000 ? 8443 : port, user, password);
      } catch (AuthenticationException eauth) {
        if (eauth.getCause() instanceof SSLException) {
          client = null;
          LOGGER.warning("SSL communication with Administration console "
              + "failed!. Trying without certificate verification. Consider "
              + "setting up certificates between connector manager and the GSA "
              + " for secure communication.");
          LOGGER.log(Level.FINE, "SSL error", eauth);
        }
      }

      if (client == null) {
        originalSocketFactory = SslUtil.setTrustingDefaultHttpsSocketFactory();
        if (originalSocketFactory != null) {
          originalHostnameVerifier = 
              SslUtil.setTrustingDefaultHostnameVerifier();
        }

        try {
          client = new GsaClient(
              "https", gsahostAddr, port == 8000 ? 8443 : port, user, password);
        } catch (AuthenticationException eauth) {
          client = null;
          LOGGER.warning("SSL communication with Administration "
              + "console without certificate verification failed! Trying "
              + "plaintext.");
          LOGGER.log(Level.FINE, "SSL Error", eauth);
          HttpsURLConnection.setDefaultSSLSocketFactory(originalSocketFactory);
          HttpsURLConnection.setDefaultHostnameVerifier(
              originalHostnameVerifier);
          originalHostnameVerifier = null;
          originalSocketFactory = null;
        }
      }

      if (client == null) {
        try {
          client = new GsaClient(gsahostAddr, port, user, password);
        } catch (AuthenticationException eauth) {
          LOGGER.severe("Could not authenticate with the GSA as '" + user
              + "' to create the social collection. Please check the "
              + "credentials");
          throw new RepositoryException(eauth);
        }
      }
      GsaEntry collection = null;
      try {
        collection = client.getEntry("collection", collectionName);
      } catch (ServiceException ex) {
        GsaEntry newColl = new GsaEntry();
        newColl.addGsaContent("collectionName", collectionName);
        newColl.addGsaContent("insertMethod", "default");
        newColl.addGsaContent("folowURLs", urlFollowPattern);
        try {
          client.insertEntry("collection", newColl);
        } catch (ServiceException e) {
          // Maybe we got the exception because the collection is already
          // created by some other thread
          try {
            collection = client.getEntry("collection", collectionName);
          } catch (ServiceException ex1) {
            // We are here because the collection does not exist and
            // collection could not be created. There may be validation error
            // for the collection name
            throw new RepositoryException(ex1);
          }
        }
      }
      if (collection != null) {
        // collection will be not null if it was found with getEntry either
        // the first time itself or after an attempt to insert. If insert
        // succeeds then collection is null and we do not want to update
        // since the collection is just created as needed.
        try {
          updateCollection(collection, urlFollowPattern, client, collectionName);
        } catch (ServiceException exSvc) {
          throw new RepositoryException(exSvc);
        }
      }
      // we will also remove the pattern from default_collection
      try {
        collection = client.getEntry("collection", DEFAULT_COLLECTION);
        addDontFollowUrl(collection, urlFollowPattern, client);
      } catch (ServiceException e) {
        // ignore: the default_collection does not exist, unusual, but no
        // issues
      }
    } catch (MalformedURLException exMal) {
      throw new RepositoryException(exMal);
    } catch (IOException exIO) {
      throw new RepositoryException(exIO);
    } finally {
      if (originalSocketFactory != null) {
        HttpsURLConnection.setDefaultSSLSocketFactory(originalSocketFactory);
      }
      if (originalHostnameVerifier != null) {
        HttpsURLConnection.setDefaultHostnameVerifier(originalHostnameVerifier);
      }
    }
    LOGGER.info("Collection [" + collectionName + "] verified");
    return collectionName;
  }

  private static void addDontFollowUrl(GsaEntry collection,
      String urlFollowPattern, GsaClient client) throws MalformedURLException,
      ServiceException, IOException {
    String dontFollowUrl = collection.getGsaContent("doNotCrawlURLs");
    if (!listContains(dontFollowUrl, urlFollowPattern)) {
      dontFollowUrl = appendToUrlList(dontFollowUrl, urlFollowPattern);
      collection.addGsaContent("doNotCrawlURLs", dontFollowUrl);
      client.updateEntry("collection", DEFAULT_COLLECTION, collection);
    }
  }

  private static void updateCollection(GsaEntry collection,
      String urlFollowPattern, GsaClient client, String collectionName)
      throws MalformedURLException, ServiceException, IOException {
    String followUrls = collection.getGsaContent("followURLs");
    if (!listContains(followUrls, urlFollowPattern)) {
      followUrls = appendToUrlList(followUrls, urlFollowPattern);
      collection.addGsaContent("followURLs", followUrls);
      client.updateEntry("collection", collectionName, collection);
    }
  }

  private static String appendToUrlList(String followUrls,
      String urlFollowPattern) {
    return followUrls + URLLIST_SEPARATOR + urlFollowPattern;
  }

  private static boolean listContains(String followUrls, String urlFollowPattern) {
    String[] parts = followUrls.split(URLLIST_SEPARATOR);
    for (String e : parts) {
      if (e.equals(urlFollowPattern))
        return true;
    }
    return false;
  }

  /**
   * Initialize user profile collection. Every social connector should call this
   * once to ensure GSA is ready to accept user profile data from the connector.
   * If collectionName parameter is null or "" then default collection
   * <code>_google_social_userprofile_collection</code> is used.
   * 
   * @param gsaHost host address of GSA
   * @param gsaPort port of GSA for gdata call
   * @param gsaAdmin userId for making gdata call to GSA
   * @param gsaAdminPassword password for making gdata call to GSA
   * @param collectionName name of the collection where user profiles will go.
   * @returns if social collection has been initialized successfully
   */
  public static boolean initializeSocialCollection(String gsaHost, int gsaPort,
      String gsaAdmin, String gsaAdminPassword, String collectionName) {
    collectionName = getCollectionName(collectionName);
    if (!(validateCollectionName(collectionName))) {
      LOGGER.warning("Invalid collection name ["
          + getCollectionName(collectionName) + "]"); 
      return false;
    }

    try {
      ensureUserProfileCollection(collectionName,
          getSocialRegexp(collectionName), gsaHost, gsaPort, gsaAdmin,
          gsaAdminPassword);
    } catch (RepositoryException e) {
      LOGGER.warning("Collection could not be created. Please create"
          + " collection [" + collectionName + "] with following definition ["
          + SocialCollectionHandler.getSocialRegexp(collectionName)
          + "] manually");
      LOGGER.log(Level.FINE, "Collection creation error", e);
      return false;
    }
    return true;
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
