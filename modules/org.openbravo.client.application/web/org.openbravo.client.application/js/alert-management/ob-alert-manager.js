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

// This file contains declarations for 2 types:
// - Alert Manager: responsible for receiving alerts.
// - OBAlertIcon: is the alert icon shown in the navigation bar
// = Alert Manager =
//
// The Alert manager calls the server at preset intervals (50 secs) to obtain the current list 
// of alerts and to update the server side session administration.
// The Alert manager makes use of OB.RemoteCallManager to make these remote calls.
//
(function (OB, isc) {

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }

  // cache object references locally
  var ISC = isc,
      alertmgr; // Local reference to RemoveCallManager instance

  function AlertManager() {}

  AlertManager.prototype = {

    // array of functions which are called when an alert is received
    // from the server.
    listeners: [],

    delay: 50000,

    // last info
    lastResponse: null,
    lastData: null,
    lastRequest: null,

    // ** {{{ AlertManager.addListener(listener) }}} **
    //
    // Register a new listener which will be called when a new alert result is
    // received
    // from the server.
    //
    // Parameters:
    // * {{{listener}}}: a function which is called when a new alert result is
    // received.
    addListener: function (listener) {
      this.listeners[this.listeners.length] = listener;
      if (this.lastResponse) {
        // call the listener once with the last data
        listener(this.lastResponse, this.lastData, this.lastRequest, true);
      }
    },

    _notify: function (rpcResponse, data, rpcRequest) {
      var i, length = OB.AlertManager.listeners.length;
      // store info for new listeners
      OB.AlertManager.lastResponse = rpcResponse;
      OB.AlertManager.lastData = data;
      OB.AlertManager.lastRequest = rpcRequest;
      for (i = 0; i < length; i++) {
        OB.AlertManager.listeners[i](rpcResponse, data, rpcRequest);
      }
      isc.Timer.setTimeout(OB.AlertManager.call, OB.AlertManager.delay);
    },

    call: function () {
      OB.RemoteCallManager.call('org.openbravo.client.application.AlertActionHandler', {}, {
        IsAjaxCall: '1',
        ignoreForSessionTimeout: '1'
      }, OB.AlertManager._notify);
    }
  };

  // Initialize AlertManager object and let it call the system every so-many
  // secs.
  alertmgr = OB.AlertManager = new AlertManager();

  // call it ones to update the pings and start the timer
  OB.AlertManager.call();
}(OB, isc));

isc.ClassFactory.defineClass('OBAlertIcon', isc.ImgButton);

// = OBAlertIcon =
// The OBAlertIcon widget creates a button which notifies the user of any alerts
// present in the system. When an alert is found it will change appearance and
// prompt.
// The OBAlertIcon extends from the Smartclient Button.
// The OBAlertIcon registers itself as a listener in the Alert Manager.
isc.OBAlertIcon.addProperties({

  initWidget: function () {
    var instance = this,
        listener;

    listener = function (rpcResponse, data, rpcRequest) {
      if (data.cnt > 0) {
        OB.I18N.getLabel(instance.alertLabel, [data.cnt], instance, 'setTitle');
        instance.setIcon(instance.alertIcon);
      } else {
        OB.I18N.getLabel(instance.alertLabel, [0], instance, 'setTitle');
        instance.setIcon({});
      }
      instance.markForRedraw();
    };

    this.Super('initWidget', arguments);

    OB.I18N.getLabel(instance.alertLabel, ['-'], instance, 'setTitle');

    // call it to update the number of alerts directly after login
    OB.AlertManager.addListener(listener);
    OB.TestRegistry.register('org.openbravo.client.application.AlertButton', this);
  },

  click: function () {
    var viewDefinition = {
      i18nTabTitle: 'UINAVBA_AlertManagement'
    };
    OB.Layout.ViewManager.openView('OBUIAPP_AlertManagement', viewDefinition);
  },

  keyboardShortcutId: 'NavBar_OBAlertIcon',

  draw: function () {
    var me = this,
        ksAction;

    ksAction = function () {
      me.click();
      return false; //To avoid keyboard shortcut propagation
    };

    if (this.keyboardShortcutId) {
      OB.KeyboardManager.Shortcuts.set(this.keyboardShortcutId, 'Canvas', ksAction);
    }
    this.Super('draw', arguments);
  },
  alertLabel: 'UINAVBA_Alerts',
  autoFit: true,
  showTitle: true,
  src: '',
  overflow: 'visible'
});