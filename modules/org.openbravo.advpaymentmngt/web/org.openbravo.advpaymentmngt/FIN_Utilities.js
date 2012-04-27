/*global getGlobalDecSeparator, getGlobalGroupSeparator, getGlobalGroupInterval,
  formatNameToMask, returnMaskChange, getDefaultMaskNumeric, getElementsByName,
  displayLogicElement, returnFormattedNumber, returnFormattedToCalc, roundNumber,
  returnCalcToFormatted, setWindowElementFocus, showJSMessage, initialize_MessageBox,
  updateData, top, getFrame*/

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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

//Global variables definition
var frm = null,
    isReceipt = true,
    isCreditAllowed = true,
    isCreditCheckedFromBPinGrid = false,
    isGLItemEnabled = false,
    globalMaskNumeric = "#0.00",
    globalDecSeparator = ".",
    globalGroupSeparator = ",",
    globalGroupInterval = "3";

function isTrue(objectName) {
  return frm.elements[objectName].value === 'Y';
}

function initFIN_Utilities(_frm, _creditAllowed, _isCreditCheckedFromBPinGrid, _isGLItemEnabled) {
  frm = _frm;
  isReceipt = isTrue('isReceipt');
  isCreditAllowed = _creditAllowed !== undefined ? _creditAllowed : true;
  isCreditCheckedFromBPinGrid = _isCreditCheckedFromBPinGrid !== undefined ? _isCreditCheckedFromBPinGrid : false;
  isGLItemEnabled = _isGLItemEnabled !== undefined ? _isGLItemEnabled : false;
  if (!isCreditAllowed) {
    frm.inpUseCredit.checked = false;
  }
  globalDecSeparator = getGlobalDecSeparator();
  globalGroupSeparator = getGlobalGroupSeparator();
  globalGroupInterval = getGlobalGroupInterval();

  globalMaskNumeric = formatNameToMask('euroEdition');
  if (!globalMaskNumeric && OB && OB.Format && OB.Format.formats) {
    globalMaskNumeric = OB.Format.formats.euroEdition;
    globalMaskNumeric = returnMaskChange(globalMaskNumeric, '.', ',', globalDecSeparator, globalGroupSeparator);
  }
  globalMaskNumeric = globalMaskNumeric || getDefaultMaskNumeric();
}

function processLabels() {
  var receiptlbls = getElementsByName('lblR'),
      i;
  for (i = 0; i < receiptlbls.length; i++) {
    displayLogicElement(receiptlbls[i].id, isReceipt);
  }
  var paidlbls = getElementsByName('lblP');
  for (i = 0; i < paidlbls.length; i++) {
    displayLogicElement(paidlbls[i].id, !isReceipt);
  }
}

function selectDifferenceAction(value) {
  var diffAction = frm.inpDifferenceAction,
      i;
  for (i = 0; i < diffAction.length; i++) {
    diffAction[i].checked = false;
    diffAction[i].checked = (diffAction[i].value === value);
  }
}

/**
 * Function that transform a plain number into a formatted one
 * @param {String} number to be formated
 * @return The converted number
 * @type String
 */

