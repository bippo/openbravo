/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.APRM = {};

OB.APRM.bankTransitoryAccountCalloutResponse = function (me, confirmMessage, financialAccountId) {
  isc.confirm(confirmMessage, function (value) {
    var post;
    if (value) {
      var bankTransitoryAccount = me.getField('fINTransitoryAcct')._value,
          bankTransitoryAccountDesc = me.getField('fINTransitoryAcct').valueMap[bankTransitoryAccount];

      me.getField('clearedPaymentAccount').valueMap[bankTransitoryAccount] = bankTransitoryAccountDesc;
      me.getField('clearedPaymentAccount').setValue(bankTransitoryAccount);
      me.getField('clearedPaymentAccountOUT').valueMap[bankTransitoryAccount] = bankTransitoryAccountDesc;
      me.getField('clearedPaymentAccountOUT').setValue(bankTransitoryAccount);

      post = {
        'eventType': 'bankTransitoryCalloutResponse',
        'financialAccountId': financialAccountId
      };

      OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.APRMActionHandler', post, {}, {});

    }
  });
};

OB.APRM.validateMPPUserWarnedAwaiting = false;
OB.APRM.validateMPPUserWarnedSign = false;

OB.APRM.validateModifyPaymentPlanAmounts = function (item, validator, value, record) {
  var indRow, allRows = item.grid.data,
      row, allGreen = true,
      totalExpected = new BigDecimal("0"),
      totalReceived = new BigDecimal("0"),
      totalOutstanding = new BigDecimal("0"),
      isNumber = isc.isA.Number,
      invoiceOutstanding = new BigDecimal(String(item.grid.view.parentWindow.views[0].getParentRecord().outstandingAmount));

  if (new BigDecimal(String(value)).compareTo(new BigDecimal("0")) !== 0 && (new BigDecimal(String(value)).compareTo(new BigDecimal("0")) !== invoiceOutstanding.compareTo(new BigDecimal("0")))) {
    if (!OB.APRM.validateMPPUserWarnedSign) {
      OB.APRM.validateMPPUserWarnedSign = true;
      isc.warn(OB.I18N.getLabel('APRM_DifferentSignError'));
    }
    return false;
  }

  for (indRow = 0; indRow < allRows.length; indRow++) {
    row = allRows[indRow];

    if (!isNumber(row.expected) || !isNumber(row.outstanding) || !isNumber(row.received)) {
      return false;
    }

    totalExpected = totalExpected.add(new BigDecimal(String(row.expected)));
    totalOutstanding = totalOutstanding.add(new BigDecimal(String(row.outstanding)));
    totalReceived = totalReceived.add(new BigDecimal(String(row.received)));
  }
  row.expected = Number(new BigDecimal(String(row.outstanding)).add(new BigDecimal(String(row.received))));
  if (totalOutstanding.abs().compareTo(invoiceOutstanding.abs()) !== 0) {
    return false;
  }
  if (new BigDecimal(String(record.awaitingExecutionAmount)).abs().compareTo(new BigDecimal(String(record.outstanding)).abs()) > 0) {
    if (!OB.APRM.validateMPPUserWarnedAwaiting) {
      OB.APRM.validateMPPUserWarnedAwaiting = true;
      isc.warn(OB.I18N.getLabel('APRM_AwaitingExecutionAmountError'));
    }
    return false;
  }
  for (indRow = 0; indRow < allRows.length; indRow++) {
    if (typeof item.grid.rowHasErrors(allRows[indRow]) !== 'undefined' && item.grid.rowHasErrors(allRows[indRow]) && allRows[indRow] !== record) {
      allGreen = false;
    }
  }
  if (allGreen) {
    OB.APRM.validateMPPUserWarnedAwaiting = false;
    OB.APRM.validateMPPUserWarnedSign = false;
  }
  return true;
};

OB.APRM.addNew = function (grid) {
  var selectedRecord = grid.view.parentWindow.views[0].getParentRecord();
  var returnObject = isc.addProperties({}, grid.data[0]);
  var indRow, allRows = grid.data,
      row, totalOutstanding = new BigDecimal("0");
  for (indRow = 0; indRow < allRows.length; indRow++) {
    row = allRows[indRow];
    totalOutstanding = totalOutstanding.add(new BigDecimal(String(row.outstanding)));
  }
  returnObject.outstanding = Number(new BigDecimal(String(selectedRecord.outstandingAmount)).subtract(totalOutstanding));
  returnObject.received = 0;
  returnObject.expected = 0;
  returnObject.awaitingExecutionAmount = 0;
  returnObject.id = '';
  returnObject.paymentMethod = selectedRecord.paymentMethod;
  returnObject['paymentMethod._identifier'] = selectedRecord['paymentMethod._identifier'];
  returnObject.currency = selectedRecord.currency;
  returnObject['currency._identifier'] = selectedRecord['currency._identifier'];
  returnObject.duedate = selectedRecord.invoiceDate;
  //General properties
  returnObject.organization = selectedRecord.organization;
  returnObject.client = selectedRecord.client;
  returnObject.invoice = selectedRecord.id;
  return returnObject;
};

OB.APRM.deleteRow = function (grid, rowNum, record) {
  if (new BigDecimal(String(record.awaitingExecutionAmount)).compareTo(new BigDecimal('0')) !== 0) {
    isc.warn(OB.I18N.getLabel('APRM_AwaitingExecutionAmountNotDeleted'));
    return false;
  }
  if (new BigDecimal(String(record.received)).compareTo(new BigDecimal('0')) !== 0) {
    isc.warn(OB.I18N.getLabel('APRM_ReceivedAmountNotDeleted'));
    return false;
  }
  return true;
};