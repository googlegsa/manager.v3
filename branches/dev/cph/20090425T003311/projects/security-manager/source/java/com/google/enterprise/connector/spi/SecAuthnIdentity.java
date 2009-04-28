// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

import com.google.enterprise.common.CookieDifferentiator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

/**
 * An extension to the connector manager's identity model to support additional identity
 * information needed by the security manager: cookies, identity verification, etc.
 */
public interface SecAuthnIdentity extends AuthenticationIdentity {

  /**
   * Get the session for this identity.
   *
   * @return The identity's session object.
   */
  public HttpSession getSession();

  /**
   * Get the cookie differentiator associated with this identity.
   *
   * @return The identity's cookie differentiator.
   */
  public CookieDifferentiator getCookieDifferentiator();

  /**
   * Associate a cookie with this identity.
   * If the cookie's name is the same as a previously associated cookie, the
   * implementation is allowed to overwrite the previous cookie, so don't count on there
   * being multiple cookies with the same name.
   * @param c The cookie to associate.
   */
  public void addCookie(Cookie c);

  /**
   * Get an associated cookie by name.
   * @param name The name of the cookie to return.
   * @return The associated cookie, or null if no such cookie.
   */
  public Cookie getCookieNamed(String name);

  /**
   * Get the verification status for this identity.
   * @return The identity's verification status.
   */
  public VerificationStatus getVerificationStatus();

  /**
   * Set the verification status for this identity.
   * @param status The new verification status.
   */
  public void setVerificationStatus(VerificationStatus status);

  /**
   * Return a login URL for identity types that require one.
   * @return The login URL, or null if no such.
   */
  public String getSampleUrl();

  /**
   * Set the identity's username.
   *
   * @param username The new username, must not be null.
   */
  public void setUsername(String username);
}
