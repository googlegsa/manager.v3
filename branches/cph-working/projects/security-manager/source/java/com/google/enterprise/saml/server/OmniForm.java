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

import com.google.enterprise.sessionmanager.AuthnDomain;
import com.google.enterprise.sessionmanager.AuthnMechanism;
import com.google.enterprise.sessionmanager.CredentialsGroup;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

class OmniForm {
  private final String actionUrl;
  private StringBuffer formContent;
  
  public OmniForm(CSVReader reader, String actionUrl) throws NumberFormatException, IOException {
    String[] nextLine;
    
    // Each line is FQDN,realm,auth_method,optional_sample_login_url
    // If sample login is missing, we deduce as FQDN + realm
    while ((nextLine = reader.readNext()) != null) {
      AuthnMechanism method = Enum.valueOf(AuthnMechanism.class, nextLine[2]);
      AuthnDomain.compatAuthSite(nextLine[0], nextLine[1], method,
                                 nextLine.length > 3 ? nextLine[3] : null);
    }
    this.actionUrl = actionUrl;
  }

  private void writeHeader() {
    // We expect this form to be posted back to the same URL from which the form
    // was GETed, so skip "action" attribute. 
    formContent = new StringBuffer("<form method=\"post\" name=\"omni\" action=\"" +
                                   actionUrl +
                                   "\">\n");
  }
  private void writeFooter() {
    formContent.append("<input type=\"submit\"></form>");
  }
 
  /*
   * Each area on the omniform looks like:
   *  "Please login to %s@%s", AuthName, HTTPServerName
   *  "username", "input type=text name=u"
   *  "password", "input type=password name=pw"
   */
  private void writeArea(CredentialsGroup group, int idx) {
    String inputUserName = "u" + idx;
    String inputPassName = "pw" + idx;
    String inputStatus = "";
    if (group.isVerified()) {
      inputStatus = " disabled";
      formContent.append("<span style=\"color:green\">Logged in to ");
    } else {
      formContent.append("Please login to ");
    }
    formContent.append(group.getMetadata().getHumanName() + ":<br>\n");
    if (group.isVerified()) {
      formContent.append("</span>");
    }
    formContent.append("<b>Username</b> <input type=\"text\" name=" + inputUserName + inputStatus + "><br>");
    formContent.append("<b>Password</b> <input type=\"password\" name=" + inputPassName + inputStatus + "><br>");
    formContent.append("\n<br><br><br>\n");
  }

  /**
   * 
   * @param groups
   * @return the whole form suitable for display
   */
  public String writeForm(List<CredentialsGroup> groups) {
    int idx = 0;

    writeHeader();
    for (CredentialsGroup group: groups) {
      writeArea(group, idx);
      idx++;
    }
    writeFooter();
    return formContent.toString();
  }
  
  /**
   * Parse the form into an array of credentials awaiting authn decision.
   */
  public void parse(HttpServletRequest request, List<CredentialsGroup> groups) {
    String username;
    String password;
    int idx = 0;
    for (CredentialsGroup group: groups) {
      username = request.getParameter("u" + idx);
      password = request.getParameter("pw" + idx);
      if (username != null && username.length() > 0 && password != null && password.length() > 0) {
        group.setUsername(username);
        group.setPassword(password);
      }
      idx++;
    }
  }

}
