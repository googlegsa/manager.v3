// Copyright 2007-2009 Google Inc.
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

package com.google.enterprise.connector.spi;

import java.util.Iterator;
import java.util.List;

public class SimpleDocumentList implements DocumentList {

  private List<? extends Document> documents;
  private Iterator<? extends Document> iterator;
  private Document document;

  public SimpleDocumentList(List<? extends Document> documents) {
    this.documents = documents;
    this.iterator = null;
    this.document = null;
  }

  public Document nextDocument() {
    if (iterator == null) {
      iterator = documents.iterator();
    }
    if (iterator.hasNext()) {
      document = iterator.next();
      return document;
    }
    return null;
  }

  public String checkpoint() throws RepositoryException {
    if (document == null) {
      return null;
    }
    return Value.getSingleValueString(document,
        SpiConstants.PROPNAME_LASTMODIFIED);
  }

}
