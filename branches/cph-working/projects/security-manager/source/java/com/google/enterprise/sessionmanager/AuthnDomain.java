// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.sessionmanager;

import java.util.ArrayList;
import java.util.List;

public class AuthnDomain {

  private final String name;
  private final AuthnMechanism mechanism;
  private final AuthnDomainGroup group;
  private final List<String> urlPatterns;

  // Fields for BASIC_AUTH and FORMS_AUTH
  private String loginUrl;
  private String testUrl;

  // Fields for SAML:
  //private final MetadataProvider metadataProvider;

  public AuthnDomain(String name, AuthnMechanism mechanism, AuthnDomainGroup group) {
    this.name = name;
    this.mechanism = mechanism;
    this.group = group;
    urlPatterns = new ArrayList<String>();
    group.addDomain(this);
  }

  public static AuthnDomain compatAuthSite(
      String fqdn, String realm, AuthnMechanism mechanism, String loginUrl) {
    AuthnDomainGroup group = new AuthnDomainGroup("\"" + realm + "\" @" + fqdn);
    AuthnDomain domain = new AuthnDomain(realm, mechanism, group);
    domain.getUrlPatterns().add(fqdn);
    domain.setLoginUrl((loginUrl == null) ? fqdn + realm : loginUrl);
    return domain;
  }

  public String getName() {
    return name;
  }

  public AuthnMechanism getMechanism() {
    return mechanism;
  }

  public AuthnDomainGroup getGroup() {
    return group;
  }

  public List<String> getUrlPatterns() {
    return urlPatterns;
  }

  public void setLoginUrl(String url) {
    loginUrl = url;
  }

  public String getLoginUrl() {
    return loginUrl;
  }

  public void setTestUrl(String url) {
    testUrl = url;
  }

  public String getTestUrl() {
    return testUrl;
  }
}
