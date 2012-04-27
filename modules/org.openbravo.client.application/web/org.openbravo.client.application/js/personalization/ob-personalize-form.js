/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
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

// ** {{{OBPersonalizeFormLayout}}} **
// Builds the whole layout which contains the form layout functionality.
// The form layout functionality consists of three parts:
// 1) a pane which shows the fields in a tree structure. This makes use
// of the TreeGrid defined in ob-personalization-treegrid
// 2) a properties pane to edit layout properties for a certain field
// 3) a pane which contains the form preview
//
// In addition there is a toolbar to do save, delete and undo.
//
// The form personalizer is called from a window itself or from the 
// Window Personalization grid for a selected record.
// 
// An important concept to understand is personalizationData.
// This is the datastructure used in the tree on the left of the form
// personalizer. It contains the fields of the form but also field groups,
// status bar field and the status bar group. 
// This datastructure is stored in the Window Personalization
// table. It is read from the database or can be constructed from 
// an existing form. The personalization data has a structure like this:
// {
//    personalizationId: null,
//    canDelete: true,
//    form: {
//      fields: []
//    }
// }
// 
// This structure is like this to cater for future addition of grid (and
// other) personalization data in the same record.
// 
// The tree shown on the left has 2 types of parts, the first part is
// the status bar group the second part is the form group which holds
// the form items. fields can be moved from one group to another.
//
// Form personalization: when a standard window is created it calls the 
// server to get (the WindowSettingsActionHandler) to retrieve the 
// personalized data for the tabs of the window. The standard window 
// then calls the personalizeWindow method in the ob-personalization.js
// file.
//
// After MP2 a new function has been delivered to store the complete view
// state of a complete window. See the ob-manage-views.js file for a detailed
// description. The form layout functionality described here is integrated
// with this new functionality.
//
isc.ClassFactory.defineClass('OBPersonalizeFormLayout', isc.VLayout);

