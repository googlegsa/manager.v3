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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.test.JsonObjectAsMap;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 
 */
public class SpringUtilsTest extends TestCase {

  /**
   * Test method for 
   * {@link com.google.enterprise.connector.instantiator.SpringUtils
   * #mapToSpring(java.util.Map, int)}.
   * @throws JSONException 
   */
  public final void testMapToSpring() throws JSONException {
    {
      String jsonInput = "{rowr:bazzle, foo:bar}";
      JSONObject jo = new JSONObject(jsonInput);
      Map m = new JsonObjectAsMap(jo);
      String springXML = SpringUtils.mapToSpring(m);
      String expected =
          "<map>\r\n" + "   <entry>\r\n"
              + "      <key><value>foo</value></key>\r\n"
              + "      <value>bar</value>\r\n" + "   </entry>\r\n"
              + "   <entry>\r\n" + "      <key><value>rowr</value></key>\r\n"
              + "      <value>bazzle</value>\r\n" + "   </entry>\r\n"
              + "</map>\r\n" + "";
      Assert.assertEquals(expected, springXML);
    }

    {
      String jsonInput = "{tree:tree, now:time, noman:island}";
      JSONObject jo = new JSONObject(jsonInput);
      Map m = new JsonObjectAsMap(jo);
      String springXML = SpringUtils.mapToSpring(m);
      String expected =
          "<map>\r\n" + "   <entry>\r\n"
              + "      <key><value>noman</value></key>\r\n"
              + "      <value>island</value>\r\n" + "   </entry>\r\n"
              + "   <entry>\r\n" + "      <key><value>now</value></key>\r\n"
              + "      <value>time</value>\r\n" + "   </entry>\r\n"
              + "   <entry>\r\n" + "      <key><value>tree</value></key>\r\n"
              + "      <value>tree</value>\r\n" + "   </entry>\r\n"
              + "</map>\r\n" + "";
      Assert.assertEquals(expected, springXML);
    }

    {
      String jsonInput = "{}";
      JSONObject jo = new JSONObject(jsonInput);
      Map m = new JsonObjectAsMap(jo);
      String springXML = SpringUtils.mapToSpring(m);
      String expected = "<map>\r\n</map>\r\n" + "";
      Assert.assertEquals(expected, springXML);
    }
  }
  
  /**
   * Test method for 
   * {@link com.google.enterprise.connector.instantiator.SpringUtils
   * #stripBeansElement(java.lang.String)}.
   */
  public final void testStripBeansElement() {
    runStripBeans("<beans>fubar</beans>","fubar");
    runStripBeans("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
            "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">\r\n" + 
            "<beans>\r\n" +
            "   <bean id=\"TestConnector1\"\r\n" + 
            "       class=\"com.google.enterprise.connector.jcradaptor.SpiRepositoryFromJcr\">\r\n" + 
            "   </bean>\r\n" + 
            "</beans>\r\n","\r\n" +
            "   <bean id=\"TestConnector1\"\r\n" + 
            "       class=\"com.google.enterprise.connector.jcradaptor.SpiRepositoryFromJcr\">\r\n" + 
            "   </bean>\r\n");
  }

  private void runStripBeans(String input, String expectedOutput) {
    String actualResult = SpringUtils.stripBeansElement(input);
    Assert.assertEquals(expectedOutput,actualResult);
  }

  /**
   * Test method for 
   * {@link com.google.enterprise.connector.instantiator.SpringUtils
   * #stripBeansElement(java.lang.String)}.
   */
  public final void testSetBeanID() {
    runSetBeanID("<bean id=\"foo\">fubar</bean>","newid","<bean id=\"newid\">fubar</bean>");
    runSetBeanID("<bean id=\"foo\">fubar</bean>","barbara streisand",
        "<bean id=\"barbara streisand\">fubar</bean>");
  }

  private void runSetBeanID(String input, String newid, String expectedOutput) {
    String actualResult = SpringUtils.setBeanID(input, newid);
    Assert.assertEquals(expectedOutput,actualResult);
  }
}
