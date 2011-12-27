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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

Page.setAppImgDir('[APP]../../skins/ltr/Default/');

isc.ClassFactory.defineClass("OBListGrid", isc.VLayout);

isc.OBListGrid.addProperties({

  // Override initWidget to populate the editor with a resizeable ListGrid and editor pane
  initWidget : function () {

    // Always call the superclass implementation when overriding initWidget
    this.Super("initWidget", arguments);


    this.newButton = isc.IButton.create({
      icon : "Main/ToolBar/iconNew.png",
      autoDraw : false,
      layoutAlign : "left",
      title : 'New',
      widget : this,
      click : function () {
        this.widget.newButtonAction();
      }
    });

    this.saveButton = isc.IButton.create({
      // have the saveButton be initially disabled - enable when a record is selected
      icon : "Main/ToolBar/iconSave.png",
      disabled : false,
      autoDraw : false,
      layoutAlign : "left",
      title : "Save",
      widget : this,
      click : function () {
        this.widget.saveButtonAction();
      }
    });

    this.copyButton = isc.IButton.create({
      icon : "Main/ToolBar/iconOrganzieMenu.png",
      autoDraw : false,
      layoutAlign : "left",
      title : 'Copy',
      grid : this.grid,
      widget : this,
      click : function () {
        this.widget.copyButtonAction();
      }
    });

    this.deleteButton = isc.IButton.create({
      icon : "Main/ToolBar/iconErase.png",
      autoDraw : false,
      layoutAlign : "left",
      title : 'Delete',
      grid : this.grid,
      widget : this,
      click : function () {
        this.widget.deleteButtonAction();
      }
    });

    this.previousButton = isc.IButton.create({
      icon : "Main/ToolBar/iconPrevious.png",
      autoDraw : false,
      layoutAlign : "left",
      title : 'Previous',
      grid : this.grid,
      widget : this,
      click : function () {
        this.widget.previousButtonAction();
      }
    });

    this.nextButton = isc.IButton.create({
      icon : "Main/ToolBar/iconNext.png",
      autoDraw : false,
      layoutAlign : "left",
      title : 'Next',
      grid : this.grid,
      widget : this,
      click : function () {
        this.widget.nextButtonAction();
      }
    });


    this.grid = isc.ListGrid.create({
      advancedListGrid : this,
      form : this.form,
      saveButton : this.saveButton,
      autoDraw : false,
      showResizeBar : true,
      showFilterEditor : this.showFilterEditor,
      filterOnKeypress : true,
      dataSource : this.dataSource,
      data : this.data,
      fields : this.fields,
      //autoFetchData : true,
      canEdit : true,

      canRemoveRecords: this.canRemoveRecords,
      // canEdit : this.canEdit, FIXME:  JSLInt Duplicate member 'canEdit'
      confirmDiscardEdits : this.confirmDiscardEdits,
      modalEditing : this.modalEditing,
      minFieldWidth : this.minFieldWidth,
      wrapCells : this.wrapCells,

      dataPageSize : this.dataPageSize,
      // showResizeBar : this.showResizeBar, FIXME: JSLint Duplicate member 'showResizeBar'
      recordClick : this.recordClick,
      canFreezeFields : this.canFreezeFields,
      canAddFormulaFields : this.canAddFormulaFields,
      canAddSummaryFields : this.canAddSummaryFields,
      autoFetchData : this.autoFetchGridData, //With different param name to avoid unexpected crash
      alternateRecordStyles : this.alternateRecordStyles/*,

      recordClick: function (viewer, record, rowNum, field, fieldNum, value, rawValue) {
        //alert(rowNum);
        this.saveButton.enable();
      },
      rowClick : function (record, recordNum, fieldNum) {

      }*/
    });


    this.toolbarLayout = isc.HLayout.create({
      height : "20",
      autoDraw : false,
      membersMargin : 5,
      members : [
        this.newButton,
        this.saveButton,
        this.copyButton,
        this.deleteButton,
        this.previousButton,
        this.nextButton
      ]
    });

    this.widgetLayout = isc.VLayout.create({
      autoDraw : false,
      membersMargin : 5,
      members : [
        this.toolbarLayout,
        this.grid
      ]
    });

    // Slot the compount into
    this.addMember(this.widgetLayout);
  },

  setRowEditing: function (rowNum, status) {
    if (status === true) {
      this.grid.startEditing(rowNum);
    } else if (status === false) {
      this.grid.endEditing(rowNum);
    }
  },

  // setDataSource()
  // Method to update the dataSource of both the grid and the form
  setDataSource : function (dataSource) {
    this.grid.setDataSource(dataSource);
    this.saveButton.disable();
    this.grid.filterData();
  },

  newButtonAction : function () {
    this.grid.startEditingNew();
  },

  saveButtonAction : function () {
    this.setRowEditing(7, true);
  },

  copyButtonAction : function () {
    this.setRowEditing(7, false);
  },

  deleteButtonAction : function () {  //Work in progress
    grid = this.grid;
    ident = grid.getSelectedRecord()._identifier;
    var func = function (value) {
      if (value !== null && value) {
        isc.DataSource.getDataSource(grid.dataSource).removeData('', '', {params : {id: grid.getSelectedRecord().id}});
      }
    };
    isc.confirm("Are you sure you want to delete " + ident + "?", func);
  },

  previousButtonAction : function () {
    var selectedRowNum = this.grid.findRowNum(this.grid.getSelectedRecord());
    if (selectedRowNum === 0) {
      selectedRowNum = 1;
    }
    this.grid.selectSingleRecord(selectedRowNum - 1);
  },

  nextButtonAction : function () {
    var selectedRowNum = this.grid.findRowNum(this.grid.getSelectedRecord());
    this.grid.selectSingleRecord(selectedRowNum + 1);
  }
});



