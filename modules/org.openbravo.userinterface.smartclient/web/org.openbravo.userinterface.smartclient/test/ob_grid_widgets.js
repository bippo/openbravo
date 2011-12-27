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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
 * Contains widgets related to Grids
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

