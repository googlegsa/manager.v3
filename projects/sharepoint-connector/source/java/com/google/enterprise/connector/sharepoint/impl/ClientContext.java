/**
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.spi.ValueType;

/**
 * 
 * ClientContext holds context information. Each "Site" is a context
 */
public class ClientContext implements Cloneable {
	private String server, feedType = ConnectorConstants.FEEDER_TYPE_URL, gsa,
			spHost, spCanonicalHost, spDomain;

	private String site, sitePath;// site is http://canonicalHost/sitePath

	private int spPort = 80;

	String cfgServer = "servers.server", cfgGSA = ".gsa", cfgUrl = ".url";

	static String cfgServers = ".servers", cfgExclude = ".exclude.url";

	static String cfgUsername = ".security[@username]",
			cfgPassword = ".security[@password]";

	static String cfgAuthType = ".security[@auth_type]";

	// gsa settings
	String gsaPath = "gsa";

	static int mode;

	public static final int defaultPageSize = 200;

	static int pageSize = defaultPageSize;

	// SP common
	String username, password;

	final String PROTOCOL = "http", COLON = ":";

	// running time members
	public static XMLConfiguration config;

	private static Log logger = null;

	static boolean crawlAll = false;

	static boolean crawlStructure = false;

	static Hashtable gMetaFields = new Hashtable();

	static Hashtable gSkipFields = new Hashtable();

	static String[] exclude = null;

	private String repositoryName;

	/**
	 * To be called when the application starts
	 * 
	 * @throws ConfigurationException
	 */
	public static void init() throws ConfigurationException {
		// setup config file
		String workingDir = System.getProperty("user.dir");
		config = new XMLConfiguration(workingDir + "/config/gsc.xml");
		config.setReloadingStrategy(new FileChangedReloadingStrategy());
		config.setAutoSave(true);

		// this will enable a lot of http messages
		// System.setProperty("java.net.debug","ALL");
		// setup for axis and httpclient
		System.setProperty("axis.ClientConfigFile", workingDir
				+ "/config/client-config.wsdd");
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.Log4JLogger");
		Protocol.registerProtocol("https", new Protocol("https",
				new EasySSLProtocolSocketFactory(), 443));

		// connector setup
		loadMetaFields();
		Util.init();
		// logger setup
		logger = LogFactory.getLog(ClientContext.class);
	}

	public ClientContext(String instanceName) throws UnknownHostException {
		int index = 0;
		String ser;
		setRepositoryName(instanceName);
		while (true) {
			ser = cfgServer + "(" + index + ")";
			server = config.getString(ser + "[@name]");
			if (server == null) {
				throw new UnknownHostException(instanceName
						+ " is not defined!");
			}
			if (server.equals(instanceName)) {
				break;
			}
			index++;
		}
		initInstance(index);
	}

	/*
	 * Assumes that there is only one instance, and username/password are
	 * provided by caller.
	 */

	public ClientContext(String username, String password)
			throws UnknownHostException {
		this(0);
		processUsername(username);
		this.password = password;
	}

	/**
	 * 
	 * @param index,
	 *            index of the wss server in the config file
	 */
	public ClientContext(int index) throws UnknownHostException {
		initInstance(index);
	}

	private void initInstance(int index) throws UnknownHostException {
		cfgServer = cfgServer + "(" + index + ")";
		feedType = ConnectorConstants.FEEDER_TYPE_URL;
		processUsername(config.getString(cfgServer + cfgUsername));
		password = config.getString(cfgServer + cfgPassword);

		// gsa url
		String gpath = this.getGsaUrl();
		if (gpath == null) {
			throw new UnknownHostException(
					"No GSA URL found, make sure the reference tag <gsa> in <server> node has the same value as the name attribute in gsa node");
		}
		if (!gpath.startsWith("http")) {
			throw new UnknownHostException(
					"You have to specify the GSA with format http://gsa_hostname");
		}
		// read server first
		server = config.getString(cfgServer + cfgUrl);
		processServerName(server);
		// load exclude
		exclude = config.getStringArray(cfgServers + cfgExclude);
	}

