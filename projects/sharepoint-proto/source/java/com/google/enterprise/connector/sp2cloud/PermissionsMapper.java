package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2c_migration.Ace;
import com.google.enterprise.connector.sp2c_migration.Folder;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionsMapper {
  private static String[] REQUIRED_FOR_WRITE = {"ViewListItems", "AddListItems", "EditListItems", "DeleteListItems", "OpenItems", "ViewVersions", "DeleteVersions", "Open", "ViewPages"};
  private static String[] REQUIRED_FOR_READ = {"ViewListItems", "OpenItems", "ViewVersions", "Open", "ViewPages"};
          
  private final Map<String, String> userAndGroupNameMap;
  
  public PermissionsMapper(Map<String, String> userAndGroupNameMap) {
    this.userAndGroupNameMap = userAndGroupNameMap;
  }
  
  public List<CloudAce> mapAcl(List<Ace> acl, String parentId, Map<String, Folder> idToFolderMap) {
    List<CloudAce> parentCloudAcl;

    if (idToFolderMap.containsKey(parentId)) {
      Folder parent = idToFolderMap.get(parentId);
      parentCloudAcl = mapAcl( parent.getAcl(), parent.getParentId(), idToFolderMap);
    } else {
      parentCloudAcl = new ArrayList<CloudAce>();
    }
    
    List<CloudAce> result = new ArrayList<CloudAce>();
    
    for (Ace ace : acl) {
      // Look up name
      String cloudName = userAndGroupNameMap.get(ace.getName());
      if (cloudName == null) {
        continue;
      }
      
       AclRole cloudRole;
      if (hasPermission(ace.getPermission().getAllowedPermissions(), REQUIRED_FOR_WRITE)) {
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
      
      // Only write an ACE if the same permission does not exist in its parent folder.
      if (!aceExistsInParent(cloudAce, parentId, idToFolderMap)) {
        result.add(cloudAce);
      }
    }
    return result;    
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
  
  // TODO(johnfelton) : This method is extremely slow since it recalculates the cloud permissions
  // over and over as it runs up folder hierarchy.
  private boolean aceExistsInParent(CloudAce cloudAce, String parentId, 
      Map<String, Folder> idToFolderMap) {
    if (!idToFolderMap.containsKey(parentId)) {
      return false;
    } else {
      Folder parentFolder = idToFolderMap.get(parentId);
      List<CloudAce> parentCloudAcl = mapAcl(parentFolder.getAcl(), parentFolder.getParentId(), 
          idToFolderMap);

      for (CloudAce parentCloudAce : parentCloudAcl) {
        if (cloudAce.getName().equals(parentCloudAce.getName())) {
          if (cloudAce.getRole().equals(AclRole.WRITER) && parentCloudAce.getRole().equals(AclRole.WRITER)) {
            return true;
          } else if (cloudAce.getRole().equals(AclRole.READER)) {
              return true;
          }
        }
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
