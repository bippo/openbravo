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


isc.ClassFactory.defineClass('OBTabBarButton', isc.StretchImgButton);

isc.ClassFactory.defineClass('OBTabSet', isc.TabSet);

isc.ClassFactory.defineClass('OBTabBar', isc.TabBar);


isc.ClassFactory.defineClass('OBTabBarButtonMain', isc.OBTabBarButton);

isc.ClassFactory.defineClass('OBTabSetMain', isc.OBTabSet);

isc.OBTabSetMain.addProperties({
  destroyPanes: true,

  stateAsString: null,

  // note see the smartclient autochild concept for why tabBarProperties is valid
  tabBarProperties: isc.addProperties({
    buttonConstructor: isc.OBTabBarButtonMain,

    dblClickWaiting: false,

    itemClick: function (item, itemNum) {
      var me = this;
      me.dblClickWaiting = true;
      isc.Timer.setTimeout(function () {
        // if no double click happened then do the single click
        if (me.dblClickWaiting) {
          me.dblClickWaiting = false;
          if (me.tabSet.selectedTab === itemNum && item.pane.doHandleClick) {
            item.pane.doHandleClick();
          }
        }
      }, OB.Constants.DBL_CLICK_DELAY);

    },
    itemDoubleClick: function (item, itemNum) {
      this.dblClickWaiting = false;
      if (this.tabSet.selectedTab === itemNum && item.pane.doHandleDoubleClick) {
        item.pane.doHandleDoubleClick();
      }
    }
  }),

  tabSelected: function (tabNum, tabPane, ID, tab) {
    if (!tabPane.isLoadingTab) {
      OB.Layout.HistoryManager.updateHistory();
    }
    if (tabPane.tabSelected) { //Redirect if tabPane has its own tabSelected handler
      tabPane.tabSelected(tabNum, tabPane, ID, tab);
    }

    // update the document title
    document.title = 'Openbravo - ' + tab.title;
  },

  tabDeselected: function (tabNum, tabPane, ID, tab, newTab) {
    if (tabPane.tabDeselected) { //Redirect if tabPane has its own tabDeselected handler
      tabPane.tabDeselected(tabNum, tabPane, ID, tab, newTab);
    }
  },

  closeClick: function (tab) {
    if (tab.pane && tab.pane.closeClick) {
      tab.pane.closeClick(tab, this);
    } else {
      this.doCloseClick(tab);
    }
  },

  doCloseClick: function (tab) {
    if (tab && tab.pane) {
      tab.pane.closing = true;
    }
    return this.Super('closeClick', arguments);
  },

  initWidget: function () {
    this.tabBarProperties.tabSet = this;
    this.Super('initWidget', arguments);
  },

  draw: function () {
    var me = this,
        ksAction_CloseSelectedTab, ksAction_SelectParentTab, ksAction_SelectChildTab, ksAction_SelectPreviousTab, ksAction_SelectNextTab, ksAction_SelectWorkspaceTab;
    ksAction_CloseSelectedTab = function () {
      me.closeSelectedTab();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TabSet_CloseSelectedTab', 'Canvas', ksAction_CloseSelectedTab);
    ksAction_SelectParentTab = function () {
      me.selectParentTab();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TabSet_SelectParentTab', 'Canvas', ksAction_SelectParentTab);
    ksAction_SelectChildTab = function () {
      me.selectChildTab();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TabSet_SelectChildTab', 'Canvas', ksAction_SelectChildTab);
    ksAction_SelectPreviousTab = function () {
      if (!isc.Page.isRTL()) { // LTR mode
        me.selectPreviousTab();
      } else { // RTL mode
        me.selectNextTab();
      }
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TabSet_SelectPreviousTab', 'Canvas', ksAction_SelectPreviousTab);
    ksAction_SelectNextTab = function () {
      if (!isc.Page.isRTL()) { // LTR mode
        me.selectNextTab();
      } else { // RTL mode
        me.selectPreviousTab();
      }
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TabSet_SelectNextTab', 'Canvas', ksAction_SelectNextTab);
    ksAction_SelectWorkspaceTab = function () {
      me.selectTab(0);
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('TabSet_SelectWorkspaceTab', 'Canvas', ksAction_SelectWorkspaceTab);
    this.Super('draw', arguments);
  },

  closeAllTabs: function () { // Except "Workspace" tab
    var tabCount, tabArray = [],
        i;
    for (i = 1; i > 0; i++) {
      if (typeof this.getTab(i) === 'undefined') {
        break;
      }
    }
    tabCount = i - 1;
    this.selectTab(0);
    for (i = 1; i <= tabCount; i++) {
      tabArray.push(i);
    }
    this.removeTabs(tabArray);
  },

  closeSelectedTab: function () { // Only if selected tab is closable
    var selectedTab = this.getSelectedTab();
    if (selectedTab.canClose) {
      this.removeTabs(selectedTab);
    }
  },

  selectParentTab: function () {
    var tabSet = this,
        tab = tabSet.getSelectedTab(),
        ID = tab.ID,
        tabNum = tabSet.getTabNumber(tab),
        tabPane = tabSet.getTabPane(tab);

    if (tabPane.selectParentTab) { //Redirect if tabPane has its own selectPreviousTab handler
      tabPane.selectParentTab(tabSet);
    }

    return true;
  },

  selectChildTab: function () {
    var tabSet = this,
        tab = tabSet.getSelectedTab(),
        ID = tab.ID,
        tabNum = tabSet.getTabNumber(tab),
        tabPane = tabSet.getTabPane(tab);

    if (tabPane.selectChildTab) { //Redirect if tabPane has its own selectPreviousTab handler
      tabPane.selectChildTab(tabSet);
    }

    return true;
  },

  selectPreviousTab: function (doDefaultAction) {
    var tabSet = this,
        tab = tabSet.getSelectedTab(),
        ID = tab.ID,
        tabNum = tabSet.getTabNumber(tab),
        tabPane = tabSet.getTabPane(tab);

    if (!doDefaultAction) {
      doDefaultAction = false;
    }

    if (!doDefaultAction && tabPane.selectPreviousTab) { //Redirect if tabPane has its own selectPreviousTab handler
      tabPane.selectPreviousTab(tabSet);
    } else {
      tabSet.selectTab(tabNum - 1);
    }

    return true;
  },

  selectNextTab: function (doDefaultAction) {
    var tabSet = this,
        tab = tabSet.getSelectedTab(),
        ID = tab.ID,
        tabNum = tabSet.getTabNumber(tab),
        tabPane = tabSet.getTabPane(tab);

    if (!doDefaultAction) {
      doDefaultAction = false;
    }

    if (!doDefaultAction && tabPane.selectNextTab) { //Redirect if tabPane has its own selectNextTab handler
      tabPane.selectNextTab(tabSet);
    } else {
      tabSet.selectTab(tabNum + 1);
    }

    return true;
  },

  // is used by selenium
  getTabFromTitle: function (title) {
    var index = 0,
        tab = null;
    for (; index < OB.MainView.TabSet.tabs.getLength(); index++) {
      tab = OB.MainView.TabSet.getTabObject(index);
      if (tab.title === title) {
        return tab;
      }
    }
    return null;
  },

  removeTabs: function (tabs, destroyPanes) {
    var i, tab, appFrame, tabsLength, toRemove = [],
        tabSet = OB.MainView.TabSet;

    if (!tabs) {
      return;
    }

    if (!isc.isAn.Array(tabs)) {
      tabs = [tabs];
    }

    // get the actual tab button object from whatever was passed in.
    // We can pass this to tabBar.removeTabs()
    tabs = this.map('getTab', tabs);

    tabsLength = tabs.length;

    for (i = 0; i < tabsLength; i++) {
      tab = tabSet.getTab(tabs[i].ID);
      if (tab.pane.Class === 'OBClassicWindow') {

        appFrame = tab.pane.appFrameWindow || tab.pane.getAppFrameWindow();

        if (appFrame && appFrame.isUserChanges) {
          if (appFrame.validate && !appFrame.validate()) {
            return false;
          }
          tab.pane.saveRecord(tabs[i].ID);
        } else {
          OB.Layout.ViewManager.views.removeTab(tabs[i].ID);
          toRemove.push(tabs[i].ID);
        }
      } else {
        OB.Layout.ViewManager.views.removeTab(tabs[i].ID);
        toRemove.push(tabs[i].ID);
      }
    }
    this.Super('removeTabs', [toRemove]);
    OB.Layout.HistoryManager.updateHistory();
    return true;
  },

  updateTab: function (tab, pane, refresh) {
    var previousPane = tab && this.getTabObject(tab).pane;

    this.Super('updateTab', arguments);

    // Note: updateTab doesn't remove the previous loading tab
    // http://www.smartclient.com/docs/8.1/a/b/c/go.html#method..TabSet.updateTab
    if (previousPane && previousPane.isLoadingTab) {
      previousPane.destroy();
    }

    if (refresh && pane.refresh) {
      this.fireOnPause('refreshRecordInView', {
        target: pane,
        methodName: 'refresh'
      }, 120);
    }
  }
});

isc.ClassFactory.defineClass('OBTabBarMain', isc.OBTabBar);

isc.OBTabBarMain.addProperties({
  initWidget: function () {
    this.Super('initWidget', arguments);
  },

  keyPress: function () {
    var ret;
    this.tabWithinToolbar = true;
    ret = this.Super('keyPress', arguments);
    this.tabWithinToolbar = false;
    return ret;
  }
});


isc.ClassFactory.defineClass('OBTabBarButtonChild', isc.OBTabBarButton);

isc.OBTabBarButtonChild.addProperties({
  // when a tab is drawn the first time it steals the focus 
  // from the active view, prevent this
  focus: function () {
    if (this.parentElement.tabSet.tabPicker) {
      this.pane.setAsActiveView();
    }
    if (this.pane.isActiveView && this.pane.isActiveView()) {
      this.Super('focus', arguments);
    }
  }
});

isc.ClassFactory.defineClass('OBTabSetChild', isc.OBTabSet);

isc.OBTabSetChild.addProperties({
  destroyPanes: true,

  stateAsString: null,

  tabBarProperties: isc.addProperties({
    buttonConstructor: isc.OBTabBarButtonChild,

    dblClickWaiting: false,

    click: function () {
      if (this.itemClicked) {
        delete this.itemClicked;
        return false;
      }
      this.tabSet.doHandleClick();
    },

    doubleClick: function () {
      if (this.itemClicked || this.itemDoubleClicked) {
        delete this.itemClicked;
        delete this.itemDoubleClicked;
        return false;
      }
      this.tabSet.doHandleDoubleClick();
    },

    canDrag: false,
    dragAppearance: 'none',
    dragStartDistance: 1,
    overflow: 'hidden',

    itemClick: function (item, itemNum) {
      var me = this,
          tab = item;
      this.itemClicked = true;
      if (this.tabSet.ignoreItemClick) {
        delete this.tabSet.ignoreItemClick;
        return false;
      }
      me.dblClickWaiting = true;
      isc.Timer.setTimeout(function () {
        // if no double click happened then do the single click
        if (me.dblClickWaiting) {
          me.dblClickWaiting = false;
          me.tabSet.doHandleClick();
        }
      }, OB.Constants.DBL_CLICK_DELAY);
      return false;
    },

    itemDoubleClick: function (item, itemNum) {
      this.dblClickWaiting = false;
      this.itemDoubleClicked = true;
      this.tabSet.doHandleDoubleClick();
    },

    dragStop: function () {
      // change the height to percentage based to handle resizing of browser:
      this.tabSet.parentContainer.convertToPercentageHeights();
      this.setCursor(isc.Canvas.ROW_RESIZE);
      return true;
    },

    mouseDown: function () {
      if (this.tabSet.state === isc.OBStandardView.STATE_IN_MID) {
        this.setCursor(isc.Canvas.MOVE);
      }
    },

    mouseUp: function () {
      if (this.tabSet.state === isc.OBStandardView.STATE_IN_MID) {
        this.setCursor(isc.Canvas.ROW_RESIZE);
      }
    },

    mouseOut: function () {
      if (this.tabSet.state === isc.OBStandardView.STATE_IN_MID) {
        this.setCursor(isc.Canvas.ROW_RESIZE);
      }
    },

    mouseOver: function () {
      if (this.tabSet.state === isc.OBStandardView.STATE_IN_MID) {
        this.setCursor(isc.Canvas.ROW_RESIZE);
      } else {
        this.setCursor(isc.Canvas.HAND);
      }
    },

    getCurrentCursor: function () {
      if (this.tabSet.state === isc.OBStandardView.STATE_IN_MID) {
        if (isc.EventHandler.leftButtonDown()) {
          return isc.Canvas.MOVE;
        }
        return isc.Canvas.ROW_RESIZE;
      }
      return this.Super('getCurrentCursor', arguments);
    },

    dragStart: function () {
      // -2 to prevent scrollbar
      this.tabSet.maxHeight = this.tabSet.parentContainer.getHeight() - 2;
      this.tabSet.minHeight = (this.getHeight() * 2) + 15;
      return true;
    },

    dragMove: function () {
      var offset = -1 * isc.EH.dragOffsetY;
      this.resizeTarget(this.tabSet, true, true, offset, -1 * this.getHeight(), null, true);
      this.tabSet.draggedHeight = this.tabSet.getHeight();
      // if (this.tabSet.getHeight() === this.getHeight()) {
      // // set the parent to top-max
      // this.tabSet.parentTabSet.setState(isc.OBStandardView.STATE_TOP_MAX);
      // this.tabSet.draggedHeight = null;
      // }
      return true;
    }
  }),

  state: null,
  previousState: null,

  // keeps track of the previous dragged height, to restore it
  draggedHeight: null,

  setDraggable: function (draggable) {
    if (draggable) {
      this.tabBar.canDrag = true;
      this.tabBar.cursor = isc.Canvas.ROW_RESIZE;
    } else {
      this.tabBar.canDrag = false;
      this.tabBar.cursor = isc.Canvas.DEFAULT;
    }
  },

  doHandleClick: function () {
    if (this.state === isc.OBStandardView.STATE_MIN) {
      // we are minimized, there must be a parent then
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_MID);
      } else {
        this.parentContainer.setHalfSplit();
      }
    } else if (this.state === isc.OBStandardView.STATE_BOTTOM_MAX) {
      this.setState(isc.OBStandardView.STATE_MID);
    } else if (this.state === isc.OBStandardView.STATE_MID) {
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_MID);
      } else {
        this.parentContainer.setHalfSplit();
      }
    } else if (this.state === isc.OBStandardView.STATE_TOP_MAX) {
      this.doHandleDoubleClick();
    }
  },

  doHandleDoubleClick: function () {
    if (this.state === isc.OBStandardView.STATE_TOP_MAX) {
      // we are maximized go back to the previous state
      if (this.previousState && this.previousState !== this.state) {
        if (this.previousState === isc.OBStandardView.STATE_IN_MID) {
          this.parentContainer.setHalfSplit();
        } else if (this.previousState === isc.OBStandardView.STATE_MIN) {
          if (this.parentTabSet) {
            this.parentTabSet.setState(isc.OBStandardView.STATE_TOP_MAX);
          } else {
            this.parentContainer.setTopMaximum();
          }
        } else {
          this.setState(this.previousState);
        }
      } else {
        this.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      }
    } else {
      // first set to IN_MID, to prevent empty tab displays
      this.setState(isc.OBStandardView.STATE_IN_MID);
      this.setState(isc.OBStandardView.STATE_TOP_MAX);
    }
  },

  getState: function () {
    return this.state;
  },

  setState: function (newState) {
    // disabled this as sometimes states have
    // to be reset to recompute heights changed automatically
    // if (this.state === newState) {
    // return;
    // }
    var tab, i, pane;
    var tmpPreviousState = this.state;
    var length = this.tabs.length;

    // is corrected below for one state
    this.setDraggable(false);

    if (newState === isc.OBStandardView.STATE_TOP_MAX) {
      this.state = newState;

      // minimize the ancestors
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }

      // note this for loop must be done before the parent's are
      // done otherwise the content is not drawn
      // the top member in each tab is maximized
      // the bottom member in each tab is set to the tabbar height
      for (i = 0; i < length; i++) {
        tab = this.tabs[i];
        this.makeTabVisible(tab);
        pane = this.getTabPane(tab);
        pane.setTopMaximum();
      }

    } else if (newState === isc.OBStandardView.STATE_MIN) {
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        this.getTabPane(tab).hide();
      }

      // the height is set to the height of the tabbar
      this.setHeight(this.tabBar.getHeight());

      this.state = newState;
    } else if (newState === isc.OBStandardView.STATE_BOTTOM_MAX) {
      // the top part in each layout is set to 0%, and the bottom to max
      this.state = newState;
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }
      for (i = 0; i < length; i++) {
        tab = this.tabs[i];
        this.makeTabVisible(tab);
        pane = this.getTabPane(tab);
        pane.setBottomMaximum();
      }
    } else if (newState === isc.OBStandardView.STATE_IN_MID) {
      this.state = newState;
      this.setDraggable(true);
      // minimize the third level
      for (i = 0; i < length; i++) {
        tab = this.tabs[i];
        pane = this.getTabPane(tab);
        pane.setHeight('100%');
        this.makeTabVisible(tab);
        if (pane.members[1]) {
          pane.members[1].setState(isc.OBStandardView.STATE_MIN);
        } else {
          pane.members[0].setHeight('100%');
        }
      }
    } else if (newState === isc.OBStandardView.STATE_MID) {
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }
      // the content of the tabs is split in 2
      this.state = newState;
      for (i = 0; i < length; i++) {
        tab = this.tabs[i];
        pane = this.getTabPane(tab);
        this.makeTabVisible(tab);
        pane.setHalfSplit();
      }
    }

    this.previousState = tmpPreviousState;

    for (i = 0; i < length; i++) {
      tab = this.tabs[i];
      tab.pane.setMaximizeRestoreButtonState();
    }
  },

  makeTabVisible: function (tab) {
    var pane;

    if (tab === this.getSelectedTab()) {
      pane = this.getTabPane(tab);
      pane.show();
      if (pane.refreshContents) {
        pane.doRefreshContents(true, true);
      }
      if (pane.members[0]) {
        pane.members[0].show();
      }
      if (pane.members[1]) {
        pane.members[1].show();
      }
      //      this.selectTab(tab);
    }
  },

  tabSelected: function (tabNum, tabPane, ID, tab) {
    var event = isc.EventHandler.getLastEvent();
    if (tabPane.refreshContents) {
      tabPane.doRefreshContents(true, true);
    }
    // if the event is a mouse event then let the item click not do max/min
    // tabselected events are also fired when drawing
    if (this.isDrawn() && event && isc.EventHandler.isMouseEvent(event.eventType) && tabPane.parentView && tabPane.parentView.state !== isc.OBStandardView.STATE_TOP_MAX && tabPane.parentView.state !== isc.OBStandardView.STATE_MID) {
      this.ignoreItemClick = true;
    }
  },

  initWidget: function () {
    this.tabBarProperties.tabSet = this;
    this.Super('initWidget', arguments);
  }
});

isc.ClassFactory.defineClass('OBTabBarChild', isc.OBTabBar);

isc.OBTabBarChild.addProperties({
  initWidget: function () {
    this.Super('initWidget', arguments);
  }
});