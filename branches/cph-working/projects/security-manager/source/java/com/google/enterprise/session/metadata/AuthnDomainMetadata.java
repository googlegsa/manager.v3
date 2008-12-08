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

package com.google.enterprise.session.metadata;

import java.util.ArrayList;
import java.util.List;

public class AuthnDomainMetadata {

  private static final List<AuthnDomainMetadata> allMetadata =
      new ArrayList<AuthnDomainMetadata>();

  public static enum AuthnMechanism {
    BASIC_AUTH,     // aka SekuLite
    FORMS_AUTH,     // was known as (non-SAML) "SSO"
    SAML,           // the SAML SPI
    SSL,            // client-side x.509 certificate authN
    CONNECTORS,     // connector manager authN logic
    SPNEGO_KERBEROS // GSSAPI/SPNEGO/Kerberos WWW-Authenticate handshake
  }

  private final String name;
  private final String humanName;
  private final AuthnMechanism mechanism;
  private final List<String> urlPatterns;
  private List<AuthnDomainMetadata> group;

  // Fields for BASIC_AUTH and FORMS_AUTH
  private String loginUrl;
  private String testUrl;

  // Fields for SAML:
  //private final MetadataProvider metadataProvider;

  public AuthnDomainMetadata(String name, String humanName, AuthnMechanism mechanism) {
    this.name = name;
    this.humanName = humanName;
    this.mechanism = mechanism;
    urlPatterns = new ArrayList<String>();
    group = new ArrayList<AuthnDomainMetadata>();
    group.add(this);
    allMetadata.add(this);
  }

  public static AuthnDomainMetadata getMetadata(String name) {
    for (AuthnDomainMetadata metadata: allMetadata) {
      if (metadata.getName().equals(name)) {
        return metadata;
      }
    }
    return null;
  }

  public static List<AuthnDomainMetadata> getAllMetadata() {
    return allMetadata;
  }

  public String getName() {
    return name;
  }

  public String getHumanName() {
    return humanName;
  }

  public AuthnMechanism getMechanism() {
    return mechanism;
  }

  public List<String> getUrlPatterns() {
    return urlPatterns;
  }

  public void mergeGroups(AuthnDomainMetadata metadata) {
    for (AuthnDomainMetadata m: metadata.group) {
      group.add(m);
      m.group = group;
    }
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
