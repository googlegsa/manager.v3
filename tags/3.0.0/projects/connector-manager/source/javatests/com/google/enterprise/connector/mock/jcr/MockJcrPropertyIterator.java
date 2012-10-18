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

package com.google.enterprise.connector.mock.jcr;

import java.util.Iterator;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

/**
 * MockJcrPropertyIterator implements the corresponding JCR interface, with
 * these limitations:
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
public class MockJcrPropertyIterator implements PropertyIterator {

  private final Iterator<MockJcrProperty> propIterator;

  public MockJcrPropertyIterator(List<MockJcrProperty> propList) {
    this.propIterator = propList.iterator();
  }

  public Property nextProperty() {
    return propIterator.next();
  }

  public boolean hasNext() {
    return propIterator.hasNext();
  }

  public Object next() {
    return propIterator.next();
  }

  // The following methods are JCR level 1 - but we do not anticipate using them

  public void skip(long arg0) {
    throw new UnsupportedOperationException();
  }

  public long getSize() {
    throw new UnsupportedOperationException();
  }

  public long getPosition() {
    throw new UnsupportedOperationException();
  }

  // The following methods are JCR level 2 - these would never be needed

  public void remove() {
    throw new UnsupportedOperationException();
  }

}
