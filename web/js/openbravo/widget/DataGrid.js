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
 * All portions are Copyright (C) 2001-2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview This JavaScript library is the DataGrid widget library
*/

dojo.provide("openbravo.widget.DataGrid");

dojo.require("dijit._Widget");
dojo.require("dojox.collections.Dictionary");
dojo.require("dojox.data.dom");

function createTextCellElement(colMetadata) {
  var hoverCell = function(evt) {
    if (dojo && !dojo.hasClass(this, 'DataGrid_Body_Cell_hover')
        && !dojo.hasClass(this, 'DataGrid_Body_Cell_clicked')){
      dojo.addClass(this, 'DataGrid_Body_Cell_hover');
    }
  };

  var plainCell = function(evt) {
    if (dojo && dojo.hasClass(this, 'DataGrid_Body_Cell_hover')){
      dojo.removeClass(this, 'DataGrid_Body_Cell_hover', false);
    }
  };

  var text = document.createElement("nobr");
  text.className = openbravo.widget.DataGrid.Column.prototype.DEFAULT_CLASS;
  dojo.addClass(text, colMetadata.className);
  text.onmouseover = hoverCell;
  text.onmouseout = plainCell;
  openbravo.widget.DataGrid.html.disableSelection(text);
  var emptyText = document.createTextNode("");
  text.appendChild(emptyText);
  if (colMetadata.visible){
    text.style.width = colMetadata.width;
  }else{ 
    text.style.display = "none";
  }
  return text;
};

