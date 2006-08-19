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

package com.google.enterprise.connector.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.enterprise.connector.mock.MockRepositoryEvent.EventType;

/**
 * Mock Document Store for Unit tests.
 * 
 * This class provides a rudimentary memory-based content management system
 * (CMS), to enable testing of the connector framework without external
 * dependencies.
 * <p>
 * The CMS has a very simple document-model: see the MockRepositoryDocument
 * class.
 * <p>
 * Documents have ID's: it is an important invariant that document ID's are
 * unique.
 * <p>
 * When constructed, the CMS is empty. Documents can be added, changed, or
 * removed by "events" (see MockRepositoryEvent). Thus the CMS is side-effected
 * only through the following calls:
 * <ul>
 * <li> reinit() - throws away the current store, if any, and sets it to an
 * empty state
 * <li> applyEvent() - takes an event and applies it the store (adds, changes or
 * deletes a document)
 * </ul>
 * <p>
 * The CMS can be inspected by the following calls:
 * <ul>
 * <li> getDocByID() - returns the document with the specified ID, if present.
 * Document must have unique IDs
 * <li> iterator() - returns an iterator for all documents, in timestamp order.
 * (Note: this class implements Iterable<MockRepositoryDocument>
 * </ul>
 * <p>
 * Two integrity constraints are enforced after every side-effecting change:
 * <ul>
 * <li> checkDocidUniquenessIntegrity() - makes sure that IDs are unique
 * <li> checkDateOrderIntegrity() - makes sure that the iterator returns
 * documents in timestamp order
 * </ul>
 * <p>
 * TODO(ziff): 
 * <ul>
 * <li>add event call-back
 * <li>add textual dump/load so bigger test cases can be built easily
 * </ul>
 */
public class MockRepositoryDocumentStore {
  private static final Logger logger = 
    Logger.getLogger(MockRepositoryDocumentStore.class.getName());
  Map store = null;

  /**
   * Makes an empty store
   */
  public MockRepositoryDocumentStore() {
    reinit();
  }

  /**
   * Returns a store to the empty state
   */
  public void reinit() {
    store = new HashMap();
    if (!checkIntegrity()) {
      throw new RuntimeException("MockRepositoryStore integrity check failed");
    }
  }

  /**
   * Side-effect the store by applying an event. This could add, remove or
   * change a document, depending on the event type. Note: the timestamp of the
   * event becomes the timestanmp of the created or changed document. The
   * implementation does NOT enforce that events are supplied in increasing
   * timestamp order. Perhaps it would be a good idea to add this later.
   * @param event
   */
  public void applyEvent(MockRepositoryEvent event) {
	  if (event.getType() == EventType.SAVE) {
        doSave(event);
	  }
	  else if (event.getType() == EventType.DELETE) {
        doDelete(event);
	  } 
	  else if (event.getType() == EventType.METADATA_ONLY_SAVE) {
        doSave(event);
	  }
	  else {
        throw new IllegalArgumentException("Unknown event type");
    }
  
    // TODO:ziff assert checkIntegrity();
  }

  /**
   * Performs the DELETE action. Deletes are keyed by docID. The other fields of
   * the event are ignored.
   * @param event Must be a DELETE event
   */
  private void doDelete(MockRepositoryEvent event) {
    String docID = event.getDocID();
    MockRepositoryDocument d = getDocByID(docID);
    if (d == null) {
      return;
    }
    store.remove(docID);
  }

  /**
   * Performs the SAVE and METADATA_ONLY_SAVE actions. Looks up the document by
   * ID - if not present, it creates it. If present, it applies the changes
   * specified in the event.
   * @param event Must be a SAVE or METADATA_ONLY_SAVE event
   */
  private void doSave(MockRepositoryEvent event) {
    String docid = event.getDocID();
    if (docid == null || docid.length() == 0) {
      throw new RuntimeException("document has no id");
    }
    MockRepositoryDocument d = getDocByID(docid);
    if (d == null) {
      // this is a new document
      d = new MockRepositoryDocument(event.getTimeStamp(), 
                                     docid, 
                                     event.getContent(), 
                                     event.getPropertyList());
      store.put(docid, d);
    } else {
      // this is a change to an old document
      if (event.getPropertyList() != null) {
        d.getProplist().merge(event.getPropertyList());
      }
      String newContent = 
        ((event.getContent() != null) ? event.getContent() : d.getContent());
      MockRepositoryDocument modifiedDoc = 
        new MockRepositoryDocument(event.getTimeStamp(), 
                                   docid, 
                                   newContent, 
                                   d.getProplist());
      store.remove(docid);
      store.put(docid, modifiedDoc);
    }
  }

