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
 * All portions are Copyright (C) 2007-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.jasperreports.engine.util.DefaultFormatFactory;

public class JRFormatFactory extends DefaultFormatFactory {
  String datePattern;

  public DateFormat createDateFormat(String pattern, Locale locale, TimeZone timeZone) {
    DateFormat dateFormat = null;
    if (pattern == null || pattern.equals("")) {
      dateFormat = new SimpleDateFormat(datePattern);
      return dateFormat;
    } else {
      return super.createDateFormat(pattern, locale, timeZone);
    }
  }

  public void setDatePattern(String datePattern) {
    this.datePattern = datePattern;
  }
}
