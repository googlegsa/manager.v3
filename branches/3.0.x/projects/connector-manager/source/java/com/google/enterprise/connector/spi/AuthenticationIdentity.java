// Copyright 2007 Google Inc.
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
 * Interface for the {@code identity} parameter of the
 * {@link AuthenticationManager#authenticate AuthenticationManager.authenticate}
 * method.
 *
 * If an implementation overrides the {# toString} method, it should
 * not include the password in the returned string.
 *
 * @since 1.0
 */
public interface AuthenticationIdentity {

  /**
   * Gets the username.
   *
   * @return the username, as a String
   */
  public String getUsername();

  /**
   * Gets the password.
   *
   * @return the password, as a String
   */
  public String getPassword();

  /**
   * Gets the domain.
   *
   * @return the domain, as a String
   * @since 2.0
   */
  public String getDomain();
}
