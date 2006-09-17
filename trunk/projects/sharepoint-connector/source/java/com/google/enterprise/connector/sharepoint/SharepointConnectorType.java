package com.google.enterprise.connector.sharepoint;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.Util;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;

public class SharepointConnectorType implements ConnectorType {
	String URL = "url", PROXY_SERVER = "proxy_server",
			PROXY_PORT = "proxy_port", AUTH_TYPE = "auth_type";

	String lang;

	ConfigureResponse response;

	private static Log logger = LogFactory
			.getLog(SharepointConnectorType.class);

	public ConfigureResponse validateConfig(Map configData, String language) {
		Iterator it = configData.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = (String) configData.get(key);
			setConfigEntry(key, value);
		}
		try {
			ClientContext.init();
			ClientContext context = new ClientContext(0);
			return response;
		} catch (Exception e) {
			Util.processException(logger, e);
			return new ConfigureResponse(e.getMessage(), response
					.getFormSnippet());
		}
	}

	public ConfigureResponse getConfigForm(String language) {
		lang = language;
		try {
			String cfg = "";
			cfg += row(col(URL, text(URL, ClientContext.getServers()[0])));
			return new ConfigureResponse(null, cfg);
		} catch (ConfigurationException e) {
			return null;
		}

	}

	private String row(String input) {
		return "<tr>" + input + "</tr>";
	}

	private String col(String name, String input) {
		return "<td>" + getLabel(name) + "</td>" + "\n<td>" + input + "</td>";
	}

	public String text(String name, String value) {
		return "<input type=text name=\"" + name + "\" value=\"" + value + "\"";
	}

	private String getLabel(String name) {
		return name;
	}

	private void setConfigEntry(String key, String value) {

	}
}
