// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;

import java.io.InputStream;

public class MockCloudPusher implements CloudPusher {
  @Override
  public void pushDocument(Document document, CloudAcl cloudAcl, InputStream inputStream)
      throws Exception {
    System.out.println("Document - name: '" + document.getName()
        + "'  id: " + document.getId()
        + "  parent id: " + document.getParentId()
        + "  owner: " + cloudAcl.getOwner());
  }

  @Override
  public void pushFolder(FolderInfo folderInfo) throws Exception {
    String parentId = folderInfo.getParent() == null ?
        null : folderInfo.getParent().getId();
    System.out.println("Folder - name: '" + folderInfo.getName()
        + "'  id: " + folderInfo.getId()
        + "  parent id: " + parentId
        + "  owner: " + folderInfo.getCloudAcl().getOwner());
  }
}
