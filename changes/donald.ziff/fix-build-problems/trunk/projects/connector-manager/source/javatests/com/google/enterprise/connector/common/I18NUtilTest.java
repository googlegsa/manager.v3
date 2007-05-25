/**
 * 
 */
package com.google.enterprise.connector.common;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * @author ziff
 *
 */
public class I18NUtilTest extends TestCase {

	/**
	 * Test method for {@link com.google.enterprise.connector.common.I18NUtil#getLocaleFromStandardLocaleString(java.lang.String)}.
	 */
	public void testGetLocaleFromStandardLocaleString() {
		doOneLocaleTest("en",new Locale("en"));
		doOneLocaleTest("en",Locale.ENGLISH);
		doOneLocaleTest("fr",new Locale("fr"));
		doOneLocaleTest("fr_CA",new Locale("fr","CA"));		
		doOneLocaleTest("fr_CA",Locale.CANADA_FRENCH);		
		doOneLocaleTest("fr-CA",Locale.CANADA_FRENCH);		
		doOneLocaleTest("FR-ca",Locale.CANADA_FRENCH);
		doOneLocaleTest("",Locale.getDefault());		
	}

	private void doOneLocaleTest(String localeString, Locale expectedLocale) {
		Locale l = I18NUtil.getLocaleFromStandardLocaleString(localeString);
		assertEquals(expectedLocale, l);
	}
}
