package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;

import java.util.List;
import java.util.logging.Logger;

public class MigrationReporter {
  
  private static final Logger LOG = Logger.getLogger(MigrationReporter.class.getName());
  
  public static void FolderMigrated(FolderInfo folderInfo, AclAdjustments aclAdjustments) {
    LOG.info("Pushed Folder: " + folderInfo);
    ReportSharePointAcl(folderInfo.getSharePointAcl());
    ReportCloudAcl(folderInfo.getCloudAcl());
    ReportCloudAclAdjustments(aclAdjustments);
  }
  
  public static void DocumentMigrated(Document document, CloudAcl cloudAcl,
      AclAdjustments aclAdjustments) {
    LOG.info("Pushed Document: " + document); 
    ReportSharePointAcl(document.getAcl());
    ReportCloudAcl(cloudAcl);
    ReportCloudAclAdjustments(aclAdjustments);
  }
  
  private static void ReportSharePointAcl(List<Ace> sharePointAce) {
    LOG.info("SharePoint ACL: " + sharePointAce);
  }
  
  private static void ReportCloudAcl(CloudAcl cloudAcl) {
    LOG.info(cloudAcl.toString());
  }
  
  private static void ReportCloudAclAdjustments(AclAdjustments aclAdjustments) {
    LOG.info(aclAdjustments.toString());
  }
  
}
