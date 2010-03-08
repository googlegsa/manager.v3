package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.SharepointSite;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class TraversalEngine {
  
  private Hashtable<String, Folder> folders = new Hashtable<String, Folder>();
  private Hashtable<String, List<Folder>> folderHierarchy = new Hashtable<String, List<Folder>>();
  private Hashtable<String, List<Document>> documentHierarchy = new Hashtable<String, List<Document>>();
  private CloudPusher cloudPusher;
  private SharepointSite site;
  private PermissionsMapper permissionsMapper;
  
  TraversalEngine(SharepointSite site, CloudPusher cloudPusher, PermissionsMapper permissionsMapper)
      throws Exception {
    this.site = site;
    this.cloudPusher = cloudPusher;
    this.permissionsMapper = permissionsMapper;
    buildFolderAndFileHierarchy();
  }

  public void pushFolderHierarchy(Folder folder) throws Exception {
    // Push the folder to the cloud.
    List<CloudAce> folderCloudAcl = permissionsMapper.mapAcl(folder.getAcl(), folder.getParentId(), folders);
    System.out.println("Mapped permission for " + folder.getName() + " : " + folderCloudAcl + printAcl(folder.getAcl()));
    cloudPusher.pushFolder(folders.get(folder.getParentId()), folder, folderCloudAcl);
    
    // If the folder contains subfolders then recursively push each one.
    if (folderHierarchy.containsKey(folder.getId())) {
      for (Folder childFolder : folderHierarchy.get(folder.getId())) {
        pushFolderHierarchy(childFolder);
      }
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
  
  public void pushDocumentHierarchy(Folder folder) throws Exception {
    // If the folder contains documents then push them to the cloud.
    if (documentHierarchy.containsKey(folder.getId())) {
      for (Document document : documentHierarchy.get(folder.getId())) {
        InputStream documentContentStream = site.getDocumentContent(document);
        List<CloudAce> documentCloudAcl =  permissionsMapper.mapAcl(document.getAcl(),
            document.getParentId(), folders);
        System.out.println("Mapped permission for " + document.getName() + " : " + documentCloudAcl + printAcl(document.getAcl()));
        cloudPusher.pushDocument(folder, document, documentCloudAcl, documentContentStream);
      }
    }
    
    // If the folder contains subfolders then recursively push each one.
    if (folderHierarchy.containsKey(folder.getId())) {
      for (Folder childFolder : folderHierarchy.get(folder.getId())) {
        pushDocumentHierarchy(childFolder);
      }
    }
  }
  
  private void buildFolderAndFileHierarchy() throws Exception {
    folderHierarchy = new Hashtable<String, List<Folder>>();
    documentHierarchy = new Hashtable<String, List<Document>>();

    // Get lists of all folders and documents for all document libraries.
    List<Folder> rootFolderList = site.getRootFolders();
    List<Folder> folderList = new ArrayList<Folder>();
    List<Document> documentList = new ArrayList<Document>();
    
    // Loop through each document library (root folder)
    for (Folder rootFolder : rootFolderList) {
      folderList.add(rootFolder);
      folderList.addAll(site.getFolders(rootFolder));
      documentList.addAll(site.getDocuments(rootFolder));
    }

    // Create a map so folders can be looked up by id.
    for (Folder folder : folderList) {
      folders.put(folder.getId(), folder);
    }

    // Create a map of folder ids to child folders.
    for (Folder folder : folderList) {

      // If this is the first time that we've seen a folder with
      // parent id then create a list for it.
      if (!folderHierarchy.containsKey(folder.getParentId())) {
        folderHierarchy.put(folder.getParentId(), new ArrayList<Folder>());
      }
      // Add the folder to its parent folder's list of child folders.
      folderHierarchy.get(folder.getParentId()).add(folder);
    }

    // Create a map of folder ids to child documents.
    for (Document document : documentList) {

      // If this is the first time that we've seen a folder with
      // parent id then create a list for it.
      if (!documentHierarchy.containsKey(document.getParentId())) {
        documentHierarchy.put(document.getParentId(), new ArrayList<Document>());
      }
      // Add the document to its parent folder's list of child documents.
      documentHierarchy.get(document.getParentId()).add(document);
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
