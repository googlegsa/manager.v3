package com.google.enterprise.connector.sp2c_migration;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2c_migration.Folder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FolderTraverser {

  private List<Folder> folderList = new ArrayList<Folder>();
  private List<Document> documentList = new ArrayList<Document>();
  private int idCounter = 1;
  private HashMap<String, File> files = new HashMap<String, File>();
  private Folder rootFolder;

  public FolderTraverser(String folderPath) {
    File rootFolder = new File(folderPath);
    String folderId = addFolder(rootFolder, "NONE", true);
    traverseChildren(rootFolder, folderId);
  }

  public List<Folder> getFolders() {
    return folderList;
  }

  public List<Document> getDocuments() {
    return documentList;
  }

  public InputStream getDocumentContents(String documentId) throws FileNotFoundException {
    File file = files.get(documentId);
    return new FileInputStream(file);
  }

  public Folder getRootFolder() {
    return rootFolder;
  }
  private void recursiveTraversal(File file, String parentId) {
    if (file.isDirectory()) {
      String folderId = addFolder(file, parentId, false);
      traverseChildren(file, folderId);
    } else if (file.isFile()) {
      addDocument(file, parentId);
    }
  }

  private void traverseChildren(File folder, String folderId) {
    File allFiles[] = folder.listFiles();
    for (File aFile : allFiles) {
      recursiveTraversal(aFile, folderId);
    }
  }

  private String addFolder(File file, String parentId, boolean isRoot) {
    String name = file.getName();
    String id = getId();
    String owner = "eric@sharepoint-connector.com";
    Folder folder = new Folder(name, id, parentId, getTestAcl(), owner, isRoot);
    folderList.add(folder);
    if (isRoot) {
      rootFolder = folder;
    }
    return id;
  }

  private void addDocument(File file, String parentId) {
    String name = file.getName();
    String id = getId();
    String mimeType = "text/plain";
    String owner = "eric@sharepoint-connector.com";
    Document document = new Document(name, id, parentId, getTestAcl(), owner, mimeType);
    documentList.add(document);
    files.put(id, file);
  }
  
  private List<Ace> getTestAcl() {
    List<Ace> acl = new ArrayList<Ace>();
    Set<String> read = new HashSet<String>();
    read.add("read");
    Set<String> write = new HashSet<String>();
    read.add("write");
    Set<String> none = new HashSet<String>();
    read.add("none");
    Ace.SharepointPermissions readPermissions = new Ace.SharepointPermissions(read, none);
    Ace.SharepointPermissions writePermissions = new Ace.SharepointPermissions(write, none);
    Ace.SharepointPermissions noPermissions = new Ace.SharepointPermissions(none, none);
    
    Ace ace1 = new Ace("eric@sharepoint-connector.com", writePermissions, Ace.Type.USER);
    ace1.setGPermission(Ace.GPermission.WRITE);
    
    Ace ace2 = new Ace("john@sharepoint-connector.com", readPermissions, Ace.Type.USER);
    ace2.setGPermission(Ace.GPermission.READ);
    
    Ace ace3 = new Ace("migration-team@sharepoint-connector.com", writePermissions, Ace.Type.DOMAINGROUP);
    ace3.setGPermission(Ace.GPermission.FULLCONTROL);

    acl.add(ace1);
    acl.add(ace2);
    acl.add(ace3);
    return acl;
  }

  private String getId() {
    String newId = String.valueOf(idCounter);
    idCounter++;
    return newId;
  }

}
