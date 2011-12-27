/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2010 Openbravo S.L.U.
 ******************************************************************************
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

public class DocBank extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocBank = Logger.getLogger(DocBank.class);

  /**
   * @return the log4jDocBank
   */
  public static Logger getLog4jDocBank() {
    return log4jDocBank;
  }

  /**
   * @param log4jDocBank
   *          the log4jDocBank to set
   */
  public static void setLog4jDocBank(Logger log4jDocBank) {
    DocBank.log4jDocBank = log4jDocBank;
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

  private String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocBank(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id)
      throws ServletException {
    setObjectFieldProvider(DocBankData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Specific Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_BankStatement;
    DateDoc = data[0].getField("StatementDate");
    // Amounts
    Amounts[AcctServer.AMTTYPE_Gross] = data[0].getField("StatementDifference");
    if (Amounts[AcctServer.AMTTYPE_Gross] == null)
      Amounts[AcctServer.AMTTYPE_Gross] = ZERO.toString();

    // Set Bank Account Info (Currency)
    setBankAccountInfo(conn);
    loadDocumentType(); // lines require doc type
    String strCount = "";
    try {
      strCount = DocLineBankData.selectLinesPeriodClosed(conn, Record_ID);
    } catch (ServletException e) {
      e.printStackTrace();
      return false;
    }
    if (!strCount.equals("0"))
      return false;
    else {
      // Contained Objects
      p_lines = loadLines(conn);
      log4jDocBank.debug("Record_ID = " + Record_ID + " - Lines=" + p_lines.length);
    }
    return true;
  } // loadDocumentDetails

  /**
   * Set Bank Account Info
   */
  private void setBankAccountInfo(ConnectionProvider conn) {
    DocBankData[] data = null;
    log4jDocBank.debug("C_BankAccount_ID : " + C_BankAccount_ID);
    try {
      data = DocBankData.selectCurrency(conn, C_BankAccount_ID);
    } catch (ServletException e) {
      log4jDocBank.warn(e);
    }
    if (data != null && data.length > 0) {
      C_Currency_ID = data[0].cCurrencyId;
    }
  } // setBankAccountInfo

  /**
   * Load Invoice Line. 4 amounts AMTTYPE_Payment AMTTYPE_Statement2 AMTTYPE_Charge AMTTYPE_Interest
   * 
   * @return DocLine Array
   */
  private DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineBankData[] data = null;
    try {
      data = DocLineBankData.select(conn, Record_ID);
    } catch (ServletException e) {
      log4jDocBank.warn(e);
    }
    //
    for (int i = 0; data != null && i < data.length; i++) {
      String Line_ID = data[i].getField("C_BANKSTATEMENTLINE_ID");
      DocLine_Bank docLine = new DocLine_Bank(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.m_DateAcct = data[i].getField("DATEACCT");
      docLine.m_DateDoc = data[i].getField("VALUTADATE");
      docLine.m_C_Payment_ID = data[i].getField("C_DEBT_PAYMENT_ID");
      docLine.m_Record_Id2 = data[i].getField("C_DEBT_PAYMENT_ID");
      docLine.m_C_GLItem_ID = data[i].getField("C_GLITEM_ID");
      docLine.chargeAmt = data[i].getField("CHARGEAMT");
      docLine.isManual = data[i].getField("ISMANUAL");
      docLine.m_C_Project_ID = data[i].getField("C_Project_ID");
      docLine.setAmount(data[i].getField("STMTAMT")/*
                                                    * ,data[i].getField( "INTERESTAMT")
                                                    */, data[i].getField("TRXAMT"));
      docLine.convertChargeAmt = data[i].getField("CONVERTCHARGEAMT");
      list.add(docLine);
    }

    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Source Currency Balance - subtracts line amounts from total - no rounding
   * 
   * @return positive amount, if total is bigger than lines
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    StringBuffer sb = new StringBuffer(" [");
    // Total
    retValue = retValue.add(new BigDecimal(getAmount(AcctServer.AMTTYPE_Gross)));
    sb.append(getAmount(AcctServer.AMTTYPE_Gross));
    // - Lines
    for (int i = 0; i < p_lines.length; i++) {
      BigDecimal lineBalance = new BigDecimal(((DocLine_Bank) p_lines[i]).m_StmtAmt);
      retValue = retValue.subtract(lineBalance);
      sb.append("-").append(lineBalance);
    }
    sb.append("]");
    //
    log4jDocBank.debug(" Balance=" + retValue + sb.toString());
    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for CMB.
   * 
   * <pre>
   *      BankAsset       DR      CR  (Statement)
   *      BankInTransit   DR      CR              (Payment)
   *      Charge          DR          (Charge)
   *      Interest        DR      CR  (Interest)
   * </pre>
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
        DocBankTemplate newTemplate = (DocBankTemplate) Class.forName(strClassname).newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocBankTemplate - " + e);
      }
    }
    log4jDocBank.debug("createFact - Inicio");
    // create Fact Header
    Fact fact = null;
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    log4jDocBank.debug("createFact - object created");
    // Header -- there may be different currency amounts
    BigDecimal TrxAmt = null;
    BigDecimal ChargeAmt = null;
    BigDecimal ConvertChargeAmt = null;
    String strDateAcct = "FirstIteration";
    // BigDecimal InterestAmt = null;
    // Lines
    fact = new Fact(this, as, Fact.POST_Actual);
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_Bank line = (DocLine_Bank) p_lines[i];
      if (strDateAcct.equals("FirstIteration"))
        strDateAcct = line.m_DateAcct;
      else if (!strDateAcct.equals(line.m_DateAcct)) {
        strDateAcct = line.m_DateAcct;
        Fact_Acct_Group_ID = SequenceIdData.getUUID();
      }

      // setC_Period_ID(line.m_DateAcct);
      // BankAsset DR CR (Statement)
      TrxAmt = new BigDecimal(line.m_TrxAmt);
      ChargeAmt = new BigDecimal(line.chargeAmt);
      ConvertChargeAmt = new BigDecimal(line.convertChargeAmt);
      log4jDocBank.debug("createFact - p_lines.length = " + p_lines.length + " - i=" + i);
      log4jDocBank.debug("createFact - Record_ID = " + Record_ID);
      log4jDocBank.debug("createFact - C_BPARTNER_ID = " + line.m_C_BPartner_ID);
      // log4jDocBank.debug("createFact - BPARTNER_ACCT = " +
      // getAccountBPartner(line.m_C_BPartner_ID, as,conn, true));
      log4jDocBank.debug("createFact - PAYMENT_ID = " + line.m_C_Payment_ID);
      log4jDocBank.debug("createFact - isManual = " + line.isManual);

      // InterestAmt = new BigDecimal(line.m_InterestAmt);
      DocLine_Bank lineAux = new DocLine_Bank(DocumentType, Record_ID, null);
      lineAux.m_DateAcct = line.m_DateAcct;
      lineAux.m_C_Project_ID = line.m_C_Project_ID;
      lineAux.m_TrxLine_ID = line.m_TrxLine_ID;
      fact.createLine(lineAux, getAccount(AcctServer.ACCTTYPE_BankAsset, as, conn),
          line.m_C_Currency_ID, line.m_StmtAmt, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
          conn);
      // BankInTransit DR CR (Payment)
      /*
       * if (!line.m_C_Payment_ID.equals("") && !line.isManual.equals("Y"))
       * fact.createLine(line,getAccountBPartner(line.m_C_BPartner_ID, as,conn,
       * (TrxAmt.negate().compareTo(new BigDecimal("0"))>0?false:true)),line.m_C_Currency_ID,
       * TrxAmt.negate().toString(), conn);
       */
      if (line.m_C_Payment_ID.equals("") && !line.m_C_GLItem_ID.equals(""))
        fact.createLine(line, line.getGlitemAccount(as, new BigDecimal(getAmount()), conn),
            line.m_C_Currency_ID, TrxAmt.signum() == -1 ? TrxAmt.negate().toString() : "",
            TrxAmt.signum() == -1 ? "" : TrxAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      else
        fact.createLine(line, getAccountBankInTransit(line.m_TrxLine_ID, as, conn),
            line.m_C_Currency_ID, TrxAmt.negate().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      // Charge DR (Charge)
      fact.createLine(
          lineAux,
          new Account(conn, DocLineBankData.selectChargeAccount(conn, C_BankAccount_ID,
              as.m_C_AcctSchema_ID)), line.m_C_Currency_ID, ChargeAmt.toString(), "",
          Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      // Interest DR CR (Interest)
      /*
       * if (InterestAmt.signum() < 0)
       * fact.createLine(line,getAccount(AcctServer.ACCTTYPE_InterestExp, as, conn),
       * getAccount(AcctServer.ACCTTYPE_InterestExp, as, conn),line.m_C_Currency_ID,
       * InterestAmt.negate().toString(), conn); else
       * fact.createLine(line,getAccount(AcctServer.ACCTTYPE_InterestExp, as, conn),
       * getAccount(AcctServer.ACCTTYPE_InterestRev, as, conn),line.m_C_Currency_ID,
       * InterestAmt.negate().toString(), conn);
       */
      //
      if (ConvertChargeAmt.signum() > 0) // >0 loss
        fact.createLine(lineAux, getAccount(AcctServer.ACCTTYPE_ConvertChargeLossAmt, as, conn),
            line.m_C_Currency_ID, line.convertChargeAmt, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
      else
        fact.createLine(lineAux, getAccount(AcctServer.ACCTTYPE_ConvertChargeGainAmt, as, conn),
            line.m_C_Currency_ID, "", ConvertChargeAmt.negate().toString(), Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, conn);

      log4jDocBank.debug("createTaxCorrection - (NIY)");
    }
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * Get the account for Accounting Schema
   * 
   * @param strcBankstatementlineId
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountBankInTransit(String strcBankstatementlineId, AcctSchema as,
      ConnectionProvider conn) {
    DocBankData[] data = null;
    try {
      data = DocBankData.selectBankInTransitAcct(conn, strcBankstatementlineId,
          as.getC_AcctSchema_ID());
    } catch (ServletException e) {
      log4j.warn(e);
    }
    // Get Acct
    String Account_ID = "";
    if (data != null && data.length != 0) {
      Account_ID = data[0].accountId;
    } else
      return null;
    // No account
    if (Account_ID.equals("")) {
      log4j.warn("DocBank - getAccountBankStatementLine - NO account BankStatementLine="
          + strcBankstatementlineId + ", Record=" + Record_ID);
      return null;
    }
    // Return Account
    Account acct = null;
    try {
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4j.warn(e);
    }
    return acct;
  } // getAccount

  public String nextSeqNo(String oldSeqNo) {
    log4jDocBank.debug("DocBank - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocBank.debug("DocBank - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    String strCount = "";
    try {
      strCount = DocLineBankData.selectLinesPeriodClosed(conn, strRecordId);
    } catch (ServletException e) {
      e.printStackTrace();
      return false;
    }
    if (!strCount.equals("0"))
      return false;
    else
      return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