dojo.declare("openbravo.widget.DataGrid", [dijit._Widget], {
  structureUrl: "",
  dataUrl: "",
  updatesUrl: "",
  calculateNumRows: false,
  numRows: 20,
  offset: 0,
  sortCols: "",
  sortDirs: "",
  editable: true,
  sortable: true,
  onInvalidValue: alert,
  onScroll: function() {},
  onGridLoad: function() {},
  defaultRow: 0,
  showLineNumbers: "true",
  lineNoColumnWidth: "40px",
  lineNoColumnTitle: "#",
  lineNoColumnClass: "DataGrid_Body_LineNoCell",
  lineNoColumnHeaderClass: "DataGrid_Header_LineNoCell",
  bufferSize: 7.0,
  maxWidth: "100%",
  percentageWidthRelativeToId: "body",
  isFirstLoad: true,
  hasBeenResized: false,
  offsetBeforeResize: 0,
  multipleRowSelection: true,
  templateString: "<div></div>",
  templateCssPath: null, // Defined in the skin --- DataGrid.css
  backendPageSize: 0,

  getBackendPageSize: function() {
    return this.backendPageSize;
  },

/**
* Dojo's function to recreate the control properties in its build process.
* @param {Array} args - array of arguments
* @param {Object} frag - object
* @param {Object} parentComp - component parent
*/
  postMixInProperties: function(args, frag, parentComp) {
    setCalloutProcessing(true);
    if (this.updatesUrl == ""){
      this.updatesUrl = dataUrl;
    }
    this.selectedRows = new openbravo.widget.DataGrid.IndexedRows();
    if (this.calculateNumRows == true) {
      this.numRows = calculateNumRows();
    }
    this.editor = null;
    this.editing = false;
    this.editingCell = null;
    this.tableNode = null;
    this.editingRow = null;
    this.selectedCell = -1;
    this.locked = false;
    this.lastHoveredColumn = null;
    this.lastAddition = "";
    this.validators = [];
    this.requestParams = [];
    this.visibleRows = this.numRows;
    this.visibleRowsMax = this.numRows;
    this.scrollWidth = openbravo.widget.DataGrid.html.getScrollbar().width;
    if (this.onInvalidValue == alert){
      this.onInvalidValue = function(msg) { alert(msg); };
    }
    this.savingInterface = {
      save: dojo.hitch(this, "saveCell"),
      cancel: dojo.hitch(this, "cancelEdit"),
      lockGrid: dojo.hitch(this, function() {
        this.locked = true;
      }),
      unlockGrid: dojo.hitch(this, function() {
        this.locked = false;
        this.setFocus();
      }),
      dataUrl: this.dataUrl,
      updatesUrl: this.updatesUrl
    };
    this.errorHandler = {
      handleError: dojo.hitch(this, "handleUpdateError")
    };
    this.columns = new openbravo.widget.DataGrid.Columns();
    this.numberOfResizes = 0;
    this.requestStructure();
  },

/**
* Dojo's function to operate on the control after its build process
*/
  postCreate: function() {
    var row = document.createElement("tr");
    row.className = 'DataGrid_Body_Row';
    var height = dojo.style(row, "height", "15px");
    // height = openbravo.widget.DataGrid.html.extractPx(height);
    this.domNode.style.height = (28) * (this.numRows + 1) + "px";
    var pos = this.maxWidth.indexOf("%");
    if (pos > 0) {
      this.proportion = this.maxWidth.substr(0, pos) / 100;
      var maxWidthInt = this.setMaxWidth();
      this.domNode.style.width = maxWidthInt + 8 + "px";
    }
  },

/**
* Call to the backend, used by the method postMixInProperties, to load the grid column structure. For the answer, it uses the ajaxUpdate methods of the Parser object.
* @param {String} url - url for the request
* @param {Object} handler - handler
* @param {Array} content - aditional content
*/
  requestStructure: function(url, handler, content) {
    var parser = new openbravo.widget.DataGrid.Parser(this);
    var handlerRef = dojo.hitch(parser, "ajaxUpdate");
    var serviceUrl = {
      url: this.structureUrl,
      handler: handlerRef,
      method: "GET",
      handleAs: "xml"
    };
    openbravo.widget.DataGrid.io.asyncCall(serviceUrl, {});
  },

/**
* Method used for the cell edition. It only will be used in editable grids. It set a cell to editable mode doing the previous necesary save tasks and controlling if the cell can be editable or not.
* @param {Object} cell - Object pointing to the actual cell
*/
  editCell: function(cell) {
    if (this.editing){
      this.saveCell();
    }
    if (!this.editing && this.editable) {
      var rowNode = cell.parentNode;
      var columnNo = dojo.indexOf(cell.parentNode.cells, cell);
      var column = this.columns.get(columnNo);
      if (column.readonly) { return; }
      var rowNo = this.getCurrentOffset() + rowNode.rowIndex;
      var rowChanged = this.editingRow && this.editingRow.offset != rowNo;
      if (!this.editingRow || rowChanged){
        this.editingRow = this.buffer.getRow(rowNo);
      }
      this.editingRow.setStatus(this.editingRow.EDITING);
      this.editor = column.renderEditor(cell, this.editingRow, this.savingInterface);
      openbravo.widget.DataGrid.html.enableSelection(cell);
      this.editingCell = cell;
      this.editing = true;
    }
  },

/**
* Saves the modified content of a cell that is in edition mode, hidding its edition mode. At the same time, it checks data, so if invalid data were found, it will launch the method onInvalidValue. If the column is defined as synchronized column, the launch to the backend for the synchronization will be launched.
* @returns True: everything is ok - False: there are some problems.
* @type Boolean
*/
  saveCell: function() {
    var cell = this.editingCell;
    if (!cell) { return; }
    var columnNo = dojo.indexOf(cell.parentNode.cells, cell);
    var column = this.columns.get(columnNo);
    var storedValue = this.editingRow.getValue(column.index);
    var inputValue = this.editor.getValue();
    if (inputValue == storedValue) {
      this.cancelEdit();
      return true;
    }
    var correct = column.type.validate(inputValue);
    if (correct) {
      this.editingRow.setValue(column.name, inputValue);
      if (column.invalidates.length > 0) {
        for (var i = 0; i < column.invalidates.length; i++) {
          var invalidIndex = this.columns.get(column.invalidates[i]).index;
          this.editingRow.setValue(column.invalidates[i], "");
          openbravo.widget.DataGrid.html.clearElement(cell.parentNode.childNodes[invalidIndex]);
        }
      }
      if (column.sync){
        this.saveRow(true);
      }
      this.hideEditor(inputValue);
      return true;
    } else {
      var msg = "Invalid value";
      this.onInvalidValue(msg);
      this.editor.setFocus();
      return false;
    }
  },

/**
* Send a row to the backend for the backend do the save operations.
* @param {Boolean} skipAlerts - Identifies if the aerts must been shown.
* @return True: if ok - False: if it's wrong.
* @type Boolean
*/
  saveRow: function(skipAlerts) {
    var row = this.editingRow;
    if (!row.modified && !row.error){
      return true;
    }
    skipAlerts = skipAlerts == true;

    var emptyFields = row.checkFields();
    if (emptyFields.length > 0) {
      if(!skipAlerts) {
        this.onInvalidValue("A required field is empty");
        var cellNumber = emptyFields[0];
        var isVisible = this.isVisible(row.offset);
        var edit = dojo.hitch(this, function() {
          this.options.onRefreshComplete.remove(edit);
          this.editCell(row.rowNode.cells[cellNumber]);
        });
        if (!isVisible) {
          this.options.onRefreshComplete.push(edit);
          this.goToRow(row.offset);
        } else {
          edit();
        }
      }
    }else{
      row.validate(this.validators);
    }
    if (!row.error) {
      row.sendRow(this.updatesUrl);
      return true;
    } else {
      return false;
    }
  },

/**
* It makes a new grid row
* @param {Number} offset - identifies the actual offset.
* @return Object pointing to the new row.
* @type openbravo.widget.DataGrid.Row
*/
  createRowObject: function(offset) {
    return new openbravo.widget.DataGrid.Row(offset, this.columns, 
      this.options, this.errorHandler, dojo.hitch(this, "isVisible"));
  },

/**
* It makes the message dialog to show the errors produced by the update.
* @param {String} title - the title of the message.
* @param {String} description - the description of the message.
*/
  handleUpdateError: function(title, description) {
    var mainDiv = document.createElement("div");
    var contentForm = document.createElement("form");
    var table = document.createElement("table");
    var tbody = document.createElement("tbody");
    var titleRow = document.createElement("tr");
    var titleCell = document.createElement("td");
    dojo.addClass(titleCell, "messageDialogTitle");
    titleCell.appendChild(document.createTextNode(title));
    titleRow.appendChild(titleCell);
    var descRow = document.createElement("tr");
    var descCell = document.createElement("td");
    dojo.addClass(descCell, "messageDialogText");
    descCell.appendChild(document.createTextNode(description));
    descCell.innerHTML = descCell.innerHTML + '<br/><br/>';
    descRow.appendChild(descCell);
    var buttonRow = document.createElement("tr");
    var buttonCell = document.createElement("td");
    buttonCell.align = "center";
    var saveButton = document.createElement("input");
    saveButton.type= "button";
    saveButton.value= "OK";
    dojo.addClass(saveButton, 'dialogButton');
    buttonCell.appendChild(saveButton);
    buttonRow.appendChild(buttonCell);
    tbody.appendChild(titleRow);
    tbody.appendChild(descRow);
    tbody.appendChild(buttonRow);
    table.appendChild(tbody);
    contentForm.appendChild(table);
    mainDiv.appendChild(contentForm);
    var options = {
      bgColor: "white",
      bgOpacity: "0.5"
      // toggle: "fade",
      // toggleDuration: 250
    }
    var dialog = dojo.widget.createWidget("dojo:Dialog", options, contentForm);
    dojo.connect(saveButton, "onclick", dialog, "hide");
    dojo.connect(dialog, "hide", dialog, "destroy");
    dialog.show();
  },

/**
* It cancels the edition of a cell. It verifies if some mistake had been produced in the same one, to show the mistake instead of cancelling. It is the method used to go out of a cell in edition.
*/
  cancelEdit: function() {
    if (!this.editing) { return; }
    var rowId = this.editingRow.getValue(this.columns.getIdentifier().index);
    if (this.options.failedRows.contains(rowId)){
      this.editingRow.setStatus(this.editingRow.ERROR);
    }else{
      this.editingRow.setStatus(this.editingRow.CORRECT);
    }
    this.hideEditor(this._getEditingCellContent(this.editingCell));
  },

/**
* It hides the edition mode to leave it in visualization mode.
* @param {String} newContent - New content
*/
  hideEditor: function(newContent) {
    var s = newContent;
    if (s && typeof s == 'string') { newContent = s.split(" ").join("&nbsp;"); }
    var editingElement = this.editingCell;
    if (!editingElement) { return; }
    openbravo.widget.DataGrid.html.disableSelection(editingElement);
    this.editing = false;
    this.editingCell = null;
    var column = null;
    var columnNo = dojo.indexOf(editingElement.parentNode.cells, editingElement);
    column = this.columns.get(columnNo);
    /*try {
      while (column.hasChildNodes()) column.removeChild(column.lastChild);
    } catch (ignored) {column.innerHTML="";}
    var textNode = createTextCellElement(column);*/
    if (column && column.type.name == 'url' && newContent != '') { editingElement.innerHTML = "<a href=\"" + newContent + "\" target=_blank><img src=\"../web/js/openbravo/templates/popup1.gif\" border=\"0\" title=\"Link\" alt=\"Link\"></a>&nbsp;" + newContent; }
    if (column && column.type.name == 'img' && newContent != '') { editingElement.innerHTML = "<a onclick=\"openPopUp('" + newContent + "', 'Image', '70%', '70%'); return false;\" href=\"" + newContent + "\"><img src=\"" + newContent + "\" border=\"0\" height=\"15px\"></a>"; }
    else { editingElement.innerHTML = newContent; }
    //editingElement.appendChild(textNode);
  },

/**
* It returns the value of the cell that is being edited. It is used in the method of cancelEdit to recover the value of the cell.
* @param {Object} cell - Object pointing to the cell.
* @return The value of the editing row.
* @type String
*/
  _getEditingCellContent: function(cell) {
    var columnNo = dojo.indexOf(cell.parentNode.cells, cell);
    var column = this.columns.get(columnNo);
    return this.editingRow.getValue(column.index);
  },

/**
* It changes the class of the selected rows in order that they have the aspect of checked.
*/
  showSelection: function() {
    var selectedCount = this.selectedRows.count();
    var offset = this.getCurrentOffset();
    var lastRowFound = false;
    if (selectedCount > 0){
      var lastSelectedRowId = this.selectedRows.getLastSelected().id;
    }
    for (var i = 0; i < this.numRows; i++) {
      var currentRow = this.buffer.getRow(offset + i);
      if (!currentRow) { continue; }
      if (selectedCount > 0 && this.selectedRows.contains(currentRow.getValue(this.columns.getIdentifier().index)) || currentRow.isNewRow) {
        if(isGridFocused) {
          openbravo.widget.DataGrid.html.prereplaceClass(this.tableNode.rows[i], 
            "DataGrid_Body_Row_focus",
            i % 2 == 0 ? "DataGrid_Body_Row_Even" : "DataGrid_Body_Row_Odd");
        } else {
          openbravo.widget.DataGrid.html.prereplaceClass(this.tableNode.rows[i], 
            "DataGrid_Body_Row_selected",
            i % 2 == 0 ? "DataGrid_Body_Row_Even" : "DataGrid_Body_Row_Odd");
        }
        for (var j = 0; j < this.columns.count(); j++) {
          if (!dojo.hasClass(this.tableNode.rows[i].cells[j], "DataGrid_Body_Cell_selected")) {
            dojo.addClass(this.tableNode.rows[i].cells[j], "DataGrid_Body_Cell_selected");
          }
          if (dojo.hasClass(this.tableNode.rows[i].cells[j], "DataGrid_Body_Cell_clicked")) {
            dojo.removeClass(this.tableNode.rows[i].cells[j], "DataGrid_Body_Cell_clicked");
          }
        }
        selectedCount--;
        if (this.selectedCell >= 0 && !lastRowFound) {
          if (currentRow.getValue(this.columns.getIdentifier().index) == lastSelectedRowId){ 
            if (!dojo.hasClass(this.tableNode.rows[i].cells[this.selectedCell], "DataGrid_Body_Cell_clicked")){
              dojo.addClass(this.tableNode.rows[i].cells[this.selectedCell], " DataGrid_Body_Cell_clicked");
            }
            lastRowFound = true;
          }
        }
      } else {
        if(isGridFocused) {
          openbravo.widget.DataGrid.html.prereplaceClass(this.tableNode.rows[i], 
            i % 2 == 0 ? "DataGrid_Body_Row_Even" : "DataGrid_Body_Row_Odd", 
            "DataGrid_Body_Row_focus");
        } else {
          openbravo.widget.DataGrid.html.prereplaceClass(this.tableNode.rows[i], 
            i % 2 == 0 ? "DataGrid_Body_Row_Even" : "DataGrid_Body_Row_Odd", 
            "DataGrid_Body_Row_selected");
        }
        for (var j = 0; j < this.columns.count(); j++) {
          if (dojo.hasClass(this.tableNode.rows[i].cells[j], "DataGrid_Body_Cell_selected")) {
            dojo.removeClass(this.tableNode.rows[i].cells[j], "DataGrid_Body_Cell_selected");
          }
          if (dojo.hasClass(this.tableNode.rows[i].cells[j], "DataGrid_Body_Cell_clicked")) {
            dojo.removeClass(this.tableNode.rows[i].cells[j], "DataGrid_Body_Cell_clicked");
          }
        }
      }
    }
  },

/**
* This method is used by scroll driver to cancel the edition of the cells when it is scrolling.
* @param {EventHandler} evt - event handler
*/
  handleScroll: function(evt) {
    if (this.editing){
      this.cancelEdit();
    }
  },

/**
*It identifies if a row number is inside the range of visible rows.
* @param {Number} rowNo - Row number.
* @return True: if is in range - False: else.
* @type Boolean
*/
  isVisible: function(rowNo) {
    var contentOffset = this.getCurrentOffset();
    var maxOffset = contentOffset + this.numRows; 
    return contentOffset <= rowNo && rowNo <= maxOffset;
  },

/**
* It returns the object that points at the cell in which there is contained the node that is given to him. It is used on the events onClick, onDblClick and on the selection, to obtain the cell without worrying about if it has been clicked on a son node or on the proper td one.
* @param {Object} node - the node
* @return the correct node.
* @type Object
*/
  _getContainerCell: function(node) {
    var isNobr = false;
    try {
      isNobr = (node.nodeName.toLowerCase() == "nobr");
    } catch (ignored) {isNobr = false;}
    while (node && (!dojo.hasClass(node, 'DataGrid_Body_Cell_hover') || isNobr)) {
      node = openbravo.widget.DataGrid.html.getParentByType(node, "td");
      if (node && !dojo.hasClass(node, 'DataGrid_Body_Cell_hover')){
        node = node.parentNode;
      }
      try {
        isNobr = (node.nodeName.toLowerCase() == "nobr");
      } catch (ignored) {isNobr = false;}
    }
    return node;
  },

/**
* It is the method that manages the double click on a cell. In a editable grid the cell will be opened in edition mode, using the function editCell. If the grid is not editable, it will call to the function onRowDblClick, to which the cell will be given as a parameter. The function onRowDblClick is a user's function so it have to be implementated by the user for the concrete case.
* @param {EventHandler} evt - event handler
*/
  cellDoubleClicked: function(evt) {
    isClickOnGrid=true;
    focusGrid();
    currentWindowElementType = 'grid';
    setWindowElementFocus('grid_table_dummy_input','id');
    var node = evt.target;
    var isCell = node.nodeName.toLowerCase() == "td";
    if (!isCell) {
      node = this._getContainerCell(node);
      isCell = (node.nodeName.toLowerCase() == "nobr");
    }
    if (this.editable && node && node != this.editingCell){
      this.editCell(node);
    }else if (node){
      onRowDblClick(node);
    }
  },

/**
* It manages the event click on a cell. For it, it will control if someone was editing another cell, saving it, also it will control if the row has been changed, to save the row (in case something had been modified in the previous row) and, finally, set as selected the clicked cell.
* @param {EventHandler} evt - event handler
*/
  cellClicked: function(evt) {
    isClickOnGrid=true;
    focusGrid();
    currentWindowElementType = 'grid';
    setWindowElementFocus('grid_table_dummy_input','id');
    if (this.locked) { return; }
    this.lastHoveredColumn = null;
    var isCell = (evt.target.nodeName.toLowerCase() == "td");
    var cell = evt.target;
    if (!isCell) {
      cell = this._getContainerCell(cell);
      isCell = (evt.target.nodeName.toLowerCase() == "nobr");
    }
    if (!cell) { return; }
    var rowNode = cell.parentNode;
    var offset = this.getCurrentOffset() + rowNode.rowIndex;
    var success = true;
    if (this.editing && cell && this.editingCell != cell) {
      success = this.saveCell();
    }
    if (this.editingRow && this.editingRow.offset != offset && isCell && success) {
      success = this.saveRow();
    }
    if (isCell && (!this.editingRow || !this.editingRow.error) && success) {
      this.handleSelection(evt);
    }
    checkAttachmentIconRelation();
  },

/**
* It is the method that controls the selection of rows. It is which is called from the method cellClicked to manage the marked one with the clicked. Between the operations that it realizes, they are controlling the multiple selections one, which can cause a call to the backend to obtain the list of ids selected (when there are ids that are not in the visible range).
* @param {EventHandler} evt - event handler
*/
  handleSelection: function(evt) {
    var isCell = (evt.target.nodeName.toLowerCase() == "td");
    var cell = evt.target;
    if (!isCell) {
      cell = this._getContainerCell(cell);
    }
    var rowNode = cell.parentNode;
    if (!rowNode) { return; }
    if ((!evt.ctrlKey && !evt.shiftKey) || this.multipleRowSelection==false){
      this.selectedRows.clear();
    }
    var rowNo = this.getCurrentOffset() + rowNode.rowIndex;
    var gridRow = this.buffer.getRow(rowNo);
    if (!gridRow) { return; }
    this.tableNode.focus();
    if (evt.shiftKey) {
      if(this.selectedRows.count() > 0) {
        var contentOffset = this.getCurrentOffset();
        var maxOffset = contentOffset + this.numRows;
        var lastSelectedRow = this.selectedRows.getLastSelected();
        var lastAddedRowOffset = lastSelectedRow.offset;
        var selectedRowOffset = gridRow.offset;
        var minValue = (lastAddedRowOffset >=  selectedRowOffset ? selectedRowOffset : lastAddedRowOffset);
        var maxValue = (lastAddedRowOffset >=  selectedRowOffset ? lastAddedRowOffset : selectedRowOffset);
        if (contentOffset <= lastAddedRowOffset && lastAddedRowOffset < maxOffset
          && contentOffset <= selectedRowOffset && selectedRowOffset < maxOffset){
          var numRows = maxValue - contentOffset;
          for (var i = (minValue - contentOffset); i < numRows; i++) {
            var currentRow = this.buffer.getRow(contentOffset + i);
            this.addSelectedRow(currentRow.getValue(this.columns.getIdentifier().name), contentOffset + i);
          }
        } else {
          var serviceUrl = {
            url: this.dataUrl,
            handler: dojo.hitch(this, "idsReceived", minValue),
            method: "POST",
            handleAs: "xml"
          };
          var content = [];
          if (this.requestParams) {
            for (var param in this.requestParams) {
              content[param] = this.requestParams[param];
            }
          }
          content["action"] = "getIdsInRange";
          content["minOffset"] = minValue;
          content["maxOffset"] = maxValue;
          if (this.sortCols) {
            content["sort_cols"] = this.sortCols;
            content["sort_dirs"] = this.sortDirs;
          }
          openbravo.widget.DataGrid.io.asyncCall(serviceUrl, content);
        }
      }
    }
    if (evt.ctrlKey && dojo.hasClass(rowNode, "DataGrid_Body_Row_focus")) {
      this.selectedRows.remove(gridRow.getValue(this.columns.getIdentifier().name));
    } else {
      var row = {id: gridRow.getValue(this.columns.getIdentifier().name), offset: rowNo};
      this.selectedRows.add(row);
      this.selectedCell = dojo.indexOf(cell.parentNode.cells, cell);
    }
    this.showSelection();
    if (evt.shiftKey || evt.ctrlKey){
      dojo.stopEvent(evt);
    }
  },

/**
* It adds  to the array of selected rows, the new selected row. It receives as parameters the value of the index field of the row and the offset. This method is called from the methods handleSelection and idsReceived.
* @param {String} id - Identifier of the selected row.
* @param {Number} offset - Offset of the row.
*/
  addSelectedRow: function(id, offset) {
    var row = {id: id, offset: offset};
    this.selectedRows.add(row);
  },

/**
* It is the method that manages the response of the backend call when the out of range ids are requested. This method is called from other methods.
*/
  idsReceived: function(minOffset, data, evt) {
    var xmlrangeid = data.getElementsByTagName('xml-rangeid');
    var status = xmlrangeid[0].getElementsByTagName('status');
    if (status.length>0){
      var type = status[0].getElementsByTagName('type');
      var title = status[0].getElementsByTagName('title');
      var description = status[0].getElementsByTagName('description');
      if (dojox.data.dom.textContent(type[0]).toUpperCase() != 'HIDDEN') {
        try {
          renderMessageBox(dojox.data.dom.textContent(type[0]),dojox.data.dom.textContent(title[0]),dojox.data.dom.textContent(description[0]));
        } catch (err) {
          alert(dojox.data.dom.textContent(title[0]) + ":\n" + dojox.data.dom.textContent(description[0]));
        }
      }
    }
    var ids = xmlrangeid[0].getElementsByTagName('id');
    for (var i = 0; i < ids.length; i++) {
      this.addSelectedRow(dojox.data.dom.textContent(ids[i]), minOffset + i);
    }
    this.showSelection();
  },

/**
* It will be thrown when the enter key is pressed on a cell. It does the exit of the edition if a cell was being edited, or the entry in current cell edition.
*/
  enterKeyPressed: function() {
    if (this.editing) {
      this.saveCell();
      setTimeout( dojo.hitch(this, function() {
        this.setFocus();
      }), 200);
    } else {
      var lastSelectedRow = this.selectedRows.getLastSelected();
      if (lastSelectedRow != null) {
        var rowOffset = lastSelectedRow.offset;    
        var currentOffset = this.getCurrentOffset();
        if (rowOffset < currentOffset || rowOffset >= (currentOffset + this.numRows)) {
          this.goToRow(rowOffset);
        } else {
          var rowNo = rowOffset - this.getCurrentOffset();
          if (rowNo >= 0){
            this.editCell(this.tableNode.rows[rowNo].cells[this.selectedCell]);
          }
        }
      }
    }
  },

/**
* Handler to the startEditingCell function.
* @param {Number} rowOffset - Row offset.
* @return Handler to the startEditingCell function.
* @type Handler
*/
  getStartEditingFunction: function(rowOffset) {
    var _this = this;
    var hitch =  dojo.hitch(_this, "startEditingCell", rowOffset, hitch);
    return hitch;
  },

/**
* Manage the begining of the edition of a cell
* @param {Number} rowOffset - row offset
* @param {Handler} hitch - handler to itself
*/
  startEditingCell: function(rowOffset, hitch) {
    this.options.onRefreshComplete.remove(hitch);
    var currentOffset = this.getCurrentOffset();
    var rowNo = rowOffset - currentOffset;
    if (rowNo >= 0) {
      this.editCell(this.tableNode.rows[rowNo].cells[this.selectedCell]);
    }
  },

/**
* It associates the keys with the corresponding methods, in order that they are executed to the pulsation of these. For example, to associate the method deleteRow to the key delete.
* @return Key mapping object
* @type keyMapping
*/
  _getKeyMapping: function() {
    if (!this.keyMapping) {
      var k = dojo.keys;
      this.keyMapping = [];
      var m = this.keyMapping;
      /* Remove to let the shortcuts.js to do the job
      m[k.KEY_ESCAPE] = this.cancelEdit;
      m[k.KEY_ENTER] = this.enterKeyPressed;
      m[k.KEY_INSERT] = this.addNewRow;
      m[k.KEY_DELETE] = this.deleteRow;
      m[k.KEY_UP_ARROW] = this.goToPreviousRow;
      m[k.KEY_DOWN_ARROW] = this.goToNextRow;
      m[k.KEY_LEFT_ARROW] = this.goToLeftCell;
      m[k.KEY_RIGHT_ARROW] = this.goToRightCell;
      m[k.KEY_HOME] = this.goToFirstRow;
      m[k.KEY_END] = this.goToLastRow;
      m[k.KEY_PAGE_UP] = this.goToPreviousPage;
      m[k.KEY_PAGE_DOWN] = this.goToNextPage;
      for (var key in m) {
        m[key] = dojo.hitch(this, m[key]);
      }*/
    }
    return this.keyMapping;
  },

/**
* It defines the array of keys which provoke the paralysation of the events in execution. It is a question of the events that will capture the grid and must not allow that they should go off up, continuing with its normal execution in the browser.
* @return Array of keys
* @type Array
*/
  _getKeyEventsToStop: function() {
    if (!this.keyEventsToStop) {
      var k = dojo.keys;
      this.keyEventsToStop = [k.KEY_UP_ARROW, k.KEY_DOWN_ARROW,
        k.KEY_HOME, k.KEY_END];
    }
    return this.keyEventsToStop;
  },

/**
* It defines the array of keys allowed in the edition mode.
* @return Array of allowed keys.
* @type Array.
*/
  _allowedKeys: function() {
    var k = dojo.keys;
    return [k.KEY_ENTER, k.KEY_ESCAPE];
  },

/**
* It manages the pulsation of a key on the grid. It will purify if it is a question of someone of the allowed keys, if a method has associated to shoot and if it is a question of one of the keys that they must be captured in order that it does not continue its execution in the browser.
* @param {EventHandler} evt - event handler
*/
  keyPressed: function(evt) {
    if (this.locked) { return; }
    var k = dojo.keys;
    var keyCode = evt.keyCode;
    var stopEvent = openbravo.widget.DataGrid.html.inArray(this._getKeyEventsToStop(), keyCode);
    if (this.editing && !openbravo.widget.DataGrid.html.inArray(this._allowedKeys(), keyCode)) { return; }
    var handler = this._getKeyMapping()[keyCode];
    if (handler){
      handler();
    }
    if (stopEvent || keyCode == k.KEY_ENTER && dojo.isIE){
      dojo.stopEvent(evt);
    }
  },

/**
* It is a small additional control to save the information in case of leaving of the page having the edition by half.
* @param {EventHandler} evt - event handler
*/
  onPageUnload: function(evt) {
    if (this.editingRow){
      this.saveRow();
    }
  },

/**
* It moves to the previous cell inside the same row. It will be associated with the left cursor key.
*/
  goToLeftCell: function() {
    this.moveSelectedCell(-1);
  },

/**
* It moves to the next cell inside the same row. It will be associated with the right cursor key.
*/
  goToRightCell: function() {
    this.moveSelectedCell(1);
  },

/**
* It is the one that realizes the movement of the area to a new cell, managing the effects of visualization. It is called by the methods goToLeftCell and goToRightCell.
* @param {Number} mov - number of moved cells
*/
  moveSelectedCell: function(mov) {
    if (!this.editing) {
      var validRow = false;
      var cellNo = this.selectedCell + mov;
      while(cellNo >= 0 && cellNo < this.columns.count() && !validRow) {
        validRow = this.checkCell(cellNo);
        if (!validRow){
          cellNo += mov;
        }
      }
      if (validRow) {
        this.selectedCell = cellNo;
        this.showSelection();
        dijit.scrollIntoView(this.tableNode.rows[0].cells[this.selectedCell]);
      }
    }
  },

/**
* It verifies that a number of cell exists (> 0 and < column_number). If it exists it verifies that it is visible, returning true or false. In any another case, it will return false. This method is used by moveSelectedCell.
* @param {Number} cellNo - cell number
* @return True: if is visible - False: if is not visible
* @type Boolean
*/
  checkCell: function (cellNo) {
    if (cellNo >= 0 && cellNo < this.columns.count()) {
      var column = this.columns.get(cellNo);
      return column.visible;
    } else {
      return false;
    }
  },

/**
* It manages the positioning of the cursor on a column header. It will do the highlighted of the cell and, in case of having selected records and be a numerical column (integer or float), it will throw the call to the backend to obtain the column total.
* @param {EventHandler} evt - event handler
*/
  hoverCellHeader: function(evt) {
    if (!dojo.hasClass(evt.target, 'DataGrid_Header_Cell_hover')){
      dojo.addClass(evt.target, 'DataGrid_Header_Cell_hover');
    }
    var header = openbravo.widget.DataGrid.html.getParentByType(evt.target, "th");
    var columnNo = dojo.indexOf(header.parentNode.cells, header);
    var column = this.columns.get(columnNo);
    this.drawTotalNumber("", header);
    if (column != null && this.selectedRows.count() > 0) {
      var index = column.index;
      if (!this.lastHoveredColumn || this.lastHoveredColumn.index != index) {
        var type = column.type.name;
        if (type == "integer" || type == "float") {
          this.lastHoveredColumn = column;
          var dataInBuffer = this.selectedRows.count() > 0;
          var total = 0;
          var selectedRows = this.selectedRows.getIterator();
          while (!selectedRows.atEnd() && dataInBuffer){
            var currentRow = selectedRows.get();
            var offset = currentRow.value.offset;
            if (this.buffer.isInRange(offset)) {
              var bufferedRow = this.buffer.getRow(offset);
              if (returnFormattedToCalc) {
                total += parseFloat(returnFormattedToCalc(bufferedRow.getValue(index)));
              } else {
                total += parseFloat(bufferedRow.getValue(index));
              }
            } else { dataInBuffer = false; }
          }
          if (!dataInBuffer){
            this.requestColumnTotals(column.name);
          } else {
            this.lastAddition = total;
            this.drawTotalNumber(this.lastAddition, header);
          }
        } else {
          this.drawTotalNumber("", header);
        }
      } else {
        var type = this.lastHoveredColumn.type.name;
        if (type == "integer" || type == "float"){
          this.drawTotalNumber(this.lastAddition, header);
        }
      }
    }
  },

/**
* Updated just the drawn total number.
* @param {String} number - the number to show
* @param {Object} header - the associated column header
*/
  drawTotalNumber : function (number, header) {
    window.status = number;
    if (header) {
      header.setAttribute('title', number);
    }
  },

/**
* It is the method that calls to the backend to obtain the column total of numerical column. It is called by the hoverCellHeader method.
* @param {String} rowName - name of the row
*/
  requestColumnTotals: function(rowName){
    // workaround request sent with rows='undefined' when no rows are selected
    var rows = this.getSelectedRows();
    if (rows == "") {
      rows = "";
    }
    setCalloutProcessing(true);
    var handlerRef = dojo.hitch(this, "showColumnTotals");
    var params = [];
    params["action"] = "getColumnTotals";
    params["columnName"] = rowName;
    params["rows"] = rows;
    var serviceUrl = {
      url: this.dataUrl,
      handler: handlerRef,
      method: "POST",
      handleAs: "xml"
    };
    openbravo.widget.DataGrid.io.asyncCall(serviceUrl, params);
  },

/**
* It manages the header class update to remove the hovered effect.
* @param {EventHandler} evt - event handler
*/
  plainCellHeader: function(evt) {
    if (dojo.hasClass(evt.target, 'DataGrid_Header_Cell_hover')){
      dojo.removeClass(evt.target, 'DataGrid_Header_Cell_hover', false);
    }
    window.status = "";
  },

/**
* It is the method that manages the response of the backend with the column total.
* @param {String} type - 
* @param {XML Structure} data - data structure
* @param {EventHandler} evt - event handler
*/
  showColumnTotals: function(data, evt) {
    this.lastAddition = openbravo.widget.DataGrid.html.getContentAsString(data.getElementsByTagName('total')[0]);
    window.status = parseFloat(this.lastAddition);
  },

/**
* It manages the grid width, taking into account the scrollbars.
*/
  setBounds: function() {
    var maxWidthInt = openbravo.widget.DataGrid.html.extractPx(this.maxWidth);
    if (maxWidthInt > 0 && maxWidthInt <= this.domNode.offsetWidth) {
      this.tableNode.parentNode.style.width = this.maxWidth;
      this.domNode.style.width = maxWidthInt +
        2 * this.scrollWidth + 8 + "px";
    }
  },

/**
* It eliminates the onmouseover and onmouseout events of all grid cells. This method is associated with the onUnload  event of the page, to realize a clean of javascript.
*/
  cleanup: function() {
    var table = this.tableNode;
    for (var i = 0; i < table.rows.length; i++) {
      for (var j = 0; j < table.rows[i].cells.length; j++) {
        var cell = table.rows[i].cells[j];
        cell.onmouseover = null;
        cell.onmouseout = null;
      }
    }
  },

/**
* It does the necessary operations to calculate the maximum grid width.
* @return Max width
* @type Number
*/
  setMaxWidth: function() {
    if(this.percentageWidthRelativeToId != "body") {
      var maxWidthInt = Math.round(this.proportion * document.getElementById(this.percentageWidthRelativeToId).clientWidth);
    } else {
      var maxWidthInt = Math.round(this.proportion * dijit.getViewport().width);
    }
    maxWidthInt -= 2 * this.scrollWidth;
    this.maxWidth = maxWidthInt + "px";
    return maxWidthInt;
  },

/**
* It manages the grid resizing.
*/
  onResize: function() {
    this.setMaxWidth();
    if (this.numRows != calculateNumRows()) {
      this.numberOfResizes += 1;
      this.offsetBeforeResize = this.getCurrentOffset();
      this.hasBeenResized = true;
      this.numRows = calculateNumRows();
      this.visibleRows = this.numRows;
      this.visibleRowsMax = this.numRows;
      document.getElementById(this.widgetId + '_container').innerHTML = ""; 
      document.getElementById(this.widgetId + '_container').style.display='none';
      document.getElementById(this.widgetId + '_container').setAttribute('id',this.widgetId + '_container_old' + this.numberOfResizes);
      this.render();
    }

    if(this.percentageWidthRelativeToId != "body") {
      var desiredWidth = Math.round(this.proportion * document.getElementById(this.percentageWidthRelativeToId).clientWidth);
    } else {
      var desiredWidth = Math.round(this.proportion * dijit.getViewport().width);
    }
    this.tableNode.parentNode.style.width = desiredWidth - 
      2 * this.scrollWidth + "px";
    this.domNode.style.width = desiredWidth - 
      2 * this.scrollWidth + 40 + "px";
  },

/**
* It does the painting the grid in the page. This method is necessary in dojo for the control construction.
*/
  render: function() {
    var hoverCell = function(evt) {
      if (dojo && !dojo.hasClass(this, 'DataGrid_Body_Cell_hover')
        && !dojo.hasClass(this, 'DataGrid_Body_Cell_clicked')){
        dojo.addClass(this, 'DataGrid_Body_Cell_hover');
      }
    };

    var plainCell = function(evt) {
      if (dojo && dojo.hasClass(this, 'DataGrid_Body_Cell_hover')){
        dojo.removeClass(this, 'DataGrid_Body_Cell_hover', false);
      }
    };

    var gridId = this.widgetId;
    var tableHeader = document.createElement("table");
    if(isGridFocused) { tableHeader.className = 'DataGrid_Header_Table_focus'; }
    else { tableHeader.className = 'DataGrid_Header_Table'; }
    if (this.sortable){
      tableHeader.id = gridId + '_table_header';
    }
    tableHeader.cellspacing = 0;
    tableHeader.cellpadding = 0;
    var thead = document.createElement('tbody');
    var numCols = this.columns.count();
    var row = document.createElement("tr");
    var totalWidth = 0;
    for(var j = 0; j < numCols; j++) {
      var cell = document.createElement("th");
      var colMetadata = this.columns.get(j);
      cell.className = openbravo.widget.DataGrid.Column.prototype.DEFAULT_HEADER_CLASS;
      dojo.addClass(cell, colMetadata.headerClassName);
      dojo.connect(cell, "onmouseover", this, "hoverCellHeader");
      dojo.connect(cell, "onmouseout", this, "plainCellHeader");
      dojo.connect(cell, "onmousedown", this, "resizeHeader");

      var s = colMetadata.title;
      cell.innerHTML = (s && typeof s == 'string')? s.split(" ").join("&nbsp;") : s; 
      if (colMetadata.visible) {
        cell.style.width = colMetadata.width;
        var end = colMetadata.width.indexOf("px");
        var rowWidth = colMetadata.width.substring(0, end);
        totalWidth += parseInt(rowWidth);
      }else{
        cell.style.display = "none";
      }
      row.appendChild(cell);
    }
    thead.appendChild(row);
    tableHeader.style.width = totalWidth + 'px';
    tableHeader.appendChild(thead);
    var table = document.createElement("table");
    table.id = gridId + "_table";
    table.cellspacing = "0";
    table.cellpadding = "0";
    if(isGridFocused) { table.className = 'DataGrid_Body_Table_focus'; }
    else { table.className = 'DataGrid_Body_Table'; }
    table.style.width = totalWidth + 'px';
    var tbody = document.createElement("tbody");
    for (var i = 0; i < this.numRows; i++) {
      var row = document.createElement("tr");
      row.className = 'DataGrid_Body_Row';
      dojo.addClass(row,( i % 2 == 0 ? "DataGrid_Body_Row_Even" : "DataGrid_Body_Row_Odd"));
      dojo.connect(row, "onclick", this, "cellClicked");
      dojo.connect(row, "ondblclick", this, "cellDoubleClicked");
      for(var j = 0; j < numCols; j++) {
        var cell = document.createElement("td");
        var colMetadata = this.columns.get(j);
        cell.className = openbravo.widget.DataGrid.Column.prototype.DEFAULT_CLASS;
        dojo.addClass(cell, colMetadata.className);
        cell.onmouseover = hoverCell;
        cell.onmouseout = plainCell;
        openbravo.widget.DataGrid.html.disableSelection(cell);
        var emptyText = document.createTextNode("");
        cell.appendChild(emptyText);
        if (colMetadata.visible){
          cell.style.width = colMetadata.width;
        }else{ 
          cell.style.display = "none";
        }
        row.appendChild(cell);
      }
      tbody.appendChild(row);
    }
    table.appendChild(tbody);
    table.focus();
    this.tableNode = table;
    this.tableHeader = tableHeader;
    if (dojo.isIE && !this.hasBeenResized) {
      dojo.connect(this.domNode, "onkey", this, "keyPressed");
    } else if (!this.hasBeenResized) {
      dojo.connect(document, "onkey", this, "keyPressed");
    }
    var container = this.domNode;
    var gridContainer = document.createElement("div");
    gridContainer.id = gridId + "_container";
    gridContainer.innerHTML = "<div style='float:left'></div>";
    var viewPort = gridContainer.firstChild;
    viewPort.id = gridId + "_viewPort";
    viewPort.style.overflow = "hidden";
    viewPort.appendChild(tableHeader);
    viewPort.appendChild(table);
    container.appendChild(gridContainer);
    this.setBounds();
    table.style.height = (row.offsetHeight + 1) * (this.numRows);
    this.domNode.style.height = table.offsetHeight + "px";
    if (!this.hasBeenResized) {
      var sortColsName = new Array();
      if (this.sortCols!=null && this.sortCols!="") {
        var auxSortCols = this.sortCols.split(",");
        var totalSortCols = auxSortCols.length;
        for (var i=0; i<totalSortCols;i++) {
          sortColsName[i] = this.columns.get(auxSortCols[i]).name;
        }
      }
      /*var auxRow = {id: this.defaultRow, offset: this.offset};
        this.selectedRows.add(auxRow);*/
      var opts = {
        prefetchBuffer : true,
        offset: this.offset, 
        columns: this.columns, 
        sortCols: sortColsName,
        sortDirs: ((this.sortDirs!=null && this.sortDirs!="")?this.sortDirs.split(","):[]),
        defaultRow: this.defaultRow,
        onscroll : this.onScroll
      };
    } else {
      var opts = {
        prefetchBuffer : true,
        offset: this.offset, 
        columns: this.columns, 
        defaultRow: this.defaultRow,
        onscroll : this.onScroll
      };
    }

    this.postRendering(opts);
    if (this.proportion){
      dojo.connect(window, "onresize", this, "onResize");
    }
    dojo.connect(this.scroller, "handleScroll", this, "handleScroll");
    dojo.addOnUnload(this, "cleanup");
    setCalloutProcessing(false);
  },

/**
* It is an auxiliary method for do the event capture for the grid. It calls to the dojo function which does this purpose.
* @param {String} eventName - event handler
* @param {Object} handler - handler
*/
  captureEvent: function(eventName, handler) {
    dojo.connect(this.tableNode, eventName, handler);
  },

/**
* It returns an array with the selected rows identifiers. It uses the internal array of selected rows as origin of data (it is the one that is refilled with the addSelectedRow method).
* @return Array with the selected rows ids
* @type Array
*/
  getSelectedRows: function() {
    var selectedRowsIds = [];
    this.selectedRows.forEach(function(row) {
      selectedRowsIds.push(row.value.id);
    });
    return selectedRowsIds;  
  },

/**
* It returns an array with the selected rows offsets. It uses the internal selected rows array as origin of data (it is the one that is refilled with the method addSelectedRow).
* @return Array with the selected positions
* @type Array
*/
  getSelectedRowsPos: function() {
    var selectedRowsPos = [];
    this.selectedRows.forEach(function(row) {
      selectedRowsPos.push(row.value.offset);
    });
    return selectedRowsPos;  
  },

/**
* It verifies if some record is edited, throwing the saveRow in affirmative case.
* @return False: if try to save and fails - True: any other cases
* @type Boolean
*/
  checkEditingRow: function() {
    return this.editingRow ? this.saveRow() : true;
  },

/**
* It moves the focus to the previous row. It will be associated with the key cursor up.
*/
  goToPreviousRow: function() {
    if (this.checkEditingRow()){
      this.moveFromLastSelected(-1);
    }
  },

/**
* It moves the focus to the following row. It will be associated with the key cursor down.
*/
  goToNextRow: function() {
    if (this.checkEditingRow()){
      this.moveFromLastSelected(1);
    }
  },

/**
* It moves the focus to the first row of the grid. It will be associated with the key home.
*/
  goToFirstRow: function() {
    if (this.checkEditingRow()){
      this.goToRow(0);  
    }
  },

/**
* It moves the area to the last row of the grid. It will be associated with the key end.
*/
  goToLastRow: function() {
    if (this.checkEditingRow()){
      this.goToRow(this.metaData.totalRows - 1);
    }
  },

/**
* It moves the area to the previous visible page.
*/
  goToPreviousPage: function() {
    if (!this.editing){
      this.moveCurrentPosition(-this.numRows);
    }
  },

/**
* It moves the area to the next visible page.
*/
  goToNextPage: function() {
    if (!this.editing){
      this.moveCurrentPosition(this.numRows);
    }
  },

/**
* It puts the page focus in the grid, selecting the first record, in case any selected record exists.
*/
  setFocus: function() {
    this.tableNode.focus();
    if (this.selectedRows.count() == 0){
      this.goToFirstRow();
    }
    else { this.goToRow(this.selectedRows.getLastSelected().offset); }
    this.showSelection();
  },

/**
* It establishes the order for a column as determinated, realizing the needed operations for the visualization.
* @param {Number} columnId - column id
* @param {String} sortOrder - it could be "ASC" or "DESC" depending if the order is ascending or descendinf
*/
  setSortedColumns: function(columnId, sortOrder) {
    this.sort.setColumnSort(columnId, ((sortOrder=="DESC")?openbravo.widget.DataGrid.Column.SORT_DESC:openbravo.widget.DataGrid.Column.SORT_ASC));
    this.sortHandler(this.sortColumns);
  },

/**
* It adds a new record to the grid.
*/
  addNewRow: function() {
    if (!this.checkEditingRow()) { return; }
    if (this.editingRow && this.editingRow.isNewRow) { return; }
    var handler = dojo.hitch(this, function() {
      this.options.onRefreshComplete.remove(handler);
      var contentOffset = this.getCurrentOffset();
      var rowNo = this.numRows - 1 + contentOffset;
      var pos = rowNo - this.buffer.startPos;
      this.editingRow = this.createRowObject(this.metaData.totalRows - 1);
      var values = [];
      for ( var i=0; i < this.metaData.columnCount; i++ ){ 
        values[i] = "";
      }
      this.editingRow.setValues(values);
      this.editingRow.isNewRow = true;
      this.editingRow.rowNode = this.tableNode.rows[this.visibleRows - 1];
      this.editingRow.setValue(0, this.metaData.totalRows);
      this.editingRow.getDefaultValues(this.dataUrl);
      this.buffer.rows[pos] = this.editingRow;
      this.selectRow(this.metaData.totalRows - 1);
    });
    this.options.onRefreshComplete.push(handler);
    this.metaData.totalRows++;
    this.setTotalRows(this.metaData.totalRows);
    this.goToRow(this.metaData.totalRows - 1);
  },

/**
* It eliminates records of the grid. This method will be associated with the delete key. It calls the backend in order that it realizes the necessary operations for the elimination of the same one.
*/
  deleteRow: function() {
    if (this.selectedRows.count() > 0 &&
      this.selectedRows.getLastSelected() != null && showJSMessage(2)) {
      if (this.editing){
        this.cancelEdit();
      }
      this.editingRow = null;
      var content = [];
      content["action"] = "deleteRow";
      var rows = [];
      this.selectedRows.forEach(dojo.hitch(this, function(row) {
        if (row.value.id){
          rows.push(row.value.id);
        }
        //this.metaData.totalRows--;
      }));
      if (rows.length > 0) {
        content["rows"] = rows;
        //this.selectedRows.clear();
        var handlerRef = dojo.hitch(this, "refreshGridDataAfterDelete");
        var serviceUrl = {
          url: this.updatesUrl,
          handler: handlerRef,
          method: "POST",
          handleAs: "xml"
        };
        openbravo.widget.DataGrid.io.asyncCall(serviceUrl, content);
      }
      else{
        this.refreshGridData();
      }
    }
  },

/**
* It is the method that manages the response of the backend for the elimination of records. It updates the grid to eliminate its missing record, as well as, to update the whole of existing records in the grid.
* @param {String} type - 
* @param {XML Structure} data - data structure
* @param {EventHandler} evt - event handler
*/
  refreshGridDataAfterDelete: function(data, evt) {
    var xmldelete = data.getElementsByTagName('xml-delete');
    var status = xmldelete[0].getElementsByTagName('status');
    if (status.length>0){
      var type = status[0].getElementsByTagName('type');
      var title = status[0].getElementsByTagName('title');
      var description = status[0].getElementsByTagName('description');
      if (dojox.data.dom.textContent(type[0]).toUpperCase() != 'HIDDEN') {
        try {
          initialize_MessageBox('messageBoxID');
        } catch (ignored) {}
        try {
          setValues_MessageBox('messageBoxID',dojox.data.dom.textContent(type[0]), dojox.data.dom.textContent(title[0]), dojox.data.dom.textContent(description[0]));
        } catch (err) {
          alert(dojox.data.dom.textContent(title[0]) + ":\n" + dojox.data.dom.textContent(description[0]));
        }
      }
    }
    var info = xmldelete[0].getElementsByTagName('info');
    var result = info[0].getElementsByTagName('result');
    var total = info[0].getElementsByTagName('total');
    if (dojox.data.dom.textContent(result[0]) != 0){
      this.metaData.totalRows = this.metaData.totalRows - parseInt(dojox.data.dom.textContent(total[0]));
      this.selectedRows.clear();
      this.refreshGridData();
    }
    this.onResize();
  },

/**
* It manages the response of the backend after the filtrate.
*/
  refreshGridDataAfterFilter: function() {
    this.buffer.isFilter=true;
    this.selectedRows.clear();
    this.isFirstLoad = true;
    selectedRow = 0;
    this.setTotalRows(this.visibleRowsMax);
    this.moveTableContent(0);
    this.isIE = dojo.isIE;
    if(!this.isIE){
      this.goToFirstRow();
    }
    return true;
  },

/**
* It updates the visualization of the grid, rewriting the classes in every row...
*/
  refreshGridData: function() {
    for (var i = 0; i < this.numRows; i++) {
      if(isGridFocused) {
        openbravo.widget.DataGrid.html.prereplaceClass(this.tableNode.rows[i], 
          i % 2 == 0 ? "DataGrid_Body_Row_Even" : "DataGrid_Body_Row_Odd",
          "DataGrid_Body_Row_focus");
      } else {
        openbravo.widget.DataGrid.html.prereplaceClass(this.tableNode.rows[i], 
          i % 2 == 0 ? "DataGrid_Body_Row_Even" : "DataGrid_Body_Row_Odd",
          "DataGrid_Body_Row_selected");
      }
    }
    var contentOffset = this.getCurrentOffset();
    if (contentOffset > this.metaData.totalRows - this.numRows){
      contentOffset = this.metaData.totalRows - this.numRows;
    }
    if (contentOffset < 0){
      contentOffset = 0;
    }
    this.setTotalRows(this.metaData.totalRows);
    this.moveTableContent(contentOffset);
  },

/**
* It display the grid focused.
*/
  focusGrid: function() {
    isGridFocused = true;
    openbravo.widget.DataGrid.html.prereplaceClass(this.table, 'DataGrid_Body_Table_focus', 'DataGrid_Body_Table');
    openbravo.widget.DataGrid.html.prereplaceClass(this.tableHeader, 'DataGrid_Header_Table_focus', 'DataGrid_Header_Table');
    for (var i = 0; i < this.numRows; i++) {
      if (this.tableNode.rows[i].className.indexOf('DataGrid_Body_Row_selected') != -1) {
        openbravo.widget.DataGrid.html.prereplaceClass(this.tableNode.rows[i], 'DataGrid_Body_Row_focus', 'DataGrid_Body_Row_selected');
      }
    }
    return true;
  },

/**
* It display the grid blured.
*/
  blurGrid: function() {
    isGridFocused = false;
    openbravo.widget.DataGrid.html.prereplaceClass(this.table, 'DataGrid_Body_Table', 'DataGrid_Body_Table_focus');
    openbravo.widget.DataGrid.html.prereplaceClass(this.tableHeader, 'DataGrid_Header_Table', 'DataGrid_Header_Table_focus');
    for (var i = 0; i < this.numRows; i++) {
      if (this.tableNode.rows[i].className.indexOf('DataGrid_Body_Row_focus') != -1) {
        openbravo.widget.DataGrid.html.prereplaceClass(this.tableNode.rows[i], 'DataGrid_Body_Row_selected', 'DataGrid_Body_Row_focus');
      }
    }
    return true;
  },

/**
* It is an utility to modify the content of a cell.
* @param {Number} rowNo - number of row
* @param {Number} column - id of the column
* @param {String} value - new vale
*/
  setCellContent: function(rowNo, column, value) {
    var row = null;
    if (this.buffer.isInRange(rowNo)) {
      row = this.buffer.getRow(rowNo);
    }
    if (row != null) {
      var selectedColumn = this.columns.get(column);
      if (!isNaN(value)){
        value = parseFloat(value);
      }
      row.setValue(selectedColumn.index, value);
      row.sendRow();
    }
  },

/**
* It moves the area to a certain row. It is the method that they use goToPreviousRow and goToNextRow.
* @param {Number} mov - number of positions moved
*/
  moveFromLastSelected: function(mov) {
    if (this.selectedRows.count() > 0) {
      var lastRowOffset = this.selectedRows.getLastSelected().offset;
      var newRowNo = lastRowOffset + mov;
      if (newRowNo >= 0 && newRowNo < this.metaData.totalRows){
        this.goToRow(newRowNo);
      }
    }
  },

/**
* It set a row as selected, deleting the rest of selections. It registers the new row in the internal array of selected rows, deleting the rest of selected rows.
* @param {Number} rowNo - number of row
* @param {Handler} hitch - handler to itself
*/
  selectRow: function(rowNo, hitch) {
    var row = this.buffer.getRow(rowNo);
    if (!row){
      row = this.createRowObject(null);
    }
    var id = row.getValue(this.columns.getIdentifier().name);
    if (escape(id) != "%A0") {
      if (hitch){
        this.options.onRefreshComplete.remove(hitch);
      }
      var newRow = {id: id, offset: rowNo};
      this.selectedRows.clear();
      this.selectedRows.add(newRow);
      this.showSelection();
    }
  },

/**
* It is a visualization method that allows to obtain the scroll offset.
* @return Current offset
* @type Number
*/
  getCurrentOffset: function() {
    return parseInt(this.scroller.scrollerDiv.scrollTop /  this.viewPort.rowHeight);
  },

/**
* It adds parameters in order that be sent to the backend with the requests.
* @param {String} params - parameters
*/
  setRequestParams: function(params) {
    this.requestParams = params;
  },

/**
* It moves the grid scroll to a concrete row.
* @param {Number} rowNo - number of row
*/
  moveTableContent: function(rowNo) {
    this.fetchBuffer(rowNo);
    this.scroller.moveScroll(rowNo);
    this.viewPort.scrollTo(this.scroller.rowToPixel(rowNo));
  },

/**
* It moves the focus to a selected record. This method uses the moveTableContent one to do the scrolling inside the grid. This method will be used in the pulsations av. pag. and re. pag. keys
* @param {Number} mov - number of moved cells
*/
  moveCurrentPosition: function(mov) {
    var newPosition = this.getCurrentOffset() + mov;
    if (newPosition < 0){
      newPosition = 0;
    }else if (newPosition >= (this.metaData.totalRows - 1)){
      newPosition = this.metaData.totalRows - this.numRows;
    }
    this.moveTableContent(newPosition);
    this.selectRow(newPosition);
    this.showSelection();
  },

/**
* Return the selected row function.
* @param {Number} rowNo - number of row
* @return Handler to the selectRow function
* @type Object
*/
  getSelectRowFunction: function (rowNo) {
    var _this = this;
    var hitch =  dojo.hitch(this, "selectRow", rowNo, hitch);
    return hitch;
  },

/**
* It moves the focus to a concrete record, using the method moveTableContent to do the positioning.
* @param {Number} rowNo - number of row
*/
  goToRow: function(rowNo) {
    var _this = this;
    var hitch =  dojo.hitch(this, "selectRow", rowNo, hitch);
    if (rowNo >= 0) {
      var minOffset = this.getCurrentOffset();
      var maxOffset = minOffset + this.numRows;
      if (minOffset <= rowNo && rowNo < maxOffset) {
        if (this.metaData.totalRows <= this.numRows) {
          this.moveTableContent(0);
        }
        this.selectRow(rowNo);
      } else {
        if (hitch != null){
          this.options.onRefreshComplete.push(hitch);
        }
        if (rowNo == (minOffset - 1)) {
          this.moveTableContent(minOffset - 1);
        } else if (rowNo == maxOffset) {
          this.moveTableContent(minOffset + 1);
        } else if (rowNo >= (this.metaData.totalRows - 1)) {
          this.moveTableContent(rowNo - this.visibleRows + 1);
        } else if (rowNo >= (this.metaData.totalRows - this.visibleRows)) {
          this.moveTableContent(this.metaData.totalRows - this.visibleRows);
        } else {
          this.moveTableContent(rowNo);
        }
      }
      checkAttachmentIconRelation();
    }
  },

/**
* It manages the column resizing. First it updates the size of the table that contains the grid data, then the size of the table of the section of headers and, finally, it updates the sizes of this column for the header and each of the grid rows.
* @param {Object} headerRow - The header row object.
* @param {Object} column - the selected column to update width.
* @param {Number} newWidth - The new with.
*/
  resizeColumn: function(headerRow, column, newWidth) {
    if (column) {
      var columnNo = column.index;
      newWidth = (newWidth > 9 ? newWidth : 9);
      var currentTableWidth = openbravo.widget.DataGrid.html.extractPx(this.tableNode.style.width);
      var newTableWidth = currentTableWidth + 
      (newWidth - openbravo.widget.DataGrid.html.extractPx(column.width));
      if (currentTableWidth != newTableWidth){
        this.hasResized = true;    
      }
      newWidth += "px";
      this.columns.get(columnNo).width = newWidth;
      headerRow.cells[columnNo].style.width = newWidth;
      this.tableNode.style.width = newTableWidth + "px";
      this.tableHeader.style.width = newTableWidth + "px";
      dojo.forEach(this.tableNode.rows, function(row) {
        row.cells[columnNo].style.width = newWidth;
      });
    }
  },

/**
* It is the method that manages the columns resizing. This method calls to resizeColumn.
* @param {EventHandler} evt - event handler
*/
  doResize: function(evt) {
    var p = this.resizingParams;
    if (parent.isRTL) {
      var newWidth = p.width - evt.clientX + p.start;
    } else {
      var newWidth = p.width + evt.clientX - p.start;
    }
    p.self.resizeColumn(p.target.parentNode, p.column, newWidth);
    this.setBounds();
  },

/**
* End the resizing mode.
* @param {EventHandler} evt - event handler
*/
  endResize: function(evt) {
    var body = dojo.body();
    var p = this.resizingParams;
    if (parent.isRTL) {
      var newWidth = p.width - evt.clientX + p.start;
    } else {
      var newWidth = p.width + evt.clientX - p.start;
    }
    dojo.disconnect(this.startResizeConnection); 
    dojo.disconnect(this.endResizeConnection); 
    p.self.resizeColumn(p.target.parentNode, p.column, newWidth);
  },

/**
* It is the event that manages the resizing, indicating that the doResize must begin and, which event will make throw the endResize for the selected column.
* @param {EventHandler} evt - event handler
*/
  resizeHeader: function(evt) {
    var target = openbravo.widget.DataGrid.html.getParentByType(evt.target, "th");
    var body = dojo.body();
    var columnNo = dojo.indexOf(target.parentNode.cells, target);
    var column = this.columns.get(columnNo);
    //  var column = this.columns.get(--columnNo);
    //  while (column && !column.visible) {
    //    column = this.columns.get(--columnNo);
    //  }
    if (!column) { return; }
    this.resizingParams = {
      start: evt.clientX, 
      self: this,
      target: target,
      width: openbravo.widget.DataGrid.html.extractPx(column.width),
      scroll: target.parentNode.parentNode.scrollLeft,
      column: column
    }
    this.hasResized = false;
    this.startResizeConnection = dojo.connect(body, "onmousemove", this, "doResize");
    this.endResizeConnection = dojo.connect(body, "onmouseup", this, "endResize"); 
    dojo.stopEvent(evt);
  },

/**
* It is an auxiliary method for the render, which does the sizes adjustment operations after having constructed the object.
* @param {Array} options - Actual options.
*/
  postRendering: function(options) {
    this.options = {
      tableClass:           this.tableNode.className,
      loadingClass:         this.tableNode.className,
      scrollerBorderRight:  '1px solid #ababab',
      bufferTimeout:        15000,
      blankImg:             dojo.baseUrl + "../../openbravo/templates/blank.gif",
      sortAscendImg:        dojo.baseUrl + "../../openbravo/templates/sort_asc.gif",
      sortDescendImg:       dojo.baseUrl + "../../openbravo/templates/sort_desc.gif",
      rowEditingImg:        dojo.baseUrl + "../../openbravo/templates/editingRow.png",
      rowErrorImg:          dojo.baseUrl + "../../openbravo/templates/rowError.png",
      rowRefreshingImg:     dojo.baseUrl + "../../openbravo/templates/refreshingRow.png",
      rowSavedImg:          dojo.baseUrl + "../../openbravo/templates/rowSaved.png",
      iconImages:           [],
      largeBufferSize:      this.bufferSize,
      sortImageWidth:       9,
      sortImageHeight:      5,
      ajaxSortURLParms:     [],
      onRefreshComplete:    [],
      requestParameters:    null,
      inlineStyles:         true,
      hasResized:           dojo.hitch(this, function() { return this.hasResized; }),
      failedRows:           new dojox.collections.Dictionary()
    };
    dojo.mixin(this.options, options);
    new Image().src = this.options.rowEditingImg;
    new Image().src = this.options.rowErrorImg;
    new Image().src = this.options.rowRefreshingImg;
    new Image().src = this.options.rowSavedImg;
    this.options.iconImages[openbravo.widget.DataGrid.Row.prototype.ERROR] = this.options.rowErrorImg;
    this.options.iconImages[openbravo.widget.DataGrid.Row.prototype.SAVED] = this.options.rowSavedImg;
    this.options.iconImages[openbravo.widget.DataGrid.Row.prototype.EDITING] = this.options.rowEditingImg;
    this.options.iconImages[openbravo.widget.DataGrid.Row.prototype.REFRESHING] = this.options.rowRefreshingImg;
    var _this = this;

    this.options.onRefreshComplete.remove = function(element) {
      var pos = dojo.indexOf(_this.options.onRefreshComplete, element);
      _this.options.onRefreshComplete.splice(pos, 1);
    };

    this.ajaxOptions = {parameters: null};
    this.tableId     = this.tableNode.id; 
    this.table       = this.tableNode;
    this.hasResized  = false;
    var columnCount  = this.table.rows[0].cells.length;
    this.metaData    = new openbravo.widget.DataGrid.MetaData(this.numRows, 0, columnCount, this.options);
    this.buffer      = new openbravo.widget.DataGrid.Buffer(this.metaData, this);
    var rowCount = this.table.rows.length;
    this.viewPort =  new openbravo.widget.DataGrid.ViewPort(this.tableNode, 
      this.tableNode.offsetHeight/rowCount,
      this.numRows,
      this.buffer, this);
    this.scroller    = new openbravo.widget.DataGrid.Scroller(this, this.viewPort);
    this.options.sortHandler = dojo.hitch(this, "sortHandler");
    if ( dojo.byId(this.tableId + '_header') ){
      this.sort = new openbravo.widget.DataGrid.SortingHandler(this.tableId + '_header', this.options)
    }
    this.processingRequest = null;
    this.unprocessedRequest = null;
    if ( this.options.prefetchBuffer || this.options.prefetchOffset > 0) {
      var offset = 0;
      if (this.options.offset ) {
        offset = this.options.offset;
        this.scroller.moveScroll(offset);
        this.viewPort.scrollTo(this.scroller.rowToPixel(offset));
      }
      if (this.options.sortCols) {
        this.sortCols = options.sortCols;
        this.sortDirs = options.sortDirs;
      }
      //this.goToRow(options.defaultRow);
      this.requestContentRefresh(offset);
    }
  },

/**
* It does the cleanliness operations for the grid repainted. For example, to realize the data arrangement.
*/
  resetContents: function() {
    this.scroller.moveScroll(0);
    this.buffer.clear();
    this.viewPort.clearContents();
  },

/**
* It updates the arrangement columns inside the array of arrangement columns and does the data buffer reload by calling the backend.
* @param {Object} columns - Object with the columns of the grid
*/
  sortHandler: function(columns) {
    if (!columns) { return; }
    this.sortCols = [];
    this.sortDirs = [];
    dojo.forEach(columns, dojo.hitch(this, function(column) {
      this.sortCols.push(column.name);
      this.sortDirs.push(column.currentSort);
    }));
    this.sortCols.reverse();
    this.sortDirs.reverse();

    // on resort, add movePage parameter for notify backend to change backendPage
    gridMovePage("FIRSTPAGE");
  },

/**
* It updates the total of existing records, verifying if it has to update the number of current rows (for example, because the total is minor than the number of rows to showing, having to eliminate the remaining rows and resizing the scrollbars).
* @param {Number} newTotalRows - number of new total rows
*/
  setTotalRows: function( newTotalRows ) {
    if (newTotalRows <= this.numRows) {
      this.visibleRows = newTotalRows;
      if (dojo.isIE) {
        for (var i = 0; i < newTotalRows; i++){
          this.tableNode.rows[i].style.display = "block";
        }
        for (var i = newTotalRows; i < this.numRows; i++){
          this.tableNode.rows[i].style.display = "none";
        }    
      }else {
        if (newTotalRows > 0){
          var rowHeight = this.tableNode.rows[0].clientHeight;
        }
        for (var i = 0; i < newTotalRows; i++) {
          this.tableNode.rows[i].style.height = rowHeight + "px";
          openbravo.widget.DataGrid.html.setVisibility(this.tableNode.rows[i], true);
        }
        for (var i = newTotalRows; i < this.numRows; i++) {
          //-> to show the empty rows with the same height than the filled cells -> this.tableNode.rows[i].style.height = rowHeight + "px";
          //-> this removes completly the empty cells -> this.tableNode.rows[i].style.display = 'none';
          openbravo.widget.DataGrid.html.setVisibility(this.tableNode.rows[i], false);
        }
      }
      this.tableNode.style.height = this.viewPort.rowHeight * this.visibleRows + "px";  
      this.scroller.updateHeight();
    }
    this.resetContents();
    this.metaData.setTotalRows(newTotalRows);
    this.scroller.updateHeight();
    this.scroller.updateSize();
  },

/**
* Timeout handler
*/
  handleTimedOut: function() {
    this.processingRequest = null;
    this.processQueuedRequest();
    showJSMessage(24);
    setCalloutProcessing(false);
  },

/**
* It does a request to the backend to load the grid information. In this process it recolect the parameters that the backend will need to return the datas (arrangement columns, additional parameters, offsets ...).
* @param {Number} rowOffset - row offset
*/
  fetchBuffer: function(offset) {
    if ( this.buffer.isInRange(offset) && !this.buffer.isNearingLimit(offset)) {
      return;
    }
    if (this.processingRequest) {
      this.unprocessedRequest = new openbravo.widget.DataGrid.ContentRequest(offset);
      return;
    }
    setCalloutProcessing(true);
    var bufferStartPos = this.buffer.getFetchOffset(offset);
    this.processingRequest = new openbravo.widget.DataGrid.ContentRequest(offset);
    this.processingRequest.bufferOffset = bufferStartPos;   
    var fetchSize = this.buffer.getFetchSize(offset);
    var partialLoaded = false;
    var content = [];
    if (this.requestParams) {
      for (var param in this.requestParams) {
        content[param] = this.requestParams[param];
      }
    }
    content["action"] = "getRows";
    content["page_size"] = fetchSize;
    content["offset"] = bufferStartPos;
    if (this.sortCols) {
      content["sort_cols"] = this.sortCols.reverse();
      content["sort_dirs"] = this.sortDirs.reverse();
    }
    var handlerRef = dojo.hitch(this, "ajaxUpdate");
    var serviceUrl = {
      url: this.dataUrl,
      handler: handlerRef,
      method: "POST",
      handleAs: "xml"
    };
    openbravo.widget.DataGrid.io.asyncCall(serviceUrl, content);
    if(this.timeoutHandler == null) {this.timeoutHandler = new Array()};
    this.timeoutHandler[this.timeoutHandler.length] = setTimeout(dojo.hitch(this, "handleTimedOut"), this.options.bufferTimeout);
  },

/**
* It calls to the method fetchBuffer.
* @param {Number} contentOffset - Offset of the content
*/
  requestContentRefresh: function(contentOffset) {
    this.fetchBuffer(contentOffset);
  },

/**
* It is the method that manages the backend response with the grid data.
* @param {String} type - 
* @param {XML Structure} data - data structure
* @param {EventHandler} evt - event handler
*/
  ajaxUpdate: function(data, evt) {
    try {
      if(this.timeoutHandler != null && this.timeoutHandler.length > 0){
        clearTimeout( this.timeoutHandler.shift() ); //shift returns and removes the first element of an array
      }
      this.buffer.update(data,this.processingRequest.bufferOffset);
      this.viewPort.bufferChanged();
    }
    catch(err) {
      //dojo.debug(err);
    }
    finally {
      this.processingRequest = null;
    }
    if (this.isFirstLoad) {
      this.isFirstLoad = false;
      this.onGridLoad();
    }
    if (this.hasBeenResized) {
      this.hasBeenResized = false;
      this.moveTableContent(this.offsetBeforeResize);
    }
    this.processQueuedRequest();
    setCalloutProcessing(false);
  },

/**
* A function to manage the queued requests
*/
  processQueuedRequest: function() {
    if (this.unprocessedRequest != null && this.unprocessedRequest.requestOffset > 0) {
      this.requestContentRefresh(this.unprocessedRequest.requestOffset);
      this.unprocessedRequest = null
    }
  },
  
/**
 * Shows or hides the move page button above and below the vertical scrollbar.
 */
  setGridPaging: function(showPrevious, showNext) {
    if (showPrevious) {
      document.getElementById('prevRange_table').style.display = "";
      document.getElementById('fillGapRange_table').style.display = "none";
    } else {
      document.getElementById('prevRange_table').style.display = "none";
      document.getElementById('fillGapRange_table').style.display = "";
    }
    if (showNext) {
      document.getElementById('nextRange_table').style.display = "";
    } else {
      document.getElementById('nextRange_table').style.display = "none";
    }
  },

/**
 * Moves the backend page in the specified direction by sending a DATA request
 * with an additional movePage parameter and refreshes the displayed rows.
 */
  gridMovePage: function(direction) {
    this.requestParams["movePage"] = direction;
    this.refreshGridDataAfterFilter();
    this.requestParams["movePage"] = "";
  }

});

