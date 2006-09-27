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

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import java.io.PrintStream;
import java.util.Iterator;

public class MockPusher implements Pusher {

  private int totalDocs;
  private PrintStream printStream;

  public MockPusher(PrintStream ps) {
    totalDocs = 0;
    printStream = ps;
  }

  public void take(PropertyMap pm, String connectorName) {

    String docid = null;

    printStream.println("<document>");

    // first take care of some special attributes

    // we just do the DOCID first so it is easy to find
    Property docidProp = null;
    try {
      docidProp = pm.getProperty(SpiConstants.PROPNAME_DOCID);
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    if (docidProp == null) {
      throw new IllegalArgumentException(SpiConstants.PROPNAME_DOCID
        + " is missing");
    }
    processProperty(docidProp);

    Property cProp = null;
    try {
      cProp = pm.getProperty(SpiConstants.PROPNAME_CONTENTURL);
      if (cProp == null) {
        cProp = pm.getProperty(SpiConstants.PROPNAME_CONTENT);
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    if (cProp == null) {
      throw new IllegalArgumentException("Both "
        + SpiConstants.PROPNAME_CONTENTURL + " and "
        + SpiConstants.PROPNAME_CONTENT + " are missing");
    }
    processProperty(cProp);

    try {
      for (Iterator i = pm.getProperties(); i.hasNext();) {
        Property property = (Property) i.next();
        String name = property.getName();
        if (name.startsWith("google:")) {
          if (name.equals(SpiConstants.PROPNAME_CONTENT)
            || name.equals(SpiConstants.PROPNAME_CONTENTURL)
            || name.equals(SpiConstants.PROPNAME_DOCID)) {
            // we already dealt with these
            break;
          }
        }
        processProperty(property);
      }
      totalDocs++;
    } catch (RepositoryException e) {
      throw new IllegalArgumentException();
    }
  }

  private void processProperty(Property p) {
    String name;
    try {
      name = p.getName();
      for (Iterator j = p.getValues(); j.hasNext();) {
        Value v = (Value) j.next();
        printStream.println("<" + name + ">");
        printStream.println(v.getString());
        printStream.println("</" + name + ">");
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

  public int getTotalDocs() {
    return totalDocs;
  }
}
