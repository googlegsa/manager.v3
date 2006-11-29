package com.google.enterprise.connector.servlet;

/**
 * Error/message codes returning from Connector Manager to GSA AdminConsole
 * 
 */
public class ConnectorMessageCode {
  public static final int SUCCESS = 0;

  /**
   * IDs from 5210 to 6000 are reserved for GSA / Connector Manager
   *
   */
  public static final int RESPONSE_HTTP_FAIL = 5210;
  public static final int RESPONSE_EMPTY_REQUEST = 5211;
  public static final int RESPONSE_EMPTY_RESPONSE = 5212;
  public static final int RESPONSE_EMPTY_NODE = 5213;
  public static final int RESPONSE_STATUS_ID_NON_INTEGER = 5214;
  public static final int RESPONSE_PARAM_MISSING = 5215;
  public static final int RESPONSE_NULL_CONNECTOR = 5216;
  public static final int RESPONSE_NULL_CONNECTOR_TYPE = 5217;
  public static final int RESPONSE_NULL_CONNECTOR_STATUS = 5218;
  public static final int RESPONSE_NULL_FORM_SNIPPET = 5219;
  public static final int RESPONSE_NULL_DOCID = 5220;
  public static final int RESPONSE_EMPTY_CONFIG_DATA = 5221;
  public static final int RESPONSE_NULL_RESOURCE = 5222;
  public static final int RESPONSE_AUTHZ_DOCID_MISMATCH = 5223;

  public static final int EXCEPTION_XML_PARSING = 5300;
  public static final int EXCEPTION_CONNECTOR_MANAGER = 5301;
  public static final int EXCEPTION_CONNECTOR_EXISTS = 5302;
  public static final int EXCEPTION_CONNECTOR_NOT_FOUND = 5303;
  public static final int EXCEPTION_CONNECTOR_TYPE_NOT_FOUND = 5304;
  public static final int EXCEPTION_INSTANTIATOR = 5305;
  public static final int EXCEPTION_PERSISTENT_STORE = 5306;
  public static final int EXCEPTION_THROWABLE = 5307;
  
  public static final int EXCEPTION_IO = 5308;
  public static final int EXCEPTION_MALFORMED_URL = 5309;
  public static final int EXCEPTION_NUMBER_FORMAT = 5310;
  public static final int EXCEPTION_SAX = 5311;
  public static final int EXCEPTION_GOOGLE = 5312;


  public static final int DUPLICATE_CONNECTOR_MANAGER_ID = 5400;
  public static final int INVALID_CONNECTOR_MANAGER_NAME = 5401;
  public static final int INVALID_CONNECTOR_MANAGER_URL = 5402;
  public static final int MISMATCHED_CONNECTOR_MANAGER = 5403;
  public static final int UNKNOWN_CONNECTOR_MANAGER_ID = 5404;

  public static final int CANNOT_ADD_INTERNAL_CONNECTOR_MANAGER = 5405;
  public static final int ATTEMPT_TO_CHANGE_INTERNAL_MANAGER = 5406;
  public static final int ATTEMPT_TO_DELETE_INTERNAL_MANAGER = 5407;
  public static final int ATTEMPT_TO_DELETE_ACTIVE_MANAGER = 5408;

  public static final int DUPLICATE_CONNECTOR_NAME = EXCEPTION_CONNECTOR_EXISTS;
  public static final int INVALID_CONNECTOR_NAME = 5409;
  public static final int INVALID_IP_ADDRESS = 5410;
  public static final int UNKNOWN_CONNECTOR_NAME = EXCEPTION_CONNECTOR_NOT_FOUND;

  private int messageId;
  private String message;
  private Object[] params;

  public ConnectorMessageCode() {
    this.messageId = SUCCESS;
    this.message = null;
    this.params = null;
  }

  public ConnectorMessageCode(int messageId) {
    this.messageId = messageId;
    this.message = null;
    this.params = null;
  }

  public ConnectorMessageCode(
      int messageId, String message, Object[] params) {
    this.messageId = messageId;
    this.message = message;
    this.params = params;
  }

  public int getMessageId() {
    return messageId;
  }

  public void setMessageId(int messageId) {
    this.messageId = messageId;
  }

  public Object[] getParams() {
    return params;
  }

  public void setParams(Object[] params) {
    this.params = params;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
