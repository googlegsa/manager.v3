// Copyright 2009 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.logging;

import junit.framework.TestCase;

import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Unit Test for logging LayoutPattern.
 */
public class LayoutPatternTest extends TestCase {
  private LogRecord logRecord;
  private String logMessage =
      "That's one small step for [a] man, one giant leap for mankind.";
  private String className = "gov.nasa.apollo.lander.LunarModule";
  private String methodName = "moonWalk";
  private long threadId;
  private String threadName = "Armstrong";
  private SimpleFormatter simpleFormatter = new SimpleFormatter();

  @Override
  protected void setUp() throws Exception {
    GregorianCalendar cal = new GregorianCalendar();
    cal.set(1969, 7, 20, 20, 17, 40);

    logRecord = new LogRecord(Level.INFO, logMessage);
    logRecord.setSequenceNumber(11);
    logRecord.setMillis(cal.getTimeInMillis());
    logRecord.setSourceClassName(className);
    logRecord.setSourceMethodName(methodName);

    threadId = Thread.currentThread().getId();
    logRecord.setThreadID((int)(threadId));
    Thread.currentThread().setName(threadName);
  }

  @Override
  protected void tearDown() throws Exception {
    MDC.remove();
    NDC.remove();
  }

  // Check that formatting via the pattern generates the expected output.
  private void checkFormat(String pattern, String expected) {
    LayoutPattern layout = new LayoutPattern(pattern);
    String output = layout.format(logRecord);
    // System.out.println("pattern: \"" + pattern + "\"   expected: \""
    //                    + expected + "\"   output: \"" + output + "\"");
    assertEquals(expected, output);
  }

  /** Test constant string.  Should go through unmolested. */
  public void testConstantString() {
    String pattern = "The quick brown fox jumped over the lazy dog's back.";
    checkFormat(pattern, pattern);
  }

  /** Test empty string.  Should go through unmolested. */
  public void testEmptyString() {
    checkFormat("", "");
  }

  /** Just a single format element. */
  public void testMessageString() {
    checkFormat("%m", logMessage);
  }

  /** Just a single format element, with some additional text. */
  public void testMessageString1() {
    String pattern = "Neil said, %m";
    String expected = "Neil said, " + logMessage;
    checkFormat(pattern, expected);
  }

  /** Just a single format element, with some additional text. */
  public void testMessageString2() {
    String pattern = "%m - Neil";
    String expected = logMessage + " - Neil";
    checkFormat(pattern, expected);
  }

  /** Just a single format element, with some surrounding text. */
  public void testMessageString3() {
    String pattern = "Neil said, %m Buzz was silent.";
    String expected = "Neil said, " + logMessage + " Buzz was silent.";
    checkFormat(pattern, expected);
  }

  /** Two adjacent format elements. */
  public void testTwoAdjacent() {
    checkFormat("%m%m", logMessage + logMessage);
  }

  /** Test '%%' is quoted percent. */
  public void testPercentPercent() {
    checkFormat("%%", "%");
  }

  /** Test '%%' is quoted percent, and some additional text. */
  public void testPercentPercent1() {
    checkFormat("50%%", "50%");
  }

  /** Test '%%' is quoted percent, and some additional text. */
  public void testPercentPercent2() {
    checkFormat("%% percent", "% percent");
  }

  /** Test '%%' is quoted percent, and some surrounding text. */
  public void testPercentPercent3() {
    checkFormat("50%% off!", "50% off!");
  }

  /** Test '%%%%' is quoted percent, should return '%%'. */
  public void testPercentPercent4() {
    checkFormat("%%%%", "%%");
  }

  /**
   * Test '%%' is quoted percent, followed by what looks like a
   * conversion char.
   */
  public void testPercentPercent5() {
    checkFormat("%%C", "%C");
  }

  /**
   * Test '%%' is quoted percent, followed by what looks like a
   * modifier + conversion char.
   */
  public void testPercentPercent6() {
    checkFormat("%%-5C", "%-5C");
  }

  /** Test '%%' is quoted percent, followed by valid conversion char. */
  public void testPercentPercent7() {
    checkFormat("%%%M", "%moonWalk");
  }

  /** Test '%%' is quoted percent, followed by modified conversion char. */
  public void testPercentPercent8() {
    checkFormat("%%%-10M", "%moonWalk  ");
  }

  /** Test '%%' is quoted percent, followed by valid conversion char. */
  public void testPercentPercent9() {
    checkFormat("%%-5C", "%-5C");
  }

