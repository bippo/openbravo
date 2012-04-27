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
module('org.openbravo.client.myob');

var d = {
  'eventType': '',
  'context': {
    'adminMode': false
  }
};

function checkMissingDbInstanceId(w) {
  var i, widgets = w || OB.MyOB.widgets;
  for (i = 0; i < widgets.length; i++) {
    if (!widgets[i].dbInstanceId) { // empty string is 'falsy'
      return true;
    }
  }
  return false;
}

asyncTest('Create Workspace', function () {
  OB.Layout.ViewManager.createAddStartTab();
});

asyncTest('Add widget', function () {

  var post = isc.addProperties({}, d, {
    'eventType': 'WIDGET_ADDED',
    'widgets': OB.MyOB.widgets
  }),
      widget = isc.addProperties({}, OB.MyOB.widgets[0]);

  expect(2);

  widget.rowNum += 1;
  widget.dbInstanceId = '';

  OB.MyOB.widgets.push(widget);

  OB.RemoteCallManager.call('org.openbravo.client.myob.MyOpenbravoActionHandler', post, {}, function (rpcResponse, data, rpcRequest) {

    equals(data.message.type, 'Success', 'Widget added');

    OB.MyOB.widgets = data && data.widgets ? eval(data.widgets) : OB.MyOB.widgets; // refreshing widgets
    ok(!checkMissingDbInstanceId(), 'All widgets have a dbInstanceId');

    start(); // restart the flow
  });
});

asyncTest('Move widget', function () {

  var post = isc.addProperties({}, d, {
    'eventType': 'WIDGET_MOVED',
    'widgets': OB.MyOB.widgets
  }),
      tmp = {
      colNum: 0,
      rowNum: 0
      },
      w1 = OB.MyOB.widgets[0],
      w2 = OB.MyOB.widgets[1];

  if (!w1 || !w2) {
    start(); // skip the test
    return;
  }

  expect(3);

  tmp.colNum = w1.colNum;
  tmp.rowNum = w1.rowNum;
  w1.colNum = w2.colNum;
  w1.rowNum = w2.colNum;
  w2.colNum = tmp.colNum;
  w2.rowNum = tmp.rowNum;

  ok(!checkMissingDbInstanceId(post.widgets), 'All posted widgets have a dbInstanceId');

  OB.RemoteCallManager.call('org.openbravo.client.myob.MyOpenbravoActionHandler', post, {}, function (rpcResponse, data, rpcRequest) {

    equals(data.message.type, 'Success', 'Widget moved');

    OB.MyOB.widgets = data && data.widgets ? eval(data.widgets) : OB.MyOB.widgets; // refreshing widgets
    ok(!checkMissingDbInstanceId(), 'All widgets have a dbInstanceId');

    start(); // restart the flow
  });
});

asyncTest('Remove widget', function () {

  var post = isc.addProperties({}, d, {
    'eventType': 'WIDGET_REMOVED',
    'widgets': OB.MyOB.widgets
  }),
      removed = OB.MyOB.widgets.splice(-1, 1);

  expect(2);

  OB.RemoteCallManager.call('org.openbravo.client.myob.MyOpenbravoActionHandler', post, {}, function (rpcResponse, data, rpcRequest) {

    equals(data.message.type, 'Success', 'Widget removed');

    OB.MyOB.widgets = data && data.widgets ? eval(data.widgets) : OB.MyOB.widgets; // refreshing widgets
    ok(!checkMissingDbInstanceId(), 'All widgets have a dbInstanceId');

    start(); // restart the flow
  });
});

asyncTest('Get user widgets', function () {

  var post = isc.addProperties({}, d, {
    'eventType': 'RELOAD_WIDGETS',
    'widgets': []
  });

  expect(2);

  OB.RemoteCallManager.call('org.openbravo.client.myob.MyOpenbravoActionHandler', post, {}, function (rpcResponse, data, rpcRequest) {

    equals(data.message.type, 'Success', 'Widgets reloaded');

    OB.MyOB.widgets = data && data.widgets ? eval(data.widgets) : OB.MyOB.widgets; // refreshing widgets
    ok(!checkMissingDbInstanceId(), 'All widgets have a dbInstanceId');

    start(); // restart the flow
  });
});