// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.SharepointSite;
import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TraversalEngine {

  private final FolderManager folderManager;
  private final CloudPusher cloudPusher;
  private final SharepointSite site;
  private final PermissionsMapper permissionsMapper;
  private final String adminId;

  TraversalEngine(SharepointSite site, CloudPusher cloudPusher, PermissionsMapper permissionsMapper,
      FolderManager folderManager, String adminId)
      throws Exception {
    this.site = site;
    this.cloudPusher = cloudPusher;
    this.permissionsMapper = permissionsMapper;
    this.folderManager = folderManager;
    this.adminId = adminId;
    AddFoldersToFolderManager();
  }

  public void pushFolderHierarchyFromPath(String path) throws Exception {
    pushFolderHierarchyToCloudLocation(findSharePointFolderFromPath(path), null);
  }
  
  public void pushRootFolderHierarchy(Folder folder) throws Exception {
    pushFolderHierarchy(folderManager.getFolderInfo(folder.getId()));
  }
  
  public void pushFolderHierarchy(FolderInfo folderInfo) throws Exception {
    pushFolderHierarchyToCloudLocation(folderInfo, folderInfo.getParent());
  }

  public void pushFolderHierarchyToCloudLocation(FolderInfo folderInfo, FolderInfo parent)
  throws Exception {
    // Push the folder to the cloud.
    AclAdjustments aclAdjustments = folderInfo.getCloudAcl().getAclAdjustments(folderInfo.getParentCloudAcl(),
      adminId);
    String cloudBaseUrl = cloudPusher.pushFolder(folderInfo.getName(), parent,
        folderInfo.getCloudAcl().getOwner(), aclAdjustments);
    
    folderInfo.setBaseUrl(cloudBaseUrl);

    MigrationReporter.FolderMigrated(folderInfo, aclAdjustments);

    // If the folder contains subfolders then recursively push each one.
    for (FolderInfo childFolderInfo : folderInfo.getChildFolders()) {
      pushFolderHierarchy(childFolderInfo);
    }
  }

  private String printAcl(List<Ace> acl) {
    StringBuilder sb = new StringBuilder();
    for (Ace ace : acl) {
      sb.append("(Name: " + ace.getName() + " ");
      sb.append(Arrays.asList(ace.getPermission().getAllowedPermissions()) + ") ");
    }
    return sb.toString();
  }

  public void pushDocuments()  throws Exception {
    // Get lists of all documents for all document libraries.
    List<Folder> rootFolderList = site.getRootFolders();
    List<Document> documentList = new ArrayList<Document>();

    // TODO(johnfelton) - Only get documents for root folders on our path
    // Loop through each document library (root folder)
    for (Folder rootFolder : rootFolderList) {
      documentList.addAll(site.getDocuments(rootFolder));
    }

    for (Document document : documentList) {
      FolderInfo parent = folderManager.getFolderInfo(document.getParentId());
      // Only push documents if their parent folder has been pushed.
      if ((parent.getBaseUrl() != null) && (!parent.getBaseUrl().isEmpty())) {
        CloudAcl documentCloudAcl =  permissionsMapper.mapAcl(document.getAcl(), document.getOwner());
        AclAdjustments aclAdjustments = documentCloudAcl.getAclAdjustments(parent.getCloudAcl(),
            adminId);
        InputStream documentContentStream = site.getDocumentContent(document);
        cloudPusher.pushDocument(document, parent, documentCloudAcl.getOwner(), aclAdjustments,
            documentContentStream);
        MigrationReporter.DocumentMigrated(document, documentCloudAcl, aclAdjustments);
      }
    }
  }

  private void AddFoldersToFolderManager() throws Exception {
    // Get lists of all folders for all document libraries.
    List<Folder> rootFolderList = site.getRootFolders();
    List<Folder> folderList = new ArrayList<Folder>();

    // Loop through each document library (root folder) and add it and all
    // folders in the document library to the folder list.
    for (Folder rootFolder : rootFolderList) {
      folderList.add(rootFolder);
      folderList.addAll(site.getFolders(rootFolder));
    }

    // Add all folders in the folder list to the folder manager.
    for (Folder folder : folderList) {
      folderManager.add(folderManager.newFolderInfo(folder.getId(),
          folder.isRootFolder() ? null : folder.getParentId(), folder.getName(),
          permissionsMapper.mapAcl(folder.getAcl(), folder.getOwner()), folder.getAcl()));
    }
  }

  private FolderInfo chooseFolderByName(String name, List<FolderInfo> folders) {
    for (FolderInfo folderInfo : folders) {
      if (name.compareToIgnoreCase(folderInfo.getName()) == 0) {
        return folderInfo;
      }
    }
    return null;
  }
  
  public FolderInfo findSharePointFolderFromPath(String path) throws Exception {

    String[] folderNames = path.split("/");
    List<FolderInfo> childFolders = folderManager.getRootFolders();
    FolderInfo currentFolderInfo = null;

    for (String folderName : folderNames) {
      currentFolderInfo = chooseFolderByName(folderName, childFolders);
      if (currentFolderInfo == null) {
        // TODO(johnfelton) - create an custom exception class.
        throw new Exception("SharePoint source folder not found '" + path + "'.");
      }
      childFolders = currentFolderInfo.getChildFolders();
    }
    return currentFolderInfo;
  }

}
