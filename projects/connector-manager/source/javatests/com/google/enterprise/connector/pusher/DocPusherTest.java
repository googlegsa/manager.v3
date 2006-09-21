// Copyright (C) 2006 Google Inc.
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


package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.jcradaptor.SpiQueryTraversalManagerFromJcr;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.MockUrlConn;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Iterator;

import javax.jcr.query.QueryManager;

/**
 * Tests DocPusher.
 */
public class DocPusherTest extends TestCase {

  private static final String GSA_HOST = "gsahost";
  private static final int GSA_PORT = 12345;
  private static final String DATASOURCE = "datasource";
  
  public void testTake() throws RepositoryException {
    String expectedXml = "<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE gsafeed "
      + "PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"gsafeed.dtd\"><gsafeed>"
      + "<header><datasource>datasource</datasource><feedtype>full</feedtype>"
      + "</header><group><record url=\"http://www.sometesturl.com/test\""
      + " mimetype=\"text/plain\" last-modified=\"Tue, 15 "
      + "Nov 1994 12:45:26 GMT\"><content encoding=\"base64binary\">"
      + "bm93IGlzIHRoZSB0aW1l</content></record></group></gsafeed>";
    String resultXML;
    String gsaExpectedResponse = "Mock response";
    String gsaActualResponse;
    
    MockRepositoryEventList mrel =
       new MockRepositoryEventList("MockRepositoryEventLog3.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    QueryTraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);
    
    MockUrlConn mockUrlConn = new MockUrlConn();
    DocPusher dpusher = new DocPusher(GSA_HOST, GSA_PORT, DATASOURCE, mockUrlConn);
    
    MockRepositoryDocument doc = r.getStore().getDocByID("doc1");
    ResultSet resultSet = qtm.startTraversal();
    
    for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
      PropertyMap propertyMap = (PropertyMap) iter.next();
      dpusher.take(propertyMap);
      resultXML = dpusher.getXmlData();
      gsaActualResponse = dpusher.getGsaResponse();
      Assert.assertEquals(true, expectedXml.equals(resultXML));
      Assert.assertEquals(true, gsaExpectedResponse.equals(gsaActualResponse));
      
    }
  }

}
