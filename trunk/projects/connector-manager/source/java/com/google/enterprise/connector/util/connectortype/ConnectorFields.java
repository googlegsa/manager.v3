// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.util.connectortype;

import com.google.common.base.Preconditions;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.XmlUtils;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The interfaces and classes contained here are useful for building
 * {@link ConnectorType} implementations. This is based on code originally
 * developed for the FileConnector
 *
 * TODO(Max): Refactor the FileConnector to use this.
 */
public class ConnectorFields {

  /**
   * This is a non-instantiable utility class.
   */
  private ConnectorFields() {
    throw new IllegalStateException();
  }

  private static final Logger LOG = Logger.getLogger(ConnectorFields.class.getName());

  public static boolean hasContent(String s) {
    /*
     * We determine content by the presence of non-whitespace characters.
     * Our field values come from HTML input boxes which get mapped to
     * empty strings when there is no user entry.
     */
    return (s != null) && (s.trim().length() != 0);
  }

  /**
   * This represents a single configuration field. This interface is
   * currently only needed so tests do not need access to AbstractField.
   */
  public static interface Field {
    String getName();

    boolean isMandatory();

    String getLabel(ResourceBundle bundle);

    /**
     * @return a tr element with two td elements inside for
     *         Config form
     */
    String getSnippet(ResourceBundle bundle, boolean highlightError);
  }

  /**
   * Holds information common to fields like their names and ways to
   * show their name as an HTML label. Does not hold value of field; instead
   * subclass has to handle value.
   */
  public abstract static class AbstractField implements Field {
    private final String name;
    private final boolean mandatory; // is field necessary for valid configuration?

    public AbstractField(String name, boolean mandatory) {
      this.name = name;
      this.mandatory = mandatory;
    }

    /* Fulfills Field interface except for getSnippet which requires value. */
    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean isMandatory() {
      return mandatory;
    }

    @Override
    public String getLabel(ResourceBundle bundle) {
      return bundle.getString(getName());
    }

    public void setBoldLabel(boolean boldLabel) {
      this.boldLabel = boldLabel;
    }

    /**
     * Makes HTML that could be used as a field label.
     * Intended to be helpful inside {@link #getSnippet}.
     *
     * @return an HTML td element with a label element inside.
     */
    protected String getLabelHtml(ResourceBundle bundle, boolean highlightError) {
      return getLabelHtml(bundle, highlightError, "");
    }

    protected boolean renderLabelTag = true;
    protected boolean boldLabel = true;

    protected String getLabelHtml(ResourceBundle bundle, boolean highlightError, String message) {
      // TODO: ensure characters are HTML escaped
      StringBuffer sb = new StringBuffer();
      sb.append("<td valign=\"top\">");
      if (highlightError) {
        sb.append("<font color=\"red\">");
      }
      if (boldLabel) {
        sb.append("<b>");
      }
      if (renderLabelTag) {
        sb.append("<label for=\"");
        sb.append(getName());
        sb.append("\">");
      }
      sb.append(getLabel(bundle));
      if (renderLabelTag) {
        sb.append("</label>");
      }
      if (boldLabel) {
        sb.append("</b>");
      }
      sb.append(message);
      if (highlightError) {
        sb.append("</font>");
      }
      sb.append("</td>");
      return sb.toString();
    }

    public abstract String getSnippet(ResourceBundle bundle, boolean hightlighError);

    /** @param config immutable Map of configuration parameters */
    public abstract void setValueFrom(Map<String, String> config);

    /** @param valueString String parameter */
    public abstract void setValueFromString(String valueString);

    /** Does this field store some user input? */
    public abstract boolean hasValue();

    /**
     * Returns the provided attribute value with XML special characters
     * escaped. This really just provides a convenience wrapper around
     * {@link XmlUtils#xmlAppendAttr}.
     */
    public static String xmlEncodeAttributeValue(String v) {
      try {
        StringBuilder sb = new StringBuilder();
        XmlUtils.xmlAppendAttrValue(v, sb);
        return sb.toString();
      } catch (IOException ioe) {
        /*
         * The IOException can occur because XmlUtils.xmlAppendAttrValue
         * appends to an Appendable which may throw an IOException. In our case
         * we pass in a StringBuilder so no IOException should occur.
         */
        LOG.log(Level.SEVERE,
            "Xml escaping encountered unexpected error ", ioe);
        throw new IllegalStateException(
            "Xml escaping encountered unexpected error ", ioe);
      }
    }
  }

