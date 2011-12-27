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
* @fileoverview Contains methods for asynchronous ajax calls. (eg. used by some
* HTML reports to display/hide a subreport)
*/

var xmlreq = false; //Deprecated in 2.50
var paramXMLRequest = null; //Deprecated in 2.50

function getXMLHttpRequest() {
  // Create XMLHttpRequest object in non-Microsoft browsers
  var XMLHttpRequestObj = null;

  try {
    XMLHttpRequestObj = new XMLHttpRequest();
  } catch (e) {
    XMLHttpRequestObj = false;
  }

  if (window.ActiveXObject) {
    try {
      // Try to create XMLHttpRequest in later versions
      // of Internet Explorer
      XMLHttpRequestObj = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e1) {
      // Failed to create required ActiveXObject
      try {
        // Try version supported by older versions
        // of Internet Explorer
        XMLHttpRequestObj = new ActiveXObject("Microsoft.XMLHTTP");
      } catch (e2) {
        // Unable to create an XMLHttpRequest by any means
        XMLHttpRequestObj = false;
      }
    }
  }
  return XMLHttpRequestObj;
}

function getReadyStateHandler(req, responseXmlHandler, notifyError) {
  if (req === null) {
    return false;
  }
  if (notifyError === null || typeof notifyError === 'undefined') {
    notifyError = true;
  }
  // If the request's status is "complete"
  if (req.readyState == 4) {
    // Check that we received a successful response from the server
    if (req.status == 200) {
      // Pass the XML payload of the response to the handler function.
      //responseXmlHandler(req.responseXML);
      return true;
    } else {
      // An HTTP problem has occurred
      if (notifyError) {
        alert("HTTP error "+req.status+": "+req.statusText);
	  }
      return false;
    }
    return false;
  }
  return false;
}

function submitXmlHttpRequestUrl(callbackFunction, url, debug, paramXMLReq) {
  var XMLHttpRequestObj = null;
  XMLHttpRequestObj = getXMLHttpRequest();
  if (!paramXMLReq) {
    paramXMLReq = paramXMLRequest; //Deprecated in 2.50
  }
  if (!XMLHttpRequestObj) {
    XMLHttpRequestObj = getXMLHttpRequest();
  }
  if (debug === null) {
    debug = false;
  }
  if (!XMLHttpRequestObj) {
    alert("Your browser doesn't support this technology");
    return false;
  }
  if (debug) {
    if (!debugXmlHttpRequest(url)) {
	  return false;
	}
  }
  XMLHttpRequestObj.open("GET", url);
  var paramXMLParticular = paramXMLReq;
  xmlreq = XMLHttpRequestObj; //Deprecated in 2.50
  XMLHttpRequestObj.onreadystatechange = function () {
    return callbackFunction(paramXMLParticular, XMLHttpRequestObj);
  };
  xmlreq = null; //Deprecated in 2.50
  paramXMLRequest = null; //Deprecated in 2.50
  XMLHttpRequestObj.send(null);

  return true;
}

// extraParams is added unencoded to the POST-data, so caller has to do encoding if needed
function submitXmlHttpRequestWithParams(callbackFunction, formObject, Command, Action, debug, extraParams, paramXMLReq) {
  var XMLHttpRequestObj = null;
  XMLHttpRequestObj = getXMLHttpRequest();
  // if (!xmlreq) xmlreq = getXMLHttpRequest(); //Deprecated in 2.50
  if (!paramXMLReq) {
    paramXMLReq = paramXMLRequest; //Deprecated in 2.50
  }
  if (formObject === null) {
    formObject = document.forms[0];
  }
  if (debug === null) {
    debug = false;
  }
  if (Action === null) {
    Action = formObject.action;
  }
  if (!XMLHttpRequestObj) {
    alert("Your browser doesn't support this technology");
    return false;
  }
  var sendText = "Command=" + encodeURIComponent(Command);
  sendText += "&IsAjaxCall=1";
  var length = formObject.elements.length;
  for (var i=0;i<length;i++) {
    if (formObject.elements[i].type) {
      var text = inputValueForms(formObject.elements[i].name, formObject.elements[i]);
      if (text && text.indexOf('=') !== 0) {
        sendText += "&" + text;
      }
    }
  }
  if (extraParams !== null && extraParams !== "" && extraParams !== "null") {
    sendText += extraParams;
  }

  if (debug) {
    if (!debugXmlHttpRequest(Command)) {
	  return false;
	}
  }
  //XMLHttpRequestObj.open("GET", Action + "?" + sendText);
  XMLHttpRequestObj.open("POST", Action);
  try {
    XMLHttpRequestObj.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
  } catch (e) {}
  var paramXMLParticular = paramXMLReq;
  xmlreq = XMLHttpRequestObj; //Deprecated in 2.50
  XMLHttpRequestObj.onreadystatechange = function () {
    return callbackFunction(paramXMLParticular, XMLHttpRequestObj);
  };
  xmlreq = null; //Deprecated in 2.50
  //XMLHttpRequestObj.send(null);
  paramXMLRequest = null; //Deprecated in 2.50
  XMLHttpRequestObj.send(sendText);

  return true;
}

function submitXmlHttpRequest(callbackFunction, formObject, Command, Action, debug, extraParams, paramXMLReq) {
  extraParams = null; //if you want to use with params use directly submitXmlHttpRequestWithParams instead
  submitXmlHttpRequestWithParams(callbackFunction, formObject, Command, Action, debug, extraParams, paramXMLReq);
}

function lockField(inputField) {
  if (inputField === null) {
    return false;
  }
  if (!inputField.type) {
    return false;
  }
  inputField.disabled=true;
  return true;
}

function unlockField(inputField) {
  if (inputField === null) {
    return false;
  }
  if (!inputField.type) {
    return false;
  }
  inputField.disabled=false;
  return true;
}
