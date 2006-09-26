package com.google.enterprise.connector.sharepoint;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.ListFactory;
import com.google.enterprise.connector.sharepoint.impl.Sharepoint;
import com.google.enterprise.connector.sharepoint.impl.Util;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SpiConstants;

public class SharepointQueryManager implements QueryTraversalManager {

	Sharepoint sp = null;

	SharepointResultSet rs = null;
	
	boolean bStartTraversalCalled = false;

	private static Log logger = LogFactory.getLog(SharepointQueryManager.class);

	public SharepointQueryManager(Sharepoint sp) {
		this.sp = sp;
	}

	/**
	 * Will be called only once by Connector Manager
	 */
	public ResultSet startTraversal() throws RepositoryException {
		// TODO Auto-generated method stub
		try {
			sp.crawl();
			bStartTraversalCalled = true;
		} catch (Exception e) {
			logger.error(e);
			logger.error(e.getMessage());
			throw new RepositoryException("startTraversal failed");
		}
		rs = new SharepointResultSet();
		return rs;
	}

	public void setBatchHint(int hint) {
		ClientContext.setPageSize(hint);
	}

	/*
	 * To Sharepoint connector, Checkpoint only means a random point in a
	 * half-process list; state is flushed to external store using java Prefs
	 * for every single list. So the worst case scenario is some items from a
	 * single list will be resubmitted if no checkpoint happened before crash.
	 * checkpoint is not only called by connector manager, but also called
	 * internally after each batch has been processed . This way, the maximum
	 * "lost memory" will be the batch size. That means minimum resubmission.
	 * (non-Javadoc)
	 * 
	 * return: checkpoint() will return null and it will use internally
	 * persisted checkpoint state
	 * @see com.google.enterprise.connector.spi.QueryTraversalManager#checkpoint(com.google.enterprise.connector.spi.PropertyMap)
	 */
	public String checkpoint(PropertyMap pm) throws RepositoryException {
		Util.checkpoint(rs.getContext(), pm);
		return null;
	}

	/*
	 * 
	 * This method can be called regularly by a schedule, or when connector is
	 * restarted, or even after s crash.
	 * 
	 * Regarding checkpoint:
	 * 
	 * In the case where Java process has been running without crashing, the
	 * connector picks up from where it left: the checkpoint. It won't try to
	 * discover everything again just to return one item. In this case,
	 * checkpoint state is persisted, but is not used by resume() because
	 * everything is still in memory.
	 * 
	 * In the second case where Java process has stopped, then login() will be
	 * called first to send username/password. Then resume() will be called.
	 * From here, the connector will rediscover everything. The behavior is very
	 * much like startTraversal(), the only difference is that when processing
	 * the "list"/"document library", it will ignore everything up to the
	 * checkpoint. In this case, the checkpoint state persisted will be
	 * retrieved and used when resume() is called.
	 * 
	 * How does resumeTraversal know there is a crash? It checks whether there
	 * are any "in-memory" state that indicates startTraversal() has been called
	 * in this running instance of Java process. If nothing is available (for
	 * example, no lists in memory or some other flag), I will assume it crashed
	 * and "recovery" - meaning rediscover the lists, and process checkpoint in
	 * particular. If the state (exising lists in memory) indicates no crash,
	 * I'll start with checkpoint only.
	 * 
	 * Regarding discovering new lists in Sharepoint:
	 * 
	 * First, what does discovery mean?
	 * 
	 * 1. changes in a previously process list.
	 * 
	 * 2. everthing in * a new list<br> When does the connector decide to
	 * perform a discovery? (non-Javadoc)
	 * 
	 * Discovery happens at several points: 
	 * 
	 * 1. After a crash
	 * 2. When all the sites/lists have been retrieved and the resumeTraversal() is called again.
	 * 
	 * @see com.google.enterprise.connector.spi.QueryTraversalManager#resumeTraversal(java.lang.String)
	 */
	public ResultSet resumeTraversal(String checkpoint)
			throws RepositoryException {
		try {
			if (bStartTraversalCalled)
			{
				//new ListFactory(context).crawlCheckpointList(checkpoint);
				return rs;
			}else
			{
				Util.restoreCheckpoint(sp.getContext(),
						checkpoint);
				return this.startTraversal();
			}
		} catch (Exception e) {
			Util.processException(logger, e);
			throw new RepositoryException("failed to resume");
		}
	}
}
