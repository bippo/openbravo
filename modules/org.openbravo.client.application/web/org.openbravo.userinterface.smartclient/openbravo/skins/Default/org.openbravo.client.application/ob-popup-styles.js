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


/* OBPopup */

isc.OBPopup.addProperties({
  // rounded frame edges
  showEdges: true,
  edgeImage: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/popup/border.png',
  customEdges: null,
  edgeSize: 2,
  edgeTop: 27,
  edgeBottom: 2,
  edgeOffsetTop: 2,
  edgeOffsetRight: 2,
  edgeOffsetBottom: 2,
  showHeaderBackground: false,
  // part of edges
  showHeaderIcon: true,

  // clear backgroundColor and style since corners are rounded
  backgroundColor: null,
  border: null,
  styleName: 'OBPopup',
  edgeCenterBackgroundColor: '#FFFFFF',
  bodyColor: 'transparent',
  bodyStyle: 'OBPopupBody',
  headerStyle: 'OBPopupHeader',

  layoutMargin: 0,
  membersMargin: 0,

  showShadow: false,
  shadowDepth: 5
});

isc.OBPopup.changeDefaults('headerDefaults', {
  layoutMargin: 0,
  height: 25
});

isc.OBPopup.changeDefaults('headerLabelDefaults', {
  styleName: 'OBPopupHeaderText',
  align: isc.Canvas.CENTER
});

isc.OBPopup.changeDefaults('resizerDefaults', {
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/popup/resizer.png'
});

isc.OBPopup.changeDefaults('headerIconDefaults', {
  styleName: 'OBPopupHeaderIcon',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/popup/iconHeader.png',
  width: 20,
  height: 16
});

isc.OBPopup.changeDefaults('restoreButtonDefaults', {
  baseStyle: 'OBPopupIconRestore',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/popup/restore.png',
  width: 24,
  height: 20
});

isc.OBPopup.changeDefaults('closeButtonDefaults', {
  baseStyle: 'OBPopupIconClose',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/popup/close.png',
  width: 24,
  height: 20
});

isc.OBPopup.changeDefaults('maximizeButtonDefaults', {
  baseStyle: 'OBPopupIconMaximize',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/popup/maximize.png',
  width: 24,
  height: 20
});

isc.OBPopup.changeDefaults('minimizeButtonDefaults', {
  baseStyle: 'OBPopupIconMinimize',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/popup/minimize.png',
  width: 24,
  height: 20
});

/* OBPopupHTMLFlow */

isc.OBPopupHTMLFlow.addProperties({
  styleName: 'OBPopupHTMLFlow'
});