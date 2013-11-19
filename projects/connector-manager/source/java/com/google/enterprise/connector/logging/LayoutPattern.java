// Copyright (C) 2009 Google Inc.
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Log message layout pattern for text (non-XML) format messages. It is
 * a flexible layout configurable with pattern string.  The pattern
 * string syntax is based upon the one used by the Log4j PatternLayout
 * class.  However, since this class is built on top of java.util.logging,
 * not all of the log4j conversion patterns are supported.
 *
 * <p>The goal of this class is to {@link #format format} a
 * {@link java.util.logging.LogRecord} and return the results as a String.
 * The results depend on the <em>conversion pattern</em> used.
 *
 * <p>A conversion pattern is composed of literal text and format
 * control expressions called <em>conversion specifiers</em>.
 *
 * <p>Each conversion specifier starts with a percent sign (%) and is
 * followed by optional <em>format modifiers</em> and a <em>conversion
 * character</em>.  The conversion character specifies the type of
 * data, e.g.  log message, level, date, thread name. The format
 * modifiers control such things as field width, padding, left and
 * right justification.
 *
 * <p>For example, given the layout conversion pattern
 * <b>"%-4p&nbsp;[%t]:&nbsp;%m%n"</b>, then the code:
 * <pre>
     Logger logger = getClass().getLogger();
     logger.info("Message 1");
     logger.warn("Message 2");
  </pre>
   would yield the output:
  <pre>
      INFO [main]: Message 1
      WARN [main]: Message 2
  </pre>
 *
 * The recognized conversion characters are:
 *
 * <p> <table border="0" CELLPADDING="8">
 *
 * <tr><td align="center" valign="top"><b>C</b></td>
 * <td>Used to output the class name of the caller
 * issuing the logging request. This conversion specifier
 * can be optionally followed by <em>precision specifier</em>,
 * that is a decimal constant in brackets.
 *
 * <p>If a precision specifier is given, then only the corresponding
 * number of right-most components of the class name will be
 * printed. By default the class name is output in fully qualified form.
 *
 * <p>For example, for the class name "com.google.enterprise.SomeClass",
 * the pattern <b>%C{1}</b> will output "SomeClass".
 * </td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>d</b></td>
 * <td>Used to output the date of the logging event. The date conversion
 * specifier may be followed by a <em>date format specifier</em> enclosed
 * between braces.  For example, <b>%d{HH:mm:ss,SSS}</b> or
 * <b>%d{dd&nbsp;MMM&nbsp;yyyy&nbsp;HH:mm:ss,SSS}</b>.  If no
 * date format specifier is given, then ISO8601 format is assumed.
 *
 * <p>The date format specifier accepts the same syntax as the
 * time pattern string of the {@link java.text.SimpleDateFormat}.
 * </td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>f</b></td>
 * <td>Expands to the output returned by the Base Formatter for this record.
 * This returns the fully formatted output of the underlying Formatter,
 * for instance, the output of
 * <code>java.util.logging.SimpleFormatter.format()</code>.
 *
 * <p>This can be useful when embellishing Base Formatter output with
 * diagnostic information.  For instance,  a layout pattern like:
 * <b>"[%T&nbsp;%t&nbsp;%X{connectorName}]&nbsp;%f"</b> would prepend a
 * formatted log message with the ThreadID, ThreadName, and ConnectorName.
 * </td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>m</b></td>
 * <td>Outputs the application supplied message associated with
 * the logging event.</td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>M</b></td>
 * <td>Outputs the method name where the logging request was issued.</td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>n</b></td>
 * <td>Outputs the platform dependent line separator character or characters.
 * </td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>p</b></td>
 * <td>Outputs the priority of the logging event. This
 * cooresponds to the java.util.logging.Level of the LogRecord.</td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>t</b></td>
 * <td>Outputs the name of the thread that generated the
 * logging event.</td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>T</b></td>
 * <td>Outputs the Id of the thread that generated the
 * logging event.</td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>x</b></td>
 * <td>Outputs the {@link NDC} (nested diagnostic context) associated
 * with the thread that generated the logging event.
 * </td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>X</b></td>
 * <td>Outputs the {@link MDC} (mapped diagnostic context) associated
 * with the thread that generated the logging event. The <b>X</b>
 * conversion character <em>must</em> be followed by the key for the
 * map placed between braces, as in <b>%X{clientNumber}</b> where
 * <code>clientNumber</code> is the key. The value in the MDC
 * corresponding to the key will be output.
 * </td>
 * </tr>
 *
 * <tr><td align="center" valign="top"><b>%</b></td>
 * <td>The sequence %% outputs a single percent sign.</td>
 * </tr>
 * </table>
 *
 * <p>By default the relevant information is output as is. However,
 * with the aid of format modifiers it is possible to change the
 * minimum field width, the maximum field width, and justification.
 *
 * <p>The optional format modifier is placed between the percent sign
 * and the conversion character.
 *
 * <p>The first optional format modifier is the <em>left justification
 * flag</em> which is just the minus (-) character. Then comes the
 * optional <em>minimum field width</em> modifier. This is a decimal
 * constant that represents the minimum number of characters to
 * output. If the data item requires fewer characters, it is padded on
 * either the left or the right until the minimum width is
 * reached. The default is to pad on the left (right justify) but you
 * can specify right padding with the left justification flag. The
 * padding character is space. If the data item is larger than the
 * minimum field width, the field is expanded to accommodate the
 * data. The value is never truncated.
 *
 * <p>This behavior can be changed using the <em>maximum field
 * width</em> modifier which is designated by a period followed by a
 * decimal constant. If the data item is longer than the maximum
 * field, then the extra characters are removed from the end.
 */
public class LayoutPattern {
  private static final String NL = System.getProperty("line.separator");

  private FormatElement[] formatElements;
  private Formatter baseFormatter;

  /**
   * Construct a layout that uses the supplied layout conversion pattern.
   *
   * @param format logging conversion format String
   */
  public LayoutPattern(String format) {
    this(format, null);
  }

  /**
   * Construct a layout that uses the supplied layout conversion pattern.
   * It may also make use of an underlying Formatter for some of its work.
   *
   * @param format logging conversion format String
   * @param baseFormatter underlying Formatter that this may use.
   */
  public LayoutPattern(String format, Formatter baseFormatter) {
    this.baseFormatter = baseFormatter;
    parse(format);
  }

  /**
   * Compile the format string into an array format elements
   * that can be more quickly evaluated when formatting log entries.
   * An example format string might look like:<pre>
   *   "[%T %t %X{ConnectorName}] %d %C %M%n%p: %m%n"</pre>
   *
   * @param format logging conversion format String
   */
  private void parse(String format) {
    ArrayList<FormatElement> elems = new ArrayList<FormatElement>();
    if (format != null && format.length() > 0) {
      String[] strs = format.split("%", -1);
      boolean inConversion = false;
      for (int i = 0; i < strs.length ; i++) {
        String str = strs[i];
        int strLen = str.length();

        // If not in a conversion, this token is pure string constant.
        if (!inConversion) {
          if (strLen > 0) {
            elems.add(new StringElement(str));
          }
          inConversion = true;
          continue;
        }

        // %% - quoted percent sign.
        if (strLen == 0) {
            elems.add(new StringElement("%"));
            inConversion = false;
            continue;
        }

        int skipCount = 0;
        // Pull out format modifier.
        while ("-0123456789.".indexOf(str.charAt(skipCount)) >= 0) {
          skipCount++;
        }
        String modifier = (skipCount > 0) ? str.substring(0, skipCount) : null;
        FormatElement elem = null;
        String arg = null;
        switch (str.charAt(skipCount++)) {
          case 'C': if ((arg = getArg(str, skipCount, strLen)) != null) {
                      elem = new ClassNameElement(Integer.parseInt(arg));
                      skipCount += arg.length() + 2;
                    } else {
                      elem = new ClassNameElement();
                    }
                    break;
          case 'd': if ((arg = getArg(str, skipCount, strLen)) != null) {
                      elem = new DateElement(arg);
                      skipCount += arg.length() + 2;
                    } else {
                      elem = new DateElement();
                    }
                    break;
          case 'f': if (baseFormatter == null) {
                      throw new IllegalArgumentException(
                          "%f format conversion requires a base Formatter");
                    }
                    elem = new FormattedRecordElement();
                    break;
          case 'M': elem = new MethodNameElement();
                    break;
          case 'm': elem = new MessageElement();
                    break;
          case 'N': elem = new SequenceNumberElement();
                    break;
          case 'n': elem = new StringElement(NL);
                    break;
          case 'p': elem = new LevelElement();
                    break;
          case 'T': elem = new ThreadIdElement();
                    break;
          case 't': elem = new ThreadNameElement();
                    break;
          case 'x': elem = new NDCElement();
                    break;
          case 'X': if ((arg = getArg(str, skipCount, strLen)) != null) {
                      elem = new MDCElement(arg);
                      skipCount += arg.length() + 2;
                    } else {
                      throw new IllegalArgumentException(
                          "MDC format conversion must specify key "
                          + str.substring(skipCount));
                    }
                    break;
          default:  // Unimplemented format Conversion.  Skip it.
                    break;
        }

        // Add the format element to the list of compiled elements.
        if (elem != null) {
          // If format conversion had a modifier, wrap it around the element.
          if (modifier != null) {
            elem = new ModifierElement(modifier, elem);
          }
          elems.add(elem);
        }

        // If there's anything left in the token, consider it a string constant.
        if (skipCount < strLen) {
          elems.add(new StringElement(str.substring(skipCount)));
        }
      }
    }
    formatElements = elems.toArray(new FormatElement[elems.size()]);
  }

  /**
   * Parse out an argument from the format string.  Looks for an argument
   * like <code>{xyzzy}</code> occurring at the start offset of the string.
   * If found, the substring between the braces is returned.  Otherwise null
   * is returned.
   *
   * @param str String we are tokenizing.
   * @param off offset into the string to start looking.
   * @param len the length of str.
   */
  private String getArg(String str, int off, int len) {
    if (off < len && str.charAt(off) == '{') {
      int end = str.indexOf('}', off + 1);
      if (end > 0) {
        return str.substring(off + 1, end);
      }
    }
    return null;
  }

  /**
   * Format the supplied LogRecord according to the layout conversion pattern.
   *
   * @param logRecord  The LogRecord to format.
   * @return a formatted log entry String.
   */
  public String format(LogRecord logRecord) {
    StringBuilder str = new StringBuilder();
    for (FormatElement elem : formatElements) {
      elem.format(str, logRecord);
    }
    return str.toString();
  }

  /* *** Compiled Format Elements *** */

  private interface FormatElement {
    public void format(StringBuilder builder, LogRecord logRecord);
  }


  /**
   * Modifier wraps another FormatElement, adding padding, justification,
   * truncation, etc.
   */
  private class ModifierElement implements FormatElement {
    private boolean leftJustified;
    private int minWidth;
    private int maxWidth;
    private FormatElement base;

    /**
     * Wrap the supplied base FormatElement, modifying its output
     * with padding, justification, and/or truncation.
     */
    public ModifierElement(String modifier, FormatElement base) {
      this.base = base;
      this.parse(modifier);
    }

    /**
     * Parse the modifier string in the form of: "-minWidth.maxWidth"
     */
    private void parse(String modifier) {
      int start;
      if (modifier.charAt(0) == '-') {
        leftJustified = true;
        start = 1;
      } else {
        leftJustified = false;
        start = 0;
      }
      int dotPos = modifier.indexOf('.');
      // Extract the minWidth.
      try {
        if (dotPos < 0) {
          minWidth = Integer.parseInt(modifier.substring(start));
        } else {
          minWidth = Integer.parseInt(modifier.substring(start, dotPos));
        }
      } catch (NumberFormatException nfe) {
        minWidth = 0; // Empty string is 0
      }
      // Extract the maxWidth.
      maxWidth = Integer.MAX_VALUE;
      if ((dotPos >= 0) && (dotPos < modifier.length())) {
        try {
          maxWidth = Integer.parseInt(modifier.substring(dotPos + 1));
        } catch (NumberFormatException nfe) {
          // Empty string is MAX_INT.
        }
      }
    }

    @Override
    public void format(StringBuilder builder, LogRecord record) {
      StringBuilder baseBuilder = new StringBuilder();
      base.format(baseBuilder, record);
      int len = baseBuilder.length();
      if (len >= minWidth && len <= maxWidth) {
        builder.append(baseBuilder);
      } else if (len < minWidth) {
        if (leftJustified) {
          builder.append(baseBuilder);
          for (; len < minWidth; len++) {
            builder.append(' ');
          }
        } else {
          for (; len < minWidth; len++) {
            builder.append(' ');
          }
          builder.append(baseBuilder);
        }
      } else { // len > maxWidth
        builder.append(baseBuilder, 0, maxWidth);
      }
    }
  }


  // String constant.
  private class StringElement implements FormatElement {
    private String string;
    public StringElement(String string) {
      this.string = string;
    }
    @Override
    public void format(StringBuilder builder, LogRecord ignored) {
      builder.append(string);
    }
  }

  // %C - Class Name.  %C{n} shows the rightmost n segments of the
  // fully qualified class name.
  private class ClassNameElement implements FormatElement {
    private int segments = 0;
    public ClassNameElement() {}
    public ClassNameElement(int numSegments) {
      this.segments = numSegments;
    }
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      String name = logRecord.getSourceClassName();
      if (segments > 0) {
        int start = name.length();
        for (int i = 0; i < segments; i++) {
          if ((start = name.lastIndexOf('.', start - 1)) < 0) {
            builder.append(name);
            return;
          }
        }
        builder.append(name.substring(start + 1));
      } else {
        builder.append(name);
      }
    }
  }

  // %d{date-format} - Date.  The date-format specifier uses the same syntax
  // as java.text.SimpleDateFormat.  ISO8601 is the default.
  private class DateElement implements FormatElement {
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd' 'HH:mm:ss";
    private SimpleDateFormat dateFormat;
    public DateElement() {
      this(DEFAULT_DATE_FORMAT);
    }
    public DateElement(String dateFormat) {
      this.dateFormat = new SimpleDateFormat(dateFormat);
    }
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      builder.append(dateFormat.format(new Date(logRecord.getMillis())));
    }
  }

  // %f - Base Formatter Formatted Record.
  // This returns the fully formatted output of the underlying Formatter.
  // For instance, the output of java.util.logging.SimpleFormatter.format().
  // This can be useful when embellishing Base Formatter output with
  // MDC information.  For instance, given a layout pattern like:
  // "[%T %t %X{connectorName}] %f" would prepend a formatted log message
  // with the ThreadID, ThreadName, and ConnectorName.
  private class FormattedRecordElement implements FormatElement {
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      if (baseFormatter != null) {
        builder.append(baseFormatter.format(logRecord));
      }
    }
  }

  // %M - Method Name.
  private class MethodNameElement implements FormatElement {
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      builder.append(logRecord.getSourceMethodName());
    }
  }

  // %m - Message.
  private class MessageElement implements FormatElement {
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      if (baseFormatter == null) {
        builder.append(logRecord.getMessage());
      } else {
        builder.append(baseFormatter.formatMessage(logRecord));
      }
    }
  }

  // %N - Sequence Number.
  private class SequenceNumberElement implements FormatElement {
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      builder.append(logRecord.getSequenceNumber());
    }
  }

  // %p - Priority/Level.
  private class LevelElement implements FormatElement {
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      builder.append(logRecord.getLevel().getName());
    }
  }

  // %T - Thread ID.
  private class ThreadIdElement implements FormatElement {
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      builder.append(Thread.currentThread().getId());
    }
  }

  // %t - Thread Name.
  private class ThreadNameElement implements FormatElement {
    @Override
    public void format(StringBuilder builder, LogRecord logRecord) {
      builder.append(Thread.currentThread().getName());
    }
  }

  // %X - MDC value for key.
  private class MDCElement implements FormatElement {
    private String key;
    public MDCElement(String key) {
      this.key = key;
    }
    @Override
    public void format(StringBuilder builder, LogRecord ignored) {
      builder.append(MDC.get(key));
    }
  }

  // %x - NDC value for thread.
  private class NDCElement implements FormatElement {
    @Override
    public void format(StringBuilder builder, LogRecord ignored) {
      builder.append(NDC.peek());
    }
  }
}
