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
  private static final String CONNECTOR_TEAM_SHAREPOINT_ADMIN = "ENT_TEST_W2K3-C\\administrator";
  private static final String CONNECTOR_TEAM_SHAREPOINT_ADMIN_PASSWORD = "test";
  private static final String CONNECTOR_TEAM_SHAREPOINT_DOMAIN = "ENT_TEST_W2K3-C";

  /**
   * @param args
   */
   public static void main(String[] args) throws Exception {
     SharepointSite spSite = SharepointSiteFactory.getSharepointSite(
         TEST_SHAREPOINT_URL, TEST_SHAREPOINT_ADMIN,
         TEST_SHAREPOINT_ADMIN_PASSWORD, TEST_SHAREPOINT_DOMAIN);
     DocsService client = DoclistPusher.mkOauthClient(
         CONSUMER_KEY, CONSUMER_SECRET);
     FolderManager folderManager = new FolderManager();
     CloudPusher cloudPusher = new DoclistPusher(
         client, folderManager, ADMIN_ID, false);

     SharePointToCloudMigrator.migrate(
         cloudPusher, spSite, folderManager, ADMIN_ID);
  }
}