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

OB.ActionButton = {};
OB.ActionButton.executingProcess = null;

isc.ClassFactory.defineClass('OBToolbarActionButton', isc.OBToolbarTextButton);

isc.OBToolbarActionButton.addProperties({
  visible: false,
  modal: true,
  contextView: null,
  labelValue: {},

  action: function () {
    this.runProcess();
  },

  runProcess: function () {
    var theView = this.view,
        record, rowNum, actionObject;

    if (!theView.isShowingForm && theView.viewGrid.getSelectedRecords().length === 1) {
      // Keep current selection that might be lost in autosave
      record = theView.viewGrid.getSelectedRecord();
      rowNum = theView.viewGrid.getRecordIndex(record);
    }

    actionObject = {
      target: this,
      method: this.doAction,
      parameters: [rowNum]
    };

    if (this.autosave) {
      theView.standardWindow.doActionAfterAutoSave(actionObject);
    } else {
      OB.Utilities.callAction(actionObject);
    }
  },

  doAction: function (rowNum) {
    var theView = this.contextView,
        me = this,
        standardWindow = this.view.standardWindow,
        autosaveButton = this.autosave,
        param, allProperties, sessionProperties, callbackFunction, popupParams;

    if (rowNum && !theView.viewGrid.getSelectedRecord()) {
      // Current selection was lost, restore it
      theView.viewGrid.selectRecord(rowNum);
    }

    allProperties = theView.getContextInfo(false, true, false, true);
    sessionProperties = theView.getContextInfo(true, true, false, true);


    OB.ActionButton.executingProcess = this;

    for (param in allProperties) {
      // TODO: these transformations shoulnd't be needed here as soon as getContextInfo returns 
      // the transformed values.
      if (allProperties.hasOwnProperty(param) && typeof allProperties[param] === 'boolean') {
        allProperties[param] = allProperties[param] ? 'Y' : 'N';
      }
    }

    allProperties.inpProcessId = this.processId;

    // obuiapp_process definition
    if (this.newDefinition) {
      callbackFunction = function () {
        standardWindow.openProcess({
          processId: me.processId,
          windowId: me.windowId,
          windowTitle: me.windowTitle,
          actionHandler: me.command,
          buttons: me.labelValue
        });
      };

      theView.setContextInfo(sessionProperties, callbackFunction, true);
      return;
    }

    // ad_process definition handling
    if (this.modal) {
      allProperties.Command = this.command;
      callbackFunction = function () {
        var popup = OB.Layout.ClassicOBCompatibility.Popup.open('process', 900, 600, OB.Utilities.applicationUrl(me.obManualURL), '', null, false, false, true, allProperties);
        if (autosaveButton) {
          // Back to header if autosave button
          popup.activeViewWhenClosed = theView;
        }
      };
    } else {
      popupParams = {
        viewId: 'OBPopupClassicWindow',
        obManualURL: this.obManualURL,
        processId: this.id,
        id: this.id,
        popup: true,
        command: this.command,
        tabTitle: this.title,
        postParams: allProperties,
        height: 600,
        width: 900
      };
      callbackFunction = function () {
        OB.Layout.ViewManager.openView('OBPopupClassicWindow', popupParams);
      };
    }

    //Force setting context info, it needs to be forced in case the current record has just been saved.
    theView.setContextInfo(sessionProperties, callbackFunction, true);
  },
  
  closeProcessPopup: function(newWindow) {
    //Keep current view for the callback function. Refresh and look for tab message.
    var contextView = OB.ActionButton.executingProcess.contextView,
        currentView = this.view,
        afterRefresh = function (doRefresh) {
          var undef,
              refresh = (doRefresh === undef || doRefresh);

          // Refresh context view
          contextView.getTabMessage();
          currentView.toolBar.refreshCustomButtons();

          if (contextView !== currentView && currentView.state === isc.OBStandardView.STATE_TOP_MAX) {
            // Executing an action defined in parent tab, current tab is maximized,
            // let's set half for each in order to see the message
            contextView.setHalfSplit();
          }

          // Refresh in order to show possible new records
          if (refresh) {
            currentView.refresh(null, false, true);
          }
        };

    if (this.autosave) {
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
    } else {
      // If the button is not autosave, do not refresh but get message.
      afterRefresh(false);
    }

    OB.ActionButton.executingProcess = null;

    if (newWindow) {
      if (OB.Application.contextUrl && newWindow.indexOf(OB.Application.contextUrl) !== -1) {
        newWindow = newWindow.substr(newWindow.indexOf(OB.Application.contextUrl) + OB.Application.contextUrl.length-1);
      }

      if (!newWindow.startsWith('/')){
        newWindow = '/'+newWindow;
      }

      if (newWindow.startsWith(contextView.mapping250)) {
        // Refreshing current tab, do not open it again.
        return;
      }
      var windowParams = {
          viewId : this.title,
          tabTitle: this.title,
          obManualURL : newWindow  
        };
      OB.Layout.ViewManager.openView('OBClassicWindow', windowParams);
    }
  },
  
  updateState: function(record, hide, context, keepNonAutosave) {
    var currentValues = record || this.contextView.getCurrentValues() || {},
        // do not hide non autosave buttons when hidding the rest if keepNonAutosave === true
        hideButton = hide && (!keepNonAutosave || this.autosave);

    if (hideButton || !record) {
      this.hide();
      return;
    }
    
    context = context || this.contextView.getContextInfo(false, true, true); 
    
    
    OB.Utilities.fixNull250(currentValues);
    
    this.visible = !this.displayIf || (context && this.displayIf(this.contextView.viewForm, record, context));
    
    // Even visible is correctly set, it is necessary to execute show() or hide()
    if (this.visible){
      this.show();
    } else {
      this.hide();
    }
    
    var readonly = this.readOnlyIf && context && this.readOnlyIf(this.contextView.viewForm, record, context);
    if (readonly) {
      this.disable();
    } else {
      this.enable();
    }
    
    var buttonValue = record[this.property];
    if (buttonValue === '--') {
      buttonValue = 'CL';
    }
    
    var label = this.labelValue[buttonValue];
    if (!label){
      if (this.realTitle) {
        label = this.realTitle;
      } else {
        label = this.title;
      }
    }
    this.realTitle = label;
    this.setTitle(label);
  }
  
});
