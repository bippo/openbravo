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
package org.openbravo.erpCommon.info;

import javax.servlet.ServletException;

/**
 * Common utility functions to be used by the different selectors.
 * 
 * @author huehner
 * 
 */
public class SelectorUtility {

  /**
   * Builds an sql orderBy clause constructed out of list of order by column names and directions.
   * 
   * @param strOrderCols
   *          String with list of orderBy columns from getInStringParameter
   * @param strOrderDirs
   *          String with list of orderBy directions from getInStringParameter
   * @return String to be passed after sql "ORDER BY"
   * @throws ServletException
   *           on malformed input
   */
  public static String buildOrderByClause(String strOrderCols, String strOrderDirs)
      throws ServletException {
    if (strOrderCols.length() <= 2 || strOrderCols.charAt(0) != '('
        || strOrderCols.charAt(strOrderCols.length() - 1) != ')') {
      throw new ServletException("Illegal orderBy specification: " + strOrderCols);
    }
    if (strOrderDirs.length() <= 2 || strOrderDirs.charAt(0) != '('
        || strOrderDirs.charAt(strOrderDirs.length() - 1) != ')') {
      throw new ServletException("Illegal orderBy specification: " + strOrderDirs);
    }

    // structure from getInStringParameter: ('colA', 'colB', ...)
    String[] orderByCols = strOrderCols.substring(1, strOrderCols.length() - 1).split(",");
    String[] orderByDirs = strOrderDirs.substring(1, strOrderDirs.length() - 1).split(",");
    StringBuilder order = new StringBuilder();
    for (int i = 0; i < orderByCols.length; i++) {
      // strip blanks and ' to get real column/direction
      String col = orderByCols[i].trim();
      String dir = orderByDirs[i].trim();
      col = col.substring(1, col.length() - 1);
      dir = dir.substring(1, dir.length() - 1);

      order.append(col);
      order.append(" ");
      order.append(dir);
      if (i < orderByCols.length - 1) {
        order.append(", ");
      }
    }

    String res = order.toString();
    return res;
  }

}
