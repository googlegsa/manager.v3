//Copyright 2009 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sp2c_migration;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;

public class SharepointSiteImpl implements SharepointSite {
	String id;
	String url;
	WebServiceClient wsClient;

    SharepointSiteImpl(String id, String url, WebServiceClient wsClient) {
		this.id = id;
		this.url = url;
		this.wsClient = wsClient;
	}

    @Override
	public String getId() {
		return id;
	}

    @Override
	public String getUrl() {
		return url;
    }

	@Override
	public List<Folder> getRootFolders() throws Exception {
		return wsClient.getListsAsFolders(id);
    }

    @Override
	public List<Folder> getFolders(Folder rootfolder) throws Exception {
		return wsClient.getFolders(rootfolder, null);
	}

    @Override
	public List<Document> getDocuments(Folder rootfolder) throws Exception {
		return wsClient.getDocuments(rootfolder, null);
    }

	/**
	 * To download document's content and set its mime type
	 */
	public InputStream getDocumentContent(Document document) throws Exception {
		HttpMethodBase method = wsClient.downloadContent(document.getDocUrl());
		if (null == method) {
			throw new Exception("Problem while downloading content");
		}

        final Header mimeType = method.getResponseHeader("Content-Type");
		if (mimeType != null) {
			document.setMimeType(mimeType.getValue());
		}
        return method.getResponseBodyAsStream();
    }

}
