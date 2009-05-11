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

package com.google.enterprise.connector.security.ui;

import com.google.enterprise.connector.security.identity.CredentialsGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This class implements the Omniform processing logic.  Its primary job is to
 * translate the user input into valid CredentialsGroups.
 *
 * The OmniForm is also the class that contains the state of the user's login
 * session.
 */
public class OmniForm {

  private final OmniFormHtml omniHtml;
  private final List<FormElement> formElements;
  private final Map<FormElement,CredentialsGroup> formElemToCG;

  /**
   * TODO: ideally instead of OmniFormHtml we would pass in a UI interface.
   * I haven't figured out the right way to do so and keep the generate/submit
   * methods consistent for all UI implementations.  Perhaps we must restrict
   * UI implementations to ones that can be returned as HTTP content.
   */
  public OmniForm(List<CredentialsGroup> groups, OmniFormHtml omniFormHtml) {
    this.omniHtml = omniFormHtml;

    this.formElements = new ArrayList<FormElement>();
    this.formElemToCG = new HashMap<FormElement, CredentialsGroup>();

    for (CredentialsGroup group : groups) {
      FormElement formElem = new FormElement(group.getHumanName());
      formElements.add(formElem);
      formElemToCG.put(formElem, group);
    }
  }

  /**
   * Generate a login form that reflects the current state of this OmniForm.
   *
   * @return an html string
   */
  public String generateForm() {
    for (FormElement formElem : formElements) {
      formElem.setActive(!formElemToCG.get(formElem).isVerified());
    }
    return omniHtml.generateForm(formElements);
  }

  /**
   * Handles a submit of an OmniForm (a POST).
   *
   * Parses the POST parameters and fills in the associated credentials groups.
   *
   * This method assumes that the provided request is a POST of a form generated
   * by the same instance of this class.
   *
   * @param request The incoming submission.
   */
  public void handleFormSubmit(HttpServletRequest request) {
    omniHtml.parsePostedForm(request, formElements);
    // Update credentials groups with info from form
    for (FormElement elem: formElements) {
      if (elem.isActive()) {
        CredentialsGroup cg = formElemToCG.get(elem);
        cg.setUsername(elem.getUsername());
        cg.setPassword(elem.getPassword());
      }
    }
  }
}
