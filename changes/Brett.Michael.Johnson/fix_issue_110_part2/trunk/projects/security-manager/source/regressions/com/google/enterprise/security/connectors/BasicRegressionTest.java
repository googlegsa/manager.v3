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

package com.google.enterprise.security.connectors;

import com.google.enterprise.saml.common.GsaConstants;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;

public class BasicRegressionTest extends TestCase {

  private static final String entity =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
      "<soap11:Body>" +
      "<samlp:ArtifactResolve" +
      " ID=\"_2b618db4d626929fd563a6556597d262\"" +
      " IssueInstant=\"2008-11-15T07:09:41.127Z\" Version=\"2.0\"" +
      " xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\">" +
      "<saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" +
      GsaConstants.GSA_ISSUER +
      "</saml:Issuer>" +
      "<samlp:Artifact>" +
      "AAQAAEKQJVvv+M3ofQmBLdUYHuk682uBPl1P6laijlxzGK3/VPXbcLAV9zM=" +
      "</samlp:Artifact>" +
      "</samlp:ArtifactResolve>" +
      "</soap11:Body>" +
      "</soap11:Envelope>\n";

  public void testSamlArtifact() throws Exception {
    WebRequest request =
        new PostMethodWebRequest("http://localhost:8973/security-manager/samlartifact",
                                 new ByteArrayInputStream(entity.getBytes("UTF-8")),
                                 "text/xml");
    request.setHeaderField("SOAPAction", "http://www.oasis-open.org/committees/security");
    (new WebConversation()).getResponse(request);
  }
}
