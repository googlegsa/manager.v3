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

import static com.google.enterprise.security.ui.OmniFormCustomization.FormGlobalOption;
import static com.google.enterprise.security.ui.OmniFormCustomization.FormGlobalOption.*;
import static com.google.enterprise.security.ui.OmniFormCustomization.PerCredentialOption;
import static com.google.enterprise.security.ui.OmniFormCustomization.PerCredentialOption.*;

import com.google.enterprise.common.FileUtil;
import com.google.enterprise.security.ui.FormElement;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is responsible for generating the HTML for an Omniform.
 *
 * TODO(martincochran): This UI generation should probably eventually evolve into
 * using XSLT/XML for the following reasons:
 * - In order to convey the active/inactive status of a credential group in the
 *   case where the user has decided to use the custom HTML override, it is
 *   necessary to insert an extra <style> tag between the <head> tags.  This
 *   is hackish.
 * - If a user uploads custom HTML and then adds a credential group, it will not
 *   be displayed.  If the customer can upload custom XSLT, however, then the
 *   user has the option of writing the XSLT in a manner that would handle
 *   the addition of a credential group.
 *
 */
public class OmniFormHtml {
  /**
   * This map is initialized with the default values above, and then
   * all customizations are inserted into the map.
   */
  private Map<FormGlobalOption, String> options =
      new HashMap<FormGlobalOption, String>();

  Logger LOGGER = Logger.getLogger(OmniForm.class.getName());


  /**
   * The URL which specifies the action of the form submission (the URL to
   * send the POST to).
   */
  String actionUrl;

  private String fileName = "OmniFormCustomization.conf";
  private BasicOmniFormCustomization customization;

  /**
   * Main constructor.
   * @param actionUrl URL to post form to on submission
   */
  public OmniFormHtml(String actionUrl) {
    this.actionUrl = actionUrl;
    customization =
        new BasicOmniFormCustomization(FileUtil.getContextFile(fileName).toString());
  }

  /**
   * Used for testing purposes.
   */
  OmniFormHtml(String actionUrl, BasicOmniFormCustomization customization) {
    this.actionUrl = actionUrl;
    this.customization = customization;
  }

