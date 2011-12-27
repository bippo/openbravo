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
 * All portions are Copyright (C) 2001-2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview This Javascript library supports keyboard navigation within
* the editing windows (tab navigation, focus highlight, sequence of elements,
* etc.).
*/

var focusedWindowElement = null;
var focusedWindowElement_tmp = null;
var focusedWindowElement_tmp2 = null;
var drawnWindowElement = null;
var windowTableParentElement = null;
var focusedWindowTable = 0;
var frameLocked = false;
var currentWindowElementType = null;
var previousWindowElementType = null;

var selectedArea = 'window';
var isGridFocused = null;
var isGenericTreeFocused = null;
var isClickOnGrid = null;
var isClickOnGenericTree = null;
var defaultActionElement = null;
var defaultActionType = null;

var isTabPressed = null;
var isOBTabBehavior = true;
var isFirstTime = true;
var isReadOnlyWindow =  false;

var selectInputTextOnTab = true;

var isGoingDown = null;
var isGoingUp = null;

var selectedCombo = null;
var isSelectedComboOpened = null;

var hasCloseWindowSearch = null;

var propagateEnter = true;

var isContextMenuOpened = false;


windowKeyboardCaptureEvents();
function windowKeyboardCaptureEvents() {
  if (typeof captureOnMouseDown == 'undefined' || captureOnMouseDown != false) {
    document.onmousedown=mouseDownLogic;

    if (document.layers) {
      window.captureEvents(Event.ONMOUSEDOWN);
      window.onmousedown=mouseDownLogic;
    }
  }
  if (typeof captureOnClick == 'undefined' || captureOnClick != false) {
    document.onclick=mouseClickLogic;

    if (document.layers) {
      window.captureEvents(Event.ONCLICK);
      window.onclick=mouseClickLogic;
    }
  }
}

function activateDefaultAction(){
  if (defaultActionElement != null && defaultActionElement != 'null' && defaultActionElement != '') {
    if (isSelectedComboOpened != true) {
      defaultActionType = 'button';
      drawWindowElementDefaultAction(defaultActionElement);
    } else {
      comboDefaultAction();
    }
  }
}

function disableDefaultAction() {
  if (defaultActionElement != null && defaultActionElement != 'null' && defaultActionElement != '') {
    defaultActionType = null;
    eraseWindowElementDefaultAction(defaultActionElement);
  }
}

function comboDefaultAction() {
  if (defaultActionElement != null && defaultActionElement != 'null' && defaultActionElement != '') {
    eraseWindowElementDefaultAction(defaultActionElement);
    defaultActionType = 'combo';
  }
}


function defaultActionLogic(obj) {
  disableDefaultAction();
  defaultActionElement = document.getElementById(windowTables[focusedWindowTable].defaultActionButtonId);
  try {
    if ((obj.tagName == 'INPUT' && obj.getAttribute('type') != 'file') || obj.tagName == 'SELECT') {
      activateDefaultAction();
      for (var i = 0; i < keyArray.length; i++) {
        if (keyArray[i] != null && keyArray[i]) {
          if (keyArray[i].key == 'ENTER') {
            if (keyArray[i].auxKey == null || keyArray[i].auxKey == '' || keyArray[i].auxKey == 'null') {
              if (keyArray[i].field == obj.getAttribute('name')) {
                disableDefaultAction();
                break;
              }
            }
          }
        }
      }
    } else {
      disableDefaultAction();
    }
  } catch(e) {}
}

function enableDefaultAction() {
  defaultActionElement = null;
  activateDefaultAction();
}

function activeElementFocus() {
  if (focusedWindowElement && selectedArea=='window') putWindowElementFocus(focusedWindowElement);
}

function setSelectedArea(area) {
  selectedArea = area;
}

function swichSelectedArea() {
  if (selectedArea=='window' && tabsTables[0].tabTableId) {
    previousWindowElementType=currentWindowElementType;
    currentWindowElementType='tab';
    focusedWindowElement_tmp2 = focusedWindowElement;
    selectedArea = 'tabs';
    removeWindowElementFocus(focusedWindowElement);
    setActiveTab();
  } else if (selectedArea=='tabs' && !isReadOnlyWindow) {
    selectedArea = 'window';
    removeTabFocus(focusedTab);
    currentWindowElementType=previousWindowElementType;
    setWindowElementFocus(focusedWindowElement_tmp2);
  } else {
    return false;
  }
}

function windowTableId(tableId, defaultActionButtonId) {
  this.tableId = tableId;
  this.defaultActionButtonId = defaultActionButtonId;
}

function setWindowTableParentElement() {
  setMDIEnvironment();
  windowTableParentElement = windowTables[focusedWindowTable].tableId;
}

var isOnMouseDown = null;

function mouseDownLogic(evt, obj) {
  if(obj == null) {
    obj = (!document.all) ? evt.target : event.srcElement;
  }
  if (obj.tagName == 'OPTION') {
    while(obj.tagName != 'SELECT') obj = obj.parentNode;
  }

  if (obj.tagName == 'TD') { /* To check if clicked TD is part of a Button */
    try {
      var parentOfTable = getObjParent(getObjParent(getObjParent(getObjParent(obj))));
      if (parentOfTable.tagName == 'BUTTON') {
        obj = parentOfTable;
      }
    }
    catch (e) { }
  }

  if (obj.tagName == 'IMG') { /* To check if clicked IMG is part of a Button */
    try {
      var parentOfTable = getObjParent(getObjParent(getObjParent(getObjParent(getObjParent(obj)))));
      if (parentOfTable.tagName == 'BUTTON') {
        obj = parentOfTable;
      }
    }
    catch (e) { }
  }

  if (checkGenericTree(obj)) {
    return true;
  }

  cursorFocus(obj, 'onmousedown');

  if (obj.tagName == 'SELECT') {
    comboKeyBehaviour(obj,'onmousedown');
  }

  if (hasCloseWindowSearch) {
    closeWindowSearch();
    hasCloseWindowSearch = false;
  }
}

