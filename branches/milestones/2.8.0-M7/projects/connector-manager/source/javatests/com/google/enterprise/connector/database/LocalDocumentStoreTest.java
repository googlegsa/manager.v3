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

package com.google.enterprise.connector.database;

import com.google.common.collect.Lists;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.LocalDocumentStore;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.util.database.JdbcDatabase;
import com.google.enterprise.connector.util.database.testing.TestJdbcDatabase;
import com.google.enterprise.connector.util.database.testing.TestResourceClassLoader;
import com.google.enterprise.connector.util.SystemClock;

import org.h2.jdbcx.JdbcDataSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Map;
import javax.sql.DataSource;

import junit.framework.TestCase;

/**
 * Tests for {@link LocalDocumentStoreImpl}.
 */
public class LocalDocumentStoreTest extends TestCase {
  protected static final String CONNECTOR_NAME_A = "connector_a";
  protected static final String CONNECTOR_NAME_B = "connector_b";

  protected static final String TABLE_NAME = "google_documents_connector_a";

  protected static final String DOCID_XYZZY = "xyzzy";
  protected static final String DOCID_BAR = "bar";
  protected static final String DOCID_BAZ = "baz";
  protected static final String DOCID_FOO = "foo";
  protected static final String DOCID_FOOBAR = "foobar";
  protected static final String[] FOOBAR_DOCS = { DOCID_BAR, DOCID_BAZ, DOCID_FOO, DOCID_XYZZY };

  protected static final String DOCID_FRED = "fred";
  protected static final String DOCID_BARNEY = "barney";
  protected static final String DOCID_BETTY = "betty";
  protected static final String DOCID_WILMA = "wilma";
  protected static final String[] FLINT_DOCS = { DOCID_BARNEY, DOCID_BETTY, DOCID_FRED, DOCID_WILMA };

  protected static final String FEEDID = "feed";
  protected static final String NEW_FEEDID = "newfeed";
  protected static final String PARENTID = "parent";
  protected static final String CONTAINER = "container";
  protected static final String MESSAGE = "message";

  protected static final File RESOURCE_DIR = new File("source/resources/");

  protected DataSource dataSource;
  protected JdbcDatabase jdbcDatabase;
  protected LocalDocumentStoreImpl store;
  protected Random random;