  /** Test unimplemented Conversion, should be silently dropped. */
  public void testUnimplemented() {
    checkFormat("%F", "");
  }

  /** Test unimplemented Conversion, should be silently dropped. */
  public void testUnimplemented1() {
    checkFormat("Not %F implemented", "Not  implemented");
  }

  /** Test thread id and name. */
  public void testThreadIdName() {
    String pattern = "%T - %t";
    String expected = threadId + " - " + threadName;
    checkFormat(pattern, expected);
  }

  /** Test className and methodName. */
  public void testClassAndMethodName() {
    String pattern = "%C - %M";
    String expected = className + " - " + methodName;
    checkFormat(pattern, expected);
  }

  /** Test %n newline. */
  public void testNewLine() {
    String pattern = "line1%nline2";
    String expected = "line1" + System.getProperty("line.separator") + "line2";
    checkFormat(pattern, expected);
  }

  /** Test SequenceNumber. */
  public void testSequenceNumber() {
    checkFormat("Apollo %N", "Apollo 11");
  }

  /** Test Default Date format. */
  public void testDefaultDate() {
    String pattern = "%d";
    String expected = "1969-08-20 20:17:40";
    checkFormat(pattern, expected);
  }

  /** Test Default Date format with additional text. */
  public void testDefaultDate1() {
    String pattern = "%d moon landing";
    String expected = "1969-08-20 20:17:40 moon landing";
    checkFormat(pattern, expected);
  }

  /** Test non-default Date format. */
  public void testFormattedDate() {
    String pattern = "%d{yyyy-MM-dd'T'HH:mm'Z'}";
    String expected = "1969-08-20T20:17Z";
    checkFormat(pattern, expected);
  }

  /** Test non-default Date format with some text. */
  public void testFormattedDate1() {
    String pattern = "%d{yyyy-MM-dd'T'HH:mm'Z'} touch down";
    String expected = "1969-08-20T20:17Z touch down";
    checkFormat(pattern, expected);
  }

  /** Test invalid Date format. */
  public void testInvalidDateFormat() {
    String pattern = "%d{xyzzy}";
    String expected = "xyzzy";
    try {
      checkFormat(pattern, expected);
      fail("Expected IllegalArgumentException to be thrown.");
    } catch (IllegalArgumentException expect) {
      assertTrue(expect.getMessage().startsWith("Illegal pattern character"));
    }
  }

  /** Test partial ClassName with %C{n}. Just leaf classname. */
  public void testPartialClassName() {
    checkFormat("%C{1}", "LunarModule");
  }

  /** Test partial ClassName with %C{n}.  4 segments. */
  public void testPartialClassName1() {
    checkFormat("%C{4}", "nasa.apollo.lander.LunarModule");
  }

  /** Test partial ClassName with %C{n}.  5 segments - the exact size. */
  public void testPartialClassName2() {
    checkFormat("%C{5}", className);
  }

  /** Test partial ClassName with %C{n}.  Should give the whole thing. */
  public void testPartialClassName3() {
    checkFormat("%C{10}", className);
  }

  /** Test partial ClassName with %C{n}.  Should give the whole thing. */
  public void testPartialClassName4() {
    checkFormat("%C{0}", className);
  }

  /** Test format Modifier minWidth. */
  public void testModifierMinWidth() {
    checkFormat("%10M", "  moonWalk");
  }

  /** Test format Modifier minWidth. */
  public void testModifierMinWidth2() {
    checkFormat("%5M", "moonWalk");
  }

  /** Test format Modifier minWidth - left justified. */
  public void testModifierLeftJustified() {
    checkFormat("%-10M", "moonWalk  ");
  }

  /** Test format Modifier minWidth - left justified. */
  public void testModifierLeftJustified1() {
    checkFormat("%-5M", "moonWalk");
  }

  /** Test format Modifier maxWidth. */
  public void testModifierMaxWidth() {
    checkFormat("%.33m", "That's one small step for [a] man");
  }

  /** Test format Modifier maxWidth. */
  public void testModifierMaxWidth1() {
    checkFormat("%.133m", logMessage);
  }

  /** Test format Modifier minWidth:maxWidth. */
  public void testModifierMinMaxWidth() {
    checkFormat("%10.133m", logMessage);
  }

  /** Test format Modifier minWidth:maxWidth. */
  public void testModifierMinMaxWidth1() {
    checkFormat("%10.133M", "  moonWalk");
  }

  /** Test format Modifier minWidth:maxWidth. */
  public void testModifierMinMaxWidth2() {
    checkFormat("%5.133M", "moonWalk");
  }

