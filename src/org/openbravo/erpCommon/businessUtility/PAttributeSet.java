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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class PAttributeSet {
  static Logger log4j = Logger.getLogger(PAttributeSet.class);
  PAttributeSetData[] pAttributesData;
  boolean isInstance = false;

  public PAttributeSet(ConnectionProvider conn, String strAttributeSet) {
    try {
      pAttributesData = PAttributeSetData.select(conn, strAttributeSet);
      isInstance = isInstanceAttributeSet(pAttributesData);
    } catch (ServletException e) {
      log4j.error(e);
    }
  }

  public static boolean isInstanceAttributeSet(PAttributeSetData[] data) {
    if (data == null || data.length < 1)
      return false;
    if (data[0].islot.equals("Y"))
      return true;
    if (data[0].isserno.equals("Y"))
      return true;
    if (data[0].isguaranteedate.equals("Y"))
      return true;
    if (!data[0].elementname.equals("")) {
      for (int i = 0; i < data.length; i++) {
        if (data[i].isinstanceattribute.equals("Y"))
          return true;
      }
    }
    return false;
  }
}
