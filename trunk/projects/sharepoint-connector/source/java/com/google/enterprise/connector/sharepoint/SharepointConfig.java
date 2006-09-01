package com.google.enterprise.connector.sharepoint;

import connector.ClientContext;

import org.apache.commons.configuration.ConfigurationException;

public class SharepointConfig {
  String URL = "url", PROXY_SERVER = "proxy_server", PROXY_PORT = "proxy_port",
  AUTH_TYPE = "auth_type";
  public String getConfig(String lang) 
  {
    try
    {
      String cfg = "";
      cfg += row (col(URL, text(URL, ClientContext.getServers()[0])));
      return cfg;
    }catch(ConfigurationException e)
    {
      return "";
    }
  }

  private String row(String input)
  {
    return "<tr>" + input + "</tr>"; 
  }

  private String col(String name, String input)
  {
    return "<td>" + getLabel(name) + "</td>" + "\n<td>" + input + "</td>"; 
  }

  public String setConfig(String lang)
  {
    return "";
  }
  
  public String text(String name, String value)
  {
    return "<input type=text name=\"" + name + "\" value=\"" + value + "\"";
  }

  private String getLabel(String name)
  {
    return name;
  }
}
