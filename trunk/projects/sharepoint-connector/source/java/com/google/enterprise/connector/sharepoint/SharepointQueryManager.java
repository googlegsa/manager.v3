package com.google.enterprise.connector.sharepoint;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.BaseList;
import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.ListFactory;
import com.google.enterprise.connector.sharepoint.impl.Sharepoint;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

public class SharepointQueryManager implements QueryTraversalManager {

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
		return new SharepointResultSet();
	}

	public void setBatchHint(int hint) {
		ClientContext.setPageSize(hint);
	}

	public String checkpoint(PropertyMap prop) throws RepositoryException {
/*		try {
			String accessTime = currentResult.checkpoint();
			currentResult = null;
			return accessTime;
		} catch (ParseException e) {
			logger.error(e.getMessage());
			throw new RepositoryException("checkpoint error");
		}
*/		return null;
	}

	public ResultSet resumeTraversal(String checkpoint) {
		if (currentResult != null) {
			return currentResult;
		}
		return null;
//		return getResultSet();
	}
}
