// Copyright (C) 2009 Google Inc.
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

public enum AuthnMechanism {
  BASIC_AUTH, // aka SekuLite
  FORMS_AUTH, // was known as (non-SAML) "SSO"
  SAML, // the SAML SPI
  SSL, // client-side x.509 certificate authN
  CONNECTORS, // connector manager authN logic
  SPNEGO_KERBEROS
  // GSSAPI/SPNEGO/Kerberos WWW-Authenticate handshake
}