  protected void setUp() throws Exception {
    super.setUp();

    random = new Random();
    jdbcDatabase = new TestJdbcDatabase();
    dataSource = jdbcDatabase.getDataSource();
    store = newDocumentStore(CONNECTOR_NAME_A);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      jdbcDatabase.shutdown();
    } finally {
      super.tearDown();
    }
  }

  // Constructs a new LocalDocumentStoreImpl for the named connector,
  // backed by the jdbcDatabase.
  private LocalDocumentStoreImpl newDocumentStore(String connectorName) {
    return new LocalDocumentStoreImpl(jdbcDatabase, connectorName,
        new TestResourceClassLoader(RESOURCE_DIR),
        new SystemClock() /* TODO: Mockable Clock */);
  }

  // Constructs a test Document with the supplied DocId.
  private Document newDocument(String docid) {
    return newDocument(docid, FEEDID, PARENTID, CONTAINER, null);
  }

  // Constructs a test Document with the supplied Pesistable Properties.
  private Document newDocument(String docid, String feedid, String parentid,
                               String container, String message) {
    Map<String, Object> props =
        ConnectorTestUtils.createSimpleDocumentBasicProperties(docid);
    if (feedid != null) {
      props.put(SpiConstants.PROPNAME_FEEDID, feedid);
    }
    if (parentid != null) {
      props.put(SpiConstants.PROPNAME_PRIMARY_FOLDER, parentid);
    }
    if (container != null) {
      props.put(SpiConstants.PROPNAME_CONTAINER, container);
    }
    if (message != null) {
      props.put(SpiConstants.PROPNAME_MESSAGE, message);
    }
    return ConnectorTestUtils.createSimpleDocument(props);
  }

  // Creates a document with the given docid and stores it to the default store.
  private void storeDocument(String docid) throws Exception {
    storeDocument(newDocument(docid));
  }

  // Stores the supplied document to the default store.
  private void storeDocument(Document document)
      throws Exception {
    store.storeDocument(document);
    store.flush();
  }

  // Retrieves the stored document with the given docid.
  private Document getDocument(String docid) throws Exception {
    Document document = store.findDocument(docid);
    checkDocument(docid, document);
    return document;
  }

  // Verifies the Document is non-null and has the expected docid.
  private void checkDocument(String expectedDocid, Document document)
      throws Exception {
    assertNotNull(document);
    assertEquals(expectedDocid, Value.getSingleValueString(document,
        SpiConstants.PROPNAME_DOCID));
  }

  private String[] asArray(String docid) {
    String[] array = new String[1];
    array[0] = docid;
    return array;
  }

  // Creates a bunch of documents with the given docids and stores them to the
  // default store in a random order.
  private void storeDocuments(String[] docids) throws Exception {
    storeDocuments(store, docids);
  }

  // Creates a bunch of documents with the given docids and stores them to the
  // specified store in a random order.
  private void storeDocuments(LocalDocumentStoreImpl store,
      String[] docids) throws Exception {
    storeDocumentsNoFlush(store, docids);
    store.flush();
  }

  // Creates a bunch of documents with the given docids and stores them to the
  // specified store in a random order.
  private void storeDocumentsNoFlush(LocalDocumentStoreImpl store,
      String[] docids) throws Exception {
    ArrayList<String> list = Lists.newArrayList(Arrays.asList(docids));
    while(!list.isEmpty()){
      store.storeDocument(newDocument(list.remove(random.nextInt(list.size()))));
    }
  }

  // Verifies that an Iterator over the default store returns all the supplied
  // docids in the supplied order.
  private void verifyDocuments(String[] docids) throws Exception {
    verifyDocuments(store, docids);
  }

  // Verifies that an Iterator over the given store returns all the supplied
  // docids in the supplied order.
  private void verifyDocuments(LocalDocumentStoreImpl store, String[] docids)
      throws Exception {
    verifyDocuments(store.getDocumentIterator(), docids);
  }

  // Verifies that the supplied Iterator returns all the supplied
  // docids in the supplied order.
  private void verifyDocuments(Iterator<Document> iter, String[] docids)
      throws Exception {
    assertNotNull(iter);
    for (String docid : docids) {
      assertTrue(iter.hasNext());
      checkDocument(docid, iter.next());
    }
    assertFalse(iter.hasNext());
  }

  // Verifies the the Iterator, although non-null is empty.
  private void verifyNoDocuments(Iterator<Document> iter) throws Exception {
    // Verify that the result set is empty.
    assertNotNull(iter);
    assertFalse(iter.hasNext());
  }

  // Tests getDocTableName with a connector name a safe name.
  public void testGetDocTableNameSimpleConnectorName() throws Exception {
    String tableName = store.getDocTableName();
    assertEquals(TABLE_NAME, tableName);
  }

  // Tests creating Documents Table lazily.
  public void testCreateTable() throws Exception {
    Connection connection = jdbcDatabase.getConnectionPool().getConnection();

    // Assert the table does not yet exist.
    assertFalse(jdbcDatabase.verifyTableExists(TABLE_NAME, null));

    // Accessing the table should force its creation.
    String tableName = store.getDocTableName();
    assertEquals(TABLE_NAME, tableName);

    // Assert the table does now exist.
    assertTrue(jdbcDatabase.verifyTableExists(TABLE_NAME, null));

    connection.close();
    jdbcDatabase.getConnectionPool().releaseConnection(connection);
  }

  // Tests reading an empty table.
  public void testReadEmptyTable() throws Exception {
    Iterator<Document> iter = store.getDocumentIterator();
    assertNotNull(iter);
    assertFalse(iter.hasNext());
  }

  // Tests reading an empty table.
  public void testReadEmptyTableFromDocid() throws Exception {
    Iterator<Document> iter = store.getDocumentIterator(DOCID_XYZZY);
    assertNotNull(iter);
    assertFalse(iter.hasNext());
  }

  // Tests reading an empty table.
  public void testReadEmptyTableWithDocid() throws Exception {
    assertNull(store.findDocument(DOCID_XYZZY));
  }

  // Tests adding a Document to the table.
  public void testStoreAndFindDocument() throws Exception {
    storeDocument(DOCID_XYZZY);
    getDocument(DOCID_XYZZY);
  }

  // Tests find non-existing document in a non-empty table.
  public void testFindNonExistingDocument() throws Exception {
    storeDocument(DOCID_XYZZY);
    assertNull(store.findDocument(DOCID_FOOBAR));
  }

  // Tests adding a Document to the table.
  public void testGetIterator() throws Exception {
    storeDocument(DOCID_XYZZY);
    verifyDocuments(asArray(DOCID_XYZZY));
  }

  // Tests GetDocumentIterator(docid) doesn't return the docid.
  public void testGetIteratorFromDocid() throws Exception {
    storeDocument(DOCID_XYZZY);
    verifyNoDocuments(store.getDocumentIterator(DOCID_XYZZY));
  }

  // Tests getDocumentIterator from docid before our key.
  public void testGetIteratorFromLesserDocid() throws Exception {
    storeDocument(DOCID_XYZZY);
    verifyDocuments(store.getDocumentIterator(DOCID_FOOBAR),
                    asArray(DOCID_XYZZY));
  }

  // Tests getDocumentIterator from docid after our key.
  public void testGetIteratorFromGreaterDocid() throws Exception {
    storeDocument(DOCID_XYZZY);
    verifyNoDocuments(store.getDocumentIterator(DOCID_XYZZY + "z"));
  }

  // Tests only Persistable properties are stored.
  public void testOnlyPersistableProperties() throws Exception {
    storeDocument(DOCID_XYZZY);
    Document stored = getDocument(DOCID_XYZZY);

    // Verify that populated persistable properties were stored.
    assertEquals(DOCID_XYZZY, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_DOCID));
    assertEquals(FEEDID, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_FEEDID));
    assertEquals(PARENTID, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_PRIMARY_FOLDER));
    assertEquals(CONTAINER, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_CONTAINER));

    // Verify that unpopulated persistable properties are null.
    assertNull(Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_SNAPSHOT));
    assertNull(Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_MESSAGE));

    // Verify that populated non-persistable properties were not stored.
    assertNull(Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_MIMETYPE));
    assertNull(Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_DISPLAYURL));
    assertNull(Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_CONTENT));
    assertNull(Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_LASTMODIFIED));
  }

  // Tests document update.
  public void testUpdateDocument() throws Exception {
    storeDocument(DOCID_XYZZY);

    // Now update it - changing one field and adding another.
    Document update = newDocument(DOCID_XYZZY, NEW_FEEDID, null, null, MESSAGE);
    storeDocument(update);
    Document stored = getDocument(DOCID_XYZZY);

    // Verify that populated persistable properties were stored,
    // and that previously persisted properties are preserved.
    assertEquals(DOCID_XYZZY, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_DOCID));
    assertEquals(NEW_FEEDID, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_FEEDID));
    assertEquals(PARENTID, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_PRIMARY_FOLDER));
    assertEquals(CONTAINER, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_CONTAINER));
    assertEquals(MESSAGE, Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_MESSAGE));

    // Verify that unpopulated persistable properties are null.
    assertNull(Value.getSingleValueString(stored,
        SpiConstants.PROPNAME_SNAPSHOT));
  }

  // Tests multiple document batches and sorted result sets.
  public void testMultiDocumentBatch() throws Exception {
    // Add documents in unsorted order.
    storeDocuments(FOOBAR_DOCS);
    // Verify that the result set returns them sorted.
    verifyDocuments(FOOBAR_DOCS);
  }

  // Tests multiple document batches and sorted result sets.
  public void testMultiDocumentFromDocid() throws Exception {
    // Add documents in unsorted order.
    storeDocuments(FOOBAR_DOCS);

    String[] subset = new String[FOOBAR_DOCS.length - 2];
    System.arraycopy(FOOBAR_DOCS, 2, subset, 0, FOOBAR_DOCS.length - 2);
    // Verify that the result set returns only the last half of the doclist.
    verifyDocuments(store.getDocumentIterator(FOOBAR_DOCS[1]), subset);
  }

  // Tests multiple connectors.
  public void testMultipleConnectors() throws Exception {
    storeDocuments(FOOBAR_DOCS);

    // Create another LocalDocumentStore on the same database.
    LocalDocumentStoreImpl storeB = newDocumentStore(CONNECTOR_NAME_B);
    // Add documents in unsorted order.
    storeDocuments(storeB, FLINT_DOCS);

    // Verify that the result set returns only documents for the
    // appropriate connectors - connector A.
    verifyDocuments(FOOBAR_DOCS);

    // Verify that the result set returns only documents for the
    // appropriate connectors - connector B.
    verifyDocuments(storeB, FLINT_DOCS);
  }

  // Tests multiple connectors whose docids might not be unique.
  public void testMultipleConnectorsSameDocids() throws Exception {
    String[] sorted = { DOCID_BAR, DOCID_BAZ, DOCID_FOO, DOCID_XYZZY };
    // Add documents in unsorted order.
    storeDocuments(sorted);

    // Create another LocalDocumentStore on the same database.
    LocalDocumentStoreImpl storeB = newDocumentStore(CONNECTOR_NAME_B);
    String[] sortedB = { DOCID_BAR, DOCID_BETTY, DOCID_XYZZY };
    // Add documents in unsorted order.
    storeDocuments(storeB, sortedB);

    // Verify that the result set returns only documents for the
    // appropriate connectors - connector A.
    verifyDocuments(sorted);

    // Verify that the result set returns only documents for the
    // appropriate connectors - connector B.
    verifyDocuments(storeB, sortedB);
  }

  // Tests delete.
  public void testdelete() throws Exception {
    // Add documents in unsorted order.
    storeDocuments(FOOBAR_DOCS);

    // Create another LocalDocumentStore on the same database.
    LocalDocumentStoreImpl storeB = newDocumentStore(CONNECTOR_NAME_B);
    // Add documents in unsorted order.
    storeDocuments(storeB, FLINT_DOCS);

    // Verify that the result set returns only documents for the
    // appropriate connectors - connector B.
    verifyDocuments(storeB, FLINT_DOCS);

    // Now delete storeB, deleteing all documents for ConnectorB.
    storeB.delete();
    // Verify that the result set returns no documents for ConnectorB.
    verifyNoDocuments(storeB.getDocumentIterator());

    // Verify that the table still holds documents for ConnectorA.
    verifyDocuments(FOOBAR_DOCS);
  }

  // Tests cancel.
  public void testCancel() throws Exception {
    // Add one batch of documents in unsorted order.
    storeDocuments(FOOBAR_DOCS);

    // Add another batch of documents in unsorted order.
    storeDocumentsNoFlush(store, FLINT_DOCS);
    // Cancel the batch, discarding these documents.
    store.cancel();
    store.flush();

    // Verify that the table holds only documents from the first batch.
    verifyDocuments(FOOBAR_DOCS);
  }
}
