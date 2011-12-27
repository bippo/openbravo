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
package org.openbravo.erpCommon.ad_actionButton;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.Utility;

public class ActionButtonUtility {
  static Logger log4j = Logger.getLogger(ActionButtonUtility.class);

  public static FieldProvider[] docAction(ConnectionProvider conn, VariablesSecureApp vars,
      String strDocAction, String strReference, String strDocStatus, String strProcessing,
      String strTable) {
    FieldProvider[] ld = null;
    if (log4j.isDebugEnabled())
      log4j.debug("DocAction - generating combo elements for table: " + strTable
          + " - actual status: " + strDocStatus);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, conn, "LIST", "DocAction",
          strReference, "", Utility.getContext(conn, vars, "#AccessibleOrgTree",
              "ActionButtonUtility"), Utility.getContext(conn, vars, "#User_Client",
              "ActionButtonUtility"), 0);
      Utility.fillSQLParameters(conn, vars, null, comboTableData, "ActionButtonUtility", "");
      ld = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception e) {
      return null;
    }
    SQLReturnObject[] data = null;
    if (ld != null) {
      Vector<Object> v = new Vector<Object>();
      SQLReturnObject data1 = new SQLReturnObject();
      if (!strProcessing.equals("") && strProcessing.equals("Y")) {
        data1.setData("ID", "XL");
        v.addElement(data1);
      } else if (strDocStatus.equals("NA")) {
        data1.setData("ID", "AP");
        v.addElement(data1);
        data1 = new SQLReturnObject();
        data1.setData("ID", "RJ");
        v.addElement(data1);
        data1 = new SQLReturnObject();
        data1.setData("ID", "VO");
        v.addElement(data1);
      } else if (strDocStatus.equals("DR") || strDocStatus.equals("IP")) {
        data1.setData("ID", "CO");
        v.addElement(data1);
        if (!strTable.equals("319") && !strTable.equals("800212")) {
          data1 = new SQLReturnObject();
          data1.setData("ID", "VO");
          v.addElement(data1);
        }
      } else if ((strDocStatus.equals("CO")) && !(strTable.equals("318")) && // C_Invoice
          !(strTable.equals("319"))) { // M_InOut
        // Exclude Close for tables C_Invoice and M_InOut because it has
        // no sense for them
        data1.setData("ID", "CL");
        v.addElement(data1);
      }
      data1 = new SQLReturnObject();
      if (strTable.equals("259")) { // C_Order
        if (strDocStatus.equals("DR")) {
          data1.setData("ID", "PR");
          v.addElement(data1);
        } else if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("318")) { // C_Invoice
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RC");
          v.addElement(data1);
          data1 = new SQLReturnObject();
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("319")) { // M_InOut
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RC");
          v.addElement(data1);
        }
      } else if (strTable.equals("224")) { // GL_Journal
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("800212")) { // M_Requisition
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      }

      data = new SQLReturnObject[v.size()];
      v.copyInto(data);
      if (log4j.isDebugEnabled())
        log4j.debug("DocAction - total combo elements: " + data.length);
      for (int i = 0; i < data.length; i++) {
        if (log4j.isDebugEnabled())
          log4j.debug("DocAction - Element: " + i + " - ID: " + data[i].getField("ID"));
        for (int j = 0; j < ld.length; j++) {
          if (data[i].getField("ID").equals(ld[j].getField("ID"))) {
            data[i].setData("NAME", ld[j].getField("NAME"));
            data[i].setData("DESCRIPTION", ld[j].getField("DESCRIPTION"));
            break;
          }
        }
      }
    }
    return data;
  }

  public static FieldProvider[] projectAction(ConnectionProvider conn, VariablesSecureApp vars,
      String strProjectAction, String strReference, String strProjectStatus) {
    FieldProvider[] ld = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, conn, "LIST", "ProjectAction",
          strReference, "", Utility.getContext(conn, vars, "#AccessibleOrgTree",
              "ActionButtonUtility"), Utility.getContext(conn, vars, "#User_Client",
              "ActionButtonUtility"), 0);
      Utility.fillSQLParameters(conn, vars, null, comboTableData, "ActionButtonUtility", "");
      ld = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception e) {
      return null;
    }
    SQLReturnObject[] data = null;
    if (ld != null) {
      Vector<Object> v = new Vector<Object>();
      SQLReturnObject data1 = new SQLReturnObject();
      if (strProjectStatus.equals("NF") || strProjectStatus.equals("OP")) {
        data1.setData("ID", "OR");
        v.addElement(data1);
        data1 = new SQLReturnObject();
        data1.setData("ID", "OC");
        v.addElement(data1);
      } else if (strProjectStatus.equals("OR")) {
        data1.setData("ID", "OC");
        v.addElement(data1);
      }

      if (v.size() > 0) {
        data = new SQLReturnObject[v.size()];
        v.copyInto(data);
        for (int i = 0; i < data.length; i++) {
          for (int j = 0; j < ld.length; j++) {
            if (data[i].getField("ID").equals(ld[j].getField("ID"))) {
              data[i].setData("NAME", ld[j].getField("NAME"));
              data[i].setData("DESCRIPTION", ld[j].getField("DESCRIPTION"));
              break;
            }
          }
        }
      }
    }
    return data;
  }
}
