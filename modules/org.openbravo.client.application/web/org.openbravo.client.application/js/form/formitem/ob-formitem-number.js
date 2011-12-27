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

// = OBNumberIte =
// Contains the widgets for editing numeric values.
isc.ClassFactory.defineClass('OBNumberItem', isc.TextItem);

// = OBNumberItem =
// The Openbravo numeric form item.
isc.OBNumberItem.addProperties({
  operator: 'equals',
  typeInstance: null,
  
  keyPressFilterNumeric: '[0-9.,-=]',

  allowMath: true,
  
  validateOnExit: true,
  valueValidator: null,
  doBlurLogic: true,
  
  init: function(){
    this.setKeyPressFilter(this.keyPressFilterNumeric);
    this.typeInstance = isc.SimpleType.getType(this.type);
    return this.Super('init', arguments);
  },
  
  // after a change also store the textual value in the form
  // for precision, the textual value is sent to the server
  // which can be transferred to a bigdecimal there
  changed: function (form, item, value) {
    if (this.form.setTextualValue) {
      this.form.setTextualValue(this.name, this.getEnteredValue(), this.typeInstance);
    }
    this.Super('changed', arguments);
  },
  
  getMaskNumeric: function(){
    return this.typeInstance.maskNumeric;
  },
  
  getDecSeparator: function(){
    return this.typeInstance.decSeparator;
  },
  
  getGroupSeparator: function(){
    return this.typeInstance.groupSeparator;
  },
  
  getGlobalGroupInterval: function(){
    return OB.Format.defaultGroupingSize;
  },
  
  returnNewCaretPosition: function(numberStr, oldCaretPosition){
    var newCaretPosition = oldCaretPosition, i;
    for (i = oldCaretPosition; i > 0; i--) {
      if (numberStr.substring(i - 1, i) === this.getGroupSeparator()) {
        newCaretPosition = newCaretPosition - 1;
      }
    }
    return newCaretPosition;
  },
  
  // focus changes the formatted value to one without grouping
  focusNumberInput: function(){
    var oldCaretPosition = 0;
    if (this.getSelectionRange()) {
      oldCaretPosition = this.getSelectionRange()[0];
    }
    // getElementValue returns the current value string, so not the typed value
    var newCaretPosition = this.returnNewCaretPosition(this.getElementValue(), oldCaretPosition);
    // update the value shown, mapValueToDisplay will call the editFormatter
    
    // get the edit value, without grouping symbol.
    var editValue = OB.Utilities.Number.OBMaskedToOBPlain(this.getElementValue(), this.getDecSeparator(), this.getGroupSeparator());
    
    if (oldCaretPosition !== newCaretPosition || editValue !== this.getElementValue()) {
      this.setElementValue(editValue);
      this.setSelectionRange(newCaretPosition, newCaretPosition);
    }
  },
  
  replaceAt: function(string, what, ini, end){
    if (typeof end === 'undefined' || end === null || end === 'null' ||
    end === '') {
      end = ini;
    }
    if (ini > end) {
      var temp = ini;
      ini = end;
      end = temp;
    }
    var newString = '';
    newString = string.substring(0, ini) + what +
    string.substring(end + 1, string.length);
    return newString;
  },
  
  // handles the decimal point of the numeric keyboard
  manageDecPoint: function(keyCode){
    var decSeparator = this.getDecSeparator();
    
    if (decSeparator === '.') {
      return true;
    }
    
    var caretPosition = 0;
    if (this.getSelectionRange()) {
      caretPosition = this.getSelectionRange()[0];
    }
    /*
     * if(keyCode>=65 && keyCode<=90) { setTimeout(function() {obj.value =
     * replaceAt(obj.value, '', caretPosition); setCaretToPos(obj,
     * caretPosition);},5); }
     */
    var inpMaxlength = this.length;
    var inpLength = this.getElementValue().length;
    var isInpMaxLength = false;
    if (inpMaxlength === null) {
      isInpMaxLength = false;
    } else if (inpLength >= inpMaxlength) {
      isInpMaxLength = true;
    }
    
    if (navigator.userAgent.toUpperCase().indexOf('OPERA') !== -1 &&
    keyCode === 78) {
      keyCode = 110;
    }
    
    var obj = this;
    if (keyCode === 110) {
      setTimeout(function(){
        var newValue = obj.replaceAt(obj.getElementValue(), decSeparator, caretPosition);
        obj.setElementValue(newValue);
        obj.setSelectionRange(caretPosition + 1, caretPosition + 1);
      }, 5);
    }
    return true;
  },

  manageEqualSymbol: function() {
    var obj = this;
    var caretPosition = 0;
    if (this.getSelectionRange()) {
      caretPosition = obj.getSelectionRange()[0];
    }
    setTimeout(function(){
      // can happen when a dynamic form has already been removed
      if (!obj.getElementValue()) {
        return;
      }
      var inputValue = obj.getElementValue().toString();
      var checkA = false; // Checks if there is a = in the beginning
      var checkB = false; // Checks if any undesired = is/has to be removed from the inputValue

      if (inputValue.indexOf('=') === 0) {
        checkA = true;
      }
      if (obj.allowMath) {
        while (inputValue.indexOf('=',1) !== -1) {
          checkB = true;
          if (checkA) {
            inputValue = inputValue.substring(1, inputValue.length);
          }
          inputValue = inputValue.replace('=', '');
          if (checkA) {
            inputValue = '=' + inputValue;
          }
        }
      } else {
        while (inputValue.indexOf('=') !== -1) {
          checkB = true;
          inputValue = inputValue.replace('=', '');
        }
      }

      if (checkA && obj.allowMath) {
        obj.setKeyPressFilter('');
      } else {
        obj.setKeyPressFilter(obj.keyPressFilterNumeric);
      }

      if (checkB) {
        obj.setElementValue(inputValue);
        obj.setSelectionRange(caretPosition, caretPosition);
      }
    }, 5);
  },
  
  keyDown: function(item, form, keyName){
    this.keyDownAction(item, form, keyName);
  },

  keyDownAction: function(item, form, keyName){
    var keyCode = isc.EventHandler.lastEvent.nativeKeyCode;
    this.manageEqualSymbol();
    this.manageDecPoint(keyCode);
  },
  
  validateOBNumberItem: function(){
    var value = this.getElementValue();
    var isValid = this.valueValidator.condition(this, this.form, value);
    var isRequired = this.required;
    if (isValid === false) {
      this.form.setFieldErrors(this.name, isc.OBDateItem.invalidValueLabel, false);
      this.form.markForRedraw();
      return false;
    } else if (isRequired === true &&
    (value === null || value === '' || typeof value === 'undefined')) {
      this.form.setFieldErrors(this.name, isc.OBDateItem.requiredValueLabel, false);
      this.form.markForRedraw();
      return false;
    } else {
      this.form.clearFieldErrors(this.name, false);
      this.form.markForRedraw();
    }
    return true;
  },
  
  focus: function(form, item){
    if (!this.getErrors()) {
      // only do the focus/reformat if no errors
      this.focusNumberInput();
    }
    return this.Super('focus', arguments);
  },

  checkMathExpression: function(expression) {
    var jsExpression = expression;
    var dummy = 'xyxdummyxyx';

    function replaceAll(text, what, byWhat) {
      while (text.toString().indexOf(what) !== -1) {
        text = text.toString().replace(what, dummy);
      }
      while (text.toString().indexOf(dummy) !== -1) {
        text = text.toString().replace(dummy, byWhat);
      }
      return text;
    }
    jsExpression = jsExpression.substring(1, jsExpression.length);

    jsExpression = replaceAll(jsExpression, '.', '');
    jsExpression = replaceAll(jsExpression, ',', '');
    jsExpression = replaceAll(jsExpression, ';', '');
    jsExpression = replaceAll(jsExpression, '(', '');
    jsExpression = replaceAll(jsExpression, ')', '');
    jsExpression = replaceAll(jsExpression, ' ', '');

    jsExpression = replaceAll(jsExpression, '0', '');
    jsExpression = replaceAll(jsExpression, '1', '');
    jsExpression = replaceAll(jsExpression, '2', '');
    jsExpression = replaceAll(jsExpression, '3', '');
    jsExpression = replaceAll(jsExpression, '4', '');
    jsExpression = replaceAll(jsExpression, '5', '');
    jsExpression = replaceAll(jsExpression, '6', '');
    jsExpression = replaceAll(jsExpression, '7', '');
    jsExpression = replaceAll(jsExpression, '8', '');
    jsExpression = replaceAll(jsExpression, '9', '');

    jsExpression = replaceAll(jsExpression, '+', '');
    jsExpression = replaceAll(jsExpression, '-', '');
    jsExpression = replaceAll(jsExpression, '*', '');
    jsExpression = replaceAll(jsExpression, '/', '');
    jsExpression = replaceAll(jsExpression, '%', '');

    jsExpression = replaceAll(jsExpression, 'E', '');
    jsExpression = replaceAll(jsExpression, 'LN2', '');
    jsExpression = replaceAll(jsExpression, 'LN10', '');
    jsExpression = replaceAll(jsExpression, 'LOG2E', '');
    jsExpression = replaceAll(jsExpression, 'LOG10E', '');
    jsExpression = replaceAll(jsExpression, 'PI', '');
    jsExpression = replaceAll(jsExpression, 'SQRT1_2', '');
    jsExpression = replaceAll(jsExpression, 'SQRT2', '');

    jsExpression = replaceAll(jsExpression, 'abs', '');
    jsExpression = replaceAll(jsExpression, 'acos', '');
    jsExpression = replaceAll(jsExpression, 'asin', '');
    jsExpression = replaceAll(jsExpression, 'atan', '');
    jsExpression = replaceAll(jsExpression, 'atan2', '');
    jsExpression = replaceAll(jsExpression, 'ceil', '');
    jsExpression = replaceAll(jsExpression, 'cos', '');
    jsExpression = replaceAll(jsExpression, 'exp', '');
    jsExpression = replaceAll(jsExpression, 'floor', '');
    jsExpression = replaceAll(jsExpression, 'log', '');
    jsExpression = replaceAll(jsExpression, 'max', '');
    jsExpression = replaceAll(jsExpression, 'min', '');
    jsExpression = replaceAll(jsExpression, 'pow', '');
    jsExpression = replaceAll(jsExpression, 'random', '');
    jsExpression = replaceAll(jsExpression, 'round', '');
    jsExpression = replaceAll(jsExpression, 'sin', '');
    jsExpression = replaceAll(jsExpression, 'sqrt', '');
    jsExpression = replaceAll(jsExpression, 'tan', '');

    if (jsExpression === '') {
      return true;
    } else {
      return false;
    }
  },

  // ** {{{ evalMathExpression }}} **
  // evalMathExpression allows you to perform mathematical tasks.
  //
  // All operations can be done by using the symbol = at the beginning of the numeric input
  //
  // Syntax examples:
  // =PI // Returns 3.14159
  // =1+2+3 // Returns 6
  // =sqrt(16) // Returns 4
  //
  // Binary operations:
  // a + b             Add a and b
  // a - b             Subtract b from a
  // a * b             Multiply a by b
  // a / b             Divide a by b
  // a % b             Find the remainder of division of a by b
  //
  // Constants:
  // E                 Returns Euler's number (approx. 2.718)
  // LN2               Returns the natural logarithm of 2 (approx. 0.693)
  // LN10              Returns the natural logarithm of 10 (approx. 2.302)
  // LOG2E             Returns the base-2 logarithm of E (approx. 1.442)
  // LOG10E            Returns the base-10 logarithm of E (approx. 0.434)
  // PI                Returns PI (approx. 3.14159)
  // SQRT1_2           Returns the square root of 1/2 (approx. 0.707)
  // SQRT2             Returns the square root of 2 (approx. 1.414)
  //
  // Operator functions
  // abs(x)            Returns the absolute value of x
  // acos(x)           Returns the arccosine of x, in radians
  // asin(x)           Returns the arcsine of x, in radians
  // atan(x)           Returns the arctangent of x as a numeric value between -PI/2 and PI/2 radians
  // atan2(y;x)        Returns the arctangent of the quotient of its arguments
  // ceil(x)           Returns x, rounded upwards to the nearest integer
  // cos(x)            Returns the cosine of x (x is in radians)
  // exp(x)            Returns the value of Ex
  // floor(x)          Returns x, rounded downwards to the nearest integer
  // log(x)            Returns the natural logarithm (base E) of x
  // max(x;y;z;...;n)  Returns the number with the highest value
  // min(x;y;z;...;n)  Returns the number with the lowest value
  // pow(x;y)          Returns the value of x to the power of y
  // random()          Returns a random number between 0 and 1
  // round(x)          Rounds x to the nearest integer
  // sin(x)            Returns the sine of x (x is in radians)
  // sqrt(x)           Returns the square root of x
  // tan(x)            Returns the tangent of an angle
  evalMathExpression: function(expression) {
    if (!this.checkMathExpression(expression)) {
      return 'error';
    }
    var jsExpression = expression;
    var dummy = 'xyxdummyxyx';
    var result;
    var decSeparator = this.getDecSeparator();
    var groupSeparator = this.getGroupSeparator();
    function replaceAll(text, what, byWhat) {
      while (text.toString().indexOf(what) !== -1) {
        text = text.toString().replace(what, dummy);
      }
      while (text.toString().indexOf(dummy) !== -1) {
        text = text.toString().replace(dummy, byWhat);
      }
      return text;
    }
    jsExpression = jsExpression.substring(1, jsExpression.length);

    jsExpression = replaceAll(jsExpression, groupSeparator, '');
    jsExpression = replaceAll(jsExpression, decSeparator, '.');
    jsExpression = replaceAll(jsExpression, ';', ',');

    jsExpression = replaceAll(jsExpression, 'E', 'Math.E');
    jsExpression = replaceAll(jsExpression, 'LN2', 'Math.LN2');
    jsExpression = replaceAll(jsExpression, 'LN10', 'Math.LN10');
    jsExpression = replaceAll(jsExpression, 'LOG2E', 'Math.LOG2E');
    jsExpression = replaceAll(jsExpression, 'LOG10E', 'Math.LOG10E');
    jsExpression = replaceAll(jsExpression, 'PI', 'Math.PI');
    jsExpression = replaceAll(jsExpression, 'SQRT1_2', 'Math.SQRT1_2');
    jsExpression = replaceAll(jsExpression, 'SQRT2', 'Math.SQRT2');

    jsExpression = replaceAll(jsExpression, 'abs', 'Math.abs');
    jsExpression = replaceAll(jsExpression, 'acos', 'Math.acos');
    jsExpression = replaceAll(jsExpression, 'asin', 'Math.asin');
    jsExpression = replaceAll(jsExpression, 'atan', 'Math.atan');
    jsExpression = replaceAll(jsExpression, 'atan2', 'Math.atan2');
    jsExpression = replaceAll(jsExpression, 'ceil', 'Math.ceil');
    jsExpression = replaceAll(jsExpression, 'cos', 'Math.cos');
    jsExpression = replaceAll(jsExpression, 'exp', 'Math.exp');
    jsExpression = replaceAll(jsExpression, 'floor', 'Math.floor');
    jsExpression = replaceAll(jsExpression, 'log', 'Math.log');
    jsExpression = replaceAll(jsExpression, 'max', 'Math.max');
    jsExpression = replaceAll(jsExpression, 'min', 'Math.min');
    jsExpression = replaceAll(jsExpression, 'pow', 'Math.pow');
    jsExpression = replaceAll(jsExpression, 'random', 'Math.random');
    jsExpression = replaceAll(jsExpression, 'round', 'Math.round');
    jsExpression = replaceAll(jsExpression, 'sin', 'Math.sin');
    jsExpression = replaceAll(jsExpression, 'sqrt', 'Math.sqrt');
    jsExpression = replaceAll(jsExpression, 'tan', 'Math.tan');

    try {
      result = eval(jsExpression);
      if (isNaN(result)) {
        result = 'error';
      }
    } catch (e) {
      result = 'error';
    }

    //result = replaceAll(result, '.', decSeparator);
    return result;
  },
  
  blur: function(){
    var value;
    
    if (this.form && this.form._isRedrawing) {
      return;
    }

    // prevent validation/blurhandling when we are showing the editor and moving
    // the focus around
    if (this.form && this.form.grid && this.form.grid._showingEditor) {
      return;
    }

    if (this.doBlurLogic) {
      this.validate();  

      value = this.getValue();
      
      // first check if the number is valid
      if (!isc.isA.String(value)) {
        // format the value displayed
        this.setElementValue(this.mapValueToDisplay(value));
      }
    }
    return this.Super('blur', arguments);
  }
});

