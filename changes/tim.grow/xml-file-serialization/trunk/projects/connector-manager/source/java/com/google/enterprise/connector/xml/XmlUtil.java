package com.google.enterprise.connector.xml;

import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.ConnectorIterator;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.ProductionManager;
import com.google.enterprise.connector.servlet.SAXParseErrorHandler;

import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.Map;

public class XmlUtil {
  private static final class XmlUtilConnectorIterator implements ConnectorIterator {

    private NodeList connectorElements;
    private int index;

    public XmlUtilConnectorIterator(Element connectorElement) {
      connectorElements = connectorElement.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR);
      index = -1;
    }

    public XmlUtilConnectorIterator(String xmlString) {
      this(ServletUtil.parse(xmlString, new SAXParseErrorHandler()).getDocumentElement());
    }

    public boolean next() {
      index++;
      return index < connectorElements.getLength();
    }

    public String getName() {
      Element connectorElement = (Element) connectorElements.item(index);
      return ServletUtil.getFirstElementByTagName(connectorElement, ServletUtil.XMLTAG_CONNECTOR_NAME);
    }

    public String getType() {
      Element connectorElement = (Element) connectorElements.item(index);
      return ServletUtil.getFirstElementByTagName(connectorElement, ServletUtil.XMLTAG_CONNECTOR_TYPE);
    }

    public Map getConfig() {
      Element connectorElement = (Element) connectorElements.item(index);
      Element configElement = (Element) connectorElement.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_CONFIG).item(0);
      return ServletUtil.getAllAttributes(configElement, ServletUtil.XMLTAG_PARAMETERS);
    }

    public int getLoad() {
      Element connectorElement = (Element) connectorElements.item(index);
      return Integer.parseInt(ServletUtil.getFirstElementByTagName(connectorElement, ServletUtil.XMLTAG_LOAD));
    }

    public String getTimeIntervals() {
      Element connectorElement = (Element) connectorElements.item(index);
      return ServletUtil.getFirstElementByTagName(connectorElement, ServletUtil.XMLTAG_TIME_INTERVALS);
    }
  }

  public static ConnectorIterator fromXmlConnectorElement(Element connectorElement) {
    return new XmlUtilConnectorIterator(connectorElement);
  }

  public static ConnectorIterator fromXmlString(String s) {
    return new XmlUtilConnectorIterator(s);
  }

  // TODO(timg): input parameter: level (default: 0)
  // TODO(timg): XML-delimit
  public static String asXmlString(ConnectorIterator ci) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    ServletUtil.writeXMLTag(pw, 0, ServletUtil.XMLTAG_CONNECTORS, false);

    while (ci.next()) {
      ServletUtil.writeXMLTag(pw, 1, ServletUtil.XMLTAG_CONNECTOR, false);
      ServletUtil.writeXMLElement(pw, 2, ServletUtil.XMLTAG_CONNECTOR_NAME, ci.getName());
      ServletUtil.writeXMLElement(pw, 2, ServletUtil.XMLTAG_CONNECTOR_TYPE, ci.getType());
      ServletUtil.writeXMLElement(pw, 2, ServletUtil.XMLTAG_LOAD, Integer.toString(ci.getLoad()));
      ServletUtil.writeXMLElement(pw, 2, ServletUtil.XMLTAG_TIME_INTERVALS, ci.getTimeIntervals());
      ServletUtil.writeXMLTag(pw, 2, ServletUtil.XMLTAG_CONNECTOR_CONFIG, false);
      for (Iterator i = ci.getConfig().entrySet().iterator(); i.hasNext(); ) {
        Map.Entry me = (Map.Entry) i.next();
        // XXX(timg): explicitly append "/" to close tag
        ServletUtil.writeXMLElementWithAttrs(pw, 3, ServletUtil.XMLTAG_PARAMETERS, ServletUtil.ATTRIBUTE_NAME + (String) me.getKey() + ServletUtil.QUOTE + ServletUtil.ATTRIBUTE_VALUE + (String) me.getValue() + ServletUtil.QUOTE + "/");
      }
      ServletUtil.writeXMLTag(pw, 2, ServletUtil.XMLTAG_CONNECTOR_CONFIG, true);
      ServletUtil.writeXMLTag(pw, 1, ServletUtil.XMLTAG_CONNECTOR, true);
    }

    ServletUtil.writeXMLTag(pw, 0, ServletUtil.XMLTAG_CONNECTORS, true);

    return sw.toString();
  }

  public static ConnectorIterator readFromFile(String filename) throws IOException {
    Reader isr = new InputStreamReader(new FileInputStream(filename), "UTF-8");
    String string = StringUtils.readAllToString(isr);
    return fromXmlString(string);
  }

  public static void writeToFile(String filename, ConnectorIterator ci) throws IOException {
    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
    osw.write(asXmlString(ci));
    osw.close();
  }

  public static final void main(String[] args) throws IOException {
    Context context = Context.getInstance();
    context.setJunitContextLocation("WEB-INF/applicationContext.xml");
    context.setJunitCommonDirPath("WEB-INF");
    Manager manager = context.getManager();

    if (args.length == 2 && args[0].equals("export")) {
      writeToFile(args[1], manager.getConnectors());
    } else if (args.length == 2 && args[0].equals("import")) {
      ConnectorIterator ci = readFromFile(args[1]);
      while (ci.next()) {
        System.out.println(ci.getName());
        System.out.println(ci.getConfig());
      }
    } else {
      System.err.println("usage: java XmlUtil (export|import) <filename>");
    }
  }
}
