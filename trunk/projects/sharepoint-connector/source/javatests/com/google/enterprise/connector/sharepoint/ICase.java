package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;

public interface ICase {
	void perform(ClientContext context, Object obj);
}