isc.ClassFactory.defineClass("OBDateItem", DateItem);

isc.OBDateItem.addClassProperties({
  OBDateItemElement : null,
  autoCompleteDate : function (value) {
    //if (!isTabPressed) {
    var fmt = this.OBDateItemElement.dateFormat;
    fmt = OB.Utilities.Date.normalizeDisplayFormat(fmt);
    try {
      if (this.OBDateItemElement.getSelectionRange()[0] != value.length) {
        return; //If we are inserting in a position different from the last one, we don't autocomplete
      }
    } catch (ignored) {}
    var strDate = value;
    var b = fmt.match(/%./g);
    var i = 0, j = -1;
    var text = "";
    var length = 0;
    var pos = fmt.indexOf(b[0]) + b[0].length;
    var separator = fmt.substring(pos, pos + 1);
    var separatorH = "";
    pos = fmt.indexOf("%H");
    if (pos !== -1) {
      separatorH = fmt.substring(pos + 2, pos + 3);
    }
    if (strDate === null) {
      return;
    }
    while (strDate.charAt(i)) {
      if (strDate.charAt(i) == separator || strDate.charAt(i) == separatorH || strDate.charAt(i) === " ") {
        i++;
        continue;
      }
      if (length <= 0) {
        j++;
        if (j > 0) {
          if (b[j] === "%H") {
            text += " ";
          }
          else if (b[j] === "%M" || b[j] === "%S") {
            text += separatorH;
          }
          else {
            text += separator;
          }
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
      } else {
        text += strDate.charAt(i);
      }
      length--;
      i++;
    }
    this.OBDateItemElement.setValue(text);
    //IE doesn't detect the onchange event if text value is modified programatically, so it's here called
    // if (i > 7 && (typeof (field.onchange)!="undefined")) field.onchange();
    //}
  },
  validateDate : function (inputDate) {
    var inputDateFormat = this.getDateFormat();
    if (OB.Utilities.Date.OBToJS(inputDate, inputDateFormat)) {
      return true;
    } else {
      return false;
    }
  },
  getTimeBlock : function (str_time, block) {
    //time parsing and formatting routimes. modify them if you wish other time format
    //function str2dt (str_time) {
    if (str_time.indexOf(".") === -1 && str_time.indexOf(":") === -1) {
      return false;
    }
    if (str_time.length !== 5 && str_time.length !== 8) {
      return false;
    }
    var response = "";
    var timeBlock = str_time.match(/(\d+)(\d+)/g);
    if (block === 1 || block === '1') {
      response = timeBlock[0];
    }
    else if (block === 2 || block === '2') {
      response = timeBlock[1];
    }
    else if (block === 3 || block === '3') {
      response = timeBlock[2];
    }
    else {
      response = timeBlock;
    }
    if ((block === 3 || block === '3') && typeof response  === 'undefined') {
      response = "00";
    }
    return response;
  },
  getDateFormat : function () {
    var dateFormat = this.OBDateItemElement.dateFormat;
    return dateFormat;
  },

  getDateBlock : function (str_date, block) {
    var datePattern = "^(\\d+)[\\-|\\/|/|:|.|\\.](\\d+)[\\-|\\/|/|:|.|\\.](\\d+)$";
    var dateRegExp = new RegExp(datePattern);
    if (!dateRegExp.exec(str_date)) {
      return false;
    }
    var dateBlock = [];
    dateBlock[1] = RegExp.$1;
    dateBlock[2] = RegExp.$2;
    dateBlock[3] = RegExp.$3;
    if (block === 1 || block === '1') {
      return dateBlock[1];
    } else if (block === 2 || block === '2') {
      return dateBlock[2];
    } else if (block === 3 || block === '3') {
      return dateBlock[3];
    } else {
      return dateBlock;
    }
  },
  expandDateYear : function (value) {
    var str_date = value;
    var str_dateFormat = this.OBDateItemElement.dateFormat;
    if (str_date === null) {
      return false;
    }
    if (str_dateFormat.indexOf('YYYY') !== -1) {
      var centuryReference = 50;
      var dateBlock = [];
      dateBlock[1] = this.getDateBlock(str_date, 1);
      dateBlock[2] = this.getDateBlock(str_date, 2);
      dateBlock[3] = this.getDateBlock(str_date, 3);

      if (!dateBlock[1] || !dateBlock[2] || !dateBlock[3]) {
        return false;
      }

      var yearBlock;
      if (str_dateFormat.substr(1, 1) === 'Y') {
        yearBlock = 1;
      } else if (str_dateFormat.substr(7, 1) == 'Y') {
        yearBlock = 3;
      } else {
        return false;
      }

      if (dateBlock[yearBlock].length === 1) {
        dateBlock[yearBlock] = '000' + dateBlock[yearBlock];
      } else if (dateBlock[yearBlock].length === 2) {
        if (dateBlock[yearBlock] < centuryReference) {
          dateBlock[yearBlock] = '20' + dateBlock[yearBlock];
        } else {
          dateBlock[yearBlock] = '19' + dateBlock[yearBlock];
        }
      } else if (dateBlock[yearBlock].length === 3) {
        dateBlock[yearBlock] = '0' + dateBlock[yearBlock];
      } else if (dateBlock[yearBlock].length === 4) {
        return true;
      }

      var dateSeparator = str_dateFormat.replace(/D/g, "").replace(/M/g, "").replace(/Y/g, "").substr(0, 1);
      var normalizedDate = dateBlock[1] + dateSeparator + dateBlock[2] + dateSeparator + dateBlock[3];
      this.OBDateItemElement.setValue(normalizedDate);
    } else {
      return false;
    }
    return true;
  },
  getRandomInteger : function (minInt, maxInt) {
    if (typeof minInt === "undefined") {
      minInt = 0;
    }
    if (typeof maxInt === "undefined") {
      maxInt = 100;
    }
    var randomInteger = minInt + (Math.random() * (maxInt - minInt));
    randomInteger = Math.round(randomInteger);
    return randomInteger;
  },
  getRandomString : function (num) {
    if (typeof num === "undefined") {
      num = 10;
    }
    var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
    var randomString = '';
    for (var i = 0; i < num; i++) {
      var rnum = Math.floor(Math.random() * chars.length);
      randomString += chars.substring(rnum, rnum + 1);
    }
    return randomString;
  },
  validateOBDateItem : function () {
    var validatorLength = this.OBDateItemElement.validators.length;
    var isValid = this.OBDateItemElement.validators[validatorLength-1].condition(this.OBDateItemElement, this.OBDateItemElement.form, this.OBDateItemElement.getValue());
    var isRequired = this.OBDateItemElement.required;
    if (typeof this.OBDateItemElement.name === "undefined") {
      this.name = "isc_" + OBDateItem.getRandomString(OBDateItem.getRandomInteger(6, 12));
    }
    if (isValid === false) {
      this.OBDateItemElement.form.setFieldErrors(this.OBDateItemElement.name, 'The value entered is not valid', true);
    } else if (isRequired === true && this.OBDateItemElement.getValue() === null) {
      this.OBDateItemElement.form.setFieldErrors(this.OBDateItemElement.name, 'The value entered is required', true);
    } else {
      this.OBDateItemElement.form.clearFieldErrors(this.OBDateItemElement.name, true);
    }
  }
});

isc.OBDateItem.addProperties({
  useTextField : true,
  changeOnKeypress: true,
  validateOnChange : false,
  focus : function () {
    OBDateItem.OBDateItemElement = this;
  },
  change :  function (form, item, value, oldValue) { /*transformInput*/
    OBDateItem.OBDateItemElement = this;
    OBDateItem.autoCompleteDate(value);
  },
  iconClick : function () {
    OBDateItem.OBDateItemElement = this;
  },
  blur : function () {
    OBDateItem.OBDateItemElement = this;
    OBDateItem.expandDateYear(this.getValue());
    OBDateItem.validateOBDateItem();
  },
  displayFormat: function () {
    var dateFormat = OBDateItem.getDateFormat();
    var displayedDate = OB.Utilities.Date.JSToOB(this, dateFormat);
    return displayedDate;
  },
  inputFormat: "toDateStamp",
  validators : [{
    type : "custom",
    condition : function (form, item, value) {
      return OBDateItem.validateDate(value);
    }
  }]
});



isc.ClassFactory.defineClass("OBNumberItem", TextItem);

isc.OBNumberItem.addClassProperties({
  OBNumberItemElement : null,
  getDefaultMaskNumeric : function () {
    var maskNumeric = "#,###.#";
    return maskNumeric;
  },
  getGlobalDecSeparator : function () {
    var decSeparator = ".";
    return decSeparator;
  },
  getGlobalGroupSeparator : function () {
    var groupSeparator = ",";
    return groupSeparator;
  },
  getGlobalGroupInterval : function () {
    var groupInterval = "3";
    return groupInterval;
  },
  returnNewCaretPosition : function (number, oldCaretPosition, groupSeparator) {
    var newCaretPosition = oldCaretPosition;
    for (var i = oldCaretPosition; i > 0; i--) {
      if (number.substring(i - 1, i) == groupSeparator) {
        newCaretPosition = newCaretPosition - 1;
      }
    }
    return newCaretPosition;
  },
  focusNumberInput : function (maskNumeric, decSeparator, groupSeparator, groupInterval) {
    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.OBNumberItemElement.maskNumeric;
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.OBNumberItemElement.decSeparator;
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.OBNumberItemElement.groupSeparator;
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.OBNumberItemElement.groupInterval;
    }

    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.getDefaultMaskNumeric();
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.getGlobalDecSeparator();
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.getGlobalGroupSeparator();
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.getGlobalGroupInterval();
    }

    var oldCaretPosition = this.OBNumberItemElement.getSelectionRange()[0];
    var newCaretPosition = this.returnNewCaretPosition(this.OBNumberItemElement.getValue(), oldCaretPosition, groupSeparator);

    var number = this.OBNumberItemElement.getValue();
    if (typeof number === "undefined" || !number) {
      number = "";
    }
    var isValid = this.validateNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    if (!isValid) {
      return false;
    }
    var plainNumber = OB.Utilities.Number.OBMaskedToOBPlain(number, decSeparator, groupSeparator);
    this.OBNumberItemElement.setValue(plainNumber);
    this.OBNumberItemElement.setSelectionRange(newCaretPosition, newCaretPosition);
  },
  blurNumberInput : function (maskNumeric, decSeparator, groupSeparator, groupInterval) {
    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.OBNumberItemElement.maskNumeric;
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.OBNumberItemElement.decSeparator;
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.OBNumberItemElement.groupSeparator;
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.OBNumberItemElement.groupInterval;
    }

    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.getDefaultMaskNumeric();
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.getGlobalDecSeparator();
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.getGlobalGroupSeparator();
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.getGlobalGroupInterval();
    }

    var number = this.OBNumberItemElement.getValue();
    var isValid = this.validateNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    /*if (obj.getAttribute('maxlength')) {
      if (obj.value.length > obj.getAttribute('maxlength')) {
        isValid = false;
      }
    }
    updateNumberMiniMB(obj, isValid); //It doesn't apply in dojo043 inputs since it has its own methods to update it*/
    if (!isValid) {
      return false;
    }

    var formattedNumber = OB.Utilities.Number.OBPlainToOBMasked(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    this.OBNumberItemElement.setValue(formattedNumber);
  },
  replaceAt : function (string, what, ini, end) {
    if (typeof end === "undefined" || end === null || end === "null" || end === "") {
      end = ini;
    }
    if (ini > end) {
      var temp = ini;
      ini = end;
      end = temp;
    }
    var newString = "";
    newString = string.substring(0, ini) + what + string.substring(end + 1, string.length);
    return newString;
  },
  manageDecPoint : function (keyCode, decSeparator) {
    if (decSeparator === null || decSeparator === "") {
      decSeparator = this.getGlobalDecSeparator();
    }

    if (decSeparator === ".") {
      return true;
    }

    var caretPosition = this.OBNumberItemElement.getSelectionRange()[0];
    /*
    * if(keyCode>=65 && keyCode<=90) { setTimeout(function()
    * {obj.value = replaceAt(obj.value, "", caretPosition);
    * setCaretToPos(obj, caretPosition);},5); }
    */
    var inpMaxlength = this.OBNumberItemElement.length;
    var inpLength = this.OBNumberItemElement.getValue().length;
    var isInpMaxLength = false;
    if (inpMaxlength === null) {
      isInpMaxLength = false;
    } else if (inpLength >= inpMaxlength) {
      isInpMaxLength = true;
    }

    if (navigator.userAgent.toUpperCase().indexOf("OPERA") != -1 && keyCode==78) {
      keyCode = 110;
    }

    var obj = this;
    if (keyCode === 110 && !isInpMaxLength) {
      setTimeout(function () {
        var newValue = obj.replaceAt(obj.OBNumberItemElement.getValue(), decSeparator, caretPosition);
        obj.OBNumberItemElement.setValue(newValue);
        obj.OBNumberItemElement.setSelectionRange(caretPosition + 1, caretPosition + 1);
      }, 5);
    }
    return true;
  },
  validateNumber :  function (number, maskNumeric, decSeparator, groupSeparator, groupInterval) {
    if (number === null || number === "" || typeof number === "undefined") {
      number = this.OBNumberItemElement.getValue();
    }
    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.OBNumberItemElement.maskNumeric;
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.OBNumberItemElement.decSeparator;
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.OBNumberItemElement.groupSeparator;
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.OBNumberItemElement.groupInterval;
    }

    if (maskNumeric === null || maskNumeric === "" || typeof maskNumeric === "undefined") {
      maskNumeric = this.getDefaultMaskNumeric();
    }
    if (decSeparator === null || decSeparator === "" || typeof decSeparator === "undefined") {
      decSeparator = this.getGlobalDecSeparator();
    }
    if (groupSeparator === null || groupSeparator === "" || typeof groupSeparator === "undefined") {
      groupSeparator = this.getGlobalGroupSeparator();
    }
    if (groupInterval === null || groupInterval === "" || typeof groupInterval === "undefined") {
      groupInterval = this.getGlobalGroupInterval();
    }

    if (number === null || number === "" || typeof number === "undefined") {
      return true;
    }

    var bolNegative = true;
    if (maskNumeric.indexOf("+") === 0) {
      bolNegative = false;
      maskNumeric = maskNumeric.substring(1, maskNumeric.length);
    }

    var bolDecimal = true;
    if (maskNumeric.indexOf(decSeparator) === -1) {
      bolDecimal = false;
    }
    var checkPattern = "";
    checkPattern += "^";
    if (bolNegative) {
      checkPattern += "([+]|[-])?";
    }
    checkPattern += "(\\d+)?((\\" + groupSeparator + "\\d{" + groupInterval + "})?)+";
    if (bolDecimal) {
      checkPattern += "(\\" + decSeparator + "\\d+)?";
    }
    checkPattern += "$";
    var checkRegExp = new RegExp(checkPattern);
    if (number.match(checkRegExp) && number.substring(0, 1) !== groupSeparator) {
      return true;
    }
    return false;
  },
  getRandomInteger : function (minInt, maxInt) {
    if (typeof minInt === "undefined") {
      minInt = 0;
    }
    if (typeof maxInt === "undefined") {
      maxInt = 100;
    }
    var randomInteger = minInt + (Math.random() * (maxInt - minInt));
    randomInteger = Math.round(randomInteger);
    return randomInteger;
  },
  getRandomString : function (num) {
    if (typeof num === "undefined") {
      num = 10;
    }
    var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
    var randomString = '';
    for (var i = 0; i < num; i++) {
      var rnum = Math.floor(Math.random() * chars.length);
      randomString += chars.substring(rnum, rnum + 1);
    }
    return randomString;
  },
  validateOBNumberItem : function () {
    var value = this.OBNumberItemElement.getValue();
    var validatorLength = this.OBDateItemElement.validators.length;
    var isValid = this.OBNumberItemElement.validators[validatorLength-1].condition(this.OBNumberItemElement, this.OBNumberItemElement.form, value);
    var isRequired = this.OBNumberItemElement.required;
    if (typeof this.OBNumberItemElement.name === "undefined") {
      this.name = "isc_" + OBNumberItem.getRandomString(OBNumberItem.getRandomInteger(6, 12));
    }
    if (isValid === false) {
      this.OBNumberItemElement.form.setFieldErrors(this.OBNumberItemElement.name, 'The value entered is not valid', true);
    } else if (isRequired === true && (value === null || value === "" || typeof value === "undefined")) {
      this.OBNumberItemElement.form.setFieldErrors(this.OBNumberItemElement.name, 'The value entered is required', true);
    } else {
      this.OBNumberItemElement.form.clearFieldErrors(this.OBNumberItemElement.name, true);
    }
  }
});

isc.OBNumberItem.addProperties({
  validateOnChange : false,
  keyPressFilter: "[0-9.,]",
  focus: function () {
    OBNumberItem.OBNumberItemElement = this;
    OBNumberItem.focusNumberInput(this.maskNumeric, this.decSeparator, this.groupSeparator, this.groupInterval);
  },
  blur: function () {
    OBNumberItem.OBNumberItemElement = this;
    OBNumberItem.blurNumberInput(this.maskNumeric, this.decSeparator, this.groupSeparator, this.groupInterval);
    OBNumberItem.validateOBNumberItem();
  },
  keyDown: function () {
    var keyCode = isc.EventHandler.getKeyCode();
    OBNumberItem.OBNumberItemElement = this;
    OBNumberItem.manageDecPoint(keyCode, this.decSeparator);
  },
  validators : [{
    type : "custom",
    condition : function (form, item, value) {
      return OBNumberItem.validateNumber(value);
    }
  }]
 // keyPress: function (keyName, character) { OBNumberItem.OBNumberItemElement = this; var event = isc.Event; OBNumberItem.manageDecPoint(event); }
});