  /**
   * Builds an HTML OmniForm from a given list of FormElements.  The order
   * in which the FormElements is presented corresponds to the order of the
   * input FormElement list.
   */
  public String generateForm(List<FormElement> formElements) {
    loadCustomization();

    if (!options.get(OVERRIDE_FORM_HTML).isEmpty()) {
      return insertStyleString(options.get(OVERRIDE_FORM_HTML),
          styleString(formElements));
    }

    StringBuilder form = new StringBuilder();
    form.append(headerString(actionUrl, formElements));
    form.append(options.get(PRE_CREDENTIAL_HTML));
    String prefix = options.get(PRE_CREDENTIAL_HTML);
    for (FormElement element : formElements) {
      form.append(prefix);
      form.append(singleFormArea(element));
      prefix = options.get(CREDENTIAL_SEPARATOR_HTML);
    }

    form.append(options.get(POST_CREDENTIAL_HTML));
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
        String username = request.getParameter(getInputUserName(formElem));
        String password = request.getParameter(getInputPassName(formElem));
        LOGGER.info("Retrieved username: " + username);

        // TODO(martincochran): Logging passwords is Bad.  This should be
        // cleaned up as soon as this feature is reasonably well-tested.
        LOGGER.info("Retrieved password: " + password);
        formElem.setUsername(username);
        formElem.setPassword(password);
      }
    }
    return formElements;
  }

  /*
   * Generates a form area for a single credential group.  The text displayed
   * and a few other options are customizable via the OmniFormCustomization
   * interface.
   */
  String singleFormArea(FormElement elem) {
    Map<PerCredentialOption, String> locals =
        customization.getCredentialGroupOptions(elem.getName());
    StringBuilder formContent = new StringBuilder();

    String introText = "Please login to " + elem.getName() + ":";
    String usernameFieldTitle = options.get(DEFAULT_USERNAME_FIELD_TITLE);
    String passwordFieldTitle = options.get(DEFAULT_PASSWORD_FIELD_TITLE);
    String usernameEntryWidth = options.get(DEFAULT_USERNAME_ENTRY_WIDTH);
    String passwordEntryWidth = options.get(DEFAULT_PASSWORD_ENTRY_WIDTH);

    if (!options.get(DEFAULT_INTRO_TEXT).isEmpty()) {
      introText = options.get(DEFAULT_INTRO_TEXT);
    }
    if (locals.containsKey(INTRO_TEXT)) {
      introText = locals.get(INTRO_TEXT);
    }
    if (locals.containsKey(USERNAME_FIELD_TITLE)) {
      usernameFieldTitle = locals.get(USERNAME_FIELD_TITLE);
    }
    if (locals.containsKey(PASSWORD_FIELD_TITLE)) {
      passwordFieldTitle = locals.get(PASSWORD_FIELD_TITLE);
    }
    if (locals.containsKey(USERNAME_ENTRY_WIDTH)) {
      usernameEntryWidth = locals.get(USERNAME_ENTRY_WIDTH);
    }
    if (locals.containsKey(PASSWORD_ENTRY_WIDTH)) {
      passwordEntryWidth = locals.get(PASSWORD_ENTRY_WIDTH);
    }

    formContent.append("<table style=\"text-align: center;\">\n");
    formContent.append("<tbody>\n");
    formContent.append("<tr>\n");
    formContent.append("<td>\n");

    String inputStatus = elem.isActive() ? "" : " disabled";
    String id1 = getActiveId(elem.getName());
    String id2 = getInactiveId(elem.getName());

    formContent.append("<tr id=\"" + id1 + "\">\n");
    formContent.append("<td>\n");
    formContent.append(introText + "\n");
    formContent.append("</td>\n");
    formContent.append("</tr>\n");

    formContent.append("<tr id=\"" + id2 + "\">\n");
    formContent.append("<td>\n");
    formContent.append("<span style=\"color:green\">Logged in to ");
    formContent.append(elem.getName());
    formContent.append("</span>\n");
    formContent.append("</td>\n");
    formContent.append("</tr>\n");

    formContent.append("<tr>\n");
    formContent.append("<td>\n");
    formContent.append("<table>\n");
    formContent.append("<tbody>\n");

    formContent.append("<tr>\n");
    formContent.append("<td>\n");
    formContent.append("<b>" + usernameFieldTitle + "</b>\n");
    formContent.append("</td>\n");
    formContent.append("<td>\n");
    formContent.append("<input style=\"width: ");
    formContent.append(usernameEntryWidth + ";\" ");
    formContent.append("type=\"text\" name=");
    formContent.append(getInputUserName(elem) + inputStatus + ">\n");
    formContent.append("</td>\n");
    formContent.append("</tr>\n");

    formContent.append("<tr>\n");
    formContent.append("<td>\n");
    formContent.append("<b>" + passwordFieldTitle + "</b>\n");
    formContent.append("</td>");
    formContent.append("<td>\n");
    formContent.append("<input type=\"password\" width=");
    formContent.append("<input style=\"width: ");
    formContent.append(passwordEntryWidth + ";\" ");
    formContent.append("type=\"password\" name=");
    formContent.append(getInputPassName(elem) + inputStatus + ">\n");
    formContent.append("</td>\n");
    formContent.append("</tr>\n");

    formContent.append("</tbody>\n");
    formContent.append("</table>\n");
    formContent.append("</td>\n");
    formContent.append("</tr>\n");
    formContent.append("</tbody>\n");
    formContent.append("</table>\n");
    return formContent.toString();
  }

  /**
   * Generates the customizable HTML that should be placed before the main
   * form code.
   */
  String headerString(String actionUrl, List<FormElement> groups) {
    StringBuilder header = new StringBuilder();
    header.append("<html>\n");
    header.append("<head>\n");
    header.append("<title>" + options.get(PAGE_TITLE) + "</title>\n");
    header.append(styleString(groups));
    header.append(options.get(HEADER_HTML));
    header.append(options.get(SUBMIT_BUTTON_JAVASCRIPT));
    header.append("</head>\n");

    header.append("<body>\n");

    // Display the logo and title.
    header.append("<table cellpadding=\"0\" cellspacing=\"0\"" +
        "width=\"100%\">\n");
    header.append("<tbody>\n");
    header.append("<tr>\n");
    header.append("<td>\n");
    header.append("<img style=\"height: " + options.get(LOGO_HEIGHT) +
        "; width: " + options.get(LOGO_WIDTH) + "; \"alt=\"Logo\" " + "src=\"" +
        options.get(LOGO_URL) + "\">\n");
    header.append("</td>\n");
    header.append("<td style=\"text-align: right;\">" +
        options.get(PAGE_TITLE) + "</td>\n");
    header.append("</tr>\n");
    header.append("</tbody>\n");
    header.append("</table>\n");
    header.append("<hr style=\"width: 100%; height: 2px;\">\n");

    // Begin the form.
    header.append("<form method=\"post\" name=\"omni\" action=\""
        + actionUrl + "\" ");

    // Insert the javascript, if applicable.
    String javascript = options.get(SUBMIT_BUTTON_JAVASCRIPT);
    if (!javascript.isEmpty()) {
      header.append("onsubmit=\"" + options.get(ONSUBMIT_FUNCTION_NAME) + "\"");
    }
    header.append(">\n");
    return header.toString();
  }

  /**
   * Returns a string which may be included in the HTML header to
   * guide how the page is displayed.  This style string is included in
   * all forms, including forms where OVERRIDE_FORM_HTML is specified.
   * This enables custom forms to be able to handle cases where a credential
   * group is already authenticated and to display the form accordingly.
   */
  String styleString(List<FormElement> groups) {
    StringBuilder style = new StringBuilder();
    style.append("<style type=\"text/css\">\n");
    style.append("body\n");
    style.append("{\n");
    style.append("font: " + options.get(FONT_SPECIFICATION) + "\n");
    style.append("}\n");

    style.append("<!--\n");
    for (FormElement elem : groups) {
      String display = elem.isActive() ? "inline" : "none";
      style.append("#" + getActiveId(elem.getName()) +
          " {display:" + display + "; }\n");

      display = elem.isActive() ? "none" : "inline";
      style.append("#" + getInactiveId(elem.getName()) +
          " {display:" + display + "; }\n");
    }
    style.append("-->\n");
    style.append("</style>");
    return style.toString();
  }

  /**
   * Loads the customization configuration from the OmniFormCustomization.
   * Marked package-private for testing purposes.
   */
  void loadCustomization() {
    // TODO(martincochran): implement a file-change listener in
    // BasicOmniFormCustomization so that it isn't necessary to do this.
    // When this is done, change customization to be of type
    // OmniFormCustomization.
    customization.readConfig();
    initializeOptions();

    // Override default values with customizations.
    options.putAll(customization.getGlobalOptions());
  }

  private String footerString() {
    return "<center><input type=\"submit\" value=\"" + options.get(SUBMIT_BUTTON_TEXT)
        + "\"/></center></form>\n" + options.get(FOOTER_HTML);
  }

  /**
   * Insert the style string in the OVERRIDE_FORM_HTML string so that
   * the form can properly handle credentials that have already been
   * authenticated.
   */
  private String insertStyleString(String html, String style) {
    int index = html.indexOf("<head>");
    if (index == -1) {
      LOGGER.warning("Could not find string '<head>' in custom html. " +
                     "As a result the style string could not be inserted.");
      return html;
    }
    return html.substring(0, index + 6) + style + html.substring(index + 6);
  }

  private String getInputUserName(FormElement element) {
    return "u" + element.getName();
  }

  private String getInputPassName(FormElement element) {
    return "pw" + element.getName();
  }

  /**
   * @param name name of group - cannot have any spaces
   */
  private String getActiveId(String name) {
    return name + "Active";
  }

  /**
   * @param name name of group - cannot have any spaces
   */
  private String getInactiveId(String name) {
    return name + "Inactive";
  }

  private void initializeOptions() {
    options = new HashMap<FormGlobalOption, String>();
    for (FormGlobalOption option : FormGlobalOption.values()) {
      options.put(option, option.getDefaultValue());
    }
  }
}
