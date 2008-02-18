// Copyright 2008 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDateTime;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrEventIterator;
import com.google.enterprise.connector.mock.jcr.MockJcrNodeIterator;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

/**
 * Simple test for JcrEventDocument.
 */
public class JcrEventDocumentTest extends TestCase {

  /**
   * To simulate normal usage the Event should be pulled from an EventIterator
   * and a Node should be pulled from a NodeIterator.  The Node will be used
   * for the lastModified property needed for the Property that is created.
   * @throws javax.jcr.RepositoryException 
   * @throws PathNotFoundException 
   * @throws RepositoryException 
   */
  public void testJcrEventProperty() throws Exception {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);

    // Create an event iterator over the entire list.
    EventIterator ei = new MockJcrEventIterator(mrel.getEventList().iterator());

    // Create a node iterator over enough of the repository to include a save
    // and delete action
    List docs = r.getStore().dateRange(new MockRepositoryDateTime(10),
        new MockRepositoryDateTime(60));
    NodeIterator ni = new MockJcrNodeIterator(docs.iterator());

    // Now iterate over the event iterator and try to pull a node from the node
    // iterator until it hits the end.  Take these and create a JcrEventDocument
    // instance.
    Event e = null;
    Node n = null;
    while (ei.hasNext()) {
      e = ei.nextEvent();
      if (ni.hasNext()) {
        n = ni.nextNode();
      }
      JcrEventDocument jcrDoc = 
          new JcrEventDocument(e, n.getProperty("jcr:lastModified").getDate());
      assertDocument(jcrDoc, e, n);
    }
  }

  private void assertDocument(JcrEventDocument jcrDoc, Event e, Node n) 
      throws Exception {
    Set propNames = jcrDoc.getPropertyNames();
    assertTrue("Missing property name " + SpiConstants.PROPNAME_DOCID, 
        propNames.contains(SpiConstants.PROPNAME_DOCID));
    assertTrue("Missing property name " + SpiConstants.PROPNAME_ACTION, 
        propNames.contains(SpiConstants.PROPNAME_ACTION));
    assertTrue("Missing property name " + SpiConstants.PROPNAME_LASTMODIFIED, 
        propNames.contains(SpiConstants.PROPNAME_LASTMODIFIED));

    Property prop = jcrDoc.findProperty(SpiConstants.PROPNAME_DOCID);
    assertTrue(prop.nextValue().toString().equals(e.getPath()));
    assertTrue(prop.nextValue() == null);

    prop = jcrDoc.findProperty(SpiConstants.PROPNAME_ACTION);
    String action = "add";
    if (e.getType() == Event.NODE_REMOVED) {
      action = "delete";
    }
    assertTrue(prop.nextValue().toString().equals(action));
    assertTrue(prop.nextValue() == null);

    prop = jcrDoc.findProperty(SpiConstants.PROPNAME_LASTMODIFIED);
    String propVal = prop.nextValue().toString();
    Calendar c = n.getProperty("jcr:lastModified").getDate();
    String nodeVal = Value.calendarToIso8601(c);
    assertTrue(propVal.equals(nodeVal));
    assertTrue(prop.nextValue() == null);
  }
}
