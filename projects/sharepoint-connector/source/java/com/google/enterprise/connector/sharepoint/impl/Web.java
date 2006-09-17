/*
 * Copyright (C) 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.enterprise.connector.sharepoint.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.gen.SiteDataStub;

/**
 * 
 * This class uses "webs" web services provided by Sharepoint to find all webs
 * in a virtual server
 */
public class Web extends ConnectorImpl {
	String _soapAddress = "/_vti_bin/SiteData.asmx";

	private static Log logger = LogFactory.getLog(Web.class);

	public Web(ClientContext context) throws ConnectorException {
		super.setContext(context);
		try {
			super.setSoap(new SiteDataStub(), context.getSite() + _soapAddress);
		} catch (Exception e) {
			Util.rethrow("Web constructor failed", logger, e);
		}
	}

	/**
	 * crawl a single site
	 * 
	 * @param context
	 */
	public void crawl() {
		ClientContext context = getContext();
		//Util.recordSite(context);
		try {
			ListFactory factory = new ListFactory(context);
			factory.crawl();
		} catch (ConnectorException e) {
			logger.error("error while crawling a single site: "
					+ context.getSite());
			logger.error(e.getMessage());
		}
	}

}
