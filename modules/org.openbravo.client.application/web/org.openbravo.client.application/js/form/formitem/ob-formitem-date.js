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

// == OBDateItem ==
// OBDateItem inherits from SmartClient DateItem
// adds autocomplete and formatting based on the Openbravo date pattern
isc.ClassFactory.defineClass('OBDateItem', isc.DateItem);

// done like this because the props are re-used in the minidaterange
OB.DateItemProperties = {
  operator: 'equals',
  // ** {{{ pickerConstructor }}} **
  // Picker constructor class
  pickerConstructor: 'OBDateChooser',
  useSharedPicker: true,

  // ** {{{ dateFormat }}} **
  // Dateformat function
  dateFormat: OB.Format.date,

  // ** {{{ useTextField }}} **
  // use text field for date entry
  useTextField: true,

  // ** {{{ changeOnKeypress }}} **
  // Fire change event on key press.
  changeOnKeypress: false,

  // is done by the blur event defined here
  validateOnExit: false,
  validateOnChange: false,
  stopOnError: false,

  textAlign: 'left',

  dateParts: [],

  doInit: function () {
    var i, dateFormatUpper, index = 0,
        length, currentTime;

    dateFormatUpper = this.dateFormat.toUpperCase();
    length = dateFormatUpper.length;
    this.dateSeparator = this.dateFormat.toUpperCase().replace(/D/g, '').replace(/M/g, '').replace(/Y/g, '').substr(0, 1);

    for (i = 0; i < length; i++) {
      if (this.isSeparator(dateFormatUpper, i)) {
        index++;
      } else {
        this.dateParts[index] = dateFormatUpper.charAt(i);
      }
    }
    currentTime = new Date();
    this.currentMonth = String(currentTime.getMonth() + 1);
    if (this.currentMonth.length === 1) {
      this.currentMonth = '0' + this.currentMonth;
    }
    this.currentDay = String(currentTime.getDate());
    if (this.currentDay.length === 1) {
      this.currentDay = '0' + this.currentDay;
    }
    this.currentYear = String(currentTime.getFullYear());

    this.Super('init', arguments);

    if (this.textField) {
      this.textField.changed = function () {
        // when the textfield of the date is updated, the date
        // field should be flagged as changed
        // see issue 20071 (https://issues.openbravo.com/view.php?id=20071)
        this._textChanged = true;
        this.parentItem._hasChanged = true;
        // There is a mechanism to prevent infinite looping in number fields 
        // (see issue https://issues.openbravo.com/view.php?id=17290) that
        // interferes with the correct behaviour of the date fields 
        // The infinite looping described in the issue does not apply to date fields, 
        // so it is safe to delete the saveFocusItemChanged flag when a date is modified
        if (this.parentItem.form && this.parentItem.form.view && this.parentItem.form.view.viewForm) {
          delete this.parentItem.form.view.viewForm.saveFocusItemChanged;
        }
      };
      // This is needed for the unit tests to be able to enter the dates using the setValue method
      this.dateTextField.setValue = function (newValue) {
        var oldValue = this.getValue();
        this.Super('setValue', newValue);
        // only flag the date as changed if it had a value, and it
        // has been actually changed
        if (!newValue || !oldValue || (oldValue === newValue)) {
          return;
        }
        this.parentItem.textField._textChanged = true;
        this.parentItem._hasChanged = true;
        if (this.parentItem.form && this.parentItem.form.view && this.parentItem.form.view.viewForm) {
          delete this.parentItem.form.view.viewForm.saveFocusItemChanged;
        }
      };
    }

    if (this.showDisabled === false) {
      this.textField.showDisabled = false;
    }
  },

  // compare while ignoring milli difference
  compareValues: function (value1, value2) {
    return (0 === isc.Date.compareLogicalDates(value1, value2));
  },

  parseValue: function () {
    var i, str = this.blurValue(),
        length, parts = ['', '', ''],
        partIndex = 0,
        result;

    if (!str || isc.isA.Date(str) || str.replace(/0/g, '') === '') {
      return str;
    }
    length = str.length;
    for (i = 0; i < length; i++) {
      if (this.isNumber(str, i)) {
        if (this.reachedLength(parts[partIndex], partIndex)) {
          partIndex++;
        }
        if (partIndex === 3) {
          break;
        }
        parts[partIndex] = parts[partIndex] + str.charAt(i);
      } else if (this.isSeparator(str, i)) {
        partIndex++;
      } else {
        // invalid date
        return str;
      }
      if (partIndex === 3) {
        break;
      }
    }
    for (i = 0; i < 3; i++) {
      if ((parts[i] === '0' || parts[i] === '00') && (this.dateParts[i] === 'D' || this.dateParts[i] === 'M')) {
        return str;
      } else {
        parts[i] = this.expandPart(parts[i], i);
      }
    }
    return parts[0] + this.dateSeparator + parts[1] + this.dateSeparator + parts[2];
  },

  expandPart: function (part, index) {
    var year;
    if (this.reachedLength(part, index)) {
      return part;
    }
    if (part === '') {
      if (this.dateParts[index] === 'D') {
        return this.currentDay;
      } else if (this.dateParts[index] === 'M') {
        return this.currentMonth;
      } else {
        return this.currentYear;
      }
    } else if (this.dateParts[index] === 'Y') {
      year = parseInt(part, 10);
      if (year <= 50) {
        return String(2000 + year);
      } else if (year < 100) {
        return String(1900 + year);
      } else {
        return '2' + part;
      }
    } else if (part.length === 1) {
      return '0' + part;
    }
    return part;
  },

  reachedLength: function (part, index) {
    var maxLength;
    if (this.dateParts[index] === 'D' || this.dateParts[index] === 'M') {
      maxLength = 2;
    } else {
      maxLength = 4;
    }
    return part.length >= maxLength;
  },

  isNumber: function (str, position) {
    return str.charAt(position) >= '0' && str.charAt(position) <= '9';
  },

  isSeparator: function (str, position) {
    return str.charAt(position) === '-' || str.charAt(position) === '\\' || str.charAt(position) === '/';
  },

  pickerDataChanged: function (picker) {
    this.Super('pickerDataChanged', arguments);
    // update the date field after picking a new date 
    this.textField._textChanged = true;
    this.updateValue();
    if (this.form.focusInNextItem) {
      if (this.form.handleItemChange) {
        this._hasChanged = true;
        this.form.handleItemChange(this);
      }

      this.form.focusInNextItem(this.name);
    }
  }
};

