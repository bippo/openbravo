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

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public final class Account implements Serializable {
  private static final long serialVersionUID = 1L;
  static Logger log4jAccount = Logger.getLogger(Account.class);

  public Account() {
    C_ValidCombination_ID = "";
    C_AcctSchema_ID = "";
    AD_Client_ID = "";
    AD_Org_ID = "";
    Account_ID = "";
    M_Product_ID = "";
    C_BPartner_ID = "";
    AD_OrgTrx_ID = "";
    C_LocFrom_ID = "";
    C_LocTo_ID = "";
    C_SalesRegion_ID = "";
    C_Project_ID = "";
    C_Channel_ID = "";
    C_Campaign_ID = "";
    C_Activity_ID = "";
    User1_ID = "";
    User2_ID = "";
    alias = "";
    combination = "";
    description = "";
    active = "Y";
    updatedBy = "";
    fullyQualified = "F";
    m_AcctType = ' ';
    m_IsValid = null;
    m_OldAccount_ID = "";
    m_changed = "Y";
  }

  public Account(ConnectionProvider conn, String newC_ValidCombination_ID) throws ServletException {
    C_ValidCombination_ID = "";
    C_AcctSchema_ID = "";
    AD_Client_ID = "";
    AD_Org_ID = "";
    Account_ID = "";
    M_Product_ID = "";
    C_BPartner_ID = "";
    AD_OrgTrx_ID = "";
    C_LocFrom_ID = "";
    C_LocTo_ID = "";
    C_SalesRegion_ID = "";
    C_Project_ID = "";
    C_Channel_ID = "";
    C_Campaign_ID = "";
    C_Activity_ID = "";
    User1_ID = "";
    User2_ID = "";
    alias = "";
    combination = "";
    description = "";
    active = "Y";
    updatedBy = "";
    fullyQualified = "N";
    m_AcctType = ' ';
    m_IsValid = null;
    m_OldAccount_ID = "";
    m_changed = "Y";
    if (newC_ValidCombination_ID != null && newC_ValidCombination_ID.equals("")) {
      return;
    } else {
      load(null, conn, newC_ValidCombination_ID);
      return;
    }
  }

  public void load(Connection conn1, ConnectionProvider conn, String newC_ValidCombination_ID)
      throws ServletException {
    AccountData[] data = null;
    try {
      log4jAccount.debug("C_ValidCombination_ID: " + C_ValidCombination_ID);
      if (conn1 == null)
        data = AccountData.select(conn, newC_ValidCombination_ID);
      else
        data = AccountData.selectConnection(conn1, conn, newC_ValidCombination_ID);
      if (data.length > 0) {
        AD_Client_ID = data[0].adClientId;
        AD_Org_ID = data[0].adOrgId;
        active = data[0].isactive;
        updatedBy = data[0].updatedby;
        alias = data[0].alias;
        combination = data[0].combination;
        description = data[0].description;
        fullyQualified = data[0].isfullyqualified;
        C_AcctSchema_ID = data[0].cAcctschemaId;
        Account_ID = data[0].accountId;
        M_Product_ID = data[0].mProductId;
        C_BPartner_ID = data[0].cBpartnerId;
        AD_OrgTrx_ID = data[0].adOrgtrxId;
        C_LocFrom_ID = data[0].cLocfromId;
        C_LocTo_ID = data[0].cLoctoId;
        C_SalesRegion_ID = data[0].cSalesregionId;
        C_Project_ID = data[0].cProjectId;
        C_Campaign_ID = data[0].cCampaignId;
        C_Activity_ID = data[0].cActivityId;
        User1_ID = data[0].user1Id;
        User2_ID = data[0].user2Id;
        C_ValidCombination_ID = newC_ValidCombination_ID;
        m_changed = "N";
      } else {
        log4jAccount.warn("Account.getAccount - " + newC_ValidCombination_ID + " not found");
      }
    } catch (ServletException e) {
      C_ValidCombination_ID = "";
      log4jAccount.warn("Account.load: " + e);
      throw new ServletException(e);
    }
    log4jAccount.debug("C_ValidCombination_ID(fin): " + C_ValidCombination_ID);
  }

  public static Account getDefault(ConnectionProvider conn, String C_AcctSchema_ID,
      boolean optionalNull) {
    AcctSchema acctSchema = new AcctSchema(conn, C_AcctSchema_ID);
    return getDefault(acctSchema, optionalNull);
  }

  public static Account getDefault(AcctSchema acctSchema, boolean optionalNull) {
    Account vc = new Account();
    vc.C_AcctSchema_ID = acctSchema.m_C_AcctSchema_ID;
    ArrayList<?> list = acctSchema.m_elementList;
    for (int i = 0; i < list.size(); i++) {
      AcctSchemaElement ase = (AcctSchemaElement) list.get(i);
      String segmentType = ase.m_segmentType;
      String defaultValue = ase.m_defaultValue;
      boolean setValue = ase.m_mandatory.equals("Y") || !ase.m_mandatory.equals("Y")
          && !optionalNull;
      if (segmentType.equals("OO"))
        vc.AD_Org_ID = defaultValue;
      else if (segmentType.equals("AC"))
        vc.Account_ID = defaultValue;
      else if (segmentType.equals("BP") && setValue)
        vc.C_BPartner_ID = defaultValue;
      else if (segmentType.equals("PR") && setValue)
        vc.M_Product_ID = defaultValue;
      else if (segmentType.equals("AY") && setValue)
        vc.C_Activity_ID = defaultValue;
      else if (segmentType.equals("LF") && setValue)
        vc.C_LocFrom_ID = defaultValue;
      else if (segmentType.equals("LT") && setValue)
        vc.C_LocTo_ID = defaultValue;
      else if (segmentType.equals("MC") && setValue)
        vc.C_Campaign_ID = defaultValue;
      else if (segmentType.equals("OT") && setValue)
        vc.AD_OrgTrx_ID = defaultValue;
      else if (segmentType.equals("PJ") && setValue)
        vc.C_Project_ID = defaultValue;
      else if (segmentType.equals("SR") && setValue)
        vc.C_SalesRegion_ID = defaultValue;
      else if (segmentType.equals("U1") && setValue)
        vc.User1_ID = defaultValue;
      else if (segmentType.equals("U2") && setValue)
        vc.User2_ID = defaultValue;
    }

    log4jAccount.debug("Account.getDefault - Client_ID=" + vc.AD_Client_ID + ", Org_ID="
        + vc.AD_Org_ID + "AcctSchema_ID=" + vc.C_AcctSchema_ID + ", Account_ID=" + vc.Account_ID);
    return vc;
  }

  public static Account getAccount(ConnectionProvider conn, String C_ValidCombination_ID)
      throws ServletException {
    return new Account(conn, C_ValidCombination_ID);
  }

  public boolean save(Connection conn1, ConnectionProvider conn, String newAD_Client_ID,
      String newUpdatedBy) throws ServletException {
    AD_Client_ID = newAD_Client_ID;
    updatedBy = newUpdatedBy;
    return save(conn1, conn);
  }

  public boolean save(Connection conn1, ConnectionProvider conn) throws ServletException {
    log4jAccount.debug("Account.save - Client_ID=" + AD_Client_ID + ", Org_ID=" + AD_Org_ID
        + "AcctSchema_ID=" + C_AcctSchema_ID + ", Account_ID=" + Account_ID);
    String C_ValidCombination_ID = "";
    boolean saved = false;
    RespuestaCS respuestaCS;
    try {
      log4jAccount.debug("Account.save - Client_ID=" + AD_Client_ID + ", Org_ID=" + AD_Org_ID
          + "AcctSchema_ID=" + C_AcctSchema_ID + ", Account_ID=" + Account_ID + "alias : " + alias
          + ", updatedBy: " + updatedBy + ", M_Product_ID: " + M_Product_ID + ", C_BPartner_ID: "
          + C_BPartner_ID + ", AD_OrgTrx_ID: " + AD_OrgTrx_ID + ", C_LocFrom_ID : " + C_LocFrom_ID
          + ", C_SalesRegion_ID : " + C_SalesRegion_ID + ", C_Project_ID : " + C_Project_ID
          + ", C_Project_ID: " + C_Project_ID + ", C_Campaign_ID: " + C_Campaign_ID
          + ", C_Activity_ID: " + C_Activity_ID + ", User1_ID: " + User1_ID + ", User1_ID: "
          + User1_ID);
      respuestaCS = AccountData.GetValidAccountCombination(conn1, conn, AD_Client_ID, AD_Org_ID,
          C_AcctSchema_ID, Account_ID, "", "Y", alias, updatedBy, M_Product_ID, C_BPartner_ID,
          AD_OrgTrx_ID, C_LocFrom_ID, C_LocTo_ID, C_SalesRegion_ID, C_Project_ID, C_Campaign_ID,
          C_Activity_ID, User1_ID, User2_ID);
      C_ValidCombination_ID = respuestaCS.CValidCombinationId;
      m_changed = "Y";
      saved = true;
    } catch (ServletException e) {
      log4jAccount.warn("Account.save: " + e);
      m_changed = "N";
    }
    load(conn1, conn, C_ValidCombination_ID);
    return saved;
  }

  /**
   * Is this a Balance Sheet Account
   * 
   * @return boolean
   */
  public boolean isBalanceSheet() {
    return (m_AcctType == 'A' || m_AcctType == 'L' || m_AcctType == 'O');
  } // isBalanceSheet

  public String getAD_Org_ID() {
    return AD_Org_ID;
  }

  public String getAccount_ID() {
    return Account_ID;
  }

  public String C_ValidCombination_ID;
  public String C_AcctSchema_ID;
  public String AD_Client_ID;
  public String AD_Org_ID;
  public String Account_ID;
  public String M_Product_ID;
  public String C_BPartner_ID;
  public String AD_OrgTrx_ID;
  public String C_LocFrom_ID;
  public String C_LocTo_ID;
  public String C_SalesRegion_ID;
  public String C_Project_ID;
  public String C_Channel_ID;
  public String C_Campaign_ID;
  public String C_Activity_ID;
  public String User1_ID;
  public String User2_ID;
  public String alias;
  public String combination;
  public String description;
  public String active;
  public String updatedBy;
  public String fullyQualified;
  public char m_AcctType;
  public Boolean m_IsValid;
  public String m_OldAccount_ID;
  public String m_changed;

}
