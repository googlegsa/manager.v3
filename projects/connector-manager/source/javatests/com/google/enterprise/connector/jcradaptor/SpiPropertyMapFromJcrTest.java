package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDocument;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrNode;
import com.google.enterprise.connector.pusher.MockFeedConnection;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import javax.jcr.Node;

public class SpiPropertyMapFromJcrTest extends TestCase {

  public final void testSpiPropertyMapFromJcr() throws RepositoryException {
    {
      String json1 =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\","
              + "\"content\":\"now is the time\", "
              + "\"google:contenturl\":\"http://www.sometesturl.com/test\""
              + ", \"google:lastmodified\":\"Tue, 15 Nov 1994 12:45:26 GMT\""
              + "}\r\n" + "";
      String date1 = "1970-01-01T00:00:10.000Z";
      String date2 = "Tue, 15 Nov 1994 12:45:26 GMT";
      PropertyMap propertyMap = makePropertyMapFromJson(json1);
      int count = countProperties(propertyMap);
      Assert.assertEquals(5, count);
      validateProperty(propertyMap, SpiConstants.PROPNAME_LASTMODIFIED, date2);
      validateProperty(propertyMap, "jcr:lastModified", date1);
      validateProperty(propertyMap, SpiConstants.PROPNAME_CONTENT,
          "now is the time");
      validateProperty(propertyMap, SpiConstants.PROPNAME_CONTENTURL,
          "http://www.sometesturl.com/test");
    }
    {
      String json1 =
          "{\"timestamp\":\"10\",\"docid\":\"doc1\","
              + "\"content\":\"now is the time\", "
              + "\"google:contenturl\":\"http://www.sometesturl.com/test\""
              + "}\r\n" + "";
      String date1 = "1970-01-01T00:00:10.000Z";
      PropertyMap propertyMap = makePropertyMapFromJson(json1);
      int count = countProperties(propertyMap);
      Assert.assertEquals(4, count);
      validateProperty(propertyMap, SpiConstants.PROPNAME_LASTMODIFIED, date1);
      validateProperty(propertyMap, SpiConstants.PROPNAME_CONTENT,
          "now is the time");
      validateProperty(propertyMap, SpiConstants.PROPNAME_CONTENTURL,
          "http://www.sometesturl.com/test");
    }
  }

  private void validateProperty(PropertyMap propertyMap, String name,
      String expectedValue) throws RepositoryException {
    Assert.assertEquals(expectedValue, propertyMap.getProperty(name).getValue()
        .getString());
  }

  public int countProperties(PropertyMap propertyMap)
      throws RepositoryException {
    int counter = 0;
    System.out.println();
    for (Iterator i = propertyMap.getProperties(); i.hasNext();) {
      Property property = (Property) i.next();
      Value value = property.getValue();
      String name = property.getName();
      System.out.print(name);
      System.out.print("(");
      String type = value.getType().toString();
      System.out.print(type);
      System.out.print(") ");
      String valueString = value.getString();
      System.out.print(valueString);
      System.out.println();
      counter++;
    }
    return counter;
  }

  public static PropertyMap makePropertyMapFromJson(String jsonString) {
    JSONObject jo;
    try {
      jo = new JSONObject(jsonString);
    } catch (JSONException e) {
      throw new IllegalArgumentException("test input can not be parsed");
    }
    MockRepositoryDocument document = new MockRepositoryDocument(jo);
    MockJcrNode node = new MockJcrNode(document);
    PropertyMap propertyMap = new SpiPropertyMapFromJcr(node);
    return propertyMap;
  }

  public final void testSpiPropertyMapFromJcrFromMockRepo()
      throws RepositoryException {
    {
      MockRepositoryEventList mrel =
          new MockRepositoryEventList("MockRepositoryEventLog3.txt");
      MockRepository r = new MockRepository(mrel);
      MockRepositoryDocument doc = r.getStore().getDocByID("doc1");

      MockFeedConnection mockFeedConnection = new MockFeedConnection();

      Node node = new MockJcrNode(doc);
      PropertyMap propertyMap = new SpiPropertyMapFromJcr(node);
      int count = countProperties(propertyMap);
    }
  }


}
