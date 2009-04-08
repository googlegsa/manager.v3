// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.security.connectors.simplecookie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexCookieIdentityExtractor implements CookieIdentityExtractor {

  final Pattern pattern;

  public RegexCookieIdentityExtractor(String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  public String extract(String s) {
    Matcher m = pattern.matcher(s);
    if (!m.find()) {
      return null;
    }
    String result = m.group(1);
    if (result != null) {
      return result;
    }
    result = m.group();
    return null;
  }

}
