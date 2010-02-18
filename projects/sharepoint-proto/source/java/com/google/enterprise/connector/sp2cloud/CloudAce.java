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
  
  @Override
  public String toString() {
    return "CloudAce Name=" + name + " role=" + role.getValue() + " type=" + type;
  }
  
}
