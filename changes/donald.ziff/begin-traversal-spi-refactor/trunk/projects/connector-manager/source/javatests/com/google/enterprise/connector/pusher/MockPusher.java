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

import com.google.enterprise.connector.common.Base64FilterInputStream;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.old.Property;
import com.google.enterprise.connector.spi.old.PropertyMap;
import com.google.enterprise.connector.spi.old.Value;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class MockPusher implements Pusher {

  private int totalDocs;
  private PrintStream printStream;
  
  public MockPusher() {
    this(System.out);
  }

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
        if (null == v.getStream()) {
          printStream.println(v.getString());
        }
        printStream.println("</" + name + ">");
      }
      // if we have a contentfile property, we want to stream the InputStream
      // to demonstrate that we don't blow up memory
      Value v = p.getValue();
      if (null != v.getStream()) {
        InputStream contentStream = v.getStream();
        InputStream encodedContentStream = null;
        if (null != contentStream) {
          encodedContentStream = new Base64FilterInputStream(contentStream);
        }
        int totalBytesRead = 0;
        if (null != encodedContentStream) {
          int bytesRead = 0;
          byte[] b = new byte[4096];
          try {
            while (-1 != (bytesRead = encodedContentStream.read(b))) {
              totalBytesRead += bytesRead;
            }
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        printStream.println("Total bytes read in base64 encoded file: "
          + totalBytesRead);
      }  
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

  public int getTotalDocs() {
    return totalDocs;
  }
}
