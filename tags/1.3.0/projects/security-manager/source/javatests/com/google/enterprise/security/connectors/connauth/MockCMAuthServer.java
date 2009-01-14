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

package com.google.enterprise.security.connectors.connauth;

import com.google.enterprise.common.PostableHttpServlet;
import com.google.enterprise.connector.servlet.Authenticate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Just reuse the servlet of connector manager, which is not interesting as
 * it is not backed up by actual connectors to repositories. But it's still
 * sufficient to verify our SecMgr connector connector is speaking the right
 * AuthnRequest protocol.
 */
public class MockCMAuthServer extends Authenticate 
  implements PostableHttpServlet {
  private static final long serialVersionUID = 1L;

  protected final Map<String, String> passwordMap;

  public MockCMAuthServer() {
    passwordMap = new HashMap<String, String>();
    passwordMap.put("joe", "plumber");
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    try {
      super.doPost(req, resp);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
