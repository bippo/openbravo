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
isc.ClassFactory.defineClass('OBMessageBarMainIcon', isc.Img);

isc.ClassFactory.defineClass('OBMessageBarDescriptionText', isc.HTMLFlow);

isc.ClassFactory.defineClass('OBMessageBarCloseIcon', isc.ImgButton);

isc.OBMessageBarCloseIcon.addProperties({
  messageBar: null,
  action: function () {
    this.messageBar.hide(true);
  }
});

isc.ClassFactory.defineClass('OBMessageBar', isc.HLayout);

isc.OBMessageBar.addClassProperties({
  TYPE_SUCCESS: 'success',
  TYPE_ERROR: 'error',
  TYPE_WARNING: 'warning',
  TYPE_INFO: 'info'
});

isc.OBMessageBar.addProperties({
  view: null,
  type: null,
  mainIcon: null,
  text: null,
  closeIcon: null,

  initWidget: function () {
    this.mainIcon = isc.OBMessageBarMainIcon.create({});
    this.text = isc.OBMessageBarDescriptionText.create({
      contents: ''
    });
    this.closeIcon = isc.OBMessageBarCloseIcon.create({
      messageBar: this
    });

    this.addMembers([this.mainIcon, this.text, this.closeIcon]);
  },

  hideCloseIcon: function () {
    this.closeIcon.hide();
  },

  showCloseIcon: function () {
    this.closeIcon.show();
  },

  setType: function (type) {
    if (this.setTypeStyle) {
      this.setTypeStyle(type);
    }
    this.type = type;
  },

  setText: function (title, text) {
    if (!title) {
      this.text.setContents(text);
    } else {
      // TODO: low-prio, move styling to a css class
      this.text.setContents('<b>' + title + '</b>' + (text ? '<br/>' + text : ''));
    }
  },

  getDefaultTitle: function (type) {
    if (type === isc.OBMessageBar.TYPE_SUCCESS) {
      return OB.I18N.getLabel('OBUIAPP_Success');
    } else if (type === isc.OBMessageBar.TYPE_ERROR) {
      return OB.I18N.getLabel('OBUIAPP_Error');
    } else if (type === isc.OBMessageBar.TYPE_INFO) {
      return OB.I18N.getLabel('OBUIAPP_Info');
    } else if (type === isc.OBMessageBar.TYPE_WARNING) {
      return OB.I18N.getLabel('OBUIAPP_Warning');
    }
    return null;
  },

  setMessage: function (type, title, text) {
    var i, length, newText, form, grid;
    if (this.view && this.view.viewForm) {
      form = this.view.viewForm;
    }
    if (this.view && this.view.viewGrid) {
      grid = this.view.viewGrid;
    }
    this.setType(type);
    if (isc.isAn.Array(text)) {
      length = text.length;
      // TODO: low prio, do some better styling display of multiple messages
      newText = '<ul class="OBMessageBarTextList">';
      for (i = 0; i < length; i++) {
        newText = newText + '<li>' + text[i] + '</li>';
      }
      text = newText + '</ul>';
    }

    if ((form && form.isSaving) || (grid && grid.isSaving)) {
      text = OB.I18N.getLabel('OBUIAPP_ErrorSavingFailed') + ' ' + text;
    }

    this.setText(title || this.getDefaultTitle(type), text);
    delete this.hasFilterMessage;
    this.show();
  },

  // calls te OB.I18N.getLabel to asynchronously get a label
  // and display it  
  setLabel: function (type, title, label, params) {
    var me = this;
    OB.I18N.getLabel(label, params, {
      setLabel: function (text) {
        me.setMessage(type, title, text);
      }
    }, 'setLabel');
  },

  hide: function (force) {
    // if hide is not forced, keep the message if marked as keepOnAutomaticRefresh
    if (force || !this.keepOnAutomaticRefresh) {
      delete this.keepOnAutomaticRefresh;
      this.Super('hide', arguments);
    }
  }
});