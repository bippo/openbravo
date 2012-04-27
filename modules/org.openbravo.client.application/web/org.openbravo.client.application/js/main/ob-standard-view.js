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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */


isc.ClassFactory.defineClass('OBFormContainerLayout', isc.VLayout);

isc.OBFormContainerLayout.addProperties({
  canFocus: true,
  width: '100%',
  height: '*',
  overflow: 'auto'
});


// = OBStandardView =
//
// An OBStandardView represents a single Openbravo tab. An OBStandardView consists
// of three parts:
// 1) a grid an instance of an OBViewGrid (property: viewGrid)
// 2) a form an instance of an OBViewForm (property: viewForm)
// 3) a tab set with child OBStandardView instances (property: childTabSet)
// 
// In addition an OBStandardView has components for a message bar and other visualization.
// 
// A standard view can be opened as a result of a direct link from another window/tab. See
// the description in ob-standard-window for the flow in that case.
//
isc.ClassFactory.defineClass('OBStandardView', isc.VLayout);

isc.OBStandardView.addClassProperties({
  // the part in the top is maximized, meaning
  STATE_TOP_MAX: 'TopMax',

  // that the tabset in the bottom is minimized
  // the tabset part is maximized, the
  STATE_BOTTOM_MAX: 'BottomMax',

  // the top has height 0
  // the view is split in the middle, the top part has
  STATE_MID: 'Mid',

  // 50%, the tabset also
  // state of the tabset which is shown in the middle,
  STATE_IN_MID: 'InMid',

  // the parent of the tabset has state
  // isc.OBStandardView.STATE_MID
  // minimized state, the parent has
  STATE_MIN: 'Min',

  // isc.OBStandardView.STATE_TOP_MAX or
  // isc.OBStandardView.STATE_IN_MID
  // the inactive state does not show an orange hat on the tab button
  MODE_INACTIVE: 'Inactive',

  UI_PATTERN_READONLY: 'RO',
  UI_PATTERN_SINGLERECORD: 'SR',
  UI_PATTERN_STANDARD: 'ST'
});

