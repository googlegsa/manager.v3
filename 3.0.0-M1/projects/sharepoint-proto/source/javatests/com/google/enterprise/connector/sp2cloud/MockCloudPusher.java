// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;

import java.io.InputStream;

public class MockCloudPusher implements CloudPusher {
  @Override
  public void pushDocument(Document document, FolderInfo parent, String owner,
      AclAdjustments aclAdjustments, InputStream inputStream) throws Exception {
    System.out.println("Document - name: '" + document.getName()
        + "'  id: " + document.getId()
        + "  parent id: " + document.getParentId()
        + "  owner: " + owner);   
  }

  @Override
  public String pushFolder(String folderName, FolderInfo parent, String owner,
      AclAdjustments aclAdjustments) throws Exception {
    System.out.println("Folder - name: '" + folderName
        + "  parent id: " + parent.getId()
        + "  owner: " + owner);
    return "DUMMY_URL";
  }
}
