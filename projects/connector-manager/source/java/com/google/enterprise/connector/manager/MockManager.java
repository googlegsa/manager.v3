// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.google.enterprise.connector.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

/**
 * Implement interface Manager.
 * TODO: move to javatests when possible.
 * 
 */
public class MockManager implements Manager {
	private static final MockManager INSTANCE = new MockManager();

	private MockManager() {
	}

	public static MockManager getInstance() {
		return INSTANCE;
	}

	public boolean authenticate(String connectorInstanceName, String username,
			String password) {
		// TODO Auto-generated method stub
		return false;
	}

	public List authorizeDocids(String connectorInstanceName, List docidList,
			String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public List authorizeTokens(String connectorInstanceName, List tokenList,
			String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getConnectorStatuses() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getConnectorTypes() {
		// TODO Auto-generated method stub
		List connectorTypes = new ArrayList();
		connectorTypes.add("documentum");
		connectorTypes.add("sharepoint");
		return connectorTypes;
	}

	public void storeConfig(boolean certAuth, String feederGateHost,
			int feederGatePort, int maxFeedRate) {
		// TODO Auto-generated method stub

	}

	public ConfigureResponse getConfigForm(String ConnectorType, String language) throws ConnectorTypeNotFoundException, ConnectorManagerException {
		// TODO Auto-generated method stub
		return null;
	}

	public ConfigureResponse getConfigFormForConnector(String connectorName, String language) throws ConnectorNotFoundException, ConnectorManagerException {
		// TODO Auto-generated method stub
		return null;
	}

	public ConnectorStatus getConnectorStatus(String connectorInstanceName) throws ConnectorManagerException {
		// TODO Auto-generated method stub
		return null;
	}

	public ConfigureResponse setConfig(String connectorName, Map configData, String language) throws ConnectorNotFoundException, ConnectorManagerException {
		// TODO Auto-generated method stub
		return null;
	}

}

