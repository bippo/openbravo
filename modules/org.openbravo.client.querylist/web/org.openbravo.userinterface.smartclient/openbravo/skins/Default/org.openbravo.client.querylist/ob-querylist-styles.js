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


isc.OBQueryListView.addProperties({
  styleName: 'OBQueryListView'
});

isc.OBQueryListGrid.addProperties({
  bodyStyleName: 'OBQueryListGridBody'
});

isc.OBQueryListWidget.addProperties({
  OBQueryListShowAllLabelHeight: 20
});


isc.ClassFactory.defineClass('OBQueryListShowAllLabel', isc.Label);

isc.OBQueryListShowAllLabel.addProperties({
  className: 'OBQueryListShowAllLabel',
  showDown: true,
  showFocused: true,
  showFocusedAsOver: true,
  showRollOver: true,
  height: 20,
  wrap: false,
  width: '*'
});

isc.ClassFactory.defineClass('OBQueryListRowsNumberLabel', isc.Label);

isc.OBQueryListRowsNumberLabel.addProperties({
  className: 'OBQueryListRowsNumberLabel',
  showDown: false,
  showFocused: false,
  showFocusedAsOver: false,
  showRollOver: false,
  height: 20,
  wrap: false,
  width: '*'
});