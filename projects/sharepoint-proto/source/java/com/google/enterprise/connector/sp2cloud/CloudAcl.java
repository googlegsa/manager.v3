package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2cloud.CloudAce.TypeAndNameKey;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holder class for a cloud acl. TODO(felton): If Sharepoint supports group
 * owners add support.
 */
class CloudAcl {
  private final List<CloudAce> aceList;
  private final String owner; // Derived/omitted from hashCode and equals.

  /**
   * Returns a new CloudAcl.
   *
   * @throws IllegalArgumentException if { @code cloudAceList} does not contain
   *         an owner, contains an owner that is not of type
   *         {@link AclScope.Type#USER}, contains more than one owner or
   *         contains a pair of {@link CloudAce} objects with equal
   *         {@link CloudAce.TypeAndNameKey} values.
   */
  static CloudAcl newCloudAcl(List<CloudAce> cloudAceList)
      throws IllegalArgumentException {
    // Called to throw an IllegalArgumenException if cloudAceList
    // contains entries with matching TypeAndNameKey values.
    newTypeAndNameKeyToCloudAceMap(cloudAceList);
    String owner = getOwner(cloudAceList);

    return new CloudAcl(Collections.unmodifiableList(
        new ArrayList<CloudAce>(cloudAceList)), owner);
  }

  private static String getOwner(List<CloudAce> aceList) {
    String owner = null;
    for (CloudAce ace : aceList) {

      if (ace.getRole().equals(AclRole.OWNER)) {
        if (!ace.getType().equals(AclScope.Type.USER)) {
          throw new IllegalArgumentException("Owner must be a user " + aceList);
        }

        if (owner != null) {
          throw new IllegalArgumentException(
              "Only one owner supported " + aceList);
        }

        owner = ace.getName();
      }
    }
    if (owner == null) {
      throw new IllegalArgumentException("Acl must define an owner " + aceList);
    }
    return owner;
  }

  /**
   * Constructs and returns a {@link Map} for efficient access to a
   * {@link CloudAce} based on its {@link TypeAndNameKey}.
   * @throws IllegalArgumentException if the passed 'aceList' includes
   *     {@link CloudAce} objects with equal {@link CloudAce.TypeAndNameKey}
   *     values.
   */
  private static Map<CloudAce.TypeAndNameKey, CloudAce>
      newTypeAndNameKeyToCloudAceMap(List<CloudAce> cloudAceList) {
    Map<CloudAce.TypeAndNameKey, CloudAce> result =
      new HashMap<CloudAce.TypeAndNameKey, CloudAce>(cloudAceList.size());
    for (CloudAce cloudAce : cloudAceList) {
      if (result.put(cloudAce.newTypeAndNameKey(), cloudAce) != null) {
        throw new IllegalArgumentException(
            "CloudAces must have unique {type, name} pairs - " + cloudAceList);
      }
    }
    return result;
  }

  private CloudAcl(List<CloudAce> aceList, String owner) {
    this.aceList = aceList;
    this.owner = owner;
  }

  public List<CloudAce> getAceList() {
    return aceList;
  }

  public String getOwner() {
    return owner;
  }

  public Map<CloudAce.TypeAndNameKey, CloudAce>
      newTypeAndNameKeyToCloudAceMap() {
    return newTypeAndNameKeyToCloudAceMap(aceList);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aceList == null) ? 0 : aceList.hashCode());
    return result;
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
    CloudAcl other = (CloudAcl) obj;
    if (aceList == null) {
      if (other.aceList != null) {
        return false;
      }
    } else if (!aceList.equals(other.aceList)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "CloudAcl owner = " + owner + " aceList = " + aceList;
  }
}
