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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model.domaintype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The type for a datetime column.
 * 
 * @author mtaal
 */

public class DatetimeDomainType extends BasePrimitiveDomainType {

  private final SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");

  public DatetimeDomainType() {
    xmlDateFormat.setLenient(true);
  }

  /**
   * @return class of the {@link Date}
   */
  public Class<?> getPrimitiveType() {
    return Date.class;
  }

  @Override
  public synchronized String convertToString(Object value) {
    if (value == null) {
      return EMPTY_STRING;
    }
    return xmlDateFormat.format(value);
  }

  @Override
  public synchronized Object createFromString(String strValue) {
    try {
      if (strValue == null || strValue.trim().length() == 0) {
        return null;
      }
      return xmlDateFormat.parse(strValue);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String getXMLSchemaType() {
    return "ob:dateTime";
  }
}
