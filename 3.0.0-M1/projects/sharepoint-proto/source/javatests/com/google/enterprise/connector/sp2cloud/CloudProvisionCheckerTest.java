// Copyright 2010 Google Inc. All Rights Reserved.
package com.google.enterprise.connector.sp2cloud;

import com.google.gdata.client.appsforyourdomain.AppsGroupsService;
import com.google.gdata.client.appsforyourdomain.UserService;

import junit.framework.TestCase;
public class CloudProvisionCheckerTest extends TestCase {
  private final static String ADMIN_EMAIL = "admin@sharepoint-connector.com";
  private final static String ADMIN_PASSWORD = "testing";
  private final static String DOMAIN_NAME = "sharepoint-connector.com";
  private final static String APPLICATION_NAME = "unit-test";
  private final int CACHE_SIZE = 3;
  private ProvisionedStateLRUCache userStates;
  private ProvisionedStateLRUCache groupStates;

  @Override
  protected void setUp() {
    userStates = new ProvisionedStateLRUCache(CACHE_SIZE);
    groupStates = new ProvisionedStateLRUCache(CACHE_SIZE);
  }

  public void testNewUserServiceService() throws Exception {
    UserService userService = CloudProvisionChecker.newUserService(
        ADMIN_EMAIL, ADMIN_PASSWORD, DOMAIN_NAME, APPLICATION_NAME);
    assertNotNull(userService);
  }

  public void testNewGroupService() throws Exception {
    AppsGroupsService groupsService = CloudProvisionChecker.newGroupsService(
        ADMIN_EMAIL, ADMIN_PASSWORD, DOMAIN_NAME, APPLICATION_NAME);
    assertNotNull(groupsService);
  }

  public void testIsUserProvisioned() throws Exception {
    CloudProvisionChecker cpc = newCloudProvisionChecker();
    assertTrue(cpc.isUserProvisioned("admin"));
    assertTrue(cpc.isUserProvisioned("admin"));
    assertTrue(cpc.isUserProvisioned("ziff"));
    assertFalse(cpc.isUserProvisioned("palin"));
    assertEquals(1, userStates.getHits());
    assertEquals(3, userStates.getMisses());
    assertFalse(cpc.isUserProvisioned("palin"));
    assertEquals(2, userStates.getHits());
    assertEquals(3, userStates.getMisses());
    assertFalse(cpc.isUserProvisioned("bush"));
    assertEquals(2, userStates.getHits());
    assertEquals(4, userStates.getMisses());
    //Admin should be misssing from cache.
    assertTrue(cpc.isUserProvisioned("admin"));
    assertEquals(2, userStates.getHits());
    assertEquals(5, userStates.getMisses());
  }

  public void testIsGrouprProvisioned() throws Exception {
    CloudProvisionChecker cpc = newCloudProvisionChecker();
    assertTrue(cpc.isGroupProvisioned("Engineering"));
    assertEquals(0, groupStates.getHits());
    assertEquals(1, groupStates.getMisses());
    assertTrue(cpc.isGroupProvisioned("Engineering"));
    assertEquals(1, groupStates.getHits());
    assertEquals(1, groupStates.getMisses());
    assertTrue(cpc.isGroupProvisioned("Guests"));
    assertEquals(1, groupStates.getHits());
    assertEquals(2, groupStates.getMisses());
    assertFalse(cpc.isGroupProvisioned("clowns"));
    assertEquals(1, groupStates.getHits());
    assertEquals(3, groupStates.getMisses());
    assertFalse(cpc.isGroupProvisioned("funBoys"));
    assertEquals(1, groupStates.getHits());
    assertEquals(4, groupStates.getMisses());
    //Should replace Engineering
    assertTrue(cpc.isGroupProvisioned("Engineering"));
    assertEquals(1, groupStates.getHits());
    assertEquals(5, groupStates.getMisses());
  }

  private CloudProvisionChecker newCloudProvisionChecker() throws Exception{
    UserService userService = CloudProvisionChecker.newUserService(
        ADMIN_EMAIL, ADMIN_PASSWORD,DOMAIN_NAME,  APPLICATION_NAME);
    AppsGroupsService groupsService = CloudProvisionChecker.newGroupsService(
        ADMIN_EMAIL, ADMIN_PASSWORD, DOMAIN_NAME, APPLICATION_NAME);
    return new CloudProvisionChecker(userService,
        groupsService, DOMAIN_NAME, userStates, groupStates);
  }
}
