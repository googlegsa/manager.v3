// Copyright 2007 Google Inc.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * Adaptor to JCR class of the same name
 */
public class JcrTraversalManager implements TraversalManager {
  private static final Logger logger = Logger
      .getLogger(JcrTraversalManager.class.getName());

  private javax.jcr.query.QueryManager queryManager;
  private javax.jcr.observation.ObservationManager observationManager;
  private RepositoryListener repoListener = null;

  private static final String XPATH_QUERY_STRING_UNBOUNDED_DEFAULT = 
    "//*[@jcr:primaryType='nt:resource'] order by @jcr:lastModified, @jcr:uuid";

  private static final String XPATH_QUERY_STRING_BOUNDED_DEFAULT = 
    "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= " +
    "''{0}''] order by @jcr:lastModified, @jcr:uuid";

  private String xpathUnboundedTraversalQuery;
  private String xpathBoundedTraversalQuery;

  private class RepositoryListener implements EventListener {
    private Map addEventStore = Collections.synchronizedMap(new HashMap());
    private Map deleteEventStore = Collections.synchronizedMap(new HashMap());

    public void onEvent(EventIterator eventIter) {
      while (eventIter.hasNext()) {
        Event event = (Event) eventIter.next();
        processEvent(event);
      }
    }

    public List getDeleteEvents() {
      return new LinkedList(deleteEventStore.values());
    }

    public void clearEventStore() {
      addEventStore.clear();
      deleteEventStore.clear();
    }

    /**
     * Used to process the event.  This listener is mainly concerned with
     * maintaining a good list of delete events.
     */
    private void processEvent(Event event) {
      int type = event.getType();
      String docid;
      try {
        docid = event.getPath();
        logger.finer("Received event: docid=" + docid + ", type=" + type);

        if (type == Event.NODE_ADDED) {
          if (deleteEventStore.containsKey(docid)) {
            deleteEventStore.remove(docid);
          } else {
            addEventStore.put(docid, event);
          }
        } else if (type == Event.NODE_REMOVED) {
          if (addEventStore.containsKey(docid)) {
            addEventStore.remove(docid);
          } else {
            deleteEventStore.put(docid, event);
          }
        }
      } catch (javax.jcr.RepositoryException e) {
        logger.log(Level.SEVERE, "Unable to access the event path", e);
      }
    }
  }

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

  public JcrTraversalManager(QueryManager queryManager, 
      ObservationManager observationManager) {
    this.queryManager = queryManager;
    this.observationManager = observationManager;
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
    String queryString = makeCheckpointQueryString(uuid, c);

    // Pull the list of events from the observed delete store and reset the
    // observer before the next query.
    registerListener();
    Collection deleteEvents = repoListener.getDeleteEvents();
    repoListener.clearEventStore();

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
        useThisNode = false;
      }
    } catch (javax.jcr.RepositoryException e) {
      throw new RepositoryException(e);
    }

    JcrDocumentList result = null;
    if (useThisNode) {
      result = new JcrDocumentList(thisNode, nodes);
    } else {
      result = new JcrDocumentList(nodes);
    }
    // If there are delete events we need to inject them as documents in the
    // DocumentList result.
    if (deleteEvents.size() > 0) {
      for (Iterator iter = deleteEvents.iterator(); iter.hasNext();) {
        Event event = (Event) iter.next();
        // Use the checkpoint calendar time as last modified
        JcrEventDocument deleteDocument = 
            new JcrEventDocument(event, c);
        result.insert(deleteDocument);
      }
    }
    return result;
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

  private String makeCheckpointQueryString(String uuid, Calendar c) {
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

  /**
   * Used to try to attach as observer.  It's important to do this before the
   *  query is constructed.
   *
   * @throws RepositoryException
   */
  private void registerListener() throws RepositoryException {
    if (repoListener == null) {
      try {
        repoListener = new RepositoryListener();
        observationManager.addEventListener(repoListener, 0, null, true,
            null, null, false);
      } catch (javax.jcr.RepositoryException e) {
        throw new RepositoryException("Unable to attach as observer.", e);
      }
    }
  }

  public DocumentList startTraversal() throws RepositoryException {
    registerListener();
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
    DocumentList result = new JcrDocumentList(nodes);
    return result;
  }

  public void setBatchHint(int batchHint) throws RepositoryException {
  }
}
