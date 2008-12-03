// Copyright 2007-8 Google Inc.
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

package com.google.enterprise.connector.manager;

import com.google.enterprise.connector.spi.AuthenticationIdentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;

public class UserPassIdentity implements AuthenticationIdentity {

  private final String username;
  private final String password;
  
  private final Map cookieJar;

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public UserPassIdentity(final String username, final String password) {
    this.username = username;
    this.password = password;
    cookieJar = new HashMap();
  }

  public String getCookie(String cookieName) {
    if (cookieName == null || cookieName.length() < 1) {
      throw new IllegalArgumentException();
    }
    return (String) cookieJar.get(cookieName);
  }

  public String setCookie(String cookieName, String value) {
    if (cookieName == null || cookieName.length() < 1) {
      throw new IllegalArgumentException();
    }
    if (value == null || value.length() < 1) {
      return (String) cookieJar.remove(cookieName);
    }
    return (String) cookieJar.put(cookieName,value);
  }

  public void setCookie(Cookie c) {
    throw new IllegalArgumentException();
  }
  
  public Set getCookieNames() {
    Set result = cookieJar.keySet();
    return result;
  }
  
  public String getLoginUrl() {
    return null;
  }

}
