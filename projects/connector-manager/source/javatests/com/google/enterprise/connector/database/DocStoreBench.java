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

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.common.AbstractCommandLineApp;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.LocalDocumentStore;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.util.database.JdbcDatabase;
import com.google.enterprise.connector.util.UniqueIdGenerator;
import com.google.enterprise.connector.util.UuidGenerator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import junit.framework.TestCase;

/**
 * Benchmark tests for {@link LocalDocumentStoreImpl}.
 *
 * <pre>
 * usage: DocStoreBench [-?] [-v] output_file
 *        -?, --help      Display this help.
 *        -v, --version   Display version.
 *        output_file     Destination file output.
 * </pre>
 */
public class DocStoreBench extends AbstractCommandLineApp {

  private static final String TEST_DIR_NAME =
      "testdata/tmp/DocStoreBench/";
  private final File baseDirectory = new File(TEST_DIR_NAME);

  // Minimum number of seconds for each benchmark. Repeat test
  // until elapsed time exceeds this value.
  private static final int MIN_SECONDS = 60;

  // Number of documents per batch to read.  TODO: Make this configurable?
  private static final int READ_BATCH_SIZE = 10000;

  private int batchSize;  // Number of docs per batch to store.
  private int numDocs;    // Total number of docs to store.
  private int numStores;  // Number of LocalDocumentStore instances.
  private int numThreads; // Number of Threads feeding each LocalDocumentStore.

  private UniqueIdGenerator uuid = new UuidGenerator();

  private String[] parents; // Assortment of Parent Folder IDs.

  private JdbcDatabase jdbcDatabase;

  @Override
  public String getName() {
    return "DocStoreBench";
  }

  @Override
  public String getDescription() {
    return "Benchmark tests for LocalDocumentStore Implementations.";
  }

  @Override
  public Options getOptions() {
    Options options = super.getOptions();
    options.addOption("b", "batchsize", true, "Number of docs to store before flush [100].");
    options.addOption("n", "numdocs", true, "Total number of docs to store [1000000].");
    options.addOption("s", "stores", true, "Number of document stores [1].");
    options.addOption("t", "threads", true, "Number of threads feeding each store [1].");
    return options;
  }

  @Override
  public String getCommandLineSyntax() {
    return super.getCommandLineSyntax()
        + "[-b batch_size] [-n num_docs] [-s num_stores] [-t num_threads]";
  }

  private int getIntOption(CommandLine commandLine, char opt, int defaultValue)
      throws NumberFormatException {
    String arg = commandLine.getOptionValue(opt);
    return (arg != null) ? Integer.parseInt(arg) : defaultValue;
  }

  private DataSource getDataSource() throws Exception {
    // Setup H2 JDBC DataSource.
    org.h2.jdbcx.JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
    dataSource.setURL("jdbc:h2:" + TEST_DIR_NAME
        + "DocStore;MVCC=TRUE;CACHE_SIZE=131072;MAX_OPERATION_MEMORY=0");
    dataSource.setUser("sa");
    dataSource.setPassword("sa");

    return dataSource;
  }

  @Override
  public void run(CommandLine commandLine) throws Exception {
    try {
      batchSize = getIntOption(commandLine,'b', 100);
      numDocs = getIntOption(commandLine,'n', 1000000);
      numStores = getIntOption(commandLine,'s', 1);
      numThreads = getIntOption( commandLine,'t', 1);
    } catch (NumberFormatException nfe) {
      printUsageAndExit(-1);
    }

    // Create database directory.
    baseDirectory.mkdirs();

    jdbcDatabase = new JdbcDatabase(getDataSource());
    System.out.println(jdbcDatabase.getDescription());

    // Create a set of plausible Parent IDs.
    parents = new String[10000];
    for (int i = 0; i < parents.length; i++) {
      parents[i] = uuid.uniqueId();
    }

    new StoreBenchMark().runBenchMark();
    new RetrieveBenchMark().runBenchMark();
    new StoreRetrieveBenchMark().runBenchMark();
    //new RetrieveFromDocidBenchMark().runBenchMark();
    //new ParentRetrieveBenchMark().runBenchMark();
    //new MaxRowsRetrieveBenchMark().runBenchMark();
    //new TopLimitRownumRetrieveBenchMark().runBenchMark();
  }

