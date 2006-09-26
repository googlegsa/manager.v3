package com.google.enterprise.connector.sharepoint.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.gen.SiteDataStub;
import com.google.enterprise.connector.sharepoint.gen.SiteDataStub.ArrayOf_sWebWithTime;
import com.google.enterprise.connector.sharepoint.gen.SiteDataStub.GetSite;
import com.google.enterprise.connector.sharepoint.gen.SiteDataStub.GetSiteResponse;
import com.google.enterprise.connector.sharepoint.gen.SiteDataStub._sWebWithTime;

public class WebFactory extends ConnectorImpl {

	Map sites = new HashMap();

	String _soapAddress = "/_vti_bin/SiteData.asmx";

	private static Log logger = LogFactory.getLog(WebFactory.class);

	public WebFactory(ClientContext context) throws ConnectorException {
		super.setContext(context);
		ListFactory.clearListFactories();
		try {
			super.setSoap(new SiteDataStub(), context.getSite() + _soapAddress);
		} catch (Exception e) {
			Util.rethrow("Web constructor failed", logger, e);
		}
	}

	/**
	 * discover all the subsites, given the top site
	 * 
	 * @param context
	 */
	public void crawl() {
		ClientContext context = getContext();
		if (context.getSite().endsWith("/sites")) {
			return;
		}
		try {
			logger.info("Start crawling top site collection: "
					+ context.getSite());
			discoverSites();
			Map sites = getSites();
			Iterator it = sites.keySet().iterator();
			logger.info("This site collection contains " + sites.size()
					+ " sub sites");
			while (it.hasNext()) {
				String site = (String) it.next();
				if (site.endsWith("SiteDirectory")) {
					continue;
				}
				if (site.endsWith("/sites")) {
					continue;
				}
				// web.getSiteInfo(site);
				ClientContext context2 = (ClientContext) context.clone();
				context2.setSite(site);
				Web web = new Web(context2);
				web.crawl();
			}
		} catch (Exception e) {
			logger.error("When crawling URL: " + context.getSite());
			logger
					.error("Possible reason: This might not be a sharepoint site, "
							+ "or invalid credential, or the web site is down");
			Util.processException(logger, e);
		}
	}

	Map getSites() {
		return sites;
	}

	public static void Login(ClientContext context) throws ConnectorException {
		try {
			WebFactory web = new WebFactory(context);
			SiteDataStub stub = ((SiteDataStub) web.getSoap());
			GetSiteResponse response = stub.GetSite(new GetSite());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ConnectorException("Connection to site "
					+ context.getSite() + " failed");
		}
	}

	/**
	 * Stores A map of sites, the key is the URL of the site, and the value is
	 * the title of the site. URL's are unique, but titles are not.
	 * 
	 * @throws RemoteException
	 */
	public void discoverSites() {
		try {
			ClientContext context = getContext();
			if (Util.matchSite(context.getSite())) {
				logger.info("found an excluded site, ignored: "
						+ context.getSite());
				return;
			}
			SiteDataStub stub = ((SiteDataStub) getSoap());
			GetSiteResponse response = stub.GetSite(new GetSite());
			ArrayOf_sWebWithTime webs = response.getVWebs();
			if (webs == null)
				return;
			_sWebWithTime[] els = webs.get_sWebWithTime();
			for (int i = 0; i < els.length; ++i) {
				String url = els[i].getUrl();
				url = Util.encodeURL(url);
				if (Util.matchSite(url)) {
					logger.info("found an excluded site, ignored: "
							+ context.getSite());
					continue;
				}
				if (url.startsWith(context.getSite())) {
					sites.put(url, "1");
				}
			}
		} catch (Exception e) {
			Util.processException(logger, e);
		}
	}
}
