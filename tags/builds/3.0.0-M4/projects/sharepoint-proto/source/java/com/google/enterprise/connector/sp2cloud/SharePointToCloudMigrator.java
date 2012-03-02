// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.SharepointSite;

public class SharePointToCloudMigrator {

  public static void migrate(CloudPusher cloudPusher, SharepointSite sharePointSite,
      FolderManager folderManager, String adminId, String sourcePath) throws Exception {
    final String defaultOwner = "admin@sharepoint-connector.com";
    final boolean strictPermissionConversionRules = false;
    PermissionsMapper permissionMapper =
        new PermissionsMapper(PermissionsMapper.makeNameMap(), defaultOwner,
            strictPermissionConversionRules);
    TraversalEngine traverser =
        new TraversalEngine(sharePointSite, cloudPusher, permissionMapper, folderManager, adminId);

    traverser.pushFolderHierarchyFromPath(sourcePath);
    traverser.pushDocuments();
  }
}