  /**
   * Looks up a document in the store by ID
   * @param docid The ID to look for
   * @return If found, the document; otherwise, null
   */
  public MockRepositoryDocument getDocByID(String docid) {
    return (MockRepositoryDocument)store.get(docid);
  }

  /**
   * Returns an iterator over all documents in the store
   */
  public Iterator iterator() {
    List l = 
      new LinkedList(store.values());
    sortDocuments(l);
    return l.listIterator();
  }

  public int size() {
    return store.size();
  }

  /**
   * Returns all documents last modified between the two dates: 
   * specifically, all documents modified at a time greater than or equal
   * to the from parameter and strictly less than the to parameter
   * @param from
   * @param to
   * @return An Iterable over these results
   */
  public List dateRange(
      final MockRepositoryDateTime from, final MockRepositoryDateTime to) {
    List l = new ArrayList();
    for (Iterator iter = store.values().iterator(); iter.hasNext(); ) {
    	MockRepositoryDocument d = (MockRepositoryDocument) iter.next();
      int c1 = from.compareTo(d.getTimeStamp());
      int c2 = d.getTimeStamp().compareTo(to);
      if (c1 <= 0  && c2 < 0) {
        l.add(d);
      }
    }
    sortDocuments(l);
    return l;
  }

  private void sortDocuments(List l) {
    Collections.sort(l, new Comparator() {
      public int compare(Object o1, Object o2) {
    	  MockRepositoryDocument d1 = (MockRepositoryDocument) o1;
    	  MockRepositoryDocument d2 = (MockRepositoryDocument) o2;
        return d1.getTimeStamp().compareTo(d2.getTimeStamp());
      }
    });
  }

  /**
   * Checks the date-order integrity constraint: iterates through the documents
   * and makes sure the timnestamps are in ascending order.
   * @return True or false, depending on whether the test passes
   */
  private boolean checkDateOrderIntegrity() {
    boolean result = true;
    int lastStamp = -1;
    for (Iterator iter = this.iterator(); iter.hasNext(); ) {
    	MockRepositoryDocument d = (MockRepositoryDocument) iter.next();
      int thisStamp = d.getTimeStamp().getTicks();
      if (lastStamp > thisStamp) {
        result = false;
        logger.info("Docid " + d.getDocID() + " appears out of order");
        logger.info("Timestamp: " + thisStamp + " follows stamp: " + lastStamp);
        lastStamp = thisStamp;
      }
    }
    return result;
  }

  /**
   * Checks the docid uniqueness constraint: iterates through the documents and
   * checks to see whether the ids have ever been seen before
   * @return True or false, depending on whether the test passes
   */

  private boolean checkDocidUniquenessIntegrity() {
    boolean result = true;
    Set m = new HashSet();
    for (Iterator iter = this.iterator(); iter.hasNext(); ) {
    	MockRepositoryDocument d = (MockRepositoryDocument) iter.next();
      if (!m.add(d.getDocID())) {
        // this docid appears more than once
        result = false;
        logger.info("Docid " + d.getDocID() + " appears more than once!");
      }
    }
    return result;
  }

  /**
   * Runs all integrity checks
   * @return true or false, depending on whether ALL tests pass
   */
  private boolean checkIntegrity() {
    boolean result = true;
    if (!checkDateOrderIntegrity()) {
      result = false;
    }
    if (!checkDocidUniquenessIntegrity()) {
      result = false;
    }
    return result;
  }
}
