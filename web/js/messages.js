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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview Main functions for displaying preloaded critical messages in
* corresponding languages (save changes, number out of range, invalid value, 
* etc.). This library works hand in hand with the DynamicJS.java (mapped 
* to utility/DynamicJS.js) servlet found in the erpCommon/utility folder.
*/

//Valores por defecto
//var defaultLang = "en_US";
var gDefaultType = 0;

/*
 * The menu frame contains a call to MessagesJS and thus contains the
 * arrMessages and arrTypes information. Use this to avoid the need for an extra
 * http call to MessagesJS in most places, by getting the two arrays from the
 * menu if they are not found in the local page
 */

if (typeof arrMessages === 'undefined') {
  var arrMessages;
  var arrTypes;

  if(typeof getFrame === 'function' && getFrame('frameMenu')) {
    arrMessages = getFrame('frameMenu').arrMessages;
    arrTypes = getFrame('frameMenu').arrTypes;
  } else {
    if (parent && parent.frameMenu) {
      arrMessages = parent.frameMenu.arrMessages;
      arrTypes = parent.frameMenu.arrTypes;
    } else if (top && top.opener && top.opener.parent && top.opener.parent.frameMenu) {
      arrMessages = top.opener.parent.frameMenu.arrMessages;
      arrTypes = top.opener.parent.frameMenu.arrTypes;
    } else if (top && top.opener && top.opener.top && top.opener.top.opener && top.opener.top.opener.parent && top.opener.top.opener.parent.frameMenu) {
      arrMessages = top.opener.top.opener.parent.frameMenu.arrMessages;
      arrTypes = top.opener.top.opener.parent.frameMenu.arrTypes;
    }
  }
}

function messageType(_messageID, _messageType) {
	this.id = _messageID;
	this.type = _messageType;
}

function messagesTexts(_language, _message, _text, _defaultText) {
	this.language = _language;
	this.message = _message;
	this.text = _text;
	this.defaultText = _defaultText;
}

function getMessage(index, _language) {
	if (_language==null) {
		if (typeof defaultLang != "undefined") {
			_language = defaultLang;
		} else if (typeof LNG_POR_DEFECTO != "undefined") {
			// Deprecated in 2.50, the following code is only for compatibility
			_language = LNG_POR_DEFECTO;
		}
	}
  var total = (arrMessages ? arrMessages.length : 0);
	for (var i=0;i<total;i++) {
		if (arrMessages[i].language == _language)
			if (arrMessages[i].message == index)
				return (arrMessages[i].text);
	}
	return null;
}

function getDefaultText(index, _language) {
  if (_language==null) {
    if (typeof defaultLang != "undefined") {
      _language = defaultLang;
    } else if (typeof LNG_POR_DEFECTO != "undefined") {
      // Deprecated in 2.50, the following code is only for compatibility
      _language = LNG_POR_DEFECTO;
    }
  }
  var total = (arrMessages ? arrMessages.length : 0);
	for (var i=0;i<total;i++) {
		if (arrMessages[i].language == _language)
			if (arrMessages[i].message == index)
				return (arrMessages[i].defaultText);
	}
	return null;
}

function getType(index) {
  var total = arrTypes.length;
	for (var i=0;i<total;i++) {
		if (arrTypes[i].id == index)
			return (arrTypes[i].type);
	}
	return null;
}

/*	Los tipos de mensajes son:
		0.- Alert -> muestra una ventana de mensaje normal con un botón aceptar
		1.- Confirm -> muestra una ventana de confirmación que tiene 2 botones (OK y CANCEL)
		2.- Prompt -> muestra una ventana de petición de un parámetro con 2 botones (OK y CANCEL)
*/
function showStdMessage(_text, _type, _defaultValue) {
	switch (_type) {
	case 1:return confirm(_text);
			 break;
	case 2:return prompt(_text, _defaultValue);
			 break;
	default: alert(_text);
	}
	return true;
}

// Deprecated in 2.50, use showStdMessage instead
function showMessage(_text, _type, _defaultValue) {
  return showStdMessage(_text, _type, _defaultValue);
}

