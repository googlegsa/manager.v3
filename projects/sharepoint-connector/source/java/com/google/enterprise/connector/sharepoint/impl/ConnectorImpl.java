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

import java.util.TimeZone;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties.BasicAuthentication;
import org.apache.axis2.transport.http.HttpTransportProperties.NTLMAuthentication;
import org.apache.axis2.transport.http.HttpTransportProperties.ProxyProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 */
public abstract class ConnectorImpl {

	private static String _xmlDocType = "<!DOCTYPE gsafeed PUBLIC \"-//Google//DTD GSA Feeds//EN\" \"";

	private Stub soap;

	private ClientContext context;

	private static Log logger = LogFactory.getLog(ConnectorImpl.class);

	protected OMFactory omfactory = OMAbstractFactory.getOMFactory();

	/**
	 * List the available TimeZone ids
	 * 
	 */
	public void getAvailableTimeZoneIDs() {
		TimeZone.getAvailableIDs();
	}

	/**
	 * @return Returns the context.
	 */
	public ClientContext getContext() {
		return context;
	}

	/**
	 * @param context
	 *            The context to set.
	 */
	public void setContext(ClientContext context) {
		this.context = context;
	}

	/**
	 * @return Returns the soap.
	 */
	public Stub getSoap() {
		return soap;
	}

	/**
	 * @param soap
	 *            The soap to set.
	 */
	public void setSoap(Stub nsoap, String endPoint) {
		this.soap = nsoap;
		Options options = new Options();
		EndpointReference target = new EndpointReference(endPoint);
		options.setTo(target);
		if (context.getUserName() != null) {
			if (ConnectorConstants.AUTH_TYPE_BASIC
					.equals(context.getAuthType())) {
				BasicAuthentication auth = new BasicAuthentication();
				auth.setRealm(context.getSpDomain());
				auth.setUsername(context.getUserName());
				auth.setPassword(context.getPassword());
				auth.setPort(context.getSPPort());
				auth.setHost(context.getSPCanonicalHost());
				options.setProperty(HTTPConstants.BASIC_AUTHENTICATION, auth);
			} else {
				NTLMAuthentication auth = new NTLMAuthentication();
				auth.setRealm(context.getSpDomain());
				auth.setUsername(context.getUserName());
				auth.setPassword(context.getPassword());
				auth.setPort(context.getSPPort());
				auth.setHost(context.getSPCanonicalHost());
				options.setProperty(HTTPConstants.NTLM_AUTHENTICATION, auth);
			}
		}
		String proxy = context.getContentServerProxy();
		if (proxy != null && !"".equals(proxy)) {
			ProxyProperties proxyProperties = new ProxyProperties();
			proxyProperties.setProxyName(proxy);
			proxyProperties.setProxyPort(context.getContentServerProxyPort());
			options.setProperty(HTTPConstants.PROXY, proxyProperties);
		}
		soap._getServiceClient().setOptions(options);
	}
}
