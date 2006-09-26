package com.google.enterprise.connector.sharepoint;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.ConnectorConstants;
import com.google.enterprise.connector.sharepoint.impl.Util;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

public class SharepointConnector implements Connector {

	private static Log logger = LogFactory.getLog(SharepointConnector.class);
	static {
		try {
			ClientContext.init();
			ClientContext.setMode(ConnectorConstants.MODE_QUERY);
		} catch (ConfigurationException e) {

		}
	}

	public Session login(String username, String password)
			throws LoginException, RepositoryException {
		try {
			return new SharepointSession(username, password);
		} catch (Exception e) {
			Util.processException(logger, e);
			logger.error(e.getMessage());
			throw new RepositoryException("connection to repo failed");
		}
	}
}
