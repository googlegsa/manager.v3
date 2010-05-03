// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.SharepointSite;
import com.google.enterprise.connector.sp2c_migration.SharepointSiteFactory;
import com.google.gdata.client.docs.DocsService;

public class Sp2cMain {

  private static final String ADMIN_ID = "admin@sharepoint-connector.com";
  private static final String CONSUMER_KEY = "sharepoint-connector.com";
  private static final String CONSUMER_SECRET = "A6dY3I1uW89q999msVYEX07l";

  private static final String TEST_SHAREPOINT_URL = "http://ent-test-w2k3-sp2007";
  private static final String TEST_SHAREPOINT_ADMIN = "administrator";
  private static final String TEST_SHAREPOINT_ADMIN_PASSWORD = "test";
  private static final String TEST_SHAREPOINT_DOMAIN = "";

  private static final String CONNECTOR_TEAM_SHAREPOINT_URL = "http://ent-test-w2k3-c";
  private static final String CONNECTOR_TEAM_SHAREPOINT_ADMIN = "administrator";
  private static final String CONNECTOR_TEAM_SHAREPOINT_ADMIN_PASSWORD = "test";
  private static final String CONNECTOR_TEAM_SHAREPOINT_DOMAIN = "ENT-TEST-W2K3-C";

  /**
   * @param args
   */
   public static void main(String[] args) throws Exception {

     String url = TEST_SHAREPOINT_URL;
     String admin = TEST_SHAREPOINT_ADMIN;
     String password = TEST_SHAREPOINT_ADMIN_PASSWORD;
     String domain = TEST_SHAREPOINT_DOMAIN;

     if (args.length == 4) {
       url = args[0];
       admin = args[1];
       password = args[2];
       domain = args[3];
     }

     SharepointSite spSite = SharepointSiteFactory.getSharepointSite(url, admin, password, domain);
     DocsService client = DoclistPusher.mkOauthClient(CONSUMER_KEY, CONSUMER_SECRET);
     FolderManager folderManager = new FolderManager();
     CloudPusher cloudPusher = new DoclistPusher(client, folderManager, ADMIN_ID, false);

     SharePointToCloudMigrator.migrate(cloudPusher, spSite, folderManager, ADMIN_ID);
  }
}