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

import com.google.enterprise.connector.util.diffing.DeleteDocumentHandle.Field;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link DocumentHandleFactory} for {@link DeleteDocumentHandle} objects.
 * Since this is provided by the diffing connector framework it is referred
 * as the internal {@link DocumentHandleFactory}.
 *
 * @since 2.8
 */
public class DeleteDocumentHandleFactory implements DocumentHandleFactory {
  /**
   * Reconstitute a {@link DeleteDocumentHandle} from its JSON representation.
   *
   * @param stringForm the JSON representation of a deleted document
   */
  public DeleteDocumentHandle fromString(String stringForm) {
    try {
      JSONObject json = new JSONObject(stringForm);
      checkForMissingRequiredFields(json);
      return new DeleteDocumentHandle(
          json.getString(Field.DOCUMENT_ID.name()));
    } catch (JSONException je) {
        throw new IllegalArgumentException(
            "Unable to parse serialized JSON Object " + stringForm, je);
    }
  }

  private static void checkForMissingRequiredFields(JSONObject o)
      throws IllegalArgumentException {
    StringBuilder buf = new StringBuilder();
    for (Field f : Field.values()) {
      if (!o.has(f.name())) {
        buf.append(f);
        buf.append(", ");
      }
    }
    if (buf.length() != 0) {
      buf.insert(0, "missing fields in JSON object: ");
      buf.setLength(buf.length() - 2);
      throw new IllegalArgumentException(buf.toString());
    }
  }
}
