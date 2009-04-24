// Copyright (C) 2008, 2009 Google Inc.
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

import com.google.enterprise.security.identity.VerificationStatus;

import java.util.Collection;

import javax.servlet.http.Cookie;

/**
 * An extension to the connector manager's identity model to support additional identity
 * information needed by the security manager: cookies, identity verification, etc.
 */
public interface SecAuthnIdentity extends AuthenticationIdentity {

  /**
   * Get the cookies associated with this identity.
   * Don't modify the result; the implementation should return an immutable copy.
   * @return A collection of cookie objects.
   */
  public Collection<Cookie> getCookies();

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
}
