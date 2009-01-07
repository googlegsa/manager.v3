// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SimpleDocumentList;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Simple test connector that can be used with <code>SimpleConnectorType</code>
 * to test storage of some specific properties.
 */
public class SimpleTestConnector implements Connector {
  private String color;
  private String repositoryFileName;
  private String username;
  private String workDirName;

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getRepositoryFileName() {
    return repositoryFileName;
  }

  public void setRepositoryFileName(String repositoryFileName) {
    this.repositoryFileName = repositoryFileName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getWorkDirName() {
    return workDirName;
  }

  public void setWorkDirName(String workDirName) {
    this.workDirName = workDirName;
  }

  public Session login() {
    return new SimpleTestSession();
  }

  public class SimpleTestSession implements Session {
    public AuthenticationManager getAuthenticationManager() {
      return new SimpleAuthenticationManager();
    }

    public AuthorizationManager getAuthorizationManager() {
      return new SimpleAuthorizationManager();
    }

    public TraversalManager getTraversalManager() {
      return new SimpleTraversalManager();
    }
  }

  public class SimpleAuthenticationManager implements AuthenticationManager {
    public AuthenticationResponse authenticate(AuthenticationIdentity id) {
      return new AuthenticationResponse(true, "admin");
    }
  }

  public class SimpleAuthorizationManager implements AuthorizationManager {
    public Collection authorizeDocids(Collection col,
        AuthenticationIdentity id) {
      return col;
    }
  }

  public class SimpleTraversalManager implements TraversalManager {
    private boolean documentServed = false;

    public void setBatchHint(int hint) {
    }

    public DocumentList startTraversal() {
      if (!documentServed) {
        return traverse();
      } else {
        return null;
      }
    }

    public DocumentList resumeTraversal(String checkpoint) {
      if (!documentServed) {
        return traverse();
      } else {
        return null;
      }
    }

    /**
     * Utility method to produce a <code>DocumentList</code> containing one
     * <code>Document</code>.  It also sets the <code>documentServed</code>
     * state so that is the only <code>Document</code> traversed for this
     * <code>Connector</code>.
     */
    private DocumentList traverse() {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(10 * 1000);

      Map props = new HashMap();
      props.put(SpiConstants.PROPNAME_DOCID, "1");
      props.put(SpiConstants.PROPNAME_LASTMODIFIED, cal);
      props.put(SpiConstants.PROPNAME_DISPLAYURL, "http://myserver/docid=1");
      props.put(SpiConstants.PROPNAME_CONTENT, "Hello World!");
      SimpleDocument document = createSimpleDocument(props);

      List docList = new LinkedList();
      docList.add(document);

      documentServed = true;
      return new SimpleDocumentList(docList);
    }

    /**
     * Utility method to convert a <code>Map</code> of Java Objects into a
     * <code>SimpleDocument</code>.
     */
    private SimpleDocument createSimpleDocument(Map props) {
      Map spiValues = new HashMap();
      for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
        String key = (String) iter.next();
        Object obj = props.get(key);
        Value val = null;
        if (obj instanceof String) {
          val = Value.getStringValue((String) obj);
        } else if (obj instanceof Calendar) {
          val = Value.getDateValue((Calendar) obj);
        } else {
          throw new AssertionError(obj);
        }
        List values = new ArrayList();
        values.add(val);
        spiValues.put(key, values);
      }
      return new SimpleDocument(spiValues);
    }
  }
}
