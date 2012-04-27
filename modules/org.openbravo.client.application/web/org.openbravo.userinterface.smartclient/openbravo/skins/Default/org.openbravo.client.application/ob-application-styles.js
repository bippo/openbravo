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

/* =====================================================================
 * Styling properties for:
 * 1) Main layout
 * 2) Main components (navbar flyout, main grid, form)
 * 3) Changes to standard isc.Dialog buttons
 =======================================================================*/


/* =====================================================================
 * Main layout styling properties
 =======================================================================*/
// note main layout styling is done a bit differently 
// as this needs to be set when the layout gets created
// Styling of the main layout containing everything
OB.Styles.TopLayout = {
  width: '100%',
  height: '1',
  styleName: 'OBTopLayout',
  overflow: 'visible',
  layoutTopMargin: 4,
  layoutBottomMargin: 10
};

// The toolbar showing the navigation bar components
OB.Styles.TopLayout.NavBar = {
  overflow: 'visible',
  defaultLayoutAlign: 'center',
  styleName: 'OBNavBarToolStrip',
  width: 1,
  layoutLeftMargin: 1,
  separatorSize: 0,
  height: 28
};


/* =====================================================================
 * Main components styling properties
 =======================================================================*/

isc.OBPopup.addProperties({
  width: 600,
  height: 500
});

isc.OBStandardWindow.addProperties({
  toolBarHeight: 40
});


/* =====================================================================
 * Loading prompt
 =======================================================================*/
OB.Styles.LoadingPrompt = {
  mainLayoutStyleName: 'OBLoadingPromptModalMask',
  loadingLayoutStyleName: 'OBLoadingPromptLabel',
  loadingImage: {
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/system/windowLoading.gif',
    width: 220,
    height: 16
  } /* Generated @ http://www.ajaxload.info/ */
  /* Indicator type: 'Bar' - Background color: #7F7F7F - Transparent background - Foreground color: #FFFFFF */
};

/* =====================================================================
 * Width of the active bar on the left in the main view
 =======================================================================*/
OB.Styles.ActiveBar = {
  width: 6,
  activeStyleName: 'OBViewActive',
  inActiveStyleName: 'OBViewInActive'
};

/* =====================================================================
 * Changed styling of the standard dialogs
 =======================================================================*/

isc.addProperties(isc.Dialog.Warn.toolbarDefaults, {
  buttonConstructor: isc.OBFormButton,
  styleName: 'OBDialogButtonToolBar'
});

isc.Dialog.changeDefaults("messageStackProperties", {
  defaultLayoutAlign: 'center'
});

isc.ListGrid.addProperties({
  alternateRecordStyles: true
});

// this can be removed after this has been solved:
// http://forums.smartclient.com/showthread.php?p=59150#post59150
isc._original_confirm = isc.confirm;
isc.confirm = function (message, callback, properties) {
  // override to set the styling
  var i;
  if (properties && properties.buttons) {
    for (i = 0; i < properties.buttons.length; i++) {
      properties.buttons[i].baseStyle = 'OBFormButton';
      properties.buttons[i].titleStyle = 'OBFormButtonTitle';
      properties.buttons[i].buttonConstructor = isc.OBFormButton;
    }
  }
  isc._original_confirm(message, callback, properties);
};

// override the standard show prompt to show a more custom Openbravo
// loading prompt
// note the loading image is set in the index.html
isc._orginal_showPrompt = isc.showPrompt;
isc.showPrompt = function (prompt) {
  var width, height, top, left, props = {},
      dialog = isc.Dialog.Prompt,
      modalTarget;
  if (OB.OBModalTarget) {
    props = {
      showEdges: false,
      showModalMask: true,
      isModal: true,
      hiliteBodyColor: null,
      bodyColor: null,
      bodyStyle: 'OBLoadingPromptBody'
    };
    props.isModal = true;
    modalTarget = OB.OBModalTarget;
    props.modalTarget = modalTarget;
    isc.Dialog.OBModalTarget = null;

    // find the top/left position, center in the modalTarget
    width = dialog.getVisibleWidth();
    height = dialog.getVisibleHeight();
    left = modalTarget.getPageLeft() + ((modalTarget.getWidth() - width) / 2) + modalTarget.getScrollLeft();
    top = modalTarget.getPageTop() + ((modalTarget.getHeight() - height) / 2) + modalTarget.getScrollTop();
    props.left = Math.round(left);
    props.top = Math.max(Math.round(top), 0);
    props.autoCenter = false;
  }

  isc._orginal_showPrompt(prompt, props);
};