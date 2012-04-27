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

test('Property Store Exists', function () {
  expect(2);
  ok(OB.PropertyStore, 'PropertyStore is present');
  ok(!OB.PropertyStore.get('abc'), 'Test property is not present (okay)');
});

test('Set/Get Property', function () {

  expect(2);

  var propName = 'CCU';
  var testValue = 'testValue';
  var propValue = OB.PropertyStore.get(propName);
  ok(!propValue, 'CCU is not present, value is ' + propValue);

  OB.PropertyStore.set(propName, testValue);
  propValue = OB.PropertyStore.get(propName);
  same(propValue, testValue, 'Equal values');
  // clear the test property
  // with a short delay to make sure that the previous set does not interfere
  // on the server
  isc.Timer.setTimeout(function () {
    OB.PropertyStore.set(propName, null);
  }, 1000);

});