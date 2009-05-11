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
import java.util.logging.LogRecord;
import java.util.logging.Formatter;

/**
 * Log message layout pattern for text (non-XML) format messages.
 */
public class LayoutPattern {
  private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd' 'HH:mm:ss";
  private static final String NL = System.getProperty("line.separator");

  private AbstractElement[] formatElements;
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
   * An example format string might look like:
   *  "[%T %t %X{ConnectorName}] %d %C %M%n%p: %m%n"
   *
   * @param format logging conversion format String
   */
  private void parse(String format) {
    ArrayList<AbstractElement> elems = new ArrayList<AbstractElement>();
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
        AbstractElement elem = null;
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
                    elem = new DateElement(DEFAULT_DATE_FORMAT);
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
          // If the format conversion had a modifier, wrap it around the element.
          if (modifier != null) {
            if (elem instanceof BasicElement) {
              elem = new BasicModifier(modifier, (BasicElement) elem);
            } else if (elem instanceof LogRecordElement) {
              elem = new LogRecordModifier(modifier, (LogRecordElement) elem);
            }
          }
          elems.add(elem);
        }

        // if there is anything left in the token, consider it a string constant.
        if (skipCount < strLen) {
          elems.add(new StringElement(str.substring(skipCount)));
        }
      }
    }
    formatElements = elems.toArray(new AbstractElement[elems.size()]);
  }

  /**
   * Parse out an argument from the format string.  Looks for an argument
   * like <code>{xyzzy}</code> ocurring at the start offset of the string.
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
   * Format the supplied LogRecord according to the layout format.
   *
   * @param logRecord  The LogRecord to format.
   * @return a formatted log entry String.
   */
  public String format(LogRecord logRecord) {
    StringBuilder str = new StringBuilder();
    for (AbstractElement elem : formatElements) {
      if (elem instanceof LogRecordString) {
        str.append(((LogRecordString)elem).toString(logRecord));
      } else if (elem instanceof BasicString) {
        str.append(((BasicString)elem).toString());
      } else if (elem instanceof LogRecordFormat) {
        ((LogRecordFormat)elem).format(str, logRecord);
      } else if (elem instanceof BasicFormat) {
        ((BasicFormat)elem).format(str);
      }
    }
    return str.toString();
  }

  /* *** Compiled Format Elements *** */

  private abstract class AbstractElement {
  }

  private interface BasicString {
    public String toString();
  }

  private interface LogRecordString {
    public String toString(LogRecord logRecord);
  }

  private interface BasicFormat {
    public void format(StringBuilder builder);
  }

  private interface LogRecordFormat {
    public void format(StringBuilder builder, LogRecord logRecord);
  }

  private abstract class AbstractModifier extends AbstractElement {
    protected boolean leftJustified;
    protected int minWidth;
    protected int maxWidth;

    /* Parse the modifier string in the form of: "-minWidth.maxWidth" */
    protected void parse(String modifier) {
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

    protected void format(StringBuilder builder, String value) {
      int len = value.length();
      if (len >= minWidth && len <= maxWidth) {
        builder.append(value);
      } else if (len < minWidth) {
        if (leftJustified) {
          builder.append(value);
          for (; len < minWidth; len++) {
            builder.append(' ');
          }
        } else {
          for (; len < minWidth; len++) {
            builder.append(' ');
          }
          builder.append(value);
        }
      } else { // len > maxWidth
        builder.append(value.substring(0, maxWidth));
      }
    }
  }

  // Wraps a BasicElement, adding precision modifier.
  private class BasicModifier extends AbstractModifier implements BasicFormat {
    private BasicElement base;

    BasicModifier(String modifier, BasicElement base) {
      parse(modifier);
      this.base = base;
    }

    public void format(StringBuilder builder) {
      format(builder, base.toString());
    }
  }

  // Wraps a LogRecordElement, adding precision modifier.
  private class LogRecordModifier extends AbstractModifier implements LogRecordFormat {
    private LogRecordElement base;

    LogRecordModifier(String modifier, LogRecordElement base) {
      parse(modifier);
      this.base = base;
    }

    public void format(StringBuilder builder, LogRecord logRecord) {
      format(builder, base.toString(logRecord));
    }
  }

  private abstract class BasicElement extends AbstractElement implements BasicString {
  }

  // String constant.
  private class StringElement extends BasicElement {
    private String string;
    public StringElement(String string) {
      this.string = string;
    }
    @Override
    public String toString() {
      return string;
    }
  }

  // Formats items from the LogRecord.
  private abstract class LogRecordElement extends AbstractElement implements LogRecordString {
    abstract public String toString(LogRecord logRecord);
  }

  // %C - Class Name.  %C{n} shows the rightmost n segments of the
  // fully qualified class name.
  private class ClassNameElement extends LogRecordElement {
    private int segments = 0;
    public ClassNameElement() {}
    public ClassNameElement(int numSegments) {
      this.segments = numSegments;
    }
    @Override
    public String toString(LogRecord logRecord) {
      String name = logRecord.getSourceClassName();
      if (segments > 0) {
        int start = name.length();
        for (int i = 0; i < segments; i++) {
          if ((start = name.lastIndexOf('.', start - 1)) < 0) {
            return name;
          }
        }
        return name.substring(start + 1);
      }
      return name;
    }
  }

  // %d{date-format} - Date.  The date-format specifier uses the same syntax
  // as java.text.SimpleDateFormat.  ISO8601 is the default.
  private class DateElement extends LogRecordElement {
    private SimpleDateFormat dateFormat;
    public DateElement() {
      this.dateFormat = new SimpleDateFormat();
    }
    public DateElement(String dateFormat) {
      this.dateFormat = new SimpleDateFormat(dateFormat);
    }
    @Override
    public String toString(LogRecord logRecord) {
      return dateFormat.format(new Date(logRecord.getMillis()));
    }
  }

  // %f - Base Formatter Formatted Record.
  // This returns the fully formatted output of the underlying Formatter.
  // For intstance, the output of java.util.logging.SimpleFormatter.format().
  // This can be usefull when embellishing Base Formatter output with
  // MDC information.  For instance, given a layout pattern like:
  // "[%T %t %X{connectorName}] %f" would prepend a formatted log message
  // with the ThreadID, ThreadName, and ConnectorName.
  private class FormattedRecordElement extends LogRecordElement {
    @Override
    public String toString(LogRecord logRecord) {
      return (baseFormatter != null) ? baseFormatter.format(logRecord) : "";
    }
  }

  // %M - Method Name.
  private class MethodNameElement extends LogRecordElement {
    @Override
    public String toString(LogRecord logRecord) {
      return logRecord.getSourceMethodName();
    }
  }

  // %m - Message.
  private class MessageElement extends LogRecordElement {
    @Override
    public String toString(LogRecord logRecord) {
      if (baseFormatter == null) {
        return logRecord.getMessage();
      } else {
        return baseFormatter.formatMessage(logRecord);
      }
    }
  }

  // %N - Sequence Number.
  private class SequenceNumberElement extends LogRecordElement {
    @Override
    public String toString(LogRecord logRecord) {
      return Long.toString(logRecord.getSequenceNumber());
    }
  }

  // %p - Priority/Level.
  private class LevelElement extends LogRecordElement {
    @Override
    public String toString(LogRecord logRecord) {
      return logRecord.getLevel().getName();
    }
  }

  // %T - Thread ID.
  private class ThreadIdElement extends LogRecordElement {
    @Override
    public String toString(LogRecord logRecord) {
      return Long.toString(Thread.currentThread().getId());
    }
  }

  // %t - Thread Name.
  private class ThreadNameElement extends LogRecordElement {
    @Override
    public String toString(LogRecord logRecord) {
      return Thread.currentThread().getName();
    }
  }

  // %X - MDC value for key.
  private class MDCElement extends BasicElement {
    private String key;
    public MDCElement(String key) {
      this.key = key;
    }
    @Override
    public String toString() {
      return MDC.get(key);
    }
  }

  // %x - NDC value for thread.
  private class NDCElement extends BasicElement {
  @Override
  public String toString() {
      return NDC.peek();
    }
  }
}
