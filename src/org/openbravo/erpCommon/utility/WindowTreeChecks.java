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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
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
 *         This class contains all the needed checks to make the node modifications in the tree
 *         windows.
 */
class WindowTreeChecks {
  static Logger log4j = Logger.getLogger(WindowTreeChecks.class);

  /**
   * Checks the common options to decide if the change can be made.
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
   * @return empty string if it's ok or the message of the error.
   * @throws ServletException
   */
  public static String checkChanges(ConnectionProvider conn, VariablesSecureApp vars, String tabId,
      String topNodeId, String nodeId, boolean isChild) throws ServletException {
    String result = "";
    if (topNodeId.equals(nodeId))
      return Utility.messageBD(conn, "SameElement", vars.getLanguage());
    try {
      String table = WindowTreeData.selectTableName(conn, tabId);
      String key = WindowTreeData.selectKey(conn, tabId);
      String TreeType = WindowTreeUtility.getTreeType(key);
      String isReady = WindowTreeData.selectIsReady(conn, nodeId);
      if ("Y".equals(isReady))
        return Utility.messageBD(conn, "OrgIsReady", vars.getLanguage());
      if (isChild && !topNodeId.equals("0")
          && WindowTreeChecksData.selectIsSummary(conn, table, key, topNodeId).equals("N"))
        return Utility.messageBD(conn, "NotIsSummary", vars.getLanguage());
      if (log4j.isDebugEnabled())
        log4j.debug("key:" + key + ", nodeId:" + nodeId + ",topNodeId:" + topNodeId);
      String treeID;
      WindowTreeData[] data = WindowTreeData.selectTreeID(conn, vars.getUserClient(), TreeType);

      if (!(data == null || data.length == 0)) {
        treeID = data[0].id;
        if (!WindowTreeChecksData.isItsOwnChild(conn, treeID, topNodeId, nodeId).equals("0"))
          return Utility.messageBD(conn, "RecursiveTree", vars.getLanguage());
      }
      result = WindowTreeChecks.checkSpecificChanges(conn, vars, tabId, topNodeId, nodeId, isChild,
          TreeType, key);
    } catch (ServletException ex) {
      log4j.error(ex);
      return Utility.messageBD(conn, "Error", vars.getLanguage());
    }
    return result;
  }

  /**
   * Checks the specific options of each tree type.
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
  public static String checkSpecificChanges(ConnectionProvider conn, VariablesSecureApp vars,
      String tabId, String topNodeId, String nodeId, boolean isChild, String TreeType, String key)
      throws ServletException {
    String result = "";
    if (TreeType.equals("MM")) { // Menu
      result = WindowTreeChecksData.isMenuItemInDev(conn, nodeId) ? "" : Utility.messageBD(conn,
          "CannotReorderNotDevModules", vars.getLanguage());
    } else if (TreeType.equals("OO")) { // Organization
      result = "";
    } else if (TreeType.equals("PR")) { // Product
      result = "";
    } else if (TreeType.equals("PC")) { // Product Category
      result = "";
    } else if (TreeType.equals("BB")) { // Product BOM
      result = "";
    } else if (TreeType.equals("EV")) { // Element Value
      result = "";
    } else if (TreeType.equals("BP")) { // BusinessPartner
      result = "";
    } else if (TreeType.equals("MC")) { // Campaign
      result = "";
    } else if (TreeType.equals("PJ")) { // Project
      result = "";
    } else if (TreeType.equals("AY")) { // Activity
      result = "";
    } else if (TreeType.equals("SR")) { // Sales Region
      result = "";
    } else if (TreeType.equals("AR")) { // Accounting report
      result = "";
    } else
      result = WindowTreeChecksClient.checkChanges(conn, vars, tabId, topNodeId, nodeId, isChild,
          TreeType, key);
    return result;
  }
}
