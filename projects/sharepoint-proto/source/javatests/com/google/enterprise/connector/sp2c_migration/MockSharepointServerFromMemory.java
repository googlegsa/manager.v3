// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2c_migration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.ByteArrayInputStream;

public class MockSharepointServerFromMemory implements SharepointSite {
  private List<Document> documents = new ArrayList<Document>();
  private List<Folder> folders = new ArrayList<Folder>();
  private List<Folder> rootFolders = new ArrayList<Folder>();
  private int idCounter = 1;

  public MockSharepointServerFromMemory(String sharePointUrl, String loginName, String password) {
    super();

    String[] read = new String[1];
    read[0] = "read";
    String[] write = new String[1];
    write[0] = "write";
    String[] none = new String[1];
    none[0] = "none";
    Ace.SharepointPermissions readPermissions = new Ace.SharepointPermissions(read, none);
    Ace.SharepointPermissions writePermissions = new Ace.SharepointPermissions(write, none);

    Ace engineeringAce = new Ace("engineering@sharepoint-connector.com", writePermissions, Ace.Type.USER);
    engineeringAce.setGPermission(Ace.GPermission.WRITE);

    Ace productManagementAce = new Ace("product-management@sharepoint-connector.com", writePermissions, Ace.Type.USER);
    productManagementAce.setGPermission(Ace.GPermission.WRITE);

    Ace contractorsAce = new Ace("contractors@sharepoint-connector.com", writePermissions, Ace.Type.DOMAINGROUP);
    contractorsAce.setGPermission(Ace.GPermission.FULLCONTROL);

    List<Ace> engineeringAcl = new ArrayList<Ace>();
    engineeringAcl.add(engineeringAce);

    String documentLibraryFolderId = addFolder("Document Library", "NONE", true);
    String engineeringFolderId = addFolder("Engineering", documentLibraryFolderId, false);
    String programManagersFolderId = addFolder("Product Management", documentLibraryFolderId, false);
    String contractorsFolderId = addFolder("Contractors", documentLibraryFolderId, false);

    addDocument("Engineering File.txt", "file/text", engineeringFolderId);
    addDocument("Product Management File.txt", "file/text", programManagersFolderId);
    addDocument("Contractors File.txt", "file/text", contractorsFolderId);
  }

  /* Override */
  public InputStream getDocumentContent(Document document) throws Exception {
    final String documentContent = "This is the file contents.";
    InputStream inputStream = new ByteArrayInputStream(documentContent.getBytes());
    return inputStream;
  }

  /* Override */
  public List<Document> getDocuments(Folder rootfolder) throws Exception {
    return documents;
  }

  /* Override */
  public List<Folder> getFolders(Folder rootfolder) throws Exception {
    return folders;
  }

  /* Override */
  public String getId() {
    return "Site ID";
  }

  /* Override */
  public List<Folder> getRootFolders() throws Exception {
    return rootFolders;
  }

  /* Override */
  public String getUrl() {
    return null;
  }

  private String addFolder(String name, String parentId, boolean isRoot) {
    String id = getNewId();
    String owner = "eric@sharepoint-connector.com";
    Folder folder = new Folder(name, id, "URL", parentId, getTestAcl(), owner, isRoot);
    folders.add(folder);
    if (isRoot) {
      rootFolders.add(folder);
    }
    return id;
  }

  private void addDocument(String name, String mimeType, String parentId) {
    String id = getNewId();
    String owner = "strellis@sharepoint-connector.com";
    Document document = new Document(name, id, parentId, getTestAcl(), owner, mimeType,
        "dummy_document_url");
    documents.add(document);
  }

  private List<Ace> getTestAcl() {
    List<Ace> acl = new ArrayList<Ace>();

//    Ace ace1 = new Ace("strellis@sharepoint-connector.com", writePermissions, Ace.Type.USER);
//    ace1.setGPermission(Ace.GPermission.WRITE);
//
//    Ace ace2 = new Ace("johnfelton@sharepoint-connector.com", readPermissions, Ace.Type.USER);
//    ace2.setGPermission(Ace.GPermission.READ);
//
//    Ace ace3 = new Ace("engineering@sharepoint-connector.com", writePermissions, Ace.Type.DOMAINGROUP);
//    ace3.setGPermission(Ace.GPermission.FULLCONTROL);
//
//    acl.add(ace1);
//    acl.add(ace2);
//    acl.add(ace3);
    return acl;
  }

  private String getNewId() {
    String newId = String.valueOf(idCounter);
    idCounter++;
    return newId;
  }
}
