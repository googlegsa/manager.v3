// Copyright (C) 2009 Google Inc.
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

import com.google.enterprise.connector.security.identity.AuthnMechanism;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.Cookie;

public class AbstractAuthnIdentityTest extends TestCase {

  public void testToJsonSimple() throws JSONException {
    MockAuthnIdentity id = new MockAuthnIdentity("http://foo.com/bar");
    id.setAuthnMechanism(AuthnMechanism.BASIC_AUTH);
    Cookie c1 = makeSnickerDoodle();
    id.addCookie(c1);
    Cookie c2 = makeOatmealCookie();
    id.addCookie(c2);
    String s = id.toJson();
    System.out.println(s);
    verifyIdentity(id, s);
  }

  private void verifyIdentity(SecAuthnIdentity id, String jsonString) throws JSONException {
    JSONObject jo = new JSONObject(jsonString);
    verifyString(id.getDomain(), jo, "domain");
    verifyString(id.getPassword(), jo, "password");
    verifyString(id.getSampleUrl(), jo, "sampleUrl");
    verifyString(id.getUsername(), jo, "username");
    verifyString(id.getVerificationStatus().toString(), jo, "verificationStatus");
    verifyString(AbstractAuthnIdentity.mechToTypeString(id.getMechanism()), 
        jo, "type");
    JSONArray ja = jo.getJSONArray("cookies");
    assertEquals(2, ja.length());
    for (int i = 0; i < 2; i++) {
      JSONObject jsonCookie = ja.getJSONObject(i);
      assertNotNull(jsonCookie);
      assertTrue(jsonCookie.has("name"));
      String name = jsonCookie.getString("name");
      assertNotNull(name);
      if (name.equals("snickerdoodle")) {
        verifyCookie(makeSnickerDoodle(), jsonCookie);
      } else if (name.equals("oatmeal")) {
        verifyCookie(makeOatmealCookie(), jsonCookie);
      } else {
        fail("unexpected cookie: " + name);
      }
    }
  }

  private Cookie makeSnickerDoodle() {
    Cookie c = new Cookie("snickerdoodle", "peanutbutter");
    c.setComment("yum");
    c.setDomain("foo.com");
    c.setMaxAge(60 * 5);
    c.setPath("/garden");
    c.setSecure(false);
    c.setVersion(0);
    return c;
  }

  private Cookie makeOatmealCookie() {
    Cookie c = new Cookie("oatmeal", "raisins");
    c.setComment("yum-yum!");
    c.setDomain("foo.com");
    c.setMaxAge(60 * 10);
    c.setPath("/kitchen");
    c.setSecure(true);
    c.setVersion(0);
    return c;
  }

  private void verifyCookie(Cookie c, JSONObject jsonCookie) throws JSONException {
    verifyString(c.getComment(), jsonCookie, "comment");
    verifyString(c.getDomain(), jsonCookie, "domain");
    verifyString(c.getPath(), jsonCookie, "path");
    verifyBoolean(c.getSecure(), jsonCookie, "secure");
    verifyInt(c.getMaxAge(), jsonCookie, "maxAge");
    verifyInt(c.getVersion(), jsonCookie, "version");
  }

  private void verifyBoolean(boolean b, JSONObject jo, String key) throws JSONException {
    assertEquals(b, jo.getBoolean(key));
  }

  private void verifyInt(int i, JSONObject jo, String key) throws JSONException {
    assertEquals(i, jo.getInt(key));
  }

  private void verifyString(String s, JSONObject jo, String key) throws JSONException {
    if (s == null) {
      assertFalse(jo.has(key));
    } else {
      assertEquals(s, jo.getString(key));
    }
  }
}
