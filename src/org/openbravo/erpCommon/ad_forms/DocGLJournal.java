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
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.financialmgmt.gl.GLJournal;

public class DocGLJournal extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocGLJournal = Logger.getLogger(DocGLJournal.class);

  private String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          client
   */
  public DocGLJournal(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
    if (log4jDocGLJournal.isDebugEnabled())
      log4jDocGLJournal.debug("- DocGLJournal - OBJECT CREATED.");
  }

  public String m_PostingType = Fact.POST_Actual;

  public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id)
      throws ServletException {
    AcctSchema docAcctSchema = new AcctSchema(conn, DocGLJournalData.selectAcctSchema(conn,
        AD_Client_ID, Id));
    ArrayList<Object> list = new ArrayList<Object>();
    list.add(docAcctSchema);
    AcctSchema[] retValue = new AcctSchema[list.size()];
    list.toArray(retValue);
    this.m_as = retValue;
    setObjectFieldProvider(DocGLJournalData.select(conn, AD_Client_ID, Id));
  }

  /**
   * Load Specific Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    loadDocumentType(); // lines require doc type

    m_PostingType = data[0].getField("PostingType");
    m_IsOpening = data[0].getField("isopening");

    // Contained Objects
    p_lines = loadLines(conn);
    log4jDocGLJournal.debug("Lines=" + p_lines.length);
    return true;
  } // loadDocumentDetails

  /**
   * Load Invoice Line
   * 
   * @return DocLine Array
   */
  private DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();

    DocLineGLJournalData[] data = null;
    try {
      data = DocLineGLJournalData.select(conn, Record_ID);
      for (int i = 0; i < data.length; i++) {
        String Line_ID = data[i].glJournallineId;
        DocLine docLine = new DocLine(DocumentType, Record_ID, Line_ID);
        docLine.loadAttributes(data[i], this);
        docLine.m_Record_Id2 = data[i].cDebtPaymentId;
        // -- Source Amounts
        String AmtSourceDr = data[i].amtsourcedr;
        String AmtSourceCr = data[i].amtsourcecr;
        docLine.setAmount(AmtSourceDr, AmtSourceCr);
        // -- Converted Amounts
        String C_AcctSchema_ID = data[i].cAcctschemaId;
        String AmtAcctDr = data[i].amtacctdr;
        String AmtAcctCr = data[i].amtacctcr;
        docLine.setConvertedAmt(C_AcctSchema_ID, AmtAcctDr, AmtAcctCr);
        docLine.m_DateAcct = this.DateAcct;
        // -- Account
        String C_ValidCombination_ID = data[i].cValidcombinationId;
        Account acct = null;
        try {
          acct = new Account(conn, C_ValidCombination_ID);
        } catch (ServletException e) {
          log4jDocGLJournal.warn(e);
        }
        docLine.setAccount(acct);
        // -- Set Org from account (x-org)
        docLine.setAD_Org_ID(data[i].adOrgId);
        //
        list.add(docLine);
      }
    } catch (ServletException e) {
      log4jDocGLJournal.warn(e);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Source Currency Balance - subtracts line and tax amounts from total - no rounding
   * 
   * @return positive amount, if total invoice is bigger than lines
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    StringBuffer sb = new StringBuffer(" [");
    // Lines
    for (int i = 0; i < p_lines.length; i++) {
      retValue = retValue.add(new BigDecimal(p_lines[i].getAmount()));
      sb.append("+").append(p_lines[i].getAmount());
    }
    sb.append("]");
    //
    log4jDocGLJournal.debug(toString() + " Balance=" + retValue + sb.toString());
    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for GLJ. (only for the accounting scheme, it was created)
   * 
   * <pre>
   *      account     DR          CR
   * </pre>
   * 
   * @param as
   *          acct schema
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
        DocGLJournalTemplate newTemplate = (DocGLJournalTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocGLJournalTemplate - " + e);
      }
    }
    // create Fact Header
    Fact fact = new Fact(this, as, m_PostingType);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    // GLJ
    if (DocumentType.equals(AcctServer.DOCTYPE_GLJournal)) {
      // account DR CR
      for (int i = 0; i < p_lines.length; i++) {
        if (p_lines[i].getC_AcctSchema_ID().equals(as.getC_AcctSchema_ID())) {
          fact.createLine(p_lines[i], p_lines[i].getAccount(), C_Currency_ID,
              p_lines[i].getAmtSourceDr(), p_lines[i].getAmtSourceCr(), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
        }
      } // for all lines
    } else {
      log4jDocGLJournal.warn("createFact - " + "DocumentType unknown: " + DocumentType);
      fact = null;
    }
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * @return the log4jDocGLJournal
   */
  public static Logger getLog4jDocGLJournal() {
    return log4jDocGLJournal;
  }

  /**
   * @param log4jDocGLJournal
   *          the log4jDocGLJournal to set
   */
  public static void setLog4jDocGLJournal(Logger log4jDocGLJournal) {
    DocGLJournal.log4jDocGLJournal = log4jDocGLJournal;
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
   * @return the m_PostingType
   */
  public String getM_PostingType() {
    return m_PostingType;
  }

  /**
   * @param postingType
   *          the m_PostingType to set
   */
  public void setM_PostingType(String postingType) {
    m_PostingType = postingType;
  }

  /**
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String nextSeqNo(String oldSeqNo) {
    log4jDocGLJournal.debug("DocGLJournal - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocGLJournal.debug("DocGLJournal - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    final String STATUS_VOIDED = "VO";
    if (STATUS_VOIDED.equals(OBDal.getInstance().get(GLJournal.class, strRecordId)
        .getDocumentStatus())) {
      setStatus(STATUS_DocumentDisabled);
      return false;
    }
    return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
