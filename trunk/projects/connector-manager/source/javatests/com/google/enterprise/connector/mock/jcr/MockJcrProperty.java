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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepositoryProperty;
import com.google.enterprise.connector.mock.MockRepositoryProperty.PropertyType;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

/**
 * MockJcrProperty implements the corresponding JCR interface, with
 * these limitations:
 * <ul>
 * <li> This is a "level 1" (read-only) implementation. All level 2
 * (side-effecting) calls throw UnsupportedOperation exceptions. These are
 * grouped at the bottom of the class implementation.
 * <li> Some level 1 calls are not implemented because they will never be used
 * by our connector infrastructure. Eventually, these will be documented as part
 * of framework documentation. In this implementation, they also throw
 * UnsupportedOperation exceptions. These are grouped above the level 2 calls.
 * <li> Some level 1 calls are not currently needed by our implementation, but
 * may be soon. These are marked with todos and throw UnsupportedOperation
 * exceptions.
 * </ul>
 */
public class MockJcrProperty implements Property {

  MockRepositoryProperty p;

  public MockJcrProperty(MockRepositoryProperty p) {
    this.p = p;
  }

  public String getName() throws RepositoryException {
    return p.getName();
  }

  public Value getValue() throws ValueFormatException, RepositoryException {
    if (p.isRepeating()) {
      throw new ValueFormatException("Can't call single-valued accessor "
          + "on repeating-valued property");
    }
    return new MockJcrValue(p);
  }

  public String getString() throws ValueFormatException, RepositoryException {
    return p.getValue();
  }

  public long getLong() throws ValueFormatException, RepositoryException {
    Value v = this.getValue();
    return v.getLong();
  }

  public double getDouble() throws ValueFormatException, RepositoryException {
    Value v = this.getValue();
    return v.getDouble();
  }

  public Calendar getDate() throws ValueFormatException, RepositoryException {
    Value v = this.getValue();
    return v.getDate();
  }

  public boolean getBoolean() throws ValueFormatException, RepositoryException {
    Value v = this.getValue();
    return v.getBoolean();
  }

  public Value[] getValues() throws ValueFormatException, RepositoryException {
    String[] values = p.getValues();
    Value[] vs = makeValueArray(p.getType(), values);
    return vs;
  }

  public InputStream getStream() throws ValueFormatException,
      RepositoryException {
    Value v = this.getValue();
    return v.getStream();
  }

  public Node getNode() throws ValueFormatException, RepositoryException {
    // TODO(ziff): perhaps implement this later
    throw new UnsupportedOperationException();
  }

  public long getLength() throws ValueFormatException, RepositoryException {
    // TODO(ziff): perhaps implement this later
    throw new UnsupportedOperationException();
  }

  public long[] getLengths() throws ValueFormatException, RepositoryException {
    // TODO(ziff): perhaps implement this later
    throw new UnsupportedOperationException();
  }

  public PropertyDefinition getDefinition() throws RepositoryException {
    // TODO(ziff): perhaps implement this later
    throw new UnsupportedOperationException();
  }

  public int getType() throws RepositoryException {
    return MockJcrValue.MockRepositoryTypeToJCRType(p.getType());
  }

  public String getPath() throws RepositoryException {
    // TODO(ziff): perhaps implement this later
    throw new UnsupportedOperationException();
  }

  // The following methods are JCR level 1 - but we do not anticipate using them

  public Item getAncestor(int arg0) throws ItemNotFoundException,
      AccessDeniedException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Node getParent() throws ItemNotFoundException, AccessDeniedException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public int getDepth() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public Session getSession() throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public boolean isNode() {
    throw new UnsupportedOperationException();
  }

  public boolean isNew() {
    throw new UnsupportedOperationException();
  }

  public boolean isModified() {
    throw new UnsupportedOperationException();
  }

  public boolean isSame(Item arg0) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void accept(ItemVisitor arg0) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  // The following methods are JCR level 2 - these would never be needed

  public void setValue(Value arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(Value[] arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(String arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(String[] arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(InputStream arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(long arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(double arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(Calendar arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(boolean arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void setValue(Node arg0) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void save() throws AccessDeniedException, ItemExistsException,
      ConstraintViolationException, InvalidItemStateException,
      ReferentialIntegrityException, VersionException, LockException,
      NoSuchNodeTypeException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void refresh(boolean arg0) throws InvalidItemStateException,
      RepositoryException {
    throw new UnsupportedOperationException();
  }

  public void remove() throws VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  private Value[] makeValueArray(PropertyType type, String[] values) {
    Value[] result = new Value[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = new MockJcrValue(type, values[i]);
    }
    return result;
  }
}
