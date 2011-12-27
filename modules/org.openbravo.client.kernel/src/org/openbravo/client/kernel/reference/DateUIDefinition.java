/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel.reference;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.model.ad.ui.Field;

/**
 * Implementation of the date ui definition.
 * 
 * @author mtaal
 */
public class DateUIDefinition extends UIDefinition {
  private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
  private static final String UIPATTERN = "yyyy-MM-dd";

  private SimpleDateFormat format = null;
  private String lastUsedPattern = null;
  private SimpleDateFormat dateFormat = null;
  private SimpleDateFormat uiDateFormat = null;

  public SimpleDateFormat getFormat() {
    if (format == null) {
      format = new SimpleDateFormat(PATTERN);
      format.setLenient(true);
    }
    return format;
  }

  @Override
  public String getParentType() {
    return "date";
  }

  @Override
  public String getFormEditorType() {
    return "OBDateItem";
  }

  @Override
  public String getFilterEditorType() {
    return "OBMiniDateRangeItem";
  }

  @Override
  public String getFilterEditorProperties(Field field) {
    return ", filterOnKeypress: false" + super.getFilterEditorProperties(field);
  }

  @Override
  public String getFieldProperties(Field field) {
    String fieldProperties = super.getFieldProperties(field);
    try {
      JSONObject o = new JSONObject(fieldProperties);
      o.put("width", "50%");
      return o.toString();
    } catch (Exception e) { // ignore
      return fieldProperties;
    }
  }

  protected SimpleDateFormat getClassicFormat() {
    String pattern = RequestContext.get().getSessionAttribute("#AD_JAVADATEFORMAT").toString();
    if (dateFormat == null || !pattern.equals(lastUsedPattern)) {
      dateFormat = new SimpleDateFormat(pattern);
      lastUsedPattern = pattern;
      dateFormat.setLenient(true);
    }
    return dateFormat;
  }

  private SimpleDateFormat getUIFormat() {
    if (uiDateFormat == null) {
      uiDateFormat = new SimpleDateFormat(UIPATTERN);
      uiDateFormat.setLenient(true);
    }
    return uiDateFormat;
  }

  @Override
  public synchronized String convertToClassicString(Object value) {
    if (value == null) {
      return "";
    }
    return getClassicFormat().format(value);
  }

  @Override
  public synchronized Object createFromClassicString(String value) {
    try {
      if (value == null || value.length() == 0 || value.equals("null")) {
        return null;
      }
      if (value.contains("T")) {
        return value;
      }
      final Date date = getClassicFormat().parse(value);
      return getUIFormat().format(date);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  @Override
  public String getTypeProperties() {
    final StringBuilder sb = new StringBuilder();
    sb.append("editFormatter: function(value, field, component, record) {"
        + "return OB.Utilities.Date.JSToOB(value, " + getClientFormatObject() + ");" + "},"
        + "parseInput: function(value, field, component, record) {"
        + "return OB.Utilities.Date.OBToJS(value, " + getClientFormatObject() + ");" + "},");
    sb.append("shortDisplayFormatter: function(value, field, component, record) {"
        + "return OB.Utilities.Date.JSToOB(value, " + getClientFormatObject() + ");" + "},"
        + "normalDisplayFormatter: function(value, field, component, record) {"
        + "return OB.Utilities.Date.JSToOB(value, " + getClientFormatObject() + ");" + "},"
        + "createClassicString: function(value) {" + "return OB.Utilities.Date.JSToOB(value, "
        + getClientFormatObject() + ");" + "},");
    return sb.toString();
  }

  protected String getClientFormatObject() {
    return "OB.Format.date";
  }

  @Override
  public String getCellAlign() {
    return "left";
  }
}
