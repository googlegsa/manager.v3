package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.enterprise.connector.sp2c_migration.SharepointSite;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.activation.MimetypesFileTypeMap;

public class TraversalEngine {
  
  private Hashtable<String, Folder> folders = new Hashtable<String, Folder>();
  private Hashtable<String, List<Folder>> folderHierarchy = new Hashtable<String, List<Folder>>();
  private Hashtable<String, List<Document>> documentHierarchy = new Hashtable<String, List<Document>>();
  private CloudPusher cloudPusher;
  private SharepointSite site;
  
  TraversalEngine(SharepointSite site, CloudPusher cloudPusher) throws Exception {
    this.site = site;
    this.cloudPusher = cloudPusher;
    buildFolderAndFileHierarchy();
  }

  public void pushFolderHierarchy(Folder folder) throws Exception {
    // Push the folder to the cloud.
    cloudPusher.pushFolder(folders.get(folder.getParentId()), folder);
    
    // If the folder contains subfolders then recursively push each one.
    if (folderHierarchy.containsKey(folder.getId())) {
      for (Folder childFolder : folderHierarchy.get(folder.getId())) {
        pushFolderHierarchy(childFolder);
      }
    }
  }
  
  public void pushDocumentHierarchy(Folder folder) throws Exception {
    // If the folder contains documents then push them to the cloud.
    if (documentHierarchy.containsKey(folder.getId())) {
      for (Document document : documentHierarchy.get(folder.getId())) {
        InputStream documentContentStream = site.getDocumentContent(document);
        cloudPusher.pushDocument(folder, document, documentContentStream);
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
