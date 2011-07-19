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

/**
 * Factory implementation for creating {@link SharepointSite} objects
 * 
 * @author nitendra_thakur
 */
public class SharepointSiteFactory {

    private SharepointSiteFactory() {

    }

    /**
     * Returns a {@link SharepointSite} object corresponding to the SharePoint
     * site represented by the passed in URL
     *
     * @param startUrl
     * @return
     */
	public static SharepointSite getSharepointSite(String startUrl,
			String username, String password, String domain) throws Exception {
		WebServiceClient wsClient = new WebServiceClient(startUrl, username,
				password, domain);
		SharepointSite site = new SharepointSiteImpl(wsClient.getWebUrl(),
				wsClient.getWebUrl(), wsClient);
		return site;
    }
}