  protected static String wrapPreviousValue(String v) {
    String result = "";
    if (hasContent(v)) {
      result = " value=\"" + v + "\"";
    }
    return result;
  }

  /**
   * Represents a string parameter. The parameter's
   * value is gathered using a single HTML input element.
   */
  public static class SingleLineField extends AbstractField {
    private static final String ONE_LINE_INPUT_HTML =
        "<td><input name=\"%s\" id=\"%s\" type=\"%s\" %s></input></td>";
    private static final String FORMAT = "<tr> %s " + ONE_LINE_INPUT_HTML + "</tr>";

    private final boolean isPassword;

    protected String value; // user's one input line value

    public SingleLineField(String name, boolean mandatory, boolean isPassword) {
      super(name, mandatory);
      this.isPassword = isPassword;
      value = "";
    }

    @Override
    public void setValueFrom(Map<String, String> config) {
      this.value = "";
      String newValue = config.get(getName());
      if (hasContent(newValue)) {
        setValueFromString(newValue);
      }
    }

    @Override
    public boolean hasValue() {
      return hasContent(value);
    }

    @Override
    public String getSnippet(ResourceBundle bundle, boolean highlightError) {
      return String.format(FORMAT, getLabelHtml(bundle, highlightError),
          getName(),
          getName(),
          isPassword ? "password" : "text", wrapPreviousValue(xmlEncodeAttributeValue(value)));
    }

    public String getValue() {
      return value;
    }

    @Override
    public void setValueFromString(String valueString) {
      if (valueString == null) {
        this.value = "";
      } else {
        this.value = valueString.trim();
      }
    }
  }

  /**
   * A SingleLineField specialized for representing an integer, with optional
   * default value.
   */
  public static class IntField extends SingleLineField {
    private Integer intValue;

    public IntField(String name, boolean mandatory, int defaultInt) {
      super(name, mandatory, false);
      setValueFromInt(defaultInt);
    }

    @Override
    public void setValueFrom(Map<String, String> config) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValueFromString(String valueString) {
      throw new UnsupportedOperationException();
    }

    public void setValueFromInt(int i) {
      intValue = i;
      value = intValue.toString();
    }

    public Integer getIntegerValue() {
      return intValue;
    }
  }

  /**
   * Represents a parameter populated through an Enum. The parameter's
   * value is gathered using a dropdown. A default value specifies which value
   * will be preselected.
   */
  public static class EnumField<E extends Enum<E>> extends AbstractField {

    private E value;
    private final Class<E> enumClass;
    private final E defaultValue;

    public EnumField(String name, boolean mandatory, Class<E> enumClass, E defaultValue) {
      super(name, mandatory);
      Preconditions.checkNotNull(enumClass);
      value = null;
      this.enumClass = enumClass;
      this.defaultValue = defaultValue;
    }

    @Override
    public boolean hasValue() {
      return value != null;
    }

    @Override
    public String getSnippet(ResourceBundle bundle, boolean highlightError) {
      E selectedValue;
      if (value != null) {
        selectedValue = value;
      } else {
        selectedValue = defaultValue;
      }
      StringBuffer sb = new StringBuffer();
      sb.append("<tr>");
      sb.append(getLabelHtml(bundle, highlightError));
      sb.append("<td><select name=\"");
      sb.append(getName());
      sb.append("\" id=\"");
      sb.append(getName());
      sb.append("\">");
      for (E e : enumClass.getEnumConstants()) {
        sb.append("\n<option value=\"");
        sb.append(e.toString());
        sb.append("\"");
        if (e == selectedValue) {
          sb.append(" selected=\"selected\"");
        }
        sb.append(">");
        sb.append(bundle.getString(e.toString()));
        sb.append("</option>");
      }
      sb.append("\n</select></td></tr>");
      return sb.toString();
    }