isc.OBDateItem.addProperties(OB.DateItemProperties, {
  validateOnExit: true,

  init: function () {
    // this call super.init
    this.doInit();
  },

  expandValue: function () {
    var newValue = this.parseValue(),
        oldValue = this.blurValue();

    if (oldValue !== newValue) {
      this.dateTextField.setValue(newValue);
    }
  },

  // update the value in update value as this is called from cellEditEnd in the
  // grid, after losing the focus on the form and when autosaving
  updateValue: function () {
    if (this.grid && this.grid._preventDateParsing && !this.grid._autoSaving) {
      return;
    }
    if (this.textField._textChanged) {
      this.expandValue();
      this.Super('updateValue', arguments);
      //  when the date field has a callout and all the mandatory fields have been entered, 
      //  the grid does not save the value before making the FIC call, so the value has to 
      //  be saved explicitly
      //  See issue 19694 (https://issues.openbravo.com/view.php?id=19694)
      if (this.grid && this.grid.getEditRow()) {
        this.grid.getEditValues(this.grid.getEditRow())[this.name] = this.getValue();
      }
      this.textField._textChanged = false;
    }
  },

  blur: function () {
    // force the update of the date when its field loses the focus
    // it has to be done before the call to the super because the
    // date should be updated before calling handleItemChange, 
    // which is called in the super blur  
    this.updateValue();
    this.Super('blur', arguments);
  },

  blurValue: function () {
    return this.dateTextField.getElementValue();
  },

  validateOBDateItem: function (value) {
    var dateValue = OB.Utilities.Date.OBToJS(value, this.dateFormat);
    var isValid = true;
    if (this.getValue() && dateValue === null) {
      isValid = false;
    }
    var isRequired = this.required;
    if (isValid === false) {
      return false;
    } else if (isRequired === true && value === null) {
      return false;
    }
    return true;
  },

  validators: [{
    type: 'custom',
    condition: function (item, validator, value) {
      return item.validateOBDateItem(value);
    }
  }]
});

OB.I18N.getLabel('OBUIAPP_InvalidValue', null, isc.OBDateItem, 'invalidValueLabel');
OB.I18N.getLabel('OBUISC_Validator.requiredField', null, isc.OBDateItem, 'requiredValueLabel');