  /** Test format Modifier minWidth:maxWidth. */
  public void testModifierMinMaxWidth3() {
    checkFormat("%5.33m", "That's one small step for [a] man");
  }

  /** Test format Modifier -minWidth:maxWidth. */
  public void testModifierMinMaxWidthLeftJustified() {
    checkFormat("%-10.133m", logMessage);
  }

  /** Test format Modifier -minWidth:maxWidth. */
  public void testModifierMinMaxWidthLeftJustified1() {
    checkFormat("%-10.133M", "moonWalk  ");
  }

  /** Test format Modifier -minWidth:maxWidth. */
  public void testModifierMinMaxWidthLeftJustified2() {
    checkFormat("%-5.133M", "moonWalk");
  }

  /** Test format Modifier -minWidth:maxWidth. */
  public void testModifierMinMaxWidthLeftJustified3() {
    checkFormat("%-5.33m", "That's one small step for [a] man");
  }

  /** Test format Modifier 0 minWidth. */
  public void testModifierMinWidth0() {
    checkFormat("%0M", "moonWalk");
  }

  /** Test format Modifier 0 maxWidth. */
  public void testModifierMaxWidth0() {
    checkFormat("%.0M", "");
  }

  /** Test Wrapped formatter - %f */
  public void testWrappedFormatter() {
    String simpleOutput = simpleFormatter.format(logRecord);
    LayoutPattern layout = new LayoutPattern("%-4N [%T %t] %f", simpleFormatter);
    String output = layout.format(logRecord);
    String expected = "11   [" + threadId + " Armstrong] " + simpleOutput;
    assertEquals(output, expected);
  }

  /** Test Wrapped formatter - %m */
  public void testWrappedFormatter1() {
    LayoutPattern layout = new LayoutPattern("%-4N [%t] %m", simpleFormatter);
    String output = layout.format(logRecord);
    String expected = "11   [Armstrong] " + logMessage;
    assertEquals(output, expected);
  }

  /** Replicate SimpleFormatter. */
  public void testReplicateSimpleFormatter() {
    String simpleOutput = simpleFormatter.format(logRecord);

    // Try to replicate the SimpleFormatter layout.
    String pattern = "%d{MMM dd, yyyy h:mm:ss a} %C %M%n%p: %m%n";

    LayoutPattern layout = new LayoutPattern(pattern);
    String output = layout.format(logRecord);

    assertEquals(output, simpleOutput);
  }

  /** Test NDC logging with %x. */
  public void testNDC() {
    String pattern = "[%x] %M";
    // No context should be empty string.
    checkFormat(pattern, "[] moonWalk");

    NDC.push("Neil Armstrong");
    checkFormat(pattern, "[Neil Armstrong] moonWalk");

    NDC.push("Buzz Aldrin");
    checkFormat(pattern, "[Buzz Aldrin] moonWalk");

    NDC.pop();
    checkFormat(pattern, "[Neil Armstrong] moonWalk");

    NDC.clear();
    checkFormat(pattern, "[] moonWalk");
  }

  /** Test MDC logging with %X. */
  public void testMDC() {
    String pattern = "[%X{Astronaut}] %M";
    // No context should be empty string.
    checkFormat(pattern, "[] moonWalk");

    MDC.put("Astronaut", "Neil Armstrong");
    checkFormat(pattern, "[Neil Armstrong] moonWalk");

    MDC.put("Astronaut", "Buzz Aldrin");
    checkFormat(pattern, "[Buzz Aldrin] moonWalk");

    MDC.put("Astronaut", "Neil Armstrong");
    checkFormat(pattern, "[Neil Armstrong] moonWalk");

    MDC.remove("Astronaut");
    checkFormat(pattern, "[] moonWalk");

    MDC.clear();
  }

  /** Test MDC logging with %X and multiple key/value pairs. */
  public void testMDC2() {
    String pattern = "[%X{Astronaut} %X{WingMan}] %M";
    // No context should be empty string.
    checkFormat(pattern, "[ ] moonWalk");

    MDC.put("Astronaut", "Neil Armstrong");
    checkFormat(pattern, "[Neil Armstrong ] moonWalk");

    MDC.put("WingMan", "Buzz Aldrin");
    checkFormat(pattern, "[Neil Armstrong Buzz Aldrin] moonWalk");

    MDC.remove("Astronaut");
    checkFormat(pattern, "[ Buzz Aldrin] moonWalk");

    MDC.remove("WingMan");
    checkFormat(pattern, "[ ] moonWalk");

    MDC.clear();
  }
}