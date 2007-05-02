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

package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.spi.RepositoryException;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

/**
 * Adaptor from connector.Spi.ResultSet to javax.jcr.Query
 */
public class SpiResultSetFromJcr implements
    com.google.enterprise.connector.spi.ResultSet {

  private final Node startNode;
  private final NodeIterator jcrIterator;

  public SpiResultSetFromJcr(final Node thisNode, final NodeIterator nodes) {
    startNode = thisNode;
    jcrIterator = nodes;
  }

  public SpiResultSetFromJcr(final NodeIterator nodes) {
    startNode = null;
    jcrIterator = nodes;
  }

  
  public Iterator iterator() throws RepositoryException {
    
    final Iterator subIterator = makeSpiIteratorFromNodeIterator(jcrIterator);
    if (startNode == null) {
      return subIterator;
    }
    Iterator internalIterator = new Iterator() {     
      boolean consumedFirst = false;

      public boolean hasNext() {
        if (!consumedFirst) {
          return true;
        }
        return subIterator.hasNext();
      }

      public Object next() {
        if (!consumedFirst){
          consumedFirst = true;
          return new SpiPropertyMapFromJcr(startNode);
        }
        return subIterator.next();
      }

      public void remove() {
        throw new UnsupportedOperationException();        
      }
    };
    return internalIterator;
  }

  private Iterator makeSpiIteratorFromNodeIterator(final NodeIterator fnodes) {
    Iterator iterator = new Iterator() {     
      public boolean hasNext() {
        return fnodes.hasNext();
      }
      public Object next() {
        javax.jcr.Node node = 
          (javax.jcr.Node) (fnodes.next());
        return new SpiPropertyMapFromJcr(node);
      }
      public void remove() {
        throw new UnsupportedOperationException();
      } 
    };
    return iterator;
  }
}
