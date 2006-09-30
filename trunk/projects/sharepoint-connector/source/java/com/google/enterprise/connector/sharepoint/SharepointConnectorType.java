package com.google.enterprise.connector.sharepoint;

import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.ConnectorException;
import com.google.enterprise.connector.sharepoint.impl.Util;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;

public class SharepointConnectorType implements ConnectorType {
	public static String URL = "url", PROXY_SERVER = "proxy_server",
			PROXY_PORT = "proxy_port", AUTH_TYPE = "auth_type",
			EXCLUDE_PATTERN = "exclude_pattern", META_TAGS = "meta_tags",
			META_EXCLUDE = "meta_exclude", URL_REQUIRED = "url_required";

	ConfigureResponse response;

	ClientContext context;

	public SharepointConnectorType() throws ConfigurationException,
			UnknownHostException {
		ClientContext.init();
		context = new ClientContext(0);
	}

	private static Log logger = LogFactory
			.getLog(SharepointConnectorType.class);

	public ConfigureResponse validateConfig(Map configData, String language) {
		try {
			Locale locale = new Locale(language);
			ResourceBundle labels = ResourceBundle.getBundle("LabelsBundle",
					locale);
			ClientContext.init();
			ClientContext context = new ClientContext(0);
			// save auth type
			String value = (String) configData.get(AUTH_TYPE);
			context.setAuthType(value);
			// validate connection works
			value = (String) configData.get(URL);
			if (value == null) {
				throw new ConnectorException(getLabel(labels, URL_REQUIRED));
			}
			context.setContentServerUrl(value);
			value = (String) configData.get(PROXY_SERVER);
			context.setContentServerProxy(value);
			value = (String) configData.get(PROXY_PORT);
			if (value != null) {
				context.setContentServerProxyPort(Integer.valueOf(value));
			} else {
				context.setContentServerProxyPort(null);
			}
			// validate exclude pattern using regx
			value = (String) configData.get(EXCLUDE_PATTERN);
			saveExcludePattern(value);
			// validate meta tags, make sure they exist
			value = (String) configData.get(META_EXCLUDE);
			saveMetaExclude(value);
			return response;
		} catch (Exception e) {
			Util.processException(logger, e);
			return new ConfigureResponse(e.getMessage(), response
					.getFormSnippet());
		}
	}

	public ConfigureResponse getConfigForm(String language) {
		Locale locale = new Locale(language);
		ResourceBundle labels = ResourceBundle
				.getBundle("LabelsBundle", locale);
		String cfg = "";
		cfg += col(URL, text(URL, context.getServer(), 50), labels);
		cfg += col(PROXY_SERVER, text(PROXY_SERVER, context
				.getContentServerProxy(), 50), labels);
		cfg += col(PROXY_PORT, text(PROXY_PORT, String.valueOf(context
				.getContentServerProxyPort()), 10), labels);
		cfg += col(AUTH_TYPE, select(AUTH_TYPE, context.getAuthType(),
				new String[] { "basic", "ntlm" }, labels), labels);
		cfg += col(EXCLUDE_PATTERN, textarea(EXCLUDE_PATTERN, context
				.getExclude()), labels);
		if (context.getMetaFields().size() != 0) {
			cfg += col(META_TAGS, label(hashToString(context.getMetaFields())),
					labels);
			cfg += col(META_EXCLUDE, textarea(META_EXCLUDE, hashToArray(context
					.getSkipFields())), labels);
		}
		response = new ConfigureResponse(null, cfg);
		return response;
	}

	String[] textToArray(String orig) {
		if (orig == null) {
			return new String[0];
		}
		return orig.split(",");
	}

	String hashToString(Hashtable table) {
		Iterator it = table.keySet().iterator();
		String result = "";
		while (it.hasNext()) {
			result += (String) it.next() + ",";
		}
		return result;
	}

	String[] hashToArray(Hashtable table) {
		Iterator it = table.keySet().iterator();
		String result[] = new String[table.size()];
		int i = 0;
		while (it.hasNext()) {
			result[i++] = (String) it.next();
		}
		return result;
	}

	private String col(String name, String input, ResourceBundle labels) {
		return "<tr><td>" + getLabel(labels, name) + "</td>" + "\n<td>" + input
				+ "</td></tr>";
	}

	public String text(String name, String value, int size) {
		return "<input type=text name=\"" + name + "\" value=\"" + value
				+ "\" size=\"" + size + "\"/>";
	}

	public String text(String name, String value) {
		return "<input type=text name=\"" + name + "\" value=\"" + value
				+ "\"/>";
	}

	public String label(String value) {
		return value;
	}

	public String textarea(String name, String values[]) {
		String result = "<textarea rows=5 cols=50 name=\"" + name + "\">";
		for (int i = 0; i < values.length; ++i) {
			result += values[i] + "\n";
		}
		result += "</textarea>";
		return result;
	}

	public String select(String name, String selected, String[] values,
			ResourceBundle labels) {
		String result = "<select name=\"" + name + "\">";
		for (int i = 0; i < values.length; ++i) {
			result += "<option value=\"" + values[i] + "\" ";
			if (selected != null && values[i].equals(selected)) {
				result += " selected ";
			}
			result += "/>" + getLabel(labels, values[i]) + "</option>";
		}
		result += "</select>";
		return result;
	}

	private String getLabel(ResourceBundle labels, String key) {
		try {
			return labels.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	void saveExcludePattern(String value) {
		if (value == null) {
			context.setExclude(null);
			return;
		}
		String urls[] = value.split("\n");
		context.setExclude(urls);
	}

	void saveMetaExclude(String value) throws ConnectorException {
		if (value != null) {
			value = value.replaceAll("\n", ",");
		}
		context.setSkiptFields(value);
	}

  public ConfigureResponse getPopulatedConfigForm(Map configMap, String language) {
    throw new UnsupportedOperationException();
  }

}
