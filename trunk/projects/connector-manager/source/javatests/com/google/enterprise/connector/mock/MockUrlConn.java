// Copyright 2006 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.pusher.UrlConn;

import java.io.IOException;
import java.net.URL;


public class MockUrlConn implements UrlConn {
  
  private URL url;
  private String data;
  
  public MockUrlConn() {   
  }
  
  public MockUrlConn(URL url, String data) {
    this.url = url;
    this.data = data;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
  
  public String sendData() throws IOException {
    return "Mock response";
  }

}
