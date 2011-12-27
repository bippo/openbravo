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

package org.openbravo.erpCommon.utility;

import java.sql.Connection;
import java.util.UUID;

import org.openbravo.database.ConnectionProvider;

public class SequenceIdData {

  /**
   * Returns a new UUID
   * 
   * @return a new random UUID
   */
  public static String getUUID() {
    return UUID.randomUUID().toString().replace("-", "").toUpperCase();
  }

  /**
   * Get the sequence for the specified table this shouldn't be used anymore, use instead getUUID()
   * It is deprecated and will be removed before the 2.60 release
   * 
   * @deprecated
   */
  public static String getSequence(ConnectionProvider conn, String table, String client) {
    return getUUID();
  }

  /**
   * Get the sequence for the specified table. It is deprecated and will be removed before the 2.60
   * release
   * 
   * @deprecated
   */
  public static String getSequenceConnection(Connection conn, ConnectionProvider con, String table,
      String client) {
    return getUUID();
  }
}
