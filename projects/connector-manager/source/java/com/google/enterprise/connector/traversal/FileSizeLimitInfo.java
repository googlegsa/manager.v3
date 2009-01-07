// Copyright 2007 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.traversal;

/**
 * Provides context to the traversal process on how big is too big.
 */
public class FileSizeLimitInfo {

  private long maxDocumentSize = Long.MAX_VALUE;

  public void setMaxDocumentSize(long maxDocumentSize) {
    this.maxDocumentSize = maxDocumentSize;
  }

  public long maxDocumentSize() {
    return maxDocumentSize;
  }

}
