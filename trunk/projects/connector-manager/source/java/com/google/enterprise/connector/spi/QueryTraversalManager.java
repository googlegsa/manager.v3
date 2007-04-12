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

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.traversal.QueryTraverserMonitor;

/**
 * Interface for implementing query-based traversal.
 * <p>
 * Query-based traversal is a scheme whereby a repository is traversed according
 * to a query that visits each document in a natural order that is efficiently
 * supported by the underlying repository and can be easily checkpointed and
 * restarted.
 * <p>
 * A good use case is a repository that supports access to documents in
 * last-modified-date order. In particular, suppose a repository supports a
 * query analogous to the following SQL query:
 * <p>
 * 
 * <pre>
 *   select documentid, lastmodifydate from documents 
 *   where  lastmodifydate &gt; &lt;i&gt;date-constant&lt;/i&gt; 
 *   order by lastmodifydate
 * </pre>
 * 
 * <p>
 * Such a repository can easily be traversed by lastmodifydate, and the state of
 * the traversal is easily encapsulated in a single, small data item: the date
 * of the last document processed. Also, this is particularly convenient because
 * if a document is processed during traversal, but then later modified, then it
 * will be picked up again later in the traversal process. Thus, this traversal
 * is appropriate both for initial load and for incremental update.
 * <p>
 * For such a repository, the implementor is urged to let the Connector Manager
 * (the caller) maintain the traversal state. This is achieved by implementing
 * the interface methods as follows:
 * <ul>
 * <li><code>startTraversal()</code> Run a query that starts from the
 * beginning, such as <br>
 * <code>
 * select documentid, lastmodifydate from documents order by lastmodifydate
 * </code>
 * <li><code>checkpoint(PropertyMap pm)</code> Extract the lastmodifydate
 * from the document that the caller passes in to you as a property map, encode
 * it as a string, and return it
 * <li><code>resumeTraversal(String checkpoint)</code> Run a query that
 * resumes traversal from the supplied checkpoint
 * </ul>
 * Please observe that the Connector Manager (the caller) makes no guarantee to
 * consume the entire ResultSet returned by either the startTraversal or
 * resumeTraversal calls. The Connector Manager will consume as many it chooses,
 * depending on load, schedule and other factors. The Connector Manager
 * guarantees to call getCheckpoint supplying the last document it has
 * successfully processed from the ResultSet it was using. Thus, the implementor
 * is free to use a query that only returns a small number of results, if that
 * gets better performance.
 * <p>
 * For example, to continue the SQL analogy, a query like this could be used:
 * 
 * <pre>
 *   select TOP 10 documentid, lastmodifydate from documents ...
 * </pre>
 * 
 * The <code>setBatchHint</code> method is provided so that the Connector
 * Manager can tell the implementation that it only wants that many results per
 * call. This is a hint - the implementation need not observe it. The
 * implementation is free to return a ResultSet with fewer or more results. For
 * example, the traversal may be completely up to date, so perhaps there are no
 * results to return. Or, for internal reasons, the implementation may not want
 * to return the full batchHint number of results. Probably, returning zero
 * results will have impact on scheduling - the Connector Manager may choose to
 * wait longer after receiving zero results before it calls again. But
 * implementations are free to return zero results if they choose. Also, if zero
 * results are returned, the Connector Manager will probably not call
 * <code>checkpoint</code> before calling start or resume traversal again.
 * <p>
 * An implementation need not let the Connector Manager store the traversal
 * state, it may choose to store the state itself. Implementors are discouraged
 * from using this technique unless necessary, because it makes transactionality
 * more difficult and it introduces resource dependencies of which the Connector
 * Manager is unaware. However, there may be repositories which have a natural
 * traversal order, but this state of this traversal is not easily expressed in
 * a small data item. For example, a repository may consist of a large number of
 * named sub-repositories, each of which can be traversed in modify date order,
 * but for which there is no convenient way of traversing them all in one query.
 * In this case, the implementation may choose to maintain state itself, as a
 * table of pairs: (sub-repository-name, per-repository-date-stamp). In such a
 * case, the implementor may implement the interface methods as follows:
 * <ul>
 * <li><code>startTraversal()</code> Clear the internal state. Return the
 * first few documents
 * <li><code>getCheckpoint(PropertyMap pm)</code> This can be taken as a
 * signal from the Connector Manager that documents have been successfully
 * processed, up to the last one reported. The implementation can now commit a
 * change to external store bringing it up to that document. A null String may
 * be returned, which signals to the Connector Manager that it need not bother
 * to store any state on the implementation's behalf. Or the implementation may
 * return a diagnostic string.
 * <li><code>resumeTraversal(String checkpoint)</code> Resume traversal
 * according to the internal state of the implementation. The Connector Manager
 * will pass in whatever checkpoint String was returned by the last call to
 * getCheckpoint - but the implementation is free to ignore this and use its
 * internal state.
 * </ul>
 * The implementation must be careful about when and how it commits its internal
 * state to external storage. Remember again that the Connector Manager makes no
 * guarantee to consume the entire result set return by a traversal call. So the
 * implementation should wait until the ckecpoint call, examine the parameter
 * passed in and only commit the state up to that document.
 * <p>
 * <b> Note on property maps returned by traversal calls </b>
 * <p>
 * The property maps returned by the queries defined here represent documents.
 * They must contain special properties according to the following rules:
 * <ul>
 * <li> {@link SpiConstants}.PROPNAME_DOCID This property must be present.
 * <li> {@link SpiConstants}.PROPNAME_CONTENTURL If present, the GSA will pull
 * content using the value of this property. If not present, then the
 * {@link SpiConstants}.PROPNAME_CONTENT property should be present.
 * <li> {@link SpiConstants}.PROPNAME_CONTENT Will not be used if the
 * {@link SpiConstants}.PROPNAME_CONTENTURL is present. If present, the
 * connector framework will base-64 encode the value and present it to the GSA
 * as the primary content to be indexed.
 * <li> {@link SpiConstants}.PROPNAME_SECURITYCLASS If present, this will be
 * persisted in the index and used at serve-time to determine whether a given
 * user is authorized to view this document.
 * <li> {@link SpiConstants}.PROPNAME_DISPLAYURL If present, this will be used
 * as the primary link on a results page
 * </ul>
 */
