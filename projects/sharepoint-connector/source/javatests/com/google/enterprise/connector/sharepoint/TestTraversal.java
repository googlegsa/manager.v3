package com.google.enterprise.connector.sharepoint;

import java.util.Iterator;

import junit.framework.TestCase;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;

public class TestTraversal extends TestCase{

  /**
   * @param args
   */
  public void testAll() throws Exception {
    // TODO Auto-generated method stub
	  Connector repo = new SharepointConnector();
    Session sess = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
    QueryTraversalManager mgr = sess.getQueryTraversalManager();
    ResultSet rs = mgr.startTraversal();
    while (rs != null) {
      Iterator it = rs.iterator();
      PropertyMap pm = null;
      while (it.hasNext()) {
        pm = (PropertyMap) it.next();
      }
      String checkPointString = mgr.checkpoint(pm);
      rs = mgr.resumeTraversal(checkPointString);
    }
  }
  
  public void testBatch() throws Exception
  {
	    Connector repo = new SharepointConnector();
	    Session sess = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
	    QueryTraversalManager mgr = sess.getQueryTraversalManager();
	    mgr.setBatchHint(1);
	    ResultSet rs = mgr.startTraversal();
	    while (rs != null) {
	      Iterator it = rs.iterator();
	      PropertyMap pm = null;
	      while (it.hasNext()) {
	        pm = (PropertyMap) it.next();
	      }
	      String checkPointString = mgr.checkpoint(pm);
	      rs = mgr.resumeTraversal(checkPointString);
	    }	  
  }
} 

