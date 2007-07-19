// Copyright (C) 2006 Google Inc.
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
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.BinaryValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;


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

  public void take(Document document, String connectorName) {

    String docid = null;

    printStream.println("<document>");

    // first take care of some special attributes
    Property property = null;

    // we just do the DOCID first so it is easy to find
    try {
      if (!document.findProperty(SpiConstants.PROPNAME_DOCID)) {
        throw new IllegalArgumentException(SpiConstants.PROPNAME_DOCID
            + " is missing");
      }
      property = document.getProperty();
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

    processProperty(property);

    try {
      if (!document.findProperty(SpiConstants.PROPNAME_CONTENTURL)) {
        if (!document.findProperty(SpiConstants.PROPNAME_CONTENT)) {
          throw new IllegalArgumentException("Both "
              + SpiConstants.PROPNAME_CONTENTURL + " and "
              + SpiConstants.PROPNAME_CONTENT + " are missing");
        }
        property = document.getProperty();
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
    processProperty(property);

    try {
      while (document.nextProperty()) {
        property = document.getProperty();
        String name = property.getPropertyName();
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

  private void processProperty(Property property) {
    String name;
    try {
      property.nextValue();
      name = property.getPropertyName();
      // if we have a contentfile property, we want to stream the InputStream
      // to demonstrate that we don't blow up memory
      Value v = property.getValue();
      if (v instanceof BinaryValue) {
        InputStream contentStream = ((BinaryValue) v).getInputStream();
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
      do {
        printStream.println("<" + name + ">");
        printStream.println(v.toString());
        printStream.println("</" + name + ">");
      } while (property.nextValue());
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

  public int getTotalDocs() {
    return totalDocs;
  }

}
