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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.OBPersonalizationTreeGrid.addProperties({
  bodyStyleName: 'OBGridBody',
  baseStyle: 'OBPersonalizationTreeGridCell',
  styleName: 'OBFormPersonalizationFieldsTreeGrid',

  showOpener: false,
  // eventhough showOpener is false, still space is taken for an opener
  // icon, set to a small number, should be > 0 (otherwise it it not used)
  // this setting of 2 makes the drag indicator to be 2 pixels to the right also
  openerIconSize: 2,

  width: '100%',
  indentSize: 10
});


OB.Styles.Personalization = {};

OB.Styles.Personalization.Menu = {
  styleName: 'OBPersonalizationPullDownMenu',
  baseStyle: 'OBPersonalizationPullDownMenuCell',
  bodyStyleName: 'OBPersonalizationPullDownMenuBody',
  tableStyle: "OBPersonalizationPullDownMenuTable",
  iconBodyStyleName: 'OBPersonalizationPullDownMenuBody',
  iconWidth: 4,
  iconHeight: 8,
  itemIcon: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/iconSelectedView.png',
  bodyBackgroundColor: null
};

OB.Styles.Personalization.saveViewPopupSmall = {
  width: 250,
  height: 150
};

OB.Styles.Personalization.saveViewPopupLarge = {
  width: 250,
  height: 280
};

OB.Styles.Personalization.popupButtonLayout = {
  layoutTopMargin: 20,
  membersMargin: 10,
  width: '100%',
  align: 'center',
  overflow: 'visible',
  height: 1
};

OB.Styles.Personalization.viewFieldDefaults = {
  width: 200
};

OB.Styles.Personalization.deleteViewPopup = {
  width: 250,
  height: 150
};

OB.Styles.Personalization.buttonBarProperties = {
  width: 30
};

OB.Styles.Personalization.Icons = {
  fieldGroup: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/iconFolder.png',
  field: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/item.png',
  fieldDisplayLogic: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemDisplayLogic.png',
  fieldDisplayLogicHidden: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemDisplayLogicHidden.png',
  fieldHidden: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemHidden.png',
  fieldRequired: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemRequired.png',
  fieldRequiredDisplayLogic: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemRequiredDisplayLogic.png',
  fieldRequiredDisplayLogicHidden: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemRequiredDisplayLogicHidden.png',
  fieldRequiredHidden: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/personalization/itemRequiredHidden.png'
};

OB.Styles.Personalization.closeButtonProperties = {
  width: 18,
  height: 18
};

OB.Styles.Personalization.FormPersonalizerLeftPane = {
  width: 200
};

OB.Styles.Personalization.FieldsLayout = {
  styleName: 'OBFieldsPane'
};

OB.Styles.Personalization.Preview = {
  styleName: 'OBFormPersonalizerPreviewPanel'
};

OB.Styles.Personalization.PropertiesTabSet = {
  expandedHeight: 175,
  collapsedHeight: 35
};

OB.Styles.Personalization.PropertiesLayout = {
  styleName: 'OBFormPersonalizerPropertiesPane'
};

// used to display a tab header above sections of the personalization form
OB.Styles.Personalization.TabSet = {
  tabBarProperties: {
    styleName: 'OBTabBarChild',
    simpleTabBaseStyle: 'OBTabBarButtonChild',
    paneContainerClassName: 'OBTabSetChildContainer',
    buttonConstructor: isc.OBTabBarButton,

    buttonProperties: {
      // prevent the orange hats
      customState: 'Inactive',

      src: '',
      capSize: 14,
      titleStyle: 'OBTabBarButtonChildTitle'
    }
  },
  tabBarPosition: 'top',
  tabBarAlign: 'left',
  width: '100%',
  height: '100%',
  overflow: 'hidden',

  showTabPicker: false,

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
};