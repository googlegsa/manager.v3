package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2cloud.FolderManager.FolderInfo;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope.Type;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link FolderManager}.
 */
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
    assertEquals(root1, rootList.get(0));
    assertEquals(child1, folderManager.getFolderInfo(child1.getId()));
    assertEquals(child2, folderManager.getFolderInfo(child2.getId()));
    assertEquals(root1, folderManager.getFolderInfo(root1.getId()));
    assertEquals(2, root1.getChildFolders().size());
    assertTrue(root1.getChildFolders().contains(child1));
    assertTrue(root1.getChildFolders().contains(child2));
  }

  public void testAdd_rootThenChild() {
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

    assertNull(folderManager.getFolderInfo(root1.getId()));
    assertEquals(0, root1.getChildFolders().size());
    folderManager.add(root1);
    List<FolderInfo>rootList = folderManager.getRootFolders();
    assertEquals(1, rootList.size());
    assertEquals(root1, rootList.get(0));

    assertNull(folderManager.getFolderInfo(child1.getId()));
    folderManager.add(child1);
    rootList = folderManager.getRootFolders();
    assertEquals(1, rootList.size());
    assertEquals(root1, rootList.get(0));
    assertEquals(child1, folderManager.getFolderInfo(child1.getId()));
    assertNull(folderManager.getFolderInfo(child2.getId()));
    assertEquals(1, root1.getChildFolders().size());
    assertTrue(root1.getChildFolders().contains(child1));
    assertFalse(root1.getChildFolders().contains(child2));

    folderManager.add(child2);
    assertEquals(1, rootList.size());
    assertEquals(root1, rootList.get(0));
    assertEquals(child1, folderManager.getFolderInfo(child1.getId()));
    assertEquals(child2, folderManager.getFolderInfo(child2.getId()));
    assertEquals(root1, folderManager.getFolderInfo(root1.getId()));
    assertEquals(2, root1.getChildFolders().size());
    assertTrue(root1.getChildFolders().contains(child1));
    assertTrue(root1.getChildFolders().contains(child2));
  }

  public void testAdd_grandchild() {
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

    FolderInfo grandchild1 = folderManager.newFolderInfo("grandchild1",
        child1.getId(), CloudAcl.newCloudAcl(childAces));

    folderManager.add(root1);
    assertEquals(1, folderManager.getRootFolders().size());
    assertEquals(root1, folderManager.getRootFolders().get(0));
    assertNull(folderManager.getFolderInfo(child1.getId()));
    assertNull(folderManager.getFolderInfo(grandchild1.getId()));
    assertNull(root1.getParent());
    assertEquals(0,
        folderManager.getFolderInfo(root1.getId()).getChildFolders().size());

    folderManager.add(grandchild1);
    assertEquals(1, folderManager.getRootFolders().size());
    assertEquals(root1, folderManager.getRootFolders().get(0));
    assertNull(folderManager.getFolderInfo(child1.getId()));
    assertEquals(grandchild1,
        folderManager.getFolderInfo(grandchild1.getId()));
    assertNull(root1.getParent());
    assertNull(grandchild1.getParent());
    assertEquals(0,
        folderManager.getFolderInfo(root1.getId()).getChildFolders().size());
    assertEquals(0, folderManager.getFolderInfo(grandchild1.getId())
        .getChildFolders().size());

    folderManager.add(child1);
    assertEquals(1, folderManager.getRootFolders().size());
    assertEquals(root1, folderManager.getRootFolders().get(0));
    assertEquals(child1, folderManager.getFolderInfo(child1.getId()));
    assertEquals(grandchild1,
        folderManager.getFolderInfo(grandchild1.getId()));
    assertNull(root1.getParent());
    assertEquals(root1,
        folderManager.getFolderInfo(child1.getId()).getParent());
    assertEquals(child1,
        folderManager.getFolderInfo(grandchild1.getId()).getParent());
    assertEquals(1,
        folderManager.getFolderInfo(root1.getId()).getChildFolders().size());
    assertEquals(child1,
        folderManager.getFolderInfo(root1.getId()).getChildFolders().get(0));
    assertEquals(1, folderManager.getFolderInfo(child1.getId())
        .getChildFolders().size());
    assertEquals(grandchild1,
        folderManager.getFolderInfo(child1.getId()).getChildFolders().get(0));
    assertEquals(0, folderManager.getFolderInfo(grandchild1.getId())
        .getChildFolders().size());
  }

  public void testAdd_repeatId() {
    List<CloudAce> root1Aces = Arrays.asList(new CloudAce(SAM, Type.USER,
        AclRole.OWNER));
    FolderInfo root1 = folderManager.newFolderInfo("root1", null,
        CloudAcl.newCloudAcl(root1Aces));
    FolderInfo folderInfoWithRoot1Id = folderManager.newFolderInfo("root1",
        null, CloudAcl.newCloudAcl(root1Aces));
    folderManager.add(root1);
    try {
      folderManager.add(folderInfoWithRoot1Id);
      fail("Add should have thrown an IllegalStateException.");
    } catch (IllegalStateException ise) {
      assertTrue(ise.getMessage().startsWith(
          "Can't add an already added folder - "));
    }
  }
}
