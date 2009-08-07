// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.security.identity.AuthnMechanism;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.Cookie;

/**
 * A base for implementing SecAuthnIdentity classes.
 */
public abstract class AbstractAuthnIdentity implements SecAuthnIdentity {

  private VerificationStatus status;

  protected AbstractAuthnIdentity() {
    status = VerificationStatus.TBD;
  }

  /**
   * Associate a cookie with this identity. If the cookie's name is the same as
   * a previously associated cookie, the implementation is allowed to overwrite
   * the previous cookie, so don't count on there being multiple cookies with
   * the same name.
   *
   * @param c The cookie to associate.
   */
  public void addCookie(Cookie c) {
    getCookies().add(c);
  }

  /**
   * Get an associated cookie by name.
   *
   * @param name The name of the cookie to return.
   * @return The associated cookie, or null if no such cookie.
   */
  public Cookie getCookieNamed(String name) {
    for (Cookie c : getCookies()) {
      if (c.getName().equalsIgnoreCase(name)) {
        return c;
      }
    }
    return null;
  }

  // For testing:
  public void clearCookies() {
    getCookies().clear();
  }

  /**
   * Get the verification status for this identity.
   *
   * @return The identity's verification status.
   */
  public VerificationStatus getVerificationStatus() {
    return status;
  }

  /**
   * Set the verification status for this identity.
   *
   * @param status The new verification status.
   */
  public void setVerificationStatus(VerificationStatus status) {
    this.status = status;
  }

  /**
   * Generate a json representation of the contents.
   *
   * @return The json string.
   */
  public String toJson() {
    JSONObject jo = toJsonObject();
    return jo.toString();
  }

  protected JSONObject toJsonObject() {
    JSONObject jo = new JSONObject();
    try {
      jo.put("username", getUsername());
      jo.put("password", getPassword());
      jo.put("domain", getDomain());
      if (getCookies().size() > 0) {
        jo.put("cookies", new JSONArray());
        for (Cookie c : getCookies()) {
          jo.accumulate("cookies", cookieToJsonObject(c));
        }
      }
      jo.put("verificationStatus", getVerificationStatus().toString());
      jo.put("sampleUrl", getSampleUrl());
      jo.put("authority", getAuthority());
      jo.put("type", mechToTypeString(getMechanism()));
    } catch (JSONException e) {
      // this should never happen -- because our data was validated already
      throw new IllegalStateException(e);
    }
    return jo;
  }

  private static JSONObject cookieToJsonObject(Cookie c) {
    JSONObject jo = new JSONObject();
    try {
      jo.put("secure", c.getSecure());
      jo.put("comment", c.getComment());
      jo.put("domain", c.getDomain());
      jo.put("maxAge", c.getMaxAge());
      jo.put("name", c.getName());
      jo.put("path", c.getPath());
      jo.put("value", c.getValue());
      jo.put("version", c.getVersion());
    } catch (JSONException e) {
      // this should never happen -- because our data was validated already
      throw new IllegalStateException(e);
    }
    return jo;
  }

  public static String mechToTypeString(AuthnMechanism m) {
    String type = null;
    // we use string constants, rather than simply this.getMechanism().toString()    // to emphasize that these constant string listed here are part of the
    // protocol.  If the enum names change, these names should not
    switch (m) {
      case BASIC_AUTH:
        type = "BASIC_AUTH";
        break;
      case FORMS_AUTH:
        type = "FORMS_AUTH";
        break;
      case SAML:
        type = "SAML";
        break;
      case SSL:
        type = "SSL";
        break;
      case CONNECTORS:
        type = "CONNECTORS";
        break;
      case SPNEGO_KERBEROS:
        type = "SPNEGO_KERBEROS";
        break;
      default:
        break;
    }
    return type;
  }
}
