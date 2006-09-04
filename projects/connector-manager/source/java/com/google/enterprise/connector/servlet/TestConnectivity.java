// Copyright (C) 2006 Google Inc.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Admin servlet to test connectivity.
 * 
 */
public class TestConnectivity extends HttpServlet {
  private static final Logger logger = 
    Logger.getLogger(TestConnectivity.class.getName());
  /**
   * Returns a simple acknowledgement.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
    protected void doGet(HttpServletRequest req,
            HttpServletResponse res)
    	throws ServletException, IOException
   	{
        logger.info("Hello from the TestConnectivity servlet!");
    	res.setContentType("text/xml");
    	PrintWriter out = res.getWriter();
        out.println("<CmResponse>" +
                    "  <StatusId>0</StatusId>" +
                    "</CmResponse>");
        out.close();
    }

    /**
     * Returns a simple acknowledgement.
     * @param req 
     * @param res 
     * @throws ServletException 
     * @throws IOException 
     * 
     */
    protected void doPost(HttpServletRequest req,
            HttpServletResponse res)
    	throws ServletException, IOException
   	{
    	doGet(req, res);
   	}
}
