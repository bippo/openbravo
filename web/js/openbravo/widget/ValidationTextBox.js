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
 * All portions are Copyright (C) 2001-2008 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

dojo.provide("openbravo.widget.ValidationTextBox");

dojo.require("dijit.form.TextBox");

dojo.declare("openbravo.widget.ValidationTextBox", [dijit.form.TextBox], {
  templateString: "",
  templatePath: dojo.moduleUrl("openbravo","templates/ValidationTextBox.html"),

  baseClass: "",
  receivedClass: "",
  value: "",

  invalidSpan: null,
  missingSpan: null,
  rangeSpan: null,

  invalidClass: "invalid",
  missingClass: "missing",
  rangeClass: "range",

  listenOnKeyPress: true,
  lastCheckedValue: null,
  htmlfloat: "none",

  classPrefix: "dojoValidate",

  required: false,

  promptMessage: "",

  invalidMessage: "$_unset_$",
  missingMessage: "$_unset_$",
  rangeMessage: "$_unset_$",

  constraints: {},

  regExp: ".*",

  regExpGen: function(constraints){ return this.regExp; },

  validator: function(value, constraints) {
    return (new RegExp("^(" + this.regExpGen(constraints) + ")"+(this.required?"":"?")+"$")).test(value) &&
      (!this.required || !this.isEmpty(value)) &&
      (this.isEmpty(value) || this.parse(value, constraints) !== undefined); // Boolean
  },

  isValid: function() {
    // summary: Need to over-ride with your own validation code in subclasses
    return this.validator(this.textbox.value, this.constraints);
  },

  isInRange: function() {
    // summary: Need to over-ride with your own validation code in subclasses
    return true;
  },

  isEmpty: function() {
    // summary: Checks for whitespace
    return ( /^\s*$/.test(this.textbox.value) ); // Boolean
  },

  isMissing: function() {
    // summary: Checks to see if value is required and is whitespace
    return ( this.required && this.isEmpty() ); // Boolean
  },


  update: function(isFocused) {
    // summary:
    //		Called by oninit, onblur, and onkeypress.
    // description:
    //		Show missing or invalid messages if appropriate, and highlight textbox field.
    this.lastCheckedValue = this.textbox.value;

    this.missingSpan.style.display = "none";
    this.invalidSpan.style.display = "none";
    this.rangeSpan.style.display = "none";

    var empty = this.isEmpty();
    var valid = this.isValid();
    var missing = this.isMissing();

    //alert(empty + " " + valid + " " + missing);

    // Display at most one error message
    if (missing) {
      this.missingSpan.style.display = "";
      this.updateClass("Empty");
    } else if (!empty && !valid) {
      this.invalidSpan.style.display = "";
      this.updateClass("Invalid");
    } else if (!empty && !this.isInRange()) {
      this.rangeSpan.style.display = "";
      this.updateClass("Invalid");
    } else {
      this.updateClass("Valid");
    }
  },
  
  //Needed to overide default dojo focused, hover, ... states css
  _setStateClass: function() {
  },

  updateClass: function(className) {
    // summary: used to ensure that only 1 validation class is set at a time
    var pre = this.classPrefix;
    if (focusedWindowElement == this.textbox) {
      dojo.removeClass(this.textbox,pre+"Empty_focus");
      dojo.removeClass(this.textbox,pre+"Valid_focus");
      dojo.removeClass(this.textbox,pre+"Invalid_focus");
      dojo.addClass(this.textbox,pre+className+"_focus");
    } else {
      dojo.removeClass(this.textbox,pre+"Empty");
      dojo.removeClass(this.textbox,pre+"Valid");
      dojo.removeClass(this.textbox,pre+"Invalid");
      dojo.addClass(this.textbox,pre+className);
    }
  },

  highlight: function() {
    // summary: by Called oninit, and onblur.
    // highlight textbox background 
    if (this.isEmpty()) {
      this.updateClass("Empty");
    } else if (this.isValid() && this.isInRange()) {
      this.updateClass("Valid");
    } else if (this.textbox.value != this.promptMessage) {
      this.updateClass("Invalid");
    } else {
      this.updateClass("Empty");
    }
  },

  _onKeyUp: function(evt){
    if(this.listenOnKeyPress) {
      //this.filter();  trim is problem if you have to type two words
      this.update(); 
    } else if (this.textbox.value != this.lastCheckedValue) {
      this.updateClass("Empty");
    }
  },

  _onFocus: function(evt) {
    if (!this.listenOnKeyPress) {
      this.updateClass("Empty");
//    this.textbox.style.backgroundColor = "";
    }
  },

  _onBlur: function(evt) {
    this.inherited(arguments);
    this.filter();
    this.update(); 
  },

  getMessage: function(index, _language) {
    if (_language==null){
      if (typeof defaultLang != "undefined") {
        _language = defaultLang;
      } else if (typeof LNG_POR_DEFECTO != "undefined") {
        // Deprecated in 2.50, only for compatibility
        _language = LNG_POR_DEFECTO;
      }
    }
    if (typeof arrMessages != "undefined") {
      var total = arrMessages.length;
      for (var i=0;i<total;i++){
        if (arrMessages[i].language == _language){
          if (arrMessages[i].message == index){
            if (index == "Invalid" && this.invalidMessage == "$_unset_$") { return arrMessages[i].text; }
            if (index == "Missing" && this.missingMessage == "$_unset_$") { return arrMessages[i].text; }
            if (index == "Range" && this.rangeMessage == "$_unset_$") { return arrMessages[i].text; }
          }
        }
      }
    } else {
      this.messages = dojo.i18n.getLocalization("dijit.form", "validate", this.lang);
      if(index == "Invalid" && this.invalidMessage == "$_unset_$"){ return this.messages.invalidMessage; }
      if(index == "Missing" && this.missingMessage == "$_unset_$"){ return this.messages.missingMessage; }
      if(index == "Range" && this.rangeMessage == "$_unset_$"){ return this.messages.rangeMessage; }
    }
    return null;
  },

  setMessages: function() {
    this.invalidMessage = this.getMessage("Invalid");
    this.missingMessage = this.getMessage("Missing");
    this.rangeMessage = this.getMessage("Range");
  },


  //////////// INITIALIZATION METHODS ///////////////////////////////////////

  constructor: function() {
    this.constraints = {};
  },

  postMixInProperties: function() {
    this.setMessages();
    this.inherited(arguments);
    var p = this.regExpGen(this.constraints);
    this.regExp = p;
  },

  postCreate: function() {
    if(this.required){ dojo.addClass(this.textbox, "required"); }
    if(this.readonly){ dojo.addClass(this.textbox, "readonly"); }
    if(this.disabled){ dojo.addClass(this.textbox, "disabled"); }
    if (this.disabled){ this.textbox.setAttribute("disabled", "true"); }
    if (this.readonly){ this.textbox.setAttribute("readonly", "true"); }
    this.invalidSpan.style.display="none";
    this.missingSpan.style.display="none";
    this.rangeSpan.style.display="none";
    this.highlight();
    this.inherited(arguments);
  }
});


