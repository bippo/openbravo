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

isc.OBWidgetMenu.addProperties({
  // menu in standard SC
  baseStyle: 'OBWidgetMenuCell',
  //normal in standard SC
  styleName: 'OBWidgetMenu',
  //normal in standard SC
  bodyStyleName: 'OBWidgetMenuBody',
  //menuTable in standard SC
  tableStyle: 'OBWidgetMenuTable',
  iconBodyStyleName: 'OBWidgetMenuTable'
});

isc.OBWidgetMenuItem.addProperties({
  showIcon: false,
  showOver: true,
  showRollOver: true,
  showRollOverIcon: true,
  showDown: false,
  showFocused: false,
  showFocusedAsOver: false,
  iconWidth: 18,
  iconHeight: 18,
  iconSpacing: 2,
  width: 18,
  height: 18,
  menuButtonImage: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/widget/edit.png',
  baseStyle: 'OBWidgetMenuButton',
  overflow: 'visible'
});


isc.OBWidget.addProperties({
  headerStyle: 'OBWidgetHeader',
  showEdges: true,
  edgeImage: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/widget/window.png',
  customEdges: null,
  edgeSize: 2,
  edgeTop: 29,
  edgeBottom: 2,
  edgeOffsetTop: 2,
  edgeOffsetRight: 2,
  edgeOffsetBottom: 2,
  edgeOffsetLeft: 2,
  //part of edges
  showHeaderBackground: false,
  showHeaderIcon: true,

  // clear backgroundColor and style since corners are rounded
  backgroundColor: null,
  border: null,
  edgeCenterBackgroundColor: "#FFFFFF",
  bodyColor: "transparent",
  bodyStyle: "windowBody",

  layoutMargin: 0,
  membersMargin: 0,

  showFooter: false,

  showShadow: false,
  shadowDepth: 5
});

isc.OBWidget.changeDefaults('headerDefaults', {
  layoutMargin: 0,
  height: 27
});

isc.OBWidget.changeDefaults('headerLabelDefaults', {
  styleName: 'OBWidgetHeaderText',
  align: isc.Canvas.CENTER
});

isc.OBWidget.changeDefaults('restoreButtonDefaults', {
  baseStyle: 'OBWidgetIconRestore',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/widget/restore.png',
  width: 24,
  height: 20
});

isc.OBWidget.changeDefaults('closeButtonDefaults', {
  baseStyle: 'OBWidgetIconClose',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/widget/close.png',
  width: 24,
  height: 20
});

isc.OBWidget.changeDefaults('maximizeButtonDefaults', {
  baseStyle: 'OBWidgetIconMaximize',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/widget/maximize.png',
  width: 24,
  height: 20
});

isc.OBWidget.changeDefaults('minimizeButtonDefaults', {
  baseStyle: 'OBWidgetIconMinimize',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/widget/minimize.png',
  width: 24,
  height: 20
});

// MyOpenbravo dialogs (left menu)
isc.OBMyOBDialog.addProperties({
  styleName: 'OBMyOBDialog',
  headerStyle: 'OBMyOBDialogHeader',
  bodyStyle: "OBMyOBDialogBody",
  showEdges: true,
  edgeImage: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/dialog/window.png',
  customEdges: null,
  edgeSize: 6,
  edgeTop: 23,
  edgeBottom: 6,
  edgeOffsetTop: 2,
  edgeOffsetRight: 5,
  edgeOffsetBottom: 5,
  showHeaderBackground: false,
  // part of edges
  showHeaderIcon: true,

  border: null,

  layoutMargin: 0,
  membersMargin: 0,

  showFooter: false,

  showShadow: false,
  shadowDepth: 5
});

isc.OBMyOBDialog.changeDefaults('headerDefaults', {
  layoutMargin: 0,
  height: 24
});

isc.OBMyOBDialog.changeDefaults('headerLabelDefaults', {
  styleName: 'OBMyOBDialogHeaderText',
  align: isc.Canvas.CENTER
});

isc.OBMyOBDialog.changeDefaults("closeButtonDefaults", {
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/dialog/headerIcons/close.png',
  showRollOver: true,
  showDown: false,
  width: 15,
  height: 15
});

isc.OBWidgetInFormItem.changeDefaults("widgetProperties", {
  edgeImage: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/form/border.png',
  edgeSize: 1,
  edgeTop: 1,
  edgeBottom: 1,
  edgeOffsetTop: 1,
  edgeOffsetRight: 1,
  edgeOffsetBottom: 1,
  edgeOffsetLeft: 1
});