function mouseClickLogic(evt, obj) {
  if(obj == null) {
    obj = (!document.all) ? evt.target : event.srcElement;
  }
  if (obj.tagName == 'OPTION') {
    while(obj.tagName != 'SELECT') obj = obj.parentNode;
  }

  if (obj.tagName == 'TD') { /* To check if clicked TD is part of a Button */
    try {
      var parentOfTable = getObjParent(getObjParent(getObjParent(getObjParent(obj))));
      if (parentOfTable.tagName == 'BUTTON') {
        obj = parentOfTable;
      }
    }
    catch (e) { }
  }

  if (obj.tagName == 'IMG') { /* To check if clicked IMG is part of a Button */
    try {
      var parentOfTable = getObjParent(getObjParent(getObjParent(getObjParent(getObjParent(obj)))));
      if (parentOfTable.tagName == 'BUTTON') {
        obj = parentOfTable;
      }
    }
    catch (e) { }
  }

  if (checkGenericTree(obj)) {
    return true;
  }

  cursorFocus(obj, 'onclick');

  if (obj.tagName == 'SELECT') {
    comboKeyBehaviour(obj,'onclick');
  }
}

function cursorFocus(obj, event) {
  isContextMenuOpened = false;
  if (obj == null || obj == 'null' || obj == '') { return false; }
  if (navigator.userAgent.toUpperCase().indexOf("MSIE") != -1 && obj.getAttribute('type') == 'checkbox' && (obj.getAttribute('readonly') == 'true' || obj.readOnly)) {
    return false;
  }
  if (event == 'onmousedown') {
    if (obj == drawnWindowElement) return true;
    if(!isClickOnGrid==true) blurGrid();
    isClickOnGrid=false;
  }
  if (isInsideWindowTable(obj) && couldHaveFocus(obj) && event == 'onmousedown') {
    removeTabFocus(focusedTab);
    frameLocked = false;
    selectedArea = 'window';
    focusedWindowElement = obj;
    setWindowElementFocus(focusedWindowElement, "obj", event);
  } else if (event == 'onclick') {
    if (selectedArea == 'window') {
      //setWindowElementFocus(focusedWindowElement, "obj", event);
      if (obj != focusedWindowElement) {
        eraseWindowElementFocus(focusedWindowElement);
      }
    } else if (selectedArea == 'tabs') {
      setTabFocus(focusedTab);
    }
  }
  return true;
}

function checkGenericTree(obj) {
  if (obj == null || obj == 'null' || obj == '') { return false; }
  if(!isClickOnGenericTree==true) blurGenericTree();
  isClickOnGenericTree=false;
  if (isGenericTreeFocused) {
    while(obj.tagName != 'BODY') {
      if (obj.getAttribute('id') != null) {
        if (obj.getAttribute('id').indexOf('genericTree') != -1) {
          return true;
        }
      }
      obj = obj.parentNode;
    }
  }
  return false
}

function comboKeyBehaviour(obj, event) {
  if (obj == null || obj == 'null' || obj == '') { return false; }
  if (event == 'onmousedown') {
    isOnMouseDown = true;
    if (focusedWindowElement == null) focusedWindowElement = '';
    if(obj.tagName == 'SELECT' && isSelectedComboOpened == true && selectedCombo == obj) {
      selectedCombo = obj;
      isSelectedComboOpened = false;
      activateDefaultAction();
    } else {
      if(obj.tagName == 'SELECT') {
        selectedCombo = obj;
        isSelectedComboOpened = true;
        comboDefaultAction();
      } else if (focusedWindowElement.tagName == 'SELECT') {
        selectedCombo = obj;
        isSelectedComboOpened = false;
        activateDefaultAction();
      }
    }
    return true;
  } else if (event == 'onclick') {
    if (obj.tagName == 'SELECT' && isOnMouseDown!=true) {
      selectedCombo = obj;
      isSelectedComboOpened = false;
      activateDefaultAction();
    }
    isOnMouseDown = false;
    return true;
  }
  return false;
}

function isInsideWindowTable(obj) {
  try {
    for(;;) {
      if(obj == null) { return false; }
      obj=obj.parentNode;
      for(var i=0;i<windowTables.length;i++) {
        if (obj==document.getElementById(windowTables[i].tableId)) {
          focusedWindowTable = i;
          setWindowTableParentElement();
          return true;
        }
      }
    }
  } catch(e) {
    return false;
  }
}

function setWindowElementFocus(obj, type, event) {
  if (type == null || type == 'null' || type == '') {
    type = "obj";
  } else if (type == "id") {
    obj = document.getElementById(obj);
  }

  if (type == "id" && !canHaveFocus(obj)) {
    setFirstWindowElementFocus();
    return false;
  }

  if (obj == 'firstElement') {
    setFirstWindowElementFocus();
  } else if (obj == 'lastElement') {
    setLastWindowElementFocus();
  } else {
    removeWindowElementFocus(focusedWindowElement_tmp);
    focusedWindowElement = obj;
    focusedWindowElement_tmp = focusedWindowElement;
    if(!frameLocked) {
      putWindowElementFocus(focusedWindowElement, event);
    }
  }
}

function drawWindowElementDefaultAction(obj) {
  try {
    if(obj.tagName == 'A') {                  //Used in old r2.40 button definition
      if (obj.className.indexOf('ButtonLink_default') == -1 && obj.className.indexOf('ButtonLink') != -1 && obj.className.indexOf('ButtonLink_disabled') == -1) { //Used in old r2.40 button definition
        obj.className = 'ButtonLink_default'; //Used in old r2.40 button definition
      }                                       //Used in old r2.40 button definition
    } else if(obj.tagName == 'BUTTON') {
      if (obj.className.indexOf('ButtonLink_default') == -1 && obj.className.indexOf('ButtonLink') != -1 && obj.className.indexOf('ButtonLink_disabled') == -1) {
        obj.className = 'ButtonLink_default';
      }
    }
  } catch (e) {
  }
}

function eraseWindowElementDefaultAction(obj) {
  try {
    if(obj.tagName == 'A') {                                                    //Used in old r2.40 button definition
      obj.className = obj.className.replace('ButtonLink_default','ButtonLink'); //Used in old r2.40 button definition
    } else if(obj.tagName == 'BUTTON') {
      obj.className = obj.className.replace('ButtonLink_default','ButtonLink');
    }
  } catch (e) {
  }
}

