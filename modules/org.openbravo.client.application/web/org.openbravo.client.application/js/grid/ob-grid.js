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
isc.ClassFactory.defineClass('OBGrid', isc.ListGrid);

// = OBGrid =
// The OBGrid combines common grid functionality usefull for different 
// grid implementations.
isc.OBGrid.addProperties({

  dragTrackerMode: 'none',

  // recycle gives better performance but also results
  // in strange results that not all record components are
  // drawn when scrolling very fast
  recordComponentPoolingMode: 'viewport',

  showRecordComponentsByCell: true,
  recordComponentPosition: 'within',
  poolComponentsPerColumn: true,
  showRecordComponents: true,
  escapeHTML: true,
  bodyProperties: {
    canSelectText: true,

    // the redraw on change should not only redraw the current item
    // but the whole edit row, make sure that happens asynchronously
    redrawFormItem: function (item, reason) {
      var lg = this.grid,
          row = lg.getEditRow(),
          col = lg.getColNum(item.getFieldName());

      // If the user has edited the cell, or setValue() has been called on the item
      // we don't want a call to redraw() on the item to drop that value
      if (lg.getEditCol() === col) {
        lg.storeUpdatedEditorValue();
      }

      if (row === 0 || row > 0) {
        lg.fireOnPause('refreshEditRow', function () {
          lg.refreshRow(row);
        });
      }
    }
  },

  enableShortcuts: function () {
    var me = this,
        ksAction_FocusFilter, ksAction_FocusGrid, ksAction_ClearFilter, ksAction_SelectAll, ksAction_UnselectAll;

    ksAction_FocusFilter = function () {
      me.focusInFirstFilterEditor();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('Grid_FocusFilter', 'OBGrid.body', ksAction_FocusFilter);

    ksAction_FocusGrid = function () {
      me.focus();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('Grid_FocusGrid', 'OBGrid.filter', ksAction_FocusGrid);

    ksAction_ClearFilter = function () {
      me.clearFilter(true);
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('Grid_ClearFilter', ['OBGrid.body', 'OBGrid.filter'], ksAction_ClearFilter);

    ksAction_SelectAll = function () {
      me.selectAllRecords();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('Grid_SelectAll', 'OBGrid.body', ksAction_SelectAll);

    ksAction_UnselectAll = function () {
      if (me.getSelectedRecords().length > 1) {
        me.deselectAllRecords();
      }
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('Grid_UnselectAll', 'OBGrid.body', ksAction_UnselectAll);
  },

  draw: function () {
    this.enableShortcuts();
    this.Super('draw', arguments);
  },

  bodyKeyPress: function (event, eventInfo) {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBGrid.body');
    if (response !== false) {
      response = this.Super('bodyKeyPress', arguments);
    }
    return response;
  },

  filterFieldsKeyDown: function (item, form, keyName) {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBGrid.filter');
    if (response !== false) {
      if (isc.EventHandler.getKeyName() === 'Tab' && !isc.EventHandler.ctrlKeyDown() && !isc.EventHandler.altKeyDown()) {
        return false; // To avoid strange double field jump while pressing Tab Key
      }
      response = this.Super('filterFieldsKeyDown', arguments);
    }
    return response;
  },

  focusInFirstFilterEditor: function () {
    if (this.getFilterEditor()) { // there is a filter editor
      var object = this.getFilterEditor().getEditForm(),
          items, item, i, length;

      // compute a focusable item
      items = object.getItems();
      length = items.length;
      for (i = 0; i < length; i++) {
        item = items[i];
        // The first filterable item (editorType!=='StaticTextItem') should be focused
        if (item.getCanFocus() && !item.isDisabled() && item.editorType !== 'StaticTextItem') {
          this.focusInFilterEditor(item);
          return true;
        }
      }
    }
    return false;
  },

  createRecordComponent: function (record, colNum) {
    var field = this.getField(colNum),
        rowNum = this.getRecordIndex(record);
    if (field.isLink && record[field.name]) {
      var linkButton = isc.OBGridLinkLayout.create({
        grid: this,
        align: this.getCellAlign(record, rowNum, colNum),
        title: this.formatLinkValue(record, field, colNum, rowNum, record[field.name]),
        record: record,
        rowNum: rowNum,
        colNum: colNum
      });
      return linkButton;
    }
    return null;
  },

  updateRecordComponent: function (record, colNum, component, recordChanged) {
    var field = this.getField(colNum),
        rowNum = this.getRecordIndex(record);
    if (field.isLink && record[field.name]) {
      component.setTitle(this.formatLinkValue(record, field, colNum, rowNum, record[field.name]));
      component.record = record;
      component.rowNum = rowNum;
      component.colNum = colNum;
      component.align = this.getCellAlign(record, rowNum, colNum);
      return component;
    }
    return null;
  },

  formatLinkValue: function (record, field, colNum, rowNum, value) {
    if (typeof value === 'undefined' || value === null) {
      return '';
    }
    var simpleType = isc.SimpleType.getType(field.type, this.dataSource);
    // note: originalFormatCellValue is set in the initWidget below
    if (field && field.originalFormatCellValue) {
      return field.originalFormatCellValue(value, record, rowNum, colNum, this);
    } else if (simpleType.shortDisplayFormatter) {
      return simpleType.shortDisplayFormatter(value, field, this, record, rowNum, colNum);
    }
    return value;
  },

  filterEditorProperties: {

    // http://forums.smartclient.com/showthread.php?p=73107
    // https://issues.openbravo.com/view.php?id=18557
    showAllColumns: true,

    setEditValue: function (rowNum, colNum, newValue, suppressDisplay, suppressChange) {
      // prevent any setting of non fields in the filter editor
      // this prevents a specific issue that smartclient will set a value
      // in the {field.name}._identifier (for example warehouse._identifier)
      // because it thinks that the field does not have its own datasource
      if (isc.isA.String(colNum) && !this.getField(colNum)) {
        return;
      }
      return this.Super('setEditValue', arguments);
    },

    getValuesAsCriteria: function (advanced, textMatchStyle, returnNulls) {
      return this.Super('getValuesAsCriteria', [true, textMatchStyle, returnNulls]);
    },

    // is needed to display information in the checkbox field 
    // header in the filter editor row
    isCheckboxField: function () {
      return false;
    },

    filterImg: {
      src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/funnel-icon.png'
    },

    makeActionButton: function () {
      var ret = this.Super('makeActionButton', arguments);
      this.filterImage.setLeft(this.computeFunnelLeft(2));
      var layout = isc.HLayout.create({
        styleName: 'OBGridFilterFunnelBackground',
        width: '100%',
        height: '100%',
        left: this.computeFunnelLeft()
      });
      this.funnelLayout = layout;
      this.addChild(layout);
      return ret;
    },

    computeFunnelLeft: function (correction) {
      correction = correction || 0;
      return this.getInnerWidth() - this.getScrollbarSize() - 3 + correction;
    },

    // keep the funnel stuff placed correctly
    layoutChildren: function () {
      var ret = this.Super("layoutChildren", arguments);
      if (this.funnelLayout) {
        this.funnelLayout.setLeft(this.computeFunnelLeft());
      }
      if (this.filterImage) {
        this.filterImage.setLeft(this.computeFunnelLeft(2));
      }
      return ret;
    },

    // overridden for:
    // https://issues.openbravo.com/view.php?id=18509
    editorChanged: function (item) {
      var prop, same, opDefs, val = item.getElementValue(),
          actOnKeypress = item.actOnKeypress === true ? item.actOnKeypress : this.actOnKeypress;

      if (this.sourceWidget.allowFilterExpressions && val && actOnKeypress) {

        // if someone starts typing and and or then do not filter
        // onkeypress either
        if (val.contains(' and') || val.contains(' or ')) {
          this.preventPerformFilterFiring();
          return;
        }

        if (val.startsWith('=')) {
          this.preventPerformFilterFiring();
          return;
        }

        // now check if the item element value is only
        // an operator, if so, go away
        opDefs = isc.DataSource.getSearchOperators();
        for (prop in opDefs) {
          if (opDefs.hasOwnProperty(prop)) {

            // let null and not null fall through
            // as they should be filtered
            if (prop === 'isNull' || prop === 'notNull') {
              continue;
            }

            same = opDefs[prop].symbol && val.startsWith(opDefs[prop].symbol);
            if (same) {
              this.preventPerformFilterFiring();
              return;
            }
          }
        }
      }
      return this.Super('editorChanged', arguments);
    },

    // function called to clear any pending performFilter calls
    // earlier type actions can already have pending filter actions
    // this deletes them
    preventPerformFilterFiring: function () {
      this.fireOnPause("performFilter", {}, this.fetchDelay);
    },

    // repair that filter criteria on fk fields can be 
    // on the identifier instead of the field itself.
    // after applying the filter the grid will set the criteria
    // back in the filtereditor effectively clearing
    // the filter field. The code here repairs/prevents this.
    setValuesAsCriteria: function (criteria, refresh) {
      // create an edit form right away
      if (!this.getEditForm()) {
        this.makeEditForm();
      }
      var prop, fullPropName;
      // make a copy so that we don't change the object
      // which is maybe used somewhere else
      criteria = isc.clone(criteria);
      var internCriteria = criteria.criteria;
      if (internCriteria && this.getEditForm()) {
        // now remove anything which is not a field
        // otherwise smartclient will keep track of them and send them again
        var fields = this.getEditForm().getFields(),
            length = fields.length,
            i;
        for (i = internCriteria.length - 1; i >= 0; i--) {
          prop = internCriteria[i].fieldName;
          // happens when the internCriteria[i], is again an advanced criteria
          if (!prop) {
            continue;
          }
          fullPropName = prop;
          if (prop.endsWith('.' + OB.Constants.IDENTIFIER)) {
            var index = prop.lastIndexOf('.');
            prop = prop.substring(0, index);
          }
          var fnd = false,
              j;
          for (j = 0; j < length; j++) {
            if (fields[j].displayField === fullPropName) {
              fnd = true;
              break;
            }
            if (fields[j].name === prop) {
              internCriteria[i].fieldName = prop;
              fnd = true;
              break;
            }
            if (fields[j].name === fullPropName) {
              fnd = true;
              break;
            }
          }
          if (!fnd) {
            internCriteria.removeAt(i);
          }
        }
      }

      return this.Super('setValuesAsCriteria', [criteria, refresh]);
    },

    // the filtereditor will assign the grids datasource to a field
    // if it has a display field and no datasource
    // prevent this as we get the datasource later it is not 
    // yet set
    getEditorProperties: function (field) {
      var noDataSource = field.displayField && !field.optionDataSource,
          ret = this.Super('getEditorProperties', arguments);
      if (ret.optionDataSource && noDataSource) {
        delete ret.optionDataSource;
      }
      return ret;
    },

    actionButtonProperties: {
      baseStyle: 'OBGridFilterFunnelIcon',
      visibility: 'hidden',
      showFocused: false,
      showDisabled: false,
      prompt: OB.I18N.getLabel('OBUIAPP_GridFilterIconToolTip'),
      initWidget: function () {
        this.recordEditor.sourceWidget.filterImage = this;
        this.recordEditor.filterImage = this;
        if (this.recordEditor.sourceWidget.filterClause) {
          this.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterImplicitToolTip');
          this.visibility = 'inherit';
        }
        this.Super('initWidget', arguments);
      },
      click: function () {
        this.recordEditor.sourceWidget.clearFilter();
      }
    }
  },

  initWidget: function () {
    // prevent the value to be displayed in case of a link
    var i, length, field, formatCellValueFunction;

    formatCellValueFunction = function (value, record, rowNum, colNum, grid) {
      return '';
    };

    if (this.fields) {
      length = this.fields.length;
      for (i = 0; i < length; i++) {
        field = this.fields[i];

        if (!field.filterEditorProperties) {
          field.filterEditorProperties = {};
        }

        field.filterEditorProperties.keyDown = this.filterFieldsKeyDown;

        if (field.isLink) {
          // store the originalFormatCellValue if not already set
          if (field.formatCellValue && !field.formatCellValueFunctionReplaced) {
            field.originalFormatCellValue = field.formatCellValue;
          }
          field.formatCellValueFunctionReplaced = true;
          field.formatCellValue = formatCellValueFunction;
        }
      }
    }

    this.Super('initWidget', arguments);
  },

  clearFilter: function (keepFilterClause, noPerformAction) {
    var i = 0,
        fld, length;
    if (!keepFilterClause) {
      delete this.filterClause;
    }
    this.forceRefresh = true;
    this.filterEditor.getEditForm().clearValues();

    // clear the date values in a different way
    length = this.filterEditor.getEditForm().getFields().length;

    for (i = 0; i < length; i++) {
      fld = this.filterEditor.getEditForm().getFields()[i];
      if (fld.clearDateValues) {
        fld.clearDateValues();
      }
    }
    if (!noPerformAction) {
      this.filterEditor.performAction();
    }
  },

  showSummaryRow: function () {
    var i, fld, fldsLength, newFields = [];
    var ret = this.Super('showSummaryRow', arguments);
    if (this.summaryRow && !this.summaryRowFieldRepaired) {
      // the summaryrow shares the same field instances as the 
      // original grid, this must be repaired as the grid and
      // and the summary row need different behavior.
      // copy the fields and repair specific parts
      // don't support links in the summaryrow
      fldsLength = this.summaryRow.fields.length;
      for (i = 0; i < fldsLength; i++) {
        fld = isc.addProperties({}, this.summaryRow.fields[i]);
        newFields[i] = fld;
        fld.isLink = false;
        if (fld.originalFormatCellValue) {
          fld.formatCellValue = fld.originalFormatCellValue;
          fld.originalFormatCellValue = null;
        } else {
          fld.formatCellValue = null;
        }
      }
      this.summaryRow.isSummaryRow = true;
      this.summaryRowFieldRepaired = true;
      this.summaryRow.setFields(newFields);
    }
    return ret;
  },

  // show or hide the filter button
  filterEditorSubmit: function (criteria) {
    this.checkShowFilterFunnelIcon(criteria);
  },

  checkShowFilterFunnelIcon: function (criteria) {
    if (!this.filterImage) {
      return;
    }
    var gridIsFiltered = this.isGridFiltered(criteria);
    var noParentOrParentSelected = !this.view || !this.view.parentView || (this.view.parentView.viewGrid.getSelectedRecords() && this.view.parentView.viewGrid.getSelectedRecords().length > 0);

    if (this.filterClause && gridIsFiltered) {
      this.filterImage.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterBothToolTip');
      this.filterImage.show(true);
    } else if (this.filterClause) {
      this.filterImage.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterImplicitToolTip');
      this.filterImage.show(true);
    } else if (gridIsFiltered) {
      this.filterImage.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterExplicitToolTip');
      this.filterImage.show(true);
    } else {
      this.filterImage.prompt = OB.I18N.getLabel('OBUIAPP_GridFilterIconToolTip');
      if (this.view && this.view.messageBar && this.view.messageBar.hasFilterMessage) {
        this.view.messageBar.hide();
      }
      this.filterImage.hide();
    }

    if (this.filterClause && !this.view.isShowingForm && (this.view.messageBar && !this.view.messageBar.isVisible())) {
      var showMessageProperty = OB.PropertyStore.get('OBUIAPP_ShowImplicitFilterMsg'),
          showMessage = (showMessageProperty !== 'N' && showMessageProperty !== '"N"' && noParentOrParentSelected);
      if (showMessage) {
        this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_INFO, '<div><div style="float: left;">' + this.filterName + '<br/>' + OB.I18N.getLabel('OBUIAPP_ClearFilters') + '</div><div style="float: right; padding-top: 15px;"><a href="#" style="font-weight:normal; color:inherit;" onclick="' + 'window[\'' + this.view.messageBar.ID + '\'].hide(); OB.PropertyStore.set(\'OBUIAPP_ShowImplicitFilterMsg\', \'N\');">' + OB.I18N.getLabel('OBUIAPP_NeverShowMessageAgain') + '</a></div></div>', ' ');
        this.view.messageBar.hasFilterMessage = true;
      }
    }
  },

  isGridFiltered: function (criteria) {
    if (!this.filterEditor) {
      return false;
    }
    if (this.filterClause) {
      return true;
    }
    if (!criteria) {
      return false;
    }
    return this.isGridFilteredWithCriteria(criteria.criteria);
  },

  isGridFilteredWithCriteria: function (criteria) {
    var i, length;
    if (!criteria) {
      return false;
    }
    length = criteria.length;
    for (i = 0; i < length; i++) {
      var criterion = criteria[i];
      var prop = criterion && criterion.fieldName;
      var fullPropName = prop;
      if (!prop) {
        if (this.isGridFilteredWithCriteria(criterion.criteria)) {
          return true;
        }
        continue;
      }
      var value = criterion.value;
      // see the description in setValuesAsCriteria above
      if (prop.endsWith('.' + OB.Constants.IDENTIFIER)) {
        var index = prop.lastIndexOf('.');
        prop = prop.substring(0, index);
      }

      var field = this.filterEditor.getField(prop);
      // criterion.operator is set in case of an and/or expression
      if (this.isValidFilterField(field) && (criterion.operator || value === false || value || value === 0)) {
        return true;
      }

      field = this.filterEditor.getField(fullPropName);
      // criterion.operator is set in case of an and/or expression
      if (this.isValidFilterField(field) && (criterion.operator || value === false || value || value === 0)) {
        return true;
      }
    }
    return false;
  },

  isValidFilterField: function (field) {
    if (!field) {
      return false;
    }
    return !field.name.startsWith('_') && field.canFilter;
  },

  // = exportData =
  // The exportData function exports the data of the grid to a file. The user will 
  // be presented with a save-as dialog.
  // Parameters:
  // * {{{exportProperties}}} defines different properties used for controlling the export, currently only the 
  // exportProperties.exportFormat is supported (which is defaulted to csv).
  // * {{{data}}} the parameters to post to the server, in addition the filter criteria of the grid are posted.  
  exportData: function (exportProperties, data) {
    var d = data || {},
        expProp = exportProperties || {},
        dsURL = this.dataSource.dataURL;
    var sortCriteria;
    var lcriteria = this.getCriteria();
    var gdata = this.getData();
    if (gdata && gdata.dataSource) {
      lcriteria = gdata.dataSource.convertRelativeDates(lcriteria);
    }

    isc.addProperties(d, {
      _dataSource: this.dataSource.ID,
      _operationType: 'fetch',
      _noCount: true,
      // never do count for export
      exportAs: expProp.exportAs || 'csv',
      viewState: expProp.viewState,
      tab: expProp.tab,
      exportToFile: true,
      _textMatchStyle: 'substring'
    }, lcriteria, this.getFetchRequestParams());
    if (this.getSortField()) {
      sortCriteria = this.getSort();
      if (sortCriteria && sortCriteria.length > 0) {
        d._sortBy = sortCriteria[0].property;
        if (sortCriteria[0].direction === 'descending') {
          d._sortBy = '-' + d._sortBy;
        }
      }
    }
    OB.Utilities.postThroughHiddenForm(dsURL, d);
  },

  getFetchRequestParams: function (params) {
    return params;
  },

  editorKeyDown: function (item, keyName) {
    if (item) {
      if (typeof item.keyDownAction === 'function') {
        item.keyDownAction();
      }
    }
    return this.Super('editorKeyDown', arguments);
  },

  // Prevents empty message to be shown in frozen part
  // http://forums.smartclient.com/showthread.php?p=57581
  createBodies: function () {
    var ret = this.Super('createBodies', arguments);
    if (this.frozenBody) {
      this.frozenBody.showEmptyMessage = false;
    }
    return ret;
  },

  //= getErrorRows =
  // Returns all the rows that have errors.
  getErrorRows: function () {
    var editRows, errorRows = [],
        i, length;

    if (this.hasErrors()) {
      editRows = this.getAllEditRows(true);
      length = editRows.length;
      for (i = 0; i < length; i++) {
        if (this.rowHasErrors(editRows[i])) {
          errorRows.push(editRows[i]);
        }
      }
    }
    return errorRows;
  }
});

isc.ClassFactory.defineClass('OBGridSummary', isc.OBGrid);

isc.OBGridSummary.addProperties({
  getCellStyle: function (record, rowNum, colNum) {
    var field = this.getField(colNum);
    if (field.summaryFunction === 'sum' && this.summaryRowStyle_sum) {
      return this.summaryRowStyle_sum;
    } else if (field.summaryFunction === 'avg' && this.summaryRowStyle_avg) {
      return this.summaryRowStyle_avg;
    } else {
      return this.summaryRowStyle;
    }
  }
});

isc.ClassFactory.defineClass('OBGridHeaderImgButton', isc.ImgButton);

isc.ClassFactory.defineClass('OBGridLinkLayout', isc.HLayout);
isc.OBGridLinkLayout.addProperties({
  overflow: 'clip-h',
  btn: null,
  height: 1,
  width: '100%',

  initWidget: function () {
    this.btn = isc.OBGridLinkButton.create({});
    this.btn.setTitle(this.title);
    this.btn.owner = this;
    this.addMember(this.btn);
    this.Super('initWidget', arguments);
  },

  setTitle: function (title) {
    this.btn.setTitle(title);
  },

  doAction: function () {
    if (this.grid && this.grid.doCellClick) {
      this.grid.doCellClick(this.record, this.rowNum, this.colNum);
    } else if (this.grid && this.grid.cellClick) {
      this.grid.cellClick(this.record, this.rowNum, this.colNum);
    }
  }

});

isc.ClassFactory.defineClass('OBGridLinkButton', isc.Button);

isc.OBGridLinkButton.addProperties({
  action: function () {
    this.owner.doAction();
  }
});

isc.ClassFactory.defineClass('OBGridFormButton', isc.OBFormButton);
isc.OBGridFormButton.addProperties({});