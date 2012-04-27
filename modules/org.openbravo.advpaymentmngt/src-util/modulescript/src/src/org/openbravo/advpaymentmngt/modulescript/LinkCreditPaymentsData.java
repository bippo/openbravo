//Sqlc generated V1.O00-1
package org.openbravo.advpaymentmngt.modulescript;

import java.sql.*;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.data.UtilSql;
import java.util.*;

class LinkCreditPaymentsData implements FieldProvider {
static Logger log4j = Logger.getLogger(LinkCreditPaymentsData.class);
  private String InitRecordNumber="0";
  public String finPaymentId;
  public String usedCredit;
  public String cCurrencyId;
  public String adClientId;
  public String adOrgId;
  public String cBpartnerId;
  public String documentno;
  public String name;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("fin_payment_id") || fieldName.equals("finPaymentId"))
      return finPaymentId;
    else if (fieldName.equalsIgnoreCase("used_credit") || fieldName.equals("usedCredit"))
      return usedCredit;
    else if (fieldName.equalsIgnoreCase("c_currency_id") || fieldName.equals("cCurrencyId"))
      return cCurrencyId;
    else if (fieldName.equalsIgnoreCase("ad_client_id") || fieldName.equals("adClientId"))
      return adClientId;
    else if (fieldName.equalsIgnoreCase("ad_org_id") || fieldName.equals("adOrgId"))
      return adOrgId;
    else if (fieldName.equalsIgnoreCase("c_bpartner_id") || fieldName.equals("cBpartnerId"))
      return cBpartnerId;
    else if (fieldName.equalsIgnoreCase("documentno"))
      return documentno;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
   else {
     log4j.debug("Field does not exist: " + fieldName);
     return null;
   }
 }

  public static LinkCreditPaymentsData[] selectPaymentsUsingCredit(ConnectionProvider connectionProvider, String isReceipt, String cbPartnerId)    throws ServletException {
    return selectPaymentsUsingCredit(connectionProvider, isReceipt, cbPartnerId, 0, 0);
  }

  public static LinkCreditPaymentsData[] selectPaymentsUsingCredit(ConnectionProvider connectionProvider, String isReceipt, String cbPartnerId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT FIN_PAYMENT_ID, USED_CREDIT, C_CURRENCY_ID, AD_CLIENT_ID, AD_ORG_ID, C_BPARTNER_ID, DOCUMENTNO, '' AS NAME" +
      "        FROM FIN_PAYMENT P" +
      "        WHERE NOT EXISTS (SELECT 1 " +
      "                          FROM FIN_PAYMENT_CREDIT PC" +
      "                          WHERE PC.FIN_PAYMENT_ID = P.FIN_PAYMENT_ID" +
      "                          )       " +
      "        AND P.GENERATED_CREDIT = 0 AND P.USED_CREDIT > 0" +
      "        AND P.ISRECEIPT = ?" +
      "        AND P.C_BPARTNER_ID = ?" +
      "        AND P.PROCESSED = 'Y'" +
      "        AND P.STATUS NOT IN ('RPAE','RPVOID')" +
      "        ORDER BY P.PAYMENTDATE, P.DOCUMENTNO";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, isReceipt);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cbPartnerId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        LinkCreditPaymentsData objectLinkCreditPaymentsData = new LinkCreditPaymentsData();
        objectLinkCreditPaymentsData.finPaymentId = UtilSql.getValue(result, "fin_payment_id");
        objectLinkCreditPaymentsData.usedCredit = UtilSql.getValue(result, "used_credit");
        objectLinkCreditPaymentsData.cCurrencyId = UtilSql.getValue(result, "c_currency_id");
        objectLinkCreditPaymentsData.adClientId = UtilSql.getValue(result, "ad_client_id");
        objectLinkCreditPaymentsData.adOrgId = UtilSql.getValue(result, "ad_org_id");
        objectLinkCreditPaymentsData.cBpartnerId = UtilSql.getValue(result, "c_bpartner_id");
        objectLinkCreditPaymentsData.documentno = UtilSql.getValue(result, "documentno");
        objectLinkCreditPaymentsData.name = UtilSql.getValue(result, "name");
        objectLinkCreditPaymentsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectLinkCreditPaymentsData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    LinkCreditPaymentsData objectLinkCreditPaymentsData[] = new LinkCreditPaymentsData[vector.size()];
    vector.copyInto(objectLinkCreditPaymentsData);
    return(objectLinkCreditPaymentsData);
  }

  public static LinkCreditPaymentsData[] selectPaymentsGeneratingCredit(ConnectionProvider connectionProvider, String isReceipt, String cbPartnerId)    throws ServletException {
    return selectPaymentsGeneratingCredit(connectionProvider, isReceipt, cbPartnerId, 0, 0);
  }

  public static LinkCreditPaymentsData[] selectPaymentsGeneratingCredit(ConnectionProvider connectionProvider, String isReceipt, String cbPartnerId, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT FIN_PAYMENT_ID, USED_CREDIT, C_CURRENCY_ID, DOCUMENTNO" +
      "        FROM FIN_PAYMENT P" +
      "        WHERE GENERATED_CREDIT > 0 AND USED_CREDIT > 0" +
      "        AND ISRECEIPT = ?" +
      "        AND C_BPARTNER_ID = ?" +
      "        AND P.PROCESSED = 'Y'" +
      "        AND P.STATUS NOT IN ('RPAE','RPVOID')" +
      "        ORDER BY PAYMENTDATE, DOCUMENTNO";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, isReceipt);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, cbPartnerId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        LinkCreditPaymentsData objectLinkCreditPaymentsData = new LinkCreditPaymentsData();
        objectLinkCreditPaymentsData.finPaymentId = UtilSql.getValue(result, "fin_payment_id");
        objectLinkCreditPaymentsData.usedCredit = UtilSql.getValue(result, "used_credit");
        objectLinkCreditPaymentsData.cCurrencyId = UtilSql.getValue(result, "c_currency_id");
        objectLinkCreditPaymentsData.documentno = UtilSql.getValue(result, "documentno");
        objectLinkCreditPaymentsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectLinkCreditPaymentsData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    LinkCreditPaymentsData objectLinkCreditPaymentsData[] = new LinkCreditPaymentsData[vector.size()];
    vector.copyInto(objectLinkCreditPaymentsData);
    return(objectLinkCreditPaymentsData);
  }

  public static int insertUsedCreditSource(ConnectionProvider connectionProvider, String clientId, String orgId, String paymentId, String creditpaymentId, String amount, String currencyId)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        INSERT INTO fin_payment_credit(" +
      "            fin_payment_credit_id, ad_client_id, ad_org_id, created, createdby, " +
      "            updated, updatedby, isactive, fin_payment_id, fin_payment_id_used, " +
      "            amount, c_currency_id)" +
      "        VALUES (get_uuid(), ?, ?, now(), '100'," +
      "                now(), '100', 'Y', ?, ?, " +
      "                TO_NUMBER( ? ), ?)";

    int updateCount = 0;
    PreparedStatement st = null;

    int iParameter = 0;
    try {
    st = connectionProvider.getPreparedStatement(strSql);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, clientId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, orgId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, paymentId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, creditpaymentId);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, amount);
      iParameter++; UtilSql.setValue(st, iParameter, 12, null, currencyId);

      updateCount = st.executeUpdate();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    return(updateCount);
  }

  public static LinkCreditPaymentsData[] selectBusinessPartners(ConnectionProvider connectionProvider)    throws ServletException {
    return selectBusinessPartners(connectionProvider, 0, 0);
  }

  public static LinkCreditPaymentsData[] selectBusinessPartners(ConnectionProvider connectionProvider, int firstRegister, int numberRegisters)    throws ServletException {
    String strSql = "";
    strSql = strSql + 
      "        SELECT C_BPARTNER_ID, NAME" +
      "        FROM C_BPARTNER BP" +
      "        WHERE EXISTS (SELECT 1" +
      "                      FROM FIN_PAYMENT P" +
      "                      WHERE P.C_BPARTNER_ID = BP.C_BPARTNER_ID" +
      "            AND USED_CREDIT > 0" +
      "            AND GENERATED_CREDIT = 0" +
      "            AND PROCESSED = 'Y'" +
      "            AND NOT EXISTS(SELECT 1 FROM FIN_PAYMENT_CREDIT" +
      "                    WHERE FIN_PAYMENT_CREDIT.FIN_PAYMENT_ID = P.FIN_PAYMENT_ID))";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    try {
    st = connectionProvider.getPreparedStatement(strSql);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while(countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while(continueResult && result.next()) {
        countRecord++;
        LinkCreditPaymentsData objectLinkCreditPaymentsData = new LinkCreditPaymentsData();
        objectLinkCreditPaymentsData.cBpartnerId = UtilSql.getValue(result, "c_bpartner_id");
        objectLinkCreditPaymentsData.name = UtilSql.getValue(result, "name");
        objectLinkCreditPaymentsData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectLinkCreditPaymentsData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch(SQLException e){
      log4j.error("SQL error in query: " + strSql + "Exception:"+ e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@" + e.getMessage());
    } catch(Exception ex){
      log4j.error("Exception in query: " + strSql + "Exception:"+ ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch(Exception ignore){
        ignore.printStackTrace();
      }
    }
    LinkCreditPaymentsData objectLinkCreditPaymentsData[] = new LinkCreditPaymentsData[vector.size()];
    vector.copyInto(objectLinkCreditPaymentsData);
    return(objectLinkCreditPaymentsData);
  }
}
