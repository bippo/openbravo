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

// = OB Classic Window =
//
// Implements the view which shows a classic OB window in a Smartclient HTMLFlow component. The 
// classic OB window is shown in a tab in the multi-tab interface. 
//
isc.defineClass('OBClassicWindow', isc.HTMLPane).addProperties({
  showsItself: false,
  contentsType: 'page',
  windowId: '',
  tabId: '',
  recordId: '',
  // tab title is the title in the MDI tab, it is set in the view-manager
  // and updated below
  tabTitle: null,
  processId: null,
  formId: null,
  mappingName: null,
  command: 'DEFAULT',
  showEdges: false,
  styleName: 'obClassicWindow',
  appURL: OB.Application.contextUrl + 'security/Menu.html',
  obManualURL: '',
  padding: 0,
  margin: 0,
  height: '100%',
  width: '100%',
  // ignore the tab info update for one time, to prevent double history entries
  ignoreTabInfoUpdate: true,
  hasBeenDrawnOnce: false,
  appFrameWindow: null
});

isc.OBClassicWindow.addMethods({

  // ** {{{ updateTabInformation }}} **
  //
  // Is called to update the tab information of an opened classic window.
  updateTabInformation: function (windowId, tabId, recordId, command, obManualURL, title) {
    // ignore the first time
    if (this.ignoreTabInfoUpdate) {
      this.ignoreTabInfoUpdate = false;
      return;
    }

    this.windowId = windowId || '';

    this.tabId = tabId || '';

    this.recordId = recordId || '';

    this.command = (command ? command.toUpperCase() : '') || 'DEFAULT';

    this.obManualURL = obManualURL || '';

    this.tabTitle = null;

    OB.Layout.HistoryManager.updateHistory();
  },

  // ** {{{ refreshTab }}} **
  //
  // Is used to handle the refresh keyboard shortcut, clicks the refresh button
  // of a classic window.
  refreshTab: function () {
    if (this.getAppFrameWindow()) {
      this.getAppFrameWindow().document.getElementById('buttonRefresh').onclick();
    }
  },

  // ** {{{ tabSelected }}} **
  //
  // Is used to place the focus in a tab after one of the flyouts is closed.
  tabSelected: function () {
    var appFrameWindow = this.getAppFrameWindow();
    if (appFrameWindow && appFrameWindow.putFocusOnWindow) {
      appFrameWindow.putFocusOnWindow();
    }
  },

  initWidget: function (args) {
    var urlCharacter = '?';
    if (this.appURL.indexOf('?') !== -1) {
      urlCharacter = '&';
    }
    if (this.keyParameter) {
      this.contentsURL = this.appURL + urlCharacter + 'url=' + this.mappingName + '&' + this.keyParameter + '=' + this.recordId + '&noprefs=true&Command=DIRECT&hideMenu=true';
    } else if (this.obManualURL && this.obManualURL !== '') {
      this.obManualURL = this.obManualURL.replace('?', '&');

      this.contentsURL = this.appURL + urlCharacter + 'url=' + this.obManualURL + '&noprefs=true&hideMenu=true';

      if (this.obManualURL.indexOf('Command=') === -1) {
        // Add command in case it is not already set in the obManualURL
        this.contentsURL = this.contentsURL + '&Command=' + this.command;
      }
    } else {
      this.contentsURL = this.appURL + urlCharacter + 'Command=' + this.command + '&noprefs=true';
      if (this.recordId !== '') {
        this.contentsURL = this.contentsURL + '&windowId=' + this.windowId;
      }
      this.contentsURL = this.contentsURL + '&tabId=' + this.tabId;
      if (this.recordId !== '') {
        this.contentsURL = this.contentsURL + '&recordId=' + this.recordId;
      }
      this.contentsURL = this.contentsURL + '&hideMenu=true';
    }

    this.Super('initWidget', args);
  },

  // ** {{{ getIframeWindow }}} **
  //
  // Returns the contentWindow object of the iframe implementing the classic
  // window.
  getIframeWindow: function () {
    var container, iframes;

    container = this.getHandle();

    if (container && container.getElementsByTagName) {
      iframes = container.getElementsByTagName('iframe');
      if (iframes.length > 0) {
        return (iframes[0].contentWindow ? iframes[0].contentWindow : null);
      }
    }
    return null;
  },

  // ** {{{ getAppFrameWindow }}} **
  //
  // Returns the appFrame object of the contentWindow of the iframe implementing
  // the classic window.
  getAppFrameWindow: function () {
    var iframe;
    if (this.appFrameWindow !== null) {
      return this.appFrameWindow;
    }
    iframe = this.getIframeWindow();
    this.appFrameWindow = (iframe && iframe.appFrame ? iframe.appFrame : null); // caching
    // reference
    return this.appFrameWindow;
  },

  // The following methods are related to history management, i.e. that a
  // specific window is only opened once.
  getBookMarkParams: function () {
    var result = {};
    if (this.recordId) {
      result.recordId = this.recordId;
    }
    if (this.windowId) {
      result.windowId = this.windowId;
    }
    if (this.obManualURL) {
      result.obManualURL = this.obManualURL;
    }
    if (this.command) {
      result.command = this.command;
    }
    if (this.tabId) {
      result.tabId = this.tabId;
    }
    if (this.processId) {
      result.processId = this.processId;
    }
    if (this.formId) {
      result.formId = this.formId;
    }
    if (this.keyParameter) {
      result.keyParameter = this.keyParameter;
    }
    if (this.mappingName) {
      result.mappingName = this.mappingName;
    }
    return result;
  },

  isEqualParams: function (params) {
    if (params && (this.recordId || params.recordId) && params.recordId !== this.recordId) {
      return false;
    }

    if (params && (this.command || params.command) && params.command !== this.command) {
      return false;
    }

    if (params && (this.tabId || params.tabId) && params.tabId !== this.tabId) {
      return false;
    }

    if (params && (this.formId || params.formId) && params.formId !== this.formId) {
      return false;
    }

    if (params && (this.windowId || params.windowId) && params.windowId !== this.windowId) {
      return false;
    }

    if (params && (this.processId || params.processId) && params.processId !== this.processId) {
      return false;
    }

    return true;
  },

  isSameTab: function (viewName, params) {
    if (viewName !== 'OBClassicWindow') {
      return false;
    }
    if (params && (params.obManualURL || this.obManualURL) && params.obManualURL === this.obManualURL) {
      return true;
    }

    if (params && (this.windowId || params.windowId) && params.windowId === this.windowId) {
      return true;
    }

    if (params && (this.processId || params.processId) && params.processId === this.processId) {
      return true;
    }

    if (params && (this.formId || params.formId) && params.formId === this.formId) {
      return true;
    }

    if ((!params || params.tabId === '') && this.tabId === '') {
      return true;
    }

    return params.tabId === this.tabId;
  },

  // ** {{{ getHelpView }}} **
  //
  // Returns the view definition of the help window for this classic window.
  getHelpView: function () {
    if (this.windowId) {
      // tabTitle is set in the viewManager
      return {
        viewId: 'ClassicOBHelp',
        tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'),
        windowId: this.windowId,
        windowType: 'W',
        windowName: this.tabTitle
      };
    }
    if (this.processId) {
      return {
        viewId: 'ClassicOBHelp',
        windowId: null,
        tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'),
        windowType: 'R',
        windowName: this.processId
      };
    }
    if (this.formId) {
      return {
        viewId: 'ClassicOBHelp',
        windowId: null,
        tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'),
        windowType: 'X',
        windowName: this.formId
      };
    }
    return null;
  },

  // ** {{{ saveRecord }}} **
  //
  // Is used for supporting autosave, saves the specific tab of the window.
  // Calls the server to do the actual save, the response calls the callback
  // method.
  saveRecord: function ( /* String */ tabID, /* Function */ callback) {
    var postData, reqObj, appFrame = this.appFrameWindow || this.getAppFrameWindow(),
        saveCallback = callback || this.ID + '.saveCallback(rpcResponse, data, rpcRequest)',
        tabid = tabID || '';

    postData = {};
    OB.Utilities.addFormInputsToCriteria(postData, appFrame);

    postData.Command = 'SAVE_XHR';
    postData.tabID = tabid;

    reqObj = {
      params: postData,
      callback: saveCallback,
      evalResult: false,
      httpMethod: 'POST',
      useSimpleHttp: true,
      actionURL: OB.Application.contextUrl + postData.mappingName
    };
    isc.RPCManager.sendRequest(reqObj);
  },

  // ** {{{ saveCallback }}} **
  //
  // If the save is successfull closes the tab.
  saveCallback: function (rpcResponse, data, rpcRequest) {
    var result = eval('(' + data + ')'),
        appFrame = this.appFrameWindow || this.getAppFrameWindow();
    if (result && result.oberror) {
      if (result.oberror.type === 'Success') {
        appFrame.isUserChanges = false;
        OB.MainView.TabSet.removeTab(OB.MainView.TabSet.getTab(result.tabid));
      } else {
        appFrame.location.href = result.redirect;
      }
    }
  }
});

// Maintained to support recent items which use the old name
// NOTE: can be removed when the user interface is released in production in Q1 2011
// at that time the recent items should have been cleaned up
isc.defineClass('ClassicOBWindow', isc.OBClassicWindow);