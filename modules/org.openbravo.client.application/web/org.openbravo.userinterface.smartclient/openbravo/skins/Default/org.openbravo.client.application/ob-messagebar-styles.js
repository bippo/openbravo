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


isc.OBMessageBar.addProperties({
  styleName: 'OBMessageBar_tip',
  width: '100%',
  height: 40,
  overflow: 'visible',
  setTypeStyle: function (type) {
    this.mainIcon.setSrc('');
    this.closeIcon.setSrc('');
    this.setStyleName('OBMessageBar_' + type);
    this.mainIcon.setSrc(OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/messagebar/mainIcon-' + type + '.png');
    this.text.setStyleName('OBMessageBarDescriptionText OBMessageBarDescriptionText_' + type);
    this.closeIcon.setSrc(OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/messagebar/closeIcon-' + type + '.png');
  }
});

isc.OBMessageBarMainIcon.addProperties({
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/messagebar/mainIcon-tip.png',
  width: 47
});

isc.OBMessageBarDescriptionText.addProperties({
  styleName: 'OBMessageBarDescriptionText OBMessageBarDescriptionText_tip',
  width: '*'
});

isc.OBMessageBarCloseIcon.addProperties({
  baseStyle: 'OBMessageBarCloseIcon',
  width: 12,
  // 10 + 2 of the margin set in the CSS
  height: 12,
  // 10 + 2 of the margin set in the CSS
  align: 'left',
  showRollOver: true,
  showDown: false,
  showFocused: false,
  overflow: 'visible',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/messagebar/closeIcon-tip.png'
});