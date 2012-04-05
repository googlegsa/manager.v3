// Copyright 2010 Google Inc.
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

import com.google.enterprise.connector.manager.Context;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Remote Address Filter that can be used to restrict access to the
 * Servlets.  In general, we allow access from the GSA, localhost,
 * proxies, and perhaps certain adminstrator machines.
 */
// TODO: This was intended to be extensible replacement for the
// Tomcat RemoteIPAddrValve.  It has since been stripped of all
// such pretense and becomes, instead, a more generalized version
// of the hack perpetrated in revision r2607.
// Offboard Connector Managers must still rely on Tomcat RemoteIPAddrValve
// to determine access.
// TODO: This should really be a javax.servlet.Filter.
public class RemoteAddressFilter {
  private static Logger LOGGER =
      Logger.getLogger(ServletUtil.class.getName());

  // Singleton instance.
  private static RemoteAddressFilter instance = null;

  // If true, Connector Manager is on-board GSA.
  private final boolean onboard;

  /**
   * The various access modes. The GSA has access to all the servlets.
   * The access mode names here are arbitrary, as their interpretations
   * vary greatly between GSA on-board and off-board.
   */
  public enum Access {
    RED, BLACK
  }

  /** Restrict constructor to singlton. */
  private RemoteAddressFilter() {
    Properties props = Context.getInstance().getConnectorManagerProperties();
    this.onboard = Boolean.valueOf(props.getProperty("manager.onboard"));
    // TODO: extract accept/deny IP address patterns from properties.
    LOGGER.config(((this.onboard) ? "On-board" : "External")
                  + " Connector Manager detected.");
  }

  /** Returns the singleton RemoteAddressFilter for this context. */
  public static synchronized RemoteAddressFilter getInstance() {
    // Delay instantiating the singleton until we are sure the
    // Context has been initialized.
    if (instance == null) {
      instance = new RemoteAddressFilter();
    }
    return instance;
  }

  /**
   * Determine whether the supplied {@code remoteAddr} is allowed
   * access under the given {@code Access} mode.
   *
   * @param accessMode the {@code Access} mode for the calling servlet.
   * @param remoteAddr the IP address of the servlet's caller.
   * @return {@code true} if the caller is permitted access to the servlet,
   *         {@code false} otherwise.
   */
  public boolean allowed(Access accessMode, String remoteAddr) {
    // Offboard - defer to RemoteIPAddr Valve.
    // Onboard - RED is Public access, BLACK is GSA or localhost only.
    if (!onboard || accessMode == Access.RED) {
      return true;
    }
    try {
      InetAddress caller = InetAddress.getByName(remoteAddr);
      if (caller.isLoopbackAddress() ||
          caller.equals(InetAddress.getLocalHost())) {
        return true;  // localhost is allowed access
      }
      String gsaHost = Context.getInstance().getGsaFeedHost();
      InetAddress[] gsaAddrs = InetAddress.getAllByName(gsaHost);
      for (int i = 0; i < gsaAddrs.length; i++) {
        if (caller.equals(gsaAddrs[i])) {
          return true;  // GSA is allowed access
        }
      }
      LOGGER.warning("Denying caller: " + caller );
    } catch (UnknownHostException uhe) {
      // Unknown host - fall through to fail.
      LOGGER.log(Level.WARNING, "Denying caller:" + remoteAddr, uhe);
    }
    return false;
  }
}
