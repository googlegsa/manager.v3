package com.google.enterprise.connector.sharepoint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.Util;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;

public class TestTraversal extends TestCase {
	static HashMap map1 = new HashMap(), map2 = new HashMap();

	/**
	 * @param args
	 */
	public void qtestAll() throws Exception {
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

	public void testTraversal() throws Exception {
		ClientContext.setCrawlAll(true);
		Connector repo = new SharepointConnector();
		Session sess = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
		QueryTraversalManager mgr = sess.getQueryTraversalManager();
		int docCount = QueryTraversalUtil.runTraversal(mgr, 10, map1);
		System.out.println("total docs:" + docCount);
		dumpMap(map1);
		ClientContext.setCrawlAll(false);
	}

	public void testBatchSizeOne() throws Exception {
		map2.clear();
		ClientContext.setCrawlAll(true);
		Connector repo = new SharepointConnector();
		Session sess = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
		QueryTraversalManager mgr = sess.getQueryTraversalManager();
		int docCount = QueryTraversalUtil.runTraversal(mgr, 1, map2);
		System.out.println("total docs:" + docCount);
		ClientContext.setCrawlAll(false);
		this.assertEquals(map1.size(), map2.size());
		assertCount(map1, map2);
	}

	/*
	 * Start with resume to simulate a crash
	 */
	public void testForcedCheckpointBeforeCrash() throws Exception {
		map2.clear();
		Util.clearCrawlState();
		System.out.println(map1.size());
		ClientContext.setCrawlAll(true);
		Connector repo = new SharepointConnector();
		Session sess = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
		QueryTraversalManager mgr = sess.getQueryTraversalManager();
		mgr.setBatchHint(100);
		ResultSet rs = mgr.startTraversal();
		PropertyMap pm = QueryTraversalUtil.processResultSet(rs, 10, map2);
		mgr.checkpoint(pm);
		System.out.println("before resume");
		dumpMap(map2);
		// now force another session

		ClientContext.setCrawlAll(false);// in production this doesn't need
											// to be called
		Session sess2 = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
		QueryTraversalManager mgr2 = sess.getQueryTraversalManager();
		mgr2.setBatchHint(100);
		rs = mgr2.resumeTraversal(null);
		QueryTraversalUtil.processResultSet(rs, -1, map2);
		dumpMap(map2);
		assertEquals(map1.size(), map2.size());
		assertCount(map1, map2);
	}

	/*
	 * Start with resume to simulate a crash
	 */
	public void testAutomaticCheckpointBeforeCrash() throws Exception {
		map2.clear();
		ClientContext.setCrawlAll(true);
		Util.clearCrawlState();
		Connector repo = new SharepointConnector();
		Session sess = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
		QueryTraversalManager mgr = sess.getQueryTraversalManager();
		mgr.setBatchHint(100);
		ResultSet rs = mgr.startTraversal();
		PropertyMap pm = QueryTraversalUtil.processResultSet(rs, 100, map2);
		// now force another session
		Session sess2 = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
		QueryTraversalManager mgr2 = sess.getQueryTraversalManager();
		mgr2.setBatchHint(100);
		rs = mgr2.resumeTraversal(null);
		QueryTraversalUtil.processResultSet(rs, -1, map2);
		assertEquals(map1.size(), map2.size());
		assertCount(map1, map2);
	}

	/**
	 * After everything is retrieved, next time resume() is called, it should
	 * try to discover everything again
	 * 
	 * @throws Exception
	 */
	public void testRepeat() throws Exception {
		map2.clear();
		Connector repo = new SharepointConnector();
		Session sess = repo.login("ent-sales-d2\\sales-admin", "t3stth@t");
		QueryTraversalManager mgr = sess.getQueryTraversalManager();
		//finish the iteration
		QueryTraversalUtil.runTraversal(mgr, -1, map2);
		assertEquals(map1.size(), map2.size());
		assertCount(map1, map2);
		ResultSet rs = mgr.resumeTraversal(null);
		while (rs.iterator().hasNext()) {
			rs.iterator().next();
		}
		assertEquals(map1.size(), map2.size());
		assertCount(map1, map2);
	}

	/**
	 * 
	 * @param mapa
	 * @param mapb
	 */
	public void assertCount(Map mapa, Map mapb) {
		Iterator it = mapa.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Integer a = ((Integer) mapa.get(key));
			Integer b = ((Integer) mapb.get(key));
			this.assertEquals(a.intValue(), b.intValue());
		}
	}

	public void dumpMap(Map mapa) {
		Iterator it = mapa.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Integer a = ((Integer) mapa.get(key));
			System.out.println(key + "  :  " + a);
		}

	}
}
