// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.StringValue;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.ValueFormatException;
import javax.jcr.observation.Event;

/**
 * Document wrapper class around events rather than nodes.
 */
public class JcrEventDocument implements Document {
  private javax.jcr.observation.Event event;
  private Calendar lastModified;
  private Set propNames;

  public JcrEventDocument(Event event, Calendar lastModified) {
    this.event = event;
    this.lastModified = lastModified;
    propNames = new HashSet();
    propNames.add(SpiConstants.PROPNAME_DOCID);
    propNames.add(SpiConstants.PROPNAME_ACTION);
    propNames.add(SpiConstants.PROPNAME_LASTMODIFIED);
  }

  public Property findProperty(String name) throws RepositoryException {
    JcrEventProperty result = null;
    if (SpiConstants.PROPNAME_DOCID.equals(name)) {
      try {
        result = new JcrEventProperty(SpiConstants.PROPNAME_DOCID,
            new StringValue(event.getPath()));
      } catch (javax.jcr.RepositoryException e) {
        throw new RepositoryException(e);
      }
    } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
      String action = "delete";
      if (event.getType() == Event.NODE_ADDED) {
        action = "add";
      }
      result = new JcrEventProperty(SpiConstants.PROPNAME_ACTION,
          new StringValue(action));
    } else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
      result = new JcrEventProperty(SpiConstants.PROPNAME_LASTMODIFIED,
          new DateValue(lastModified));
    }
    return result;
  }

  public Set getPropertyNames() throws RepositoryException {
    return propNames;
  }
}
