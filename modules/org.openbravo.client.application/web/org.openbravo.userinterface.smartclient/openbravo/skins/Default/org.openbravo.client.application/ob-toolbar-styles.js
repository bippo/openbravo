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

isc.OBToolbar.addProperties({
  width: '100%',
  height: 45,
  leftMargin: 6,
  rightMargin: 4,
  leftMembersMargin: 4,
  rightMembersMargin: 12
});

isc.OBToolbarIconButton.addProperties({
  width: 30,
  height: 28,
  menuButtonImage: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/toolbar/iconButton-menu-unfold.png' /** There is a CSS hack to avoid showing it when no menu available. this.menuButtonImage inside initWidget doesn't run **/
});

isc.OBToolbarTextButton.addProperties({
  height: 30,
  autoFit: true
});