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

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.test.JsonObjectAsMap;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 
 */
public class SimpleConfigurerTest extends TestCase {

  /**
   * Test method for {@link com.google.enterprise.connector.spi.SimpleConfigurer
   * #getConfigForm(java.lang.String)}.
   */
  public final void testGetConfigForm() {
    {
      SimpleConfigurer simpleConfigurer = new SimpleConfigurer();
      simpleConfigurer.setConfigKeys(new String[] {"foo", "bar"});
      ConfigureResponse configureResponse =
          simpleConfigurer.getConfigForm(null);
      String initialConfigForm = configureResponse.getFormSnippet();
      String expectedResult =
          "<tr>\r\n" + "<td>foo</td>\r\n"
              + "<td><input type=\"text\" name=\"foo\"></td>\r\n" + "</tr>\r\n"
              + "<tr>\r\n" + "<td>bar</td>\r\n"
              + "<td><input type=\"text\" name=\"bar\"></td>\r\n" + "</tr>\r\n";
      Assert.assertEquals(expectedResult, initialConfigForm);
    }
    {
      SimpleConfigurer simpleConfigurer = new SimpleConfigurer();
      simpleConfigurer.setConfigKeys(new String[] {"user", "password"});
      ConfigureResponse configureResponse =
          simpleConfigurer.getConfigForm(null);
      String initialConfigForm = configureResponse.getFormSnippet();
      String expectedResult =
          "<tr>\r\n" + "<td>user</td>\r\n"
              + "<td><input type=\"text\" name=\"user\"></td>\r\n"
              + "</tr>\r\n" + "<tr>\r\n" + "<td>password</td>\r\n"
              + "<td><input type=\"password\" name=\"password\"></td>\r\n"
              + "</tr>\r\n";
      Assert.assertEquals(expectedResult, initialConfigForm);
    }
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleConfigurer#validateConfig(java.util.Map, java.lang.String)}.
   * 
   * @throws JSONException
   */
  public final void testValidateConfig() throws JSONException {
    {
      SimpleConfigurer simpleConfigurer = new SimpleConfigurer();
      simpleConfigurer.setConfigKeys(new String[] {"user", "password"});
      JSONObject jo = 
        new JSONObject("{user:max, dog:snickers, destination:heaven}");
      Map map = new JsonObjectAsMap(jo);
      ConfigureResponse configureResponse = 
        simpleConfigurer.validateConfig(map, null);
      String configForm = configureResponse.getFormSnippet();
      String expectedResult = "<tr>\r\n" + 
            "<td>user</td>\r\n" + 
            "<td>max<input type=\"hidden\" value=\"max\" name=\"user\"></td>\r\n" + 
            "</tr>\r\n" + 
            "<tr>\r\n" + 
            "<td>password</td>\r\n" + 
            "<td><input type=\"password\" name=\"password\"></td>\r\n" + 
            "</tr>\r\n" + 
            "<input type=\"hidden\" value=\"heaven\" name=\"destination\">\r\n" +
            "<input type=\"hidden\" value=\"snickers\" name=\"dog\">\r\n" +
            "";
      Assert.assertEquals(expectedResult, configForm);
      String message = configureResponse.getMessage();
      Assert.assertTrue(message.length() > 0);      
    }
    
    {
      SimpleConfigurer simpleConfigurer = new SimpleConfigurer();
      simpleConfigurer.setConfigKeys(new String[] {"user", "password"});
      JSONObject jo = 
        new JSONObject("{user:max, password:xyzzy, dog:snickers}");
      Map map = new JsonObjectAsMap(jo);
      ConfigureResponse configureResponse = 
        simpleConfigurer.validateConfig(map, null);
      String configForm = configureResponse.getFormSnippet();
      Assert.assertEquals(null, configForm);
      String message = configureResponse.getMessage();
      Assert.assertEquals(null, message);      
    }
  }
}
