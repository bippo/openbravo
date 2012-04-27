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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = OBViewDataSource =
//
// The datasource which is used within a view. It adds specific behavior
// by adding extra request parameters.
//
isc.ClassFactory.defineClass('OBViewDataSource', isc.OBRestDataSource);

isc.OBViewDataSource.addProperties({
  additionalProps: null,
  showProgressAfterDelay: false,

  showProgress: function (editedRecord) {
    var btn, btn2;

    if (!this.showProgressAfterDelay) {
      // don't show it, done too quickly
      return;
    }

    if (editedRecord) {
      if (editedRecord && editedRecord.editColumnLayout) {
        if (!this.view.isShowingForm) {
          editedRecord.editColumnLayout.toggleProgressIcon(true);
        }
      }
    }

    // Always show progress in save button and disable save and close one
    btn = this.view.toolBar.getLeftMember(isc.OBToolbar.TYPE_SAVE);
    btn.setDisabled(true);
    btn.customState = 'Progress';
    btn.resetBaseStyle();
    btn.markForRedraw();

    btn2 = this.view.toolBar.getLeftMember(isc.OBToolbar.TYPE_SAVECLOSE);
    if (btn2) {
      btn2.setDisabled(true);
      btn2.markForRedraw();
    }
  },

  hideProgress: function (editedRecord) {
    var btn;
    this.showProgressAfterDelay = false;
    if (editedRecord && editedRecord.editColumnLayout) {
      editedRecord.editColumnLayout.toggleProgressIcon(false);
    }

    // always remove the progress style here anyway
    btn = this.view.toolBar.getLeftMember(isc.OBToolbar.TYPE_SAVE);
    btn.customState = '';
    btn.resetBaseStyle();
    btn.markForRedraw();
  },

  performDSOperation: function (operationType, data, callback, requestProperties) {
    var currentRecord;

    requestProperties = requestProperties || {};
    requestProperties.clientContext = requestProperties.clientContext || {};

    // only update the values of the record itself but not of any referenced 
    // entity
    if (operationType === 'update' || operationType === 'add') {
      var correctedData = {},
          prop;
      for (prop in data) {
        if (data.hasOwnProperty(prop) && !prop.contains('.')) {
          correctedData[prop] = data[prop];
        }
      }
      data = correctedData;
      this.showProgressAfterDelay = true;
      currentRecord = this.view.viewGrid.getSelectedRecord();
      if (currentRecord) {
        // only show progress after 200ms delay
        // set the current selected record before the delay
        // keep the edited record in the client context
        requestProperties.clientContext.progressIndicatorSelectedRecord = currentRecord;
      }

      this.delayCall('showProgress', [requestProperties.clientContext.progressIndicatorSelectedRecord], 200);
    }

    // doing row editing
    if (this.view.viewGrid.getEditRow() || this.view.viewGrid.getEditRow() === 0) {
      if (!requestProperties.clientContext) {
        requestProperties.clientContext = {};
      }
      requestProperties.clientContext.editRow = this.view.viewGrid.getEditRow();
    }

    var newRequestProperties = this.getTabInfoRequestProperties(this.view, requestProperties);
    // standard update is not sent with operationType
    var additionalPara = {
      _operationType: 'update',
      _noActiveFilter: true,
      sendOriginalIDBack: true,
      _extraProperties: this.getAdditionalProps()
    };
    isc.addProperties(newRequestProperties.params, additionalPara);
    if (!newRequestProperties.dataSource) {
      newRequestProperties.dataSource = this;
    }
    this.Super('performDSOperation', [operationType, data, callback, newRequestProperties]);
  },

  getAdditionalProps: function () {
    var prop, length, i, fld;
    if (this.additionalProps !== null) {
      return this.additionalProps;
    }
    this.additionalProps = "";
    length = this.getFields().length;
    for (i = 0; i < length; i++) {
      fld = this.getFields()[i];
      if (fld.additional) {
        if (this.additionalProps.length > 0) {
          this.additionalProps += ",";
        }
        this.additionalProps += fld.name;
      }
    }
    return this.additionalProps;
  },

  // do special id-handling so that we can replace the old if with the new
  // id
  // in the correct way, see the ob-view-grid.js editComplete method
  validateJSONRecord: function (record) {
    record = this.Super('validateJSONRecord', arguments);
    if (record && record._originalId) {
      var newId = record.id;
      record.id = record._originalId;
      record._newId = newId;
    }
    return record;
  },

  transformResponse: function (dsResponse, dsRequest, jsonData) {

    if (dsRequest.clientContext) {
      this.hideProgress(dsRequest.clientContext.progressIndicatorSelectedRecord);
    }
    if (jsonData) {
      var errorStatus = !jsonData.response || jsonData.response.status === 'undefined' || jsonData.response.status !== isc.RPCResponse.STATUS_SUCCESS;
      if (errorStatus) {
        var handled = this.view.setErrorMessageFromResponse(dsResponse, jsonData, dsRequest);

        if (!handled && !dsRequest.willHandleError && jsonData.response && jsonData.response.error) {
          OB.KernelUtilities.handleSystemException(jsonData.response.error.message);
        }
      } else {
        // there are some cases where the jsonData is not passed, in case of
        // errors
        // make it available through the response object
        dsResponse.dataObject = jsonData;
      }
    }
    return this.Super('transformResponse', arguments);
  },

  // ** {{{ getTabInfoRequestProperties }}} **
  //
  // Adds tab and module information to the requestProperties.
  //
  // Parameters:
  // * {{{theView}}}: view to obtain tab and module info from.
  // * {{{requestProperties}}}: original requestProperties.
  // Return:
  // * Original requestProperties including the new module and tab
  // properties.
  getTabInfoRequestProperties: function (theView, requestProperties) {
    if (theView && theView.tabId) {
      requestProperties.params = requestProperties.params || {};
      isc.addProperties(requestProperties.params, {
        windowId: theView.standardWindow.windowId,
        tabId: theView.tabId,
        moduleId: theView.moduleId
      });
    }
    return requestProperties;
  }
});