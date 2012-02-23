// Copyright 2007-2008 Google Inc.
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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * Adaptor to JCR class of the same name
 */
public class JcrTraversalManager implements TraversalManager {

  javax.jcr.query.QueryManager queryManager;

  private static final String XPATH_QUERY_STRING_UNBOUNDED_DEFAULT =
    "//*[@jcr:primaryType='nt:resource'] order by @jcr:lastModified, @jcr:uuid";

  private static final String XPATH_QUERY_STRING_BOUNDED_DEFAULT =
    "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= " +
    "''{0}''] order by @jcr:lastModified, @jcr:uuid";

  private String xpathUnboundedTraversalQuery;
  private String xpathBoundedTraversalQuery;

  /**
   * @param xpathBoundedTraversalQuery the xpathBoundedTraversalQuery to set
   */
  void setXpathBoundedTraversalQuery(String xpathBoundedTraversalQuery) {
    this.xpathBoundedTraversalQuery = xpathBoundedTraversalQuery;
  }

  /**
   * @param xpathUnboundedTraversalQuery the xpathUnboundedTraversalQuery to set
   */
  void setXpathUnboundedTraversalQuery(String xpathUnboundedTraversalQuery) {
    this.xpathUnboundedTraversalQuery = xpathUnboundedTraversalQuery;
  }

  public JcrTraversalManager(QueryManager queryManager) {
    this.queryManager = queryManager;
    this.xpathUnboundedTraversalQuery = XPATH_QUERY_STRING_UNBOUNDED_DEFAULT;
    this.xpathBoundedTraversalQuery = XPATH_QUERY_STRING_BOUNDED_DEFAULT;
  }

  public DocumentList resumeTraversal(String checkPoint)
      throws RepositoryException {
    JSONObject jo = null;
    try {
      jo = new JSONObject(checkPoint);
    } catch (JSONException e) {
      throw new IllegalArgumentException(
          "checkPoint string does not parse as JSON: " + checkPoint);
    }
    String uuid = extractDocidFromCheckpoint(jo, checkPoint);
    Calendar c = extractCalendarFromCheckpoint(jo, checkPoint);
    String queryString = makeCheckpointQueryString(c);
    String lang = Query.XPATH;
    javax.jcr.query.Query query = makeCheckpointQuery(queryString, lang);
    // now iterate past the last document processed
    QueryResult queryResult = null;
    try {
      queryResult = query.execute();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    Node thisNode = null;
    boolean useThisNode = false;
    NodeIterator nodes = null;
    try {
      for (nodes = queryResult.getNodes(); nodes.hasNext();) {
        thisNode = nodes.nextNode();
        useThisNode = true;
        Calendar thisCal = thisNode.getProperty("jcr:lastModified").getDate();
        if (thisCal.after(c)) {
          // we have passed the last document we processed
          break;
        }
        String thisUuid = thisNode.getUUID();
        if (thisUuid.compareTo(uuid) > 0) {
          // we have passed the last document we processed
          break;
        }
        useThisNode = false;
      }
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }

    if (useThisNode) {
      DocumentList result = new JcrDocumentList(thisNode, nodes);
      return result;
    }

    return (nodes.hasNext()) ? new JcrDocumentList(nodes) : null;
  }

  private javax.jcr.query.Query makeCheckpointQuery(String queryString,
      String lang) throws RepositoryException {
    javax.jcr.query.Query query = null;
    try {
      query = queryManager.createQuery(queryString, lang);
    } catch (javax.jcr.query.InvalidQueryException e) {
      throw new RepositoryException(e);
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return query;
  }

  private String makeCheckpointQueryString(Calendar c) {

    String time = Value.calendarToIso8601(c);
    Object[] arguments = { time };
    String statement = MessageFormat.format(
        xpathBoundedTraversalQuery,
        arguments);
    return statement;
  }

  String extractDocidFromCheckpoint(JSONObject jo, String checkPoint) {
    String uuid = null;
    try {
      uuid = jo.getString("uuid");
    } catch (JSONException e) {
      throw new IllegalArgumentException(
          "could not get uuid from checkPoint string: " + checkPoint);
    }
    return uuid;
  }

  Calendar extractCalendarFromCheckpoint(JSONObject jo, String checkPoint) {
    String dateString = null;
    try {
      dateString = jo.getString("lastModified");
    } catch (JSONException e) {
      throw new IllegalArgumentException(
          "could not get lastmodify from checkPoint string: " + checkPoint);
    }
    Calendar c = null;
    try {
      c = Value.iso8601ToCalendar(dateString);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
          "could not parse date string from checkPoint string: " + dateString);
    }
    return c;
  }

  public DocumentList startTraversal() throws RepositoryException {
    String lang = Query.XPATH;
    javax.jcr.query.Query query =
      makeCheckpointQuery(xpathUnboundedTraversalQuery, lang);
    QueryResult queryResult = null;
    try {
      queryResult = query.execute();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    NodeIterator nodes = null;
    try {
      nodes = queryResult.getNodes();
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }
    return (nodes.hasNext()) ? new JcrDocumentList(nodes) : null;
  }

  public void setBatchHint(int batchHint) {
  }

}
