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

import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedUrl {
  // TODO (bmj): This discards fragments at the end of the URLs,
  // which possibly makes Issue 214 (b/6514016) worse.
  private static final Pattern GOOGLECONNECTOR_URL_PATTERN =
      Pattern.compile("^" + ServletUtil.PROTOCOL + "([^./]*)(?:[^/]*)?"
          + "(?:/[dD][oO][cC]\\?(?:[^&]*&)*[dD][oO][cC][iI][dD]=([^&#]*))?");

  private static final Pattern RETRIEVER_URL_PATTERN =
      Pattern.compile("^http.+/getDocumentContent\\?[cC][oO][nN][nN][eE][cC]"
          + "[tT][oO][rR][nN][aA][mM][eE]=([^&]*)&[dD][oO][cC][iI][dD]="
          + "([^&#]*)");
  // TODO: We should handle the case where the query parameters are swapped.

  private int urlStatus = ConnectorMessageCode.SUCCESS;
  private String url = null;
  private String connectorName = null;
  private String docid = null;

  ParsedUrl(String urlparam) {
    url = urlparam;
    Matcher matcher = GOOGLECONNECTOR_URL_PATTERN.matcher(url);
    boolean found = matcher.find();

    if (found) {
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
    } else {
      // TODO: Use java.net.URI instead of URLDecoder. Better we should write
      // our own RFC 3986 compliant decoder instead.
      matcher = RETRIEVER_URL_PATTERN.matcher(url);
      found = matcher.find();
      if (found) {
        try {
          connectorName = URLDecoder.decode(matcher.group(1), "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
          // Can't happen with UTF-8.
        } catch (IllegalStateException e) {
          // just leave the connectorName null - we'll catch the error later
        }
        try {
          docid = URLDecoder.decode(matcher.group(2), "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
          // Can't happen with UTF-8.
        } catch (IllegalStateException e) {
          // just leave the docid null - we'll catch the error later
        }
      }
    }

    if (!found || Strings.isNullOrEmpty(connectorName)) {
      urlStatus = ConnectorMessageCode.RESPONSE_NULL_CONNECTOR;
    } else if (Strings.isNullOrEmpty(docid)) {
      urlStatus = ConnectorMessageCode.RESPONSE_NULL_DOCID;
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