/**
* It is a small additional control to save the information in case of leaving of the page having the edition by half.
* @param {EventHandler} colMetadata - event handler
* @return new cell text element
* @type Object
*/
function createTextCellElement(colMetadata) {

  var hoverCell = function(evt) {
    if (dojo && !dojo.hasClass(this, 'DataGrid_Body_Cell_hover')
        && !dojo.hasClass(this, 'DataGrid_Body_Cell_clicked')){
      dojo.addClass(this, 'DataGrid_Body_Cell_hover');
    }
  };

  var plainCell = function(evt) {
    if (dojo && dojo.hasClass(this, 'DataGrid_Body_Cell_hover')){
      dojo.removeClass(this, 'DataGrid_Body_Cell_hover', false);
    }
  };

  var text = document.createElement("nobr");
  text.className = openbravo.widget.DataGrid.Column.prototype.DEFAULT_CLASS;
  dojo.addClass(text, colMetadata.className);
  text.onmouseover = hoverCell;
  text.onmouseout = plainCell;
  openbravo.widget.DataGrid.html.disableSelection(text);
  var emptyText = document.createTextNode("");
  text.appendChild(emptyText);
  if (colMetadata.visible){
    text.style.width = colMetadata.width;
  }else{
    text.style.display = "none";
  }
  return text;
};