function drawWindowElementFocus(obj) {
  drawnWindowElement = obj;
  try {
    if (parent.frames['frameMenu']) parent.frames['frameMenu'].onBlurMenu();
  } catch (e) {
  }
  try {
    if(obj.tagName == 'A') {
      if (obj.className.indexOf(' Popup_Client_Help_LabelLink_focus') == -1 && obj.className.indexOf('Popup_Client_Help_LabelLink') != -1) {
        obj.className = obj.className + ' Popup_Client_Help_LabelLink_focus';
      } else if (obj.className.indexOf('DataGrid_Popup_text_pagerange_focus') == -1 && obj.className.indexOf('DataGrid_Popup_text_pagerange') != -1) {
        obj.className = obj.className + ' DataGrid_Popup_text_pagerange_focus';
      } else if (obj.className.indexOf('Popup_Client_Help_Icon_LabelLink_focus') == -1 && obj.className.indexOf('Popup_Client_Help_Icon_LabelLink') != -1) {
        obj.className = 'Popup_Client_Help_Icon_LabelLink_focus';
      } else if (obj.className.indexOf(' Popup_Client_UserOps_LabelLink_Selected_focus') == -1 && obj.className.indexOf('Popup_Client_UserOps_LabelLink_Selected') != -1) {
        obj.className = obj.className + ' Popup_Client_UserOps_LabelLink_Selected_focus';
      } else if (obj.className.indexOf(' Popup_Client_UserOps_LabelLink_focus') == -1 && obj.className.indexOf('Popup_Client_UserOps_LabelLink') != -1) {
        obj.className = obj.className + ' Popup_Client_UserOps_LabelLink_focus';
      } else if (obj.className.indexOf(' LabelLink_noicon_focus') == -1 && obj.className.indexOf('LabelLink_noicon') != -1) {
        obj.className = obj.className + ' LabelLink_noicon_focus';
      } else if (obj.className.indexOf('LabelLink_focus') == -1 && obj.className.indexOf('LabelLink') != -1) {
        obj.className = obj.className + ' LabelLink_focus';
      } else if (obj.className.indexOf('FieldButtonLink_focus') == -1 && obj.className.indexOf('FieldButtonLink') != -1) {
        obj.className = 'FieldButtonLink_focus';
      } else if (obj.className.indexOf('ButtonLink_focus') == -1 && obj.className.indexOf('ButtonLink') != -1 && obj.className.indexOf('ButtonLink_disabled') == -1) { //Used in old r2.40 button definition
        obj.className = 'ButtonLink_focus'; //Used in old r2.40 button definition
      } else if (obj.className.indexOf('List_Button_TopLink_focus') == -1 && obj.className.indexOf('List_Button_TopLink') != -1) {
        obj.className = 'List_Button_TopLink_focus';
      } else if (obj.className.indexOf('List_Button_MiddleLink_focus') == -1 && obj.className.indexOf('List_Button_MiddleLink') != -1) {
        obj.className = 'List_Button_MiddleLink_focus';
      } else if (obj.className.indexOf('List_Button_BottomLink_focus') == -1 && obj.className.indexOf('List_Button_BottomLink') != -1) {
        obj.className = 'List_Button_BottomLink_focus';
      } else if (obj.className.indexOf('Dimension_LeftRight_Button_TopLink_focus') == -1 && obj.className.indexOf('Dimension_LeftRight_Button_TopLink') != -1) {
        obj.className = 'Dimension_LeftRight_Button_TopLink_focus';
      } else if (obj.className.indexOf('Dimension_LeftRight_Button_BottomLink_focus') == -1 && obj.className.indexOf('Dimension_LeftRight_Button_BottomLink') != -1) {
        obj.className = 'Dimension_LeftRight_Button_BottomLink_focus';
      } else if (obj.className.indexOf('Dimension_UpDown_Button_BottomLink_focus') == -1 && obj.className.indexOf('Dimension_UpDown_Button_BottomLink') != -1) {
        obj.className = 'Dimension_UpDown_Button_BottomLink_focus';
      } else if (obj.className.indexOf('Dimension_UpDown_Button_TopLink_focus') == -1 && obj.className.indexOf('Dimension_UpDown_Button_TopLink') != -1) {
        obj.className = 'Dimension_UpDown_Button_TopLink_focus';
      } else if (obj.className.indexOf('Popup_Workflow_Button_focus') == -1 && obj.className.indexOf('Popup_Workflow_Button') != -1) {
        obj.className = 'Popup_Workflow_Button_focus';
      } else if (obj.className.indexOf('Popup_Workflow_text_focus') == -1 && obj.className.indexOf('Popup_Workflow_text') != -1) {
        obj.className = 'Popup_Workflow_text_focus';
      } else if (obj.className.indexOf('MessageBox_TextLink_focus') == -1 && obj.className.indexOf('MessageBox_TextLink') != -1) {
        obj.className = 'MessageBox_TextLink_focus';
      }
      isFirstTime = false;
    } else if (obj.tagName == 'BUTTON') {
      if (obj.className.indexOf('ButtonLink_focus') == -1 && obj.className.indexOf('ButtonLink') != -1 && obj.className.indexOf('ButtonLink_disabled') == -1) {
        obj.className = 'ButtonLink_focus';
      }
      isFirstTime = false;
    } else if (obj.tagName == 'SELECT') {
      if (navigator.appName.toUpperCase().indexOf('MICROSOFT') == -1) {
        if (obj.className.indexOf(' Combo_focus') == -1) {
          obj.className = obj.className.replace('Login_Combo', 'Login_Combo_focus');
          obj.className = obj.className + ' Combo_focus';
        }
      }
      isFirstTime = false;
    } else if (obj.tagName == 'INPUT') {
      if ((obj.className.indexOf(' TextBox_focus') == -1) &&
      (obj.className.indexOf('dojoValidateEmpty') != -1 ||
      obj.className.indexOf('dojoValidateValid') != -1 ||
      obj.className.indexOf('dojoValidateInvalid') != -1 ||
      obj.className.indexOf('dojoValidateRange') != -1)) {
        obj.className = obj.className.replace('dojoValidateEmpty', 'dojoValidateEmpty_focus');
        obj.className = obj.className.replace('dojoValidateValid', 'dojoValidateValid_focus');
        obj.className = obj.className.replace('dojoValidateInvalid', 'dojoValidateInvalid_focus');
        obj.className = obj.className.replace('dojoValidateRange', 'dojoValidateRange_focus');
        obj.className = obj.className.replace('required', 'required_focus');
        obj.className = obj.className.replace('readonly', 'readonly_focus');
        obj.className = obj.className.replace('Login_TextBox', 'Login_TextBox_focus');
        obj.className = obj.className + ' TextBox_focus';
      } else if (obj.getAttribute('type') == 'checkbox') {
        obj.className = 'Checkbox_Focused';
        var obj_tmp = obj;
        try {
          for (;;) {
            if (obj_tmp.getAttribute('class') == 'Checkbox_container_NOT_Focused') {
              obj_tmp.className = 'Checkbox_container_Focused';
              break;
            } else {
              obj_tmp = obj_tmp.parentNode;
            }
          }
        } catch(e) {}
          isFirstTime = false;
      } else if (obj.getAttribute('type') == 'radio') {
        obj.className='Radio_Focused';
        var obj_tmp = obj;
        try {
          for(;;) {
            if (obj_tmp.getAttribute('class')=='Radio_container_NOT_Focused') {
              obj_tmp.className='Radio_container_Focused';
              break;
            } else {
              obj_tmp = obj_tmp.parentNode;
            }
          }
        } catch(e) {}

      }
      isFirstTime = false;
    } else if (obj.tagName == 'TEXTAREA') {
      if ((obj.className.indexOf(' TextBox_focus') == -1)
      && (obj.className.indexOf('dojoValidateEmpty') != -1
      || obj.className.indexOf('dojoValidateValid') != -1
      || obj.className.indexOf('dojoValidateInvalid') != -1
      || obj.className.indexOf('dojoValidateRange') != -1)) {
        obj.className = obj.className.replace('dojoValidateValid', 'dojoValidateValid_focus');
        obj.className = obj.className.replace('required', 'required_focus');
        obj.className = obj.className + ' TextBox_focus';
      }
      isFirstTime = false;
    } else if (currentWindowElementType == 'grid') {
      isFirstTime = false;
    } else if (currentWindowElementType == 'genericTree') {
      isFirstTime = false;
    } else if (currentWindowElementType == 'custom') {  //Added for custom element support
      isFirstTime = false;
    } else {
    }
  } catch (e) {
  }
}