    public E getValue() {
      if (hasValue()) {
        return value;
      }
      return defaultValue;
    }

    public void setValue(E value) {
      this.value = value;
    }

    @Override
    public void setValueFrom(Map<String, String> config) {
      // TODO(Max): so far this is only being used in contexts where this operation isn't needed.
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValueFromString(String valueString) {
      // TODO(Max): so far this is only being used in contexts where this operation isn't needed.
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Represents a parameter populated through a set of keys. The parameter's
   * value is gathered using a bunch of checkboxes.
   */
  public static class MultiCheckboxField extends AbstractField {
    private SortedSet<String> selectedKeys;
    private SortedSet<String> keys;
    private final String message;
    private Callback callback;

    public interface Callback {
      Map<String, String> getAttributes(String key);
    }

    public MultiCheckboxField(String name, boolean mandatory, Set<String> keys, String message) {
      super(name, mandatory);
      setKeys(keys);
      setSelectedKeys(null);
      this.message = message;
      this.renderLabelTag = false;
    }

    public MultiCheckboxField(String name, boolean mandatory, Set<String> keys,
        String message, Callback callback) {
      this(name, mandatory, keys, message);
      this.callback = callback;
    }

    private void makeSingleCheckboxHtml(StringBuffer sb, String boxname, String key,
        boolean selected) {
      sb.append("<label>");
      sb.append("<input type=\"checkbox\" name=\"");
      sb.append(boxname);
      sb.append("\" value=\"");
      sb.append(key);
      sb.append("\"");
      if (selected) {
        sb.append(" checked=\"checked\"");
      }
      if (callback != null) {
        Map<String, String> attributes = callback.getAttributes(key);
        try {
          for (Map.Entry<String, String> attr : attributes.entrySet()) {
            XmlUtils.xmlAppendAttr(attr.getKey(), attr.getValue(), sb);
          }
        } catch (IOException e) {
          // StringBuffer.append does not throw IOExceptions.
          throw new AssertionError(e);
        }
      }
      sb.append("/> ");
      sb.append(key);
      sb.append("</label>");
    }

    private String getCheckboxesHtml(String name, ResourceBundle bundle) {
      StringBuffer sb = new StringBuffer();
      sb.append("<td>");
      int i = 0;
      String boxName;
      for (String key : selectedKeys) {
        sb.append("\n");
        boxName = getName() + "_" + i;
        i++;
        makeSingleCheckboxHtml(sb, boxName, key, true);
        sb.append("<br/>");
      }
      for (String key : keys) {
        if (!selectedKeys.contains(key)) {
          sb.append("\n");
          boxName = getName() + "_" + i;
          i++;
          makeSingleCheckboxHtml(sb, boxName, key, false);
          sb.append("<br/>");
        }
      }
      sb.append("</td>");
      return sb.toString();
    }

    @Override
    public String getSnippet(ResourceBundle bundle, boolean highlightError) {
      if (keys == null || keys.size() < 1) {
        return "";
      }
      StringBuffer sb = new StringBuffer();
      sb.append("<tr>");
      String htmlTableName = getName() + "_table";
      String processedMessage = "";
      if (ConnectorFields.hasContent(message)) {
        processedMessage = "<br/>" + bundle.getString(message);
      }
      sb.append(getLabelHtml(bundle, highlightError, processedMessage));
      sb.append(getCheckboxesHtml(htmlTableName, bundle));
      sb.append("</tr>");
      return sb.toString();
    }

    @Override
    public boolean hasValue() {
      return selectedKeys.size() > 0;
    }

    public boolean isEmpty() {
      return keys.size() < 1;
    }

    private static SortedSet<String> makeSortedSet(Set<String> s) {
      if (s == null) {
        return new TreeSet<String>();
      } else {
        return new TreeSet<String>(s);
      }
    }

    public void setKeys(Set<String> keys) {
      this.keys = makeSortedSet(keys);
    }

    public void setSelectedKeys(Set<String> selectedKeys) {
      this.selectedKeys = makeSortedSet(selectedKeys);
    }

    @Override
    public void setValueFrom(Map<String, String> config) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValueFromString(String valueString) {
      throw new UnsupportedOperationException();
    }
  }
}
