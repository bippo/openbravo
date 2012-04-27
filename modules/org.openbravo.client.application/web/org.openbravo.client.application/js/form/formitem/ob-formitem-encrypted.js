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
// == OBEncryptedItem ==
// The type used for encrypted items.
isc.ClassFactory.defineClass('OBEncryptedItem', isc.PasswordItem);

// add specific properties here
isc.OBEncryptedItem.addProperties({
  operator: 'iContains',
  changed: function (form, item, value) {
    this.Super('changed', arguments);
    this.form.setValue(item.name + '_cleartext', value);
    if (this.grid.getEditRow() !== null) {
      this.grid.getEditValues(this.grid.getEditRow())[item.name + '_cleartext'] = value;
    }
  }
});