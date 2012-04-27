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
isc.ClassFactory.defineClass('OBUserProfile', isc.OBQuickRun);

// = OBUserProfile =
// The OBUserProfile implements a widget which displays the currently logged in
// user. By clicking the widget a form is opened which allows to edit the
// user/role information and change the password.
isc.OBUserProfile.addProperties({

  layoutProperties: {},

  // ** {{{ title }}} **
  //
  // Contains the user name of the user
  title: OB.User.userName,

  // ** {{{ src }}} **
  //
  // Set to empty to prevent an icon from being displayed on the button.
  src: '',

  // ** {{{ prompt }}} **
  //
  // Shown on hover, shows some user information.
  prompt: '<b>' + OB.I18N.getLabel('UINAVBA_Role') + '</b>: ' + OB.User.roleName + '<br/>' + '<b>' + OB.I18N.getLabel('UINAVBA_Client') + '</b>: ' + OB.User.clientName + '<br/>' + '<b>' + OB.I18N.getLabel('UINAVBA_Organization') + '</b>: ' + OB.User.organizationName,
  hoverWidth: 200,

  showTitle: true,

  // ** {{{ doShow() }}} **
  //
  // Is called when the forms are shown.
  doShow: function () {
    this.initialize();

    // reset before showing
    this.roleForm.reset();
    this.roleForm.focusInItem('role');
    this.tabSet.selectTab(0);
    this.passwordForm.reset();
    this.passwordForm.setFocusItem('currentPwd');
    this.Super('doShow', arguments);
  },

  formActionHandler: 'org.openbravo.client.application.navigationbarcomponents.UserInfoWidgetActionHandler',

  keyboardShortcutId: 'NavBar_OBUserProfile',

  initWidget: function () {
    var me = this;
    OB.RemoteCallManager.call(this.formActionHandler, {}, {
      'command': 'data'
    }, function (req, data, resp) {
      me.formData = data;
    });

    this.Super('initWidget', arguments);
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileButton', this);
  },

  // ** {{{ initialize() }}} **
  //
  // Creates the forms, fields and buttons.
  initialize: function () {
    if (this.roleForm) {
      return;
    }

    var me = this,
        formLayout, newPasswordField, passwordForm, confirmPasswordField, buttonLayout, currentPasswordField, roleForm, widgetInstance, comboBoxFieldProperties, roleField, orgField, warehouseField, languageField, checkboxFieldProperties, defaultField, clientField, tabSet, pwdButtonLayout, pwdFormLayout, pwdSaveButton, textFieldProperties, passwordFieldProperties, dummyFirstField, dummyLastField;

    OB.Layout.userProfileWidget = this;

    // have a pointer to this instance
    widgetInstance = this;

    // create a default form field types
    comboBoxFieldProperties = {
      errorOrientation: OB.Styles.OBFormField.DefaultComboBox.errorOrientation,
      cellStyle: OB.Styles.OBFormField.DefaultComboBox.cellStyle,
      titleStyle: OB.Styles.OBFormField.DefaultComboBox.titleStyle,
      textBoxStyle: OB.Styles.OBFormField.DefaultComboBox.textBoxStyle,
      pendingTextBoxStyle: OB.Styles.OBFormField.DefaultComboBox.pendingTextBoxStyle,
      controlStyle: OB.Styles.OBFormField.DefaultComboBox.controlStyle,
      width: '*',
      pickListBaseStyle: OB.Styles.OBFormField.DefaultComboBox.pickListBaseStyle,
      pickListTallBaseStyle: OB.Styles.OBFormField.DefaultComboBox.pickListTallBaseStyle,
      pickerIconSrc: OB.Styles.OBFormField.DefaultComboBox.pickerIconSrc,

      height: OB.Styles.OBFormField.DefaultComboBox.height,
      pickerIconWidth: OB.Styles.OBFormField.DefaultComboBox.pickerIconWidth,
      pickListCellHeight: OB.Styles.OBFormField.DefaultComboBox.pickListCellHeight,
      pickListProperties: {
        bodyStyleName: OB.Styles.OBFormField.DefaultComboBox.pickListProperties.bodyStyleName
      },

      // workaround for this issue:
      // https://issues.openbravo.com/view.php?id=18501
      setUpPickList: function () {
        this.Super("setUpPickList", arguments);
        if (this.pickList) {
          this.pickList.setBodyStyleName(this.pickListProperties.bodyStyleName);
        }
      },

      titleOrientation: 'top',
      showFocused: true,
      editorType: 'select',
      selectOnFocus: true,
      addUnknownValues: false,
      allowEmptyValue: false,
      defaultToFirstOption: true,

      // to solve: https://issues.openbravo.com/view.php?id=20067
      // in chrome the order of the valueMap object is not retained
      // the solution is to keep a separate entries array with the
      // records in the correct order, see also the setEntries
      // method
      getClientPickListData: function () {
        if (this.entries) {
          return this.entries;
        }
        return this.Super('getClientPickListData', arguments);
      },

      setEntries: function (entries) {
        var length = entries.length,
            i, id, identifier, valueField = this.getValueFieldName(),
            valueMap = {};
        this.entries = [];
        for (i = 0; i < length; i++) {
          id = entries[i][OB.Constants.ID] || '';
          identifier = entries[i][OB.Constants.IDENTIFIER] || '';
          valueMap[id] = identifier;
          this.entries[i] = {};
          this.entries[i][valueField] = id;
        }
        this.setValueMap(valueMap);
      }
    };

    roleField = isc.addProperties({
      name: 'role',
      title: OB.I18N.getLabel('UINAVBA_Role')
    }, comboBoxFieldProperties);

    orgField = isc.addProperties({
      name: 'organization',
      title: OB.I18N.getLabel('UINAVBA_Organization')
    }, comboBoxFieldProperties);

    warehouseField = isc.addProperties({
      name: 'warehouse',
      title: OB.I18N.getLabel('UINAVBA_Warehouse')
    }, comboBoxFieldProperties);

    languageField = isc.addProperties({
      name: 'language',
      title: OB.I18N.getLabel('UINAVBA_Language')
    }, comboBoxFieldProperties);

    checkboxFieldProperties = {
      cellStyle: OB.Styles.OBFormField.DefaultCheckbox.cellStyle,
      titleStyle: OB.Styles.OBFormField.DefaultCheckbox.titleStyle,
      textBoxStyle: OB.Styles.OBFormField.DefaultCheckbox.textBoxStyle,
      showValueIconOver: OB.Styles.OBFormField.DefaultCheckbox.showValueIconOver,
      showValueIconFocused: OB.Styles.OBFormField.DefaultCheckbox.showValueIconFocused,
      showFocused: OB.Styles.OBFormField.DefaultCheckbox.showFocused,
      defaultValue: OB.Styles.OBFormField.DefaultCheckbox.defaultValue,
      checkedImage: OB.Styles.OBFormField.DefaultCheckbox.checkedImage,
      uncheckedImage: OB.Styles.OBFormField.DefaultCheckbox.uncheckedImage,
      titleOrientation: 'right',
      editorType: 'checkbox'
    };

    defaultField = isc.addProperties({
      name: 'default',
      title: OB.I18N.getLabel('UINAVBA_SetAsDefault')
    }, checkboxFieldProperties);

    textFieldProperties = {
      errorOrientation: OB.Styles.OBFormField.DefaultTextItem.errorOrientation,
      cellStyle: OB.Styles.OBFormField.DefaultTextItem.cellStyle,
      titleStyle: OB.Styles.OBFormField.DefaultTextItem.titleStyle,
      textBoxStyle: OB.Styles.OBFormField.DefaultTextItem.textBoxStyle,
      showFocused: true,
      showDisabled: true,
      disabled: true,
      showIcons: false,
      width: '*',
      titleOrientation: 'top',
      editorType: 'TextItem'
    };

    clientField = isc.addProperties({
      name: 'client',
      title: OB.I18N.getLabel('UINAVBA_Client')
    }, textFieldProperties);

    // create the form for the role information
    roleForm = isc.DynamicForm.create({
      autoFocus: true,
      overflow: 'visible',
      numCols: 1,
      width: '100%',
      titleSuffix: '',
      errorsPreamble: '',
      showInlineErrors: false,
      widgetInstance: me,

      initWidget: function () {
        this.Super('initWidget', arguments);
        this.setInitialData(this.widgetInstance.formData);
      },

      itemKeyPress: function (item, keyName, characterValue) {
        if (keyName === 'Escape') {
          if (isc.OBQuickRun.currentQuickRun) {
            isc.OBQuickRun.currentQuickRun.doHide();
          }
        }

        this.Super('itemKeyPress', arguments);
      },

      localFormData: null,
      reset: function () {
        // note order is important, first order item then do ValueMaps
        // then do setValues
        // this is needed because the select items will reject values
        // if the valuemap is not yet set
        this.setValue('role', this.localFormData.initialValues.role);
        this.setOtherEntries();
        // note, need to make a copy of the initial values
        // otherwise they are updated when the form values change!
        this.setValues(isc.addProperties({}, this.localFormData.initialValues));
        this.setWarehouseValueMap();
        //We set initial values again to set warehouse correctly
        this.setValues(isc.addProperties({}, this.localFormData.initialValues));
        if (this.getItem('warehouse').getClientPickListData().length > 0 && !this.getItem('warehouse').getValue()) {
          this.getItem('warehouse').moveToFirstValue();
        }
      },
      setInitialData: function (data) {
        // order of these statements is important see comments in reset
        // function
        this.localFormData = data;
        this.getItem('language').setEntries(data.language.valueMap);
        this.getItem('role').setEntries(data.role.valueMap);
        this.setValue('role', data.initialValues.role);
        this.setValue('client', data.initialValues.client);
        this.setOtherEntries();
        //First we set initial values, but warehouse will not work
        //as its combo hasn't yet been filled
        this.setValues(isc.addProperties({}, data.initialValues));
        this.setWarehouseValueMap();
        //We set initial values again to set warehouse correctly
        this.setValues(isc.addProperties({}, data.initialValues));
      },
      // updates the dependent combos
      itemChanged: function (item, newValue) {
        this.setOtherEntries();
        if (item.name === 'role') {
          if (this.getItem('organization').getClientPickListData().length > 0) {
            this.getItem('organization').moveToFirstValue();
          }
        }
        this.setWarehouseValueMap();
        if (item.name !== 'warehouse') {
          if (this.getItem('warehouse').getClientPickListData().length > 0) {
            this.getItem('warehouse').moveToFirstValue();
          }
        }
      },
      setOtherEntries: function () {
        var i, role, roleId = this.getValue('role'),
            length = this.localFormData.role.roles.length;
        for (i = 0; i < length; i++) {
          role = this.localFormData.role.roles[i];
          if (role.id === roleId) {
            this.getItem('organization').setEntries(role.organizationValueMap);
            this.setValue('client', role.client);
          }
        }
      },
      setWarehouseValueMap: function () {
        var i, j, warehouseOrg, role, roleId, roleLength, length, orgId = this.getItem('organization').getValue();
        if (!orgId) {
          return;
        }
        roleLength = this.localFormData.role.roles.length;
        roleId = this.getValue('role');
        for (i = 0; i < roleLength; i++) {
          role = this.localFormData.role.roles[i];
          if (role.id === roleId) {
            length = role.warehouseOrgMap.length;
            for (j = 0; j < length; j++) {
              warehouseOrg = role.warehouseOrgMap[j];
              if (warehouseOrg.orgId === orgId) {
                this.getItem('warehouse').setEntries(warehouseOrg.warehouseMap);
              }
            }
          }
        }
      },

      // call the server to save the information
      doSave: function () {
        OB.RemoteCallManager.call(this.widgetInstance.formActionHandler, this.getValues(), {
          'command': 'save'
        }, this.doSaveCallback);
      },

      // and reload
      doSaveCallback: function (rpcResponse, data, rpcRequest) {
        // if not success then an error, can not really occur
        // is handled as an exception is returned anyway
        if (data.result === OB.Constants.SUCCESS) {
          // reload the window to reflect the changed role etc.
          window.location.href = OB.Utilities.getLocationUrlWithoutFragment();
        }
      },

      fields: [roleField, clientField, orgField, warehouseField, languageField, defaultField]
    });

    // create the form layout which contains both the form and the buttons
    formLayout = isc.VStack.create({
      align: 'center',
      overflow: 'visible',
      height: 1,
      width: '100%'
    });
    formLayout.addMembers(roleForm);

    // pointer to the form
    widgetInstance.roleForm = roleForm;

    // create the buttons
    buttonLayout = isc.HStack.create({
      layoutTopMargin: 10,
      membersMargin: 10,
      align: 'center',
      overflow: 'visible',
      height: 1,
      width: '100%'
    });
    buttonLayout.addMembers(isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUIAPP_Apply'),
      click: function () {
        roleForm.doSave();
      }
    }));
    buttonLayout.addMembers(isc.OBFormButton.create({
      title: OB.I18N.getLabel('UINAVBA_Cancel'),
      click: isc.OBQuickRun.hide
    }));
    formLayout.addMembers(buttonLayout);

    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.RoleField', roleForm.getField('role'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.OrgField', roleForm.getField('organization'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.WarehouseField', roleForm.getField('warehouse'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.LanguageField', roleForm.getField('language'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.DefaultField', roleForm.getField('default'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.ClientField', roleForm.getField('client'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.Form', roleForm);
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.SaveButton', buttonLayout.members[0]);
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfileRole.CancelButton', buttonLayout.members[1]);

    // now create the fields for the password form
    passwordFieldProperties = {
      errorOrientation: OB.Styles.OBFormField.DefaultTextItem.errorOrientation,
      cellStyle: OB.Styles.OBFormField.DefaultTextItem.cellStyle,
      titleStyle: OB.Styles.OBFormField.DefaultTextItem.titleStyle,
      textBoxStyle: OB.Styles.OBFormField.DefaultTextItem.textBoxStyle,
      titleOrientation: 'top',
      width: '*',
      showErrorIcon: false,
      showFocused: true,
      required: true,
      selectOnFocus: false,
      editorType: 'PasswordItem'
    };

    currentPasswordField = isc.addProperties({
      name: 'currentPwd',
      title: OB.I18N.getLabel('UINAVBA_CurrentPwd')
    }, passwordFieldProperties);

    newPasswordField = isc.addProperties({
      name: 'newPwd',
      title: OB.I18N.getLabel('UINAVBA_NewPwd')
    }, passwordFieldProperties);

    confirmPasswordField = isc.addProperties({
      name: 'confirmPwd',
      title: OB.I18N.getLabel('UINAVBA_ConfirmPwd')
    }, passwordFieldProperties);

    // create the password form
    passwordForm = isc.DynamicForm.create({
      autoFocus: true,
      overflow: 'visible',
      width: '100%',
      titleSuffix: '</b>',
      titlePrefix: '<b>',
      requiredTitleSuffix: ' *</b>',
      requiredRightTitlePrefix: '<b>* ',
      rightTitlePrefix: '<b>',
      rightTitleSuffix: '</b>',
      numCols: 1,
      errorOrientation: 'right',

      // overridden to pass suppressautofocus to parent
      addFieldErrors: function (fieldName, errors, showErrors) {
        if (!this.errors) {
          this.errors = {};
        }

        this.addValidationError(this.errors, fieldName, errors);

        // Don't bother updating hiddenErrors - this will be updated by 
        // showErrors() / showFieldErrors()
        if (showErrors) {
          this.showFieldErrors(fieldName, true);
        }
      },

      itemKeyPress: function (item, keyName, characterValue) {
        if (keyName === 'Escape') {
          if (isc.OBQuickRun.currentQuickRun) {
            isc.OBQuickRun.currentQuickRun.doHide();
          }
        }

        this.Super('itemKeyPress', arguments);
      },

      // call the server
      formActionHandler: 'org.openbravo.client.application.navigationbarcomponents.UserInfoWidgetActionHandler',
      doSave: function () {
        OB.RemoteCallManager.call(passwordForm.formActionHandler, passwordForm.getValues(), {
          'command': 'changePwd'
        }, passwordForm.doSaveCallback);
      },

      // the callback displays an info dialog and then hides the form
      doSaveCallback: function (rpcResponse, data, rpcRequest) {
        var i, length;
        if (data.result === OB.Constants.SUCCESS) {
          isc.OBQuickRun.hide();
          isc.say(OB.I18N.getLabel('UINAVBA_PasswordChanged'));
        } else {
          if (data.messageCode) {
            isc.showPrompt(OB.I18N.getLabel(data.message));
          }
          if (data.fields) {
            length = data.fields.length;
            for (i = 0; i < length; i++) {
              var field = data.fields[i];
              passwordForm.addFieldErrors(field.field, OB.I18N.getLabel(field.messageCode), true);
            }
          }
        }
      },

      // enable/disable the save button, show an error if the two values
      // are unequal
      itemChanged: function (item, newValue) {
        var currentPwd = this.getValue('currentPwd');
        var newPwd = this.getValue('newPwd');
        var confirmPwd = this.getValue('confirmPwd');
        if (OB.Utilities.isNonEmptyString(currentPwd) && OB.Utilities.isNonEmptyString(newPwd) && OB.Utilities.isNonEmptyString(confirmPwd) && OB.Utilities.areEqualWithTrim(newPwd, confirmPwd)) {
          if (pwdSaveButton.isDisabled()) {
            pwdSaveButton.enable();
          }
          passwordForm.clearFieldErrors('confirmPwd', true);
        } else if (pwdSaveButton.isEnabled()) {
          pwdSaveButton.disable();
        }
        if (item.name === 'newPwd' || item.name === 'confirmPwd') {
          if (!OB.Utilities.areEqualWithTrim(newPwd, confirmPwd)) {
            passwordForm.addFieldErrors('confirmPwd', OB.I18N.getLabel('UINAVBA_UnequalPwd'), true);
          }
        }
        passwordForm.focusInItem(item.name);
      },
      fields: [currentPasswordField, newPasswordField, confirmPasswordField]
    });

    // create the layout that holds the form and the buttons
    pwdFormLayout = isc.VStack.create({
      overflow: 'visible',
      height: 1,
      width: '100%',
      align: 'center'
    });
    pwdFormLayout.addMembers(passwordForm);

    widgetInstance.passwordForm = passwordForm;

    pwdSaveButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUIAPP_Apply'),
      action: passwordForm.doSave,
      disabled: true
    });

    pwdButtonLayout = isc.HStack.create({
      layoutTopMargin: 10,
      membersMargin: 10,
      width: '100%',
      align: 'center',
      overflow: 'visible',
      height: 1
    });
    pwdButtonLayout.addMembers(pwdSaveButton);
    pwdButtonLayout.addMembers(isc.OBFormButton.create({
      title: OB.I18N.getLabel('UINAVBA_Cancel'),
      click: isc.OBQuickRun.hide
    }));
    pwdFormLayout.addMembers(pwdButtonLayout);

    // and create the tabset
    tabSet = isc.TabSet.create({
      paneContainerOverflow: 'visible',
      overflow: 'visible',
      useSimpleTabs: true,
      simpleTabBaseStyle: 'OBNavBarComponentFormTabButton',
      paneContainerClassName: 'OBNavBarComponentFormTabSetContainer',
      tabBarProperties: {
        baseLineThickness: 0
      },
      width: 250,
      tabs: [{
        title: OB.I18N.getLabel('UINAVBA_Profile'),
        pane: formLayout,
        overflow: 'visible'
      }, {
        title: OB.I18N.getLabel('UINAVBA_ChangePassword'),
        pane: pwdFormLayout,
        overflow: 'visible'
      }]
    });
    widgetInstance.tabSet = tabSet;

    dummyFirstField = isc.OBFocusButton.create({
      getFocusTarget: function () {
        var tabSet = this.parentElement.members[1];
        var selectedTabNumber = tabSet.getSelectedTabNumber();
        var length1 = tabSet.getTabPane(selectedTabNumber).members.length - 1;
        var length2 = tabSet.getTabPane(selectedTabNumber).members[length1].members.length - 1;
        return tabSet.getTabPane(selectedTabNumber).members[length1].members[length2];
      }
    });

    dummyLastField = isc.OBFocusButton.create({
      getFocusTarget: function () {
        var tabSet = this.parentElement.members[1];
        var selectedTabNumber = tabSet.getSelectedTabNumber();
        return tabSet.tabBar.members[selectedTabNumber];
      }
    });

    this.members = [dummyFirstField, tabSet, dummyLastField];

    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfile.Tabset', tabSet);
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfilePassword.SaveButton', pwdSaveButton);
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfilePassword.CancelButton', pwdButtonLayout.members[1]);
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfilePassword.CurrentPasswordField', passwordForm.getField('currentPwd'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfilePassword.NewPasswordField', passwordForm.getField('newPwd'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfilePassword.ConfirmPasswordField', passwordForm.getField('confirmPwd'));
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.UserProfilePassword.Form', passwordForm);

    this.resetLayout();
    this.computeSetContent();
  }
});