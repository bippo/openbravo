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

//== OBListFilterItem ==
// Combo box for list references in filter editors.
isc.ClassFactory.defineClass('OBListFilterItem', isc.OBListItem);

isc.OBListFilterItem.addProperties({
  allowExpressions: false,
  moveFocusOnPickValue: false,
  operator: 'equals',
  validateOnExit: false,
  validateOnChange: false,
  filterOnKeypress: false,
  addUnknownValues: false,

  init: function () {
    if (this.valueMap) {
      // add the empty value in this way to make sure that the 
      // space is shown first
      this.valueMap = isc.addProperties({
        '': ''
      }, this.valueMap);
    }
    this.Super('init', arguments);
  },

  setValueMap: function (valueMap) {
    this.Super('setValueMap', [isc.addProperties({
      '': ''
    }, valueMap)]);
  },

  // note: can't override changed as it is used by the filter editor 
  // itself, see the RecordEditor source code and the changed event
  change: function (form, item, value, oldValue) {
    if (this._pickedValue || !value) {
      // filter with a delay to let the value be set
      isc.Page.setEvent(isc.EH.IDLE, this.form.grid, isc.Page.FIRE_ONCE, 'performAction');
    }
    this.Super('change', arguments);
  }
});