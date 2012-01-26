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

// The quick run widget is used for flyouts in the navigation bar
isc.OBQuickRun.addProperties({

  // ** {{{ baseStyle }}} **
  // The base style for the quick run launch button. All other styles are
  // derived
  // from this base style.
  baseStyle: 'OBNavBarImgButton'
});

// Styling properties for the help/about navigation bar component
isc.OBHelpAbout.addProperties({
  baseStyle: 'OBNavBarTextButton',
  iconHeight: 6,
  iconWidth: 10,
  iconSpacing: 10,
  icon: {
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/navbar/iconOpenDropDown.png'
  },
  iconOrientation: 'right'
});

isc.OBUserProfile.addProperties({
  baseStyle: 'OBNavBarTextButton',

  // ** {{{ icon settings }}} **
  //
  // The green triangle icon on the right of the button.
  iconHeight: 6,
  iconWidth: 10,
  iconSpacing: 10,
  icon: {
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/navbar/iconOpenDropDown.png'
  },
  iconOrientation: 'right'
});

// Styling properties for the quick launch and quick create components
// See also isc.OBQuickRun styling properties
isc.OBQuickLaunch.addProperties({
  // todo: it is nicer to move this to a style but then this issue occurs:
  // https://issues.openbravo.com/view.php?id=13786
  width: 57,
  separatorHeight: 10,
  
  layoutProperties: {
    width: 250,
    membersMargin: 0 // use custom attribute "separatorHeight" instead
  },

  nodeIcons: {
    Window: OB.Styles.OBApplicationMenu.Icons.window,
    Process: OB.Styles.OBApplicationMenu.Icons.process,
    Report: OB.Styles.OBApplicationMenu.Icons.report,
    Form: OB.Styles.OBApplicationMenu.Icons.form
  },

  createNew_src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/navbar/iconCreateNew.png',
  quickLaunch_src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/navbar/iconQuickLaunch.png'
});

// Styling properties for the logout button in the navbar
isc.OBLogout.addProperties({
  baseStyle: 'OBNavBarImgButton',
  height: 14,
  width: 36,
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/navbar/iconClose.png',
  showTitle: false,
  imageType: 'normal',
  layoutAlign: 'center',
  overflow: 'visible',
  showRollOver: false,
  showFocused: false,
  showDown: false
});


isc.OBQuickLaunchRecentLinkButton.addProperties({
  width: 1, //To allow button be just text width
  align: 'left',
  baseStyle: 'OBQuickLaunchRecentLinkButton',
  showDown: true,
  showFocused: true,
  showFocusedAsOver: false,
  showRollOver: true,
  autoFit: true,
  height: 1,
  overflow: 'visible'
});

isc.OBHelpAboutLinkButton.addProperties({
  width: 1, //To allow button be just text width
  align: 'left',
  baseStyle: 'OBHelpAboutLinkButton',
  showDown: true,
  showFocused: true,
  showFocusedAsOver: false,
  showRollOver: true,
  autoFit: true,
  height: 1,
  overflow: 'visible'
});


isc.OBAlertIcon.addProperties({
  baseStyle : 'OBNavBarTextButton',
  alertIcon : {
    src : OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/navbar/iconAlert.png'
  },
  iconOrientation : 'left',
  iconWidth : 11,
  iconHeight : 13
});