  /**
   * Proper main() in case this is called directly.
   */
  public static void main(String[] args) throws Exception {
    DocStoreBench app = new DocStoreBench();
    app.run(app.parseArgs(args));
    System.exit(0);
  }

  /**
   * BenchMark store documents into database.
   */
  private class StoreBenchMark extends BenchMark {
    StoreBenchMark() {
      this(numStores);
    }

    StoreBenchMark(int numStores) {
      super(numStores);
    }

    @Override
    BenchMarkRunnable newBenchMarkRunnable(int id) {
      return new BenchStore(jdbcDatabase, id, numThreads, numDocs/numStores,
                            batchSize, parents);
    }

    @Override
    void runBenchMark() {
      System.out.println("Starting Document Store Benchmark: "
          + "Number of Documents: " + numDocs
          + ", Batch Size: " + batchSize
          + ", Number of Stores: " + numStores
          + ", Threads per Store: " + numThreads);
      start();

      boolean stillRunning = false;
      int lastDocsStored = 0;
      long lastTime = getElapsedTime();
      do {
        stillRunning = join(60);
        int docsStored = getTotalDocs();
        int seconds = getElapsedSeconds();
        int hours = seconds / 3600;
        int mins = (seconds % 3600) / 60;
        int secs = seconds % 60;
        long elapsed = getElapsedTime();
        int dps = (int)(((docsStored - lastDocsStored) * 1000)/(elapsed - lastTime));
        lastDocsStored = docsStored;
        lastTime = elapsed;

        System.out.println("Total Documents Stored: " + docsStored
          + ", Elapsed Time (secs): " + seconds
          + " ( " + hours + "h " + mins + "m " + secs + "s )"
          + ", Documents per Second: " + dps);
      } while (stillRunning);
    }
  }

  /**
   * BenchMark read documents back from database using LocalDocumentStore
   * Iterator.
   */
  private class RetrieveBenchMark extends BenchMark {
    private final int runTime;

    RetrieveBenchMark() {
      this(numStores, MIN_SECONDS);
    }

    RetrieveBenchMark(int numStores, int runTime) {
      super(numStores);
      this.runTime = runTime;
    }

    @Override
    BenchMarkRunnable newBenchMarkRunnable(int id) {
      return new BenchRead(jdbcDatabase, id, runTime);
    }

    @Override
    void runBenchMark() {
      System.out.println("Starting Document Retrieve Benchmark: "
          + "Number of Documents: " + numDocs
          + ", Number of Stores: " + numStores);

      start();

      boolean stillRunning = false;
      do {
        stillRunning = join(60);
        int docsRead = getTotalDocs();
        int seconds = getElapsedSeconds();
        int hours = seconds / 3600;
        int mins = (seconds % 3600) / 60;
        int secs = seconds % 60;
        System.out.println(
            "Total Documents Read: " + docsRead
            + ", Elapsed Time (secs): " + seconds
            + " ( " + hours + "h " + mins + "m " + secs + "s )"
            + ", Iterations:" + getTotalIterations()
            + ", Documents per Second: " + (docsRead/seconds));
      } while (stillRunning);
    }
  }

  /**
   * BenchMark concurrent Storing and Retrieving
   */
  private class StoreRetrieveBenchMark extends TimedBenchMark {
    final int runTime = 10 * 60;
    final BenchMark storeBench;
    final BenchMark retrieveBench;

    StoreRetrieveBenchMark() {
      storeBench = new StoreBenchMark(1);
      retrieveBench = new RetrieveBenchMark(1, runTime);
    }

