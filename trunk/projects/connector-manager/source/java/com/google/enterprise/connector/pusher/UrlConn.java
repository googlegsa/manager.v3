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

package com.google.enterprise.connector.pusher;

import java.io.IOException;
import java.net.URL;

/**
 * Interface for a url connection. This takes in data and url to which the 
 * data needs to be sent.
 */
public interface UrlConn {

  public URL getUrl();
  public void setUrl(URL url);
  public String getData();
  public void setData(String data);
  
  /**
   * Open a connection to this url and sends data.
   * @return response from the server.
   * @throws IOException
   */
  public String sendData()throws IOException;
  
}
