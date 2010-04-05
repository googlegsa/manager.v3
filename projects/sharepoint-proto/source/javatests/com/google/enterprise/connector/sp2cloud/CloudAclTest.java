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
}
