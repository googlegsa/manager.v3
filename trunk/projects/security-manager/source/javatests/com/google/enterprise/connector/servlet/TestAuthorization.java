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

package com.google.enterprise.connector.servlet;


import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.mock.SimpleMockAuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple "Hello world" servlet.
 *
 */
public class TestAuthorization extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	/**
     * Generates a "Hello world" response.
     * @param req 
     * @param res 
     * @throws IOException 
     * 
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
              throws IOException
    {
      AuthorizationManager am = new SimpleMockAuthorizationManager();
      String thisUrl = req.getPathTranslated();
      if (thisUrl == null) {
        thisUrl = "";
      }
      List<String> docids = ImmutableList.of(thisUrl);
      Collection<AuthorizationResponse> rs = null;
      
      try {
        rs = am.authorizeDocids(docids, null);
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
      boolean authOk = false;
      if (rs != null) {
        for (AuthorizationResponse ar: rs) {
          if (ar.getDocid().equals(thisUrl)) {
            authOk = ar.isValid();
          }
        }
      }
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();
      out.println("<HTML><HEAD><TITLE>Authorization Test</TITLE>");
      out.print("</HEAD><BODY>" + thisUrl + " is ");
      if (authOk) {
        out.print("ok.");
      } else {
        out.print("not ok.");
      }
      out.println("</BODY></HTML>");
      out.close();
    }
    
    /**
     * Returns servlet info.
     * @return informational message
     * 
     */
    @Override
    public String getServletInfo()
    {
      return "Tests an authorization manager in the security manager context";
    }
}
