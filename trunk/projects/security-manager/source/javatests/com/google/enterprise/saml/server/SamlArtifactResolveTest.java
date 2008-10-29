package com.google.enterprise.saml.server;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Unit test for SamlArtifactResolve handler.
 */
public class SamlArtifactResolveTest extends TestCase {

  SamlArtifactResolve samlArtifactResolveInstance;

  public void setUp() {
    samlArtifactResolveInstance = new SamlArtifactResolve();
  }

  /**
   * At the moment this test just makes sure the post handler codepath executes
   * without hitting an exception and returns non-empty content.
   *
   * @throws UnsupportedEncodingException
   */
  public void testPostHandler() throws UnsupportedEncodingException {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    try {
      samlArtifactResolveInstance.handlePost(mockRequest, mockResponse);
    } catch (IOException e) {
      assertTrue(false);
    }

    String returnedContent = mockResponse.getContentAsString();
    System.out.println("content: \n" + returnedContent);

    /** make sure we got something back */
    assertTrue(returnedContent.length() > 0);
  }
}
