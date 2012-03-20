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
 * Contributions are Copyright (C) 2001-2011 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRM_FinaccTransactionV;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.CustomerAccounts;
import org.openbravo.model.common.businesspartner.VendorAccounts;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.ReversedInvoice;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.gl.GLItemAccounts;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public abstract class AcctServer {
  static Logger log4j = Logger.getLogger(AcctServer.class);

  protected ConnectionProvider connectionProvider;

  public String batchSize = "100";

  public BigDecimal ZERO = new BigDecimal("0");

  public String groupLines = "";
  public String Qty = null;
  public String tableName = "";
  public String strDateColumn = "";
  public String AD_Table_ID = "";
  public String AD_Client_ID = "";
  public String AD_Org_ID = "";
  public String Status = "";
  public String C_BPartner_ID = "";
  public String C_BPartner_Location_ID = "";
  public String M_Product_ID = "";
  public String AD_OrgTrx_ID = "";
  public String C_SalesRegion_ID = "";
  public String C_Project_ID = "";
  public String C_Campaign_ID = "";
  public String C_Activity_ID = "";
  public String C_LocFrom_ID = "";
  public String C_LocTo_ID = "";
  public String User1_ID = "";
  public String User2_ID = "";
  public String Name = "";
  public String DocumentNo = "";
  public String DateAcct = "";
  public String DateDoc = "";
  public String C_Period_ID = "";
  public String C_Currency_ID = "";
  public String C_DocType_ID = "";
  public String C_Charge_ID = "";
  public String ChargeAmt = "";
  public String C_BankAccount_ID = "";
  public String C_CashBook_ID = "";
  public String M_Warehouse_ID = "";
  public String Posted = "";
  public String DocumentType = "";
  public String TaxIncluded = "";
  public String GL_Category_ID = "";
  public String Record_ID = "";
  public String IsReversal = "";
  /** No Currency in Document Indicator */
  protected static final String NO_CURRENCY = "-1";
  // This is just for the initialization of the accounting
  public String m_IsOpening = "N";

  public Fact[] m_fact = null;
  public AcctSchema[] m_as = null;

  private FieldProvider objectFieldProvider[];

  public String[] Amounts = new String[4];

  // Conversion Rate precision. defaulted to 6 as it is stated in Format.xml
  int conversionRatePrecision = 6;

  public DocLine[] p_lines = new DocLine[0];
  public DocLine_Payment[] m_debt_payments = new DocLine_Payment[0];

  /**
   * Is (Source) Multi-Currency Document - i.e. the document has different currencies (if true, the
   * document will not be source balanced)
   */
  public boolean MultiCurrency = false;

  /** Amount Type - Invoice */
  public static final int AMTTYPE_Gross = 0;
  public static final int AMTTYPE_Net = 1;
  public static final int AMTTYPE_Charge = 2;
  /** Amount Type - Allocation */
  public static final int AMTTYPE_Invoice = 0;
  public static final int AMTTYPE_Allocation = 1;
  public static final int AMTTYPE_Discount = 2;
  public static final int AMTTYPE_WriteOff = 3;

  /** Document Status */
  public static final String STATUS_NotPosted = "N";
  /** Document Status */
  public static final String STATUS_NotBalanced = "b";
  /** Document Status */
  public static final String STATUS_NotConvertible = "c";
  /** Document Status */
  public static final String STATUS_PeriodClosed = "p";
  /** Document Status */
  public static final String STATUS_InvalidAccount = "i";
  /** Document Status */
  public static final String STATUS_PostPrepared = "y";
  /** Document Status */
  public static final String STATUS_Posted = "Y";
  /** Document Status */
  public static final String STATUS_Error = "E";
  /** Document Status */
  public static final String STATUS_InvalidCost = "C";
  /** Document Status */
  public static final String STATUS_DocumentLocked = "L";
  /** Document Status */
  public static final String STATUS_DocumentDisabled = "D";
  /** Document Status */
  public static final String STATUS_TableDisabled = "T";
  /** Document Status */
  public static final String STATUS_BackgroundDisabled = "d";

  /** Table IDs for document level conversion rates */
  public static final String TABLEID_Invoice = "318";
  public static final String TABLEID_Payment = "D1A97202E832470285C9B1EB026D54E2";
  public static final String TABLEID_Transaction = "4D8C3B3C31D1410DA046140C9F024D17";
  public static final String TABLEID_Reconciliation = "B1B7075C46934F0A9FD4C4D0F1457B42";

  @Deprecated
  // Use TABLEID_Invoice instead
  public static final String EXCHANGE_DOCTYPE_Invoice = "318";
  @Deprecated
  // Use TABLEID_Payment instead
  public static final String EXCHANGE_DOCTYPE_Payment = "D1A97202E832470285C9B1EB026D54E2";
  @Deprecated
  // Use TABLEID_Transaction instead
  public static final String EXCHANGE_DOCTYPE_Transaction = "4D8C3B3C31D1410DA046140C9F024D17";

  OBError messageResult = null;
  String strMessage = null;

  /** AR Invoices */
  public static final String DOCTYPE_ARInvoice = "ARI";
  /** AR Credit Memo */
  public static final String DOCTYPE_ARCredit = "ARC";
  /** AR Receipt */
  public static final String DOCTYPE_ARReceipt = "ARR";
  /** AR ProForma */
  public static final String DOCTYPE_ARProForma = "ARF";

  /** AP Invoices */
  public static final String DOCTYPE_APInvoice = "API";
  /** AP Credit Memo */
  public static final String DOCTYPE_APCredit = "APC";
  /** AP Payment */
  public static final String DOCTYPE_APPayment = "APP";

  /** CashManagement Bank Statement */
  public static final String DOCTYPE_BankStatement = "CMB";
  /** CashManagement Cash Journals */
  public static final String DOCTYPE_CashJournal = "CMC";
  /** CashManagement Allocations */
  public static final String DOCTYPE_Allocation = "CMA";

  /** Amortization */
  public static final String DOCTYPE_Amortization = "AMZ";

  /** Material Shipment */
  public static final String DOCTYPE_MatShipment = "MMS";
  /** Material Receipt */
  public static final String DOCTYPE_MatReceipt = "MMR";
  /** Material Inventory */
  public static final String DOCTYPE_MatInventory = "MMI";
  /** Material Movement */
  public static final String DOCTYPE_MatMovement = "MMM";
  /** Material Production */
  public static final String DOCTYPE_MatProduction = "MMP";

  /** Match Invoice */
  public static final String DOCTYPE_MatMatchInv = "MXI";
  /** Match PO */
  public static final String DOCTYPE_MatMatchPO = "MXP";

  /** GL Journal */
  public static final String DOCTYPE_GLJournal = "GLJ";

  /** Purchase Order */
  public static final String DOCTYPE_POrder = "POO";
  /** Sales Order */
  public static final String DOCTYPE_SOrder = "SOO";

  // DPManagement
  public static final String DOCTYPE_DPManagement = "DPM";

  // FinAccTransaction
  public static final String DOCTYPE_FinAccTransaction = "FAT";
  // FinReconciliation
  public static final String DOCTYPE_Reconciliation = "REC";
  // FinBankStatement
  public static final String DOCTYPE_FinBankStatement = "BST";

  /*************************************************************************/

  /** Account Type - Invoice */
  public static final String ACCTTYPE_Charge = "0";
  public static final String ACCTTYPE_C_Receivable = "1";
  public static final String ACCTTYPE_V_Liability = "2";
  public static final String ACCTTYPE_V_Liability_Services = "3";

  /** Account Type - Payment */
  public static final String ACCTTYPE_UnallocatedCash = "10";
  public static final String ACCTTYPE_BankInTransit = "11";
  public static final String ACCTTYPE_PaymentSelect = "12";
  public static final String ACCTTYPE_WriteOffDefault = "13";
  public static final String ACCTTYPE_WriteOffDefault_Revenue = "63";
  public static final String ACCTTYPE_BankInTransitDefault = "14";
  public static final String ACCTTYPE_ConvertChargeDefaultAmt = "15";
  public static final String ACCTTYPE_ConvertGainDefaultAmt = "16";

  /** Account Type - Cash */
  public static final String ACCTTYPE_CashAsset = "20";
  public static final String ACCTTYPE_CashTransfer = "21";
  public static final String ACCTTYPE_CashExpense = "22";
  public static final String ACCTTYPE_CashReceipt = "23";
  public static final String ACCTTYPE_CashDifference = "24";

  /** Account Type - Allocation */
  public static final String ACCTTYPE_DiscountExp = "30";
  public static final String ACCTTYPE_DiscountRev = "31";
  public static final String ACCTTYPE_WriteOff = "32";
  public static final String ACCTTYPE_WriteOff_Revenue = "64";

  /** Account Type - Bank Statement */
  public static final String ACCTTYPE_BankAsset = "40";
  public static final String ACCTTYPE_InterestRev = "41";
  public static final String ACCTTYPE_InterestExp = "42";
  public static final String ACCTTYPE_ConvertChargeLossAmt = "43";
  public static final String ACCTTYPE_ConvertChargeGainAmt = "44";

  /** Inventory Accounts */
  public static final String ACCTTYPE_InvDifferences = "50";
  public static final String ACCTTYPE_NotInvoicedReceipts = "51";

  /** Project Accounts */
  public static final String ACCTTYPE_ProjectAsset = "61";
  public static final String ACCTTYPE_ProjectWIP = "62";

  /** GL Accounts */
  public static final String ACCTTYPE_PPVOffset = "60";

  // Reference (to find SalesRegion from BPartner)
  public String BP_C_SalesRegion_ID = ""; // set in FactLine

  public int errors = 0;
  int success = 0;
  // Distinguish background process
  boolean isBackground = false;

  /**
   * Constructor
   * 
   * @param m_AD_Client_ID
   *          Client ID of these Documents
   * @param connectionProvider
   *          Provider for db connections.
   */
  public AcctServer(String m_AD_Client_ID, String m_AD_Org_ID, ConnectionProvider connectionProvider) {
    AD_Client_ID = m_AD_Client_ID;
    AD_Org_ID = m_AD_Org_ID;
    this.connectionProvider = connectionProvider;
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - LOADING ARRAY: " + m_AD_Client_ID);
    m_as = AcctSchema.getAcctSchemaArray(connectionProvider, m_AD_Client_ID, m_AD_Org_ID);
  } //

  /*
   * Empty constructor to initialize the class using reflexion, set() method should be called
   * afterwards.
   */

  public AcctServer() {

  }

  public void setBatchSize(String newbatchSize) {
    batchSize = newbatchSize;
  }

  public void run(VariablesSecureApp vars) throws IOException, ServletException {
    if (AD_Client_ID.equals(""))
      AD_Client_ID = vars.getClient();
    Connection con = null;
    try {
      String strIDs = "";

      if (log4j.isDebugEnabled()) {
        log4j.debug("AcctServer - Run - TableName = " + tableName);
      }

      log4j.debug("AcctServer.run - AD_Client_ID: " + AD_Client_ID);
      AcctServerData[] data = AcctServerData.select(connectionProvider, tableName, AD_Client_ID,
          AD_Org_ID, strDateColumn, 0, Integer.valueOf(batchSize).intValue());

      if (data != null && data.length > 0) {
        if (log4j.isDebugEnabled()) {
          log4j.debug("AcctServer - Run -Select inicial realizada N = " + data.length + " - Key: "
              + data[0].id);
        }
      }

      for (int i = 0; data != null && i < data.length; i++) {
        con = connectionProvider.getTransactionConnection();
        strIDs += data[i].getField("ID") + ", ";
        this.setMessageResult(null);
        if (!post(data[i].getField("ID"), false, vars, connectionProvider, con)) {
          connectionProvider.releaseRollbackConnection(con);
          return;
        } else {
          connectionProvider.releaseCommitConnection(con);
        }
      }
      if (log4j.isDebugEnabled() && data != null)
        log4j.debug("AcctServer - Run -" + data.length + " IDs [" + strIDs + "]");
      // Create Automatic Matching
      // match (vars, this,con);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable", ex);
    } catch (SQLException ex2) {
      try {
        connectionProvider.releaseRollbackConnection(con);
      } catch (SQLException se) {
        log4j.error("Failed to close connection after an error", se);
      }
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@"
          + ex2.getMessage(), ex2);
    } catch (Exception ex3) {
      log4j.error("Exception in AcctServer.run", ex3);
      try {
        connectionProvider.releaseRollbackConnection(con);
      } catch (SQLException se) {
        log4j.error("Failed to close connection after an error", se);
      }
    }
  }

  /**
   * @return the isBackground
   */
  public boolean isBackground() {
    return isBackground;
  }

  /**
   * @param isBackground
   *          the isBackground to set
   */
  public void setBackground(boolean isBackground) {
    this.isBackground = isBackground;
  }

  /**
   * Factory - Create Posting document
   * 
   * @param AD_Table_ID
   *          Table ID of Documents
   * @param AD_Client_ID
   *          Client ID of Documents
   * @param connectionProvider
   *          Database connection provider
   * @return Document
   */
  public static AcctServer get(String AD_Table_ID, String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) throws ServletException {
    AcctServer acct = null;
    if (log4j.isDebugEnabled())
      log4j.debug("get - table: " + AD_Table_ID);
    if (AD_Table_ID.equals("318") || AD_Table_ID.equals("800060") || AD_Table_ID.equals("800176")
        || AD_Table_ID.equals("407") || AD_Table_ID.equals("392") || AD_Table_ID.equals("259")
        || AD_Table_ID.equals("800019") || AD_Table_ID.equals("319") || AD_Table_ID.equals("321")
        || AD_Table_ID.equals("323") || AD_Table_ID.equals("325") || AD_Table_ID.equals("224")
        || AD_Table_ID.equals("472")) {
      switch (Integer.parseInt(AD_Table_ID)) {
      case 318:
        acct = new DocInvoice(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "C_Invoice";
        acct.AD_Table_ID = "318";
        acct.strDateColumn = "DateAcct";
        acct.reloadAcctSchemaArray();
        acct.groupLines = AcctServerData.selectGroupLines(acct.connectionProvider, AD_Client_ID);
        break;
      /*
       * case 390: acct = new DocAllocation (AD_Client_ID); acct.strDateColumn = "";
       * acct.AD_Table_ID = "390"; acct.reloadAcctSchemaArray(); acct.break;
       */
      case 800060:
        acct = new DocAmortization(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "A_Amortization";
        acct.AD_Table_ID = "800060";
        acct.strDateColumn = "DateAcct";
        acct.reloadAcctSchemaArray();
        break;

      case 800176:
        if (log4j.isDebugEnabled())
          log4j.debug("AcctServer - Get DPM");
        acct = new DocDPManagement(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "C_DP_Management";
        acct.AD_Table_ID = "800176";
        acct.strDateColumn = "DateAcct";
        acct.reloadAcctSchemaArray();
        break;
      case 407:
        acct = new DocCash(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "C_Cash";
        acct.strDateColumn = "DateAcct";
        acct.AD_Table_ID = "407";
        acct.reloadAcctSchemaArray();
        break;
      case 392:
        acct = new DocBank(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "C_Bankstatement";
        acct.strDateColumn = "StatementDate";
        acct.AD_Table_ID = "392";
        acct.reloadAcctSchemaArray();
        break;
      case 259:
        acct = new DocOrder(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "C_Order";
        acct.strDateColumn = "DateAcct";
        acct.AD_Table_ID = "259";
        acct.reloadAcctSchemaArray();
        break;
      case 800019:
        acct = new DocPayment(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "C_Settlement";
        acct.strDateColumn = "Dateacct";
        acct.AD_Table_ID = "800019";
        acct.reloadAcctSchemaArray();
        break;
      case 319:
        acct = new DocInOut(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "M_InOut";
        acct.strDateColumn = "DateAcct";
        acct.AD_Table_ID = "319";
        acct.reloadAcctSchemaArray();
        break;
      case 321:
        acct = new DocInventory(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "M_Inventory";
        acct.strDateColumn = "MovementDate";
        acct.AD_Table_ID = "321";
        acct.reloadAcctSchemaArray();
        break;
      case 323:
        acct = new DocMovement(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "M_Movement";
        acct.strDateColumn = "MovementDate";
        acct.AD_Table_ID = "323";
        acct.reloadAcctSchemaArray();
        break;
      case 325:
        acct = new DocProduction(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "M_Production";
        acct.strDateColumn = "MovementDate";
        acct.AD_Table_ID = "325";
        acct.reloadAcctSchemaArray();
        break;
      case 224:
        if (log4j.isDebugEnabled())
          log4j.debug("AcctServer - Before OBJECT CREATION");
        acct = new DocGLJournal(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "GL_Journal";
        acct.strDateColumn = "DateAcct";
        acct.AD_Table_ID = "224";
        acct.reloadAcctSchemaArray();
        break;
      case 472:
        acct = new DocMatchInv(AD_Client_ID, AD_Org_ID, connectionProvider);
        acct.tableName = "M_MatchInv";
        acct.strDateColumn = "DateTrx";
        acct.AD_Table_ID = "472";
        acct.reloadAcctSchemaArray();
        break;
      // case 473: acct = new
      // DocMatchPO (AD_Client_ID); acct.strDateColumn = "MovementDate";
      // acct.reloadAcctSchemaArray(); break; case DocProjectIssue.AD_TABLE_ID: acct = new
      // DocProjectIssue (AD_Client_ID); acct.strDateColumn = "MovementDate";
      // acct.reloadAcctSchemaArray(); break;

      }
    } else {
      AcctServerData[] acctinfo = AcctServerData.getTableInfo(connectionProvider, AD_Table_ID);
      if (acctinfo != null && acctinfo.length != 0) {
        if (!acctinfo[0].acctclassname.equals("") && !acctinfo[0].acctdatecolumn.equals("")) {
          try {
            acct = (AcctServer) Class.forName(acctinfo[0].acctclassname).newInstance();
            acct.set(AD_Table_ID, AD_Client_ID, AD_Org_ID, connectionProvider,
                acctinfo[0].tablename, acctinfo[0].acctdatecolumn);
            acct.reloadAcctSchemaArray();
          } catch (Exception e) {
            log4j.error("Error while creating new instance for AcctServer - " + e, e);
          }
        }
      }
    }

    if (acct == null)
      log4j.warn("AcctServer - get - Unknown AD_Table_ID=" + AD_Table_ID);
    else if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - get - AcctSchemaArray length=" + (acct.m_as).length);
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - get - AD_Table_ID=" + AD_Table_ID);
    return acct;
  } // get

  public void set(String m_AD_Table_ID, String m_AD_Client_ID, String m_AD_Org_ID,
      ConnectionProvider connectionProvider, String tablename, String acctdatecolumn) {
    AD_Client_ID = m_AD_Client_ID;
    AD_Org_ID = m_AD_Org_ID;
    this.connectionProvider = connectionProvider;
    tableName = tablename;
    strDateColumn = acctdatecolumn;
    AD_Table_ID = m_AD_Table_ID;
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - LOADING ARRAY: " + m_AD_Client_ID);
    m_as = AcctSchema.getAcctSchemaArray(connectionProvider, m_AD_Client_ID, m_AD_Org_ID);
  }

  public void reloadAcctSchemaArray() throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - reloadAcctSchemaArray - " + AD_Table_ID);
    AcctSchema acct = null;
    ArrayList<Object> new_as = new ArrayList<Object>();
    for (int i = 0; i < (this.m_as).length; i++) {
      acct = m_as[i];
      if (AcctSchemaData.selectAcctSchemaTable(connectionProvider, acct.m_C_AcctSchema_ID,
          AD_Table_ID)) {
        new_as.add(new AcctSchema(connectionProvider, acct.m_C_AcctSchema_ID));
      }
    }
    AcctSchema[] retValue = new AcctSchema[new_as.size()];
    new_as.toArray(retValue);
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - RELOADING ARRAY: " + retValue.length);
    this.m_as = retValue;
  }

  private void reloadAcctSchemaArray(String adOrgId) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j
          .debug("AcctServer - reloadAcctSchemaArray - " + AD_Table_ID + ", AD_ORG_ID: " + adOrgId);
    AcctSchema acct = null;
    ArrayList<Object> new_as = new ArrayList<Object>();
    // We reload again all the acct schemas of the client
    m_as = AcctSchema.getAcctSchemaArray(connectionProvider, AD_Client_ID, AD_Org_ID);
    // Filter the right acct schemas for the organization
    for (int i = 0; i < (this.m_as).length; i++) {
      acct = m_as[i];
      if (AcctSchemaData.selectAcctSchemaTable2(connectionProvider, acct.m_C_AcctSchema_ID,
          AD_Table_ID, adOrgId)) {
        new_as.add(new AcctSchema(connectionProvider, acct.m_C_AcctSchema_ID));
      }
    }
    AcctSchema[] retValue = new AcctSchema[new_as.size()];
    new_as.toArray(retValue);
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - RELOADING ARRAY: " + retValue.length);
    this.m_as = retValue;
  }

  public boolean post(String strClave, boolean force, VariablesSecureApp vars,
      ConnectionProvider conn, Connection con) throws ServletException {
    Record_ID = strClave;
    if (log4j.isDebugEnabled())
      log4j.debug("post " + strClave + " tablename: " + tableName);
    try {
      if (AcctServerData.update(conn, tableName, strClave) != 1) {
        log4j.warn("AcctServer - Post -Cannot lock Document - ignored: " + tableName + "_ID="
            + strClave);
        setStatus(STATUS_DocumentLocked); // Status locked document
        this.setMessageResult(conn, vars, STATUS_DocumentLocked, "Error");
        return false;
      } else
        AcctServerData.delete(connectionProvider, AD_Table_ID, Record_ID);
      if (log4j.isDebugEnabled())
        log4j.debug("AcctServer - Post -TableName -" + tableName + "- ad_client_id -"
            + AD_Client_ID + "- " + tableName + "_id -" + strClave);
      try {
        loadObjectFieldProvider(connectionProvider, AD_Client_ID, strClave);
      } catch (ServletException e) {
        log4j.warn(e);
        e.printStackTrace();
      }
      FieldProvider data[] = getObjectFieldProvider();
      try {
        if (getDocumentConfirmation(conn, Record_ID) && post(data, force, vars, conn, con)) {
          success++;
        } else {
          errors++;
          if (messageResult == null)
            setMessageResult(conn, vars, getStatus(), "");
          save(conn, vars.getUser());
        }
      } catch (Exception e) {
        errors++;
        Status = AcctServer.STATUS_Error;
        save(conn, vars.getUser());
        log4j.warn(e);
        e.printStackTrace();
      }
    } catch (ServletException e) {
      log4j.error(e);
      return false;
    }
    return true;
  }

  private boolean post(FieldProvider[] data, boolean force, VariablesSecureApp vars,
      ConnectionProvider conn, Connection con) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("post data" + C_Currency_ID);
    if (!loadDocument(data, force, conn, con)) {
      log4j.warn("AcctServer - post - Error loading document");
      return false;
    }
    // Set Currency precision
    conversionRatePrecision = getConversionRatePrecision(vars);
    if (data == null || data.length == 0)
      return false;
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - Post - Antes de getAcctSchemaArray - C_CURRENCY_ID = "
    // + C_Currency_ID);
    // Create Fact per AcctSchema
    // if (log4j.isDebugEnabled()) log4j.debug("POSTLOADING ARRAY: " +
    // AD_Client_ID);
    if (!DocumentType.equals(DOCTYPE_GLJournal))
      // m_as = AcctSchema.getAcctSchemaArray(conn, AD_Client_ID, AD_Org_ID);
      reloadAcctSchemaArray(AD_Org_ID);
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - Post - Antes de new Fact - C_CURRENCY_ID = "
    // + C_Currency_ID);
    m_fact = new Fact[m_as.length];
    // AcctSchema Table check
    boolean isTableActive = false;
    try {
      OBContext.setAdminMode(true);
      for (AcctSchema as : m_as) {
        AcctSchemaTable table = null;
        OBCriteria<AcctSchemaTable> criteria = OBDao.getFilteredCriteria(AcctSchemaTable.class,
            Restrictions.eq("accountingSchema.id", as.getC_AcctSchema_ID()),
            Restrictions.eq("table.id", AD_Table_ID));
        criteria.setFilterOnReadableClients(false);
        criteria.setFilterOnReadableOrganization(false);
        table = (AcctSchemaTable) criteria.uniqueResult();
        if (table != null) {
          isTableActive = true;
          break;
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    if (!isTableActive) {
      setMessageResult(conn, vars, STATUS_TableDisabled, "Warning");
      return false;
    }
    // for all Accounting Schema
    boolean OK = true;
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - Post -Beforde the loop - C_CURRENCY_ID = " + C_Currency_ID);
    for (int i = 0; OK && i < m_as.length; i++) {
      setStatus(STATUS_NotPosted);
      if (isBackground && !isBackGroundEnabled(conn, m_as[i], AD_Table_ID)) {
        setStatus(STATUS_BackgroundDisabled);
        break;
      }
      if (log4j.isDebugEnabled())
        log4j.debug("AcctServer - Post - Before the postLogic - C_CURRENCY_ID = " + C_Currency_ID);
      Status = postLogic(i, conn, con, vars, m_as[i]);
      if (log4j.isDebugEnabled())
        log4j.debug("AcctServer - Post - After postLogic");
      if (!Status.equals(STATUS_Posted))
        return false;
    }
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - Post - Before the postCommit - C_CURRENCY_ID = " + C_Currency_ID);
    for (int i = 0; i < m_fact.length; i++)
      if (m_fact[i] != null && (m_fact[i].getLines() == null || m_fact[i].getLines().length == 0))
        return false;
    // commitFact
    Status = postCommit(Status, conn, vars, con);

    // dispose facts
    for (int i = 0; i < m_fact.length; i++)
      if (m_fact[i] != null)
        m_fact[i].dispose();
    p_lines = null;
    return Status.equals(STATUS_Posted);
  } // post

  boolean isBackGroundEnabled(ConnectionProvider conn, AcctSchema acctSchema, String adTableId)
      throws ServletException {
    return AcctServerData.selectBackgroundEnabled(conn, acctSchema.m_C_AcctSchema_ID, adTableId);
  }

  /**
   * Post Commit. Save Facts & Document
   * 
   * @param status
   *          status
   * @return Posting Status
   */
  private final String postCommit(String status, ConnectionProvider conn, VariablesSecureApp vars,
      Connection con) throws ServletException {
    log4j.debug("AcctServer - postCommit Sta=" + status + " DT=" + DocumentType + " ID="
        + Record_ID);
    Status = status;
    try {
      // *** Transaction Start ***
      // Commit Facts
      if (Status.equals(AcctServer.STATUS_Posted)) {
        if (m_fact != null && m_fact.length != 0) {
          log4j.debug("AcctServer - postCommit - m_fact.length = " + m_fact.length);
          for (int i = 0; i < m_fact.length; i++) {
            if (m_fact[i] != null && m_fact[i].save(con, conn, vars))
              ;
            else {
              // conn.releaseRollbackConnection(con);
              unlock(conn);
              Status = AcctServer.STATUS_Error;
            }
          }
        }
      }
      // Commit Doc
      if (!save(conn, vars.getUser())) { // contains unlock
        // conn.releaseRollbackConnection(con);
        unlock(conn);
        // Status = AcctServer.STATUS_Error;
      }
      // conn.releaseCommitConnection(con);
      // *** Transaction End ***
    } catch (Exception e) {
      log4j.warn("AcctServer - postCommit" + e);
      Status = AcctServer.STATUS_Error;
      // conn.releaseRollbackConnection(con);
      unlock(conn);
    }
    return Status;
  } // postCommit

  /**
   * Save to Disk - set posted flag
   * 
   * @param con
   *          connection
   * @param strUser
   *          AD_User_ID
   * @return true if saved
   */
  private final boolean save(ConnectionProvider conn, String strUser) {
    int no = 0;
    try {
      no = AcctServerData.updateSave(conn, tableName, Status, strUser, Record_ID);
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    return no == 1;
  } // save

  /**
   * Unlock Document
   */
  private void unlock(ConnectionProvider conn) {
    try {
      AcctServerData.updateUnlock(conn, tableName, Record_ID);
    } catch (ServletException e) {
      log4j.warn("AcctServer - Document locked: -" + e);
    }
  } // unlock

  @Deprecated
  // Deprecated in 2.50 because of a missing connection needed
  public boolean loadDocument(FieldProvider[] data, boolean force, ConnectionProvider conn) {
    try {
      Connection con = conn.getConnection();
      return loadDocument(data, force, conn, con);
    } catch (NoConnectionAvailableException e) {
      log4j.warn(e);
      e.printStackTrace();
      return false;
    }
  }

  public boolean loadDocument(FieldProvider[] data, boolean force, ConnectionProvider conn,
      Connection con) {
    if (log4j.isDebugEnabled())
      log4j.debug("loadDocument " + data.length);

    setStatus(STATUS_NotPosted);
    Name = "";
    AD_Client_ID = data[0].getField("AD_Client_ID");
    AD_Org_ID = data[0].getField("AD_Org_ID");
    C_BPartner_ID = data[0].getField("C_BPartner_ID");
    M_Product_ID = data[0].getField("M_Product_ID");
    AD_OrgTrx_ID = data[0].getField("AD_OrgTrx_ID");
    C_SalesRegion_ID = data[0].getField("C_SalesRegion_ID");
    C_Project_ID = data[0].getField("C_Project_ID");
    C_Campaign_ID = data[0].getField("C_Campaign_ID");
    C_Activity_ID = data[0].getField("C_Activity_ID");
    C_LocFrom_ID = data[0].getField("C_LocFrom_ID");
    C_LocTo_ID = data[0].getField("C_LocTo_ID");
    User1_ID = data[0].getField("User1_ID");
    User2_ID = data[0].getField("User2_ID");

    Name = data[0].getField("Name");
    DocumentNo = data[0].getField("DocumentNo");
    DateAcct = data[0].getField("DateAcct");
    DateDoc = data[0].getField("DateDoc");
    C_Period_ID = data[0].getField("C_Period_ID");
    C_Currency_ID = data[0].getField("C_Currency_ID");
    C_DocType_ID = data[0].getField("C_DocType_ID");
    C_Charge_ID = data[0].getField("C_Charge_ID");
    ChargeAmt = data[0].getField("ChargeAmt");
    C_BankAccount_ID = data[0].getField("C_BankAccount_ID");
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - loadDocument - C_BankAccount_ID : " + C_BankAccount_ID);
    Posted = data[0].getField("Posted");
    if (!loadDocumentDetails(data, conn))
      loadDocumentType();
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - loadDocument - DocumentDetails Loaded");
    if ((DateAcct == null || DateAcct.equals("")) && (DateDoc != null && !DateDoc.equals("")))
      DateAcct = DateDoc;
    else if ((DateDoc == null || DateDoc.equals("")) && (DateAcct != null && !DateAcct.equals("")))
      DateDoc = DateAcct;
    // DocumentNo (or Name)
    if (DocumentNo == null || DocumentNo.length() == 0)
      DocumentNo = Name;
    // if (DocumentNo == null || DocumentNo.length() ==
    // 0)(DateDoc.equals("") && !DateAcct.equals(""))
    // DocumentNo = "";

    // Check Mandatory Info
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - loadDocument - C_Currency_ID : " +
    // C_Currency_ID);
    String error = "";
    if (AD_Table_ID == null || AD_Table_ID.equals(""))
      error += " AD_Table_ID";
    if (Record_ID == null || Record_ID.equals(""))
      error += " Record_ID";
    if (AD_Client_ID == null || AD_Client_ID.equals(""))
      error += " AD_Client_ID";
    if (AD_Org_ID == null || AD_Org_ID.equals(""))
      error += " AD_Org_ID";
    if (C_Currency_ID == null || C_Currency_ID.equals(""))
      error += " C_Currency_ID";
    if (DateAcct == null || DateAcct.equals(""))
      error += " DateAcct";
    if (DateDoc == null || DateDoc.equals(""))
      error += " DateDoc";
    if (error.length() > 0) {
      log4j.warn("AcctServer - loadDocument - " + DocumentNo + " - Mandatory info missing: "
          + error);
      return false;
    }

    // Delete existing Accounting
    if (force) {
      if (Posted.equals("Y") && !isPeriodOpen()) { // already posted -
        // don't delete if
        // period closed
        log4j.warn("AcctServer - loadDocument - " + DocumentNo
            + " - Period Closed for already posted document");
        return false;
      }
      // delete it
      try {
        AcctServerData.delete(connectionProvider, AD_Table_ID, Record_ID);
      } catch (ServletException e) {
        log4j.warn(e);
        e.printStackTrace();
      }
      // if (log4j.isDebugEnabled()) log4j.debug("post - deleted=" + no);
    } else if (Posted.equals("Y")) {
      log4j.warn("AcctServer - loadDocument - " + DocumentNo + " - Document already posted");
      return false;
    }
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - loadDocument -finished");
    return true;
  } // loadDocument

  public void loadDocumentType() {
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - loadDocumentType - DocumentType: " +
    // DocumentType + " - C_DocType_ID : " + C_DocType_ID);
    try {
      if (/* DocumentType.equals("") && */C_DocType_ID != null && C_DocType_ID != "") {
        AcctServerData[] data = AcctServerData.selectDocType(connectionProvider, C_DocType_ID);
        DocumentType = data[0].docbasetype;
        GL_Category_ID = data[0].glCategoryId;
        IsReversal = data[0].isreversal;
      }
      // We have a document Type, but no GL info - search for DocType
      if (GL_Category_ID != null && GL_Category_ID.equals("")) {
        AcctServerData[] data = AcctServerData.selectGLCategory(connectionProvider, AD_Client_ID,
            DocumentType);
        if (data != null && data.length != 0) {
          GL_Category_ID = data[0].glCategoryId;
          IsReversal = data[0].isreversal;
        }
      }
      if (DocumentType != null && DocumentType.equals(""))
        log4j.warn("AcctServer - loadDocumentType - No DocType for GL Info");
      if (GL_Category_ID != null && GL_Category_ID.equals("")) {
        AcctServerData[] data = AcctServerData.selectDefaultGLCategory(connectionProvider,
            AD_Client_ID);
        GL_Category_ID = data[0].glCategoryId;
      }
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    if (GL_Category_ID != null && GL_Category_ID.equals(""))
      log4j.warn("AcctServer - loadDocumentType - No GL Info");
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - loadDocumentType -" + tableName + "_ID : "
    // + Record_ID + " - C_DocType_ID: " + C_DocType_ID +
    // " - DocumentType: " + DocumentType);
  }

  /**
   * @deprecated During cleanup for 3.0 the entire table ad_node was removed from core, so this
   *             insertNote method doesn't serve have any purpose anymore. Keep as deprecated noop
   *             in case any module may call it.
   */
  @Deprecated
  public boolean insertNote(String AD_Client_ID, String AD_Org_ID, String AD_User_ID,
      String AD_Table_ID, String Record_ID, String AD_MessageValue, String Text, String Reference,
      VariablesSecureApp vars, ConnectionProvider conn, Connection con) {
    return false;
  }

  /**
   * Posting logic for Accounting Schema index
   * 
   * @param index
   *          Accounting Schema index
   * @return posting status/error code
   */
  private final String postLogic(int index, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars, AcctSchema as) throws ServletException {
    // rejectUnbalanced
    if (!m_as[index].isSuspenseBalancing() && !isBalanced())
      return STATUS_NotBalanced;

    // rejectUnconvertible
    if (!isConvertible(m_as[index], conn))
      return STATUS_NotConvertible;

    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - Before isPeriodOpen");
    // rejectPeriodClosed
    if (!isPeriodOpen())
      return STATUS_PeriodClosed;
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - After isPeriodOpen");

    // createFacts
    try {
      m_fact[index] = createFact(m_as[index], conn, con, vars);
    } catch (Exception e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    if (!Status.equals(STATUS_NotPosted))
      return Status;
    if (m_fact[index] == null)
      return STATUS_Error;
    Status = STATUS_PostPrepared;

    // Distinguish multi-currency Documents
    MultiCurrency = m_fact[index].isMulticurrencyDocument();
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - Before balanceSource");
    // balanceSource
    if (!MultiCurrency && !m_fact[index].isSourceBalanced())
      m_fact[index].balanceSource(conn);
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - After balanceSource");

    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - Before isSegmentBalanced");
    // balanceSegments
    if (!MultiCurrency && !m_fact[index].isSegmentBalanced(conn))
      m_fact[index].balanceSegments(conn);
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - After isSegmentBalanced");

    // balanceAccounting
    if (!m_fact[index].isAcctBalanced())
      m_fact[index].balanceAccounting(conn);

    // Here processes defined to be executed at posting time, when existing, will be executed
    AcctServerData[] data = AcctServerData.selectAcctProcess(conn, as.m_C_AcctSchema_ID);
    for (int i = 0; data != null && i < data.length; i++) {
      String strClassname = data[i].classname;
      if (!strClassname.equals("")) {
        try {
          AcctProcessTemplate newTemplate = (AcctProcessTemplate) Class.forName(strClassname)
              .newInstance();
          if (!newTemplate.execute(this, as, conn, con, vars)) {
            OBDal.getInstance().rollbackAndClose();
            return getStatus();
          }
        } catch (Exception e) {
          log4j.error("Error while creating new instance for AcctProcessTemplate - " + e);
          return AcctServer.STATUS_Error;
        }
      }
    }
    if (messageResult != null)
      return getStatus();
    return STATUS_Posted;
  } // postLogic

  /**
   * Is the Source Document Balanced
   * 
   * @return true if (source) balanced
   */
  public boolean isBalanced() {
    // Multi-Currency documents are source balanced by definition
    if (MultiCurrency)
      return true;
    //
    boolean retValue = (getBalance().compareTo(ZERO) == 0);
    if (retValue) {
      if (log4j.isDebugEnabled())
        log4j.debug("AcctServer - isBalanced - " + DocumentNo);
    } else
      log4j.warn("AcctServer - is not Balanced - " + DocumentNo);
    return retValue;
  } // isBalanced

  /**
   * Is Document convertible to currency and Conversion Type
   * 
   * @param acctSchema
   *          accounting schema
   * @return true, if convertible to accounting currency
   */
  public boolean isConvertible(AcctSchema acctSchema, ConnectionProvider conn)
      throws ServletException {
    // No Currency in document
    if (NO_CURRENCY.equals(C_Currency_ID)) {
      // if (log4j.isDebugEnabled())
      // log4j.debug("AcctServer - isConvertible (none) - " + DocumentNo);
      return true;
    }
    // Get All Currencies
    Vector<Object> set = new Vector<Object>();
    set.addElement(C_Currency_ID);
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      String currency = p_lines[i].m_C_Currency_ID;
      if (currency != null && !currency.equals(""))
        set.addElement(currency);
    }

    // just one and the same
    if (set.size() == 1 && acctSchema.m_C_Currency_ID.equals(C_Currency_ID)) {
      // if (log4j.isDebugEnabled()) log4j.debug
      // ("AcctServer - isConvertible (same) Cur=" + C_Currency_ID + " - "
      // + DocumentNo);
      return true;
    }
    boolean convertible = true;
    for (int i = 0; i < set.size() && convertible == true; i++) {
      // if (log4j.isDebugEnabled()) log4j.debug
      // ("AcctServer - get currency");
      String currency = (String) set.elementAt(i);
      if (currency == null)
        currency = "";
      // if (log4j.isDebugEnabled()) log4j.debug
      // ("AcctServer - currency = " + currency);
      if (!currency.equals(acctSchema.m_C_Currency_ID)) {
        // if (log4j.isDebugEnabled()) log4j.debug
        // ("AcctServer - get converted amount (init)");
        String amt = getConvertedAmt("1", currency, acctSchema.m_C_Currency_ID, DateAcct,
            acctSchema.m_CurrencyRateType, AD_Client_ID, AD_Org_ID, conn);
        // if (log4j.isDebugEnabled()) log4j.debug
        // ("get converted amount (end)");
        if (amt == null || amt.equals("")) {
          convertible = false;
          log4j.warn("AcctServer - isConvertible NOT from " + currency + " - " + DocumentNo);
        } else if (log4j.isDebugEnabled())
          log4j.debug("AcctServer - isConvertible from " + currency);
      }
    }
    // if (log4j.isDebugEnabled()) log4j.debug
    // ("AcctServer - isConvertible=" + convertible + ", AcctSchemaCur=" +
    // acctSchema.m_C_Currency_ID + " - " + DocumentNo);
    return convertible;
  } // isConvertible

  /**
   * Get the Amount (loaded in loadDocumentDetails)
   * 
   * @param AmtType
   *          see AMTTYPE_*
   * @return Amount
   */
  public String getAmount(int AmtType) {
    if (AmtType < 0 || Amounts == null || AmtType >= Amounts.length)
      return null;
    return (Amounts[AmtType].equals("")) ? "0" : Amounts[AmtType];
  } // getAmount

  /**
   * Get Amount with index 0
   * 
   * @return Amount (primary document amount)
   */
  public String getAmount() {
    return Amounts[0];
  } // getAmount

  /**
   * Convert an amount
   * 
   * @param CurFrom_ID
   *          The C_Currency_ID FROM
   * @param CurTo_ID
   *          The C_Currency_ID TO
   * @param ConvDate
   *          The Conversion date - if null - use current date
   * @param RateType
   *          The Conversion rate type - if null/empty - use Spot
   * @param Amt
   *          The amount to be converted
   * @return converted amount
   */
  public static String getConvertedAmt(String Amt, String CurFrom_ID, String CurTo_ID,
      String ConvDate, String RateType, ConnectionProvider conn) {
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - getConvertedAmount no client nor org");
    return getConvertedAmt(Amt, CurFrom_ID, CurTo_ID, ConvDate, RateType, "", "", conn);
  }

  public static String getConvertedAmt(String Amt, String CurFrom_ID, String CurTo_ID,
      String ConvDate, String RateType, String client, String org, ConnectionProvider conn) {
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - getConvertedAmount - starting method - Amt : " + Amt
          + " - CurFrom_ID : " + CurFrom_ID + " - CurTo_ID : " + CurTo_ID + "- ConvDate: "
          + ConvDate + " - RateType:" + RateType + " - client:" + client + "- org:" + org);

    if (Amt.equals(""))
      throw new IllegalArgumentException(
          "AcctServer - getConvertedAmt - required parameter missing - Amt");
    if (CurFrom_ID.equals(CurTo_ID) || Amt.equals("0"))
      return Amt;
    AcctServerData[] data = null;
    try {
      if (ConvDate != null && ConvDate.equals(""))
        ConvDate = DateTimeData.today(conn);
      // ConvDate IN DATE
      if (RateType == null || RateType.equals(""))
        RateType = "S";
      data = AcctServerData.currencyConvert(conn, Amt, CurFrom_ID, CurTo_ID, ConvDate, RateType,
          client, org);
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    if (data == null || data.length == 0) {
      /*
       * log4j.error("No conversion ratio"); throw new
       * ServletException("No conversion ratio defined!");
       */
      return "";
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("getConvertedAmount - converted:" + data[0].converted);
      return data[0].converted;
    }
  } // getConvertedAmt

  public static BigDecimal getConvertionRate(String CurFrom_ID, String CurTo_ID, String ConvDate,
      String RateType, String client, String org, ConnectionProvider conn) {
    if (CurFrom_ID.equals(CurTo_ID))
      return BigDecimal.ONE;
    AcctServerData[] data = null;
    try {
      if (ConvDate != null && ConvDate.equals(""))
        ConvDate = DateTimeData.today(conn);
      // ConvDate IN DATE
      if (RateType == null || RateType.equals(""))
        RateType = "S";
      data = AcctServerData.currencyConvertionRate(conn, CurFrom_ID, CurTo_ID, ConvDate, RateType,
          client, org);
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    if (data == null || data.length == 0) {
      log4j.error("No conversion ratio");
      return BigDecimal.ZERO;
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("getConvertionRate - rate:" + data[0].converted);
      return new BigDecimal(data[0].converted);
    }
  } // getConvertedAmt

  /**
   * Is Period Open
   * 
   * @return true if period is open
   */
  public boolean isPeriodOpen() {
    // if (log4j.isDebugEnabled())
    // log4j.debug(" ***************************** AD_Client_ID - " +
    // AD_Client_ID + " -- DateAcct - " + DateAcct + " -- DocumentType - " +
    // DocumentType);
    setC_Period_ID();
    boolean open = (!C_Period_ID.equals(""));
    if (open) {
      if (log4j.isDebugEnabled())
        log4j.debug("AcctServer - isPeriodOpen - " + DocumentNo);
    } else {
      log4j.warn("AcctServer - isPeriodOpen NO - " + DocumentNo);
    }
    return open;
  } // isPeriodOpen

  /**
   * Calculate Period ID. Set to -1 if no period open, 0 if no period control
   */
  public void setC_Period_ID() {
    if (C_Period_ID != null)
      return;
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - setC_Period_ID - AD_Client_ID - " + AD_Client_ID + "--DateAcct - "
          + DateAcct + "--DocumentType -" + DocumentType);
    AcctServerData[] data = null;
    try {
      if (log4j.isDebugEnabled())
        log4j.debug("setC_Period_ID - inside try - AD_Client_ID - " + AD_Client_ID
            + " -- DateAcct - " + DateAcct + " -- DocumentType - " + DocumentType);
      data = AcctServerData.periodOpen(connectionProvider, AD_Client_ID, DocumentType, AD_Org_ID,
          DateAcct);
      C_Period_ID = data[0].period;
      if (log4j.isDebugEnabled())
        log4j.debug("AcctServer - setC_Period_ID - " + AD_Client_ID + "/" + DateAcct + "/"
            + DocumentType + " => " + C_Period_ID);
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
  } // setC_Period_ID

  /**
   * Matching
   * 
   * <pre>
   *  Derive Invoice-Receipt Match from PO-Invoice and PO-Receipt
   *  Purchase Order (20)
   *  - Invoice1 (10)
   *  - Invoice2 (10)
   *  - Receipt1 (5)
   *  - Receipt2 (15)
   *  (a) Creates Directs
   *      - Invoice1 - Receipt1 (5)
   *      - Invoice2 - Receipt2 (10)
   *  (b) Creates Indirects
   *      - Invoice1 - Receipt2 (5)
   *  (Not imlemented)
   * 
   * 
   * </pre>
   * 
   * @return number of records created
   */
  public int match(VariablesSecureApp vars, ConnectionProvider conn, Connection con) {
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - Match--Starting");
    int counter = 0;
    // (a) Direct Matches
    AcctServerData[] data = null;
    try {
      data = AcctServerData.selectMatch(conn, AD_Client_ID);
      for (int i = 0; i < data.length; i++) {
        BigDecimal qty1 = new BigDecimal(data[i].qty1);
        BigDecimal qty2 = new BigDecimal(data[i].qty2);
        BigDecimal Qty = qty1.min(qty2);
        if (Qty.toString().equals("0"))
          continue;
        // if (log4j.isDebugEnabled())
        // log4j.debug("AcctServer - Match--dateTrx1 :->" + data[i].datetrx1
        // + "Match--dateTrx2: ->" + data[i].datetrx2);
        String dateTrx1 = data[i].datetrx1;
        String dateTrx2 = data[i].datetrx2;
        String compare = "";
        try {
          compare = DateTimeData.compare(conn, dateTrx1, dateTrx2);
        } catch (ServletException e) {
          log4j.warn(e);
          e.printStackTrace();
        }
        String DateTrx = dateTrx1;
        if (compare.equals("-1"))
          DateTrx = dateTrx2;
        //
        String strQty = Qty.toString();
        String strDateTrx = DateTrx;
        String AD_Client_ID = data[i].adClientId;
        String AD_Org_ID = data[i].adOrgId;
        String C_InvoiceLine_ID = data[i].cInvoicelineId;
        String M_InOutLine_ID = data[i].mInoutlineId;
        String M_Product_ID = data[i].mProductId;
        //
        if (createMatchInv(AD_Client_ID, AD_Org_ID, M_InOutLine_ID, C_InvoiceLine_ID, M_Product_ID,
            strDateTrx, strQty, vars, conn, con) == 1)
          counter++;
      }
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - Matcher.match - Client_ID=" + AD_Client_ID
    // + ", Records created=" + counter);
    return counter;
  } // match

  /**
   * Create MatchInv record
   * 
   * @param AD_Client_ID
   *          Client
   * @param AD_Org_ID
   *          Org
   * @param M_InOutLine_ID
   *          Receipt
   * @param C_InvoiceLine_ID
   *          Invoice
   * @param M_Product_ID
   *          Product
   * @param DateTrx
   *          Date
   * @param Qty
   *          Qty
   * @return true if record created
   */
  private int createMatchInv(String AD_Client_ID, String AD_Org_ID, String M_InOutLine_ID,
      String C_InvoiceLine_ID, String M_Product_ID, String DateTrx, String Qty,
      VariablesSecureApp vars, ConnectionProvider conn, Connection con) {
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - createMatchInv - InvLine=" +
    // C_InvoiceLine_ID + ",Rec=" + M_InOutLine_ID + ", Qty=" + Qty + ", " +
    // DateTrx);
    int no = 0;
    try {
      String M_MatchInv_ID = SequenceIdData.getUUID();
      //
      no = AcctServerData.insertMatchInv(con, conn, M_MatchInv_ID, AD_Client_ID, AD_Org_ID,
          M_InOutLine_ID, C_InvoiceLine_ID, M_Product_ID, DateTrx, Qty);
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    return no;
  } // createMatchInv

  /**
   * Get the account for Accounting Schema
   * 
   * @param AcctType
   *          see ACCTTYPE_*
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccount(String AcctType, AcctSchema as, ConnectionProvider conn) {
    BigDecimal AMT = null;
    AcctServerData[] data = null;
    // if (log4j.isDebugEnabled())
    // log4j.debug("*******************************getAccount 1: AcctType:-->"
    // + AcctType);
    try {
      /** Account Type - Invoice */
      if (AcctType.equals(ACCTTYPE_Charge)) { // see getChargeAccount in
        // DocLine
        // if (log4j.isDebugEnabled())
        // log4j.debug("AcctServer - *******************amount(AMT);-->"
        // + getAmount(AMTTYPE_Charge));
        AMT = new BigDecimal(getAmount(AMTTYPE_Charge));
        // if (log4j.isDebugEnabled())
        // log4j.debug("AcctServer - *******************AMT;-->" + AMT);
        int cmp = AMT.compareTo(BigDecimal.ZERO);
        // if (log4j.isDebugEnabled())
        // log4j.debug("AcctServer - ******************* CMP: " + cmp);
        if (cmp == 0)
          return null;
        else if (cmp < 0)
          data = AcctServerData.selectExpenseAcct(conn, C_Charge_ID, as.getC_AcctSchema_ID());
        else
          data = AcctServerData.selectRevenueAcct(conn, C_Charge_ID, as.getC_AcctSchema_ID());
        // if (log4j.isDebugEnabled())
        // log4j.debug("AcctServer - *******************************getAccount 2");
      } else if (AcctType.equals(ACCTTYPE_V_Liability)) {
        data = AcctServerData.selectLiabilityAcct(conn, C_BPartner_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_V_Liability_Services)) {
        data = AcctServerData.selectLiabilityServicesAcct(conn, C_BPartner_ID,
            as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_C_Receivable)) {
        data = AcctServerData.selectReceivableAcct(conn, C_BPartner_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_UnallocatedCash)) {
        /** Account Type - Payment */
        data = AcctServerData.selectUnallocatedCashAcct(conn, C_BankAccount_ID,
            as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_BankInTransit)) {
        data = AcctServerData.selectInTransitAcct(conn, C_BankAccount_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_BankInTransitDefault)) {
        data = AcctServerData.selectInTransitDefaultAcct(conn, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_ConvertChargeDefaultAmt)) {
        data = AcctServerData.selectConvertChargeDefaultAmtAcct(conn, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_ConvertGainDefaultAmt)) {
        data = AcctServerData.selectConvertGainDefaultAmtAcct(conn, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_PaymentSelect)) {
        data = AcctServerData.selectPaymentSelectAcct(conn, C_BankAccount_ID,
            as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_WriteOffDefault)) {
        data = AcctServerData.selectWriteOffDefault(conn, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_WriteOffDefault_Revenue)) {
        data = AcctServerData.selectWriteOffDefaultRevenue(conn, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_DiscountExp)) {
        /** Account Type - Allocation */
        data = AcctServerData.selectDiscountExpAcct(conn, C_BPartner_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_DiscountRev)) {
        data = AcctServerData.selectDiscountRevAcct(conn, C_BPartner_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_WriteOff)) {
        data = AcctServerData.selectWriteOffAcct(conn, C_BPartner_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_WriteOff_Revenue)) {
        data = AcctServerData.selectWriteOffAcctRevenue(conn, C_BPartner_ID,
            as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_ConvertChargeLossAmt)) {
        /** Account Type - Bank Statement */
        data = AcctServerData.selectConvertChargeLossAmt(conn, C_BankAccount_ID,
            as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_ConvertChargeGainAmt)) {
        data = AcctServerData.selectConvertChargeGainAmt(conn, C_BankAccount_ID,
            as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_BankAsset)) {
        data = AcctServerData.selectAssetAcct(conn, C_BankAccount_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_InterestRev)) {
        data = AcctServerData
            .selectInterestRevAcct(conn, C_BankAccount_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_InterestExp)) {
        data = AcctServerData
            .selectInterestExpAcct(conn, C_BankAccount_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_CashAsset)) {
        /** Account Type - Cash */
        data = AcctServerData.selectCBAssetAcct(conn, C_CashBook_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_CashTransfer)) {
        data = AcctServerData.selectCashTransferAcct(conn, C_CashBook_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_CashExpense)) {
        data = AcctServerData.selectCBExpenseAcct(conn, C_CashBook_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_CashReceipt)) {
        data = AcctServerData.selectCBReceiptAcct(conn, C_CashBook_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_CashDifference)) {
        data = AcctServerData.selectCBDifferencesAcct(conn, C_CashBook_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_InvDifferences)) {
        /** Inventory Accounts */
        data = AcctServerData.selectWDifferencesAcct(conn, M_Warehouse_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_NotInvoicedReceipts)) {
        if (log4j.isDebugEnabled())
          log4j.debug("AcctServer - getAccount - ACCTYPE_NotInvoicedReceipts - C_BPartner_ID - "
              + C_BPartner_ID);
        data = AcctServerData.selectNotInvoicedReceiptsAcct(conn, C_BPartner_ID,
            as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_ProjectAsset)) {
        /** Project Accounts */
        data = AcctServerData.selectPJAssetAcct(conn, C_Project_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_ProjectWIP)) {
        data = AcctServerData.selectPJWIPAcct(conn, C_Project_ID, as.getC_AcctSchema_ID());
      } else if (AcctType.equals(ACCTTYPE_PPVOffset)) {
        /** GL Accounts */
        data = AcctServerData.selectPPVOffsetAcct(conn, as.getC_AcctSchema_ID());
      } else {
        log4j.warn("AcctServer - getAccount - Not found AcctType=" + AcctType);
        return null;
      }
      // if (log4j.isDebugEnabled())
      // log4j.debug("AcctServer - *******************************getAccount 3");
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    // Get Acct
    String Account_ID = "";
    if (data != null && data.length != 0) {
      Account_ID = data[0].accountId;
    } else
      return null;
    // No account
    if (Account_ID.equals("")) {
      log4j.warn("AcctServer - getAccount - NO account Type=" + AcctType + ", Record=" + Record_ID);
      return null;
    }
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - *******************************getAccount 4");
    // Return Account
    Account acct = null;
    try {
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    }
    return acct;
  } // getAccount

  /**
   * Get the account for Accounting Schema
   * 
   * @param cBPartnerId
   *          business partner id
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountBPartner(String cBPartnerId, AcctSchema as, boolean isReceipt,
      boolean isPrepayment, ConnectionProvider conn) throws ServletException {

    String strValidCombination = "";
    if (isReceipt) {
      final StringBuilder whereClause = new StringBuilder();

      whereClause.append(" as cusa ");
      whereClause.append(" where cusa.businessPartner.id = '" + cBPartnerId + "'");
      whereClause.append(" and cusa.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
      whereClause.append(" and (cusa.status is null or cusa.status = 'DE')");

      final OBQuery<CustomerAccounts> obqParameters = OBDal.getInstance().createQuery(
          CustomerAccounts.class, whereClause.toString());
      obqParameters.setFilterOnReadableClients(false);
      obqParameters.setFilterOnReadableOrganization(false);
      final List<CustomerAccounts> customerAccounts = obqParameters.list();
      if (customerAccounts != null && customerAccounts.size() > 0
          && customerAccounts.get(0).getCustomerReceivablesNo() != null && !isPrepayment)
        strValidCombination = customerAccounts.get(0).getCustomerReceivablesNo().getId();
      if (customerAccounts != null && customerAccounts.size() > 0
          && customerAccounts.get(0).getCustomerPrepayment() != null && isPrepayment)
        strValidCombination = customerAccounts.get(0).getCustomerPrepayment().getId();
    } else {
      final StringBuilder whereClause = new StringBuilder();

      whereClause.append(" as vena ");
      whereClause.append(" where vena.businessPartner.id = '" + cBPartnerId + "'");
      whereClause.append(" and vena.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
      whereClause.append(" and (vena.status is null or vena.status = 'DE')");

      final OBQuery<VendorAccounts> obqParameters = OBDal.getInstance().createQuery(
          VendorAccounts.class, whereClause.toString());
      obqParameters.setFilterOnReadableClients(false);
      obqParameters.setFilterOnReadableOrganization(false);
      final List<VendorAccounts> vendorAccounts = obqParameters.list();
      if (vendorAccounts != null && vendorAccounts.size() > 0
          && vendorAccounts.get(0).getVendorLiability() != null && !isPrepayment)
        strValidCombination = vendorAccounts.get(0).getVendorLiability().getId();
      if (vendorAccounts != null && vendorAccounts.size() > 0
          && vendorAccounts.get(0).getVendorPrepayment() != null && isPrepayment)
        strValidCombination = vendorAccounts.get(0).getVendorPrepayment().getId();
    }
    if (strValidCombination.equals("")) {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("Account", isReceipt ? (isPrepayment ? "@CustomerPrepayment@"
          : "@CustomerReceivables@") : (isPrepayment ? "@VendorPrepayment@" : "@VendorLiability@"));
      BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, cBPartnerId);
      if (bp != null) {
        parameters.put("Entity", bp.getIdentifier());
      }
      parameters.put(
          "AccountingSchema",
          OBDal
              .getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.getC_AcctSchema_ID()).getIdentifier());
      setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
      throw new IllegalStateException();
    }
    return new Account(conn, strValidCombination);
  } // getAccount

  /**
   * Get the account for GL Item
   */
  public Account getAccountGLItem(GLItem glItem, AcctSchema as, boolean bIsReceipt,
      ConnectionProvider conn) throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    try {
      OBCriteria<GLItemAccounts> accounts = OBDal.getInstance()
          .createCriteria(GLItemAccounts.class);
      accounts.add(Restrictions.eq(GLItemAccounts.PROPERTY_GLITEM, glItem));
      accounts.add(Restrictions.eq(
          GLItemAccounts.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(
              org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(GLItemAccounts.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<GLItemAccounts> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0)
        return null;
      if (bIsReceipt)
        account = new Account(conn, accountList.get(0).getGlitemCreditAcct().getId());
      else
        account = new Account(conn, accountList.get(0).getGlitemDebitAcct().getId());
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Account", bIsReceipt ? "@GlitemCreditAccount@" : "@GlitemDebitAccount@");
        if (glItem != null) {
          parameters.put("Entity", glItem.getIdentifier());
        }
        parameters.put(
            "AccountingSchema",
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  public Account getAccountFee(AcctSchema as, FIN_FinancialAccount finAccount,
      ConnectionProvider conn) throws ServletException {
    Account account = null;
    OBContext.setAdminMode();
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance().createCriteria(
          FIN_FinancialAccountAccounting.class);
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Restrictions.eq(
          FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(
              org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0)
        return account;
      account = new Account(conn, accountList.get(0).getFINBankfeeAcct().getId());
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Account", "@BankfeeAccount@");
        if (finAccount != null) {
          parameters.put("Entity", finAccount.getIdentifier());
        }
        parameters.put(
            "AccountingSchema",
            OBDal
                .getInstance()
                .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                    as.getC_AcctSchema_ID()).getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  /**
   * Get the account for Financial Account (Uses: INT - In Transit DEP - Deposit CLE - Clearing WIT
   * - Withdraw)
   */
  public Account getAccount(ConnectionProvider conn, String use,
      FIN_FinancialAccountAccounting financialAccountAccounting, boolean bIsReceipt)
      throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    String strvalidCombination = "";
    try {
      if ("INT".equals(use))
        strvalidCombination = bIsReceipt ? financialAccountAccounting
            .getInTransitPaymentAccountIN().getId() : financialAccountAccounting
            .getFINOutIntransitAcct().getId();
      else if ("DEP".equals(use))
        strvalidCombination = financialAccountAccounting.getDepositAccount().getId();
      else if ("CLE".equals(use))
        strvalidCombination = bIsReceipt ? financialAccountAccounting.getClearedPaymentAccount()
            .getId() : financialAccountAccounting.getClearedPaymentAccountOUT().getId();
      else if ("WIT".equals(use))
        strvalidCombination = financialAccountAccounting.getWithdrawalAccount().getId();
      else
        return account;
      account = new Account(conn, strvalidCombination);
    } finally {
      OBContext.restorePreviousMode();
      if (account == null) {
        Map<String, String> parameters = new HashMap<String, String>();
        String strAccount = bIsReceipt ? ("INT".equals(use) ? "@InTransitPaymentAccountIN@"
            : ("DEP".equals(use) ? "@DepositAccount@" : "@ClearedPaymentAccount@")) : ("INT"
            .equals(use) ? "@InTransitPaymentAccountOUT@"
            : ("CLE".equals(use) ? "@ClearedPaymentAccountOUT@" : "@WithdrawalAccount@"));
        parameters.put("Account", strAccount);
        if (financialAccountAccounting.getAccount() != null) {
          parameters.put("Entity", financialAccountAccounting.getAccount().getIdentifier());
        }
        parameters.put("AccountingSchema", financialAccountAccounting.getAccountingSchema()
            .getIdentifier());
        setMessageResult(conn, STATUS_InvalidAccount, "error", parameters);
        throw new IllegalStateException();
      }
    }
    return account;
  }

  public FieldProvider[] getObjectFieldProvider() {
    return objectFieldProvider;
  }

  public void setObjectFieldProvider(FieldProvider[] fieldProvider) {
    objectFieldProvider = fieldProvider;
  }

  public abstract void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID,
      String Id) throws ServletException;

  public abstract boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn);

  /**
   * Get Source Currency Balance - subtracts line (and tax) amounts from total - no rounding
   * 
   * @return positive amount, if total header is bigger than lines
   */
  public abstract BigDecimal getBalance();

  /**
   * Create Facts (the accounting logic)
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public abstract Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException;

  public abstract boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId);

  public String getInfo(VariablesSecureApp vars) {
    return (Utility.messageBD(connectionProvider, "Created", vars.getLanguage()) + "=" + success
    // + ", " + Utility . messageBD ( this , "Errors" , vars . getLanguage ( ) ) + "=" + errors
    );
  } // end of getInfo() method

  /**
   * @param language
   * @return a String representing the result of created
   */
  public String getInfo(String language) {
    return (Utility.messageBD(connectionProvider, "Created", language) + "=" + success);
  }

  public boolean checkDocuments() throws ServletException {
    if (m_as.length == 0)
      return false;
    AcctServerData[] docTypes = AcctServerData.selectDocTypes(connectionProvider, AD_Table_ID,
        AD_Client_ID);
    // if (log4j.isDebugEnabled())
    // log4j.debug("AcctServer - AcctSchema length-" + (this.m_as).length);
    for (int i = 0; i < docTypes.length; i++) {
      AcctServerData data = AcctServerData.selectDocuments(connectionProvider, tableName,
          AD_Client_ID, AD_Org_ID, docTypes[i].name, strDateColumn);

      if (data != null) {
        if (data.id != null && !data.id.equals("")) {
          if (log4j.isDebugEnabled()) {
            log4j.debug("AcctServer - not posted - " + docTypes[i].name + " document id: "
                + data.id);
          }
          return true;
        }
      }
    }
    return false;
  } // end of checkDocuments() method

  public void setMessageResult(OBError error) {
    messageResult = error;
  }

  /*
   * Sets OBError message for the given status
   */
  public void setMessageResult(ConnectionProvider conn, VariablesSecureApp vars, String strStatus,
      String strMessageType) {
    setMessageResult(conn, strStatus, strMessageType, null);
  }

  /*
   * Sets OBError message for the given status
   */
  public void setMessageResult(ConnectionProvider conn, String _strStatus, String strMessageType,
      Map<String, String> _parameters) {
    VariablesSecureApp vars = new VariablesSecureApp(RequestContext.get().getRequest());
    setMessageResult(conn, vars, _strStatus, strMessageType, _parameters);
  }

  /*
   * Sets OBError message for the given status
   */
  public void setMessageResult(ConnectionProvider conn, VariablesSecureApp vars, String _strStatus,
      String strMessageType, Map<String, String> _parameters) {
    String strStatus = StringUtils.isEmpty(_strStatus) ? getStatus() : _strStatus;
    setStatus(strStatus);
    String strTitle = "";
    Map<String, String> parameters = _parameters != null ? _parameters
        : new HashMap<String, String>();
    if (messageResult == null)
      messageResult = new OBError();
    if (strMessageType == null || strMessageType.equals(""))
      messageResult.setType("Error");
    else
      messageResult.setType(strMessageType);
    if (strStatus.equals(STATUS_Error))
      strTitle = "@ProcessRunError@";
    else if (strStatus.equals(STATUS_DocumentLocked)) {
      strTitle = "@OtherPostingProcessActive@";
      messageResult.setType("Warning");
    } else if (strStatus.equals(STATUS_InvalidCost))
      strTitle = "@InvalidCost@";
    else if (strStatus.equals(STATUS_DocumentDisabled)) {
      strTitle = "@DocumentDisabled@";
      messageResult.setType("Warning");
    } else if (strStatus.equals(STATUS_BackgroundDisabled)) {
      strTitle = "@BackgroundDisabled@";
      messageResult.setType("Warning");
    } else if (strStatus.equals(STATUS_InvalidAccount)) {
      if (parameters.isEmpty()) {
        strTitle = "@InvalidAccount@";
      } else {
        strTitle = "@InvalidWhichAccount@";
        // Transalate account name from messages
        parameters.put("Account",
            Utility.parseTranslation(conn, vars, vars.getLanguage(), parameters.get("Account")));
      }
    } else if (strStatus.equals(STATUS_PeriodClosed)) {
      strTitle = "@PeriodClosed@";
    } else if (strStatus.equals(STATUS_NotConvertible)) {
      strTitle = "@NotConvertible@";
    } else if (strStatus.equals(STATUS_NotBalanced)) {
      strTitle = "@NotBalanced@";
    } else if (strStatus.equals(STATUS_NotPosted)) {
      strTitle = "@NotPosted@";
    } else if (strStatus.equals(STATUS_PostPrepared)) {
      strTitle = "@PostPrepared@";
    } else if (strStatus.equals(STATUS_Posted)) {
      strTitle = "@Posted@";
    } else if (strStatus.equals(STATUS_TableDisabled)) {
      strTitle = "@TableDisabled@";
      parameters.put("Table", tableName);
      messageResult.setType("Warning");
    }
    messageResult.setMessage(Utility.parseTranslation(conn, vars, parameters, vars.getLanguage(),
        Utility.parseTranslation(conn, vars, vars.getLanguage(), strTitle)));
    if (strMessage != null)
      messageResult.setMessage(Utility.parseTranslation(conn, vars, parameters, vars.getLanguage(),
          Utility.parseTranslation(conn, vars, vars.getLanguage(), strMessage)));
  }

  public Map<String, String> getInvalidAccountParameters(String strAccount, String strEntity,
      String strAccountingSchema) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("Account", strAccount);
    parameters.put("Entity", strEntity);
    parameters.put("AccountingSchema", strAccountingSchema);
    return parameters;
  }

  public OBError getMessageResult() {
    return messageResult;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method

  public String getStatus() {
    return Status;
  }

  public void setStatus(String strStatus) {
    Status = strStatus;
  }

  public ConnectionProvider getConnectionProvider() {
    return connectionProvider;
  }

  public ConversionRateDoc getConversionRateDoc(String table_ID, String record_ID,
      String curFrom_ID, String curTo_ID) {
    OBCriteria<ConversionRateDoc> docRateCriteria = OBDal.getInstance().createCriteria(
        ConversionRateDoc.class);
    docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, OBDal.getInstance()
        .get(Currency.class, curTo_ID)));
    docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, OBDal.getInstance()
        .get(Currency.class, curFrom_ID)));
    if (record_ID != null) {
      if (table_ID.equals(TABLEID_Invoice)) {
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_INVOICE, OBDal.getInstance()
            .get(Invoice.class, OBDal.getInstance().get(Invoice.class, record_ID).getId())));
      } else if (table_ID.equals(TABLEID_Payment)) {
        docRateCriteria
            .add(Restrictions.eq(
                ConversionRateDoc.PROPERTY_PAYMENT,
                OBDal.getInstance().get(FIN_Payment.class,
                    OBDal.getInstance().get(FIN_Payment.class, record_ID).getId())));
      } else if (table_ID.equals(TABLEID_Transaction)) {
        docRateCriteria.add(Restrictions.eq(
            ConversionRateDoc.PROPERTY_FINANCIALACCOUNTTRANSACTION,
            OBDal.getInstance().get(FIN_FinaccTransaction.class,
                OBDal.getInstance().get(FIN_FinaccTransaction.class, record_ID).getId())));
      } else {
        return null;
      }
    } else {
      return null;
    }
    List<ConversionRateDoc> conversionRates = docRateCriteria.list();
    if (!conversionRates.isEmpty()) {
      return conversionRates.get(0);
    }
    return null;
  }

  // public BigDecimal convertAmount(BigDecimal _amount, boolean isReceipt, String dateAcct,
  // String conversionDate, String currencyIDFrom, String currencyIDTo, DocLine line,
  // AcctSchema as, Fact fact, String Fact_Acct_Group_ID, String seqNo, ConnectionProvider conn)
  // throws ServletException {
  // BigDecimal amount = _amount;
  // if (log4j.isDebugEnabled())
  // log4j.debug("Amount:" + amount + " curr from:" + currencyIDFrom + " Curr to:" + currencyIDTo
  // + " convDate:" + conversionDate + " DateAcct:" + dateAcct);
  // if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
  // return amount;
  // }
  // if (currencyIDFrom.equals(currencyIDTo)) {
  // return amount;
  // }
  // MultiCurrency = true;
  // BigDecimal amt = new BigDecimal(getConvertedAmt(amount.toString(), currencyIDFrom,
  // currencyIDTo, conversionDate, "", AD_Client_ID, AD_Org_ID, conn));
  // BigDecimal amtTo = new BigDecimal(getConvertedAmt(amount.toString(), currencyIDFrom,
  // currencyIDTo, dateAcct, "", AD_Client_ID, AD_Org_ID, conn));
  // BigDecimal amtDiff = (amtTo).subtract(amt);
  // if ((isReceipt && amtDiff.compareTo(BigDecimal.ZERO) == 1)
  // || (!isReceipt && amtDiff.compareTo(BigDecimal.ZERO) == -1)) {
  // fact.createLine(line, getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, as, conn),
  // currencyIDTo, "", amtDiff.abs().toString(), Fact_Acct_Group_ID, seqNo, DocumentType, conn);
  // } else {
  // fact.createLine(line, getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, as, conn),
  // currencyIDTo, amtDiff.abs().toString(), "", Fact_Acct_Group_ID, seqNo, DocumentType, conn);
  // }
  // return amt;
  // }

  public BigDecimal convertAmount(BigDecimal _amount, boolean isReceipt, String dateAcct,
      String table_ID, String record_ID, String currencyIDFrom, String currencyIDTo, DocLine line,
      AcctSchema as, Fact fact, String Fact_Acct_Group_ID, String seqNo, ConnectionProvider conn)
      throws ServletException {
    BigDecimal amtDiff = BigDecimal.ZERO;
    if (_amount == null || _amount.compareTo(BigDecimal.ZERO) == 0) {
      return _amount;
    }
    String conversionDate = dateAcct;
    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
    ConversionRateDoc conversionRateDoc = getConversionRateDoc(table_ID, record_ID, currencyIDFrom,
        currencyIDTo);
    BigDecimal amtFrom = BigDecimal.ZERO;
    BigDecimal amtFromSourcecurrency = BigDecimal.ZERO;
    BigDecimal amtTo = BigDecimal.ZERO;
    if (table_ID.equals(TABLEID_Invoice)) {
      Invoice invoice = OBDal.getInstance().get(Invoice.class, record_ID);
      conversionDate = dateFormat.format(invoice.getAccountingDate());
    } else if (table_ID.equals(TABLEID_Payment)) {
      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, record_ID);
      conversionDate = dateFormat.format(payment.getPaymentDate());
    } else if (table_ID.equals(TABLEID_Transaction)) {
      FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
          record_ID);
      conversionDate = dateFormat.format(transaction.getDateAcct());
    }
    if (conversionRateDoc != null && record_ID != null) {
      amtFrom = applyRate(_amount, conversionRateDoc, true);
    } else {
      // I try to find a reversal rate for the doc, if exists i apply it reversal as well
      conversionRateDoc = getConversionRateDoc(table_ID, record_ID, currencyIDTo, currencyIDFrom);
      if (conversionRateDoc != null) {
        amtFrom = applyRate(_amount, conversionRateDoc, false);
      } else {
        amtFrom = new BigDecimal(getConvertedAmt(_amount.toString(), currencyIDFrom, currencyIDTo,
            conversionDate, "", AD_Client_ID, AD_Org_ID, conn));
      }
    }
    ConversionRateDoc conversionRateCurrentDoc = getConversionRateDoc(AD_Table_ID, Record_ID,
        currencyIDFrom, currencyIDTo);
    if (AD_Table_ID.equals(TABLEID_Invoice)) {
      Invoice invoice = OBDal.getInstance().get(Invoice.class, Record_ID);
      conversionDate = dateFormat.format(invoice.getAccountingDate());
    } else if (AD_Table_ID.equals(TABLEID_Payment)) {
      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, Record_ID);
      conversionDate = dateFormat.format(payment.getPaymentDate());
    } else if (AD_Table_ID.equals(TABLEID_Transaction)
        || AD_Table_ID.equals(TABLEID_Reconciliation)) {
      String transactionID = Record_ID;
      // When TableID= Reconciliation info is loaded from transaction
      if (AD_Table_ID.equals(AcctServer.TABLEID_Reconciliation)
          && line instanceof DocLine_FINReconciliation) {
        transactionID = ((DocLine_FINReconciliation) line).getFinFinAccTransactionId();
      }
      FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
          transactionID);
      conversionDate = dateFormat.format(transaction.getDateAcct());
      conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Transaction, transaction.getId(),
          currencyIDFrom, currencyIDTo);
    }
    if (conversionRateCurrentDoc != null) {
      amtTo = applyRate(_amount, conversionRateCurrentDoc, true);
      amtFromSourcecurrency = applyRate(amtFrom, conversionRateCurrentDoc, false);
    } else {
      // I try to find a reversal rate for the doc, if exists i apply it reversal as well
      if (AD_Table_ID.equals(AcctServer.TABLEID_Reconciliation)
          && line instanceof DocLine_FINReconciliation) {
        String transactionID = ((DocLine_FINReconciliation) line).getFinFinAccTransactionId();
        conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Transaction, transactionID,
            currencyIDTo, currencyIDFrom);
      } else {
        conversionRateCurrentDoc = getConversionRateDoc(AD_Table_ID, Record_ID, currencyIDTo,
            currencyIDFrom);
      }
      if (conversionRateCurrentDoc != null) {
        amtTo = applyRate(_amount, conversionRateCurrentDoc, false);
        amtFromSourcecurrency = applyRate(amtFrom, conversionRateCurrentDoc, true);
      } else {
        amtTo = new BigDecimal(getConvertedAmt(_amount.toString(), currencyIDFrom, currencyIDTo,
            conversionDate, "", AD_Client_ID, AD_Org_ID, conn));
        Currency currency = OBDal.getInstance().get(Currency.class, currencyIDFrom);
        amtFromSourcecurrency = amtFrom.multiply(_amount)
            .divide(amtTo, conversionRatePrecision, BigDecimal.ROUND_HALF_EVEN)
            .setScale(currency.getStandardPrecision().intValue(), BigDecimal.ROUND_HALF_EVEN);
      }
    }
    amtDiff = (amtTo).subtract(amtFrom);
    // Add differences related to Different rates for accounting among currencies
    // _amount * ((TrxRate *
    // AccountingRateCurrencyFromCurrencyTo)-AccountingRateCurrencyDocCurrencyTo)
    amtDiff = amtDiff.add(calculateMultipleRatesDifferences(_amount, currencyIDFrom, currencyIDTo,
        line, conn));
    // Currency currencyTo = OBDal.getInstance().get(Currency.class, currencyIDTo);
    // amtDiff = amtDiff.setScale(currencyTo.getStandardPrecision().intValue(),
    // BigDecimal.ROUND_HALF_EVEN);
    if ((!isReceipt && amtDiff.compareTo(BigDecimal.ZERO) == 1)
        || (isReceipt && amtDiff.compareTo(BigDecimal.ZERO) == -1)) {
      fact.createLine(line, getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, as, conn),
          currencyIDTo, "", amtDiff.abs().toString(), Fact_Acct_Group_ID, seqNo, DocumentType, conn);
    } else if (amtDiff.compareTo(BigDecimal.ZERO) != 0) {
      fact.createLine(line, getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, as, conn),
          currencyIDTo, amtDiff.abs().toString(), "", Fact_Acct_Group_ID, seqNo, DocumentType, conn);
    } else {
      return amtFromSourcecurrency;
    }
    if (log4j.isDebugEnabled())
      log4j.debug("Amt from: " + amtFrom + "[" + currencyIDFrom + "]" + " Amt to: " + amtTo + "["
          + currencyIDTo + "] - amtFromSourcecurrency: " + amtFromSourcecurrency);
    // return value in original currency
    return amtFromSourcecurrency;
  }

  @Deprecated
  public static String getConvertedAmt(String Amt, String CurFrom_ID, String CurTo_ID,
      String ConvDate, String RateType, String client, String org, String recordId, String docType,
      ConnectionProvider conn) {
    boolean useSystemConversionRate = true;
    if (log4j.isDebugEnabled())
      log4j.debug("AcctServer - getConvertedAmount - starting method - Amt : " + Amt
          + " - CurFrom_ID : " + CurFrom_ID + " - CurTo_ID : " + CurTo_ID + "- ConvDate: "
          + ConvDate + " - RateType:" + RateType + " - client:" + client + "- org:" + org);

    if (Amt.equals(""))
      throw new IllegalArgumentException(
          "AcctServer - getConvertedAmt - required parameter missing - Amt");
    if ((CurFrom_ID.equals(CurTo_ID) && !docType.equals(EXCHANGE_DOCTYPE_Transaction))
        || Amt.equals("0"))
      return Amt;
    AcctServerData[] data = null;
    OBContext.setAdminMode();
    try {
      if (ConvDate != null && ConvDate.equals(""))
        ConvDate = DateTimeData.today(conn);
      // ConvDate IN DATE
      if (RateType == null || RateType.equals(""))
        RateType = "S";
      data = AcctServerData.currencyConvert(conn, Amt, CurFrom_ID, CurTo_ID, ConvDate, RateType,
          client, org);
      // Search if exists any conversion rate at document level

      OBCriteria<ConversionRateDoc> docRateCriteria = OBDal.getInstance().createCriteria(
          ConversionRateDoc.class);
      if (docType.equals(EXCHANGE_DOCTYPE_Invoice) && recordId != null) {
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, OBDal
            .getInstance().get(Currency.class, CurTo_ID)));
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, OBDal
            .getInstance().get(Currency.class, CurFrom_ID)));
        // get reversed invoice id if exist.
        OBCriteria<ReversedInvoice> reversedCriteria = OBDal.getInstance().createCriteria(
            ReversedInvoice.class);
        reversedCriteria.add(Restrictions.eq(ReversedInvoice.PROPERTY_INVOICE, OBDal.getInstance()
            .get(Invoice.class, recordId)));
        if (!reversedCriteria.list().isEmpty()) {
          String strDateFormat;
          strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
              .getProperty("dateFormat.java");
          final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
          ConvDate = dateFormat.format(reversedCriteria.list().get(0).getReversedInvoice()
              .getAccountingDate());
          data = AcctServerData.currencyConvert(conn, Amt, CurFrom_ID, CurTo_ID, ConvDate,
              RateType, client, org);
          docRateCriteria.add(Restrictions.eq(
              ConversionRateDoc.PROPERTY_INVOICE,
              OBDal.getInstance().get(Invoice.class,
                  reversedCriteria.list().get(0).getReversedInvoice().getId())));
        } else {
          docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_INVOICE, OBDal
              .getInstance().get(Invoice.class, recordId)));
        }
        useSystemConversionRate = false;
      } else if (docType.equals(EXCHANGE_DOCTYPE_Payment)) {
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, OBDal
            .getInstance().get(Currency.class, CurTo_ID)));
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, OBDal
            .getInstance().get(Currency.class, CurFrom_ID)));
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_PAYMENT, OBDal.getInstance()
            .get(FIN_Payment.class, recordId)));
        useSystemConversionRate = false;
      } else if (docType.equals(EXCHANGE_DOCTYPE_Transaction)) {
        APRM_FinaccTransactionV a = OBDal.getInstance()
            .get(APRM_FinaccTransactionV.class, recordId);
        if (a.getForeignCurrency() != null) { // && !a.getForeignCurrency().getId().equals(CurTo_ID)
          String strDateFormat;
          strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
              .getProperty("dateFormat.java");
          final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
          Amt = a.getForeignAmount().toString();
          data = AcctServerData.currencyConvert(conn, Amt, a.getForeignCurrency().getId(),
              CurTo_ID, ConvDate, RateType, client, org);
          docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, OBDal
              .getInstance().get(Currency.class, CurTo_ID)));
          docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, OBDal
              .getInstance().get(Currency.class, a.getForeignCurrency().getId())));
        } else {
          docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_TOCURRENCY, OBDal
              .getInstance().get(Currency.class, CurTo_ID)));
          docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_CURRENCY, OBDal
              .getInstance().get(Currency.class, CurFrom_ID)));
        }
        docRateCriteria.add(Restrictions.eq(ConversionRateDoc.PROPERTY_FINANCIALACCOUNTTRANSACTION,
            OBDal.getInstance().get(APRM_FinaccTransactionV.class, recordId)));
        useSystemConversionRate = false;
      }
      if (docType.equals(EXCHANGE_DOCTYPE_Invoice) || docType.equals(EXCHANGE_DOCTYPE_Payment)
          || docType.equals(EXCHANGE_DOCTYPE_Transaction)) {
        List<ConversionRateDoc> conversionRates = docRateCriteria.list();
        if (!conversionRates.isEmpty() && !useSystemConversionRate) {
          BigDecimal Amount = new BigDecimal(Amt);
          BigDecimal AmountConverted = Amount.multiply(conversionRates.get(0).getRate()).setScale(
              2, BigDecimal.ROUND_HALF_UP);
          return AmountConverted.toString();
        }
      }
    } catch (ServletException e) {
      log4j.warn(e);
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    if (data == null || data.length == 0) {
      /*
       * log4j.error("No conversion ratio"); throw new
       * ServletException("No conversion ratio defined!");
       */
      return "";
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("getConvertedAmount - converted:" + data[0].converted);
      return data[0].converted;
    }
  } // getConvertedAmt

  private BigDecimal calculateMultipleRatesDifferences(BigDecimal _amount, String currencyIDFrom,
      String currencyIDTo, DocLine line, ConnectionProvider conn) {
    // _amount * ((TrxRate *
    // AccountingRateCurrencyFromCurrencyTo)-AccountingRateCurrencyDocCurrencyTo)
    String conversionDate = DateAcct;
    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
    // Calculate accountingRateCurrencyFromCurrencyTo
    BigDecimal accountingRateCurrencyFromCurrencyTo = BigDecimal.ONE;
    if (!currencyIDFrom.equals(currencyIDTo)) {
      ConversionRateDoc conversionRateCurrentDoc = getConversionRateDoc(AD_Table_ID, Record_ID,
          currencyIDFrom, currencyIDTo);
      if (AD_Table_ID.equals(AcctServer.TABLEID_Reconciliation)
          && line instanceof DocLine_FINReconciliation) {
        String transactionID = ((DocLine_FINReconciliation) line).getFinFinAccTransactionId();
        FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
            transactionID);
        conversionDate = dateFormat.format(transaction.getDateAcct());
        conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Transaction, transaction.getId(),
            currencyIDFrom, currencyIDTo);
      }
      if (conversionRateCurrentDoc != null) {
        accountingRateCurrencyFromCurrencyTo = conversionRateCurrentDoc.getRate();
      } else {
        // I try to find a reversal rate for the doc, if exists i apply it reversal as well
        if (AD_Table_ID.equals(AcctServer.TABLEID_Reconciliation)
            && line instanceof DocLine_FINReconciliation) {
          String transactionID = ((DocLine_FINReconciliation) line).getFinFinAccTransactionId();
          FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
              transactionID);
          conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Transaction, transaction.getId(),
              currencyIDTo, currencyIDFrom);
        } else {
          conversionRateCurrentDoc = getConversionRateDoc(AD_Table_ID, Record_ID, currencyIDTo,
              currencyIDFrom);
        }
        if (conversionRateCurrentDoc != null) {
          accountingRateCurrencyFromCurrencyTo = BigDecimal.ONE.divide(
              conversionRateCurrentDoc.getRate(), MathContext.DECIMAL64);
        } else {
          accountingRateCurrencyFromCurrencyTo = getConvertionRate(currencyIDFrom, currencyIDTo,
              conversionDate, "", AD_Client_ID, AD_Org_ID, conn);
        }
      }
    }

    // Calculate accountingRateCurrencyFromCurrencyTo
    BigDecimal accountingRateCurrencyDocCurrencyTo = BigDecimal.ONE;
    if (!C_Currency_ID.equals(currencyIDTo)) {
      ConversionRateDoc conversionRateCurrentDoc = getConversionRateDoc(AD_Table_ID, Record_ID,
          C_Currency_ID, currencyIDTo);
      if (AD_Table_ID.equals(AcctServer.TABLEID_Reconciliation)
          && line instanceof DocLine_FINReconciliation) {
        String transactionID = ((DocLine_FINReconciliation) line).getFinFinAccTransactionId();
        FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
            transactionID);
        conversionDate = dateFormat.format(transaction.getTransactionDate());
        conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Transaction, transaction.getId(),
            C_Currency_ID, currencyIDTo);
      }
      if (conversionRateCurrentDoc != null) {
        accountingRateCurrencyDocCurrencyTo = conversionRateCurrentDoc.getRate();
      } else {
        // I try to find a reversal rate for the doc, if exists i apply it reversal as well
        if (AD_Table_ID.equals(AcctServer.TABLEID_Reconciliation)
            && line instanceof DocLine_FINReconciliation) {
          String transactionID = ((DocLine_FINReconciliation) line).getFinFinAccTransactionId();
          FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
              transactionID);
          conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Transaction, transaction.getId(),
              currencyIDTo, C_Currency_ID);
        } else {
          conversionRateCurrentDoc = getConversionRateDoc(AD_Table_ID, Record_ID, currencyIDTo,
              C_Currency_ID);
        }
        if (conversionRateCurrentDoc != null) {
          accountingRateCurrencyDocCurrencyTo = BigDecimal.ONE.divide(
              conversionRateCurrentDoc.getRate(), MathContext.DECIMAL64);
        } else {
          accountingRateCurrencyDocCurrencyTo = getConvertionRate(C_Currency_ID, currencyIDTo,
              conversionDate, "", AD_Client_ID, AD_Org_ID, conn);
        }
      }
    }
    // Calculate transaction rate
    BigDecimal trxRate = BigDecimal.ONE;
    if (!C_Currency_ID.equals(currencyIDFrom)) {
      if (AD_Table_ID.equals(TABLEID_Payment)) {
        FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, Record_ID);
        trxRate = payment.getFinancialTransactionConvertRate();
      } else if (AD_Table_ID.equals(TABLEID_Transaction)
          || AD_Table_ID.equals(TABLEID_Reconciliation)) {
        String transactionID = Record_ID;
        // When TableID = Reconciliation info is loaded from transaction
        if (AD_Table_ID.equals(AcctServer.TABLEID_Reconciliation)
            && line instanceof DocLine_FINReconciliation) {
          transactionID = ((DocLine_FINReconciliation) line).getFinFinAccTransactionId();
        }
        FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
            transactionID);
        trxRate = transaction.getForeignConversionRate();
      }
    }
    Currency currencyFrom = OBDal.getInstance().get(Currency.class, currencyIDTo);
    return _amount.multiply(
        trxRate.multiply(accountingRateCurrencyDocCurrencyTo).subtract(
            accountingRateCurrencyFromCurrencyTo)).setScale(
        currencyFrom.getStandardPrecision().intValue(), BigDecimal.ROUND_HALF_EVEN);
  }

  public static BigDecimal applyRate(BigDecimal _amount, ConversionRateDoc conversionRateDoc,
      boolean multiply) {
    BigDecimal amount = _amount;
    if (multiply) {
      return amount.multiply(conversionRateDoc.getRate()).setScale(
          conversionRateDoc.getToCurrency().getStandardPrecision().intValue(),
          BigDecimal.ROUND_HALF_EVEN);
    } else {
      return amount.divide(conversionRateDoc.getRate(), 6, BigDecimal.ROUND_HALF_EVEN).setScale(
          conversionRateDoc.getToCurrency().getStandardPrecision().intValue(),
          BigDecimal.ROUND_HALF_EVEN);
    }
  }

  public static int getConversionRatePrecision(VariablesSecureApp vars) {
    try {
      String formatOutput = vars.getSessionValue("#FormatOutput|generalQtyEdition", "#0.######");
      String decimalSeparator = ".";
      if (formatOutput.contains(decimalSeparator)) {
        formatOutput = formatOutput.substring(formatOutput.indexOf(decimalSeparator),
            formatOutput.length());
        return formatOutput.length() - decimalSeparator.length();
      } else {
        return 0;
      }
    } catch (Exception e) {
      log4j.error(e);
      return 6; // by default precision of 6 decimals as is defaulted in Format.xml
    }
  }
}