dojo.declare("openbravo.widget.ValidationTextBox.Date", [openbravo.widget.ValidationTextBox], {
  displayFormat: "",
  saveFormat: "",

  listenOnKeyPress: false,

  greaterThan:"",
  lowerThan:"",

  isWrong:false,

  postMixInProperties: function() {
    this.inherited(arguments);
  },

  postCreate:function() {
    this.inherited(arguments);
    this.textbox.setAttribute("displayFormat", this.displayFormat);
    this.displayFormat=this.displayFormat.replace("mm","MM").replace("dd","DD").replace("yyyy","YYYY");
    this.displayFormat=this.displayFormat.replace("MM","%m").replace("DD","%d").replace("YYYY","%Y");
    this.saveFormat=this.displayFormat;
  },

  isValid: function() {
    if (this.getDate(this.textbox.value,this.displayFormat)){
      return true;
    } else {
      return false;
    }
  },

  isInRange: function() {
    if ((this.greaterThan == "" || this.greaterThan == null) && (this.lowerThan == "" || this.lowerThan == null)) {
      return true;
    }
    if (this.greaterThan != "") {
      if (dojo.byId(this.greaterThan).value == null || dojo.byId(this.greaterThan).value == "") {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.greaterThan).update();
        }
        return true;
      } else if (this.textbox.value == "" || this.textbox.value == null) {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.greaterThan).update();
        }
        return true;
      } else if (this.getDate(this.textbox.value,this.displayFormat) < this.getDate(dojo.byId(this.greaterThan).value,this.displayFormat)) {
        if (this.isWrong == false) {
          this.isWrong = true;
          dijit.byId(this.greaterThan).update();
        }
        return false;
      } else {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.greaterThan).update();
        }
        return true;
      }
    }
    if (this.lowerThan != "") {
      if (dojo.byId(this.lowerThan).value == null || dojo.byId(this.lowerThan).value == "") {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.lowerThan).update();
        }
        return true;
      } else if (this.textbox.value == "" || this.textbox.value == null) {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.lowerThan).update();
        }
        return true;
      } else if (this.getDate(this.textbox.value,this.displayFormat) > this.getDate(dojo.byId(this.lowerThan).value,this.displayFormat)) {
        if (this.isWrong == false) {
          this.isWrong = true;
          dijit.byId(this.lowerThan).update();
        }
        return false;
      } else {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.lowerThan).update();
        }
        return true;
      }
    }
  },

  getDate: function(str_datetime, str_dateFormat) {
    var inputDate=new Date(0,0,0); 
    if (str_datetime.length == 0) { return inputDate; } 
    // datetime parsing and formatting routimes. modify them if you wish other datetime format 
    var re_date = new RegExp("^(\\d+)[\\-|\\/|/|:|.|\\.](\\d+)[\\-|\\/|/|:|.|\\.](\\d+)$"); 
    if (!re_date.test(str_datetime)){
      return false; 
    }
    var m = re_date.exec(str_datetime);
    if (!str_dateFormat){ str_dateFormat = defaultDateFormat; }
    switch (str_dateFormat) {
      case "%m-%d-%Y": 
      case "%m/%d/%Y": 
      case "%m.%d.%Y": 
      case "%m:%d:%Y": 
        if (m[2] < 1 || m[2] > 31){ return false; }
        if (m[1] < 1 || m[1] > 12){ return false; }
        if (m[3] < 1 || m[3] > 9999){ return false; }
        inputDate=new Date(parseFloat(m[3]), parseFloat(m[1])-1, parseFloat(m[2]));
        return inputDate;
      break;
      case "%Y-%m-%d": 
      case "%Y/%m/%d": 
      case "%Y.%m.%d": 
      case "%Y:%m:%d": 
        if (m[3] < 1 || m[3] > 31){ return false; }
        if (m[2] < 1 || m[2] > 12){ return false; }
        if (m[1] < 1 || m[1] > 9999){ return false; }
        inputDate=new Date(parseFloat(m[1]), parseFloat(m[2])-1, parseFloat(m[3]));
        return inputDate;
      break;
      case "%d-%m-%Y": 
      case "%d/%m/%Y": 
      case "%d.%m.%Y": 
      case "%d:%m:%Y": 
        if (m[1] < 1 || m[1] > 31){ return false; }
        if (m[2] < 1 || m[2] > 12){ return false; }
        if (m[3] < 1 || m[3] > 9999){ return false; }
        inputDate=new Date(parseFloat(m[3]), parseFloat(m[2])-1, parseFloat(m[1]));
        return inputDate;
      break;
    }
    return false; 
  },

  _onKeyUp: function(evt) {
    this.inherited(arguments);
    this.autoCompleteDate(this.textbox ,this.displayFormat);
  },

