// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.SharepointSite;
import com.google.enterprise.connector.sp2c_migration.SharepointSiteFactory;
import com.google.gdata.client.docs.DocsService;

public class Sp2cMain {
  /**
   * @param args
   */
   public static void main(String[] args) throws Exception {
     Sp2cConfig config = Sp2cConfig.newConfig(args);

     SharepointSite spSite = SharepointSiteFactory.getSharepointSite(config.getSpUrl(),
         config.getSpAdminId(), config.getSpAdminPassword(), config.getSpDomain());
     DocsService client = DoclistPusher.mkOauthClient(config.getCloudConsumerKey(),
         config.getCloudConsumerSecret());
     FolderManager folderManager = new FolderManager();
     CloudPusher cloudPusher = new DoclistPusher(client, folderManager, config.getCloudAdminId(),
         false);

     SharePointToCloudMigrator.migrate(cloudPusher, spSite, folderManager,
         config.getCloudAdminId());
  }
}
