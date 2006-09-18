package com.google.enterprise.connector.sharepoint;

import java.util.Iterator;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.impl.BaseList;
import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.ListFactory;
import com.google.enterprise.connector.sharepoint.impl.Sharepoint;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

public class TestList extends TestCase {
	String username = "ent-sales-d2\\sales-admin", password = "t3stth@t";

	boolean hasSurvey = false, hasDoc = false, hasList = false,
			hasDiscussion = false, hasIssue = false;

	protected void setUp() throws Exception {
		super.setUp();
		ClientContext.init();
	}

	public void testValidUrl() throws Exception {
		loopItems(BaseList.TYPE_GENERIC_LIST, new UrlCase());
	}

	public void atestItemCount() throws Exception {
		ClientContext context = new ClientContext(username, password);
		ClientContext.setCrawlAll(true);
		Sharepoint sp = new Sharepoint(context);
		sp.crawl();
		Iterator it = ListFactory.getListFactories().iterator();
		while (it.hasNext()) {
			ListFactory f = (ListFactory) it.next();
			Iterator itList = f.getLists().iterator();
			System.out.println("test list factory for: "
					+ f.getContext().getSite());
			while (itList.hasNext()) {
				BaseList list = (BaseList) itList.next();
				int itemCnt = 0;
				while (list.query()) {
					itemCnt += list.getCurrentRecords().size();
				}
				System.out.println("Item count=" + itemCnt);
				assertEquals(itemCnt, list.getTotalItems());
			}
		}
	}

	public void testSortOrder() throws Exception {
		loopItems(BaseList.TYPE_GENERIC_LIST, new SortOrderCase());
	}

	public void testContent() throws Exception {
		ClientContext context = new ClientContext(username, password);
		Sharepoint sp = new Sharepoint(context);
		sp.crawl();
		Iterator it = ListFactory.getListFactories().iterator();
		while (it.hasNext()) {
			ListFactory f = (ListFactory) it.next();
			Iterator itList = f.getLists().iterator();
			while (itList.hasNext()) {
				BaseList list = (BaseList) itList.next();
				String listType = list.getClass().getName();
				if (listType.indexOf("Documents") > 0) {
					hasDoc = true;
				} else if (listType.indexOf("Issue") > 0) {
					hasIssue = true;
				} else if (listType.indexOf("Discussions") > 0) {
					hasDiscussion = true;
				} else if (listType.indexOf("Survey") > 0) {
					hasSurvey = true;
				} else if (listType.indexOf("List") > 0) {
					hasList = true;
				}
			}
		}
		assertTrue(hasList);
		assertTrue(hasDoc);
		assertTrue(hasDiscussion);
		assertTrue(hasSurvey);
		assertTrue(hasIssue);
	}

	protected void loopItems(String type, ICase icase) throws Exception {
		Connector repo = new SharepointConnector();
		Session sess = repo.login(username, password);
		QueryTraversalManager mgr = sess.getQueryTraversalManager();
		ResultSet rs = mgr.startTraversal();
		Iterator it = rs.iterator();
		PropertyMap pm = null;
		while (it.hasNext()) {
			pm = (PropertyMap) it.next();

			icase.perform(((SharepointResultSet) rs).getContext(), pm);
		}
	}
}
