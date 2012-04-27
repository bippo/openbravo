/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// = OBBaseView =
//
// A class which implements the view api.
isc.ClassFactory.defineClass('OBBaseView', isc.Layout);

isc.OBBaseView.addProperties({

  // ** {{{ OBBaseView.showsItself }}} **
  // If this boolean property is set to true then the Openbravo view manager
  // will not place the view instance in a tab in the main Multi-Document-Interface.
  // Instead it will call the show method on the instance. This makes 
  // it for example possible to define views which are implemented as 
  // popups instead of opened in the main MDI.
  showsItself: false,

  // ** {{{ OBBaseView.isSameTab() }}} **
  // Is called by the view manager when opening a view. The view manager
  // will first check if there is already a tab open by calling the 
  // isSameTab method on each opened view. If one of the views returns
  // true then the requested view is opened in that tab (effectively
  // replacing the current open view there). This is needed for cases
  // when a certain view may only be opened once.
  isSameTab: function (viewId, params) {
    var prop;

    for (prop in params) {
      if (params.hasOwnProperty(prop)) {
        if (params[prop] !== this[prop]) {
          return false;
        }
      }
    }

    // a common implementation does this, this allows only 
    // one instance of certain view class to be open at one point 
    // in time.
    // this will allow multiple tabs to be opened:
    return viewId === this.getClassName();
  },

  // ** {{{ OBBaseView.getBookMarkParams() }}} **
  // Is used to create a bookmarkable url in the browser's address bar.
  // For each opened view this method is called and the result is added
  // to the address bar. This makes it possible for the user to do 
  // back in the browser, to bookmark the url and to build history in the 
  // browser itself. 
  getBookMarkParams: function () {
    var result = {};
    result.viewId = this.getClassName();
    result.tabTitle = this.tabTitle;
    return result;
  },

  // ** {{{ OBBaseView.getHelpView() }}} **
  // This method can return an object containing a view definition. 
  // If this method returns an object then a link is activated in the 
  // help pull-down in the top.
  getHelpView: function () {
    return;
    // an example of returning a view definition, the viewId contains
    // the help view classname, the tabTitle denotes the tab title of the
    // help view
    //    return {
    //        viewId: 'ClassicOBHelp',
    //        tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'),
    //        windowId: this.windowId,
    //        windowType: 'W',
    //        windowName: this.tabTitle
    //    };
  }

});