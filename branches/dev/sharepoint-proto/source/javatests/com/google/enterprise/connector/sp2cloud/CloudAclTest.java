// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope.Type;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@loin CloudAcl}.
 */
public class CloudAclTest extends TestCase {
  public final static String SAM = "sam";
  public void testGoodAcl() {
     List<CloudAce> goodAces = Arrays.asList(
         new CloudAce(SAM, Type.USER, AclRole.OWNER),
         new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
         new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
     CloudAcl cloudAcl = CloudAcl.newCloudAcl(goodAces);
     assertEquals(SAM, cloudAcl.getOwner());
     assertEquals(goodAces, cloudAcl.getAceList());
  }

  public void testMultipleOwners() {
    List<CloudAce> duplicateOwner = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("tom", Type.USER, AclRole.OWNER));
    try {
      CloudAcl.newCloudAcl(duplicateOwner);
      fail("Multiple owners not detected.");
    } catch (IllegalArgumentException iae) {
      assertTrue(iae.getMessage().startsWith("Only one owner supported"));
    }
  }

  public void testMissingOwner() {
    List<CloudAce> missingOwner = Arrays.asList(
        new CloudAce("bob", Type.USER, AclRole.READER),
        new CloudAce("jill", Type.USER, AclRole.WRITER));
    try {
      CloudAcl.newCloudAcl(missingOwner);
      fail("Missing owner not detected.");
    } catch (IllegalArgumentException iae) {
      assertTrue(iae.getMessage().startsWith("Acl must define an owner "));
    }
  }

  public void testGroupOwner() {
    List<CloudAce> missingOwner = Arrays.asList(
        new CloudAce("singers", Type.GROUP, AclRole.OWNER),
        new CloudAce("jill", Type.USER, AclRole.WRITER));
    try {
      CloudAcl.newCloudAcl(missingOwner);
      fail("Group owner not detected.");
    } catch (IllegalArgumentException iae) {
      assertTrue(iae.getMessage().startsWith("Owner must be a user "));
    }
  }

