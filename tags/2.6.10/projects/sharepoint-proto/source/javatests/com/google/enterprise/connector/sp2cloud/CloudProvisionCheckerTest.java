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
    assertTrue(cpc.isUserProvisioned("ziff"));
    assertFalse(cpc.isUserProvisioned("palin"));
  }
  
  public void testIsGrouprProvisioned() throws Exception {
    CloudProvisionChecker cpc = newCloudProvisionChecker();
    assertTrue(cpc.isGroupProvisioned("Engineering"));
    assertTrue(cpc.isGroupProvisioned("Guests"));
    assertFalse(cpc.isGroupProvisioned("clowns"));
  }
  
  private CloudProvisionChecker newCloudProvisionChecker() throws Exception{
    UserService userService = CloudProvisionChecker.newUserService(
        ADMIN_EMAIL, ADMIN_PASSWORD,DOMAIN_NAME,  APPLICATION_NAME);
    AppsGroupsService groupsService = CloudProvisionChecker.newGroupsService(
        ADMIN_EMAIL, ADMIN_PASSWORD, DOMAIN_NAME, APPLICATION_NAME);
    return new CloudProvisionChecker(userService, 
        groupsService, DOMAIN_NAME);
  }
}
