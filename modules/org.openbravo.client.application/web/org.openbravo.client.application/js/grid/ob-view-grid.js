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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
isc.ClassFactory.defineClass('OBViewGrid', isc.OBGrid);

isc.OBViewGrid.addClassProperties({
  EDIT_LINK_FIELD_NAME: '_editLink',
  NO_COUNT_PARAMETER: '_noCount', // prevent the count operation on the server
  // note following 2 values should be the same
  // ListGrid._$ArrowUp and ListGrid._$ArrowDown
  ARROW_UP_KEY_NAME: 'Arrow_Up',
  ARROW_DOWN_KEY_NAME: 'Arrow_Down',
  ERROR_MESSAGE_PROP: isc.OBViewGrid.ERROR_MESSAGE_PROP
});

// = OBViewGrid =
// The OBViewGrid is the Openbravo specific subclass of the Smartclient
// ListGrid.
isc.OBViewGrid.addProperties({

  // ** {{{ view }}} **
  // The view member contains the pointer to the composite canvas which
  // handles this form
  // and the grid and other related components.
  view: null,
  
  // ** {{{ editGrid }}} **
  // Controls if an edit link column is created in the grid, set to false to
  // prevent this.
  editGrid: true,
  
  // ** {{{ editLinkFieldProperties }}} **
  // The properties of the ListGridField created for the edit links.
  editLinkFieldProperties: {
    type: 'text',
    canSort: false,
    frozen: true,
    canFreeze: true,
    canEdit: false,
    canGroupBy: false,
    canHide: false,
    showTitle: true,
    title: '&nbsp;',
    //autoFitWidth: true,
    canDragResize: false,
    canFilter: true,
    autoExpand: false,
    filterEditorType: 'StaticTextItem',
    name: isc.OBViewGrid.EDIT_LINK_FIELD_NAME
  },
  
  editLinkColNum: -1,
  
  // ** {{{ dataPageSize }}} **
  // The data page size used for loading paged data from the server.
  dataPageSize: 100,
  
  fetchDelay: 500,
  
  autoFitFieldWidths: true,
  autoFitWidthApproach: 'title',
  canAutoFitFields: false,
  minFieldWidth: 75,
  width: '100%',
  height: '100%',
  
  showSortArrow: 'field',
  autoFetchTextMatchStyle: 'substring',
  showFilterEditor: true,
  canEdit: true,
  alternateRecordStyles: true,
  canReorderFields: true,
  canFreezeFields: true,
  canAddFormulaFields: true,
  canAddSummaryFields: true,
  canGroupBy: false,
  selectionAppearance: 'checkbox',
  useAllDataSourceFields: false,
  editEvent: 'none',
  showCellContextMenus: true,
  canOpenRecordEditor: true,
  showDetailFields: true,
  showErrorIcons: false,
  
  allowFilterExpressions: true,
  showFilterExpressionLegendMenuItem: true,
  
  // internal sc grid property, see the ListGrid source code
  preserveEditsOnSetData: false,
  
  // enabling this results in a slower user interaction
  // it is better to allow fast grid interaction and if an error occurs
  // dismiss any new records being edited and go back to the edit row
  // which causes the error
  waitForSave: false,
  stopOnErrors: false,
  confirmDiscardEdits: false,
  canMultiSort: false,
  
  emptyMessage: OB.I18N.getLabel('OBUISC_ListGrid.loadingDataMessage'),
  discardEditsSaveButtonTitle: OB.I18N.getLabel('UINAVBA_Save'),
  editPendingCSSText: null,
  
  // commented out because of: https://issues.openbravo.com/view.php?id=16515
  // default is much smaller which give smoother scrolling
//  quickDrawAheadRatio: 1.0,
  //drawAheadRatio: 2.0,
  // see this discussion:
  // http://forums.smartclient.com/showthread.php?t=16376
  //scrollRedrawDelay: 20,
  // note: don't set drawAllMaxCells too high as it results in extra reads
  // of data, Smartclient will try to read until drawAllMaxCells has been
  // reached
  drawAllMaxCells: 0,
  
  // the default is enabled which is a commonly used field
  recordEnabledProperty: '_enabled',
  
  // keeps track if we are in objectSelectionMode or in toggleSelectionMode
  // objectSelectionMode = singleRecordSelection === true
  singleRecordSelection: false,
  
  // editing props
  rowEndEditAction: 'next',
  listEndEditAction: 'next',
  
  enforceVClipping: true,
  fixedRecordHeights: true,
  
  validateByCell: true,
  
  currentEditColumnLayout: null,
  
  recordBaseStyleProperty: '_recordStyle',
  
  // set to false because of this: https://issues.openbravo.com/view.php?id=16509
  modalEditing: false,
  // set to true because if not all cols are drawn then when doing inline editing
  // errors were reported for undrawn columns
  // need to rework how the FormInitializationComponent sets the valuemap and defaultvalue
  // for non-existing columns this should be stored somewhere, see this reply in
  // the smartclient forum:
  // http://forums.smartclient.com/showthread.php?p=63146
  showAllColumns: true,
  //showGridSummary: true,
  
  timeFormatter: 'to24HourTime',
  
  dataProperties: {
    // this means that after an update/add the new/updated row does not fit 
    // in the current filter criteria then they are still shown
    // note that if this is set to false that when using the _dummy criteria
    // that the _dummy criteria can mean that new/updated records are not 
    // shown in the grid
    neverDropUpdatedRows: true,
    useClientFiltering: false,
    useClientSorting: false,

    // overridden to update the context/request properties for the fetch
    fetchRemoteData : function (serverCriteria, startRow, endRow) {
      var requestProperties = this.context;
      this.grid.getFetchRequestParams(requestProperties.params);

      return this.Super('fetchRemoteData', arguments);
    },

    clearLoadingMarkers: function(start, end) {
      var j;
      if (this.localData) {
        for (j = start; j < end; j++) { 
          if (Array.isLoading(this.localData[j])) {
            this.localData[j] = null;
          }
        }
      }
    },

    transformData: function(newData, dsResponse) {
      var i, length;
      
      // only do this stuff for fetch operations, in other cases strange things
      // happen as update/delete operations do not return the totalRows parameter
      if (dsResponse && dsResponse.context && dsResponse.context.operationType !== 'fetch') {
        return;
      }
      // correct the length if there is already data in the localData array
      if (this.localData) {
        length = this.localData.length;
        for (i = dsResponse.endRow + 1; i < length; i++) {
          if (!Array.isLoading(this.localData[i]) && this.localData[i]) {
            dsResponse.totalRows = i + 1;
          } else {
            break;
          }
        }

        // get rid of old loading markers, this has to be done explicitly 
        // as we can return another rowset than requested
        // call with a delay otherwise the grid will keep requesting rows while processing the 
        // current rowset
        this.delayCall('clearLoadingMarkers', [dsResponse.context.startRow, dsResponse.context.endRow], 100);
      }
      if (this.localData && this.localData[dsResponse.totalRows]) {
        this.localData[dsResponse.totalRows] = null;
      }
    }
  },

  initWidget: function () {
    var i, vwState;
    
    // make a copy of the dataProperties otherwise we get 
    // change results that values of one grid are copied/coming back
    // in other grids
    this.dataProperties = isc.addProperties({}, this.dataProperties);

    var thisGrid = this, localEditLinkField;
    if (this.editGrid) {
      // add the edit pencil in the beginning
      localEditLinkField = isc.addProperties({}, this.editLinkFieldProperties);
      localEditLinkField.width = this.editLinkColumnWidth;
      this.fields.unshift(localEditLinkField);
      // is the column after the checkbox field
      this.editLinkColNum = 1;
    }

    this.editFormDefaults = isc.addProperties({}, isc.clone(OB.ViewFormProperties), this.editFormDefaults);

    // added for showing counts in the filtereditor row
    this.checkboxFieldDefaults = isc.addProperties(this.checkboxFieldDefaults, {
      canFilter: true,
      // frozen is much nicer, but check out this forum discussion:
      // http://forums.smartclient.com/showthread.php?p=57581
      frozen: true,
      canFreeze: true,
      showHover: true,
      prompt: OB.I18N.getLabel('OBUIAPP_GridSelectAllColumnPrompt'),
      filterEditorType: 'StaticTextItem'
    });

    var grid = this;
    var menuItems = [{
        title: OB.I18N.getLabel('OBUIAPP_CreateRecordInGrid'),
        click: function(){
          grid.deselectAllRecords();
          grid.startEditingNew();
        }
      }, {
        title: OB.I18N.getLabel('OBUIAPP_CreateRecordInForm'),
        click: function(){
          grid.deselectAllRecords();
          grid.view.newDocument();
        }
      }
    ];

    if (this.showSortArrow === 'field') {
      // solves https://issues.openbravo.com/view.php?id=17362
      this.showSortArrow = isc.ListGrid.BOTH;
      this.sorterDefaults = {};
    }
    
	// TODO: add dynamic part of readonly (via setWindowSettings: see issue 17441)
    // add context-menu only if 'new' is allowed in tab definition
    if (this.uiPattern !== 'SR' && this.uiPattern !== 'RO') {
      this.contextMenu = this.getMenuConstructor().create({items: menuItems});
    }
  
    var ret = this.Super('initWidget', arguments);
    
    // only personalize if there is a professional license
    if (!OB.Utilities.checkProfessionalLicense(null, true)) {
      vwState = this.view.standardWindow.getDefaultGridViewState(this.view.tabId);
      if (vwState) {
        this.setViewState(vwState);
      }
    }
    
    this.noDataEmptyMessage = '<span class="OBGridNotificationText">' + OB.I18N.getLabel('OBUISC_ListGrid.loadingDataMessage') + '</span>'; // OB.I18N.getLabel('OBUIAPP_GridNoRecords')
    this.filterNoRecordsEmptyMessage = '<span class="OBGridNotificationText">' + OB.I18N.getLabel('OBUIAPP_GridFilterNoResults') + '</span>' +
    '<span onclick="window[\'' +
    this.ID +
    '\'].clearFilter();" class="OBLinkButtonItem">' +
    OB.I18N.getLabel('OBUIAPP_GridClearFilter') +
    '</span>';

    return ret;
  },

  // destroy the context menu also
  // see why this needs to be done in the 
  // documentation of canvas.contextMenu in Canvas.js
  destroy: function () {
    var i, field, fields = this.getFields(), 
      editorProperties, len = fields.length, ds, dataSources = [];

    for(i = 0; i < len; i++) {
      field = fields[i];
      editorProperties = field && field.editorProperties;
      ds = editorProperties && editorProperties.optionDataSource;
      if(ds) {
        dataSources.push(ds);
      }
    }

    if (this.contextMenu) {
      this.contextMenu.destroy();
      this.contextMenu = null;
    }

    this.Super('destroy', arguments);

    len = dataSources.length;

    for(i = 0; i < len; i++) {
      ds = dataSources[i];
      if(ds) {
        ds.destroy();
        ds = null;
      }
    }
  },
  
  setData: function(data) {
    data.grid = this;
    this.Super('setData', arguments);
  },

  refreshFields: function(){
    this.setFields(this.completeFields.duplicate());
  },

  draw: function() {
    var drawnBefore = this.isDrawn(), i, form, item, items, length;

    this.Super('draw', arguments);
    
    // set the focus in the filter editor
    if (this.view && this.view.isActiveView() && !drawnBefore && this.isVisible() &&
        this.getFilterEditor() && this.getFilterEditor().getEditForm()) {
      // there is a filter editor
      form = this.getFilterEditor().getEditForm();
      
      // compute a focus item, set focus with some delay
      // to give everyone time to be ready
      if (!form.getFocusItem()) {
        items = form.getItems();
        length = items.length;

        for (i = 0; i < length; i++) {
          item = items[i];
          if (item.getCanFocus() && !item.isDisabled()) {
            item.delayCall('focusInItem', null, 100);
            break;
          }
        }
      } else {
        form.getFocusItem().delayCall('focusInItem', null, 100);
      }
    }
  },
  
  // add the properties from the form
  addFormProperties: function(props){
    isc.addProperties(this.editFormDefaults, props);
  },
  
  getCellVAlign: function() {
    return 'center';
  },
  
  getCellAlign: function(record, rowNum, colNum){
    if (rowNum === this.getEditRow()) {
      return 'center';
    }
    return this.Super('getCellAlign', arguments);
  },
  
  // overridden to support hover on the header for the checkbox field
  setFieldProperties: function(field, properties){
    var localField = field;
    if (isc.isA.Number(localField)) {
      localField = this.fields[localField];
    }
    if (this.isCheckboxField(localField) && properties) {
      properties.showHover = true;
      properties.prompt = OB.I18N.getLabel('OBUIAPP_GridSelectAllColumnPrompt');
    }
    
    return this.Super('setFieldProperties', arguments);
  },
  
  cellHoverHTML: function(record, rowNum, colNum){
    var ret, field = this.getField(colNum), cellErrors, msg = '', i;
    if (this.isCheckboxField(field)) {
      return OB.I18N.getLabel('OBUIAPP_GridSelectColumnPrompt');
    }
    if (this.cellHasErrors(rowNum, colNum)) {
      cellErrors = this.getCellErrors(rowNum, colNum);
      // note cellErrors can be a string or array
      // accidentally both have the length property
      if (cellErrors && cellErrors.length > 0) {
        return OB.Utilities.getPromptString(cellErrors);
      }
    }
    if (record && record[isc.OBViewGrid.ERROR_MESSAGE_PROP]) {
      return record[isc.OBViewGrid.ERROR_MESSAGE_PROP];
    }
    
    this.inCellHoverHTML = true;
    ret = this.Super('cellHoverHTML', arguments);
    delete this.inCellHoverHTML;
    return ret;
  },
  
  reorderField: function(fieldNum, moveToPosition){
    var res = this.Super('reorderField', arguments);
    this.view.standardWindow.storeViewState();
    return res;
  },

  hideField: function(field, suppressRelayout){
    var res = this.Super('hideField', arguments);
    this.view.standardWindow.storeViewState();
    return res;
  },

  showField: function(field, suppressRelayout){
    var res = this.Super('showField', arguments);
    this.view.standardWindow.storeViewState();
    return res;
  },

  resizeField: function(fieldNum, newWidth, storeWidth){
    var res = this.Super('resizeField', arguments);
    this.view.standardWindow.storeViewState();
    return res;
  },
  
  // also store the filter criteria
  getViewState: function (returnObject, includeFilter) {
    var state = this.Super('getViewState', [returnObject || true]);

    if (includeFilter) {
      state.filter = this.getCriteria();

      if (!this.filterClause) {
        state.noFilterClause = true;
      }
    }
    
    // get rid of the selected state
    delete state.selected;
    
    if (returnObject) {
      return state;
    }
    return '(' + isc.Comm.serialize(state,false) + ')';
  },
  
  setViewState: function (state) {
    var i, length, 
      localState = this.evalViewState(state, 'viewState');
    
    // strange case, sometimes need to call twice
    if (isc.isA.String(localState)) {
      localState = this.evalViewState(state, 'viewState');
    }
    
    if (!localState) {
      return;
    }
    
    if (this.getDataSource()) {
      this.Super('setViewState', arguments);
    }

    if (localState.noFilterClause) {
      this.filterClause = null;
      if (this.view.messageBar) {
        this.view.messageBar.hide();
      }
    }

    // and no additional filter clauses passed in
    if (localState.filter && 
        this.view.tabId !== this.view.standardWindow.additionalCriteriaTabId &&
        this.view.tabId !== this.view.standardWindow.additionalFilterTabId) {
      this.setCriteria(localState.filter);
    }
  },
 
  setView: function(view){
    var dataPageSizeaux, length, i, crit;
    
    this.view = view;
    
    this.editFormDefaults.view = view;
    
    if (this.getField(this.view.parentProperty)) {
      this.getField(this.view.parentProperty).canFilter = false;
      this.getField(this.view.parentProperty).canEdit = false;
    }
 
  //// Begins-added to have the additional filter clause and tabid..Mallikarjun M
 //URL example:http://localhost:8080/openbravo/?tabId=186&filterClause=e.businessPartner.searchKey%3D%27mcgiver%27&replaceDefaultFilter=true&
    if (this.view.tabId === this.view.standardWindow.additionalFilterTabId) {
        
      if (!this.filterClause || this.view.standardWindow.replaceDefaultFilter==='true') {
        this.filterClause = unescape(this.view.standardWindow.additionalFilterClause);
      } else if (this.filterClause) {
        this.filterClause = '((' + this.filterClause + ') and (' +unescape(this.view.standardWindow.additionalFilterClause)  + '))';
      }
    }
 //// Ends..
    if (this.view.tabId === this.view.standardWindow.additionalCriteriaTabId && 
        this.view.standardWindow.additionalCriteria) {
      crit = isc.JSON.decode(unescape(this.view.standardWindow.additionalCriteria));
      this.setCriteria(crit);
      delete this.view.standardWindow.additionalCriteria;
    }
    // if there is no autoexpand field then just divide the space
    if (!this.getAutoFitExpandField()) {
      length = this.fields.length;
      
      // nobody, then give all the fields a new size, dividing
      // the space among them
      for (i = 0; i < length; i++) {
        // ignore the first 2 fields, the checkbox and edit/form
        // buttons
        if (i > 1) {
          this.fields[i].width = '*';
        }
      }
    }
    //Modify the quantity of lines to count per Window
    dataPageSizeaux = OB.PropertyStore.get('dataPageSize',this.view.windowId);
    this.dataPageSize = dataPageSizeaux ? +dataPageSizeaux : 100;
  },

  show: function(){
    var ret = this.Super('show', arguments);
    
    this.view.toolBar.updateButtonState(true);
    
    this.updateRowCountDisplay();
    
    this.resetEmptyMessage();
    
    return ret;
  },
  
  headerClick: function(fieldNum, header, autoSaveDone){
    delete this.wasEditing;
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.headerClick,
        parameters: [fieldNum, header, true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    var field = this.fields[fieldNum];
    if (this.isCheckboxField(field) && this.singleRecordSelection) {
      this.deselectAllRecords();
      this.singleRecordSelection = false;
    }
    return this.Super('headerClick', arguments);
  },
/*

  */

  keyPress: function() {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBViewGrid');
    if (response !== false) {
      response = this.Super('keyPress', arguments);
    }
    return response;
  },
  
  bodyKeyPress: function (event, eventInfo) {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBViewGrid.body');
    if (response !== false) {
      if (event.keyName === 'Space' && 
        (isc.EventHandler.ctrlKeyDown() || isc.EventHandler.altKeyDown() || isc.EventHandler.shiftKeyDown())) {
        return true;
      }
      response = this.Super('bodyKeyPress', arguments);
    }
    return response;
  },

  // called when the view gets activated
  setActive: function(active) {
    if (active) {
      this.enableShortcuts();
    } else {
      this.disableShortcuts();
    }
  },

  disableShortcuts: function() {
    OB.KeyboardManager.Shortcuts.set('ViewGrid_EditInGrid', null, function() { return true; });
    OB.KeyboardManager.Shortcuts.set('ViewGrid_EditInForm', null, function() { return true; });
  },
  
  enableShortcuts: function() {
    var me = this;

    var ksAction_CancelEditing = function() {
      if (me.getEditForm()) {
        me.cancelEditing();
        return false; //To avoid keyboard shortcut propagation
      } else {
        return true;
      }
    };
    OB.KeyboardManager.Shortcuts.set('ViewGrid_CancelEditing', ['OBViewGrid', 'OBViewGrid.body'], ksAction_CancelEditing);

    var ksAction_MoveUpWhileEditing = function() {
      if (me.getEditForm()) {
        var editRow = me.getEditRow();
        me.cancelEditing();
        if (editRow) {
          me.startEditing(editRow - 1);
        }
        return false; //To avoid keyboard shortcut propagation
      } else {
        return true;
      }
    };
    OB.KeyboardManager.Shortcuts.set('ViewGrid_MoveUpWhileEditing', ['OBViewGrid', 'OBViewGrid.body'], ksAction_MoveUpWhileEditing, null, {"key": "Arrow_Up"});

    var ksAction_MoveDownWhileEditing = function() {
      if (me.getEditForm()) {
        var editRow = me.getEditRow();
        me.cancelEditing();
        if (editRow || editRow === 0) {
          me.startEditing(editRow + 1);
        }
        return false; //To avoid keyboard shortcut propagation
      } else {
        return true;
      }
    };
    OB.KeyboardManager.Shortcuts.set('ViewGrid_MoveDownWhileEditing', ['OBViewGrid', 'OBViewGrid.body'], ksAction_MoveDownWhileEditing, null, {"key": "Arrow_Down"});

    var ksAction_DeleteSelectedRecords = function() {
      if (me.getSelectedRecords().length > 0) {
        me.view.deleteSelectedRows();
        return false; //To avoid keyboard shortcut propagation
      } else {
        return true;
      }
    };
    OB.KeyboardManager.Shortcuts.set('ViewGrid_DeleteSelectedRecords', 'OBViewGrid.body', ksAction_DeleteSelectedRecords);

    var ksAction_EditInGrid = function(){
      if (me.getSelectedRecords().length === 1) {
        me.endEditing();
        me.startEditing(me.getRecordIndex(me.getSelectedRecords()[0]));
        return false; //To avoid keyboard shortcut propagation
      } else {
        return true;
      }
    };
    OB.KeyboardManager.Shortcuts.set('ViewGrid_EditInGrid', 'OBViewGrid.body', ksAction_EditInGrid);

    var ksAction_EditInForm = function(){
      if (me.getSelectedRecords().length === 1) {
        me.endEditing();
        me.view.editRecord(me.getSelectedRecords()[0]);
        return false; //To avoid keyboard shortcut propagation
      } else {
        return true;
      }
    };
    OB.KeyboardManager.Shortcuts.set('ViewGrid_EditInForm', 'OBViewGrid.body', ksAction_EditInForm);

    var ksAction_CancelChanges = function() {
      me.view.undo();
      return false;
    };
    OB.KeyboardManager.Shortcuts.set('ViewGrid_CancelChanges', 'OBViewGrid.body', ksAction_CancelChanges);

    this.Super('enableShortcuts', arguments);
  },
  
  deselectAllRecords: function(preventUpdateSelectInfo, autoSaveDone){
    // if there is nothing to deselect then don't deselect
    if (!this.getSelectedRecord()) {
      return;
    }
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.deselectAllRecords,
        parameters: [preventUpdateSelectInfo, true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    
    this.allSelected = false;
    var ret = this.Super('deselectAllRecords', arguments);
    this.lastSelectedRecord = null;
    if (!preventUpdateSelectInfo) {
      this.selectionUpdated();
    }
    return ret;
  },
  
  selectAllRecords: function(autoSaveDone){
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.selectAllRecords,
        parameters: [true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    this.allSelected = true;
    var ret = this.Super('selectAllRecords', arguments);
    this.selectionUpdated();
    return ret;
  },
  
  updateRowCountDisplay: function(delayed){
    if (!delayed) {
      this.delayCall('updateRowCountDisplay', [true], 100);
      return;
    }
    var newValue = '', length = this.data.getLength();
    if (length > this.dataPageSize) {
      newValue = '>' + this.dataPageSize;
    } else if (length === 0) {
      newValue = '&nbsp;';
    } else {
      newValue = length;
    }
    if (this.filterEditor && this.filterEditor.getEditForm()) {
      this.filterEditor.getEditForm().setValue(isc.OBViewGrid.EDIT_LINK_FIELD_NAME, newValue);
      this.filterEditor.getEditForm().getField(isc.OBViewGrid.EDIT_LINK_FIELD_NAME).defaultValue = newValue;
    }
  },
  
  refreshContents: function(callback){
    var selectedValues;
    
    this.resetEmptyMessage();
    this.view.updateTabTitle();
    
    // do not refresh if the parent is not selected and we have no data
    // anyway
    if (this.view.parentProperty && (!this.data || !this.data.getLength || this.data.getLength() === 0)) {
      selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      if (selectedValues && !this.isOpenDirectMode && selectedValues.length === 0) {
        if (callback) {
          callback();
        }
        // but in this case we should show ourselves also
        if (!this.isVisible()) {
          this.makeVisible();
        }
        return;
      }
    }
    
    var context = {
      showPrompt: false
    };
    this.filterData(this.getCriteria(), callback, context);
  },
  
  // the dataarrived method is where different actions are done after
  // data has arrived in the grid:
  // - open the edit view if default edit mode is enabled
  // - if the user goes directly to a tab (from a link in another window)
  // then
  // opening the relevant record is done here or if no record is passed grid
  // mode is opened
  // - if there is only one record then select it directly
  dataArrived: function(startRow, endRow){
    // do this now, to replace the loading message
    // TODO: add dynamic part of readonly (via setWindowSettings: see issue 17441)
    if (this.uiPattern === 'SR' || this.uiPattern === 'RO') {
      this.noDataEmptyMessage = '<span class="OBGridNotificationText">' + OB.I18N.getLabel('OBUIAPP_NoDataInGrid') + '</span>';
    } else {
      this.noDataEmptyMessage = '<span class="OBGridNotificationText">' + OB.I18N.getLabel('OBUIAPP_GridNoRecords') + '</span>' +
      '<span onclick="window[\'' +
      this.ID +
      '\'].view.newRow();" class="OBLinkButtonItem">' +
      OB.I18N.getLabel('OBUIAPP_GridCreateOne') +
      '</span>';
    }
    this.resetEmptyMessage();
    
    var record, ret = this.Super('dataArrived', arguments);
    this.updateRowCountDisplay();
    if (this.getSelectedRecords() && this.getSelectedRecords().length > 0) {
      this.selectionUpdated();
    }
    
    // no data and the grid is not visible, only do this is if the 
    // form is not in new mode
    if (this.data && this.data.getLength() === 0 && !this.isVisible() && !this.view.viewForm.isNew) {
      this.makeVisible();
    }
    
    // get the record id from any record
    if (this.isOpenDirectMode && this.data && this.data.getLength() >= 1) {
      // now tell the parent grid to refresh on the basis of this parentRecordId also
      if (this.view.parentView) {
        this.view.parentRecordId = this.data.get(startRow)[this.view.parentProperty];

        this.view.parentView.viewGrid.isOpenDirectMode = true;
        // makes sure that the parent refresh will not fire back to cause a child refresh
        this.view.parentView.isOpenDirectModeParent = true;
        // prevents opening edit mode for parent views
        this.view.parentView.viewGrid.isOpenDirectModeParent = true;
        this.view.parentView.viewGrid.targetRecordId = this.view.parentRecordId;
        this.view.parentView.viewGrid.delayCall('refreshContents', [], 10);
      }
    }
    delete this.isOpenDirectMode;
    
    if (this.targetOpenNewEdit) {
      delete this.targetOpenNewEdit;
      // not passing record opens new
      this.view.editRecord();
    } else if (this.targetOpenGrid) {
      // direct link from other window but without a record id
      // so just show grid mode
      // don't need to do anything here
      delete this.targetOpenGrid;
    } else if (this.targetRecordId) {
      // direct link from other tab to a specific record
      this.delayedHandleTargetRecord(startRow, endRow);
    } else if (this.view.shouldOpenDefaultEditMode()) {
      // ui-pattern: single record/edit mode
      this.view.openDefaultEditView(this.getRecord(startRow));
    } else if (this.data && this.data.getLength() === 1) {
      // one record select it directly
      record = this.getRecord(0);
      // this select method prevents state changing if the record
      // was already selected
      this.doSelectSingleRecord(record);
    } else if (this.lastSelectedRecord) {
      // if nothing was select, select the record again
      if (!this.getSelectedRecord()) {
        // if it is still in the cache ofcourse
        var gridRecord = this.data.find(OB.Constants.ID, this.lastSelectedRecord.id);
        if (gridRecord) {
          this.doSelectSingleRecord(gridRecord);
        }
      } else if (this.getSelectedRecords() && this.getSelectedRecords().length !== 1) {
        this.lastSelectedRecord = null;
      }
    }
    
    if (this.actionAfterDataArrived) {
      this.actionAfterDataArrived();
      this.actionAfterDataArrived = null;
    }
    
    return ret;
  },
  
  // overridden to prevent the filter editor fields from using the 
  // grids datasource
  getFilterEditorProperties: function(field){
    return isc.addProperties({
      optionDataSource: null
    }, this.Super('getFilterEditorProperties', arguments));
  },
  
  refreshGrid: function(callback){
    if (this.getSelectedRecord()) {
      this.targetRecordId = this.getSelectedRecord()[OB.Constants.ID];
      // as the record is already selected it is already in the filter
      this.notRemoveFilter = true;
    }
    this.actionAfterDataArrived = callback;
    this.invalidateCache();
    
    var context = {
      showPrompt: false
    };
    this.filterData(this.getCriteria(), null, context);
  },
  
  // with a delay to handle the target record when the body has been drawn
  delayedHandleTargetRecord: function(startRow, endRow){
    var rowTop, recordIndex, i, data = this.data, tmpTargetRecordId = this.targetRecordId;
    if (!this.targetRecordId) {
      return;
    }
    if (this.body) {
      // don't need it anymore
      delete this.targetRecordId;
      delete this.notRemoveFilter;

      var gridRecord = data.find(OB.Constants.ID, tmpTargetRecordId);
      
      // no grid record found, stop here
      if (!gridRecord) {
        return;
      }
      recordIndex = this.getRecordIndex(gridRecord);
      
      if (data.criteria) {
        data.criteria._targetRecordId = null;
      }
      
      this.doSelectSingleRecord(gridRecord);

      this.scrollCellIntoView(recordIndex, null, true, true);
      
      // show the form with the selected record
      if (!this.view.isShowingForm && this.isOpenDirectModeLeaf) {
        this.view.editRecord(gridRecord);
      }
      
      delete this.isOpenDirectModeLeaf;
      delete this.isOpenDirectModeParent;

    } else {
      // wait a bit longer til the body is drawn
      this.delayCall('delayedHandleTargetRecord', [startRow, endRow], 200, this);
    }
  },
  
  selectRecordById: function(id, forceFetch){
    if (forceFetch) {
      this.targetRecordId = id;
      this.filterData(this.getCriteria());
      return;
    }
    
    var recordIndex, gridRecord = this.data.find(OB.Constants.ID, id);
    // no grid record fetch it
    if (!gridRecord) {
      this.targetRecordId = id;
      this.filterData(this.getCriteria());
      return;
    }
    recordIndex = this.getRecordIndex(gridRecord);
    this.scrollRecordIntoView(recordIndex, true);
    this.doSelectSingleRecord(gridRecord);
  },
  
  filterData: function(criteria, callback, requestProperties){
    if (!requestProperties) {
      requestProperties = {};
    }
    requestProperties.showPrompt = false;
    
    var theView = this.view;
    var newCallBack = function(){
      theView.recordSelected();
      if (callback) {
        callback();
      }
    };
    
    return this.Super('filterData', [this.convertCriteria(criteria), newCallBack, requestProperties]);
  },
  
  fetchData: function(criteria, callback, requestProperties){
    if (!requestProperties) {
      requestProperties = {};
    }
    requestProperties.showPrompt = false;
    
    var theView = this.view;
    
    var newCallBack = function(){
      theView.recordSelected();
      if (callback) {
        callback();
      }
    };
    
    return this.Super('fetchData', [this.convertCriteria(criteria), newCallBack, requestProperties]);
  },
  
  handleFilterEditorSubmit: function(criteria, context, autoSaveDone){
    if (!autoSaveDone) {
      var actionObject = {
        target: this,
        method: this.handleFilterEditorSubmit,
        parameters: [criteria, context, true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    
    this.Super('handleFilterEditorSubmit', arguments);
  },
  
  getInitialCriteria: function(){
    var criteria = this.Super('getInitialCriteria', arguments);   
    return this.convertCriteria(criteria);
  },
  
  getCriteria: function(){
    var criteria = this.Super('getCriteria', arguments) || {};
    if ((criteria === null || !criteria.criteria) && this.initialCriteria) {
      criteria = isc.shallowClone(this.initialCriteria);
    }
    criteria = this.convertCriteria(criteria);
    return criteria;
  },
    
  convertCriteria: function(criteria){
    var selectedValues, prop, fld, value, i, 
      criterion, fldName, length;

    if (!criteria) {
      criteria = {};      
    } else {
      criteria = isc.clone(criteria);
    }
    
    if (!criteria.operator) {
      criteria.operator = 'and';
    }
    if (!criteria._constructor) {
      criteria._constructor = "AdvancedCriteria"; 
    }
    
    if (!criteria.criteria) {
      criteria.criteria = [];
    }
    
    if (!this.notRemoveFilter && this.targetRecordId) {
      // do not filter on anything with a targetrecord
      criteria = {
        operator: 'and', 
        _constructor: "AdvancedCriteria", 
        criteria:[]};
        
      // add a dummy criteria to force a fetch
      criteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());
    } else if (this.forceRefresh) {
      // add a dummy criteria to force a fetch
      criteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());
      delete this.forceRefresh;
    } else {
      // remove the _dummy
      length = criteria.criteria.length;
      for (i = 0; i < length; i++) {
        if (criteria.criteria[i].fieldName === isc.OBRestDataSource.DUMMY_CRITERION_NAME) {
          criteria.criteria.removeAt(i);
          break;
        }
      }
    }
    
    // note pass in criteria otherwise infinite looping!
    this.resetEmptyMessage(criteria);
    
    if (this.view.parentProperty && !this.isOpenDirectMode) {
      selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      var parentPropertyFilterValue = -1;
      if (selectedValues) {
        if (selectedValues.length === 0) {
          parentPropertyFilterValue = '-1';
        } else if (selectedValues.length > 1) {
          parentPropertyFilterValue = '-1';
        } else {
          parentPropertyFilterValue = selectedValues[0][OB.Constants.ID];
        }
      }
      
      this.view.parentRecordId = parentPropertyFilterValue;
      
      var fnd = false;
      var innerCriteria = criteria.criteria;
      length = innerCriteria.length;
      for (i = 0; i < length; i++) {
        criterion = innerCriteria[i];
        fldName = criterion.fieldName;
        if (fldName === this.view.parentProperty) {
          fnd = true;
          criterion.operator = 'equals';
          criterion.value = parentPropertyFilterValue;
          break;
        }
      }
      if (!fnd) {
        innerCriteria.add({
          fieldName: this.view.parentProperty,
          operator: 'equals',
          value: parentPropertyFilterValue
        });
      }
    }
    
    // get rid of some unneeded stuff in the criteria
    if (criteria && criteria.criteria) {
      var internalCriteria = criteria.criteria;
      for (i = (internalCriteria.length - 1); i >= 0; i--) {
        var shouldRemove = false;
        criterion = internalCriteria[i];
        // but do not remove dummy criterion
        if (criterion.fieldName && criterion.fieldName.startsWith('_') && criterion.fieldName !== isc.OBRestDataSource.DUMMY_CRITERION_NAME) {
          shouldRemove = true;
        } else if (isc.isA.emptyString(criterion.value)) {
          shouldRemove = true;
        }
        if (shouldRemove) {
          internalCriteria.removeAt(i);
        }
      }
    }

    this.checkShowFilterFunnelIcon(criteria);

    return criteria;
  },
  
  onFetchData: function(criteria, requestProperties) {    
    requestProperties = requestProperties || {};
    requestProperties.params = this.getFetchRequestParams(requestProperties.params);
  },
  
  getFetchRequestParams: function(params) {
    params = params || {};
    
    if (this.targetRecordId) {
      params._targetRecordId = this.targetRecordId;
      if (!this.notRemoveFilter) {
        // remove the filter clause we don't want to use it anymore
        this.filterClause = null;
      }
      
      // this mode means that no parent is selected but the parent needs to be 
      // determined from the target record and the parent property
      if (this.isOpenDirectMode && this.view.parentView) {
        params._filterByParentProperty = this.view.parentProperty;
      }
    } else if (params._targetRecordId) {
      delete params._targetRecordId;
    }

    // prevent the count operation
    params[isc.OBViewGrid.NO_COUNT_PARAMETER] = 'true';
    
    if (this.orderByClause) {
      params[OB.Constants.ORDERBY_PARAMETER] = this.orderByClause;
    }

    // add all the new session properties context info to the requestProperties
    isc.addProperties(params, this.view.getContextInfo(true, false));
          
    if (this.filterClause) {
      if (this.whereClause) {
        params[OB.Constants.WHERE_PARAMETER] = ' ((' + this.whereClause + ') and (' + this.filterClause + ")) ";
      } else {
        params[OB.Constants.WHERE_PARAMETER] = this.filterClause;
      }
    } else if (this.whereClause) {
      params[OB.Constants.WHERE_PARAMETER] = this.whereClause;
    } else {
      params[OB.Constants.WHERE_PARAMETER] = null;
    }
    return params;    
  },
  
  createNew: function(){
    this.view.editRecord();
  },
  
  makeVisible: function() {
    if (this.view.isShowingForm) {
      this.view.switchFormGridVisibility();
    } else {
      this.show();
    }
  },
  
  // determine which field can be autoexpanded to use extra space
  getAutoFitExpandField: function() {    
    var ret, i, length;
    length = this.view.autoExpandFieldNames.length;
    for (i = 0; i < length; i++) {
      var field = this.getField(this.view.autoExpandFieldNames[i]);
      if (field && field.name) {
        return field.name;
      }
    }
    ret = this.Super('getAutoFitExpandField', arguments);    
    return ret;
  },
  
  recordClick: function(viewer, record, recordNum, field, fieldNum, value, rawValue){
    var textDeselectInterval = setInterval(function() { //To ensure that if finally a double click (recordDoubleClick) is executed, no work is highlighted/selected
      if (document.selection && document.selection.empty) {
        document.selection.empty();
      } else if (window.getSelection) {
        var sel = window.getSelection();
        sel.removeAllRanges();
      }
    }, 15);
    setTimeout(function() {
      clearInterval(textDeselectInterval);
    }, 350);
    var actionObject = {
      target: this,
      method: this.handleRecordSelection,
      parameters: [viewer, record, recordNum, field, fieldNum, value, rawValue, false, this.view.isEditingGrid]
    };
    this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
  },

  recordDoubleClick: function(viewer, record, recordNum, field, fieldNum, value, rawValue){
    var actionObject = {
      target: this.view,
      method: this.view.editRecord,
      parameters: [record, false, (field ? field.name : null)]
    };
    this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
  },
  
  resetEmptyMessage: function(criteria){
    var selectedValues, parentIsNew, oldMessage = this.emptyMessage;
    criteria = criteria || this.getCriteria();
    if (!this.view) {
      this.emptyMessage = this.noDataEmptyMessage;
    } else if (this.isGridFiltered(criteria)) {
      this.emptyMessage = this.filterNoRecordsEmptyMessage;
    } else if (this.view.isRootView) {
      this.emptyMessage = this.noDataEmptyMessage;
    } else {
      selectedValues = this.view.parentView.viewGrid.getSelectedRecords();
      parentIsNew = this.view.parentView.isShowingForm && this.view.parentView.viewForm.isNew;
      parentIsNew = parentIsNew || (selectedValues.length === 1 && selectedValues[0]._new);
      if (parentIsNew) {
        this.emptyMessage = '<span class="OBGridNotificationText">' + OB.I18N.getLabel('OBUIAPP_ParentIsNew') + '</span>';
      } else if (selectedValues.length === 0) {
        this.emptyMessage = '<span class="OBGridNotificationText">' + OB.I18N.getLabel('OBUIAPP_NoParentSelected') + '</span>';
      } else if (selectedValues.length > 1) {
        this.emptyMessage = '<span class="OBGridNotificationText">' + OB.I18N.getLabel('OBUIAPP_MultipleParentsSelected') + '</span>';
      } else {
        this.emptyMessage = this.noDataEmptyMessage;
      }
    }
    if (oldMessage !== this.emptyMessage) {
      this.markForRedraw();
    }
  },
  
  // +++++++++++++++++++++++++++++ Context menu on record click +++++++++++++++++++++++
  
  cellContextClick: function(record, rowNum, colNum){
    
    // don't do anything if right-clicking on a selected record
    if (!this.isSelected(record)) {
      this.handleRecordSelection(null, record, rowNum, null, colNum, null, null, true);
    }

    this.view.setAsActiveView();
    var ret = this.Super('cellContextClick', arguments);
    return ret;
  },

  makeCellContextItems: function(record, rowNum, colNum){
    var sourceWindow = this.view.standardWindow.windowId;
    var menuItems = [];
    var recordsSelected = this.getSelectedRecords().length > 0;
    var singleSelected = this.getSelectedRecords().length === 1;
    var field = this.getField(colNum);
    var grid = this;
    if (!this.view.hasNotChanged() || this.view.viewGrid.hasErrors()) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_UndoChanges'),
        keyTitle: OB.KeyboardManager.Shortcuts.getProperty('keyComb.text','Grid_CancelChanges','id'),
        click: function(){
          grid.view.undo();
        }
      });
    }

    if (singleSelected && this.canEdit && this.isWritable(record) && !this.view.readOnly) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_EditInGrid'),
        keyTitle: OB.KeyboardManager.Shortcuts.getProperty('keyComb.text','ViewGrid_EditInGrid','id'),
        click: function(){
          grid.endEditing();
          if (colNum || colNum === 0) {
            grid.forceFocusColumn = grid.getField(colNum).name;
          }
          grid.startEditing(rowNum, colNum);
        }
      });
//      menuItems.add({
//        title: OB.I18N.getLabel('OBUIAPP_EditInForm'),
//        click: function(){
//          grid.endEditing();
//          grid.view.editRecord(record);
//        }
//      });
    }

    if (!this.view.singleRecord && !this.view.readOnly) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_CreateRecordInGrid'),
        keyTitle: OB.KeyboardManager.Shortcuts.getProperty('keyComb.text','ToolBar_NewRow','id'),
        click: function(){
          grid.startEditingNew(rowNum);
        }
      });
    }
