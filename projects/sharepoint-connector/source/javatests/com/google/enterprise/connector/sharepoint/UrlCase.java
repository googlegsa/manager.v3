package com.google.enterprise.connector.sharepoint;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.impl.ClientContext;
import com.google.enterprise.connector.sharepoint.impl.ConnectorImpl;

public class UrlCase extends TestCase implements ICase {

	private static Log logger = LogFactory.getLog(UrlCase.class);

	public void perform(ClientContext context, Object obj) {
		try {
			HttpClient client = new HttpClient();
			HeadMethod head = new HeadMethod((String) obj);
			logger.info("test url:  " + (String)obj);
			head.setDoAuthentication(true);
			Credentials defaultcreds;
			if (context.getAuthType().equals("ntlm")) {
				//defaultcreds = new NTCredentials(context.getUserName(),
					//	context.getPassword(), context.getSPHost(), context.getSpDomain());
				defaultcreds = new NTCredentials(context.getUserName(), context.getPassword(), context.getSPCanonicalHost(), context.getSpDomain());
			} else {
				defaultcreds = new UsernamePasswordCredentials(context
						.getUserName(), context.getPassword());
			}
			client.getState().setCredentials(
					new AuthScope(context.getSPCanonicalHost(), context.getSPPort(), AuthScope.ANY_REALM, null),
					defaultcreds);
			client.executeMethod(head);

			if (head.getStatusCode() != HttpStatus.SC_OK) {
				head.releaseConnection();
				fail();
			}
		} catch (Exception e) {
			fail();
		}
	}
}
