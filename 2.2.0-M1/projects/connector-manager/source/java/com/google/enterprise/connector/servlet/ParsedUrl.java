// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.servlet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedUrl {
  private static final Pattern URL_PATTERN =
      Pattern.compile("^" + ServletUtil.PROTOCOL + "([^./]*)(?:[^/]*)?"
          + "(?:/[dD][oO][cC]\\?(?:[^&]*&)*[dD][oO][cC][iI][dD]=([^&]*))?");

  private int urlStatus = ConnectorMessageCode.SUCCESS;
  private String url = null;
  private String connectorName = null;
  private String docid = null;

  ParsedUrl(String urlparam) {

    url = urlparam;
    Matcher matcher = URL_PATTERN.matcher(url);
    boolean found = matcher.find();

    if (!found) {
      urlStatus = ConnectorMessageCode.RESPONSE_NULL_CONNECTOR;
      return;
    } else {
      try {
        connectorName = matcher.group(1);
      } catch (IllegalStateException e) {
        // just leave the connectorName null - we'll catch the error later
      }
      try {
        docid = matcher.group(2);
      } catch (IllegalStateException e) {
        // just leave the docid null - we'll catch the error later
      }
    }

    if (docid == null || docid.length() < 1) {
      urlStatus = ConnectorMessageCode.RESPONSE_NULL_DOCID;
    }
    if (connectorName == null || connectorName.length() < 1) {
      urlStatus = ConnectorMessageCode.RESPONSE_NULL_CONNECTOR;
    }
  }

  public String getConnectorName() {
    return connectorName;
  }

  public String getDocid() {
    return docid;
  }

  public int getStatus() {
    return urlStatus;
  }

  public String getUrl() {
    return url;
  }
}
