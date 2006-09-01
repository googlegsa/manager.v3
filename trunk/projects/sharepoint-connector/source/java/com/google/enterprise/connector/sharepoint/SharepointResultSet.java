package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

import connector.sharepoint.BaseList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.util.Iterator;

public class SharepointResultSet implements ResultSet {

  BaseList list = null;
  private static Log logger = LogFactory.getLog(SharepointResultSet.class);

  public SharepointResultSet(BaseList list) {
    this.list = list;
  }

  public Iterator iterator() throws RepositoryException {
    return new ResultIterator(list);
  }

  public String checkpoint() throws ParseException {
    return list.updateLastAccessTime();
  }
}
