package com.google.enterprise.connector.sharepoint;

import java.util.HashMap;

import junit.framework.TestCase;

import com.google.enterprise.connector.spi.ConfigureResponse;

public class TestConnectorType extends TestCase {
	SharepointConnectorType sptype;

	protected void setUp() throws Exception {
		sptype = new SharepointConnectorType();
		super.setUp();
	}

	public void testGet() throws Exception {
		ConfigureResponse response = sptype.getConfigForm("en_US");
		System.out.println(response.getFormSnippet());
		sptype.getConfigForm("zh_CN");
	}

	public void testValidateSuccess() throws Exception {
		ConfigureResponse response = sptype.getConfigForm("en_US");
		HashMap map = new HashMap();
		map.put(SharepointConnectorType.AUTH_TYPE, "ntlm");
		map.put(SharepointConnectorType.URL, "http://columbus.corp.google.com");
		map.put(SharepointConnectorType.EXCLUDE_PATTERN, "http://a\nhttp://b");
		map.put(SharepointConnectorType.META_EXCLUDE, "Title");
		map.put(SharepointConnectorType.PROXY_SERVER, "cache.google.com");
		map.put(SharepointConnectorType.PROXY_PORT, "8123");
		response = sptype.validateConfig(map, "en_US");
		this.assertNull(response.getMessage());
	}

	public void testValidateWrongUrl() throws Exception {
		ConfigureResponse response = sptype.getConfigForm("en_US");
		HashMap map = new HashMap();
		map.put(SharepointConnectorType.AUTH_TYPE, "ntlm");
		map.put(SharepointConnectorType.URL, "http://columbus.corp.google.com");
		response = sptype.validateConfig(map, "en_US");
		this.assertNull(response.getMessage());
	}

	public void testValidateWrongMeta() throws Exception {
		ConfigureResponse response = sptype.getConfigForm("en_US");
		HashMap map = new HashMap();
		map.put(SharepointConnectorType.AUTH_TYPE, "ntlm");
		map.put(SharepointConnectorType.URL, "http://columbus.corp.google.com");
		map.put(SharepointConnectorType.META_EXCLUDE, "a, b");
		response = sptype.validateConfig(map, "en_US");
		this.assertNotNull(response.getMessage());
	}

	public void testValidateNullValue() throws Exception {
		ConfigureResponse response = sptype.getConfigForm("en_US");
		HashMap map = new HashMap();
		map.put(SharepointConnectorType.AUTH_TYPE, "ntlm");
		map.put(SharepointConnectorType.URL, "http://columbus.corp.google.com");
		map.put(SharepointConnectorType.EXCLUDE_PATTERN, null);
		map.put(SharepointConnectorType.META_EXCLUDE, null);
		map.put(SharepointConnectorType.PROXY_SERVER, null);
		map.put(SharepointConnectorType.PROXY_PORT, null);
		response = sptype.validateConfig(map, "en_US");
		this.assertNull(response.getMessage());
	}
	public void testReset() throws Exception {
		ConfigureResponse response = sptype.getConfigForm("en_US");
		HashMap map = new HashMap();
		map.put(SharepointConnectorType.AUTH_TYPE, "ntlm");
		map.put(SharepointConnectorType.URL, "http://columbus.corp.google.com");
		map.put(SharepointConnectorType.EXCLUDE_PATTERN, null);
		map.put(SharepointConnectorType.META_EXCLUDE, "Title");
		map.put(SharepointConnectorType.PROXY_SERVER, "cache.google.com");
		map.put(SharepointConnectorType.PROXY_PORT, "8123");
		response = sptype.validateConfig(map, "en_US");
		this.assertNull(response.getMessage());
	}
}
