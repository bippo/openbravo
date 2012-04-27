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
 * Contributor(s): ___________
 ************************************************************************
 */

// = Manage Views Popups =
// Contains the implementation of the popups which are called
// from the toolbar button when the user does save or delete of a view.
// There are 2 popups: one to save, and one to delete
//
// The implementation is split in 3 parts:
// - OB.Personalization.ManageViewsPopupProperties: common properties for 
//  both the save and the delete function.
// - OB.Personalization.ManageViewsPopupPropertiesDelete: properties/overrides
//  specific for the delete popup.
// - OB.Personalization.ManageViewsPopupPropertiesSave: properties specific 
//  for the save popup
// ** {{{OB.Personalization.ManageViewsPopupProperties}}} **
// The common part of the popup which allows to save or delete 
// a view. 
OB.Personalization.ManageViewsPopupProperties = {
  toggleSave: true,
  showMaximizeButton: false,
  showMinimizeButton: false,

  initWidget: function () {
    var layout, window = this,
        form, saveButton, buttonsLayout;

    // create a save button, it is enabled/disabled
    // from the form
    saveButton = isc.OBFormButton.create({
      title: this.actionLabel,
      action: function () {
        // the doAction is overridden/implemented in the 
        // specific save/delete properties
        window.doAction(this.form);
        window.closeClick();
      },
      disabled: this.toggleSave
    });

    form = isc.DynamicForm.create({
      // TODO: parts are the same as in the user-profile 
      // navigation bar component form, should be put in
      // a generic style somewhere
      autoFocus: true,
      overflow: 'visible',
      titleOrientation: 'top',
      height: 1,
      width: 1,
      titleSuffix: '</b>',
      titlePrefix: '<b>',
      requiredTitleSuffix: ' *</b>',
      requiredRightTitlePrefix: '<b>* ',
      rightTitlePrefix: '<b>',
      rightTitleSuffix: '</b>',
      numCols: 1,
      errorOrientation: 'right',
      toggleSave: this.toggleSave,
      itemChanged: function () {
        var pers = this.getValue("personalization");
        if (this.toggleSave) {
          // enable the save button when there is a 
          // personalization record chosen/name entered
          saveButton.setDisabled(!pers);
        }
      },

      handleKeyPress: function () {
        var key = isc.EH.lastEvent.keyName;
        if (key === 'Enter' && !this.saveButton.isDisabled()) {
          this.saveButton.action();
          return false;
        } else {
          return this.Super('handleKeyPress', arguments);
        }
      },

      fields: this.getFields()
    });
    saveButton.form = form;
    form.saveButton = saveButton;

    // create some layouts to put the form/buttons
    // in the popup window
    layout = isc.VLayout.create({
      defaultLayoutAlign: 'center',
      width: '100%',
      height: '100%'
    });
    this.addItem(layout);

    buttonsLayout = isc.HStack.create({}, OB.Styles.Personalization.popupButtonLayout);
    buttonsLayout.addMembers(saveButton);
    buttonsLayout.addMembers(isc.OBFormButton.create({
      title: OB.I18N.getLabel('UINAVBA_Cancel'),
      click: function () {
        window.closeClick();
      }
    }));

    layout.addMember(form);
    layout.addMember(buttonsLayout);

    this.Super('initWidget', arguments);
  },

  getFields: function () {
    // is overridden/implemented in the specific 
    // implementation below.
  }
};

