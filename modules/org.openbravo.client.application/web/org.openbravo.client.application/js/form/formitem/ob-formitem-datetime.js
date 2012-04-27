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

// = OBDateTimeItem =
// Contains the widget for editing Date Time, it works differently than the OBDateItem
// in that it auto-completes while typing. A TODO: make this widget work the same as the 
// OBDateItem, autocomplete when blurring.
isc.ClassFactory.defineClass('OBDateTimeItem', isc.OBDateItem);

isc.OBDateTimeItem.addClassProperties({

  // ** {{{ autoCompleteData }}} **
  //
  // Autocomplets the date entered.
  // Parameters:
  // * {{{dateFormat}}}: the dateFormat in OB format
  // * {{{value}}}: the current entered value
  autoCompleteDate: function (dateFormat, value, item) {
    var fmt;

    // if (!isTabPressed) {
    if (value === null) {
      return value;
    }
    fmt = OB.Utilities.Date.normalizeDisplayFormat(dateFormat);
    try {
      if (item.getSelectionRange() && item.getSelectionRange()[0] !== value.length) {
        // If we are inserting in a position different from  the last one, we don't autocomplete
        return value;
      }
    } catch (ignored) {}
    var strDate = value;
    var b = fmt.match(/%./g);
    var i = 0,
        j = -1;
    var text = '';
    var length = 0;
    var pos = fmt.indexOf(b[0]) + b[0].length;
    var separator = fmt.substring(pos, pos + 1);
    var separatorH = '';
    pos = fmt.indexOf('%H');
    if (pos !== -1) {
      separatorH = fmt.substring(pos + 2, pos + 3);
    }
    while (strDate.charAt(i)) {
      if (strDate.charAt(i) === separator || strDate.charAt(i) === separatorH || strDate.charAt(i) === ' ') {
        i++;
        continue;
      }
      if (length <= 0) {
        j++;
        if (j > 0) {
          if (b[j] === '%H') {
            text += ' ';
          } else if (b[j] === '%M' || b[j] === '%S') {
            text += separatorH;
          } else {
            text += separator;
          }
        }
        switch (b[j]) {
        case '%d':
        case '%e':
          text += strDate.charAt(i);
          length = 2;
          break;
        case '%m':
          text += strDate.charAt(i);
          length = 2;
          break;
        case '%Y':
          text += strDate.charAt(i);
          length = 4;
          break;
        case '%y':
          text += strDate.charAt(i);
          length = 2;
          break;
        case '%H':
        case '%I':
        case '%k':
        case '%l':
          text += strDate.charAt(i);
          length = 2;
          break;
        case '%M':
          text += strDate.charAt(i);
          length = 2;
          break;
        case '%S':
          text += strDate.charAt(i);
          length = 2;
          break;
        }
      } else {
        text += strDate.charAt(i);
      }
      length--;
      i++;
    }
    return text;
    // IE doesn't detect the onchange event if text value is modified
    // programatically, so it's here called
    // if (i > 7 && (typeof (field.onchange)!='undefined'))
    // field.onchange();
    // }
  }
});

// == OBDateItem properties ==
isc.OBDateTimeItem.addProperties({
  showTime: true,

  blurValue: function () {
    var value = OB.Utilities.Date.OBToJS(this.dateTextField.getElementValue(), OB.Format.dateTime);
    this.setValue(value);
    return value;
  },

  parseValue: function () {
    return this.dateTextField.getElementValue();
  },

  // ** {{{ change }}} **
  // Called when changing a value.
  change: function (form, item, value, oldValue) { /* transformInput */
    var isADate = value !== null && Object.prototype.toString.call(value) === '[object Date]';
    if (isADate) {
      return;
    }
    // prevent change events from happening
    var completedDate = isc.OBDateTimeItem.autoCompleteDate(item.dateFormat, value, this);
    if (completedDate !== oldValue) {
      item.setValue(completedDate);
    }
  }
});