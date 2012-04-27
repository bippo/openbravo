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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// = Openbravo Date Utilities =
// Defines utility methods related to handling date, incl. formatting.
OB.Utilities.Date = {};

// ** {{{ OB.Utilities.Date.centuryReference }}} **
// For a two-digit year display format, it establishes where is the frontier
// between the 20th and the 21st century
// The range is taken between 1900+centuryReference and 2100-centuryReference-1
OB.Utilities.Date.centuryReference = 50; // Notice that change this value
// implies that also the QUnit test
// case should be changed
// ** {{{ OB.Utilities.Date.normalizeDisplayFormat }}} **
// Repairs the displayFormat definition (passed in as a parameter) to a value
// expected by the rest of the system. For example mm is replaced by MM,
// dd is replacecd by DD, YYYY to %Y.
//
// Parameters:
// * {{{displayFormat}}}: the string displayFormat definition to repair.
OB.Utilities.Date.normalizeDisplayFormat = function (displayFormat) {
  var newFormat = '';
  displayFormat = displayFormat.replace('mm', 'MM').replace('dd', 'DD').replace('yyyy', 'YYYY').replace('yy', 'YY');
  displayFormat = displayFormat.replace('%D', '%d').replace('%M', '%m');
  if (displayFormat !== null && displayFormat !== '') {
    newFormat = displayFormat;
    newFormat = newFormat.replace('YYYY', '%Y');
    newFormat = newFormat.replace('YY', '%y');
    newFormat = newFormat.replace('MM', '%m');
    newFormat = newFormat.replace('DD', '%d');
    newFormat = newFormat.substring(0, 8);
  }
  displayFormat = displayFormat.replace('hh', 'HH').replace('HH24', 'HH').replace('mi', 'MI').replace('ss', 'SS');
  displayFormat = displayFormat.replace('%H', 'HH').replace('HH:%m', 'HH:MI').replace('HH.%m', 'HH.MI').replace('%S', 'SS');
  displayFormat = displayFormat.replace('HH:mm', 'HH:MI').replace('HH.mm', 'HH.MI');
  displayFormat = displayFormat.replace('HH:MM', 'HH:MI').replace('HH.MM', 'HH.MI');
  if (displayFormat.indexOf(' HH:MI:SS') !== -1) {
    newFormat += ' %H:%M:%S';
  } else if (displayFormat.indexOf(' HH:MI') !== -1) {
    newFormat += ' %H:%M';
  } else if (displayFormat.indexOf(' HH.MI.SS') !== -1) {
    newFormat += ' %H.%M.%S';
  } else if (displayFormat.indexOf(' HH.MI') !== -1) {
    newFormat += ' %H.%M';
  }
  return newFormat;
};