    void runBenchMark() {
      System.out.println("Starting Concurrent Document Store and Retrieve Benchmark: "
          + "Number of Stores: " + numStores);

      startTimer();
      storeBench.start();
      retrieveBench.start();

      boolean stillRunning = false;
      int lastDocsStored = 0;
      int lastDocsRead = 0;
      long lastTime = getElapsedTime();
      int seconds;
      do {
        stillRunning = storeBench.join(30);
        stillRunning |= retrieveBench.join(30);
        seconds = getElapsedSeconds();
        long elapsed = getElapsedTime();
        int docsStored = storeBench.getTotalDocs();
        int docsRead = retrieveBench.getTotalDocs();
        int mins = (seconds % 3600) / 60;
        int secs = seconds % 60;
        int sdps = (int)(((docsStored - lastDocsStored) * 1000)/(elapsed - lastTime));
        int rdps = (int)(((docsRead - lastDocsRead) * 1000)/(elapsed - lastTime));
        lastDocsStored = docsStored;
        lastDocsRead = docsRead;
        lastTime = elapsed;
        System.out.println(
            "Total Documents Stored: " + docsStored + " ( " + sdps + " docs/sec )"
            + ", Total Documents Read: " + docsRead + " ( " + rdps + " docs/sec )"
            + ", Elapsed Time (secs): " + seconds
            + " ( " + mins + "m " + secs + "s )");
      } while (stillRunning && (seconds < runTime));
    }
  }

  /**
   * BenchMark read documents back from database using LocalDocumentStore
   * Iterator.
   */
  private class RetrieveFromDocidBenchMark extends BenchMark {
    RetrieveFromDocidBenchMark() {
      super(numStores);
    }

    @Override
    BenchMarkRunnable newBenchMarkRunnable(int id) {
      return new BenchDocIdRead(jdbcDatabase, id, READ_BATCH_SIZE);
    }

    @Override
    void runBenchMark() {
      System.out.println("Starting Document Retrieve from DocId Benchmark: "
          + "Rows per ResultSet: " + READ_BATCH_SIZE);

      start();
      join();

      int docsRead = getTotalDocs();
      int seconds = getElapsedSeconds();
      System.out.println(
          "Total Documents Read: " + docsRead
          + ", Elapsed Time (secs): " + seconds
          + ", Iterations:" + getTotalIterations()
          + ", Documents per Second: " + (docsRead/seconds));
    }
  }

  /**
   * BenchMark read documents where parent is in list.
   */
  private class ParentRetrieveBenchMark extends BenchMark {
    ParentRetrieveBenchMark() {
      super(numStores);
    }

    @Override
    BenchMarkRunnable newBenchMarkRunnable(int id) {
      return new BenchParentRead(jdbcDatabase, id, parents);
    }

    @Override
    void runBenchMark() {
      System.out.println("Starting Parent Document Retrieve Benchmark: "
          + "Number of Parents per query: " + 100);

      start();
      join();

      int docsRead = getTotalDocs();
      int seconds = getElapsedSeconds();
      System.out.println(
          "Total Documents Read: " + docsRead
          + ", Elapsed Time (secs): " + seconds
          + ", Iterations:" + getTotalIterations()
          + ", Documents per Second: " + (docsRead/seconds));
    }
  }

  /**
   * BenchMark read all documents using JDBC Statement.setMaxRows()
   * to limit the number of rows returned in a result set.
   */
  private class MaxRowsRetrieveBenchMark extends BenchMark {
    MaxRowsRetrieveBenchMark() {
      super(numStores);
    }

    @Override
    BenchMarkRunnable newBenchMarkRunnable(int id) {
      return new BenchMaxRowsRead(jdbcDatabase, id, READ_BATCH_SIZE);
    }

    @Override
    void runBenchMark() {
      System.out.println("Starting MaxRows Document Retrieve Benchmark: "
          + "MaxRows per ResultSet: " + READ_BATCH_SIZE);

      start();
      join();

      int docsRead = getTotalDocs();
      int seconds = getElapsedSeconds();
      System.out.println(
          "Total Documents Read: " + docsRead
          + ", Elapsed Time (secs): " + seconds
          + ", Iterations:" + getTotalIterations()
          + ", Documents per Second: " + (docsRead/seconds));
    }
  }

  /**
   * BenchMark read all documents using SQL TOP/LIMIT/ROWNUM to
   * limit the number of rows returned in Result set.
   */
  private class TopLimitRownumRetrieveBenchMark extends BenchMark {
    TopLimitRownumRetrieveBenchMark() {
      super(numStores);
    }

