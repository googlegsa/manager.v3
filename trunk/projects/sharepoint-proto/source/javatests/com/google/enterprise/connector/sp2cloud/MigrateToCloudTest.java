// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.SharepointSite;
import com.google.enterprise.connector.sp2c_migration.SharepointSiteFactory;
import com.google.gdata.client.docs.DocsService;

public class MigrateToCloudTest {

  private static final String TEST_SHAREPOINT_URL = "http://ent-test-w2k3-sp2007";
  private static final String TEST_SHAREPOINT_ADMIN = "administrator";
  private static final String TEST_SHAREPOINT_ADMIN_PASSWORD = "test";
  private static final String TEST_SHAREPOINT_DOMAIN = "";
  private static final String CONNECTOR_TEAM_SHAREPOINT_URL = "http://ent-test-w2k3-c";
  private static final String CONNECTOR_TEAM_SHAREPOINT_ADMIN = "ENT_TEST_W2K3-C\\administrator";
  private static final String CONNECTOR_TEAM_SHAREPOINT_ADMIN_PASSWORD = "test";
  private static final String CONNECTOR_TEAM_SHAREPOINT_DOMAIN = "ENT_TEST_W2K3-C";
 private static final String ADMIN_ID = "admin@sharepoint-connector.com";
//  private static final String ADMIN_TOKEN =
//      "DQAAAI0AAACtL50YtS8-Zaksz6dHNGRqdok_JFP-DKxSDWbNVtQySK4PIIRc2GE9Zv1iC-Gt-SwrM6xcCuJBk4gSVt09HIpmq3n0mu2ZlTKbTF671NAPk6TD9w7-EP7rjJojBjACUQPhtSieb-iU6af9PqwyPW-XybYudrU1A9RCOaOIvcedr9A8GtDJWms1YrPolPG0Elw";
  private static final String CONSUMER_KEY = "sharepoint-connector.com";
  private static final String CONSUMER_SECRET = "A6dY3I1uW89q999msVYEX07l";

  /**
   * @param args
   */    
   public static void main(String[] args) throws Exception {
     SharepointSite spSite = SharepointSiteFactory.getSharepointSite(TEST_SHAREPOINT_URL, 
         TEST_SHAREPOINT_ADMIN, 
         TEST_SHAREPOINT_ADMIN_PASSWORD, 
         TEST_SHAREPOINT_DOMAIN);
//     SharepointSite spSite = SharepointSiteFactory.getSharepointSite(CONNECTOR_TEAM_SHAREPOINT_URL, 
//         CONNECTOR_TEAM_SHAREPOINT_ADMIN, 
//         CONNECTOR_TEAM_SHAREPOINT_ADMIN_PASSWORD, 
//         CONNECTOR_TEAM_SHAREPOINT_DOMAIN);
     DocsService client = DoclistPusher.mkOauthClient(CONSUMER_KEY, CONSUMER_SECRET);
     FolderManager folderManager = new FolderManager();
     CloudPusher cloudPusher = new DoclistPusher(client, folderManager, ADMIN_ID, false);
     
     SharePointToCloudMigrator.migrate(cloudPusher, spSite, folderManager, ADMIN_ID);
  }

}
