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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.Styles.OBApplicationMenu = {
  Icons: {
    folderOpened: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconFolderOpened.png',
    folderClosed: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconFolderClosed.png',
    window: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconWindow.png',
    process: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconProcess.png',
    processManual: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconProcess.png',
    report: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconReport.png',
    task: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconTask.png',
    form: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconForm.png',
    externalLink: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconExternalLink.png',
    view: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconForm.png',
    document: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/application-menu/iconDocument.png'
  }
};


isc.OBApplicationMenuTreeChild.addProperties({
  styleName: 'OBApplicationMenuTree',
  baseStyle: 'OBApplicationMenuTreeItemCell',
  bodyStyleName: 'OBApplicationMenuTreeBody',
  iconBodyStyleName: 'OBApplicationMenuTreeIconBody',
  tableStyle: "OBApplicationMenuTreeTable"
});


isc.OBApplicationMenuTree.addProperties({
  styleName: 'OBApplicationMenuTree',
  baseStyle: 'OBApplicationMenuTreeItemCell',
  bodyStyleName: 'OBApplicationMenuTreeBody',
  iconBodyStyleName: 'OBApplicationMenuTreeIconBody',
  tableStyle: "OBApplicationMenuTreeTable",
  hideButtonLineStyle: 'OBNavBarComponentHideLine',
  submenuOffset: -6,
  drawStyle: function () {
    //this.setStyleName(this.styleName);
  },
  showStyle: function () {
    this.menuButton.parentElement.setStyleName('OBNavBarComponentSelected');
  },
  hideStyle: function () {
    this.menuButton.parentElement.setStyleName('OBNavBarComponent');
  }
});


isc.OBApplicationMenuButton.addProperties({
  baseStyle: 'OBNavBarTextButton',
  showMenuButtonImage: false,
  align: 'center',
  height: 26,
  iconHeight: 6,
  iconWidth: 10,
  iconSpacing: 10,
  iconAlign: 'left',
  iconOrientation: 'right',
  nodeIcons: {
    Window: OB.Styles.OBApplicationMenu.Icons.window,
    Process: OB.Styles.OBApplicationMenu.Icons.process,
    ProcessManual: OB.Styles.OBApplicationMenu.Icons.processManual,
    Report: OB.Styles.OBApplicationMenu.Icons.report,
    Task: OB.Styles.OBApplicationMenu.Icons.task,
    Form: OB.Styles.OBApplicationMenu.Icons.form,
    ExternalLink: OB.Styles.OBApplicationMenu.Icons.externalLink,
    Folder: OB.Styles.OBApplicationMenu.Icons.folderOpened,
    View: OB.Styles.OBApplicationMenu.Icons.view
  },
  icon: {
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/navbar/iconOpenDropDown.png'
  },
  showMenuStyle: function () {
    this.parentElement.setStyleName('OBNavBarComponentSelected');
  }
});