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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// = Openbravo Number Utilities =
// Defines utility methods related to handling numbers on the client, for 
// example formatting. 
OB.Utilities.Number = {};

// ** {{{ OB.Utilities.Number.roundJSNumber }}} **
//
// Function that rounds a JS number to a given decimal number
//
// Parameters:
// * {{{num}}}: the JS number
// * {{{dec}}}: the JS number of decimals
// Return:
// * The rounded JS number
OB.Utilities.Number.roundJSNumber = function (num, dec) {
  var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
  return result;
};

// ** {{{ OB.Utilities.Number.OBMaskedToOBPlain }}} **
//
// Function that returns a plain OB number just with the decimal Separator
//
// Parameters:
// * {{{number}}}: the formatted OB number
// * {{{decSeparator}}}: the decimal separator of the OB number
// * {{{groupSeparator}}}: the group separator of the OB number
// Return:
// * The plain OB number
OB.Utilities.Number.OBMaskedToOBPlain = function (number, decSeparator, groupSeparator) {
  number = number.toString();
  var plainNumber = number;

  // Remove group separators
  if (groupSeparator) {
    var groupRegExp = new RegExp('\\' + groupSeparator, 'g');
    plainNumber = plainNumber.replace(groupRegExp, '');
  }

  // Catch sign
  var numberSign = '';
  if (plainNumber.substring(0, 1) === '+') {
    numberSign = '';
    plainNumber = plainNumber.substring(1, number.length);
  } else if (plainNumber.substring(0, 1) === '-') {
    numberSign = '-';
    plainNumber = plainNumber.substring(1, number.length);
  }

  // Remove ending decimal '0'
  if (plainNumber.indexOf(decSeparator) !== -1) {
    while (plainNumber.substring(plainNumber.length - 1, plainNumber.length) === '0') {
      plainNumber = plainNumber.substring(0, plainNumber.length - 1);
    }
  }

  // Remove starting integer '0'
  while (plainNumber.substring(0, 1) === '0' && plainNumber.substring(1, 2) !== decSeparator && plainNumber.length > 1) {
    plainNumber = plainNumber.substring(1, plainNumber.length);
  }

  // Remove decimal separator if is the last character
  if (plainNumber.substring(plainNumber.length - 1, plainNumber.length) === decSeparator) {
    plainNumber = plainNumber.substring(0, plainNumber.length - 1);
  }

  // Re-set sign
  if (plainNumber !== '0') {
    plainNumber = numberSign + plainNumber;
  }

  // Return plain number
  return plainNumber;
};

