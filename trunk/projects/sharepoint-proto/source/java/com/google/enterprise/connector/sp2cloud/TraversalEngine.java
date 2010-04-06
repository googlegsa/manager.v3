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
  
  private FolderManager folderManager;
  private CloudPusher cloudPusher;
  private SharepointSite site;
  private PermissionsMapper permissionsMapper;
  
  TraversalEngine(SharepointSite site, CloudPusher cloudPusher, PermissionsMapper permissionsMapper, FolderManager folderManager)
      throws Exception {
    this.site = site;
    this.cloudPusher = cloudPusher;
    this.permissionsMapper = permissionsMapper;
    this.folderManager = folderManager;
    AddFoldersToFolderManager();
  }
  
  public void pushRootFolderHierarchy(Folder folder) throws Exception {
    pushFolderHierarchy(folderManager.getFolderInfo(folder.getId()));
  }

  public void pushFolderHierarchy(FolderInfo folderInfo) throws Exception {
    // Push the folder to the cloud.
    cloudPusher.pushFolder(folderInfo);

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
    
    // Loop through each document library (root folder)
    for (Folder rootFolder : rootFolderList) {
      documentList.addAll(site.getDocuments(rootFolder));
    }
    
    for (Document document : documentList) {
      CloudAcl documentCloudAcl =  permissionsMapper.mapAcl(document.getAcl(), document.getOwner());
      System.out.println("Mapped permission for " + document.getName() + " : " + documentCloudAcl + printAcl(document.getAcl()));
      InputStream documentContentStream = site.getDocumentContent(document);
      cloudPusher.pushDocument(document, documentCloudAcl, documentContentStream);
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
      folderManager.add(folderManager.newFolderInfo(folder.getId(), folder.getParentId(), folder.getName(),
          permissionsMapper.mapAcl(folder.getAcl(), folder.getOwner())));
    }
  }
  
//  public Folder findSharePointFolderFromPath(String path) throws Exception {
//    
//    return getDocumentLibraryRootFolder();
//    
    // for now we will always return the root
//    String[] folderNames = path.split("/");
//    Folder currentFolder = getDocumentLibraryRootFolder();
//    
//    for (String folderName : folderNames) {
//      for(Folder folder : folderHierarchy.get(currentFolder.getId())) {
//        if (folderName.compareToIgnoreCase(folder.getName()) == 0) {
//          currentFolder = folder;
//          break;
//        }
//        throw new Exception();
//      }
//    }
//    return currentFolder;
//    
//  }

}