// Deprecated in 2.50, use showJSMessage instead
function mensaje(index, _language)
{
	showJSMessage(index, _language);
}

function showJSMessage(index, _language, clean)
{
  var clearMsgBox = typeof clean == 'undefined' || clean == null ? true : clean;
  if(clearMsgBox) {
	  try {
	    initialize_MessageBox('messageBoxID');
	  } catch (ignored) {}
  }
	var strMessage = getMessage(index, _language);
  if (strMessage == null)  strMessage = getMessage(index, "en_US");
	if (strMessage == null) {
    getDataBaseMessage(index);
    return true;
  }
	var strDefault = getDefaultText(index, _language);
	if (strDefault == null) { 
		strDefault = getDefaultText(index, 'en_US');
	}
	var type = getType(index, _language);
	if (type==null) type=gDefaultType;
	return showStdMessage(strMessage, type, strDefault);
}


function renderMessageBox(type, title, text) {
  try {
    dojo.widget.byId('messageBoxID').setValues(type, title, text);
  } catch (err) {
    alert(title + ":\n" + text);
  }
  return true;
}

function getUrl() {
  var url = window.location.href;
  var pos = url.indexOf("://");
  var pos2 = url.indexOf("/", pos+3);
  if (pos2!=-1) {
    pos2 = url.indexOf("/", pos2+1);
    if (pos2!=-1) url = url.substring(0, pos2);
  }
  return url;
}

function getDataBaseMessage(value, responseFunction) {
  var appUrl = getAppUrl();
  var paramXMLReq = null;
  var msgCode;
  if (new RegExp("^[0-9]+$").test(value)) {
    msgCode = "JS" + value;
  } else {
    msgCode = value;
  }
  submitXmlHttpRequestUrl(((responseFunction==null)?messageResponse:responseFunction), (appUrl + "/businessUtility/MessageJS.html?inpvalue=" + escape(msgCode)), false, paramXMLReq)
}

function getDataBaseStandardMessage(value, responseFunction) {
  var appUrl = getAppUrl();
  var paramXMLReq = null;
  submitXmlHttpRequestUrl(((responseFunction==null)?messageResponse:responseFunction), (appUrl + "/businessUtility/MessageJS.html?inpvalue=" + escape(value)), false, paramXMLReq)
}

function messageResponse(paramArray, XMLHttpRequestObj) {
   var obj;
   if (getReadyStateHandler(XMLHttpRequestObj)) {
    try {
      if (XMLHttpRequestObj.responseXML) obj = XMLHttpRequestObj.responseXML.documentElement;
    } catch (e) {
    }
    /*if (paramArray!=null && paramArray.length>0) {
      field = paramArray[0];
      try {
        var obj = document.getElementById(field);
        setFocus(obj);
      } catch (ignore) {}
    }*/
    if (obj) {
      var status = obj.getElementsByTagName('status');
      if (status.length>0) {
        var type = status[0].getElementsByTagName('type');
        var title = status[0].getElementsByTagName('title');
        var description = status[0].getElementsByTagName('description');
        try {
          setValues_MessageBox('messageBoxID',type[0].firstChild.nodeValue.toUpperCase(), title[0].firstChild.nodeValue, description[0].firstChild.nodeValue);
        } catch (err) {
          alert(title[0].firstChild.nodeValue + ":\n" + description[0].firstChild.nodeValue);
        }
      }
    }
  }
  return true;
}

/**
 * sets the translated messages in the validation span for missing and invalid value validation
 */
function setValidationMessages() {
  var missing = getMessage("Missing");
  var missingElements = getElementsByName("missingText", "div");
  for (i = 0; i < missingElements.length; i++) {
    missingElements[i].innerHTML = missing;
  }
  
  var invalid = getMessage("Invalid");
  var invalidElements = getElementsByName("invalidText", "div");
  for (i = 0; i < invalidElements.length; i++) {
    invalidElements[i].innerHTML = invalid;
  }
}