dojo.declare("openbravo.widget.DataGrid.Parser", null, {
  constructor: function(datagrid) {
    this.datagrid = datagrid;
  },

  ajaxUpdate: function(data, evt) {
    this.parseStructure(data);
  },

/**
* It receives the root element of a dom tree and parse it to build the objects that identify each of its columns.
* @param {Object} rootElem - root element of a xml object
*/
  parseStructure: function(rootElem) {
    try {
      var xmlstructure = rootElem.getElementsByTagName('xml-structure');
      var datagrids = xmlstructure[0].getElementsByTagName('datagrid');
      var status = xmlstructure[0].getElementsByTagName('status');
      if (status.length>0){
        var type = status[0].getElementsByTagName('type');
        var title = status[0].getElementsByTagName('title');
        var description = status[0].getElementsByTagName('description');
        if (dojox.data.dom.textContent(type[0]).toUpperCase() != 'HIDDEN') {
          try {
            renderMessageBox(dojox.data.dom.textContent(type[0]),dojox.data.dom.textContent(title[0]),dojox.data.dom.textContent(description[0]));
          } catch (err) {
            alert(dojox.data.dom.textContent(title[0]) + ":\n" + dojox.data.dom.textContent(description[0]));
          }
        }
      }

      // get backendPageSize and store for later use (getters + lineNumbers)
      var strPageSize = datagrids[0].getAttribute('backendPageSize');
      var backendPageSize = parseInt(strPageSize);
      this.datagrid.backendPageSize = backendPageSize;

      var validators = datagrids[0].getElementsByTagName('validator');
      for (var i = 0; i < validators.length; i++) {
        openbravo.loadScriptFromUrl(validators[i].getAttribute("src"));
        this.datagrid.validators.push(validators[i].getAttribute("className"));
      }
      var typesElem = datagrids[0].getElementsByTagName('types');
      var typeFactory = new openbravo.widget.DataGrid.TypeFactory();
      if (typesElem.length > 0) {
        var types = typesElem[0].getElementsByTagName('type');
        for (var i = 0; i < types.length; i++) {
          var name = types[i].getAttribute('name');
          var kind = types[i].getAttribute('kind');
          if (kind == 'enum') {
            var enumValues = types[i].getElementsByTagName('enumeration');
            var type = new openbravo.widget.DataGrid.EnumType(name);
            for (var j = 0; j < enumValues.length; j++) {
              var value = enumValues[j].getAttribute('value');
              type.addValue(value);  
            }
            typeFactory.addType(type);
          } else if (kind == 'class') {
            var type = new openbravo.widget.DataGrid.CustomType(name);
            type.className = types[i].getAttribute('className');
            openbravo.loadScriptFromUrl(types[i].getAttribute('src'));
            typeFactory.addType(type);
          } else {
            throw Error("Unknown kind for type named " + name);
          }
        }
      }
      var name = this.datagrid.lineNoColumnTitle;
      var typeName = "string";
      var type = typeFactory.getType(typeName);
      var gridWidth;
      if (this.datagrid.maxWidth == "0px"){
        gridWidth = dijit.getViewport().width - 80 -
          openbravo.widget.DataGrid.html.extractPx(this.datagrid.lineNoColumnWidth);
      }else{
        gridWidth = openbravo.widget.DataGrid.html.extractPx(this.datagrid.maxWidth) - 
          3 * this.datagrid.scrollWidth -
          openbravo.widget.DataGrid.html.extractPx(this.datagrid.lineNoColumnWidth);
      }
      var params = {
        width: this.datagrid.lineNoColumnWidth,
        readonly: "true",
        required: "false",
        sortable: "false",
        className: this.datagrid.lineNoColumnClass,
        headerClassName: this.datagrid.lineNoColumnHeaderClass,
        visible: this.datagrid.showLineNumbers
      }
      var column = new openbravo.widget.DataGrid.Column(name, type, params);
      this.datagrid.columns.addColumn(column);
      var columnsElem = datagrids[0].getElementsByTagName('columns');
      var columns = columnsElem[0].getElementsByTagName('column');
      for (var i = 0; i < columns.length; i++) {
        if (columns[i].parentNode == columnsElem[0]) {
          var name = columns[i].getAttribute('name');
          var typeName = columns[i].getAttribute('type');
          var type = typeFactory.getType(typeName);
          var invalidatesElem = columns[i].getElementsByTagName('invalidates');
          var invalidColumns = [];
          if (invalidatesElem.length > 0) {
            var invalidColumnsElem = invalidatesElem[0].getElementsByTagName('column');
            for (var j = 0; j < invalidColumnsElem.length; j++){
              invalidColumns.push(invalidColumnsElem[j].getAttribute('name'));
            }
          }
          var colWidth = columns[i].getAttribute('width');
          if (colWidth) {
            var pos = colWidth.indexOf("%");
            if (pos > 0) {
              colWidth = colWidth.substr(0, pos) * gridWidth / 100 + "px";
            }
          }
          var params = {
            title: columns[i].getAttribute('title'),
            width: colWidth,
            readonly: columns[i].getAttribute('readonly'),
            required: columns[i].getAttribute('required'),
            sync: columns[i].getAttribute('sync'),
            visible: columns[i].getAttribute('visible'),
            sortable: columns[i].getAttribute('sortable'),
            className: columns[i].getAttribute('className'),
            headerClassName: columns[i].getAttribute('headerClassName'),
            identifier: columns[i].getAttribute('identifier'),
            invalidates: invalidColumns,
            subordinated: []
          }
          var column = new openbravo.widget.DataGrid.Column(name, type, params);
          this.datagrid.columns.addColumn(column);
        }
      }
      if (!this.datagrid.columns.getIdentifier()){
        throw Error("No identifier specified");
      }
      var size = this.datagrid.columns.count();
      for (var i = 0; i < size; i++) {
        var currentColumn = this.datagrid.columns.get(i);
        var masterColumns = [];
        for (var j = 0; j < size; j++) {
          var sourceColumn = this.datagrid.columns.get(j);
          var sourceColumnInvalidates = sourceColumn.invalidates;
          var invalidatesSize = sourceColumnInvalidates.length;
          for (var k = 0; k < invalidatesSize; k++)
            if (sourceColumnInvalidates[k] == currentColumn.name) {
              masterColumns[masterColumns.length] = sourceColumn.name;
            }
        }
        currentColumn.subordinated = masterColumns;
      }
      this.datagrid.render();
    } catch (ex) {
      this.xmlError(ex);
    }
  },

  xmlError: function(e) {
    alert("Error while parsing datagrid structure:\n\n" + e.message);
  }
});