//** {{{OB.Personalization.ManageViewsPopupPropertiesDelete}}} **
//Contains delete specific properties for the popup.
OB.Personalization.ManageViewsPopupPropertiesDefault = {
  title: OB.I18N.getLabel('OBUIAPP_SetDefaultView'),
  actionLabel: OB.I18N.getLabel('OBUIAPP_Apply'),
  toggleSave: true,

  getFields: function () {
    var i, value, personalization = this.standardWindow.getClass().personalization,
        views = personalization && personalization.views ? personalization.views : [],
        valueMap = {},
        flds = [],
        standardWindow = this.standardWindow,
        length;

    if (views) {
      length = views.length;
      for (i = 0; i < length; i++) {
        valueMap[views[i].personalizationId] = views[i].viewDefinition.name;
      }
    }

    flds[0] = isc.addProperties({
      name: 'personalization',
      title: OB.I18N.getLabel('OBUIAPP_DefaultView'),
      valueMap: valueMap,
      editorType: 'select',
      addUnknownValues: false,
      required: true,
      allowEmptyValue: true,
      changed: function () {
        // enable the save button when there is a change
        this.form.saveButton.setDisabled(false);
        // don't let it be disabled again
        this.form.toggleSave = false;
      }
    }, OB.Styles.Personalization.viewFieldDefaults, OB.Styles.OBFormField.DefaultComboBox);

    // set the value
    value = OB.PropertyStore.get('OBUIAPP_DefaultSavedView', this.standardWindow.windowId);
    if (value) {
      flds[0].value = value;
    }

    return flds;
  },

  // do the set default action
  doAction: function (form) {
    var personalizationId = form.getValue("personalization");
    OB.PropertyStore.set('OBUIAPP_DefaultSavedView', personalizationId, this.standardWindow.windowId);
  }
};

//** {{{OB.Personalization.ManageViewsPopupPropertiesDelete}}} **
// Contains delete specific properties for the popup.
OB.Personalization.ManageViewsPopupPropertiesDelete = {
  title: OB.I18N.getLabel('OBUIAPP_DeleteView'),
  actionLabel: OB.I18N.getLabel('OBUIAPP_Delete'),

  // creates one combo with the viewdefinitions which can
  // be deleted by the current user
  getFields: function () {
    var i, personalization = this.standardWindow.getClass().personalization,
        views = personalization && personalization.views ? personalization.views : [],
        valueMap = {},
        flds = [],
        standardWindow = this.standardWindow,
        length;

    if (views) {
      length = views.length;
      for (i = 0; i < length; i++) {
        if (views[i].canEdit) {
          valueMap[views[i].personalizationId] = views[i].viewDefinition.name;
        }
      }
    }

    flds[0] = isc.addProperties({
      name: 'personalization',
      title: OB.I18N.getLabel('OBUIAPP_View'),
      valueMap: valueMap,
      editorType: 'select',
      required: true,
      allowEmptyValue: true
    }, OB.Styles.Personalization.viewFieldDefaults, OB.Styles.OBFormField.DefaultComboBox);
    return flds;
  },

  // do the delete action
  doAction: function (form) {
    var standardWindow = this.standardWindow,
        personalizationId = form.getValue("personalization");
    OB.Personalization.deleteViewDefinition(standardWindow, personalizationId);
  }
};

