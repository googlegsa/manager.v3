package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.SharepointSite;

public class SharePointToCloudMigrator {
  
  public static void migrate(CloudPusher cloudPusher, SharepointSite sharePointSite) throws Exception {
    TraversalEngine traverser = new TraversalEngine(sharePointSite, cloudPusher);
    for (Folder documentLibraryRoot : sharePointSite.getRootFolders()) {
      traverser.pushFolderHierarchy(documentLibraryRoot);
      traverser.pushDocumentHierarchy(documentLibraryRoot);
    }
  }
}
