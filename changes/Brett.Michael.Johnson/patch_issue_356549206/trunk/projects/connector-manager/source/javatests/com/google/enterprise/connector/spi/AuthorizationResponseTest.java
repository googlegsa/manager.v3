package com.google.enterprise.connector.spi;

import junit.framework.Assert;
import junit.framework.TestCase;

public class AuthorizationResponseTest extends TestCase {

  public void testHashCodeAndEquals() {
    AuthorizationResponse ar1 = new AuthorizationResponse(true, "docid1");
    AuthorizationResponse ar2 = new AuthorizationResponse(true, "docid1");
    AuthorizationResponse ar3 = new AuthorizationResponse(false, "docid1");
    AuthorizationResponse ar4 = new AuthorizationResponse(true, "docid2");
    Assert.assertTrue(ar1.equals(ar1));
    Assert.assertTrue(ar1.equals(ar2));
    Assert.assertTrue(ar2.equals(ar1));
    Assert.assertTrue(ar1.equals(ar3));
    Assert.assertFalse(ar1.equals(ar4));
    Assert.assertTrue(ar1.hashCode() == ar2.hashCode());
    Assert.assertTrue(ar1.hashCode() == ar3.hashCode());
    Assert.assertFalse(ar1.hashCode() == ar4.hashCode());
    Assert.assertFalse(ar1.equals("docid1"));
    // TBD change the case below to assertFalse (and then make it work!)
    Assert.assertTrue(ar1.hashCode() == "docid1".hashCode());
  }

}