  public void testTypeAndNameKeyMatch() {
    List<CloudAce> missingOwner = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce(SAM, Type.USER, AclRole.WRITER));
    try {
      CloudAcl.newCloudAcl(missingOwner);
      fail("Group owner not detected.");
    } catch (IllegalArgumentException iae) {
      assertTrue(iae.getMessage().startsWith(
          "CloudAces must have unique {type, name} pairs - "));
    }
  }

  public void testNewTypeAndNameKeyToCloudAceMap() {
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    Map<CloudAce.TypeAndNameKey, CloudAce> typeAndNameKeyToCloudAceMap =
      cloudAcl.newTypeAndNameKeyToCloudAceMap();

    for (CloudAce cloudAce : cloudAces) {
      assertEquals(cloudAce,
          typeAndNameKeyToCloudAceMap.get(cloudAce.newTypeAndNameKey()));
    }
    assertEquals(cloudAces.size(), typeAndNameKeyToCloudAceMap.size());
  }

  private static String ADMIN_USER_ID = "AdMiN";
  public void testGetParentAclAdjustments_exactMatchAdminOwner() {
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(cloudAcl, ADMIN_USER_ID);
    assertTrue(aclAdjustments.getInserts().isEmpty());
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_exactMatchNonAdminOwner() {
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(cloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertEquals(new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER),
        aclAdjustments.getInserts().get(0));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_exactMatchAdminReader() {
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce(ADMIN_USER_ID, Type.GROUP, AclRole.READER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(cloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertEquals(new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER),
        aclAdjustments.getInserts().get(0));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_exactMatchAdminWriter() {
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce(ADMIN_USER_ID, Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(cloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertEquals(new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER),
        aclAdjustments.getInserts().get(0));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_nullParent() {
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(null, ADMIN_USER_ID);
    assertEquals(3, aclAdjustments.getInserts().size());
    for (CloudAce expect : cloudAces) {
      if (!expect.getRole().equals(AclRole.OWNER)) {
        assertTrue(aclAdjustments.getInserts().contains(expect));
      }
    }
    assertTrue(aclAdjustments.getInserts().contains(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER)));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_deleteReader() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertTrue(aclAdjustments.getInserts().isEmpty());
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertEquals(1, aclAdjustments.getDeletes().size());
    assertEquals(parentCloudAces.get(2), aclAdjustments.getDeletes().get(0));
  }

  public void testGetParentAclAdjustments_deleteWriter() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertTrue(aclAdjustments.getInserts().isEmpty());
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertEquals(1, aclAdjustments.getDeletes().size());
    assertEquals(parentCloudAces.get(1), aclAdjustments.getDeletes().get(0));
  }

  public void testGetParentAclAdjustments_updateReader() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.WRITER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertTrue(aclAdjustments.getInserts().isEmpty());
    assertEquals(1, aclAdjustments.getUpdates().size());
    assertEquals(cloudAces.get(2), aclAdjustments.getUpdates().get(0));
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_updateWriter() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.READER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertTrue(aclAdjustments.getInserts().isEmpty());
    assertEquals(1, aclAdjustments.getUpdates().size());
    assertEquals(cloudAces.get(1), aclAdjustments.getUpdates().get(0));
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_insertReader() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertEquals(cloudAces.get(2), aclAdjustments.getInserts().get(0));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_insertWriter() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertEquals(cloudAces.get(1), aclAdjustments.getInserts().get(0));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_childOwnerMatchesParentWriter() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce(SAM, Type.USER, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertTrue(aclAdjustments.getInserts().contains(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER)));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_childOwnerMatchesParentReader() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce(SAM, Type.USER, AclRole.READER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertTrue(aclAdjustments.getInserts().contains(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER)));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustmants_childOwnerNotInParent() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertTrue(aclAdjustments.getInserts().contains(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER)));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_parentOwnerMatchesWriter() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce("bill", Type.USER, AclRole.OWNER),
        new CloudAce(SAM, Type.USER, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertTrue(aclAdjustments.getInserts().contains(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER)));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_parentOwnerMatchesReader() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce("bill", Type.USER, AclRole.OWNER),
        new CloudAce(SAM, Type.USER, AclRole.READER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertTrue(aclAdjustments.getInserts().contains(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER)));
    assertEquals(1, aclAdjustments.getUpdates().size());
    assertTrue(aclAdjustments.getUpdates().contains(
        new CloudAce(SAM, Type.USER, AclRole.READER)));
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }

  public void testGetParentAclAdjustments_parentOwnerMissing() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(SAM, Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce("bill", Type.USER, AclRole.OWNER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertTrue(aclAdjustments.getInserts().contains(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.WRITER)));
   assertTrue(aclAdjustments.getUpdates().isEmpty());
   assertEquals(1, aclAdjustments.getDeletes().size());
   assertTrue(aclAdjustments.getDeletes().contains(
       new CloudAce(SAM, Type.USER, AclRole.WRITER)));
  }

  public void testGetParentAclAdjustments_typeDifferNameMatch() {
    List<CloudAce> parentCloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl parentCloudAcl = CloudAcl.newCloudAcl(parentCloudAces);
    List<CloudAce> cloudAces = Arrays.asList(
        new CloudAce(ADMIN_USER_ID, Type.USER, AclRole.OWNER),
        new CloudAce("sinners", Type.USER, AclRole.WRITER),
        new CloudAce("sinners", Type.GROUP, AclRole.WRITER),
        new CloudAce("samantha.com", Type.DOMAIN, AclRole.READER));
    CloudAcl cloudAcl = CloudAcl.newCloudAcl(cloudAces);
    AclAdjustments aclAdjustments =
        cloudAcl.getAclAdjustments(parentCloudAcl, ADMIN_USER_ID);
    assertEquals(1, aclAdjustments.getInserts().size());
    assertEquals(cloudAces.get(1), aclAdjustments.getInserts().get(0));
    assertTrue(aclAdjustments.getUpdates().isEmpty());
    assertTrue(aclAdjustments.getDeletes().isEmpty());
  }
}
