// Copyright 2010 Google Inc.
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

import java.io.OutputStream;
import java.util.Iterator;

import javax.sql.DataSource;

/**
 * This interface is not implemented.
 *
 * A local persistent store of information about documents that is managed by
 * the Connector Manager for connectors.
 * <p>
 * Document metadata is automatically persisted (or deleted) for documents by
 * the Connector Manager:
 * <ul>
 * <li>for documents in {@link DocumentList} objects returned by the connector
 * in response to {@link TraversalManager#startTraversal()} or
 * {@link TraversalManager#resumeTraversal(String)} calls</li>
 * <li>provided the connector explicitly sets the
 * {@link SpiConstants#PROPNAME_MANAGER_SHOULD_PERSIST} property to {@code true}
 * </li>
 * <li>and provided the {@link SpiConstants#PROPNAME_ACTION} property is not set
 * to {@link SpiConstants.ActionType#DELETE} (if it is, then the document is
 * removed from the local store)</li>
 * <li>however, only those metadata items that are keys in the
 * {@link SpiConstants#PERSISTABLE_ATTRIBUTES} map are persisted</li>
 * </ul>
 * In addition, connector implementors may use the
 * {@link #storeDocument(Document)} method to programmatically store a document
 * in the local store without sending it through the traversal manager.
 * <p>
 * The connector may explicitly request stored information to be returned
 * through the {@link #findDocument(String)}, {@link #getDocumentIterator()} and
 * {@link #getDocumentIterator(String)} calls. These calls return
 * {@link Document} objects; again, only those keys in the
 * {@link SpiConstants#PERSISTABLE_ATTRIBUTES} map will be populated.
 * <p>
 * If necessary, the connector developer can also get access to the underlying
 * document table in the database implementation of this store by:
 * <ul>
 * <li>getting it's name through the
 * {@link LocalDocumentStore#getDocTableName()} call</li>
 * <li>getting a {@link DataSource} object by calling
 * {@link LocalDatabase#getDataSource()}</li>
 * <li>constructing SQL queries and submitting them through JDBC</li>
 * </ul>
 * The connector implementor should not create records in this table through
 * JDBC.
 *
 * @see ConnectorPersistentStore
 * @since 2.8
 * @deprecated This interface has never been publicly implemented
 */
@Deprecated
public interface LocalDocumentStore {

  /**
   * Finds a {@link Document} in the Connector Manager's per-document store by
   * docid. If not found, null is returned. If found, the {@code Document}
   * returned contains only the persisted attributes. See
   * {@link SpiConstants#PERSISTABLE_ATTRIBUTES}.
   *
   * @param docid the docid to search for
   * @return a {@link Document} or {@code null}, if this document is not found.
   */
  public Document findDocument(String docid);

  /**
   * Returns an iterator of all documents in the store created by this
   * connector instance, in sorted order by docid.
   * The result iterator is read-only. It will not support
   * {@link Iterator#remove()}.
   * <p>
   * The documents returned will be non-null and will contain only the
   * persisted attributes. See {@link SpiConstants#PERSISTABLE_ATTRIBUTES}.
   * <p>
   * <strong>Note:</strong> The {@code LocalDocumentStore} implementation
   * buffers information stored via {@link #storeDocument(Document)}, then
   * writes records out in batches.  Consequently unflushed documents will
   * not yet be available for retrieval.  Consider calling {@link #flush()}
   * before getting an Iterator.  Similarly, the document {@code Iterator}
   * implementation fetches documents in batches, so the Iterator may
   * return documents that were committed to the document store after the
   * {@code Iterator} was created.
   *
   * @return an {@link Iterator} of all documents created by this connector
   *         instance, in order by docid
   */
  public Iterator<Document> getDocumentIterator();

  /**
   * Returns an iterator of all documents in the store created by this
   * connector instance whose docids are {@code > } the specified docid,
   * in sorted order by docid.
   * The result iterator is read-only. It will not support
   * {@link Iterator#remove()}.
   * <p>
   * The documents returned will be non-null and will contain only the
   * persisted attributes. See {@link SpiConstants#PERSISTABLE_ATTRIBUTES}.
   * <p>
   * <strong>Note:</strong> The {@code LocalDocumentStore} implementation
   * buffers information stored via {@link #storeDocument(Document)}, then
   * writes records out in batches.  Consequently unflushed documents will
   * not yet be available for retrieval.  Consider calling {@link #flush()}
   * before getting an Iterator.  Similarly, the document {@code Iterator}
   * implementation fetches documents in batches, so the Iterator may
   * return documents that were committed to the document store after the
   * {@code Iterator} was created.
   *
   * @param docid the docid after which to start the iteration, if {@code null}
   *        or empty, all documents created by this connector are returned
   * @return an {@link Iterator} of all documents created by this connector
   *         instance whose docid exceeds the supplied docid, in order by docid
   */
  public Iterator<Document> getDocumentIterator(String docid);

  /**
   * Persists information about a document. Any attributes that are not keys in
   * the {@link SpiConstants#PERSISTABLE_ATTRIBUTES} table will be ignored.
   * <p>
   * <strong>Note:</strong> The {@code LocalDocumentStore} implementation
   * buffers information stored via {@link #storeDocument(Document)}, then
   * writes records out in batches for improved document store performance.
   * The buffered records are flushed to the document store periodically
   * and at the end of processing a traversal batch.  See {@link #flush()}.
   *
   * @param document a {@link Document}
   */
  public void storeDocument(Document document);

  /**
   * Flushes all changes pending in the table. This method is
   * similar to {@link OutputStream#flush()}: it is an indication that, if any
   * documents previously added or deleted have not yet been committed to the
   * store, such records should immediately be committed.
   */
  public void flush();

  /**
   * Returns the table name of the underlying implementation table.
   * The connector developer should not use this to do operations that could be
   * done directly through this {@code LocalDocumentStore} object. We expect
   * this to be used to do queries with non-updatable cursors, involving joins
   * between this table and other tables independently managed by the connector.
   *
   * @return the table name of the underlying table
   */
  public String getDocTableName();
}
