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

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.util.Properties;

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

  public void testInitialize() throws RepositoryException {
    SocialCollectionHandler
        .initializeSocialCollection(gsaHost, gsaPort, gsaAdmin,
            gsaAdminPassword, SpiConstants.DEFAULT_USERPROFILE_COLLECTION);
  }

}
