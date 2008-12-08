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

package com.google.enterprise.session.object;

import com.google.enterprise.security.manager.SessionInterface;

public class SessionCookie extends SessionObject {

  private static final String NAME_KEY = "name";
  private static final String VALUE_KEY = "value";
  private static final String DOMAIN_KEY = "domain";
  private static final String PATH_KEY = "path";
  private static final String COMMENT_KEY = "comment";
  private static final String IS_SECURE_KEY = "isSecure";
  private static final String MAX_AGE_KEY = "maxAge";
  private static final String VERSION_KEY = "version";

  public SessionCookie(SessionInterface session, String name, String value) {
    super(session);
    setString(NAME_KEY, name);
    setValue(value);
    setVersion(0);
  }

  public String getName() {
    return getString(NAME_KEY);
  }

  public String getValue() {
    return getString(VALUE_KEY);
  }

  public void setValue(String value) {
    setString(VALUE_KEY, value);
  }

  public String getDomain() {
    return getString(DOMAIN_KEY);
  }

  public void setDomain(String domain) {
    setString(DOMAIN_KEY, domain);
  }

  public String getPath() {
    return getString(PATH_KEY);
  }

  public void setPath(String path) {
    setString(PATH_KEY, path);
  }

  public String getComment() {
    return getString(COMMENT_KEY);
  }

  public void setComment(String comment) {
    setString(COMMENT_KEY, comment);
  }

  public boolean getSecure() {
    return getBoolean(IS_SECURE_KEY);
  }

  public void setSecure(boolean isSecure) {
    setBoolean(IS_SECURE_KEY, isSecure);
  }

  public int getMaxAge() {
    return getInt(MAX_AGE_KEY);
  }

  public void setMaxAge(int maxAge) {
    setInt(MAX_AGE_KEY, maxAge);
  }

  public int getVersion() {
    return getInt(VERSION_KEY);
  }

  public void setVersion(int version) {
    setInt(VERSION_KEY, version);
  }
}
