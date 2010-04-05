package com.google.enterprise.connector.sp2cloud;

import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;

public class CloudAce {
  private final String name;
  private final AclScope.Type type;
  private final AclRole role;

  public CloudAce(String name, AclScope.Type type, AclRole role) {
    this.name = name;
    this.type = type;
    this.role = role;
  }

  public String getName() {
    return name;
  }

  public AclScope.Type getType() {
    return type;
  }

  public AclRole getRole() {
    return role;
  }

  public TypeAndNameKey newTypeAndNameKey() {
    return new TypeAndNameKey();
  }

  @Override
  public String toString() {
    return "CloudAce Name="
        + name + " role=" + role.getValue()
        + " type=" + type;
  }

  /**
   *
   * @author strellis@google.com (Your Name Here)
   *
   */
  class TypeAndNameKey {
    String getName() {
      return name;
    }
    AclScope.Type getType() {
      return type;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
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
      TypeAndNameKey other = (TypeAndNameKey) obj;
      if (name == null) {
        if (other.getName() != null) {
          return false;
        }
      } else if (!name.equals(other.getName())) {
        return false;
      }
      if (type == null) {
        if (other.getType() != null) {
          return false;
        }
      } else if (!type.equals(other.getType())) {
        return false;
      }
      return true;
    }
  }
}
