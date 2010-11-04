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

package com.google.enterprise.connector.util.database;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.enterprise.connector.spi.DatabaseResourceBundle;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.LocalDocumentStore;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.SystemClock;

import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.sql.DataSource;

/**
 * An implementation of {@link LocalDocumentStore} for a Connector.
 */
public class LocalDocumentStoreImpl implements DocumentStore {
  private static final Logger LOGGER =
      Logger.getLogger(LocalDocumentStoreImpl.class.getName());

  static final String RESOURCE_BUNDLE_NAME = "sql.connector-manager.DocumentStore";
  private DatabaseResourceBundle resourceBundle = null;

  // Classloader tailored to test environment.
  private ClassLoader classLoader = null;

  // Clock used for Timestamp generation;
  private Clock clock = new SystemClock();

  private final JdbcDatabase database;
  private final String connectorName;

  // Current DocumentBatch under construction.
  private DocumentBatch batch = null;

  // Cached SQL Resources
  private String singleDocumentQuery; // Get Document by ID.
  private String manyDocumentsQuery;  // Returns all Documents >= ID.
  private String batchDocumentsQuery; // Batch insert/update Query.
  private String deleteStatement;     // Delete Connector statement.
  private String tableName;

  /**
   * Constructs a {@link LocalDocumentStore} instance for the named
   * {@link Connector}, backed by the supplied {@link JdbcDatabase}.
   *
   * @param database a JdbcDatabase instance.
   * @param connectorName the name of the Connector.
   */
  public LocalDocumentStoreImpl(JdbcDatabase database, String connectorName) {
    Preconditions.checkNotNull(database, "Must set JdbcDatabase");
    Preconditions.checkNotNull(connectorName, "Must set Connector Name");
    this.database = database;
    this.connectorName = connectorName;
  }

  /* Constructor used by tests supplies Mockable clock and
   * test resource class loader.
   */
  @VisibleForTesting
  public LocalDocumentStoreImpl(JdbcDatabase database, String connectorName,
      ClassLoader resourceClassLoader, Clock clock) {
    this(database, connectorName);
    this.classLoader = resourceClassLoader;
    this.clock = clock;
  }

  private synchronized void init() {
    if (resourceBundle != null) {
      return;
    }

    // Locate our SQL DatabaseResourceBundle.
    DatabaseResourceBundleManager mgr = new DatabaseResourceBundleManager();
    DatabaseInfo dbInfo = database.getDatabaseInfo();
    resourceBundle = mgr.getResourceBundle(RESOURCE_BUNDLE_NAME, dbInfo, classLoader);
    if (resourceBundle == null) {
      throw new RuntimeException("Failed to load SQL ResourceBundle "
                                 + RESOURCE_BUNDLE_NAME);
    }

    // Verify that the connector instance table exists.
    tableName = getResource("table.name");
    if (!database.verifyTableExists(tableName,
         resourceBundle.getStringArray("table.create.ddl"))) {
      throw new RuntimeException("Document Store Table does not exist "
                                 + tableName);
    }

    // Cache some resources.
    singleDocumentQuery = getResource("single.document.query");
    manyDocumentsQuery = getResource("all.documents.after.id.query");
    batchDocumentsQuery = getResource("batch.documents.query");
    deleteStatement = getResource("delete.connector");
  }

  /**
   * Returns the SQL resource for the supplied key.
   */
  private String getResource(String key) {
    String value = resourceBundle.getString(key);
    if (value == null) {
      LOGGER.log(Level.WARNING, "Failed to resolve SQL resource " + key);
    }
    return value;
  }

  /**
   * Returns the docid of the supplied document, or empty string if
   * it is not available.
   */
  private String getDocId(Document document) {
    String docid = null;
    if (document != null) {
      try {
        docid = Value.getSingleValueString(document, SpiConstants.PROPNAME_DOCID);
      } catch (RepositoryException ignored) {}
    }
    return Strings.nullToEmpty(docid);
  }

