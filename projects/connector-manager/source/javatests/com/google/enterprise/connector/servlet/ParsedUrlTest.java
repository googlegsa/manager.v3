// Copyright 2006 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.servlet;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ParsedUrlTest extends TestCase {

  public void testParsedUrl() {
    doParsedUrlTest("googleconnector://connector1.localhost/doc?docid=foo1",
        "connector1", "foo1", ConnectorMessageCode.SUCCESS);
    doParsedUrlTest("googleconnector://connector2.localhost/doc?docid=foo2",
        "connector2", "foo2", ConnectorMessageCode.SUCCESS);
    doParsedUrlTest("googleconnector://connector3.localhost/doc?docid=foo3",
        "connector3", "foo3", ConnectorMessageCode.SUCCESS);
    doParsedUrlTest("googleconnector://connector1/doc?docid=foo1",
        "connector1", "foo1", ConnectorMessageCode.SUCCESS);
    doParsedUrlTest("googleconnector://connector1/doc?bar=baz&docid=foo1",
        "connector1", "foo1", ConnectorMessageCode.SUCCESS);
    doParsedUrlTest(
        "googleconnector://connector1/doc?bar=baz&argle=bargle&docid=foo1",
        "connector1", "foo1", ConnectorMessageCode.SUCCESS);
    doParsedUrlTest(
        "googleconnector://connector1/doc?bar=baz&docid=foo1&argle=bargle",
        "connector1", "foo1", ConnectorMessageCode.SUCCESS);
    doParsedUrlTest("googleconnector://Connector3.localhost/doc?DOCID=foo1",
        "Connector3", "foo1", ConnectorMessageCode.SUCCESS);
  }

  private void doParsedUrlTest(String url, String expectedName,
      String expectedDocid, int expectedStatus) {
    ParsedUrl parsedUrl = new ParsedUrl(url);
    Assert.assertEquals(expectedStatus, parsedUrl.getStatus());
    Assert.assertEquals(expectedName, parsedUrl.getConnectorName());
    Assert.assertEquals(expectedDocid, parsedUrl.getDocid());
    Assert.assertEquals(url, parsedUrl.getUrl());
  }

}
