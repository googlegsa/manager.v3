package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.ValueType;

import java.io.InputStream;
import java.util.Calendar;

public class SharepointValue implements Value {

  Object obj;
  ValueType type;

  public SharepointValue(Object obj) {
    this.obj = obj;
  }

  public SharepointValue(Object obj, ValueType type) {
    this.obj = obj;
    this.type = type;
  }

  public String getString() throws IllegalArgumentException,
      RepositoryException {
    // TODO Auto-generated method stub
    return obj.toString();
  }

  public InputStream getStream() throws IllegalStateException,
      RepositoryException {
    // TODO Auto-generated method stub
    throw new RepositoryException("getStream Not supported");
  }

  public long getLong() throws IllegalArgumentException, IllegalStateException,
      RepositoryException {
    // TODO Auto-generated method stub
    return Long.valueOf(obj.toString()).longValue();
  }

  public double getDouble() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    // TODO Auto-generated method stub
    return Double.valueOf(obj.toString()).doubleValue();
  }

  public Calendar getDate() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    // TODO Auto-generated method stub
    return Calendar.getInstance();
  }

  public boolean getBoolean() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    // TODO Auto-generated method stub
    return Boolean.valueOf(obj.toString()).booleanValue();
  }

  public ValueType getType() throws RepositoryException {
    // TODO Auto-generated method stub
    if (type != null) {
      return type;
    }
    //the trial must be in certain order, String must be the last
    try
    {
      getDate();
      return ValueType.DATE;
    }catch(Exception e)
    {
    }
    try
    {
      getLong();
      return ValueType.LONG;
    }catch(Exception e)
    {
    }
    try
    {
      getDouble();
      return ValueType.DOUBLE;
    }catch(Exception e)
    {
    }
    try
    {
      getBoolean();
      return ValueType.BOOLEAN;
    }catch(Exception e)
    {
    }
    return ValueType.STRING;
  }
}
