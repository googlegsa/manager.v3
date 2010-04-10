// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2c_migration;



import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MockSharepointServerUsingFileSystem implements SharepointSite {
  
  private FolderTraverser traverser;
  
  public MockSharepointServerUsingFileSystem(String sharePointUrl, String loginName, String password) {
    traverser = new FolderTraverser(sharePointUrl);
  }

  @Override
  public InputStream getDocumentContent(Document document) throws Exception {
    return traverser.getDocumentContents(document.getId());
  }

  @Override
  public List<Document> getDocuments(Folder rootfolder) throws Exception {
    return traverser.getDocuments();
  }

  @Override
  public List<Folder> getFolders(Folder rootfolder) throws Exception {
    return traverser.getFolders();
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public List<Folder> getRootFolders() throws Exception {
    List<Folder> roots = new ArrayList<Folder>();
    roots.add(traverser.getRootFolder());
    return roots;
  }

  @Override
  public String getUrl() {
    return "Site URL";
  }

}
