// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionsMapper {
  private static String[] REQUIRED_FOR_OWN_BASIC = {"ViewListItems", "EditListItems", "DeleteListItems",};
  private static String[] REQUIRED_FOR_WRITE_BASIC = {"ViewListItems", "EditListItems"};
  private static String[] REQUIRED_FOR_READ_BASIC = {"ViewListItems"};

  private static String[] REQUIRED_FOR_OWN_STRICT = {"ViewListItems", "ViewVersions", "ViewApplicationPages", "ViewPages", "Open", "EditListItems", "DeleteListItems", "AddListItems", "DeleteVersions", "DeleteListItems", "ManagePermissions"};
  private static String[] REQUIRED_FOR_WRITE_STRICT = {"ViewListItems", "ViewVersions", "ViewApplicationPages", "ViewPages", "Open", "EditListItems", "DeleteListItems", "AddListItems", "DeleteVersions"};
  private static String[] REQUIRED_FOR_READ_STRICT = {"ViewListItems", "ViewVersions", "ViewApplicationPages", "ViewPages", "Open"};

  private static String[] REQUIRED_FOR_OWN;
  private static String[] REQUIRED_FOR_WRITE;
  private static String[] REQUIRED_FOR_READ;

  private final Map<String, String> userAndGroupNameMap;
  private final String defaultOwner;
  
  public PermissionsMapper(Map<String, String> userAndGroupNameMap, String defaultOwner, boolean strict) {
    this.userAndGroupNameMap = userAndGroupNameMap;
    this.defaultOwner = defaultOwner;
    if (strict) {
      REQUIRED_FOR_OWN = REQUIRED_FOR_OWN_STRICT;
      REQUIRED_FOR_WRITE = REQUIRED_FOR_WRITE_STRICT;
      REQUIRED_FOR_READ = REQUIRED_FOR_READ_STRICT;
    } else {
      REQUIRED_FOR_OWN = REQUIRED_FOR_OWN_BASIC;
      REQUIRED_FOR_WRITE = REQUIRED_FOR_WRITE_BASIC;
      REQUIRED_FOR_READ = REQUIRED_FOR_READ_BASIC;
    }
  }
  
  public CloudAcl mapAcl(List<Ace> sharePointAcl, String sharePointOwnerName) {
    boolean ownerAssigned = false;
    List<CloudAce> result = new ArrayList<CloudAce>();
    String cloudOwnerName = userAndGroupNameMap.get(sharePointOwnerName);
    
    for (Ace ace : sharePointAcl) {
      // Look up name
      String cloudName = userAndGroupNameMap.get(ace.getName());
      if (cloudName == null) {
        continue;
      }
      
      AclRole cloudRole;
      if (cloudName.equals(cloudOwnerName) && (ace.getType() == Ace.Type.USER)
          && hasPermission(ace.getPermission().getAllowedPermissions(), REQUIRED_FOR_OWN)) {
        cloudRole = AclRole.OWNER;
        ownerAssigned = true;
      } else if (hasPermission(ace.getPermission().getAllowedPermissions(), REQUIRED_FOR_WRITE)) {
        cloudRole = AclRole.WRITER;
      } else if (hasPermission(ace.getPermission().getAllowedPermissions(), REQUIRED_FOR_READ)) {
        cloudRole = AclRole.READER;
      } else {
        continue;
      }
      
      AclScope.Type cloudType;
      if (ace.getType().equals(Ace.Type.USER)) {
        cloudType = AclScope.Type.USER;
      } else {
        cloudType = AclScope.Type.GROUP;
      }
      
      CloudAce cloudAce = new CloudAce(cloudName, cloudType, cloudRole);
      result.add(cloudAce);
    }
    
    // If no owner was assigned then assign the default owner.
    if (!ownerAssigned) {
      CloudAce cloudAce = new CloudAce(defaultOwner, AclScope.Type.USER, AclRole.OWNER);
      result.add(cloudAce);      
    }

    return CloudAcl.newCloudAcl(result);
  }
    
  public String mapPrincipleName(String name, String defaultResult) {
    String result = userAndGroupNameMap.get(name);
    if (result == null) {
      result = defaultResult;
    }
    System.out.println("Principle Name In: " + name + "  Out: " + result);
    return result;
  }
  
  private boolean hasPermission(String[] permissionArray, String[] requiredPermissions) {
    for (String requiredPermission : requiredPermissions) {
      if (!containsPermission(permissionArray, requiredPermission)) {
        return false;
      }
    }
    return true;
  }
  
  private boolean containsPermission(String[] permissionArray, String permission) {
    for (String permissionFromList : permissionArray) {
      if (permission.equals(permissionFromList)) {
        return true;
      }
    }
    return false;
  }
    
  static public Map<String, String> makeNameMap() {
    Map<String, String> result = new HashMap<String, String>();
    
    result.put("John Felton", "johnfelton@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\johnfelton", "johnfelton@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\peeyush", "peeyush@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\richardliu", "richardliu@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\strellis", "strellis@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\ziff", "ziff@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\darshan", "darshan@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\rakeshs", "rakeshs@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\anil", "anil@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\chris", "chris@sharepoint-connector.com");
    result.put("W2K3-SP2007\\admistrator", "admin@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\contractors", "contractors@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\engineering", "engineering@sharepoint-connector.com");
    result.put("ES-TEST-DOM1\\product management", "product-management@sharepoint-connector.com");
    result.put("Guests", "guests@sharepoint-connector.com");
    return result;
  }
  

}
