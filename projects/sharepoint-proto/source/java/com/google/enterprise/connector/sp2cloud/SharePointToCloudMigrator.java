// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.SharepointSite;

public class SharePointToCloudMigrator {
  
  public static void migrate(CloudPusher cloudPusher, SharepointSite sharePointSite, FolderManager folderManager) throws Exception {
    final String defaultOwner = "admin@sharepoint-connector.com";
    final boolean strictPermissionConversionRules = false;
    PermissionsMapper permissionMapper = new PermissionsMapper(PermissionsMapper.makeNameMap(), defaultOwner, strictPermissionConversionRules);
    TraversalEngine traverser = new TraversalEngine(sharePointSite, cloudPusher, permissionMapper, folderManager);
    for (Folder documentLibraryRoot : sharePointSite.getRootFolders()) {
      System.out.println("Mapping Owner for Document Library Root " + documentLibraryRoot.getName());
     documentLibraryRoot = documentLibraryRoot.fixFolderOwner(permissionMapper.mapPrincipleName(documentLibraryRoot.getOwner(), "admin@sharepoint-connector.com"));
      traverser.pushRootFolderHierarchy(documentLibraryRoot);
      traverser.pushDocuments();
    }
  }
}
