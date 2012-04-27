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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// = Openbravo Datasource Utilities =
// Defines a number of utility methods related to datasources.
OB.Datasource = {};

// ** {{{ OB.Datasource.get(dataSourceId, target, dsFieldName) }}} **
//
// Retrieves a datasource from the server. The return from the server is a
// javascript
// string which is evaluated. This string should create a datasource. The
// datasource
// object is set in a field of the target (if the target parameter is set). This
// is
// done asynchronously.
//
// The method returns the datasourceid.
//
// Parameters:
// * {{{dataSourceId}}}: the id or name of the datasource
// * {{{target}}}: the target object which needs the datasource
// * {{{dsFieldName}}}: the field name to set in the target object.
// * {{{doNew}}}: if set to true then a new datasource is created
// If not set then setDataSource or optionDataSource are used.
//
OB.Datasource.get = function (dataSourceId, target, dsFieldName, doNew) {
  var ds, callback, rpcRequest;

  if (!doNew) {
    ds = isc.DataSource.getDataSource(dataSourceId);
    if (ds) {
      // only set if target is defined
      if (target) {
        if (dsFieldName) {
          target[dsFieldName] = ds;
        } else if (target.setDataSource) {
          target.setDataSource(ds);
        } else {
          target.optionDataSource = ds;
        }
      }
      return ds;
    }
  }

  // create the callback
  callback = function (rpcResponse, data, rpcRequest) {
    // prevent registering it again
    var ds = isc.DataSource.getDataSource(data.ID);
    if (ds) {
      data = ds;
    } else if (!isc.DataSource.get(data.ID)) {
      isc.DataSource.registerDataSource(data);
    }

    // only set if target is defined
    if (target) {
      if (dsFieldName) {
        target[dsFieldName] = data;
      } else if (target.setDataSource) {
        target.setDataSource(data);
      } else {
        target.optionDataSource = data;
      }
    }
  };

  rpcRequest = {};
  rpcRequest.params = {
    '_create': true
  };
  if (doNew) {
    rpcRequest.params._new = true;
  }
  rpcRequest.httpMethod = 'GET';
  rpcRequest.actionURL = OB.Application.contextUrl + 'org.openbravo.client.kernel/OBSERDS_Datasource/' + dataSourceId;
  rpcRequest.callback = callback;
  rpcRequest.useSimpleHttp = true;
  rpcRequest.evalResult = true;
  isc.RPCManager.sendRequest(rpcRequest);

  // return null
  return dataSourceId;
};

// ** {{{ OB.Datasource.create}}} **
// Performs a last check if the datasource was already registered before
// actually creating it, prevents re-creating datasources when multiple
// async requests are done for the same datasource.
// Parameters:
// * {{{dsProperties}}}: the properties of the datasource which needs to be
// created.
OB.Datasource.create = function (dsProperties) {
  var i, length, flds;

  // set some default properties
  if (!dsProperties.operationBindings) {
    dsProperties.operationBindings = [{
      operationType: 'fetch',
      dataProtocol: 'postParams',
      requestProperties: {
        httpMethod: 'POST'
      }
    }, {
      operationType: 'add',
      dataProtocol: 'postMessage'
    }, {
      operationType: 'remove',
      dataProtocol: 'postParams',
      requestProperties: {
        httpMethod: 'DELETE'
      }
    }, {
      operationType: 'update',
      dataProtocol: 'postMessage',
      requestProperties: {
        httpMethod: 'PUT'
      }
    }];
  }
  dsProperties.recordXPath = dsProperties.recordXPath || '/response/data';
  dsProperties.dataFormat = dsProperties.dataFormat || 'json';
  dsProperties.titleField = dsProperties.titleField || OB.Constants.IDENTIFIER;

  if (dsProperties.fields) {
    flds = dsProperties.fields;
    length = flds.length;
    for (i = 0; i < length; i++) {
      if (!flds[i].type) {
        flds[i].type = 'text';
      }
    }
  }

  // if must be a new datasource then change the id 
  // https://issues.openbravo.com/view.php?id=16581
  if (dsProperties._new && dsProperties.ID) {
    dsProperties.ID = dsProperties.ID + '_' + new Date().getTime();
  }
  if (dsProperties.ID) {
    var ds = isc.DataSource.getDataSource(dsProperties.ID);
    if (ds) {
      return ds;
    }
  }
  if (dsProperties.createClassName) {
    return isc[dsProperties.createClassName].create(dsProperties);
  }
  return isc.OBRestDataSource.create(dsProperties);
};

// always use a subclass to make it easier to override some default stuff
isc.ClassFactory.defineClass('OBRestDataSource', isc.RestDataSource);

isc.OBRestDataSource.addClassProperties({
  // is used to force a unique criterion with a unique value
  DUMMY_CRITERION_NAME: '_dummy',

  getDummyCriterion: function () {
    return {
      fieldName: isc.OBRestDataSource.DUMMY_CRITERION_NAME,
      operator: 'equals',
      value: new Date().getTime()
    };
  }
});

isc.OBRestDataSource.addProperties({
  sendDSRequest: function (dsRequest) {
    //TODO: Report an issue to SmartClient - This part is a work around
    if (dsRequest.params && this.requestProperties && this.requestProperties.params) {
      isc.addProperties(dsRequest.params, this.requestProperties.params);
    }
    this.Super('sendDSRequest', arguments);
  },

  // always let the dummy criterion be true
  evaluateCriterion: function (record, criterion) {
    if (criterion && criterion.fieldName && criterion.fieldName === isc.OBRestDataSource.DUMMY_CRITERION_NAME) {
      return true;
    }
    return this.Super('evaluateCriterion', arguments);
  }
});