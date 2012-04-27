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

// == OB.CalloutRegistry ==
// A registry which can be used to register callouts for a certain
// tab and field combination. Multiple callouts can be registered
// for one field.
OB.OnChangeRegistry = {
  registry: {},

  register: function (tabId, field, callback, id) {
    var tabEntry, fieldEntry, i, overwritten = false;

    if (!this.registry[tabId]) {
      this.registry[tabId] = {};
    }

    tabEntry = this.registry[tabId];
    if (!tabEntry[field]) {
      tabEntry[field] = [];
    }

    if (id && !callback.id) {
      callback.id = id;
    }

    // just set a default sort if not defined
    if (callback.sort !== 0 && !callback.sort) {
      callback.sort = 100;
    }

    // check if there is one with the same name
    for (i = 0; i < tabEntry[field].length; i++) {
      if (tabEntry[field][i] && tabEntry[field][i].id === callback.id) {
        tabEntry[field][i] = callback;
        overwritten = true;
        break;
      }
    }

    // add
    if (!overwritten) {
      tabEntry[field].push(callback);
    }

    // and sort according to the sort property
    tabEntry[field].sortByProperty('sort', true);
  },

  hasOnChange: function (tabId, item) {
    return this.getFieldEntry(tabId, item);
  },

  getFieldEntry: function (tabId, item) {
    var tabEntry, field = item.name;
    if (!this.registry[tabId]) {
      return;
    }
    tabEntry = this.registry[tabId];
    return tabEntry[field];
  },

  call: function (tabId, item, view, form, grid) {
    var callResult, fieldEntry = this.getFieldEntry(tabId, item),
        i;

    if (!fieldEntry) {
      return;
    }
    for (i = 0; i < fieldEntry.length; i++) {
      if (fieldEntry[i]) {
        callResult = fieldEntry[i](item, view, form, grid);
        if (callResult === false) {
          return;
        }
      }
    }
  }
};

OB.OnChangeRegistry.TestFunction = function (item) {
  alert('You changed ' + item.name);
};