package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.Repository;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;

import java.util.Iterator;

public class TraveralTest {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    // TODO Auto-generated method stub
    Repository repo = new SharepointRepo();
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
}
