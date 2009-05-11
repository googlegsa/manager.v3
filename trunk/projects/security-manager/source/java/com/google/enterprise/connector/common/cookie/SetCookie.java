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

package com.google.enterprise.connector.common.cookie;

import javax.servlet.http.Cookie;

/**
 * Represent the cookie received from Web server via Set-Cookie and
 * Set-Cookie2 HTTP header. Used with parser in SetCookieParser.java.
 */
public class SetCookie extends Cookie {

  private String expires;
  private boolean discard;
  private boolean httpOnly;

  public int[] ports;
  public String commentURL;
  static final int[] EMPTY_PORT = new int[1];

  public SetCookie(String name, String value) {
    super(name, value);
  }

  public void setExpires(String e) {
    expires = e;
  }
  public String getExpires() {
    return expires;
  }

  public void setDiscard(boolean d) {
    discard = d;
  }
  public boolean getDiscard() {
    return discard;
  }

  public void setHttpOnly(boolean h) {
    httpOnly = h;
  }
  public boolean getHttpOnly() {
    return httpOnly;
  }

  @Override
  public String toString() {
    StringBuffer strbuf = new StringBuffer("");
    try {
      strbuf.append(getName()).append("=");
      if ( getValue() != null )
        strbuf.append(getValue());
      if ( getComment() != null )
        strbuf.append("; comment=").append(getComment());
      if ( getDomain() != null )
        strbuf.append("; domain=").append(getDomain());
      if ( getMaxAge() > 0 )
        strbuf.append("; Max-Age=").append(getMaxAge());
      if ( getPath() != null )
        strbuf.append("; path=").append(getPath());
      if ( getVersion() != -1 )
        strbuf.append("; version=").append("" + getVersion());
      if ( getSecure() )
        strbuf.append("; secure");
      if ( httpOnly )
        strbuf.append("; HttpOnly");
      if ( commentURL != null )
        strbuf.append("; commentURL=").append(commentURL);
      if ( expires != null )
        strbuf.append("; expires=").append(expires);
      if ( discard )
        strbuf.append("; discard");

      if ( ports != null && (ports.length != 0) ) {
        strbuf.append("; ports=\"");
        if ( ports != EMPTY_PORT ) {
          for ( int i = 0; i < ports.length; i++ )
            strbuf.append(" " + ports[i]);
        }
        strbuf.append("\"");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return strbuf.toString();
  }
}
