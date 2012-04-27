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

// = Classic OB Help =
//
// Implements the view which shows a help window for a classic OB window in a tab
// of the main layout. It is called from the OBHelpAbout widget displayed
// in the navigation bar.
//
isc.defineClass('ClassicOBHelp', isc.HTMLPane).addProperties({
  showsItself: false,
  contentsType: 'page',
  windowId: null,
  windowType: null,
  windowName: null,
  recordId: '',
  command: 'DEFAULT',
  showEdges: false,
  styleName: 'obClassicWindow',
  appURL: OB.Application.contextUrl + 'security/Menu.html',
  padding: 0,
  margin: 0,
  height: '100%',
  width: '100%'
});

// get the label and set it in the class
OB.I18N.getLabel('OBUIAPP_Loading', null, isc.ClassicOBHelp, 'loadingMessage');

isc.ClassicOBHelp.addMethods({
  initWidget: function (args) {
    this.contentsURL = this.appURL + '?url=/ad_help/DisplayHelp.html&hideMenu=true&noprefs=true';
    if (this.windowId) {
      this.contentsURL = this.contentsURL + '&inpwindowId=' + this.windowId;
    }
    if (this.windowType) {
      this.contentsURL = this.contentsURL + '&inpwindowType=' + this.windowType;
    }
    if (this.windowName) {
      this.contentsURL = this.contentsURL + '&inpwindowName=' + this.windowName;
    }
    this.Super('initWidget', args);
  },

  // The following methods are involved in making sure that a help tab for a 
  // certain window is only opened once.
  getBookMarkParams: function () {
    var result = {};
    if (this.windowId) {
      result.windowId = this.windowId;
    }
    if (this.windowType) {
      result.windowType = this.windowType;
    }
    if (this.windowName) {
      result.windowName = this.windowName;
    }
    result.viewId = 'ClassicOBHelp';
    return result;
  },

  isEqualParams: function (params) {
    if (!params || params.viewId !== 'ClassicOBHelp') {
      return false;
    }
    if ((this.windowId || params.windowId) && params.windowId !== this.windowId) {
      return false;
    }

    if ((this.windowType || params.windowType) && params.windowType !== this.windowType) {
      return false;
    }

    if ((this.windowName || params.windowName) && params.windowName !== this.windowName) {
      return false;
    }

    return true;
  },

  isSameTab: function (viewId, params) {
    if (viewId !== 'ClassicOBHelp') {
      return false;
    }

    if (this.isEqualParams(params)) {
      return true;
    }
    return false;
  }
});