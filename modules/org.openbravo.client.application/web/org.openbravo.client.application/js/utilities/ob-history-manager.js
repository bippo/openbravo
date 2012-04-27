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

// = History Manager =
//
// Keeps track of the state of the user interface and handle the back button 
// in a correct way. The state is stored in the url in the address field 
// of the browser. Every user action related to the tabs can result in an 
// update of the history state: select a tab, open a tab, close a tab etc.
// The history manager makes use of the smartclient isc.History object to 
// actually update the address in the address bar and to get called when
// the user presses the back or forward button.
//
(function (OB, isc) {

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }

  // cache object references locally
  var L = OB.Layout,
      QUOTE_REPLACE = '__',
      ISC = isc,
      historyMgr;

  function HistoryManager() {}

  HistoryManager.prototype = {

    // ** {{{ updateHistory }}} **
    //
    // Stores the current state of the main layout as a bookmark. This method
    // is called when new tabs are opened, tabs are closed, a tab is selected
    // or another history changing event occurs. Each
    // tab in the layout is asked for its bookMarkParams which represent the
    // state of that tab. The combined state is passed to the smartclient
    // History object.
    //
    updateHistory: function () {

      var state = {},
          stateStr, data, i, tabsLength, tab, tabObject;

      if (L.ViewManager.inStateHandling) {
        return;
      }

      // the selected tab, note short name to
      // help with browser url restriction
      state.st = OB.MainView.TabSet.getSelectedTabNumber();

      // then for each tab store the book mark parameters
      // note name is abbreviated to help with browser url restriction
      state.bm = [];
      tabsLength = OB.MainView.TabSet.tabs.length;
      data = [];
      for (i = 0; i < tabsLength; i++) {
        tab = OB.MainView.TabSet.tabs[i];

        state.bm[i] = {};

        // get the original tab object
        tabObject = OB.MainView.TabSet.getTabObject(tab);

        state.bm[i] = {
          viewId: tabObject.viewName
        };

        // store the bookmark parameters
        if (tabObject.pane && tabObject.pane.getBookMarkParams) {
          state.bm[i].params = tabObject.pane.getBookMarkParams();
          if (!state.bm[i].params.tabTitle) {
            state.bm[i].params.tabTitle = tabObject.title;
          }
        }

        // let tabs store extra data
        if (tabObject.pane && tabObject.pane.getState) {
          data[i] = tabObject.pane.getState();
        }
      }

      // now encode the state as a json string
      // which is used as a the id in the url
      stateStr = isc.JSON.encode(state, {
        prettyPrint: false,
        strictQuoting: false
      });

      // smartclient fails if there is a " in the string, replace them all
      stateStr = stateStr.replace(/"/g, QUOTE_REPLACE);

      // some browsers have this limitation
      if (stateStr.length > 2083) {
        isc.Log.logWarn('History string length > 2083, not storing history');
        return;
      }

      if (tabsLength === 0) {
        stateStr = null;
      }

      // only store state if something changed
      if (stateStr !== isc.History.getCurrentHistoryId()) {
        isc.Log.logDebug('Updating history ' + stateStr, 'OB');

        isc.History.addHistoryEntry(stateStr, 'Openbravo History', data);
      }
    },

    // ** {{ restoreHistory }} **
    // The call back called by the smartclient history manager when the user
    // presses the back or forward button or moves through the history in
    // general.
    // Parameters:
    // * {{{id}}} type: JSON String, contains the complete state as a json
    // string
    // * {{{data}}} type: String, extra parameter passed in by Smartclient, not
    // used
    restoreHistory: function (id, data) {
      var correctedId, state;
      isc.Log.logDebug('Restoring history ' + id, 'OB');

      if (!id) {
        // don't do anything in this case
        return;
      }

      // see the QUOTE_REPLACE constant
      correctedId = id.replace(/__/g, '"');
      state = isc.JSON.decode(correctedId);

      L.ViewManager.restoreState(state, data);
    }
  };

  // Initialize the HistoryManager object
  historyMgr = L.HistoryManager = new HistoryManager();

  // and register the callback
  isc.History.registerCallback('OB.Layout.HistoryManager.restoreHistory(id, data)');

}(OB, isc));