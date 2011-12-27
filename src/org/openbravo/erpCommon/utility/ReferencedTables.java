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

import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

class ReferencedTables {
  static Logger log4j = Logger.getLogger(ReferencedTables.class);
  private ConnectionProvider conn;
  private String adTableId;
  private String keyName;
  private String keyId;
  private boolean hassotrx = false;
  private boolean sotrx = true;

  public ReferencedTables(ConnectionProvider _conn, String _adTableId, String _keyName,
      String _keyId) throws ServletException {
    if (_adTableId == null || _adTableId.equals("") || _keyName == null || _keyName.equals("")) {
      throw new ServletException("ReferenceTables() - Missing arguments");
    }
    adTableId = _adTableId;
    keyName = _keyName;
    keyId = _keyId;
    conn = _conn;
    process();
  }

  private void process() throws ServletException {
    String tableName = ReferencedTablesData.selectTableName(conn, adTableId);
    if (keyId != null && !keyId.equals("")) {
      if (adTableId.equals("800018")) { // C_Debt_Payment
        if (log4j.isDebugEnabled())
          log4j.debug("DP");
        String invoiceId = ReferencedTablesData.selectKeyId(conn, "C_INVOICE_ID", tableName,
            keyName, keyId);
        if (!invoiceId.equals("")) {
          if (log4j.isDebugEnabled())
            log4j.debug("InvoiceId: " + invoiceId);
          String newAdTableId = ReferencedTablesData.selectTableId(conn, "C_Invoice");
          ReferencedTables ref = new ReferencedTables(conn, newAdTableId, "C_Invoice_ID", invoiceId);
          hassotrx = ref.hasSOTrx();
          sotrx = ref.isSOTrx();
          ref = null;
        } else {
          String orderId = ReferencedTablesData.selectKeyId(conn, "C_ORDER_ID", tableName, keyName,
              keyId);
          if (!orderId.equals("")) {
            if (log4j.isDebugEnabled())
              log4j.debug("OrderId: " + orderId);
            String newAdTableId = ReferencedTablesData.selectTableId(conn, "C_Order");
            ReferencedTables ref = new ReferencedTables(conn, newAdTableId, "C_Order_ID", orderId);
            hassotrx = ref.hasSOTrx();
            sotrx = ref.isSOTrx();
            ref = null;
          } else {
            if (log4j.isDebugEnabled())
              log4j.debug("Settlement");
            checkParent(tableName, "C_Settlement_Generate_ID");
          }
        }
      } else if (adTableId.equals("800019")) { // C_Settlement
        hassotrx = true;
        sotrx = ReferencedTablesData.selectNotManual(conn, keyId);
      } else if (ReferencedTablesData.hasIsSOTrx(conn, adTableId)) {
        hassotrx = true;
        if (keyName.equalsIgnoreCase("C_DocTypeTarget_ID"))
          keyName = "C_DocType_ID";
        else if (keyName.equalsIgnoreCase("PO_Window_ID"))
          keyName = "AD_Window_ID";
        else if (keyName.equalsIgnoreCase("BillTo_ID"))
          keyName = "C_BPartner_Location_ID";
        sotrx = ReferencedTablesData.selectSOTrx(conn, tableName, keyName, keyId);
      } else
        checkParent(tableName, "");
    }
  }

  private void checkParent(String tableName, String filterField) throws ServletException {
    ReferencedTablesData[] data = ReferencedTablesData.select(conn, filterField, adTableId);
    if (keyName.equalsIgnoreCase("C_DocTypeTarget_ID"))
      keyName = "C_DocType_ID";
    else if (keyName.equalsIgnoreCase("PO_Window_ID"))
      keyName = "AD_Window_ID";
    else if (keyName.equalsIgnoreCase("BillTo_ID"))
      keyName = "C_BPartner_Location_ID";
    else if ((keyName.equalsIgnoreCase("SO_Bankaccount_ID"))
        || (keyName.equalsIgnoreCase("PO_Bankaccount_ID")))
      keyName = "C_BankAccount_ID";
    if (data != null && data.length > 0) {
      for (int i = 0; i < data.length; i++) {
        Vector<Object> vecReference = getTableNameReferenced(data[i].columnname,
            data[i].adReferenceId, data[i].adReferenceValueId);
        String newKeyId = ReferencedTablesData.selectKeyId(conn, data[i].columnname, tableName,
            keyName, keyId);
        if (vecReference != null && vecReference.size() > 0) {
          String newAdTableId = ReferencedTablesData.selectTableId(conn,
              ((String) vecReference.elementAt(0)));
          try {
            ReferencedTables ref = new ReferencedTables(conn, newAdTableId,
                ((String) vecReference.elementAt(1)), newKeyId);
            if (ref.hasSOTrx()) {
              hassotrx = ref.hasSOTrx();
              sotrx = ref.isSOTrx();
              break;
            }
            ref = null;
          } catch (ServletException ex) {
            ex.printStackTrace();
          }
        }
      }
    }
  }

  private Vector<Object> getTableNameReferenced(String columnname, String adReferenceId,
      String adReferenceValueId) throws ServletException {
    String tableDirName = "", columnName = "";
    tableDirName = columnname.substring(0, columnname.length() - 3);
    columnName = columnname;
    if (columnname.startsWith("C_Settlement")) {
      tableDirName = "C_Settlement";
      columnName = "C_Settlement_ID";
    }
    Vector<Object> vec = new Vector<Object>();
    if ("13".equals(adReferenceId) || "19".equals(adReferenceId) || "35".equals(adReferenceId)
        || "30".equals(adReferenceId)) {
      vec.addElement(tableDirName);
      vec.addElement(columnName);
    } else if ("21".equals(adReferenceId)) {
      vec.addElement("C_Location");
      vec.addElement("C_Location_ID");
    } else if ("25".equals(adReferenceId)) {
      vec.addElement("C_ValidCombination");
      vec.addElement("C_ValidCombination_ID");
    } else if ("31".equals(adReferenceId)) {
      vec.addElement("M_Locator");
      vec.addElement("M_Locator_ID");
    } else if ("800011".equals(adReferenceId)) {
      vec.addElement("M_Product");
      vec.addElement("M_Product_ID");
    } else if ("800013".equals(adReferenceId)) {
      vec.addElement("M_Locator");
      vec.addElement("M_Locator_ID");
    } else if ("18".equals(adReferenceId)) {
      ReferencedTablesData[] data = ReferencedTablesData.selectRefTable(conn, adReferenceValueId);
      if (data != null && data.length > 0) {
        vec.addElement(data[0].tablename);
        vec.addElement(data[0].columnname);
      }
    }
    return vec;
  }

  public boolean isSOTrx() {
    return sotrx;
  }

  public boolean hasSOTrx() {
    return hassotrx;
  }
}