	/**
	 * Given user specified URL, find canonical host name, port; set the Server
	 * value and the initial Site value
	 * 
	 * @param cfgServer
	 * @throws UnknownHostException
	 */
	public void processServerName(String cfgServer) throws UnknownHostException {
		// find host and port
		cfgServer = cfgServer.trim();
		String host = cfgServer;
		if (host.indexOf("://") > 0) {
			host = host.substring(host.indexOf("://") + 3);
		}
		int lastSlash = host.indexOf(ConnectorConstants.SLASH);
		if (lastSlash > 0) {
			// it might not end with /, but in the middle
			// for example, a site can be linked in SPS:
			// http://server:port/default.aspx
			if (cfgServer.endsWith("aspx")) {
				cfgServer = cfgServer.substring(0, cfgServer
						.lastIndexOf(ConnectorConstants.SLASH));
			}
			host = host.substring(0, lastSlash);
		}
		int iPort = 80;
		if (host.indexOf(COLON) > 0) {
			String port = host.substring(host.indexOf(COLON) + 1);
			host = host.substring(0, host.indexOf(COLON));
			iPort = Integer.valueOf(port).intValue();
		}
		if (host.indexOf(ConnectorConstants.SLASH) > 0) {
			host = host.substring(0, host.indexOf(ConnectorConstants.SLASH));
		}
		String userSpecifiedHost = host;
		// only support IPv4

		char chost[] = host.toCharArray();
		int index = host.indexOf(".");
		// try to get canonical host name only if non-qualified name
		// is used
		if (index < 0) {
			setSPHost(host);
			InetAddress addr = InetAddress.getByName(host);
			host = addr.getCanonicalHostName();
			setSPCanonicalHost(host);
		} else {
			setSPHost(host.substring(0, index));
			setSPCanonicalHost(host);
		}
		setSPPort(iPort);
		// now set the site, it might be a sub site
		// if the user wants to crawl from that site, not from root
		site = cfgServer.replaceAll(userSpecifiedHost, getCanonicalSPHost());
		if (site.endsWith("/")) {
			site = site.substring(0, site.length() - 1);
		}
		server = site;
		// get rid of the
		int idx = 0;
		if (server.indexOf("://") > 0) {
			idx = "http://".length() + 2;
		}
		idx = server.indexOf("/", idx);
		if (idx > 0)
			server = server.substring(0, idx);
	}

	public void setContentServerUrl(String url) throws UnknownHostException {
		processServerName(url);
		config.setProperty(cfgServer + cfgUrl, url);
	}

	void processUsername(String userName) {
		String domainUser[] = null;
		if (userName == null) {
			return;
		}
		if (userName.indexOf("\\") > 0) {
			userName = userName.replace('\\', '/');
		}
		if (userName.indexOf("/") > 0) {
			domainUser = userName.split("/");
			spDomain = domainUser[0];
			username = domainUser[1];
		}
	}

	/**
	 * @return Returns the gsa.
	 */
	public String getGsa() {
		return gsa;
	}

	/**
	 * @param gsa
	 *            The gsa to set.
	 */
	public void setGsa(String gsa) {
		this.gsa = gsa;
	}

	/**
	 * @return Returns the feedType.
	 */
	public String getFeedType() {
		return feedType;
	}

	/**
	 * @param feedType
	 *            The feedType to set.
	 */
	public void setFeedType(String feedType) {
		this.feedType = feedType;
	}

	public static Hashtable getMetaFields() {
		return gMetaFields;
	}

	/**
	 * getServer() returns the same getSite() will change when the crawl goes
	 * down to sub sites
	 * 
	 * @return Returns the server.
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @param server
	 *            The server to set.
	 */
	private void setServer(String server) {
		this.server = server;
	}

	/**
	 * Site will change when the crawl goes down to sub sites, but getServer()
	 * won't
	 * 
	 * @return Returns the site.
	 */
	public String getSite() {
		return site;
	}