function putWindowElementFocus(obj, event) {
  isContextMenuOpened = false;
  previousWindowElementType=currentWindowElementType;
  drawWindowElementFocus(obj);
  defaultActionLogic(obj);
  try {
    if (currentWindowElementType == 'grid') {
      obj.focus();
      focusGrid();
    } else if (currentWindowElementType == 'genericTree') {
      obj.focus();
      focusGenericTree();
    } else if (currentWindowElementType == 'custom') {  //Added for custom element support
      if (obj.focusLogic) {
        obj.focusLogic('focus');
      }
    } else {
      if (obj.tagName.toLowerCase() == 'input' && obj.type.toLowerCase() == 'text' && event == "onmousedown" && navigator.userAgent.toUpperCase().indexOf("MSIE") == -1) {
      } else {
        obj.focus();
      }
    }
    if(selectInputTextOnTab && event!='onclick' && event!='onmousedown') obj.select();
  } catch (e) {
  }
}

function eraseWindowElementFocus(obj) {
  drawnWindowElement = null;
  try {
    if(obj.tagName == 'A') {
      obj.className = obj.className.replace(' Popup_Client_UserOps_LabelLink_focus','');
      obj.className = obj.className.replace(' Popup_Client_UserOps_LabelLink_Selected_focus','');
      obj.className = obj.className.replace(' Popup_Client_Help_LabelLink_focus','');
      obj.className = obj.className.replace(' DataGrid_Popup_text_pagerange_focus','');
      obj.className = obj.className.replace(' LabelLink_focus','');
      obj.className = obj.className.replace(' LabelLink_noicon_focus','');
      obj.className = obj.className.replace('FieldButtonLink_focus','FieldButtonLink');
      obj.className = obj.className.replace('ButtonLink_focus','ButtonLink'); //Used in old r2.40 button definition
      obj.className = obj.className.replace('List_Button_TopLink_focus','List_Button_TopLink');
      obj.className = obj.className.replace('List_Button_MiddleLink_focus','List_Button_MiddleLink');
      obj.className = obj.className.replace('List_Button_BottomLink_focus','List_Button_BottomLink');
      obj.className = obj.className.replace('Dimension_LeftRight_Button_TopLink_focus','Dimension_LeftRight_Button_TopLink');
      obj.className = obj.className.replace('Dimension_LeftRight_Button_BottomLink_focus','Dimension_LeftRight_Button_BottomLink');
      obj.className = obj.className.replace('Dimension_UpDown_Button_BottomLink_focus','Dimension_UpDown_Button_BottomLink');
      obj.className = obj.className.replace('Dimension_UpDown_Button_TopLink_focus','Dimension_UpDown_Button_TopLink');
      obj.className = obj.className.replace('Popup_Workflow_Button_focus','Popup_Workflow_Button');
      obj.className = obj.className.replace('Popup_Workflow_text_focus','Popup_Workflow_text');
      obj.className = obj.className.replace('Popup_Client_Help_Icon_LabelLink_focus','Popup_Client_Help_Icon_LabelLink');
      obj.className = obj.className.replace('MessageBox_TextLink_focus','MessageBox_TextLink');
    } else if (obj.tagName == 'BUTTON') {
      obj.className = obj.className.replace('ButtonLink_focus','ButtonLink');
    } else if (obj.tagName == 'SELECT') {
      obj.className = obj.className.replace(' Combo_focus','');
      obj.className = obj.className.replace('Login_Combo_focus','Login_Combo');
    } else if (obj.tagName == 'INPUT') {
      obj.className = obj.className.replace(' TextBox_focus','');
      obj.className = obj.className.replace('dojoValidateEmpty_focus', 'dojoValidateEmpty');
      obj.className = obj.className.replace('dojoValidateValid_focus', 'dojoValidateValid');
      obj.className = obj.className.replace('dojoValidateInvalid_focus', 'dojoValidateInvalid');
      obj.className = obj.className.replace('dojoValidateRange_focus', 'dojoValidateRange');
      obj.className = obj.className.replace('required_focus', 'required');
      obj.className = obj.className.replace('readonly_focus', 'readonly');
      obj.className = obj.className.replace('Login_TextBox_focus', 'Login_TextBox');
      if (obj.getAttribute('type')=='checkbox') {
        obj.className='Checkbox_NOT_Focused';
        var obj_tmp = obj;
        try {
          for(;;) {
            if (obj_tmp.getAttribute('class')=='Checkbox_container_Focused') {
              obj_tmp.className='Checkbox_container_NOT_Focused';
              break;
            } else {
              obj_tmp = obj_tmp.parentNode;
            }
          }
        } catch(e) {}
      } else if (obj.getAttribute('type')=='radio') {
        obj.className='Radio_NOT_Focused';
        var obj_tmp = obj;
        try {
          for(;;) {
            if (obj_tmp.getAttribute('class')=='Radio_container_Focused') {
              obj_tmp.className='Radio_container_NOT_Focused';
              break;
            } else {
              obj_tmp = obj_tmp.parentNode;
            }
          }
        } catch(e) {}
      }
    } else if (obj.tagName == 'TEXTAREA') {
      obj.className = obj.className.replace(' TextBox_focus','');
      obj.className = obj.className.replace('dojoValidateValid_focus', 'dojoValidateValid');
      obj.className = obj.className.replace('required_focus', 'required');
    } else if (previousWindowElementType == 'grid') {
      blurGrid();
    } else if (previousWindowElementType == 'genericTree') {
      blurGenericTree();
    } else if (previousWindowElementType == 'custom') {  //Added for custom element support
      if (obj.focusLogic) {
        obj.focusLogic('blur');
      }
    } else {
    }
  } catch (e) {
  }
}

