package com.google.enterprise.security.connectors;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;

import junit.framework.TestCase;

public class BasicRegressionTest extends TestCase {

  public void testSamlartifact() throws Exception {
    WebConversation wc = new WebConversation();
    WebRequest request = new GetMethodWebRequest("http://localhost:8973/security-manager/samlartifact");
    wc.getResponse(request);
  }
}