    @Override
    BenchMarkRunnable newBenchMarkRunnable(int id) {
      return new BenchTopLimitRownumRead(jdbcDatabase, id, READ_BATCH_SIZE);
    }

    @Override
    void runBenchMark() {
      System.out.println("Starting TOP/LIMIT/ROWNUM Document Retrieve Benchmark: "
          + "Rows per ResultSet: " + READ_BATCH_SIZE);

      start();
      join();

      int docsRead = getTotalDocs();
      int seconds = getElapsedSeconds();
      System.out.println(
          "Total Documents Read: " + docsRead
          + ", Elapsed Time (secs): " + seconds
          + ", Iterations:" + getTotalIterations()
          + ", Documents per Second: " + (docsRead/seconds));
    }
  }

  private abstract class BenchMark extends TimedBenchMark {
    BenchMarkRunnable[] benchMarks;

    BenchMark(int numBenchMarks) {
      benchMarks = new BenchMarkRunnable[numBenchMarks];
    }

    /** Factory for creating a BenchMarkRunnable instance. */
    abstract BenchMarkRunnable newBenchMarkRunnable(int id);

    /** Actually run the benchmark. */
    abstract void runBenchMark();

    /** Start all the benchmark tests. */
    void start() {
      // Create the stores.
      for (int i = 0; i < benchMarks.length; i++) {
        benchMarks[i] = newBenchMarkRunnable(i);
      }

      startTimer();
      for (BenchMarkRunnable benchMark : benchMarks) {
        benchMark.start();
      }
    }

    /** Wait for the benchmark tests to finish. */
    void join() {
      while(join(0));
    }

    /**
     * Waits at least the specified number of seconds for all the
     * benchmarks to finish.
     *
     * @param seconds number of seconds to wait, 0 is wait forever.
     * @return true if benchmarks are still running, false otherwise.
     */
    boolean join(int seconds) {
      for (BenchMarkRunnable benchMark : benchMarks) {
        if (benchMark.join(seconds)) {
          return true;
        }
      }
      stopTimer();
      return false;
    }


    /** Returns number of docs stored/read/touched. */
    int getTotalDocs() {
      int totalDocs = 0;
      for (BenchMarkRunnable benchMark : benchMarks) {
        totalDocs += benchMark.getTotalDocs();
      }
      return totalDocs;
    }

    /** Returns number of test iterations. */
    int getTotalIterations() {
      int totalIterations = 0;
      for (BenchMarkRunnable benchMark : benchMarks) {
        totalIterations += benchMark.getTotalIterations();
      }
      return totalIterations;
    }
  }

  /**
   * Creates a LocalDocumentStore and stores the specified number of
   * documents to the store, using the specified number of threads to do so.
   */
  private class BenchStore extends BenchMarkRunnable {
    private final int numDocs;
    private final int batchSize;
    private final String[] parents;

    BenchStore(JdbcDatabase jdbcDatabase, int id, int numThreads, int numDocs,
               int batchSize, String[] parents) {
      super(jdbcDatabase, id, numThreads);
      this.numDocs = numDocs / numThreads;
      this.batchSize = batchSize;
      this.parents = parents;
    }

    /** Store numDocs Documents to the LocalDocumentStore. */
    public void run() {
      Random random = new Random();
      UniqueIdGenerator uuid = new UuidGenerator();
      String feedid = Thread.currentThread().getName();
      for (int i = 0, j = 0; i < numDocs; i++, j++) {
        if (j == batchSize) {
          store.flush();
          addDocs(batchSize);
          j = 0;
        }
        store.storeDocument(new SimpleDocument(
            ImmutableMap.<String, List<Value>> builder()
            .put(SpiConstants.PROPNAME_FEEDID, valueList(feedid))
            .put(SpiConstants.PROPNAME_DOCID, valueList(uuid.uniqueId()))
            .put(SpiConstants.PROPNAME_PRIMARY_FOLDER,
                 valueList(parents[random.nextInt(parents.length)]))
            .put(SpiConstants.PROPNAME_ACTION,
                 valueList(SpiConstants.ActionType.ADD.toString()))
            .build()));
      }
      store.flush();
      addDocs(numDocs % batchSize);
      addIteration();
    }

