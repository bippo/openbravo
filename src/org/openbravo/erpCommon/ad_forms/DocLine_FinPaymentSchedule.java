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
 * The Initial Developer of the Original Code is Openbravo SL
 * All portions are Copyright (C) 2001-2010 Openbravo SL
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import org.apache.log4j.Logger;

public class DocLine_FinPaymentSchedule extends DocLine {
  static Logger log4jDocLine_Payment = Logger.getLogger(DocLine_Payment.class);

  String Line_ID = "";
  String Amount = "";
  String isPaid = "";
  String C_GLItem_ID = "";
  String C_Currency_ID_From;
  String conversionDate;
  String C_INVOICE_ID = "";
  String C_ORDER_ID = "";
  String C_BPARTNER_ID = "";
  String PrepaidAmount = "";

  public DocLine_FinPaymentSchedule(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
    Line_ID = TrxLine_ID;
    m_Record_Id2 = Line_ID;
  }

  public String getLine_ID() {
    return Line_ID;
  }

  public void setLine_ID(String line_ID) {
    Line_ID = line_ID;
  }

  public String getAmount() {
    return Amount;
  }

  public void setAmount(String amount) {
    Amount = amount;
  }

  public String getIsPaid() {
    return isPaid;
  }

  public void setIsPaid(String isPaid) {
    this.isPaid = isPaid;
  }

  public String getC_GLItem_ID() {
    return C_GLItem_ID;
  }

  public void setC_GLItem_ID(String item_ID) {
    C_GLItem_ID = item_ID;
  }

  public String getC_Currency_ID_From() {
    return C_Currency_ID_From;
  }

  public void setC_Currency_ID_From(String currency_ID_From) {
    C_Currency_ID_From = currency_ID_From;
  }

  public String getConversionDate() {
    return conversionDate;
  }

  public void setConversionDate(String conversionDate) {
    this.conversionDate = conversionDate;
  }

  public String getC_INVOICE_ID() {
    return C_INVOICE_ID;
  }

  public void setC_INVOICE_ID(String c_invoice_id) {
    C_INVOICE_ID = c_invoice_id;
  }

  public String getC_ORDER_ID() {
    return C_ORDER_ID;
  }

  public void setC_ORDER_ID(String c_order_id) {
    C_ORDER_ID = c_order_id;
  }

  public String getC_BPARTNER_ID() {
    return C_BPARTNER_ID;
  }

  public void setC_BPARTNER_ID(String c_bpartner_id) {
    C_BPARTNER_ID = c_bpartner_id;
  }

  public String getPrepaidAmount() {
    return PrepaidAmount;
  }

  public void setPrepaidAmount(String prepaidAmount) {
    PrepaidAmount = prepaidAmount;
  }

  public String getServletInfo() {
    return "Servlet for accounting";
  } // end of getServletInfo() method
}
