/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
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

isc.defineClass('OBPickAndExecuteGrid', isc.OBGrid);

isc.OBPickAndExecuteGrid.addProperties({
  dataProperties: {
    useClientFiltering: false,
    useClientSorting: false
  },
  view: null,
  dataSource: null,
  showFilterEditor: true,

  // Editing
  canEdit: true,
  editEvent: isc.EH.CLICK,
  autoSaveEdits: false,

  selectionAppearance: 'checkbox',
  autoFitFieldWidths: true,
  autoFitWidthApproach: 'title',
  canAutoFitFields: false,
  minFieldWidth: 75,
  width: '100%',
  height: '100%',
  autoFitFieldsFillViewport: false,
  confirmDiscardEdits: false,
  animateRemoveRecord: false,
  removeFieldProperties: {
    width: 32
  },

  // default selection
  selectionProperty: 'obSelected',

  initWidget: function () {
    var i, len = this.fields.length;

    this.selectedIds = [];

    // the origSetValuesAsCriteria member is added as 'class' level
    // we only need to do it once
    if (!this.filterEditorProperties.origSetValuesAsCriteria) {

      this.filterEditorProperties.origSetValuesAsCriteria = this.filterEditorProperties.setValuesAsCriteria;

      this.filterEditorProperties.setValuesAsCriteria = function (criteria, advanced) {
        var orig = (criteria && criteria.criteria) || [],
            len = orig.length,
            crit, i;

        if (criteria._OrExpression) {
          for (i = 0; i < len; i++) {
            if (orig[i].fieldName && orig[i].fieldName === 'id') {
              continue;
            }

            if (orig[i].operator && orig[i]._constructor) {
              crit = orig[i];
              break;
            }
          }
        } else {
          crit = criteria;
        }

        this.origSetValuesAsCriteria(crit, advanced);
      };
    }

    // adding a reference to the plain field object to this grid
    // useful when working with custom field validators
    for (i = 0; i < len; i++) {
      this.fields[i].grid = this;
    }

    // required to show the funnel icon and to work
    this.filterClause = this.gridProperties.filterClause;

    this.orderByClause = this.gridProperties.orderByClause;

    this.checkboxFieldDefaults = isc.addProperties(this.checkboxFieldDefaults, {
      canFilter: true,
      filterEditorType: 'StaticTextItem'
    });

    OB.TestRegistry.register('org.openbravo.client.application.process.pickandexecute.Grid', this);

    this.Super('initWidget', arguments);
  },

  selectionChanged: function (record, state) {
    var recordIdx;

    if (this.view.viewProperties.selectionFn) {
      this.view.viewProperties.selectionFn(this, record, state);
    }

    recordIdx = this.getRecordIndex(record);

    if (!state && recordIdx !== -1) {
      this.discardEdits(recordIdx);
    }

    this.Super('selectionChanged', arguments);
  },

  selectionUpdated: function (record, recordList) {
    var i, len = recordList.length;

    this.selectedIds = [];

    for (i = 0; i < len; i++) {
      this.selectedIds.push(recordList[i].id);
    }
    // refresh it all as multiple lines can be selected
    this.markForRedraw('Selection changed');

    this.Super('selectionUpdated', arguments);
  },

  handleFilterEditorSubmit: function (criteria, context) {
    var ids = [],
        crit = {},
        len = this.selectedIds.length,
        i, c, found;

    for (i = 0; i < len; i++) {
      ids.push({
        fieldName: 'id',
        operator: 'equals',
        value: this.selectedIds[i]
      });
    }

    if (len > 0) {

      crit._constructor = 'AdvancedCriteria';
      crit._OrExpression = true; // trick to get a really _or_ in the backend
      crit.operator = 'or';
      crit.criteria = ids;

      c = (criteria && criteria.criteria) || [];
      found = false;

      for (i = 0; i < c.length; i++) {
        if (c[i].fieldName && c[i].value !== '') {
          found = true;
          break;
        }
      }

      if (!found) {

        if (!criteria) {
          criteria = {
            _constructor: 'AdvancedCriteria',
            operator: 'and',
            criteria: []
          };
        }

        // adding an *always true* sentence
        criteria.criteria.push({
          fieldName: 'id',
          operator: 'notNull'
        });
      }
      crit.criteria.push(criteria); // original filter
    } else {
      crit = criteria;
    }

    this.Super('handleFilterEditorSubmit', [crit, context]);
  },

  dataArrived: function (startRow, endRow) {
    var record, i, allRows, len = this.selectedIds.length;
    for (i = 0; i < len; i++) {
      record = this.data.findByKey(this.selectedIds[i]);
      if (record) {
        record[this.selectionProperty] = true;
      }
    }

    if (len === 0) {
      // push all *selected* rows into selectedIds cache
      allRows = this.data.allRows || [];
      len = allRows.length;
      for (i = 0; i < len; i++) {
        if (allRows[i][this.selectionProperty]) {
          this.selectedIds.push(allRows[i][OB.Constants.ID]);
        }
      }
    }

    this.Super('dataArrived', arguments);
  },

  recordClick: function (grid, record, recordNum, field, fieldNum, value, rawValue) {
    if (fieldNum === 0 && value.indexOf('unchecked.png') !== -1) {
      grid.endEditing();
      return false;
    }
    return this.Super('recordClick', arguments);
  },

  getOrgParameter: function () {
    var view = this.view.parentWindow.activeView,
        context, i;

    context = view.getContextInfo(true, false);

    for (i in context) {
      if (context.hasOwnProperty(i) && i.indexOf('organization') !== -1) {
        return context[i];
      }
    }

    return null;
  },

  onFetchData: function (criteria, requestProperties) {
    requestProperties = requestProperties || {};
    requestProperties.params = this.getFetchRequestParams(requestProperties.params);
  },

  clearFilter: function () {
    this.filterClause = null;
    this.Super('clearFilter', arguments);
  },

  getFetchRequestParams: function (params) {
    var props = this.gridProperties || {},
        view = this.view.parentWindow.activeView;

    params = params || {};

    isc.addProperties(params, view.getContextInfo(true, false));

    params[OB.Constants.ORG_PARAMETER] = this.getOrgParameter();

    if (this.orderByClause) {
      params[OB.Constants.ORDERBY_PARAMETER] = this.orderByClause;
    }

    if (this.filterClause) {
      if (props.whereClause) {
        params[OB.Constants.WHERE_PARAMETER] = ' ((' + props.whereClause + ') and (' + this.filterClause + ")) ";
      } else {
        params[OB.Constants.WHERE_PARAMETER] = this.filterClause;
      }
    } else if (props.whereClause) {
      params[OB.Constants.WHERE_PARAMETER] = props.whereClause;
    } else {
      params[OB.Constants.WHERE_PARAMETER] = null;
    }

    return params;
  },

  getFieldByColumnName: function (columnName) {
    var i, len = this.fields.length,
        colName;

    if (!this.fieldsByColumnName) {
      this.fieldsByColumnName = [];
      for (i = 0; i < len; i++) {
        colName = this.fields[i].columnName;
        if (colName) {
          this.fieldsByColumnName[colName] = this.fields[i];
        }
      }
    }

    return this.fieldsByColumnName[columnName];
  },

  setValueMap: function (field, entries) {
    var len = entries.length,
        map = {},
        i, undef;

    for (i = 0; i < len; i++) {
      if (entries[i][OB.Constants.ID] !== undef) {
        map[entries[i][OB.Constants.ID]] = entries[i][OB.Constants.IDENTIFIER];
      }
    }

    this.Super('setValueMap', [field, map]);
  },

  processColumnValue: function (rowNum, columnName, columnValue, editValues) {
    var field;
    if (!columnValue) {
      return;
    }

    if (columnValue.entries) {
      field = this.getFieldByColumnName(columnName);
      if (!field) {
        return;
      }
      this.setValueMap(field.name, columnValue.entries);
    }
  },

  processFICReturn: function (response, data, request) {
    var context = response && response.clientContext,
        rowNum = context && context.rowNum,
        grid = context && context.grid,
        columnValues, prop, value, editValues, undef;


    if (rowNum === undef || !data || !data.columnValues) {
      return;
    }

    columnValues = data.columnValues;
    editValues = grid.getEditValues(rowNum);

    for (prop in columnValues) {
      if (columnValues.hasOwnProperty(prop)) {
        grid.processColumnValue(rowNum, prop, columnValues[prop], editValues);
      }
    }
  },

  getContextInfo: function (rowNum) {
    var contextInfo = isc.addProperties({}, this.view.parentWindow.activeView.getContextInfo(false, true, false, true)),
        record = isc.addProperties({}, this.getRecord(rowNum), this.getEditValues(rowNum)),
        fields = this.view.viewProperties.fields,
        len = fields.length,
        fld, i, value, undef, type;

    for (i = 0; i < len; i++) {
      fld = fields[i];
      value = record[fld.name];
      if (value !== undef) {
        if (fld.type) {
          type = isc.SimpleType.getType(fld.type);
          if (type.createClassicString) {
            contextInfo[fld.inpColumnName] = type.createClassicString(value);
          } else {
            contextInfo[fld.inpColumnName] = this.view.parentWindow.activeView.convertContextValue(value, fld.type);
          }
        } else {
          contextInfo[fld.inpColumnName] = this.view.parentWindow.activeView.convertContextValue(value, fld.type);
        }
      }
    }

    return contextInfo;
  },

  retrieveInitialValues: function (rowNum, colNum, newCell, newRow, suppressFocus) {
    var requestParams, allProperties, i, record;

    allProperties = this.getContextInfo(rowNum);
    record = this.getRecord(rowNum);

    requestParams = {
      MODE: (newRow ? 'NEW' : 'EDIT'),
      PARENT_ID: null,
      TAB_ID: this.view.viewProperties.tabId,
      ROW_ID: (record ? record[OB.Constants.ID] : null)
    };

    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, this.processFICReturn, {
      grid: this,
      rowNum: rowNum,
      colNum: colNum,
      newCell: newCell,
      newRow: newRow,
      suppressFocus: suppressFocus
    });
  },

  showInlineEditor: function (rowNum, colNum, newCell, newRow, suppressFocus) {
    this.retrieveInitialValues(rowNum, colNum, newCell, newRow, suppressFocus);
    this.Super('showInlineEditor', arguments);
  },

  hideInlineEditor: function (focusInBody, suppressCMHide) {
    var ret = this.Super('hideInlineEditor', arguments);
    this.validateRows();
    return ret;
  },

  validateRows: function () {
    var i, row, field, errors;

    if (!this.neverValidate) {
      return;
    }

    for (i = 0; i < this.fields.length; i++) {
      field = this.fields[i];

      if (!field.validationFn) {
        continue;
      }

      for (row = 0; row < this.data.length; row++) {
        errors = this.validateCellValue(row, i, this.data[row][field.name]);
        if (!errors || isc.isA.emptyArray(errors)) {
          this.clearFieldError(row, field.name);
        } else {
          this.setFieldError(row, field.name, errors[0]);
        }
      }
    }
    this.recalculateSummaries();
  },

  removeRecord: function (rowNum, record) {
    var remove = true,
        removeFn = this.view.viewProperties && this.view.viewProperties.removeFn;

    if (removeFn && isc.isA.Function(removeFn)) {
      remove = removeFn(this, rowNum, record);
    }

    if (!remove) {
      this.validateRows();
      return;
    }

    this.Super('removeRecord', arguments);

    this.validateRows();
  }
});