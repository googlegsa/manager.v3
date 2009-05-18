// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.connector.common;

import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.saml.common.Metadata;
import com.google.enterprise.connector.saml.server.BackEnd;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opensaml.saml2.metadata.EntityDescriptor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

/**
 * Useful utilities for writing servlets.
 */
public abstract class ServletBase extends HttpServlet {

  protected static final DateTimeFormatter dtFormat =
      DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

  public static String httpDateString() {
    return dtFormat.print((new DateTime()).withZone(DateTimeZone.UTC));
  }

  public static PrintWriter initNormalResponse(HttpServletResponse response) throws IOException {
    initResponse(response);
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Content-Type", "text/html; charset=UTF-8");
    response.setBufferSize(0x1000);
    return response.getWriter();
  }

  public static void initErrorResponse(HttpServletResponse response, int code)
      throws IOException {
    initResponse(response);
    response.sendError(code);
  }

  public static void initResponse(HttpServletResponse response) {
    response.setHeader("Date", httpDateString());
  }

  public static ConnectorManager getConnectorManager() {
    return ConnectorManager.class.cast(Context.getInstance().getManager());
  }

  public static BackEnd getBackEnd() {
    BackEnd backend = 
      BackEnd.class.cast(Context.getInstance().getRequiredBean("BackEnd", BackEnd.class));
    return backend;
  }

  public static EntityDescriptor getEntity(String id) throws IOException {
    return getMetadata().getEntity(id);
  }

  public static EntityDescriptor getSmEntity() throws IOException {
    return getMetadata().getSmEntity();
  }

  public static String getSmEntityId() {
    return getMetadata().getSmEntityId();
  }

  public static Metadata getMetadata() {
    return Metadata.class.cast(Context.getInstance().getRequiredBean("Metadata", Metadata.class));
  }
}
