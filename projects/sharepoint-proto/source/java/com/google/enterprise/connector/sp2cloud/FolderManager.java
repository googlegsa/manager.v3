package com.google.enterprise.connector.sp2cloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderManager {

  private final Map<String, FolderInfo> folderIdToFolderInfoMap =
      new HashMap<String, FolderInfo>();

  // Key is id for not yet created parent and value is list of
  // already defined children.
  private final Map<String, List<FolderInfo>>
      awaitingParents = new HashMap<String, List<FolderInfo>>();

  private final List<FolderInfo> rootFolders = new ArrayList<FolderInfo>();

  /**
   * Add a {@link FolderInfo} to this {@link FolderManager}.
   * @param folderInfo
   * @throws IllegalStateException if a {@link FolderInfo} with a matching
   * {@link FolderInfo#getId()} has already been added.
   */
  void add(FolderInfo folderInfo) throws IllegalStateException{
    if (folderIdToFolderInfoMap.get(folderInfo.getId()) != null) {
      throw new IllegalStateException("Can't add an already added folder - "
          + folderInfo);
    }
    folderIdToFolderInfoMap.put(folderInfo.getId(), folderInfo);
    if (folderInfo.isRoot()) {
      rootFolders.add(folderInfo);
    }

    // Add previously defined children to folderInfo.
    List<FolderInfo> childFolderInfos = awaitingParents.get(folderInfo.getId());
    if (childFolderInfos != null) {
      for (FolderInfo childFolderInfo : childFolderInfos) {
        folderInfo.addChild(childFolderInfo);
      }
    }

    if (!folderInfo.isRoot()) {
      // Add folderInfo to parent.
      FolderInfo parentFolderInfo = folderIdToFolderInfoMap
          .get(folderInfo.parentId);
      if (parentFolderInfo == null) {
        List<FolderInfo> childList = awaitingParents
            .get(folderInfo.parentId);
        if (childList == null) {
          childList = new ArrayList<FolderInfo>();
          awaitingParents.put(folderInfo.parentId, childList);
        }
        childList.add(folderInfo);
      } else {
        parentFolderInfo.addChild(folderInfo);
      }

    }
  }

  FolderInfo getFolderInfo(String folderId) {
    return folderIdToFolderInfoMap.get(folderId);
  }

  List<FolderInfo> getRootFolders() {
    return Collections.unmodifiableList(rootFolders);
  }

  FolderInfo newFolderInfo(String id, String parentId, String name,
      CloudAcl cloudAcl) {

    return new FolderInfo(id, parentId, name, cloudAcl);
  }

  /**
   * Holder class for folder state.
   */
  class FolderInfo {
    private final String id;
    private final String parentId;
    private final String name;
    private final CloudAcl cloudAcl;
    private final List<FolderInfo> childFolders;
    private String baseUrl;

    private FolderInfo(String id, String parentId, String name,
        CloudAcl cloudAcl) {
      this.id = id;
      this.parentId = parentId;
      this.name = name;
      this.cloudAcl = cloudAcl;

      this.childFolders = new ArrayList<FolderInfo>();
    }

    String getId() {
      return id;
    }

    boolean isRoot() {
      return parentId == null;
    }

    FolderInfo getParent() {
      return folderIdToFolderInfoMap.get(parentId);
    }
    
    String getName() {
      return name;
    }

    CloudAcl getCloudAcl() {
      return cloudAcl;
    }

    private void addChild(FolderInfo childFolderInfo) {
      childFolders.add(childFolderInfo);
    }

    List<FolderInfo> getChildFolders() {
      return Collections.unmodifiableList(childFolders);
    }

    void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    String getBaseUrl() {
      return baseUrl;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      FolderInfo other = (FolderInfo) obj;
      if (!getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (baseUrl == null) {
        if (other.baseUrl != null) {
          return false;
        }
      } else if (!baseUrl.equals(other.baseUrl)) {
        return false;
      }
      if (childFolders == null) {
        if (other.childFolders != null) {
          return false;
        }
      } else if (!childFolders.equals(other.childFolders))
        return false;
      if (cloudAcl == null) {
        if (other.cloudAcl != null) {
          return false;
        }
      } else if (!cloudAcl.equals(other.cloudAcl)) {
        return false;
      }
      if (id == null) {
        if (other.id != null) {
          return false;
        }
      } else if (!id.equals(other.id)) {
        return false;
      }
      if (parentId == null) {
        if (other.parentId != null) {
          return false;
        }
      } else if (!parentId.equals(other.parentId)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
      result = prime * result
          + ((childFolders == null) ? 0 : childFolders.hashCode());
      result = prime * result + ((cloudAcl == null) ? 0 : cloudAcl.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "FolderInfo: id = " + id + " parentId = " + parentId
          + " baseUrl = " + baseUrl + " cloudAcl = " + cloudAcl
          + " childFolders = " + childFolders;
    }

    private FolderManager getOuterType() {
      return FolderManager.this;
    }
  }
}
