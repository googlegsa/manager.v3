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

import com.google.enterprise.connector.util.diffing.DocumentHandle;
import com.google.enterprise.connector.util.diffing.DocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link DocumentSnapshot} for testing.
 *
 */
public class MockDocumentSnapshot implements DocumentSnapshot {
  static enum Field {
    DOCUMENT_ID, EXTRA
  }

  private final String documentId;
  private final String extra;

  MockDocumentSnapshot(String documentId, String extra) {
    this.documentId = documentId;
    this.extra = extra;
  }

  public String getDocumentId() {
    return documentId;
  }

  String getExtra() {
    return extra;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((documentId == null) ? 0 : documentId.hashCode());
    result = prime * result + ((extra == null) ? 0 : extra.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MockDocumentSnapshot other = (MockDocumentSnapshot) obj;
    if (documentId == null) {
      if (other.documentId != null) {
        return false;
      }
    } else if (!documentId.equals(other.documentId)) {
      return false;
    }
    if (extra == null) {
      if (other.extra != null) {
        return false;
      }
    } else if (!extra.equals(other.extra)) {
      return false;
    }
    return true;
  }

  private JSONObject getJson() {
    JSONObject result = new JSONObject();
    try {
      result.put(Field.DOCUMENT_ID.name(), documentId);
      result.put(Field.EXTRA.name(), extra);
      return result;
    } catch (JSONException e) {
      // This cannot happen.
      throw new RuntimeException("internal error: failed to encode snapshot record", e);
    }
  }

  @Override
  public String toString() {
    return getJson().toString();
  }

  public DocumentHandle getUpdate(DocumentSnapshot onGsa) {
   throw new UnsupportedOperationException();
  }
}
