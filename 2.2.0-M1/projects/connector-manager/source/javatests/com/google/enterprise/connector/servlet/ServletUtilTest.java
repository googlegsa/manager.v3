// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.common.SecurityUtils;
import com.google.enterprise.connector.spi.XmlUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class ServletUtilTest extends TestCase {
  private static final String HIDE_KEY_ONE = "PasswordOne";
  private static final String HIDE_KEY_TWO = "a_password_two";
  private static final String HIDE_KEY_THREE = "imapasswordtoo";
  private static final String CLEAR_KEY_ONE = "NotAPwd";

  public void testPrependCmPrefix() {
    onePrependTest("foo name=\"bar\" bar", "foo name=\"CM_bar\" bar");
    onePrependTest("name=\"bar\"", "name=\"CM_bar\"");
    onePrependTest("name=\"bar\" name=\"baz\"", "name=\"CM_bar\" name=\"CM_baz\"");
    onePrependTest("name='bar' name=\"baz\"", "name='CM_bar' name=\"CM_baz\"");
    onePrependTest("name = 'bar'   name   =  \"baz\"",
        "name = 'CM_bar'   name   =  \"CM_baz\"");
  }

  private void onePrependTest(String original, String expected) {
    String result = ServletUtil.prependCmPrefix(original);
    Assert.assertEquals(expected, result);
    Assert.assertEquals(original, ServletUtil.stripCmPrefix(result));
  }

  public void testEmptyTextArea() throws Exception {
    // Create form with character entities.
    String configForm =
        "<tr>"
        + "<td>Sensitive input to force parsing</td>"
        + "<td><input name=\"Password\" type=\"password\" value=\"protected\"/></td>"
        + "</tr>"
        + "<tr>"
        + "<td>Sample text</td>"
        + "<td><textarea cols=\"50\" name=\"SampleText\" rows=\"5\"></textarea></td>"
        + "</tr>";
    String expectedForm =
        "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">Sensitive input to force parsing</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"Password\" type=\"password\" value=\"*********\"></td>"
        + "</tr>"
        + "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">Sample text</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><textarea cols=\"50\" name=\"SampleText\" rows=\"5\"></textarea></td>"
        + "</tr>";
    addDtdToClassLoader();
    String obfuscateForm = ServletUtil.filterSensitiveData(configForm);
    assertNotNull("Form returned", obfuscateForm);
    assertEquals("Form changed as expected", expectedForm, obfuscateForm);
  }

  public void testRemoveNestedMarkers() {
    String formWithMarkers =
        "<script language=\"JavaScript\" type=\"text/javascript\">"
        + "//<![CDATA["
        + "  function foo() {"
        + "    alert('foo');"
        + "  }"
        + "//]]>"
        + "</script>";
    String expectedFormWithMarkers =
        "<script language=\"JavaScript\" type=\"text/javascript\">"
        + "  function foo() {"
        + "    alert('foo');"
        + "  }"
        + "</script>";
    String formWithSimilarMarkers =
        "<script language=\"JavaScript\" type=\"text/javascript\">"
        + "<![CDATA["
        + "  function foo() {"
        + "    alert('foo');"
        + "  }"
        + "]]>"
        + "</script>";
    String expectedFormWithSimilarMarkers =
        "<script language=\"JavaScript\" type=\"text/javascript\">"
        + "  function foo() {"
        + "    alert('foo');"
        + "  }"
        + "</script>";
    String formWithoutMarkers =
        "<script language=\"JavaScript\" type=\"text/javascript\">"
        + "function foo() {"
        + "  alert('foo');"
        + "}"
        + "</script>";
    String result = ServletUtil.removeNestedMarkers(formWithMarkers);
    assertEquals("Form with markers cleaned up",
        expectedFormWithMarkers, result);
    result = ServletUtil.removeNestedMarkers(formWithSimilarMarkers);
    assertEquals("Form with similar markers cleaned up",
        expectedFormWithSimilarMarkers, result);
    result = ServletUtil.removeNestedMarkers(formWithoutMarkers);
    assertEquals("Form without markers left alone",
        formWithoutMarkers, result);
  }

  public void testObfuscateForm() throws Exception {
    // Create simple form.
    String protectedValue = "protected";
    String clearValue = "clear";
    Map<String, String> configMap = new HashMap<String, String>();
    configMap.put(HIDE_KEY_ONE, protectedValue);
    configMap.put(HIDE_KEY_TWO, protectedValue);
    configMap.put(HIDE_KEY_THREE, protectedValue);
    configMap.put(CLEAR_KEY_ONE, clearValue);
    String configForm = makeConfigForm(configMap);

    // Filter out sensitive data.
    addDtdToClassLoader();
    String obfuscatedForm = ServletUtil.filterSensitiveData(configForm);
    assertNotNull("Form returned", obfuscatedForm);
    assertTrue("Form does not contain protected values",
        obfuscatedForm.indexOf(protectedValue) == -1);
    assertTrue("Form still contains clear values",
        obfuscatedForm.indexOf(clearValue) != -1);

    // Test exception cases.
    configForm = configForm.substring(1);
    obfuscatedForm = ServletUtil.filterSensitiveData(configForm);
    assertNull("Null form returned when form invalid", obfuscatedForm);
  }

  public void testObfuscateEvilForm() throws Exception {
    // Create form with radio buttons with sensitive names.
    String sensitiveName = "doPasswordCheck";
    assertTrue("name is still considered sensitive",
        SecurityUtils.isKeySensitive(sensitiveName));
    String configForm = "<tr>\n"
      + "<td>Password Check</td>\n"
      + "<td><input type=\"radio\" name=\"" + sensitiveName + "\" "
      +            "id=\"doPasswordCheck-true\" value=\"true\"/>\n"
      + "<label for=\"doPasswordCheck-true\">True</label><br/>\n"
      + "<input type=\"radio\" name=\"" + sensitiveName + "\" "
      +        "id=\"doPasswordCheck-false\" value=\"false\" "
      +        "checked=\"checked\"/>\n"
      + "<label for=\"doPasswordCheck-false\">False</label><br/>\n"
      + "</td>\n"
      + "</tr>";
    addDtdToClassLoader();
    String obfuscateForm = ServletUtil.filterSensitiveData(configForm);
    assertNotNull("Form returned", obfuscateForm);
    assertEquals("Form not changed", configForm, obfuscateForm);
  }

  public void testObfuscateFormWithEntities() throws Exception {
    // Create form with character entities.
    String configForm =
        "<tr>"
        + "<td>Sensitive input to force parsing</td>"
        + "<td><input name=\"Password\" type=\"password\" value=\"protected\"/></td>"
        + "</tr>"
        + "<tr>"
        + "<td>HTML and XML &amp; &lt;</td>"
        + "<td><input name=\"HtmlAndXml\" type=\"text\" value=\"clear\"/></td>"
        + "</tr>"
        + "<tr>"
        + "<td>Some&nbsp;of&#160;the&#xA0;other 252 &copy; &#169; &#xA9;</td>"
        + "<td><input name=\"Other252\" type=\"text\" value=\"clear\"/></td>"
        + "</tr>"
        + "<tr>"
        + "<td>Value has non-252 but needs to be preserved</td>"
        + "<td><input name=\"ValueHas\" type=\"text\" value=\"clear1&#10;clear2&#xA;clear3\"/></td>"
        + "</tr>"
        + "<tr>"
        + "<td>Two words</td>"
        + "<td><textarea rows='5' cols='40' name='url'>"
        + "http://www.example.com/doc?a=b&amp;c=d"
        + "</textarea></td>"
        + "</tr>"
        + "<tr>"
        + "<td>Two words</td>"
        + "<td><textarea rows='5' cols='40' name='quote'>"
        + "Is that a &dagger; I see before me?"
        + "</textarea></td>"
        + "</tr>";
    String expectedForm =
        "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">Sensitive input to force parsing</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"Password\" type=\"password\" value=\"*********\"></td>"
        + "</tr>"
        + "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">HTML and XML &amp; &lt;</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"HtmlAndXml\" type=\"text\" value=\"clear\"></td>"
        + "</tr>"
        + "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">Some&nbsp;of&nbsp;the&nbsp;other 252 &copy; &copy; &copy;</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"Other252\" type=\"text\" value=\"clear\"></td>"
        + "</tr>"
        + "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">Value has non-252 but needs to be preserved</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"ValueHas\" type=\"text\" value=\"clear1\nclear2\nclear3\"></td>"
        + "</tr>"
        + "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">Two words</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><textarea cols=\"40\" name=\"url\" rows=\"5\">"
        + "http://www.example.com/doc?a=b&amp;c=d"
        + "</textarea></td>"
        + "</tr>"
        + "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">Two words</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><textarea cols=\"40\" name=\"quote\" rows=\"5\">"
        + "Is that a &dagger; I see before me?"
        + "</textarea></td>"
        + "</tr>";
    addDtdToClassLoader();
    String obfuscateForm = ServletUtil.filterSensitiveData(configForm);
    assertNotNull("Form returned", obfuscateForm);
    assertEquals("Form changed as expected", expectedForm, obfuscateForm);
  }

  public void testObfuscateFormWithScript() throws Exception {
    // Create form with JavaScript.
    String configForm =
        "<script language=\"JavaScript\" type=\"text/javascript\">"
        + "<![CDATA["
        + "  function checkSelect() {"
        + "    var opt = document.getElementById('Version');"
        + "    if (opt == 'version1') {"
        + "      alert('Version1 Selected');"
        + "    } else {"
        + "      alert('Version1 Not Selected');"
        + "    }"
        + "  }"
        + "]]>"
        + "</script>"
        + "<tr>"
        + "<td>Sensitive input to force parsing</td>"
        + "<td><input name=\"Password\" type=\"password\" value=\"protected\"/></td>"
        + "</tr>"
        + "<tr>"
        + "<td><div style='float: left;'>Select Version</div></td>"
        + "<td><select id=\"SPType\" name=\"Version\" size=\"1\" onchange=\"checkSelect();\">"
        + "  <option selected=\"\" value=\"version1\">Version 1</option>"
        + "  <option value=\"version2\">Version 2</option>"
        + "</select></td>"
        + "</tr>";
    String expectedForm =
        "<script language=\"JavaScript\" type=\"text/javascript\" xml:space=\"preserve\">"
        + "  function checkSelect() {"
        + "    var opt = document.getElementById('Version');"
        + "    if (opt == 'version1') {"
        + "      alert('Version1 Selected');"
        + "    } else {"
        + "      alert('Version1 Not Selected');"
        + "    }"
        + "  }"
        + "</script>"
        + "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\">Sensitive input to force parsing</td>"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"Password\" type=\"password\" value=\"*********\"></td>"
        + "</tr>"
        + "<tr>"
        + "<td colspan=\"1\" rowspan=\"1\"><div style=\"float: left;\">Select Version</div></td>"
        + "<td colspan=\"1\" rowspan=\"1\"><select id=\"SPType\" name=\"Version\" onchange=\"checkSelect();\" size=\"1\">"
        + "  <option selected value=\"version1\">Version 1</option>"
        + "  <option value=\"version2\">Version 2</option></select>"
        + "</td>"
        + "</tr>";
    addDtdToClassLoader();
    String obfuscateForm = ServletUtil.filterSensitiveData(configForm);
    assertNotNull("Form returned", obfuscateForm);
    assertEquals("Form changed as expected", expectedForm, obfuscateForm);
  }

  public void testObfuscateTools() {
    String baseClearValue = "this is open";
    String baseObfuscatedValue = "************";
    String clear;
    String obfuscated;

    // Simple cycle.
    clear = baseClearValue;
    obfuscated = ServletUtil.obfuscateValue(clear);
    assertEquals("clear was not changed", baseClearValue, clear);
    assertEquals("string was obfuscated", baseObfuscatedValue, obfuscated);
    assertTrue("obfuscated string recognized",
        ServletUtil.isObfuscated(obfuscated));

    // isObfuscated corner cases.
    assertFalse(ServletUtil.isObfuscated("***n***"));
    assertFalse(ServletUtil.isObfuscated("***n"));
    assertFalse(ServletUtil.isObfuscated("n***"));

    // obfuscateValue corner cases.
    assertEquals(baseObfuscatedValue,
        ServletUtil.obfuscateValue("1234 56 7890"));
    assertEquals(baseObfuscatedValue,
        ServletUtil.obfuscateValue("-+=< >^ ()[]"));
    assertEquals(baseObfuscatedValue,
        ServletUtil.obfuscateValue("ABCD EF GHIJ"));
    assertEquals(baseObfuscatedValue,
        ServletUtil.obfuscateValue("**** &@ !#$%"));
  }

  public void testReplaceSensitiveData() {
    String clearValue = "clear value";
    Map<String, String> clearConfig = new HashMap<String, String>();
    clearConfig.put(HIDE_KEY_ONE, clearValue);
    clearConfig.put(HIDE_KEY_TWO, clearValue);
    clearConfig.put(HIDE_KEY_THREE, clearValue);
    clearConfig.put(CLEAR_KEY_ONE, clearValue);

    // Let's just obfuscate all the ones that should be and then revert them.
    String obfuscatedValue = "***********";
    Map<String, String> obfuscatedConfig = new HashMap<String, String>();
    obfuscateValues(clearConfig, obfuscatedConfig);
    assertEquals(obfuscatedValue, obfuscatedConfig.get(HIDE_KEY_ONE));
    assertEquals(obfuscatedValue, obfuscatedConfig.get(HIDE_KEY_TWO));
    assertEquals(obfuscatedValue, obfuscatedConfig.get(HIDE_KEY_THREE));
    assertEquals(clearValue, obfuscatedConfig.get(CLEAR_KEY_ONE));
    ServletUtil.replaceSensitiveData(obfuscatedConfig, clearConfig);
    assertEquals(clearValue, obfuscatedConfig.get(HIDE_KEY_ONE));
    assertEquals(clearValue, obfuscatedConfig.get(HIDE_KEY_TWO));
    assertEquals(clearValue, obfuscatedConfig.get(HIDE_KEY_THREE));
    assertEquals(clearValue, obfuscatedConfig.get(CLEAR_KEY_ONE));

    // Now let's obfuscate and change some of the values to make sure the
    // new values are preserved.
    String newValueOne = "new nice value";
    String newValueTwo = "******n******";
    String evilValue = "***";
    obfuscatedConfig.clear();
    obfuscateValues(clearConfig, obfuscatedConfig);
    obfuscatedConfig.put(HIDE_KEY_ONE, newValueOne);
    obfuscatedConfig.put(HIDE_KEY_TWO, newValueTwo);
    obfuscatedConfig.put(HIDE_KEY_THREE, evilValue);
    ServletUtil.replaceSensitiveData(obfuscatedConfig, clearConfig);
    assertEquals(newValueOne, obfuscatedConfig.get(HIDE_KEY_ONE));
    assertEquals(newValueTwo, obfuscatedConfig.get(HIDE_KEY_TWO));
    assertEquals(evilValue, obfuscatedConfig.get(HIDE_KEY_THREE));
    assertEquals(clearValue, obfuscatedConfig.get(CLEAR_KEY_ONE));
  }

  private static final String VALUE = "value";
  private static final String NAME = "name";
  private static final String TEXT = "text";
  private static final String TYPE = "type";
  private static final String INPUT = "input";
  private static final String CLOSE_ELEMENT = "/>";
  private static final String OPEN_ELEMENT = "<";
  private static final String PASSWORD = "password";
  private static final String TR_END = "</tr>\r\n";
  private static final String TD_END = "</td>\r\n";
  private static final String TD_START = "<td>";
  private static final String TR_START = "<tr>\r\n";

  private String makeConfigForm(Map<String, String> configMap) {
    StringBuilder buf = new StringBuilder(2048);
    for (Map.Entry<String, String> entry : configMap.entrySet()) {
      appendStartRow(buf, entry.getKey());
      buf.append(OPEN_ELEMENT);
      buf.append(INPUT);
      if (SecurityUtils.isKeySensitive(entry.getKey())) {
        appendAttribute(buf, TYPE, PASSWORD);
      } else {
        appendAttribute(buf, TYPE, TEXT);
      }
      appendAttribute(buf, NAME, entry.getKey());
      if (configMap != null) {
        String value = entry.getValue();
        if (value != null) {
          appendAttribute(buf, VALUE, value);
        }
      }
      appendEndRow(buf);
    }
    return buf.toString();
  }

  private void appendStartRow(StringBuilder buf, String key) {
    buf.append(TR_START);
    buf.append(TD_START);
    buf.append(key);
    buf.append(TD_END);
    buf.append(TD_START);
  }

  private void appendEndRow(StringBuilder buf) {
    buf.append(CLOSE_ELEMENT);
    buf.append(TD_END);
    buf.append(TR_END);
  }

  private void appendAttribute(StringBuilder buf, String attrName,
      String attrValue) {
    try {
      XmlUtils.xmlAppendAttr(attrName, attrValue, buf);
    } catch (IOException e) {
      // Can't happen with StringBuilder.
      fail("Unexpected exception: " + e.getMessage());
    }
  }

  private void obfuscateValues(Map<String, String> clearConfig,
       Map<String, String> obfuscatedConfig) {
    for (Map.Entry<String, String> entry : clearConfig.entrySet()) {
      obfuscatedConfig.put(entry.getKey(),
          (SecurityUtils.isKeySensitive(entry.getKey())) ?
              ServletUtil.obfuscateValue(entry.getValue()) : entry.getValue());
    }
  }

  private static final String DTD_DIRECTORY = "source/dtds/";

  private void addDtdToClassLoader() throws Exception {
    // Add the DTD files to the System ClassLoader.
    File f = new File(DTD_DIRECTORY);
    URL u = f.toURI().toURL();
    URLClassLoader sysClassLoader =
        (URLClassLoader) ServletUtil.class.getClassLoader();
    Class<URLClassLoader> clazz = URLClassLoader.class;
    // Since the addURL() method is protected need to use reflection.
    Method method = clazz.getDeclaredMethod("addURL", new Class[] {URL.class});
    method.setAccessible(true);
    method.invoke(sysClassLoader, new Object[] {u});
  }
}
