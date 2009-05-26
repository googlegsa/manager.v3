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

package com.google.enterprise.connector.mock;

/**
 * Mock time object to replace Calendar in unit tests.
 * <p>
 * Implemented with a single int "ticks"
 */
public class MockRepositoryDateTime implements Comparable<MockRepositoryDateTime> {
  private int ticks;

  @Override
  public String toString() {
    return Integer.toString(ticks);
  }

  public MockRepositoryDateTime(int ticks) {
    this.ticks = ticks;
  }

  public int getTicks() {
    return ticks;
  }

  public int compareTo(MockRepositoryDateTime t) {
    return this.getTicks() - t.getTicks();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MockRepositoryDateTime)) {
      return false;
    }
    return (compareTo((MockRepositoryDateTime)obj) == 0);
  }

  @Override
  public int hashCode() {
    long lticks = ticks;
    return (int)(lticks ^ (lticks >>> 32));  // See Bloch, EJ, p. 38
  }
}
