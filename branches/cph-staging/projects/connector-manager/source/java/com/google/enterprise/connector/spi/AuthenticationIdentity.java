// Copyright 2007-8 Google Inc.
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

import java.util.Set;

/**
 * Interface for the identity parameter of the
 * {@link AuthenticationManager}.authenticate method.
 */
public interface AuthenticationIdentity {

  /**
   * Get the username
   * @return the username, as a String
   */
  public String getUsername();

  /**
   * Get the password
   * @return the password, as a String
   */
  public String getPassword();

  /**
   * Get a named cookie value.
   * 
   * @param cookieName a non-null, non-empty String
   * @return the cookie value, as a String, or null if not set
   */
  public String getCookie(String cookieName);

  /**
   * Set a named cookie value. If there was a previous value for this cookie,
   * the old value is replaced by the specified value. If the value is null or
   * empty, this has the effect of removing the cookie, if present.
   * 
   * @param cookieName a non-null, non-empty String
   * @param value the new value for this cookie; if null or empty, then the
   *        cookie is removed
   * @return the previous value associated with this cookie name, or null if
   *         there was no value
   */
  public String setCookie(String cookieName, String value);

  /**
   * Get the Set of all defined cookies. If none are defined, an empty
   * Set is returned.
   * 
   * @return the Set (of String) of all cookie names for which a value exists
   */
  public Set getCookieNames();
}
