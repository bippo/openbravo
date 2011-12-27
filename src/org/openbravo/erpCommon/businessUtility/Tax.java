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
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;

public class Tax {
  static Logger log4jTax = Logger.getLogger(Tax.class);

  public static String get(ConnectionProvider conn, String M_Product_ID, String shipDate,
      String AD_Org_ID, String M_Warehouse_ID, String billC_BPartner_Location_ID,
      String shipC_BPartner_Location_ID, String C_Project_ID, boolean IsSOTrx) throws IOException,
      ServletException {
    log4jTax.debug("Tax.get");
    return Tax.get(conn, M_Product_ID, shipDate, AD_Org_ID, M_Warehouse_ID,
        billC_BPartner_Location_ID, shipC_BPartner_Location_ID, C_Project_ID, IsSOTrx, "");

    // FIXME: If this code is not goint be used, it should be removed
    /*
     * System.out.println("billC_BPartner_Location_ID: " + billC_BPartner_Location_ID); String
     * billFrom = TaxData.billLocation(conn, AD_Org_ID); TaxData[] td = TaxData.billTo(conn,
     * billC_BPartner_Location_ID);
     * 
     * if (td==null || td.length==0) return ""; String billTo = td[0].billtocLocationId; String
     * IsTaxExempt = (IsSOTrx?td[0].istaxexempt:"N"); String IsSpecialTax =
     * (IsSOTrx?td[0].isspecialtax:"N"); String taxID=""; if (IsTaxExempt.equalsIgnoreCase("Y")) {
     * TaxData[] tdAux = TaxData.taxId(conn, AD_Org_ID); if (tdAux!=null && tdAux.length >0) taxID =
     * tdAux[0].cTaxId; } else { String taxCategory = TaxData.taxCategory(conn, M_Product_ID);
     * String shipFrom = TaxData.shipLocation(conn, M_Warehouse_ID); String shipTo =
     * TaxData.shipTo(conn, shipC_BPartner_Location_ID); System.out.println("taxCategory: " +
     * taxCategory); System.out.println("shipFrom: " + shipFrom); System.out.println("shipTo: " +
     * shipTo); if (!IsSOTrx) { String strAux = billTo; billTo = billFrom; billFrom = strAux; strAux
     * = shipTo; shipTo = shipFrom; shipFrom = strAux; System.out.println("shipFrom: " + shipFrom);
     * System.out.println("shipTo: " + shipTo); } System.out.println("taxCategory: " + taxCategory);
     * System.out.println("billFrom: " + billFrom); System.out.println("shipTo: " + shipTo);
     * System.out.println("shipDate: " + shipDate); System.out.println("IsSpecialTax: " +
     * IsSpecialTax); TaxData[] tdAux = TaxData.select(conn, taxCategory, billFrom, shipTo,
     * shipDate, IsSpecialTax); if (tdAux==null || tdAux.length==0) tdAux = TaxData.select2(conn,
     * taxCategory, billFrom, shipDate, IsSpecialTax); if (tdAux==null || tdAux.length==0) tdAux =
     * TaxData.select3(conn, billFrom, shipDate); if (tdAux!=null && tdAux.length!=0) taxID =
     * tdAux[0].cTaxId; } System.out.println("TaxID: " + taxID); return taxID;
     */
  }

  public static String get(ConnectionProvider conn, String M_Product_ID, String shipDate,
      String AD_Org_ID, String M_Warehouse_ID, String billC_BPartner_Location_ID,
      String shipC_BPartner_Location_ID, String C_Project_ID, boolean IsSOTrx, String account_id)
      throws IOException, ServletException {
    log4jTax.debug("Tax.get");
    return TaxData.taxGet(conn, M_Product_ID, shipDate, AD_Org_ID, M_Warehouse_ID,
        billC_BPartner_Location_ID, shipC_BPartner_Location_ID, C_Project_ID,
        (IsSOTrx ? "Y" : "N"), account_id);
  }

  public static String get(ConnectionProvider conn, String M_Product_ID, String shipDate,
      String AD_Org_ID, String M_Warehouse_ID, String billC_BPartner_Location_ID,
      String shipC_BPartner_Location_ID, String C_Project_ID, String IsSOTrx, String glItemId)
      throws IOException, ServletException {
    log4jTax.debug("Tax.get");
    return TaxData.taxGet(conn, M_Product_ID, shipDate, AD_Org_ID, M_Warehouse_ID,
        billC_BPartner_Location_ID, shipC_BPartner_Location_ID, C_Project_ID, IsSOTrx, glItemId);
  }

  public static String checkNumeric(String data) {
    if (data == null || data.length() == 0)
      return "";
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < data.length(); i++) {
      if (Character.isDigit(data.charAt(i)))
        sb.append(data.charAt(i));
    }
    return sb.toString();
  }

  public static String validateRoutingNo(String routingNo) {
    int longitud = checkNumeric(routingNo).length();
    if (longitud == 8 || longitud == 9) {
      return "";
    } else {
      return "PaymentBankRoutingNotValid";
    }
  }

  public static String validateCreditCardNumber(String creditCardNumber, String creditCardType) {
    if (creditCardNumber == null || creditCardType == null)
      return "CreditCardNumberError";
    String ccStartList = "";
    String ccLengthList = "";
    // FIXME: If we know that creditCardType is M and assign some vars, why
    // we do check creditCardType
    // variable again. We should use else clauses more intelligently to
    // avoid doing unnecessary comparations
    if (creditCardType.equals("M")) {
      ccStartList = "51,52,53,54,55";
      ccLengthList = "16";
    } else {
    }
    if (creditCardType.equals("V")) {
      ccStartList = "4";
      ccLengthList = "13,16";
    } else {
    }
    if (creditCardType.equals("A")) {
      ccStartList = "34,37";
      ccLengthList = "15";
    } else {
    }
    if (creditCardType.equals("N")) {
      ccStartList = "6011";
      ccLengthList = "16";
    } else {
    }
    if (creditCardType.equals("D")) {
      ccStartList = "300,301,302,303,304,305,36,38";
      ccLengthList = "14";
    } else {
      ccStartList = "2014,2149";
      ccLengthList = "15";
      ccStartList = ccStartList + ",3088,3096,3112,3158,3337,3528";
      ccLengthList = ccLengthList + ",16";
      ccStartList = ccStartList + ",2131,1800";
      ccLengthList = ccLengthList + ",15";
    }
    String ccNumber = checkNumeric(creditCardNumber);
    int ccLength = ccNumber.length();
    boolean ccLengthOK = false;
    StringTokenizer st = new StringTokenizer(ccLengthList, ",", false);
    do {
      if (!st.hasMoreTokens() || ccLengthOK)
        break;
      int l = Integer.parseInt(st.nextToken());
      if (ccLength == l)
        ccLengthOK = true;
    } while (true);
    if (!ccLengthOK) {
      return "CreditCardNumberError";
    }
    boolean ccIdentified = false;
    st = new StringTokenizer(ccStartList, ",", false);
    do {
      if (!st.hasMoreTokens() || ccIdentified)
        break;
      if (ccNumber.startsWith(st.nextToken()))
        ccIdentified = true;
    } while (true);
    String check = validateCreditCardNumber(ccNumber, "");
    if (check.length() != 0) {
      return check;
    }
    if (!ccIdentified) {
      return "CreditCardNumberProblem?";
    } else {
      return "";
    }
  }

}
