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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

public class JcrDocumentList implements DocumentList {

  private Node startNode;
  private final NodeIterator jcrIterator;
  private Document document = null;
  private List insertedDocuments = new LinkedList();

  public JcrDocumentList(final Node thisNode, final NodeIterator nodes) {
    startNode = thisNode;
    jcrIterator = nodes;
  }

  public JcrDocumentList(final NodeIterator nodes) {
    startNode = null;
    jcrIterator = nodes;
  }

  public Document nextDocument() {
    if (insertedDocuments.size() > 0) {
      document = (Document) insertedDocuments.get(0);
      insertedDocuments.remove(0);
      return document;
    }
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
    String dateString = Value.getSingleValueString(document,
        SpiConstants.PROPNAME_LASTMODIFIED);
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
    if (document != null) {
      return checkpoint(document);
    } else {
      return null;
    }
  }

  public void insert(JcrEventDocument deleteDocument) {
    insertedDocuments.add(deleteDocument);
  }
}
