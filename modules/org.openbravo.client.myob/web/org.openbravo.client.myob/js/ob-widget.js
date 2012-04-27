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

// = OBWidget Definition =
//
// == OBWidgetMenu ==
// Defines the menu handling
//
isc.defineClass('OBWidgetMenu', isc.Menu).addProperties({
  portlet: null,
  fields: ['icon', 'title'],

  // overridden to get reliable custom style name
  getBaseStyle: function (record, rowNum, colNum) {
    var name = this.getField(colNum).name;
    return this.baseStyle + name.substr(0, 1).toUpperCase() + name.substr(1) + 'Field';
  },


  // overridden to let the menu to expand to the left, within the widget
  // TODO: how to handle RTL?
  placeNear: function (left, top) {
    var newLeft = left - this.width + this.menuButton.getVisibleWidth();
    // don't show left from the portlet, in that extremely rare
    // case use the old left
    if (newLeft < this.portlet.getPageLeft()) {
      newLeft = left;
    }
    return this.Super('placeNear', [newLeft, top]);
  }
});

//
// == OBWidgetMenuItem ==
//
isc.defineClass('OBWidgetMenuItem', isc.MenuButton).addProperties({
  widget: null,
  menu: null,

  title: '',

  editFormLayout: null,
  windowContents: null,
  menuItems: null,

  initWidget: function (args) {
    this.widget = args.portlet;
    this.menuItems = this.widget.menuItems;
    this.menu = isc.OBWidgetMenu.create({
      portlet: this.widget
    });
    this.Super('initWidget', args);
  },

  showMenu: function () {
    var me = this,
        menuItems, i, baseMenuItem;

    baseMenuItem = {
      title: '',
      widget: me.widget,
      isSeparator: false,
      iconHeight: 0,
      iconWidth: 0,
      click: null
    };

    this.menu.menuButton = this;

    menuItems = [{
      title: OB.I18N.getLabel('OBKMO_WMO_EditSettings'),
      widget: this.widget,
      enableIf: function (target, menu, item) {
        // already in edit mode
        if (this.widget.widgetMode === this.widget.EDIT_MODE) {
          return false;
        }
        return this.widget.fieldDefinitions.length > 0;
      },
      click: function (target, item, menu) {
        this.widget.switchMode();
      }
    }, {
      isSeparator: true
    }, {
      title: OB.I18N.getLabel('OBKMO_WMO_Refresh'),
      iconHeight: 0,
      iconWidth: 0,
      widget: this.widget,
      click: function (target, item, menu) {
        this.widget.refresh();
      }
    }, {
      title: OB.I18N.getLabel('OBKMO_WMO_DeleteThisWidget'),
      widget: this.widget,
      enableIf: function (target, menu, item) {
        return this.widget.canDelete;
      },
      click: function (target, item, menu) {
        this.widget.closeClick();
      }
    }];
    if (isc.isAn.Array(this.menuItems) && this.menuItems.length > 0) {
      for (i = 0; i < this.menuItems.length; i++) {
        if (this.menuItems[i].isSeparator) {
          menuItems.push({
            isSeparator: true
          });
          continue;
        }

        if (!this.widget[this.menuItems[i].click]) {
          isc.Log.logWarn('Method: ' + this.menuItems[i].click + ' not defined for widget: ' + this.widget);
        }

        menuItems.push(isc.addProperties({}, baseMenuItem, {
          title: this.menuItems[i].title,
          click: this.widget[this.menuItems[i].click]
        }));
      }
    }
    menuItems.push({
      isSeparator: true
    });
    menuItems.push({
      title: OB.I18N.getLabel('OBKMO_WMO_About'),
      iconHeight: 0,
      iconWidth: 0,
      widget: this.widget,
      click: function (target, item, menu) {
        this.widget.showAbout();
      }
    });

    this.menu.setData(menuItems);
    return this.Super('showMenu', arguments);
  }
});

