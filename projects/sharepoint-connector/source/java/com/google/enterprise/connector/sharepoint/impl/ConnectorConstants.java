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

/**
 * 
 * Constants class
 */
public class ConnectorConstants {
	// mime
	public final static String MIME_TYPE = "mimetype";

	public final static String MIME_TEXT = "text/plain";

	// feeder
	public final static String FEEDER_LAST_MODIFIED = "last-modified";

	public final static String FEEDER_NAME = "name";

	public final static String FEEDER_CONTENT = "content";

	public final static String FEEDER_METADATA = "metadata";

	public final static String FEEDER_META = "meta";

	public final static String FEEDER_URL = "url";

	public final static String FEEDER_HEADER = "header";

	public final static String FEEDER_RECORD = "record";

	public final static String FEEDER_GROUP = "group";

	public final static String FEEDER_GSAFEED = "gsafeed";

	public final static String FEEDER_TYPE = "feedtype";

	public final static String FEEDER_DATA = "data";

	public final static String FEEDER_DS = "datasource";

	// feed types
	public final static String FEEDER_TYPE_FULL = "full";

	public final static String FEEDER_TYPE_INC = "incremental";

	public final static String FEEDER_TYPE_URL = "metadata-and-url";

	// authentication
	public final static String AUTH_TYPE_BASIC = "basic";

	public final static String AUTH_TYPE_NTLM = "ntlm";

	public static String utcFormat = "yyyy-MM-dd HH:mm:ss'Z'";

	public static String SLASH = "/";

	public static int MODE_FEED = 0;

	public static int MODE_QUERY = 1;

}
