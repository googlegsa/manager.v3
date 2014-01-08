// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.util.connectortype;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.util.XmlParseUtil;
import com.google.enterprise.connector.util.connectortype.ConnectorFields.EnumField;
import com.google.enterprise.connector.util.connectortype.ConnectorFields.IntField;
import com.google.enterprise.connector.util.connectortype.ConnectorFields.MultiCheckboxField;
import com.google.enterprise.connector.util.connectortype.ConnectorFields.SingleLineField;

import junit.framework.TestCase;

import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class ConnectorFieldsTest extends TestCase {

  /**
   * A simple resource bundle that uppercases the keys, and optionally
   * returns a specified value for a given key. This implementation is
   * used to show that the resource bundle passed in to various Fields
   * is being used, and to test XML encoding.
   */
  private static class UpcasingResourceBundle extends ResourceBundle {
    private final String distinguishedKey;
    private final String distinguishedValue;

    UpcasingResourceBundle() {
      this(null, null);
    }

    UpcasingResourceBundle(String key, String value) {
      this.distinguishedKey = key;
      this.distinguishedValue = value;
    }

    @Override
    public Enumeration<String> getKeys() {
      throw new IllegalStateException();
    }

    /**
     * All it does is translates the key to upper case
     */
    @Override
    protected Object handleGetObject(String key) {
      return (key.equals(distinguishedKey))
          ? distinguishedValue :  key.toUpperCase();
    }
  }

  public void testSimpleFieldEmpty() throws Exception {
    doTestSimpleFieldWithValue(false, null, null, null, true);
  }

  public void testSimpleFieldWithValueFromMap() throws Exception {
    doTestSimpleFieldWithValue(false, "bar", null, "bar", true);
  }

  public void testSimpleFieldWithValueFromString() throws Exception {
    doTestSimpleFieldWithValue(false, "bar", null, "bar", false);
  }

  public void testSimpleFieldWithValueEncoded() throws Exception {
    doTestSimpleFieldWithValue(false, "ain't", null, "ain&#39;t", true);
  }

  public void testSimpleFieldPassword() throws Exception {
    doTestSimpleFieldWithValue(true, null, null, null, true);
  }

  public void testSimpleFieldDefaultValueOnly() throws Exception {
    doTestSimpleFieldWithValue(false, null, "bar", "bar", true);
  }

  public void testSimpleFieldDefaultValueUnusedFromMap() throws Exception {
    doTestSimpleFieldWithValue(false, "bar", "foo", "bar", true);
  }

  public void testSimpleFieldDefaultValueUnusedFromString() throws Exception {
    doTestSimpleFieldWithValue(false, "bar", "foo", "bar", false);
  }

  private void doTestSimpleFieldWithValue(boolean isPassword, String value,
      String defaultValue, String encodedValue, boolean fromMap)
      throws Exception {
    String name = "simple";
    String label = "d'accord";
    boolean mandatory = false;
    SingleLineField field = new SingleLineField(name, mandatory, isPassword);
    if (value != null) {
      if (fromMap) {
        field.setValueFrom(ImmutableMap.of(name, value));
      } else {
        field.setValueFromString(value);
      }
    }
    if (defaultValue != null) {
      field.setDefaultValue(defaultValue);
    }
    boolean highlightError = false;
    String snippet = field.getSnippet(new UpcasingResourceBundle(name, label),
        highlightError);
    XmlParseUtil.validateXhtml(snippet);
    assertTrue(snippet.contains("name=\"" + name + "\""));
    assertTrue(snippet.contains("input"));
    if (isPassword) {
      assertTrue(snippet.contains("type=\"password\""));
    } else {
      assertTrue(snippet.contains("type=\"text\""));
    }
    if (value == null) {
      assertFalse(snippet.contains("value"));
    } else {
      assertTrue(snippet.contains("value=\"" + encodedValue + "\""));
    }

    // The encoded label is not declared earlier so that I don't use
    // it by mistake elsewhere.
    doTestBundleMessage(snippet, name, label, "d&#39;accord");
  }

  /**
   * Tests bundle messages that require XML encoding.
   *
   * @param snippet the form snippet containing the message under test
   * @param name the bundle key
   * @param message the bundle value
   * @param encodedMessage the XML encoded bundle value
   */
  private void doTestBundleMessage(String snippet, String name, String message,
      String encodedMessage) {
    assertFalse("Found undistinguished message " + name.toUpperCase(),
        snippet.contains(name.toUpperCase()));
    assertFalse("Found unencoded message " + message,
        snippet.contains(message));
    int index = snippet.indexOf(encodedMessage);
    assertTrue("Did not find encoded message " + encodedMessage, index != -1);
    assertFalse("More than one occurrence of " + encodedMessage,
        snippet.substring(index + name.length()).contains(encodedMessage));
  }

  public void testIntField() throws Exception {
    doTestIntField(43);
  }

  private void doTestIntField(int value) throws Exception {
    String name = "intfield";
    boolean mandatory = false;
    int defaultInt = 47;
    IntField field = new IntField(name, mandatory, defaultInt);
    field.setValueFromInt(value);
    boolean highlightError = false;
    String snippet = field.getSnippet(new UpcasingResourceBundle(), highlightError);
    XmlParseUtil.validateXhtml(snippet);
    assertTrue(snippet.contains("name=\"" + name + "\""));
    assertTrue(snippet.contains("input"));
    assertTrue(snippet.contains("type=\"text\""));
    assertTrue(snippet.contains("value=\"" + value + "\""));
    assertEquals(value, field.getIntegerValue().intValue());
    assertTrue(snippet.contains(name.toUpperCase()));
    assertTrue(snippet.contains(name));
  }

  enum TestEnum1 {
    ABC, DEF, GHI
  }

  public void testEnumFieldNoDefault() throws Exception {
    doTestEnumField(TestEnum1.class, null);
  }

  enum TestEnum2 {
    ZYX, WVU, TSR, QPO
  }

  public void testEnumFieldWithDefault() throws Exception {
    doTestEnumField(TestEnum2.class, TestEnum2.QPO);
  }

  private <E extends Enum<E>> void doTestEnumField(Class<E> enumClass, E defaultValue)
      throws Exception {
    String name = "enumfield";
    boolean mandatory = false;
    EnumField<E> field = new EnumField<E>(name, mandatory, enumClass, defaultValue);
    boolean highlightError = false;
    String snippet = field.getSnippet(new UpcasingResourceBundle(), highlightError);
    XmlParseUtil.validateXhtml(snippet);
    assertTrue(snippet.contains("name=\"" + name + "\""));
    assertTrue(snippet.contains("select"));
    assertTrue(snippet.contains(name.toUpperCase()));
    assertTrue(snippet.contains(name));
    for (E e : enumClass.getEnumConstants()) {
      assertTrue(snippet.contains("value=\"" + e.toString() + "\""));
    }
    assertEquals(defaultValue != null, snippet.contains("selected"));
  }

  public void testMultiCheckboxField() throws Exception {
    String name = "multicheckboxfield";
    ImmutableSet<String> keys = ImmutableSet.of("foo", "bar", "baz");
    String snippet = getMultiCheckboxFieldSnippet(name, keys, null, null);
    assertTrue(snippet.contains("checkbox"));
    assertTrue(snippet.contains(name.toUpperCase()));
    assertTrue(snippet.contains(name));
  }

  public void testMultiCheckboxFieldMessage() throws Exception {
    String name = "multicheckboxfield";
    ImmutableSet<String> keys = ImmutableSet.of("foo", "bar", "baz");
    String message = "d'accord";
    String snippet = getMultiCheckboxFieldSnippet(name, keys, message, null);
    doTestBundleMessage(snippet, "ignore me", message, "D&#39;ACCORD");
  }

  public void testMultiCheckboxFieldCallbackAppend() throws Exception {
    String name = "multicheckboxfield";
    ImmutableSet<String> keys = ImmutableSet.of("foo", "bar", "baz");
    String original = getMultiCheckboxFieldSnippet(name, keys, null, null);

    // The appended value must be valid inside an XHTML input tag.
    final String text = "disabled";
    String append = getMultiCheckboxFieldSnippet(name, keys, null,
        new MultiCheckboxField.Callback() {
          @Override public Map<String, String> getAttributes(String key) {
            return ImmutableMap.of(text, text);
          }
        });
    assertFalse(original.equals(append));
    assertTrue(append.contains(text));
    // For each key, we appended _text="text", two occurrences of the
    // text value, three punctuation characters, and a leading space.
    assertEquals(original.length() + keys.size() * (2 * text.length() + 4),
        append.length());
  }

  public void testMultiCheckboxFieldCallbackException() throws Exception {
    String name = "multicheckboxfield";
    ImmutableSet<String> keys = ImmutableSet.of("foo", "bar", "baz");
    try {
      getMultiCheckboxFieldSnippet(name, keys, null,
        new MultiCheckboxField.Callback() {
          @Override public Map<String, String> getAttributes(String key) {
            throw new IllegalStateException();
          }
        });
      fail("Expected an exception");
    } catch (IllegalStateException expected) {
    }
  }

  /** @throws Exception if the snippet is not valid XHTML */
  private String getMultiCheckboxFieldSnippet(String name, Set<String> keys,
      String message, MultiCheckboxField.Callback callback) throws Exception {
    boolean mandatory = false;
    MultiCheckboxField field = new MultiCheckboxField(name, mandatory, keys,
        message, callback);
    boolean highlightError = false;
    String snippet = field.getSnippet(new UpcasingResourceBundle(), highlightError);
    XmlParseUtil.validateXhtml(snippet);
    return snippet;
  }
}
