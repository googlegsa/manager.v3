// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utility methods for SSL.
 *
 * @since 2.8
 */
public class SslUtil {
  /** An all-trusting TrustManager for SSL URL validation. */
  private static final TrustManager[] trustAllCerts =
      new TrustManager[] {
          new X509TrustManager() {
              public X509Certificate[] getAcceptedIssuers() {
                  return null;
              }
              public void checkServerTrusted(
                  X509Certificate[] certs, String authType) {
                    return;
              }
              public void checkClientTrusted(
                  X509Certificate[] certs, String authType) {
                    return;
              }
          }
      };

  /** An all-trusting HostnameVerifier for SSL URL validation. */
  private static final HostnameVerifier trustAllHosts =
      new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };

  private static SSLSocketFactory getTrustingFactory()
      throws GeneralSecurityException {
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, null);
    return sc.getSocketFactory();
  }

  /**
   * Replaces the default {@code TrustManager} for this
   * connection with one that trusts all certificates, and the default
   * {@code HostnameVerifier} with one that accepts all
   * hostnames.
   *
   * @param conn the HTTPS URL connection
   * @throws GeneralSecurityException if an error occurs setting the properties
   */
  public static void setTrustingHttpsOptions(HttpsURLConnection conn)
      throws GeneralSecurityException {
    conn.setSSLSocketFactory(getTrustingFactory());
    conn.setHostnameVerifier(trustAllHosts);
  }

  /**
   * Replaces the default SSLSocketFactory with one that doesn't verify
   * certificates
   * @return original socket factory
   */
  public static SSLSocketFactory setTrustingDefaultHttpsSocketFactory() {
    SSLSocketFactory original = null;
    SSLSocketFactory factory = null;
    try {
      original = HttpsURLConnection.getDefaultSSLSocketFactory();
      factory = getTrustingFactory();
    } catch (GeneralSecurityException e) {
    }
    if (factory != null) {
      HttpsURLConnection.setDefaultSSLSocketFactory(factory);
      return original;
    } else {
      return null;
    }
  }
  /**
   * Replaces the default HTTPS hostname verifier with one that trusts all
   * hosts
   * @return original hostname verifier
   */
  public static HostnameVerifier setTrustingDefaultHostnameVerifier() {
    HostnameVerifier original = HttpsURLConnection.getDefaultHostnameVerifier();
    HttpsURLConnection.setDefaultHostnameVerifier(trustAllHosts);
    return original;
  }

  /** Prevents instantiation. */
  private SslUtil() {
    throw new AssertionError();
  }
}
