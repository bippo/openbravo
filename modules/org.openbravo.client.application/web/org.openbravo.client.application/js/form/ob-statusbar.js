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
isc.ClassFactory.defineClass('OBStatusBarLeftBar', isc.HLayout);

isc.OBStatusBarLeftBar.addProperties({
  // to allow setting the active view when clicking in the statusbar
  canFocus: true
});

isc.ClassFactory.defineClass('OBStatusBarTextLabel', isc.Label);

isc.OBStatusBarTextLabel.addProperties({
  // to allow setting the active view when clicking in the statusbar
  canFocus: true,
  canSelectText: true
});

isc.ClassFactory.defineClass('OBStatusBarIconButtonBar', isc.HLayout);

isc.OBStatusBarIconButtonBar.addProperties({
  // to allow setting the active view when clicking in the statusbar
  canFocus: true
});

isc.ClassFactory.defineClass('OBStatusBarIconButton', isc.ImgButton);

isc.OBStatusBarIconButton.addProperties({
  buttonType: null,
  view: null,
  // to allow setting the active view when clicking in the statusbar
  canFocus: true,
  keyboardShortcutId: null,

  // always go through the autosave of the window
  action: function () {
    // to avoid issue that autosave is executed when maximize/minimize views using KS
    if (this.buttonType === 'maximizeRestore') {
      this.doAction();
      return;
    }

    // don't do autosave if new and nothing changed
    if (this.buttonType === 'close' && !this.view.viewForm.hasChanged && this.view.viewForm.isNew) {
      this.view.standardWindow.setDirtyEditForm(null);
    }

    // or when maximizing/minimizing
    if (this.buttonType === 'maximize' || this.buttonType === 'restore') {
      this.doAction();
      return;
    }

    var actionObject = {
      target: this,
      method: this.doAction,
      parameters: []
    };
    this.view.standardWindow.doActionAfterAutoSave(actionObject, false);
  },

  doAction: function () {
    var invalidFormState = this.view.viewForm.hasChanged && !this.view.viewForm.validateForm(),
        rowNum, newRowNum, newRecord, theButtonBar, i, length;
    if (this.buttonType === 'previous') {
      if (invalidFormState) {
        return;
      }
      this.view.editNextPreviousRecord(false);
    } else if (this.buttonType === 'maximize') {
      this.view.maximize();
    } else if (this.buttonType === 'restore') {
      this.view.restore();
    } else if (this.buttonType === 'next') {
      if (invalidFormState) {
        return;
      }
      this.view.editNextPreviousRecord(true);
    } else if (this.buttonType === 'close') {
      if (invalidFormState) {
        return;
      }
      this.view.viewForm.doClose();
    } else if (this.buttonType === 'maximizeRestore') {
      theButtonBar = this.view.statusBar.buttonBar;
      if (theButtonBar.members) {
        length = theButtonBar.members.length;
        for (i = 0; i < length; i++) {
          if (theButtonBar.members[i].buttonType === 'maximize' && !theButtonBar.members[i].isDisabled() && theButtonBar.members[i].isVisible()) {
            theButtonBar.members[i].action();
            break;
          } else if (theButtonBar.members[i].buttonType === 'restore' && !theButtonBar.members[i].isDisabled() && theButtonBar.members[i].isVisible()) {
            theButtonBar.members[i].action();
            break;
          }
        }
      }
    }
  },

  enableShortcut: function () {
    var me = this,
        ksAction;
    if (this.keyboardShortcutId) {
      ksAction = function () {
        if (!me.isDisabled() && me.isVisible()) {
          me.focus();
          me.action();
        } else if (me.forceKeyboardShortcut) {
          me.action();
        }
        return false; //To avoid keyboard shortcut propagation
      };
      OB.KeyboardManager.Shortcuts.set(this.keyboardShortcutId, 'OBViewForm', ksAction);
    }
  },

  disableShortcut: function () {
    if (this.keyboardShortcutId) {
      OB.KeyboardManager.Shortcuts.set(this.keyboardShortcutId, null, function () {
        return true;
      });
    }
  },

  initWidget: function () {
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }

});

isc.ClassFactory.defineClass('OBStatusBar', isc.HLayout);

