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

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class Tree {
  static Logger log4jTree = Logger.getLogger(Tree.class);

  public static String getMembers(ConnectionProvider conn, String treeId, String parentNodeId)
      throws IOException, ServletException {
    log4jTree.debug("Tree.getMembers");
    TreeData[] data = TreeData.select(conn, treeId, parentNodeId);

    boolean bolFirstLine = true;
    String strText = "";
    for (int i = 0; i < data.length; i++) {
      data[i].id = "'" + data[i].id + "'";
      if (bolFirstLine) {
        bolFirstLine = false;
        strText = data[i].id;
      } else {
        strText = data[i].id + "," + strText;
      }
    }
    return strText;
  }

  public static String getTreeOrgs(ConnectionProvider conn, String client) throws IOException,
      ServletException {
    log4jTree.debug("Tree.getTreeOrg");
    TreeData[] data = TreeData.getTreeOrgs(conn, client);

    boolean bolFirstLine = true;
    String strText = "";
    for (int i = 0; i < data.length; i++) {
      // FIXME: Get this comparation out of the loop. It is only done once
      if (bolFirstLine) {
        bolFirstLine = false;
        strText = data[i].id;
      } else {
        strText = data[i].id + "," + strText;
      }
    }
    return strText;
  }

  public static String getTreeAccounts(ConnectionProvider conn, String client) throws IOException,
      ServletException {
    log4jTree.debug("Tree.getTreeOrg");
    TreeData[] data = TreeData.getTreeAccounts(conn, client);

    boolean bolFirstLine = true;
    String strText = "";
    for (int i = 0; i < data.length; i++) {
      // FIXME: Get this comparation out of the loop. It is only done once
      if (bolFirstLine) {
        bolFirstLine = false;
        strText = data[i].id;
      } else {
        strText = data[i].id + "," + strText;
      }
    }
    return strText;
  }
}
