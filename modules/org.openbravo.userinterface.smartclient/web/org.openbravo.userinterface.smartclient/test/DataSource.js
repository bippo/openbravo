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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.DataSource.addClassMethods({

  // locating dataSources
  dataSourceBaseURL : "../../datasources/",

  get : function (name, callback, context, schemaType) {
    var ds = this.getDataSource(name, callback, context, schemaType);
    if (!ds) {
      this.logWarn("DS IS NOT SET");
      this.loadSchema(name, callback, context);
    }
    return ds;
  },

  // loadSchema - attempt to load a remote dataSource schema from the server.
  // This is supported as part of the SmartClient server functionality
  loadSchema : function (name, callback, context) {
    this.logWarn("CALLING SERVER '" + name + "' from " + this.dataSourceBaseURL + name);

    isc.RPCManager.sendRequest({
      evalResult : true,
      useSimpleHttp : true,
      httpMethod : "GET",
      actionURL : this.dataSourceBaseURL + name,
      callback : this._loadSchemaComplete,
      clientContext : {
        dataSource : name,
        callback : callback,
        context : context
      }
    });

    return null;
  },

  _loadSchemaComplete : function (rpcResponse, data, rpcRequest) {
    this.logWarn("LOAD SCHEMA COMPLETE");
    var clientContext = rpcResponse.clientContext;
    var name = clientContext.dataSource;
    var callback = clientContext.callback;
    var context = clientContext.context;
    // Now that the dataSource is loaded, we can leverage the
    // DataSource.getDataSource()
      // method to make the callback.
    if (callback) {
      context.fireCallback(callback, "ds", [ data ], context);
    }
  }
});