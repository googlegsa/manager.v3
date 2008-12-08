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

package com.google.enterprise.saml.server;

import com.google.enterprise.session.metadata.AuthnDomainMetadata;
import com.google.enterprise.session.metadata.AuthnDomainMetadata.AuthnMechanism;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

class OmniForm {
  private final String actionUrl;
  private StringBuffer formContent;
  
  public OmniForm(CSVReader reader, String actionUrl) throws NumberFormatException, IOException {
    while (true) {
      String[] nextLine = reader.readNext();
      if (nextLine == null) {
        break;
      }
      if (! (nextLine.length == 3 || nextLine.length == 4)) {
        throw new IllegalStateException("ill-formed configuration line");
      }
      String fqdn = nextLine[0];
      String realm = nextLine[1];
      AuthnMechanism mechanism = Enum.valueOf(AuthnMechanism.class, nextLine[2]);
      String loginUrl = (nextLine.length > 3) ? nextLine[3] : fqdn + realm;

      String humanName = "\"" + realm + "\" @" + fqdn;
      AuthnDomainMetadata metadata = new AuthnDomainMetadata(realm, humanName, mechanism);
      metadata.getUrlPatterns().add(fqdn);
      metadata.setLoginUrl(loginUrl);
    }
    this.actionUrl = actionUrl;
  }

  /**
   * 
   * @param ids
   * @return the whole form suitable for display
   */
  public String writeForm(UserIdentity[] ids) {
    formContent = new StringBuffer("<form method=\"post\" name=\"omni\" action=\"" +
                                   actionUrl +
                                   "\">\n");
    int idx = 0;
    for (AuthnDomainMetadata metadata: AuthnDomainMetadata.getAllMetadata()) {
      writeArea(metadata, idx, (ids != null) ? ids[idx] : null);
      idx += 1;
    }
    formContent.append("<input type=\"submit\"></form>");
    return formContent.toString();
  }

  /*
   * Each area on the omniform looks like:
   *  "Please login to %s@%s", AuthName, HTTPServerName
   *  "username", "input type=text name=u"
   *  "password", "input type=password name=pw"
   */
  private void writeArea(AuthnDomainMetadata metadata, int idx, UserIdentity id) {
    String inputStatus = "";
    if ((id != null) && id.isVerified()) {
      inputStatus = " disabled";
      formContent.append("<span style=\"color:green\">Logged in to " + metadata.getHumanName() + "</span>");
    } else {
      formContent.append("Please login to " + metadata.getHumanName());
    }
    formContent.append(":<br>\n");
    formContent.append("<b>Username</b> <input type=\"text\" name=u" + idx + inputStatus + "><br>\n");
    formContent.append("<b>Password</b> <input type=\"password\" name=pw" + idx + inputStatus + "><br>\n");
    formContent.append("<br><br><br>\n");
  }
  
  /**
   * Parse the form into an array of credentials awaiting authn decision.
   */
  public UserIdentity[] parse(HttpServletRequest request, UserIdentity[] oldIds) {
    String username;
    String password;
    UserIdentity[] identities = new UserIdentity[sites.size()];
    int idx = 0;
    
    for (AuthnDomainMetadata metadata: AuthnDomainMetadata.getAllMetadata()) {
      username = request.getParameter("u" + idx);
      password = request.getParameter("pw" + idx);
      if (username != null && username.length() > 0 && password != null && password.length() > 0)
        identities[idx] = new UserIdentity(username, password, site);
      else
        identities[idx] = (oldIds == null ? null : oldIds[idx]);
      idx++;
    }
    return identities;
  }

}