dojo.declare("openbravo.widget.DataGrid.Type", null, {
/**
* It is the builder that dojo needs to initialize an object.
* @param {String} name - New name
*/
  constructor: function(name) {
    this.name = name;
  },

/**
* It builds the control in the page.
* @param {Object} params - Params of the editing object.
* @return Object with the editor handler
* @type openbravo.widget.DataGrid.InputEditorHandler
*/
  renderEditor: function(params) {
    var input = document.createElement("input");
    input.setAttribute('autocomplete','off');
    input.value = params.initialValue;
    params.parentNode.appendChild(input);
    input.style.width = params.width - 8 + "px";
    return new openbravo.widget.DataGrid.InputEditorHandler(input);
  },

/**
* It implements the necessary validation on the control data.
* @param {String} value - value to validate
*/
  validate: function(value) {
    return true;
  }
});

dojo.declare("openbravo.widget.DataGrid.StringType", openbravo.widget.DataGrid.Type, {
});

dojo.declare("openbravo.widget.DataGrid.IntegerType", openbravo.widget.DataGrid.Type, {
  constructor: function(name) {
    dojo.require("dojox.validate");
  },  

  renderEditor: function(params) {
    var name = "IntegerTextbox";
    var refNode = document.createElement("input");
    params.parentNode.appendChild(refNode);
    var props = {
      name: params.name,
      value: params.initialValue,
      trim: "true",
      required: params.required,
      invalidMessage: " ",
      missingMessage: "*"
    };
    var widget = dojo.widget.createWidget(name, props, refNode);
    widget.textbox.style.width = params.width - 8 + "px";
    widget.textbox.setAttribute('autocomplete','off');
    return new openbravo.widget.DataGrid.InputEditorHandler(widget.textbox);
  },

  validate: function(value) {
    return dojox.validate.isNumberFormat(value);
  }
});

dojo.declare("openbravo.widget.DataGrid.FloatType", openbravo.widget.DataGrid.Type, {
  constructor: function(name) {
    dojo.require("dojox.validate");
  },

  renderEditor: function(params) {
    var name = "RealNumberTextbox";
    var refNode = document.createElement("input");
    params.parentNode.appendChild(refNode);
    var props = {
      name: params.name,
      value: params.initialValue,
      trim: "true",
      required: params.required,
      invalidMessage: " ",
      missingMessage: "*"
    };
    var widget = dojo.widget.createWidget(name, props, refNode);
    widget.textbox.style.width = params.width - 8 + "px";
    widget.textbox.setAttribute('autocomplete','off');
    return new openbravo.widget.DataGrid.InputEditorHandler(widget.textbox);
  },

  validate: function(value) {
    return dojox.validate.isNumberFormat(value);
  }
});

dojo.declare("openbravo.widget.DataGrid.DateType", openbravo.widget.DataGrid.Type, {
  constructor: function(name) {
    dojo.require("dijit.form.DateTextBox");
  },

  renderEditor: function(params) {
    var name = "DropdownDatePicker";
    var refNode = document.createElement("input");
    params.parentNode.appendChild(refNode);
    var props = {
    };
    var widget = dojo.widget.createWidget(name, props, refNode);
    widget.inputNode.value = params.initialValue;
    widget.inputNode.style.width = params.width - 30 + "px";
    widget.inputNode.setAttribute('autocomplete','off');
    return new openbravo.widget.DataGrid.InputEditorHandler(widget.inputNode);
  },

  validate: function(value) {
    return dojox.validate.isNumberFormat(value);
  }
});

dojo.declare("openbravo.widget.DataGrid.UrlType", openbravo.widget.DataGrid.Type, {
});

dojo.declare("openbravo.widget.DataGrid.ImgType", openbravo.widget.DataGrid.Type, {
});

dojo.declare("openbravo.widget.DataGrid.EnumType", openbravo.widget.DataGrid.Type, {
  constructor: function(name) {
    //dojo.require("dojo.widget.Select");
    this.values = [];
  },

  addValue: function(value) {
    this.values.push(value);
  },

  renderEditor: function(params) {
    var  select = document.createElement("select");
    for (var i = 0; i < this.values.length; i++) {
      var value = this.values[i];
      var option = new Option(value, value, false);
      select.options[select.options.length] = option;
    }
    select.value = params.initialValue;
    params.parentNode.appendChild(select);
    var name = "ComboBox";
    var props = {
      autoComplete: true,
      mode: "local"
    };
    var widget = dojo.widget.createWidget(name, props, select);
    widget.textInputNode.style.width = params.width - 30 + "px";
    widget.textInputNode.setAttribute('autocomplete','off');
    widget.textInputNode.value = params.initialValue;
    return new openbravo.widget.DataGrid.InputEditorHandler(widget.textInputNode);
  },

  validate: function(value) {
    return value == "" || openbravo.widget.DataGrid.html.inArray(this.values, value);
  }
});

dojo.declare("openbravo.widget.DataGrid.DynamicEnumType", openbravo.widget.DataGrid.EnumType, {
  constructor: function(name) {
    this.widget = null;
  },

  populateCombo: function(data, evt) {
    var options = data.getElementsByTagName('option');
    var availableOptions = [];
    for (var i = 0; i < options.length; i++) {
      var option = openbravo.widget.DataGrid.html.getContentAsString(options[i]);
      availableOptions.push([option, option]);
      this.values.push(option);
    }
    this.widget.dataProvider.setData(availableOptions);
  },

  queryContent: function(params) {
    var values = [];
    var columnNames = [];
    var success = true;
    for (var i = 0; i < params.subordinated.length && success; i++) {
      var columnName = params.subordinated[i];
      columnNames.push(columnName);
      var invalidatorValue = params.row.getValue(columnName);
      if (invalidatorValue == ""){
        success = false;
      }else {
        values[columnName] = invalidatorValue;
      }
    }
    if (success) {
      var handlerRef = dojo.hitch(this, "populateCombo");
      var content = [];
      content["action"] = "getComboContent";
      content["subordinatedColumn"] = params.name;
      for (var i = 0; i < params.subordinated.length; i++) {
        content[columnName] = values[columnNames[i]];
      }
      var selectedGenre = "";
      var serviceUrl = {
        url: params.cellSaver.dataUrl,
        handler: handlerRef,
        method: "POST",
        handleAs: "xml"
      };
      openbravo.widget.DataGrid.io.asyncCall(serviceUrl, content);
    }
  },  

  renderEditor: function(params) {
    var  select = document.createElement("select");
    params.parentNode.appendChild(select);
    var name = "combobox";
    var props = {
      autoComplete: true,
      mode: "local"
    };
    var widget = dojo.widget.createWidget(name, props, select);
    widget.textInputNode.style.width = params.width - 30 + "px";
    widget.textInputNode.setAttribute('autocomplete','off');
    widget.textInputNode.value = params.initialValue;
    this.widget = widget;
    this.queryContent(params);
    return new openbravo.widget.DataGrid.InputEditorHandler(widget.textInputNode);
  }
});

dojo.declare("openbravo.widget.DataGrid.CustomType", openbravo.widget.DataGrid.Type, {
  constructor: function(name) {
    this.className = "";
  },

  renderEditor: function(params) {
    params.cell.innerHTML = params.initialValue;
    var customEditor = dojo.getObject(this.className, true);
    var editorHandler = new openbravo.widget.DataGrid.EditorHandler(function() {
      return params.initialValue;
    });
    var handler = {
      save: function(values) {
        for (var columnName in values){
          params.row.setValue(columnName, values[columnName]);
        }
        params.row.updateGrid();

        editorHandler.getValue = function() {
          return values[params.name];
        };

        params.cellSaver.unlockGrid();
        params.cellSaver.save();
      },

      cancel: function() {
        params.cellSaver.unlockGrid();
        params.cellSaver.cancel();
      }
    };
    params.cellSaver.lockGrid();
    new customEditor(params.row, handler).render();
    return editorHandler;
  }
});

dojo.declare("openbravo.widget.DataGrid.TypeFactory", null, {
  constructor: function() {
    var supportedTypes = [];
    supportedTypes["string"] = "openbravo.widget.DataGrid.StringType";
    supportedTypes["integer"] = "openbravo.widget.DataGrid.IntegerType";
    supportedTypes["float"] = "openbravo.widget.DataGrid.FloatType";
    supportedTypes["date"] = "openbravo.widget.DataGrid.DateType";
    supportedTypes["url"] = "openbravo.widget.DataGrid.UrlType";
    supportedTypes["img"] = "openbravo.widget.DataGrid.ImgType";
    supportedTypes["dynamicEnum"] = "openbravo.widget.DataGrid.DynamicEnumType";
    var types = [];

    this.addType = function(type) {
      types[type.name] = type;
    };

    this.getType = function(typeName) {
      if (types[typeName]){
        return types[typeName];
      }else {
        var className = supportedTypes[typeName];
        var type;
        if (className) {
          var typeClass = dojo.getObject(className, true);
          type = new typeClass(typeName);
        }else{
          type = new openbravo.widget.DataGrid.Type(typeName);
        }
        types[typeName] = type;
        return type;
      }
    };

  }
});

dojo.declare("openbravo.widget.DataGrid.EditorHandler", null, {
  constructor: function() {
    this.getValue = function() {};
    this.setFocus = function() {};
  }
});

dojo.declare("openbravo.widget.DataGrid.InputEditorHandler", openbravo.widget.DataGrid.EditorHandler, {
  constructor: function(input) {

    this.getValue = function() {
      return input.value;
    };

    this.setFocus = function() {
      if (input.parentNode){
        input.focus();
      }
    };

  }
});

