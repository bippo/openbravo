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
// = ViewManager =
//
// The ViewManager manages the views displayed in the tabs of the main layout.
// It is called to restore a previous state which is maintained by the History 
// manager. View types which are not yet defined on the client are loaded
// from the server. 
//
(function (OB, isc) {

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }

  // cache object references locally
  var L = OB.Layout,
      ISC = isc,
      vmgr; // Local reference to ViewManager instance

  function ViewManager() {
    // keep the last 5 opened views
    this.recentManager.recentNum = 5;
  }

  ViewManager.prototype = {

    // if true then certain functions are disabled
    inStateHandling: false,

    recentManager: new OB.RecentUtilitiesClass(),

    // ** {{ ViewManager.views }} **
    // Collection of opened views
    views: {
      cache: [],

      getViewTabID: function (vName, params) {
        var len = this.cache.length,
            i, item;
        for (i = len; i > 0; i--) {
          item = this.cache[i - 1];
          if (item.instance.isSameTab && item.instance.isSameTab(vName, params)) {
            return item.viewTabId;
          }
        }
        return null;
      },

      getTabNumberFromViewParam: function (param, value) {
        var numberOfTabs = OB.MainView.TabSet.tabs.length,
            viewParam = '',
            result = null,
            i;
        for (i = 0; i < numberOfTabs; i++) {
          viewParam = OB.MainView.TabSet.getTabPane(i)[param];
          if (viewParam === value) {
            result = i;
          }
        }
        return result;
      },

      push: function (instanceDetails) {
        this.cache.push(instanceDetails);
      },

      removeTab: function (viewTabId) {
        var len = this.cache.length,
            i, item, removed;
        for (i = len; i > 0; i--) {
          item = this.cache[i - 1];
          if (item.viewTabId === viewTabId) {
            removed = this.cache.splice(i - 1, 1);
            return;
          }
        }
      }
    },

    findLoadingTab: function (params) {
      var i, length;
      if (!params.loadingTabId) {
        return null;
      }
      length = OB.MainView.TabSet.tabs.length;
      for (i = 0; i < length; i++) {
        var pane = OB.MainView.TabSet.tabs[i].pane;
        if (pane.viewTabId && pane.isLoadingTab && pane.viewTabId === params.loadingTabId) {
          return OB.MainView.TabSet.tabs[i];
        }
      }
      return null;
    },

    createLoadingTab: function (viewId, params, viewTabId) {
      // open a loading tab
      params = params || {};
      var layout = OB.Utilities.createLoadingLayout();
      // is used to prevent history updating
      layout.isLoadingTab = true;
      viewTabId = viewTabId || params.currentViewTabId || '_' + new Date().getTime();
      params.loadingTabId = viewTabId;
      this.createTab(viewId, viewTabId, layout, params);
      return params;
    },

    fetchView: function (viewId, callback, clientContext, params, useLoadingTab) {
      var rpcMgr = ISC.RPCManager,
          reqObj, request;

      if (useLoadingTab) {
        params = this.createLoadingTab(viewId, params);
      }

      reqObj = {
        params: {
          viewId: viewId
        },
        callback: callback,
        clientContext: clientContext,
        evalResult: true,
        httpMethod: 'GET',
        useSimpleHttp: true,
        actionURL: OB.Application.contextUrl + 'org.openbravo.client.kernel/OBUIAPP_MainLayout/View'
      };
      request = rpcMgr.sendRequest(reqObj);
    },

    addRecentDocument: function (params) {
      vmgr.recentManager.addRecent('OBUIAPP_RecentDocumentsList', isc.addProperties({
        icon: OB.Styles.OBApplicationMenu.Icons.window
      }, params));
    },

    createTab: function (viewName, viewTabId, viewInstance, params) {
      var tabTitle;

      if (params.i18nTabTitle) {
        // note call to I18N is done below after the tab
        // has been created
        tabTitle = '';
      } else {
        tabTitle = params.tabTitle || viewInstance.tabTitle || params.tabId || viewName;
      }

      var tabDef = {
        ID: viewTabId,
        title: tabTitle,
        canClose: true,
        viewName: viewName,
        params: params,
        pane: viewInstance
      };

      // let the params override tab properties like canClose
      tabDef = isc.addProperties(tabDef, params);

      // let the viewinstance decide if it can be closed
      // see https://issues.openbravo.com/view.php?id=15953
      if (viewInstance.notClosable) {
        tabDef.canClose = false;
      }

      // Adding to the MainView tabSet
      OB.MainView.TabSet.addTab(tabDef);

      if (params.i18nTabTitle) {
        tabTitle = '';
        // note the callback calls the tabSet
        // with the tabid to set the label
        OB.I18N.getLabel(params.i18nTabTitle, null, {
          setTitle: function (label) {
            OB.MainView.TabSet.setTabTitle(viewTabId, label);
          }
        }, 'setTitle');
      }

      // tell the viewinstance what tab it is on
      // note do not use tabId on the viewInstance
      // as tabId is used by the classic ob window
      // local variable is: viewTabId
      if (viewInstance.setViewTabId) {
        viewInstance.setViewTabId(viewTabId);
      } else {
        viewInstance.viewTabId = viewTabId;
      }

      // Adding a reference to opened views collection
      vmgr.views.push({
        viewName: viewName,
        params: params,
        instance: viewInstance,
        viewTabId: viewTabId
      });

      // the select tab event will update the history
      OB.MainView.TabSet.selectTab(viewTabId);
    },

    // ** {{{ ViewManager.openView(viewName, params) }}} **
    //
    // Shows a new tab in the {{{ Main Layout }}}
    //
    // Parameters:
    // * {{{viewName}}} is a String of the view implementation, e.g. {{{
    // OBClassicWindow }}}
    // * {{{params}}} is an Object with the parameters passed to the
    // implementation to initialize an instance, e.g. {{{ {tabId: '100'} }}}
    // * {{{state}}} is an Object which can contain more complex state
    // information
    // to initialize an instance.
    //
    openView: function (viewName, params, state) {
      var recentObjProperties;
      params = params || {};

      // only add closable views to the recent items, this prevents the workspace
      // view from being displayed, explicitly doing !== false to catch 
      // views which don't have this set at all
      // don't store OBPopupClassicWindow in the viewmanager 
      // don't store direct links to a target tab, this should be set in a different
      // property
      if (!params.targetTabId && params.canClose !== false && !vmgr.inStateHandling && params.viewId !== 'OBPopupClassicWindow') {
        if (params.i18nTabTitle && !params.tabTitle) {
          params.tabTitle = OB.I18N.getLabel(params.i18nTabTitle);
        }
        if (!params.viewId) {
          params.viewId = viewName;
        }
        //If recents receives null in params the tab is not added to the recent list
        recentObjProperties = null;
        if (params.addToRecents === undefined || params.addToRecents === null || params.addToRecents === true) {
          // add and set a default icon
          recentObjProperties = isc.addProperties({
            icon: OB.Styles.OBApplicationMenu.Icons.window
          }, params);
        }
        vmgr.recentManager.addRecent('OBUIAPP_RecentViewList', recentObjProperties);
      }

      //
      // Returns the function implementation of a View
      //

      function getView(viewName, params, state) {

        if (!viewName) {
          throw {
            name: 'ParameterRequired',
            message: 'View implementation name is required'
          };
        }

        //
        // Shows a view in a tab in the {{{ TabSet }}} or external
        //

        function showTab(viewName, params, state) {

          // will as a default display a loading tab when loading the 
          // view from the server or creating a new instance
          // different cases:
          // 1) view is not open and class not loaded (open view and show loading bar)
          // 2) view is not open but class was loaded (open view and show loading bar)
          // 3) view is open and class is loaded (show loading bar in open view)          
          var viewTabId, tabTitle, loadingTab = vmgr.findLoadingTab(params),
              loadingPane, currentPane, tabSet = OB.MainView.TabSet;

          params = params || {};

          if (loadingTab) {
            viewTabId = loadingTab.pane.viewTabId;
          } else if (!params.popup && viewName !== 'OBPopupClassicWindow' && !params.showsItself) {
            viewTabId = vmgr.views.getViewTabID(viewName, params);
            if (viewTabId) {
              // tab exists, replace its contents
              loadingPane = OB.Utilities.createLoadingLayout();

              // make sure it gets found in the next round
              params.loadingTabId = viewTabId;
              loadingPane.viewTabId = viewTabId;

              // is used to prevent history updating
              loadingPane.isLoadingTab = true;

              tabSet.updateTab(viewTabId, loadingPane);

              // and show it
              tabSet.selectTab(viewTabId);
            } else {
              // create a completely new tab
              // first create a loading tab and then call again
              // in another thread
              params = vmgr.createLoadingTab(viewName, params, viewTabId);
            }

            // make a clone to prevent params being updated in multiple threads
            // happens for the myob tab in special cases
            // https://issues.openbravo.com/view.php?id=18548
            params = isc.shallowClone(params);

            // use a canvas to make use of the fireOnPause possibilities
            // but don't forget to destroy it afterwards...
            var cnv = isc.Canvas.create({
              openView: function () {
                vmgr.openView(viewName, params);
                // delete so that at the next opening a new loading layout
                // is created
                delete params.loadingTabId;
                this.destroy();
              }
            });
            cnv.fireOnPause('openView', cnv.openView, null, cnv);
            return;
          }

          // always create a new instance anyway as parameters
          // may have changed
          var viewInstance = ISC[viewName].create(params);

          if (state && viewInstance.setViewState) {
            viewInstance.setViewState(state);
          }

          // is not shown in a tab, let it show itself in a different way
          // but first get rid of the loading tab
          if (viewInstance && viewInstance.show && viewInstance.showsItself) {
            if (loadingTab) {
              delete params.loadingTabId;
              tabSet.removeTab(loadingTab.ID);
            }
            viewInstance.show();
            return;
          }

          // eventhough there is already an open tab
          // still refresh it
          if (viewTabId !== null) {

            // refresh the view
            tabSet.updateTab(viewTabId, viewInstance);

            // and show it
            // only select a non myob tab
            if (viewInstance.getClassName() !== 'OBMyOpenbravoImplementation') {
              tabSet.selectTab(viewTabId);
            }

            // tell the viewinstance what tab it is on
            // note do not use tabId on the viewInstance
            // as tabId is used by the classic ob window
            // local variable is: viewTabId (with uppercase ID)
            // function call and other variable uses camelcase Id
            if (viewInstance.setViewTabId) {
              viewInstance.setViewTabId(viewTabId);
            } else {
              viewInstance.viewTabId = viewTabId;
            }

            // update the cache
            vmgr.views.removeTab(viewTabId, false);
            vmgr.views.push({
              viewName: viewName,
              params: params,
              instance: viewInstance,
              viewTabId: viewTabId
            });
            // note after this the viewTabId is not anymore viewInstance.ID +
            // '_tab'
            // but this is not a problem, it should be unique that's the most
            // important part
            // the select tab event will update the history
            if (tabSet.getSelectedTab() && tabSet.getSelectedTab().pane.viewTabId === viewTabId) {
              OB.Layout.HistoryManager.updateHistory();
            } else if (viewInstance.getClassName() !== 'OBMyOpenbravoImplementation') {
              // only select a non myob tab
              tabSet.selectTab(viewTabId);
            }

            return;
          }

          // Creating an instance of the view implementation
          viewTabId = viewInstance.ID + '_tab';

          if (viewInstance) {
            vmgr.createTab(viewName, viewTabId, viewInstance, params);
          }
        }

        //
        // Function used by the {{ ISC.RPCManager }} after receiving the view
        // implementation from the back-end
        //          

        function fetchViewCallback(response, data, request) {
          // if the window is in development it's name is always unique
          // and has changed
          if (vmgr.loadedWindowClassName) {
            viewName = vmgr.loadedWindowClassName;
          }
          if (!ISC[viewName]) {
            throw {
              name: 'ReferenceError',
              message: 'The view ' + viewName + ' not defined'
            };
          }
          showTab(viewName, params);
        }

        if (isc[viewName]) {
          showTab(viewName, params);
        } else {
          vmgr.fetchView(viewName, fetchViewCallback, null, params, true);
        }
      }
      getView(viewName, params, state);
    },

    // ** {{{ ViewManager.restoreState(state, data) }}} **
    //
    // Restores the state of the main layout using the passed in state object.
    // This state object contains view id's and book marked parameters.
    // The data object contains extra (more complex) state information which 
    // can not be bookmarked.
    //
    restoreState: function (newState, data) {

      var viewId, tabSet = OB.MainView.TabSet,
          tabsLength, i, tabObject, hasChanged = false,
          stateData, requestViewsRestoreState;

      if (vmgr.inStateHandling) {
        return;
      }

      vmgr.inStateHandling = true;

      // create an empty layout
      if (!newState) {
        OB.MainView.TabSet.removeTabs(OB.MainView.TabSet.tabs);
        vmgr.views.cache = [];
        vmgr.inStateHandling = false;
        return;
      }

      // do some comparison
      tabsLength = newState.bm.length;
      hasChanged = OB.MainView.TabSet.tabs.length !== tabsLength;
      // same length, compare individual tabs
      if (!hasChanged) {
        for (i = 0; i < tabsLength; i++) {
          tabObject = OB.MainView.TabSet.getTabObject(i);

          // changed if the view id is different
          if (newState.bm[i].viewId !== tabObject.viewName) {
            hasChanged = true;
          } else if (tabObject.pane.isEqualParams) {
            // or if the bookmark params are not the same
            hasChanged = hasChanged || !tabObject.pane.isEqualParams(newState.bm[i].params);
          }
        }
      }

      isc.Log.logDebug('Changed ' + hasChanged, 'OB');

      // changes occured, start from scratch again, recreating each view
      if (hasChanged) {
        // stop if tabSet removed failed because a tab has incorrect data
        if (!OB.MainView.TabSet.removeTabs(OB.MainView.TabSet.tabs)) {
          vmgr.inStateHandling = false;
          return;
        }

        vmgr.views.cache = [];

        // handles the case that not all views are there
        // view implementations are requested async resulting
        // in a wrong tab order, therefore only get the views
        // in the correct order, continuing when a new view 
        // arrives
        // see here:
        // https://issues.openbravo.com/view.php?id=15146
        requestViewsRestoreState = function (rpcResponse) {
          var clientContext = rpcResponse.clientContext;
          var currentIndex = clientContext.currentIndex;
          var data = clientContext.data;
          var newState = clientContext.newState;
          var tabsLength = clientContext.tabsLength;
          var i, viewId;

          if (currentIndex < tabsLength) {
            for (i = currentIndex; i < tabsLength; i++) {

              // ignore the first tab, or the tabs opened without view id
              if (!newState.bm[i].viewId) {
                continue;
              }
              // not defined get the view!
              if (!isc[newState.bm[i].viewId]) {
                viewId = newState.bm[i].viewId;
                clientContext.currentIndex = i + 1;
                vmgr.fetchView(viewId, requestViewsRestoreState, clientContext, newState.bm[i].params);
                return;
              }
            }
          }
          // everything is here, open the views
          for (i = 0; i < tabsLength; i++) {

            if (data && data[i]) {
              stateData = data[i];
            }

            // ignore the first tab, or the tabs opened without view id
            if (!newState.bm[i].viewId) {
              continue;
            }

            vmgr.openView(newState.bm[i].viewId, newState.bm[i].params, stateData);
          }
          OB.MainView.TabSet.selectTab(newState.st);
          vmgr.inStateHandling = false;
        };

        for (i = 0; i < tabsLength; i++) {

          if (data && data[i]) {
            stateData = data[i];
          }

          // ignore the first tab, or the tabs opened without view id
          if (!newState.bm[i].viewId) {
            continue;
          }

          if (!isc[newState.bm[i].viewId]) {
            var clientContext = {};

            viewId = newState.bm[i].viewId;
            clientContext.currentIndex = i + 1;
            clientContext.data = data;
            clientContext.newState = newState;
            clientContext.tabsLength = tabsLength;
            vmgr.fetchView(viewId, requestViewsRestoreState, clientContext, newState.bm[i].params);
            return;
          }

          vmgr.openView(newState.bm[i].viewId, newState.bm[i].params, stateData);
        }
      }

      OB.MainView.TabSet.selectTab(newState.st);

      vmgr.inStateHandling = false;
    },

    createAddStartTab: function () {
      var error, historyId = isc.History.getCurrentHistoryId();
      if (historyId) {
        try {
          OB.Layout.HistoryManager.restoreHistory(historyId, isc.History.getHistoryData(historyId));
          return;
        } catch (exception) {
          // ignore all errors
        }
      }

      // todo: this call in a way assumes that there is a myob module installed
      // it is nicer to somehow set the page to load in a different way
      // this can be done if an smartclient problem has been solved
      // see this forum post:
      // http://forums.smartclient.com/showthread.php?p=53077
      var viewId = 'OBMyOpenbravoImplementation';
      var viewParams = {
        tabTitle: OB.I18N.getLabel('OBUIAPP_MyOpenbravo'),
        myOB: true,
        canClose: false
      };
      // check if there is already a start page, only open it if not
      var viewTabId = this.views.getViewTabID(viewId, viewParams);
      if (!viewTabId) {
        this.openView(viewId, viewParams, null);
      }

      // check if a tabId was passed as a url param
      // only do this if there is no other history
      if (!historyId) {
        var urlParams = OB.Utilities.getUrlParameters();
        if (urlParams.tabId) {
          OB.Utilities.openDirectTab(urlParams.tabId, urlParams.recordId, urlParams.command);
        }
      }
    }
  };

  // Initialize ViewManager object
  vmgr = L.ViewManager = OB.ViewManager = new ViewManager();
}(OB, isc));