//    menuItems.add({
//      title: OB.I18N.getLabel('OBUIAPP_CreateRecordInForm'),
//      click: function(){
//        grid.view.newDocument();
//      }
//    });
    if (singleSelected && field.canFilter) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_UseAsFilter'),
        click: function(){
          var value;
          // a foreign key field, use the displayfield/identifier
          if (field.fkField && field.displayField) {
            value = record[field.displayField];
          } else {
            value = grid.getEditDisplayValue(rowNum, colNum, record);
          }
          // assume a date range filter item
          if (isc.isA.Date(value) && field.filterEditorType === 'OBMiniDateRangeItem') {
            grid.filterEditor.getEditForm().getField(field.name).setSingleDateValue(value);
          } else {
            grid.filterEditor.getEditForm().setValue(field.name, value);
          }
          var criteria = grid.filterEditor.getEditForm().getValuesAsCriteria();
          grid.checkShowFilterFunnelIcon(criteria);
          grid.filterData(criteria);
        }
      });
    }
    if (singleSelected && field.fkField) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_OpenOnTab'),
        click: function(){
          var fldName = field.name;
          var dotIndex = fldName.indexOf('.');
          if (dotIndex !== -1) {
            fldName = fldName.substring(0, dotIndex);
          }
          OB.Utilities.openDirectView(sourceWindow, field.refColumnName, field.targetEntity, record[fldName]);
        }
      });
    }
    if (recordsSelected && !this.view.readOnly && !this.view.singleRecord && this.allSelectedRecordsWritable()) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Delete'),
        keyTitle: OB.KeyboardManager.Shortcuts.getProperty('keyComb.text','ToolBar_Eliminate','id'),
        click: function(){
          grid.view.deleteSelectedRows();
        }
      });
    }
    
    return menuItems;
  },
  
  // +++++++++++++++++++++++++++++ Record Selection Handling +++++++++++++++++++++++
  
  updateSelectedCountDisplay: function(){
    var selection = this.getSelection(), fld, grid = this;
    var selectionLength = selection.getLength();
    var newValue = '&nbsp;';
    if (selectionLength > 0) {
      newValue = selectionLength;
      
      if (this.filterEditor && this.filterEditor.getEditForm()) {
        fld = this.filterEditor.getEditForm().getField(this.getCheckboxField().name);
        if (fld && !fld.clickForSelectedRow) {
          fld.clickForSelectedRow = true;
          fld.originalClick = fld.click;
          fld.click = function() {
            if (grid.getSelection().getLength() === 0) {
              return;
            }
            grid.scrollToRow(grid.getRecordIndex(grid.getSelectedRecord()));
            // do redraw as first columns with buttons are not drawn
            grid.markForRedraw();
          };
          fld.itemHoverHTML = function() {
            return OB.I18N.getLabel('OBUIAPP_ClickSelectedCount');
          };
        }
        fld.textBoxStyle = fld.clickableTextBoxStyle;
        fld.updateState();
      }
    } else {
      if (this.filterEditor && this.filterEditor.getEditForm()) {
        fld = this.filterEditor.getEditForm().getField(this.getCheckboxField().name);
        if (fld) {
          fld.textBoxStyle = fld.nonClickableTextBoxStyle;
          fld.updateState();
        }
      }
    }
    if (this.filterEditor) {
      this.filterEditor.getEditForm().setValue(this.getCheckboxField().name, newValue);
      this.filterEditor.getEditForm().getField(this.getCheckboxField().name).defaultValue = newValue;
    }
  },
  
  // note when solving selection issues in the future also
  // consider using the selectionChanged method, but that
  // one has as disadvantage that it is called multiple times
  // for one select/deselect action
  selectionUpdated: function(record, recordList){
    if ((!recordList || recordList.length === 1) && record === this.lastSelectedRecord && (this.lastSelectedRecord || record)) {
      return;
    }
  
    // close any editors, but only if it is different from the one we are editing
    if (this.isEditingGrid) {
      var editRecord = this.getRecord(this.getEditRow());
      if (editRecord !== record) {
        this.closeAnyOpenEditor();
      }
    }
    this.stopHover();
    this.updateSelectedCountDisplay();
    this.view.recordSelected();
    if (this.getSelectedRecords() && this.getSelectedRecords().length !== 1) {
      this.lastSelectedRecord = null;
    } else {
      this.lastSelectedRecord = this.getSelectedRecord();
    }
  },
  
  selectOnMouseDown: function(record, recordNum, fieldNum, autoSaveDone){
    // don't change selection on right mouse down
    var EH = isc.EventHandler, eventType = EH.getEventType();
    this.wasEditing = this.view.isEditingGrid;
    
    // don't do anything if right-clicking on a selected record
    if (EH.rightButtonDown() && this.isSelected(record)) {
      return;
    }

    // do autosave when this is a click on a checkbox field or when this is not
    // a mouse event, in other cases the autosave is done as part of the recordclick
    // which is called for a mousedown also
    var passToAutoSave = this.getCheckboxFieldPosition() === fieldNum || !EH.isMouseEvent(eventType);
    
    if (!autoSaveDone && passToAutoSave) {
      var actionObject = {
        target: this,
        method: this.selectOnMouseDown,
        parameters: [record, recordNum, fieldNum, true]
      };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
      // only call this method in case a checkbox click was done
      // in all other cases the recordClick will be called later
      // anyway
      //      if (this.getCheckboxFieldPosition() === fieldNum) {
      //        this.setActionAfterAutoSave(this, this.selectOnMouseDown, arguments);
      //      }
    }
    
    var previousSingleRecordSelection = this.singleRecordSelection;
    var currentSelectedRecordSelected = (this.getSelectedRecord() === record);
    if (this.getCheckboxFieldPosition() === fieldNum) {
      if (this.singleRecordSelection) {
        this.deselectAllRecords(true);
      }
      this.singleRecordSelection = false;
      this.Super('selectOnMouseDown', arguments);
      
      // handle a special case:
      // - singlerecordmode: checkbox is not checked
      // - user clicks on checkbox
      // in this case move to multi select mode and keep the record selected
      if (previousSingleRecordSelection && currentSelectedRecordSelected) {
        this.selectSingleRecord(record);
      }
      
      this.selectionUpdated();
      
      this.markForRedraw('Selection checkboxes need to be redrawn');
    } else {
      // do some checking, the handleRecordSelection should only be called
      // in case of keyboard navigation and not for real mouse clicks,
      // these are handled by the recordClick and recordDoubleClick methods
      // if this method here would also handle mouseclicks then the
      // doubleClick
      // event is not captured anymore
      if (!EH.isMouseEvent(eventType)) {
        this.handleRecordSelection(null, record, recordNum, null, fieldNum, null, null, true);
      }
    }
  },
  
  handleRecordSelection: function(viewer, record, recordNum, field, fieldNum, value, rawValue, fromSelectOnMouseDown){
    var wasEditing = this.wasEditing;
    delete this.wasEditing;
    var EH = isc.EventHandler;
    var keyName = EH.getKey();
    
    // stop editing if the user clicks out of the row
    if ((this.getEditRow() || this.getEditRow() === 0) && this.getEditRow() !== recordNum) {
      this.endEditing();
      wasEditing = true;
    }
    // do nothing, click in the editrow itself
    if ((this.getEditRow() || this.getEditRow() === 0) && this.getEditRow() === recordNum) {
      return;
    }
    
    // if the arrow key was pressed and no ctrl/shift pressed then
    // go to single select mode
    var arrowKeyPressed = keyName && (keyName === isc.OBViewGrid.ARROW_UP_KEY_NAME || keyName === isc.OBViewGrid.ARROW_DOWN_KEY_NAME);
    
    var previousSingleRecordSelection = this.singleRecordSelection;
    if (arrowKeyPressed) {
      if ((EH.ctrlKeyDown() && !EH.altKeyDown() && !EH.shiftKeyDown()) || (!EH.ctrlKeyDown() && !EH.altKeyDown() && EH.shiftKeyDown())) {
        // move to multi-select mode, let the standard do it for us
        this.singleRecordSelection = false;
      } else if (!(!EH.ctrlKeyDown() && EH.altKeyDown() && EH.shiftKeyDown())) {  // 'if' statement to avoid do an action when the KS to move to a child tab is fired
        this.doSelectSingleRecord(record);
      }
    } else if (this.getCheckboxFieldPosition() === fieldNum) {
      if (this.singleRecordSelection) {
        this.deselectAllRecords(true);
      }
      // click in checkbox field is done by standard logic
      // in the selectOnMouseDown
      this.singleRecordSelection = false;
      this.selectionUpdated();
    } else if (isc.EventHandler.ctrlKeyDown() && !isc.EventHandler.altKeyDown() && !isc.EventHandler.shiftKeyDown()) {
      // only do something if record clicked and not from selectOnMouseDown
      // this method got called twice from one clicK: through recordClick
      // and
      // to selectOnMouseDown. Only handle one.
      if (!fromSelectOnMouseDown) {
        this.singleRecordSelection = false;
        // let ctrl-click also deselect records
        if (this.isSelected(record)) {
          this.deselectRecord(record);
        } else {
          this.selectRecord(record);
        }
      }
    } else if (!isc.EventHandler.ctrlKeyDown() && !isc.EventHandler.altKeyDown() && isc.EventHandler.shiftKeyDown()) {
      this.singleRecordSelection = false;
      this.selection.selectOnMouseDown(this, recordNum, fieldNum);
      this.selectionUpdated(this.getSelectedRecord(), this.getSelection());
    } else {
      // click on the record which was already selected
      this.doSelectSingleRecord(record);
      
      // if we were editing then a single click continue edit mode
      if (wasEditing) {
        // set the focus in the clicked cell
        this.forceFocusColumn = this.getField(fieldNum).name;        
        this.startEditing(recordNum, fieldNum);
      }
    }
    
    this.updateSelectedCountDisplay();
    this.view.toolBar.updateButtonState(true);

    // mark some redraws if there are lines which don't
    // have a checkbox flagged, so if we move from single record selection
    // to multi record selection
    if (!this.singleRecordSelection && previousSingleRecordSelection) {
      this.markForRedraw('Selection checkboxes need to be redrawn');
    }
  },
  
  selectRecordForEdit: function(record){
    this.Super('selectRecordForEdit', arguments);
    this.doSelectSingleRecord(record);
  },
  
  doSelectSingleRecord: function(record){
    // if this record is already selected and the only one then do nothing
    // note that when navigating with the arrow key that at a certain 2 are
    // selected
    // when going into this method therefore the extra check on length === 1
    if (this.singleRecordSelection && this.getSelectedRecord() === record && this.getSelection().length === 1) {
      return;
    }
    this.singleRecordSelection = true;
    this.selectSingleRecord(record);
    
    // deselect the checkbox in the top
    var fieldNum = this.getCheckboxFieldPosition(), field = this.fields[fieldNum];
    var icon = this.checkboxFieldFalseImage || this.booleanFalseImage;
    var title = this.getValueIconHTML(icon, field);
    
    this.setFieldTitle(fieldNum, title);
  },
  
  // overridden to prevent the checkbox to be shown when only one
  // record is selected.
  getCellValue: function(record, recordNum, fieldNum, gridBody){
    var field = this.fields[fieldNum];
    if (!field || this.allSelected) {
      return this.Super('getCellValue', arguments);
    }
    // do all the cases which are handled in the super directly
    if (this.isCheckboxField(field)) {
      // NOTE: code copied from super class
      var icon;
      if (this.singleRecordSelection && !this.allSelected) {
        // always show the false image
        icon = (this.checkboxFieldFalseImage || this.booleanFalseImage);
      } else {
        // checked if selected, otherwise unchecked
        var isSel = this.selection.isSelected(record) ? true : false;
        icon = isSel ? (this.checkboxFieldTrueImage || this.booleanTrueImage) : (this.checkboxFieldFalseImage || this.booleanFalseImage);
      }
      // if the record is disabled, make the checkbox image disabled as well
      // or if the record is new then also show disabled
      if (!record || record[this.recordEnabledProperty] === false) {
        icon = icon.replace('.', '_Disabled.');
      }
      
      var html = this.getValueIconHTML(icon, field);
      
      return html;
    } else {
      return this.Super('getCellValue', arguments);
    }
  },
  
  getSelectedRecords: function(){
    return this.getSelection();
  },

  // +++++++++++++++++ functions for grid editing +++++++++++++++++

  startEditing: function (rowNum, colNum, suppressFocus, eCe, suppressWarning) {
    var i, ret, fld, length = this.getFields().length;
    // if a row is set and not a col then check if we should focus in the
    // first error field
    if ((rowNum || rowNum === 0) && (!colNum && colNum !== 0) && this.rowHasErrors(rowNum))  {
      for (i = 0; i < length; i++) {
        if (this.cellHasErrors(rowNum, i)) {
         colNum = i;
         break; 
        }
      }
    }
    
    if (colNum || colNum === 0) {
      this.forceFocusColumn = this.getField(colNum).name;
    } else {
      // set the first focused column
      for (i = 0; i < length; i++) {
        if (this.getFields()[i].editorProperties && this.getFields()[i].editorProperties.firstFocusedField) {
          colNum = i;
        }
      }    
    }
    
    ret = this.Super('startEditing', [rowNum, colNum, suppressFocus, eCe, suppressWarning]);
    
    this.recomputeCanvasComponents(rowNum);
    
    return ret;
  },
  
  startEditingNew: function(rowNum){
    // several cases:
    // - no current rows, add at position 0
    // - row selected, add row after selected row
    // - no row selected, add in the bottom
    var undef, insertRow;
    if (rowNum === undef) {
      // nothing selected
      if (!this.getSelectedRecord()) {
        insertRow = this.getTotalRows();
      } else {
        insertRow = 1 + this.getRecordIndex(this.getSelectedRecord()); 
      }
    } else {
      insertRow = rowNum + 1;
    }
    this.createNewRecordForEditing(insertRow);
    this.startEditing(insertRow);
    this.recomputeCanvasComponents(insertRow);
    this.view.refreshChildViews();
  },
  
  // recompute recordcomponents
  recomputeCanvasComponents: function(rowNum) {
    var i, fld, length = this.getFields().length;

    // remove client record components in edit mode
    for (i = 0; i < length; i++) {
      fld = this.getFields()[i];
      if (fld.clientClass) {
        this.refreshRecordComponent(rowNum, i);
      }
    }
  },
  
  initializeEditValues: function(rowNum, colNum){
    var record = this.getRecord(rowNum);
    // no record create one
    if (!record) {
      this.createNewRecordForEditing(rowNum);
    }
    return this.Super('initializeEditValues', arguments);
  },
  
  createNewRecordForEditing: function(rowNum){
    // note: the id is dummy, will be replaced when the save succeeds, 
    // it MUST start with _ to identify it is a temporary id 
    var record = {
      _new: true,
      id: '_' + new Date().getTime()
    };
    
    this.data.insertCacheData(record, rowNum);
    this.scrollToRow(rowNum);
    this.updateRowCountDisplay();
    this.view.toolBar.updateButtonState(true);
    
    // do it with a delay to give the system time to set the record information
    this.markForRedraw();
  },
  
  editFailed: function(rowNum, colNum, newValues, oldValues, editCompletionEvent, dsResponse, dsRequest){
    var record = this.getRecord(rowNum), editRow, editSession;
    var view = this.view;
    
    // set the default error message, 
    // is possibly overridden in the next call
    if (record) {
      if (!record[isc.OBViewGrid.ERROR_MESSAGE_PROP]) {
        this.setRecordErrorMessage(rowNum, OB.I18N.getLabel('OBUIAPP_ErrorInFields'));
        // do not automatically remove this message
        this.view.messageBar.keepOnAutomaticRefresh = true;
      } else {
        record[this.recordBaseStyleProperty] = this.recordStyleError;
      }
    }
    
    if (!this.isVisible()) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_TabWithErrors', [this.view.tabTitle]));
    } else if (view.standardWindow.forceDialogOnFailure && !this.view.isActiveView) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_AutoSaveError', [this.view.tabTitle]));
    }
    
    view.standardWindow.cleanUpAutoSaveProperties();
    view.updateTabTitle();
    view.toolBar.updateButtonState(true);
    
    // if nothing else got selected, select ourselves then
    if (record && !this.getSelectedRecord()) {
      this.selectRecord(record);
    }
  },
  
  editComplete: function(rowNum, colNum, newValues, oldValues, editCompletionEvent, dsResponse){

    var record = this.getRecord(rowNum), editRow, editSession, autoSaveAction;
    
    // a new id has been computed use that now    
    if (record && record._newId) {
      record.id = record._newId;
      delete record._newId;
    }
    
    // during save the record looses the link to the editColumnLayout,
    // restore it
    if (oldValues.editColumnLayout && !record.editColumnLayout) {
      var editColumnLayout = oldValues.editColumnLayout;
      editColumnLayout.record = record;
      record.editColumnLayout = editColumnLayout;
    }
    if (record.editColumnLayout) {
      record.editColumnLayout.editButton.setErrorState(false);
      record.editColumnLayout.showEditOpen();
    }
    
    // remove any new pointer
    delete record._new;
    
    // success invoke the action, if any there
    this.view.standardWindow.autoSaveDone(this.view, true);
    
    // if nothing else got selected, select ourselves then
    if (!this.getSelectedRecord()) {
      this.selectRecord(record);
      this.view.refreshChildViews();
    } else if (this.getSelectedRecord() === record) {
      this.view.refreshChildViews();
    }
    
    // remove the error style/message
    this.setRecordErrorMessage(rowNum, null);
    // update after the error message has been removed
    this.view.updateTabTitle();
    this.view.toolBar.updateButtonState(true);
    this.view.messageBar.hide();
    this.view.refreshParentRecord();
    this.refreshRow(rowNum);
  },

  undoEditSelectedRows: function(){
    var selectedRecords = this.getSelectedRecords(), 
      toRemove = [], i, length = selectedRecords.length;
    for (i = 0; i < length; i++) {
      var rowNum = this.getRecordIndex(selectedRecords[i]);
      var record = selectedRecords[i];
      this.Super('discardEdits', [rowNum, false, false, isc.ListGrid.PROGRAMMATIC]);
      // remove the record if new
      if (record._new) {
        toRemove.push({
          id: record.id
        });
      } else {
        // remove the error style/msg    
        this.setRecordErrorMessage(rowNum, null);
      }
    }
    this.deselectAllRecords();
    this.view.refreshChildViews();
    if (toRemove.length > 0) {
      this.data.handleUpdate('remove', toRemove);
      this.updateRowCountDisplay();
      this.view.toolBar.updateButtonState(true);
    }
    this.view.standardWindow.cleanUpAutoSaveProperties();
    this.view.updateTabTitle();
    this.view.toolBar.updateButtonState(true);
  },
  
  getCellStyle: function (record, rowNum, colNum) {
    // inactive, selected
    if (record && record[this.recordCustomStyleProperty]) {
      return record[this.recordCustomStyleProperty];
    }

    if (!this.view.isActiveView() && record[this.selection.selectionProperty]) {
      return this.recordStyleSelectedViewInActive;
    }

    return this.Super('getCellStyle', arguments);
  },
  
  // prevent multi-line content to show strangely
  // https://issues.openbravo.com/view.php?id=17531
  formatDisplayValue: function(value, record, rowNum, colNum) {
    var fld = this.getFields()[colNum], index;
    
    if (fld.clientClass) {
      return '';
    }

    if (this.inCellHoverHTML || !isc.isA.String(value)) {
      return value;
    }
  
    index = value.indexOf('\n');
    if (index !== -1) {
      return value.substring(0, index) + '...';
    } 
    
    return value;
  },
  
  discardEdits: function(rowNum, colNum, dontHideEditor, editCompletionEvent, preventConfirm){
    var localArguments = arguments, editForm = this.getEditForm(), totalRows;
    var me = this, record = this.getRecord(rowNum);
    
    if (!preventConfirm &&
    (editForm.hasChanged || this.rowHasErrors(rowNum))) {
      me.Super('discardEdits', localArguments);
      
      // remove the record if new
      if (record._new) {
        totalRows = me.data.totalRows;
        me.data.handleUpdate('remove', [{
          id: record.id
        }]);
        // the total rows should be decreased
        if (me.data.totalRows === totalRows) {
          me.data.totalRows = me.data.totalRows - 1;
        }
        me.updateRowCountDisplay();
        me.view.toolBar.updateButtonState(true);
        me.view.refreshChildViews();
      } else {
        // remove the error style/msg    
        me.setRecordErrorMessage(rowNum, null);
      }
      
      me.view.standardWindow.cleanUpAutoSaveProperties();
      
      // update after removing the error msg
      me.view.updateTabTitle();
      me.view.toolBar.updateButtonState(true);
    } else {
      me.Super('discardEdits', localArguments);
      
      // remove the record if new
      if (record && record._new) {
        totalRows = me.data.totalRows;
        me.data.handleUpdate('remove', [{
          id: record.id
        }]);
        // the total rows should be decreased
        if (me.data.totalRows === totalRows) {
          me.data.totalRows = me.data.totalRows - 1;
        }
        me.updateRowCountDisplay();
        me.view.toolBar.updateButtonState(true);
        me.view.refreshChildViews();
      } else {
        // remove the error style/msg    
        me.setRecordErrorMessage(rowNum, null);
      }
      
      this.view.standardWindow.cleanUpAutoSaveProperties();

      this.refreshRow(rowNum);
   
      // update after removing the error msg
      this.view.updateTabTitle();
    }
  },
  
  saveEdits: function(editCompletionEvent, callback, rowNum, colNum, validateOnly, skipValidation){
    var ret = this.Super('saveEdits', arguments);
    // save was not done, because there were no changes probably
    if (!ret) {
      this.view.standardWindow.cleanUpAutoSaveProperties();
      this.view.updateTabTitle();
      this.view.toolBar.updateButtonState(true);
    }
    return ret;
  },
  
  // check if a fic call needs to be done when leaving a cell and moving to the next
  // row
  // see description in saveEditvalues
  cellEditEnd: function (editCompletionEvent, newValue, ficCallDone, autoSaveDone) {
    var rowNum = this.getEditRow(), colNum = this.getEditCol();
    var editForm = this.getEditForm(), focusItem = (editForm ? editForm.getFocusItem() : null);
    // sometimes rowNum and colnum are not set, then don't compute the next cell
    var nextEditCell = ((rowNum || rowNum === 0) && (colNum || colNum === 0) ? this.getNextEditCell(rowNum, colNum, editCompletionEvent) : null);
    var newRow = nextEditCell && nextEditCell[0] !== rowNum;
    // the enter key saves anyway, so no autosave needed
    var enterKey = editCompletionEvent === 'enter';
    if (!this.view.standardWindow.isAutoSaveEnabled() && !enterKey && !autoSaveDone && newRow && (editForm.hasChanged || editForm.isNew)) {
      var actionObject = {
          target: this,
          method: this.cellEditEnd,
          parameters: [editCompletionEvent, newValue, ficCallDone, true]
        };
      this.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      return;
    }
    if (ficCallDone) {
      // get new value as the row can have changed
      this.Super('cellEditEnd', [editCompletionEvent, this.getEditValue(rowNum, colNum)]);
      return;
    } else {
      // only needed for non picklist fields
      // as picklist fields will always have picked a value
      // note that focusItem updatevalue for picklist can result in extra datasource requests
      if (focusItem && !focusItem.hasPickList) {
        focusItem.blur(focusItem.form, focusItem);
        if (editForm.inFicCall) {
          // use editValues object as the edit form will be re-used for a next row
          var editValues = this.getEditValues(rowNum);
          editValues.actionAfterFicReturn = {
            target: this,
            method: this.cellEditEnd,
            parameters: [editCompletionEvent, newValue, true, autoSaveDone]
          };
          return;
        }
      }      
    }
    if (newValue) {
      this.Super('cellEditEnd', [editCompletionEvent, newValue]);
    } else {
      this.Super('cellEditEnd', [editCompletionEvent]);
    }
  },
  
  // overridden to set the enterkeyaction to nextrowstart in cases the current row
  // is the last being edited
  // also sets a flag which is used in canEditCell   
  getNextEditCell: function (rowNum, colNum, editCompletionEvent) {
    var ret, i, length = this.getFields().length;
    this._inGetNextEditCell = true;
    // past the last row
    if (editCompletionEvent === isc.ListGrid.ENTER_KEYPRESS && rowNum === (this.getTotalRows() - 1)) {
      // move to the next row
      ret = this.findNextEditCell(rowNum + 1, 0, 1, true, true);

      // force the focus column in the first focus field
      for (i = 0; i < length; i++) {
        if (this.getFields()[i].editorProperties && this.getFields()[i].editorProperties.firstFocusedField) {
          this.forceFocusColumn = this.getFields()[i].name;
          break;
        }
      }    

    } else {
      ret = this.Super('getNextEditCell', arguments);
    }
        
    // when moving between rows with the arrow keys, force the focus in the correct 
    // column
    if (ret && ret[0] !== rowNum && this.getField(colNum) && 
          (editCompletionEvent === isc.ListGrid.UP_ARROW_KEYPRESS || 
              editCompletionEvent === isc.ListGrid.DOWN_ARROW_KEYPRESS)) {
      this.forceFocusColumn = this.getField(colNum).name;
    }
    
    delete this._inGetNextEditCell;
    return ret;
  },

  // overridden to take into account disabled at item level
  // only used when computing the next edit cell
  // if caneditcell returns false in other cases then smartclient
  // won't even show an input but shows the display value directly
  // this interferes sometimes with the very dynamic enabling 
  // disabling of fields by the readonlylogic
  canEditCell: function (rowNum, colNum) {
    var ret;
    if (this._inGetNextEditCell) {
      var field = this.getField(colNum);
      if (field && this.getEditForm()) {
        var item = this.getEditForm().getItem(field.name);
        if (item && item.isDisabled()) {
          return false;
        }
      }
    }
    
    if (!colNum && colNum !== 0) {
      return false;
    }

    ret = this.Super('canEditCell', arguments);
    return ret;
  },
      
  // saveEditedValues: when saving, first check if a FIC call needs to be done to update to the 
  // latest values. This can happen when the focus is in a field and the save action is
  // done, at that point first try to force a fic call (handleItemChange) and if that
  // indeed happens stop the saveEdit until the fic returns
  saveEditedValues: function(rowNum, colNum, newValues, oldValues, editValuesID, editCompletionEvent, saveCallback, ficCallDone){
    if (!rowNum && rowNum !== 0) {
      rowNum = this.getEditRow();
    }
    if (!colNum && colNum !== 0) {
      colNum = this.getEditCol();
    }
    
    // nothing changed just fire the calback and bail
    if (!ficCallDone && this.getEditForm() && !this.getEditForm().hasChanged && !this.getEditForm().isNew) {
      if (saveCallback) {
        this.fireCallback(saveCallback, "rowNum,colNum,editCompletionEvent,success", [rowNum, colNum, editCompletionEvent]);
      }
      return true;
    }
    
    if (ficCallDone) {
      // reset the new values as this can have changed because of a fic call
      newValues = this.getEditValues(editValuesID);
    } else {
      var editForm = this.getEditForm(), focusItem = editForm.getFocusItem();
      if (focusItem && !focusItem.hasPickList) {
        focusItem.blur(focusItem.form, focusItem);
        if (editForm.inFicCall) {
          // use editValues object as the edit form will be re-used for a next row
          var editValues = this.getEditValues(editValuesID);
          editValues.actionAfterFicReturn = {
            target: this,
            method: this.saveEditedValues,
            parameters: [rowNum, colNum, newValues, oldValues, editValuesID, editCompletionEvent, saveCallback, true]
          };
          return;
        }
      }
    }
    this.Super('saveEditedValues', [rowNum, colNum, newValues, oldValues, editValuesID, editCompletionEvent, saveCallback]);
    // commented out as it removes an autosave action which is done in the edit complete method
//    this.view.standardWindow.setDirtyEditForm(null);
  },
  
  autoSave: function(){
    this.storeUpdatedEditorValue();
    this.endEditing();
  },
  
  hideInlineEditor: function(focusInBody, suppressCMHide) {

    var rowNum = this.getEditRow(), record = this.getRecord(rowNum), editForm = this.getEditForm();
    this._hidingInlineEditor = true;
    if (record && (rowNum === 0 || rowNum)) {
      if (!this.rowHasErrors(rowNum)) {
        record[this.recordBaseStyleProperty] = null;
      }
  
      if (record && record.editColumnLayout) {
        isc.Log.logDebug('hideInlineEditor has record and editColumnLayout', 'OB');
        record.editColumnLayout.showEditOpen();
      } else if (this.currentEditColumnLayout) {
        this.currentEditColumnLayout.showEditOpen();
      } else {
        isc.Log.logDebug('hideInlineEditor has NO record and editColumnLayout', 'OB');
      }
      this.view.isEditingGrid = false;
    }
    
    // always hide the clickmask, as it needs to be re-applied
    // this super call needs to be done before clearing the values
    // of the form, as the form value clear will result
    // in a field to be flagged with an error
    var ret = this.Super('hideInlineEditor', [focusInBody, false]);

    if (editForm) {
      // canFocus is set when disabling a form item
      // a new record needs to compute canFocus again
      editForm.resetCanFocus();
      // clear all values, as null values in the new row won't overwrite filled form
      // values
      editForm.clearValues();      
      // clear the errors so that they don't show up at the next row
      editForm.clearErrors();
    }

    delete this._hidingInlineEditor;
    return ret;
  },
  
  getEditDisplayValue: function(rowNum, colNum, record){
    // somehow this extra call is needed to not restore
    // the old value when the new value is null
    this.storeUpdatedEditorValue();
    return this.Super('getEditDisplayValue', arguments);
  },
  
  showInlineEditor: function(rowNum, colNum, newCell, newRow, suppressFocus){
    var fld;
    
    this._showingEditor = true;
    
    if (newRow) {
      if (this.getEditForm()) {
        this.getEditForm().clearErrors();
      }
      // if the focus does not get suppressed then the clicked field will receive focus
      // and won't be disabled so the user can already start typing      
      suppressFocus = true;
    }
    
    var ret = this.Super('showInlineEditor', [rowNum, colNum, newCell, newRow, suppressFocus]);
    
    if (!newRow) {
      delete this._showingEditor;
      return ret;
    }
    
    if (this.forceFocusColumn) {
      // set the field to focus on after returning from the fic
      this.getEditForm().forceFocusedField = this.forceFocusColumn;
      delete this.forceFocusColumn;
    } else if (colNum || colNum === 0) {
      fld = this.getField(colNum);
      this.getEditForm().forceFocusedField = fld.name;
    }
    
    var record = this.getRecord(rowNum);
    
    this.view.isEditingGrid = true;
    
    record[this.recordBaseStyleProperty] = this.baseStyleEdit;
    
    // also called in case of new
    var form = this.getEditForm();
    
    // also make sure that the new indicator is send to the server
    if (record._new) {
      form.setValue('_new', true);
    }
    
    form.doEditRecordActions(false, record._new && !record._editedBefore);
    record._editedBefore = true;
    
    // must be done after doEditRecordActions    
    if (this.rowHasErrors(rowNum)) {
      this.getEditForm().setErrors(this.getRowValidationErrors(rowNum));
      this.view.standardWindow.setDirtyEditForm(form);
    }
    
    if (record && record.editColumnLayout) {
      record.editColumnLayout.showSaveCancel();
    }
    
    this.view.messageBar.hide();
    
    delete this._showingEditor;
    return ret;
  },
  
  closeAnyOpenEditor: function(){
    delete this.wasEditing;
    // close any editors we may have
    if (this.getEditRow() || this.getEditRow() === 0) {
      this.endEditing();
    }
  },
  
  validateField: function(field, validators, value, record, options){
    // Smartclient passes in the grid field, use the editform field
    // as it contains the latest valuemap
    var editField = this.getEditForm().getField(field.name) || field;
    var ret = this.Super('validateField', [editField, validators, value, record, options]);
    return ret;
  },

  refreshEditRow: function(){
    var editRow = this.view.viewGrid.getEditRow(), i, length;
    if (editRow || editRow === 0) {
      // don't refresh the frozen fields, this give strange
      // styling issues in chrome
      length = this.view.viewGrid.fields.length;
      for (i = 0; i < length; i++) {
        if (!this.fieldIsFrozen(i)) {
          this.view.viewGrid.refreshCell(editRow, i, true);
        }
      }
    }
  },

  // having a valueMap property results in setValueMap to be called
  // on an item. On items with a picklist this causes calls to the
  // server side
  //  https://issues.openbravo.com/view.php?id=16611
  getEditItem: function() {
    var result = this.Super('getEditItem', arguments);
    if (result.hasOwnProperty('valueMap') && !result.valueMap) {
      delete result.valueMap;
    }
    return result;
  },

  // set some flags to prevent the picklist fields from doing extra datasource 
  // requests
  // https://issues.openbravo.com/view.php?id=16611
  storeUpdatedEditorValue : function (suppressChange, editCol) {
    this._storingUpdatedEditorValue = true;
    this.Super('storeUpdatedEditorValue', arguments);
    delete this._storingUpdatedEditorValue;
  },

  // the form gets recreated many times, maintain the already read valuemap
  getEditorValueMap: function(field, values){
    var editRow = this.getEditRow(), editValues = this.getEditValues(editRow);
    // valuemap is set in the processcolumnvalues of the ob-view-form.js
    if (editValues && editValues[field.name + '._valueMap']) {
      return editValues[field.name + '._valueMap'];
    }
    
    if (this.getEditForm() && this.getEditForm().getField(field.name)) {
      var liveField = this.getEditForm().getField(field.name);
      if (liveField.valueMap) {
        return liveField.valueMap;
      }
    }
    
    return this.Super('getEditorValueMap', arguments);
  },
  
  setFieldError: function (rowNum, fieldID, errorMessage, dontDisplay) {
    // if there are no errors then no need to clear
    // prevents an undefined exception because also keep errors in other 
    // places then the editvalues._validationErrors
    if (!errorMessage && !this.Super('cellHasErrors', [rowNum, fieldID])) {
      return;
    }
    return this.Super('setFieldError', arguments);
   },
  
  cellHasErrors: function(rowNum, fieldID){
    if (this.Super('cellHasErrors', arguments)) {
      return true;
    }
    if (this.getEditRow() === rowNum) {
      var itemName = this.getEditorName(rowNum, fieldID);
      
      if (this.getEditForm().hasFieldErrors(itemName)) {
        return true;
      }
      // sometimes the error is there but the error message is null
      if (this.getEditForm().getErrors().hasOwnProperty(itemName)) {
        return true;
      }
    }
    return false;
  },
  
  getCellErrors: function(rowNum, fieldName){
    var itemName;
    var ret = this.Super('getCellErrors', arguments);
    if (this.getEditRow() === rowNum) {
      return this.getEditForm().getFieldErrors(itemName);
    }
    return ret;
  },
  
  rowHasErrors: function(rowNum, colNum){
    if (this.Super('rowHasErrors', arguments)) {
      return true;
    }
    if (!this.getEditForm()) {
      return false;
    }
    if (this.getEditRow() === rowNum && this.getEditForm().hasErrors()) {
      return true;
    }
    var record = this.getRecord(rowNum);
    if (record) {
      return record[isc.OBViewGrid.ERROR_MESSAGE_PROP];
    }
    return false;
  },
  
  // we are being reshown, get new values for the combos
  visibilityChanged: function(visible){
    if (visible && this.getEditRow()) {
      this.getEditForm().doChangeFICCall();
    }
    if (!this.view.isVisible() && this.hasErrors()) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_TabWithErrors', [this.view.tabTitle]));
    }
  },

  isWritable: function(record){
    return !record._readOnly;
  },

  allSelectedRecordsWritable: function() {
    var i, length = this.getSelectedRecords().length;
    for (i = 0; i < length; i++) {
      var record = this.getSelectedRecords()[i];
      if (!this.isWritable(record) || record._new) {
        return false;
      }
    }
    return true;
  },

  setRecordErrorMessage: function(rowNum, msg){
    var record = this.getRecord(rowNum);
    if (!record) {
      return;
    }
    record[isc.OBViewGrid.ERROR_MESSAGE_PROP] = msg;
    if (msg) {
      record[this.recordBaseStyleProperty] = this.recordStyleError;
    } else {
      record[this.recordBaseStyleProperty] = null;
    }
    if (record.editColumnLayout) {
      record.editColumnLayout.editButton.setErrorState(msg);
      record.editColumnLayout.editButton.setErrorMessage(msg);
    }
    this.refreshRow(rowNum);
  },
  
  setRecordFieldErrorMessages: function(rowNum, errors){
    var record = this.getRecord(rowNum);
    if (!record) {
      return;
    }
    if (record.editColumnLayout) {
      record.editColumnLayout.editButton.setErrorState(errors);
      record.editColumnLayout.editButton.setErrorMessage(OB.I18N.getLabel('OBUIAPP_ErrorInFields'));
    }
    this.setRowErrors(rowNum, errors);
    if (errors) {
      record[this.recordBaseStyleProperty] = this.recordStyleError;
    } else {
      record[this.recordBaseStyleProperty] = null;
    }
    
    if (this.frozenBody) {
      this.frozenBody.markForRedraw();
    }
    this.body.markForRedraw();
  },
  
  // overridden to handle the case that the rowNum is in fact 
  // an edit state id
  getRecord: function(rowNum){
    if (!isc.isA.Number(rowNum)) {
      // an edit id
      rowNum = this.getEditSessionRowNum(rowNum);
      return this.Super('getRecord', [rowNum]);
    }
    return this.Super('getRecord', arguments);
  },

  // always work with fixed rowheights
  // https://issues.openbravo.com/view.php?id=16307
  shouldFixRowHeight: function() {
    return true;
  },
  
  // needed for: https://issues.openbravo.com/view.php?id=16307
  getRowHeight: function() {
    return this.cellHeight;
  },

  // +++++++++++++++++ functions for the edit-link column +++++++++++++++++
  
  createRecordComponent: function(record, colNum){
    var fld = this.getFields()[colNum], canvas,
      rowNum = this.getRecordIndex(record), isEditRecord = rowNum === this.getEditRow();
    if (this.isEditLinkColumn(colNum)) {
      var layout = isc.OBGridButtonsComponent.create({
        record: record,
        grid: this
      });
      layout.editButton.setErrorState(record[isc.OBViewGrid.ERROR_MESSAGE_PROP]);
      layout.editButton.setErrorMessage(record[isc.OBViewGrid.ERROR_MESSAGE_PROP]);
      layout.showEditOpen();
      record.editColumnLayout = layout;
      return layout;
    }
    if (fld.clientClass && !isEditRecord) {
      canvas = isc.ClassFactory.newInstance(fld.clientClass, {grid: this, fieldName: fld.name, rowNum: rowNum, record: record, colNum: colNum});
      if (canvas) {
        if (canvas.setRecord) {
          canvas.setRecord(record);
        }
        return canvas;
      }
    }
  },
  
  updateRecordComponent: function(record, colNum, component, recordChanged){
    var rowNum = this.getRecordIndex(record), isEditRecord = rowNum === this.getEditRow();
    if (component.editButton) {
      if (recordChanged && component.record.editColumnLayout === component) {
        component.record.editColumnLayout = null;
      }
      component.record = record;
      record.editColumnLayout = component;
      component.editButton.setErrorState(record[isc.OBViewGrid.ERROR_MESSAGE_PROP]);
      component.editButton.setErrorMessage(record[isc.OBViewGrid.ERROR_MESSAGE_PROP]);
      component.showEditOpen();
    } else if (isEditRecord) {
      return null;
    } else {
      if (component.setRecord) {
        component.setRecord(record);
      } else {
        component.record = record;
      }
      component.rowNum = rowNum;
    }
    return component;
  },
  
  isEditLinkColumn: function(colNum){
    return this.editLinkColNum === colNum;
  }
  
});