dojo.declare("openbravo.widget.DataGrid.Columns", null, {
  constructor: function() {
    var columns = [];
    var identifier = null;

    this.count = function() {
      return columns.length;
    };

    this.addColumn = function(column) {
      column.index = columns.length;
      columns[columns.length] = column;
      if (column.identifier) {
        if (identifier){
          throw Error("Column " + column.name + " set as an identifier, but " + 
            identifier.name + " is already the identifier.");
        }
        identifier = column;
      }
    };

    this.removeColumn = function(position) {
      if(columns[position].identifier){
        identifier = null;
      }
      columns[position] = null;
    };

    this.get = function(position) {
      if (!isNaN(position)){
        return columns[position];
      }else{
        for (var i in columns) {
          if (columns[i].name == position){
            return columns[i];
          }
        }
      }
    };

    this.getIdentifier = function() {
      return identifier;
    };
  }
});

dojo.declare("openbravo.widget.DataGrid.Column", null, {
  DEFAULT_WIDTH: '100px',
  DEFAULT_CLASS: 'DataGrid_Body_Cell',
  DEFAULT_CLASS_INVERSE: 'DataGrid_Body_Cell DataGrid_Body_Cell_Inverse',
  DEFAULT_HEADER_CLASS: 'DataGrid_Header_Cell',
  DEFAULT_HEADER_CLASS_INVERSE: 'DataGrid_Header_Cell DataGrid_Header_Cell_Inverse',

  constructor: function(name, type, params) {
    this.index = -1;
    this.name = name;
    this.type = type;
    this.title = params.title ? params.title : name;
    this.width = params.width ? params.width : this.DEFAULT_WIDTH;
    this.readonly = params.readonly == "true";
    this.required = params.required != "false";
    this.sync = params.sync == "true";
    this.visible = params.visible != "false";
    this.sortable = params.sortable != "false";
    if (this.type.name == 'integer' || this.type.name == 'float') {
      this.className = params.className ? params.className : this.DEFAULT_CLASS_INVERSE;
      this.headerClassName = params.headerClassName ? params.headerClassName : this.DEFAULT_HEADER_CLASS_INVERSE;
    } else {
      this.className = params.className ? params.className : this.DEFAULT_CLASS;
      this.headerClassName = params.headerClassName ? params.headerClassName : this.DEFAULT_HEADER_CLASS;
    }
    this.identifier = params.identifier == "true";
    this.invalidates = params.invalidates ? params.invalidates : [];
    this.subordinated = [];
    this.currentSort = openbravo.widget.DataGrid.Column.UNSORTED;
    if (this.identifier || !this.visible || this.readonly){
      this.required = false;
    }
    if (this.identifier){
      this.readonly = true;
    }
  },

  renderEditor: function(cell, row, cellSaver) {
    var container = document.createElement("span");
    container.style.display = "none";
    var columnNo = dojo.indexOf(cell.parentNode.cells, cell);
    var content = row.getValue(columnNo);
    openbravo.widget.DataGrid.html.clearElement(cell);
    var tdWidth = openbravo.widget.DataGrid.html.extractPx(cell.style.width);
    var params = {
      parentNode: container,
      initialValue: content,
      width: tdWidth,
      required: this.required,
      name: this.name,
      cell: cell,
      subordinated: this.subordinated,
      cellSaver: cellSaver,
      row: row
    }
    var editor = this.type.renderEditor(params);
    cell.appendChild(container);
    dojo.lfx.html.fadeShow(container, 300, null, function() {
      editor.setFocus();
    }).play();
    return editor;
  },

  isSortable: function() {
    return this.sortable;
  },

  isSorted: function() {
    return this.currentSort != openbravo.widget.DataGrid.Column.UNSORTED;
  },

  getSortDirection: function() {
    return this.currentSort;
  },

  toggleSort: function() {
    if (this.currentSort == openbravo.widget.DataGrid.Column.UNSORTED ||
        this.currentSort == openbravo.widget.DataGrid.Column.SORT_DESC){
      this.currentSort = openbravo.widget.DataGrid.Column.SORT_ASC;
    }else if ( this.currentSort == openbravo.widget.DataGrid.Column.SORT_ASC ){
      this.currentSort = openbravo.widget.DataGrid.Column.SORT_DESC;
    }
  },

  setUnsorted: function(direction) {
    this.setSorted(openbravo.widget.DataGrid.Column.UNSORTED);
  },

  setSorted: function(direction) {
    this.currentSort = direction;
  }  

});

openbravo.widget.DataGrid.Column.UNSORTED  = 0;
openbravo.widget.DataGrid.Column.SORT_ASC  = "ASC";
openbravo.widget.DataGrid.Column.SORT_DESC = "DESC";

dojo.declare("openbravo.widget.DataGrid.Row", null, {
  CORRECT:    0,
  ERROR:      1,
  SAVED:      2,
  EDITING:    3,
  REFRESHING: 4,

  constructor: function(offset, columns, options, errorHandler, isVisible) {
    this.modified = false;
    this.error = false;
    this.offset = offset;
    this.rowNode = null;
    this.status = 0;
    this.isNewRow = false;
    var values = [];
    var storedValues = openbravo.widget.DataGrid.html.toArray(values);

    var toObject = function() {
      var object = {};
      for (var i = 0; i < values.length; i++){
        object[columns.get(i).name] = values[i];
      }
      return object;
    };

/**
* It repaints the line in the page, taking again the values of the internal array and presenting them in a correct format, according to the information type of every column. Finally, it calls to the setIcon method if the state of the row is not CORRECT.
*/
    this.updateGrid = function() {
      if (this.rowNode && (!this.offset || isVisible(this.offset))) {
        for (var i = 0; i < values.length; i++) {
          var s = values[i];
          var value = s;
          if (s && typeof s == 'string') {
            value = s.split(" ").join("&nbsp;");
          }
          var column = columns.get(i);
          /*try {
          while (column.hasChildNodes()) column.removeChild(column.lastChild);
          } catch (ignored) {
            column.innerHTML="";
          }
          var textNode = createTextCellElement(column);*/
          if (column.type.name == 'url' && value != '') { this.rowNode.cells[i].innerHTML = "<a href=\"" + value + "\" target=_blank><img src=\"../web/js/openbravo/templates/popup1.gif\" border=\"0\ title=\"Link\" alt=\"Link\"></a>&nbsp;" + value; }
          if (column.type.name == 'img' && value != '') { this.rowNode.cells[i].innerHTML = "<a onclick=\"openPopUp('" + value + "', 'Image', '70%', '70%'); return false;\" href=\"" + value + "\"><img src=\"" + value + "\" border=\"0\" height=\"15px\"></a>"; }
          else { this.rowNode.cells[i].innerHTML = value; }
          //this.rowNode.cells[i].appendChild(textNode);
        }
        if (this.status > 0) { this.setIcon(); }
      }
    };

/**
* It returns the value of a concrete column.
* @param {Number} columnNo - number of column
* @return value for the selected column
* @type String
*/
    this.getValue = function(columnNo) {
      if (isNaN(columnNo)) {
        columnNo = columns.get(columnNo).index;
      }
      return values[columnNo];
    };

/**
* It modifies the value of a column, establishing the modified property to true.
* @param {Number} columnNo - number of column
* @param {String} value - The new value
*/
    this.setValue = function(columnNo, value) {
      if (isNaN(columnNo)){
        columnNo = columns.get(columnNo).index;
      }
      if (values[columnNo] != value){
        values[columnNo] = value;
      }
      this.modified = true;
    };

/**
* It updates the internal array of values with the array of values that is given to it.
* @param {Array} columnValues - Array of values for each column
*/
    this.setValues = function(columnValues) {
      for (var i = 0; i < columnValues.length; i++) {
        values[i] = columnValues[i];
      }
      storedValues = openbravo.widget.DataGrid.html.toArray(values);      
    };

/**
* It verifies if there exist needed columns without value, putting the error property to true. It returns an array with the needed columns without value.
* @return Array of any empty field
* @type Array
*/
    this.checkFields = function() {
      var emptyFields = [];
      for (var i = 0; i < values.length; i++) {
        var column = columns.get(i);
        if (column.required && values[i] == ""){
          emptyFields[emptyFields.length] = i;
        }
      };
      this.error = emptyFields.length > 0;
      return emptyFields;
    };

/**
* It receives an array with the validations on the different columns and it applies them. If it finds some error when it is validating, the property puts error to true and ends.
* @param {Array} validators - Array of validators for each column.
*/
    this.validate = function(validators) {
      var correct = true;
      for (var i = 0; i < validators.length; i++) {
        var validator = dojo.getObject(validators[i], true);
        correct = new validator().validate(toObject());
        if (!correct){
          break;
        }
      }
      this.error = !correct;
    };

/**
* It is the method used to obtain the error message returned by the backend xml, at the same time that modifies the row state to ERROR. When it ends, it calls to the updateGrid method to repaint the row with the data and the right icon.
* @param {Object} errorElement - Xml object with the response.
*/
    this.handleError = function(errorElement) {
      this.setStatus(this.ERROR);
      var id = values[columns.getIdentifier().index];
      options.failedRows.add(id);
      var title = openbravo.widget.DataGrid.html.getContentAsString(errorElement.getElementsByTagName("title")[0]);
      var description = openbravo.widget.DataGrid.html.getContentAsString(errorElement.getElementsByTagName("description")[0]);
      values = openbravo.widget.DataGrid.html.toArray(storedValues);
      this.updateGrid();
      errorHandler.handleError(title, description);
    };

/**
* It refills the columns with the default values and repaints the row with the method updateGrid.
* @param {Object} rowElement - The pointer to the row that we want to populate
*/
    this.populateDefaultValues = function(rowElement) {
      dojo.forEach(rowElement.childNodes, dojo.hitch(this, function(element) {
        if (element.nodeType == dojox.data.dom.ELEMENT_NODE) {
          var column = columns.get(element.tagName);
          if (column) {
            this.setValue(column.index, openbravo.widget.DataGrid.html.getContentAsString(element));
            this.updateGrid();
          }
        }
      }));
    };

/**
* It does the call to the backend to obtain the values by default.
* @param {String} url - url for the request
*/
    this.getDefaultValues = function(url) {
      var content = [];
      content["action"] = "getDefaultValues";
      handlerRef = dojo.hitch(this, parseResponse);
      var serviceUrl = {
        url: url,
        handler: handlerRef,
        method: "POST",
        handleAs: "xml"
      };
      openbravo.widget.DataGrid.io.asyncCall(serviceUrl, content);
    };

/**
* It manages the backend answers to the requests of by default values, and update or row creation.
* @param {String} type - xx
* @param {XML Structure} data - data structure
* @param {EventHandler} evt - event handler
*/
    var parseResponse = function(data, evt) {
      if (!data) { return; }
      var xmldata = data.getElementsByTagName('xml-data');
      var status = xmldata[0].getElementsByTagName('status');
      if (status.length>0){
        var type = status[0].getElementsByTagName('type');
        var title = status[0].getElementsByTagName('title');
        var description = status[0].getElementsByTagName('description');
        if (dojox.data.dom.textContent(type[0]).toUpperCase() != 'HIDDEN') {
          try {
            renderMessageBox(dojox.data.dom.textContent(type[0]),dojox.data.dom.textContent(title[0]),dojox.data.dom.textContent(description[0]));
          } catch (err) {
            alert(dojox.data.dom.textContent(title[0]) + ":\n" + dojox.data.dom.textContent(description[0]));
          }
        }
      }
      var errors = xmldata[0].getElementsByTagName("error");
      if (errors.length > 0) {
        this.handleError(errors[0]);
      } else {
        var rowId = this.getValue(columns.getIdentifier().index);
        if (rowId){
          this.setStatus(this.SAVED);
        }
        storedValues = openbravo.widget.DataGrid.html.toArray(values);
        if (options.failedRows.contains(rowId)) {
          options.failedRows.remove(rowId);
        }
        setTimeout( dojo.hitch(this, function() {
          this.setStatus(this.CORRECT);
        }), 2000);
      }
      var rows = xmldata[0].getElementsByTagName('rows');
      if (rows.length > 0){
        this.populateDefaultValues(rows[0]);
      }
    };

/**
* It modifies the state of the row and it calls to the setIcon method.
* @param {String} statusName - The new status name
*/
    this.setStatus = function(statusName) {
      this.status = statusName;
      //  if (this.offset || isVisible(this.offset))
      if (this.rowNode){
        this.setIcon();
      }
    };

/**
* It shows on screen the icon corresponding to the state of the row.
*/
    this.setIcon = function() {
      this.removeIcon();
      if (this.status > 0) {
        if (options.iconImages[this.status]) {
          var column = columns.get(0);
          var htmlCode = this.rowNode.cells[column.index].innerHTML;
          this.rowNode.cells[column.index].innerHTML = htmlCode + '&nbsp;&nbsp;<img width="16px" height="16px" ' +
            'src="' + options.iconImages[this.status] + '"/>';
        }
      }
    };

/**
* It eliminates of the row the current icon.
*/
    this.removeIcon = function() {
      if (this.rowNode.cells[0].innerHTML != ""){
        this.rowNode.cells[0].innerHTML = this.offset + 1;
      }
    };

/**
* It does the call to the backend in order to it updates or build the new row.
* @param {String} url - url for the request
*/
    this.sendRow = function(url) {
      this.setStatus(this.REFRESHING);
      var content = toObject();
      var  handlerRef = dojo.hitch(this, parseResponse);
      if (this.isNewRow) {
        content["action"] = "addNewRow";
      } else {
        content["action"] = "updateRow";
      }
      var serviceUrl = {
        url: url,
        handler: handlerRef,
        method: "POST",
        handleAs: "xml"
      };
      openbravo.widget.DataGrid.io.asyncCall(serviceUrl, content);
      this.modified = false;
      if (this.isNewRow){
        this.isNewRow = false;
      }
    };

  }
});

dojo.declare("openbravo.widget.DataGrid.IndexedRows", null, {
  constructor: function() {
    var selectedRows = new dojox.collections.Dictionary();
    var lastSelected;
    var selectedIds = [];

/**
* Auxiliar method to get the total of rows selected.
* @return number of selected rows
* @type Number
*/
    this.count = function() {
      return selectedRows.count;
    };

/**
* Auxiliar method to add new selected rows.
* @param {Object} selectedRow - The selected row
*/
    this.add = function(selectedRow) {
      if(!selectedRows.contains(selectedRow.id)) {
        selectedRows.add(selectedRow.id, selectedRow);
        selectedIds.push(selectedRow.id);
      }
      lastSelected = selectedRow; 
    };

/**
* Auxiliar method to clear the selected rows.
*/
    this.clear = function() {
      selectedRows.clear();
      selectedIds = new Array();
      lastSelected = null;
    };

/**
* Auxiliar method to remove one row from the selected rows array.
* @param {String} id - id of the row to remove
*/
    this.remove = function(id) {
      selectedRows.remove(id);
      if (lastSelected.id == id) {
        selectedIds.pop();
        var id = selectedIds[selectedIds.length-1];
        lastSelected = selectedRows.item(id);
      } else {
        var size = selectedIds.length;
        for(var i = 0; i < size; i++){
          if (id == selectedIds[i]) {
            selectedIds.splice(i,1);
            break;
          }
        }
      }
    };

/**
* Returns the last selected row.
* @return Object pointing to the last selected row
* @type Object
*/
    this.getLastSelected = function() {
      return lastSelected;
    };

    this.contains = this.containsKey = selectedRows.contains;
    this.get = selectedRows.item;
    this.getKeyList = selectedRows.getKeyList;
    this.forEach = selectedRows.forEach;
    this.getIterator = selectedRows.getIterator;
  }
});

dojo.declare("openbravo.widget.DataGrid.MetaData", null, {

  /**
* Constructor.
* @param {Number} pageSize - page size
* @param {Number} totalRows - total rows
* @param {Number} columnCount - columns count
* @param {Array} options - Array of options
*/
  constructor: function( pageSize, totalRows, columnCount, options ) {
    this.pageSize  = pageSize;
    this.totalRows = totalRows;
    this.setOptions(options);
    this.ArrowHeight = 16;
    this.columnCount = columnCount;
    this.backendPage = 0;
  },

/**
* Auxiliar method to set some aditional options.
* @param {Array} options - Array of options
*/
  setOptions: function(options) {
    this.options = {
      largeBufferSize    : 7.0,   // 7 pages
      nearLimitFactor    : 0.2    // 20% of buffer
    };
    dojo.mixin(this.options, options || {});
  },

/**
* Returns the page size.
* @return page size
* @type Number
*/
  getPageSize: function() {
    return this.pageSize;
  },

/**
* Returns the total rows.
* @return total rows
* @type Number
*/
  getTotalRows: function() {
    return this.totalRows;
  },

/**
* Sets the new total rows value.
* @param {Number} n - total of rows
*/
  setTotalRows: function(n) {
    this.totalRows = n;
  },

/**
 * Returns the current backend page corresponding to the last DATA response received.
 */
  getBackendPage: function() {
    return this.backendPage;
  },

/**
* Returns the large buffer size.
* @return large buffer size
* @type Number
*/
  getLargeBufferSize: function() {
    return parseInt(this.options.largeBufferSize * this.pageSize);
  },

/**
* Returns the limit of tolerance.
* @return limit of tolerance
* @type Number
*/
  getLimitTolerance: function() {
    return parseInt(this.getLargeBufferSize() * this.options.nearLimitFactor);
  }
});

