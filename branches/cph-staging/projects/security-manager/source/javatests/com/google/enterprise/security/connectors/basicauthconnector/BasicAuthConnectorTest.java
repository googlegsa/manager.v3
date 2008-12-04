package com.google.enterprise.security.connectors.basicauthconnector;

import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;
import com.google.enterprise.saml.server.AuthSite;
import com.google.enterprise.saml.server.UserIdentity;
import com.google.enterprise.security.connectors.basicauth.BasicAuthConnector;

import junit.framework.TestCase;

/* 
 * Tests for the {@link BasicAuthConnector} class.
 * Maybe should use a mock Idp...
 */
public class BasicAuthConnectorTest extends TestCase {
  
  public void testAuthenticate() {
    BasicAuthConnector conn;
    AuthSite site;
    UserIdentity id;

    // HTTP Basic Auth
    site = new AuthSite("http://leiz.mtv.corp.google.com", "/basic/", AuthNMechanism.BASIC_AUTH, null);
    id = new UserIdentity("basic", "test", site);
    conn = new BasicAuthConnector(site.getHostname() + site.getRealm());
    AuthenticationResponse result = conn.authenticate(id);
    assertTrue(result.isValid());
    
    // HTTPS Basic Auth
    site = new AuthSite("https://entconcx100-testbed.corp.google.com",
                        "/sslsecure/test1/", AuthNMechanism.BASIC_AUTH, null);
    id = new UserIdentity("ruth_test1", "test1", site);
    conn = new BasicAuthConnector(site.getHostname() + site.getRealm());
    result = conn.authenticate(id);
    assertFalse(result.isValid());  // TODO SSL problem, make this work
  }
}
