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

import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.service.json.JsonUtils;

/**
 * Implementation of the date ui definition.
 * 
 * @author mtaal
 */
public class TimeUIDefinition extends UIDefinition {
  private SimpleDateFormat classicFormat = null;
  private SimpleDateFormat xmlTimeFormat = JsonUtils.createJSTimeFormat();

  @Override
  public String getParentType() {
    return "time";
  }

  @Override
  public String getFormEditorType() {
    return "OBTimeItem";
  }

  @Override
  public synchronized String convertToClassicString(Object value) {
    if (value == null) {
      return "";
    }
    return value.toString();
  }

  private SimpleDateFormat getClassicFormat() {
    if (classicFormat == null) {
      String dateTimeFormat = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .get("dateTimeFormat.java");
      if (dateTimeFormat.contains(" ")) {
        dateTimeFormat = dateTimeFormat.substring(dateTimeFormat.indexOf(" ") + 1);
      } else {
        dateTimeFormat = "HH:mm:ss";
      }
      classicFormat = new SimpleDateFormat(dateTimeFormat);
      classicFormat.setLenient(true);
    }
    return classicFormat;
  }

  @Override
  public synchronized Object createFromClassicString(String value) {
    try {
      if (value == null || value.length() == 0 || value.equals("null")) {
        return null;
      }

      final java.util.Date date = getClassicFormat().parse(value);

      return xmlTimeFormat.format(date);
    } catch (Exception e) {
      throw new OBException("Exception when handling value " + value, e);
    }
  }
}