// Use our custom validator for float and integers
isc.OBNumberItem.validateCondition = function(item, validator, value){
  var undef, ret, type;
  
  if (!item.typeInstance) {
    // this happens when data is validated which is returned from the system
    // and added to the grid
    return true;
  }
  
  if (value === null || value === undef) {
    return true;
  }
  
  if (item.allowMath && isc.isA.String(value) && value.indexOf('=') === 0) {
    value = String('') + item.evalMathExpression(value);
  }
  
  type = item.typeInstance;
  validator.resultingValue = null;

  // return a formatted value, if it was valid
  if (isc.isA.String(value)) {
    if (OB.Utilities.Number.IsValidValueString(type, value)) {
      validator.resultingValue = OB.Utilities.Number.OBMaskedToJS(value, type.decSeparator, type.groupSeparator);
      item.storeValue(validator.resultingValue);
      if (item.form && item.form.setTextualValue) {
        item.form.setTextualValue(item.name, value, item.typeInstance);
      }
      return true;
    } else {
      // don't loose illegal values
      validator.resultingValue = item.getElementValue();
      return false;
    }
  } else if (isc.isA.Number(value)) {
    return true;
  }
  // don't loose illegal values
  validator.resultingValue = item.getElementValue();
  return false;
};

isc.Validator.addValidator('isFloat', isc.OBNumberItem.validateCondition);
isc.Validator.addValidator('isInteger', isc.OBNumberItem.validateCondition);

