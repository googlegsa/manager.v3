// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

/**
 * Represents a principal for authentication and authorization purposes.
 *
 * @since 3.0
 */
public class Principal implements Comparable<Principal> {

  private final PrincipalType type;
  private final String namespace;
  private final String name;
  private final CaseSensitivityType caseSensitivityType;

  /**
   * Builds a Principal instance with simply a case-sensitive name.
   *
   * @param name the name of the principal
   */
  public Principal(String name) {
    this(null, null, name);
  }

  /**
   * Builds a case-sensitive Principal instance.
   *
   * @param type the principal type for the principal
   * @param namespace the namespace for the principal
   * @param name the name of the principal
   */
  public Principal(PrincipalType type, String namespace, String name) {
    this(type, namespace, name, CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
  }

  /**
   * Builds a Principal instance.
   *
   * @param type the principal type for the principal
   * @param namespace the namespace for the principal
   * @param name the name of the principal
   * @param caseSensitivityType how to handle casing for the principal
   */
  public Principal(PrincipalType type, String namespace, String name,
      CaseSensitivityType caseSensitivityType) {
    if (caseSensitivityType == null) {
      throw new NullPointerException("caseSensitivityType must not be null");
    }
    this.type = type;
    this.namespace = namespace;
    this.name = name;
    this.caseSensitivityType = caseSensitivityType;
  }

  /**
   * Gets the principal name.
   *
   * @return the principal name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the principal namespace.
   *
   * @return the principal namespace
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Gets the principal type.
   *
   * @return the principal type
   */
  public PrincipalType getType() {
    return type;
  }

  /**
   * Gets how cases are handled for the principal.
   *
   * @return the case sensitivity type
   */
  public CaseSensitivityType getCaseSensitivityType() {
    return caseSensitivityType;
  }

  @Override
  public String toString() {
    return "{ type = " + type + ", namespace = " + namespace
           + ", name = " + name + ", casing = " + caseSensitivityType + " }";
  }

  @Override
  public int hashCode() {
    final int prime = 593;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
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
    Principal other = (Principal) obj;
    if ((compareStrings(name, other.name) != 0) ||
        (compareStrings(namespace, other.namespace) != 0)) {
      return false;
    }
    return (type == null) ? (other.type == null) : type.equals(other.type);
  }

  /**
   * Note that is comparison is inconsistent with equals, in
   * that the PrincipalType is not considered in ordering.
   */
  @Override
  public int compareTo(Principal other) {
    if (this == other) {
      return 0;
    }
    if (other == null) {
      return 1;
    }
    int result = compareStrings(namespace, other.namespace);
    if (result != 0) {
      return result;
    }
    return compareStrings(name, other.name);
  }

  private static int compareStrings(String s1, String s2) {
    if (s1 == null) {
      return (s2 != null) ? -1 : 0;
    } else if (s2 != null) {
      return s1.compareTo(s2);
    }
    return 1;
  }
}
