// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.util.diffing;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.SpiConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link DocumentHandle} implementation for deleting documents.
 *
 * @since 2.8
 */
public class DeleteDocumentHandle implements DocumentHandle {
  static enum Field {
    DOCUMENT_ID
  }

  private final String documentId;

  /**
   * Construct a DeleteDocumentHandle for a {@code documentId}.
   *
   * @param documentId the deleted document
   */
  DeleteDocumentHandle(String documentId) {
    if (documentId == null) {
      throw new IllegalArgumentException();
    }
    this.documentId = documentId;
  }

  @Override
  public Document getDocument() {
    GenericDocument result = new GenericDocument();
    result.setProperty(SpiConstants.PROPNAME_ACTION,
        SpiConstants.ActionType.DELETE.toString());
    result.setProperty(SpiConstants.PROPNAME_DOCID, documentId);
    return result;
  }

  private JSONObject getJson() {
    JSONObject result = new JSONObject();
    try {
      result.put(Field.DOCUMENT_ID.name(), documentId);
      return result;
    } catch (JSONException e) {
      // Should ever happen.
      throw new RuntimeException("internal error: failed to create JSON", e);
    }
  }

  @Override
  public String getDocumentId() {
    return documentId;
  }

  @Override
  public String toString() {
    return getJson().toString();
  }
}
