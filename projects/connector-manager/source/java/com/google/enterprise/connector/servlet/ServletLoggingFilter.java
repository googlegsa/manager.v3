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

import com.google.enterprise.connector.logging.NDC;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A Servlet Filter that logs incomming ServletRequests.
 */
public class ServletLoggingFilter implements Filter {
  private static final Logger LOGGER =
      Logger.getLogger(ServletLoggingFilter.class.getName());

  @Override
  public void init(FilterConfig filterConfig) {
    NDC.push("Init");
    try {
      LOGGER.fine("Initializing Servlet Logging Filter");
    } finally {
      NDC.remove();
    }
  }

  @Override
  public void destroy() {
    NDC.push("Shutdown");
    try {
      LOGGER.fine("Shutting Down Servlet Logging Filter");
    } finally {
      NDC.remove();
    }
  }

  /** Log the ServletRequest path, pathInfo, and query info. */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    NDC.push("Servlet");
    try {
      // We log at FINE, because Authz traffic would be too much for INFO.
      boolean isLogging = LOGGER.isLoggable(Level.FINE)
          && (request instanceof HttpServletRequest);
      HttpServletRequest req = null;
      String servletPath = null;

      if (isLogging) {
        req = (HttpServletRequest) request;
        StringBuilder builder = new StringBuilder();

        // Log the HttpServletRequest requestor, path, and pathInfo.
        if (req.isSecure()) {
          builder.append("Secure ");
        }
        builder.append(req.getMethod()).append(" from " )
               .append(req.getRemoteAddr()).append(" : ");

        int i = builder.length();
        if (req.getServletPath() != null) {
          builder.append(req.getServletPath());
        }
        if (req.getPathInfo() != null) {
          builder.append(req.getPathInfo());
        }
        servletPath = builder.substring(i);

        // Log request Attributes.
        @SuppressWarnings("unchecked") Enumeration<String> attrNames =
            req.getAttributeNames();
        if (attrNames.hasMoreElements()) {
          builder.append(" attrs = { ");
          while (attrNames.hasMoreElements()) {
            String name = attrNames.nextElement();
            builder.append(name).append('=').append(req.getAttribute(name));
            builder.append(", ");
          }
          builder.setLength(builder.length() - 2); // backup over last comma
          builder.append(" }");
        }

        // Log the request Parameters.
        @SuppressWarnings("unchecked") Enumeration<String> paramNames =
            req.getParameterNames();
        if (paramNames.hasMoreElements()) {
          builder.append(" params = { ");
          while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            builder.append(name).append('=').append(req.getParameter(name));
            builder.append(", ");
          }
          builder.setLength(builder.length() - 2); // backup over last comma
          builder.append(" }");
        }

        // Actually log the request.
        LOGGER.fine(builder.toString());
      }

      // Let the rest of the filter chain go at it.
      try {
        chain.doFilter(request, response);
      } catch (ServletException se) {
        LOGGER.log(Level.WARNING, "Servlet " + servletPath
                   + " threw exception: ", se);
        throw se;
      } finally {
        if (isLogging && LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Done handling servlet request: " + servletPath);
        }
      }

    } finally {
      NDC.clear();
    }
  }
}
