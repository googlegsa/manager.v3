package com.google.enterprise.connector.sp2cloud;

import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holder class for a cloud acl.
 * TODO(felton): If Sharepoint supports group owners add support.
 */
class CloudAcl {
  private final List<CloudAce> aceList;
  private final String owner; // Derived/omitted from hashCode and equals.

  /**
   * Returns a new CloudAcl.
   * @throws IllegalArgumentException if {{@link #aceList} does not contain
   * an owner, contains an owner that is not of type {@link AclScope.Type#USER}, or
   * contains more than one owner.
   */
  static CloudAcl newCloudAcl(List<CloudAce> aceList) throws IllegalArgumentException{
    String owner = null;
    for (CloudAce ace : aceList) {

      if (ace.getRole().equals(AclRole.OWNER)) {
        if (!ace.getType().equals(AclScope.Type.USER)) {
          throw new IllegalArgumentException("Unsupported Ace - owner must be a user - name = " + ace.getName() + " type = " + ace.getType().toString());
        }

        if (owner != null) {
          throw new IllegalArgumentException("Only one owner supported " + aceList);
        }

        owner = ace.getName();
      }
    }
    if (owner == null) {
      throw new IllegalArgumentException("Acl must define an owner " + aceList);
    }
    return new CloudAcl(Collections.unmodifiableList(
        new ArrayList<CloudAce>(aceList)), owner);
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