//
// == OBWidget ==
//
// Implements the base class from where all MyOpenbravo widgets extend.
//
isc.defineClass('OBWidget', isc.Portlet).addProperties({

  CONTENT_MODE: 'content',
  EDIT_MODE: 'edit',

  canResizeRows: false,
  showMaximizeButton: false,
  showMinimizeButton: false,
  showCloseButton: false,
  closeConfirmationMessage: OB.I18N.getLabel('OBKMO_DeleteThisWidgetConfirmation'),
  destroyOnClose: true,

  canDelete: true,
  dbInstanceId: '',

  // Parameters handling
  dbFilterProperty: 'obkmoWidgetInstance',
  entityName: 'OBKMO_WidgetInstance',

  autoSize: false,

  fieldDefinitions: [],
  parameters: {},


  headerProperties: {
    defaultLayoutAlign: 'center'
  },

  // note: dragappearance target gives strange results if one attempts to 
  // drag a widget outside of the portallayout, this because actually
  // the target is dragged and not a separate layout  
  dragAppearance: 'outline',
  dragRepositionStart: function () {
    // keep the widget in the portallayout
    this.keepInParentRect = OB.MyOB.portalLayout.getPageRect();
    return true;
  },

  // set by my openbravo  
  widgetManager: null,

  widgetMode: null,

  // viewForm if widget widget is embedded into a generated window
  viewForm: null,

  initWidget: function (args) {
    var widget = this,
        headerControls = ['headerLabel'];

    // when widget placed inside generated window
    if (this.inWidgetInFormMode) {
      this.showHeader = false;
      this.canDragReposition = false;
      this.height = '0px'; // together with overflow:visible to get height up-to rowspan
    } else {
      // set the headercontrols in initWidget otherwise only  
      // one menubutton gets created for all widgets
      this.menuButton = isc.OBWidgetMenuItem.create({
        portlet: this
      });

      headerControls.push(this.menuButton);

      if (args.showMaximizeButton) {
        headerControls.push('maximizeButton');
      }

      this.headerControls = headerControls;
    }

    this.editFormLayout = this.createEditFormLayout();
    this.windowContents = this.createWindowContents();

    // if not all mandatory params are set then edit mode
    // otherwise content mode
    if (!this.allRequiredParametersSet()) {
      this.widgetMode = this.EDIT_MODE;
    } else {
      this.widgetMode = this.CONTENT_MODE;
    }
    this.toMode(this.widgetMode);

    this.src = null;
    this.items = [this.windowContents, this.editFormLayout];
    this.Super('initWidget', arguments);
  },

  confirmedClosePortlet: function (ok) {
    if (ok) {
      this.Super('confirmedClosePortlet', arguments);
      OB.MyOB.notifyEvent('WIDGET_REMOVED');
    }
  },

  // ** {{{ OBMyOpenbravo.switchMode() }}} **
  //
  // Switches the widget from edit to content mode and vice versa.
  // Edit mode is the edit parameters mode, content mode shows the 
  // normal content of the widget. 
  switchMode: function () {
    if (this.widgetMode === this.CONTENT_MODE) {
      this.toMode(this.EDIT_MODE);
    } else {
      this.refresh();
      this.toMode(this.CONTENT_MODE);
    }
  },

  toMode: function (targetMode) {
    if (targetMode === this.EDIT_MODE) {
      this.windowContents.hide();
      this.editFormLayout.editForm.clearValues();
      this.editFormLayout.editForm.setValues(isc.addProperties({}, this.parameters));
      this.editFormLayout.show();
      this.widgetMode = this.EDIT_MODE;
    } else {
      this.windowContents.show();
      this.editFormLayout.hide();
      this.widgetMode = this.CONTENT_MODE;
    }
  },

  // ** {{{ OBMyOpenbravo.createEditFormLayout() }}} **
  //
  // Creates the edit form layout used to edit parameters.
  createEditFormLayout: function () {
    var formLayout, theForm, buttonLayout, widget = this,
        i, fieldDefinition, items = [];

    formLayout = isc.VStack.create({
      defaultLayoutAlign: 'center',
      overflow: 'visible',
      height: 1,
      width: '100%'
    });

    // no fields, stop here
    if (this.fieldDefinitions.length === 0) {
      return formLayout;
    }

    theForm = isc.DynamicForm.create({
      width: '99%',
      height: '100%',
      titleSuffix: '',
      requiredTitleSuffix: '',
      autoFocus: true,
      titleOrientation: 'top',
      numCols: 2,
      colWidths: ['*', '*']
    });

    // set the initial values
    theForm.values = isc.addProperties({}, this.parameters);

    // create the fields    
    for (i = 0; i < this.fieldDefinitions.length; i++) {
      fieldDefinition = this.fieldDefinitions[i];

      // handle it when there are fieldProperties
      if (fieldDefinition.fieldProperties) {
        fieldDefinition = isc.addProperties(fieldDefinition, fieldDefinition.fieldProperties);
        delete fieldDefinition.fieldProperties;
      }

      var formItem = isc.addProperties({}, fieldDefinition);

      items.push(formItem);
    }
    theForm.setItems(items);

    formLayout.addMember(theForm);
    formLayout.editForm = theForm;

    buttonLayout = isc.HStack.create({
      layoutTopMargin: 10,
      membersMargin: 10,
      align: 'center',
      overflow: 'visible',
      height: 1,
      width: '100%'
    });

    buttonLayout.addMembers(isc.OBFormButton.create({
      autoFit: true,
      // note reusing label from navba, is fine as these are 
      // moved to client.app later
      title: OB.I18N.getLabel('UINAVBA_Save'),
      click: function () {
        if (theForm.validate(true)) {
          widget.setParameters(isc.addProperties(widget.parameters, theForm.getValues()));
          theForm.rememberValues();
          widget.saveParameters();
        }
      }
    }));
    buttonLayout.addMembers(isc.OBFormButton.create({
      autoFit: true,
      // note reusing label from navba, is fine as these are 
      // moved to client.app later
      title: OB.I18N.getLabel('UINAVBA_Cancel'),
      click: function () {
        if (widget.allRequiredParametersSet()) {
          widget.switchMode();
        } else {
          isc.warn(OB.I18N.getLabel('OBKMO_NotAllParametersSet'));
        }
      }
    }));
    formLayout.addMembers(buttonLayout);

    return formLayout;
  },

  allRequiredParametersSet: function () {
    var i, fieldDefinition;
    for (i = 0; i < this.fieldDefinitions.length; i++) {
      fieldDefinition = this.fieldDefinitions[i];
      if (fieldDefinition.required && !this.parameters[fieldDefinition.name] && this.parameters[fieldDefinition.name] !== false) {
        return false;
      }
    }
    return true;
  },

  // ** {{{ OBMyOpenbravo.createWindowContents() }}} **
  //
  // Creates the Canvas which implements the normal content
  // of the window. Must be overridden by the implementing subclass.
  createWindowContents: function () {
    return isc.Label.create({
      contents: 'Implement the createWindowContents method in the subclass!'
    });
  },

  // ** {{{ OBMyOpenbravo.evaluateContents() }}} **
  //
  // Evaluates the str and replaces all parameters which have the form
  // ${parameter} with a value read from the javascript context. The 
  // parameters of this widget are also set as values. 
  evaluateContents: function (str) {
    return str.evalDynamicString(this, this.parameters);
  },

  // ** {{{ OBMyOpenbravo.setParameters(parameters) }}} **
  //
  // Is called when the edit parameters form is saved, the parameters 
  // object is passed in. The default implementation sets the parameters
  // of the widget.
  setParameters: function (parameters) {
    this.parameters = parameters;
  },

  //
  // ** {{{ OBWidget.refresh }}} **
  //
  // The refresh is called from the widget menu. The OBWidget subclass needs to
  // implement this method and handle the refresh of its contents
  //
  refresh: function () {
    isc.Log.logInfo('The subclass needs to implement this method');
  },

  //
  // ** {{{ OBWidget.showAbout }}} **
  //
  // The showAbout is called from the widget menu. 
  //
  showAbout: function () {
    isc.OBAboutPopupWindow.create({
      title: OB.I18N.getLabel('OBKMO_WMO_About') + ' ' + this.title,
      aboutFieldDefinitions: this.aboutFieldDefinitions
    }).show();
  },

  //
  // ** {{{ OBWidget.isSameWidget }}} **
  //
  // Returns true if the object passed as parameter is the same instance.
  // 
  // Parameters:
  // {{widget}} an object to which you want to compare
  // {{isNew}} If this flag is true, the comparison is based on the ID of the
  // client side object, otherwise the dbInstanceId is used
  isSameWidget: function (widget, isNew) {
    if (!widget) {
      return false;
    }

    if (!isNew) {
      return this.dbInstanceId === widget.dbInstanceId;
    }

    return this.ID === widget.ID;
  },

  setDbInstanceId: function (instanceId) {
    this.dbInstanceId = instanceId;
    this.refresh();
  },

  saveParameters: function () {
    var post, i, param, paramObj, fieldDef;

    if (isc.isA.emptyObject(this.parameters)) {
      return;
    }

    post = {
      ID: this.ID,
      dbInstanceId: this.dbInstanceId,
      dbFilterProperty: this.dbFilterProperty,
      action: 'SAVE',
      entityName: this.entityName,
      parameters: []
    };

    for (param in this.parameters) {
      if (this.parameters.hasOwnProperty(param)) {
        for (i = 0; i < this.fieldDefinitions.length; i++) {
          fieldDef = this.fieldDefinitions[i];
          if (param === fieldDef.name) {
            paramObj = {};
            paramObj.name = param;
            paramObj.parameterId = fieldDef.parameterId;
            paramObj.value = this.parameters[param];
            post.parameters.push(paramObj);
          }
        }
      }
    }

    OB.RemoteCallManager.call('org.openbravo.client.application.ParametersActionHandler', post, {}, function (rpcResponse, data, rpcRequest) {
      if (data && data.ID && window[data.ID]) {
        window[data.ID].saveParametersResponseHandler(rpcResponse, data, rpcRequest);
      }
    });
  },

  saveParametersResponseHandler: function (rpcReponse, data, rpcRequest) {
    if (data && data.message) {
      if (data.message.type !== 'Success') {
        isc.Log.logError(data.message.message);
      }
    }
    this.switchMode();
  }
});