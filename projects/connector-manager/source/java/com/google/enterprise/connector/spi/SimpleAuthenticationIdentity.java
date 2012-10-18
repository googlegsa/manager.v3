// Copyright 2009 Google Inc.
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

import com.google.enterprise.connector.spi.AuthenticationIdentity;

/**
 * Simple implementation of the {@link AuthenticationIdentity} interface.
 * Connector developers may want to use this to implement unit tests.
 */
public class SimpleAuthenticationIdentity implements AuthenticationIdentity {

  private final String username;
  private final String password;
  private final String domain;

  /* @Override */
  public String getPassword() {
    return password;
  }

  /* @Override */
  public String getUsername() {
    return username;
  }

  /* @Override */
  public String getDomain() {
    return domain;
  }

  /**
   * Constructs a {@code SimpleAuthenticationIdentity} using just a user-name.
   *
   * @param username the user's name
   */
  public SimpleAuthenticationIdentity(final String username) {
    this(username, null, null);
  }

  /**
   * Constructs a {@code SimpleAuthenticationIdentity} using only a user-name
   * and a password.
   *
   * @param username the user's name
   * @param password the password associated with the user's name
   */
  public SimpleAuthenticationIdentity(final String username,
      final String password) {
    this(username, password, null);
  }

  /**
   * Constructs a {@code SimpleAuthenticationIdentity} using user-name,
   * and password and domain.
   *
   * @param username the user's name
   * @param password the password associated with the user's name
   * @param domain the domain associated with this user
   */
  public SimpleAuthenticationIdentity(final String username,
      final String password, final String domain) {
    this.domain = domain;
    this.username = username;
    this.password = password;
  }

  @Override
  public String toString() {
    return "{ domain = " + domain + ", username = " + username + " }";
  }
}
