package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class SpiAuthorizationManagerFromJcrTest extends TestCase {

  public final void testAuthorizeDocids() throws LoginException,
      RepositoryException,
      com.google.enterprise.connector.spi.RepositoryException {
    
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog2.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());
    Session session = repo.login(creds);
    AuthorizationManager authorizationManager =
        new SpiAuthorizationManagerFromJcr(session);

    {
      String username = "joe";

      Map expectedResults = new HashMap();
      expectedResults.put("doc1", Boolean.TRUE);
      expectedResults.put("doc2", Boolean.TRUE);
      expectedResults.put("doc3", Boolean.TRUE);
      expectedResults.put("doc4", Boolean.FALSE);
      expectedResults.put("doc5", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

    {
      String username = "bill";

      Map expectedResults = new HashMap();
      expectedResults.put("doc1", Boolean.FALSE);
      expectedResults.put("doc2", Boolean.FALSE);
      expectedResults.put("doc3", Boolean.TRUE);
      expectedResults.put("doc4", Boolean.TRUE);
      expectedResults.put("doc5", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

    {
      String username = "fred";

      Map expectedResults = new HashMap();
      expectedResults.put("doc1", Boolean.FALSE);
      expectedResults.put("doc2", Boolean.FALSE);
      expectedResults.put("doc3", Boolean.TRUE);
      expectedResults.put("doc4", Boolean.TRUE);
      expectedResults.put("doc5", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

    {
      String username = "murgatroyd";

      Map expectedResults = new HashMap();
      expectedResults.put("doc1", Boolean.FALSE);
      expectedResults.put("doc2", Boolean.FALSE);
      expectedResults.put("doc3", Boolean.TRUE);
      expectedResults.put("doc4", Boolean.FALSE);
      expectedResults.put("doc5", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

  }

  private void testAuthorization(AuthorizationManager authorizationManager, Map expectedResults, String username) throws com.google.enterprise.connector.spi.RepositoryException {
    List docids = new LinkedList(expectedResults.keySet());

    ResultSet resultSet =
        authorizationManager.authorizeDocids(docids, username);

    for (Iterator i = resultSet.iterator(); i.hasNext();) {
      PropertyMap pm = (PropertyMap) i.next();
      String uuid =
          pm.getProperty(SpiConstants.PROPNAME_DOCID).getValue().getString();
      boolean ok =
          pm.getProperty(SpiConstants.PROPNAME_AUTH_VIEWPERMIT).getValue()
              .getBoolean();
      Boolean expected = (Boolean) expectedResults.get(uuid);
      Assert.assertEquals(username + " access to " + uuid, expected.booleanValue(), ok);
    }
  }
}