/**
* Text insertion of mask at inserting time
*/
  autoCompleteDate: function(/*String*/field, /*String*/fmt) {
    if (!isTabPressed) {
      try {
      if (this.getCaretPosition(field).start != field.value.length) { return; } //If we are inserting in a position different from the last one, we don't autocomplete
      } catch (ignored) {}
      if (fmt == null || fmt == "") {
        alert('openbravo.widget.DateTextBox ERROR: No displayFormat specified');
        return;
      }
//    fmt = getDateFormat(fmt);
      var strDate = field.value;
      var b = fmt.match(/%./g);
      var i = 0, j = -1;
      var text = "";
      var length = 0;
      var pos = fmt.indexOf(b[0]) + b[0].length;
      var separator = fmt.substring(pos, pos+1);
      var separatorH = "";
      pos = fmt.indexOf("%H");
      if (pos!=-1) { separatorH = fmt.substring(pos + 2, pos + 3); }
      while (strDate.charAt(i)) {
        if (strDate.charAt(i)==separator || strDate.charAt(i)==separatorH) {
          i++;
          continue;
        }
        if (length<=0) {
          j++;
          if (j>0) {
            if (b[j]=="%H") { text += " "; }
            else if (b[j]=="%M" || b[j]=="%S") { text += separatorH; }
            else { text += separator; }
          }
          switch (b[j]) {
              case "%d":
              case "%e":
                  text += strDate.charAt(i);
                  length = 2;
                  break;
              case "%m":
                  text += strDate.charAt(i);
                  length = 2;
                  break;
              case "%Y":
                  text += strDate.charAt(i);
                  length = 4;
                  break;
              case "%y":
                  text += strDate.charAt(i);
                  length = 2;
                  break;
              case "%H":
              case "%I":
              case "%k":
              case "%l":
                  text += strDate.charAt(i);
                  length = 2;
                  break;
              case "%M":
                  text += strDate.charAt(i);
                  length = 2;
                  break;
              case "%S":
                  text += strDate.charAt(i);
                  length = 2;
                  break;
          }
        } else { text += strDate.charAt(i); }
        length--;
        i++;
      }
      field.value = text;
      //IE doesn't detect the onchange event if text value is modified programatically, so it's here called
      if (i > 7 && (typeof (field.onchange)!="undefined")) { field.onchange(); }
    }
  },

  // caretPosition object
  caretPosition: function() {
    var start = null;
    var end = null;
  },