//** {{{OB.Personalization.ManageViewsPopupPropertiesSave}}} **
//Contains delete specific properties for the popup.
OB.Personalization.ManageViewsPopupPropertiesSave = {
  title: OB.I18N.getLabel('OBUIAPP_SaveView'),

  actionLabel: OB.I18N.getLabel('OBUIAPP_Save'),

  // 3 combo fields are created: views, level and level value
  // the last 2 are only created if the user is allowed to
  // change or set views for different levels
  getFields: function () {
    var i, formData, valueMap = {},
        levelMapSet = false,
        levelMap = {
        '': ''
        },
        flds = [],
        length, standardWindow = this.standardWindow,
        personalization = standardWindow.getClass().personalization,
        views = personalization && personalization.views ? personalization.views : [];

    // create the view combo
    if (views) {
      length = views.length;
      for (i = 0; i < length; i++) {
        if (views[i].canEdit) {
          valueMap[views[i].personalizationId] = views[i].viewDefinition.name;
        }
      }
    }

    flds[0] = isc.addProperties({
      standardWindow: standardWindow,
      name: 'personalization',
      title: OB.I18N.getLabel('OBUIAPP_SaveAs'),
      valueMap: valueMap,
      editorType: 'ComboBoxItem',
      allowEmptyValue: true,
      required: true,

      // if changed, then set the level and levelvalue
      // fields to the current level of the personalization
      changed: function (form, item, value) {
        var i, levelField = form.getField('level'),
            length, levelValueField = form.getField('levelValue'),
            personalization = this.standardWindow.getClass().personalization,
            views;

        // find the personalization
        if (levelField && personalization.views) {
          // and the view, and set the level and level value
          // combos
          views = personalization.views;
          length = views.length;
          for (i = 0; i < length; i++) {
            if (views[i].personalizationId === value) {
              if (views[i].clientId) {
                levelField.storeValue('clients');
                levelValueField.storeValue(views[i].clientId);
              }
              if (views[i].orgId) {
                levelField.storeValue('orgs');
                levelValueField.storeValue(views[i].orgId);
              }
              if (views[i].roleId) {
                levelField.storeValue('roles');
                levelValueField.storeValue(views[i].roleId);
              }
              if (views[i].viewDefinition) {
                form.setValue('default', views[i].viewDefinition.isDefault);
              }
              levelField.updateValueMap(true);
              levelValueField.updateValueMap(true);
            }
          }
        }
      }
    }, OB.Styles.Personalization.viewFieldDefaults, OB.Styles.OBFormField.DefaultComboBox);

    // create the level combo
    if (personalization && personalization.formData) {
      formData = personalization.formData;
      // note the key in the levelMap (clients, orgs, roles) corresponds
      // to the property name in the formData
      if (formData.clients) {
        levelMap.clients = OB.I18N.getLabel("OBUIAPP_Client");
        levelMapSet = true;
      }
      if (formData.orgs) {
        levelMap.orgs = OB.I18N.getLabel("OBUIAPP_Organization");
        levelMapSet = true;
      }
      if (formData.roles) {
        levelMap.roles = OB.I18N.getLabel("OBUIAPP_Role");
        levelMapSet = true;
      }
    }

    // if the user is allowed to set views on different 
    // levels, then create the 2 combos
    if (levelMapSet) {
      flds[1] = isc.addProperties({
        name: 'level',
        title: OB.I18N.getLabel('OBUIAPP_Level'),
        valueMap: levelMap,
        editorType: 'select',
        defaultToFirstOption: true,
        emptyDisplayValue: OB.I18N.getLabel('OBUIAPP_User'),
        changed: function (form, item, value) {
          // if the level combo changes, then set the
          // level value map (so that it shows clients, orgs
          // or roles resp.)
          var levelValueField = form.getField('levelValue');
          levelValueField.setValueMap(formData[value]);
          levelValueField.clearValue();
        }
      }, OB.Styles.Personalization.viewFieldDefaults, OB.Styles.OBFormField.DefaultComboBox);

      flds[2] = isc.addProperties({
        name: 'levelValue',
        title: OB.I18N.getLabel('OBUIAPP_Value'),
        valueMap: {},
        editorType: 'select',
        emptyDisplayValue: OB.User.userName,
        defaultToFirstOption: true
      }, OB.Styles.Personalization.viewFieldDefaults, OB.Styles.OBFormField.DefaultComboBox);

      // and create the checkbox to let it be the default 
      // for other users
      flds[3] = isc.addProperties({
        name: 'default',
        title: OB.I18N.getLabel('OBUIAPP_DefaultView'),
        editorType: 'OBCheckboxItem'
      }, OB.Styles.Personalization.viewFieldDefaults, OB.Styles.OBFormField.DefaultCheckbox);
    }
    return flds;
  },

  doAction: function (form) {
    var name, levelInformation = {},
        persId = {},
        level = form.getValue('level'),
        levelValue = form.getValue('levelValue');

    if (level === 'clients' && levelValue) {
      levelInformation.clientId = levelValue;
    }
    if (level === 'roles' && levelValue) {
      levelInformation.roleId = levelValue;
    }
    if (level === 'orgs' && levelValue) {
      levelInformation.orgId = levelValue;
    }
    if (!levelInformation.clientId && !levelInformation.orgId && !levelInformation.roleId) {
      levelInformation.userId = OB.User.id;
    }
    persId = form.getValue("personalization");
    name = form.getField("personalization").getDisplayValue();

    // same value, the user typed in a new name
    if (name === persId) {
      persId = null;
    }

    OB.Personalization.storeViewDefinition(this.standardWindow, levelInformation, persId, name, form.getValue('default'));
  }
};