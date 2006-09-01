package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

import connector.ClientContext;
import connector.sharepoint.BaseList;
import connector.sharepoint.ListFactory;
import connector.sharepoint.Sharepoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

public class SharepointQueryManager implements QueryTraversalManager {

  Iterator lists = null;
  int currentFactory = 0;
  Sharepoint sp = null;
  SharepointResultSet currentResult = null;
  private static Log logger = LogFactory.getLog(SharepointQueryManager.class);

  public SharepointQueryManager(Sharepoint sp) {
    this.sp = sp;
  }

  public ResultSet startTraversal() throws RepositoryException {
    // TODO Auto-generated method stub
    try {
      sp.crawl();
    } catch (Exception e) {
      logger.error(e);
      logger.error(e.getMessage());
      throw new RepositoryException("startTraversal failed");
    }
    return getResultSet();
  }

  /**
   * There are two loops: one is an array of ListFactory, collected from
   * multiple sites; For each ListFactory, there is another array of "lists"
   * 
   * @return ResultSet, representing a single list
   */
  private ResultSet getResultSet() {
    while (true) {
      // see if the current listfactory has anything left
      BaseList list = getList();
      if (list != null) {
        currentResult = new SharepointResultSet(list);
        return currentResult;
      }
      // now move to next ListFactory
      ArrayList factories = ListFactory.getListFactories();
      ListFactory fac = null;
      if (currentFactory >= factories.size()) {
        return null;
      }
      fac = (ListFactory) factories.get(currentFactory);
      currentFactory++;
      if (fac == null) {
        return null;
      }
      lists = fac.getLists().iterator();
    }
  }

  private BaseList getList() {
    if (lists != null) {
      if (!lists.hasNext()) {
        return null;
      }
      return (BaseList) lists.next();
    }
    return null;
  }

  public void setBatchHint(int hint) {
    if (hint < ClientContext.defaultPageSize) {
      ClientContext.setPageSize(hint);
    }
  }

  public String checkpoint(PropertyMap prop) throws RepositoryException {
    try {
      String accessTime = currentResult.checkpoint();
      currentResult = null;
      return accessTime;
    } catch (ParseException e) {
      logger.error(e.getMessage());
      throw new RepositoryException("checkpoint error");
    }
  }

  public ResultSet resumeTraversal(String checkpoint) {
    if (currentResult != null) {
      return currentResult;
    }
    return getResultSet();
  }
}
