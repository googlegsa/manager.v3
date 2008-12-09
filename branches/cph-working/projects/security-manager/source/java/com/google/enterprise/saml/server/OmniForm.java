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

import com.google.enterprise.session.manager.AuthnDomain;
import com.google.enterprise.session.manager.AuthnMechanism;
import com.google.enterprise.session.manager.CredentialsGroup;

import java.io.IOException;
import java.util.List;

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
      AuthnDomain.compatAuthSite(nextLine[0], nextLine[1],
                                 Enum.valueOf(AuthnMechanism.class, nextLine[2]),
                                 (nextLine.length > 3) ? nextLine[3] : null);
    }
    this.actionUrl = actionUrl;
  }

  /**
   * 
   * @param groups
   * @return the whole form suitable for display
   */
  public String writeForm(List<CredentialsGroup> groups) {
    formContent = new StringBuffer("<form method=\"post\" name=\"omni\" action=\"" +
                                   actionUrl +
                                   "\">\n");
    int idx = 0;
    for (CredentialsGroup group: groups) {
      writeArea(group, idx);
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
  private void writeArea(CredentialsGroup group, int idx) {
    String inputStatus = "";
    if (group.isVerified()) {
      inputStatus = " disabled";
      formContent.append("<span style=\"color:green\">Logged in to " + group.getMetadata().getHumanName() + "</span>");
    } else {
      formContent.append("Please login to " + group.getMetadata().getHumanName());
    }
    formContent.append(":<br>\n");
    formContent.append("<b>Username</b> <input type=\"text\" name=u" + idx + inputStatus + "><br>\n");
    formContent.append("<b>Password</b> <input type=\"password\" name=pw" + idx + inputStatus + "><br>\n");
    formContent.append("<br><br><br>\n");
  }
  
  /**
   * Parse the form into an array of credentials awaiting authn decision.
   */
  public void parse(HttpServletRequest request, List<CredentialsGroup> groups) {
    int idx = 0;
    for (CredentialsGroup group: groups) {
      String username = request.getParameter("u" + idx);
      String password = request.getParameter("pw" + idx);
      if ((username != null) && (username.length() > 0) &&
          (password != null) && (password.length() > 0)) {
        group.setUsername(username);
        group.setPassword(password);
      }
      idx++;
    }
  }
}
