// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.loadbalancer;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A Tomcat Valve that does round-robin load balancing to the
 * configured Connector Manager web applications.
 */
public class LoadBalancerValve extends ValveBase {

  protected String manager = "/connector-manager";
  protected String[] workers = {"/connector-manager-1", "/connector-manager-2"};
  protected volatile int workerIndex = 0;

  /**
   * Sets the Connector Manager to proxy.  Requests to this connector manager
   * will be farmed out to the various workers.
   *
   * @param manager the proxied Connector Manager (default "connector-manager")
   */
  public void setManager(String manager) {
    if (manager == null || manager.length() == 0) {
      throw new IllegalArgumentException("manager must not be null or empty");
    }
    this.manager = (manager.charAt(0) == '/') ? manager : ("/" + manager);
  }

  /**
   * Sets the list of Connector Managers web applications to distribute work to.
   *
   * @param workers a comma-separated list of connector manager web apps
   *                 (default "connector-manager-1,connector-manager-2")
   */
  public void setWorkers(String workers) {
    if (workers == null || workers.length() == 0) {
      throw new IllegalArgumentException("workers must not be null or empty");
    }
    this.workers = workers.split("\\s*,\\s*");
    this.workerIndex = 0;
  }

  /** Return descriptive information about this Valve implementation. */
  @Override
  public String getInfo() {
    return "Google Connector Manager Load Balancing Valve - distribute "
      + manager + " requests to " + Arrays.toString(workers);
  }

  /**
   * The implementation-specific logic represented by this Valve.
   *
   * @param request The servlet request to be processed
   * @param response The servlet response to be created
   *
   * @exception IOException if an input/output error occurs
   * @exception ServletException if a servlet error occurs
   */
  @Override
  public void invoke(Request request, Response response)
      throws IOException, ServletException {
    if (request.getContextPath() == null ||
        request.getContextPath().length() == 0) {
      String servletPath = request.getServletPath();
      if (servletPath.equals(manager)) {
        servletPath = manager + "/";
      }
      if (servletPath.startsWith(manager + "/")) {
        String newContextPath = (workers[workerIndex].charAt(0) == '/')
                                ? workers[workerIndex]
                                : ("/" + workers[workerIndex]);
        if (++workerIndex == workers.length) {
          workerIndex = 0;
        }
        String newServletPath = servletPath.substring(manager.length());
        containerLog.info("BalancerValve redirecting to: " + newContextPath
                          + newServletPath);
        ServletContext workerContext =
            request.getContext().getServletContext().getContext(newContextPath);
        RequestDispatcher workerDispatcher =
            workerContext.getRequestDispatcher(newServletPath);
        workerDispatcher.forward(request, response);
        return;
      }
    }
    getNext().invoke(request, response);
  }
}
