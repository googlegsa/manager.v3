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


import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple "Hello world" servlet.
 *
 */
public class HelloWorld extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
     * Generates a "Hello world" response.
     * @param req
     * @param res
     * @throws IOException
     *
     */
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
              throws IOException
    {
      Preconditions.checkNotNull(req);
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();
      out.println("<HTML><HEAD><TITLE>Hello World</TITLE>"+
                  "</HEAD><BODY>Hello World!</BODY></HTML>");
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
      return "Hello World";
    }
}
