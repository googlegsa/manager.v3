//package com.google.enterprise.connector.sp2cloud;
//
//import com.google.enterprise.connector.sp2c_migration.Document;
//import com.google.enterprise.connector.sp2c_migration.Folder;
//
//import java.io.InputStream;
//
//public class MockCloudPusher implements CloudPusher {
//
//  @Override
//  public void pushDocument(Folder parent, Document document, InputStream inputStream)
//      throws Exception {
//    System.out.println("Document - name: '" + document.getName() + "'  id: " + document.getId() + "  parent id: " + document.getParentId() + "  owner: " + document.getOwner());
//  }
//
//  @Override
//  public void pushFolder(Folder parent, Folder folder) throws Exception {
//    System.out.println("Folder - name: '" + folder.getName() + "'  id: " + folder.getId() + "  parent id: " + folder.getParentId() + "  owner: " + folder.getOwner());
//  }
//
//}
