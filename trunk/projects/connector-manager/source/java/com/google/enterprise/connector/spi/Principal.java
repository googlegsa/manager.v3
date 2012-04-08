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


/**
 * Represents a principal for authentication and authorization purposes.
 *
 * @since 3.0
 */
public class Principal {

  private final SpiConstants.PrincipalType type;
  private final String namespace;
  private final String name;

  /**
   * Builds a Principal instance with simply a name.
   *
   * @param name the name of the principal
   */
  public Principal(String name) {
    this(null, null, name);
  }

  /**
   * Builds a Principal instance.
   *
   * @param type the principal type for the principal
   * @param namespace the namespace for the principal
   * @param name the name of the principal
   */
  public Principal(SpiConstants.PrincipalType type, String namespace,
                   String name) {
    this.type = type;
    this.namespace = namespace;
    this.name = name;
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
  public SpiConstants.PrincipalType getType() {
    return type;
  }

  @Override
  public String toString() {
    return "{ type = " + type + ", namespace = " + namespace
           + ", name = " + name + " }";
  }
}
