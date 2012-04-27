/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
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

// == OBFKItem ==
// Extends OBListItem
isc.ClassFactory.defineClass('OBFKItem', isc.OBListItem);

isc.ClassFactory.mixInInterface('OBFKItem', 'OBLinkTitleItem');

isc.OBFKItem.addProperties({
  operator: 'iContains',

  // set the identifier field also, that's what gets displayed in the grid
  changed: function (form, item, value) {
    if (!this._pickedValue && value) {
      return;
    }

    var display = this.mapValueToDisplay(value),
        identifierFieldName = this.name + '.' + OB.Constants.IDENTIFIER;
    form.setValue(identifierFieldName, display);
    // make sure that the grid does not display the old identifier
    if (form.grid) {
      form.grid.setEditValue(form.grid.getEditRow(), identifierFieldName, display);
    }
    return this.Super('changed', arguments);
  }
});