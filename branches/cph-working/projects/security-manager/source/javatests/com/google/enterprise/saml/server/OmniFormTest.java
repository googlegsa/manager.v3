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

package com.google.enterprise.saml.server;

import com.google.enterprise.sessionmanager.CredentialsGroup;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpPost;

/**
 * Unit test for SamlAuthn handler.
 */
public class OmniFormTest extends TestCase {

  private OmniForm formOne;
     
  @Override
  public void setUp() {
    CSVReader reader = null;
    try {
      FileReader file = new FileReader("testdata/AuthSites.conf");
      reader = new CSVReader(file);
    } catch(Exception e) {
      System.out.println("CSV parsing aborted:" + e.toString());
    }

    try {
      formOne = new OmniForm(reader, "whatever");
    } catch (Exception e) {
      System.out.println("CSV parsing problematic: " + e.toString());
    }
  }


  /**
   * Make sure we can construct login form.
   *
   * @throws UnsupportedEncodingException
   */
  public void testWriteForm() throws UnsupportedEncodingException {
    String form = formOne.writeForm(null);
    System.out.println(form);
  }
  
  /**
   * Make sure login form has slight tweak if we already have some data.
   */
  public void testRewriteForm() {
    UserIdentity[] ids = new UserIdentity[4];
    ids[0] = UserIdentity.compatNew("voyager", "foobar", null);
    ids[0].setVerified();
    ids[1] = UserIdentity.compatNew(null, null, null);
    ids[2] = UserIdentity.compatNew(null, null, null);
    ids[3] = UserIdentity.compatNew(null, null, null);
    String form = formOne.writeForm(getGroups(ids));
    System.out.println(form);
  }
  
  /**
   * Make sure we can extract data from the form.
   * @throws IOException 
   */
  public void testParseForm() throws IOException {
    final UserIdentity[] ids = new UserIdentity[2];
    ids[0] = UserIdentity.compatNew("fred", "frocks", null);
    ids[1] = UserIdentity.compatNew("jane", "jrocks", null);

    MockHttpServletRequest request = makeMockHttpPost(null, "http://localhost/");
    request.addParameter("u1", "fred");
    request.addParameter("pw1", "frocks");
    request.addParameter("u2", "jane");
    request.addParameter("pw2", "jrocks");

    formOne.parse(request, getGroups(ids));

    assertEquals("fred", ids[0].getUsername());
    assertEquals("frocks", ids[0].getPassword());

    assertEquals("jane", ids[1].getUsername());
    assertEquals("jrocks", ids[1].getPassword());
  }

  private List<CredentialsGroup> getGroups(UserIdentity[] ids) {
    List<CredentialsGroup> groups = new ArrayList<CredentialsGroup>();
    for (UserIdentity id: ids) {
      groups.add(id.getCredentials().getGroup());
    }
    return groups;
  }
}
