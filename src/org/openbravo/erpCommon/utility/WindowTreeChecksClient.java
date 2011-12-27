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

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;

/**
 * @author Fernando Iriazabal
 * 
 *         Class defined to implement the specific checks of each client.
 */
class WindowTreeChecksClient {
  static Logger log4j = Logger.getLogger(WindowTreeChecksClient.class);

  /**
   * Checks the specific options of each tree type in the client.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param tabId
   *          Tab id.
   * @param topNodeId
   *          Parent node id.
   * @param nodeId
   *          Id of the node to change.
   * @param isChild
   *          Is gonna be child of the parent node?
   * @param TreeType
   *          Type of tree.
   * @param key
   *          Key column name.
   * @return empty string if it's ok or the message of the error.
   * @throws ServletException
   */
  public static String checkChanges(ConnectionProvider conn, VariablesSecureApp vars, String tabId,
      String topNodeId, String nodeId, boolean isChild, String TreeType, String key)
      throws ServletException {
    String result = "";
    return result;
  }
}
