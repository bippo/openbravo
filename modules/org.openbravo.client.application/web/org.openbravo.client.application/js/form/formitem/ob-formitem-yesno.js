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

// == OBYesNoItem ==
// Extends ComboBoxItem with preset yes and no values.
isc.ClassFactory.defineClass('OBYesNoItem', isc.ComboBoxItem);

isc.OBYesNoItem.addProperties({
  operator: 'equals',
  addUnknownValues: false,

  // solves:
  // https://issues.openbravo.com/view.php?id=18592
  setValue: function (value) {
    if (value === 'true') {
      this.Super('setValue', [true]);
    } else if (value === 'false') {
      this.Super('setValue', [false]);
    } else {
      this.Super('setValue', arguments);
    }
  },

  // is needed because addUnknownValues is false
  isUnknownValue: function (enteredValue) {
    var i, vm = this.getValueMap();
    if (vm !== null) {
      for (i = 0; i < vm.length; i++) {
        if (enteredValue === this.mapValueToDisplay(vm[i])) {
          return false;
        }
      }
    }
    return this.Super('isUnknownValue', arguments);
  },

  mapValueToDisplay: function (value, a, b, c) {
    return OB.Utilities.getYesNoDisplayValue(value);
  },
  formatPickListValue: function (value, record, field, rowNum, colNum) {
    return OB.Utilities.getYesNoDisplayValue(value);
  }
});