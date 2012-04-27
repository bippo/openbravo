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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
module('org.openbravo.client.application');

test('OB.Utilities.Date.* functions', function () {
  expect(4);

  var outputText = '';

  ok((function () {
    var expectedCenturyReference = 50;
    var successText = 'OB.Utilities.Date.centuryReference has a valid value';
    var failureText = 'OB.Utilities.Date.centuryReference ';
    var success = true;

    if (OB.Utilities.Date.centuryReference !== expectedCenturyReference) {
      success = false;
    }

    if (success) {
      outputText = successText;
    } else {
      failureText = failureText + ' has a wrong value: ' + OB.Utilities.Date.centuryReference + ' (expected: ' + expectedCenturyReference + '). This could make the following tests fail.';
      outputText = failureText;
    }
    return success;
  }()), outputText);

  ok((function () {
    var i;
    var successText = 'OB.Utilities.Date.normalizeDisplayFormat works properly';
    var failureText = 'OB.Utilities.Date.normalizeDisplayFormat failed while eval';
    var success = true;
    var normalizedDisplayFormat;
    var list = [
      ['DD-MM-YYYY', '%d-%m-%Y'],
      ['DD-MM-YY', '%d-%m-%y'],
      ['dd-mm-yyyy', '%d-%m-%Y'],
      ['dd-mm-yy', '%d-%m-%y'],
      ['%D-%M-%Y', '%d-%m-%Y'],
      ['%D-%M-%y', '%d-%m-%y'],
      ['%d-%m-%Y', '%d-%m-%Y'],
      ['%d-%m-%y', '%d-%m-%y'],
      ['%d-%m-%Y hh:mi:ss', '%d-%m-%Y %H:%M:%S'],
      ['%d-%m-%Y HH:MI:SS', '%d-%m-%Y %H:%M:%S'],
      ['%d-%m-%Y HH24:mi:ss', '%d-%m-%Y %H:%M:%S'],
      ['%d-%m-%Y HH24:MI:SS', '%d-%m-%Y %H:%M:%S'],
      ['%d-%m-%Y HH:MM:SS', '%d-%m-%Y %H:%M:%S'],
      ['%d-%m-%Y HH24:MM:SS', '%d-%m-%Y %H:%M:%S'],
      ['%d-%m-%Y %H:%M:%S', '%d-%m-%Y %H:%M:%S'],
      ['%d-%m-%Y %H.%M.%S', '%d-%m-%Y %H.%M.%S']
    ];
    for (i = 0; i < list.length; i++) {
      normalizedDisplayFormat = OB.Utilities.Date.normalizeDisplayFormat(list[i][0]);
      if (normalizedDisplayFormat !== list[i][1]) {
        success = false;
        failureText = failureText + ' normalizeDisplayFormat(\'' + list[i][0] + '\') === \'' + list[i][1] + '\' (returned: ' + normalizedDisplayFormat + ') &';
      }
    }
    if (success) {
      outputText = successText;
    } else {
      failureText = failureText.substring(0, failureText.length - 2);
      outputText = failureText;
    }
    return success;
  }()), outputText);

  ok((function () {
    var i;
    var successText = 'OB.Utilities.Date.OBToJS works properly';
    var failureText = 'OB.Utilities.Date.OBToJS failed while eval';
    var success = true;
    var OBToJS;
    var list = [
      ['BadDefinedOBDate', '%d-%m-%Y', 'null'],
      ['31/12/2010', '%d/%m/%Y', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['31:12:2010', '%d:%m:%Y', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['31.12.2010', '%d.%m.%Y', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['31-12-2010', '%d-%m-%Y', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['12-31-2010', '%m-%d-%Y', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['2010-31-12', '%Y-%d-%m', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['2010-12-31', '%Y-%m-%d', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['01-01-0001', '%d-%m-%Y', (function () {
        var date = new Date(1, 0, 1, 0, 0, 0, 0);
        date.setFullYear('1');
        return date;
      }())],
      ['31-12-10', '%d-%m-%y', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['12-31-10', '%m-%d-%y', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['10-31-12', '%y-%d-%m', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['10-12-31', '%y-%m-%d', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['01-01-00', '%d-%m-%y', new Date(2000, 0, 1, 0, 0, 0, 0)],
      ['01-01-01', '%d-%m-%y', new Date(2001, 0, 1, 0, 0, 0, 0)],
      ['01-01-49', '%d-%m-%y', new Date(2049, 0, 1, 0, 0, 0, 0)],
      ['01-01-50', '%d-%m-%y', new Date(1950, 0, 1, 0, 0, 0, 0)],
      ['01-01-99', '%d-%m-%y', new Date(1999, 0, 1, 0, 0, 0, 0)],
      ['31-12-2010', '%d-%m-%Y', new Date(2010, 11, 31, 0, 0, 0, 0)],
      ['31-12-2010 23:59', '%d-%m-%Y %H:%M', new Date(2010, 11, 31, 23, 59, 0, 0)],
      ['31-12-2010 23:59:58', '%d-%m-%Y %H:%M:%S', new Date(2010, 11, 31, 23, 59, 58, 0)]
    ];
    for (i = 0; i < list.length; i++) {
      OBToJS = OB.Utilities.Date.OBToJS(list[i][0], list[i][1]);
      if (OBToJS === null) {
        OBToJS = 'null';
      }
      if (OBToJS.toString() !== list[i][2].toString()) {
        success = false;
        failureText = failureText + ' OBToJS(\'' + list[i][0] + '\', \'' + list[i][1] + '\') === \'' + list[i][2].toString() + '\' (returned: ' + OBToJS + ') &';
      }
    }
    if (success) {
      outputText = successText;
    } else {
      failureText = failureText.substring(0, failureText.length - 2);
      outputText = failureText;
    }
    return success;
  }()), outputText);


  ok((function () {
    var i;
    var successText = 'OB.Utilities.Date.JSToOB works properly';
    var failureText = 'OB.Utilities.Date.JSToOB failed while eval';
    var success = true;
    var JSToOB;
    var list = [
      ['BadDefinedJSDate', '%d-%m-%Y', 'null'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%d/%m/%Y', '31/12/2010'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%d:%m:%Y', '31:12:2010'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%d.%m.%Y', '31.12.2010'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%d-%m-%Y', '31-12-2010'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%m-%d-%Y', '12-31-2010'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%Y-%d-%m', '2010-31-12'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%Y-%m-%d', '2010-12-31'],
      [(function () {
        var date = new Date(1, 0, 1, 0, 0, 0, 0);
        date.setFullYear('1');
        return date;
      }()), '%d-%m-%Y', '01-01-0001'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%d-%m-%y', '31-12-10'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%m-%d-%y', '12-31-10'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%y-%d-%m', '10-31-12'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%y-%m-%d', '10-12-31'],
      [new Date(2000, 0, 1, 0, 0, 0, 0), '%d-%m-%y', '01-01-00'],
      [new Date(2001, 0, 1, 0, 0, 0, 0), '%d-%m-%y', '01-01-01'],
      [new Date(2049, 0, 1, 0, 0, 0, 0), '%d-%m-%y', '01-01-49'],
      [new Date(1950, 0, 1, 0, 0, 0, 0), '%d-%m-%y', '01-01-50'],
      [new Date(1999, 0, 1, 0, 0, 0, 0), '%d-%m-%y', '01-01-99'],
      [new Date(1949, 0, 1, 0, 0, 0, 0), '%d-%m-%y', 'null'],
      [new Date(2010, 11, 31, 0, 0, 0, 0), '%d-%m-%Y', '31-12-2010'],
      [new Date(2010, 11, 31, 23, 59, 0, 0), '%d-%m-%Y %H:%M', '31-12-2010 23:59'],
      [new Date(2010, 11, 31, 23, 59, 58, 0), '%d-%m-%Y %H:%M:%S', '31-12-2010 23:59:58']
    ];
    for (i = 0; i < list.length; i++) {
      JSToOB = OB.Utilities.Date.JSToOB(list[i][0], list[i][1]);
      if (JSToOB === null) {
        JSToOB = 'null';
      }
      if (JSToOB.toString() !== list[i][2].toString()) {
        success = false;
        failureText = failureText + ' JSToOB(\'' + list[i][0] + '\', \'' + list[i][1] + '\') === \'' + list[i][2].toString() + '\' (returned: ' + JSToOB + ') &';
      }
    }
    if (success) {
      outputText = successText;
    } else {
      failureText = failureText.substring(0, failureText.length - 2);
      outputText = failureText;
    }
    return success;
  }()), outputText);
});