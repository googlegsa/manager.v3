// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.DateValue;

import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

public class JcrDocumentList implements DocumentList {

  private Node startNode;
  private final NodeIterator jcrIterator;
  private Document document = null;

  public JcrDocumentList(final Node thisNode, final NodeIterator nodes) {
    startNode = thisNode;
    jcrIterator = nodes;
  }

  public JcrDocumentList(final NodeIterator nodes) {
    startNode = null;
    jcrIterator = nodes;
  }

  public Document nextDocument() {
    if (startNode != null) {
      document = new JcrDocument(startNode);
      startNode = null;
      return document;
    }
    if (jcrIterator == null) {
      return null;
    }
    if (jcrIterator.hasNext()) {
      document = new JcrDocument(jcrIterator.nextNode());
      return document;
    }
    return null;
  }

  public static String checkpoint(Document document) throws RepositoryException {
    String uuid = Value.getSingleValueString(document,
        SpiConstants.PROPNAME_DOCID);
    DateValue dateValue = (DateValue) Value.getSingleValue(document,
        SpiConstants.PROPNAME_LASTMODIFIED);
    String dateString = dateValue.toIso8601();
    String result = null;
    try {
      JSONObject jo = new JSONObject();
      jo.put("uuid", uuid);
      jo.put("lastModified", dateString);
      result = jo.toString();
    } catch (JSONException e) {
      throw new RepositoryException("Unexpected JSON problem", e);
    }
    return result;
  }

  public String checkpoint() throws RepositoryException {
    return checkpoint(document);
  }

}
