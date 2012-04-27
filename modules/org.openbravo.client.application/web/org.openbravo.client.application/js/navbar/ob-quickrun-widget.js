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
isc.ClassFactory.defineClass('OBQuickRun', isc.ImgButton);

// = OBQuickRun =
// The OBQuickRun widget creates a button with a layout which is displayed below
// it
// when the button gets clicked. The layout is hidden again when a mouse click
// happens on another part of the window. This makes use of the Smartclient
// click mask concept.
// The OBQuickRun extends from the Smartclient Button.
isc.OBQuickRun.addClassProperties({

  // ** {{{ currentQuickRun }}} **
  // The current OBQuickRun widget which is expanded (or null if none is
  // expanded).
  currentQuickRun: null,

  // ** {{{ hide }}} **
  // Class method which hides the one visible quick run widget layout (if one is
  // expanded).
  hide: function () {
    if (isc.OBQuickRun.currentQuickRun && isc.OBQuickRun.currentQuickRun.showing) {
      var tempQuickRun = isc.OBQuickRun.currentQuickRun;
      this.currentQuickRun = null;
      tempQuickRun.doHide();
    }
  }
});

// = OBQuickRun Properties =
isc.OBQuickRun.addProperties({

  autoFit: true,
  imageType: 'center',
  showRollOver: false,
  showFocused: false,
  showDown: false,
  overflow: 'visible',

  // ** {{{ layout }}} **
  // The layout which is expanded down when clicking the quick run button.
  layout: null,

  // ** {{{ layoutProperties }}} **
  // Properties which are used to configure the layout
  layoutProperties: {},

  // ** {{{ members }}} **
  // The members of the layout.
  members: [],

  // ** {{{ showing }}} **
  // Is set to true when the layout is showing/visible.
  showing: false,

  selectedHideLayout: null,

  draw: function () {
    var me = this,
        ksAction;

    if (!this.keyboardShortcutId) {
      return this.Super('draw', arguments);
    }

    ksAction = function () {
      me.getLayoutContainer().setStyleName('OBNavBarComponentSelected');
      if (!me.showing) {
        isc.EH.clickMaskClick();
      }
      setTimeout(function () {
        me.click();
      }, 10); //setTimeout to avoid delayCall function that manages the focus
      return false; //To avoid keyboard shortcut propagation
    };

    OB.KeyboardManager.Shortcuts.set(this.keyboardShortcutId, 'Canvas', ksAction);
    this.Super('draw', arguments);
  },

  // ** {{{ initWidget }}} **
  // Creates the layout (invisible as a default).
  initWidget: function () {
    // Always call the superclass implementation when overriding initWidget
    this.Super('initWidget', arguments);

    this.computeSetContent();
  },

  computeSetContent: function () {
    // set some defaults
    var defaultLayoutProperties = {
      styleName: 'OBFlyoutLayout',
      showShadow: false,
      layoutMargin: 10,
      shadowDepth: 5,
      bodyBackgroundColor: null
    };

    // compute the height
    if (this.members) {
      defaultLayoutProperties.members = this.members;
      var computedHeight = 0,
          i, length = this.members.length;
      for (i = 0; i < length; i++) {
        if (this.members[i].height) {
          computedHeight = computedHeight + this.members[i].height;
        }
      }
      defaultLayoutProperties.height = computedHeight;
    }

    // set the properties which are used, override by user set properties
    var usedLayoutProperties = {};
    isc.addProperties(usedLayoutProperties, defaultLayoutProperties, this.layoutProperties);

    // create the layout
    if (!this.layout) {
      this.layout = isc.VLayout.create(usedLayoutProperties);
    }
    // this.overCanvas = this.layout;
    // this.showOverCanvas = true;
  },

  resetLayout: function () {
    if (this.layout) {
      this.layout.destroy();
      this.layout = null;
    }
  },

  // ** {{{ click }}} **
  // clicking the button shows or hides the layout.
  click: function () {
    if (this.showing) {
      this.doHide();
      return false;
    } else {
      this.doShow();
    }
  },

  // 16012: Double click and single click on nav bar flyouts are treated the same
  // https://issues.openbravo.com/view.php?id=16012
  doubleClick: function () {
    this.click();
  },

  // ** {{{ keyPress }}} **
  // handle the escape and enter keys, these should hide the layout.
  keyPress: function () {
    var key = isc.EventHandler.getKey();
    if (key === 'Escape' || key === 'Enter') {
      if (isc.OBQuickRun.currentQuickRun) {
        isc.OBQuickRun.currentQuickRun.doHide();
      }
    }
    return true;
  },

  // ** {{{ doShow }}} **
  // Called to actually show the layout.
  doShow: function () {
    // start with clean form values
    // if (this.layout) {
    // for (var i=0; i < this.layout.members.length; i++) {
    // if (this.layout.members[i].clearValues) {
    // this.layout.members[i].clearValues();
    // }
    // }
    // }
    this.focusOnHide = isc.EH.getFocusCanvas();

    var left = this.getLeftPosition() + 1;
    var top = this.getPageTop() + this.getVisibleHeight() - 1;

    this.beforeShow();

    this.layout.placeNear(left, top);

    isc.OBQuickRun.clickMask = this.showClickMask('isc.OBQuickRun.hide()', 'soft', [this, this.layout]);

    this.layout.show();

    isc.OBQuickRun.currentQuickRun = this;

    // this code hides the horizontal line between the menu button and the
    // menu
    var layoutContainer = this.getLayoutContainer();
    layoutContainer.setStyleName('OBNavBarComponentSelected');
    this.selectedHideLayout = isc.Layout.create({
      styleName: 'OBNavBarComponentHideLine',
      height: 3,
      width: layoutContainer.getVisibleWidth() - 2,
      top: layoutContainer.getPageTop() + layoutContainer.getVisibleHeight() - 1,
      left: layoutContainer.getPageLeft() + 1,
      overflow: 'hidden'
    });
    this.selectedHideLayout.show();
    this.selectedHideLayout.moveAbove(this.layout);

    this.showing = true;
  },

  getLayoutContainer: function () {
    return this.parentElement;
  },

  getLeftPosition: function () {
    return this.parentElement.getPageLeft() - 1;
  },

  // ** {{{ beforeShow }}} **
  // Intended to be overridden, is called just before the layout.show()
  // method
  // is called.
  beforeShow: function () {},

  // ** {{{ doHide }}} **
  // Hide the expanded layout.
  doHide: function () {
    this.hideClickMask();
    this.layout.hide();

    this.getLayoutContainer().setStyleName('OBNavBarComponent');

    if (this.selectedHideLayout) {
      this.selectedHideLayout.hide();
      this.selectedHideLayout.destroy();
      this.selectedHideLayout = null;
    }

    this.showing = false;
    if (isc.OBQuickRun.currentQuickRun === this) {
      isc.OBQuickRun.currentQuickRun = null;
    }

    if (isc.isA.Canvas(this.focusOnHide)) {
      this.focusOnHide.focus();
    }

    if (typeof OB.MainView.TabSet.getSelectedTab().pane.tabSelected === 'function') {
      OB.MainView.TabSet.getSelectedTab().pane.tabSelected();
    }
  }
});