// ** {{{ OB.Utilities.Date.OBToJS }}} **
//
// Converts a String to a Date object.
//
// Parameters:
// * {{{OBDate}}}: the date string to convert
// * {{{dateFormat}}}: the dateFormat pattern to use
// Return:
// * a Date object or null if conversion was not possible.
OB.Utilities.Date.OBToJS = function (OBDate, dateFormat) {
  if (!OBDate) {
    return null;
  }

  // if already a date then return true
  var isADate = Object.prototype.toString.call(OBDate) === '[object Date]';
  if (isADate) {
    return OBDate;
  }

  dateFormat = OB.Utilities.Date.normalizeDisplayFormat(dateFormat);
  var dateSeparator = dateFormat.substring(2, 3);
  var timeSeparator = dateFormat.substring(11, 12);
  var isFullYear = (dateFormat.indexOf('%Y') !== -1);

  if ((isFullYear ? OBDate.length - 2 : OBDate.length) !== dateFormat.length) {
    return null;
  }
  if (isFullYear) {
    dateFormat = dateFormat.replace('%Y', '%YYY');
  }

  if (dateFormat.indexOf('-') !== -1 && OBDate.indexOf('-') === -1) {
    return null;
  } else if (dateFormat.indexOf('/') !== -1 && OBDate.indexOf('/') === -1) {
    return null;
  } else if (dateFormat.indexOf(':') !== -1 && OBDate.indexOf(':') === -1) {
    return null;
  } else if (dateFormat.indexOf('.') !== -1 && OBDate.indexOf('.') === -1) {
    return null;
  }

  var year = dateFormat.indexOf('%y') !== -1 ? OBDate.substring(dateFormat.indexOf('%y'), dateFormat.indexOf('%y') + 2) : 0;
  var fullYear = dateFormat.indexOf('%Y') !== -1 ? OBDate.substring(dateFormat.indexOf('%Y'), dateFormat.indexOf('%Y') + 4) : 0;
  var month = dateFormat.indexOf('%m') !== -1 ? OBDate.substring(dateFormat.indexOf('%m'), dateFormat.indexOf('%m') + 2) : 0;
  var day = dateFormat.indexOf('%d') !== -1 ? OBDate.substring(dateFormat.indexOf('%d'), dateFormat.indexOf('%d') + 2) : 0;
  var hours = dateFormat.indexOf('%H') !== -1 ? OBDate.substring(dateFormat.indexOf('%H'), dateFormat.indexOf('%H') + 2) : 0;
  var minutes = dateFormat.indexOf('%M') !== -1 ? OBDate.substring(dateFormat.indexOf('%M'), dateFormat.indexOf('%M') + 2) : 0;
  var seconds = dateFormat.indexOf('%S') !== -1 ? OBDate.substring(dateFormat.indexOf('%S'), dateFormat.indexOf('%S') + 2) : 0;

  month = parseInt(month, 10);
  day = parseInt(day, 10);
  hours = parseInt(hours, 10);
  minutes = parseInt(minutes, 10);
  seconds = parseInt(seconds, 10);

  if (day < 1 || day > 31 || month < 1 || month > 12 || year > 99 || fullYear > 9999) {
    return null;
  }

  if (hours > 23 || minutes > 59 || seconds > 59) {
    return null;
  }

  // alert('year: ' + year + '\n' + 'fullYear: ' + fullYear + '\n' + 'month: ' +
  // month + '\n' + 'day: ' + day + '\n' + 'hours: ' + hours + '\n' + 'minutes:
  // ' + minutes + '\n' + 'seconds: ' + seconds);
  // var JSDate = isc.Date.create(); /**It doesn't work in IE**/
  var JSDate = new Date();
  var centuryReference = OB.Utilities.Date.centuryReference;
  if (!isFullYear) {
    if (parseInt(year, 10) < centuryReference) {
      fullYear = '20' + year;
    } else {
      fullYear = '19' + year;
    }
  }

  fullYear = parseInt(fullYear, 10);
  JSDate.setFullYear(fullYear, month - 1, day);
  JSDate.setHours(hours);
  JSDate.setMinutes(minutes);
  JSDate.setSeconds(seconds);
  JSDate.setMilliseconds(0);
  if (JSDate.toString() === 'Invalid Date' || JSDate.toString() === 'NaN') {
    return null;
  } else {
    return JSDate;
  }
};

// ** {{{ OB.Utilities.Date.JSToOB }}} **
//
// Converts a Date to a String
//
// Parameters:
// * {{{JSDate}}}: the javascript Date object
// * {{{dateFormat}}}: the dateFormat pattern to use
// Return:
// * a String or null if the JSDate is not a date.
OB.Utilities.Date.JSToOB = function (JSDate, dateFormat) {
  dateFormat = OB.Utilities.Date.normalizeDisplayFormat(dateFormat);

  var isADate = Object.prototype.toString.call(JSDate) === '[object Date]';
  if (!isADate) {
    return null;
  }

  var year = JSDate.getYear().toString();
  var fullYear = JSDate.getFullYear().toString();
  var month = (JSDate.getMonth() + 1).toString();
  var day = JSDate.getDate().toString();
  var hours = JSDate.getHours().toString();
  var minutes = JSDate.getMinutes().toString();
  var seconds = JSDate.getSeconds().toString();

  var centuryReference = OB.Utilities.Date.centuryReference;
  if (dateFormat.indexOf('%y') !== -1) {
    if (parseInt(fullYear, 10) >= 1900 + centuryReference && parseInt(fullYear, 10) < 2100 - centuryReference) {
      if (parseInt(year, 10) >= 100) {
        year = parseInt(year, 10) - 100;
        year = year.toString();
      }
    } else {
      return null;
    }
  }

  while (year.length < 2) {
    year = '0' + year;
  }
  while (fullYear.length < 4) {
    fullYear = '0' + fullYear;
  }
  while (month.length < 2) {
    month = '0' + month;
  }
  while (day.length < 2) {
    day = '0' + day;
  }
  while (hours.length < 2) {
    hours = '0' + hours;
  }
  while (minutes.length < 2) {
    minutes = '0' + minutes;
  }
  while (seconds.length < 2) {
    seconds = '0' + seconds;
  }
  var OBDate = dateFormat;
  OBDate = OBDate.replace('%y', year);
  OBDate = OBDate.replace('%Y', fullYear);
  OBDate = OBDate.replace('%m', month);
  OBDate = OBDate.replace('%d', day);
  OBDate = OBDate.replace('%H', hours);
  OBDate = OBDate.replace('%M', minutes);
  OBDate = OBDate.replace('%S', seconds);

  return OBDate;
};