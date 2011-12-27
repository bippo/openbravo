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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import org.apache.log4j.Logger;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;

public class DocLine_FINPayment extends DocLine {
  private static final Logger log4j = Logger.getLogger(DocLine_FINPayment.class);

  String Line_ID = "";
  String Amount = "";
  String WriteOffAmt = "";
  String isReceipt = "";
  String C_GLItem_ID = "";
  String isPrepayment = "";
  Invoice invoice = null;
  Order order = null;

  public Invoice getInvoice() {
    return invoice;
  }

  public void setInvoice(Invoice invoice) {
    this.invoice = invoice;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  /**
   * @param isReceipt
   *          the isReceipt to set
   */
  public void setIsReceipt(String isReceipt) {
    this.isReceipt = isReceipt;
  }

  /**
   * @return the isReceipt
   */
  public String getIsReceipt() {
    return isReceipt;
  }

  /**
   * @param isPrepayment
   *          the isPrepayment to set
   */
  public void setIsPrepayment(String isPrepayment) {
    this.isPrepayment = isPrepayment;
  }

  /**
   * @return the isPrepayment
   */
  public String getIsPrepayment() {
    return isPrepayment;
  }

  /**
   * @return the amount
   */
  public String getAmount() {
    return Amount;
  }

  /**
   * @return the line_ID
   */
  public String getLine_ID() {
    return Line_ID;
  }

  /**
   * @param line_ID
   *          the line_ID to set
   */
  public void setLine_ID(String line_ID) {
    Line_ID = line_ID;
  }

  /**
   * @return the writeOffAmt
   */
  public String getWriteOffAmt() {
    return WriteOffAmt;
  }

  /**
   * @param writeOffAmt
   *          the writeOffAmt to set
   */
  public void setWriteOffAmt(String writeOffAmt) {
    WriteOffAmt = writeOffAmt;
  }

  /**
   * @return the c_GLItem_ID
   */
  public String getC_GLItem_ID() {
    return C_GLItem_ID;
  }

  /**
   * @param item_ID
   *          the c_GLItem_ID to set
   */
  public void setC_GLItem_ID(String item_ID) {
    C_GLItem_ID = item_ID;
  }

  /**
   * @param amount
   *          the amount to set
   */
  public void setAmount(String amount) {
    Amount = amount;
  }

  public DocLine_FINPayment(String DocumentType, String TrxHeader_ID, String TrxLine_ID) {
    super(DocumentType, TrxHeader_ID, TrxLine_ID);
    Line_ID = TrxLine_ID;
    m_Record_Id2 = Line_ID;
  }

  public String getServletInfo() {
    return "Servlet for accounting";
  } // end of getServletInfo() method
}
