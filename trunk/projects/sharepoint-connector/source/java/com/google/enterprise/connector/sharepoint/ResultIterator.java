package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

import connector.ConnectorConstants;
import connector.sharepoint.BaseList;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

import javax.xml.namespace.QName;

public class ResultIterator implements Iterator {
  BaseList list;
  private static Log logger = LogFactory.getLog(ResultIterator.class);

  public ResultIterator(BaseList lst) {
    list = lst;
  }

  public boolean hasNext() {
    return list.hasNext();
  }

  /**
   * returns the formated node
   */
  public Object next() {
    OMElement em = (OMElement) list.next();
    SharepointPropertyMap pm = new SharepointPropertyMap();
    // process meta data
    try {
      String modifiedTime = em.getAttributeValue(new QName(
        ConnectorConstants.FEEDER_LAST_MODIFIED));
      SharepointProperty nameProp = new SharepointProperty(
        SpiConstants.PROPNAME_LASTMODIFY, modifiedTime, ValueType.DATE);
      pm.setProperty(SpiConstants.PROPNAME_LASTMODIFY, nameProp);
      String url = em
        .getAttributeValue(new QName(ConnectorConstants.FEEDER_URL));
      nameProp = new SharepointProperty(SpiConstants.PROPNAME_CONTENTURL, url);
      pm.setProperty(SpiConstants.PROPNAME_CONTENTURL, nameProp);
      pm.setProperty(SpiConstants.PROPNAME_DOCID, nameProp);
      Iterator metas;
      if (em.getChildren().hasNext()) {
        OMElement metadata = (OMElement) em.getChildren().next();
        metas = metadata.getChildren();
        while (metas.hasNext()) {
          OMElement meta = (OMElement) metas.next();
          String name = meta.getAttributeValue(new QName(
            ConnectorConstants.FEEDER_NAME));
          String value = meta.getAttributeValue(new QName(
            ConnectorConstants.FEEDER_CONTENT));
          nameProp = new SharepointProperty(name, value);
          pm.setProperty(name, nameProp);
        }
      }
      return pm;
    } catch (RepositoryException e) {
      logger.error(e.getMessage());
      return null;
    }
  }

  public void remove() {
    throw new UnsupportedOperationException("remove not implemented");
  }
}
