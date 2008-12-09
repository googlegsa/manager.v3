// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.security.connectors.formauthconnector;

import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.server.UserIdentity;
import com.google.enterprise.security.connectors.formauth.FormAuthConnector;
import com.google.enterprise.sessionmanager.AuthnDomain;
import com.google.enterprise.sessionmanager.AuthnMechanism;

import junit.framework.TestCase;

import org.apache.commons.httpclient.NameValuePair;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.servlet.http.Cookie;

/* 
 * Tests for the {@link FormAuthConnector} class.
 * Maybe should use a mock Idp...
 */
public class FormAuthConnectorTest extends TestCase {
  final String servers[] = new String[] { "http://gama.corp.google.com/secured/",
                       "http://leiz.mtv.corp.google.com/secure/",
                       "http://leiz.mtv.corp.google.com/voyager/" };
  String formUri[] = new String[servers.length];
  final String users[] = new String[] { "gama1", "test", "voyager" };
  final String passwords[] = new String[] { "gama%%1", "test", "foobar" };
  
  public void testFetch() {
    FormAuthConnector conn = new FormAuthConnector("SMSESSION");
    int idx = 0;
    
    for (String server : servers) {
      StringBuffer form = new StringBuffer();
      try {
        formUri[idx] = conn.fetchLoginForm(server, form);
      } catch (Exception e) {
        System.out.println("Could not GET " + server + ": " + e.toString());
      }
      System.out.println(form.toString());
      idx++;
    }
  }

  public void testParse() throws IOException {
    FormAuthConnector conn = new FormAuthConnector("SMSESSION");
    int idx = 0;
    Vector<NameValuePair> params;
    
    for (String server : servers) {
      StringBuffer form = new StringBuffer();
      String[] action = new String[1];
      try {
         formUri[idx]= conn.fetchLoginForm(server, form);
      } catch (Exception e) {
        System.out.println("Could not GET " + server + ": " + e.toString());
      }
      params = conn.parseLoginForm(form, users[idx], passwords[idx], action);
      for (NameValuePair oneParam : params) {
        System.out.println(oneParam.getName());
      }
      System.out.println("-------------------");
      idx++;
    }
  }

  public void testSubmit() throws IOException {
    FormAuthConnector conn = new FormAuthConnector("SMSESSION");
    int idx = 0;
    Vector<NameValuePair> params;
    
    for (String server : servers) {
      StringBuffer form = new StringBuffer();
      String[] action = new String[1];
      
      try {
        formUri[idx] = conn.fetchLoginForm(server, form);
      } catch (Exception e) {
        System.out.println("Could not GET " + server + ": " + e.toString());
      }
      params = conn.parseLoginForm(form, users[idx], passwords[idx], action);
      System.out.println("Action is " + action);
      for (NameValuePair oneParam : params) {
        System.out.println(oneParam.getName());
      }
      System.out.println("-------------------");
      System.out.println("POSTING to " + formUri[idx]);
      if (action[0] != null) {
        URL formUrl = new URL(formUri[idx]);
        URL newUrl = new URL(formUrl.getProtocol(), formUrl.getAuthority(), action[0]);
        formUri[idx] = newUrl.toString();
      }
      Vector<Cookie> cookies = conn.submitLoginForm(formUri[idx], params.toArray(new NameValuePair[params.size()]));
      assertNotNull(cookies);
      System.out.println("Authenticated as " + users[idx]);
      
      idx++;
    }
   
  }
  
  public void testAuthenticate() {
    FormAuthConnector conn = new FormAuthConnector("SMSESSION");
    
    AuthnDomain domain =
        AuthnDomain.compatAuthSite("http://gama.corp.google.com",
                                   "/secured/", AuthnMechanism.FORMS_AUTH, null);
    UserIdentity id = UserIdentity.compatNew("gama1", "gama%%1", domain);
    AuthenticationResponse result = conn.authenticate(id);
    assertTrue(result.isValid());
    
    AuthnDomain domain2 =
        AuthnDomain.compatAuthSite("http://gama.corp.google.com",
                                   "/user1", AuthnMechanism.FORMS_AUTH, null);
    UserIdentity id2 = UserIdentity.compatNew("gama1", "deadbeef", domain2);
    result = conn.authenticate(id2);
    assertFalse(result.isValid());
  }
}
