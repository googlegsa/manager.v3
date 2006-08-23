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

package com.google.enterprise.connector.test;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.ValueType;

import java.util.Iterator;

/**
 * This class provides a simple end-to-end test for an SPI implementation's
 * QueryTraversalManager. The way this class uses the SPI's traversal calls is
 * close to the way that the connector manager does, but this is a single
 * self-contained class. See the comments to understand how the connector
 * manager differs from this simple test.
 */
public class QueryTraversalTest {

  public static void runTraversal(QueryTraversalManager queryTraversalManager,
      int batchHint) throws RepositoryException {

    queryTraversalManager.setBatchHint(batchHint);

    ResultSet resultSet = queryTraversalManager.startTraversal();
    // The real connector manager will not always start from the beginning.
    // It will start from the beginning if it receives an explicit admin
    // command to do so, or if it thinks that it has never run this connector
    // before. It decides whether it has run a connector before by storing
    // every checkpoint it receives from
    // the connector. If it can find no stored checkpoint, it assumes that
    // it has never run this connector before and starts from the beginning,
    // as here.
    if (resultSet == null) {
      // in this test program, we will stop in this situation. The real
      // connector manager might wait for a while, then try again
      return;
    }

    while (true) {
      int counter = 0;
      PropertyMap pm = null;
      for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
        pm = (PropertyMap) iter.next();
        Property nameProp = pm.getProperty(SpiConstants.PROPNAME_DOCID);
        // every document should have a name
        String name = nameProp.getValue().getString();

        Property contentUrlProp = pm
            .getProperty(SpiConstants.PROPNAME_CONTENTURL);

        Property contentProp = null;
        String contentSnippet = "...no content...";
        // contentSnippet is just for display in this test

        if (contentUrlProp != null) {
          contentSnippet = contentUrlProp.getValue().getString();
          // if a contentUrl is present, the content manager will
          // just pass it to the GSA. In this test, we print it out
        } else {
          // if there is no contentUrl, the connector manager will ask for
          // the content property and will base-64 encode it. Here we will
          // only access the first so many characers of the content and
          // print it out
          contentProp = pm.getProperty(SpiConstants.PROPNAME_CONTENT);
          if (contentProp != null) {
            Value contentVal = contentProp.getValue();
            ValueType contentType = contentVal.getType();
            if (contentType == ValueType.STRING) {
              String fullContent = contentVal.getString();
              int snippetLength = 20;
              if (fullContent == null) {
                contentSnippet = "...null content...";
              } else if (fullContent.length() == 0) {
                contentSnippet = "...empty content...";
              } else if (fullContent.length() > snippetLength) {
                contentSnippet = fullContent.substring(0, snippetLength);
              } else {
                contentSnippet = fullContent;
              }
            } else if (contentType == ValueType.BINARY) {
              contentSnippet = "...binary content...";
            } else {
              throw new IllegalArgumentException("content value is "
                  + contentType + ": it should be string or binary ");
            }
          }
        }
        // here the real content manager would format the document as
        // required by the feed API and send it to the GSA.
        System.out.println("Document " + name);
        System.out.println("Content");
        System.out.println(contentSnippet);
        counter++;
        if (counter == batchHint) {
          // this test program only takes batchHint results from each
          // resultSet. The real connector manager may take fewer - for
          // example, if it receives a shutdown request
          break;
        }
      }

      if (counter == 0) {
        // this test program stops if it receives zero results in a resultSet.
        // the real connector Manager might wait a while, then try again
        break;
      }

      String checkPointString = queryTraversalManager.checkpoint(pm);
      resultSet = queryTraversalManager.resumeTraversal(checkPointString);
      // the real connector manager will call checkpoint (as here) as soon
      // as possible after processing the last property map it wants to process.
      // It would then store the checkpoint string it received in persistent
      // store.
      // Unlike here, it might not then immediately turn around and call
      // resumeTraversal. For example, it may have received a shutdown command,
      // so it won't call resumeTraversal again until it starts up again.
      // Or, it may be running this connector on a schedule and there may be a
      // scheduled pause.
    }
  }
}
