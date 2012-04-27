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

// = Property Store =
//
// The Property Store maintains properties. A property can be anything from the width of a column to 
// the last menu selections of a user. If a component sets a property then the property is also set 
// on the server. All properties defined on the server are loaded when the application starts. So
// the property store has always a complete set of properties (read at page start and updated).
// If a component requests a certain property then the local cache (OB.Properties) is checked. 
// If no value can be found there then an undefined value is returned. 
//
(function (OB, isc) {

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }

  // cache object references locally
  var ISC = isc,
      pstore; // Local reference to RemoveCallManager instance

  function PropertyStore() {}

  PropertyStore.prototype = {

    // array of functions which are called when a property change
    // occurs
    listeners: [],

    // ** {{{ PropertyStore.get(propertyName) }}} **
    //
    // Retrieves the property from the local cache. If not found then null
    // is returned.
    //
    // Parameters:
    // * {{{propertyName}}}: the name of the property
    // * {{{windowId}}}: the system will first search for property on windowId level
    //
    get: function (propertyName, windowId) {
      if (windowId && OB.Properties[propertyName + '_' + windowId]) {
        return OB.Properties[propertyName + '_' + windowId];
      }
      if (!OB.Properties[propertyName]) {
        return null;
      }
      return OB.Properties[propertyName];
    },

    // ** {{{ PropertyStore.set(propertyName, value) }}} **
    //
    // Sets the property in the local cache. Also performs a server call to
    // persist the
    // property in the database.
    //
    // Parameters:
    // * {{{propertyName}}}: the name of the property
    // * {{{value}}}: the value of the property
    //
    set: function (propertyName, value, windowId, noSetInServer, setAsSystem) {
      var currentValue = OB.Properties[propertyName],
          localPropertyName = propertyName,
          i, length, data;

      data = {
        property: propertyName,
        system: setAsSystem ? true : false
      };

      if (windowId) {
        data.windowId = windowId;
        localPropertyName = propertyName + '_' + windowId;
      }

      // set it locally
      OB.Properties[localPropertyName] = value;

      if (!noSetInServer) {
        // and set it in the server also
        OB.RemoteCallManager.call('org.openbravo.client.application.StorePropertyActionHandler', value, data, function () {});
      }

      // call the listeners
      length = this.listeners.length;
      for (i = 0; i < length; i++) {
        this.listeners[i](localPropertyName, currentValue, value);
      }

    },

    // ** {{{addListener(listener) }}} **
    //
    // Register a new listener which will be called when a property change occurs.
    // The function is called after the property change.
    //
    // Parameters:
    // * {{{listener}}}: a function which is called when a new alert result is
    // received. The function will get three parameters: property name, old value, new value
    addListener: function (listener) {
      this.listeners[this.listeners.length] = listener;
    }
  };

  // Initialize PropertyStore object
  pstore = OB.PropertyStore = new PropertyStore();
}(OB, isc));