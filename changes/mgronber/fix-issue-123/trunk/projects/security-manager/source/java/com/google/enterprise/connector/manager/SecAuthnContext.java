// Copyright (C) 2008 Google Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;

public class SecAuthnContext {

  private final List<Cookie> cookies;

  public SecAuthnContext() {
    cookies = new ArrayList<Cookie>();
  }

  public Collection<Cookie> getCookies() {
    return Collections.unmodifiableCollection(cookies);
  }

  public Cookie getCookieNamed(String name) {
    for (Cookie c: cookies) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  public void addCookie(Cookie c) {
    cookies.add(c);
  }
}