  /**
   * Deletes all rows in the Document Table for this Connector.
   */
  /* @Override */
  public void reset() {
    init();
    Connection connection = null;
    PreparedStatement statement = null;
    try {
      connection = database.getConnectionPool().getConnection();
      statement = connection.prepareStatement(deleteStatement);
      statement.setString(1, connectorName);
      int numDeleted = statement.executeUpdate();
      LOGGER.fine("Deleted " + numDeleted + " records for connector "
                  + connectorName);
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to delete documents for connector "
                 + connectorName, e);
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException se) {
        // Already processing an exception.
        LOGGER.log(Level.WARNING, "Failed to close SQL Statement: ", se);
      } finally {
        if (connection != null) {
          database.getConnectionPool().releaseConnection(connection);
        }
      }
    }
  }

  /**
   * Returns the table name of the underlying implementation table.
   * The connector developer should not use this to do operations that
   * could be done directly through this {@code LocalDocumentStore} object.
   * We expect this to be used to do queries with non-updatable cursors,
   * involving joins between this table and other tables independently
   * managed by the connector.
   *
   * @return the table name of the underlying table.
   */
  public String getDocTableName() {
    init();
    return tableName;
  }

  /**
   * Finds a {@link Document} in the Connector Manager's per-document store by
   * docid. If not found, null is returned. If found, the {@code Document}
   * returned contains only the persisted attributes. See
   * {@link SpiConstants#PERSISTABLE_ATTRIBUTES}.
   *
   * @param docid the docid to search for
   * @return a {@link Document} or {@code null}, if this document is not found.
   */
  public Document findDocument(String docid) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(docid),
                                "DocId must not be null or empty.");
    init();
    Connection connection = null;
    PreparedStatement statement = null;
    try {
      connection = database.getConnectionPool().getConnection();
      statement = connection.prepareStatement(singleDocumentQuery);
      statement.setString(1, connectorName);
      statement.setString(2, docid);
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return makeDocument(resultSet);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document " + docid
                 + " for connector " + connectorName, e);
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException se) {
        // Already processing an exception.
        LOGGER.log(Level.WARNING, "Failed to close SQL Statement: ", se);
      } finally {
        if (connection != null) {
          database.getConnectionPool().releaseConnection(connection);
        }
      }
    }
    return null;
  }

  /**
   * Returns an iterator of all documents in the store created by this connector
   * instance, in sorted order by docid.
   * The result iterator is read-only. It will not support
   * {@link Iterator#remove()}.
   * <p/>
   * The documents returned will be non-null and will contain only the persisted
   * attributes. See {@link SpiConstants#PERSISTABLE_ATTRIBUTES}.
   *
   * @return an {@link Iterator} of all documents created by this connector
   *         instance, in order by docid.
   */
  public Iterator<Document> getDocumentIterator() {
    return getDocumentIterator(null);
  }

  /**
   * Returns an iterator of all documents in the store created by this
   * connector instance whose docids are {@code > } the specified docid,
   * in sorted order by docid.
   * The result iterator is read-only. It will not support
   * {@link Iterator#remove()}.
   * <p/>
   * The documents returned will be non-null and will contain only the
   * persisted attributes. See {@link SpiConstants#PERSISTABLE_ATTRIBUTES}.
   *
   * @param docid the docid after which to start the iteration, if {@code null}
   *        or empty, all documents created by this connector are returned.
   * @return an {@link Iterator} of all documents created by this connector
   *         instance whose docid exceeds the supplied docid, in order by docid.
   */
  public Iterator<Document> getDocumentIterator(String docid) {
    return new DocumentIterator(Strings.nullToEmpty(docid));
  }

  /**
   * An {@link Iterator} over the Documents Table, which fetches the
   * Documents in batches, iterates over them, then fetches another
   * the next batch.
   */
  private class DocumentIterator implements Iterator<Document> {
    private static final int BATCH_SIZE = 20000;
    private String lastDocid;
    private Document[] documents;
    private int currentDoc;
    private int numDocs;

    public DocumentIterator(String docid) {
      init();
      lastDocid = docid;
      documents = new Document[BATCH_SIZE];
      currentDoc = 0;
      numDocs = 0;
    }

    /* @Override */
    public synchronized boolean hasNext() {
      if (currentDoc == numDocs) {
        getBatch();
      }
      return (currentDoc < numDocs);
    }

    /* @Override */
    public synchronized Document next() throws NoSuchElementException {
      if (hasNext()) {
        Document document = documents[currentDoc];
        // Allow the document to be reapped once the client is finished with it.
        documents[currentDoc++] = null;
        return document;
      } else {
        throw new NoSuchElementException();
      }
    }

    /* @Override */
    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

    /** Gets the next batch of Documents from the Database. */
    private void getBatch() {
      Connection connection = null;
      PreparedStatement statement = null;
      currentDoc = 0;
      numDocs = 0;

      try {
        connection = database.getConnectionPool().getConnection();
        statement = connection.prepareStatement(manyDocumentsQuery);
        statement.setQueryTimeout(15 * 60); // TODO: make this configurable.
        statement.setMaxRows(documents.length);
        statement.setString(1, connectorName);
        statement.setString(2, lastDocid);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
          documents[numDocs++] = makeDocument(resultSet);
        }
        if (numDocs > 0) {
          lastDocid = getDocId(documents[numDocs -1]);
        }

      } catch (SQLException e) {
        LOGGER.log(Level.WARNING, "Failed to retrieve documents: "
                   + " for connector " + connectorName, e);
      } finally {
        try {
          if (statement != null) {
            statement.close();
          }
        } catch (SQLException se) {
          // Already processing an exception.
          LOGGER.log(Level.WARNING, "Failed to close SQL Statement: ", se);
        } finally {
          if (connection != null) {
            database.getConnectionPool().releaseConnection(connection);
          }
        }
      }
    }
  }

  /**
   * Constructs a {@link Document} based upon the data in the current row
   * of the {@link ResultSet}.
   *
   * @param resultSet a {@link ResultSet}
   * @return a {@link Document}
   */
  private Document makeDocument(ResultSet resultSet) {
    ImmutableMap.Builder<String, List<Value>> builder = ImmutableMap.builder();
    for (String propertyName : SpiConstants.PERSISTABLE_ATTRIBUTES.keySet()) {
      String value = null;
      try {
        value = resultSet.getString(
            SpiConstants.PERSISTABLE_ATTRIBUTES.get(propertyName));
      } catch (SQLException e) {
        LOGGER.log(Level.WARNING, "Failed to fetch value for "
                   + SpiConstants.PERSISTABLE_ATTRIBUTES.get(propertyName)
                   + " from ResultSet", e);
      }
      if (value != null) {
        builder.put(propertyName,
            Collections.singletonList(Value.getStringValue(value)));
      }
    }
    return new SimpleDocument(builder.build());
  }

  /**
   * Persists information about a document. Any attributes that are not keys
   * in the {@link SpiConstants#PERSISTABLE_ATTRIBUTES} table will be ignored.
   *
   * @param document a {@link Document}
   */
  public synchronized void storeDocument(Document document) {
    init();
    try {
      if (batch == null) {
        batch = new DocumentBatch();
      }
      batch.addDocument(document);
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to persist document "
                 + getDocId(document) + " for connector " + connectorName, e);
    }
  }

  /**
   * Cancels any pending additions to the Documents table.
   * This discards any data that has not already been committed,
   * specifically, Documents that would have been written by a
   * call to {@link #flush()}.
   */
  /* @Override */
  public synchronized void cancel() {
    if (batch != null) {
      batch.cancel();
      batch = null;
    }
  }

  /**
   * Flushes all changes pending in the table. This method is similar to
   * {@link OutputStream#flush()}: it is an indication that, if any
   * documents previously added or deleted have not yet been committed to
   * the store, such records should immediately be committed.
   */
  public synchronized void flush() {
    init();
    if (batch != null) {
      batch.close();
      batch = null;
    }
  }

  /**
   * Represents a batch of {@link Document} inserts into the documents table.
   * This is backed by a JDBC batch insert and committed by a call to
   * {@code flush()}.
   */
  private class DocumentBatch {
    // Oracle supports a maximum of 1000 items in a list.
    private static final int MAX_LIST_SIZE = 1000;
    private Connection connection;
    private boolean originalAutoCommit = true;
    private HashMap<String, Map<String, String>> documents;

    DocumentBatch() throws SQLException {
      connection = database.getConnectionPool().getConnection();
      documents = Maps.newHashMap();

      originalAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);
    }

    /**
     * Adds the supplied {@link Document} to this batch of documents
     * to insert into the documents table.
     */
    synchronized void addDocument(Document document) throws SQLException {
      String docid = getDocId(document);
      Preconditions.checkState((docid.length() > 0), "DocID must not be null or empty");

      // Remember all the persistable attributes for this document.
      Map<String, String> properties = documents.get(docid);
      if (properties == null) {
        properties = new HashMap<String, String>();
        documents.put(docid, properties);
      }
      Set propNames;
      try {
        propNames = document.getPropertyNames();
      } catch (RepositoryException re) {
        LOGGER.log(Level.WARNING, "Failed to retrieve property names from "
                   + "document " + docid, re);
        return;
      }

      for (String propertyName : SpiConstants.PERSISTABLE_ATTRIBUTES.keySet()) {
        if (propNames.contains(propertyName)) {
          String value = null;
          try {
            value = Value.getSingleValueString(document, propertyName);
          } catch (RepositoryException e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve property " + propertyName
                       + " from document " + docid, e);
          }
          properties.put(propertyName, value);
        }
      }

      // If we hit the maximum size of a list, submit the batch.
      if (documents.size() == MAX_LIST_SIZE) {
        writeDocuments();
      }
    }

    /**
     * Quotes the supplied value using single qoutes.  MessageFormat
     * considers embedded single-quotes special, and doesn't do
     * substitutions within them.  Unfortunately, this is exactly
     * where we want to use substitutions: in SQL queries like:
     * {@code ... WHERE ( connector_name='{0}' ...}.
     * <p>
     * One solution is to add the quote characters to the value being
     * substituted in (the purpose of this method).  Another solution would
     * be to avoid MessageFormat, possibly trying PreparedStatement syntax.
     */
    private String quoteValue(String value) {
      return "'" + value.replace("'", "''") + "'";
    }

    /**
     * Writes out the collected Document data.
     */
    private void writeDocuments() throws SQLException {
      if (documents.isEmpty()) {
        return;
      }

      StringBuilder builder = new StringBuilder();
      for (String docid : documents.keySet()) {
        builder.append(quoteValue(docid)).append(',');
      }
      builder.setLength(builder.length() - 1);

      Object[] params = { quoteValue(connectorName), builder.toString() };
      String query = MessageFormat.format(batchDocumentsQuery, params);
      Statement statement = connection.createStatement(
          ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
      try {
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
          // Update existing document.
          Map<String, String> properties = documents.remove(resultSet.getString(6));
          resultSet.updateTimestamp(1, new Timestamp(clock.getTimeMillis()));
          updateValue(resultSet, 3, properties, SpiConstants.PROPNAME_FEEDID);
          updateValue(resultSet, 4, properties, SpiConstants.PROPNAME_SNAPSHOT);
          updateValue(resultSet, 5, properties, SpiConstants.PROPNAME_ACTION);
          updateValue(resultSet, 7, properties, SpiConstants.PROPNAME_PRIMARY_FOLDER);
          updateValue(resultSet, 8, properties, SpiConstants.PROPNAME_CONTAINER);
          updateValue(resultSet, 9, properties, SpiConstants.PROPNAME_MESSAGE);
          updateValue(resultSet, 10, properties, SpiConstants.PROPNAME_PERSISTED_CUSTOMDATA_1);
          updateValue(resultSet, 11, properties, SpiConstants.PROPNAME_PERSISTED_CUSTOMDATA_2);
          resultSet.updateRow();
        }
        for (Map<String, String> properties : documents.values()) {
          // Insert new document.
          resultSet.moveToInsertRow();
          resultSet.updateTimestamp(1, new Timestamp(clock.getTimeMillis()));
          resultSet.updateString(2, connectorName);
          setValue(resultSet, 3, properties, SpiConstants.PROPNAME_FEEDID);
          setValue(resultSet, 4, properties, SpiConstants.PROPNAME_SNAPSHOT);
          setValue(resultSet, 5, properties, SpiConstants.PROPNAME_ACTION);
          setValue(resultSet, 6, properties, SpiConstants.PROPNAME_DOCID);
          setValue(resultSet, 7, properties, SpiConstants.PROPNAME_PRIMARY_FOLDER);
          setValue(resultSet, 8, properties, SpiConstants.PROPNAME_CONTAINER);
          setValue(resultSet, 9, properties, SpiConstants.PROPNAME_MESSAGE);
          setValue(resultSet, 10, properties, SpiConstants.PROPNAME_PERSISTED_CUSTOMDATA_1);
          setValue(resultSet, 11, properties, SpiConstants.PROPNAME_PERSISTED_CUSTOMDATA_2);
          resultSet.insertRow();
        }
        resultSet.close();
        connection.commit();
      } catch (SQLException e) {
        try {
          connection.rollback();
        } catch (SQLException se) {
          LOGGER.log(Level.WARNING, "Failed to rollback SQL transaction: ", se);
        }
        throw e;
      } finally {
        try {
          statement.close();
        } catch (SQLException se) {
          LOGGER.log(Level.WARNING, "Failed to close SQL Statement: ", se);
        } finally {
          documents.clear();
        }
      }
    }

    /**
     * Sets the updatable {@code ResultSet} parameter to the value of the named
     * {@link Property}, if that property is present in {@code properties}.
     *
     * @param resultSet a ResultSet
     * @param paramNum index of the parameter to set in the resultSet
     * @param properties the map of properties for document
     * @param propertyName the name of the Property to extract from document
     */
    private void updateValue(ResultSet resultSet, int paramNum,
                        Map<String, String> properties, String propertyName)
        throws SQLException {
      if (properties.containsKey(propertyName)) {
        setValue(resultSet, paramNum, properties, propertyName);
      }
    }

    /**
     * Sets the updatable {@code ResultSet} parameter to the value of the named
     * {@link Property} from the supplied {@code properties}, or {@code null} if
     * {@code properties} has no value for the requested property.
     *
     * @param resultSet a ResultSet
     * @param paramNum index of the parameter to set in the resultSet
     * @param properties the map of properties for document
     * @param propertyName the name of the Property to extract from document
     */
    private void setValue(ResultSet resultSet, int paramNum,
                          Map<String, String> properties, String propertyName)
        throws SQLException {
      String value = properties.get(propertyName);
      if (value == null) {
        resultSet.updateNull(paramNum);
      } else {
        resultSet.updateString(paramNum, value);
      }
    }

    /**
     * Cancels this batch, discarding any pending Document writes, and
     * closing the batch.
     */
    synchronized void cancel() {
      if (documents != null) {
        documents.clear();
        close();
      }
    }

    /**
     * Closes this statement, and returns its Connection to the pool.
     */
    synchronized void close() {
      if (documents != null) {
        try {
          writeDocuments();
        } catch (SQLException e) {
          LOGGER.log(Level.WARNING, "Failed write document batch", e);
        } finally {
          documents = null;
          try {
            connection.setAutoCommit(originalAutoCommit);
          } catch (SQLException se) {
            LOGGER.log(Level.WARNING, "Failed to restore autocommit: ", se);
          }
          database.getConnectionPool().releaseConnection(connection);
        }
      }
    }

    @Override
    public synchronized void finalize() throws Exception {
      close();
    }
  }
}
