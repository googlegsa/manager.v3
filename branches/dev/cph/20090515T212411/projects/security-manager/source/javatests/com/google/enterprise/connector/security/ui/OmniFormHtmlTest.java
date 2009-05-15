// Copyright 2009 Google Inc.
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

import com.google.enterprise.connector.security.ui.OmniFormCustomization.FormGlobalOption;
import com.google.enterprise.connector.security.ui.OmniFormCustomization.PerCredentialOption;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.enterprise.connector.security.ui.OmniFormCustomization.FormGlobalOption.*;
import static com.google.enterprise.connector.security.ui.OmniFormCustomization.PerCredentialOption.*;

/**
 * Tests the basic HTML output of the omniform works with and
 * without customization.
 *
 * TODO(martincochran): it would be better to write the test assertions
 * using an HTML parser to get better/more information on the produced
 * document's structure.
 */
public class OmniFormHtmlTest extends TestCase {

  private static final String kCredentialGroup = "Credential_Group";

  private static final String kCustomJavascript =
      "<script> function welcome() { alert() } </script>";

  private static final String kCustomOnSubmitName = "welcome()";

  private static final String kCustomUsernameFieldTitle =
      "Default Username Title";
  private static final String kCustomPasswordFieldTitle =
      "Default Password Title";
  private static final String kCustomUsernameEntryWidth = "101px";
  private static final String kCustomPasswordEntryWidth = "102px";
  private static final String kCustomLogoUrl = "custom_url.png";
  private static final String kCustomLogoWidth = "11px";
  private static final String kCustomLogoHeight = "12px";
  private static final String kCustomTitle = "CustomTitle";
  private static final String kCustomHeaderHtml =
      "<!-- custom header -->";
  private static final String kCustomFooterHtml =
      "<!-- custom footer -->";
  private static final String kCustomPreCredentialHtml =
      "<!-- pre-credential-->";
  private static final String kCustomPostCredentialHtml =
      "<!-- post-credential -->";
  private static final String kCustomSubmitButtonText = "Boom!";
  private static final String kCustomIntroText =
      "Custom intro text";
  private static final String kCustomFont =
      "italic small-caps 900 12px arial";

  private static final String kCredentialIntroText =
      "Per-credential intro text";
  private static final String kCredentialUsernameFieldTitle = "User1";
  private static final String kCredentialUsernameEntryWidth = "98px";
  private static final String kCredentialPasswordFieldTitle = "Pass1";
  private static final String kCredentialPasswordEntryWidth = "99px";

  private OmniFormHtml omniFormHtml;
  private BasicOmniFormCustomization customization;
  private Map<FormGlobalOption, String> globals;
  private Map<PerCredentialOption, String> locals;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    customization = new BasicOmniFormCustomization();
    omniFormHtml = new OmniFormHtml("submit", customization);
    initializeOptions();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  // Test that a page with empty forms can be generated with
  // correct-looking html.
  public void testDefaultHtml() throws IOException {
    List<FormElement> empty = Collections.emptyList();
    String form = omniFormHtml.generateForm(empty);

    // Check for the default customization options.
    assertFalse("Default title not found",
        -1 == form.indexOf("Google Search Appliance Universal Login Form"));
    assertFalse("Default logo URL not found",
        -1 == form.indexOf("Title_Left.png"));
    assertFalse("Default logo width not found",
        -1 == form.indexOf("200px"));
    assertFalse("Default logo width not found",
        -1 == form.indexOf("78px"));
    assertFalse("Default header not found",
        -1 == form.indexOf("meta content"));
    assertFalse("Default footer not found",
        -1 == form.indexOf("</body>"));
    assertFalse("Default pre-credential html not found",
        -1 == form.indexOf("table border"));
    assertFalse("Default post-credential html not found",
        -1 == form.indexOf("</tbody>\n</table>"));
    assertFalse("Default submit button text not found",
        -1 == form.indexOf("Login"));
    assertFalse("Default font specification not found",
        -1 == form.indexOf("font: normal"));
  }