isc.OBStandardView.addProperties({

  // properties used by the ViewManager, only relevant in case this is the
  // top
  // view shown directly in the main tab
  showsItself: false,
  tabTitle: null,

  // ** {{{ windowId }}} **
  // The id of the window shown here, only set for the top view in the
  // hierarchy
  // and if this is a window/tab view.
  windowId: null,

  // ** {{{ tabId }}} **
  // The id of the tab shown here, set in case of a window/tab view.
  tabId: null,

  // ** {{{ processId }}} **
  // The id of the process shown here, set in case of a process view.
  processId: null,

  // ** {{{ formId }}} **
  // The id of the form shown here, set in case of a form view.
  formId: null,

  // ** {{{ parentView }}} **
  // The parentView if this view is a child in a parent child structure.
  parentView: null,

  // ** {{{ parentTabSet }}} **
  // The tabSet which shows this view. If the parentView is null then this
  // is the
  // top tabSet.
  parentTabSet: null,
  tab: null,

  // ** {{{ toolbar }}} **
  // The toolbar canvas.
  toolBar: null,

  messageBar: null,

  // ** {{{ formGridLayout }}} **
  // The layout which holds the form and grid.
  formGridLayout: null,

  // ** {{{ childTabSet }}} **
  // The tabSet holding the child tabs with the OBView instances.
  childTabSet: null,

  // ** {{{ hasChildTabs }}} **
  // Is set to true if there are child tabs.
  hasChildTabs: false,

  // ** {{{ dataSource }}} **
  // The dataSource used to fill the data in the grid/form.
  dataSource: null,

  // ** {{{ viewForm }}} **
  // The viewForm used to display single records
  viewForm: null,

  // ** {{{ viewGrid }}} **
  // The viewGrid used to display multiple records
  viewGrid: null,

  // ** {{{ parentProperty }}} **
  // The name of the property refering to the parent record, if any
  parentProperty: null,

  // ** {{{ targetRecordId }}} **
  // The id of the record to initially show.
  targetRecordId: null,

  // ** {{{ entity }}} **
  // The entity to show.
  entity: null,

  width: '100%',
  height: '100%',
  margin: 0,
  padding: 0,
  overflow: 'hidden',

  // set if one record has been selected
  lastRecordSelected: null,
  lastRecordSelectedCount: 0,
  fireOnPauseDelay: 200,

  // ** {{{ refreshContents }}} **
  // Should the contents listgrid/forms be refreshed when the tab
  // gets selected and shown to the user.
  refreshContents: true,

  state: isc.OBStandardView.STATE_MID,
  previousState: isc.OBStandardView.STATE_TOP_MAX,

  // last item in the filtergrid or the form which had focus
  // when the view is activated it will set focus here
  lastFocusedItem: null,

  // initially set to true, is set to false after the 
  // first time default edit mode is opened or a new parent 
  // is selected.
  // note that opening the edit view is done in the viewGrid.dataArrived
  // method
  allowDefaultEditMode: true,

  readOnly: false,
  singleRecord: false,

  isShowingForm: false,
  isEditingGrid: false,

  propertyToColumns: [],

  initWidget: function (properties) {
    var length, rightMemberButtons = [],
        leftMemberButtons = [],
        i, actionButton;

    this.messageBar = isc.OBMessageBar.create({
      visibility: 'hidden',
      view: this
    });

    if (this.isRootView) {
      this.buildStructure();
    }

    OB.TestRegistry.register('org.openbravo.client.application.View_' + this.tabId, this);
    OB.TestRegistry.register('org.openbravo.client.application.ViewGrid_' + this.tabId, this.viewGrid);
    OB.TestRegistry.register('org.openbravo.client.application.ViewForm_' + this.tabId, this.viewForm);

    if (this.actionToolbarButtons) {
      length = this.actionToolbarButtons.length;
      for (i = 0; i < length; i++) {
        actionButton = isc.OBToolbarActionButton.create(this.actionToolbarButtons[i]);
        actionButton.contextView = this;
        rightMemberButtons.push(actionButton);
      }
    }

    // Look for specific toolbar buttons for this tab
    if (this.iconToolbarButtons) {
      length = this.iconToolbarButtons.length;
      for (i = 0; i < length; i++) {
        // note create a somewhat unique id by concatenating the tabid and the index
        OB.ToolbarRegistry.registerButton(this.tabId + '_' + i, isc.OBToolbarIconButton, this.iconToolbarButtons[i], 200 + (i * 10), this.tabId);
      }
    }

    this.toolBar = isc.OBToolbar.create({
      view: this,
      visibility: 'hidden',
      leftMembers: leftMemberButtons,
      rightMembers: rightMemberButtons
    });

    this.Super('initWidget', arguments);

    this.toolBar.updateButtonState(true);
  },

  show: function () {
    this.Super('show', arguments);
  },

  destroy: function () {
    // destroy the datasource
    if (this.dataSource) {
      this.dataSource.destroy();
      this.dataSource = null;
    }
    return this.Super('destroy', arguments);
  },

  buildStructure: function () {
    var length, i, fld;
    this.createMainParts();
    this.createViewStructure();
    if (this.childTabSet && this.childTabSet.tabs.length === 0) {
      this.hasChildTabs = false;
      this.activeGridFormMessageLayout.setHeight('100%');
    }
    this.dataSource.view = this;

    // directTabInfo is set when we are in direct link mode, i.e. directly opening
    // a specific tab with a record, the direct link logic will already take care
    // of fetching data
    if (this.isRootView && !this.standardWindow.directTabInfo) {
      this.viewGrid.fetchData(this.viewGrid.getCriteria());
      this.refreshContents = false;
    }

    if (this.viewForm) {
      // setDataSource executes setFields which replaces the current fields
      // We don't want to destroy the associated DataSource objects
      this.viewForm.destroyItemObjects = false;

      // is used to keep track of the original simple objects
      // used to create fields
      this.viewForm._originalFields = isc.clone(this.formFields);
      this.viewForm.fields = this.formFields;
      this.viewForm.firstFocusedField = this.firstFocusedField;

      this.viewForm.setDataSource(this.dataSource, this.formFields);
      this.viewForm.isViewForm = true;
      this.viewForm.destroyItemObjects = true;
    }

    if (this.isRootView) {
      if (this.childTabSet) {
        this.childTabSet.setState(isc.OBStandardView.STATE_IN_MID);
        this.childTabSet.selectTab(this.childTabSet.tabs[0]);
        OB.TestRegistry.register('org.openbravo.client.application.ChildTabSet_' + this.tabId, this.viewForm);
      }
    }

    if (this.defaultEditMode) {
      // prevent the grid from showing very shortly, so hide it right away
      this.viewGrid.hide();
    }

  },

  // handles different ways by which an error can be passed from the 
  // system, translates this to an object with a type, title and message
  setErrorMessageFromResponse: function (resp, data, req) {
    var errorCode, index1, index2;

    // only handle it once
    if (resp._errorMessageHandled) {
      return true;
    }
    var msg = '',
        title = null,
        type = isc.OBMessageBar.TYPE_ERROR,
        isLabel = false,
        params = null;
    var gridEditing = req.clientContext && (req.clientContext.editRow || req.clientContext.editRow === 0);
    if (isc.isA.String(data)) {
      msg = data;
    } else if (data && data.response) {
      if (data.response.errors) {
        // give it to the form
        if (this.isShowingForm) {
          this.viewForm.handleFieldErrors(data.response.errors);
        } else {
          this.viewGrid.setRecordFieldErrorMessages(req.clientContext.editRow, data.response.errors);
        }
        return true;
      } else if (data.response.error) {
        var error = data.response.error;
        if (error.type && error.type === 'user') {
          isLabel = true;
          msg = error.message;
          params = error.params;
        } else if (error.message) {
          type = error.messageType || type;
          params = error.params;
          // error.messageType can be Error
          type = type.toLowerCase();
          title = error.title || title;
          msg = error.message;
        } else {
          // hope that someone else will handle it
          return false;
        }
      } else {
        // hope that someone else will handle it
        return false;
      }
    } else if (data.data) {
      // try it with data.data
      return this.setErrorMessageFromResponse(resp, data.data, req);
    } else {
      // hope that someone else will handle it
      return false;
    }

    req.willHandleError = true;
    resp._errorMessageHandled = true;
    if (msg.indexOf('@') !== -1) {
      index1 = msg.indexOf('@');
      index2 = msg.indexOf('@', index1 + 1);
      if (index2 !== -1) {
        errorCode = msg.substring(index1 + 1, index2);
        if (gridEditing) {
          this.setLabelInRow(req.clientContext.editRow, errorCode, params);
        } else {
          this.messageBar.setLabel(type, title, errorCode, params);
        }
      }
    } else if (isLabel) {
      if (gridEditing) {
        this.setLabelInRow(req.clientContext.editRow, msg, params);
      } else {
        this.messageBar.setLabel(type, title, msg, params);
      }
    } else if (gridEditing) {
      this.viewGrid.setRecordErrorMessage(req.clientContext.editRow, msg);
    } else {
      this.messageBar.setMessage(type, title, msg);
    }
    return true;
  },

  setLabelInRow: function (rowNum, label, params) {
    var me = this;
    OB.I18N.getLabel(label, params, {
      setLabel: function (text) {
        me.viewGrid.setRecordErrorMessage(rowNum, text);
      }
    }, 'setLabel');
  },

  // ** {{{ createViewStructure }}} **
  // Is to be overridden, is called in initWidget.
  createViewStructure: function () {},

  // ** {{{ createMainParts }}} **
  // Creates the main layout components of this view.
  createMainParts: function () {
    var me = this,
        completeFieldsWithoutImages, fieldsWithoutImages;
    if (this.tabId && this.tabId.length > 0) {
      this.formGridLayout = isc.HLayout.create({
        canFocus: true,
        width: '100%',
        height: '*',
        overflow: 'visible',
        view: this
      });

      this.activeBar = isc.HLayout.create({
        height: '100%',
        canFocus: true,
        // to set active view when it gets clicked
        contents: '&nbsp;',
        width: OB.Styles.ActiveBar.width,
        styleName: OB.Styles.ActiveBar.inActiveStyleName,
        activeStyleName: OB.Styles.ActiveBar.activeStyleName,
        inActiveStyleName: OB.Styles.ActiveBar.inActiveStyleName,

        setActive: function (active) {
          if (active) {
            this.setStyleName(this.activeStyleName);
          } else {
            this.setStyleName(this.inActiveStyleName);
          }
        }
      });

      // the grid should not show the image fields
      // see issue 20049 (https://issues.openbravo.com/view.php?id=20049)
      completeFieldsWithoutImages = this.removeImageFields(this.viewGrid.completeFields);
      fieldsWithoutImages = this.removeImageFields(this.viewGrid.fields);

      this.viewGrid.setDataSource(this.dataSource, completeFieldsWithoutImages || fieldsWithoutImages);

      if (this.viewGrid) {
        this.viewGrid.setWidth('100%');
        this.viewGrid.setView(this);
        this.formGridLayout.addMember(this.viewGrid);
      }

      if (this.viewForm) {
        this.viewForm.setWidth('100%');
        this.formGridLayout.addMember(this.viewForm);
        this.viewForm.view = this;

        this.viewGrid.addFormProperties(this.viewForm.obFormProperties);
      }

      this.statusBar = isc.OBStatusBar.create({
        view: this.viewForm.view
      });

      // NOTE: when changing the layout structure and the scrollbar
      // location for these layouts check if the scrollTo method 
      // in ob-view-form-linked-items is still called on the correct
      // object 
      this.statusBarFormLayout = isc.VLayout.create({
        canFocus: true,
        width: '100%',
        height: '*',
        visibility: 'hidden',
        overflow: 'hidden'
      });

      // to make sure that the form gets the correct scrollbars
      this.formContainerLayout = isc.OBFormContainerLayout.create({});
      this.formContainerLayout.addMember(this.viewForm);

      this.statusBarFormLayout.addMember(this.statusBar);
      this.statusBarFormLayout.addMember(this.formContainerLayout);

      this.formGridLayout.addMember(this.statusBarFormLayout);

      // wrap the messagebar and the formgridlayout in a VLayout
      this.gridFormMessageLayout = isc.VLayout.create({
        canFocus: true,
        height: '100%',
        width: '100%',
        overflow: 'auto'
      });
      this.gridFormMessageLayout.addMember(this.messageBar);
      this.gridFormMessageLayout.addMember(this.formGridLayout);

      // and place the active bar to the left of the form/grid/messagebar
      this.activeGridFormMessageLayout = isc.HLayout.create({
        canFocus: true,
        height: (this.hasChildTabs ? '50%' : '100%'),
        width: '100%',
        overflow: 'hidden'
      });

      this.activeGridFormMessageLayout.addMember(this.activeBar);
      this.activeGridFormMessageLayout.addMember(this.gridFormMessageLayout);

      this.addMember(this.activeGridFormMessageLayout);
    }
    if (this.hasChildTabs) {
      this.childTabSet = isc.OBTabSetChild.create({
        height: '*',
        parentContainer: this,
        parentTabSet: this.parentTabSet
      });
      this.addMember(this.childTabSet);
    } else if (this.isRootView) {
      // disable the maximize button if this is the root without
      // children
      this.statusBar.maximizeButton.disable();
    }
  },

  // returns a copy of fields after deleting the image fields
  // see issue 20049 (https://issues.openbravo.com/view.php?id=20049)
  removeImageFields: function (fields) {
    var indexesToDelete, i, length, fieldsWithoutImages;
    indexesToDelete = [];
    if (fields) {
      fieldsWithoutImages = fields.duplicate();
      length = fieldsWithoutImages.length;
      // gets the index of the image fields
      for (i = 0; i < length; i++) {
        if (fieldsWithoutImages[i].targetEntity === 'ADImage') {
          indexesToDelete.push(i);
        }
      }
      // removes the image fields
      length = indexesToDelete.length;
      for (i = 0; i < length; i++) {
        fieldsWithoutImages.splice(indexesToDelete[i] - i, 1);
      }
    } else {
      fieldsWithoutImages = fields;
    }
    return fieldsWithoutImages;
  },

  getDirectLinkUrl: function () {
    var url = window.location.href,
        crit;
    var qIndex = url.indexOf('?');
    var dIndex = url.indexOf('#');
    var index = -1;
    if (dIndex !== -1 && qIndex !== -1) {
      if (dIndex < qIndex) {
        index = dIndex;
      } else {
        index = qIndex;
      }
    } else if (qIndex !== -1) {
      index = qIndex;
    } else if (dIndex !== -1) {
      index = dIndex;
    }
    if (index !== -1) {
      url = url.substring(0, index);
    }

    url = url + '?tabId=' + this.tabId;
    if (this.isShowingForm && this.viewForm.isNew && this.isRootView) {
      url = url + '&command=NEW';
    } else if ((this.isShowingForm || !this.isRootView) && this.viewGrid.getSelectedRecords() && this.viewGrid.getSelectedRecords().length === 1) {
      url = url + '&recordId=' + this.viewGrid.getSelectedRecord().id;
    } else if (!this.isShowingForm && this.isRootView) {
      crit = this.viewGrid.getCriteria();
      if (crit && crit.criteria && crit.criteria.length > 0) {
        url = url + '&criteria=' + escape(isc.JSON.encode(crit, {
          prettyPrint: false,
          dateFormat: 'dateConstructor'
        }));
      }
    }

    return url;
  },

  // ** {{{ addChildView }}} **
  // The addChildView creates the child tab and sets the pointer back to
  // this
  // parent.
  addChildView: function (childView) {
    var length, i, actionButton;

    if ((childView.isTrlTab && OB.PropertyStore.get('ShowTrl', this.windowId) !== 'Y') || (childView.isAcctTab && OB.PropertyStore.get('ShowAcct', this.windowId) !== 'Y')) {
      return;
    }

    this.standardWindow.addView(childView);

    // Add buttons in parent to child. Note that currently it is only added one level.
    if (this.actionToolbarButtons && this.actionToolbarButtons.length > 0 && childView.showParentButtons) {
      length = this.actionToolbarButtons.length;
      for (i = 0; i < length; i++) {
        actionButton = isc.OBToolbarActionButton.create(isc.addProperties({}, this.actionToolbarButtons[i], {
          baseStyle: 'OBToolbarTextButtonParent'
        }));
        actionButton.contextView = this; // Context is still parent view
        actionButton.toolBar = childView.toolBar;
        actionButton.view = childView;

        childView.toolBar.rightMembers.push(actionButton);

        childView.toolBar.addMems([
          [actionButton]
        ]);
        childView.toolBar.addMems([
          [isc.HLayout.create({
            width: (this.toolBar && this.toolBar.rightMembersMargin) || 12,
            height: 1
          })]
        ]);
      }

      if (this.actionToolbarButtons.length > 0) {
        // Add margin in the right
        childView.toolBar.addMems([
          [isc.HLayout.create({
            width: (this.toolBar && this.toolBar.rightMargin) || 4,
            height: 1
          })]
        ]);
      }
    }

    childView.parentView = this;
    childView.parentTabSet = this.childTabSet;

    // build the structure of the children
    childView.buildStructure();

    var childTabDef = {
      title: childView.tabTitle,
      pane: childView
    };

    this.childTabSet.addTab(childTabDef);

    childView.tab = this.childTabSet.getTab(this.childTabSet.tabs.length - 1);
    // start inactive
    childView.tab.setCustomState(isc.OBStandardView.MODE_INACTIVE);

    OB.TestRegistry.register('org.openbravo.client.application.ChildTab_' + this.tabId + '_' + childView.tabId, childView.tab);
  },

  setReadOnly: function (readOnly) {
    this.readOnly = readOnly;
    this.viewForm.readOnly = readOnly;
    if (this.viewGrid && readOnly) {
      this.viewGrid.setReadOnlyMode();
    }
  },

  setSingleRecord: function (singleRecord) {
    this.singleRecord = singleRecord;
  },

  setViewFocus: function () {

    var object, functionName, items, item, i;

    // clear for a non-focusable item
    if (this.lastFocusedItem && !this.lastFocusedItem.getCanFocus()) {
      this.lastFocusedItem = null;
    }

    if (this.isShowingForm && this.viewForm && this.viewForm.getFocusItem()) {
      object = this.viewForm.getFocusItem();
      functionName = 'focusInItem';
    } else if (this.isEditingGrid && this.viewGrid.getEditForm() && this.viewGrid.getEditForm().getFocusItem()) {
      object = this.viewGrid.getEditForm();
      functionName = 'focus';
    } else if (this.lastRecordSelected) {
      object = this.viewGrid;
      functionName = 'focus';
    } else if (this.lastFocusedItem) {
      object = this.lastFocusedItem;
      functionName = 'focusInItem';
    } else if (this.viewGrid && !this.isShowingForm && this.viewGrid.getFilterEditor() && this.viewGrid.getFilterEditor().getEditForm()) {
      this.viewGrid.focusInFirstFilterEditor();
      functionName = 'focus';
    } else if (this.viewGrid) {
      object = this.viewGrid;
      functionName = 'focus';
    }

    if (object && functionName) {
      isc.Page.setEvent(isc.EH.IDLE, object, isc.Page.FIRE_ONCE, functionName);
    }
  },

  setTabButtonState: function (active) {
    var tabButton;
    if (this.tab) {
      tabButton = this.tab;
    } else {
      // don't like to use the global window object, but okay..
      tabButton = window[this.standardWindow.viewTabId];
    }
    // enable this code to set the styleclass changes
    if (!tabButton) {
      return;
    }
    if (active) {
      tabButton.setCustomState('');
    } else {
      tabButton.setCustomState(isc.OBStandardView.MODE_INACTIVE);
    }
  },

  hasValidState: function () {
    return this.isRootView || this.getParentId();
  },

  isActiveView: function () {
    if (this.standardWindow && this.standardWindow.activeView) {
      return this.standardWindow.activeView === this;
    } else {
      return false;
    }
  },

  setAsActiveView: function (autoSaveDone) {
    if (!autoSaveDone && this.standardWindow.activeView && this.standardWindow.activeView !== this) {
      var actionObject = {
        target: this,
        method: this.setAsActiveView,
        parameters: [true]
      };
      this.standardWindow.doActionAfterAutoSave(actionObject, false);
      return;
    }
    this.standardWindow.setActiveView(this);
  },

  setTargetRecordInWindow: function (recordId) {
    if (this.isActiveView()) {
      this.standardWindow.setTargetInformation(this.tabId, recordId);
    }
  },

  setRecentDocument: function (record) {
    var params = this.standardWindow.getBookMarkParams();
    params.targetTabId = this.tabId;
    params.targetRecordId = record.id;
    params.recentId = this.tabId + '_' + record.id;
    params.recentTitle = record[OB.Constants.IDENTIFIER];
    OB.Layout.ViewManager.addRecentDocument(params);
  },

  setActiveViewProps: function (state) {
    if (state) {
      this.toolBar.show();
      this.statusBar.setActive(true);
      this.activeBar.setActive(true);
      this.setViewFocus();
      this.viewGrid.setActive(true);
      this.viewGrid.markForRedraw();
      // if we are in form view
      if (this.isShowingForm && !this.viewForm.isNew) {
        this.setTargetRecordInWindow(this.viewGrid.getSelectedRecord().id);
      }
    } else {

      // close any editors we may have
      this.viewGrid.closeAnyOpenEditor();

      this.toolBar.hide();
      this.statusBar.setActive(false);
      this.activeBar.setActive(false);
      this.viewGrid.setActive(false);
      this.viewGrid.markForRedraw();
      // note we can not check on viewForm visibility as 
      // the grid and form can both be hidden when changing
      // to another tab, this handles the case that the grid
      // is shown but the underlying form has errors
      if (this.isShowingForm) {
        this.lastFocusedItem = this.viewForm.getFocusItem();
        this.viewForm.setFocusItem(null);
      }
      this.standardWindow.autoSave();
    }
    this.setTabButtonState(state);
  },

  visibilityChanged: function (visible) {
    if (visible && this.refreshContents) {
      this.doRefreshContents(true);
    }
  },

  doRefreshContents: function (doRefreshWhenVisible, forceRefresh) {

    // if not visible anymore, reset the view back
    if (!this.isViewVisible()) {
      if (this.isShowingForm) {
        this.switchFormGridVisibility();
      }
      // deselect any records
      this.viewGrid.deselectAllRecords(false, true);
    }

    // update this one at least before bailing out
    this.updateTabTitle();

    if (!this.isViewVisible() && !forceRefresh) {
      this.refreshContents = doRefreshWhenVisible;
      return;
    }

    if (!this.refreshContents && !doRefreshWhenVisible && !forceRefresh) {
      return;
    }

    // can be used by others to see that we are refreshing content
    this.refreshContents = true;

    // clear all our selections..
    // note the true parameter prevents autosave actions from happening
    // this should have been done before anyway
    this.viewGrid.deselectAllRecords(false, true);

    if (this.viewGrid.filterEditor) {
      this.viewGrid.clearFilter(false, true);
    }
    if (this.viewGrid.data && this.viewGrid.data.setCriteria) {
      this.viewGrid.data.setCriteria(null);
    }

    // hide the messagebar
    this.messageBar.hide();

    // allow default edit mode again
    this.allowDefaultEditMode = true;

    if (this.viewForm && this.isShowingForm) {
      this.viewForm.resetForm();
    }

    if (this.shouldOpenDefaultEditMode()) {
      this.openDefaultEditView();
    } else if (this.isShowingForm && !(this.allowDefaultEditMode && this.defaultEditMode)) {
      this.switchFormGridVisibility();
    }

    this.viewGrid.refreshContents();

    this.toolBar.updateButtonState(true);

    // if not visible or the parent also needs to be refreshed
    // enable the following code if we don't automatically select the first
    // record
    this.refreshChildViews();

    // set this at false at the end
    this.refreshContents = false;
  },

  refreshChildViews: function () {
    var i, length, tabViewPane;

    if (this.childTabSet) {
      length = this.childTabSet.tabs.length;
      for (i = 0; i < length; i++) {
        tabViewPane = this.childTabSet.tabs[i].pane;
        // force a refresh, only the visible ones will really 
        // be refreshed
        tabViewPane.doRefreshContents(true);
      }
    }
  },

  refreshMeAndMyChildViewsWithEntity: function (entity, excludedTabIds) {
    var i, length, tabViewPane, excludeTab = false;
    if (entity && excludedTabIds) {
      //Check is the tab has to be refreshed
      for (i = 0; i < excludedTabIds.length; i++) {
        if (excludedTabIds[i].match(this.tabId)) {
          excludeTab = true;
          // removes the tabId from the list of excluded, so it does
          // not have to be checked by the child tabs
          excludedTabIds.splice(i, 1);
          break;
        }
      }
      // If it the tab is not in the exclude list, refresh 
      // it if it belongs to the entered entity
      if (!excludeTab) {
        if (this.entity === entity) {
          this.doRefreshContents(true);
        }
      }
      // Refresh the child views of this tab
      if (this.childTabSet) {
        length = this.childTabSet.tabs.length;
        for (i = 0; i < length; i++) {
          tabViewPane = this.childTabSet.tabs[i].pane;
          tabViewPane.refreshMeAndMyChildViewsWithEntity(entity, excludedTabIds);
        }
      }
    }
  },

  shouldOpenDefaultEditMode: function () {
    // can open default edit mode if defaultEditMode is set
    // and this is the root view or a child view with a selected parent.
    var oneOrMoreSelected = this.viewGrid.data && this.viewGrid.data.lengthIsKnown && this.viewGrid.data.lengthIsKnown() && this.viewGrid.data.getLength() >= 1;
    return this.allowDefaultEditMode && oneOrMoreSelected && this.defaultEditMode && (this.isRootView || this.parentView.viewGrid.getSelectedRecords().length === 1);
  },

  // opendefaultedit view for a child view is only called
  // when a new parent is selected, in that case the 
  // edit view should be opened without setting the focus in the form
  openDefaultEditView: function (record) {
    if (!this.shouldOpenDefaultEditMode()) {
      return;
    }
    // preventFocus is treated as a boolean later
    var preventFocus = !this.isRootView;

    // don't open it again
    this.allowDefaultEditMode = false;

    // open form in edit mode
    if (record) {
      this.editRecord(record, preventFocus);
    } else if (this.viewGrid.data && this.viewGrid.data.getLength() > 0 && this.viewGrid.data.lengthIsKnown && this.viewGrid.data.lengthIsKnown()) {
      // edit the first record
      this.editRecord(this.viewGrid.getRecord(0), preventFocus);
    }
    // in other cases just show grid
  },

  // ** {{{ switchFormGridVisibility }}} **
  // Switch from form to grid view or the other way around
  switchFormGridVisibility: function () {
    if (!this.isShowingForm) {
      this.viewGrid.hide();
      this.statusBarFormLayout.show();
      this.statusBarFormLayout.setHeight('100%');
      if (this.isActiveView()) {
        this.viewForm.focus();
      }
      this.isShowingForm = true;
    } else {
      this.statusBarFormLayout.hide();
      // clear the form    
      this.viewForm.resetForm();
      this.isShowingForm = false;
      this.viewGrid.markForRedraw('showing');
      this.viewGrid.show();
      if (this.isActiveView()) {
        if (this.viewGrid.getSelectedRecords() && this.viewGrid.getSelectedRecords().length === 1) {
          this.viewGrid.focus();
        } else {
          this.viewGrid.focusInFirstFilterEditor();
        }
      }

      this.viewGrid.setHeight('100%');
    }
    this.updateTabTitle();
  },

  doHandleClick: function () {
    if (!this.childTabSet) {
      return;
    }
    if (this.state !== isc.OBStandardView.STATE_MID) {
      this.setHalfSplit();
      this.previousState = this.state;
      this.state = isc.OBStandardView.STATE_MID;
    }
  },

  doHandleDoubleClick: function () {
    var tempState;
    if (!this.childTabSet) {
      return;
    }
    tempState = this.state;
    this.state = this.previousState;
    if (this.previousState === isc.OBStandardView.STATE_BOTTOM_MAX) {
      this.setBottomMaximum();
    } else if (tempState === isc.OBStandardView.STATE_MID && this.previousState === isc.OBStandardView.STATE_MID) {
      this.setTopMaximum();
    } else if (this.previousState === isc.OBStandardView.STATE_MID) {
      this.setHalfSplit();
    } else if (this.previousState === isc.OBStandardView.STATE_TOP_MAX) {
      this.setTopMaximum();
    } else {
      isc.warn(this.previousState + ' not supported ');
    }
    this.previousState = tempState;
  },

  // ** {{{ editNewRecordGrid }}} **
  // Opens the inline grid editing for a new record.
  editNewRecordGrid: function (rowNum) {
    if (this.isShowingForm) {
      this.switchFormGridVisibility();
    }
    this.viewGrid.startEditingNew(rowNum);
  },

  // ** {{{ editRecord }}} **
  // Opens the edit form and selects the record in the grid, will refresh
  // child views also
  editRecord: function (record, preventFocus, focusFieldName) {

    this.messageBar.hide();

    if (!this.isShowingForm) {
      this.switchFormGridVisibility();
    }

    if (!record) { //  new case
      this.viewGrid.deselectAllRecords();
      this.refreshChildViews();
      this.viewForm.editNewRecord(preventFocus);
    } else {
      this.viewGrid.doSelectSingleRecord(record);

      // also handle the case that there are unsaved values in the grid
      // show them in the form
      var rowNum = this.viewGrid.getRecordIndex(record);
      this.viewForm.editRecord(this.viewGrid.getEditedRecord(rowNum), preventFocus, this.viewGrid.recordHasChanges(rowNum), focusFieldName);
    }
  },

  setMaximizeRestoreButtonState: function () {
    // single view, no maximize or restore
    if (!this.hasChildTabs && this.isRootView) {
      return;
    }
    // different cases:
    var theState = this.state;
    if (this.parentTabSet) {
      theState = this.parentTabSet.state;
    }

    if (theState === isc.OBStandardView.STATE_TOP_MAX) {
      this.statusBar.maximizeButton.hide();
      this.statusBar.restoreButton.show(true);
    } else if (theState === isc.OBStandardView.STATE_IN_MID) {
      this.statusBar.maximizeButton.show(true);
      this.statusBar.restoreButton.hide();
    } else if (!this.hasChildTabs) {
      this.statusBar.maximizeButton.hide();
      this.statusBar.restoreButton.show(true);
    } else {
      this.statusBar.maximizeButton.show(true);
      this.statusBar.restoreButton.hide();
    }
  },

  maximize: function () {
    if (this.parentTabSet) {
      this.parentTabSet.doHandleDoubleClick();
    } else {
      this.doHandleDoubleClick();
    }
    this.setMaximizeRestoreButtonState();
  },

  restore: function () {
    if (this.parentTabSet) {
      this.parentTabSet.doHandleDoubleClick();
    } else {
      this.doHandleDoubleClick();
    }
    this.setMaximizeRestoreButtonState();
  },

  // go to a next or previous record, if !next then the previous one is used
  editNextPreviousRecord: function (next) {
    var rowNum, newRowNum, newRecord, currentSelectedRecord = this.viewGrid.getSelectedRecord();
    if (!currentSelectedRecord) {
      return;
    }
    rowNum = this.viewGrid.data.indexOf(currentSelectedRecord);
    if (next) {
      newRowNum = rowNum + 1;
    } else {
      newRowNum = rowNum - 1;
    }
    newRecord = this.viewGrid.getRecord(newRowNum);
    if (!newRecord) {
      return;
    }
    this.viewGrid.scrollRecordToTop(newRowNum);
    this.editRecord(newRecord);
  },

  openDirectTabView: function (showContent) {
    // our content is done through the direct mode stuff
    this.refreshContents = false;

    if (this.parentTabSet && this.parentTabSet.getSelectedTab() !== this.tab) {
      this.parentTabSet.selectTab(this.tab);
    }

    if (showContent) {
      // this view is the last in the list then show it
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_MID);
      } else {
        this.doHandleClick();
      }
      this.setMaximizeRestoreButtonState();

      // show the form with the selected record
      // if there is one, otherwise we are in grid mode
      if (this.viewGrid.targetRecordId && !this.isShowingForm) {
        // hide the grid as it should not show up in a short flash
        this.viewGrid.hide();
      }
      // bypass the autosave logic
      this.standardWindow.setActiveView(this);
      this.viewGrid.isOpenDirectMode = true;
      this.viewGrid.isOpenDirectModeLeaf = true;
    }

    if (this.parentView) {
      this.parentView.openDirectTabView(false);
    }
  },

  // ** {{{ recordSelected }}} **
  // Is called when a record get's selected. Will refresh direct child views
  // which will again refresh their children.
  recordSelected: function () {
    // no change go away
    if (!this.hasSelectionStateChanged()) {
      return;
    }
    var me = this,
        callback = function () {
        me.delayedRecordSelected();
        };
    // wait 2 times longer than the fire on pause delay default
    this.fireOnPause('delayedRecordSelected_' + this.ID, callback, this.fireOnPauseDelay * 2);
  },

  // function is called with a small delay to handle the case that a user
  // navigates quickly over a grid
  delayedRecordSelected: function () {
    var length;

    // is actually a different parent selected, only then refresh children
    var differentRecordId = !this.lastRecordSelected || !this.viewGrid.getSelectedRecord() || this.viewGrid.getSelectedRecord().id !== this.lastRecordSelected.id;
    var selectedRecordId = this.viewGrid.getSelectedRecord() ? this.viewGrid.getSelectedRecord().id : null;

    this.updateLastSelectedState();
    this.updateTabTitle();

    // commented line because of https://issues.openbravo.com/view.php?id=18963
    // toolbar seems to be refreshed in any case
    // note only set session info if there is a record selected
    this.toolBar.updateButtonState(!selectedRecordId || this.isEditingGrid || this.isShowingForm);

    var tabViewPane = null,
        i;

    // refresh the tabs
    if (this.childTabSet && (differentRecordId || !this.isOpenDirectModeParent)) {
      length = this.childTabSet.tabs.length;
      for (i = 0; i < length; i++) {
        tabViewPane = this.childTabSet.tabs[i].pane;

        if (!selectedRecordId || !this.isOpenDirectModeParent || selectedRecordId !== tabViewPane.parentRecordId) {
          tabViewPane.doRefreshContents(true);
        }
      }
    }
    delete this.isOpenDirectModeParent;
  },

  // set childs to refresh when they are made visible
  setChildsToRefresh: function () {
    var length, i;

    if (this.childTabSet) {
      length = this.childTabSet.tabs.length;
      for (i = 0; i < length; i++) {
        if (!this.childTabSet.tabs[i].pane.isVisible()) {
          this.childTabSet.tabs[i].pane.refreshContents = true;
        }
      }
    }
  },

  hasSelectionStateChanged: function () {
    return ((this.viewGrid.getSelectedRecords() && this.viewGrid.getSelectedRecords().length !== this.lastRecordSelectedCount) || (this.viewGrid.getSelectedRecord() && this.viewGrid.getSelectedRecord().id !== this.lastRecordSelected.id)) || (this.lastRecordSelected && !this.viewGrid.getSelectedRecord());
  },

  updateLastSelectedState: function () {
    this.lastRecordSelectedCount = this.viewGrid.getSelectedRecords().length;
    this.lastRecordSelected = this.viewGrid.getSelectedRecord();
  },

  getParentId: function () {
    var parentRecord = this.getParentRecord();
    if (parentRecord) {
      return parentRecord.id;
    }
  },

  getParentRecord: function () {
    if (!this.parentView || !this.parentView.viewGrid.getSelectedRecords() || this.parentView.viewGrid.getSelectedRecords().length !== 1) {
      return null;
    }

    // a new parent is not a real parent
    if (this.parentView.viewGrid.getSelectedRecord()._new) {
      return null;
    }

    return this.parentView.viewGrid.getSelectedRecord();
  },

  updateTabTitle: function () {
    var prefix = '',
        postFix;
    var suffix = '';
    var hasChanged = this.isShowingForm && (this.viewForm.isNew || this.viewForm.hasChanged);
    hasChanged = hasChanged || (this.isEditingGrid && (this.viewGrid.hasErrors() || this.viewGrid.getEditForm().isNew || this.viewGrid.getEditForm().hasChanged));
    if (hasChanged) {
      prefix = '* ';
    }

    // store the original tab title
    if (!this.originalTabTitle) {
      this.originalTabTitle = this.tabTitle;
    }

    var identifier, tab, tabSet, title;

    if (this.viewGrid.getSelectedRecord()) {
      identifier = this.viewGrid.getSelectedRecord()[OB.Constants.IDENTIFIER];
      if (this.viewGrid.getSelectedRecord()._new) {
        identifier = OB.I18N.getLabel('OBUIAPP_New');
      }
      if (!identifier) {
        identifier = '';
      } else {
        identifier = ' - ' + identifier;
      }
    }

    // showing the form
    if (this.isShowingForm && this.viewGrid.getSelectedRecord() && this.viewGrid.getSelectedRecord()[OB.Constants.IDENTIFIER]) {

      if (!this.parentTabSet && this.viewTabId) {
        tab = OB.MainView.TabSet.getTab(this.viewTabId);
        tabSet = OB.MainView.TabSet;
        title = this.originalTabTitle + identifier;
      } else if (this.parentTabSet && this.tab) {
        tab = this.tab;
        tabSet = this.parentTabSet;
        title = this.originalTabTitle + identifier;
      }
    } else if (this.viewGrid.getSelectedRecords() && this.viewGrid.getSelectedRecords().length > 0) {
      if (this.viewGrid.getSelectedRecords().length === 1) {
        postFix = identifier;
      } else {
        postFix = ' - ' + OB.I18N.getLabel('OBUIAPP_SelectedRecords', [this.viewGrid.getSelectedRecords().length]);
      }
      if (!this.parentTabSet && this.viewTabId) {
        tab = OB.MainView.TabSet.getTab(this.viewTabId);
        tabSet = OB.MainView.TabSet;
        title = this.originalTabTitle + postFix;
      } else if (this.parentTabSet && this.tab) {
        tab = this.tab;
        tabSet = this.parentTabSet;
        title = this.originalTabTitle + postFix;
      }
    } else if (!this.parentTabSet && this.viewTabId) {
      // the root view
      tabSet = OB.MainView.TabSet;
      tab = OB.MainView.TabSet.getTab(this.viewTabId);
      title = this.originalTabTitle;
    } else if (this.parentTabSet && this.tab) {
      // the check on this.tab is required for the initialization phase
      // only show a count if there is one parent
      tab = this.tab;
      tabSet = this.parentTabSet;

      if (!this.parentView.viewGrid.getSelectedRecords() || this.parentView.viewGrid.getSelectedRecords().length !== 1) {
        title = this.originalTabTitle;
      } else if (this.recordCount) {
        title = this.originalTabTitle + ' (' + this.recordCount + ')';
      } else {
        title = this.originalTabTitle;
      }
    }

    // happens when a tab gets closed
    if (!tab) {
      return;
    }

    if (title) {

      // show a prompt with the title info
      tab.prompt = title;
      tab.showPrompt = true;
      tab.hoverWidth = 150;

      // trunc the title if it too large 
      title = OB.Utilities.truncTitle(title);

      // add the prefix/suffix here to prevent cutoff on that
      title = prefix + title + suffix;
      tabSet.setTabTitle(tab, title);
    }

    // added check on tab as initially it is not set
    if (this.isRootView && tab) {
      // update the document title
      document.title = 'Openbravo - ' + tab.title;
    }
  },

  isViewVisible: function () {
    // this prevents data requests for minimized tabs
    // note this.tab.isVisible is done as the tab is visible earlier than
    // the pane
    var visible = this.tab && this.tab.isDrawn() && this.tab.pane.isDrawn() && this.tab.pane.isVisible();
    return visible && (!this.parentTabSet || this.parentTabSet.getSelectedTabNumber() === this.parentTabSet.getTabNumber(this.tab));
  },

  // ++++++++++++++++++++ Button Actions ++++++++++++++++++++++++++
  // make a special refresh:
  // - refresh the current selected record without changing the selection
  // - refresh the parent/grand-parent in the same way without changing the selection
  // - recursive to children: refresh the children, put the children in grid mode and refresh
  refresh: function (refreshCallback, autoSaveDone) {
    var me = this,
        view = this,
        actionObject, formRefresh, callback;

    // first save what we have edited
    if (!autoSaveDone) {
      actionObject = {
        target: this,
        method: this.refresh,
        parameters: [refreshCallback, true]
      };
      this.standardWindow.doActionAfterAutoSave(actionObject, false);
      return;
    }

    if (this.viewForm && this.viewForm.contextInfo) {
      this.viewForm.contextInfo = null;
    }

    formRefresh = function () {
      if (refreshCallback) {
        refreshCallback();
      }
      me.viewForm.refresh();
    };

    if (!this.isShowingForm) {
      this.viewGrid.refreshGrid(refreshCallback);
    } else {
      if (this.viewForm.hasChanged) {
        callback = function (ok) {
          if (ok) {
            view.viewGrid.refreshGrid(formRefresh);
          }
        };
        isc.ask(OB.I18N.getLabel('OBUIAPP_ConfirmRefresh'), callback);
      } else {
        this.viewGrid.refreshGrid(formRefresh);
      }
    }
  },

  refreshParentRecord: function (callBackFunction) {
    if (this.parentView) {
      this.parentView.refreshCurrentRecord(callBackFunction);
    }
  },

  refreshCurrentRecord: function (callBackFunction) {
    var me = this,
        record, criteria, callback;

    if (!this.viewGrid.getSelectedRecord()) {
      return;
    }

    record = this.viewGrid.getSelectedRecord();

    criteria = {
      operator: 'and',
      _constructor: "AdvancedCriteria",
      criteria: []
    };

    // add a dummy criteria to force a fetch
    criteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());

    // and add a criteria for the record itself
    criteria.criteria.push({
      fieldName: OB.Constants.ID,
      operator: 'equals',
      value: record.id
    });

    callback = function (resp, data, req) {
      // this line does not work, but it should:
      //      me.getDataSource().updateCaches(resp, req);
      // therefore do an explicit update of the visual components
      if (me.isShowingForm) {
        me.viewForm.refresh();
      }
      if (me.viewGrid.data) {
        var recordIndex = me.viewGrid.getRecordIndex(me.viewGrid.getSelectedRecord());
        me.viewGrid.data.updateCacheData(data, req);
        me.viewGrid.selectRecord(me.viewGrid.getRecord(recordIndex));
        me.viewGrid.refreshRow(recordIndex);
        me.viewGrid.redraw();
      }


      if (callBackFunction) {
        callBackFunction();
      }
    };

    if (this.viewForm && this.viewForm.contextInfo) {
      this.viewForm.contextInfo = null;
    }

    this.getDataSource().fetchData(criteria, callback);
    this.refreshParentRecord(callBackFunction);
  },

  hasNotChanged: function () {
    var view = this,
        form = view.viewForm,
        length, selectedRecords, grid = view.viewGrid,
        allRowsHaveErrors, hasErrors = false,
        editRow, i;
    if (view.isShowingForm) {
      if (form.isNew) {
        return false;
      }
      return form.isSaving || form.readOnly || !view.hasValidState() || !form.hasChanged;
    } else if (view.isEditingGrid) {
      editRow = view.viewGrid.getEditRow();
      hasErrors = view.viewGrid.rowHasErrors(editRow);
      form = grid.getEditForm();
      return !form.isNew && !hasErrors && (form.isSaving || form.readOnly || !view.hasValidState() || !form.hasChanged);
    } else {
      selectedRecords = grid.getSelectedRecords();
      allRowsHaveErrors = true;
      length = selectedRecords.length;
      for (i = 0; i < length; i++) {
        var rowNum = grid.getRecordIndex(selectedRecords[i]);
        allRowsHaveErrors = allRowsHaveErrors && grid.rowHasErrors(rowNum);
      }
      return selectedRecords.length === 0 || !allRowsHaveErrors;
    }
  },

  saveRow: function () {
    if (this.isEditingGrid) {
      this.viewGrid.endEditing();
    } else {
      this.viewForm.saveRow();
    }
  },

  deleteSelectedRows: function (autoSaveDone) {
    var msg, dialogTitle, view = this,
        deleteCount, callback;

    if (!this.readOnly) {
      // first save what we have edited
      if (!autoSaveDone) {
        var actionObject = {
          target: this,
          method: this.deleteSelectedRows,
          parameters: [true]
        };
        this.standardWindow.doActionAfterAutoSave(actionObject, false);
        return;
      }

      deleteCount = this.viewGrid.getSelection().length;

      if (deleteCount === 1) {
        msg = OB.I18N.getLabel('OBUIAPP_DeleteConfirmationSingle');
        dialogTitle = OB.I18N.getLabel('OBUIAPP_DialogTitle_DeleteRecord');
      } else {
        msg = OB.I18N.getLabel('OBUIAPP_DeleteConfirmationMultiple', [this.viewGrid.getSelection().length]);
        dialogTitle = OB.I18N.getLabel('OBUIAPP_DialogTitle_DeleteRecords');
      }

      callback = function (ok) {
        var i, doUpdateTotalRows, data, deleteData, error, recordInfos = [],
            length, removeCallBack, selection;

        removeCallBack = function (resp, data, req) {
          var length, localData = resp.dataObject || resp.data || data,
              i, updateTotalRows;

          if (!localData) {
            // bail out, an error occured which should be displayed to the user now
            return;
          }
          var status = resp.status;
          if (localData && localData.hasOwnProperty('status')) {
            status = localData.status;
          }
          if (localData && localData.response && localData.response.hasOwnProperty('status')) {
            status = localData.response.status;
          }
          if (status === isc.RPCResponse.STATUS_SUCCESS) {
            if (view.isShowingForm) {
              view.switchFormGridVisibility();
            }
            view.messageBar.setMessage(isc.OBMessageBar.TYPE_SUCCESS, null, OB.I18N.getLabel('OBUIAPP_DeleteResult', [deleteCount]));
            if (deleteData) {
              // note totalrows is used when inserting a new row, to determine after which
              // record to add a new row
              updateTotalRows = (view.viewGrid.data.getLength() === view.viewGrid.data.totalRows);
              // deleteData is computed below
              length = deleteData.ids.length;
              for (i = 0; i < length; i++) {
                recordInfos.push({
                  id: deleteData.ids[i]
                });
              }
              view.viewGrid.data.handleUpdate('remove', recordInfos, false, req);
              if (updateTotalRows) {
                view.viewGrid.data.totalRows = view.viewGrid.data.getLength();
              }
            } else if (doUpdateTotalRows) {
              view.viewGrid.data.totalRows = view.viewGrid.data.getLength();
            }
            view.viewGrid.updateRowCountDisplay();
            view.refreshChildViews();
            view.refreshParentRecord();
          } else {
            // get the error message from the dataObject 
            if (localData.response && localData.response.error && localData.response.error.message) {
              error = localData.response.error;
              if (error.type && error.type === 'user') {
                view.messageBar.setLabel(isc.OBMessageBar.TYPE_ERROR, null, error.message, error.params);
              } else if (error.message && error.params) {
                view.messageBar.setLabel(isc.OBMessageBar.TYPE_ERROR, null, error.message, error.params);
              } else if (error.message) {
                view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, error.message);
              } else {
                view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('OBUIAPP_DeleteResult', [0]));
              }
            }
          }
        };

        if (ok) {
          selection = view.viewGrid.getSelection().duplicate();
          // deselect the current records
          view.viewGrid.deselectAllRecords();

          if (selection.length > 1) {
            deleteData = {};
            deleteData.entity = view.entity;
            deleteData.ids = [];
            length = selection.length;
            for (i = 0; i < length; i++) {
              deleteData.ids.push(selection[i][OB.Constants.ID]);
            }
            OB.RemoteCallManager.call('org.openbravo.client.application.MultipleDeleteActionHandler', deleteData, {}, removeCallBack, {
              refreshGrid: true
            });
          } else {
            // note totalrows is used when inserting a new row, to determine after which
            // record to add a new row
            doUpdateTotalRows = (view.viewGrid.data.getLength() === view.viewGrid.data.totalRows);
            // note remove data expects only the id, the record key as the first param
            view.viewGrid.removeData({
              id: selection[0].id
            }, removeCallBack, {});
          }
        }
      };
      isc.ask(msg, callback, {
        title: dialogTitle
      });
    }
  },

  newRow: function (rowNum) {
    var actionObject = {
      target: this,
      method: this.editNewRecordGrid,
      parameters: [rowNum]
    };
    this.standardWindow.doActionAfterAutoSave(actionObject, false);
  },

  newDocument: function () {
    var actionObject = {
      target: this,
      method: this.editRecord,
      parameters: null
    };
    this.standardWindow.doActionAfterAutoSave(actionObject, false);
  },

  undo: function () {
    var view = this,
        callback, form, grid, errorRows, i, length;
    view.messageBar.hide(true);
    if (this.isEditingGrid) {
      grid = view.viewGrid;
      // the editing grid will take care of the confirmation
      grid.cancelEditing();

      // undo edit in all records with errors
      if (grid.hasErrors()) {
        errorRows = grid.getErrorRows();
        length = errorRows.length;
        for (i = 0; i < length; i++) {
          grid.selectRecord(grid.getRecord(errorRows[i]));
        }
        grid.undoEditSelectedRows();
      }
      return;
    } else if (this.isShowingForm) {
      form = this.viewForm;
    } else {
      // selected records
      grid = view.viewGrid;
    }
    if (form) {
      form.undo();
    } else {
      grid.undoEditSelectedRows();
    }
  },

  // ++++++++++++++++++++ Parent-Child Tab Handling ++++++++++++++++++++++++++
  convertToPercentageHeights: function () {
    if (!this.members[1]) {
      return;
    }
    var height = this.members[1].getHeight();
    var percentage = ((height / this.getHeight()) * 100);
    // this.members[0].setHeight((100 - percentage) + '%');
    this.members[0].setHeight('*');
    this.members[1].setHeight(percentage + '%');
  },

  setTopMaximum: function () {
    this.setHeight('100%');
    if (this.members[1]) {
      this.members[1].setState(isc.OBStandardView.STATE_MIN);
      this.convertToPercentageHeights();
    } else {
      this.members[0].setHeight('100%');
    }
    this.members[0].show();
    this.state = isc.OBStandardView.STATE_TOP_MAX;
    this.setMaximizeRestoreButtonState();
  },

  setBottomMaximum: function () {
    if (this.members[1]) {
      this.members[0].hide();
      this.members[1].setHeight('100%');
    }
    this.state = isc.OBStandardView.STATE_BOTTOM_MAX;
    this.setMaximizeRestoreButtonState();
  },

  setHalfSplit: function () {
    this.setHeight('100%');
    var i, tab, pane;
    if (this.members[1]) {
      // divide the space between the first and second level
      if (this.members[1].draggedHeight) {
        this.members[1].setHeight(this.members[1].draggedHeight);
        this.convertToPercentageHeights();
      } else {
        // NOTE: noticed that when resizing multiple members in a layout, that it 
        // makes a difference what the order of resizing is, first resize the 
        // one which will be larger, then the one which will be smaller.
        this.members[1].setHeight('50%');
        this.members[0].setHeight('50%');
      }
      this.members[1].setState(isc.OBStandardView.STATE_IN_MID);
    } else {
      this.members[0].setHeight('100%');
    }
    this.members[0].show();
    this.state = isc.OBStandardView.STATE_MID;
    this.setMaximizeRestoreButtonState();
  },

  getCurrentValues: function () {
    var ret;
    if (this.isShowingForm) {
      ret = this.viewForm.getValues();
    } else if (this.isEditingGrid) {
      ret = isc.addProperties({}, this.viewGrid.getSelectedRecord(), this.viewGrid.getEditForm().getValues());
    } else {
      ret = this.viewGrid.getSelectedRecord();
    }
    // return an empty object if ret is not set
    // this happens when a new record could not be saved
    // and the form view is switched for grid view
    return ret || {};
  },

  getPropertyFromColumnName: function (columnName) {
    var length = this.view.propertyToColumns.length,
        i;
    for (i = 0; i < length; i++) {
      var propDef = this.view.propertyToColumns[i];
      if (propDef.dbColumn === columnName) {
        return propDef.property;
      }
    }
    return null;
  },

  getPropertyDefinitionFromDbColumnName: function (columnName) {
    var length = this.propertyToColumns.length,
        i;
    for (i = 0; i < length; i++) {
      var propDef = this.propertyToColumns[i];
      if (propDef.dbColumn === columnName) {
        return propDef;
      }
    }
    return null;
  },

  getPropertyFromDBColumnName: function (columnName) {
    var length = this.propertyToColumns.length,
        i;
    for (i = 0; i < length; i++) {
      var propDef = this.propertyToColumns[i];
      if (propDef.dbColumn === columnName) {
        return propDef.property;
      }
    }
    return null;
  },

  getPropertyDefinitionFromInpColumnName: function (columnName) {
    var length = this.propertyToColumns.length,
        i;
    for (i = 0; i < length; i++) {
      var propDef = this.propertyToColumns[i];
      if (propDef.inpColumn === columnName) {
        return propDef;
      }
    }
    return null;
  },

  //++++++++++++++++++ Reading context ++++++++++++++++++++++++++++++
  getContextInfo: function (onlySessionProperties, classicMode, forceSettingContextVars, convertToClassicFormat) {
    var contextInfo = {},
        addProperty, rowNum, properties, i, p;
    // if classicmode is undefined then both classic and new props are used
    var classicModeUndefined = (typeof classicMode === 'undefined');
    var value, field, record, form, component, propertyObj, type, length;

    if (classicModeUndefined) {
      classicMode = true;
    }

    // a special case, the editform has been build but it is not present yet in the
    // form, so isEditingGrid is true but the edit form is not there yet, in that 
    // case use the viewGrid as component and the selected record
    if (this.isEditingGrid && this.viewGrid.getEditForm()) {
      rowNum = this.viewGrid.getEditRow();
      if (rowNum || rowNum === 0) {
        record = isc.addProperties({}, this.viewGrid.getRecord(rowNum), this.viewGrid.getEditValues(rowNum));
      } else {
        record = isc.addProperties({}, this.viewGrid.getSelectedRecord());
      }
      component = this.viewGrid.getEditForm();
      form = component;
    } else if (this.isShowingForm) {
      // note on purpose not calling form.getValues() as this will cause extra requests 
      // in case of a picklist
      record = isc.addProperties({}, this.viewGrid.getSelectedRecord(), this.viewForm.values);
      component = this.viewForm;
      form = component;
    } else {
      record = this.viewGrid.getSelectedRecord();
      rowNum = this.viewGrid.getRecordIndex(record);
      if (rowNum || rowNum === 0) {
        record = isc.addProperties({}, record, this.viewGrid.getEditValues(rowNum));
      }
      component = this.viewGrid;
    }

    properties = this.propertyToColumns;

    if (record) {

      // add the id of the record itself also if not set
      if (!record[OB.Constants.ID] && this.viewGrid.getSelectedRecord()) {
        // if in edit mode then the grid always has the current record selected
        record[OB.Constants.ID] = this.viewGrid.getSelectedRecord()[OB.Constants.ID];
      }

      // New records in grid have a dummy id (see OBViewGrid.createNewRecordForEditing)
      // whereas new form records don't have it. This temporary id starts with _. Removing this
      // id so it behaves in the same way in form and grid
      if (record[OB.Constants.ID] && record[OB.Constants.ID].indexOf('_') === 0) { // startsWith a SC function, is slower than indexOf
        record[OB.Constants.ID] = undefined;
      }

      length = properties.length;
      for (i = 0; i < length; i++) {
        propertyObj = properties[i];
        value = record[propertyObj.property];
        field = component.getField(propertyObj.property);
        addProperty = propertyObj.sessionProperty || !onlySessionProperties;
        if (addProperty) {
          if (classicMode) {
            if (propertyObj.type && convertToClassicFormat) {
              type = isc.SimpleType.getType(propertyObj.type);
              if (type.createClassicString) {
                contextInfo[properties[i].inpColumn] = type.createClassicString(value);
              } else {
                contextInfo[properties[i].inpColumn] = this.convertContextValue(value, propertyObj.type);
              }
            } else {
              contextInfo[properties[i].inpColumn] = this.convertContextValue(value, propertyObj.type);
            }
          } else {
            // surround the property name with @ symbols to make them different
            // from filter criteria and such          
            contextInfo['@' + this.entity + '.' + properties[i].property + '@'] = this.convertContextValue(value, propertyObj.type);
          }
        }
      }

      if (!onlySessionProperties) {
        for (p in this.standardProperties) {
          if (this.standardProperties.hasOwnProperty(p)) {
            if (classicMode) {
              contextInfo[p] = this.convertContextValue(this.standardProperties[p]);
            } else {
              // surround the property name with @ symbols to make them different
              // from filter criteria and such          
              contextInfo['@' + this.entity + '.' + p + '@'] = this.convertContextValue(this.standardProperties[p]);
            }
          }
        }
      }
    }
    if (form || forceSettingContextVars) {
      if (!form) {
        form = this.viewForm;
      }
      isc.addProperties(contextInfo, form.auxInputs);
      isc.addProperties(contextInfo, form.hiddenInputs);
      isc.addProperties(contextInfo, form.sessionAttributes);
    }

    if (this.parentView) {
      // parent properties do not override contextInfo
      var parentContextInfo = this.parentView.getContextInfo(onlySessionProperties, classicMode, forceSettingContextVars, convertToClassicFormat);
      contextInfo = isc.addProperties(parentContextInfo, contextInfo);
    }

    return contextInfo;
  },

  convertContextValue: function (value, type) {
    var isTime = isc.isA.Date(value) && type && isc.SimpleType.getType(type).inheritsFrom === 'time';
    if (isTime) {
      return value.getUTCHours() + ':' + value.getUTCMinutes() + ':' + value.getUTCSeconds();
    }
    return value;
  },

  getPropertyDefinition: function (property) {
    var properties = this.propertyToColumns,
        i, length = properties.length;
    for (i = 0; i < length; i++) {
      if (property === properties[i].property) {
        return properties[i];
      }
    }
    return null;
  },

  setContextInfo: function (sessionProperties, callbackFunction, forced) {
    // no need to set the context in this case
    if (!forced && (this.isEditingGrid || this.isShowingForm)) {
      if (callbackFunction) {
        callbackFunction();
      }
      return;
    }

    if (!sessionProperties) {
      sessionProperties = this.getContextInfo(true, true, false, true);
    }

    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', sessionProperties, {
      MODE: 'SETSESSION',
      TAB_ID: this.tabId,
      PARENT_ID: this.getParentId(),
      ROW_ID: this.viewGrid.getSelectedRecord() ? this.viewGrid.getSelectedRecord().id : this.getCurrentValues().id
    }, callbackFunction);

  },

  getTabMessage: function (forcedTabId) {
    var tabId = forcedTabId || this.tabId,
        callback;

    callback = function (resp, data, req) {
      if (req.clientContext && data.type && (data.text || data.title)) {
        req.clientContext.messageBar.setMessage(isc.OBMessageBar[data.type], data.title, data.text);
      }
    };

    OB.RemoteCallManager.call('org.openbravo.client.application.window.GetTabMessageActionHandler', {
      tabId: tabId
    }, null, callback, this);
  },

  getFormPersonalization: function (checkSavedView) {
    if (!this.standardWindow) {
      // happens during the initialization
      return null;
    }
    return this.standardWindow.getFormPersonalization(this, checkSavedView);
  },

  // TODO: consider caching the prepared fields on
  // class level, the question is if it is faster
  // as then a clone action needs to be done
  prepareFields: function () {
    // first compute the gridfields and then the formfields
    this.prepareViewFields(this.fields);
    this.gridFields = this.prepareGridFields(this.fields);
    this.formFields = this.prepareFormFields(this.fields);
  },

  prepareFormFields: function (fields) {
    var i, length = fields.length,
        result = [],
        fld;

    for (i = 0; i < length; i++) {
      fld = isc.shallowClone(fields[i]);
      result.push(this.setFieldFormProperties(fld));

      if (fld.firstFocusedField) {
        this.firstFocusedField = fld.name;
      }
    }

    return result;
  },

  setFieldFormProperties: function (fld) {
    var onChangeFunction;

    if (fld.displayed === false) {
      fld.visible = false;
      fld.alwaysTakeSpace = false;
    }

    if (!fld.width) {
      fld.width = '*';
    }
    if (fld.showIf && !fld.originalShowIf) {
      fld.originalShowIf = fld.showIf;
      fld.showIf = function (item, value, form, values) {
        var currentValues = values || form.view.getCurrentValues(),
            context = form.getCachedContextInfo(),
            originalShowIfValue = false;

        OB.Utilities.fixNull250(currentValues);

        try {
          originalShowIfValue = this.originalShowIf(item, value, form, currentValues, context);
        } catch (_exception) {
          isc.warn(_exception + ' ' + _exception.message + ' ' + _exception.stack);
        }

        return !this.hiddenInForm && context && originalShowIfValue;
      };
    }
    if (fld.type === 'OBAuditSectionItem') {
      var expandAudit = OB.PropertyStore.get('ShowAuditDefault', this.standardProperties.inpwindowId);
      if (expandAudit && expandAudit === 'Y') {
        fld.sectionExpanded = true;
      }
    }

    if (fld.onChangeFunction) {
      // the default
      fld.onChangeFunction.sort = 50;

      OB.OnChangeRegistry.register(this.tabId, fld.name, fld.onChangeFunction, 'default');
    }

    return fld;
  },

  // prepare stuff on view level
  prepareViewFields: function (fields) {
    var i, length = fields.length,
        fld;

    // start with the initial ones
    this.propertyToColumns = this.initialPropertyToColumns.duplicate();

    this.propertyToColumns.push({
      property: this.standardProperties.keyProperty,
      dbColumn: this.standardProperties.keyColumnName,
      inpColumn: this.standardProperties.inpKeyName,
      sessionProperty: true,
      type: this.standardProperties.keyPropertyType
    });

    for (i = 0; i < length; i++) {
      fld = fields[i];
      if (fld.columnName) {
        this.propertyToColumns.push({
          property: fld.name,
          dbColumn: fld.columnName,
          inpColumn: fld.inpColumnName,
          sessionProperty: fld.sessionProperty,
          type: fld.type
        });
      }
    }
  },

  prepareGridFields: function (fields) {
    var result = [],
        i, length = fields.length,
        fld, type, expandFieldNames, hoverFunction, yesNoFormatFunction;

    hoverFunction = function (record, value, rowNum, colNum, grid) {
      return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : this.name)]);
    };

    yesNoFormatFunction = function (value, record, rowNum, colNum, grid) {
      return OB.Utilities.getYesNoDisplayValue(value);
    };

    for (i = 0; i < length; i++) {
      fld = fields[i];
      if (!fld.gridProps) {
        continue;
      }
      fld = isc.shallowClone(fields[i]);

      if (fld.showHover) {
        fld.hoverHTML = hoverFunction;
      }

      if (fld.gridProps.displaylength) {
        fld.gridProps.width = isc.OBGrid.getDefaultColumnWidth(fld.gridProps.displaylength);
      } else {
        fld.gridProps.width = isc.OBGrid.getDefaultColumnWidth(30);
      }

      // move the showif defined on form level
      // otherwise it interferes with the grid level
      if (fld.showIf) {
        fld.formShowIf = fld.showIf;
        delete fld.showIf;
      }

      isc.addProperties(fld, fld.gridProps);

      // correct some stuff coming from the form fields
      if (fld.displayed === false) {
        fld.visible = true;
        fld.alwaysTakeSpace = true;
      }

      fld.canExport = (fld.canExport === false ? false : true);
      fld.canHide = (fld.canHide === false ? false : true);
      fld.canFilter = (fld.canFilter === false ? false : true);
      fld.filterOnKeypress = (fld.filterOnKeypress === false ? false : true);
      fld.escapeHTML = (fld.escapeHTML === false ? false : true);
      fld.prompt = fld.title;
      fld.editorProperties = isc.addProperties({}, fld, isc.shallowClone(fld.editorProps));
      this.setFieldFormProperties(fld.editorProperties);

      if (fld.disabled) {
        fld.editorProperties.disabled = true;
      }
      fld.disabled = false;

      if (fld.yesNo) {
        fld.formatCellValue = yesNoFormatFunction;
      }

      type = isc.SimpleType.getType(fld.type);
      if (type.editorType && !fld.editorType) {
        fld.editorType = type.editorType;
      }

      if (type.filterEditorType && !fld.filterEditorType) {
        fld.filterEditorType = type.filterEditorType;
      }

      if (fld.fkField) {
        fld.displayField = fld.name + '.' + OB.Constants.IDENTIFIER;
        fld.valueField = fld.name;
      }

      if (fld.validationFn) {

        if (!fld.validators) {
          fld.validators = [];
        }

        fld.validators.push({
          type: 'custom',
          condition: fld.validationFn
        });
      }

      if (!fld.filterEditorProperties) {
        fld.filterEditorProperties = {};
      }
      fld.filterEditorProperties.required = false;

      result.push(fld);
    }

    // sort according to displaylength, for the autoexpandfieldnames
    result.sort(function (v1, v2) {
      var t1 = v1.displaylength,
          t2 = v2.displaylength,
          l1 = v1.length,
          l2 = v2.length,
          n1 = v1.name,
          n2 = v2.name;
      if (!t1 && !t2) {
        return 0;
      }
      if (!t1) {
        return 1;
      }
      if (!t2) {
        return -1;
      }
      if (t1 > t2) {
        return -1;
      } else if (t1 === t2) {
        if (!l1 && !l2) {
          return 0;
        }
        if (!l1) {
          return 1;
        }
        if (!l2) {
          return -1;
        }
        if (l1 > l2) {
          return -1;
        } else if (l1 === l2) {
          if (v1.name > v2.name) {
            return 1;
          } else {
            return -1;
          }
        }
        return 1;
      }
      return 1;
    });

    this.autoExpandFieldNames = [];
    length = result.length;
    for (i = 0; i < length; i++) {
      if (result[i].autoExpand) {
        this.autoExpandFieldNames.push(result[i].name);
      }
    }
    // sort according to the sortnum
    // that's how they are displayed
    result.sort(function (v1, v2) {
      var t1 = v1.sort,
          t2 = v2.sort;
      if (!t1 && !t2) {
        return 0;
      }
      if (!t1) {
        return -1;
      }
      if (!t2) {
        return 1;
      }
      if (t1 < t2) {
        return -1;
      } else if (t1 === t2) {
        return 0;
      }
      return 1;
    });

    return result;
  }

});