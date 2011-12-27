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
 * All portions are Copyright (C) 2001-200/ Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview This JavaScript defines keyboard operation shortcuts for common
* actions within the application (eg. save record, show/hide menu, bring up 
* help, tab navigation, etc.).
*/

/**
* Builds the keys array on each screen. Each key that we want to use should have this structure.
* @param {String} key A text version of the handled key.
* @param {String} evalfunc Function that will be eval when the key is is pressed.
* @param {String} field Name of the field on the window. If is null, is a global event, for the hole window.
* @param {String} auxKey Text defining the auxiliar key. The value could be CTRL for the Control key, ALT for the Alt, null if we don't have to use an auxiliar key.
* @param {Boolean} propagateKey True if the key is going to be prograpated or false if is not going to be propagated.
* @param {String} eventShotter Function that will launch the process.
*/
function keyArrayItem(key, evalfunc, field, auxKey, propagateKey, event) {
  this.key = key;
  this.evalfunc = evalfunc;
  this.field = field;
  this.auxKey = auxKey;
  this.propagateKey = propagateKey;
  this.eventShotter = event;
}

/**
* Defines the keys array for all the application.
*/
function getShortcuts(type) {
  if (type==null || type=="" || type=="null") {
  } else if (type=='applicationCommonKeys') {
    // don't override browser shortcuts in case of MDI environment
    if (!isWindowInMDIContext) {
      this.keyArray.splice(keyArray.length-1, 0,
          new keyArrayItem("M", "executeMenuButton('buttonExpand');executeMenuButton('buttonCollapse');", null, "ctrlKey+shiftKey", false, 'onkeydown'),
          new keyArrayItem("U", "executeMenuButton('buttonUserOptions');", null, "ctrlKey", false, 'onkeydown'),
          new keyArrayItem("Q", "executeMenuButton('buttonQuit');", null, "ctrlKey", false, 'onkeydown'),
          new keyArrayItem("F8", "executeMenuButton('buttonAlerts');", null, null, false, 'onkeydown'),
          new keyArrayItem("F9", "menuShowHide();", null, null, false, 'onkeydown'),
          new keyArrayItem("I", "executeWindowButton('buttonAbout');", null, "ctrlKey", false, 'onkeydown'),
          new keyArrayItem("H", "executeWindowButton('buttonHelp');", null, "ctrlKey", false, 'onkeydown'),
          new keyArrayItem("R", "executeWindowButton('buttonRefresh');", null, "ctrlKey", false, 'onkeydown'),
          new keyArrayItem("BACKSPACE", "executeWindowButton('buttonBack');", null, "ctrlKey+shiftKey", false, 'onkeydown')
        );
    } else {
      var LayoutMDI = getFrame('LayoutMDI');
      if (typeof LayoutMDI.OB.Layout.ClassicOBCompatibility.Keyboard === "object" && typeof LayoutMDI.OB.Layout.ClassicOBCompatibility.Keyboard.getMDIKS === "function") {
        var MDIKeyJSON = LayoutMDI.OB.Layout.ClassicOBCompatibility.Keyboard.getMDIKS();
        for (var i=0; i<MDIKeyJSON.length; i++) {
          this.keyArray.splice(keyArray.length-1, 0,
              new keyArrayItem(MDIKeyJSON[i].key, [MDIKeyJSON[i].action, MDIKeyJSON[i].funcParam], null, MDIKeyJSON[i].auxKey, false, 'onkeydown')
          );
        }
      }
    }
  } else if (type=='menuSpecificKeys') {
      this.keyArray.splice(keyArray.length-1, 0,
        new keyArrayItem("M", "putFocusOnWindow();", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("TAB", "menuTabKey(true);", null, null, false, 'onkeydown'),
        new keyArrayItem("TAB", "menuTabKey(false);", null, null, false, 'onkeyup'),
        new keyArrayItem("TAB", "menuShiftTabKey(true);", null, "shiftKey", false, 'onkeydown'),
        new keyArrayItem("TAB", "menuShiftTabKey(false);", null, "shiftKey", false, 'onkeyup'),
        new keyArrayItem("ENTER", "menuEnterKey();", null, null, false, 'onkeydown'),
        new keyArrayItem("UPARROW", "menuUpKey(true);", null, null, false, 'onkeydown'),
        new keyArrayItem("RIGHTARROW", "menuRightKey();", null, null, false, 'onkeydown'),
        new keyArrayItem("DOWNARROW", "menuDownKey(true);", null, null, false, 'onkeydown'),
        new keyArrayItem("LEFTARROW", "menuLeftKey();", null, null, false, 'onkeydown'),
        new keyArrayItem("HOME", "menuHomeKey();", null, null, false, 'onkeydown'),
        new keyArrayItem("END", "menuEndKey();", null, null, false, 'onkeydown'),
        new keyArrayItem("UPARROW", "menuUpKey(false);", null, null, null, 'onkeyup'),
        new keyArrayItem("DOWNARROW", "menuDownKey(false);", null, null, null, 'onkeyup')
      );
  } else if (type=='windowCommonKeys') {
      this.keyArray.splice(keyArray.length-1, 0,
        new keyArrayItem("M", "putFocusOnMenu();", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("F10", "swichSelectedArea();", null, null, false, 'onkeydown'),
        new keyArrayItem("N", "executeWindowButton('linkButtonNew',true);", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("N", "executeWindowButton('linkButtonSave_Next',true);", null, "ctrlKey+shiftKey", false, 'onkeydown'),
        new keyArrayItem("G", "executeWindowButton('linkButtonSave_Relation',true);", null, "ctrlKey+shiftKey", false, 'onkeydown'),
        new keyArrayItem("S", "executeWindowButton('linkButtonSave',true);", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("S", "executeWindowButton('linkButtonSave_New',true);", null, "ctrlKey+shiftKey", false, 'onkeydown'),
        new keyArrayItem("D", "executeWindowButton('linkButtonDelete');", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("Z", "executeWindowButton('linkButtonUndo');", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("A", "executeWindowButton('linkButtonAttachment');", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("F", "executeWindowButton('linkButtonSearch');executeWindowButton('linkButtonSearchFiltered');", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("HOME", "executeWindowButton('linkButtonFirst',true);", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("END", "executeWindowButton('linkButtonLast',true);", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("LEFTARROW", "executeWindowButton('linkButtonPrevious',true);", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("RIGHTARROW", "executeWindowButton('linkButtonNext',true);", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("L", "executeWindowButton('linkButtonRelatedInfo');", null, "ctrlKey", false, 'onkeydown')
      );
  } else if (type=='editionSpecificKeys') {
      this.keyArray.splice(keyArray.length-1, 0,
        new keyArrayItem("TAB", "windowTabKey(true);", null, null, false, 'onkeydown'),
        new keyArrayItem("TAB", "windowTabKey(false);", null, null, false, 'onkeyup'),
        new keyArrayItem("TAB", "windowShiftTabKey(true);", null, "shiftKey", false, 'onkeydown'),
        new keyArrayItem("TAB", "windowShiftTabKey(false);", null, "shiftKey", false, 'onkeyup'),
        new keyArrayItem("ENTER", "windowCtrlShiftEnterKey();", null, "ctrlKey+shiftKey", false, 'onkeydown'),
        new keyArrayItem("ENTER", "windowCtrlEnterKey();", null, "ctrlKey", true, 'onkeydown'),
        new keyArrayItem("ENTER", "windowEnterKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("G", "executeWindowButton('buttonRelation');", null, "ctrlKey", false, 'onkeydown')
      );
  } else if (type=='relationSpecificKeys') {
      this.keyArray.splice(keyArray.length-1, 0,
        new keyArrayItem("TAB", "windowTabKey(true);", null, null, false, 'onkeydown'),
        new keyArrayItem("TAB", "windowTabKey(false);", null, null, false, 'onkeyup'),
        new keyArrayItem("TAB", "windowShiftTabKey(true);", null, "shiftKey", false, 'onkeydown'),
        new keyArrayItem("TAB", "windowShiftTabKey(false);", null, "shiftKey", false, 'onkeyup'),
        new keyArrayItem("G", "executeWindowButton('buttonEdition');", null, "ctrlKey", false, 'onkeydown'),
        new keyArrayItem("DELETE", "executeWindowButton('linkButtonDelete');", null, null, false, 'onkeydown'),
        new keyArrayItem("ENTER", "windowEnterKey();", null, null, true, 'onkeydown')
      );
  } else if (type=='gridKeys') {
      this.keyArray.splice(keyArray.length-1, 0,
        new keyArrayItem("UPARROW", "windowUpKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("RIGHTARROW", "windowRightKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("DOWNARROW", "windowDownKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("LEFTARROW", "windowLeftKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("HOME", "windowHomeKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("END", "windowEndKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("REPAGE", "windowRepageKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("AVPAGE", "windowAvpageKey();", null, null, true, 'onkeydown')
      );
  } else if (type=='genericTreeKeys') {
      this.keyArray.splice(keyArray.length-1, 0,
        new keyArrayItem("UPARROW", "windowUpKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("RIGHTARROW", "windowRightKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("DOWNARROW", "windowDownKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("LEFTARROW", "windowLeftKey();", null, null, true, 'onkeydown'),
        new keyArrayItem("SPACE", "windowSpaceKey();", null, null, true, 'onkeydown')
      );
  } else if (type=='popupSpecificKeys') {
      this.keyArray.splice(keyArray.length-1, 0,
        new keyArrayItem("ESCAPE", "closePage();", null, null, false, 'onkeydown'),
        new keyArrayItem("ENTER", "xx();", null, "shiftKey", false, 'onkeydown')
      );
  }
}
