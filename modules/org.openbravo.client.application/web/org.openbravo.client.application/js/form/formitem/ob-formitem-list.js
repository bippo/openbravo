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

// == OBListItem ==
// Combo box for list references, note is extended by OBFKItem again.
isc.ClassFactory.defineClass('OBListItem', isc.ComboBoxItem);

isc.OBListItem.addProperties({
  operator: 'equals',
  hasPickList: true,
  showPickListOnKeypress: true,
  cachePickListResults: false,
  completeOnTab: true,
  validateOnExit: true,

  // https://issues.openbravo.com/view.php?id=19876
  selectOnFocus: false,
  // still do select on focus initially
  doInitialSelectOnFocus: true,

  // textMatchStyle is used for the client-side picklist
  textMatchStyle: 'substring',

  // NOTE: Setting this property to false fixes the issue when using the mouse
  // to pick a value
  // FIXME: Sometimes the field label gets a red color (a blink)
  // if set to false then the picklist is shown at focus:
  // https://issues.openbravo.com/view.php?id=18075
  // addUnknownValues: true,
  // changeOnKeypress should not be set to false together 
  // with addUnknownValues (to false) as this will 
  // cause the picklist not to show
  // changeOnKeypress: false,
  moveFocusOnPickValue: true,

  hidePickListOnBlur: function () {

    // when the form gets redrawn the the focus may not be in
    // the item but it is still the item which gets the focus
    // after redrawing
    if (this.form && this.form._isRedrawing && this.form.getFocusItem() === this) {
      return;
    }

    this.Super('hidePickListOnBlur', arguments);
  },

  // is overridden to keep track that a value has been explicitly picked
  pickValue: function (value) {
    this._pickedValue = true;
    this.Super('pickValue', arguments);
    delete this._pickedValue;
    if (this.moveFocusOnPickValue && this.form.focusInNextItem) {
      // update the display before moving the focus
      this.updateValueMap(true);
      this.form.focusInNextItem(this.name);
    }
  },

  changed: function (form, item, value) {
    this.Super('changed', arguments);
    // if not picking a value then don't do a fic call
    // otherwise every keypress would result in a fic call
    if (!this._pickedValue) {
      return;
    }
    if (this._hasChanged && this.form && this.form.handleItemChange) {
      this.form.handleItemChange(this);
    }
  },

  pickListProperties: {
    showHeaderContextMenu: false
  },

  // to solve: https://issues.openbravo.com/view.php?id=17800
  // in chrome the order of the valueMap object is not retained
  // the solution is to keep a separate entries array with the
  // records in the correct order, see also the setEntries/setEntry
  // methods
  getClientPickListData: function () {
    if (this.entries) {
      return this.entries;
    }
    return this.Super('getClientPickListData', arguments);
  },

  setEntries: function (entries) {
    var length = entries.length,
        i, id, identifier, valueField = this.getValueFieldName(),
        valueMap = {};
    this.entries = [];
    for (i = 0; i < length; i++) {
      id = entries[i][OB.Constants.ID] || '';
      identifier = entries[i][OB.Constants.IDENTIFIER] || '';
      valueMap[id] = identifier;
      this.entries[i] = {};
      this.entries[i][valueField] = id;
    }
    this.setValueMap(valueMap);
  },

  setEntry: function (id, identifier) {
    var i, entries = this.entries || [],
        entry = {},
        valueField = this.getValueFieldName(),
        length = entries.length;
    for (i = 0; i < length; i++) {
      if (entries[i][valueField] === id) {
        return;
      }
    }

    // not found add/create a new one
    entry[valueField] = id;
    entries.push(entry);

    this.setEntries(entries);
  },

  // prevent ids from showing up
  mapValueToDisplay: function (value) {
    var ret = this.Super('mapValueToDisplay', arguments);
    if (this.valueMap && this.valueMap[value]) {
      return this.valueMap[value];
    }
    if (ret === value && this.isDisabled()) {
      return '';
    }
    if (ret === value) {
      if (!this.valueMap) {
        this.valueMap = {};
        this.valueMap[value] = '';
        return '';
      } else if (!this.valueMap[value]) {
        return '';
      }
    }
    return ret;
  }

});