isc.OBPersonalizeFormLayout.addProperties({

  // the datastructure with personalization information
  personalizationData: null,

  // identifiers displayed in the statusbar of the form personalizer
  tabIdentifier: null,
  clientIdentifier: null,
  orgIdentifier: null,
  roleIdentifier: null,
  userIdentifier: null,

  // are used to set a personalization record on the most
  // detailed user level, is not used if personalizationId
  // is set
  clientId: null,
  orgId: null,
  roleId: null,
  userId: null,

  // maintain the state of the buttons in the toolbar
  isNew: false,
  isChanged: false,
  isSaved: false,

  // should the caller be refreshed or not
  hasBeenSaved: false,

  // the form instance shown to the user
  previewForm: null,

  // retrieved from the server
  previewFormProperties: null,

  initWidget: function () {

    this.initializing = true;

    this.createAddToolbar();
    this.createAddStatusbar();
    this.createAddMainLayout();
    this.Super('initWidget', arguments);
  },

  // everything except the toolbar and statusbar in the top, some inner 
  // parts are created later (see buildPreviewForm)
  createAddMainLayout: function () {
    var me = this,
        fieldsTabSet, leftLayout, mainLayout = isc.VLayout.create({}, OB.Styles.Personalization.MainLayout);

    this.fieldsLayout = isc.Layout.create({
      height: '100%'
    }, OB.Styles.Personalization.FieldsLayout);

    fieldsTabSet = isc.OBTabSet.create({
      height: '*',

      initWidget: function () {
        // copy the tabBarProperties as it is coming from
        // OB.Styles.Personalization.TabSet which is also used
        // by the other tabsets
        this.tabBarProperties = isc.addProperties({}, this.tabBarProperties);
        this.tabBarProperties.tabSet = this;
        this.tabBarProperties.itemClick = function (item, itemNum) {
          me.propertiesTabSet.toggleVisualState();
        };
        this.Super('initWidget', arguments);
      }
    }, OB.Styles.Personalization.TabSet);

    fieldsTabSet.addTab({
      title: OB.I18N.getLabel('OBUIAPP_Personalization_HeaderFields'),
      pane: this.fieldsLayout
    });

    this.previewTabSet = isc.OBTabSet.create(OB.Styles.Personalization.TabSet);
    this.previewTabSet.addTab({
      title: OB.I18N.getLabel('OBUIAPP_Personalization_HeaderPreview')
    });

    leftLayout = isc.VLayout.create({
      height: '100%'
    }, OB.Styles.Personalization.FormPersonalizerLeftPane);
    leftLayout.addMember(fieldsTabSet);
    leftLayout.addMember(this.createPropertiesLayout());

    this.managementLayout = isc.HLayout.create({
      height: '100%',
      width: '100%'
    }, OB.Styles.Personalization.ManagementLayout);
    this.managementLayout.addMember(leftLayout);
    this.managementLayout.addMember(this.previewTabSet);
    mainLayout.addMember(this.managementLayout);
    this.mainLayout = mainLayout;
    this.addMember(mainLayout);
  },

  // creates the properties layout which contains the form
  // with a few buttons
  createPropertiesLayout: function () {
    var NumericField, CheckboxField, propertiesLayout = isc.Layout.create(OB.Styles.Personalization.PropertiesLayout);

    // a backpointer
    propertiesLayout.personalizeForm = this;

    CheckboxField = function (props) {
      if (props) {
        isc.addProperties(this, props);
      }
    };
    CheckboxField.prototype = {
      alwaysTakeSpace: false,
      editorType: 'OBCheckboxItem',
      labelAsTitle: true,

      // HACK: when a checkbox does not have focus and you click
      // the first click is lost, does not happen in normal OBViewForm
      // TODO: research why this happens
      handleCellClick: function () {
        this.fromCellClick = true;
        var ret = this.Super('handleClick', arguments);
        delete this.fromCellClick;
        return ret;
      },
      handleClick: function () {
        if (!this.fromCellClick) {
          // cellclick will also be called
          return true;
        }
        return this.Super('handleClick', arguments);
      }
    };

    NumericField = function (props) {
      if (props) {
        isc.addProperties(this, props);
      }
    };
    NumericField.prototype = {
      showFocused: true,
      alwaysTakeSpace: false,
      required: true,
      validateOnExit: true,
      showIcons: true,
      width: 75,
      titleOrientation: 'top',
      titleSuffix: '</b>',
      titlePrefix: '<b>',
      requiredTitleSuffix: ' *</b>',
      requiredRightTitlePrefix: '<b>* ',
      rightTitlePrefix: '<b>',
      rightTitleSuffix: '</b>',
      keyPressFilter: '[1-9]',
      editorType: 'OBSpinnerItem'
    };

    propertiesLayout.formLayout = isc.VStack.create({
      align: 'center',
      overflow: 'visible',
      height: 1,
      visible: false,
      width: '100%'
    });

    propertiesLayout.formLayout.form = isc.DynamicForm.create({
      personalizeForm: this,
      overflow: 'visible',
      numCols: 2,
      width: '100%',

      titleSuffix: '</b>',
      titlePrefix: '<b>',
      requiredTitleSuffix: ' *</b>',
      requiredRightTitlePrefix: '<b>* ',
      rightTitlePrefix: '<b>',
      rightTitleSuffix: '</b>',

      errorsPreamble: '',
      showErrorIcons: false,
      showErrorStyle: true,
      showInlineErrors: true,

      fields: [
      new NumericField({
        name: 'colSpan',
        keyPressFilter: '[1-4]',
        min: 1,
        max: 4,
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Colspan')
      }), new NumericField({
        name: 'rowSpan',
        keyPressFilter: '[1-9]',
        min: 1,
        max: 9,
        required: true,
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Rowspan')
      }), new CheckboxField({
        name: 'startRow',
        startRow: true,
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Startrow')
      }), new CheckboxField({
        name: 'displayed',
        startRow: true,
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Displayed')
      }), new CheckboxField({
        name: 'firstFocus',
        startRow: true,
        title: OB.I18N.getLabel('OBUIAPP_Personalization_FirstFocus')
      })],

      // is called when a field in the tree is clicked
      setRecord: function (record) {
        var fld, i = 0,
            length = this.getFields().length;

        this.record = record;

        for (i = 0; i < length; i++) {
          fld = this.getFields()[i];
          // the field has the opposite meaning of the form
          if (fld.name === 'displayed') {
            this.setValue('displayed', !record.hiddenInForm);
          } else {
            this.setValue(fld.name, record[fld.name]);
          }
        }

        // hide some fields, static status bar fields only need the
        // hidden fields and not the other ones
        if (record.isStatusBarField) {
          this.hideItem('colSpan');
          this.hideItem('rowSpan');
          this.hideItem('firstFocus');
          this.hideItem('startRow');
        } else {
          this.showItem('colSpan');
          this.showItem('rowSpan');
          this.showItem('firstFocus');
          this.showItem('startRow');
        }

        if (!record.wasOnStatusBarField && !record.isStatusBarField && record.required && !record.hasDefaultValue) {
          this.hideItem('displayed');
        } else {
          this.showItem('displayed');
        }

        this.rememberValues();
      },

      // store the values in the record
      doSave: function () {
        var i, allNodes, length;

        // don't save if there are errors
        // could be an idea to disable the save button
        this.validate();
        if (this.hasErrors()) {
          return;
        }

        // only one field may have first focus
        // first get rid of all first focus if it was set now
        if (this.getValue('firstFocus')) {
          allNodes = this.personalizeForm.fieldsTreeGrid.data.getAllNodes();
          length = allNodes.length;
          for (i = 0; i < length; i++) {
            if (allNodes[i].firstFocus) {
              allNodes[i].firstFocus = false;
            }
          }
        }

        // now it will be set, maximum one field will have 
        // the focus now
        this.record.hiddenInForm = !this.getValue('displayed');
        this.record.startRow = this.getValue('startRow');
        this.record.colSpan = this.getValue('colSpan');
        this.record.rowSpan = this.getValue('rowSpan');
        this.record.firstFocus = this.getValue('firstFocus');

        this.rememberValues();
        this.focus();

        // items may have been hidden, which changes their colour
        // so rebuild the tree
        this.personalizeForm.fieldsTreeGrid.markForRedraw();

        // this will reset everything
        this.personalizeForm.changed();
      },

      doCancel: function () {
        this.reset();
        this.focus();
      },

      // called when a field in the form changes
      // enable the apply/cancel buttons
      itemChanged: function (item, newValue) {
        this.doSave();
      }
    });

    propertiesLayout.formLayout.addMembers(propertiesLayout.formLayout.form);

    // the empty message is shown when no field is selected on the left
    propertiesLayout.emptyMessage = isc.Layout.create({
      margin: 5,
      members: [
      isc.Label.create({
        width: '100%',
        height: 1,
        overflow: 'visible',
        contents: OB.I18N.getLabel('OBUIAPP_Personalization_PropertiesFormEmptyMessage')
      })]
    });

    propertiesLayout.addMember(propertiesLayout.formLayout);
    propertiesLayout.addMember(propertiesLayout.emptyMessage);
    propertiesLayout.hideMember(propertiesLayout.formLayout);
    propertiesLayout.showMember(propertiesLayout.emptyMessage);

    this.propertiesLayout = propertiesLayout;

    // put it all in a tabset...    
    this.propertiesTabSet = isc.OBTabSet.create(OB.Styles.Personalization.TabSet, {
      height: OB.Styles.Personalization.PropertiesTabSet.expandedHeight,
      expanded: true,

      toggleVisualState: function () {
        if (this.expanded) {
          this.setHeight(OB.Styles.Personalization.PropertiesTabSet.collapsedHeight);
          this.expanded = false;
        } else {
          this.setHeight(OB.Styles.Personalization.PropertiesTabSet.expandedHeight);
          this.expanded = true;
        }
      },

      initWidget: function () {
        this.tabBarProperties.tabSet = this;
        this.tabBarProperties.itemClick = function (item, itemNum) {
          this.tabSet.toggleVisualState();
        };
        this.Super('initWidget', arguments);
      }
    });
    this.propertiesTabSet.addTab({
      title: OB.I18N.getLabel('OBUIAPP_Personalization_HeaderProperties'),
      pane: propertiesLayout
    });

    // is called when a field in the tree is selected or unselected
    propertiesLayout.updatePropertiesDisplay = function (record) {
      var newRecord;
      if (!record) {
        this.hideMember(this.formLayout);
        this.emptyMessage.show();
        this.propertiesTabSet.setTabTitle(this.propertiesTabSet.getTab(0), OB.I18N.getLabel('OBUIAPP_Personalization_HeaderProperties'));
      } else {
        this.propertiesTabSet.setTabTitle(this.propertiesTabSet.getTab(0), record.title);
        this.formLayout.form.setRecord(record);
        this.hideMember(this.emptyMessage);
        this.showMember(propertiesLayout.formLayout);
        // set focus to the first one if we get focus
        this.formLayout.form.setFocusItem(this.formLayout.form.getFields()[0]);
      }
    };
    propertiesLayout.propertiesTabSet = this.propertiesTabSet;

    return this.propertiesTabSet;
  },

  // the status bar shows information about the personalization record
  // and it has the close button
  createAddStatusbar: function () {
    var owner = this;
    this.statusBar = isc.OBStatusBar.create({
      view: this,
      buttonBarProperties: OB.Styles.Personalization.buttonBarProperties,

      // add the close button
      addCreateButtons: function () {
        this.buttonBar.setWidth(1);
        this.buttonBar.setOverflow('visible');
        this.buttonBar.defaultLayoutAlign = 'center';
        var closeButton = isc.OBStatusBarIconButton.create({
          view: this.view,
          buttonType: 'close',
          keyboardShortcutId: 'StatusBar_Close',
          prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Statusbar_Close'),
          action: function () {
            var clz = (owner.getWindow() ? owner.getWindow().getClass() : null);
            if (!clz) {
              owner.doClose();
            } else if (!clz.autoSave) {
              owner.doClose();
            } else if (clz.showAutoSaveConfirmation) {
              owner.doClose();
            } else {
              owner.saveAndClose();
            }
          }
        }, OB.Styles.Personalization.closeButtonProperties);
        this.buttonBar.addMembers([closeButton]);
      }
    });
    this.addMember(this.statusBar);
  },

  // the toolbar shows the save, delete and undo button
  createAddToolbar: function () {
    var saveButtonProperties, saveCloseButtonProperties, deleteButtonProperties, cancelButtonProperties, restoreButtonProperties, restoreLayout;

    saveButtonProperties = {
      action: function () {
        this.view.save();
      },
      disabled: true,
      buttonType: 'save',
      prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_Save'),
      updateState: function () {
        this.setDisabled(this.view.hasNotChanged());
      },
      keyboardShortcutId: 'ToolBar_Save'
    };

    saveCloseButtonProperties = {
      action: function () {
        this.view.saveAndClose();
      },
      saveDisabled: true,
      buttonType: 'savecloseX',
      prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_SaveClose'),
      updateState: function () {
        this.saveDisabled = this.view.hasNotChanged();

        if (this.saveDisabled) {
          this.buttonType = 'savecloseX';
          this.prompt = OB.I18N.getLabel('OBUIAPP_Personalization_Statusbar_Close');
        } else {
          this.buttonType = 'saveclose';
          this.prompt = OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_SaveClose');
        }
        this.resetBaseStyle();
      },
      keyboardShortcutId: 'ToolBar_SaveClose'
    };

    deleteButtonProperties = {
      action: function () {
        this.view.deletePersonalization();
      },
      disabled: true,
      buttonType: 'eliminate',
      prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_Delete'),
      updateState: function () {
        // never allow delete when opened from the maintenance window
        this.setDisabled(this.openedFromMaintenanceWindow || !this.view.form.view.getFormPersonalization(false) || !this.view.form.view.getFormPersonalization(false).canDelete);
      },
      keyboardShortcutId: 'ToolBar_Eliminate'
    };

    cancelButtonProperties = {
      action: function () {
        this.view.cancel();
      },
      disabled: true,
      buttonType: 'undo',
      prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_CancelEdit'),
      updateState: function () {
        this.setDisabled(this.view.hasNotChanged());
      },
      keyboardShortcutId: 'ToolBar_Undo'
    };

    restoreButtonProperties = {
      action: function () {
        var i, standardWindow = this.view.getStandardWindow(),
            viewDefinitions = standardWindow.getClass().originalView.viewDefinition,
            length = standardWindow.views.length,
            view, viewTabDefinition;
        for (i = 0; i < length; i++) {
          view = standardWindow.views[i];
          if (view.tabId !== this.view.tabId) {
            continue;
          }
          viewTabDefinition = viewDefinitions[view.tabId];

          this.view.initializing = true;

          this.view.destroyAndRemoveMembers(this.view.mainLayout);
          this.view.mainLayout = null;
          this.view.createAddMainLayout();

          this.view.buildFieldsTreeGrid(viewTabDefinition);
          this.view.buildPreviewForm();
          this.view.fieldsTreeGrid.openFolders();

          delete this.view.initializing;
          this.view.changed();
        }
      },
      title: OB.I18N.getLabel('OBUIAPP_RestoreDefaults'),
      updateState: function () {
        //        this.setDisabled(this.view.hasNotChanged());
      }
    };

    this.toolBar = isc.OBToolbar.create({
      view: this,
      leftMembers: [isc.OBToolbarIconButton.create(saveButtonProperties), isc.OBToolbarIconButton.create(saveCloseButtonProperties), isc.OBToolbarIconButton.create(cancelButtonProperties), isc.OBToolbarIconButton.create(deleteButtonProperties)],
      rightMembers: [isc.OBToolbarTextButton.create(restoreButtonProperties)],
      refreshCustomButtons: function () {
        this.rightMembers[0].updateState();
      }
    });
    this.addMember(this.toolBar);
  },

  // save the new form layout to the server and updates the preview form
  save: function (callback) {
    var params, me = this,
        newDataFields, formPers = this.form.view.getFormPersonalization();

    // if there is a personalization id then use that
    // this ensures that a specific record will be updated
    // on the server
    // note the target parameter is form, in other implementations
    // it can be grid. The reason that not the whole structure is send
    // is that the call should only change the target itself (the form
    // personalization data) and not the other parts
    if (formPers && formPers.personalizationId) {
      params = {
        action: 'store',
        target: 'form',
        personalizationId: formPers.personalizationId
      };

    } else {
      // this case is used if there is no personalization record
      // it will be created at the most detailed level: client, org, role and user
      params = {
        action: 'store',
        target: 'form',
        clientId: this.clientId,
        orgId: this.orgId,
        roleId: this.roleId,
        userId: this.userId,
        tabId: this.tabId
      };
    }

    newDataFields = this.getPersonalizationFields();

    // store the data, the reply can contain an id and also if the current
    // user may delete the record, this is allowed if the record is on
    // user level
    OB.RemoteCallManager.call('org.openbravo.client.application.personalization.PersonalizationActionHandler', this.getPersonalizationFields(), params, function (resp, data, req) {
      var personalization;

      // if there is no personalization data then create it
      if (!me.personalizationData) {
        me.personalizationData = {};
      }

      // as the user can save, the user can also delete it
      me.personalizationData.canDelete = true;

      if (data && data.personalizationId) {
        me.personalizationData.personalizationId = data.personalizationId;
      }
      if (!me.personalizationData.form) {
        me.personalizationData.form = {};
      }
      // overwrite what we have
      me.personalizationData.form = newDataFields;

      me.form.view.standardWindow.updateFormPersonalization(me.form.view, me.personalizationData);

      me.initializing = true;
      me.isNew = false;
      me.isSaved = true;
      me.isChanged = false;
      me.hasBeenSaved = true;
      me.setStatusBarInformation();

      // update the save, delete, undo buttons 
      // delete can get enabled if canDelete was set to true
      me.toolBar.updateButtonState();

      delete me.initializing;
      if (callback) {
        callback();
      }
    });
  },

  // called when the delete button is called
  deletePersonalization: function (confirmed) {
    var me = this,
        callback;

    // only delete if we have a personalization id
    // this should always be the case
    if (!this.personalizationData.personalizationId) {
      return;
    }

    if (!confirmed) {
      callback = function (ok) {
        if (ok) {
          me.deletePersonalization(true);
        }
      };

      isc.ask(OB.I18N.getLabel('OBUIAPP_Personalization_ConfirmDelete'), callback);
      return;
    }

    OB.RemoteCallManager.call('org.openbravo.client.application.personalization.PersonalizationActionHandler', {}, {
      personalizationId: this.personalizationData.personalizationId,
      action: 'delete'
    }, function (resp, data, req) {
      var personalization;

      me.hasBeenDeleted = true;
      // close when returned
      me.doClose(true);

      personalization = me.getStandardWindow().getClass().personalization;
      personalization[me.tabId] = null;
    });
  },

  // the undo action, resets everything to the loaded, last-saved state
  cancel: function (confirmed) {
    var me = this,
        callback;
    if (!confirmed) {
      callback = function (ok) {
        if (ok) {
          me.cancel(true);
        }
      };

      isc.ask(OB.I18N.getLabel('OBUIAPP_Personalization_ConfirmCancel'), callback);
      return;
    }

    this.initializing = true;
    this.isChanged = false;
    this.isSaved = false;
    this.isNew = !this.personalizationData.personalizationId;

    this.destroyAndRemoveMembers(this.mainLayout);
    this.mainLayout = null;
    this.createAddMainLayout();

    this.buildFieldsTreeGrid();
    this.buildPreviewForm();
    this.setStatusBarInformation();
    this.fieldsTreeGrid.openFolders();

    delete this.initializing;
  },

  // shows the settings in the preview form
  refresh: function () {
    this.buildPreviewForm();
  },

  // called when something changes in properties, ordering of fields
  // field in a different group etc.
  // will set the changed state which enables buttons
  changed: function () {
    // nothing to do when we are building everything
    // then changed is fired a few times
    if (this.initializing) {
      return;
    }
    this.isChanged = true;
    this.isSaved = false;
    this.setStatusBarInformation();
    this.buildPreviewForm();
  },

  // used by buttons to check if the state has changed
  hasNotChanged: function () {
    return !this.isChanged;
  },

  // creates the preview form and displays it
  buildPreviewForm: function () {
    var statusBar, currentPane, i, fld, itemClick, me = this;

    this.formLayout = isc.VLayout.create({
      height: '100%',
      width: '100%'
    }, OB.Styles.Personalization.Preview);

    // add a status bar to the formlayout
    statusBar = isc.OBStatusBar.create({
      addCreateButtons: function () {}
    });
    this.formLayout.addMember(statusBar);

    // create the form and add it to the formLayout
    this.previewForm = isc.OBViewForm.create(isc.clone(OB.ViewFormProperties), this.previewFormProperties, {
      preventAllEvents: true,
      statusBar: statusBar,
      personalizeForm: this,
      isPreviewForm: true,
      autoFocus: false,

      // overridden to prevent js errors when switching views
      visibilityChanged: function () {},

      titleHoverHTML: function (item) {
        return this.personalizeForm.getHoverHTML(null, item);
      },

      itemHoverHTML: function (item) {
        return this.personalizeForm.getHoverHTML(null, item);
      },

      // overridden to always show a statusbar field with some spaces
      // even if the status bar field does not have a value (which it
      // does not have in the form preview)
      getStatusBarFields: function () {
        var statusBarFields = [
          [],
          []
        ],
            i, item, value, tmpValue, length = this.statusBarFields.length;

        for (i = 0; i < length; i++) {
          item = this.getItem(this.statusBarFields[i]);
          statusBarFields[0].push(item.getTitle());
          statusBarFields[1].push('&nbsp;&nbsp&nbsp;');
        }
        return statusBarFields;
      }

    });

    itemClick = function (item) {
      // disabled clicking in the form itself as multiple things need to be 
      // solved:
      // - the cursor needs to become a pointer
      // - when the field in a collapsed group in the tree (on the left)
      //    then the group has to expand automatically
      // - when the field is not in the viewport on the left then 
      //    it needs to be scrolled there
      // - we also need to support clicking in the status bar
      //      if (item.parentItem) {
      //        me.doHandlePreviewFormItemClick(item.parentItem);
      //      } else {
      //        me.doHandlePreviewFormItemClick(item);
      //      }
    };

    var persFields = this.getPersonalizationFields(),
        length;
    if (persFields) {
      OB.Personalization.personalizeForm({
        form: persFields
      }, this.previewForm);
    }

    // expand by default
    length = this.previewForm.getFields().length;
    for (i = 0; i < length; i++) {
      fld = this.previewForm.getFields()[i];

      fld.showFocused = false;

      // the personalizable is set in freemarker templates, for example
      // the audit and notes section are not personalizable and not shown
      // in the form preview
      if (fld.personalizable) {
        // always expand section items
        if (fld.sectionExpanded) {
          fld.expandSection();
        } else {
          // replace some methods so that clicking a field in the form
          // will select it on the left
          fld.handleClick = itemClick;
          fld.iconClick = itemClick;
          fld.handleTitleClick = itemClick;
          fld.linkButtonClick = itemClick;
        }
      }
    }

    this.formLayout.addMember(this.previewForm);

    if (this.previewTabSet.getTab(0).pane) {
      currentPane = this.previewTabSet.getTab(0).pane;
    }
    this.previewTabSet.updateTab(this.previewTabSet.getTab(0), this.formLayout);
    if (currentPane) {
      currentPane.destroy();
    }
  },

  buildFormAndTree: function () {
    var computedPersonalizationData;

    this.buildPreviewForm();

    // if no personalization data then we need to compute it from the form
    // the personalization data can also be set directly (when called from 
    // the maintenance window)
    if (!this.personalizationData || !this.personalizationData.form) {
      if (this.form) {
        computedPersonalizationData = OB.Personalization.getPersonalizationDataFromForm(this.form);
      } else {
        // create from the previewForm
        computedPersonalizationData = OB.Personalization.getPersonalizationDataFromForm(this.previewForm);
      }
      if (!this.personalizationData) {
        this.personalizationData = {};
      }

      // and copy over what got computed
      isc.addProperties(this.personalizationData, computedPersonalizationData);
    }

    // personalize the preview form, this will remove any non-personalized
    // fields, which are also present in the preview form original field
    // list. Maybe in the future we can personalize these fields also
    OB.Personalization.personalizeForm(this.personalizationData, this.previewForm);

    // new if no id
    this.isNew = !this.personalizationData.personalizationId;

    // handle changes in the AD, needs to be done before building the tree
    OB.Personalization.updatePersonalizationDataFromFields(this.personalizationData.form.fields, this.previewForm.getFields(), this.previewForm.statusBarFields || []);

    this.buildFieldsTreeGrid();

    this.setStatusBarInformation();
    this.fieldsTreeGrid.openFolders();
  },

  buildFieldsTreeGrid: function (personalizationData) {
    var i, prop, fld, length;

    personalizationData = personalizationData || this.personalizationData;

    this.fieldsLayout.destroyAndRemoveMembers(this.fieldsLayout.getMembers());
    if (this.fieldsTreeGrid) {
      this.fieldsTreeGrid.destroy();
    }

    // the tree will add properties to the objects as fieldData
    // when retrieving the data from the tree (getPersonalizationFields)
    // we only want these properties and not the original ones.
    // see the function getPersonalizationFields
    // add some default fields
    this.personalizationDataProperties = ['isStatusBarField', 'displayed', 'isSection', 'parentName', 'title', 'hiddenInForm', 'colSpan', 'rowSpan', 'required', 'sectionExpanded', 'startRow', 'name', 'hasDisplayLogic'];
    length = personalizationData.form.fields.length;
    for (i = 0; i < length; i++) {
      fld = personalizationData.form.fields[i];
      for (prop in fld) {
        if (fld.hasOwnProperty(prop) && !this.personalizationDataProperties.contains(prop)) {
          this.personalizationDataProperties.push(prop);
        }
      }
    }

    // create the tree on the left, the tree only wants the fields
    // and nothing else
    this.fieldsTreeGrid = isc.OBPersonalizationTreeGrid.create({
      // make a clone so that the original personalization data is not
      // updated, when doing cancel, the original is restored
      fieldData: isc.shallowClone(personalizationData.form.fields),
      personalizeForm: this,
      selectionUpdated: function (record, recordList) {
        this.personalizeForm.selectionUpdated(record, recordList);
      }
    });

    this.fieldsLayout.addMember(this.fieldsTreeGrid);

  },

  // is called when something is clicked in the form
  doHandlePreviewFormItemClick: function (item) {
    // select the node in the tree 
    var treeNode = this.fieldsTreeGrid.data.find('name', item.name);
    this.fieldsTreeGrid.deselectAllRecords();
    this.fieldsTreeGrid.selectRecord(treeNode);
  },

  // is called when the selection changes in the tree
  selectionUpdated: function (record, recordList) {
    if (record && !record.isSection && recordList.length === 1) {
      this.propertiesLayout.updatePropertiesDisplay(record);
    } else {
      this.propertiesLayout.updatePropertiesDisplay(null);
    }
  },

  saveAndClose: function () {
    var view = this;
    if (this.hasNotChanged()) {
      view.doClose(true);
    } else {
      view.save(function () {
        view.doClose(true);
      });
    }
  },

  getStandardWindow: function () {
    if (this.openedFromMaintenanceWindow) {
      return this.maintenanceView.standardWindow;
    } else {
      return this.form.view.standardWindow;
    }
  },

  // close the form personalizer, refresh the existing form so that 
  // the changes are shown immediately, or if called from the 
  // maintenance window refresh the record there
  doClose: function (confirmed) {
    var callback, me = this,
        window;

    // ask for confirmation
    if (this.isChanged && !confirmed) {
      callback = function (ok) {
        if (ok) {
          // do it with a small delay so that any mouse events are processed
          // by the button itself and not by the standard view below it
          me.delayCall('doClose', [true], 100);
        }
      };

      isc.ask(OB.I18N.getLabel('OBUIAPP_Personalization_ConfirmClose'), callback);
      return;
    }

    if (this.openedFromMaintenanceWindow) {
      window = this.maintenanceView.standardWindow;
    } else if (this.openedInForm) {
      if (this.hasBeenSaved || this.hasBeenDeleted) {
        // update the form in the view
        OB.Personalization.personalizeForm(me.personalizationData, this.form.view.viewForm);
      }
      window = this.form.view.standardWindow;
    }
    window.destroyAndRemoveMembers(this);

    // restores the tabtitle
    window.view.updateTabTitle();

    window.toolBarLayout.show();
    window.view.show();

    if (this.openedFromMaintenanceWindow) {
      this.maintenanceView.refresh();
    }
  },

  getWindow: function () {
    if (this.openedFromMaintenanceWindow) {
      return this.maintenanceView.standardWindow;
    } else if (this.openedInForm) {
      return this.form.view.standardWindow;
    }
    return null;
  },

  // called by the buttons in the toolbar of the standard maintenance form/grid
  doOpen: function (retrievedInitialData) {
    var me = this,
        window, i, j, persField, fld, tabSet, tab;

    // first get the preview form data, continue after receiving it
    if (!retrievedInitialData) {
      OB.RemoteCallManager.call('org.openbravo.client.application.personalization.PersonalizationActionHandler', {}, {
        action: 'getFormDefinition',
        tabId: this.tabId
      }, function (resp, data, req) {
        me.previewFormProperties = data;

        // copy some stuff
        me.previewFormProperties._originalFields = isc.clone(me.previewFormProperties.fields);

        me.doOpen(true);
      });
      return;
    }

    this.buildFormAndTree();

    // depending on how we opened set the information here
    if (this.openedFromMaintenanceWindow) {
      window = this.maintenanceView.standardWindow;
    } else if (this.openedInForm) {
      // opened directly from the actual form itself
      // always work on user level then
      window = this.form.view.standardWindow;

      this.roleId = OB.User.roleId;
      this.clientId = OB.User.clientId;
      this.orgId = OB.User.organizationId;
      this.userId = OB.User.id;
    }

    // hide the part from which we were opened 
    window.toolBarLayout.hide();
    window.view.hide();
    window.addMember(this);

    // change the tabtitle
    tabSet = OB.MainView.TabSet;
    tab = OB.MainView.TabSet.getTab(window.view.viewTabId);
    tabSet.setTabTitle(tab, OB.I18N.getLabel('OBUIAPP_Personalize_TitlePrefix', [this.tabIdentifier]));
    delete this.initializing;
  },

  // is called when hovering over tree fields and also 
  // when hovering over items in the preview form
  getHoverHTML: function (record, item) {
    if ((record && record.hasDisplayLogic) || (item && item.showIf)) {
      return OB.I18N.getLabel('OBUIAPP_Personalization_DisplayLogicPrompt');
    }
    // TODO: show information about the item being hovered...
    return null;
    //    return title + '<br/>' + 'give me more!';
  },

  // reads the data from the tree grid and returns it in the expected
  // format. Note may return null during initialization
  getPersonalizationFields: function () {
    var i, record, result = [],
        node, nodes, value, j, undef, length;
    if (!this.fieldsTreeGrid || !this.fieldsTreeGrid.data) {
      return null;
    }
    // the nodes will contain internal data from the tree
    // only get the properties we want
    nodes = this.fieldsTreeGrid.data.getAllNodes();
    length = nodes.length;
    for (i = 0; i < length; i++) {
      node = nodes[i];
      record = {};
      for (j = 0; j < this.personalizationDataProperties.length; j++) {
        value = node[this.personalizationDataProperties[j]];
        if (value !== undef) {
          record[this.personalizationDataProperties[j]] = value;
        }
      }
      result.push(record);
    }
    return {
      fields: result
    };
  },

  // sets information about the current personalization record
  // in the status bar
  setStatusBarInformation: function () {
    this.toolBar.updateButtonState();

    var statusBarFields = null,
        barFieldValues = [],
        barFieldTitles = [],
        label, icon = null,
        statusCode = null;
    if (this.isNew) {
      icon = this.statusBar.newIcon;
      label = 'OBUIAPP_New';
    } else if (this.isChanged) {
      icon = this.statusBar.editIcon;
      label = 'OBUIAPP_Editing';
    } else if (this.isSaved) {
      icon = this.statusBar.savedIcon;
      label = 'OBUIAPP_Saved';
    }

    if (this.clientIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_Client'));
      barFieldValues.push(this.clientIdentifier);
    }
    if (this.orgIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_Organization'));
      barFieldValues.push(this.orgIdentifier);
    }
    if (this.roleIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_Role'));
      barFieldValues.push(this.roleIdentifier);
    }
    if (this.userIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_User'));
      barFieldValues.push(this.userIdentifier);
    }
    if (this.tabIdentifier) {
      barFieldTitles.push(OB.I18N.getLabel('OBUIAPP_Tab'));
      barFieldValues.push(this.tabIdentifier);
    }

    if (barFieldTitles.length > 0) {
      statusBarFields = [];
      statusBarFields.push(barFieldTitles);
      statusBarFields.push(barFieldValues);
    }

    this.statusBar.setContentLabel(icon, label, statusBarFields, OB.I18N.getLabel('OBUIAPP_WindowPersonalization_Guidance'));
  }
});