// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import com.google.enterprise.connector.sp2cloud.CloudAce.TypeAndNameKey;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.acl.AclScope.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google cloud Acl for representing a DocList ACL.
 *
 * TODO(felton): If Sharepoint supports group owners add support.
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

  /**
   * A {@link CloudAcl} for representing the {@link CloudAcl}  of the
   * cloud root folder which has the following special properties
   * <ol>
   * <li> no {@link CloudAce} entries
   * <li> null return for {@link #getOwner()}.
   * </ol>
   *
   * {@link #NULL_ACL} is used to avoid treating the {@link CloudAcl} for the
   * DocList root folder as a special case in the {@link
   * #getAclAdjustments(CloudAcl, String)} implementation. Because all
   * {@link CloudAcl} objects exposed by this class have a non null
   * {@link #getOwner()} value this object should be kept strictly private.
   */
  private static CloudAcl NULL_ACL = new CloudAcl(
      Collections.unmodifiableList(new ArrayList<CloudAce>()), null);

  /**
   * Returns the adjustments that must be made to the inherited parentAcl to
   * obtain this {@link CloudAcl}. In the case 'adminUserId' is not the
   * owner this adds an ace to give the 'adminUserId'
   * {@link AclRole#WRITER} permission.
   */
  AclAdjustments getAclAdjustments(CloudAcl parentAcl,
      String adminUserId) {
    if (parentAcl == null) {
     parentAcl = NULL_ACL;
    }
    InsertsAndUpdates insertsAndUpdates = getInsertsAndUpdates(
        parentAcl, adminUserId);
    List<CloudAce> deletes = createDeletes(parentAcl, adminUserId);
    return new AclAdjustments(insertsAndUpdates.getInserts(),
        insertsAndUpdates.getUpdates(), deletes);
  }

  /**
   * Temporary holder class for lists of {@link CloudAce} objects that must
   * inserted or updated for use constructing {@link AclAdjustments}.
   */
  private static class InsertsAndUpdates {
    private final List<CloudAce> inserts;
    private final List<CloudAce> updates;
    InsertsAndUpdates(List<CloudAce> inserts, List<CloudAce>updates) {
      this.inserts = inserts;
      this.updates = updates;
    }

    List<CloudAce> getInserts() {
      return inserts;
    }

    List<CloudAce> getUpdates() {
      return updates;
    }
  }

  /**
   * Returns an {@link InsertsAndUpdates} with lists of {@link CloudAce}
   * {@link CloudAce} objects that must be
   * <ol>
   * <li> inserted because they are defined for this {@link CloudAcl} and
   *      not for our parent {@link CloudAcl}.
   * <li> updated because they are defined with different {@link AclRole}
   *      values for our {@link CloudAcl} and this {@link CloudAcl}.
   * </li>
   *
   * Note owners are special because
   * <ol>
   * <li> A child ACL inherits {@link AclRole#WRITER} from a corresponding
   *      parent {@link AclRole#OWNER} entry.
   * <li> Doclist automatically adds the {@link AclRole#OWNER} entry to an
   *      object's ACL during creation.
   * </ol>
   * This adds an entry to inserts to give 'adminUserId' {@link
   * AclRole#WRITER} permission unless 'adminUserId' already has
   * {@link AclRole#OWNER} permission.
   */
  private InsertsAndUpdates getInsertsAndUpdates(CloudAcl parentAcl, String adminUserId) {
    List<CloudAce> inserts = new ArrayList<CloudAce>();
    List<CloudAce> updates = new ArrayList<CloudAce>();
    Map<CloudAce.TypeAndNameKey, CloudAce> parentTypeAndNameKeyToCloudAceMap =
        parentAcl.newTypeAndNameKeyToCloudAceMap();
    for (CloudAce cloudAce : getAceList()) {
      if (cloudAce.getName().equals(adminUserId)) {
        continue;
      }

      if (cloudAce.getRole().equals(AclRole.OWNER)) {
        continue;
      }

      CloudAce parentAce =
        parentTypeAndNameKeyToCloudAceMap.get(cloudAce.newTypeAndNameKey());
      if (parentAce == null) {
        inserts.add(cloudAce);
      } else if (parentAce.getRole().equals(cloudAce.getRole())) {
        continue;
      } else if (parentAce.getRole().equals(AclRole.OWNER)
          && cloudAce.getRole().equals(AclRole.WRITER)) {
        continue;
      } else {
        updates.add(cloudAce);
      }
    }
    if (!getOwner().equals(adminUserId)) {
      inserts.add(new CloudAce(adminUserId, Type.USER, AclRole.WRITER));
    }
    return new InsertsAndUpdates(inserts, updates);
  }

  /**
   * Returns the {@link CloudAce} items that must be deleted because they are
   * defined for our parent {@link CloudAcl} but not this {@link CloudAcl}.
   */
  private List<CloudAce> createDeletes(CloudAcl parentAcl, String adminUserId) {
    List<CloudAce> deletes = new ArrayList<CloudAce>();
    if (parentAcl != null) {
      Map<CloudAce.TypeAndNameKey, CloudAce> childTypeAndNameKeyToCloudAceMap =
          newTypeAndNameKeyToCloudAceMap();
      for (CloudAce parentAce : parentAcl.getAceList()) {
        //DO not delete admin user ACE. We add it if it is missing from
        // cloudAcl. Could add it before calling this too.
        if (parentAce.getName().equals(adminUserId)
            && parentAce.getType().equals(AclScope.Type.USER)) {
          continue;
        }
        if (parentAce.getRole().equals(AclRole.OWNER)) {
          parentAce = new CloudAce(parentAce.getName(), parentAce.getType(),
              AclRole.WRITER);
        }
        if (!childTypeAndNameKeyToCloudAceMap.containsKey(
            parentAce.newTypeAndNameKey())) {
          deletes.add(parentAce);
        }
      }
    }
    return deletes;
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
    return "CloudAcl: owner = " + owner + "; aceList = " + aceList;
  }
}
