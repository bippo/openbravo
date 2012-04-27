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

// == OBTextAreaItem and OBPopupTextArea ==
// Input for large strings, contains 2 widgets one for editing in the form
// and one for in the grid.
isc.ClassFactory.defineClass('OBTextAreaItem', isc.TextAreaItem);

isc.OBTextAreaItem.addProperties({
  operator: 'iContains',
  validateOnExit: true,

  selectOnFocus: false,
  rowSpan: 2,

  init: function () {
    if (this.initStyle) {
      this.initStyle();
    }
    this.Super('init', arguments);
  },

  isDisabled: function () {
    var ret = this.Super('isDisabled', arguments);
    if (ret && this.showDisabled) {
      this.readOnly = true;
      if (this.cellStyle.indexOf('Disabled') === -1 || (this.cellStyle.indexOf('Disabled') !== -1 && this.cellStyle.lastIndexOf('Disabled') !== this.cellStyle.length - 8)) {
        this.cellStyle = this.cellStyle + 'Disabled';
      }
      if (this.titleStyle.indexOf('Disabled') === -1 || (this.titleStyle.indexOf('Disabled') !== -1 && this.titleStyle.lastIndexOf('Disabled') !== this.titleStyle.length - 8)) {
        this.titleStyle = this.titleStyle + 'Disabled';
      }
      if (this.textBoxStyle.indexOf('Disabled') === -1 || (this.textBoxStyle.indexOf('Disabled') !== -1 && this.textBoxStyle.lastIndexOf('Disabled') !== this.textBoxStyle.length - 8)) {
        this.textBoxStyle = this.textBoxStyle + 'Disabled';
      }
    } else {
      this.readOnly = false;
      if (this.cellStyle.lastIndexOf('Disabled') === this.cellStyle.length - 8) {
        this.cellStyle = this.cellStyle.substring(0, this.cellStyle.length - 8);
      }
      if (this.titleStyle.lastIndexOf('Disabled') === this.titleStyle.length - 8) {
        this.titleStyle = this.titleStyle.substring(0, this.titleStyle.length - 8);
      }
      if (this.textBoxStyle.lastIndexOf('Disabled') === this.textBoxStyle.length - 8) {
        this.textBoxStyle = this.textBoxStyle.substring(0, this.textBoxStyle.length - 8);
      }
    }
    return false;
  },

  itemHoverHTML: function (item, form) {
    if (this.isDisabled()) {
      return this.getValue();
    }
  }
});

// used in the grid
isc.ClassFactory.defineClass('OBPopUpTextAreaItem', isc.PopUpTextAreaItem);

isc.OBPopUpTextAreaItem.addProperties({
  validateOnExit: true,
  canFocus: true,
  popUpOnEnter: true
});

// hack until this gets answered:
// http://forums.smartclient.com/showthread.php?p=61621#post61621
// to solve this: https://issues.openbravo.com/view.php?id=16327
if (isc.ListGrid.getPrototype()._popUpTextAreaEditorTypes) {
  // handle case when loading source code
  isc.ListGrid.getPrototype()._popUpTextAreaEditorTypes.OBPopUpTextAreaItem = true;
} else if (isc.ListGrid.getPrototype().$309) {
  isc.ListGrid.getPrototype().$309.OBPopUpTextAreaItem = true;
}