public interface QueryTraversalManager {

  /**
   * Starts (or restarts) traversal from the beginning. This action will return
   * objects starting from the very oldest, or with the smallest IDs, or
   * whatever natural order the implementation prefers. The caller may consume
   * as many or as few of the results as it wants, but it gurantees to call
   * {@link #checkpoint(PropertyMap)} passing in the past object is has
   * successfully processed.
   * @param monitor An object implementing QueryTraverserMonitor, which the
   *        Traverser (or the objects that IT calls) may use to request a 
   *        different timeout, and/or to report progress.
   * @return A ResultSet of documents from the repository in natural order
   * @throws RepositoryException if the Repository is unreachable or similar
   *           exceptional condition.
   */
  public ResultSet startTraversal(QueryTraverserMonitor monitor) 
    throws RepositoryException;

  /**
   * Continues traversal from a supplied checkpoint. The checkPoint parameter
   * will have been created by a call to the {@link #checkpoint(PropertyMap)}
   * method. The ResultSet object returns objects from the repository in natural
   * order starting just after the document that was used to create the
   * checkpoint string.
   * 
   * @param checkPoint String that indicates from where to resume traversal.
   * @param monitor An object implementing QueryTraverserMonitor, which the
   *        Traverser (or the objects that IT calls) may use to request a 
   *        different timeout, and/or to report progress.
   * @return ResultSet object that returns documents starting just after the
   *         checkpoint.
   * @throws RepositoryException
   */
  public ResultSet resumeTraversal(String checkPoint,
      QueryTraverserMonitor monitor)
      throws RepositoryException;

  /**
   * Checkpoints the traversal process. The caller passes in a property map
   * taken from the {@link ResultSet} object that it obtained from either the
   * startTraversal or resumeTraversal methods. This property map is the last
   * document that the caller successfully processed. This is NOT necessarily
   * the last object from the result set - the caller may consume as much or as
   * little of a result set as it chooses. If the implementation wants the
   * caller to persist the traversal state, then it should write a string
   * representation of that state and return it. If the implementation prefers
   * to maintain state itself, it should use this call as a signal to commit its
   * state, up to the document passed in.
   * 
   * @param pm A property map obtained from a ResultSet obtained from either
   *          {@link #startTraversal()} or {link
   *          {@link #resumeTraversal(String)}.
   * @return A string that can be used by a subsequent call to the
   *         {@link #resumeTraversal(String)} method.
   * @throws RepositoryException
   */
  public String checkpoint(PropertyMap pm) throws RepositoryException;

  /**
   * Sets the preferred batch size. The caller advises the implementation that
   * the result sets returned by startTraversal or resumeTraversal need not be
   * larger than this number. The implementation may ignore this call or do its
   * best to return approximately this number.
   * 
   * @param batchHint
   * @throws RepositoryException
   */
  public void setBatchHint(int batchHint) throws RepositoryException;
}
