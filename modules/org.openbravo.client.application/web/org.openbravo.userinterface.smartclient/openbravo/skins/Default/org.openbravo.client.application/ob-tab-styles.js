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


isc.OBTabBarButtonMain.addProperties({
  src: '',
  showSelectedIcon: true,
  showRollOverIcon: true,
  align: 'right',
  width: 1,
  overflow: 'visible',
  capSize: 9,
  titleStyle: 'OBTabBarButtonMainTitle'
});

isc.OBTabSetMain.addProperties({
  tabBarConstructor: isc.OBTabBarMain,
  tabBarPosition: 'top',
  tabBarAlign: 'left',
  width: '100%',
  height: '*',
  overflow: 'hidden',

  // get rid of the margin around the content of a pane
  paneMargin: 0,
  paneContainerMargin: 0,
  paneContainerPadding: 0,
  showPaneContainerEdges: false,

  useSimpleTabs: true,
  tabBarThickness: 30,

  styleName: 'OBTabSetMain',
  simpleTabBaseStyle: 'OBTabBarButtonMain',
  paneContainerClassName: 'OBTabSetMainContainer',

  tabProperties: {
    margin: 0,
    padding: 0
  },

  closeTabIcon: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/tab/tabBarButtonMain_CloseIcon.png',
  closeTabIconSize: 18,

  //symmetricScroller:true,
  scrollerSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/tab/tabBarButtonMain_OverflowIcon.png',
  pickerButtonSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/tab/tabBarButtonMain_OverflowIconPicker.png'
});

isc.OBTabBarMain.addProperties({
  styleName: 'OBTabBarMain'
});


isc.OBTabBarButtonChild.addProperties({
  src: '',
  showSelectedIcon: true,
  showRollOverIcon: true,
  align: 'right',
  width: 1,
  overflow: 'visible',
  capSize: 14,
  titleStyle: 'OBTabBarButtonChildTitle'
});

isc.OBTabSetChild.addProperties({
  tabBarConstructor: isc.OBTabBarChild,
  tabBarPosition: 'top',
  tabBarAlign: 'left',
  width: '100%',
  height: '*',
  overflow: 'hidden',

  // get rid of the margin around the content of a pane
  paneMargin: 0,
  paneContainerMargin: 0,
  paneContainerPadding: 0,
  showPaneContainerEdges: false,

  useSimpleTabs: true,
  tabBarThickness: 38,
  styleName: 'OBTabSetChild',
  simpleTabBaseStyle: 'OBTabBarButtonChild',
  paneContainerClassName: 'OBTabSetChildContainer',

  scrollerSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/tab/tabBarButtonChild_OverflowIcon.png',
  pickerButtonSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/tab/tabBarButtonChild_OverflowIconPicker.png'
});

isc.OBTabBarChild.addProperties({
  styleName: 'OBTabBarChild'
});