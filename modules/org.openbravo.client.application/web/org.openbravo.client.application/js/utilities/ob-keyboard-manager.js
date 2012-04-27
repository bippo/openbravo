/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License+
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

// = Keyboard Manager =
//
// Manages the keyboard shortcuts which are used to open flyouts, menus etc.
//
(function (OB, isc) {

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }

  // cache object references locally
  var O = OB,
      ISC = isc,
      keyboardMgr; // Local reference to UrlManager instance

  function KeyboardManager() {}

  KeyboardManager.prototype = {

    Shortcuts: {

      setPredefinedList: function (RefList) {
        var i, list = [],
            length;

        list = OB.PropertyStore.get(RefList);
        if (list) {
          length = list.length;
          for (i = 0; i < length; i++) {
            this.set(list[i].id, null, null, null, list[i].keyComb);
          }
        }
      },

      set: function (id, execLevel, action, funcParam, keyComb) {
        if (typeof id === 'undefined' || id === null) {
          return false;
        }

        var position = this.getProperty('position', id, 'id');
        if (position === null) {
          position = this.list.length;
          this.list[position] = {};
          this.list[position].keyComb = {};
          this.list[position].keyComb.text = '';
        }
        if (typeof id !== 'undefined' && id !== null) {
          this.list[position].id = id;
        }
        if (typeof execLevel !== 'undefined' && execLevel !== null) {
          if (typeof execLevel === 'string') {
            execLevel = new Array(execLevel);
          }
          this.list[position].execLevel = execLevel;
        }
        if (typeof action !== 'undefined' && action !== null) {
          this.list[position].action = action;
        }
        if (typeof funcParam !== 'undefined' && funcParam !== null) {
          this.list[position].funcParam = funcParam;
        }
        if (typeof keyComb !== 'undefined' && keyComb !== null) {
          this.list[position].keyComb.text = '';
          if (typeof keyComb.ctrl === 'undefined') {
            this.list[position].keyComb.ctrl = false;
          } else {
            this.list[position].keyComb.ctrl = keyComb.ctrl;
            if (keyComb.ctrl === true) {
              if (this.list[position].keyComb.text.length > 0) {
                this.list[position].keyComb.text += '+';
              }
              this.list[position].keyComb.text += 'Ctrl';
            }
          }
          if (typeof keyComb.alt === 'undefined') {
            this.list[position].keyComb.alt = false;
          } else {
            this.list[position].keyComb.alt = keyComb.alt;
            if (keyComb.alt === true) {
              if (this.list[position].keyComb.text.length > 0) {
                this.list[position].keyComb.text += '+';
              }
              this.list[position].keyComb.text += 'Alt';
            }
          }
          if (typeof keyComb.shift === 'undefined') {
            this.list[position].keyComb.shift = false;
          } else {
            this.list[position].keyComb.shift = keyComb.shift;
            if (keyComb.shift === true) {
              if (this.list[position].keyComb.text.length > 0) {
                this.list[position].keyComb.text += '+';
              }
              this.list[position].keyComb.text += 'Shift';
            }
          }
          if (typeof keyComb.key === 'undefined') {
            this.list[position].keyComb.key = null;
          } else {
            this.list[position].keyComb.key = keyComb.key;
            if (keyComb.key) {
              if (this.list[position].keyComb.text.length > 0) {
                this.list[position].keyComb.text += '+';
              }
              switch (keyComb.key) {
              case 'f1':
                keyComb.key = 'F1';
                break;
              case 'f2':
                keyComb.key = 'F2';
                break;
              case 'f3':
                keyComb.key = 'F3';
                break;
              case 'f4':
                keyComb.key = 'F4';
                break;
              case 'f5':
                keyComb.key = 'F5';
                break;
              case 'f6':
                keyComb.key = 'F6';
                break;
              case 'f7':
                keyComb.key = 'F7';
                break;
              case 'f8':
                keyComb.key = 'F8';
                break;
              case 'f9':
                keyComb.key = 'F9';
                break;
              case 'f10':
                keyComb.key = 'F10';
                break;
              case 'f11':
                keyComb.key = 'F11';
                break;
              case 'f12':
                keyComb.key = 'F12';
                break;
              case 'I':
                keyComb.key = 'i';
                break; //Special case to ensure 'I' is different than 'l'
              case 'Space':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_SpaceKey');
                break;
              case 'Tab':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_TabKey');
                break;
              case 'Enter':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_EnterKey');
                break;
              case 'Escape':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_EscKey');
                break;
              case 'Backspace':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_BackspaceKey');
                break;
              case 'Insert':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_InsKey');
                break;
              case 'Delete':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_DelKey');
                break;
              case 'Arrow_Up':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_ArrowUpKey');
                break;
              case 'Arrow_Down':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_ArrowDownKey');
                break;
              case 'Arrow_Left':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_ArrowLeftKey');
                break;
              case 'Arrow_Right':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_ArrowRightKey');
                break;
              case 'Home':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_HomeKey');
                break;
              case 'End':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_EndKey');
                break;
              case 'Page_Up':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_PgUpKey');
                break;
              case 'Page_Down':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_PgDnKey');
                break;
              case 'Shift':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_ShiftKey');
                break;
              case 'Ctrl':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_CtrlKey');
                break;
              case 'Alt':
                keyComb.key = OB.I18N.getLabel('OBUIAPP_AltKey');
                break;
              }
              this.list[position].keyComb.text += keyComb.key;
            }
          }
        }
      },

      remove: function (id) {
        var position = this.getProperty('position', id, 'id');
        if (position === null) {
          return false;
        }
        delete this.list[position];
      },

      getProperty: function (property, element, searchPattern) {
        var i, position = null,
            length = this.list.length;
        for (i = 0; i < length; i++) {
          if (typeof this.list[i] === 'undefined') {
            break;
          }
          if (searchPattern === 'id' && this.list[i].id === element) {
            position = i;
          } else if (searchPattern === 'execLevel' && this.list[i].execLevel === element) {
            position = i;
          } else if (searchPattern === 'keyComb' && this.list[i].keyComb.ctrl === element.ctrl && this.list[i].keyComb.alt === element.alt && this.list[i].keyComb.shift === element.shift && this.list[i].keyComb.key === element.key) {
            position = i;
          } else if (searchPattern === 'action' && this.list[i].action === element) {
            position = i;
          }
        }
        if (position !== null) {
          if (property === 'position') {
            return position;
          } else if (property.indexOf('keyComb.') === 0 && this.list[position].keyComb[property.substring(8, property.length)]) {
            return this.list[position].keyComb[property.substring(8, property.length)];
          } else if (this.list[position][property]) {
            return this.list[position][property];
          }
        } else {
          return null;
        }
      },

      execute: function (position) {
        if (this.list[position].action !== null && typeof this.list[position].action === 'function') {
          return this.list[position].action(this.list[position].funcParam);
        } else {
          return true;
        }
      },

      getList: function () {
        return this.list;
      },

      monitor: function (execLevel) {
        var i, j, length = this.list.length,
            position = null,
            pushedKS = {};
        pushedKS.ctrl = false;
        pushedKS.alt = false;
        pushedKS.shift = false;
        pushedKS.key = null;
        if (isc.Event.ctrlKeyDown()) {
          pushedKS.ctrl = true;
        }
        if (isc.Event.altKeyDown()) {
          pushedKS.alt = true;
        }
        if (isc.Event.shiftKeyDown()) {
          pushedKS.shift = true;
        }
        pushedKS.key = isc.Event.getKey();

        for (i = 0; i < length; i++) {
          if (typeof this.list[i] === 'undefined' && !execLevel) {
            break;
          }
          if (this.list[i].execLevel) {
            for (j = 0; j < this.list[i].execLevel.length; j++) {
              if (this.list[i].execLevel[j] === execLevel && this.list[i].keyComb.ctrl === pushedKS.ctrl && this.list[i].keyComb.alt === pushedKS.alt && this.list[i].keyComb.shift === pushedKS.shift && this.list[i].keyComb.key === pushedKS.key) {
                position = i;
                break;
              }
            }
          }

          if (position) {
            break;
          }
        }

        if (position !== null) {
          return this.execute(position);
        } else {
          return true;
        }
      },

      list: []
    }

  };

  // Initialize KeyboardManager object
  keyboardMgr = O.KeyboardManager = new KeyboardManager();

  /* isc.Page.setEvent('keyPress', 'OB.KeyboardManager.Shortcuts.monitor('Canvas')'); // Discart due to Chrome event propagation problems http://forums.smartclient.com/showthread.php?p=65578 */
  isc.Canvas.getPrototype()._originalKeyDown = isc.Canvas.getPrototype().keyDown;
  isc.Canvas.getPrototype().keyDown = function () {
    var response = OB.KeyboardManager.Shortcuts.monitor('Canvas');
    if (response) { // To ensure that if a previous keyDown was set in the Canvas it is executed if the action KeyboardManager.action should be propagated
      response = this._originalKeyDown();
    }
    return response;
  };

}(OB, isc));