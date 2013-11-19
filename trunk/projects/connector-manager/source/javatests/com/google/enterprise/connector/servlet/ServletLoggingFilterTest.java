// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.servlet;

import junit.framework.TestCase;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;

/**
 * Tests {@link ServletLoggingFilter}.
 */
public class ServletLoggingFilterTest extends TestCase {
  private MockHttpServletRequest req;
  private MockHttpServletResponse res;
  private ServletLoggingFilter filter;
  private Logger logger;
  private RecordingHandler handler;
  private MockFilterChain chain;

  @Override
  protected void setUp() throws Exception {
    filter = new ServletLoggingFilter();
    filter.init(null);
    logger = Logger.getLogger(ServletLoggingFilter.class.getName());
    handler = new RecordingHandler();
    logger.addHandler(handler);
    req = new MockHttpServletRequest("GET","/connector-manager/test/info");
    req.addParameter("Param1", "param1");
    req.addParameter("Param2", "param2");
    req.addParameter("Param3", "param3");
    req.setAttribute("Attr1", "attr1");
    req.setAttribute("Attr2", "attr2");
    req.setServletPath("/test");
    req.setPathInfo("/info");
    res = new MockHttpServletResponse();
    chain = new MockFilterChain();
  }

  @Override
  protected void tearDown() {
    filter.destroy();
  }

  /** Test no logging if Level is not sufficiently verbose. */
  public void testLogLevelInfo() throws Exception {
    logger.setLevel(Level.INFO);
    filter.doFilter(req, res, chain);
    assertEquals("", handler.toString());
    assertSame(req, chain.getRequest());
    assertSame(res, chain.getResponse());
  }

  /** Test logging a request at Fine. */
  public void testLogLevelFine() throws Exception {
    String expected = "GET from 127.0.0.1 : /test/info "
        + "attrs = { Attr2=attr2, Attr1=attr1 } "
        + "params = { Param1=param1, Param2=param2, Param3=param3 }\n";
    logger.setLevel(Level.FINE);
    filter.doFilter(req, res, chain);
    assertEquals(expected, handler.toString());
    assertSame(req, chain.getRequest());
    assertSame(res, chain.getResponse());
  }

  /** Test logging a request at Fine. */
  public void testLogLevelFinest() throws Exception {
    String expected = "Secure GET from 127.0.0.1 : /test/info "
        + "attrs = { Attr2=attr2, Attr1=attr1 } "
        + "params = { Param1=param1, Param2=param2, Param3=param3 }\n"
        + "Done handling servlet request: /test/info\n";
    logger.setLevel(Level.FINEST);
    req.setSecure(true);
    filter.doFilter(req, res, chain);
    assertEquals(expected, handler.toString());
    assertSame(req, chain.getRequest());
    assertSame(res, chain.getResponse());
  }

  /** Test no logging if request is not HttpServletRequest. */
  public void testNotHttpServletRequest() throws Exception {
    ServletRequest request = new ServletRequestWrapper(req);
    ServletResponse response = new ServletResponseWrapper(res);
    logger.setLevel(Level.FINEST);
    filter.doFilter(request, response, chain);
    assertEquals("", handler.toString());
    assertSame(request, chain.getRequest());
    assertSame(response, chain.getResponse());
  }

  /** A Handler that records logged messages. */
  private class RecordingHandler extends Handler {
    private StringWriter writer = new StringWriter();

    @Override
    public void publish(LogRecord record) {
      writer.write(record.getMessage());
      writer.write('\n');
    }

    @Override
    public void flush() {
      writer.flush();
    }

    @Override
    public void close() {
      flush();
    }

    @Override
    public String toString() {
      flush();
      return writer.toString();
    }
  }
}