dojo.declare("openbravo.widget.DataGrid.Scroller", null, {
  constructor: function(liveGrid, viewPort) {
    this.isIE = dojo.isIE;
    this.liveGrid = liveGrid;
    this.metaData = liveGrid.metaData;
    this.createScrollBar();
    this.scrollTimeout = null;
    this.lastScrollPos = 0;
    this.viewPort = viewPort;
    this.rows = [];
  },

/**
* Checks if the onscroll method is unplugged.
* @return True: if is unplugged - False: if isn't unplugged
* @type Boolean
*/
  isUnPlugged: function() {
    return this.scrollerDiv.onscroll == null;
  },

/**
* Plug in the onscroll method.
*/
  plugin: function() {
    this.scrollerDiv.onscroll = dojo.hitch(this, "handleScroll");
  },

/**
* Un plug the onscroll method.
*/
  unplug: function() {
    this.scrollerDiv.onscroll = null;
  },

/**
* A header hack function for ie.
*/
  sizeIEHeaderHack: function() {
    if ( !this.isIE ) { return; }
    var headerTable = dojo.byId(this.liveGrid.tableId + "_header");
    if ( headerTable ) {
      headerTable.rows[0].cells[0].style.width =
        (headerTable.rows[0].cells[0].offsetWidth + 1) + "px";
    }
  },

/**
* Update the height for the visualization.
*/
  updateHeight: function() {
    var visibleHeight = this.liveGrid.viewPort.visibleHeight();
    var table = this.liveGrid.table;
    var headerHeight = table.parentNode.firstChild.offsetHeight;
    var scrollerStyle = this.scrollerDiv.style;
    scrollerStyle.top         = headerHeight - 16;
    scrollerStyle.height      = visibleHeight + "px";
    var totalHeight = parseInt(visibleHeight * this.metaData.getTotalRows()/this.metaData.getPageSize());
    this.heightDiv.style.height = totalHeight + "px" ;
    if (totalHeight > visibleHeight) {
      scrollerStyle.borderRight = this.liveGrid.options.scrollerBorderRight;
    } else {
      scrollerStyle.borderRight = "0px";
    }
  },

/**
* Creates the scroll bar object.
*/
  createScrollBar: function() {
    var table = this.liveGrid.table;

    this.fillGapRange = document.createElement("table");
    this.fillGapRange.className = "DataGrid_Scroll_icon_prevRange";
    this.fillGapRange.id = "fillGapRange_table";
    this.fillGapRange.style.cursor = "default";
    this.fillGapRange.style.background = "transparent";
    var fillGapRange_tbody = document.createElement("tbody");
    var fillGapRange_tr = document.createElement("tr");
    var fillGapRange_td = document.createElement("td");
    fillGapRange_tr.appendChild(fillGapRange_td);
    fillGapRange_tbody.appendChild(fillGapRange_tr);
    this.fillGapRange.appendChild(fillGapRange_tbody);

    this.prevRange = document.createElement("table");
    this.prevRange.className = "DataGrid_Scroll_icon_prevRange";
    this.prevRange.id = "prevRange_table";
    this.prevRange.onclick = function() { gridMovePage("PREVIOUSPAGE"); };
    this.prevRange.style.display = "none";
    var prevRange_tbody = document.createElement("tbody");
    var prevRange_tr = document.createElement("tr");
    var prevRange_td = document.createElement("td");
    prevRange_tr.appendChild(prevRange_td);
    prevRange_tbody.appendChild(prevRange_tr);
    this.prevRange.appendChild(prevRange_tbody);

    this.nextRange = document.createElement("table");
    this.nextRange.className = "DataGrid_Scroll_icon_nextRange";
    this.nextRange.id = "nextRange_table";
    this.nextRange.onclick = function() { gridMovePage("NEXTPAGE"); };
    this.nextRange.style.display = "none";
    var nextRange_tbody = document.createElement("tbody");
    var nextRange_tr = document.createElement("tr");
    var nextRange_td = document.createElement("td");
    nextRange_tr.appendChild(nextRange_td);
    nextRange_tbody.appendChild(nextRange_tr);
    this.nextRange.appendChild(nextRange_tbody);

    this.scrolandRangeDiv  = document.createElement("div");
    this.scrollerDiv  = document.createElement("div");
    var scrollerStyle = this.scrollerDiv.style;
    //scrollerStyle.borderRight = this.liveGrid.options.scrollerBorderRight;  <- Moved to updateHeight()
    scrollerStyle.position    = "relative";
    scrollerStyle.left        = this.isIE ? "-6px" : "-3px";
    scrollerStyle.width       = "19px";
    scrollerStyle.overflow    = "auto";
    this.heightDiv = document.createElement("div");
    this.heightDiv.style.width  = "1px";
    this.updateHeight();
    this.scrolandRangeDiv.appendChild(this.fillGapRange);
    this.scrolandRangeDiv.appendChild(this.prevRange);
    this.scrolandRangeDiv.appendChild(this.scrollerDiv);
    this.scrolandRangeDiv.appendChild(this.nextRange);
    this.scrollerDiv.appendChild(this.heightDiv);
    this.scrollerDiv.onscroll = dojo.hitch(this, "handleScroll");
    table.parentNode.parentNode.insertBefore( this.scrolandRangeDiv, table.parentNode.nextSibling );
    //  if (this.isIE)
    table.parentNode.style.overflowX = "auto";
    var eventName = this.isIE ? "onmousewheel" : "DOMMouseScroll";
    //  dojo.connect(window, eventName, dojo.hitch(this, 
    //  function(evt) {
    //  if (evt.wheelDelta>=0 || evt.detail < 0) //wheel-up
    //  this.scrollerDiv.scrollTop -= (2*this.viewPort.rowHeight);
    //  else
    //  this.scrollerDiv.scrollTop += (2*this.viewPort.rowHeight);
    //  this.handleScroll(false);
    //  }));
    var handler = dojo.hitch(this, function(event){
      var delta = 0;
      if (!event){
        event = window.event;
      }
      if (event.wheelDelta) {
        delta = event.wheelDelta/120;
        if (window.opera){
          delta = -delta;
        }
      } else if (event.detail) {
        delta = -event.detail/3;
      }
      if (delta) {
        if (delta >0){
          this.scrollerDiv.scrollTop -= (2*this.viewPort.rowHeight);
        }else{
          this.scrollerDiv.scrollTop += (2*this.viewPort.rowHeight);
        }
        this.handleScroll(false);
      }
      if (event.preventDefault){
        event.preventDefault();
      }
      event.returnValue = false;
    });
    if (window.addEventListener){
      this.liveGrid.domNode.addEventListener('DOMMouseScroll', handler, false);
    }else{
      dojo.connect(this.liveGrid.domNode, eventName, handler);
    }
  },

/**
* Updates the size of the grid.
*/
  updateSize: function() {
    var table = this.liveGrid.table;
    var visibleHeight = this.viewPort.visibleHeight();
    this.heightDiv.style.height = parseInt(visibleHeight *
      this.metaData.getTotalRows()/this.metaData.getPageSize()) + "px";
  },

/**
* Transform the row height to a pixel unit.
* @param {Number} rowOffset - row offset
* @return the pixel value
* @type Number
*/
  rowToPixel: function(rowOffset) {
    return (rowOffset / this.metaData.getTotalRows()) * this.heightDiv.offsetHeight
  },

/**
* Moves the scroll to the offset selected.
* @param {Number} rowOffset - row offset
*/
  moveScroll: function(rowOffset) {
    this.scrollerDiv.scrollTop = this.rowToPixel(rowOffset);
    if ( this.metaData.options.onscroll ){
      this.metaData.options.onscroll( this.liveGrid, rowOffset );
    }
  },

/**
* Handle function to manage the scrolling process.
*/
  handleScroll: function() {
    if (this.metaData.totalRows != '0') {
      if ( this.scrollTimeout ){
        clearTimeout( this.scrollTimeout );
      }
      var scrollDiff = this.lastScrollPos-this.scrollerDiv.scrollTop;
      if (scrollDiff != 0.00) {
        var r = this.scrollerDiv.scrollTop % this.viewPort.rowHeight;
        if (r != 0) {
          this.unplug();
          if ( scrollDiff < 0 ) {
            this.scrollerDiv.scrollTop += (this.viewPort.rowHeight-r);
          } else {
            this.scrollerDiv.scrollTop -= r;
          }
          this.plugin();
        }
      }
      var contentOffset = parseInt(this.scrollerDiv.scrollTop / this.viewPort.rowHeight);
      this.liveGrid.requestContentRefresh(contentOffset);
      this.viewPort.scrollTo(this.scrollerDiv.scrollTop);
      if ( this.metaData.options.onscroll ){
        this.metaData.options.onscroll( this.liveGrid, contentOffset );
      }
      this.scrollTimeout = setTimeout(dojo.hitch(this, "scrollIdle"), 1200 );
      this.lastScrollPos = this.scrollerDiv.scrollTop;
    }
  },

/**
* Method to launch the function that manages the onscrollidle event.
*/
  scrollIdle: function() {
    if ( this.metaData.options.onscrollidle ){
      this.metaData.options.onscrollidle();
    }
  }
});

dojo.declare("openbravo.widget.DataGrid.Buffer", null, {
  
/**
* Constructor.
* @param {Object} metaData - meta data handler
* @param {Object} liveGrid - pointer to the live grid
*/
  constructor: function(metaData, liveGrid) {
    this.startPos = 0;
    this.size     = 0;
    this.metaData = metaData;
    this.rows     = [];
    this.updateInProgress = false;
    this.liveGrid = liveGrid;
    this.maxBufferSize = metaData.getLargeBufferSize() * 2;
    this.maxFetchSize = metaData.getLargeBufferSize();
    this.lastOffset = 0;
    this.isFilter = false;
  },

/**
* Returns a blank row.
* @return the new blank row
* @type Object
*/
  getBlankRow: function() {
    if (!this.blankRow ) {
      this.blankRow = this.liveGrid.createRowObject(null);
      var values = [];
      for ( var i=0; i < this.metaData.columnCount; i++ ) {
        values[i] = "";
      }
      this.blankRow.setValues(values);
    }
    return this.blankRow;
  },

/**
* Loads the rows received by the ajax call.
* @param {Object} ajaxResponse - Xml object with the response
* @return Array with the new rows created
* @type Array
*/
  loadRows: function(ajaxResponse) {
    var xmldata = ajaxResponse.getElementsByTagName('xml-data');
    var status = xmldata[0].getElementsByTagName('status');
    if (status.length>0){
      var type = status[0].getElementsByTagName('type');
      var title = status[0].getElementsByTagName('title');
      var description = status[0].getElementsByTagName('description');
      if (dojox.data.dom.textContent(type[0]).toUpperCase() != 'HIDDEN') {
        try {
          renderMessageBox(dojox.data.dom.textContent(type[0]),dojox.data.dom.textContent(title[0]),dojox.data.dom.textContent(description[0]));
        } catch (err) {
          alert(dojox.data.dom.textContent(title[0]) + ":\n" + dojox.data.dom.textContent(description[0]));
        }
      }
    }
    var rowsElement = xmldata[0].getElementsByTagName('rows')[0];
    this.updateUI = rowsElement.getAttribute("update_ui") == "true"
      var numRows = parseInt(rowsElement.getAttribute("numRows"));
    var strPage = rowsElement.getAttribute("backendPage");
    if (!strPage) {
      strPage = "0"; // fallback if backendPage attribute is not set by serverside backend
    }
    var backendPage = parseInt(strPage);
    this.metaData.backendPage = backendPage;
    if(this.isFilter) {
      this.metaData.totalRows = numRows;
      this.liveGrid.setTotalRows(numRows);
      this.isFilter=false;
    }// else if (!this.isFilter && numRows==0) return;
    if (this.metaData.totalRows == 0) {
      this.liveGrid.setTotalRows(numRows);
      this.liveGrid.fetchBuffer(0);
      if (this.metaData.options.onscroll){
        this.metaData.options.onscroll(this.liveGrid, 0);
      }
    }
    var newRows = [];
    var trs = rowsElement.getElementsByTagName("tr");
    if(trs.length>0){
      for ( var i=0 ; i < trs.length; i++ ) {
        var row = newRows[i] = this.liveGrid.createRowObject(this.lastOffset + i); 
        var cells = trs[i].getElementsByTagName("td");
        // linenumber calculation
        var values = [(backendPage * this.liveGrid.getBackendPageSize()) + this.lastOffset + i + 1];
        for ( var j=0; j < cells.length ; j++ ) {
          var cell = cells[j];
          var cellContent = dojox.data.dom.textContent(cell);
          values.push(cellContent);
        }
        row.setValues(values);
        var rowId = row.getValue(this.liveGrid.columns.getIdentifier().index);
        if (this.liveGrid.options.failedRows.count > 0) {
          if (this.liveGrid.options.failedRows.contains(rowId)) {
            row.setStatus(row.ERROR);
          }
        }
      }
      return newRows;
    }
  },

/**
* Method to update rows after an ajax call.
* @param {Object} ajaxResponse - Object with the xml response
* @param {Number} start - start position column
*/
  update: function(ajaxResponse, start) {
    var newRows = this.loadRows(ajaxResponse);
    if (newRows==null) { newRows=[]; }
    if (this.rows.length == 0) {
      this.rows = newRows;
      this.size = this.rows.length;
      this.startPos = start;
      return;
    }
    if (start > this.startPos) {
      if (this.startPos + this.rows.length < start) {
        this.rows =  newRows;
        this.startPos = start;
      } else {
        this.rows = this.rows.concat( newRows.slice(0, newRows.length));
        if (this.rows.length > this.maxBufferSize) {
          var fullSize = this.rows.length;
          this.rows = this.rows.slice(this.rows.length - this.maxBufferSize, this.rows.length)
          this.startPos = this.startPos +  (fullSize - this.rows.length);
        }
      }
    } else {
      if (start + newRows.length < this.startPos) {
        this.rows =  newRows;
      } else {
        this.rows = newRows.slice(0, this.startPos).concat(this.rows);
        if (this.rows.length > this.maxBufferSize) {
          this.rows = this.rows.slice(0, this.maxBufferSize)
        }
      }
      this.startPos =  start;
    }
    this.size = this.rows.length;
  },

/**
* Method to clear the rows array.
*/
  clear: function() {
    this.rows = [];
    this.startPos = 0;
    this.size = 0;
  },

/**
* Indicates if is overlapped.
* @param {Number} start - start position
* @param {Number} size - size of the elements
* @return True: if is overlapped - False: if isn't
* @type Boolean
*/
  isOverlapping: function(start, size) {
    return ((start < this.endPos()) && (this.startPos < start + size)) || (this.endPos() == 0)
  },

/**
* Returns a boolean to indicate is is in range.
* @param {Number} position - Actual position
* @return True: if is in range - False: if isn't
* @type Boolean
*/
  isInRange: function(position) {
    return (position >= this.startPos) && (position + this.metaData.getPageSize() <= this.endPos()); 
  },

/**
* Indicates if is near top limit.
* @param {Number} position - Position to evaluate
* @return True: if is near top limit - False: if isn't
* @type Boolean
*/
  isNearingTopLimit: function(position) {
    return position - this.startPos < this.metaData.getLimitTolerance();
  },

/**
* Returns the end position.
* @return end position
* @type Number
*/
  endPos: function() {
    return this.startPos + this.rows.length;
  },

/**
* Indicates if is near bottom limit.
* @param {Number} position - Position to evaluate
* @return True: if is near bottom limit - False: if isn't
* @type Boolean
*/
  isNearingBottomLimit: function(position) {
    return this.endPos() - (position + this.metaData.getPageSize()) < this.metaData.getLimitTolerance();
  },

/**
* Indicates if is at top.
* @return True: if is at top - False: if isn't
* @type Boolean
*/
  isAtTop: function() {
    return this.startPos == 0;
  },

/**
* Indicates if is at bottom.
* @return True: if is at bottom - False: if isn't
* @type Boolean
*/
  isAtBottom: function() {
    return this.endPos() == this.metaData.getTotalRows();
  },

/**
* Indicates if is near limit.
* @param {Number} position - Position to evaluate
* @return True: if is near limit - False: if isn't
* @type Boolean
*/
  isNearingLimit: function(position) {
    return ( !this.isAtTop() && this.isNearingTopLimit(position)) ||
      ( !this.isAtBottom() && this.isNearingBottomLimit(position) )
  },

/**
* Returns the fetch size.
* @param {Number} offset - Offset of the row.
* @return fetch size
* @type Number
*/
  getFetchSize: function(offset) {
    var adjustedOffset = this.getFetchOffset(offset);
    var adjustedSize = 0;
    if (adjustedOffset >= this.startPos) {
      var endFetchOffset = this.maxFetchSize  + adjustedOffset;
      if (this.metaData.totalRows > 0 && endFetchOffset > this.metaData.totalRows){
        endFetchOffset = this.metaData.totalRows;
      }
      adjustedSize = endFetchOffset - adjustedOffset;  
      if(adjustedOffset == 0 && adjustedSize < this.maxFetchSize){
        adjustedSize = this.maxFetchSize;
      }
    } else {
      var adjustedSize = this.startPos - adjustedOffset;
      if (adjustedSize > this.maxFetchSize){
        adjustedSize = this.maxFetchSize;
      }
    }
    return adjustedSize;
  }, 

/**
* Returns the fetch offset.
* @param {Number} offset - Offset of the row.
* @return fetch offset
* @type Number
*/
  getFetchOffset: function(offset) {
    var adjustedOffset = offset;
    if (offset > this.startPos) {
      adjustedOffset = (offset > this.endPos()) ? offset :  this.endPos(); 
    } else {
      if (offset + this.maxFetchSize >= this.startPos) {
        var adjustedOffset = this.startPos - this.maxFetchSize;
        if (adjustedOffset < 0){
          adjustedOffset = 0;
        }
      }
    }
    this.lastOffset = adjustedOffset;
    return adjustedOffset;
  },

/**
* Return a specific row.
* @param {Number} offset - Offset of the row.
* @return the specific row
* @type Object
*/
  getRow: function(offset) {
    var rows = this.getRows(offset, 1);
    if (rows.length > 0){
      return rows[0];
    }else{
      return this.liveGrid.editingRow;
    }
  },

/**
* Returns an array of rows.
* @param {Number} start - start number
* @param {Number} count - total of rows
* @return Array of rows
* @type Array
*/
  getRows: function(start, count) {
    var begPos = start - this.startPos;
    var endPos = begPos + count;
    if ( endPos > this.size ){
      endPos = this.size;
    }
    var results = [];
    var index = 0;
    for ( var i=begPos ; i < endPos; i++ ) {
      results[index++] = this.rows[i];
    }
    return results;
  }
});

