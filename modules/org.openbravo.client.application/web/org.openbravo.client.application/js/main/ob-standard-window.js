/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may obtain  a copy of
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
isc.ClassFactory.defineClass('OBStandardWindow', isc.VLayout);

isc.OBStandardWindow.addClassProperties({
  COMMAND_NEW: 'NEW' // tells the window to open the first tab in new mode
});

// = OBStandardWindow =
//
// Represents the root container for an Openbravo window consisting of a
// hierarchy of tabs. Each tab is represented with an instance of the
// OBStandardView.
//
// The standard window can be opened as a result of a click on a link
// in another tab. In this case the window should open all tabs from the
// target tab up to the root tab. The flow starts by opening the deepest tab.
// This tab then forces the ancestor tabs to read data (asynchronously in sequence). This is 
// controlled through the isOpenDirectMode flag which tells a tab that it 
// should open its grid using a target record id and use the parent property
// to define the parent id by which to filter (if the tab has a parent). 
// 
isc.OBStandardWindow.addProperties({
  toolBarLayout: null,
  
  view: null,
  
  viewProperties: null,
  
  activeView: null,
  
  views: [],
  
  stackZIndex: 'firstOnTop',
  align: 'center',
  defaultLayoutAlign: 'center',

  // is set when a form or grid editing results in dirty data
  // in the window
  dirtyEditForm: null,
  
  initWidget: function(){

    this.views = [];

    this.processLayout = isc.VStack.create({
      height: '100%',
      width: '100%',
      overflow: 'auto',
      visibility: 'hidden'
    });

    this.addMember(this.processLayout);
    
    this.toolBarLayout = isc.HLayout.create({
      mouseDownCancelParentPropagation: true,
      width: '100%',
      height: 1, // is set by its content
      overflow: 'visible'
    });
    
    if (this.targetTabId) {
      // is used as a flag so that we are in direct link mode
      // prevents extra fetch data actions
      this.directTabInfo = {};
    }
        
    this.addMember(this.toolBarLayout);
    
    this.viewProperties.standardWindow = this;
    this.viewProperties.isRootView = true;
    if (this.command === isc.OBStandardWindow.COMMAND_NEW) {
      this.viewProperties.allowDefaultEditMode = false;
    }
    this.viewState = OB.PropertyStore.get('OBUIAPP_GridConfiguration', this.windowId);
    this.view = isc.OBStandardView.create(this.viewProperties);
    this.addView(this.view);
    this.addMember(this.view);

    this.Super('initWidget', arguments);
    
    // is set later after creation
    this.view.tabTitle = this.tabTitle;
    
    // retrieve user specific window settings from the server
    // they are stored at class level to only do the call once
    // note this if is not done inside the method as this 
    // method is also called explicitly from the personalization window
    if (!this.getClass().windowSettingsRead) {
      this.readWindowSettings();
    } else if (this.getClass().personalization) {
      this.setPersonalization(this.getClass().personalization);
    }
  },

  openProcess: function (params) {
    var parts = this.getPrototype().Class.split('_'), 
        len = parts.length,
        className = '_',
        tabSet = OB.MainView.TabSet, vStack;
    
    if (params.windowId) {
      className = className + params.windowId;
      if(len === 3) {
        // debug mode, we have added _timestamp
        className = className + '_' + parts[2];
      }

      if (isc[className]) {
        this.selectedState = this.activeView && this.activeView.viewGrid && this.activeView.viewGrid.getSelectedState();
        this.runningProcess = isc[className].create(isc.addProperties({}, params, {
          parentWindow: this
        }));

        this.processLayout.addMember(this.runningProcess);
        this.toolBarLayout.hide();
        this.view.hide();
        this.processLayout.show();
      }
    }
  },

  refresh: function () {
    var currentView = this.activeView, afterRefresh;

    afterRefresh = function () {
      // Refresh context view
      //contextView.getTabMessage();
      currentView.toolBar.refreshCustomButtons();
//
//      if (contextView !== currentView && currentView.state === isc.OBStandardView.STATE_TOP_MAX) {
//        // Executing an action defined in parent tab, current tab is maximized,
//        // let's set half for each in order to see the message
//        contextView.setHalfSplit();
//      }

      // Refresh in order to show possible new records
      currentView.refresh(null, false, true);
    };

    if(!currentView) {
      return;
    }

    if(this.selectedState) {
      currentView.viewGrid.setSelectedState(this.selectedState);
      this.selectedState = null;
    }

    if (currentView.parentView) {
      currentView.parentView.setChildsToRefresh();
    } else {
      currentView.setChildsToRefresh();
    }

    if (currentView.viewGrid.getSelectedRecord()) {
      // There is a record selected, refresh it and its parent
      currentView.refreshCurrentRecord(afterRefresh);
    } else {
      // No record selected, refresh parent
      currentView.refreshParentRecord(afterRefresh);
    }
  },

  readWindowSettings: function() {
    var standardWindow = this;
    
    OB.RemoteCallManager.call('org.openbravo.client.application.WindowSettingsActionHandler', null, {
      windowId: this.windowId
    }, function(response, data, request){
      standardWindow.setWindowSettings(data);
    });    
  },
  
  // set window specific user settings, purposely set on class level
  setWindowSettings: function(data) {
    var i, views, length;

    if (data && data.personalization) {
      this.setPersonalization(data.personalization);
    }

    this.getClass().windowSettingsRead = true;

    this.getClass().uiPattern = data.uiPattern;
    this.getClass().autoSave = data.autoSave;
    this.getClass().showAutoSaveConfirmation = data.showAutoSaveConfirmation;
    // set the views to readonly
    length = this.views.length;
    for (i = 0; i < length; i++) {
      this.views[i].setReadOnly(data.uiPattern[this.views[i].tabId] === isc.OBStandardView.UI_PATTERN_READONLY);
      this.views[i].setSingleRecord(data.uiPattern[this.views[i].tabId] === isc.OBStandardView.UI_PATTERN_SINGLERECORD);
      this.views[i].toolBar.updateButtonState(true);
    }
  },
  
  setPersonalization: function(personalization) {
    var i, defaultView, persDefaultValue, views, length, me = this;
    
    // only personalize if there is a professional license
    if (!OB.Utilities.checkProfessionalLicense(null, true)) {
      return;
    }
    
    // cache the original view so that it can be restored
    if (!this.getClass().originalView) {
      this.getClass().originalView = {};
      this.getClass().originalView.personalizationId = 'dummyId';
      this.getClass().originalView.viewDefinition = OB.Personalization.getViewDefinition(this, '', false);
      this.getClass().originalView.viewDefinition.name = OB.I18N.getLabel('OBUIAPP_StandardView');
      this.getClass().originalView.canDelete = false;
      
      // and clone the original view so that it can't get updated accidentally
      this.getClass().originalView = isc.clone(this.getClass().originalView);
    }

    this.getClass().personalization = personalization;
    
    persDefaultValue = OB.PropertyStore.get('OBUIAPP_DefaultSavedView', this.windowId);
    
    // find the default view, the personalizations are
    // returned in order of prio, then do sort by name
    if (this.getClass().personalization.views) {
      views = this.getClass().personalization.views;
      length = views.length;
      if (persDefaultValue) {
        for (i = 0; i < length; i++) {
          if (persDefaultValue === views[i].personalizationId) {
            defaultView = views[i];
            break;
          }
        }
      }
      if (!defaultView) {
        for (i = 0; i < length; i++) {
          if (views[i].viewDefinition && views[i].viewDefinition.isDefault) {
            defaultView = views[i];
            break;
          }
        }
      }
      
      // apply the default view
      // maybe do this in a separate thread
      if (defaultView) {
        OB.Personalization.applyViewDefinition(defaultView.personalizationId, defaultView.viewDefinition, this);
      } else { 
        // only apply the default form/grid if there are no views
        // otherwise you get strange interference
        // check the default form and grid viewstates
        length = this.views.length;
        for (i = 0; i < length; i++) {
          if (personalization.forms && personalization.forms[this.views[i].tabId]) {
            OB.Personalization.personalizeForm(personalization.forms[this.views[i].tabId], this.views[i].viewForm);
          }
          if (this.viewState && this.viewState[this.views[i].tabId]) {
            this.views[i].viewGrid.setViewState(this.viewState[this.views[i].tabId]);
          }
        }
      }
      
      this.getClass().personalization.views.sort(function(v1, v2) {
        var t1 = v1.viewDefinition.name, t2 = v2.viewDefinition.name;
        if (t1 < t2) {
          return -1;
        } else if (t1 === t2) {
          return 0;
        }
        return 1;
      });
    }
  },

  getDefaultGridViewState: function(tabId) {
    var views, length, i, personalization = this.getClass().personalization,    
      defaultView,
      persDefaultValue = OB.PropertyStore.get('OBUIAPP_DefaultSavedView', this.windowId);
    
    if (personalization && personalization.views) {
      views = personalization.views;
      length = views.length;
      if (persDefaultValue) {
        for (i = 0; i < length; i++) {
          if (persDefaultValue === views[i].personalizationId) {
            defaultView = views[i];
            break;
          }
        }
      }
      if (!defaultView) {
        for (i = 0; i < length; i++) {
          if (views[i].viewDefinition && views[i].viewDefinition.isDefault) {
            defaultView = views[i];
            break;
          }
        }
      }
    }
    
    if (defaultView && defaultView.viewDefinition && 
        defaultView.viewDefinition[tabId]) {
      return defaultView.viewDefinition[tabId].grid;
    } 
        
    if (this.viewState && this.viewState[tabId]) {
      return this.viewState[tabId];
    }
    
    return null;
  },
  
  // Update the personalization record which is stored 
  updateFormPersonalization: function(view, formPersonalization) {
    if (!this.getClass().personalization) {
      this.getClass().personalization = {};
     }
    if (!this.getClass().personalization.forms) {
      this.getClass().personalization.forms = [];
    }
    this.getClass().personalization.forms[view.tabId] = formPersonalization;
   },
   
  getFormPersonalization: function(view, checkSavedView) {
    var formPersonalization, i, persView;
    if (!this.getClass().personalization || !this.getClass().personalization.forms) {
      // no form personalization on form level
      // check window level
      if (checkSavedView && this.getClass().personalization && this.getClass().personalization.views 
          && this.selectedPersonalizationId) {
        for (i = 0; i < this.getClass().personalization.views.length; i++) {
          persView = this.getClass().personalization.views[i];
          if (persView.viewDefinition && 
              persView.viewDefinition[view.tabId] && 
              persView.personalizationId === this.selectedPersonalizationId) {
            return persView.viewDefinition[view.tabId].form;
          }
        }
      }
      // nothing found go away
      return null;
    }
    formPersonalization = this.getClass().personalization.forms;
    return formPersonalization[view.tabId];
  },

  removeAllFormPersonalizations: function() {
    var i, updateButtons = false, length = this.views.length;
    if (!this.getClass().personalization) {
      return;
    }
    updateButtons = this.getClass().personalization.forms;
    if (updateButtons) {
      delete this.getClass().personalization.forms;
      for (i = 0; i < length; i++) {
        this.views[i].toolBar.updateButtonState(false);
      }
    }
  },

  isAutoSaveEnabled: function(){
    return this.getClass().autoSave;
  },

  getDirtyEditForm: function() {
    return this.dirtyEditForm;
  },

  setDirtyEditForm: function (editObject) {
    this.dirtyEditForm = editObject;
    if (!editObject) {
      this.cleanUpAutoSaveProperties();
    }
  },

  autoSave: function() {
    this.doActionAfterAutoSave(null, false);
  },

  doActionAfterAutoSave: function(action, forceDialogOnFailure, ignoreAutoSaveEnabled) {
    var me = this;

    var saveCallback = function(ok){
      if (!ok){
        if (me.getDirtyEditForm()) {
          me.getDirtyEditForm().resetForm();
        }
        if (action) {
          OB.Utilities.callAction(action);
        }
        return;
      }
      
      // if not dirty or we know that the object has errors
      if (!me.getDirtyEditForm() || (me.getDirtyEditForm() && !me.getDirtyEditForm().validateForm())) {
        // clean up before calling the action, as the action
        // can set dirty form again
        me.cleanUpAutoSaveProperties();

        // nothing to do, execute immediately
        OB.Utilities.callAction(action);
        return;
      }

      if (action) {
        me.autoSaveAction = action;
      }

      // saving stuff already, go away
      if (me.isAutoSaving) {
        return;
      }

      if (!me.isAutoSaveEnabled() && !ignoreAutoSaveEnabled) {
        me.autoSaveConfirmAction();
        return;
      }

      me.isAutoSaving = true;
      me.forceDialogOnFailure = forceDialogOnFailure;
      me.getDirtyEditForm().autoSave();
    };

    if (this.getClass().autoSave && this.getClass().showAutoSaveConfirmation) {
      // Auto save confirmation required
      if (!this.getDirtyEditForm()) {
        // No changes in record, clean it up and continue
        this.cleanUpAutoSaveProperties();
        OB.Utilities.callAction(action);
        return;
      }
      isc.ask(OB.I18N.getLabel('OBUIAPP_AutosaveConfirm'), saveCallback);
    } else {
      // Auto save confirmation not required: continue as confirmation was accepted
      saveCallback(true);
    }
  },

  callAutoSaveAction: function() {
    var action = this.autoSaveAction;
    this.cleanUpAutoSaveProperties();
    if (!action) {
      return;
    }
    OB.Utilities.callAction(action);
  },

  cleanUpAutoSaveProperties: function() {
    delete this.dirtyEditForm;
    delete this.isAutoSaving;
    delete this.autoSaveAction;
    delete this.forceDialogOnFailure;
  },

  autoSaveDone: function(view, success) {
    if (!this.isAutoSaving) {
      this.cleanUpAutoSaveProperties();
      return;
    }

    if (success) {
      this.callAutoSaveAction();
    } else if (!view.isVisible() || this.forceDialogOnFailure) {
      isc.warn(OB.I18N.getLabel('OBUIAPP_AutoSaveError', [view.tabTitle]));
    } else if (!this.isAutoSaveEnabled()) {
      this.autoSaveConfirmAction();
    }
    this.cleanUpAutoSaveProperties();
  },
  
  autoSaveConfirmAction: function(){
    var action = this.autoSaveAction, me = this;
    this.autoSaveAction = null;

    
    if (this.isAutoSaveEnabled()) {
      // clean up everything
      me.cleanUpAutoSaveProperties();
    }

    var callback = function(ok){
      delete me.inAutoSaveConfirmation;
      if (ok) {
        if (me.getDirtyEditForm()) {
          me.getDirtyEditForm().resetForm();
        }
        if (action) {
          OB.Utilities.callAction(action);
        }
      } else {
        // and focus to the first error field
        if (!me.getDirtyEditForm()) {
          me.view.setAsActiveView();
        } else {
          me.getDirtyEditForm().setFocusInErrorField(true);
          me.getDirtyEditForm().focus();
        }
      }
    };
    this.inAutoSaveConfirmation = true;
    isc.ask(OB.I18N.getLabel('OBUIAPP_AutoSaveNotPossibleExecuteAction'), callback);
  },
  
  addView: function(view){
    view.standardWindow = this;
    this.views.push(view);
    this.toolBarLayout.addMember(view.toolBar);
    if (this.getClass().readOnlyTabDefinition) {
      view.setReadOnly(this.getClass().readOnlyTabDefinition[view.tabId]);
    }
  },

  // is called from the main app tabset. Redirects to custom viewSelected
  tabSelected: function(tabNum, tabPane, ID, tab) {
    if (this.activeView && this.activeView.setViewFocus) {
      this.activeView.setViewFocus();
    }
  },

  // is called from the main app tabset. Redirects to custom viewDeselected
  tabDeselected: function(tabNum, tabPane, ID, tab, newTab){
    this.wasDeselected = true;
  },

  // ** {{{ selectParentTab }}} **
  //
  // Called from the main app tabset
  // Selects the parent tab of the current selected and active tab (independently of its level)
  selectParentTab: function(mainTabSet) {
    if (!this.activeView.parentView) {
      return false;
    }

    var parentTabSet = this.activeView.parentView.parentTabSet;

    if (!parentTabSet) { // If parentTabSet is null means that we are going to move to the top level
      parentTabSet = mainTabSet;
    }

    var parentTab = parentTabSet.getSelectedTab(),
        parentTabNum = parentTabSet.getTabNumber(parentTab),
        parentTabPane = parentTabSet.getTabPane(parentTab);

    parentTabSet.selectTab(parentTabNum);
    if (parentTabPane.setAsActiveView) {
      parentTabPane.setAsActiveView();
      isc.Timer.setTimeout(function(){ // Inside a timeout like in itemClick case. Also to avoid a strange effect that child tab not deployed properly
        parentTabSet.doHandleClick();
      }, 0);
    } else if (parentTabPane.view.setAsActiveView) {
      parentTabPane.view.setAsActiveView();
      isc.Timer.setTimeout(function(){ // Inside a timeout like in itemClick case. Also to avoid a strange effect that parent tab not deployed properly
        parentTabPane.view.doHandleClick();
      }, 0);
    }
  },

  // ** {{{ selectChildTab }}} **
  //
  // Called from the main app tabset
  // Selects the child tab of the current selected and active tab (independently of its level)
  selectChildTab: function(mainTabSet) {
    var childTabSet = this.activeView.childTabSet;

    if (!childTabSet) {
      return false;
    }

    var childTab = childTabSet.getSelectedTab(),
        childTabNum = childTabSet.getTabNumber(childTab),
        childTabPane = childTabSet.getTabPane(childTab);

    childTabSet.selectTab(childTabNum);
    if (childTabPane.setAsActiveView) {
      childTabPane.setAsActiveView();
      isc.Timer.setTimeout(function(){ // Inside a timeout like in itemClick case. Also to avoid a strange effect that child tab not deployed properly
        childTabSet.doHandleClick();
      }, 0);
    }
  },

  // ** {{{ selectPreviousTab }}} **
  //
  // Called from the main app tabset
  // Selects the previous tab of the current selected and active tab (independently of its level)
  selectPreviousTab: function(mainTabSet) {
    var activeTabSet = this.activeView.parentTabSet;
    if (!activeTabSet) { // If activeTabSet is null means that we are in the top level
      activeTabSet = mainTabSet;
    }
    var activeTab = activeTabSet.getSelectedTab(),
        activeTabNum = activeTabSet.getTabNumber(activeTab),
        activeTabPane = activeTabSet.getTabPane(activeTab);

    if ((activeTabNum-1) < 0) {
      return false;
    }

    activeTabSet.selectTab(activeTabNum-1);

    // after select the new tab, activeTab related variables are updated
    activeTab = activeTabSet.getSelectedTab();
    activeTabNum = activeTabSet.getTabNumber(activeTab);
    activeTabPane = activeTabSet.getTabPane(activeTab);

    // and the new selected view is set as active
    if (activeTabPane.setAsActiveView) {
      activeTabPane.setAsActiveView();
    }
  },

  // ** {{{ selectNextTab }}} **
  //
  // Called from the main app tabset
  // Selects the next tab of the current selected and active tab (independently of its level)
  selectNextTab: function(mainTabSet) {
    var activeTabSet = this.activeView.parentTabSet;
    if (!activeTabSet) { // If activeTabSet is null means that we are in the top level
      activeTabSet = mainTabSet;
    }
    var activeTab = activeTabSet.getSelectedTab(),
        activeTabNum = activeTabSet.getTabNumber(activeTab),
        activeTabPane = activeTabSet.getTabPane(activeTab);

    if ((activeTabNum+1) >= activeTabSet.tabs.getLength()) {
      return false;
    }

    activeTabSet.selectTab(activeTabNum+1);

    // after select the new tab, activeTab related variables are updated
    activeTab = activeTabSet.getSelectedTab();
    activeTabNum = activeTabSet.getTabNumber(activeTab);
    activeTabPane = activeTabSet.getTabPane(activeTab);

    // and the new selected view is set as active
    if (activeTabPane.setAsActiveView) {
      activeTabPane.setAsActiveView();
    }
  },
  
  closeClick: function(tab, tabSet){
    if (!this.activeView.viewForm.hasChanged && this.activeView.viewForm.isNew) {
      this.view.standardWindow.setDirtyEditForm(null);
    }

    var actionObject = {
      target: tabSet,
      method: tabSet.doCloseClick,
      parameters: [tab]
    };
    this.doActionAfterAutoSave(actionObject, false);
  },
  
  setActiveView: function(view){
    if (!this.isDrawn()) {
      return;
    }
    if (this.activeView === view) {
      return;
    }
    
    var currentView = this.activeView;
    // note the new activeView must be set before disabling
    // the other one
    this.activeView = view;
    if (currentView) {
      currentView.setActiveViewProps(false);
    }
    view.setActiveViewProps(true);
  },
  
  setFocusInView: function(view){
    var currentView = view || this.activeView || this.view;
    this.setActiveView(currentView);
  },

  show: function() {
    var ret = this.Super('show', arguments);
    this.setFocusInView();
    return ret;
  },

  draw: function(){
    var standardWindow = this, targetEntity,
        ret = this.Super('draw', arguments), i,
        length = this.views.length;
    if (this.targetTabId) {
      for (i = 0; i < length; i++) {
        if (this.views[i].tabId === this.targetTabId) {
          targetEntity = this.views[i].entity;
          this.views[i].viewGrid.targetRecordId = this.targetRecordId;
          this.views[i].openDirectTabView(true);
          this.views[i].viewGrid.refreshContents();
          this.setFocusInView(this.views[i]);
          break;
        }
      }
    } else if (this.command === isc.OBStandardWindow.COMMAND_NEW) {
      var currentView = this.activeView || this.view;
      currentView.editRecord();
      this.command = null;
    } else {
      this.setFocusInView(this.view);
    }
    
    return ret;
  },
  
  setViewTabId: function(viewTabId){
    this.view.viewTabId = viewTabId;
    this.viewTabId = viewTabId;
  },
  
  doHandleClick: function(){
    // happens when we are getting selected
    // then don't change state
    if (this.wasDeselected) {
      this.wasDeselected = false;
      return;
    }
    this.setActiveView(this.view);
    this.view.doHandleClick();
  },
  
  doHandleDoubleClick: function(){
    // happens when we are getting selected
    // then don't change state
    if (this.wasDeselected) {
      this.wasDeselected = false;
      return;
    }
    this.setActiveView(this.view);
    this.view.doHandleDoubleClick();
  },
  
  // +++++++++++++ Methods for the main tab handling +++++++++++++++++++++
  
  getHelpView: function(){
    // tabTitle is set in the viewManager
    return {
        viewId: 'ClassicOBHelp',
        tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'),
        windowId: this.windowId,
        windowType: 'W',
        windowName: this.tabTitle
    };
  },
  
  getBookMarkParams: function(){
    var result = {};
    result.windowId = this.windowId;
    result.viewId = this.getClassName();
    result.tabTitle = this.tabTitle;
    if (this.targetTabId) {
      result.targetTabId = this.targetTabId;
      result.targetRecordId = this.targetRecordId;
    }
    return result;
  },
  
  isEqualParams: function(params){
    var equalTab = params.windowId && params.windowId === this.windowId;
    return equalTab;
  },
  
  isSameTab: function(viewName, params){
    // always return false to force new tabs
    if (this.multiDocumentEnabled) {
      return false;
    }
    return this.isEqualParams(params) && viewName === this.getClassName();
  },

  setTargetInformation: function(tabId, recordId) {
    this.targetTabId = tabId;
    this.targetRecordId = recordId;
    OB.Layout.HistoryManager.updateHistory();
  },
  
  getView: function(tabId) {
    // find is a SC extension on arrays
    return this.views.find('tabId', tabId);
  },

  storeViewState: function(){
    var result = {}, i, length = this.views.length;
    
    if (!OB.Utilities.checkProfessionalLicense(null, true)) {
      return;
    }
    
    for (i = 0; i < length; i++) {
      if ( this.views[i].viewGrid ) {
        result[this.views[i].tabId] = this.views[i].viewGrid.getViewState();
      }
    }
    this.viewState = result;
    OB.PropertyStore.set('OBUIAPP_GridConfiguration', result, this.windowId);
  }
});
