package com.google.enterprise.connector.sharepoint;

import java.text.ParseException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.BaseList;
import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

public class SharepointResultSet implements ResultSet {

  BaseList list = null;
  private static Log logger = LogFactory.getLog(SharepointResultSet.class);

  public SharepointResultSet(BaseList list) {
    this.list = list;
  }

  public Iterator iterator() throws RepositoryException {
    return list;
  }

  public String checkpoint() throws ParseException {
    return list.updateLastAccessTime();
  }
  
  public ClientContext getContext()
  {
	  return list.getContext();
  }
}
