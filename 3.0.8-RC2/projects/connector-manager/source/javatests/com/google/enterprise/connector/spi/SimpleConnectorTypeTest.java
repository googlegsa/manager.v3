// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.test.JsonObjectAsMap;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class SimpleConnectorTypeTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleConnectorType
   * #getConfigForm(Locale)}.
   */
  public final void testGetConfigForm() {
    {
      SimpleConnectorType simpleConnectorType = new SimpleConnectorType();
      simpleConnectorType.setConfigKeys(new String[] {"foo", "bar"});
      ConfigureResponse configureResponse = simpleConnectorType
          .getConfigForm(null);
      String initialConfigForm = configureResponse.getFormSnippet();
      String expectedResult = "<tr>\r\n" + "<td>foo</td>\r\n"
          + "<td><input type=\"text\" name=\"foo\"/></td>\r\n" + "</tr>\r\n"
          + "<tr>\r\n" + "<td>bar</td>\r\n"
          + "<td><input type=\"text\" name=\"bar\"/></td>\r\n" + "</tr>\r\n";
      Assert.assertEquals(expectedResult, initialConfigForm);
    }
    {
      SimpleConnectorType simpleConnectorType = new SimpleConnectorType();
      simpleConnectorType.setConfigKeys(new String[] {"user", "password"});
      ConfigureResponse configureResponse = simpleConnectorType
          .getConfigForm(null);
      String initialConfigForm = configureResponse.getFormSnippet();
      String expectedResult = "<tr>\r\n" + "<td>user</td>\r\n"
          + "<td><input type=\"text\" name=\"user\"/></td>\r\n" + "</tr>\r\n"
          + "<tr>\r\n" + "<td>password</td>\r\n"
          + "<td><input type=\"password\" name=\"password\"/></td>\r\n"
          + "</tr>\r\n";
      Assert.assertEquals(expectedResult, initialConfigForm);
    }
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleConnectorType#
   * validateConfig(java.util.Map, Locale, ConnectorFactory)}.
   *
   * @throws JSONException
   */
  public final void testValidateConfig() throws JSONException {
    {
      SimpleConnectorType simpleConnectorType = new SimpleConnectorType();
      simpleConnectorType.setConfigKeys(new String[] {"user", "password"});
      JSONObject jo = new JSONObject(
          "{user:max, dog:snickers, destination:heaven}");
      Map<String, String> map = new JsonObjectAsMap(jo);
      ConfigureResponse configureResponse = simpleConnectorType.validateConfig(
          map, null, null);
      String configForm = configureResponse.getFormSnippet();
      String expectedResult =
        "<tr>\r\n"
          + "<td>user</td>\r\n"
          + "<td><input type=\"text\" value=\"max\" name=\"user\"/></td>\r\n"
          + "</tr>\r\n"
          + "<tr>\r\n"
          + "<td><font color=\"red\">password</font></td>\r\n"
          + "<td><input type=\"password\" name=\"password\"/></td>\r\n"
          + "</tr>\r\n"
          + "<input type=\"hidden\" value=\"heaven\" name=\"destination\"/>\r\n"
          + "<input type=\"hidden\" value=\"snickers\" name=\"dog\"/>\r\n" + "";
      Assert.assertEquals(expectedResult, configForm);
      String message = configureResponse.getMessage();
      Assert.assertTrue(message.length() > 0);
    }

    {
      SimpleConnectorType simpleConnectorType = new SimpleConnectorType();
      simpleConnectorType.setConfigKeys(new String[] {"user", "password"});
      JSONObject jo = new JSONObject("{user:max, password:xyzzy, dog:snickers}");
      Map<String, String> map = new JsonObjectAsMap(jo);
      ConfigureResponse configureResponse = simpleConnectorType.validateConfig(
          map, null, null);
      Assert.assertNull(configureResponse);
    }

    {
      TestConnectorType testConnectorType = new TestConnectorType();
      testConnectorType.setConfigKeys(new String[] {"user", "password"});
      JSONObject jo = new JSONObject("{user:max, password:xyzzy}");
      Map<String, String> map = new JsonObjectAsMap(jo);
      ConfigureResponse configureResponse = testConnectorType.validateConfig(
          map, null, null);
      String configForm = configureResponse.getFormSnippet();
      String expectedResult =
        "<tr>\r\n"
          + "<td>user</td>\r\n"
          + "<td><input type=\"text\" value=\"max\" name=\"user\"/></td>\r\n"
          + "</tr>\r\n"
          + "<tr>\r\n"
          + "<td><font color=\"red\">password</font></td>\r\n"
          + "<td><input type=\"password\" name=\"password\"/></td>\r\n"
          + "</tr>\r\n";
      Assert.assertEquals(expectedResult, configForm);
      String message = configureResponse.getMessage();
      Assert.assertTrue(message.length() > 0);
    }
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleConnectorType#
   * getPopulatedConfigForm(java.util.Map, Locale)}.
   *
   * @throws JSONException
   */
  public final void testGetPopulatedConfigForm() throws JSONException {
    {
      SimpleConnectorType simpleConnectorType = new SimpleConnectorType();
      simpleConnectorType.setConfigKeys(new String[] {"user", "password"});
      Map<String, String> map =
          new JsonObjectAsMap(new JSONObject("{user:max, password:foo}"));
      ConfigureResponse configureResponse = simpleConnectorType
          .getPopulatedConfigForm(map, null);
      String configForm = configureResponse.getFormSnippet();
      String expectedResult = "<tr>\r\n" + "<td>user</td>\r\n"
          + "<td><input type=\"text\" name=\"user\" value=\"max\"/></td>\r\n"
          + "</tr>\r\n" + "<tr>\r\n" + "<td>password</td>\r\n"
          + "<td><input type=\"password\" name=\"password\" value=\"foo\"/>"
          + "</td>\r\n" + "</tr>\r\n";
      Assert.assertEquals(expectedResult, configForm);
      String message = configureResponse.getMessage();
      Assert.assertTrue(message.length() == 0);
    }
  }

 public final void testGetPopulatedConfigWithSpecialXmlCharForm() 
     throws JSONException {
      SimpleConnectorType simpleConnectorType = new SimpleConnectorType();
      simpleConnectorType.setConfigKeys(new String[] {"user", "password"});
      Map<String, String> map =
          new JsonObjectAsMap(new JSONObject("{user:m&x, password:f&<oo}"));
      ConfigureResponse configureResponse = simpleConnectorType
          .getPopulatedConfigForm(map, null);
      String configForm = configureResponse.getFormSnippet();
      String expectedResult = "<tr>\r\n" + "<td>user</td>\r\n"
          + "<td><input type=\"text\" name=\"user\" value=\"m&amp;x\"/></td>\r\n"
          + "</tr>\r\n" + "<tr>\r\n" + "<td>password</td>\r\n"
          + "<td><input type=\"password\" name=\"password\" value=\"f&amp;&lt;oo\"/>"
          + "</td>\r\n" + "</tr>\r\n";
      Assert.assertEquals(expectedResult, configForm);
      String message = configureResponse.getMessage();
      Assert.assertTrue(message.length() == 0);
  }

  private static class TestConnectorType extends SimpleConnectorType {
    public TestConnectorType() {
      super();
    }
    @Override
    public boolean validateConfigPair(String key, String val) {
      if (!super.validateConfigPair(key, val)) {
        return false;
      }
      if (val.equals("xyzzy")) {
        return false;
      }
      return true;
    }
  }
}
