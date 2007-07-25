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

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.spi.old.SimpleProperty;
import com.google.enterprise.connector.spi.old.SimpleValue;
import com.google.enterprise.connector.spi.old.Value;
import com.google.enterprise.connector.spi.old.ValueType;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author ziff
 * 
 */
public class SimplePropertyTest extends TestCase {

	/**
	 * Test method for
	 * {@link com.google.enterprise.connector.spi.old.SimpleProperty#getName()}.
	 * 
	 * @throws RepositoryException
	 */
	public void testGetName() throws RepositoryException {
		Assert.assertEquals("name", new SimpleProperty("name", "value")
				.getName());
	}

	/**
	 * Test method for
	 * {@link com.google.enterprise.connector.spi.old.SimpleProperty#getValue()}.
	 * 
	 * @throws RepositoryException
	 * @throws IllegalArgumentException
	 */
	public void testGetValue() throws IllegalArgumentException,
			RepositoryException {
		Assert.assertEquals("value", new SimpleProperty("name", "value")
				.getValue().getString());
		SimpleProperty simpleProperty = new SimpleProperty("name", Arrays
				.asList(new SimpleValue[] { new SimpleValue(ValueType.STRING,
						"foo") }));
		Assert.assertEquals("foo", simpleProperty.getValue().getString());
	}

	/**
	 * Test method for
	 * {@link com.google.enterprise.connector.spi.old.SimpleProperty#getValues()}.
	 * 
	 * @throws RepositoryException
	 * @throws IllegalArgumentException
	 */
	public void testGetValues() throws IllegalArgumentException,
			RepositoryException {
		{
			// test property created with a singleton value
			SimpleProperty simpleProperty = new SimpleProperty("name", "value");
			Iterator values = simpleProperty.getValues();
			Assert.assertTrue(values.hasNext());
			Value value = (Value) values.next();
			Assert.assertEquals("value", value.getString());
			Assert.assertFalse(values.hasNext());
		}
		{
			// test property created with null value list
			List valueList = null;
			SimpleProperty simpleProperty = new SimpleProperty("name",
					valueList);
			Iterator values = simpleProperty.getValues();
			Assert.assertFalse(values.hasNext());

		}
	}

}
