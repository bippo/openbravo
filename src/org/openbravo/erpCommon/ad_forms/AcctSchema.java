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
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public final class AcctSchema implements Serializable {
  private static final long serialVersionUID = 1L;
  static Logger log4jAcctSchema = Logger.getLogger(AcctSchema.class);

  public AcctSchema(ConnectionProvider conn, String C_AcctSchema_ID) {
    m_C_AcctSchema_ID = "";
    m_Name = null;
    m_GAAP = null;
    m_IsAccrual = "N";
    m_HasAlias = "Y";
    m_CostingMethod = null;
    m_C_Currency_ID = "";
    m_AD_Client_ID = "";
    m_IsTradeDiscountPosted = "N";
    m_IsDiscountCorrectsTax = "N";
    m_CurrencyRateType = "S";
    m_UseSuspenseBalancing = "N";
    m_SuspenseBalancing_Acct = null;
    m_UseSuspenseError = "N";
    m_SuspenseError_Acct = null;
    m_UseCurrencyBalancing = "N";
    m_CurrencyBalancing_Acct = null;
    m_DueTo_Acct = null;
    m_DueFrom_Acct = null;
    m_elementList = null;
    log4jAcctSchema.debug("AcctSchema - " + C_AcctSchema_ID);
    load(conn, C_AcctSchema_ID);
  }

  public void load(ConnectionProvider conn, String newC_AcctSchema_ID) {
    log4jAcctSchema.debug("AcctSchema.load - " + newC_AcctSchema_ID);
    m_C_AcctSchema_ID = newC_AcctSchema_ID;
    AcctSchemaData[] data;
    try {
      data = AcctSchemaData.select(conn, m_C_AcctSchema_ID);
      if (data.length == 1) {
        m_Name = data[0].name;
        m_GAAP = data[0].gaap;
        m_IsAccrual = data[0].isaccrual;
        m_CostingMethod = data[0].costingmethod;
        m_C_Currency_ID = data[0].cCurrencyId;
        m_HasAlias = data[0].hasalias;
        m_IsTradeDiscountPosted = data[0].istradediscountposted;
        m_IsDiscountCorrectsTax = data[0].isdiscountcorrectstax;
        m_AD_Client_ID = data[0].adClientId;
      }
      data = AcctSchemaData.selectAcctSchemaGL(conn, m_C_AcctSchema_ID);
      if (data.length == 1) {
        m_UseSuspenseBalancing = data[0].usesuspensebalancing;
        String ID = data[0].suspensebalancingAcct;
        if ("Y".equals(m_UseSuspenseBalancing) && !ID.equals(""))// antes
          // era
          // "0"
          m_SuspenseBalancing_Acct = Account.getAccount(conn, ID);
        else
          m_UseSuspenseBalancing = "N";
        log4jAcctSchema.debug("SuspenseBalancing=" + m_UseSuspenseBalancing + " "
            + m_SuspenseBalancing_Acct);
        m_UseSuspenseError = data[0].usesuspenseerror;
        ID = data[0].suspenseerrorAcct;
        if ("Y".equals(m_UseSuspenseError) && !ID.equals(""))// antes
          // era "0"
          m_SuspenseError_Acct = Account.getAccount(conn, ID);
        else
          m_UseSuspenseError = "N";
        log4jAcctSchema.debug("SuspenseError=" + m_UseSuspenseError + " " + m_SuspenseError_Acct);
        m_UseCurrencyBalancing = data[0].usecurrencybalancing;
        ID = data[0].currencybalancingAcct;
        if ("Y".equals(m_UseCurrencyBalancing) && !ID.equals(""))// antes
          // era
          // "0"
          m_CurrencyBalancing_Acct = Account.getAccount(conn, ID);
        else
          m_UseCurrencyBalancing = "N";
        log4jAcctSchema.debug("CurrencyBalancing=" + m_UseCurrencyBalancing + " "
            + m_CurrencyBalancing_Acct);
        ID = data[0].intercompanyduetoAcct;
        if (!ID.equals(""))// antes era "0"
          m_DueTo_Acct = Account.getAccount(conn, ID);
        ID = data[0].intercompanyduefromAcct;
        if (!ID.equals(""))// antes era "0"
          m_DueFrom_Acct = Account.getAccount(conn, ID);
      }
      m_elementList = AcctSchemaElement.getAcctSchemaElementList(conn, m_C_AcctSchema_ID);
    } catch (ServletException e) {
      log4jAcctSchema.warn("AcctSchema : " + e);
      m_C_AcctSchema_ID = "";
    }
  }

  /**
   * Use Currency Balancing
   * 
   * @return true if currency balancing
   */
  public boolean isCurrencyBalancing() {
    return "Y".equals(m_UseCurrencyBalancing);
  } // useCurrencyBalancing

  /**
   * Get Currency Balancing Account
   * 
   * @return currency balancing account
   */
  public Account getCurrencyBalancing_Acct() {
    return m_CurrencyBalancing_Acct;
  } // getCurrencyBalancing_Acct

  /**
   * Get AcctSchema Element
   * 
   * @param segmentType
   *          segment type - AcctSchemaElement.SEGMENT_
   * @return AcctSchemaElement
   */
  public AcctSchemaElement getAcctSchemaElement(String segmentType) {
    int size = m_elementList.size();
    for (int i = 0; i < size; i++) {
      AcctSchemaElement ase = (AcctSchemaElement) m_elementList.get(i);
      if (ase.m_segmentType.equals(segmentType))
        return ase;
    }
    return null;
  } // getAcctSchemaElement

  /**
   * Get Acct_Schema
   * 
   * @return C_AcctSchema_ID
   */
  public String getC_AcctSchema_ID() {
    return m_C_AcctSchema_ID;
  } // getC_AcctSchema_ID

  /**
   * Get C_Currency_ID
   * 
   * @return C_Currency_ID
   */
  public String getC_Currency_ID() {
    return m_C_Currency_ID;
  } // getC_Currency_ID

  /**
   * Get Currency Rate Type
   * 
   * @return Currency Rate Type
   */
  public String getCurrencyRateType() {
    return m_CurrencyRateType;
  } // getCurrencyRateType

  /**
   * Use Suspense Balancing
   * 
   * @return true if suspense balancing
   */
  public boolean isSuspenseBalancing() {
    return "Y".equals(m_UseSuspenseBalancing);
  } // useSuspenseBalancing

  /**
   * Is Accrual
   * 
   * @return true if accrual
   */
  public boolean isAccrual() {
    return "Y".equals(m_IsAccrual);
  } // isAccrual

  /**
   * Has AcctSchema Element
   * 
   * @param segmentType
   *          segment type - AcctSchemaElement.SEGMENT_
   * @return true if schema has segment type
   */
  public boolean isAcctSchemaElement(String segmentType) {
    return getAcctSchemaElement(segmentType) != null;
  } // isAcctSchemaElement

  /**
   * Get Suspense Balancing Account
   * 
   * @return suspense balancing account
   */
  public Account getSuspenseBalancing_Acct() {
    return m_SuspenseBalancing_Acct;
  } // getSuspenseBalancing_Acct

  /**
   * AcctSchema Element Array
   * 
   * @param AD_Client_ID
   *          client
   * @return AcctSchema Array of Client
   */
  public static AcctSchema[] getAcctSchemaArray(ConnectionProvider conn, String AD_Client_ID,
      String AD_Org_ID) {
    ArrayList<Object> list = getAcctSchemaList(conn, AD_Client_ID, AD_Org_ID);
    AcctSchema[] retValue = new AcctSchema[list.size()];
    list.toArray(retValue);
    return retValue;
  } // getAcctSchemaArray

  /**
   * Factory: Get AccountSchema List
   * 
   * @param AD_Client_ID
   *          client
   * @return ArrayList of AcctSchema of Client
   */
  public static synchronized ArrayList<Object> getAcctSchemaList(ConnectionProvider conn,
      String AD_Client_ID, String AD_Org_ID) {
    // Create New
    ArrayList<Object> list = new ArrayList<Object>();
    AcctSchemaData[] data = null;
    try {
      data = AcctSchemaData.selectAcctSchemas(conn, AD_Client_ID, AD_Org_ID);
      for (int i = 0; data.length > i; i++) {
        String as = data[i].cAcctschemaId;
        list.add(new AcctSchema(conn, as));
      }
    } catch (ServletException e) {
      log4jAcctSchema.warn(e);
    }
    // Save
    return list;
  } // getAcctSchemaList

  public String m_C_AcctSchema_ID;
  public String m_Name;
  public String m_GAAP;
  public String m_IsAccrual;
  public String m_HasAlias;
  public String m_CostingMethod;
  public String m_C_Currency_ID;
  public String m_AD_Client_ID;
  public String m_IsTradeDiscountPosted;
  public String m_IsDiscountCorrectsTax;
  public String m_CurrencyRateType;
  public String m_UseSuspenseBalancing;
  public Account m_SuspenseBalancing_Acct;
  public String m_UseSuspenseError;
  public Account m_SuspenseError_Acct;
  public String m_UseCurrencyBalancing;
  public Account m_CurrencyBalancing_Acct;
  public Account m_DueTo_Acct;
  public Account m_DueFrom_Acct;
  ArrayList<Object> m_elementList;

  // Costing Methods
  public static final String COSTING_AVERAGE = "A";
  public static final String COSTING_STANDARD = "S";
  // public static final String COSTING_FIFO = "F";
  // public static final String COSTING_LIFO = "L";
  public static final String COSTING_LASTPO = "P";

}