	/**
	 * @param site
	 *            The site to set.
	 */
	public void setSite(String site) {
		this.site = site;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	public String getAuthType() {
		return config.getString(cfgServer + cfgAuthType);
	}

	public void setAuthType(String auth) {
		config.setProperty(cfgServer + cfgAuthType, auth);
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	public void setPassword(String password) {
		config.setProperty(cfgServer + cfgUsername, password);
	}

	/**
	 * If server specific user exists, use it; otherwise, use common user name
	 * 
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return username;
	}

	/**
	 * @param userName
	 *            The userName to set.
	 */
	public void setUserName(String userName) {
		config.setProperty(cfgServer + cfgUsername, userName);
	}

	public String getGsaUrl() {
		return config.getString(gsaPath + ".url");
	}

	public String getGsaProxy() {
		return config.getString(gsaPath + ".url[@proxy_server]");
	}

	public int getGsaProxyPort() {
		return config.getInt(gsaPath + ".url[@proxy_port]");
	}

	public String getContentServerProxy() {
		return config.getString(cfgServer + ".url[@proxy_server]");
	}

	public void setContentServerProxy(String proxy) {
		String tag = cfgServer + ".url[@proxy_server]";
		if (config.getProperty(tag) == null) {
			config.addProperty(tag, proxy);
		} else {
			config.setProperty(tag, proxy);
		}
	}

	public int getContentServerProxyPort() {
		try {
			return config.getInt(cfgServer + ".url[@proxy_port]");
		} catch (NoSuchElementException e) {
			return 80;
		}
	}

	public void setContentServerProxyPort(Integer port) {
		String tag = cfgServer + ".url[@proxy_port]";
		if (port == null) {
			config.setProperty(tag, "");
		} else {
			if (config.getProperty(tag) == null)
				config.addProperty(tag, port);
		}
	}

	public static String getDtd() {
		return "./config/gsafeed.dtd";
	}

	public static int getCorePoolSize() {
		return config.getInt(cfgServers + ".thread[@pool_size]", 5);
	}

	public boolean saveFeed() {
		return config.getBoolean(cfgServers + "[@save_feed]");
	}

	public static String[] getServers() throws ConfigurationException {
		return config.getStringArray("servers.server[@name]");
	}

	public String getFeedFileName(String postFix) {
		String fileName = getSite();
		int idx = fileName.indexOf("//");
		if (idx > 0) {
			fileName = fileName.substring(idx + 2);
		}
		idx = fileName.indexOf("/");
		if (idx > 0) {
			fileName = fileName.substring(idx);
		} else
			fileName = ""; // no URL path
		if (getSPPort() == 80) {
			fileName = getSPHost() + fileName + postFix;
		} else {
			fileName = getSPHost() + "_" + getSPPort() + fileName + postFix;
		}
		return normalize(fileName);
	}

	/*
	 * The data source must start with alphabet, and the rest can be
	 * alphanumeric
	 */
	public String getDataSource() {
		if (getSPPort() == 80) {
			return normalize(getSPHost());
		} else {
			return normalize(getSPHost() + getSPPort());
		}
	}

	/**
	 * normalize string so that it can be used as GSA datasource name, or normal
	 * file name
	 * 
	 * @param input
	 * @return
	 */
	String normalize(String input) {
		input = input.replaceAll("http://", "");
		input = input.replaceAll("/", "_");
		input = "SP_" + input;
		char[] a = input.toCharArray();
		for (int i = 0; i < a.length; ++i) {
			if ((a[i] >= '0' && a[i] <= '9') || (a[i] >= 'A' && a[i] <= 'Z')
					|| (a[i] >= 'a' && a[i] <= 'z') || a[i] == '_') {
				continue;
			}
			a[i] = '_';
		}
		return String.valueOf(a);
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * @return Returns the gsaHost.
	 */
	public String getCanonicalSPHost() {
		return spCanonicalHost;
	}

	/**
	 * @return Returns the gsaHost.
	 */
	public String getSPHost() {
		return spHost;
	}

	/**
	 * @param gsaHost
	 *            The gsaHost to set.
	 */
	public void setSPHost(String spHost) {
		this.spHost = spHost;
	}

	public void setSPCanonicalHost(String host) {
		this.spCanonicalHost = host;
	}

	public String getSPCanonicalHost() {
		return this.spCanonicalHost;
	}

	/**
	 * @return Returns the gsaPort.
	 */
	public int getSPPort() {
		return spPort;
	}

	/**
	 * @param gsaPort
	 *            The gsaPort to set.
	 */
	public void setSPPort(int port) {
		this.spPort = port;
	}

	/**
	 * @return Returns the sitePath.
	 */
	public String getSitePath() {
		return sitePath;
	}

	/**
	 * @return Returns the crawlAll.
	 */
	public static boolean isCrawlAll() {
		return crawlAll;
	}

	/**
	 * @param crawlAll
	 *            The crawlAll to set.
	 */
	public static void setCrawlAll(boolean crawlAll) {
		ClientContext.crawlAll = crawlAll;
	}

	/**
	 * @return Returns the crawlStructure.
	 */
	public static boolean isCrawlStructure() {
		return crawlStructure;
	}

	/**
	 * @param crawlStructure
	 *            The crawlStructure to set.
	 */
	public static void setCrawlStructure(boolean crawlStructure) {
		ClientContext.crawlStructure = crawlStructure;
	}

	/**
	 * @return Returns the g_SkipFields.
	 */
	public static Hashtable getSkipFields() {
		return gSkipFields;
	}

	private static void loadMetaFields() {
		String[] metaTags = config.getStringArray(cfgServers
				+ ".metadata[@tags]");
		if (metaTags == null)
			return;
		for (int i = 0; i < metaTags.length; ++i) {
			metaTags[i] = metaTags[i].trim();
			if (metaTags[i].equals(""))
				continue;
			gMetaFields.put(metaTags[i], metaTags[i]);
		}
		String[] meta = config.getStringArray(cfgServers
				+ ".metadata[@exclude]");
		for (int i = 0; i < meta.length; ++i) {
			meta[i] = meta[i].trim();
			if (meta[i].equals(""))
				continue;
			gSkipFields.put(meta[i], meta[i]);
		}
	}

	public static void saveMetaFields(String meta) {
		config.setProperty(cfgServers + ".metadata[@tags]", meta);
	}

	public static void setSkiptFields(String fields) throws ConnectorException {
		if (fields == null) {
			gSkipFields.clear();
		} else {
			String skips[] = fields.split(",");
			String missingFields = "";
			for (int i = 0; i < skips.length; ++i) {
				if (!ClientContext.getMetaFields().keySet().contains(skips[i])) {
					missingFields += skips[i] + ",";
				}
			}
			if (!missingFields.equals("")) {
				throw new ConnectorException(missingFields
						+ " not found in meta tags of Sharepoint instance");

			}
			config.setProperty(cfgServers + ".metadata[@exclude]", fields);
		}

	}

	public static int getPageSize() {
		return pageSize;
	}

	public static void setPageSize(int size) {
		pageSize = size;
	}

	/**
	 * @return Returns the repositoryName.
	 */
	public String getRepositoryName() {
		return repositoryName;
	}

	/**
	 * @param repositoryName
	 *            The repositoryName to set.
	 */
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	/**
	 * @return Returns the mode.
	 */
	public static int getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            The mode to set.
	 */
	public static void setMode(int mode) {
		ClientContext.mode = mode;
	}

	/**
	 * @return Returns the exclude.
	 */
	public static String[] getExclude() {
		return exclude;
	}

	public static void setExclude(String[] urls) {
		exclude = urls;
		config.setProperty(cfgServers + cfgExclude, null);
		if (urls != null) {
			config.setProperty(cfgServers + cfgExclude, exclude);
		}
	}

	public static ValueType getFieldType(String name) {
		String type = (String) gMetaFields.get(name);
		if (type != null) {
			if (type.equals("Counter") || type.equals("Integer")) {
				return ValueType.LONG;
			}
			if (type.equals("DateTime")) {
				return ValueType.DATE;
			}
		}
		return ValueType.STRING;
	}

	public String getSpDomain() {
		return spDomain;
	}

	public void setSpDomain(String spDomain) {
		this.spDomain = spDomain;
	}
}
