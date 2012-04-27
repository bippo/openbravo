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

// = ClassicOBCompatibility =
//
// The ClassicOBCompatibility handles the interaction between the classic OB window
// and the {{{Main View}}}. This includes opening views from the linked items display,
// from direct links in other tabs.
//
(function (OB, isc) {

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }

  // cache object references locally
  var O = OB,
      L = OB.Layout,
      M = OB.MainView,
      ISC = isc,
      cobcomp;

  function ClassicOBCompatibility() {}

  ClassicOBCompatibility.prototype = {

    // ** {{{ openLinkedItem(tabId, recordId) }}} **
    //
    // Opens a window from the linked item view.
    //
    // Parameters:
    // * {{{tabId}}} id of the tab to open
    // * {{{recordId}}} the record to show
    //
    openLinkedItem: function (tabId, recordId) {
      var doOpenClassicWindow;
      doOpenClassicWindow = function (response, data, request) {

        if (!data.recordId || data.recordId.length === 0) {
          L.ViewManager.openView('OBClassicWindow', {
            tabTitle: data.tabTitle,
            windowId: data.windowId,
            tabId: data.tabId,
            mappingName: data.mappingName,
            command: 'DEFAULT'
          });
        } else {
          L.ViewManager.openView('OBClassicWindow', {
            tabTitle: data.tabTitle,
            windowId: data.windowId,
            tabId: data.tabId,
            mappingName: data.mappingName,
            keyParameter: data.keyParameter,
            recordId: data.recordId,
            command: 'DIRECT'
          });
        }
      };

      OB.RemoteCallManager.call('org.openbravo.client.application.ComputeWindowActionHandler', {}, {
        'tabId': tabId,
        'recordId': recordId
      }, doOpenClassicWindow);
    },

    // ** {{{ sendDirectLink(action, form) }}} **
    //
    // Shows a new tab with the clicked link content
    //
    // Parameters:
    // * {{{action}}} is a String with the Command action to call the servlet,
    // e.g. {{{ JSON }}}
    // * {{{form}}} is an Object of the form from where it has been submitted
    //
    sendDirectLink: function (action, form) {

      //
      // Returns a form field and value as a javascript object composed by name and value fields
      //

      function inputValueForms(name, field) {
        var result = {};
        if (!field || name.toString().replace(/^\s*/, '').replace(/\s*$/, '') === '') {
          return result;
        }
        if (!field.type && field.length > 1) {
          field = field[0];
        }
        if (field.type) {
          if (field.type.toUpperCase().indexOf('SELECT') !== -1) {
            if (field.selectedIndex === -1) {
              return result;
            } else {
              var length = field.options.length,
                  fieldsCount;
              for (fieldsCount = 0; fieldsCount < length; fieldsCount++) {
                if (field.options[fieldsCount].selected) {
                  result.name = name;
                  result.value = field.options[fieldsCount].value;
                }
              }
              return result;
            }
          } else if (field.type.toUpperCase().indexOf('RADIO') !== -1 || field.type.toUpperCase().indexOf('CHECK') !== -1) {
            if (!field.length) {
              if (field.checked) {
                result.name = name;
                result.value = field.value;
                return result;
              } else {
                return result;
              }
            } else {
              var total = field.length,
                  i;
              for (i = 0; i < total; i++) {
                if (field[i].checked) {
                  result.name = name;
                  result.value = field[i].value;
                }
              }
              return result;
            }
          } else {
            result.name = name;
            result.value = field.value;
            return result;
          }
        }

        return result;
      }

      //
      // Returns a JSON of all the form fields. Also adds Command and IsAjaxCall
      // parameters to communicate with the classic OB class
      //

      function getXHRParamsObj(action, formObject) {
        var paramsObj = {},
            i, length = formObject.elements.length;

        paramsObj.Command = action;
        paramsObj.IsAjaxCall = '1';

        for (i = 0; i < length; i++) {
          if (formObject.elements[i].type) {
            var param = inputValueForms(formObject.elements[i].name, formObject.elements[i]);

            if (param && param.name !== 'Command') {
              paramsObj[param.name] = param.value;
            }
          }
        }
        return paramsObj;
      }

      //
      // Function used by the {{ ISC.RPCManager }} after receiving the
      // target address from the back-end
      //

      function fetchSendDirectLinkCallback(response, data) {
        OB.Utilities.openView(data.windowId, data.tabId, data.tabTitle, data.recordId);
      }

      //
      // Fetch the target address from the server. After fetching
      // the implementation, stores a reference in the cache
      //

      function fetchSendDirectLink(action, formObject) {
        var paramsObj = getXHRParamsObj(action, formObject);
        // hardcode the reference link url
        // formObject.action.toString();
        // hardcode to prevent this issue with chrome:
        // https://issues.openbravo.com/view.php?id=13837
        var actionURL = OB.Application.contextUrl + 'utility/ReferencedLink.html';

        var rpcMgr = ISC.RPCManager;
        var reqObj = {
          params: paramsObj,
          callback: fetchSendDirectLinkCallback,
          evalResult: true,
          httpMethod: 'GET',
          useSimpleHttp: true,
          actionURL: actionURL
        };
        var request = rpcMgr.sendRequest(reqObj);
      }
      fetchSendDirectLink(action, form);
    },

    setTabInformation: function (windowId, tabId, recordId, mode, obManualURL, title) {
      var tabNumber = null,
          tabSet, tabPane;
      tabSet = M.TabSet;
      if (windowId) {
        tabNumber = L.ViewManager.views.getTabNumberFromViewParam('windowId', windowId);
      } else if (obManualURL) {
        tabNumber = L.ViewManager.views.getTabNumberFromViewParam('obManualURL', obManualURL);
      } else if (tabId) {
        tabNumber = L.ViewManager.views.getTabNumberFromViewParam('tabId', tabId);
      }

      if (!tabNumber) {
        tabNumber = tabSet.getTabNumber(tabSet.getSelectedTab());
      }

      if (tabNumber === null) {
        return false;
      }

      tabPane = tabSet.getTabPane(tabNumber);

      tabSet.setTabTitle(tabNumber, title);
      tabPane.updateTabInformation(windowId, tabId, recordId, mode, obManualURL, title);
    },

    Keyboard: {
      getMDIKS: function () {
        var key, auxKey, action, funcParam, keyMap, ClassicKeyJSON = [],
            LKS = O.KeyboardManager.Shortcuts,
            i, length = LKS.list.length;

        for (i = 0; i < length; i++) {
          auxKey = '';
          if (LKS.list[i].isGlobal) {
            if (LKS.list[i].keyComb.ctrl === true) {
              if (auxKey.length > 0) {
                auxKey += '+';
              }
              auxKey += 'ctrlKey';
            }
            if (LKS.list[i].keyComb.alt === true) {
              if (auxKey.length > 0) {
                auxKey += '+';
              }
              auxKey += 'altKey';
            }
            if (LKS.list[i].keyComb.shift === true) {
              if (auxKey.length > 0) {
                auxKey += '+';
              }
              auxKey += 'shiftKey';
            }
            if (LKS.list[i].keyComb.key !== null) {
              key = LKS.list[i].keyComb.key;
            }

            action = LKS.list[i].action;
            funcParam = LKS.list[i].funcParam;

            // Special keys nomenclature adaptation from Smartclient way to
            // classic utils.js way
            keyMap = {
              'Backspace': 'BACKSPACE',
              'Tab': 'TAB',
              'Enter': 'ENTER',
              'Space': 'SPACE',
              'Insert': 'INSERT',
              'End': 'END',
              'Home': 'HOME',
              'Page_Up': 'REPAGE',
              'Page_Down': 'AVPAGE',
              'Arrow_Left': 'LEFTARROW',
              'Arrow_Right': 'RIGHTARROW',
              'Arrow_Up': 'UPARROW',
              'Arrow_Down': 'DOWNARROW',
              '+': 'NUMBERPOSITIVE',
              '-': 'NUMBERNEGATIVE',
              '.': 'NUMBERDECIMAL',
              'Escape': 'ESCAPE',
              'f1': 'F1',
              'f2': 'F2',
              'f3': 'F3',
              'f4': 'F4',
              'f5': 'F5',
              'f6': 'F6',
              'f7': 'F7',
              'f8': 'F8',
              'f9': 'F9',
              'f10': 'F10',
              'f11': 'F11',
              'f12': 'F12'
            };

            key = keyMap[key] || key;

            ClassicKeyJSON.push({
              'key': key,
              'auxKey': auxKey,
              'action': action,
              'funcParam': funcParam
            });
          }
        }

        return ClassicKeyJSON;
      },

      executeKSFunction: function (func, funcParam) {
        func(funcParam);
      }
    },

    Popup: {
      secString: 'dbXE8hjGuKyMefVf',

      // ** {{{ Popup.open(name, width, height, url, title, theOpener) }}} **
      //
      // Shows a new popup with content
      //
      // Parameters:
      // * {{{name}}} type: String - the name of the window. Used to manage
      // window.open target
      // * {{{width}}} type: String | Number - the width of the popup. It can be
      // set as a String as a %
      // * {{{height}}} type: String | Number - the height of the popup. It can
      // be set as a String as a %
      // * {{{url}}} type: String - the url to be opened in the popup
      // * {{{title}}} type: String - the title to be displayed in the popup
      // * {{{theOpener}}} type: Window Object - the window object of the opener
      // * {{{showMinimizeControl}}} type: Boolean - to specify if the popup should show the minimize control or not. The default value is "true" if it is not specified
      // * {{{showMaximizeControl}}} type: Boolean - to specify if the popup should show the maximize control or not. The default value is "true" if it is not specified
      // * {{{showCloseControl}}} type: Boolean - to specify if the popup should show the close control or not. The default value is "true" if it is not specified
      // * {{{postParams}}} type: Object - parameters to be sent to the url using POST instead of GET
      // * {{{isModal}}} type: Boolean - to specify if the popup should be modal or not. The default value is "true" if it is not specified
      // of the popup. Used in window.open to allow IE know which is the opener
      // 
      // returns the created OBClassicPopupWindow
      open: function (name, width, height, url, title, theOpener, showMinimizeControl, showMaximizeControl, showCloseControl, postParams, isModal) {
        var urlCharacter = (url && url.indexOf('?') !== -1) ? '&' : '?';

        if (showMinimizeControl !== false) {
          showMinimizeControl = true;
        }
        if (showMaximizeControl !== false) {
          showMaximizeControl = true;
        }
        if (showCloseControl !== false) {
          showCloseControl = true;
        }
        if (isModal !== false) {
          isModal = true;
        }
        var cPopup = isc.OBClassicPopup.create({
          ID: name + '_' + cobcomp.Popup.secString,
          width: width,
          height: height,
          showMinimizeButton: showMinimizeControl,
          showMaximizeButton: showMaximizeControl,
          showCloseButton: showCloseControl,
          isModal: isModal,
          showModalMask: isModal,
          theOpener: theOpener,
          areParamsSet: false,
          isFramesetDraw: false,
          isLoaded: false,
          htmlCode: '<html><head></head><frameset cols="*, 0%" rows="*" frameborder="no" border="0" framespacing="0">' + '<frame id="MDIPopupContainer" name="MDIPopupContainer"></frame>' + '<frame name="frameMenu" scrolling="no" src="' + OB.Application.contextUrl + 'utility/VerticalMenu.html?Command=HIDE" id="paramFrameMenuLoading"></frame>' + '</frameset><body></body></html>',
          popupURL: url + urlCharacter + 'IsPopUpCall=1'
        });
        cPopup.show();
        cobcomp.Popup.postOpen(cPopup, postParams);
        OB.Utilities.registerClassicPopupInTestRegistry(url, cPopup);
        return cPopup;
      },

      // ** {{{ Popup.postOpen(cPopup, postParams) }}} **
      //
      // Actions to be performed once the popup is draw.
      //
      // Parameters:
      // * {{{cPopup}}} type: Canvas - the drawn popup
      // * {{{postParams}}} type: Object - parameters to be sent to the url using POST instead of GET
      postOpen: function (cPopup, postParams) {
        if (!cPopup.isFramesetDraw) {
          cPopup.getIframeHtmlObj().contentWindow.document.write(cPopup.htmlCode);
          cPopup.isFramesetDraw = true;
        }
        if (!cPopup.getIframeHtmlObj().contentWindow.frames[0].document.body) {
          setTimeout(function () {
            cobcomp.Popup.postOpen(cPopup, postParams);
          }, 50);
          return true;
        }
        if (navigator.userAgent.toUpperCase().indexOf('MSIE') !== -1) {
          //  In IE if window.open is executed against a frame, the target frame doesn't know which is its opener
          if (typeof cPopup.getIframeHtmlObj().contentWindow.frames[0].opener === 'undefined') {
            cPopup.getIframeHtmlObj().contentWindow.frames[0].opener = cPopup.theOpener;
            if (typeof cPopup.getIframeHtmlObj().contentWindow.frames[0].opener === 'undefined') {
              setTimeout(function () {
                cobcomp.Popup.postOpen(cPopup, postParams);
              }, 50);
              return true;
            }
          }
        }
        var wName = cPopup.ID;
        wName = wName.substring(0, wName.lastIndexOf(cobcomp.Popup.secString) - 1);
        if (!cPopup.areParamsSet) {
          if (!postParams) {
            cPopup.getIframeHtmlObj().contentWindow.frames[0].location.href = cPopup.popupURL;
          } else {
            // Create a form and POST parameters as input hidden values
            var doc = cPopup.getIframeHtmlObj().contentWindow.frames[0].document,
                frm = doc.createElement('form'),
                i;
            frm.setAttribute('method', 'post');
            frm.setAttribute('action', cPopup.popupURL);
            for (i in postParams) {
              if (postParams.hasOwnProperty(i)) {
                var inp = doc.createElement('input');
                inp.setAttribute('type', 'hidden');
                inp.setAttribute('name', i);
                inp.setAttribute('value', postParams[i]);
                frm.appendChild(inp);
              }
            }
            doc.body.appendChild(frm);
            frm.submit();
          }
          cPopup.getIframeHtmlObj().contentWindow.frames[0].name = wName;
          cPopup.getIframeHtmlObj().contentWindow.document.getElementById('MDIPopupContainer').name = wName;
          cPopup.areParamsSet = true;
        }
        if (cPopup.areParamsSet && cPopup.getIframeHtmlObj().contentWindow.frames[0].name !== wName) {
          setTimeout(function () {
            cobcomp.Popup.postOpen(cPopup, postParams);
          }, 50);
        }
        cPopup.isLoaded = true;
      },

      // ** {{{ Popup.close(name) }}} **
      //
      // Closes a popup
      //
      // Parameters:
      // * {{{name}}} type: String - the name of the window
      close: function (name, cancelEvent) {
        var activateView;

        name = name + '_' + cobcomp.Popup.secString;
        activateView = window[name].activeViewWhenClosed;
        window[name].closeClick();

        if (!cancelEvent && activateView) {
          activateView.setAsActiveView();
        }
      },

      // ** {{{ Popup.getPopup(name) }}} **
      //
      // Get the popup instance.
      //
      // Parameters:
      // * {{{name}}} type: String - the name of the window
      getPopup: function (name) {
        return window[name + '_' + cobcomp.Popup.secString];
      },

      // ** {{{ Popup.close(name, width, height) }}} **
      //
      // Resizes a poup
      //
      // Parameters:
      // * {{{name}}} type: String - the name of the window
      // * {{{width}}} type: Number - width to resize to
      // * {{{height}}} type: Number - height to resize to
      resize: function (name, width, height) {
        name = name + '_' + cobcomp.Popup.secString;
        window[name].resizeTo(width, height);
      },

      // ** {{{ Popup.setTitle(name, title) }}} **
      //
      // Sets the window title
      //
      // Parameters:
      // * {{{name}}} type: String - the name of the window
      // * {{{title}}} type: String - the title of the window
      setTitle: function (name, title) {
        name = name + '_' + cobcomp.Popup.secString;
        window[name].setTitle(title);
      },

      // ** {{{ Popup.isLoaded(name) }}} **
      //
      // Function to know if a popup is already opened (and ready) or not
      //
      // Parameters:
      // * {{{name}}} type: String - the name of the window
      isLoaded: function (name) {
        name = name + '_' + cobcomp.Popup.secString;
        if (window[name].isLoaded) {
          return true;
        } else {
          return false;
        }
      },


      /** Upgrading process pop ups **/
      standardUpgrading: function () {
        var actionButton = isc.addProperties({}, isc.Dialog.OK, {
          getTitle: function () {
            return '<b>' + OB.I18N.getLabel('OBUIAPP_LogOut') + '</b>';
          },
          click: function () {
            this.topElement.cancelClick();
            OB.Utilities.logout(true);
          }
        });

        isc.confirm(OB.I18N.getLabel('OBUIAPP_UpgradeDesc'), {
          isModal: true,
          showModalMask: true,
          title: OB.I18N.getLabel('OBUIAPP_UpgradeTitle'),
          toolbarButtons: [actionButton, isc.Dialog.CANCEL]
        });
      },

      openAPRMPopup: function () {
        var actionButton = isc.addProperties({}, isc.Dialog.OK, {
          getTitle: function () {
            return '<b>' + OB.I18N.getLabel('OBUIAPP_UpgradeRunAPRMBtn') + '</b>';
          },
          click: function () {
            this.topElement.cancelClick();
            OB.Layout.ViewManager.openView('OBClassicWindow', {
              command: "DEFAULT",
              formId: "E4F4AAC7DD6D4FBDA3AF973B7767F374",
              icon: "Form",
              id: "/org.openbravo.erputil.aprmigrationtool.ad_forms/MigrationTool.html",
              obManualURL: "/org.openbravo.erputil.aprmigrationtool.ad_forms/MigrationTool.html",
              tabTitle: OB.I18N.getLabel('APRMT_MigrationToolTitle'),
              viewId: "OBClassicWindow"
            });
          }
        });

        isc.confirm(OB.I18N.getLabel('OBUIAPP_UpgradeRunAPRMDesc'), {
          isModal: true,
          showModalMask: true,
          title: OB.I18N.getLabel('OBUIAPP_UpgradeRunAPRMTitle'),
          toolbarButtons: [actionButton, isc.Dialog.CANCEL]
        });
      },

      openConfigScriptPopup: function (scripts) {
        var actionButton = isc.addProperties({}, isc.Dialog.OK, {
          click: function () {
            this.topElement.cancelClick();
          }
        });

        isc.confirm(OB.I18N.getLabel('OBUIAPP_UpgradeScriptDesc', [scripts]), {
          isModal: true,
          showModalMask: true,
          title: OB.I18N.getLabel('OBUIAPP_UpgradeScriptTitle'),
          toolbarButtons: [actionButton]
        });
      },

      openSuccessUpgradePopup: function () {
        var actionButton = isc.addProperties({}, isc.Dialog.OK, {
          click: function () {
            this.topElement.cancelClick();
            OB.PropertyStore.set('isUpgrading', 'N', null, false, true);
          }
        });

        isc.confirm(OB.I18N.getLabel('OBUIAPP_UpgradeEndDesc'), {
          isModal: true,
          showModalMask: true,
          title: OB.I18N.getLabel('OBUIAPP_UpgradeEndTitle'),
          toolbarButtons: [actionButton]
        });
      },

      /** Particular windows * */
      // ** {{{ Popup.openInstancePurpose() }}} **
      //
      // Opens directly the "Instance Purpose" window inside a popup
      openInstancePurpose: function () {
        cobcomp.Popup.open('InstancePurpose', 600, 500, OB.Application.contextUrl + 'ad_forms/InstancePurpose.html', '', window, false, false, true);
      },

      // ** {{{ Popup.openHeartbeat() }}} **
      //
      // Opens directly the "Heartbeat" window inside a popup
      openHeartbeat: function () {
        cobcomp.Popup.open('Heartbeat', 600, 500, OB.Application.contextUrl + 'ad_forms/Heartbeat.html', '', window, false, false, true);
      },

      // ** {{{ Popup.openRegistration() }}} **
      //
      // Opens directly the "Registration" window inside a popup
      openRegistration: function () {
        cobcomp.Popup.open('Registration', 600, 500, OB.Application.contextUrl + 'ad_forms/Registration.html', '', window, false, false, true);
      }
    }
  };

  // Initialize ClassicOBCompatibility object
  cobcomp = L.ClassicOBCompatibility = new ClassicOBCompatibility();
}(OB, isc));

isc.ClassFactory.defineClass('OBUIAPP_RegistrationView', isc.Layout).addProperties({
  initWidget: function () {
    OB.Layout.ClassicOBCompatibility.Popup.openRegistration();

    this.Super('initWidget', arguments);
  }
});