function removeWindowElementFocus(obj) {
  isContextMenuOpened = false;
  eraseWindowElementFocus(obj);
  try {
    if (previousWindowElementType == 'grid') {
      blurGrid();
    } else if (previousWindowElementType == 'genericTree') {
      blurGenericTree();
    } else {
      //obj.blur();
    }
  } catch (e) {
  }
}

function mustBeJumped(obj) {
  if (obj.focusLogic) {  //Added for custom element support
    return obj.focusLogic('mustBeJumped');
  }
  if (obj.style.display == 'none') return true;
  if (obj.getAttribute('id') == 'genericTree') { return true; }
  return false;
}

function mustBeIgnored(obj) {
  if (obj.focusLogic) {  //Added for custom element support
    return obj.focusLogic('mustBeIgnored');
  }
  if (obj.style.display == 'none') return true;
  if (obj.getAttribute('type') == 'hidden') {
    return true;
  }
  if (obj.getAttribute('readonly') == 'true' && obj.getAttribute('tabindex') != '1') return true;
  if (obj.readOnly && obj.getAttribute('tabindex') != '1') return true;
  if (obj.getAttribute('disabled') == 'true') return true;
  if (obj.disabled) return true;
  if (obj.className.indexOf('LabelLink')!=-1 && obj.className.indexOf('_LabelLink')==-1 && obj.className.indexOf('LabelLink_')==-1) return true;
  if (obj.className.indexOf('FieldButtonLink')!=-1) return true;
  if (obj.className.indexOf('ButtonLink_disabled')!=-1) return true;
  return false;
}

function canHaveFocus(obj) {
  if (mustBeIgnored(obj)) return false;
  if (couldHaveFocus(obj)) return true;
  return false;
}

function couldHaveFocus(obj) {
  try {
    if (obj.tagName == 'INPUT' && obj.getAttribute('id') == 'grid_table_dummy_input') {
      currentWindowElementType = 'grid';
      return true;
    }
  } catch(e) {
  }
  try {
    if (obj.tagName == 'INPUT' && obj.getAttribute('id') == 'genericTree_dummy_input') {
      currentWindowElementType = 'genericTree';
      return true;
    }
  } catch(e) {
  }
  try {  //Added for custom element support
    if (obj.focusLogic) {
      currentWindowElementType = 'custom';
      return obj.focusLogic('couldHaveFocus');
    }
  } catch(e) {
  }
  if (obj.tagName == 'INPUT') {
    currentWindowElementType='input';
    return true;
  }
  if (obj.tagName == 'A') {
    currentWindowElementType='a';
    return true;
  }
  if (obj.tagName == 'BUTTON' && obj.className.indexOf('ButtonLink_disabled')==-1) {
    currentWindowElementType='button';
    return true;
  }
  if (obj.tagName == 'SELECT') {
    currentWindowElementType = 'select';
    return true;
  }
  if (obj.tagName == 'TEXTAREA') {
    currentWindowElementType = 'textarea';
    return true;
  }
  return false;
}