dojo.declare("openbravo.widget.DataGrid.ViewPort", null, {

/**
* Constructor.
* @param {Object} table - Pointer to the table
* @param {Number} rowHeight - row height
* @param {Number} visibleRows - number of visible rows
* @param {Object} buffer - buffer handler
* @param {Object} liveGrid - pointer to the live grid
*/
  constructor: function(table, rowHeight, visibleRows, buffer, liveGrid) {
    this.lastDisplayedStartPos = 0;
    this.div = table.parentNode;
    this.table = table;
    this.rowHeight = rowHeight;
    this.div.style.height = (this.rowHeight * visibleRows) + this.div.firstChild.offsetHeight + 15 + "px";
    this.buffer = buffer;
    this.liveGrid = liveGrid;
    this.visibleRows = visibleRows;
    this.lastPixelOffset = 0;
    this.startPos = 0;
  },

/**
* Updates the height.
*/
  updateHeight: function() {
    this.div.style.height = (this.rowHeight * this.liveGrid.visibleRows) + this.div.firstChild.offsetHeight + 15 + "px";
  },

/**
* Populates the row.
* @param {Object} htmlRow - Pointer to the html row
* @param {Object} row - Pointer to the row
*/
  populateRow: function(htmlRow, row) {
    if (row) {
      row.rowNode = htmlRow;
      row.updateGrid();
    }
  },

/**
* Manage the buffer changes.
*/
  bufferChanged: function() {
    this.refreshContents( parseInt(this.lastPixelOffset / this.rowHeight));
  },

/**
* Clear all rows.
*/
  clearRows: function() {
    if (!this.isBlank) {
      this.liveGrid.table.className = this.liveGrid.options.loadingClass;
      for (var i=0; i < this.visibleRows; i++){
        this.populateRow(this.table.rows[i], this.buffer.getBlankRow());
      }
      this.isBlank = true;
    }
  },

/**
* Clear the contents.
*/
  clearContents: function() {   
    this.clearRows();
    this.scrollTo(0);
    this.startPos = 0;
    this.lastStartPos = -1;   
  },

/**
* Refresh the content for the specific position.
* @param {Number} startPos - the start position
*/
  refreshContents: function(startPos) {
    if (startPos == this.lastRowPos && !this.isPartialBlank && !this.isBlank) {
      return;
    }
    if ((startPos + this.visibleRows < this.buffer.startPos)  
      || (this.buffer.startPos + this.buffer.size < startPos) 
      || (this.buffer.size == 0)) {
      this.clearRows();
      return;
    }
    try {
      setGridRefreshing(true);
    } catch (e) {}
    this.isBlank = false;
    var viewPrecedesBuffer = this.buffer.startPos > startPos
    var contentStartPos = viewPrecedesBuffer ? this.buffer.startPos: startPos; 
    var contentEndPos = (this.buffer.startPos + this.buffer.size < startPos + this.visibleRows) 
      ? this.buffer.startPos + this.buffer.size : startPos + this.visibleRows;
    var rowSize = contentEndPos - contentStartPos;
    var rows = this.buffer.getRows(contentStartPos, rowSize ); 
    var blankSize = this.visibleRows - rowSize;
    var blankOffset = viewPrecedesBuffer ? 0: rowSize;
    var contentOffset = viewPrecedesBuffer ? blankSize: 0;
    for (var i=0; i < rows.length; i++) {//initialize what we have
      this.populateRow(this.table.rows[i + contentOffset], rows[i]);
    }
    for (var i=0; i < blankSize; i++) {// blank out the rest 
      this.populateRow(this.table.rows[i + blankOffset], this.buffer.getBlankRow());
    }
    this.isPartialBlank = blankSize > 0;
    this.lastRowPos = startPos;
    this.liveGrid.table.className = this.liveGrid.options.tableClass;
    var onRefreshComplete = this.liveGrid.options.onRefreshComplete;
    this.liveGrid.showSelection();
    if (onRefreshComplete != null) {
      for (var i = 0; i < onRefreshComplete.length; i++) {
        onRefreshComplete[i]();
      }
    }
    try {
      setTimeout('setGridRefreshing(false);',50);
    } catch (e) {}
  },

/**
* Scrolls the view to the top.
* @param {Number} pixelOffset - pixel offset
*/
  scrollTo: function(pixelOffset) {
    if (this.lastPixelOffset == pixelOffset){
      return;
    }
    this.refreshContents(parseInt(pixelOffset / this.rowHeight))
    this.div.scrollTop = pixelOffset % this.rowHeight
    this.lastPixelOffset = pixelOffset;
  },

/**
* Returns the visible height.
* @return visible height.
* @type Number.
*/
  visibleHeight: function() {
    var visibleHeight = parseInt(dojo.getComputedStyle(this.div).height) - 15 - this.div.firstChild.offsetHeight;
    if (getBrowserInfo('name').toUpperCase().indexOf("CHROME") != -1 || getBrowserInfo('name').toUpperCase().indexOf("SAFARI") != -1) {
      if (parent.frameMenu) {
        visibleHeight = visibleHeight + 18;
      } else {
        visibleHeight = visibleHeight + 1;
      }
    }
    return visibleHeight;
  },

/**
* Manage the spaces of a given string.
* @param {String} s - String with the spaces.
*/
  convertSpaces: function(s) {
  }
});

dojo.declare("openbravo.widget.DataGrid.ContentRequest", null, {
/**
* Constructor.
* @param {Object} requestOffset - requests handler
* @param {Array} options - Array of options
*/
  constructor: function( requestOffset, options ) {
    this.requestOffset = requestOffset;
  }
});

dojo.declare("openbravo.widget.DataGrid.SortingHandler", null, {
/**
* It is the method that dojo needs for the initialization of the object.
* @param {String} headerTableId - id of the header table
* @param {Array} options - Array of options
*/
  constructor: function(headerTableId, options) {
    this.headerTableId = headerTableId;
    this.headerTable   = dojo.byId(headerTableId);
    this.options = options;
    this.setOptions();
    this.applySortBehavior();
    this.sortColumns = [];
    if ( this.options.sortCols ) {
      this.setSortUI( this.options.sortCols, this.options.sortDirs );
    }
  },

/**
* It establishes the arrangements that are indicated for a column array that is given as a parameter. It uses the method setColumnSort for the arrangement of every column of the array.
* @param {Array} columnNames - array of column names
* @param {Array} sortDirections - array of sort directions
*/
  setSortUI: function( columnNames, sortDirections ) {
    for (var i = 0; i < columnNames.length; i++) {
      var column = this.options.columns.get(columnNames[i]) 
      if (column){
        this.setColumnSort(column.index, sortDirections[i]);
      }
    };
  },

/**
* It do the load of the images for indicate the ascending or the descending arrangement, but it doesn’t associate them to any object, so it seems a verification exercise to verify both existence. Then it established the driver for the arrangement in the method sort (this driver is obtained from the property options and it is the sortHandler).
*/
  setOptions: function() {
    new Image().src = this.options.sortAscendImg;
    new Image().src = this.options.sortDescendImg;
    this.sort = this.options.sortHandler;
  },

/**
* It crosses each of the columns of the header to execute the method addSortBehaviorToColumn on them.
*/
  applySortBehavior: function() {
    var headerRow   = this.headerTable.rows[0];
    var headerCells = headerRow.cells;
    for ( var i = 0 ; i < headerCells.length ; i++ ) {
      this.addSortBehaviorToColumn( i, headerCells[i] );
    }
  },

/**
* It assigns correctly, to a given column, go, the driver to manage the event onClick (headerCellClicked), the cursor style and the zone reserved to place the image of arrangement.
* @param {Number} n - column position
* @param {Object} cell - cell object
*/
  addSortBehaviorToColumn: function( n, cell ) {
    if ( this.options.columns.get(n).isSortable() ) {
      cell.id            = this.headerTableId + '_' + n;
      cell.style.cursor  = 'pointer';
      dojo.connect(cell, "onclick", this, "headerCellClicked");
      //var nobr1 = document.createElement("nobr");
      //var nobr1 = document.createElement("span");
      //nobr1.setAttribute("id", 'nobr_' + this.headerTableId + '_img_' + n);
      //nobr1.setAttribute("id", this.headerTableId + '_img_' + n);
      //nobr1.innerHTML     =  '<span id="' + this.headerTableId + '_img_' + n + '"></span>' +
      //cell.innerHTML;
      cell.innerHTML = '<span id="' + this.headerTableId + '_img_' + n + '"></span>' + cell.innerHTML;
      //cell.appendChild(nobr1);
    }
  },

/**
* It is the driver for the event onClick on the header. It will control if the cell is being resized to do nothing, because this is another function job. The process that controls this driver is the arrangement one.
* @param {EventHandler} evt - event handler
*/
  headerCellClicked: function(evt) {
    if (this.options.hasResized()) { return; }
    var eventTarget = openbravo.widget.DataGrid.html.getEventTarget(evt);
    var cellId = eventTarget.id;
    var columnNumber = parseInt(cellId.substring( cellId.lastIndexOf('_') + 1 ));
    var column = this.options.columns.get(columnNumber);
    if (!column) { return; }
    var isSorted = column.isSorted();
    if ( this.sortColumns.length > 0 ) {
      if (!isSorted) {
        if (!evt.ctrlKey){
          this.removeAllColumnsSort();
        }
        this.setColumnSort(columnNumber, openbravo.widget.DataGrid.Column.SORT_ASC);
      } else {
        if (!evt.ctrlKey){
          dojo.forEach(this.sortColumns, dojo.hitch(this, function(sortColumn) {
            if (sortColumn != column){
              this.removeColumnSort(sortColumn.index);
            }
          }));
        }
        this.toggleColumnSort(columnNumber);
      }
    } else {
      this.setColumnSort(columnNumber, openbravo.widget.DataGrid.Column.SORT_ASC);
    }
    if (this.options.sortHandler) {
      this.options.sortHandler(this.sortColumns);
    }
  },

/**
* It eliminates all the arrangements of the visualization and the internal array columns.
*/
  removeAllColumnsSort: function() {
    dojo.forEach(this.sortColumns, dojo.hitch(this, function(column) {
      this.options.columns.get(column.index).setUnsorted();
      this.setSortImage(column.index);
    }));
    this.sortColumns = [];
  },

/**
* Removes the column sort.
* @param {Number} n - Index of the column
*/
  removeColumnSort: function(n) {
    var column = this.options.columns.get(n);
    column.setUnsorted();
    var index = dojo.indexOf(this.sortColumns, column);
    this.sortColumns.splice(index, 1);
    this.setSortImage(n);
  },

/**
* It marks a column as sorted and it adds it to the internal array of columns of arrangement. Also it updates the icon for the visualization of the arrangement.
* @param {Number} n - Index of the column
* @param {String} direction - Direction for the new order
*/
  setColumnSort: function(n, direction) {
    if(isNaN(n)) { return; }
    var column = this.options.columns.get(n);
    column.setSorted(direction);
    this.sortColumns.push(column);
    this.setSortImage(n);
  },

/**
* It changes the orientation of the arrangement
* @param {Number} n - column index
*/
  toggleColumnSort: function(n) {
    this.options.columns.get(n).toggleSort();
    this.setSortImage(n);
  },

/**
* It is the method that constructs the image of the corresponding arrangement on screen.
* @param {Number} n - Column index
*/
  setSortImage: function(n) {
    var sortDirection = this.options.columns.get(n).getSortDirection();
    var sortImageSpan = dojo.byId( this.headerTableId + '_img_' + n );
    if ( sortDirection == openbravo.widget.DataGrid.Column.UNSORTED ){
      sortImageSpan.innerHTML = '';
    }else if ( sortDirection == openbravo.widget.DataGrid.Column.SORT_ASC ){
      sortImageSpan.innerHTML = '<div class="DataGrid_Header_icon_ascArrow"/>&nbsp;&nbsp;';
    }else if ( sortDirection == openbravo.widget.DataGrid.Column.SORT_DESC ){
      sortImageSpan.innerHTML = '<div class="DataGrid_Header_icon_descArrow"/>&nbsp;&nbsp;';
    }
  }
});


openbravo.widget.DataGrid.html = {

  getEventTarget: function(evt){
    if (!evt) { evt = dojo.global.event || {}; }
    var t = (evt.srcElement ? evt.srcElement : (evt.target ? evt.target : null));
    while ((t)&&(t.nodeType!=1)) { t = t.parentNode; }
    return t;
  },

  getParentByType: function(node, type) {
    var _document = dojo.doc;
    var parent = dojo.byId(node);
    type = type.toLowerCase();
    while ((parent)&&(parent.nodeName.toLowerCase()!=type)) {
      if (parent==(_document["body"]||_document["documentElement"])) {
        return null;
      }
      parent = parent.parentNode;
    }
    return parent;
  },

  getScrollbar: function() {
    var scroll = document.createElement("div");
    scroll.style.width="100px";
    scroll.style.height="100px";
    scroll.style.overflow="scroll";
    scroll.style.position="absolute";
    scroll.style.top="-300px";
    scroll.style.left="0px"
    var test = document.createElement("div");
    test.style.width="400px";
    test.style.height="400px";
    scroll.appendChild(test);
    dojo.body().appendChild(scroll);
    var width=scroll.offsetWidth - scroll.clientWidth;
    dojo.body().removeChild(scroll);
    scroll.removeChild(test);
    scroll=test=null;
    return { width: width };
  },

  setVisibility: function(node, visibility) {
    dojo.style(node, 'visibility', ((visibility instanceof String || typeof visibility == "string") ? visibility : (visibility ? 'visible' : 'hidden')));
  },

  disableSelection: function(element) {
    element = dojo.byId(element)||dojo.body();
    if (dojo.isFF) {
      element.style.MozUserSelect = "none";
    } else if (dojo.isSafari) {
      element.style.KhtmlUserSelect = "none"; 
    } else if (dojo.isIE) {
      element.unselectable = "on";
    } else {
      return false;
    }
    return true;
  },

  enableSelection: function(element) {
    element = dojo.byId(element)||dojo.body();
    if (dojo.isFF) { 
      element.style.MozUserSelect = ""; 
    } else if (dojo.isSafari) {
      element.style.KhtmlUserSelect = "";
    } else if (dojo.isIE) {
      element.unselectable = "off";
    } else {
      return false;
    }
    return true;
  },

  inArray: function(array , value) {
    return dojo.indexOf(array, value) > -1;
  },

  toArray:function(array,value) {
    var collection=[];
    for (var i=value||0; i<array.length; i++) {
      collection.push(array[i]);
    }
    return collection;
  },

  setContent: function(element, content) {
    if (element.innerText != undefined){
      element.innerText = content;
    }else{
      element.textContent = content;
    }
  },

  prereplaceClass: function(node, newClass, oldClass) {
    if (dojo.hasClass(node, oldClass)){
      dojo.removeClass(node, oldClass);
    }
    if (!dojo.hasClass(node, newClass)){
      dojo.addClass(node, newClass);
    }
  },

  stripNbsp: function(string) {
    return string;
  },

  extractPx: function(property) {
    return parseInt(property.substring(0, property.indexOf("px")));
  },

  getContent: function(node) {
    return node.innerText != undefined ? node.innerText : node.textContent;
  },

  getContentAsString: function(node){
    if (typeof node.xml != "undefined"){
      return this.getContentAsStringIE(node);
    }else if (typeof XMLSerializer != "undefined" ){
      return this.getContentAsStringMozilla(node);
    }else{
      return this.getContentAsStringGeneric(node);
    }
  },

  getContentAsStringIE: function(node){
    var s="";
      for (var i = 0; i < node.childNodes.length; i++){
        s += node.childNodes[i].xml;
      }
      return s;
  },

  getContentAsStringMozilla: function(node){
    var xmlSerializer = new XMLSerializer();
    var s = "";
    for (var i = 0; i < node.childNodes.length; i++) {
      s += xmlSerializer.serializeToString(node.childNodes[i]);
      if (s == "undefined"){
        return this.getContentAsStringGeneric(node);
      }
    }
    return s;
  },

  getContentAsStringGeneric: function(node){
    var s="";
    if (node == null) { return s; }
    for (var i = 0; i < node.childNodes.length; i++) {
      switch (node.childNodes[i].nodeType) {
        case 1: // ELEMENT_NODE
        case 5: // ENTITY_REFERENCE_NODE
          s += this.getElementAsStringGeneric(node.childNodes[i]);
          break;
        case 3: // TEXT_NODE
        case 2: // ATTRIBUTE_NODE
        case 4: // CDATA_SECTION_NODE
          s += node.childNodes[i].nodeValue;
          break;
        default:
          break;
      }
    }
    return s;
  },

  getElementAsStringGeneric: function(node){
    if (!node) { return ""; }
    
    var s='<' + node.nodeName;
    // add attributes
    if (node.attributes && node.attributes.length > 0) {
      for (var i=0; i < node.attributes.length; i++) {
        s += " " + node.attributes[i].name + "=\"" + node.attributes[i].value + "\"";
      }
    }
    // close start tag
    s += '>';
    // content of tag
    s += this.getContentAsStringGeneric(node);
    // end tag
    s += '</' + node.nodeName + '>';
    return s;
  },

  clearElement: function(element) {
    if (typeof(element) == 'string'){
      element = dojo.byId(element);
    }
    while (element.firstChild){
      element.removeChild(element.firstChild);
    }
  }
};


openbravo.widget.DataGrid.io = {
  asyncCall: function(serviceUrl, content) {
    if (serviceUrl.method == 'GET') {
      dojo.xhrGet({
        url: serviceUrl.url,
        preventCache: true,
        handleAs: serviceUrl.handleAs,
        content: content,
        load: serviceUrl.handler,
        error: openbravo.widget.DataGrid.io.handleError
      });
    } else if (serviceUrl.method == 'POST') {
      dojo.xhrPost({
        url: serviceUrl.url,
        preventCache: true,
        handleAs: serviceUrl.handleAs,
        content: content,
        load: serviceUrl.handler,
        error: openbravo.widget.DataGrid.io.handleError
      });
    }
  },

  handleError: function(exception, http, kwArgs){
    // ignore special status == 0, which happens when browser does cancel a request
    if (exception.status == 0) {
      return;
    }
    setValues_MessageBox('messageBoxID',"ERROR", "Error received in IO response:", exception.message);
  }
};
