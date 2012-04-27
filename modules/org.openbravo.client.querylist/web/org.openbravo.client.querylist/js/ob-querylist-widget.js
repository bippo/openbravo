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
// = OBQueryListWidget =
//
// Implements the Query / List widget superclass.
//
isc.defineClass('OBQueryListWidget', isc.OBWidget).addProperties({

  widgetId: null,
  widgetInstanceId: null,
  fields: null,
  maximizedFields: null,
  gridDataSource: null,
  grid: null,
  gridProperties: {},
  viewMode: 'widget',
  totalRows: null,
  widgetTitle: null,

  showAllLabel: null,
  OBQueryListShowAllLabelHeight: null,

  initWidget: function () {
    this.showAllLabel = isc.HLayout.create({
      height: this.OBQueryListShowAllLabelHeight,
      members: [
      isc.OBQueryListRowsNumberLabel.create({
        contents: ''
      }), isc.OBQueryListShowAllLabel.create({
        contents: OB.I18N.getLabel('OBCQL_ShowAll'),
        widget: this,
        action: function () {
          this.widget.maximize();
        }
      })]
    });

    this.gridDataSource = this.createGridDataSource();

    this.Super('initWidget', arguments);
    this.widgetTitle = this.title;
    // refresh if the dbInstanceId is set
    if (this.dbInstanceId) {
      this.refresh();
    }
  },

  setDbInstanceId: function (instanceId) {
    this.Super('setDbInstanceId', instanceId);
    this.grid.fetchData();
  },

  setWidgetHeight: function () {
    // when used inside generated window, just accept externally defined height (via rowspan) and
    // don't override it as will break normal window layout
    if (this.inWidgetInFormMode) {
      return;
    }
    var currentHeight = this.getHeight(),
        edgeTop = this.edgeTop,
        edgeBottom = this.edgeBottom,
        newGridHeight = this.grid.headerHeight + (this.grid.cellHeight * (this.parameters.RowsNumber ? this.parameters.RowsNumber : 10)) + this.grid.summaryRowHeight + 2;
    this.grid.setHeight(newGridHeight);

    var newHeight = edgeTop + newGridHeight + edgeBottom;
    if (this.showAllLabel.isVisible()) {
      newHeight += this.showAllLabel.height;
    }
    this.setHeight(newHeight);
    if (this.parentElement) {
      var heightDiff = newHeight - currentHeight,
          parentHeight = this.parentElement.getHeight();
      this.parentElement.setHeight(parentHeight + heightDiff);
    }
  },

  createWindowContents: function () {
    var layout, url, params = {},
        showFilter = (this.viewMode === 'maximized');

    layout = isc.VStack.create({
      height: '100%',
      width: '100%',
      styleName: ''
    });

    isc.addProperties(this.gridProperties, {
      showFilterEditor: showFilter
    });

    this.grid = isc.OBQueryListGrid.create(isc.addProperties({
      dataSource: this.gridDataSource,
      widget: this,
      fields: this.fields
    }, this.gridProperties));

    layout.addMember(this.grid);
    layout.addMember(this.showAllLabel);

    return layout;
  },

  refresh: function () {
    if (this.viewMode === 'widget') {
      this.setWidgetHeight();
    }
    // sometimes when removing the form, this gets called
    // at that point this.grid is not set anymore
    if (this.grid) {
      this.grid.invalidateCache();
      this.grid.filterData();
    }
  },

  exportGrid: function () {
    var grid = this.widget.grid,
        requestProperties, additionalProperties;

    if (OB.Application.licenseType === 'C') {
      isc.warn(OB.I18N.getLabel('OBUIAPP_ActivateMessage', [OB.I18N.getLabel('OBCQL_ActivateMessageExport')]), {
        isModal: true,
        showModalMask: true,
        toolbarButtons: [isc.Dialog.OK]
      });
      return;
    }

    requestProperties = {
      exportAs: 'csv',
      exportDisplay: 'download',
      params: {
        exportToFile: true
      }
    };

    additionalProperties = {
      widgetInstanceId: this.widget.dbInstanceId
    };

    grid.exportData(requestProperties, additionalProperties);
  },

  maximize: function () {
    OB.Layout.ViewManager.openView('OBQueryListView', {
      tabTitle: this.widgetTitle,
      widgetInstanceId: this.dbInstanceId,
      widgetId: this.widgetId,
      fields: this.maximizedFields,
      gridDataSource: this.gridDataSource,
      parameters: this.parameters,
      menuItems: this.menuItems,
      fieldDefinitions: this.fieldDefinitions
    });
  },

  setTotalRows: function (totalRows) {
    this.totalRows = totalRows;
    if (this.viewMode === 'maximized') {
      this.setTitle(this.widgetTitle + " (" + this.totalRows + ")");
      this.showAllLabel.hide();
      return;
    }
    if (this.showAllLabel.getMembers()[0]) {
      this.showAllLabel.getMembers()[0].setContents(
      OB.I18N.getLabel('OBCQL_RowsNumber', [this.parameters.RowsNumber, this.totalRows]));
    }
    if (this.parameters.showAll || this.totalRows <= this.parameters.RowsNumber) {
      this.showAllLabel.hide();
    } else {
      this.showAllLabel.show();
    }
    this.setWidgetHeight();
  }
});

