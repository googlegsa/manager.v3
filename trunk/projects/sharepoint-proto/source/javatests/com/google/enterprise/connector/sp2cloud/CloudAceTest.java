package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2cloud.CloudAce.TypeAndNameKey;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope.Type;

import junit.framework.TestCase;

/**
 * Tests for {@link CloudAce}
 */
public class CloudAceTest extends TestCase {
  public void testTypeAndNameKey_identity() {
    CloudAce cloudAce1 = new CloudAce("group1", Type.GROUP, AclRole.PEEKER);
    TypeAndNameKey k1 = cloudAce1.newTypeAndNameKey();
    assertEquals(k1, k1);
  }

  public void testTypeAndNameKey_clones() {
    CloudAce cloudAce1 = new CloudAce("user1", Type.USER, AclRole.READER);
    CloudAce cloudAce2 = new CloudAce(cloudAce1.getName(),
        cloudAce1.getType(), cloudAce1.getRole());
    assertEquals(cloudAce1.newTypeAndNameKey(), cloudAce2.newTypeAndNameKey());
  }

  public void testTypeAndNameKey_typeMismatch() {
    CloudAce cloudAce1 = new CloudAce("user1", Type.USER, AclRole.READER);
    CloudAce cloudAce2 = new CloudAce(cloudAce1.getName(),
        Type.GROUP, cloudAce1.getRole());
    assertFalse(cloudAce1.newTypeAndNameKey().equals(
        cloudAce2.newTypeAndNameKey()));
  }

  public void testTypeAndNameKey_nameMismatch() {
    CloudAce cloudAce1 = new CloudAce("user1", Type.USER, AclRole.READER);
    CloudAce cloudAce2 = new CloudAce(cloudAce1.getName() + "_not",
        cloudAce1.getType(), cloudAce1.getRole());
    assertFalse(cloudAce1.newTypeAndNameKey().equals(
        cloudAce2.newTypeAndNameKey()));
  }

  public void testTypeAndNameKey_rollMismatch() {
    CloudAce cloudAce1 = new CloudAce("user1", Type.USER, AclRole.READER);
    CloudAce cloudAce2 = new CloudAce(cloudAce1.getName(),
        cloudAce1.getType(), AclRole.OWNER);
    assertEquals(cloudAce1.newTypeAndNameKey(), cloudAce2.newTypeAndNameKey());
  }
}
