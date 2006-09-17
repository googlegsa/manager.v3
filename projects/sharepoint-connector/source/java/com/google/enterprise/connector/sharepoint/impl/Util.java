package com.google.enterprise.connector.sharepoint.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.threadpool.ThreadPool;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Util {

  public static XMLConfiguration config;
  static String cfgLists = "lists", lastAccessTimeTag = "[@last_access_time]";
  static OMDocument docReport;
  static OMFactory omfactory;
  static String SERVER = "Server", URL = "Url", SLASH = "/", LIST = "List",
      NAME = "Name", SITE = "Site", SERVERS = "Servers";
  private static ThreadPool pool;
  private static Log logger = LogFactory.getLog(Util.class);
  private static String timeFormat1 = "yyyyMMdd HH:mm:ss",
      timeFormat2 = "yyyy-MM-dd HH:mm:ss", // in GetListItems
      // format3 is in GetListItemChanges,
      // has to replacy "T" with "_" because SimpleDateFormatter doesn't take
      // "T"
      timeFormat3 = "yyyy-MM-dd HH:mm:ss z",
      gsaFormat = "EEE d MMM yyyy HH:mm:ss z", // GSA format
      utcFormat2 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  public static int webCount = 0, itemCount = 0, crawlListCount = 0,
      existListCount = 0;
  public static Integer c = new Integer(3);

  static {
    omfactory = OMAbstractFactory.getOMFactory();
    // pool = new ConnectorPool(ClientContext.getCorePoolSize());
    docReport = omfactory.createOMDocument();
    OMElement omservers = omfactory.createOMElement(SERVERS, null);
    docReport.addChild(omservers);
  }

  static public void recordItem(ClientContext context, int items) {
    synchronized (ClientContext.class) {
      itemCount = itemCount + items;
    }
  }

  private static void recordList(ClientContext context, int total)
      throws ConnectorException {
    synchronized (c) {
      existListCount += total;
      OMElement s = locateSite(context);
      s.addAttribute("total_list", "" + total, null);
    }
  }

  private static void recordCrawledList(int cnt) {
    synchronized (c) {
      crawlListCount += cnt;
    }
  }

  private static void recordSite(ClientContext context) {
    Iterator it = docReport.getOMDocumentElement().getChildrenWithName(
      new QName(SERVER));
    OMElement omserver = null;
    while (it.hasNext()) {
      OMElement om = (OMElement) it.next();
      String url = om.getAttributeValue(new QName(URL));
      if (context.getServer().equals(url)) {
        omserver = om;
        break;
      }
    }
    if (omserver == null) {
      omserver = omfactory.createOMElement(SERVER, context.getServer(), null);
      omserver.addAttribute(URL, context.getServer(), null);
      synchronized (docReport) {
        docReport.getOMDocumentElement().addChild(omserver);
      }
    }
    String path = context.getSite().replaceAll(context.getServer(), "");
    String[] paths = path.split("[/]");
    OMElement omsite = omserver;
    for (int i = 0; i < paths.length; ++i) {
      if (paths[i].equals("")) {
        paths[i] = "/";
      }
      it = omsite.getChildren();
      boolean found = false;
      while (it.hasNext()) {
        OMElement om = (OMElement) it.next();
        String name = om.getAttributeValue(new QName(NAME));
        if (name.equals(paths[i])) {
          omsite = om;
          found = true;
          break;
        }
      }
      if (!found) {
        OMElement el = omfactory.createOMElement(SITE, null);
        el.addAttribute(NAME, paths[i], null);
        omsite.addChild(el);
        omsite = el;
      }
      synchronized (context) {
        webCount++;
      }
    }
  }

  public static OMElement locateSite(ClientContext context)
      throws ConnectorException {
    // OMElement omSite = omfactory.createOMElement(site, null);
    Iterator it = docReport.getOMDocumentElement().getChildrenWithName(
      new QName(SERVER));
    OMElement omserver = null;
    while (it.hasNext()) {
      OMElement om = (OMElement) it.next();
      String url = om.getAttributeValue(new QName(URL));
      if (context.getServer().equals(url)) {
        omserver = om;
        break;
      }
    }
    if (omserver == null) {
      throw new ConnectorException("Missing server node " + context.getServer());
    }
    String path = context.getSite().replaceAll(context.getServer(), "");
    String[] paths = path.split("[/]");
    OMElement omsite = omserver;
    for (int i = 0; i < paths.length; ++i) {
      if (paths[i].equals("")) {
        paths[i] = "/";
      }
      it = omsite.getChildren();
      boolean found = false;
      while (it.hasNext()) {
        OMElement om = (OMElement) it.next();
        String name = om.getAttributeValue(new QName("Name"));
        if (name.equals(paths[i])) {
          omsite = om;
          found = true;
          break;
        }
      }
      if (!found) {
        throw new ConnectorException("Missing site node " + context.getSite());
      }
    }
    return omsite;
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

  private static void recordList(ClientContext context, String title,
      int itemCount) {
    // siteDirectory "site" is used to get all sites as list, but it's not a
    // list
    if (context.getSite().endsWith("SiteDirectory")) {
      return;
    }
    try {
      OMElement s = locateSite(context);
      OMElement l = omfactory.createOMElement(LIST, null);
      l.addAttribute(NAME, title, null);
      l.addAttribute("item_count", "" + itemCount, null);
      s.addChild(l);
    } catch (ConnectorException e) {
      logger.error(e.getMessage());
    }
  }

  public static void report() {
    logger.info("\nFound " + webCount + " Sites with " + existListCount
      + " Lists, " + itemCount + " Items. Crawled " + crawlListCount
      + " Lists \n");
    String siteMap = Util.xmlToString(docReport);
    try {
      Writer out = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream("logs/SiteMap.xml")));
      out.write(siteMap);
      out.close();
    } catch (FileNotFoundException e) {
      logger.error(e.getMessage());
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

  public static void init() throws ConfigurationException {
    String workingDir = System.getProperty("user.dir");
    config = new XMLConfiguration(workingDir + "/config/checkpoint.xml");
    config.setReloadingStrategy(new FileChangedReloadingStrategy());
    config.setAutoSave(true);
  }

  public static String getLastAccessTime(String list) {
    synchronized (config) {
      list = list.substring(1, list.length() - 2);
      return config.getString(cfgLists + ".LS_" + list + lastAccessTimeTag);
    }
  }

  /**
   * It seems the GetListItemChanges always use GMT time that's why GMT is
   * hardcoded here
   * 
   * @param list
   * @param dt
   */
  public static void saveLastAccessTime(String list, Date dt) {
    String format;
    SimpleDateFormat formatter = new SimpleDateFormat(
      ConnectorConstants.utcFormat);
    String sDate = formatter.format(dt);
    list = list.substring(1, list.length() - 2);
    String tag = cfgLists + ".LS_" + list + lastAccessTimeTag;
    synchronized (config) {
      config.setProperty(tag, sDate);
    }
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
}