isc.ClassFactory.defineClass('OBQueryListGrid', isc.OBGrid);

isc.OBQueryListGrid.addProperties({
  width: '100%',
  height: '100%',
  dataSource: null,

  // some common settings
  //showFilterEditor: false,
  //filterOnKeypress: false,
  canEdit: false,
  alternateRecordStyles: true,
  canReorderFields: false,
  canFreezeFields: false,
  canGroupBy: false,
  autoFetchData: false,
  canAutoFitFields: false,
  showGridSummary: true,

  summaryRowProperties: {
    showEmptyMessage: false
  },

  dataProperties: {
    useClientFiltering: false //,
    //useClientSorting: false
  },

  initWidget: function () {
    var i;
    // overridden as query list widgets can't handle date ranges (yet)
    for (i = 0; i < this.getFields().length; i++) {
      var fld = this.getFields()[i];
      if (fld.filterEditorType === 'OBMiniDateRangeItem') {
        fld.filterEditorType = 'OBDateItem';
      }
    }
    this.Super('initWidget', arguments);
  },

  filterData: function (criteria, callback, requestProperties) {
    var newCallBack, crit = criteria || {},
        reqProperties = requestProperties || {};

    reqProperties.params = reqProperties.params || {};
    reqProperties.params = this.getFetchRequestParams(reqProperties.params);

    reqProperties.showPrompt = false;

    reqProperties.clientContext = {
      grid: this,
      criteria: crit
    };

    newCallBack = function (dsResponse, data, dsRequest) {
      dsResponse.clientContext.grid.getWidgetTotalRows(dsResponse, data, dsRequest);
      if (callback) {
        callback();
      }
    };

    return this.Super('filterData', [crit, newCallBack, reqProperties]);
  },

  getFetchRequestParams: function (params) {
    var localWidgetProperties, propName, propValue;

    // process dynamic parameters
    localWidgetProperties = isc.clone(this.widget.parameters);
    delete localWidgetProperties.formValues;
    for (propName in localWidgetProperties) {
      if (localWidgetProperties.hasOwnProperty(propName)) {
        propValue = localWidgetProperties[propName];
        if (typeof propValue === 'string') {
          localWidgetProperties[propName] = this.widget.evaluateContents(propValue);
        }
      }
    }

    params = params || {};
    params.serializedParameters = isc.JSON.encode(localWidgetProperties);
    params.widgetId = this.widget.widgetId;
    params.widgetInstanceId = this.widget.dbInstanceId;
    params.rowsNumber = this.widget.parameters.RowsNumber;
    params.viewMode = this.widget.viewMode;
    params.showAll = this.widget.parameters.showAll;
    return params;
  },

  destroy: function () {
    if (this.dataSource) {
      this.dataSource.destroy();
      this.dataSource = null;
    }
    this.Super('destroy', arguments);
  },

  fetchData: function (criteria, callback, requestProperties) {
    var newCallBack, crit = criteria || {},
        reqProperties = requestProperties || {};

    reqProperties.params = reqProperties.params || {};
    reqProperties.params = this.getFetchRequestParams(reqProperties.params);

    reqProperties.showPrompt = false;

    reqProperties.clientContext = {
      grid: this,
      criteria: crit
    };

    newCallBack = function (dsResponse, data, dsRequest) {
      dsResponse.clientContext.grid.getWidgetTotalRows(dsResponse, data, dsRequest);
      if (callback) {
        callback();
      }
    };

    return this.Super('fetchData', [crit, newCallBack, reqProperties]);
  },

  cellClick: function (record, rowNum, colNum) {
    var field = this.getField(colNum);
    if (field.isLink) {
      if (field.OB_TabId && field.OB_LinkExpression) {
        OB.Utilities.openDirectView(field.OB_WindowId, field.OB_keyColumnName, field.OB_entityName, record[field.OB_LinkExpression]);
      }
    }
  },

  getWidgetTotalRows: function (dsResponse, data, dsRequest) {
    if (this.widget.viewMode === 'widget' && !this.widget.parameters.showAll) {
      var criteria = dsResponse.clientContext.criteria || {},
          requestProperties = {};
      requestProperties.showPrompt = false;
      requestProperties.clientContext = {
        grid: this
      };
      requestProperties.params = requestProperties.params || {};
      requestProperties.params = this.getFetchRequestParams(requestProperties.params);

      requestProperties.params.showAll = true;
      // sometimes we get here before the datasource
      // is set
      if (this.dataSource) {
        this.dataSource.fetchData(criteria, function (dsResponse, data, dsRequest) {
          dsResponse.clientContext.grid.widget.setTotalRows(dsResponse.totalRows);
        }, requestProperties);
      }
    } else {
      this.widget.setTotalRows(dsResponse.totalRows);
    }
  }
});