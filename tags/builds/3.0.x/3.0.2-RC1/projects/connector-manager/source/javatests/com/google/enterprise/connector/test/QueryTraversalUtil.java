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

package com.google.enterprise.connector.test;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;

/**
 * This class provides a simple end-to-end test for an SPI implementation's
 * TraversalManager. The way this class uses the SPI's traversal calls is close
 * to the way that the connector manager does, but this is a single
 * self-contained class. See the comments to understand how the connector
 * manager differs from this simple test.
 */
public class QueryTraversalUtil {

  public static void runTraversal(TraversalManager queryTraversalManager,
      int batchHint) throws RepositoryException {

    queryTraversalManager.setBatchHint(batchHint);

    DocumentList documentList = queryTraversalManager.startTraversal();
    // The real connector manager will not always start from the beginning.
    // It will start from the beginning if it receives an explicit admin
    // command to do so, or if it thinks that it has never run this connector
    // before. It decides whether it has run a connector before by storing
    // every checkpoint it receives from
    // the connector. If it can find no stored checkpoint, it assumes that
    // it has never run this connector before and starts from the beginning,
    // as here.

    while (documentList != null) {
      int counter = 0;
      Document document = null;
      while ((document = documentList.nextDocument()) != null) {
        processOneDocument(document);
        counter++;
        if (counter == batchHint) {
          // this test program only takes batchHint results from each
          // resultSet. The real connector manager may take fewer - for
          // example, if it receives a shutdown request
          break;
        }
      }

      String checkPointString = documentList.checkpoint();

      if (counter == 0) {
        // this test program stops if it receives zero results in a resultSet.
        // the real connector Manager might wait a while, then try again
        break;
      }

      documentList = queryTraversalManager.resumeTraversal(checkPointString);
      // the real connector manager will call checkpoint (as here) as soon
      // as possible after processing the last document it wants to process.
      // It would then store the checkpoint string it received in persistent
      // store.
      // Unlike here, it might not then immediately turn around and call
      // resumeTraversal. For example, it may have received a shutdown command,
      // so it won't call resumeTraversal again until it starts up again.
      // Or, it may be running this connector on a schedule and there may be a
      // scheduled pause.
    }
  }

  public static void processOneDocument(Document document)
      throws RepositoryException {

    // every document should have a name
    String name = Value.getSingleValueString(document,
        SpiConstants.PROPNAME_DOCID);

    // if a CONTENTURL was specified, use it
    String contentSnippet = Value.getSingleValueString(document,
        SpiConstants.PROPNAME_CONTENTURL);

    if (contentSnippet == null) {
      // if there is no contentUrl, the connector manager will ask for
      // the content property and will base-64 encode it. Here we will
      // only access the first so many characters of the content and
      // print it out
      contentSnippet = Value.getSingleValueString(document,
          SpiConstants.PROPNAME_CONTENT);
      if (contentSnippet == null) {
        contentSnippet = "no content";
      }
    }
    // here the real content manager would format the document as
    // required by the feed API and send it to the GSA.
    System.out.println("Document " + name);
    System.out.println("Content");
    System.out.println(contentSnippet);
  }
}
