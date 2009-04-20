// Copyright (C) 2008, 2009 Google Inc.
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

package com.google.enterprise.security.identity;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of identity elements that share the same username/password for every user.
 * This grouping is used to partition the login form.
 */
public class CredentialsGroupConfig {

  private final String name;      // A string to identify this group in programs.
  private final String humanName; // A string to identify this group in the login form.
  private final List<IdentityElementConfig> elements;

  public CredentialsGroupConfig(String name, String humanName) {
    this.name = name;
    this.humanName = humanName;
    elements = new ArrayList<IdentityElementConfig>();
  }

  // For upwards compatibility:
  public CredentialsGroupConfig(String humanName) {
    this(humanName, humanName);
  }

  public String getName() {
    return name;
  }

  public String getHumanName() {
    return humanName;
  }

  public void addElement(IdentityElementConfig element) {
    if (elements.contains(element)) {
      return;
    }
    elements.add(element);
  }

  public List<IdentityElementConfig> getElements() {
    return elements;
  }
}