isc.ClassFactory.defineClass('OBNumberFilterItem', isc.OBNumberItem);

isc.OBNumberFilterItem.addProperties({
  allowExpressions: true,
  validateOnExit: false,
  validateOnChange: false,
  keyPressFilterNumeric: '[0-9.,-=<>!#orand ]',
  doBlurLogic: false,
  operator: 'equals',
  validOperators: ['equals', 'lessThan', 'greaterThan', 'notEqual',
                   'lessThan', 'lessThanOrEqual', 'greaterThanOrEqual',
                   'between', 'betweenInclusive', 'isNull', 'isNotNull'
                   ],
  
  // prevent handling of equal symbol in filteritem
  keyDownAction: function(item, form, keyName){
    var keyCode = isc.EventHandler.lastEvent.nativeKeyCode;
    this.manageDecPoint(keyCode);
  },

  parseValueExpressions: function() {
    var ret = this.Super('parseValueExpressions', arguments);
    if (ret && ret.start) {
      ret.start = this.convertToTypedValue(ret.start);
    } 
    
    if (ret && ret.end) {
      ret.end = this.convertToTypedValue(ret.end);
    }
    
    if (ret && ret.value) {
      ret.value = this.convertToTypedValue(ret.value);
    }
    
    return ret;
  },
  
  buildValueExpressions: function(criterion) {
    var i = 0, criteria, length;
    if (criterion && !criterion.criteria) {
      criterion = { criteria: [criterion] };
    }
    if (criterion.criteria) {
      criterion = isc.clone(criterion);
      length = criterion.criteria.length;
      for (i = 0; i < length; i++) {
        criteria = criterion.criteria[i];
        if (criteria.operator === 'iNotEqual') {
          criteria.operator = 'notEqual';
        }
        if (criteria.start) {
          criteria.start = this.convertToStringValue(criteria.start);
        }
        if (criteria.end) {
          criteria.end = this.convertToStringValue(criteria.end);
        }
        if (criteria.value) {
          criteria.value = this.convertToStringValue(criteria.value);
        }
      }
    }
    var ret = this.Super('buildValueExpressions', [criterion]);
    if (isc.isA.String(ret) && ret.contains('undefined')) {
      return ret.replace('undefined', '');
    }
    return ret;
  },

  convertToStringValue: function(value) {
    var type = this.typeInstance;
    if (!isc.isA.String(value)) {
      // on purpose no grouping symbol
      return OB.Utilities.Number.JSToOBMasked(value, type.maskNumeric, type.decSeparator, null, type.groupInterval);
    }
    return value;
  },
  
  focusNumberInput: function() {
  },
  
  convertToTypedValue: function(value) {
    if (isc.isA.String(value) && OB.Utilities.Number.IsValidValueString(this.typeInstance, value)) {
      return OB.Utilities.Number.OBMaskedToJS(value, this.typeInstance.decSeparator, this.typeInstance.groupSeparator);
    }
    return value;
  }
});
