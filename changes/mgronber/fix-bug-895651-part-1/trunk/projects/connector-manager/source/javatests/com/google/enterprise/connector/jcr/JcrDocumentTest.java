package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrNode;
import com.google.enterprise.connector.pusher.MockFeedConnection;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Value;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import javax.jcr.Node;

public class JcrDocumentTest extends TestCase {

  public final void testJcrDocument() throws RepositoryException {
    {
      String json1 =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\","
              + "\"content\":\"now is the time\", "
              + "\"google:contenturl\":\"http://www.sometesturl.com/test\""
              + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
              + "}\r\n" + "";
      String date1 = "1970-01-01T00:00:10.000Z";
      String date2 = "Tue, 15 Nov 1994 12:45:26 GMT";
      Document propertyMap = makeDocumentFromJson(json1);
    
      validateProperty(propertyMap, SpiConstants.PROPNAME_LASTMODIFIED, date2);
      validateProperty(propertyMap, "jcr:lastModified", date1);
      validateProperty(propertyMap, SpiConstants.PROPNAME_CONTENT,
          "now is the time");
      validateProperty(propertyMap, SpiConstants.PROPNAME_CONTENTURL,
          "http://www.sometesturl.com/test");
      
      int count = countProperties(propertyMap);
      Assert.assertEquals(5, count);
    }
    {
      String json1 =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\","
              + "\"content\":\"now is the time\", "
              + "\"google:contenturl\":\"http://www.sometesturl.com/test\""
              + "}\r\n" + "";
      String date1 = "1970-01-01T00:00:10.000Z";
      Document propertyMap = makeDocumentFromJson(json1);
      validateProperty(propertyMap, SpiConstants.PROPNAME_LASTMODIFIED, date1);
      validateProperty(propertyMap, SpiConstants.PROPNAME_CONTENT,
          "now is the time");
      validateProperty(propertyMap, SpiConstants.PROPNAME_CONTENTURL,
          "http://www.sometesturl.com/test");
      
      int count = countProperties(propertyMap);
      Assert.assertEquals(4, count);
    }
  }

  private void validateProperty(Document document, String name,
      String expectedValue) throws RepositoryException {
    Assert.assertEquals(expectedValue, document.findProperty(name).nextValue()
        .toString());
  }

  public int countProperties(Document document)
      throws RepositoryException {
    int counter = 0;
    System.out.println();
    for (Iterator i = document.getPropertyNames().iterator(); i.hasNext();) {
      String name = (String) i.next();
      Property property = document.findProperty(name);
      Assert.assertNotNull(property);
      Value value = property.nextValue();
      Assert.assertNotNull(value);
      System.out.print(name);
      System.out.print("(");
      String type = value.getClass().getName();
      System.out.print(type);
      System.out.print(") ");
      String valueString = value.toString();
      System.out.print(valueString);
      System.out.println();
      counter++;
    }
    return counter;
  }

  public static Document makeDocumentFromJson(String jsonString) {
    JSONObject jo;
    try {
      jo = new JSONObject(jsonString);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    MockRepositoryDocument mockDocument = new MockRepositoryDocument(jo);
    MockJcrNode node = new MockJcrNode(mockDocument);
    Document document = new JcrDocument(node);
    return document;
  }

  public final void testJcrDocumentFromMockRepo()
      throws RepositoryException {
    {
      MockRepositoryEventList mrel =
          new MockRepositoryEventList("MockRepositoryEventLog3.txt");
      MockRepository r = new MockRepository(mrel);
      MockRepositoryDocument doc = r.getStore().getDocByID("doc1");

      MockFeedConnection mockFeedConnection = new MockFeedConnection();

      Node node = new MockJcrNode(doc);
      Document document = new JcrDocument(node);
      int count = countProperties(document);
    }
  }
}
