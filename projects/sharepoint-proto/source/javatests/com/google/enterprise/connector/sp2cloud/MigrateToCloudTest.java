// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.SharepointSite;
import com.google.enterprise.connector.sp2c_migration.SharepointSiteFactory;
import com.google.gdata.client.docs.DocsService;

public class MigrateToCloudTest {

  private static final String ADMIN_ID = "admin@sharepoint-connector.com";
//  private static final String ADMIN_TOKEN =
//      "DQAAAI0AAACtL50YtS8-Zaksz6dHNGRqdok_JFP-DKxSDWbNVtQySK4PIIRc2GE9Zv1iC-Gt-SwrM6xcCuJBk4gSVt09HIpmq3n0mu2ZlTKbTF671NAPk6TD9w7-EP7rjJojBjACUQPhtSieb-iU6af9PqwyPW-XybYudrU1A9RCOaOIvcedr9A8GtDJWms1YrPolPG0Elw";
  private static final String CONSUMER_KEY = "sharepoint-connector.com";
  private static final String CONSUMER_SECRET = "A6dY3I1uW89q999msVYEX07l";

  /**
   * @param args
   */    
   public static void main(String[] args) throws Exception {
     SharepointSite spSite = SharepointSiteFactory.getSharepointSite("http://ent-test-w2k3-sp2007", "administrator", "test", "");
     DocsService client = DoclistPusher.mkOauthClient(CONSUMER_KEY, CONSUMER_SECRET);
     FolderManager folderManager = new FolderManager();
     CloudPusher cloudPusher = new DoclistPusher(client, folderManager, ADMIN_ID, false);
     
     SharePointToCloudMigrator.migrate(cloudPusher, spSite, folderManager);
  }

}
