package com.google.enterprise.connector.sharepoint.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;

public class Util {

	public static XMLConfiguration config;

	static String cfgLists = "lists",
			lastAccessTimeTag = "[@last_access_time]";

	static OMDocument docReport;

	static OMFactory omfactory;

	static String SERVER = "Server", URL = "Url", SLASH = "/", LIST = "List",
			NAME = "Name", SITE = "Site", SERVERS = "Servers";

	private static Log logger = LogFactory.getLog(Util.class);

	private static String timeFormat1 = "yyyyMMdd HH:mm:ss",
			timeFormat2 = "yyyy-MM-dd HH:mm:ss", // in GetListItems
			// format3 is in GetListItemChanges,
			// has to replacy "T" with "_" because SimpleDateFormatter doesn't
			// take
			// "T"
			timeFormat3 = "yyyy-MM-dd HH:mm:ss z",
			gsaFormat = "EEE d MMM yyyy HH:mm:ss z", // GSA format
			utcFormat2 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static int webCount = 0, itemCount = 0, crawlListCount = 0,
			existListCount = 0;

	public static Integer c = new Integer(3);

	static Preferences pref = Preferences
			.systemNodeForPackage(Sharepoint.class);
	static {
		omfactory = OMAbstractFactory.getOMFactory();
		// pool = new ConnectorPool(ClientContext.getCorePoolSize());
		docReport = omfactory.createOMDocument();
		OMElement omservers = omfactory.createOMElement(SERVERS, null);
		docReport.addChild(omservers);
	}

	/**
	 * Transforms one date format to another, the default target format is
	 * yyyyMMdd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static String toGSAFormat(String date) throws ParseException {
		Date dt = stringToTime(date);
		return timeToString(dt, gsaFormat);
	}

	public static String toUniversalFormat(String date) throws ParseException {
		Date dt = stringToTime(date);
		return timeToString(dt, Util.utcFormat2);
	}

	public static Date stringToTime(String date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(getTimeFormat(date));
		return formatter.parse(date);
	}

	public static String timeToString(Date date, String format)
			throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	public static String timeToUTCFormat(Date date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(
				ConnectorConstants.utcFormat);
		return formatter.format(date);
	}

	/**
	 * split the whole URL and encode char segments
	 * 
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeURL(String url) {
		int idx = -1;
		String prefix = "";
		if (url.startsWith("http")) {
			idx = url.indexOf("//");
			idx = url.indexOf(SLASH, idx + 2);
			if (idx == -1)
				return url;
			prefix = url.substring(0, idx);
			url = url.substring(idx);
		}
		try {
			StringTokenizer tok = new StringTokenizer(url, SLASH);
			while (tok.hasMoreElements()) {
				String path = (String) tok.nextElement();
				if (path.indexOf("?") >= 0) { // we are at the parameter part
					prefix = prefix + SLASH + path;
					break;
				}
				prefix = prefix + SLASH + URLEncoder.encode(path, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			return "";
		}
		prefix = prefix.replaceAll("[+]", "%20");
		return prefix;
	}

	/**
	 * @return Returns the timeFormat.
	 */
	public static String getTimeFormat(String date) {
		if (date.endsWith("Z")) {
			if (date.indexOf('T') < 0) {
				return ConnectorConstants.utcFormat;
			} else {
				return utcFormat2;
			}
		}
		if (date.indexOf(' ') < 0) {
			return timeFormat3;
		} else if (date.indexOf('-') > 0) {

			return timeFormat2;
		} else {
			return timeFormat1;
		}
	}

	public static void init() throws ConfigurationException {
		String workingDir = System.getProperty("user.dir");
		config = new XMLConfiguration(workingDir + "/config/checkpoint.xml");
		config.setReloadingStrategy(new FileChangedReloadingStrategy());
		config.setAutoSave(true);
	}

	public static void saveLastAccessTime(String list, Date dt) {

		SimpleDateFormat formatter = new SimpleDateFormat(Util.utcFormat2);
		String sDate = formatter.format(dt);
		String tag = list;
		Preferences state = pref.node("crawlstate");
		state.put(tag, sDate);
	}

	public static String getLastAccessTime(String list) {
		Preferences state = pref.node("crawlstate");
		return state.get(list, null);
	}

	public static String xmlToString(OMDocument doc) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			doc.serialize(buffer);
			buffer.close();
			return buffer.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}

	}

	public static String xmlToString(OMElement dl) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			dl.serialize(buffer);
			buffer.close();
			return buffer.toString();
		} catch (XMLStreamException e) {
			logger.error(e.getMessage());
			return null;
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public static void processException(Log logger, Exception e) {
		logger.debug("processException", e);
		if (e instanceof AxisFault) {
			AxisFault fault = (AxisFault) e;
			logger.error("faultNode: " + fault.getFaultNode());
			if (fault != null && fault.getDetail() != null) {
				logger.error("details: " + Util.xmlToString(fault.getDetail()));
			}
			logger.error("faultcode: " + fault.getFaultCode());
		}
		Throwable th = e;
		while (th != null) {
			logger.error(th.getClass().getName());
			logger.error(th.getMessage());
			th = th.getCause();
		}
	}

	public static void rethrow(String message, Log logger, Exception e)
			throws ConnectorException {
		processException(logger, e);
		throw new ConnectorException(message);
	}

	public static boolean matchSite(String site) {
		String exclude[] = ClientContext.getExclude();
		for (int i = 0; i < exclude.length; ++i) {
			if (exclude[i] == null || "".equals(exclude[i].trim())) {
				continue;
			}
			if (match(site, exclude[i])) {
				return true;
			}
		}
		return false;
	}

	protected static boolean match(String input, String pat) {
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(input);
		return m.find();
	}

	public static String checkpoint(ClientContext context, PropertyMap pm)
			throws RepositoryException {
		Preferences cp = pref.node("checkpoint");
		Property prop = pm.getProperty(SpiConstants.PROPNAME_DOCID);
		String name = prop.getValue().getString();
		int itemIndex = name.indexOf("}");
		String listKey = name.substring(0, itemIndex + 1);
		if (itemIndex + 2 < name.length()) // the list itself might be the
		// property
		{
			String item = name.substring(itemIndex + 2);
			cp.putLong("item", Integer.valueOf(item).longValue());
		}
		cp.put("list", listKey);
		prop = pm.getProperty(SpiConstants.PROPNAME_LASTMODIFY);
		String lastAccessTime = prop.getValue().getString();
		try {
			// now we reduce the # of items to retrieve when it's resumed
			saveLastAccessTime(listKey, Util.stringToTime(lastAccessTime));
		} catch (ParseException e) {
			Util.processException(logger, e);
			throw new RepositoryException("checkpoint error");
		}
		return name;
	}

	public static void restoreCheckpoint(ClientContext mainContext,
			String checkpoint) throws UnknownHostException {
		Preferences cp = pref.node("checkpoint");
		String listKey = cp.get("list", null);
		long item = cp.getLong("item", -1);
		ClientContext.setCheckpointItem(item);
		ClientContext.setCheckpointList(listKey);
	}

	public static void clearCrawlState() {
		try {
			Preferences state = pref.node("crawlstate");
			state.removeNode();
			state = pref.node("checkpoint");
			state.removeNode();			
		} catch (BackingStoreException e) {
			Util.processException(logger, e);
		}
	}
}
