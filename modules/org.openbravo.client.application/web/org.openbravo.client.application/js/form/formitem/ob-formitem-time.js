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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBTimeItem ==
// For entering times.
isc.ClassFactory.defineClass('OBTimeItem', isc.TimeItem);

isc.OBTimeItem.addProperties({
  operator: 'equals',
  validateOnExit: true,
  showHint: false,
  displayFormat: 'to24HourTime',
  short24TimeFormat: 'HH:MM:SS',
  shortTimeFormat: 'HH:MM:SS',
  long24TimeFormat: 'HH:MM:SS',
  longTimeFormat: 'HH:MM:SS',
  
  // make sure that the undo/save buttons get enabled, needs to be done like
  // this because changeOnKeypress is false
  keyPress: function(item, form, keyName, characterValue){
    if (characterValue || keyName === 'Backspace' || keyName === 'Delete') {
      if (this.form.itemChangeActions) {
        this.form.itemChangeActions();
      }
    }
    return this.Super('keyPress', arguments);
  }
});

