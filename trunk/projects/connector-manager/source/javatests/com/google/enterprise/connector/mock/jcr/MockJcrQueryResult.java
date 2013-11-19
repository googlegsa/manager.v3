// Copyright (C) 2006-2009 Google Inc.
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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepositoryDocument;

import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

/**
 * MockJcrQueryResult implements the corresponding JCR interface, with these
 * limitations:
 * <ul>
 * <li> This is a "level 1" (read-only) implementation. All level 2
 * (side-effecting) calls throw UnsupportedOperation exceptions. These are
 * grouped at the bottom of the class implementation.
 * <li> Some level 1 calls are not implemented because they will never be used
 * by our connector infrastructure. Eventually, these will be documented as part
 * of framework documentation. In this implementation, they also throw
 * UnsupportedOperation exceptions. These are grouped above the level 2 calls.
 * <li> Some level 1 calls are not currently needed by our implementation, but
 * may be soon. These are marked with todos and throw UnsupportedOperation
 * exceptions.
 * </ul>
 */
public class MockJcrQueryResult implements QueryResult {

  List<MockRepositoryDocument> result;

  /**
   * Creates a MockJcrQueryResult from an Iterable<MockRepositoryDocument>
   * (a MockRepository result)
   * @param result
   */
  public MockJcrQueryResult(List<MockRepositoryDocument> result) {
    this.result = result;
  }

  /**
   * Returns the result as a NodeIterator.  At present, this is the only
   * supported operation.
   * @return MockJcrNodeIterator
   */
  public NodeIterator getNodes() {
    return new MockJcrNodeIterator(result.iterator());
  }

  // The following methods may be needed later but are temporarily
  // unimplemented

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public String[] getColumnNames() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws UnsupportedOperationException
   * @return nothing
   */
  public RowIterator getRows() {
    throw new UnsupportedOperationException();
  }
}
