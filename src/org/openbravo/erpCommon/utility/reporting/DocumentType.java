/*
 * 
 * ************************************************************************ The
 * contents of this file are subject to the Openbravo Public License Version 1.1
 * (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2010 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 * ***********************************************************************
 */
package org.openbravo.erpCommon.utility.reporting;

public enum DocumentType {
  UNKNOWN(null, null), QUOTATION("C_ORDER", "quotations/"), SALESORDER("C_ORDER", "orders/"), SALESINVOICE(
      "C_INVOICE", "invoices/"), SHIPMENT("M_INOUT", "shipments/"), PURCHASEORDER("C_ORDER",
      "purchaseorders/"), PAYMENT("FIN_PAYMENT", "payments/");

  private String _tableName;
  private String _contextSubFolder;

  private DocumentType(String tableName, String contextSubFolder) {
    _tableName = tableName;
    _contextSubFolder = contextSubFolder;
  }

  public String getTableName() {
    return _tableName;
  }

  public String getContextSubFolder() {
    return _contextSubFolder;
  }
}
