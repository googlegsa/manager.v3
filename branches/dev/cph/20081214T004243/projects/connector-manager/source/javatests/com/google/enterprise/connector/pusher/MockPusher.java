// Copyright (C) 2006-2008 Google Inc.
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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.common.Base64FilterInputStream;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.BinaryValue;

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

  public void take(Document document, String connectorName)
      throws RepositoryException {
    printStream.println("<document>");

    // first take care of some special attributes
    Property property = null;

    // we just do the DOCID first so it is easy to find
    String name = SpiConstants.PROPNAME_DOCID;
    if ((property = document.findProperty(name)) == null) {
      throw new RepositoryDocumentException(SpiConstants.PROPNAME_DOCID
                                            + " is missing");
    }
    processProperty(name, property);

    for (Iterator i = document.getPropertyNames().iterator(); i.hasNext();) {
      name = (String) i.next();
      if (name.equals(SpiConstants.PROPNAME_DOCID)) {
        // we already dealt with these
        continue;
      }
      property = document.findProperty(name);
      processProperty(name, property);
    }

    printStream.println("</document>");
    totalDocs++;
  }

  private void processProperty(String name, Property property)
      throws RepositoryException {
    InputStream contentStream = null;
    InputStream encodedContentStream = null;
    try {
      Value v = property.nextValue();
      // if we have a contentfile property, we want to stream the InputStream
      // to demonstrate that we don't blow up memory
      if (v instanceof BinaryValue) {
        contentStream = ((BinaryValue) v).getInputStream();
        if (null != contentStream) {
          encodedContentStream = new Base64FilterInputStream(contentStream);
        }
        int totalBytesRead = 0;
        if (null != encodedContentStream) {
          int bytesRead = 0;
          byte[] b = new byte[16384];
          try {
            while (-1 != (bytesRead = encodedContentStream.read(b))) {
              totalBytesRead += bytesRead;
            }
          } catch (IOException e) {
            throw new RepositoryDocumentException(
                "Error reading content stream.", e);
          }
        }
        printStream.println("Total bytes read in base64 encoded file: "
            + totalBytesRead);
      }
      do {
        printStream.println("<" + name + ">");
        printStream.println(v.toString());
        printStream.println("</" + name + ">");
      } while ((v = property.nextValue()) != null);
    } finally {
      if (null != encodedContentStream) {
        try { encodedContentStream.close(); } catch (IOException e) {}
      }
      if (null != contentStream) {
        try { contentStream.close(); } catch (IOException e) {}
      }
    }
  }

  public int getTotalDocs() {
    return totalDocs;
  }
}
