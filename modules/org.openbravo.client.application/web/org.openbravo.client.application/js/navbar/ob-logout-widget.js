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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBLogout', isc.ImgButton);

// = OBLogout =
// The OBLogout implements a widget to logout the application
isc.OBLogout.addProperties({
  keyboardShortcutId: 'NavBar_OBLogout',

  draw: function () {
    var me = this,
        ksAction;

    ksAction = function () {
      OB.Utilities.logout();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set(this.keyboardShortcutId, 'Canvas', ksAction);
    this.Super("draw", arguments);

    this.setPrompt(OB.I18N.getLabel('UINAVBA_EndSession'));
/* Avoid declare directly "prompt: " in this widget definition.
       Declared as "setPrompt" inside "draw" function in order to solve issue https://issues.openbravo.com/view.php?id=18192 in FF */

    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.QuitButton', this);
  },

  click: function () {
    var handle = this.getHandle();
    OB.Utilities.logout();
  }
});