/**
* Function that returns actual position of -1 if we are at last position
*/
  getCaretPosition: function(oField) {
    var oCaretPos = new this.caretPosition();
    // IE support
    if(document.selection) {
      oField.focus();
      var oSel = document.selection.createRange();
      var selectionLength = oSel.text.length;
      oSel.moveStart ('character', -oField.value.length);
      oCaretPos.start = oSel.text.length - selectionLength;
      oCaretPos.end = oSel.text.length;
    } /*Firefox support */ else if (oField.selectionStart || oField.selectionStart == '0') {
      oCaretPos.start = oField.selectionStart;
      oCaretPos.end = oField.selectionEnd;
    }
    // Return results
    return (oCaretPos);
  }

});

dojo.require("dojo.number");

dojo.declare("openbravo.widget.ValidationTextBox.Number", [openbravo.widget.ValidationTextBox], {
  listenOnKeyPress: true,

  greaterThan:"",
  lowerThan:"",

  group: "",
  decimal: "",
  pattern: "#,##0.###",

  isWrong:false,

  regExpGen: function(){ return this.generateRegExp().regexp; },

  generateRegExp: function(options) {
    options = options || {};
    var pattern = this.pattern;
    var group = this.group;
    var decimal = this.decimal;
    var factor = 1;
    var _numberPatternRE = /[#0,]*[#0](?:\.0*#*)?/;

    //TODO: handle quoted escapes
    var patternList = pattern.split(';');
    if(patternList.length == 1){
      patternList.push("-" + patternList[0]);
    }

    var re = dojo.regexp.buildGroupRE(patternList, function(pattern){
      pattern = "(?:"+dojo.regexp.escapeString(pattern, '.')+")";
      return pattern.replace(_numberPatternRE, function(format){
        var flags = {
          signed: false,
          separator: options.strict ? group : [group,""],
          fractional: options.fractional,
          decimal: decimal,
          exponent: false};
        var parts = format.split('.');
        var places = options.places;
        if(parts.length == 1 || places === 0){flags.fractional = false;}
        else{
          if(places === undefined){ places = options.pattern ? parts[1].lastIndexOf('0')+1 : Infinity; }
          if(places && options.fractional == undefined){flags.fractional = true;} // required fractional, unless otherwise specified
          if(!options.places && (places < parts[1].length)){ places += "," + parts[1].length; }
          flags.places = places;
        }
        var groups = parts[0].split(',');
        if(groups.length>1){
          flags.groupSize = groups.pop().length;
          if(groups.length>1){
            flags.groupSize2 = groups.pop().length;
          }
        }
        return "("+dojo.number._realNumberRegexp(flags)+")";
      });
    }, true);

  //TODO: substitute localized sign/percent/permille/etc.?

    // normalize whitespace and return
    return {regexp: re.replace(/[\xa0 ]/g, "[\\s\\xa0]"), group: group, decimal: decimal, factor: factor}; // Object
  },

  isInRange: function() {
    if ((this.greaterThan == "" || this.greaterThan == null) && (this.lowerThan == "" || this.lowerThan == null)) {
      return true;
    }
    if (this.greaterThan != "") {
      if (dojo.byId(this.greaterThan).value == null || dojo.byId(this.greaterThan).value == "") {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.greaterThan).update();
        }
        return true;
      } else if (this.textbox.value == "" || this.textbox.value == null) {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.greaterThan).update();
        }
        return true;
      } else if (parseFloat(this.textbox.value) < parseFloat(dojo.byId(this.greaterThan).value)) {
        if (this.isWrong == false) {
          this.isWrong = true;
          dijit.byId(this.greaterThan).update();
        }
        return false;
      } else {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.greaterThan).update();
        }
        return true;
      }
    }
    if (this.lowerThan != "") {
      if (dojo.byId(this.lowerThan).value == null || dojo.byId(this.lowerThan).value == "") {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.lowerThan).update();
        }
        return true;
      } else if (parseFloat(this.textbox.value) > parseFloat(dojo.byId(this.lowerThan).value)) {
        if (this.isWrong == false) {
          this.isWrong = true;
          dijit.byId(this.lowerThan).update();
        }
        return false;
      } else {
        if (this.isWrong == true) {
          this.isWrong = false;
          dijit.byId(this.lowerThan).update();
        }
        return true;
      }
    }
  }

});

dojo.declare("openbravo.widget.ValidationTextBox.RealNumber", [openbravo.widget.ValidationTextBox.Number], {
  group: ",",
  decimal: "."
});

dojo.declare("openbravo.widget.ValidationTextBox.IntegerNumber", [openbravo.widget.ValidationTextBox.Number], {
  group: ",",
  decimal: ""
});
