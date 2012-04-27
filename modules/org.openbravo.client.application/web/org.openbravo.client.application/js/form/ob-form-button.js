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

// == OBFormButton ==
// The default form button.
isc.ClassFactory.defineClass('OBFormButton', isc.Button);

isc.OBFormButton.addProperties({
  autoFit: true,
  baseStyle: 'OBFormButton',
  titleStyle: 'OBFormButtonTitle'
});


// == OBFocusButton ==
// Invisible button. It changes the focus location when it gets the focus.
isc.ClassFactory.defineClass('OBFocusButton', isc.Button);

isc.OBFocusButton.addProperties({
  title: '',
  width: 1,
  height: 1,
  border: '0px solid',
  getFocusTarget: null,
  focusChanged: function (hasFocus) {
    if (hasFocus && typeof this.getFocusTarget === 'function' && typeof this.getFocusTarget().focus === 'function') {
      this.getFocusTarget().focus();
    }
  }
});