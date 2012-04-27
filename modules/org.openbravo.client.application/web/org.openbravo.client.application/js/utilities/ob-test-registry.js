/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License+
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
// = Test Registry =
//
// Global registry to facilitate testing. Components can register themselves in the 
// TestRegistry using a unique name, selenium tests can then easily retrieve components from the test registry
// using the unique name.
//
(function (OB, isc) {

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }

  var fullUrl = parent.document.URL;
  var queryString = fullUrl.substring(fullUrl.indexOf('?') + 1, fullUrl.length);
  var isTestEnvironment = queryString.indexOf('test=true') !== -1;
  if (isTestEnvironment || OB.Application.testEnvironment) {
    isc.Log.logDebug('Test Environment, registering test components enabled', 'OB');
  } else {
    isc.Log.logDebug('No Test Environment, registering test components disabled', 'OB');
  }

  function TestRegistry() {}

  TestRegistry.prototype = {

    registry: {},

    register: function (key, object) {
      if (isTestEnvironment || OB.Application.testEnvironment) {
        isc.Log.logDebug('Registering ' + key + ' in test registry ', 'OB');
        this.registry[key] = object;
      }
    }
  };

  OB.TestRegistry = new TestRegistry();
}(OB, isc));