// ** {{{ OB.Utilities.Number.OBPlainToOBMasked }}} **
//
// Function that transform a OB plain number into a OB formatted one (by
// applying a mask).
//
// Parameters:
// * {{{number}}}: The OB plain number
// * {{{maskNumeric}}}: The numeric mask of the OB number
// * {{{decSeparator}}}: The decimal separator of the OB number
// * {{{groupSeparator}}}: The group separator of the OB number
// * {{{groupInterval}}}: The group interval of the OB number
// Return:
// * The OB formatted number.
OB.Utilities.Number.OBPlainToOBMasked = function (number, maskNumeric, decSeparator, groupSeparator, groupInterval) {
  if (number === '' || number === null || number === undefined) {
    return number;
  }

  // Management of the mask
  if (maskNumeric.indexOf('+') === 0 || maskNumeric.indexOf('-') === 0) {
    maskNumeric = maskNumeric.substring(1, maskNumeric.length);
  }
  if (groupSeparator && maskNumeric.indexOf(groupSeparator) !== -1 && maskNumeric.indexOf(decSeparator) !== -1 && maskNumeric.indexOf(groupSeparator) > maskNumeric.indexOf(decSeparator)) {
    var fixRegExp = new RegExp('\\' + groupSeparator, 'g');
    maskNumeric = maskNumeric.replace(fixRegExp, '');
  }
  var maskLength = maskNumeric.length;
  var decMaskPosition = maskNumeric.indexOf(decSeparator);
  if (decMaskPosition === -1) {
    decMaskPosition = maskLength;
  }
  var intMask = maskNumeric.substring(0, decMaskPosition);
  var decMask = maskNumeric.substring(decMaskPosition + 1, maskLength);

  if ((groupSeparator && decMask.indexOf(groupSeparator) !== -1) || decMask.indexOf(decSeparator) !== -1) {
    if (groupSeparator) {
      var fixRegExp_1 = new RegExp('\\' + groupSeparator, 'g');
      decMask = decMask.replace(fixRegExp_1, '');
    }
    var fixRegExp_2 = new RegExp('\\' + decSeparator, 'g');
    decMask = decMask.replace(fixRegExp_2, '');
  }

  // Management of the number
  number = number.toString();
  number = OB.Utilities.Number.OBMaskedToOBPlain(number, decSeparator, groupSeparator);
  var numberSign = '';
  if (number.substring(0, 1) === '+') {
    numberSign = '';
    number = number.substring(1, number.length);
  } else if (number.substring(0, 1) === '-') {
    numberSign = '-';
    number = number.substring(1, number.length);
  }

  // //Splitting the number
  var formattedNumber = '';
  var numberLength = number.length;
  var decPosition = number.indexOf(decSeparator);
  if (decPosition === -1) {
    decPosition = numberLength;
  }
  var intNumber = number.substring(0, decPosition);
  var decNumber = number.substring(decPosition + 1, numberLength);

  // //Management of the decimal part
  if (decNumber.length > decMask.length) {
    decNumber = '0.' + decNumber;
    decNumber = OB.Utilities.Number.roundJSNumber(decNumber, decMask.length);
    decNumber = decNumber.toString();
    if (decNumber.substring(0, 1) === '1') {
      intNumber = parseFloat(intNumber);
      intNumber = intNumber + 1;
      intNumber = intNumber.toString();
    }
    decNumber = decNumber.substring(2, decNumber.length);
  }

  if (decNumber.length < decMask.length) {
    var decNumber_temp = '',
        decMaskLength = decMask.length,
        i;
    for (i = 0; i < decMaskLength; i++) {
      if (decMask.substring(i, i + 1) === '#') {
        if (decNumber.substring(i, i + 1) !== '') {
          decNumber_temp = decNumber_temp + decNumber.substring(i, i + 1);
        }
      } else if (decMask.substring(i, i + 1) === '0') {
        if (decNumber.substring(i, i + 1) !== '') {
          decNumber_temp = decNumber_temp + decNumber.substring(i, i + 1);
        } else {
          decNumber_temp = decNumber_temp + '0';
        }
      }
    }
    decNumber = decNumber_temp;
  }

  // Management of the integer part
  var isGroup = false;

  if (groupSeparator) {
    if (intMask.indexOf(groupSeparator) !== -1) {
      isGroup = true;
    }

    var groupRegExp = new RegExp('\\' + groupSeparator, 'g');
    intMask = intMask.replace(groupRegExp, '');
  }

  var intNumber_temp;
  if (intNumber.length < intMask.length) {
    intNumber_temp = '';
    var diff = intMask.length - intNumber.length,
        j;
    for (j = intMask.length; j > 0; j--) {
      if (intMask.substring(j - 1, j) === '#') {
        if (intNumber.substring(j - 1 - diff, j - diff) !== '') {
          intNumber_temp = intNumber.substring(j - 1 - diff, j - diff) + intNumber_temp;
        }
      } else if (intMask.substring(j - 1, j) === '0') {
        if (intNumber.substring(j - 1 - diff, j - diff) !== '') {
          intNumber_temp = intNumber.substring(j - 1 - diff, j - diff) + intNumber_temp;
        } else {
          intNumber_temp = '0' + intNumber_temp;
        }
      }
    }
    intNumber = intNumber_temp;
  }

  if (isGroup === true) {
    intNumber_temp = '';
    var groupCounter = 0,
        k;
    for (k = intNumber.length; k > 0; k--) {
      intNumber_temp = intNumber.substring(k - 1, k) + intNumber_temp;
      groupCounter++;
      if (groupCounter.toString() === groupInterval.toString() && k !== 1) {
        groupCounter = 0;
        intNumber_temp = groupSeparator + intNumber_temp;
      }
    }
    intNumber = intNumber_temp;
  }

  // Building the final number
  if (intNumber === '' && decNumber !== '') {
    intNumber = '0';
  }

  formattedNumber = numberSign + intNumber;
  if (decNumber !== '') {
    formattedNumber += decSeparator + decNumber;
  }
  return formattedNumber;
};

