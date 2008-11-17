package com.google.enterprise.security.connectors;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;

import junit.framework.TestCase;

public class BasicRegressionTest extends TestCase {

  public void testSamlartifact() throws Exception {
    WebConversation wc = new WebConversation();
    WebRequest request = new PostMethodWebRequest("http://localhost:8973/security-manager/samlartifact");
    wc.getResponse(request);
  }
}