  /**
   * Test that the global customizations show up in the page.
   */
  public void testCustomHtml() throws IOException {
    List<FormElement> empty = Collections.emptyList();

    customization.setGlobalOptions(globals);
    omniFormHtml.loadCustomization();
    String form = omniFormHtml.generateForm(empty);

    // Check for the customized options.
    assertFalse("Custom title not found",
        -1 == form.indexOf(kCustomTitle));
    assertFalse("Custom logo URL not found",
        -1 == form.indexOf(kCustomLogoUrl));
    assertFalse("Custom logo height not found",
        -1 == form.indexOf(kCustomLogoWidth));
    assertFalse("Custom logo height not found",
        -1 == form.indexOf(kCustomLogoHeight));
    assertFalse("Custom header not found",
        -1 == form.indexOf(kCustomHeaderHtml));
    assertFalse("Custom footer not found",
        -1 == form.indexOf(kCustomFooterHtml));
    assertFalse("Custom pre-credential html not found",
        -1 == form.indexOf(kCustomPreCredentialHtml));
    assertFalse("Custom post-credential html not found",
        -1 == form.indexOf(kCustomPostCredentialHtml));
    assertFalse("Custom submit button text not found",
        -1 == form.indexOf(kCustomSubmitButtonText));
    assertFalse("Custom font specification not found",
        -1 == form.indexOf(kCustomFont));
    assertFalse("Custom submit button javascript not found",
        -1 == form.indexOf(kCustomJavascript));

  }

  /**
   * Test the default HTML is generated correctly for a form area.
   */
  public void testDefaultSingleFormArea() throws IOException {
    FormElement element = new FormElement(kCredentialGroup);

    omniFormHtml.loadCustomization();

    String formArea = omniFormHtml.singleFormArea(element);
    assertFalse("Default intro text not found",
        -1 == formArea.indexOf("Please login to"));
    assertFalse("Default username field title not found",
        -1 == formArea.indexOf("Username:"));
    assertFalse("Default username width not found",
        -1 == formArea.indexOf("100px"));
    assertFalse("Default password field title not found",
        -1 == formArea.indexOf("Password:"));
    assertFalse("Default password widthnot found",
        -1 == formArea.indexOf("100px"));
  }

  /**
   * Test that the global customizations show up when no
   * per-credential customizations are used.
   */
  public void testGlobalCustomSingleFormArea() throws IOException {
    FormElement element = new FormElement(kCredentialGroup);

    customization.setGlobalOptions(globals);
    omniFormHtml.loadCustomization();

    String formArea = omniFormHtml.singleFormArea(element);
    assertFalse("Global intro text not found",
        -1 == formArea.indexOf(kCustomIntroText));
    assertFalse("Global username field title not found",
        -1 == formArea.indexOf(kCustomUsernameFieldTitle));
    assertFalse("Default username width not found",
        -1 == formArea.indexOf(kCustomUsernameEntryWidth));
    assertFalse("Default password field title not found",
        -1 == formArea.indexOf(kCustomPasswordFieldTitle));
    assertFalse("Default password widthnot found",
        -1 == formArea.indexOf(kCustomPasswordEntryWidth));
  }

  /**
   * Test that the local customizations show up when used.
   */
  public void testGlobalCustomSingleFormAreaWithLocalOverride() throws IOException {
    FormElement element = new FormElement(kCredentialGroup);

    customization.setGlobalOptions(globals);
    customization.setCredentialGroupOptions(kCredentialGroup, locals);
    omniFormHtml.loadCustomization();

    String formArea = omniFormHtml.singleFormArea(element);
    assertFalse("Per-credential intro text not found",
        -1 == formArea.indexOf(kCredentialIntroText));
    assertFalse("Per-credential username field title not found",
        -1 == formArea.indexOf(kCredentialUsernameFieldTitle));
    assertFalse("Per-credential username width not found",
        -1 == formArea.indexOf(kCredentialUsernameEntryWidth));
    assertFalse("Per-credential password field title not found",
        -1 == formArea.indexOf(kCredentialPasswordFieldTitle));
    assertFalse("Per-credential password widthnot found",
        -1 == formArea.indexOf(kCredentialPasswordEntryWidth));
  }

  /**
   * Verify that the css style string is generated correctly.  This string
   * will be used to determine which elements are displayed and which
   * are not.
   */
  public void testStyleString() {
    List<FormElement> elements = new LinkedList<FormElement>();
    FormElement element = new FormElement(kCredentialGroup + "_1");
    element.setActive(false);
    elements.add(element);
    element = new FormElement(kCredentialGroup + "_2");
    elements.add(element);

    String styleString = omniFormHtml.styleString(elements);
    assertFalse("Active style for inactive group incorrectly set",
        -1 == styleString.indexOf("#" +
        kCredentialGroup + "_1Active {display:none; }"));
    assertFalse("Inactive style for inactive group incorrectly set",
        -1 == styleString.indexOf("#" +
        kCredentialGroup + "_1Inactive {display:inline; }"));
    assertFalse("Active style for active group incorrectly set",
        -1 == styleString.indexOf("#" +
        kCredentialGroup + "_2Active {display:inline; }"));
    assertFalse("Inactive style for active group incorrectly set",
        -1 == styleString.indexOf("#" +
        kCredentialGroup + "_2Inactive {display:none; }"));
  }