// ** {{{ OB.Utilities.Number.OBMaskedToJS }}} **
//
// Function that returns a JS number just with the decimal separator which
// always is '.'. It is used for math operations
//
// Parameters:
// * {{{number}}}: The OB formatted (or plain) number
// * {{{decSeparator}}}: The decimal separator of the OB number
// * {{{groupSeparator}}}: The group separator of the OB number
// Return:
// * The JS number.
OB.Utilities.Number.OBMaskedToJS = function (numberStr, decSeparator, groupSeparator) {
  if (!numberStr || numberStr.trim() === '') {
    return null;
  }
  var calcNumber = OB.Utilities.Number.OBMaskedToOBPlain(numberStr, decSeparator, groupSeparator);
  calcNumber = calcNumber.replace(decSeparator, '.');
  var numberResult = parseFloat(calcNumber);
  if (isNaN(numberResult)) {
    return numberStr;
  }
  return numberResult;
};

// ** {{{ OB.Utilities.Number.JSToOBMasked }}} **
//
// Function that returns a OB formatted number given as input a JS number just
// with the decimal separator which always is '.'
//
// Parameters:
// * {{{number}}}: The JS number
// * {{{maskNumeric}}}: The numeric mask of the OB number
// * {{{decSeparator}}}: The decimal separator of the OB number
// * {{{groupSeparator}}}: The group separator of the OB number
// * {{{groupInterval}}}: The group interval of the OB number
// Return:
// * The OB formatted number.
OB.Utilities.Number.JSToOBMasked = function (number, maskNumeric, decSeparator, groupSeparator, groupInterval) {
  if (!isc.isA.Number(number)) {
    return number;
  }
  var formattedNumber = number;
  formattedNumber = formattedNumber.toString();
  formattedNumber = formattedNumber.replace('.', decSeparator);
  formattedNumber = OB.Utilities.Number.OBPlainToOBMasked(formattedNumber, maskNumeric, decSeparator, groupSeparator, groupInterval);
  return formattedNumber;
};

OB.Utilities.Number.IsValidValueString = function (type, numberStr) {
  var maskNumeric = type.maskNumeric;
  // note 0 is also okay to return true
  if (!numberStr) {
    return true;
  }

  var bolNegative = true;
  if (maskNumeric.indexOf('+') === 0) {
    bolNegative = false;
    maskNumeric = maskNumeric.substring(1, maskNumeric.length);
  }

  var bolDecimal = true;
  if (maskNumeric.indexOf(type.decSeparator) === -1) {
    bolDecimal = false;
  }
  var checkPattern = '';
  checkPattern += '^';
  if (bolNegative) {
    checkPattern += '([+]|[-])?';
  }
  checkPattern += '(\\d+)?((\\' + type.groupSeparator + '\\d{' + OB.Format.defaultGroupingSize + '})?)+';
  if (bolDecimal) {
    checkPattern += '(\\' + type.decSeparator + '\\d+)?';
  }
  checkPattern += '$';
  var checkRegExp = new RegExp(checkPattern);
  if (numberStr.match(checkRegExp) && numberStr.substring(0, 1) !== type.groupSeparator) {
    return true;
  }
  return false;
};