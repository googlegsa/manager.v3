// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Document;
import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DoclistPusherTest extends TestCase {
  private static final String ADMIN_ID = "admin@sharepoint-connector.com";
  private static final String TUSER1_ID = "strellis@sharepoint-connector.com";
  private static final String TUSER2_ID = "johnfelton@sharepoint-connector.com";
  private static final String TUSER3_ID = "ziff@sharepoint-connector.com";
  private static final String TGROUP1_ID = "engineering@sharepoint-connector.com";
  private static final String CONSUMER_KEY = "sharepoint-connector.com";
  private static final String CONSUMER_SECRET = "A6dY3I1uW89q999msVYEX07l";

  private Random generator;
  private DoclistPusher pusher;
  private String rootFolderId;
  private FolderManager folderManager;
  private DocsService client;

  @Override
  public void setUp() throws Exception {
    client = DoclistPusher.mkOauthClient(CONSUMER_KEY, CONSUMER_SECRET);
    generator = new Random();
    rootFolderId = "root_" + generator.nextInt(Integer.MAX_VALUE);
    folderManager = new FolderManager();
    pusher = new DoclistPusher(client, folderManager, ADMIN_ID, false);
  }
  
  public void pushFolder(FolderInfo folder, String owner) throws Exception {
    String cloudBaseUrl =  pusher.pushFolder(folder.getName(), folder.getParent(), owner, 
        folder.getCloudAcl().getAclAdjustments(folder.getParentCloudAcl(), ADMIN_ID));
    folder.setBaseUrl(cloudBaseUrl);
  
  }

  public void testFolders() throws Exception {
    List<CloudAce> rootCloudAceList = Arrays.asList(
        new CloudAce(ADMIN_ID, AclScope.Type.USER, AclRole.OWNER),
        new CloudAce(TUSER1_ID, AclScope.Type.USER, AclRole.READER),
        new CloudAce(TUSER3_ID, AclScope.Type.USER, AclRole.READER));
    FolderInfo root =
        mkAndRegisterFolderInfo(rootFolderId, null, rootCloudAceList);
    pushFolder(root, ADMIN_ID);

    //Add child1 to root as TUSER3_ID
    List<CloudAce> child1CloudAceList = Arrays.asList(
        new CloudAce(TGROUP1_ID,AclScope.Type.USER, AclRole.WRITER),
        new CloudAce(TUSER2_ID, AclScope.Type.USER, AclRole.READER),
        new CloudAce(TUSER3_ID, AclScope.Type.USER, AclRole.OWNER));
    FolderInfo child1 = mkAndRegisterFolderInfo("child1", root.getId(),
        child1CloudAceList);
    pushFolder(child1, TUSER3_ID);

    //Add child2 to root as ADMIN_ID with same acl as root
    FolderInfo child2 = mkAndRegisterFolderInfo("child2", root.getId(),
        rootCloudAceList);
    pushFolder(child2, ADMIN_ID);
 
    //Add a grandchild to child1 as TUSER3_ID
    // Updates ace from reader->writer and writer->reader
    List<CloudAce> grandchild1CloudAceList = Arrays.asList(
        new CloudAce(TGROUP1_ID,AclScope.Type.USER, AclRole.READER),
        new CloudAce(TUSER2_ID, AclScope.Type.USER, AclRole.WRITER),
        new CloudAce(TUSER3_ID, AclScope.Type.USER, AclRole.OWNER));
    FolderInfo grandchild1 = mkAndRegisterFolderInfo("grandchild1",
        child1.getId(), grandchild1CloudAceList);
    pushFolder(grandchild1, TUSER3_ID);
  }

  public void testDocuments() throws Exception {
    List<CloudAce> rootCloudAceList = Arrays.asList(
        new CloudAce(ADMIN_ID, AclScope.Type.USER, AclRole.OWNER),
        new CloudAce(TUSER1_ID, AclScope.Type.USER, AclRole.READER));
    FolderInfo root =
      mkAndRegisterFolderInfo(rootFolderId, null, rootCloudAceList);
    pushFolder(root, ADMIN_ID);

    List<CloudAce> doc1CloudAceList = Arrays.asList(
        new CloudAce(TUSER3_ID, AclScope.Type.USER, AclRole.OWNER),
        new CloudAce(TUSER2_ID, AclScope.Type.USER, AclRole.WRITER));
    CloudAcl doc1CloudAcl = CloudAcl.newCloudAcl(doc1CloudAceList);
    Document document = new Document("d1_"
        + generator.nextInt(Integer.MAX_VALUE), "d1", root.getId(), null, TUSER3_ID,
        "text/plain", "not-used");
    pusher.pushDocument(document, folderManager.getFolderInfo(document.getParentId()), 
        doc1CloudAcl.getOwner(),
        doc1CloudAcl.getAclAdjustments(root.getCloudAcl(), ADMIN_ID),
        new ByteArrayInputStream("Hi Eric\n".getBytes("US-ASCII")));

    List<CloudAce> doc2CloudAceList = Arrays.asList(
        new CloudAce(ADMIN_ID, AclScope.Type.USER, AclRole.OWNER),
        new CloudAce(TUSER2_ID, AclScope.Type.USER, AclRole.WRITER));
    CloudAcl doc2CloudAcl = CloudAcl.newCloudAcl(doc2CloudAceList);
    document = new Document("d2", "d2_id", root.getId(), null, ADMIN_ID,
        "text/plain", "not-used");
    pusher.pushDocument(document, folderManager.getFolderInfo(document.getParentId()), 
        doc2CloudAcl.getOwner(),
        doc1CloudAcl.getAclAdjustments(root.getCloudAcl(), ADMIN_ID),
        new ByteArrayInputStream("Hi Eric2\n".getBytes("US-ASCII")));
  }

  public void printAclEntry(AclEntry entry) {
    System.out.println(" -- " + entry.getScope().getValue() + ": "
        + entry.getRole().getValue());
  }

  private FolderInfo mkAndRegisterFolderInfo(
      String folderId, String parentId, List<CloudAce> aceList) {
    FolderInfo folderInfo = folderManager.newFolderInfo(folderId, parentId,
        "n_" + folderId, CloudAcl.newCloudAcl(aceList), new ArrayList<Ace>());
    folderManager.add(folderInfo);
    return folderInfo;
  }
}