function applyFormat(number) {
  return returnFormattedNumber(number, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

/**
 * Function that transform a JavaScript number into a formatted one
 * @param {String} number to be formated
 * @return The converted number
 * @type String
 */

function applyFormatJSToOBMasked(number) {
  return OB.Utilities.Number.JSToOBMasked(number, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

function applyFormatOBMaskedToJS(number) {
  return OB.Utilities.Number.OBMaskedToJS(number, globalDecSeparator, globalGroupSeparator);
}


/**
 * Function to operate with formatted number
 * @param {Number} number1 The first operand
 * @param {String} operator The operator (+ - * / % < > <= >= ...)
 * @param {Number} number2 The second operand
 * @param {String} result_maskNumeric The numeric mask of the result
 * @param {String} decSeparator The decimal separator of the number
 * @param {String} groupSeparator The group separator of the number
 * @param {String} groupInterval The group interval of the number
 * @return The result of the operation or true or false if the operator is (< > <= >= ...)
 * @type String or Boolean
 * @deprecated TO BE REMOVED ON MP22
 */

function formattedNumberOpTemp(number1, operator, number2, result_maskNumeric, decSeparator, groupSeparator, groupInterval) {
  var result;

  if (result_maskNumeric === null || result_maskNumeric === "") {
    result_maskNumeric = getDefaultMaskNumeric();
  }
  if (decSeparator === null || decSeparator === "") {
    decSeparator = getGlobalDecSeparator();
  }
  if (groupSeparator === null || groupSeparator === "") {
    groupSeparator = getGlobalGroupSeparator();
  }
  if (groupInterval === null || groupInterval === "") {
    groupInterval = getGlobalGroupInterval();
  }

  number1 = returnFormattedToCalc(number1, decSeparator, groupSeparator);
  number1 = parseFloat(number1);

  number2 = returnFormattedToCalc(number2, decSeparator, groupSeparator);
  number2 = parseFloat(number2);

  if (operator === "sqrt") {
    result = Math.sqrt(number1);
  } else if (operator === "round") {
    result = roundNumber(number1, number2);
  } else {
    result = eval('(' + number1 + ')' + operator + '(' + number2 + ')');
  }
  if (result !== true && result !== false && result !== null && result !== "") {
    result = returnCalcToFormatted(result, result_maskNumeric, decSeparator, groupSeparator, groupInterval);
  }
  return result;
}

/**
 * Calculates the absolute value using the global formats
 * @param {String} number1 The number
 * @return The result of the Math.abs() operation in a formatted string
 * @type String
 */

function abs(number1) {
  var result;
  number1 = returnFormattedToCalc(number1, globalDecSeparator, globalGroupSeparator);
  number1 = parseFloat(number1);
  result = Math.abs(number1);

  if (result !== null && result !== "") {
    result = returnCalcToFormatted(result, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
  }
  return result;
}

/**
 * Arithmetic add operation of two Strings using the global formats.
 * @param {String} number1 The first operand
 * @param {String} number2 The second operand
 * @return The result of adding number1 to number2 using the global formats.
 * @type String
 */

function add(number1, number2) {
  return formattedNumberOpTemp(number1, '+', number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

/**
 * Arithmetic subtract operation of two Strings using the global formats.
 * @param {String} number1 The first operand
 * @param {String} number2 The second operand
 * @return The result of adding number1 to number2 using the global formats.
 * @type String
 */

function subtract(number1, number2) {
  return formattedNumberOpTemp(number1, '-', number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

/**
 * Arithmetic divide operation of two Strings using the global formats.
 * @param {String} number1 The first operand
 * @param {String} number2 The second operand
 * @return The result of dividing number1 to number2 using the global formats.
 * @type String
 */

function divide(number1, number2) {
  return formattedNumberOpTemp(number1, '/', number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

/**
 * Arithmetic multiply operation of two Strings using the global formats.
 * @param {String} number1 The first operand
 * @param {String} number2 The second operand
 * @return The result of multiplying number1 to number2 using the global formats.
 * @type String
 */

function multiply(number1, number2) {
  return formattedNumberOpTemp(number1, '*', number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

/**
 * Compares two Strings using the operator
 * @param {String} number1 The first operand
 * @param {String} operator The operator (+ - * / % < > <= >= ...)
 * @param {String} number2 The second operand
 * @return true or false
 * @type boolean
 */

function compare(number1, operator, number2) {
  return formattedNumberOpTemp(number1, operator, number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
}

/**
 * Compares two Strings using the operator.
 * If both numbers are negative it compares using the absolute value.
 */

function compareWithSign(number1, operator, number2) {
  if (compare(number1, '<', 0) && compare(number2, '<', 0)) {
    return formattedNumberOpTemp(abs(number1), operator, abs(number2), globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
  } else {
    return formattedNumberOpTemp(number1, operator, number2, globalMaskNumeric, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
  }
}

function isBetweenZeroAndMaxValue(value, maxValue) {
  return ((compare(value, '>=', 0) && compare(value, '<=', maxValue)) || (compare(value, '<=', 0) && compare(value, '>=', maxValue)));
}

function applyPrecisionToMask(currencyPrecision) {
  var i, c, output, currentDecimalMask, currentPrecision;
  var toConvertDecimalMask = globalMaskNumeric;
  if (globalMaskNumeric.indexOf(globalDecSeparator) !== -1) {
    currentDecimalMask = globalMaskNumeric.substring(globalMaskNumeric.indexOf(globalDecSeparator), globalMaskNumeric.length);
    currentPrecision = currentDecimalMask.length - globalDecSeparator.length;
    if (currentPrecision) {
      toConvertDecimalMask = globalDecSeparator;
      c = currentDecimalMask.charAt(1);
      for (i = 0; i < currencyPrecision; i++) {
        toConvertDecimalMask = toConvertDecimalMask + c;
      }
      toConvertDecimalMask = globalMaskNumeric.replace(currentDecimalMask, toConvertDecimalMask);
    }
  }
  return toConvertDecimalMask;
}

function updateConvertedAmounts(recalcExchangeRate) {
  var exchangeRate = frm.inpExchangeRate;
  var precision = frm.inpFinancialAccountCurrencyPrecision ? frm.inpFinancialAccountCurrencyPrecision.value : 2;
  var roundedMask = applyPrecisionToMask(precision);
  var expectedConverted = frm.inpExpectedConverted;
  var actualConverted = frm.inpActualConverted;
  var expectedPayment = frm.inpExpectedPayment;
  var actualPayment = frm.inpActualPayment;

  if (actualConverted && expectedConverted && exchangeRate) {
    if (recalcExchangeRate) {
      if (actualConverted.value && actualPayment.value) {
        exchangeRate.value = formattedNumberOpTemp(actualConverted.value, '/', actualPayment.value, roundedMask, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
      } else {
        exchangeRate.value = '';
      }
    } else {
      actualConverted.value = formattedNumberOpTemp(actualPayment.value, '*', exchangeRate.value, roundedMask, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
    }
    expectedConverted.value = formattedNumberOpTemp(expectedPayment.value, '*', exchangeRate.value, roundedMask, globalDecSeparator, globalGroupSeparator, globalGroupInterval);
  }
}

function validateSelectedAmounts(recordID, existsPendingAmount, selectedAction) {
  var pendingAmount = document.frmMain.elements["inpRecordAmt" + recordID].value,
      amount = document.frmMain.elements["inpPaymentAmount" + recordID].value;
  if (existsPendingAmount === null) {
    existsPendingAmount = false;
  }
  if (amount === null || amount === "") {
    setWindowElementFocus(frm.elements["inpPaymentAmount" + recordID]);
    showJSMessage(7);
    return false;
  }
  if (!isBetweenZeroAndMaxValue(amount, pendingAmount)) {
    setWindowElementFocus(frm.elements["inpPaymentAmount" + recordID]);
    showJSMessage(9);
    return false;
  }
  // Only possible to pay 0 in case of a write off
  if (selectedAction !== "writeoff" && compare(amount, '==', 0)) {
    setWindowElementFocus(frm.elements["inpPaymentAmount" + recordID]);
    showJSMessage(9);
    return false;
  }
  if (existsPendingAmount && compare(amount, '<', pendingAmount)) {
    setWindowElementFocus(frm.elements["inpPaymentAmount" + recordID]);
    showJSMessage('APRM_JSNOTALLAMOUTALLOCATED');
    return false;
  }
  return true;
}

function updateDifference() {
  var expected = (frm.inpExpectedPayment && frm.inpExpectedPayment.value) ? frm.inpExpectedPayment.value : applyFormat('0'),
      total = (frm.inpTotal && frm.inpTotal.value) ? frm.inpTotal.value : applyFormat('0'),
      amount = total,
      invoicedAmount = total;

  if (isGLItemEnabled) {
    invoicedAmount = frm.inpInvoiceAmount.value;
  }

  if (frm.inpActualPayment !== null) {
    amount = frm.inpActualPayment.value;
  }
  if (frm.inpUseCredit.checked) {
    amount = add(amount, frm.inpCredit.value);
  }
  if (compareWithSign(expected, '>', total)) {
    frm.inpDifference.value = subtract(expected, total);
  } else if (compareWithSign(amount, '>', total)) {
    frm.inpDifference.value = subtract(amount, total);
  } else {
    frm.inpDifference.value = applyFormat('0');
  }
  document.getElementById('paramDifference').innerHTML = frm.inpDifference.value;
  displayLogicElement('sectionDifference', (compare(expected, '!=', total) || compareWithSign(amount, '>', total)));
  displayLogicElement('sectionDifferenceBox', (compare(expected, '!=', total) || (isCreditAllowed && compareWithSign(amount, '>', total))));
  displayLogicElement('writeoff', compare(expected, '!=', total));
  displayLogicElement('underpayment', compareWithSign(expected, '>', total));
  displayLogicElement('credit', isCreditAllowed && compareWithSign(amount, '>', total));
  displayLogicElement('refund', isCreditAllowed && isReceipt && compareWithSign(amount, '>', total));
  if (!(compare(expected, '!=', total) || (isCreditAllowed && compareWithSign(amount, '>', total)))) {
    // No action available
    selectDifferenceAction('none');
  } else if (isCreditAllowed && compareWithSign(amount, '>', total)) {
    selectDifferenceAction('credit');
  } else if (!isCreditAllowed || compareWithSign(expected, '>', total)) {
    selectDifferenceAction('underpayment');
  } else {
    selectDifferenceAction('none');
  }
}

function updateTotal() {
  var chk = frm.inpScheduledPaymentDetailId;
  var total = applyFormat('0'),
      i, invalidSpan;
  var scheduledPaymentDetailId, pendingAmount, amount, isAnyChecked = false;
  var selectedBusinessPartners = {
    numberofitems: 0,
    increase: function (obj) {
      if (obj && obj.value) {
        var key = obj.value;
        var value = this[key];
        if (value) {
          this[key] = value + 1;
        } else {
          this[key] = 1;
          this.numberofitems = this.numberofitems + 1;
        }
      }
    },
    reset: function () {
      var i;
      this.numberofitems = 0;
      for (i in this) {
        if (this.hasOwnProperty(i)) {
          if (typeof this[i] !== "function") {
            this[i] = 0;
          }
        }
      }
    },
    isMultibpleSelection: function () {
      return (this.numberofitems > 1);
    }
  };

  selectedBusinessPartners.reset();

  if (!chk) {
    if (frm.inpGeneratedCredit && !isReceipt) {
      frm.inpActualPayment.value = frm.inpGeneratedCredit.value;
    }
    updateDifference();
    //if (OB.APRM.HasGLItems === 'undefined' || !OB.APRM.HasGLItems) {
    //return;
    //}
  } else if (!chk.length) {
    scheduledPaymentDetailId = frm.inpRecordId0.value;
    pendingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
    amount = frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
    if (amount !== "" && !isBetweenZeroAndMaxValue(amount, pendingAmount)) {
      setWindowElementFocus(frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
    } else {
      initialize_MessageBox('messageBoxID');
    }
    if (chk.checked) {
      invalidSpan = document.getElementById('paraminvalidSpan' + scheduledPaymentDetailId);
      if (invalidSpan) {
        document.getElementById('paraminvalidSpan' + scheduledPaymentDetailId).style.display = !isBetweenZeroAndMaxValue(amount, pendingAmount) ? 'block' : 'none';
      }
      total = (frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value === '') ? "0" : frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
      selectedBusinessPartners.increase(frm.elements['inpRecordBP' + scheduledPaymentDetailId]);
      isAnyChecked = true;
    }
  } else {
    var rows = chk.length;
    for (i = 0; i < rows; i++) {
      scheduledPaymentDetailId = frm.elements["inpRecordId" + i].value;
      pendingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
      amount = frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value;
      if (amount !== "" && !isBetweenZeroAndMaxValue(amount, pendingAmount)) {
        setWindowElementFocus(frm.elements["inpPaymentAmount" + scheduledPaymentDetailId]);
      } else {
        initialize_MessageBox('messageBoxID');
      }
      if (chk[i].checked) {
        invalidSpan = document.getElementById('paraminvalidSpan' + scheduledPaymentDetailId);
        if (invalidSpan) {
          document.getElementById('paraminvalidSpan' + scheduledPaymentDetailId).style.display = !isBetweenZeroAndMaxValue(amount, pendingAmount) ? 'block' : 'none';
        }
        total = (frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value === '') ? total : add(total, frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value);
        selectedBusinessPartners.increase(frm.elements['inpRecordBP' + scheduledPaymentDetailId]);
        isAnyChecked = true;
      }
    }
  }
  if (isGLItemEnabled) {
    frm.inpInvoiceAmount.value = total;
    document.getElementById('paramInvoicesAmt').innerHTML = frm.inpInvoiceAmount.value;
    total = add(total, frm.inpGLSumAmount.value);
  }
  frm.inpTotal.value = total;
  document.getElementById('paramTotal').innerHTML = frm.inpTotal.value;
  var inheritedActualPayment = (frm.paramInheritedActualPayment && frm.paramInheritedActualPayment.value === "Y");
  if (!isReceipt && !inheritedActualPayment) {
    if (frm.inpUseCredit.checked) {
      if (compare(total, '>', frm.inpCredit.value)) {
        frm.inpActualPayment.value = subtract(total, frm.inpCredit.value);
      } else {
        frm.inpActualPayment.value = 0;
      }
    } else {
      if (isAnyChecked) {
        frm.inpActualPayment.value = frm.inpTotal.value;
      }
      if (frm.inpGeneratedCredit) {
        frm.inpActualPayment.value = add(frm.inpTotal.value, frm.inpGeneratedCredit.value);
      }
    }
  }
  if (isCreditCheckedFromBPinGrid) {
    isCreditAllowed = !selectedBusinessPartners.isMultibpleSelection();
  }
  updateDifference();
  updateConvertedAmounts();
}

function distributeAmount(_amount) {
  var amount = applyFormat(_amount);
  var chk = frm.inpScheduledPaymentDetailId;
  var scheduledPaymentDetailId, outstandingAmount, j, i;
  if (isGLItemEnabled) {
    amount = subtract(amount, frm.inpGLSumAmount.value);
  }

  if (!chk) {
    updateTotal();
    return;
  } else if (!chk.length) {
    scheduledPaymentDetailId = frm.inpRecordId0.value;
    outstandingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
    if (compare(outstandingAmount, '>', amount)) {
      outstandingAmount = amount;
    }
    frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = outstandingAmount;
    if (!chk.checked) {
      chk.checked = true;
      updateData(chk.value, chk.checked);
    }
  } else {
    var total = chk.length;
    for (i = 0; i < total; i++) {
      scheduledPaymentDetailId = frm.elements["inpRecordId" + i].value;
      outstandingAmount = frm.elements["inpRecordAmt" + scheduledPaymentDetailId].value;
      if (compare(outstandingAmount, '>', amount)) {
        outstandingAmount = amount;
      }
      if (compare(amount, '==', 0)) {
        frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = "";
        for (j = 0; j < total; j++) {
          if (chk[j].checked && chk[j].value === scheduledPaymentDetailId) {
            chk[j].checked = false;
            updateData(chk[j].value, chk[j].checked);
          }
        }
      } else {
        frm.elements["inpPaymentAmount" + scheduledPaymentDetailId].value = outstandingAmount;
        for (j = 0; j < total; j++) {
          if (!chk[j].checked && chk[j].value === scheduledPaymentDetailId) {
            chk[j].checked = true;
            updateData(chk[j].value, chk[j].checked);
          }
        }
        amount = subtract(amount, outstandingAmount);
      }
    }
  }
  updateTotal();
  return true;
}

function updateReadOnly(key, mark) {
  if (mark === null) {
    mark = false;
  }
  frm.elements["inpPaymentAmount" + key].disabled = !mark;
  var expectedAmount = frm.inpExpectedPayment.value,
      invalidSpan;
  var recordAmount = frm.elements["inpRecordAmt" + key].value;

  if (mark) {
    frm.elements["inpPaymentAmount" + key].className = frm.elements["inpPaymentAmount" + key].className.replace(' readonly', '');
    frm.inpExpectedPayment.value = add(expectedAmount, recordAmount);
  } else {
    var classText = frm.elements["inpPaymentAmount" + key].className;
    if (classText.search('readonly') === -1) {
      frm.elements["inpPaymentAmount" + key].className = classText.concat(" readonly");
    }
    frm.elements["inpPaymentAmount" + key].value = '';
    frm.inpExpectedPayment.value = subtract(expectedAmount, recordAmount);
    invalidSpan = document.getElementById('paraminvalidSpan' + key);
    if (invalidSpan) {
      document.getElementById('paraminvalidSpan' + key).style.display = 'none';
    }
  }
  if (!mark) {
    frm.inpAllLines.checked = false;
  }
  return true;
}

function updateAll(drivenByGrid) {
  var frm = document.frmMain;
  var chk = frm.inpScheduledPaymentDetailId;
  var recordAmount, i;

  frm.inpExpectedPayment.value = applyFormat('0');
  if (isGLItemEnabled) {
    frm.inpExpectedPayment.value = frm.inpGLSumAmount.value || applyFormat('0');
  }
  if (!chk) {
    return;
  } else if (!chk.length) {
    if (!chk.checked) {
      recordAmount = frm.elements["inpRecordAmt" + chk.value].value;
      frm.inpExpectedPayment.value = add(frm.inpExpectedPayment.value, recordAmount);
    }
    updateData(chk.value, chk.checked, drivenByGrid);
  } else {
    var total = chk.length;
    for (i = 0; i < total; i++) {
      if (!chk[i].checked) {
        recordAmount = frm.elements["inpRecordAmt" + chk[i].value].value;
        frm.inpExpectedPayment.value = add(frm.inpExpectedPayment.value, recordAmount);
      }
      updateData(chk[i].value, chk[i].checked, drivenByGrid);
    }
  }
  return true;
}

/**
 *
 * @param allowCreditGeneration true if it is allowed to not select any pending payment if actualPayment amount is not
 *        zero.
 * @return true if validations are fine.
 */

function validateSelectedPendingPayments(allowNotSelectingPendingPayment, action) {
  if (allowNotSelectingPendingPayment === undefined) {
    allowNotSelectingPendingPayment = false;
  }
  // If no credit usage is allowed we are forced to select at least one pending payment.
  allowNotSelectingPendingPayment = isCreditAllowed && allowNotSelectingPendingPayment;
  var actualPayment = document.frmMain.inpActualPayment.value;
  var expectedPayment = document.frmMain.inpExpectedPayment.value,
      i;
  if (document.frmMain.inpUseCredit.checked) {
/*if ( compare(expectedPayment, '<=', actualPayment) ) {
      setWindowElementFocus(document.frmMain.inpUseCredit);
      showJSMessage('APRM_JSCANNOTUSECREDIT');
      return false;
    }*/
    actualPayment = add(actualPayment, document.frmMain.inpCredit.value);
  }
  if (action === null && compare(frm.inpDifference.value, '!=', 0)) {
    showJSMessage('APRM_JSDIFFERENCEWITHOUTACTION');
    return false;
  }
  var selectedTotal = document.frmMain.inpTotal.value;
  if (compareWithSign(selectedTotal, '>', actualPayment)) {
    setWindowElementFocus(document.frmMain.inpActualPayment);
    showJSMessage('APRM_JSMOREAMOUTALLOCATED');
    return false;
  }
  var chk = frm.inpScheduledPaymentDetailId;
  if (!chk) {
    return true;
  } else if (!chk.length) {
    if (chk.checked) {
      if (!validateSelectedAmounts(chk.value, compare(selectedTotal, '<', actualPayment), action)) {
        return false;
      }
    } else {
      if (!(typeof OB !== 'undefined' && OB.APRM && OB.APRM.HasGLItems) && (!allowNotSelectingPendingPayment || compare(actualPayment, '==', "0"))) {
        showJSMessage('APRM_JSNOTLINESELECTED');
        return false;
      }
    }
  } else {
    var total = chk.length;
    var isAnyChecked = false;
    for (i = 0; i < total; i++) {
      if (chk[i].checked) {
        isAnyChecked = true;
        if (!validateSelectedAmounts(chk[i].value, compare(selectedTotal, '<', actualPayment), action)) {
          return false;
        }
      }
    }
    if (!(typeof OB !== 'undefined' && OB.APRM && OB.APRM.HasGLItems) && !isAnyChecked && (!allowNotSelectingPendingPayment || compare(actualPayment, '==', "0"))) {
      showJSMessage('APRM_JSNOTLINESELECTED');
      return false;
    }
  }
  return true;
}

/**
 * Creates a select html object with the option string list
 * @param object
 *     select html object.
 * @param innerHTML
 *     The string with the options. Example '<option value="id1">fist<option>'
 */

function createCombo(object, innerHTML) {
  object.innerHTML = "";
  var selTemp = document.createElement("temp");
  var opt, i, j;
  selTemp.id = "temp1";
  document.body.appendChild(selTemp);
  selTemp = document.getElementById("temp1");
  selTemp.style.display = "none";
  innerHTML = innerHTML.replace(/<option/g, "<span").replace(/<\/option/g, "</span");
  selTemp.innerHTML = innerHTML;

  for (i = 0; i < selTemp.childNodes.length; i++) {
    var spantemp = selTemp.childNodes[i];

    if (spantemp.tagName) {
      opt = document.createElement("option");
      if (document.all) { //IE
        object.add(opt);
      } else {
        object.appendChild(opt);
      }

      //getting attributes
      for (j = 0; j < spantemp.attributes.length; j++) {
        var attrName = spantemp.attributes[j].nodeName;
        var attrVal = spantemp.attributes[j].nodeValue;
        if (attrVal) {
          try {
            opt.setAttribute(attrName, attrVal);
            opt.setAttributeNode(spantemp.attributes[j].cloneNode(true));
          } catch (e) {}
        }
      }
      //value and text
      opt.value = spantemp.getAttribute("value");
      opt.text = spantemp.innerHTML;
      //IE
      opt.selected = spantemp.getAttribute('selected');
      opt.className = spantemp.className;
    }
  }
  document.body.removeChild(selTemp);
  selTemp = null;
}

/**
 * Helper function to reload the opener window dynamic grid.
 * @return
 */

function reloadParentGrid() {
  var f, dad, layoutMDI, popup;
  try {
    f = getFrame('LayoutMDI');
    popup = f && f.OB && f.OB.Layout.ClassicOBCompatibility.Popup;
    layoutMDI = popup && popup.getPopup('process') && popup.getPopup('process').getIframeHtmlObj() && popup.getPopup('process').getIframeHtmlObj().contentWindow && popup.getPopup('process').getIframeHtmlObj().contentWindow.frames[0];
    dad = layoutMDI || top.opener;
    if (dad) {
      if (typeof dad.loadGrid === "function" || typeof dad.loadGrid === "object") {
        dad.loadGrid();
      } else if (typeof dad.updateGridDataAfterFilter === "function" || typeof dad.updateGridDataAfterFilter === "object") {
        dad.updateGridDataAfterFilter();
      }
    } else if (f && f.OB.MainView.TabSet.getSelectedTab().pane.view) {
      var theView = f.OB.MainView.TabSet.getSelectedTab().pane.view;
      theView.refresh(function () {
        theView.getTabMessage();
        theView.toolBar.refreshCustomButtons();
      });
    }
  } catch (e) {
    // not possible to reload parent grid
  }
}

/**
 * Helper function to turn a JSON string representation into an object.
 * @param jsonString
 */

function decodeJSON(jsonString) {
  try {
    return eval('(' + jsonString + ')'); // do the eval
  } catch (e) {
    throw new SyntaxError("Invalid JSON string: " + e.message + " parsing: " + jsonString);
  }
}