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

import com.google.enterprise.session.manager.SessionManagerInterface;
import com.google.enterprise.session.metadata.AuthnDomainMetadata;

import java.util.HashMap;
import java.util.Map;

public class SessionRoot extends SessionObject {

  private static final Map<String, SessionRoot> roots = new HashMap<String, SessionRoot>();
  private static final String COOKIES_KEY = "cookies";
  private static final String CREDENTIALS_KEY = "credentials";

  private final SessionManagerInterface sessionManager;
  private final String sessionId;
  private final Map<String, SessionObject> oidCache;

  public static SessionRoot getInstance(SessionManagerInterface sessionManager, String sessionId) {
    synchronized (roots) {
      SessionRoot root = roots.get(sessionId);
      if (root == null) {
        root = new SessionRoot(sessionManager, sessionId);
        roots.put(sessionId, root);
      }
      return root;
    }
  }

  private SessionRoot(SessionManagerInterface sessionManager, String sessionId) {
    init(this, makeOid(SessionRoot.class, 0));
    this.sessionManager = sessionManager;
    this.sessionId = sessionId;
    this.oidCache = new HashMap<String, SessionObject>();
  }

  String getValue(String key) {
    return sessionManager.getValue(sessionId, key);
  }

  void setValue(String key, String value) {
    sessionManager.setValue(sessionId, key, value);
  }

  void cacheOidValue(String oid, SessionObject value) {
    oidCache.put(oid, value);
  }

  <T extends SessionObject> T cachedOidValue(String oid) {
    Class<T> clazz = oidToType(oid);
    return clazz.cast(oidCache.get(oid));
  }

  public synchronized SessionCookie getCookie(String name, boolean create) {
    SessionList<SessionCookie> cookies = getCookies();
    for (SessionCookie cookie: cookies) {
      if (name.equals(cookie.getName())) {
        return cookie;
      }
    }
    if (!create) {
      return null;
    }
    SessionCookie cookie = new SessionCookie(root, name);
    cookies.add(cookie);
    return cookie;
  }

  public synchronized SessionList<SessionCookie> getCookies() {
    SessionList<SessionCookie> cookies = getObject(COOKIES_KEY);
    if (cookies == null) {
      cookies = new SessionList<SessionCookie>(root);
      setObject(COOKIES_KEY, cookies);
    }
    return cookies;
  }

  public synchronized SessionList<AuthnDomainCredentials> getCredentials() {
    SessionList<AuthnDomainCredentials> credentials = getObject(CREDENTIALS_KEY);
    if (credentials == null) {
      credentials = new SessionList<AuthnDomainCredentials>(root);
      for (AuthnDomainMetadata metadata: AuthnDomainMetadata.getAllMetadata()) {
        credentials.add(new AuthnDomainCredentials(root, metadata));
      }
      setObject(CREDENTIALS_KEY, credentials);
    }
    return credentials;
  }
}
