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
module('org.openbravo.client.application.navigationbarcomponents');

test('Basic requirements', function () {
  expect(1);
  ok(OB.RecentUtilities, 'recent utilities defined');
});

test('Test user info data read', function () {
  stop(1000);

  expect(16);

  var callback;
  callback = function (rpcResponse, data, rpcRequest) {
    ok(data.language, 'Language present');
    ok(data.language.value, 'Language value present');
    ok(data.language.valueMap, 'Language valueMap present');
    ok(data.language.value, 'Language value present');

    ok(data.initialValues.role, 'Initial role value set');
    ok(data.initialValues.client, 'Initial client value set');
    ok(data.initialValues.organization, 'Initial organization value set');
    ok(data.initialValues.language, 'Initial language value set');

    ok(data.role, 'Role set');
    ok(data.role.value, 'Role value set');
    ok(data.role.valueMap, 'Role valueMap set');
    ok(data.role.roles, 'Role info set');
    ok(data.role.roles.length > 0, 'More than one role present');
    ok(data.role.roles[0].id, 'Role id set');
    ok(data.role.roles[0].organizationValueMap, 'Role org value map set');
    ok(data.role.roles[0].warehouseValueMap, 'Role wh value map set');

    start();
  };
  var action = 'org.openbravo.client.application.navigationbarcomponents.UserInfoWidgetActionHandler';
  OB.RemoteCallManager.call(action, {}, {
    'command': 'data'
  }, callback);
});