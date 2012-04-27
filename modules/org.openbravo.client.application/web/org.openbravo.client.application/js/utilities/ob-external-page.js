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

// = Popup External Window =
//
// A view which displays an external url inside a tab of the main layout.
//
isc.defineClass('OBExternalPage', isc.HTMLFlow).addProperties({
  showsItself: false,
  contentsType: 'page',
  showEdges: false,
  styleName: 'obClassicWindow',
  padding: 0,
  margin: 0,
  height: '100%',
  width: '100%'
});

// get the label and set it in the class
OB.I18N.getLabel('OBUIAPP_Loading', null, isc.OBExternalPage, 'loadingMessage');

isc.OBExternalPage.addMethods({

  // the following methods are used to support history management
  getBookMarkParams: function () {
    var result = {};
    result.contentsURL = this.contentsURL;
    return result;
  },

  isSameTab: function (params) {
    return (params && (params.contentsURL || this.contentsURL) && params.contentsURL === this.contentsURL);
  }
});