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

package com.google.enterprise.connector.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mock event object.
 * <p>
 * An embedded enumerated type, plus some simple boiler plate.
 */
public class MockRepositoryEvent {
  /**
   * Enumeration for event types.
   * @author ziff@google.com (Donald "Max" Ziff)
   */
  public static class EventType implements Comparable {
	  private static int nextOrdinal = 0;
	  private final int ordinal = nextOrdinal++;

	    public static final EventType SAVE = 
	    	new EventType("save");
	    public static final EventType DELETE = 
	    	new EventType("delete");
	    public static final EventType METADATA_ONLY_SAVE = 
	    	new EventType("metadata_only_save");
	    public static final EventType ERROR = 
	    	new EventType("error");

		private static final EventType[] PRIVATE_VALUES =
		  {SAVE, DELETE, METADATA_ONLY_SAVE, ERROR};
		public static final List Values = 
			Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));

		private String tag;

    EventType(String m) {
    	tag = m;
    }

    public String toString() {
      return tag;
    }
    
    public static EventType findEventType(String tag) {
        if (tag == null) {
          return ERROR;
        }
        for (int i =0; i<PRIVATE_VALUES.length; i++) {
          if (PRIVATE_VALUES[i].tag.equals(tag)) {
            return PRIVATE_VALUES[i];
          }
        }
        return ERROR;
      }

  	public int compareTo(Object o) {
  		return ordinal - ((EventType)o).ordinal;
  	}
  }

  private EventType type;
  private String docID;
  private String content;
  private MockRepositoryPropertyList propertyList;
  private MockRepositoryDateTime timeStamp;
  
  public String toString() {
    String displayContent = 
      ((content != null) ? (" content:\"" + content + "\" ") : "null");
    return "{" + timeStamp.toString() + " " + type + " " + docID + 
      displayContent + propertyList.toString() + "}";
  }

  public MockRepositoryEvent(EventType type, 
                             String docID, 
                             String content,
                             MockRepositoryPropertyList propertyList, 
                             MockRepositoryDateTime timeStamp) {
    this.type = type;
    this.docID = docID;
    this.content = content;
    this.propertyList = propertyList;
    this.timeStamp = timeStamp;
  }

  public MockRepositoryEvent(Map params) {
    String docid = null;
    String tempContent = null;
    String eventTypeTag = null;
    String timeStampStr = null;
    Map propBag = new HashMap();
    for (Iterator iter = params.entrySet().iterator(); iter.hasNext(); ) {
    	Map.Entry entry = (Map.Entry) iter.next();
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if ("docid".equals(key)) {
        docid = value;
      } else if ("content".equals(key)) {
        tempContent = value;
      } else if ("type".equals(key)) {
        eventTypeTag = value;
      } else if ("timestamp".equals(key)) {
        timeStampStr = value;
      } else {
        propBag.put(key, value);
      }    
    }
    // validate the important parameters
    // content may be null or "" - at this level we don't care
    if (docid == null) {
      throw new RuntimeException("Event parameters must specify docid");
    }
    EventType t = EventType.findEventType(eventTypeTag);
    if (t == EventType.ERROR) {
      throw new RuntimeException("Event parameters must " +
        "specify a legal event type");
    }
    int ticks = -1;
    ticks = Integer.parseInt(timeStampStr);
    if (ticks < 0) {
      throw new RuntimeException("Event parameters must " +
        "specify a non-zero time stamp");      
    }
    List l = new LinkedList();
    for (Iterator iter = params.entrySet().iterator(); iter.hasNext(); ) {
    	Map.Entry entry = (Map.Entry) iter.next();
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      l.add(new MockRepositoryProperty(key, value));
    }
    this.type = t;
    this.docID = docid;
    this.content = tempContent;
    this.propertyList = new MockRepositoryPropertyList(l);
    this.timeStamp = new MockRepositoryDateTime(ticks);
  }

  public String getContent() {
    return content;
  }

  public String getDocID() {
    return docID;
  }

  public MockRepositoryPropertyList getPropertyList() {
    return propertyList;
  }

  public EventType getType() {
    return type;
  }

  public MockRepositoryDateTime getTimeStamp() {
    return timeStamp;
  }
}
