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

package com.google.enterprise.security.identity;

import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * This mis-named class models the per-session info associated with verifying a particular
 * identity using a particular mechanism.  A better name might be "verification info", but
 * we have yet to settle on an appropriate name.  This object directly holds the
 * per-session data, while the configuration info, which is shared by all sessions, is
 * stored in a separate object.
 */
public class DomainCredentials implements SecAuthnIdentity {

  private final AuthnDomain configInfo;
  private final CredentialsGroup cg;
  private VerificationStatus status;
  private final List<Cookie> cookies;

  DomainCredentials(AuthnDomain configInfo, CredentialsGroup cg) {
    this.configInfo = configInfo;
    this.cg = cg;
    status = VerificationStatus.TBD;
    cookies = new ArrayList<Cookie>();
    cg.addElement(this);
  }

  // Used for testing only:
  public static DomainCredentials dummy() {
    return new DomainCredentials(null, CredentialsGroup.dummy());
  }

  public String getDomain() {
    return configInfo.getName();
  }

  public AuthNMechanism getMechanism() {
    return configInfo.getMechanism();
  }

  public String getSampleUrl() {
    return configInfo.getSampleUrl();
  }

  public CredentialsGroup getGroup() {
    return cg;
  }

  public String getUsername() {
    return cg.getUsername();
  }

  public String getPassword() {
    return cg.getPassword();
  }

  public void addCookie(Cookie c) {
    cookies.add(c);
  }

  // For testing:
  public void clearCookies() {
    cookies.clear();
  }

  public VerificationStatus getVerificationStatus() {
    return status;
  }

  public void setVerificationStatus(VerificationStatus status) {
    this.status = status;
  }

  public Collection<Cookie> getCookies() {
    return Collections.unmodifiableCollection(cookies);
  }

  public Cookie getCookieNamed(String name) {
    for (Cookie c: cookies) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  private String dumpCookies() {
    StringBuilder sb = new StringBuilder();
    for (Cookie c : cookies) {
      sb.append(c.getName() + "::" + c.getValue() + ";");
    }
    return sb.toString();
  }

  public String dumpInfo() {
    return getUsername() + ":" + getPassword() + ":" + dumpCookies();
  }
}
