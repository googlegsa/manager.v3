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
import com.google.gdata.util.AuthenticationException;

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Random;

public class SocialCollectionHandlerTest extends TestCase {
  String gsaHost;
  int gsaPort;
  String gsaAdmin;
  String gsaAdminPassword;

  protected void setUp() throws Exception {
    super.setUp();
    final Properties properties = new Properties();
    // TODO: refer to google-enterprise-connector-manager.properties
    properties.load(new FileInputStream(
        "source/javatests/TestConfig.properties"));
    gsaHost = properties.getProperty("GsaHost");
    gsaPort = Integer.parseInt(properties.getProperty("GsaPort"));
    gsaAdmin = properties.getProperty("GsaAdminUsername");
    gsaAdminPassword = properties.getProperty("GsaAdminPassword");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private GsaEntry getCollection(String collectionName) throws Exception {
    GsaClient client = 
        new GsaClient(gsaHost, gsaPort, gsaAdmin, gsaAdminPassword);
    return client.getEntry("collection", collectionName);
  }

  public void testInitialize() throws Exception {
    SocialCollectionHandler
        .initializeSocialCollection(gsaHost, gsaPort, gsaAdmin,
            gsaAdminPassword, SpiConstants.DEFAULT_USERPROFILE_COLLECTION);
    assertNotNull(getCollection(SpiConstants.DEFAULT_USERPROFILE_COLLECTION));
  }

  public void testInitializeRandom() throws Exception {
    Random random = new Random();
    String collectionName = SpiConstants.DEFAULT_USERPROFILE_COLLECTION
        + random.nextInt();
    SocialCollectionHandler.initializeSocialCollection(gsaHost, gsaPort,
        gsaAdmin, gsaAdminPassword, collectionName);
    assertNotNull(getCollection(collectionName));
  }

  public void testInitializeNull() throws Exception {
    SocialCollectionHandler.initializeSocialCollection(gsaHost, gsaPort,
        gsaAdmin, gsaAdminPassword, null);
    assertNotNull(getCollection(SpiConstants.DEFAULT_USERPROFILE_COLLECTION));
  }

  public void testInitializeEmpty() throws Exception {
    SocialCollectionHandler.initializeSocialCollection(gsaHost, gsaPort,
        gsaAdmin, gsaAdminPassword, "");
    assertNotNull(getCollection(SpiConstants.DEFAULT_USERPROFILE_COLLECTION));
  }

  public void testInitializeAuthException() throws Exception {
    try {
      SocialCollectionHandler.initializeSocialCollection(gsaHost, gsaPort,
          gsaAdmin, gsaAdminPassword + "x", "");
      fail("Expected an exception");
    } catch (RepositoryException e) {
      //good;
    }
  }
}