function getNextWindowElement() {
  if (isReadOnlyWindow) {
    try {
      setTimeout("setSelectedArea('window'); swichSelectedArea();",50);
      return true;
    } catch(e) {}
  }

  var success = null;
  var nextElementTmp = null;
  var nextElement = focusedWindowElement;
  if (nextElement==null) {
    nextElement = getFirstWindowElement();
    return nextElement;
  } else {
    for(;;){
      nextElementTmp = nextElement;
      try {
        nextElement = nextElement.firstChild;
        for (;;) {
          for (;;) {
            if (nextElement.nodeType != '1') {
              nextElement = nextElement.nextSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(nextElement)) {
            break;
          } else {
            nextElement = nextElement.nextSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        nextElement=nextElementTmp;
      }
      if(success) {
        if(canHaveFocus(nextElement)) return nextElement;
      } else {
        for(;;) {
          nextElementTmp = nextElement;
          try {
            nextElement = nextElement.nextSibling;
            for (;;) {
              for (;;) {
                if (nextElement.nodeType != '1') {
                  nextElement = nextElement.nextSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(nextElement)) break;
              else nextElement = nextElement.nextSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            nextElement=nextElementTmp;
          }
          if (success) {
            if(canHaveFocus(nextElement)) return nextElement;
            break;
          } else {
            nextElement = nextElement.parentNode
            if (nextElement==document.getElementById(windowTableParentElement) || nextElement==document.getElementsByTagName('BODY')[0]) {
              goToNextWindowTable();
              if (!isFirstTime) return getCurrentWindowTableFirstElement();
              else {
                try {
                  isReadOnlyWindow = true;
                  setTimeout("setSelectedArea('window'); swichSelectedArea();",50);
                } catch(e) {}
              }
            }
          }
        }
      }
    }
  }
}

function getPreviousWindowElement() {
  var success = null;
  var previousElementTmp = null;
  var previousElement = focusedWindowElement;
  if (previousElement==null) {
    previousElement = getLastWindowElement();
    return previousElement;
  } else {
    for(;;){
      previousElementTmp = previousElement;
      try {
        previousElement = previousElement.lastChild;
        for (;;) {
          for (;;) {
            if (previousElement.nodeType != '1') {
              previousElement = previousElement.previousSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(previousElement)) {
            break;
          } else {
            previousElement = previousElement.previousSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        previousElement=previousElementTmp;
      }
      if(success) {
        if(canHaveFocus(previousElement)) return previousElement;
      } else {
        for(;;) {
          previousElementTmp = previousElement;
          try {
            previousElement = previousElement.previousSibling;
            for (;;) {
              for (;;) {
                if (previousElement.nodeType != '1') {
                  previousElement = previousElement.previousSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(previousElement)) break;
              else previousElement = previousElement.previousSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            previousElement=previousElementTmp;
          }
          if (success) {
            if(canHaveFocus(previousElement)) return previousElement;
            break;
          } else {
            previousElement = previousElement.parentNode
            if (previousElement==document.getElementById(windowTableParentElement)) {
              goToPreviousWindowTable();
              return getCurrentWindowTableLastElement();
            }
          }
        }
      }
    }
  }
}

function goToNextWindowTable() {
  for(;;) {
    if (focusedWindowTable < windowTables.length-1) {
      focusedWindowTable = focusedWindowTable + 1;
    } else {
      focusedWindowTable = 0;
    }
    if (document.getElementById(windowTables[focusedWindowTable].tableId).style.display != 'none') {
      break;
    }
  }
  setWindowTableParentElement();
}

function goToPreviousWindowTable() {
  for(;;) {
    if (focusedWindowTable > 0) {
      focusedWindowTable = focusedWindowTable - 1;
    } else {
      focusedWindowTable = windowTables.length-1;
    }
    if (document.getElementById(windowTables[focusedWindowTable].tableId).style.display != 'none') {
      break;
    }
  }
  setWindowTableParentElement();

}

function getCurrentWindowTableFirstElement() {
  focusedWindowElement = document.getElementById(windowTables[focusedWindowTable].tableId);
  var obj = getNextWindowElement();
  return obj;
}

function getCurrentWindowTableLastElement() {
  focusedWindowElement = document.getElementById(windowTables[focusedWindowTable].tableId);
  var obj = getPreviousWindowElement();
  return obj;
}


function getFirstWindowElement() {
  focusedWindowElement = document.getElementById(windowTables[0].tableId);
  focusedWindowTable = 0;
  try {
    var obj = getNextWindowElement();
    return obj;
  }
  catch (e) {
    obj = null;
    focusedWindowElement = document.getElementsByTagName('BODY')[0];
  }
}

function getLastWindowElement() {
  focusedWindowElement = document.getElementById(windowTables[windowTables.length-1].tableId);
  focusedWindowTable = windowTables.length-1;
  var obj = getPreviousWindowElement();
  return obj;
}

function setOBTabBehavior(state) {
  if (state == true) {
    isOBTabBehavior = true;
  } else if (state == false) {
    isOBTabBehavior = false;
  }
  return true;
}

function windowTabKey(state) {
  if (isOBTabBehavior) {
    if (state==true) {
      if (isTabBlocked == false) {
        isTabPressed = true;
        isSelectedComboOpened = false;
        if (selectedArea == 'window') {
          var obj = getNextWindowElement();
          setWindowElementFocus(obj);
        } else if (selectedArea == 'tabs') {
          var obj = getNextTab();
          setTabFocus(obj);
        }
      }
    } else {
      isTabPressed = false;
    }
    return false;
  }
  return true;
}

function windowShiftTabKey(state) {
  if (isOBTabBehavior) {
    if (state==true) {
      if (isTabBlocked == false) {
        isTabPressed = true;
        isSelectedComboOpened = false;
        if (selectedArea == 'window') {
          var obj = getPreviousWindowElement();
          setWindowElementFocus(obj);
        } else if (selectedArea == 'tabs') {
          var obj = getPreviousTab();
          setTabFocus(obj);
        }
      }
    } else {
      isTabPressed = false;
    }
    return false;
  }
  return true;
}

function windowEnterKey() {
  if (isGridFocused) {
    propagateEnter = false;
    onRowDblClick();
  } else if (isGenericTreeFocused) {
    propagateEnter = false;
    gt_showNodeDescription(gt_focusedNode);
  } else if (defaultActionType == 'button') {
    propagateEnter = false;
    executeWindowButton(defaultActionElement.getAttribute('id'));
  } else if (defaultActionType == 'combo') {
    propagateEnter = true;
    isSelectedComboOpened=false;
    activateDefaultAction();
  } else {
    propagateEnter = true;
    return true;
  }
}

function windowCtrlShiftEnterKey() {
  executeAssociatedLink();
}

function windowCtrlEnterKey() {
  if (isGenericTreeFocused) {
    gt_goToNodeLink(gt_focusedNode);
  } else {
    executeAssociatedFieldButton();
  }
}

function setFirstWindowElementFocus() {
  var obj = getFirstWindowElement();
  setWindowElementFocus(obj);
}

function setLastWindowElementFocus() {
  var obj = getLastWindowElement();
  setWindowElementFocus(obj);
}

function setCurrentWindowTableFirstElementFocus() {
  var obj = getCurrentWindowTableFirstElement();
  setWindowElementFocus(obj);
}

function setCurrentWindowTableLastElementFocus() {
  var obj = getCurrentWindowTableLastElement();
  setWindowElementFocus(obj);
}

function getAssociatedLink() {
  var link = null;
  var success = true;
  link = focusedWindowElement;
  //Go to the TD parent
  if (link.className.indexOf('TextBox_btn') != -1) {
    //If is an Input Text + Field Button
    try {
      for (;;) {
        link = link.parentNode;
        if (link.tagName == 'TABLE') {
          break;
        } else if (link.tagName == 'BODY') {
          success = false;
          break;
        }
      }
      for (;;) {
        link = link.parentNode;
        if (link.tagName == 'TABLE') {
          break;
        } else if (link.tagName == 'BODY') {
          success = false;
          break;
        }
      }
      success = true;
    } catch (e) {
      success = false;
    }
  }
  if (success == true) {
    try {
      for (;;) {
        link = link.parentNode;
        if (link.tagName == 'TD') {
          success = true;
          break;
        } else if (link.tagName == 'BODY') {
          success = false;
          break;
        }
      }
    } catch (e) {
      success = false;
    }
  }
  if (success == true) {
    try {
      //Go to the TD previous sibling
      link = link.previousSibling;
      for (;;) {
        if (link.nodeType != '1') {
          link = link.previousSibling;
        } else {
          break;
        }
      }
      //Go to the SPAN element
      link = link.firstChild;
      for (;;) {
        if (link.nodeType != '1') {
          link = link.nextSibling;
        } else {
          break;
        }
      }
      //Go to the A element
      link = link.firstChild;
      for (;;) {
        if (link.nodeType != '1') {
          link = link.nextSibling;
        } else {
          break;
        }
      }
      success = true;
    } catch (e) {
      success = false;
    }
  }
  if (success == true && link.tagName == 'A') {
    return link;
  } else {
    return false;
  }
}

function executeAssociatedLink() {
  var link = null;
  link = getAssociatedLink();
  if (link != null && link != false) {
    link.onclick();
    return true;
  } else {
    return false;
  }
}

function getAssociatedFieldButton(input, type) {
  var fieldButton = null;
  var success = true;
  if (typeof input == 'undefined' || input == null) {
    fieldButton = focusedWindowElement;
  } else {
    fieldButton = input;
  }
  if (type=='window') {
    try {
      //Go to the TD parent
      for (;;) {
        fieldButton = fieldButton.parentNode;
        if (fieldButton.tagName == 'TD' && fieldButton.className.indexOf('TextBox_ContentCell') != -1) {
          break;
        } else if (fieldButton.tagName == 'BODY') {
          success = false;
          break;
        }
      }
      success = true;
    } catch (e) {
      success = false;
    }
  }
  if (success == true) {
    try {
      //Go to the TD next sibling
      fieldButton = fieldButton.nextSibling;
      for (;;) {
        if (fieldButton.nodeType != '1') {
          fieldButton = fieldButton.nextSibling;
        } else {
          break;
        }
      }
      //Go to the A element
      fieldButton = fieldButton.firstChild;
      for (;;) {
        if (fieldButton.nodeType != '1') {
          fieldButton = fieldButton.nextSibling;
        } else {
          break;
        }
      }
      success = true;
    } catch (e) {
      success = false;
    }
  }
  if (success == true && fieldButton.tagName == 'A') {
    return fieldButton;
  } else {
    return false;
  }
}

function executeAssociatedFieldButton() {
  var fieldButton = null;
  fieldButton = getAssociatedFieldButton(null, 'window');
  if (fieldButton != null && fieldButton != false) {
    fieldButton.onclick();
    return true;
  }
}


// Tabs functions

var focusedTab = null;
var focusedTab_tmp = null;
var tabTableParentElement = null;
var focusedTabTable = 0;

function tabTableId(tabTableId) {
  this.tabTableId = tabTableId;
}

function isTabActive(obj) {
  if (obj.className.indexOf('Tabcurrent')!=-1) {
    return true;
  } else {
    return false;
  }
}



function drawTabFocus(obj) {
  var obj_tmp = null;
  try {
    if(obj.tagName == 'A') {
      if (obj.className.indexOf('dojoTabLink_focus') == -1 && obj.className.indexOf('dojoTabLink') != -1) {
        obj_tmp = obj;
        obj = obj.parentNode;
        obj = obj.parentNode;
        obj = obj.parentNode;
        if (obj.className.indexOf('dojoTabparentfirst')!=-1) obj.className = 'dojoTabparentfirst_focus';
        else if (obj.className.indexOf('dojoTabparent')!=-1) obj.className = 'dojoTabparent_focus';
        else if (obj.className.indexOf('dojoTabcurrentfirst')!=-1) obj.className = 'dojoTabcurrentfirst_focus';
        else if (obj.className.indexOf('dojoTabcurrent')!=-1) obj.className = 'dojoTabcurrent_focus';
        else {
          obj = obj_tmp;
          obj.className = 'dojoTabLink_focus';
        }
      }
    } else {
    }
  } catch (e) {
  }
}

function putTabFocus(obj) {
  drawTabFocus(obj);
  defaultActionLogic(obj);
  obj.focus();
}

function eraseTabFocus(obj) {
  var obj_tmp = null;
  try {
    if(obj.tagName == 'A') {
      obj_tmp = obj;
      obj = obj.parentNode;
      obj = obj.parentNode;
      obj = obj.parentNode;
      if (obj.className.indexOf('dojoTabparentfirst_focus')!=-1) obj.className = 'dojoTabparentfirst';
      else if (obj.className.indexOf('dojoTabparent_focus')!=-1) obj.className = 'dojoTabparent';
      else if (obj.className.indexOf('dojoTabcurrentfirst_focus')!=-1) obj.className = 'dojoTabcurrentfirst';
      else if (obj.className.indexOf('dojoTabcurrent_focus')!=-1) obj.className = 'dojoTabcurrent';
      else {
        obj = obj_tmp;
        if (obj.className.indexOf('dojoTabLink_focus') != -1) obj.className = 'dojoTabLink';
      }
    }
  } catch (e) {
  }
}

function removeTabFocus(obj) {
  eraseTabFocus(obj);
  //obj.blur();
}



function getFirstTab() {
  focusedTab = document.getElementById(tabsTables[0].tabTableId);
  focusedTabTable = 0;
  var obj = getNextTab();
  return obj;
}

function getLastTab() {
  focusedTab = document.getElementById(tabsTables[tabsTables.length-1].tabTableId);
  focusedTabTable = tabsTables.length-1;
  var obj = getPreviousTab();
  return obj;
}

function getNextTab() {
  var success = null;
  var nextElementTmp = null;
  var nextElement = focusedTab;
  if (nextElement==null) {
    nextElement = getActiveTab();
    return nextElement;
  } else {
    for(;;){
      nextElementTmp = nextElement;
      try {
        nextElement = nextElement.firstChild;
        for (;;) {
          for (;;) {
            if (nextElement.nodeType != '1') {
              nextElement = nextElement.nextSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(nextElement)) {
            break;
          } else {
            nextElement = nextElement.nextSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        nextElement=nextElementTmp;
      }
      if(success) {
        if(canHaveFocus(nextElement)) return nextElement;
      } else {
        for(;;) {
          nextElementTmp = nextElement;
          try {
            nextElement = nextElement.nextSibling;
            for (;;) {
              for (;;) {
                if (nextElement.nodeType != '1') {
                  nextElement = nextElement.nextSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(nextElement)) break;
              else nextElement = nextElement.nextSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            nextElement=nextElementTmp;
          }
          if (success) {
            if(canHaveFocus(nextElement)) return nextElement;
            break;
          } else {
            nextElement = nextElement.parentNode
            if (nextElement==document.getElementById(tabTableParentElement) || nextElement==document.getElementsByTagName('BODY')[0]) {
              goToNextTabs();
              return getFirstTab();
            }
          }
        }
      }
    }
  }
}

function getPreviousTab() {
  var success = null;
  var previousElementTmp = null;
  var previousElement = focusedTab;
  if (previousElement==null) {
    previousElement = getActiveTab();
    return previousElement;
  } else {
    for(;;){
      previousElementTmp = previousElement;
      try {
        previousElement = previousElement.lastChild;
        for (;;) {
          for (;;) {
            if (previousElement.nodeType != '1') {
              previousElement = previousElement.previousSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(previousElement)) {
            break;
          } else {
            previousElement = previousElement.previousSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        previousElement=previousElementTmp;
      }
      if(success) {
        if(canHaveFocus(previousElement)) return previousElement;
      } else {
        for(;;) {
          previousElementTmp = previousElement;
          try {
            previousElement = previousElement.previousSibling;
            for (;;) {
              for (;;) {
                if (previousElement.nodeType != '1') {
                  previousElement = previousElement.previousSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(previousElement)) break;
              else previousElement = previousElement.previousSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            previousElement=previousElementTmp;
          }
          if (success) {
            if(canHaveFocus(previousElement)) return previousElement;
            break;
          } else {
            previousElement = previousElement.parentNode
            if (previousElement==document.getElementById(tabTableParentElement)) {
              goToPreviousTabs();
              return getLastTab();
            }
          }
        }
      }
    }
  }
}


function setTabFocus(obj, type) {
  if (type == null || type == 'null' || type == '' || type == 'obj') {
    if (obj == 'firstElement') {
      setFirstTabFocus();
    } else if (obj == 'lastElement') {
      setLastTabFocus();
    } else {
      focusedTab = obj;
      removeTabFocus(focusedTab_tmp);
      focusedTab_tmp = focusedTab;
      if(!frameLocked)
      putTabFocus(focusedTab);
    }
  } else if (type == 'id') {
    obj = document.getElementById(obj);
    focusedTab = obj;
    removeTabFocus(focusedTab_tmp);
    focusedTab_tmp = focusedTab;
    if(!frameLocked) putWindowElementFocus(obj);
    putTabFocus(focusedTab);
  }
}

function setFirstTabFocus() {
  var obj = getFirstTab();
  setTabFocus(obj);
}

function setLastTabFocus() {
  var obj = getLastTab();
  setTabFocus(obj);
}

function goToNextTabs() {
  if (focusedTabTable < tabsTables.length-1) {
    focusedTabTable = focusedTabTable + 1;
  } else {
    focusedTabTable = 0;
  }
  setTabTableParentElement();
}

function goToPreviousTabs() {
  if (focusedTabTable > 0) {
    focusedTabTable = focusedTabTable - 1;
  } else {
    focusedTabTable = tabsTables.length-1;
  }
  setTabTableParentElement();
}

function setActiveTab() {
  var obj = getActiveTab();
  setTabFocus(obj);
}

function setTabTableParentElement() {
  tabTableParentElement = tabsTables[focusedTabTable].tabTableId;
}

function getActiveTab() {
  var obj = getActiveTabContainer();
  var focusedTab_tmp = focusedTab;
  focusedTab = obj;
  obj = getNextTab();
  focusedTab = focusedTab_tmp;
  return obj;
}

function getActiveTabContainer() {
  var success = null;
  var nextElementTmp = null;
  var nextElement = document.getElementById(tabsTables[0].tabTableId);
  if (nextElement==null) {
    return false;
  } else {
    for(;;){
      nextElementTmp = nextElement;
      try {
        nextElement = nextElement.firstChild;
        for (;;) {
          for (;;) {
            if (nextElement.nodeType != '1') {
              nextElement = nextElement.nextSibling;
            } else {
              break;
            }
          }
          if (!mustBeJumped(nextElement)) {
            break;
          } else {
            nextElement = nextElement.nextSibling;
          }
        }
        success=true;
      } catch (e) {
        success=false;
        nextElement=nextElementTmp;
      }
      if(success) {
        if(isTabActive(nextElement)) return nextElement;
      } else {
        for(;;) {
          nextElementTmp = nextElement;
          try {
            nextElement = nextElement.nextSibling;
            for (;;) {
              for (;;) {
                if (nextElement.nodeType != '1') {
                  nextElement = nextElement.nextSibling;
                } else {
                  break;
                }
              }
              if (!mustBeJumped(nextElement)) break;
              else nextElement = nextElement.nextSibling;
            }
            success=true;
          } catch (e) {
            success=false;
            nextElement=nextElementTmp;
          }
          if (success) {
            if(isTabActive(nextElement)) return nextElement;
            break;
          } else {
            nextElement = nextElement.parentNode
            if (nextElement==document.getElementById(windowTableParentElement) || nextElement==document.getElementsByTagName('BODY')[0]) {
              goToNextTabs();
              return false;
            }
          }
        }
      }
    }
  }
}

function focusGrid() {
  try {
    dijit.byId('grid').focusGrid();
  } catch(e){}
}

function blurGrid() {
  try {
    dijit.byId('grid').blurGrid();
  } catch(e) {}
}

function focusGenericTree() {
  try {
    gt_focusTreeContainer('genericTree','id');
  } catch(e) {}
}

function blurGenericTree() {
  try {
    if (typeof gt_focusedNode != "undefined") { gt_blurTreeContainer('genericTree','id'); }
  } catch(e) {}
}

function windowUpKey() {
  if (isGridFocused) {
    dijit.byId('grid').goToPreviousRow();
  } else if (isGenericTreeFocused) {
    gt_goToPreviousNode();
  }
}

function windowDownKey() {
  if (isGridFocused) {
    dijit.byId('grid').goToNextRow();
  } else if (isGenericTreeFocused) {
    gt_goToNextNode();
  }
}

function windowLeftKey() {
  if (isGridFocused) {
  } else if (isGenericTreeFocused) {
    if (gt_isClosedNode(gt_focusedNode)) {
      gt_goToParentNode();
    } else {
      gt_closeNode(gt_focusedNode);
    }
  }
}

function windowRightKey() {
  if (isGridFocused) {
  } else if (isGenericTreeFocused) {
    if (gt_isClosedNode(gt_focusedNode)) {
      gt_openNode(gt_focusedNode);
    } else {
      gt_goToNextNode();
    }
  }
}

function windowSpaceKey() {
  if (isGridFocused) {
  } else if (isGenericTreeFocused) {
    gt_checkToggleNode(gt_focusedNode);
  }
}

function windowHomeKey() {
  if (isGridFocused) {
    dijit.byId('grid').goToFirstRow();
  }
}

function windowEndKey() {
  if (isGridFocused) {
    dijit.byId('grid').goToLastRow();
  }
}

function windowAvpageKey() {
  if (isGridFocused) {
    dijit.byId('grid').goToNextPage();
  }
}

function windowRepageKey() {
  if (isGridFocused) {
    dijit.byId('grid').goToPreviousPage();
  }
}
