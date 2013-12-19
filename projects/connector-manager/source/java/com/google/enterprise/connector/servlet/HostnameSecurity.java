// Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.servlet;

import com.google.common.base.Objects;
import com.google.enterprise.connector.manager.Context;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.auth.x500.X500Principal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter that stops the filter chain if the client is not granted access, based
 * on its IP or client certificate. If using client certificates, then the
 * client must provide a client certificate whose Subject has a Common Name that
 * appears within the allowed hosts list. If not using client certificates, the
 * client's IP must match the IP of one of the hosts in the allowed hosts list.
 *
 * <p>By default, only gsa.feed.host is in the allowed hosts list. If the
 * allowedHostsConfigName init parameter is provided, then that value is looked
 * up in the connectorManagerProperties and is treated as a comma-separated list
 * of allowed hosts to allow in addition to the GSA.
 *
 * <p>By default, client certificates are not used for security. If the
 * {@code useClientCertificateSecurityConfigName} init parameter is provided,
 * then that value is looked up in the connectorManagerProperties and is parsed
 * as a boolean. When {@code true}, it will enable client certificate security
 * instead of IP-based.
 */
public class HostnameSecurity implements Filter {
  private static Logger LOGGER
      = Logger.getLogger(HostnameSecurity.class.getName());

  private FilterConfig filterConfig;
  private Set<String> allowedAccessCommonNames;
  private Set<InetAddress> allowedAccessAddresses;
  private boolean useClientCertificateSecurity;

  private String gsaHostInUse;

  @Override
  public void init(FilterConfig config) {
    LOGGER.fine("init");
    this.filterConfig = config;
    loadConnectorConfig(filterConfig);
    LOGGER.info("init done.");
  }

  private void loadConnectorConfig(FilterConfig config) {
    allowedAccessCommonNames = new HashSet<String>();
    allowedAccessAddresses = new HashSet<InetAddress>();

    Properties props = Context.getInstance().getConnectorManagerProperties();
    String useClientCertificateSecurityConfigName
        = config.getInitParameter("useClientCertificateSecurityConfigName");
    this.useClientCertificateSecurity = false;
    if (useClientCertificateSecurityConfigName != null) {
      this.useClientCertificateSecurity = Boolean.valueOf(props.getProperty(
          useClientCertificateSecurityConfigName));
    }
    if (this.useClientCertificateSecurity) {
      LOGGER.info("Using client certificate-based security");
    } else {
      LOGGER.info("Using IP-based security");
    }

    String gsaFeedHost = props.getProperty(Context.GSA_FEED_HOST_PROPERTY_KEY);
    gsaHostInUse = gsaFeedHost;
    if (gsaFeedHost != null) {
      allowedAccessCommonNames.add(gsaFeedHost.toLowerCase(Locale.ENGLISH));
    }
    String allowedHostsConfigName
        = config.getInitParameter("allowedHostsConfigName");
    String allowedHosts = "";
    if (allowedHostsConfigName != null) {
      allowedHosts = props.getProperty(allowedHostsConfigName, "");
    }
    for (String hostname : allowedHosts.split(",")) {
      hostname = hostname.trim();
      if ("".equals(hostname)) {
        continue;
      }
      allowedAccessCommonNames.add(hostname.toLowerCase(Locale.ENGLISH));
    }
    String filterName = config.getFilterName();
    LOGGER.log(Level.CONFIG, "When using client certificates, common names that"
               + " are permitted in {0}: {1}",
               new Object[] {filterName, allowedAccessCommonNames});

    for (String hostname : allowedAccessCommonNames) {
      try {
        InetAddress[] ips = InetAddress.getAllByName(hostname);
        allowedAccessAddresses.addAll(Arrays.asList(ips));
      } catch (UnknownHostException ex) {
        LOGGER.log(Level.WARNING, "Could not resolve hostname. Not adding it to"
                   + " full access list of IPs: " + hostname, ex);
      }
    }
    LOGGER.log(Level.CONFIG,
               "When not using client certificates, IPs that are permitted in "
               + "{0}: {1}", new Object[] {filterName, allowedAccessAddresses});
  }

  @Override
  public void destroy() {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    if (isAllowed(request)) {
      chain.doFilter(request, response);
    } else {
      ((HttpServletResponse) response)
          .sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  protected boolean isAllowed(ServletRequest request) {
    String currentGsaHost = Context.getInstance().getGsaFeedHost();
    if (!Objects.equal(gsaHostInUse, currentGsaHost)) {
      // The GSA hostname has changed. Update the allowedAccess sets.
      LOGGER.info("GSA hostname changed; reloading config.");
      loadConnectorConfig(filterConfig);
    }

    if (!useClientCertificateSecurity) {
      InetAddress addr;
      try {
        addr = InetAddress.getByName(request.getRemoteAddr());
      } catch (UnknownHostException ex) {
        throw new AssertionError(ex);
      }
      boolean allowed = allowedAccessAddresses.contains(addr);
      if (!allowed) {
        LOGGER.log(Level.WARNING, "Denying caller: {0}", addr);
      }
      return allowed;
    } else {
      if (!request.isSecure()) {
        LOGGER.log(Level.WARNING, "Denying caller: unencrypted channel");
        return false;
      }
      Object o = request.getAttribute("javax.servlet.request.X509Certificate");
      if (o == null) {
        LOGGER.log(Level.WARNING, "Denying caller: no client certificate");
        return false;
      }
      if (!(o instanceof X509Certificate[])) {
        LOGGER.log(Level.WARNING, "Denying caller: unexpected certificate "
                   + "class: " + o.getClass().getName());
        return false;
      }
      X509Certificate[] certificate = (X509Certificate[]) o;
      if (certificate == null || certificate.length < 1) {
        LOGGER.log(Level.WARNING, "Denying caller: no client certificate");
        return false;
      }
      X500Principal principal = certificate[0].getSubjectX500Principal();
      LdapName dn;
      try {
        // getName() provides RFC2253-encoded data.
        dn = new LdapName(principal.getName());
      } catch (InvalidNameException e) {
        // Getting here may represent a bug in the standard libraries.
        LOGGER.log(Level.WARNING, "Denying caller: non-parsable Subject");
        return false;
      }
      String commonName = null;
      for (Rdn rdn : dn.getRdns()) {
        if ("CN".equalsIgnoreCase(rdn.getType())
            && (rdn.getValue() instanceof String)) {
          commonName = (String) rdn.getValue();
          break;
        }
      }
      if (commonName == null) {
        LOGGER.log(Level.WARNING, "Denying caller: could not get Common Name");
        return false;
      }
      commonName = commonName.toLowerCase(Locale.ENGLISH);
      boolean allowed = allowedAccessCommonNames.contains(commonName);
      if (!allowed) {
        LOGGER.log(Level.WARNING, "Denying caller: {0}", commonName);
      }
      return allowed;
    }
  }
}
