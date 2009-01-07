// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.common;

import java.util.Locale;

import junit.framework.TestCase;

public class I18NUtilTest extends TestCase {

  /**
   * Test method for {@link com.google.enterprise.connector.common.I18NUtil#getLocaleFromStandardLocaleString(java.lang.String)}.
   */
  public void testGetLocaleFromStandardLocaleString() {
    doOneLocaleTest("en",new Locale("en"));
    doOneLocaleTest("en",Locale.ENGLISH);
    doOneLocaleTest("fr",new Locale("fr"));
    doOneLocaleTest("fr_CA",new Locale("fr","CA"));
    doOneLocaleTest("fr_CA",Locale.CANADA_FRENCH);
    doOneLocaleTest("fr-CA",Locale.CANADA_FRENCH);
    doOneLocaleTest("FR-ca",Locale.CANADA_FRENCH);
    doOneLocaleTest("",Locale.getDefault());
  }

  private void doOneLocaleTest(String localeString, Locale expectedLocale) {
    Locale l = I18NUtil.getLocaleFromStandardLocaleString(localeString);
    assertEquals(expectedLocale, l);
  }
}
