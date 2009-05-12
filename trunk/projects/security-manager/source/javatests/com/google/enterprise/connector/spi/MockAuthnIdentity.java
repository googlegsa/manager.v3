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

import com.google.enterprise.connector.common.cookie.CookieSet;
import com.google.enterprise.connector.spi.AbstractAuthnIdentity;

import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

public class MockAuthnIdentity extends AbstractAuthnIdentity {

  private final CookieSet cookies;
  private final String sampleUrl;
  
  private String username;
  private String domain;
  private String password;
  
  public MockAuthnIdentity(String sampleUrl) {
    this.cookies = new CookieSet();
    this.sampleUrl = sampleUrl;
    username = null;
    domain = null;
    password = null;
  }

  @Override
  public Collection<Cookie> getCookies() {
    return cookies;
  }

  @Override
  public String getSampleUrl() {
    return sampleUrl;
  }

  @Override
  public HttpSession getSession() {
    return null;
  }

  @Override
  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String getDomain() {
    return domain;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }
}
