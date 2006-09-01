package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.HashMap;
import java.util.Iterator;

public class SharepointPropertyMap implements PropertyMap {

  private HashMap map = new HashMap();
  public Property getProperty(String name) throws RepositoryException {
    // TODO Auto-generated method stub
    return (Property)map.get(name);
  }

  public Iterator getProperties() throws RepositoryException {
    // TODO Auto-generated method stub
    return map.entrySet().iterator();
  }
  
  public void setProperty(String name, Property prop)throws RepositoryException {
   map.put(name, prop); 
  }  
}