  /**
   * Test that the default header string is generated correctly.
   */
  public void testDefaultHeaderString() throws IOException {
    List<FormElement> empty = Collections.emptyList();

    omniFormHtml.loadCustomization();

    String headerString = omniFormHtml.headerString("action", empty);
    assertFalse("Default header html not found",
        -1 == headerString.indexOf("table cellpadding"));
    assertTrue("No script should be present in default form",
        -1 == headerString.indexOf("<script>"));
  }

  /**
   * Test that javascript is correctly inserted into the
   * header string.
   */
  public void testDefaultHeaderStringWithJavascript() throws IOException {
    List<FormElement> empty = Collections.emptyList();

    globals.put(SUBMIT_BUTTON_JAVASCRIPT, kCustomJavascript);
    customization.setGlobalOptions(globals);
    omniFormHtml.loadCustomization();

    String headerString = omniFormHtml.headerString("action", empty);
    assertFalse("Script not found in customized form",
        -1 == headerString.indexOf("<script>"));
    assertFalse("Javascript function name not correctly parsed",
        -1 == headerString.indexOf("onsubmit=\"welcome()\""));

  }

  /**
   * Tests that the style string is correctly inserted into the form
   * in cases where OVERRIDE_FORM_HTML is set.
   */
  public void testInsertStyleString() throws IOException {
    String goodHtml = "<html><head>";
    String styleString = "<style type=\"text/css\">\n" +
        "body\n{\nfont: " + kCustomFont + "\n}\n" +
        "<!--\n-->\n</style>";
    String expected = goodHtml + styleString;

    List<FormElement> empty = Collections.emptyList();
    globals.put(OVERRIDE_FORM_HTML, goodHtml);
    customization.setGlobalOptions(globals);
    omniFormHtml.loadCustomization();

    assertEquals("Style string not inserted correctly",
        expected, omniFormHtml.generateForm(empty));

    String badHtml = "<html>";
    globals.put(OVERRIDE_FORM_HTML, badHtml);
    customization.setGlobalOptions(globals);
    omniFormHtml.loadCustomization();
    assertEquals("Style string not inserted correctly",
        badHtml, omniFormHtml.generateForm(empty));
  }

  /**
   * Set the customization options for both global options and
   * per-credential options.
   */
  private void initializeOptions() {
    globals = new HashMap<FormGlobalOption, String>();
    globals.put(DEFAULT_USERNAME_FIELD_TITLE, kCustomUsernameFieldTitle);
    globals.put(DEFAULT_USERNAME_ENTRY_WIDTH, kCustomUsernameEntryWidth);
    globals.put(DEFAULT_PASSWORD_FIELD_TITLE, kCustomPasswordFieldTitle);
    globals.put(DEFAULT_PASSWORD_ENTRY_WIDTH, kCustomPasswordEntryWidth);
    globals.put(DEFAULT_INTRO_TEXT, kCustomIntroText);
    globals.put(FONT_SPECIFICATION, kCustomFont);
    globals.put(LOGO_URL, kCustomLogoUrl);
    globals.put(LOGO_WIDTH, kCustomLogoWidth);
    globals.put(LOGO_HEIGHT, kCustomLogoHeight);
    globals.put(PAGE_TITLE, kCustomTitle);
    globals.put(HEADER_HTML, kCustomHeaderHtml);
    globals.put(FOOTER_HTML, kCustomFooterHtml);
    globals.put(PRE_CREDENTIAL_HTML, kCustomPreCredentialHtml);
    globals.put(POST_CREDENTIAL_HTML, kCustomPostCredentialHtml);
    globals.put(SUBMIT_BUTTON_TEXT, kCustomSubmitButtonText);
    globals.put(SUBMIT_BUTTON_JAVASCRIPT, kCustomJavascript);
    globals.put(ONSUBMIT_FUNCTION_NAME, kCustomOnSubmitName);

    locals = new HashMap<PerCredentialOption, String>();
    locals.put(INTRO_TEXT, kCredentialIntroText);
    locals.put(USERNAME_FIELD_TITLE, kCredentialUsernameFieldTitle);
    locals.put(USERNAME_ENTRY_WIDTH, kCredentialUsernameEntryWidth);
    locals.put(PASSWORD_FIELD_TITLE, kCredentialPasswordFieldTitle);
    locals.put(PASSWORD_ENTRY_WIDTH, kCredentialPasswordEntryWidth);
  }
}