// = OBGridToolStripIcon =
// The icons which are inside of OBGridToolStrip
isc.ClassFactory.defineClass('OBGridToolStripIcon', isc.ImgButton);

isc.OBGridToolStripIcon.addProperties({
  buttonType: null, /* This could be: edit - form - cancel - save */
  initWidget: function(){
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }
});

// = OBGridToolStripSeparator =
// The separator between icons of OBGridToolStrip
isc.ClassFactory.defineClass('OBGridToolStripSeparator', isc.Img);

isc.OBGridToolStripSeparator.addProperties({  
});

// = OBGridButtonsComponent =
// The component which is used to create the contents of the
// edit open column in the grid
isc.ClassFactory.defineClass('OBGridButtonsComponent', isc.HLayout);

isc.OBGridButtonsComponent.addProperties({
  OBGridToolStrip: null,
  saveCancelLayout: null,
  
  // the grid to which this component belongs
  grid: null,
  
  rowNum: null,
  
  // the record to which this component belongs
  record: null,
  
  initWidget: function(){
    var me = this, formButton;
    
    this.editButton = isc.OBGridToolStripIcon.create({
      buttonType: 'edit',
      originalPrompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      prompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      action: function(){
        var actionObject = {
          target: me,
          method: me.doEdit,
          parameters: null
        };
        me.grid.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      },
      
      setErrorMessage: function(msg){
        if (msg) {
          this.prompt = msg + '<br><br>' + this.originalPrompt;
        } else {
          this.prompt = this.originalPrompt;
        }
      },
      
      showable: function(){
        return !me.grid.view.readOnly && !me.record._readOnly;
      },
      
      show: function(){
        if (!this.showable()) {
          return;
        }
        return this.Super('show', arguments);
      }
    });
    
    formButton = isc.OBGridToolStripIcon.create({
      buttonType: 'form',
      prompt: OB.I18N.getLabel('OBUIAPP_GridFormButtonPrompt'),
      action: function(){
        var actionObject = {
          target: me,
          method: me.doOpen,
          parameters: null
        };
        me.grid.view.standardWindow.doActionAfterAutoSave(actionObject, true);
      }
    });
    
    this.buttonSeparator1 = isc.OBGridToolStripSeparator.create({});
    
    if (me.grid.view.readOnly) {
      this.buttonSeparator1.visibility = 'hidden';
    }
    
    this.addMembers([formButton, this.buttonSeparator1, this.editButton]);
    this.Super('initWidget', arguments);
  },
  
  addSaveCancelProgressButtons: function() {
    var me = this;
    // already been here
    if (this.cancelButton) {
      return;
    }
    
    this.progressIcon = isc.Img.create(this.grid.progressIconDefaults);
    this.progressIcon.setVisibility(false);
    this.addMember(this.progressIcon, 0);
    
    // is referred to in OBViewForm.showClickMask
    this.cancelButton = isc.OBGridToolStripIcon.create({
      buttonType: 'cancel',
      prompt: OB.I18N.getLabel('OBUIAPP_GridCancelButtonPrompt'),
      action: function(){
        me.doCancel();
      }
    });
    
    var saveButton = isc.OBGridToolStripIcon.create({
      buttonType: 'save',
      prompt: OB.I18N.getLabel('OBUIAPP_GridSaveButtonPrompt'),
      action: function(){
        me.doSave();
      }
    });
    
    this.addMembers([ this.cancelButton, isc.OBGridToolStripSeparator.create({}), saveButton]);
  },
  
  toggleProgressIcon: function(toggle){
    if (toggle) {
      this.hideMember(6);
      this.hideMember(5);
      this.hideMember(4);
      this.showMember(0);
    } else {
      var offset = 0;
      if (this.cancelButton) {
        offset = 1;
        this.hideMember(0);
      }
      this.showMember(2 + offset);
      this.showMember(1 + offset);
      this.showMember(offset);
    }
  },
  
  showEditOpen: function(){
    var offset = 0;
    if (this.cancelButton) {
      this.hideMember(6);
      this.hideMember(5);
      this.hideMember(4);
      this.hideMember(0);
      offset = 1;
    }
    this.showMember(offset);
    if (this.editButton.showable()) {
      this.showMember(1 + offset);
      this.showMember(2 + offset);
    } else {
      this.hideMember(1 + offset);
      this.hideMember(2 + offset);
    }
    this.grid.currentEditColumnLayout = null;
  },
  
  showSaveCancel: function(){
    this.addSaveCancelProgressButtons();
    
    this.hideMember(3);
    this.hideMember(2);
    this.hideMember(1);
    this.hideMember(0);
    
    this.showMember(4);
    this.showMember(5);
    this.showMember(6);
    
    this.grid.currentEditColumnLayout = this;
  },
  
  doEdit: function(){
    this.showSaveCancel();
    this.grid.selectSingleRecord(this.record);
    var rowNum = this.grid.getRecordIndex(this.record);
    this.grid.startEditing(rowNum);
  },
  
  doOpen: function(){
    this.grid.endEditing();
    this.grid.view.editRecord(this.record);
  },
  
  doSave: function(){
    // note change back to editOpen is done in the editComplete event of the
    // grid itself
    this.grid.endEditing();
  },
  
  doCancel: function(){
    this.grid.cancelEditing();
  },
  
  hideMember: function(memberNo) {
    if (!this.members[memberNo]) {
      return;
    }
    // already hidden
    if (this.members[memberNo] && this.members[memberNo].visibility === isc.Canvas.HIDDEN) {
      return;
    }
    this.Super('hideMember', arguments);
  },

  showMember: function(memberNo) {
    if (!this.members[memberNo]) {
      return;
    }
    // already visible
    if (this.members[memberNo] && (this.members[memberNo].visibility === isc.Canvas.INHERIT || this.members[memberNo].visibility === isc.Canvas.VISIBLE)) {
      return;
    }
    this.Super('showMember', arguments);
  }

});