    /** Creates a List with a single String Value. */
    private List<Value> valueList(String value) {
      return Collections.singletonList(Value.getStringValue(value));
    }
  }

  /**
   * Benchmarks Read all documents from the store.
   */
  private class BenchRead extends BenchMarkRunnable {
    final int runTime;

    BenchRead(JdbcDatabase jdbcDatabase, int id, int runTime) {
      super(jdbcDatabase, id, 1);
      this.runTime = runTime;
    }

    /** Reads all the documents stored for this Connector. */
    /* @Override */
    public void run() {
      do {
        Iterator<Document> iter = store.getDocumentIterator();
        int docsRead = 0;
        while (iter.hasNext()) {
          iter.next();
          docsRead++;
          if (docsRead == 1000) {
            addDocs(docsRead);
            docsRead = 0;
          }
        }
        addDocs(docsRead);
        addIteration();
      } while (getElapsedSeconds() < runTime);
    }
  }

  /**
   * Benchmarks Read all documents from the store using
   * {@code LocalDocumentStore.getDocumentIterator(docid)}.
   */
  private class BenchDocIdRead extends BenchMarkRunnable {
    private int maxRows;

    BenchDocIdRead(JdbcDatabase jdbcDatabase, int id, int maxRows) {
      super(jdbcDatabase, id, 1);
      this.maxRows = maxRows;
    }

    /** Reads all the documents in chunks of size maxRows. */
    /* @Override */
    public void run() {
      do {
        String lastDocid = "";
        int docsRead;
        do {
          docsRead = 0;
          long start = System.currentTimeMillis();
          Iterator<Document> iter = store.getDocumentIterator(lastDocid);
          while (docsRead < maxRows && iter.hasNext()) {
            try {
              lastDocid = Value.getSingleValueString(iter.next(),
                          SpiConstants.PROPNAME_DOCID);
              if (lastDocid == null) System.err.println("WARNING: null docid in ResultSet item " + docsRead);
            } catch (RepositoryException ignored) {}
            docsRead++;
          }
          addDocs(docsRead);
        } while (docsRead == maxRows);
        addIteration();
      } while (getElapsedSeconds() < MIN_SECONDS);
    }
  }

  /**
   * Benchmarks Read documents from the store whose parents
   * match one from a list.
   */
  private class BenchParentRead extends SqlBenchMarkRunnable {
    private static final String parentsQuery = "SELECT docid FROM {0} WHERE ("
        + " connector_name = {1} AND folderparent IN ( {2} ))";

    private final String[] parents;
    private final Random random;

    BenchParentRead(JdbcDatabase jdbcDatabase, int id, String[] parents) {
      super(jdbcDatabase, id, 1);
      this.parents = parents;
      this.random = new Random();
    }

    /**
     * Reads all the documents whose parent matches one from an arbitarty
     * list.
     */
    /* @Override */
    public void run() {
      do {
        addDocs(runParentsQuery());
        addIteration();
      } while (getElapsedSeconds() < MIN_SECONDS);
    }

    /**
     * Runs a query against the DB, extracting documents whose parents
     * are in a set.  Simulates the query that would be delete of a large
     * hierarchical directory tree in the repository.
     */
    private int runParentsQuery() {
      // Construct list of Parent IDs.
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < 100; i++) {
        String parentId = parents[random.nextInt(parents.length)];
        builder.append(quoteValue(parentId)).append(',');
      }
      builder.setLength(builder.length() - 1);
      Object[] params = { store.getDocTableName(), quoteValue(connectorName),
                          builder.toString() };

