// Copyright 2006 Google Inc.
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

/**
 * Interface for implementing query-based traversal.
 * <p/>
 * Query-based traversal is a scheme whereby a repository is traversed according
 * to a query that visits each document in a natural order that is efficiently
 * supported by the underlying repository and can be easily checkpointed and
 * restarted.
 * <p/>
 * A good use case is a repository that supports access to documents in
 * last-modified-date order. In particular, suppose a repository supports a
 * query analogous to the following SQL query (the repository need not support
 * SQL, SQL is used here only as an example):
 * <p/>
 *
 * <pre>
 *        select documentid, lastmodifydate from documents
 *        where  lastmodifydate &lt; <b><i>date-constant</i></b>
 *        order by lastmodifydate
 * </pre>
 *
 * <p/>
 * Such a repository can easily be traversed by lastmodifydate, and the state of
 * the traversal is easily encapsulated in a single, small data item: the date
 * of the last document processed. Increasing last-modified-date order is
 * convenient because if a document is processed during traversal, but then
 * later modified, then it will be picked up again later in the traversal
 * process. Thus, this traversal is appropriate both for initial load and for
 * incremental update.
 * <p/>
 * For such a repository, the implementor is urged to let the Connector Manager
 * (the caller) maintain the traversal state. This is achieved by implementing
 * the interface methods as follows:
 * <ul>
 * <li>{@code startTraversal()} Run a query that starts from the
 * beginning, such as
 * <pre>
 *   select documentid, lastmodifydate from documents order by lastmodifydate
 * </pre></li>
 * <li>{@code resumeTraversal(String checkpoint)} Run a query that
 * resumes traversal from the supplied checkpoint</li>
 * </ul>
 * Checkpoints are supplied by the
 * {@link DocumentList#checkpoint()} method.
 * <p/>
 * Please observe that the Connector Manager (the caller) makes no guarantee
 * to consume the entire {@code DocumentList} returned by either the
 * {@code startTraversal} or {@code resumeTraversal} calls.
 * The Connector Manager will consume as many it chooses, depending on load,
 * schedule and other factors. The Connector Manager guarantees to call
 * {@code checkpoint} after handling the last document it has
 * successfully processed from the {@code DocumentList} it was using.
 * Thus, the implementor is free to use a query that only returns a small
 * number of results, if that gets better performance.
 * <p/>
 * For example, to continue the SQL analogy, a query like this could be used:
 * <pre>
 *        select TOP 10 documentid, lastmodifydate from documents ...
 * </pre>
 *
 * <p/>
 * The {@code setBatchHint} method is provided so that the Connector
 * Manager can tell the implementation that it only wants that many results per
 * call. This is a hint - the implementation need not observe it. The
 * implementation is free to return a DocumentList with fewer or more
 * results. For example, the traversal may be completely up to date, so perhaps
 * there are no results to return. Or, for internal reasons, the implementation
 * may not want to return the full batchHint number of results.  When returning
 * more results than the hint, some or all of the extra documents may be
 * ignored.
 * <p/>
 * The Connector Manager makes a distinction between the return of a 
 * {@code null} DocumentList and an empty DocumentList (a DocumentList with 
 * zero entries). Returning a {@code null} DocumentList will have an impact on
 * scheduling - the Connector Manager may choose to wait longer after receiving
 * a {@code null} result before it calls again.  Also, if a {@code null} result
 * is returned, the Connector Manager will not [indeed, cannot] call
 * {@code checkpoint} before calling start or resume traversal again. Returning
 * a {@code null} DocumentList is suitable when a traversal is completely up to
 * date, with no new documents available and no new checkpoint state.
 * <p/>
 * Returning an empty DocumentList will probably not have an impact on
 * scheduling.  The Connector Manager will call {@code checkpoint},
 * and will likely call {@code resumeTraversal} again immediately.
 * Returning an empty DocumentList is not appropriate if a traversal is
 * completely up to date, as it would effectively induce a spin, constantly
 * calling {@code resumeTraversal} when it has no work to do.
 * Returning an empty DocumentList is a convenient way to indicate to the
 * Connector Manager, that although no documents were provided in this
 * batch, the Connector wishes to continue searching the repository for
 * suitable content.  The call to {@code checkpoint} allows the
 * Connector to record its progress through the repository.  This mechanism
 * is suitable for cases when the search for suitable content may exceed
 * the Connector Manager's timeout.
 * <p/>
 * If the Connector returns a non-{@code null} {@code DocumentList}, even
 * one with zero entries, the Connector Manager will nearly always call
 * {@code checkpoint} when it has finished processing the DocumentList.
 * <p/>
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
 * <li>{@code startTraversal()} Clear the internal state. Return the
 * first few documents</li>
 * <li>{@code resumeTraversal(String checkpoint)} Resume traversal
 * according to the internal state of the implementation. The Connector Manager
 * will pass in whatever checkpoint String was returned by the last call to
 * {@link DocumentList#checkpoint()} but the implementation is free to ignore
 * this and use its internal state.  However, even in this case, 
 * {@code checkpoint} must not return a {@code null} String.</li>
 * </ul>
 * The implementation must be careful about when and how it commits its internal
 * state to external storage. Remember again that the Connector Manager makes no
 * guarantee to consume the entire result set return by a traversal call. If the
 * Connector Manager does not call checkpoint, the implementation should not
 * assume that the documents returned by {@link DocumentList#nextDocument} have
 * been processed. The implementation should wait until the checkpoint call, and
 * only commit the state up to the last document returned.
 * <p/>
 * <strong>Note on "Metadata and URL" feeds vs. Content feeds:</strong>
 * <p/>
 * Some repositories are fully web-enabled but are difficult or impossible for
 * the Search Appliance to crawl, because they make heavy use of ASP or JSP, or
 * they have a metadata model that is not conveniently accessible with the
 * content in a single page. Such repositories are good candidates for
 * connectors. However, a developer may not choose to implement authentication
 * and authorization through a connector. It may be sufficient to use standard
 * web mechanisms for these tasks.
 * <p/>
 * The developer can achieve this by following these steps. In the document list
 * returned by the traversal methods, specify the
 * {@link SpiConstants#PROPNAME_SEARCHURL}
 * property. The value should be a URL. If this property is specified, the
 * Connector Manager will use a "URL Feed" rather than a "Content Feed" for
 * that document. In this case, the implementor should <strong>not</strong>
 * supply the content of the document. The Search Appliance will fetch the
 * content from the specified URL. Also, this URL will be used to trigger 
 * normal authentication and authorization for that document. For more details, 
 * see the documentation on Metadata and URL Feeds.
 * <p/>
 * <strong>Note on Documents returned by traversal calls:</strong>
 * <p/>
 * The {@code Document} objects returned by the queries defined here
 * must contain special properties according to the following rules:
 * <ul>
 * <li> {@link SpiConstants#PROPNAME_DOCID} This property must be present.</li>
 * <li> {@link SpiConstants#PROPNAME_SEARCHURL} If present, this means that the
 * Connector Manager will generate a Metadata and URL feed, with the specified
 * URL. If this is present, then the {@link SpiConstants#PROPNAME_CONTENT}
 * property should <strong>not</strong> be.</li>
 * <li> {@link SpiConstants#PROPNAME_CONTENT} This property should hold the
 * content of the document. If present, the connector framework will base-64
 * encode the value and present it to the Search Appliance as the primary
 * content to be indexed. If this is present, then the 
 * {@link SpiConstants#PROPNAME_SEARCHURL} property should <strong>not</strong>
 * be.</li>
 * <li> {@link SpiConstants#PROPNAME_DISPLAYURL} If present, this will be used
 * as the primary link on a results page. This should <strong>not</strong>
 * be used with {@link SpiConstants#PROPNAME_SEARCHURL}.</li>
 * </ul>
 */
