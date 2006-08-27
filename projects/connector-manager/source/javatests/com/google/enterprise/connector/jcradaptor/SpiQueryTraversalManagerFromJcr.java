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

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.Value;

import org.json.JSONException;
import org.json.JSONObject;

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
public class SpiQueryTraversalManagerFromJcr implements QueryTraversalManager {

  javax.jcr.query.QueryManager queryManager;

  public SpiQueryTraversalManagerFromJcr(QueryManager queryManager) {
    this.queryManager = queryManager;
  }

  public String checkpoint(PropertyMap pm) throws RepositoryException {
    String uuid = fetchAndVerifyValueForCheckpoint(pm, "jcr:uuid").getString();
    Calendar c = fetchAndVerifyValueForCheckpoint(pm, "jcr:lastModified")
        .getDate();
    String dateString = SimpleValue.calendarToIso8601(c);
    String result = null;
    try {
      JSONObject jo = new JSONObject();
      jo.put("uuid", uuid);
      jo.put("lastModified", dateString);
      result = jo.toString();
    } catch (JSONException e) {
      throw new RepositoryException("Unexpected JSON problem", e);
    }
    return result;
  }

  private Value fetchAndVerifyValueForCheckpoint(PropertyMap pm, String pName)
      throws RepositoryException {
    Property property = pm.getProperty(pName);
    if (property == null) {
      throw new IllegalArgumentException("checkpoint must have a " + pName
          + " property");
    }
    Value value = property.getValue();
    if (value == null) {
      throw new IllegalArgumentException("checkpoint " + pName
          + " property must have a non-null value");
    }
    return value;
  }

  public ResultSet resumeTraversal(String checkPoint)
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
      ResultSet result = new SpiResultSetFromJcr(thisNode, nodes);
      return result;
    }

    ResultSet result = new SpiResultSetFromJcr(nodes);
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

  private String makeCheckpointQueryString(String uuid, Calendar c)
      throws RepositoryException {
    String queryPrefix = "//element(*, nt:resource)[@jcr:lastModified >= xs:dateTime(\"";
    String queryPostfix = "\")] order by jcr:lastModified, jcr:uuid";

    String time = SimpleValue.calendarToIso8601(c);

    String statement = queryPrefix + time + queryPostfix;

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
      c = SimpleValue.iso8601ToCalendar(dateString);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
          "could not parse date string from checkPoint string: " + dateString);
    }
    return c;
  }

  public ResultSet startTraversal() throws RepositoryException {
    String queryString = "//element(*, nt:resource) order by jcr:lastModified, jcr:uuid";
    String lang = Query.XPATH;
    javax.jcr.query.Query query = makeCheckpointQuery(queryString, lang);
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
    ResultSet result = new SpiResultSetFromJcr(nodes);
    return result;
  }

  public void setBatchHint(int batchHint) throws RepositoryException {
    ;
  }
}
