package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope.Type;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class FolderManagerTest extends TestCase {
  FolderManager folderManager;
  @Override
  public void setUp() {
    folderManager = new FolderManager();
  }
  public final static String SAM = "sam";
  public final static String BOB = "bob";
  public void testAdd_root() {
    List<FolderInfo> rootList = folderManager.getRootFolders();
    assertEquals(0, rootList.size());
    List<CloudAce> root1Aces = Arrays.asList(new CloudAce(SAM, Type.USER,
        AclRole.OWNER));
    FolderInfo root1 = folderManager.newFolderInfo("id1", null,
        CloudAcl.newCloudAcl(root1Aces));
    assertTrue(root1.isRoot());
    assertNull(root1.getParent());
    folderManager.add(root1);
    rootList = folderManager.getRootFolders();
    assertEquals(1, rootList.size());
    assertTrue(rootList.contains(root1));

    List<CloudAce> root2Aces = Arrays.asList(new CloudAce(BOB, Type.USER,
        AclRole.OWNER));
    FolderInfo root2 = folderManager.newFolderInfo("id2",
        null, CloudAcl.newCloudAcl(root2Aces));
    folderManager.add(root2);
    rootList = folderManager.getRootFolders();
    assertEquals(2, rootList.size());
    assertTrue(rootList.contains(root1));
    assertTrue(rootList.contains(root2));

  }

  public void testAdd_childThenRoot() {
    List<CloudAce> root1Aces = Arrays.asList(new CloudAce(SAM, Type.USER,
        AclRole.OWNER));
    FolderInfo root1 = folderManager.newFolderInfo("root1", null,
        CloudAcl.newCloudAcl(root1Aces));
    assertTrue(root1.isRoot());
    assertNull(root1.getParent());

    List<CloudAce> childAces = Arrays.asList(new CloudAce(BOB, Type.USER,
        AclRole.OWNER));
    FolderInfo child1 = folderManager.newFolderInfo("child1",
        root1.getId(), CloudAcl.newCloudAcl(childAces));

    FolderInfo child2 = folderManager.newFolderInfo("child2",
        root1.getId(), CloudAcl.newCloudAcl(childAces));

    List<FolderInfo> rootList = folderManager.getRootFolders();
    assertEquals(0, rootList.size());
    assertNull(folderManager.getFolderInfo(child1.getId()));
    folderManager.add(child1);
    rootList = folderManager.getRootFolders();
    assertEquals(0, rootList.size());
    assertEquals(child1, folderManager.getFolderInfo(child1.getId()));
    assertNull(folderManager.getFolderInfo(child2.getId()));
    folderManager.add(child2);
    rootList = folderManager.getRootFolders();
    assertEquals(0, rootList.size());
    assertEquals(child1, folderManager.getFolderInfo(child1.getId()));
    assertEquals(child2, folderManager.getFolderInfo(child2.getId()));

    assertNull(folderManager.getFolderInfo(root1.getId()));
    assertEquals(0, root1.getChildFolders().size());
    folderManager.add(root1);
    rootList = folderManager.getRootFolders();
    assertEquals(1, rootList.size());
    assertEquals(root1,rootList.get(0));
    assertEquals(child1, folderManager.getFolderInfo(child1.getId()));
    assertEquals(child2, folderManager.getFolderInfo(child2.getId()));
    assertEquals(root1, folderManager.getFolderInfo(root1.getId()));
    assertEquals(2, root1.getChildFolders().size());
    assertTrue(root1.getChildFolders().contains(child1));
    assertTrue(root1.getChildFolders().contains(child2));
  }

  public void testAdd_RootThenChild() {

  }

  public void testAdd_grandchild() {


  }

  public void testAdd_repeatId() {

  }
}
