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


isc.OBStatusBar.addProperties({
  styleName: "OBStatusBar",
  width: '100%',
  height: 30,
  leaveScrollbarGap: false,
  overflow: 'hidden',

  statusLabelStyle: 'OBStatusBarTextLabel_Status',
  titleLabelStyle: 'OBStatusBarTextLabel_Title',
  titleLinkStyle: 'OBStatusBarTextLink_Title',
  titleLinkImageSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/form/ico-to-new-tab.png',
  titleLinkImageWidth: 8,
  titleLinkImageHeight: 8,
  fieldLabelStyle: 'OBStatusBarTextLabel_Field',
  separatorLabelStyle: 'OBStatusBarTextLabel_Separator',
  labelOverflowHidden: true,

  savedIconDefaults: {
    height: 14,
    width: 14,
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/statusbar/ico-saved.png'
  },

  newIconDefaults: {
    height: 14,
    width: 14,
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/statusbar/ico-new.png'
  },

  editIconDefaults: {
    height: 14,
    width: 14,
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/statusbar/ico-edit.png'
  },

  iconButtonGroupSpacerWidth: 5
});

isc.OBStatusBarLeftBar.addProperties({
  baseStyle: 'OBStatusBarLeftBar',
  width: '*',
  membersMargin: 5,
  layoutLeftMargin: 7,
  defaultLayoutAlign: 'center',
  align: 'left',
  overflow: 'visible'
});

isc.OBStatusBarTextLabel.addProperties({
  wrap: false,
  width: 1,
  height: 1,
  overflow: 'visible',
  baseStyle: "OBStatusBarTextLabel"
});

isc.OBStatusBarIconButtonBar.addProperties({
  styleName: "OBStatusBarIconButtonBar",
  width: 130,
  align: 'right',
  overflow: 'visible',
  membersMargin: 4
});

isc.OBStatusBarIconButton.addProperties({
  imageType: 'center',
  showRollOver: true,
  showDown: true,
  showFocused: false,
  genericIconSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/statusbar/iconButton.png',
  /* Can be: previous - next - maximize - minimize - restore - close */
  initWidgetStyle: function () {
    var fileExt = this.genericIconSrc.substring(this.genericIconSrc.lastIndexOf('.'), this.genericIconSrc.length);
    var filePath = this.genericIconSrc.substring(0, this.genericIconSrc.length - fileExt.length) + '-';
    var buttonType = this.buttonType;
    if (isc.Page.isRTL()) {
      if (buttonType === 'next') {
        buttonType = 'previous';
      } else if (buttonType === 'previous') {
        buttonType = 'next';
      }
    }
    this.setSrc(filePath + buttonType + fileExt);
  }
});