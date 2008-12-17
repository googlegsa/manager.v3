package com.google.enterprise.security.connectors.formauthconnector;

import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;
import com.google.enterprise.saml.server.AuthSite;
import com.google.enterprise.saml.server.UserIdentity;
import com.google.enterprise.security.connectors.formauth.FormAuthConnector;
import org.apache.commons.httpclient.NameValuePair;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

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
    
    AuthSite site = new AuthSite("http://gama.corp.google.com", "/secured/", AuthNMechanism.FORMS_AUTH, null);
    UserIdentity id = new UserIdentity("gama1", "gama%%1", site);
    AuthenticationResponse result = conn.authenticate(id);
    assertTrue(result.isValid());
    
    AuthSite site2 = new AuthSite("http://gama.corp.google.com", "/user1", AuthNMechanism.FORMS_AUTH, null);
    UserIdentity id2 = new UserIdentity("gama1", "deadbeef", site2);
    result = conn.authenticate(id2);
    assertFalse(result.isValid());
  }
}