isc.OBStatusBar.addProperties({
  view: null,
  iconButtonGroupSpacerWidth: 0,
  // Set in the skin
  previousButton: null,
  nextButton: null,
  closeButton: null,
  maximizeButton: null,
  restoreButton: null,
  maximizeRestoreButton: null,

  newIcon: null,
  editIcon: null,
  showingIcon: false,
  mode: '',
  isActive: true,
  buttonBar: null,
  buttonBarProperties: {},

  initWidget: function () {
    this.content = isc.HLayout.create({
      defaultLayoutAlign: 'center',
      width: '100%',
      height: '100%'
    });

    this.leftStatusBar = isc.OBStatusBarLeftBar.create({});
    this.leftStatusBar.addMember(this.content);

    this.buttonBar = isc.OBStatusBarIconButtonBar.create(this.buttonBarProperties);
    this.addCreateButtons();

    this.savedIcon = isc.Img.create(this.savedIconDefaults);
    this.newIcon = isc.Img.create(this.newIconDefaults);
    this.editIcon = isc.Img.create(this.editIconDefaults);
    this.spacer = isc.LayoutSpacer.create({
      width: 14
    });
    this.leftStatusBar.addMember(this.spacer, 0);

    this.addMembers([this.leftStatusBar, this.buttonBar]);
    this.Super('initWidget', arguments);
  },

  addCreateButtons: function () {
    var i, length, buttonSpacer;

    buttonSpacer = isc.HLayout.create({
      width: this.iconButtonGroupSpacerWidth
    });

    this.previousButton = isc.OBStatusBarIconButton.create({
      view: this.view,
      buttonType: 'previous',
      keyboardShortcutId: 'StatusBar_Previous',
      prompt: OB.I18N.getLabel('OBUIAPP_PREVIOUSBUTTON')
    });

    this.nextButton = isc.OBStatusBarIconButton.create({
      view: this.view,
      buttonType: 'next',
      keyboardShortcutId: 'StatusBar_Next',
      prompt: OB.I18N.getLabel('OBUIAPP_NEXTBUTTON')
    });

    this.closeButton = isc.OBStatusBarIconButton.create({
      view: this.view,
      buttonType: 'close',
      keyboardShortcutId: 'StatusBar_Close',
      prompt: OB.I18N.getLabel('OBUIAPP_CLOSEBUTTON')
    });

    this.maximizeButton = isc.OBStatusBarIconButton.create({
      view: this.view,
      buttonType: 'maximize',
      prompt: OB.I18N.getLabel('OBUIAPP_MAXIMIZEBUTTON')
    });

    this.restoreButton = isc.OBStatusBarIconButton.create({
      visibility: 'hidden',
      view: this.view,
      buttonType: 'restore',
      prompt: OB.I18N.getLabel('OBUIAPP_RESTOREBUTTON')
    });

    this.maximizeRestoreButton = isc.OBStatusBarIconButton.create({ // Only for implement 'StatusBar_Maximize-Restore' keyboard shortcut
      visibility: 'hidden',
      view: this.view,
      buttonType: 'maximizeRestore',
      forceKeyboardShortcut: true,
      keyboardShortcutId: 'StatusBar_Maximize-Restore'
    });

    this.buttonBar.addMembers([this.previousButton, this.nextButton, buttonSpacer, this.maximizeButton, this.restoreButton, this.closeButton, this.maximizeRestoreButton]);
    length = this.buttonBar.members.length;
    for (i = 0; i < length; i++) {
      if (this.buttonBar.members[i].buttonType) {
        OB.TestRegistry.register('org.openbravo.client.application.statusbar.button.' + this.buttonBar.members[i].buttonType + '.' + this.view.tabId, this.buttonBar.members[i]);
      }
    }
  },

  draw: function () {
    this.Super('draw', arguments);
  },

  visibilityChanged: function (state) {
    if (this.isActive) {
      if (state) {
        this.enableShortcuts();
      } else {
        this.disableShortcuts();
      }
    }
  },

  setActive: function (value) {
    if (value) {
      this.isActive = true;
      this.enableShortcuts();
    } else {
      this.isActive = false;
      this.disableShortcuts();
    }
  },

  enableShortcuts: function () {
    var i;
    if (this.buttonBar.members) {
      for (i = 0; i < this.buttonBar.members.length; i++) {
        if (this.buttonBar.members[i].enableShortcut) {
          this.buttonBar.members[i].enableShortcut();
        }
      }
    }
  },

  disableShortcuts: function () {
    var length, i;
    if (this.buttonBar.members) {
      length = this.buttonBar.members.length;
      for (i = 0; i < length; i++) {
        if (this.buttonBar.members[i].disableShortcut) {
          this.buttonBar.members[i].disableShortcut();
        }
      }
    }
  },

  addIcon: function (icon) {
    // remove any existing icon or spacer
    this.leftStatusBar.destroyAndRemoveMembers(this.leftStatusBar.members[0]);
    this.leftStatusBar.addMember(icon, 0);
  },

  removeIcon: function () {
    // remove any existing icon or spacer
    this.leftStatusBar.destroyAndRemoveMembers(this.leftStatusBar.members[0]);
    this.leftStatusBar.addMember(this.spacer, 0);
  },

  setNewState: function (isNew) {
    this.previousButton.setDisabled(isNew);
    this.nextButton.setDisabled(isNew);
    if (isNew) {
      this.mode = 'NEW';
      this.setContentLabel(this.newIcon, 'OBUIAPP_New');
    }
  },

  setContentLabel: function (icon, statusCode, arrayTitleField, message) {
    // set the status code before calling updateContentTitle
    this.statusCode = statusCode;

    this.updateContentTitle(arrayTitleField, message);

    if (icon) {
      this.addIcon(icon);
    } else {
      this.removeIcon(icon);
    }
  },

  updateContentTitle: function (arrayTitleField, message) {
    var linkImageWidth = this.titleLinkImageWidth,
        linkImageHeight = this.titleLinkImageHeight,
        msg = '',
        i, length, undef;

    if (typeof linkImageWidth !== 'undefined') {
      linkImageWidth = linkImageWidth.toString();
      if (linkImageWidth.indexOf('px') === -1) {
        linkImageWidth = linkImageWidth + 'px';
      }
      linkImageWidth = 'width: ' + linkImageWidth + ';';
    } else {
      linkImageWidth = '';
    }

    if (typeof linkImageHeight !== 'undefined') {
      linkImageHeight = linkImageHeight.toString();
      if (linkImageHeight.indexOf('px') === -1) {
        linkImageHeight = linkImageHeight + 'px';
      }
      linkImageHeight = 'height: ' + linkImageHeight + ';';
    } else {
      linkImageHeight = '';
    }

    for (i = this.content.members.length - 1; i >= 0; i--) {
      if (this.content.members[i].canvasItem) {
        this.content.removeMember(this.content.members[i]);
      }
    }

    this.content.destroyAndRemoveMembers(this.content.members);
    this.content.setMembers([]);

    if (!isc.Page.isRTL()) { // LTR mode
      if (this.statusCode) {
        msg = '<span class="' + (this.statusLabelStyle ? this.statusLabelStyle : '') + '">' + OB.I18N.getLabel(this.statusCode) + '</span>';
        this.content.addMember(isc.OBStatusBarTextLabel.create({
          contents: msg
        }));
      }
      if (arrayTitleField) {
        length = arrayTitleField[0].length;
        for (i = 0; i < length; i++) {
          if (i !== 0 || this.statusCode) {
            msg = '<span class="' + (this.separatorLabelStyle ? this.separatorLabelStyle : '') + '">' + '&nbsp;&nbsp;|&nbsp;&nbsp;' + '</span>';
          }
          if (isc.isA.Canvas(arrayTitleField[1][i])) {
            if (msg) {
              this.content.addMember(isc.OBStatusBarTextLabel.create({
                contents: msg
              }));
            }
            if (arrayTitleField[0][i]) {
              msg = '<span class="' + (this.titleLabelStyle ? this.titleLabelStyle : '') + '">' + arrayTitleField[0][i] + ':&nbsp;' + '</span>';
              this.content.addMember(isc.OBStatusBarTextLabel.create({
                contents: msg
              }));
            }

            // required by the automatic smoke test
            arrayTitleField[1][i]._title = arrayTitleField[0][i];
            arrayTitleField[1][i]._value = arrayTitleField[1][i].contents;

            arrayTitleField[1][i].show();
            arrayTitleField[1][i].inStatusBar = true;
            this.content.addMember(arrayTitleField[1][i]);
            continue;
          }

          if (arrayTitleField.length === 6 && arrayTitleField[2][i] !== undef && arrayTitleField[3][i] !== undef && arrayTitleField[4][i] !== undef && arrayTitleField[5][i] !== undef) {
            msg += '<span class="' + (this.titleLinkStyle ? this.titleLinkStyle : '') + '" onclick="OB.Utilities.openDirectView(\'' + arrayTitleField[2][i] + '\', \'' + arrayTitleField[3][i] + '\', \'' + arrayTitleField[4][i] + '\', \'' + arrayTitleField[5][i] + '\')">' + arrayTitleField[0][i] + ':&nbsp;<img src="' + (this.titleLinkImageSrc ? this.titleLinkImageSrc : '') + '" style="' + linkImageWidth + linkImageHeight + '" />&nbsp;' + '</span>';
          } else {
            msg += '<span class="' + (this.titleLabelStyle ? this.titleLabelStyle : '') + '">' + arrayTitleField[0][i] + ':&nbsp;' + '</span>';
          }
          msg += '<span class="' + (this.fieldLabelStyle ? this.fieldLabelStyle : '') + '">' + this.getValidValue(arrayTitleField[1][i]) + '</span>';
          this.content.addMember(isc.OBStatusBarTextLabel.create({
            contents: msg,
            _title: arrayTitleField[0][i],
            _value: this.getValidValue(arrayTitleField[1][i])
          }));
          msg = null;
        }
      }
      if (message) {
        if (arrayTitleField || this.statusCode) {
          msg = '<span class="' + (this.separatorLabelStyle ? this.separatorLabelStyle : '') + '">' + '&nbsp;&nbsp;|&nbsp;&nbsp;' + '</span>';
        }
        msg += '<span class="' + (this.titleLabelStyle ? this.titleLabelStyle : '') + '">' + message + '</span>';
        this.content.addMember(isc.OBStatusBarTextLabel.create({
          contents: msg
        }));
      }
    } else { // RTL mode
      if (message) {
        msg = '<span class="' + (this.titleLabelStyle ? this.titleLabelStyle : '') + '">' + message + '</span>';
        if (arrayTitleField || this.statusCode) {
          msg += '<span class="' + (this.separatorLabelStyle ? this.separatorLabelStyle : '') + '">' + '&nbsp;&nbsp;|&nbsp;&nbsp;' + '</span>';
        }
        this.content.addMember(isc.OBStatusBarTextLabel.create({
          contents: msg
        }));
      }
      if (arrayTitleField) {
        for (i = arrayTitleField[0].length - 1; i >= 0; i--) {
          msg = '<span class="' + (this.fieldLabelStyle ? this.fieldLabelStyle : '') + '">' + this.getValidValue(arrayTitleField[1][i]) + '</span>';
          if (arrayTitleField[2][i] !== undef && arrayTitleField[3][i] !== undef && arrayTitleField[4][i] !== undef && arrayTitleField[5][i] !== undef) {
            msg += '<span class="' + (this.titleLinkStyle ? this.titleLinkStyle : '') + '" onclick="OB.Utilities.openDirectView(\'' + arrayTitleField[2][i] + '\', \'' + arrayTitleField[3][i] + '\', \'' + arrayTitleField[4][i] + '\', \'' + arrayTitleField[5][i] + '\')">' + '&nbsp;<img src="' + (this.titleLinkImageSrc ? this.titleLinkImageSrc : '') + '" style="' + linkImageWidth + linkImageHeight + '"/>&nbsp;:' + arrayTitleField[0][i] + '</span>';
          } else {
            msg += '<span class="' + (this.titleLabelStyle ? this.titleLabelStyle : '') + '">' + ' :' + arrayTitleField[0][i] + '</span>';
          }
          if (i !== 0 || this.statusCode) {
            msg += '<span class="' + (this.separatorLabelStyle ? this.separatorLabelStyle : '') + '">' + '&nbsp;&nbsp;|&nbsp;&nbsp;' + '</span>';
          }
          this.content.addMember(isc.OBStatusBarTextLabel.create({
            contents: msg
          }));
        }
      }
      if (this.statusCode) {
        msg = '<span class="' + (this.statusLabelStyle ? this.statusLabelStyle : '') + '">' + OB.I18N.getLabel(this.statusCode) + '</span>';
        this.content.addMember(isc.OBStatusBarTextLabel.create({
          contents: msg
        }));
      }
    }
  },

  getValidValue: function (value) {
    var undef;
    if (value === null || value === undef) {
      return '&nbsp;&nbsp;&nbsp;';
    }
    return value;
  },

  destroy: function () {
    if (this.savedIcon) {
      this.savedIcon.destroy();
      this.savedIcon = null;
    }

    if (this.newIcon) {
      this.newIcon.destroy();
      this.newIcon = null;
    }

    if (this.editIcon) {
      this.editIcon.destroy();
      this.editIcon = null;
    }
    this.Super('destroy', arguments);
  }
});