      return runQuery(MessageFormat.format(parentsQuery, params), 0);
    }
  }

  /**
   * Benchmarks Read all documents from stores using JDBC
   * {@code Statement.setMaxRows()} to limit the size of the
   * returned ResultSet.
   */
  private class BenchMaxRowsRead extends SqlBenchMarkRunnable {
    private static final String maxRowsQuery = "SELECT docid FROM {0} WHERE "
        + "( docid > {1} ) ORDER BY docid";
    private String lastDocid;
    private int maxRows;

    BenchMaxRowsRead(JdbcDatabase jdbcDatabase, int id, int maxRows) {
      super(jdbcDatabase, id, 1);
      this.maxRows = maxRows;
    }

    /** Reads all the documents in chunks of size maxRows. */
    /* @Override */
    public void run() {
      do {
        lastDocid = " ";
        int numRows;
        do {
          numRows = runMaxRowsQuery();
          addDocs(numRows);
        } while (numRows > 0);
        addIteration();
      } while (getElapsedSeconds() < MIN_SECONDS);
    }

    /**
     * Runs a query against the DB, setting the maximum number of rows returned.
     */
    private int runMaxRowsQuery() {
      Object[] params = { store.getDocTableName(), quoteValue(lastDocid) };
      return runQuery(MessageFormat.format(maxRowsQuery, params), maxRows);
    }

    @Override
    void processRow(ResultSet resultSet) throws SQLException {
      lastDocid = resultSet.getString(1);
    }
  }

  /**
   * Benchmarks Read all documents from stores using
   * {@code SQL TOP/LIMIT/ROWNUM} to limit the size of the
   * returned ResultSet.
   */
  private class BenchTopLimitRownumRead extends SqlBenchMarkRunnable {
    private final Map<SpiConstants.DatabaseType, String> queries =
        ImmutableMap.<SpiConstants.DatabaseType, String> builder()
        .put(SpiConstants.DatabaseType.H2,
             "SELECT docid FROM {0} WHERE ( docid > {1} )"
             + " ORDER BY docid LIMIT {2}")
        .put(SpiConstants.DatabaseType.MYSQL,
             "SELECT docid FROM {0} WHERE ( docid > {1} )"
             + " ORDER BY docid LIMIT {2}")
        .put(SpiConstants.DatabaseType.ORACLE,
             "SELECT docid FROM {0} WHERE ( docid > {1}"
             + " AND ROWNUM <= {2} ) ORDER BY docid")
        .put(SpiConstants.DatabaseType.SQLSERVER,
             "SELECT TOP {2} docid FROM {0} "
             + "WHERE ( docid > {1} ) ORDER BY docid")
        .put(SpiConstants.DatabaseType.OTHER,
             "SELECT docid FROM {0} WHERE ( docid > {1} )"
             + " FETCH FIRST {2} ROWS ONLY ORDER BY docid")
        .build();

    private String lastDocid;
    private int maxRows;

    BenchTopLimitRownumRead(JdbcDatabase jdbcDatabase, int id, int maxRows) {
      super(jdbcDatabase, id, 1);
      this.maxRows = maxRows;
    }

    /**
     * Reads all the documents whose parent matches one from an arbitrary
     * list.
     */
    /* @Override */
    public void run() {
      do {
        lastDocid = " ";
        int numRows;
        do {
          numRows = runMaxRowsQuery();
          addDocs(numRows);
        } while (numRows > 0);
        addIteration();
      } while (getElapsedSeconds() < MIN_SECONDS);
    }

    /**
     * Runs a query against the DB, setting the maximum number of rows returned.
     */
    private int runMaxRowsQuery() {
      Object[] params = { store.getDocTableName(), quoteValue(lastDocid),
                          Integer.toString(maxRows) };
      String query = queries.get(jdbcDatabase.getDatabaseType());
      return runQuery(MessageFormat.format(query, params), 0);
    }

    @Override
    void processRow(ResultSet resultSet) throws SQLException {
      lastDocid = resultSet.getString(1);
    }
  }

  /**
   * Benchmarks Read documents from the store whose parents
   * match one from a list.
   */
  private abstract class SqlBenchMarkRunnable extends BenchMarkRunnable {
    SqlBenchMarkRunnable(JdbcDatabase jdbcDatabase, int id, int numThreads) {
      super(jdbcDatabase, id, numThreads);
    }

    /**
     * Process a {@link ResultSet} row.  This is called iteratively
     * for each row of a {@code ResultSet}.  This implementatino does
     * nothing with the row.  Subclasses may override this method to
     * perform real work.
     */
    void processRow(ResultSet resultSet) throws SQLException {
    }

    /**
     * Runs the {@code query} against the DB, iterating over the returned
     * {@link ResultSet}, calling {@link #processRow(ResultSet)} for each row.
     *
     * @param query the SQL query to run
     * @param maxRows if greater than 0, the maximum number of rows to return.
     * @return the number of ResultSet rows processed.
     */
    int runQuery(String query, int maxRows) {
      int rows = 0;
      Connection connection = null;
      Statement statement = null;
      try {
        connection = jdbcDatabase.getConnectionPool().getConnection();
        statement = connection.createStatement();
        if (maxRows > 0) {
          statement.setMaxRows(maxRows);
        }
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
          processRow(resultSet);
          rows++;
        }
      } catch (SQLException e) {
        System.err.println("Failed to retrieve document." + e);
      } finally {
        try {
          if (statement != null) {
            statement.close();
          }
        } catch (SQLException ignored) {
          // Already processing an exception.
        } finally {
          if (connection != null) {
            jdbcDatabase.getConnectionPool().releaseConnection(connection);
          }
        }
      }
      return rows;
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
    String quoteValue(String value) {
      return "'" + value.replace("'", "''") + "'";
    }
  }

  /**
   * Abstract Benchmark Runnable class runs the benchmark in one
   * or more separate threads.
   */
  private abstract class BenchMarkRunnable extends TimedBenchMark
      implements Runnable {
    final JdbcDatabase jdbcDatabase;
    final LocalDocumentStoreImpl store;
    final String connectorName;
    final Thread[] threads;
    int totalDocs = 0;
    int totalIterations = 0;

    BenchMarkRunnable(JdbcDatabase jdbcDatabase, int id, int numThreads) {
      this.jdbcDatabase = jdbcDatabase;
      connectorName = "Connector" + id;
      store = new LocalDocumentStoreImpl(jdbcDatabase, connectorName);
      threads = new Thread[numThreads];
      for (int i = 0; i < numThreads; i++) {
        threads[i] = new Thread(this, "Thread" + id + "." + i);
      }
    }

    /** Starts all the threads. */
    void start() {
      startTimer();
      for (Thread thread : threads) {
        thread.start();
        try { Thread.sleep(1000); } catch (InterruptedException ie) {}
      }
    }

    /** Waits for all the threads to finish. */
    void join() {
      while(join(0));
    }

    /**
     * Waits up at least the specified number of seconds for all the
     * threads to finish.
     *
     * @param seconds number of seconds to wait, 0 is wait forever.
     * @return true if threads still alive, false otherwise.
     */
    boolean join(int seconds) {
      for (Thread thread : threads) {
        try {
          thread.join(seconds * 1000L);
          // If thread is still alive, we must have timed out.
          if (thread.isAlive()) {
            return true;
          }
        } catch (InterruptedException e) {
          break;
        }
      }

      // If we got all the way through, all of them are dead.
      stopTimer();
      return false;
    }

    /** Runs the Benchmark worker thread. */
    abstract public void run();

    /** Returns number of docs stored/read/touched. */
    synchronized int getTotalDocs() {
      return totalDocs;
    }

    /** Adds the supplied value to totalDocs. */
    synchronized int addDocs(int count) {
      return (totalDocs += count);
    }

    /** Returns number of test iterations. */
    synchronized int getTotalIterations() {
      return totalIterations;
    }

    /** Bumps the iteration count. */
    synchronized int addIteration() {
      return (++totalIterations);
    }
 }

  /**
   * Timing a BenchMark test.
   */
  private class TimedBenchMark {
    long startTime = 0L;
    long stopTime = 0L;

    /** Starts the benchmark timer. */
    synchronized void startTimer() {
      startTime = System.currentTimeMillis();
    }

    /** Starts the benchmark timer. */
    synchronized void stopTimer() {
      stopTime = System.currentTimeMillis();
    }

    /** Returns the elapsed time in milliseconds. */
    synchronized long getElapsedTime() {
      return ((stopTime == 0L) ? System.currentTimeMillis() : stopTime) - startTime;
    }

    /** Returns the elapsed time for the test, in seconds. */
    int getElapsedSeconds() {
      return (int) ((getElapsedTime() + 999)/1000);
    }
  }
}
