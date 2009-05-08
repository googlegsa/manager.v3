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

package com.google.enterprise.security.ui;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is responsible for generating the HTML for an Omniform.
 */
public class OmniFormHtml {

  Logger LOGGER = Logger.getLogger(OmniForm.class.getName());
  String actionUrl;

  public OmniFormHtml(String actionUrl) {
    this.actionUrl = actionUrl;
  }

  /**
   * Builds an HTML OmniForm from a given list of FormElements.  The order
   * in which the FormElements is presented corresponds to the order of the
   * input FormElement list.
   */
  public String generateForm(List<FormElement> formElements) {
    StringBuilder form = new StringBuilder();
    form.append(headerString(actionUrl));

    for (int i = 0; i < formElements.size(); i++) {
      form.append(singleFormArea(formElements.get(i), i));
    }

    form.append(footerString());
    return form.toString();
  }

  /**
   * Parses a POSTed OmniForm and updates the given list of FormElements to
   * reflect the input from the POST.  This method assumes that the FormElement
   * list is the same list that was used to generate the form in the POST
   * request.
   *
   * @param request a servlet request that POSTs a user-filled Omniform
   * @param formElements this must be the same list of FormElements as the one
   * that was used to generate the page from which the request was posted
   * @return an updated list of FormElements
   */
  public List<FormElement> parsePostedForm(HttpServletRequest request, List<FormElement> formElements) {
    for (int i = 0; i < formElements.size(); i++) {
      FormElement formElem = formElements.get(i);
      if (formElem.isActive()) {
        LOGGER.info("Retrieved username/password: " + request.getParameter("u"+i) +
                    "/#" + request.getParameter("pw"+i).hashCode());
        formElem.setUsername(request.getParameter("u" + i));
        formElem.setPassword(request.getParameter("pw" + i));
      }
    }
    return formElements;
  }

  /*
   * Each area on the omniform looks like:
   *  "Please login to %s@%s", AuthName, HTTPServerName
   *  "username", "input type=text name=u"
   *  "password", "input type=password name=pw"
   */
  private String singleFormArea(FormElement elem, int index) {
    StringBuilder formContent = new StringBuilder();
    String inputUserName = "u" + index;
    String inputPassName = "pw" + index;
    String inputStatus = "";
    if (elem.isActive()) {
      formContent.append("Please login to " + elem.getName() + ":<br>\n");
    } else {
      inputStatus = " disabled";
      formContent.append("<span style=\"color:green\">Logged in to " + elem.getName() + "<br>\n</span>");
    }
    formContent.append("<b>Username</b> <input type=\"text\" name=" + inputUserName + inputStatus + "><br>");
    formContent.append("<b>Password</b> <input type=\"password\" name=" + inputPassName + inputStatus + "><br>");
    formContent.append("\n<br><br><br>\n");

    return formContent.toString();
  }

  private String headerString(String actionUrl) {
    return
        "<html><head><title>Please login</title></head><body>\n"
        + "<form method=\"post\" name=\"omni\" action=\"" + actionUrl + "\">\n";
  }

  private String footerString() {
    return "<input type=\"submit\"></form></body></html>\n";
  }
}
