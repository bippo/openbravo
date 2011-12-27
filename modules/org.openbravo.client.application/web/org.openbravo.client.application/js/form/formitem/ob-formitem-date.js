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
 * All portions are Copyright (C) 2011 Openbravo SLU
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
  
  dateParts : [],

  doInit: function() {
    var i, dateFormatUpper, index = 0, 
      length, currentTime;
    
    dateFormatUpper = this.dateFormat.toUpperCase();
    length = dateFormatUpper.length;
    this.dateSeparator = this.dateFormat.toUpperCase().replace(/D/g, '')
        .replace(/M/g, '').replace(/Y/g, '').substr(0, 1);
    
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
    
    if (this.showDisabled === false) {
      this.textField.showDisabled = false;
    }
  },

  // compare while ignoring milli difference
  compareValues: function (value1, value2) {
    return (0 === isc.Date.compareLogicalDates(value1, value2));
  },
  
  parseValue: function() {
    var i, str = this.blurValue(), 
      length = str.length, 
      parts = [ '', '', '' ], partIndex = 0, result;
    if (!str || isc.isA.Date(str) || str.replace(/0/g, '') === '') {
      return str;
    }
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
    return parts[0] + this.dateSeparator + parts[1] + this.dateSeparator
        + parts[2];
  },

  expandPart : function(part, index) {
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

  reachedLength : function(part, index) {
    var maxLength;
    if (this.dateParts[index] === 'D' || this.dateParts[index] === 'M') {
      maxLength = 2;
    } else {
      maxLength = 4;
    }
    return part.length >= maxLength;
  },

  isNumber : function(str, position) {
    return str.charAt(position) >= '0' && str.charAt(position) <= '9';
  },

  isSeparator : function(str, position) {
    return str.charAt(position) === '-' || str.charAt(position) === '\\'
        || str.charAt(position) === '/';
  },
  
  pickerDataChanged: function(picker) {
    this.Super('pickerDataChanged', arguments);
    if (this.form.focusInNextItem) {
      this.form.focusInNextItem(this.name);
    }
  }
};

isc.OBDateItem.addProperties(OB.DateItemProperties, {
  validateOnExit: true,
  
  init: function() {
    // this call super.init
    this.doInit();
  },
   
  expandValue: function() {
    var newValue = this.parseValue(), oldValue = this.blurValue();
    
    if (oldValue !== newValue) {
      this.dateTextField.setValue(newValue);
    }
  },
  
  // update the value in update value as this is called from cellEditEnd in the
  // grid
  updateValue: function() {
    this.expandValue();
    this.Super('updateValue', arguments);
  },
  
  blurValue: function() {
    return this.dateTextField.getElementValue();
  },
  
  validateOBDateItem: function(value){
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
    condition: function(item, validator, value){
      return item.validateOBDateItem(value);
    }
  }]
});

OB.I18N.getLabel('OBUIAPP_InvalidValue', null, isc.OBDateItem, 'invalidValueLabel');
OB.I18N.getLabel('OBUISC_Validator.requiredField', null, isc.OBDateItem, 'requiredValueLabel');
