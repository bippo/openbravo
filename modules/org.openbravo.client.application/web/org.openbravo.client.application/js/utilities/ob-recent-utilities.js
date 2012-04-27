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

// = Recent Utilities =
//
// The Recent Utilities provides a mechanism to store and maintain recent choices
// by the user in the user interface. Examples are the last three menu choices 
// of a user in the application menu. The number of entries stores is defined
// by a property UINAVBA_RecentListSize.
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
      rcutils; // Local reference to RemoveCallManager instance

  function RecentUtilities() {}

  RecentUtilities.prototype = {
    getRecentNum: function () {
      if (this.recentNum) {
        return this.recentNum;
      }
      this.recentNum = null;
      var storedRecentNum = OB.PropertyStore.get('UINAVBA_RecentListSize');
      if (storedRecentNum && storedRecentNum.size) {
        this.recentNum = storedRecentNum.size;
      } else if (storedRecentNum) {
        // a direct number
        this.recentNum = storedRecentNum;
      }

      // is the value valid
      if (!this.recentNum || (String(this.recentNum)) !== (String(parseInt(this.recentNum, 10)))) {
        this.recentNum = 3;
      }
      return this.recentNum;
    },

    // ** {{{ RecentUtilities.getRecent(/*String*/ propertyName) }}} **
    //
    // Retrieves the most recent choices which are stored under the name
    // specified by
    // by the parameter
    //
    // Parameters:
    // * {{{propertyName}}}: the name under which the recent value is stored.
    //
    getRecentValue: function (propertyName) {
      var value = OB.PropertyStore.get(propertyName);
      if (!value) {
        return [];
      }
      return value;
    },

    // ** {{{ RecentUtilities.addRecent(/*String*/ propertyName, /*Object*/
    // choiceObject) }}} **
    //
    // Add a new recent entry to the array of recent entries. There are never
    // stored more than {{{recentNum}}}
    // in the recent entries.
    //
    // Parameters:
    // * {{{propertyName}}}: the name under which the recent value is stored.
    // * {{{choiceObject}}}: the object defining the last user choice, can be null
    //                        in this case the call returns without changing things.
    //
    addRecent: function (propertyName, choiceObject) {
      // in some cases the choiceobject is not set, just ignore those
      if (!choiceObject) {
        return;
      }
      var currentRecentValue = this.getRecentValue(propertyName);
      // check if there is already an entry for the new recent
      var currentIndex = -1,
          i, length = currentRecentValue.length;
      for (i = 0; i < length; i++) {
        // if the title is the same then assume they are the same, only 
        // check this if there is no recentId being used
        if (!currentRecentValue[i].recentId && currentRecentValue[i].tabTitle && choiceObject.tabTitle && choiceObject.tabTitle === currentRecentValue[i].tabTitle) {
          currentIndex = i;
        } else if (currentRecentValue[i].recentId && choiceObject.recentId && choiceObject.recentId === currentRecentValue[i].recentId) {
          currentIndex = i;
        } else if (currentRecentValue[i].id && currentRecentValue[i].id === choiceObject.id) {
          currentIndex = i;
        }
      }

      // if found then first remove it, re-add it later
      if (currentIndex > -1) {
        var currentLength = currentRecentValue.length;
        for (i = currentIndex; i < (currentLength - 1); i++) {
          currentRecentValue[i] = currentRecentValue[i + 1];
        }
        currentRecentValue.length = currentRecentValue.length - 1;
      }

      var newLength = 1;
      for (i = (currentRecentValue.length - 1); i >= 0; i--) {
        if (i < (this.getRecentNum() - 1)) {
          currentRecentValue[i + 1] = currentRecentValue[i];
          if (newLength === 1) {
            newLength = i + 1 + 1;
          }
        }
      }
      currentRecentValue[0] = choiceObject;
      currentRecentValue.length = newLength;
      OB.PropertyStore.set(propertyName, currentRecentValue);
    }
  };

  // Initialize RemoteCallManager object
  rcutils = OB.RecentUtilities = new RecentUtilities();
  OB.RecentUtilitiesClass = RecentUtilities;
}(OB, isc));