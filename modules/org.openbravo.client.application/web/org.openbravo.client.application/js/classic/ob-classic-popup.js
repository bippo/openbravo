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

// This file contains declarations for 3 types:
// - OBPopupClassicWindow: opens a popup to show a url, a classic OB process window.
// - OBClassicPopupModal: Opens a classic compatibility modal popup
// - OBClassicPopup: shows popups like heartbeat
// = Popup Classic OB Window =
//
// Opens a popup to show a url. Is used to show a classic OB process window.
//
isc.defineClass('OBPopupClassicWindow', isc.Class).addProperties({
  showsItself: true,
  command: 'DEFAULT',
  appURL: OB.Application.contextUrl + 'security/Menu.html',
  obManualURL: '',
  height: 450,
  width: 625
});

isc.OBPopupClassicWindow.addMethods({
  show: function () {
    var urlCharacter = '?',
        contentsURL;
    if (this.appURL.indexOf('?') !== -1) {
      urlCharacter = '&';
    }
    if (this.obManualURL !== '') {
      contentsURL = OB.Utilities.applicationUrl(this.obManualURL) + '?Command=' + this.command;
    } else {
      contentsURL = this.appURL + urlCharacter + 'Command=' + this.command + '&noprefs=true&tabId=' + this.tabId + '&hideMenu=true';
    }

    OB.Utilities.openProcessPopup(contentsURL, false, this.postParams, this.height, this.width);
  }
});

//= OBClassicPopupModal =
//
// Opens a modal classic compatibility popup to show a url. Is used to show a classic OB process window in modal popups.
//
isc.defineClass('OBClassicPopupModal', isc.Class).addProperties({
  showsItself: true,
  show: function () {
    OB.Layout.ClassicOBCompatibility.Popup.open(this.id, 625, 450, OB.Utilities.applicationUrl(this.obManualURL) + '?Command=' + this.command, '', null, false, false, true);
  }

});

isc.ClassFactory.defineClass('OBClassicPopup', isc.OBPopup);

// = OBClassicPopup =
//
// The OBClassicPopup is a shortcut for render classic OB popups in the new layout. It extends
// the OBPopup type (declared in the ob-popup.js file).
//
isc.OBClassicPopup.addProperties({

  init: function () {
    if (typeof this.width === 'string' && this.width.indexOf('%') !== -1) {
      this.percentualWidth = true;
    }
    if (typeof this.height === 'string' && this.height.indexOf('%') !== -1) {
      this.percentualHeight = true;
    }
    this.Super('init', arguments);
  },

  initWidget: function () {
    this.items = [
    isc.OBPopupHTMLFlow.create({
      contentsURL: ''
    })];
    this.Super('initWidget', arguments);
    var frameWidth, frameHeight;
    if (!this.percentualWidth) {
      frameWidth = this.width;
      frameWidth = parseInt(frameWidth, 10);
      frameWidth = frameWidth + this.edgeSize + this.edgeSize; // Smartclient to calculate the width takes into account the margin width
      frameWidth = parseInt(frameWidth, 10);
      if (frameWidth > OB.Layout.getVisibleWidth()) {
        frameWidth = OB.Layout.getVisibleWidth();
      }
      this.setWidth(frameWidth);
    }
    if (!this.percentualHeight) {
      frameHeight = this.height;
      frameHeight = parseInt(frameHeight, 10);
      frameHeight = frameHeight + this.edgeBottom + this.edgeTop; // Smartclient to calculate the height takes into account the margin width
      frameHeight = parseInt(frameHeight, 10);
      if (frameHeight > OB.Layout.getVisibleHeight()) {
        frameHeight = OB.Layout.getVisibleHeight();
      }
      this.setHeight(frameHeight);
    }
  },

  autoSize: false,
  showMaximizeButton: true,
  showHeaderIcon: true,
  showCloseButton: true,
  showMinimizeButton: true,
  showModalMask: true,
  showTitle: true,
  width: 600,
  height: 500,
  percentualWidth: false,
  percentualHeight: false,

  getIframeHtmlObj: function () {
    var container, iframes;
    container = this.getHandle();
    if (container && container.getElementsByTagName) {
      iframes = container.getElementsByTagName('iframe');
      if (iframes) {
        return iframes[0];
      }
    }
    return null;
  }
});