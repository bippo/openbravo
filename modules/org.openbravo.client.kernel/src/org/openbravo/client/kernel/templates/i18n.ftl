<#--
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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
-->
// jslint

OB.I18N.labels = {
<#list data.labels as label>
'${label.key?js_string}':  '${label.value?js_string}'<#if label_has_next>,</#if>
</#list>
};

<#--
// key is the message key
// params is used for parameter substitution
// if object and property are set then the label is set directly in
// the object
// Note: property may also be a function expecting the label as a string
// if the label is not defined and object and property are set
// then a call to the server is done to request the label.
-->
OB.I18N.getLabel = function(key, params, object, property) {
    if (!OB.I18N.labels[key]) {
      if (object && property) {
        OB.I18N.getLabelFromServer(key, params, object, property);
      }
      return 'UNDEFINED ' + key;      
    }
    var label = OB.I18N.labels[key], i;
    if (params && params.length && params.length > 0) {
        for (i = 0; i < params.length; i++) {
            label = label.replace("%" + i, params[i]);
        }
    }
    if (object && property) {
      if (isc.isA.Function(object[property])) {
        object[property](label);
      } else {
        object[property] = label;
      }
    }
    return label;
};

OB.I18N.getLabelFromServer = function(key, params, object, property) {
      var requestParameters = {};
      requestParameters._action = 'org.openbravo.client.kernel.GetLabelActionHandler';
      requestParameters.key = key;

      var rpcRequest = {};
      rpcRequest.actionURL = OB.Application.contextUrl + 'org.openbravo.client.kernel';
      rpcRequest.callback = function (response, data, request) {
        var clientContext = response.clientContext;
        if (data.label) {
          OB.I18N.labels[clientContext.key] = data.label;
          OB.I18N.getLabel(clientContext.key, clientContext.params, clientContext.object, clientContext.property);
        } else {
          if (isc.isA.Function(clientContext.object[clientContext.property])) {
            clientContext.object[clientContext.property]('LABEL NOT FOUND ' + clientContext.key);
          } else {
            clientContext.object[clientContext.property] = 'LABEL NOT FOUND ' + clientContext.key;
          }          
        }
      };
      rpcRequest.httpMethod = 'GET';
      rpcRequest.contentType = 'application/json;charset=UTF-8';
      rpcRequest.useSimpleHttp = true;
      rpcRequest.evalResult = true;
      rpcRequest.params = requestParameters;
      rpcRequest.clientContext = {'key' : key, 'object': object, 'params': params, 'property': property};
      isc.RPCManager.sendRequest(rpcRequest);
};