public interface TraversalManager {

  /**
   * Starts (or restarts) traversal from the beginning. This action will return
   * objects starting from the very oldest, or with the smallest IDs, or
   * whatever natural order the implementation prefers. The caller may consume
   * as many or as few of the results as it wants, but it guarantees to call
   * {@link DocumentList#checkpoint()} passing in the last object
   * it has successfully processed.
   *
   * @return A DocumentList of documents from the repository in natural order,
   *         or {@code null} if there are no documents.
   * @throws RepositoryException if the Repository is unreachable or similar
   *         exceptional condition.
   */
  public DocumentList startTraversal() throws RepositoryException;

  /**
   * Continues traversal from a supplied checkpoint. The checkPoint parameter
   * will have been created by a call to the
   * {@link DocumentList#checkpoint()} method. The
   * DocumentList object returns objects from the repository in natural order
   * starting just after the document that was used to create the checkpoint
   * string.
   *
   * @param checkPoint String that indicates from where to resume traversal.
   * @return DocumentList object that returns documents starting just after the
   *         checkpoint, or {@code null} if there are no documents.
   * @throws RepositoryException
   */
  public DocumentList resumeTraversal(String checkPoint)
      throws RepositoryException;

  /**
   * Sets the preferred batch size. The caller advises the implementation that
   * the result sets returned by startTraversal or resumeTraversal should be
   * as close to this number as is reasonable. The implementation may ignore
   * this call or do its best to return approximately this number.
   *
   * @param batchHint
   * @throws RepositoryException
   */
  public void setBatchHint(int batchHint) throws RepositoryException;
}
