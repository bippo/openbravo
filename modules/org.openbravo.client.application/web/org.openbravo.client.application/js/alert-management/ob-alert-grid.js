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

isc.ClassFactory.defineClass('OBAlertGrid', isc.OBGrid);
isc.OBAlertGrid.addProperties({
  alertStatus: null,

  width: '100%',
  height: '100%',
  dataSource: null,
  canEdit: true,
  alternateRecordStyles: true,
  showFilterEditor: true,
  canReorderFields: false,
  canFreezeFields: false,
  canGroupBy: false,
  canAutoFitFields: false,
  selectionType: 'simple',
  //editEvent: 'click',
  editOnFocus: true,
  showCellContextMenus: true,
  selectOnEdit: false,
  dataPageSize: 100,

  // keeps track if we are in objectSelectionMode or in toggleSelectionMode
  // objectSelectionMode = singleRecordSelection === true
  singleRecordSelection: false,

  dataProperties: {
    useClientFiltering: false //,
    //useClientSorting: false
  },

  gridFields: [{
    name: 'alertRule',
    title: OB.I18N.getLabel('OBUIAPP_AlertGrid_AlertRule'),
    displayField: 'alertRule._identifier',
    canFilter: true,
    canEdit: false,
    filterOnKeypress: true,
    filterEditorType: 'OBFKFilterTextItem',
    type: '_id_19'
  }, {
    name: 'description',
    title: OB.I18N.getLabel('OBUIAPP_AlertGrid_Alert'),
    canFilter: true,
    canEdit: false,
    filterOnKeypress: true,
    filterEditorType: 'OBTextItem',
    type: '_id_10'
  }, {
    name: 'creationDate',
    title: OB.I18N.getLabel('OBUIAPP_AlertGrid_Time'),
    canFilter: true,
    canEdit: false,
    filterEditorType: 'OBMiniDateRangeItem',
    type: '_id_16'
  }, {
    name: 'comments',
    title: OB.I18N.getLabel('OBUIAPP_AlertGrid_Note'),
    canFilter: true,
    canEdit: true,
    filterOnKeypress: true,
    filterEditorType: 'OBTextItem',
    editorType: 'OBTextItem',
    editorProperties: {
      width: '90%',
      columnName: 'comments',
      disabled: false,
      updatable: true
    },
    type: '_id_10'
  }, {
    name: 'recordID',
    title: OB.I18N.getLabel('OBUIAPP_AlertGrid_Record'),
    canFilter: true,
    canEdit: false,
    isLink: true,
    filterOnKeypress: true,
    filterEditorType: 'OBTextItem',
    type: '_id_10',
    formatCellValueFunctionReplaced: true,
    formatCellValue: function (value, record, rowNum, colNum, grid) {
      return '';
    }
  }],

  initWidget: function () {
    // added for showing counts in the filtereditor row
    this.checkboxFieldDefaults = isc.addProperties(this.checkboxFieldDefaults, {
      canFilter: true,
      //frozen: true, /* Removed due to issue https://issues.openbravo.com/view.php?id=17611 */
      canFreeze: true,
      showHover: true,
      prompt: OB.I18N.getLabel('OBUIAPP_GridSelectAllColumnPrompt'),
      filterEditorProperties: {
        textAlign: 'center'
      },
      filterEditorType: 'StaticTextItem'
    });

    this.contextMenu = this.getMenuConstructor().create({
      items: []
    });

    OB.Datasource.get('ADAlert', this, null, true);

    this.Super('initWidget', arguments);
  },

  setDataSource: function (ds) {
    this.Super('setDataSource', [ds, this.gridFields]);
    // Some properties need to be set when the datasource is loaded to avoid errors when form is
    // open the first time.
    this.setSelectionAppearance('checkbox');

    // this extra call is needed to solve this issue:
    // https://issues.openbravo.com/view.php?id=17145
    this.refreshFields();

    this.sort('creationDate', 'descending');

    this.fetchData();
  },

  dataArrived: function (startRow, endRow) {
    this.getGridTotalRows();
    return this.Super('dataArrived', arguments);
  },

  getGridTotalRows: function () {
    var criteria = this.getCriteria() || {},
        requestProperties = {};

    if (!OB.AlertManagement.sections[this.alertStatus].expanded) {
      // fetch to the datasource with an empty criteria to get all the rows
      requestProperties.params = requestProperties.params || {};
      requestProperties.params[OB.Constants.WHERE_PARAMETER] = this.getFilterClause();
      requestProperties.clientContext = {
        alertStatus: this.alertStatus
      };
      this.dataSource.fetchData(criteria, function (dsResponse, data, dsRequest) {
        OB.AlertManagement.setTotalRows(dsResponse.totalRows, dsResponse.clientContext.alertStatus);
      }, requestProperties);

    } else {
      OB.AlertManagement.setTotalRows(this.getTotalRows(), this.alertStatus);
    }
  },

  onFetchData: function (criteria, requestProperties) {
    requestProperties = requestProperties || {};
    requestProperties.params = requestProperties.params || {};

    requestProperties.params[OB.Constants.WHERE_PARAMETER] = this.getFilterClause();
  },

  getFilterClause: function () {
    var i, filterClause = '',
        alertRuleIds = '',
        arlength = OB.AlertManagement.alertRules.length,
        whereClause = 'coalesce(to_char(status), \'NEW\') = upper(\'' + this.alertStatus + '\')';

    for (i = 0; i < arlength; i++) {
      if (alertRuleIds !== '') {
        alertRuleIds += ',';
      }
      alertRuleIds += '\'' + OB.AlertManagement.alertRules[i].alertRuleId + '\'';
      // if an alertRule has some alerts to filter by, add them to the where clause as:
      // alerts are of a different alertRule or only the alerts predefined
      // this only happens if the alertRule has an SQL filter expression defined
      if (OB.AlertManagement.alertRules[i].alerts) {
        filterClause += ' and (e.alertRule.id != \'' + OB.AlertManagement.alertRules[i].alertRuleId + '\'';
        filterClause += ' or e.id in (' + OB.AlertManagement.alertRules[i].alerts + '))';
      }
    }
    if (alertRuleIds !== '') {
      whereClause += ' and alertRule.id in (' + alertRuleIds + ')';
    } else {
      whereClause += ' and 1=2';
    }
    if (filterClause !== '') {
      whereClause += filterClause;
    }
    return whereClause;
  },

  headerClick: function (fieldNum, header, autoSaveDone) {
    var field = this.fields[fieldNum];
    if (this.isCheckboxField(field) && this.singleRecordSelection) {
      this.deselectAllRecords();
      this.singleRecordSelection = false;
    }
    return this.Super('headerClick', arguments);
  },

  cellClick: function (record, rowNum, colNum) {
    var i, tabId, field = this.getField(colNum),
        length = OB.AlertManagement.alertRules.length;
    for (i = 0; i < length; i++) {
      if (OB.AlertManagement.alertRules[i].alertRuleId === record.alertRule) {
        tabId = OB.AlertManagement.alertRules[i].tabId;
      }
    }
    if (field.isLink && tabId && tabId !== '') {
      OB.Utilities.openDirectTab(tabId, record.referenceSearchKey);
    }
  },

  recordClick: function (viewer, record, recordNum, field, fieldNum, value, rawValue) {
    this.handleRecordSelection(viewer, record, recordNum, field, fieldNum, value, rawValue, false, true);
  },


  // +++++++++++++++++++++++++++++ Record Selection Handling +++++++++++++++++++++++
  // Functions based on the ob-view-grid.js Record Selection Handling.
  deselectAllRecords: function (preventUpdateSelectInfo, autoSaveDone) {
    this.allSelected = false;
    var ret = this.Super('deselectAllRecords', arguments);
    this.lastSelectedRecord = null;
    if (!preventUpdateSelectInfo) {
      this.selectionUpdated();
    }
    return ret;
  },

  selectAllRecords: function (autoSaveDone) {
    this.allSelected = true;
    var ret = this.Super('selectAllRecords', arguments);
    this.selectionUpdated();
    return ret;
  },

  updateSelectedCountDisplay: function () {
    var selection = this.getSelection();
    var selectionLength = selection.getLength();
    var newValue = '&nbsp;';
    if (selectionLength > 0) {
      newValue = String(selectionLength);
    }
    if (this.filterEditor) {
      this.filterEditor.getEditForm().setValue(this.getCheckboxField().name, newValue);
    }
  },

  // note when solving selection issues in the future also
  // consider using the selectionChanged method, but that
  // one has as disadvantage that it is called multiple times
  // for one select/deselect action
  selectionUpdated: function (record, recordList) {

    this.stopHover();
    this.updateSelectedCountDisplay();
    if (this.getSelectedRecords() && this.getSelectedRecords().length !== 1) {
      this.lastSelectedRecord = null;
    } else {
      this.lastSelectedRecord = this.getSelectedRecord();
    }
  },

  selectOnMouseDown: function (record, recordNum, fieldNum, autoSaveDone) {
    // don't change selection on right mouse down
    var EH = isc.EventHandler,
        eventType;

    // don't do anything if right-clicking on a selected record
    if (EH.rightButtonDown() && this.isSelected(record)) {
      return;
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
      eventType = EH.getEventType();
      if (!EH.isMouseEvent(eventType)) {
        this.handleRecordSelection(null, record, recordNum, null, fieldNum, null, null, true);
      }
    }
  },

  handleRecordSelection: function (viewer, record, recordNum, field, fieldNum, value, rawValue, fromSelectOnMouseDown) {
    var EH = isc.EventHandler;
    var keyName = EH.getKey();

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
      } else if (!(!EH.ctrlKeyDown() && EH.altKeyDown() && EH.shiftKeyDown())) { // 'if' statement to avoid do an action when the KS to move to a child tab is fired
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
    } else {
      // click on the record which was already selected
      this.doSelectSingleRecord(record);
    }

    this.updateSelectedCountDisplay();

    // mark some redraws if there are lines which don't
    // have a checkbox flagged, so if we move from single record selection
    // to multi record selection
    if (!this.singleRecordSelection && previousSingleRecordSelection) {
      this.markForRedraw('Selection checkboxes need to be redrawn');
    }
  },

  //selectRecordForEdit: function(record){
  //  this.Super('selectRecordForEdit', arguments);
  //  this.doSelectSingleRecord(record);
  //},
  doSelectSingleRecord: function (record) {
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
    var fieldNum = this.getCheckboxFieldPosition(),
        field = this.fields[fieldNum];
    var icon = this.checkboxFieldFalseImage || this.booleanFalseImage;
    var title = this.getValueIconHTML(icon, field);

    this.setFieldTitle(fieldNum, title);
  },

  // overridden to prevent the checkbox to be shown when only one record is selected.
  getCellValue: function (record, recordNum, fieldNum, gridBody) {
    var field = this.fields[fieldNum];
    if (!field || this.allSelected) {
      return this.Super('getCellValue', arguments);
    }
    // do all the cases which are handled in the super directly
    if (this.isCheckboxField(field)) {
      // NOTE: code copied from super class
      var icon;
      if (!this.body.canSelectRecord(record)) {
        // record cannot be selected but we want the space allocated for the
        // checkbox anyway.
        icon = '[SKINIMG]/blank.gif';
      } else if (this.singleRecordSelection && !this.allSelected) {
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

  getSelectedRecords: function () {
    return this.getSelection();
  },
  // ++++++++++++++ end of Record Selection handling ++++++++++++++
  // overridden to support hover on the header for the checkbox field
  setFieldProperties: function (field, properties) {
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

  cellHoverHTML: function (record, rowNum, colNum) {
    var field = this.getField(colNum),
        cellErrors, msg = '',
        i;
    if (this.isCheckboxField(field)) {
      return OB.I18N.getLabel('OBUIAPP_GridSelectColumnPrompt');
    }
  },

  makeCellContextItems: function (record, rowNum, colNum) {
    var menuItems = [];
    var grid = this;
    if (grid.alertStatus === 'Acknowledged' || grid.alertStatus === 'Suppressed') {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_MoveToStatus', [OB.AlertManagement.translatedStatus.New]),
        click: function () {
          OB.AlertManagement.moveToStatus(record.id, grid.alertStatus, 'New');
        }
      });
    }
    if (grid.alertStatus === 'New' || grid.alertStatus === 'Suppressed') {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_MoveToStatus', [OB.AlertManagement.translatedStatus.Acknowledged]),
        click: function () {
          OB.AlertManagement.moveToStatus(record.id, grid.alertStatus, 'Acknowledged');
        }
      });
    }
    if (grid.alertStatus === 'New' || grid.alertStatus === 'Acknowledged') {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_MoveToStatus', [OB.AlertManagement.translatedStatus.Suppressed]),
        click: function () {
          OB.AlertManagement.moveToStatus(record.id, grid.alertStatus, 'Suppressed');
        }
      });
    }
    return menuItems;
  },

  // a trick to prevent a javascript error, draw the first time
  // without record components
  // this topic needs to be revisited after a certain time
  // to check if newer Smartclient components solve it
  // Note, this was not reproducable in Smartclient standard
  // https://issues.openbravo.com/view.php?id=17289
  // https://issues.openbravo.com/view.php?id=17784
  firstTimeRedrawCalled: true,
  draw: function () {
    if (this.firstTimeRedrawCalled && this.showRecordComponents) {
      this.showRecordComponents = false;
      this.Super('draw', arguments);
      this.showRecordComponents = true;
      delete this.firstTimeRedrawCalled;
      this.Super('redraw', arguments);
      return;
    }
    delete this.firstTimeRedrawCalled;
    this.Super('draw', arguments);
  }

});