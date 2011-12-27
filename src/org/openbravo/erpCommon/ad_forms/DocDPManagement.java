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
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;

public class DocDPManagement extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(DocDPManagement.class);

  private String SeqNo = "0";

  /**
   * @return the log4j
   */
  public static Logger getLog4j() {
    return log4j;
  }

  /**
   * @param log4j
   *          the log4j to set
   */
  public static void setLog4j(Logger log4j) {
    DocDPManagement.log4j = log4j;
  }

  /**
   * @return the seqNo
   */
  public String getSeqNo() {
    return SeqNo;
  }

  /**
   * @param seqNo
   *          the seqNo to set
   */
  public void setSeqNo(String seqNo) {
    SeqNo = seqNo;
  }

  /**
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocDPManagement(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id)
      throws ServletException {
    setObjectFieldProvider(DocDPManagementData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Specific Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_DPManagement;
    DateDoc = data[0].getField("Dateacct");
    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn);
    log4j.debug("Record_ID = " + Record_ID + " - Lines=" + p_lines.length);
    return false;
  } // loadDocumentDetails

  /**
   * Load AmortizationLine Line.
   * 
   * @return DocLine Array
   */
  private DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineDPManagementData[] data = null;

    try {
      data = DocLineDPManagementData.select(conn, Record_ID);
      log4j.debug("LoadLines: data.length " + data.length + " record_ID " + Record_ID);
    } catch (ServletException e) {
      log4j.warn(e);
    }

    //
    for (int i = 0; data != null && i < data.length; i++) {
      String Line_ID = data[i].getField("C_DP_MANAGEMENTLINE_ID");
      DocLine_DPManagement docLine = new DocLine_DPManagement(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.Amount = data[i].getField("AMOUNT");
      docLine.m_Record_Id2 = data[i].getField("C_DEBT_PAYMENT_ID");
      docLine.conversionDate = data[i].getField("conversiondate");
      docLine.Isreceipt = data[i].getField("ISRECEIPT");
      docLine.StatusTo = data[i].getField("STATUS_TO");
      docLine.StatusFrom = data[i].getField("STATUS_FROM");
      docLine.IsManual = data[i].getField("ISMANUAL");
      docLine.IsDirectPosting = data[i].getField("ISDIRECTPOSTING");
      list.add(docLine);
    }

    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Source Currency Balance - always zero
   * 
   * @return Zero (always balanced)
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;

    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Select specific definition
    String strClassname = AcctServerData
        .selectTemplateDoc(conn, as.m_C_AcctSchema_ID, DocumentType);
    if (strClassname.equals(""))
      strClassname = AcctServerData.selectTemplate(conn, as.m_C_AcctSchema_ID, AD_Table_ID);
    if (!strClassname.equals("")) {
      try {
        DocDPManagementTemplate newTemplate = (DocDPManagementTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocDPManagementTemplate - " + e);
      }
    }
    log4j.debug("createFact - Inicio");
    // create Fact Header
    Fact fact = null;
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    log4j.debug("createFact - object created");
    // Lines
    fact = new Fact(this, as, Fact.POST_Actual);
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_DPManagement line = (DocLine_DPManagement) p_lines[i];
      if (line.IsManual.equals("N") || line.IsDirectPosting.equals("Y")) {
        String amount = calculateAmount(as, line, conn);
        if (line.Isreceipt.equals("Y")) {
          fact.createLine(line,
              getAccount(line.Isreceipt, line.m_C_BPartner_ID, as, line.StatusTo, conn),
              line.m_C_Currency_ID, amount, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              conn);
          fact.createLine(line,
              getAccount(line.Isreceipt, line.m_C_BPartner_ID, as, line.StatusFrom, conn),
              line.m_C_Currency_ID, "", amount, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              conn);

        } else {
          fact.createLine(line,
              getAccount(line.Isreceipt, line.m_C_BPartner_ID, as, line.StatusTo, conn),
              line.m_C_Currency_ID, "", amount, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              conn);
          fact.createLine(line,
              getAccount(line.Isreceipt, line.m_C_BPartner_ID, as, line.StatusFrom, conn),
              line.m_C_Currency_ID, amount, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              conn);
        }
      }
    }
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * 
   * @param oldSeqNo
   */
  public String nextSeqNo(String oldSeqNo) {
    log4j.debug("DocDPManagement - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4j.debug("DocDPManagement - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * 
   * @param as
   * @param line
   * @param conn
   */
  public String calculateAmount(AcctSchema as, DocLine_DPManagement line, ConnectionProvider conn) {
    String Amt = getConvertedAmt(line.Amount, line.m_C_Currency_ID, as.m_C_Currency_ID,
        line.conversionDate, "", AD_Client_ID, AD_Org_ID, conn);
    Amt = getConvertedAmt(Amt, as.m_C_Currency_ID, line.m_C_Currency_ID, DateAcct, "",
        AD_Client_ID, AD_Org_ID, conn);
    return Amt;
  }

  /**
 *
 */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    DocDPManagementData[] data;
    try {
      data = DocDPManagementData.paymentInformation(conn, strRecordId);
    } catch (ServletException e) {
      log4j.error(e.getMessage(), e);
      setStatus(STATUS_Error);
      return false;
    }

    if (data.length > 0) {
      for (DocDPManagementData row : data) {
        if (row.ismanual.equals("N") || row.isdirectposting.equals("Y"))
          return true;
      }
      setStatus(STATUS_DocumentDisabled);
      return false;
    }
    return true;
  }

  /**
   * 
   * @param Isreceipt
   * @param partnerID
   * @param as
   * @param status
   * @param conn
   */
  public Account getAccount(String Isreceipt, String partnerID, AcctSchema as, String status,
      ConnectionProvider conn) {
    /*
     * if (Integer.parseInt(AcctType) < 1 || Integer.parseInt(AcctType) > 2) return null;
     */

    // No Product - get Default from Product Category
    /*
     * if (A_Asset_ID.equals("")) return getAccountDefault(AcctType, as, conn);
     */
    DocDPManagementData[] data = null;
    Account acc = null;
    try {

      String validCombination_ID = "";

      if (Isreceipt.equals("Y")) {
        data = DocDPManagementData.selectReceiptAcct(conn, partnerID, as.getC_AcctSchema_ID(),
            status);
        validCombination_ID = data[0].acct;
      } else {
        data = DocDPManagementData.selectNoReceiptAcct(conn, partnerID, as.getC_AcctSchema_ID(),
            status);
        validCombination_ID = data[0].acct;
      }

      if (data == null || data.length == 0)
        return null;

      if (validCombination_ID.equals(""))
        return null;
      acc = Account.getAccount(conn, validCombination_ID);
      log4j.debug("DocDPManagement - getAccount - " + acc.Account_ID);
    } catch (ServletException e) {
      log4j.warn(e);
    }
    return acc;
  } // getAccount

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
