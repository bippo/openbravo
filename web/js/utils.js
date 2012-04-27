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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview Contains core Javascript functions used on all pages to set focus,
*  clear forms, pop up confirmation messages, submit the form, etc.
*/

/**
 * Code that will be executed once the file is parsed
*/
function utilsJSDirectExecution() {
  isWindowInMDIPopup = checkWindowInMDIPopup();
  isWindowInMDITab = checkWindowInMDITab();
  isWindowInMDIPage = checkWindowInMDIPage();
  isWindowInMDIContext = checkWindowInMDIContext();
  if (isWindowInMDIPage) {
    adaptSkinToMDIEnvironment();
  }
}

var isWindowInMDIPopup = false;
var isWindowInMDITab = false;
var isWindowInMDIPage = false;
var isWindowInMDIContext = false;
var isMDIEnvironmentSet = false;
var MDIPopupId = null;

var baseFrameServlet = "../security/Login_FS.html";
var gColorSelected = "#c0c0c0";
var gWhiteColor = "#F2EEEE";
var arrGeneralChange=[];
var dateFormat;
var defaultDateFormat = "%d-%m-%Y";

var mainFrame_windowObj = "";
var LayoutMDI_windowObj = "";

//Days of a Month
var daysOfMonth = [[0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31],  //No leap year
                   [0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]]; //Leap year

var gByDefaultAction;
var gSubmitted=false;
var keyArray=null;

var gWaitingCallOut=false;

var isKeyboardLocked=false;

var isPopupLoadingWindowLoaded=false;

var isCtrlPressed = null;
var isAltPressed = null;
var isShiftPressed = null;
var isTabBlocked = false;
var pressedKeyCode = null;
var isInputFile = false;
var ExternalKeyDownFunction;
var ExternalKeyUpFunction;

var isPageLoading = true;
var isUserChanges = false;
var isTabClick = false;
var isButtonClick = false;
var calloutProcessedObj = null;

var debugMode = false; // Flag to output debug messages in Firebug

/**
 * Checks if Firebug's console is available and you are in debug mode
 * @return Boolean
 */
function isDebugEnabled() {
  return debugMode && typeof window.console === 'object';
}

/**
* Return a number that would be checked at the Login screen to know if the file is cached with the correct version
*/
function getCurrentRevision() {
  var number = '16049';
  return number;
}


/**
* Checks if the version is the correct one
*/
function revisionControl(number) {
  var current = getCurrentRevision();
  if (current != number) {
    return false;
  } else {
    return true;
  }
}

/**
* Gets information of the browser
* @param {name} string Required - It could be "name", "version", "nameAndVersion" or "complete"
*/
function getBrowserInfo(param) {
  var navUserAgent = navigator.userAgent.toUpperCase();
  var browserName = "Unknown";
  var browserVersion = "";
  var browserMajorVersion = "";
  var i=0
  if (navUserAgent.indexOf("MSIE") >= 0) {
    browserName = "Microsoft Internet Explorer";
    i=navUserAgent.indexOf("MSIE")+5;
  } else if (navUserAgent.indexOf("FIREFOX") >= 0) {
    browserName = "Mozilla Firefox";
    i=navUserAgent.indexOf("FIREFOX")+8;
  } else if (navUserAgent.indexOf("ICEWEASEL") >= 0) {
    browserName = "IceWeasel";
    i=navUserAgent.indexOf("ICEWEASEL")+10;
  } else if (navUserAgent.indexOf("CHROME") >= 0) {
    browserName = "Google Chrome";
    i=navUserAgent.indexOf("CHROME")+7;
  } else if (navUserAgent.indexOf("OPERA") >= 0) {
    browserName = "Opera";
    if (navUserAgent.indexOf("VERSION") != -1) {
      i=navUserAgent.indexOf("VERSION")+8;
    } else {
      i=navUserAgent.indexOf("OPERA")+6;
    }
  } else if (navUserAgent.indexOf("SAFARI") >= 0) {
    browserName = "Apple Safari";
    if (navUserAgent.indexOf("VERSION") != -1) {
      i=navUserAgent.indexOf("VERSION")+8;
    } else {
      i=navUserAgent.indexOf("SAFARI")+7;
    }
  } else if (navUserAgent.indexOf("NETSCAPE") >= 0) {
    browserName = "Netscape";
    i=navUserAgent.indexOf("NETSCAPE")+9;
  } else if (navUserAgent.indexOf("KONQUEROR") >= 0) {
    browserName = "Konqueror";
    i=navUserAgent.indexOf("KONQUEROR")+10;
  }
  if (i!=0) {
    while (navUserAgent.substring(i, i+1) != " " && navUserAgent.substring(i, i+1) != ";" && i < navUserAgent.length) {
      browserVersion += navUserAgent.substring(i, i+1);
      i++;
    }
  }
  var browserNameAndVersion = browserName + " " + browserVersion;
  browserMajorVersion = browserVersion;
  if (browserMajorVersion.indexOf(".") != -1) {
    browserMajorVersion = browserMajorVersion.substring(0, browserVersion.indexOf("."));
    browserMajorVersion = parseInt(browserMajorVersion);
  }
  if (param == "name") {
    return browserName;
  } else if (param == "version") {
    return browserVersion;
  } else if (param == "majorVersion") {
    return browserMajorVersion;
  } else if (param == "nameAndVersion" || typeof param == "undefined" || param == "" || param == null) {
    return browserNameAndVersion;
  } else {
    return false;
  }
}

/**
* Checks if the browser is a supported one. Just for 2.50
*/
function checkBrowserCompatibility250() {
   var browserName = getBrowserInfo("name");
   var browserVersion = getBrowserInfo("version");
   var browserMajorVersion = getBrowserInfo("majorVersion");
   var isValid = false;
   if (browserName.toUpperCase().indexOf('FIREFOX') != -1 || browserName.toUpperCase().indexOf('ICEWEASEL') != -1) {
     if (browserMajorVersion >= 3) {
       isValid = true;
     }
   } else if (browserName.toUpperCase().indexOf('INTERNET EXPLORER') != -1) {
     if (browserMajorVersion >= 7) {
       isValid = true;
     }
   }
   return isValid;
}

function getObjAttribute(obj, attribute) {
  attribute = attribute.toLowerCase();
  var attribute_text = "";
  if (navigator.userAgent.toUpperCase().indexOf("MSIE") == -1) {
    attribute_text = obj.getAttribute(attribute);
  } else {
    attribute_text = obj.getAttribute(attribute).toString();
    attribute_text = attribute_text.replace("function anonymous()","");
    attribute_text = attribute_text.replace("function " + attribute + "()","");
    attribute_text = attribute_text.replace("{\n","");
    attribute_text = attribute_text.replace("\n","");
    attribute_text = attribute_text.replace("}","");
  }
  attribute_text = attribute_text.replace(/^(\s|\&nbsp;)*|(\s|\&nbsp;)*$/g,"");
  return attribute_text;
}

function setObjAttribute(obj, attribute, attribute_text) {
  attribute = attribute.toLowerCase();
  attribute_text = attribute_text.toString();
  attribute_text = attribute_text.replace(/^(\s|\&nbsp;)*|(\s|\&nbsp;)*$/g,"");
  if (navigator.userAgent.toUpperCase().indexOf("MSIE") == -1) {
    obj.setAttribute(attribute, attribute_text);
  } else {
    obj[attribute]=new Function(attribute_text);
  }
}

/**
* Get the array of elements with a given name and tag. Its purpose is to supply the lack of document.getElementsByName support in IE
* @param {name} string Required - The desired name to search
* @param {tag} string Required - The tag of the desired name array
*/
function getElementsByName(name, tag) {
  var resultArray = [];
  if (!tag || tag == "" || tag == null || typeof tag == "undefined") {
    if (navigator.userAgent.toUpperCase().indexOf("MSIE") != -1) {
      var inputs = document.all;
      for (var i=0; i<inputs.length; i++){
        if (inputs.item(i).getAttribute('name') == name){
          resultArray.push(inputs.item(i));
        }
      }
    } else {
      resultArray = document.getElementsByName(name);
    }
  } else {
    tag = tag.toLowerCase()
    var inputs = document.getElementsByTagName(tag);
    for (var i=0; i<inputs.length; i++){
      if (inputs.item(i).getAttribute('name') == name){
        resultArray.push(inputs.item(i));
      }
    }
  }
  return resultArray;
}


/**
* Get the array of elements with a given name and tag. Its purpose is to supply the lack of document.getElementsByClassName support in IE
* @param {className} string Required - The desired className to search
* @param {tag} string Required - The tag of the desired className array
*/
function getElementsByClassName(className, tag) {
  var resultArray = [], classAttributeName;
  if (navigator.userAgent.toUpperCase().indexOf('MSIE') !== -1) {
    classAttributeName = 'className';
  } else {
    classAttributeName = 'class';
  }
  if (!tag) {
    if (navigator.userAgent.toUpperCase().indexOf('MSIE') !== -1) {
      var inputs = document.all;
      for (var i=0; i<inputs.length; i++) {
        if (inputs.item(i).getAttribute(classAttributeName) === className){
          resultArray.push(inputs.item(i));
        }
      }
    } else {
      resultArray = document.getElementsByClassName(className);
    }
  } else {
    tag = tag.toLowerCase()
    var inputs = document.getElementsByTagName(tag);
    for (var i=0; i<inputs.length; i++) {
      if (inputs.item(i).getAttribute(classAttributeName) === className){
        resultArray.push(inputs.item(i));
      }
    }
  }
  return resultArray;
}


/**
* Set the focus on the first visible control in the form
* @param {Form} form Optional- Defines the form containing the field, where we want to set the focus. If is not present, the first form of the page will be used.
* @param {String} field Optional - Name of the control where we want to set the focus. If is not present the first field will be used.
*/
function setFocusFirstControl(form, field) {
  var found = false;
  if (form==null) form=document.forms[0];
  var total = form.length;
  for(var i=0;i<total; i++)
  {
    if ((form.elements[i].type != "hidden") && (form.elements[i].type != "button") && (form.elements[i].type != "submit") && (form.elements[i].type != "image") && (form.elements[i].type != "reset")) 
    { 
      if(field!=null) {
        if (field == form.elements[i].name && !form.elements[i].readonly && !form.elements[i].disabled) {
          form.elements[i].focus();
          found=true;
          break;
        }
      } else if (!form.elements[i].readonly && !form.elements[i].disabled) {
        try {
          form.elements[i].focus();
          found=true;
          break;
        } catch (ignore) {}
      }
    }
  }
  if (found && form.elements[i].type && form.elements[i].type.indexOf("select")==-1)
    form.elements[i].select();
}

/** 
* Clean the content of all text fields in a form
* @param {Form} form Optional - Form where the fields that we want to clean, are contained. If not exist, the first form in the page will be used.
*/
function clearForm(form) {
  if (form == null)
    form = document.forms[0];

  var total = form.length;
  for (var i=0;i<total;i++){
    if (form.elements[i].type == "text" || form.elements[i].type == "password")
      form.elements[i].value = "";
  }
}

/**
* Display a message depending on the action parameter. Is used by other functions to submit forms.
* @param {String} action Text that identify the Command to execute
* @returns True in case of not having a message associated to the Command or by the user confirmation. False if the user cancel the confirmation message.
* @type Boolean
*/
function confirmAction(action) {
  switch (action)
  {
  case 'DELETE': return showJSMessage(2);
  case 'DELETE_RELATION': return showJSMessage(2);
  case 'GUARDAR': return showJSMessage(3);
  default: return true;
  }
}

/**
* Submit the first form in the page with GET parameters method 
* @param {String} Command The command String to execute
* @param {String} action The URL to receive the form
* @returns Always return true
* @type Boolean
*/
function submitFormGetParams(Command, action) {
  var frm = document.forms[0];
  frm.action=action + "?Command=" + Command;
  var params="";
  for (var i=2;arguments[i]!=null;i++) {
    params += "&" + arguments[i] + ((arguments[i+1]!=null)?("=" + arguments[i+1]):"");
    i++;
  }
  if (params!="") frm.action += params;
  frm.target="_self";

  removeOnUnloadHandler(frm); // Prevents opener reload
  frm.submit();
  return true;
}

/** 
* Receive a form with filled fields and transform it into a String of GET paramaters
* @param {Form} form Form that we want to transform.
* @returns The transformed string for GET method submition.
* @type String
*/
function getParamsScript(form) {
  if (form==null) return "";
  var script="";
  var total = form.length;
  for (var i=0;i<total;i++) {
    if (form.elements[i].type && (form.elements[i].type != "button") && (form.elements[i].type != "submit") && (form.elements[i].type != "image") && (form.elements[i].type != "reset") && (form.elements[i].readonly!="true") && (form.elements[i].name != "Command") && (form.elements[i].name!="") && !form.elements[i].disabled) {
      if (form.elements[i].type.toUpperCase().indexOf("SELECT")!=-1 && form.elements[i].selectedIndex!=-1) {
        script += ((script=="")?"":"&") + form.elements[i].name + "=" + escape(form.elements[i].options[form.elements[i].selectedIndex].value);
      } else if (form.elements[i].type.toUpperCase().indexOf("CHECKBOX")!=-1 || form.elements[i].type.toUpperCase().indexOf("RADIO")!=-1) {
        if (radioValue(form.elements[i]) != null) script += ((script=="")?"":"&") + form.elements[i].name + "=" + escape(radioValue(form.elements[i]));
      } else if (form.elements[i].value!=null && form.elements[i].value!="") {
        script += ((script=="")?"":"&") + form.elements[i].name + "=" + escape(form.elements[i].value);
      }
    }
  }
  return script;
}


/**
* Submit a form after setting a value to a field and control a single form submission
* @param {Object} field Reference to the field in the form
* @param {String} value Value to set in the field
* @param {Form} form Reference to the form, to submit
* @param {Boolean} bolOneFormSubmission To control if we want to validate only one form submission
* @param {Boolean} isCallout Verify if we will wait for a CallOut response
* @param {String} frameName Name of the frame that makes the commit needed for callouts 
* @returns True if the form is sent correctly, false if an error occours and is not possible to send the data.
* @type Boolean
*/
function submitForm(field, value, form, bolOneFormSubmission, isCallOut, frameName) {
  if (form == null) form = document.forms[0];
  if (isCallOut==null) isCallOut = false;
  if (bolOneFormSubmission!=null && bolOneFormSubmission) {
    if (gSubmitted==1) {
      showJSMessage(16);
      return false;
    } else {
      gSubmitted=1;
      if (isCallOut) setGWaitingCallOut(true, frameName);
      field.value = value;
      removeOnUnloadHandler(form); // Prevents opener reload
      form.submit();
    }
  } else {
    if (isCallOut) setGWaitingCallOut(true, frameName);
    field.value = value;
    removeOnUnloadHandler(form); // Prevents opener reload
    form.submit();
  }
  return true;
}

/**
* Delays a Command execution
* @param {String} text String that contains the JavaScript command
* @returns An identificator for the timer.
*/
function reloadFunction(text) {
  return setTimeout(text, 1000);
}

/**
* Identify the last field changed, for on screen debugging. This function requires the inpLastFieldChanged field.
* @param {Object} field Reference to the modified field. 
* @param {Form} form Form where the inpLastFieldChanged is located 
* @returns True if everything was correct. False if the inpLastFieldChanged was not found
* @type Boolean
*/
function setChangedField(field, form) {
  if (form==null || !form) form = document.forms[0];
  if (form.inpLastFieldChanged==null) return false;
  if (field.type.toUpperCase().indexOf("SELECT")!=-1) {
    if(field.selectedIndex==-1 || field.options[field.selectedIndex].defaultSelected)
      return false;
  }
  form.inpLastFieldChanged.value = field.name;
  return true;
}

/**
 * Checks if Autosave is enabled or not
 * @return true if autosave is enable, otherwise false
 */
function isAutosaveEnabled() {
  var autosave = getFrame('frameMenu').autosave;
  return autosave;
}

/**
 * Logs a User click to flag the document as changed
 * @param hiddenInput HTML input part of the button UI
 * @return
 */
function logClick(hiddenInput) {
  var autosave = isAutosaveEnabled();
  if(typeof autosave == "undefined" || !autosave) {
    return;
  }
  if(hiddenInput != null) {
    logChanges(hiddenInput);
    isButtonClick = true;
    return;
  }
  isTabClick = true;
}

/**
 * Helper function to reload the opener window. Used on NEW documents when closing a pop-pup.
 * @return
 */
function reloadOpener() {
  if(top.opener) {
    var f = getFrame('appFrame');
    if(f == null) {
      f = top.opener;
    }
    if(isDebugEnabled()) {
      console.info("getFrame - f: %o", f);
    }
    var buttonRefresh = f.document.getElementById('buttonRefresh');
    if(buttonRefresh !== null) {
      buttonRefresh.onclick();
    }
  }
}

 /**
  * Removes the onunload function reference
  * @return
  */
function removeOnUnload() {
  window.onunload = null;
}

/**
 * Checks if the window is a pop-up and removes the onUnload handler
 * All pop-ups must include IsPopUpCall hidden input
 * @param f
 * @return
 */
function removeOnUnloadHandler(form) {
  var f = form;
  if(typeof f === 'undefined' || f === null) {
    f = document.forms[0];
  }
  if(typeof f.IsPopUpCall !== 'undefined' && f.IsPopUpCall.value === '1') {
    // Checking for a onunload event handler
    if(typeof window.onunload === 'function') {
      if(isDebugEnabled()) {
        console.log("Removing onUnload handler");
      }
      removeOnUnload();
    }
  }
}

/**
* Check for changes in a Form. This function requires the inpLastFieldChanged field. Is a complementary function to {@link #setChangedField}
* @param {Form} f Reference to a form where the inpLastFieldChanged is located.
* @returns True if the inpLastFieldChanged has data and the user confirm the pop-up message. False if the field has no data or the user no confirm the pop-up message.
* @type Boolean
*/
function checkForChanges(f) {
	var form = f;
	
	if (form === null) {
		form = getFrame('appFrame').document.forms[0];
	}
	
	if(typeof form === 'undefined') {
		return true;
	}
	
	var autosave = isAutosaveEnabled();
	
	if(typeof autosave === 'undefined' || !autosave) { // 2.40 behavior		
		if (inputValue(form.inpLastFieldChanged) !== "") {
			if (!showJSMessage(26))
				return false;
		}
		if(form.autosave) {
			form.autosave.value = 'N';
		}
		return true;
	}
	else {
		if(typeof parent.appFrame === 'undefined'){
			return true;
		}

		try {
		  var promptConfirmation = typeof parent.appFrame.confirmOnChanges === 'undefined' ? true : parent.appFrame.confirmOnChanges;
		} catch(e) {
		  if(isDebugEnabled()) {
            console.error("%o", e);
		  }
		}

		try {
		  var hasUserChanges = typeof parent.appFrame.isUserChanges === 'undefined' ? false : parent.appFrame.isUserChanges;
		} catch(e) {
		  if(isDebugEnabled()) {
            console.error("%o", e);
		  }
		}

        if(typeof promptConfirmation === 'undefined' || typeof hasUserChanges === 'undefined') { // Nothing to be done
          return true;
        }

		if (form.inpLastFieldChanged && (hasUserChanges || isButtonClick || isTabClick)) { // if the inpLastFieldChanged exists and there is a user change
			var autoSaveFlag = autosave;		
			if (promptConfirmation && hasUserChanges) {
				autoSaveFlag = showJSMessage(25);
				if(typeof parent.appFrame.confirmOnChanges !== 'undefined' && autoSaveFlag) {
					parent.appFrame.confirmOnChanges = false;
				}
			}
			if (autoSaveFlag) {
				if(form.autosave) {
					form.autosave.value = 'Y';
				}
			}
		}
		return true;
	}	
}

/**
 * Prompt a confirmation when an autosave process has failed. If the wants to
 * stay in the page or navigate to the requested URL
 * @param refererURL String URL to navigate to
 * @return 
 */
function continueUserAction(requestURL) {
	if(typeof(requestURL) == 'undefined') { 
		return false;
	}	
	var continueAction = showJSMessage(26, null, false);
	if(continueAction) {
		submitCommandForm('DEFAULT', false, null, requestURL, 'appFrame', false, true);
	}
	return true;
}

/**
* Function Description
* @param {Form} form
* @param {String} columName
* @param {String} parentKey
* @param {String} url
* @param {String} keyId
* @param {String} tableId
* @param {String} newTarget
* @param {Boolean} bolCheckChanges
* @returns
* @type Boolean
*/
function sendDirectLink(form, columnName, parentKey, url, keyId, tableId, newTarget, bolCheckChanges) {
  if (form == null) form = document.forms[0];
  var frmDebug = document.forms[0];
  var action = "DEFAULT";
  var autosave = isAutosaveEnabled();

  if(autosave && isUserChanges) {
	try { initialize_MessageBox('messageBoxID'); } catch (ignored) {}
	if (!depurar_validate_wrapper(action, form, "")) return false;
  }
  if (bolCheckChanges==null) bolCheckChanges = false;
  if (arrGeneralChange!=null && arrGeneralChange.length>0 && bolCheckChanges) {
    var strFunction = "sendDirectLink('" + form.name + "', '" + columnName + "', '" + parentKey + "', '" + url + "', '" + keyId + "', '" + tableId + "', " + ((newTarget==null)?"null":"'" + newTarget + "'") + ", " + bolCheckChanges + ")";
    reloadFunction(strFunction);
    return false;
  }
  if (bolCheckChanges && !checkForChanges(frmDebug)) return false;
  if (confirmAction(action)) {
    form.action = url;
    if (newTarget != null) form.target = newTarget;
    form.inpKeyReferenceColumnName.value = columnName;
    form.inpSecondKey.value = parentKey;
    form.inpKeyReferenceId.value = keyId;
    form.inpTableReferenceId.value = tableId;
    if (isWindowInMDIContext) {
      var LayoutMDI = getFrame('LayoutMDI');
      if (typeof LayoutMDI.OB.Layout.ClassicOBCompatibility.sendDirectLink === "function") {
        action = "JSON";
        LayoutMDI.OB.Layout.ClassicOBCompatibility.sendDirectLink(action, form);
      }
    } else {
      submitForm(form.Command, action, form, false, false);
    }

  }
  return true;
}

/**
* Fires the onChange event on a specified field
* @param {Object} target Reference to the evaluated field.
* @returns True
* @type Boolean
*/
function dispatchEventChange(target) {
  if (!target) return true;
  if (!target.type) return true;
  if (target.onchange && target.defaultValue && target.defaultValue != inputValue(target)) target.onchange();
  else if (target.onblur) target.onblur();
  return true;
}

/**
 * Created and Deprecated in 2.50
 * It calls either the validate-function or if it does not exist
 * the depurar-function. The depurar-function  has been renamed to validate.
 * These functions are defined in each HTML-page and called from here.
 * This wrapper-function is used to support both function-names until
 * all custom code has been migrated to the new name.
 */
function depurar_validate_wrapper(action, form, value) {
  // if new-style validate-function exists => call it
  if (typeof validate === "function") {
    return validate(action, form, value);
  } else {
    // call old-style depurar function
    return depurar(action, form, value);
  } 
}

/**
* Submit a form after setting a value to the Command field. The Command field is a string to define the type of operation that the servlet will execute. Also allows to debug previous the submition. This function execution requires a hidden field with name Command in the form.
* @param {String} action Identify the operation that the servlet will execute.
* @param {Boolean} bolValidation Set if you want to debug previous the form submission. The default value is false. If is true, you must implement a boolean returning function named depurar that makes all the debugging functionality. If depurar returns false the form will not be submited.
* @param {Form} form A reference to the form that will be submitted. If is null, the first form in the page will be used.
* @param {String} newAction Set the URL where we want to send the form. If is null the URL in the form's action attribute will be used.
* @param {String} newTarget Set the window or frame where we want to send the form. If is null the form's target attribute will be used.
* @param {Boolean} bolOneFormSubmission Verify the form submission, waits for a server response. Prevents a multiple submission. The default value false.
* @param {Boolean} bolCheckChanges  If we want to check for changes in the window, and presents a pop-up message.
* @param {Boolean} isCallOut Defines if we are making a submission to a CallOut.
* @param {Boolean} controlEvt
* @param {Event} evt
* @returns True if everything goes correct and the data is sent. False on any problem, is no able to send the data or by the user cancelation un the pop-up message.
* @type Boolean
*/
function submitCommandForm(action, bolValidation, form, newAction, newTarget, bolOneFormSubmission, bolCheckChanges, isCallOut, controlEvt, evt) {
  var f = form || document.forms[0];
  if (bolValidation!=null && bolValidation==true){
    try { initialize_MessageBox('messageBoxID'); } catch (ignored) {}
    if (!depurar_validate_wrapper(action, f, "")) return false;
  } 
  if (bolCheckChanges==null) bolCheckChanges = false;
  if (isCallOut==null) isCallOut = false;
  if (controlEvt==null) controlEvt = false;
  if (controlEvt) {
    if (!evt) evt = window.event;
    var target = (document.layers) ? evt.target : evt.srcElement;
    dispatchEventChange(target);
  }
  if (gWaitingCallOut || (arrGeneralChange!=null && arrGeneralChange.length>0 && bolCheckChanges)) {
    var strFunction = "submitCommandForm('" + action + "', " + bolValidation + ", " + f.name + ", " + ((newAction!=null)?("'" + newAction + "'"):"null") + ", " + ((newTarget!=null)?("'" + newTarget + "'"):"null") + ", " + bolOneFormSubmission + ", " + bolCheckChanges + ")";
    reloadFunction(strFunction);
    return false;
  }
  if (bolCheckChanges && !checkForChanges(f)) return false;
  if (confirmAction(action)) {
    if (newAction != null) f.action = newAction;
    // Deprecated in 2.50, This code is only here fore backwards compatibility
    // it allow callers which still use the old names to work
    if ((newTarget != null) && (newTarget == 'frameAplicacion')) {
      newTarget = 'appFrame';
    }
    if ((newTarget != null) && (newTarget == 'frameOculto')) {
        newTarget = 'hiddenFrame';
      }
    if (newTarget != null) f.target = newTarget;
    submitForm(f.Command, action, f, bolOneFormSubmission, isCallOut);
  }
  return true;
}


/**
* Submit a form after setting a value to the Command field, and adding an additional parameter/value to the form. This function requires a hidden Command field in the form.
* @param {String} action Identify the operation to be executed by the servlet. 
* @param {Object} field Reference to the field where we want to set the value.
* @param {String} value Value to set at the selected field.
* @param {Boolean} bolValidation Set if you want to debug previous the form submission. The default value is false. If is true, you must implement a boolean returning function named depurar that makes all the debugging functionality. If depurar returns false the form will not be submited.
* @param {Form} form A reference to the form that will be submitted. If is null, the first form in the page will be used.
* @param {String} formAction Set the URL where we want to send the form. If is null the URL in the form's action attribute will be used.
* @param {String} newTarget Set the window or frame where we want to send the form. If is null the form's target attribute will be used.
* @param {Boolean} bolOneFormSubmission Verify the form submission, waits for a server response. Prevents a multiple submission. The default value false.
* @param {Boolean} bolCheckChanges If we want to check for changes in the window, and presents a pop-up message.
* @param {Boolean} isCallOut Defines if we are sending the data to a CallOut 
* @param {Boolean} controlEvt Set if the function should control the events.
* @param {Event} evt Event handling object
* @returns True if everything works correctly. False on any problem or by the user cancellation at the pop-up message.
* @type Boolean
*/
function submitCommandFormParameter(action, field, value, bolValidation, form, formAction, newTarget, bolOneFormSubmission, bolCheckChanges, isCallOut, controlEvt, evt) {
  if (form == null) form = document.forms[0];
  if (bolValidation!=null && bolValidation==true){
    try { initialize_MessageBox('messageBoxID'); } catch (ignored) {}
  if (!depurar_validate_wrapper(action, form, value)) return false;
  }
  if (bolCheckChanges==null) bolCheckChanges = false;
  if (isCallOut==null) isCallOut = false;
  if (controlEvt==null) controlEvt = false;
  if (controlEvt) {
    if (!evt) evt = window.event;
    var target = (document.layers) ? evt.target : evt.srcElement;
    dispatchEventChange(target);
  }
  if (gWaitingCallOut || (arrGeneralChange!=null && arrGeneralChange.length>0 && bolCheckChanges)) {
    var strFunction = "submitCommandFormParameter('" + action + "', " + field.form.name + "." + field.name + ", '" + value + "', " + bolValidation + ", " + form.name + ", " + ((formAction!=null)?("'" + formAction + "'"):"null") + ", " + ((newTarget!=null)?("'" + newTarget + "'"):"null") + ", " + bolOneFormSubmission + ", " + bolCheckChanges + ", " + isCallOut + ")";
    reloadFunction(strFunction);
    return false;
  }

  if (bolCheckChanges && !checkForChanges(form)) return false;

  if (confirmAction(action)) {
    field.value = value;
    if (formAction != null) form.action = formAction;
    // Deprecated in 2.50, This code is only here fore backwards compatibility
    // it allow callers which still use the old frameAplicacion name to work with the new name appFrame
    if ((newTarget != null) && (newTarget == 'frameAplicacion')) {
      newTarget = 'appFrame';
    }
    if ((newTarget != null) && (newTarget == 'frameOculto')) {
      newTarget = 'hiddenFrame';
    }
    if (newTarget != null) form.target = newTarget;
    submitForm(form.Command, action, form, bolOneFormSubmission, isCallOut);
  }
  return true;
}

/**
* Verify if a text is an allowed number.
* @param {String} strValue Text to evaluate.
* @param {Boolean} isFloatAllowed Set if a float number is allowed
* @param {Boolean} isNegativeAllowed Set if a negative number is allowed
* @returns True if the text is a allowed number, false if not is a number or not an allowed number.
* @type Boolean
*/
function validateNumber(strValue, isFloatAllowed, isNegativeAllowed) {
  var isComma = false;
  var isNegative = false;
  var i=0;
  if (strValue == null || strValue=="") return true;

  var decSeparator = getGlobalDecSeparator();
  var groupSeparator = getGlobalGroupSeparator();

  strValue = returnFormattedToCalc(strValue, decSeparator, groupSeparator);

  if (strValue.substring(i, i+1)=="-") {
    if (isNegativeAllowed !=null && isNegativeAllowed) {
      isNegative = true;
      i++;
    } else {
      return false;
    }
  } else if (strValue.substring(i, i+1)=="+")
    i++;
  var total = strValue.length;
  for (i=i;i<total;i++) {
    if (isNaN(strValue.substring(i,i+1))) {
      if (isFloatAllowed && strValue.substring(i,i+1)=="." && !isComma) 
        isComma = true;
      else
        return false;
    }
  }
  return true;
}

/**
* Validate that the information entered in a field is a number, if not, this function displays an error message and set the focus on the field. Also you can control if the number is an Integer, positive or negative number.
* @param {Object} field A reference to a field that will be evaluated.
* @param {Boolean} isFloatAllowed Set if a float number is allowed.
* @param {Boolean} isNegativeAllowed Set if a negative number is allowed.
* @returns True if the field's content is a number, false if the field's content is not a number or does not accomplish the requirements
* @type Boolean
* @see #validateNumber
*/
function validateNumberField(field, isFloatAllowed, isNegativeAllowed) {
  if (!validateNumber(field.value, isFloatAllowed, isNegativeAllowed))
  {
    showJSMessage(4);
    field.focus();
    field.select();
    return false;
  }
  return true;
}

/**
* Search in the array, and return the value from a specified index.
* @param {Array} data Array into search for
* @param {String} name The index to look for
* @param {String} defaultValue The default value if the index is not found
* @returns The value of the array if the index name was found, otherwise returns the defaultValue. If the defaultValue is null returns an empty String.
* @type String
*/
function getArrayValue(data, name, defaultValue) {
  if (data==null || data.length<=0) return ((defaultValue!=null)?defaultValue:"");
  var total = data.length;
  for (var i=0;i<total;i++) {
    if (data[i][0]==name) return data[i][1];
  }
  return ((defaultValue!=null)?defaultValue:"");
}

/**
* Add a value to Array
* @param {Array} data Array where the value will be added
* @param {String} name Index of the new value
* @param {String} value Value to add
* @param {Boolean} isUrlParameter Set if is a URL Parameter
* @returns An array with the added value.
* @type Array
*/
function addArrayValue(data, name, value, isUrlParameter) {
  if (isUrlParameter==null) isUrlParameter=false;
  if (data==null || data.length<=0) {
    data = new Array();
    data[0] = new Array(name, value, (isUrlParameter?"true":"false"));
    return data;
  }
  var total = data.length;
  for (var i=0;i<total;i++) {
    if (data[i][0]==name) {
      data[i][1] = value;
      return data;
    }
  }
  data[total] = new Array(name, value, (isUrlParameter?"true":"false"));
  return data;
}

/**
* Extract the parameters from the array 
* @param {Array} data Array to extract all the parameters
* @returns A String in the form variable1=value1[&variablen=valuen]
* @type String
*/
function addUrlParameters(data) {
  if (data==null || data.length<=0) return "";
  var total = data.length;
  var text = "";
  for (var i=0;i<total;i++) {
    if (data[i][2]=="true") text += ((text!=null && text!="")?"&":"") + data[i][0] + "=" + escape(data[i][1]);
  }
  if (text!=null && text!="") text = "?" + text;
  return text;
}

var openPopUpMDICheck = false;
/**
* Opens a pop-up window and adds custom properties to it 
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels.
* @param {Number} top Specifies the distance the new window is placed from the top side of the work area for applications of the user's operating system to the topmost border (resizing handle) of the browser window.
* @param {Number} left Specifies the distance the new window is placed from the left side of the work area for applications of the user's operating system to the leftmost border (resizing handle) of the browser window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @param {String} target Specifies the window or frame where we want to send the form. If is null the form's target attribute will be used.
* @param {Boolean} doSubmit Specifies whether or not should submit the form. If is true this function calls {@link #submitCommandForm} function
* @param {Boolean} closeControl Specifies if the new window should be closed in the unload event.
* @param {Array} parameters Array list of the available parameters for the new window.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #addUrlParameters 
* @see #getArrayValue 
* @see #submitCommandForm
*/
function openPopUp(url, _name, height, width, top, left, checkChanges, target, doSubmit, closeControl, parameters, hasLoading, openInMDIPopup) {
  var appUrl = getAppUrl();
  var adds = "";
  var isPopup = null;
  if (navigator.userAgent.toUpperCase().indexOf("MSIE") != -1) {
    _name = _name.replace(/ /g,"_"); //To fix strange issue with IE8 that window name declared as var xx = window.open can not have spaces
  }
  // Deprecated in 2.50, search for the old frameAplication and the new appFrame
  if (_name!='appFrame' && _name!='frameAplicacion' && _name!='frameMenu') isPopup =  true;
  else isPopup = false;
  if (height==null) height = screen.height - 50;
  if (width==null) width = screen.width;
  if (height.toString().indexOf("%") != -1) {
    height = height.replace('%','');
    height = parseInt(height);
    height = screen.height * (height/100);
  }
  if (width.toString().indexOf("%") != -1) {
    width = width.replace('%','');
    width = parseInt(width);
    width = screen.width * (width/100);
  }
  if (top==null) top = (screen.height - height) / 2;
  if (left==null) left = (screen.width - width) / 2;
  if (checkChanges==null) checkChanges = false;
  if (closeControl==null) closeControl = false;
  if (doSubmit==null) doSubmit = false;
  if (checkChanges && !checkForChanges()) return false;
  if (url!=null && url!="") url += addUrlParameters(parameters);
  if (target!=null && target!="" && target.indexOf("_")!=0) {
    var objFrame = eval("parent." + target);
    objFrame.location.href=url;
    return true;
  }
  if (hasLoading==null) hasLoading = true;
  adds = "height=" + height + ", width=" + width + ", left=" + left + ", top=" + top;
  if (navigator.appName.indexOf("Netscape")) {
    adds += ", alwaysRaised=" + getArrayValue(parameters, "alwaysRaised", "1");
    adds += ", dependent=" + getArrayValue(parameters, "dependent", "1");
    adds += ", directories=" + getArrayValue(parameters, "directories", "0");
    adds += ", hotkeys=" + getArrayValue(parameters, "hotkeys", "0");
  }
  adds += ", location=" + getArrayValue(parameters, "location", "0");
  adds += ", scrollbars=" + getArrayValue(parameters, "scrollbars", "0");
  adds += ", status=" + getArrayValue(parameters, "status", "1");
  adds += ", menubar=" + getArrayValue(parameters, "menubar", "0");
  adds += ", toolbar=" + getArrayValue(parameters, "toolbar", "0");
  adds += ", resizable=" + getArrayValue(parameters, "resizable", "1");
  if (doSubmit && (getArrayValue(parameters, "debug", false)==true)) {
    if (!depurar_validate_wrapper(getArrayValue(parameters, "Command", "DEFAULT"), null, "")) return false;
  }

  if (isWindowInMDIPage && openInMDIPopup && isPopup == true) {
    if (getFrame('LayoutMDI') && getFrame('LayoutMDI').OB && getFrame('LayoutMDI').OB.Layout && getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility && getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup && getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup.open) {
      if (!openPopUpMDICheck) {
        getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup.open(_name, width, height, "", "", window);
        openPopUpMDICheck = true;
      }

      if (!getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup.isLoaded(_name)) {
        setTimeout(function() { openPopUp(url, _name, height, width, top, left, checkChanges, target, doSubmit, closeControl, parameters, hasLoading, openInMDIPopup); }, 50);
        return true;
      }
      openPopUpMDICheck = false;
    }
  }

  if (isPopup == true && hasLoading == true) {
    isPopupLoadingWindowLoaded=false;
    var urlLoading = appUrl + '/utility/PopupLoading.html';
    var winPopUp = window.open((doSubmit?urlLoading:url), _name, adds);
  } else {
    var winPopUp = window.open((doSubmit?"":url), _name, adds);
  }
  winPopUp.onunload = function() {
    if (winPopUp.location.href != "about:blank" && winPopUp.location.href.indexOf("utility/PopupLoading.html") == -1) {
      putFocusOnMenu();
    }
  }
  if (closeControl) window.onunload = function(){winPopUp.close();}
  if (doSubmit) {
    if (isPopup==true && hasLoading == true) synchronizedSubmitCommandForm(getArrayValue(parameters, "Command", "DEFAULT"), (getArrayValue(parameters, "debug", false)==true), null, url, _name, target, checkChanges);
    else submitCommandForm(getArrayValue(parameters, "Command", "DEFAULT"), (getArrayValue(parameters, "debug", false)==true), null, url, _name, target, checkChanges);
  }
  winPopUp.focus();
  return winPopUp;
}

function synchronizedSubmitCommandForm(action, bolValidation, form, newAction, newTarget, bolOneFormSubmission, bolCheckChanges, isCallOut, controlEvt, evt) {
  if (isPopupLoadingWindowLoaded==false) {
    setTimeout(function() {synchronizedSubmitCommandForm(action, bolValidation, form, newAction, newTarget, bolOneFormSubmission, bolCheckChanges, isCallOut, controlEvt, evt);},50);
    return;
  } else {
    submitCommandForm(action, bolValidation, form, newAction, newTarget, bolOneFormSubmission, bolCheckChanges, isCallOut, controlEvt, evt);
  }
}

function setPopupLoadingWindowLoaded(value) {
  if (value == '' || value == 'null' || value == null) value = true;
  isPopupLoadingWindowLoaded = value;
}

/**
* Opens a pop-up window and adds custom properties to it 
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels.
* @param {Number} top Specifies the distance the new window is placed from the top side of the work area for applications of the user's operating system to the topmost border (resizing handle) of the browser window.
* @param {Number} left Specifies the distance the new window is placed from the left side of the work area for applications of the user's operating system to the leftmost border (resizing handle) of the browser window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @param {String} target Specifies the window or frame where we want to send the form. If is null the form's target attribute will be used.
* @param {Boolean} doSubmit Specifies whether or not should submit the form. If is true this function calls {@link #submitCommandForm} function
* @param {Boolean} closeControl Specifies if the new window should be closed in the unload event.
* @param {Array} parameters Array list of the available parameters for the new window.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp 
* @see #addArrayValue
*/
function openNewLink(url, _name, height, width, top, left, checkChanges, target, doSubmit, closeControl, parameters) {
  parameters = addArrayValue(parameters, "location", "1");
  parameters = addArrayValue(parameters, "scrollbars", "1");
  parameters = addArrayValue(parameters, "status", "1");
  parameters = addArrayValue(parameters, "menubar", "1");
  parameters = addArrayValue(parameters, "toolbar", "1");
  parameters = addArrayValue(parameters, "resizable", "1");
  return openPopUp(url, _name, height, width, top, left, checkChanges, target, doSubmit, closeControl, parameters);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels.
* @param {Number} top Specifies the distance the new window is placed from the top side of the work area for applications of the user's operating system to the topmost border (resizing handle) of the browser window.
* @param {Number} left Specifies the distance the new window is placed from the left side of the work area for applications of the user's operating system to the leftmost border (resizing handle) of the browser window.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openNewLink
*/
function openNewBrowser(url, _name, height, width, top, left) {
  return openNewLink(url, _name, height, width, top, left, null, null, null, true, null);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp
*/
function openExcel(url, _name, checkChanges) {
  return openPopUp(url, _name, null, null, null, null, checkChanges, null, null, false, null);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp
*/
function openPDF(url, _name, checkChanges) {
  return openPopUp(url, _name, null, null, null, null, checkChanges, null, null, false, null);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Boolean} checkChanges Set if we want to check for changes previous the form submition.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp
*/
function openPDFFiltered(url, _name, checkChanges) {
  return openPopUp(url, _name, null, null, null, null, checkChanges, null, true, false, null);
}

/**
* Opens a pop-up window with the default window parameters
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels. If is null, a fixed height of 250 pixels is used.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels. If is null, a fixed width of 230 pixels is used.
* @param {Boolean} closeControl Specifies if the new window should be closed in the unload event.
* @param {Boolean} showstatus
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp
*/
function openPopUpDefaultSize(url, _name, height, width, closeControl, showstatus) {
  if (height==null) height = 250;
  if (width==null) width = 230;
  return openPopUp(url, _name, height, width, null, null, null, null, null, closeControl, null);
}

/**
* Opens a PDF session
* @param {String} strPage
* @param {String} strDirectPrinting
* @param {String} strHiddenKey
* @param {String} strHiddenValue
* @param {Boolean} bolCheckChanges
* @returns
* @type Boolean
* @see #submitCommandForm
*/
function openPDFSession(strPage, strDirectPrinting, strHiddenKey, strHiddenValue, bolCheckChanges) {
  var appUrl = getAppUrl();
  var direct = (strDirectPrinting!="")?"Y":"N";
  return submitCommandForm("DEFAULT", false, null, appUrl + "/businessUtility/PrinterReports.html?inppdfpath=" + escape(strPage) + "&inpdirectprint=" + escape(direct) + "&inphiddenkey=" + escape(strHiddenKey) + ((strHiddenValue!=null)?"&inphiddenvalue=" + escape(strHiddenValue):""), "hiddenFrame", null, bolCheckChanges);
}

/**
* Opens a pop-up window after setting the necessary parameters
* @param {String} url
* @param {String} _name
* @param {String} tabId
* @param {String} windowName
* @param {String} windowId
* @param {String} checkChanges
* @returns An ID reference pointing to the newly opened browser window.
* @type Object 
* @see #addArrayValue 
* @see #openPopUp 
*/
function openSearchWindow(url, _name, tabId, windowName, windowId, checkChanges) {
  var parameters = new Array();
  parameters = addArrayValue(parameters, "inpTabId", tabId, true);
  parameters = addArrayValue(parameters, "inpWindow", windowName, true);
  parameters = addArrayValue(parameters, "inpWindowId", windowId, true);
  return openPopUp(url, _name, 450, 600, null, null, checkChanges, null, null, true, parameters);
}

/**
* Function Description
* @param {String} windowId
* @param {String} url
* @param {String} _name
* @param {Boolean} checkChanges
* @param {Number} height
* @param {Number} width
* @param {String} windowType
* @param {String} windowName
* @returns A reference pointing to the newly opened window
* @type Object
* @see #openPopUp 
* @see #addArrayValue
*/
function openHelp(windowId, url, _name, checkChanges, height, width, windowType, windowName, openInMDIPopup) {
  if (height==null) height = 450;
  if (width==null) width = 700;
  var parameters = new Array();
  parameters = addArrayValue(parameters, "inpwindowId", windowId, true);
  parameters = addArrayValue(parameters, "inpwindowType", windowType, true);
  parameters = addArrayValue(parameters, "inpwindowName", windowName, true);
  return openPopUp(url, _name, height, width, null, null, checkChanges, null, null, true, parameters, null, openInMDIPopup);
}

/**
* Function Description
* @param {String} Command
* @param {Boolean} bolValidation
* @param {String} url
* @param {String} _name
* @returns An ID reference pointing to the newly opened browser window.
* @type Object 
* @see #openPopUp 
* @see #addArrayValue
*/
function openServletNewWindow(Command, bolValidation, url, _name, processId, checkChanges, height, width, resizable, hasStatus, closeControl, hasLoading, openInMDIPopup) {
  if (height==null) height = 350;
  if (width==null) width = 500;
  if (closeControl==null) closeControl = true;
  var parameters = new Array();
  parameters = addArrayValue(parameters, "scrollbars", "1");
  parameters = addArrayValue(parameters, "debug", bolValidation, false);
  if (processId!=null && processId!="") parameters = addArrayValue(parameters, "inpProcessId", processId, true);
  if (Command!=null && Command!="") parameters = addArrayValue(parameters, "Command", Command, false);
  if (navigator.userAgent.toUpperCase().indexOf("MSIE") != -1) {
    setTimeout(function() {return openPopUp(url, _name, height, width, null, null, checkChanges, null, true, closeControl, parameters, hasLoading, openInMDIPopup);},10);
  } else {
    return openPopUp(url, _name, height, width, null, null, checkChanges, null, true, closeControl, parameters, hasLoading, openInMDIPopup);
  }
}


/**
* Opens a pop-up window with default parameter values 
* @param {String} url This is the URL to be loaded in the newly opened window.
* @param {String} _name This is the string that just names the new window.
* @param {Number} height Specifies the height of the content area, viewing area of the new secondary window in pixels.
* @param {Number} width Specifies the width of the content area, viewing area of the new secondary window in pixels.
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openNewLink
*/
function openLink(url, _name, height, width) {
  return openNewLink(url, ((_name.indexOf("_")==0)?"":_name), height, width, null, null, null, ((_name.indexOf("_")==0)?_name:""), false, false, null);
}

/**
* Opens a pop-up window 
* @param {String} url
* @param {String} type
* @param {String} id
* @param {String} value
* @param {Number} height
* @param {Number} width
* @returns An ID reference pointing to the newly opened browser window.
* @type Object
* @see #openPopUp 
* @see #addArrayValue
*/
function editHelp(url, type, id, value, height, width, openInMDIPopup) {
  if (height==null) height = 500;
  if (width==null) width = 600;
  var parameters = new Array();
  parameters = addArrayValue(parameters, "Command", type, true);
  parameters = addArrayValue(parameters, "inpClave", value, true);
  parameters = addArrayValue(parameters, "IsPopUpCall", "1", true);
  return openPopUp(url, "HELP_EDIT", height, width, null, null, null, null, false, true, parameters, null, openInMDIPopup);
}

/**
* Handles window events. This function handles events such as KeyDown; when a user hit the ENTER key to do somethig by default.
* @param {Number} keyCode ASCII code of the key pressed.
* @returns True if the key pressed is not ment to be handled. False if is a handled key. 
* @type Boolean
*/
function keyPress(keyCode) {
  if (gByDefaultAction!=null)
  {
    var tecla = (!keyCode) ? window.event.keyCode : keyCode.which;
    if (tecla == 13)
    {
      eval(gByDefaultAction);
      return false;
    }
  }
  return true;
}


/**
* Defines a defult action on each page, the one that will be executed when the user hit the ENTER key. This function is shared in pages containing frames.
* @param {String} action Default command to be executed when the user hit the ENTER key.
* @returns Always retrun true.
* @type Boolean
* @see #keyPress
*/
function byDefaultAction(action) {
  gByDefaultAction = action;
  if (!document.all)
  {
    document.captureEvents(Event.KEYDOWN);
  }
  document.onkeydown=keyPress;
  return true;
}


/**
* Stops the propagation and the default action of the browser shortchut
* @param {Event} evt Event handling object.
*/
var previousOnKeyPress = "";

/**
* Stops the propagation and the default action of the browser shortchut
* @param {Event} evt Event handling object.
*/
function stopKeyPressEvent(evt) {
  previousOnKeyPress = document.onkeypress;
  document.onkeypress = stopKeyPressPropagation;
  return true;
}

/**
* Stops the propagation and the default action of the browser shortchut
* @param {Event} evt Event handling object.
*/
function stopKeyPressPropagation(evt) {
  try {
    if (evt.ctrlKey) {
      evt.cancelBubble = true;
      evt.returnValue = false;
        if (evt.stopPropagation) {
          evt.preventDefault();
        }
    } else if (evt.altKey) {
      evt.cancelBubble = true;
      evt.returnValue = false;
        if (evt.stopPropagation) {
          evt.preventDefault();
        }
    }
  } catch(e) {}
  document.onkeypress = previousOnKeyPress;
}


/**
* Enables the propagation and the default action of the browser shortchut
* @param {Event} evt Event handling object.
*/
function startKeyPressEvent(evt) {
  return true;
}


/**
* Builds the keys array on each screen. Each key that we want to use should have this structure.
* @param {String} key A text version of the handled key.
* @param {String} evalfunc Function that will be eval when the key is is pressed.
* @param {String} field Name of the field on the window. If is null, is a global event, for the hole window.
* @param {String} auxKey Text defining the auxiliar key. The value could be CTRL for the Control key, ALT for the Alt, null if we don't have to use an auxiliar key.
* @param {Boolean} propagateKey True if the key is going to be prograpated or false if is not going to be propagated.
* @param {String} eventShotter Function that will launch the process.
*/
function keyArrayItem(key, evalfunc, field, auxKey, propagateKey, event) {
  this.key = key;
  this.evalfunc = evalfunc;
  this.field = field;
  this.auxKey = auxKey;
  this.propagateKey = propagateKey;
  this.eventShotter = event;
}


/**
* Returns the ASCII code of the given key
* @param {String} code Text version of a key
* @returns The ASCII code of the key
* @type Number
*/
function obtainKeyCode(code) {
  if (code==null) return 0;
  else if (code.length==1) return code.toUpperCase().charCodeAt(0);
  switch (code.toUpperCase()) {
    case "BACKSPACE": return 8;
    case "TAB": return 9;
    case "ENTER": return 13;
    case "SPACE": return 32;
    case "DELETE": return 46;
    case "INSERT": return 45;
    case "END": return 35;
    case "HOME": return 36;
    case "REPAGE": return 33;
    case "AVPAGE": return 34;
    case "LEFTARROW": return 37;
    case "RIGHTARROW": return 39;
    case "UPARROW": return 38;
    case "DOWNARROW": return 40;
    case "NUMBERPOSITIVE": return 107;
    case "NUMBERNEGATIVE": return 109;
    case "NEGATIVE": return 189;
    case "DECIMAL": return 190;
    case "NUMBERDECIMAL": return 110;
    case "ESCAPE": return 27;
    case "F1": return 112;
    case "F2": return 113;
    case "F3": return 114;
    case "F4": return 115;
    case "F5": return 116;
    case "F6": return 117;
    case "F7": return 118;
    case "F8": return 119;
    case "F9": return 120;
    case "F10": return 121;
    case "F11": return 122;
    case "F12": return 123;
    case "P": return 80;
/*    case "shiftKey": return 16;
    case "ctrlKey": return 17;
    case "altKey": return 18;*/
    default: return 0;
  }
}


/**
* Handles the events execution of keys pressed, based on the events registered in the keyArray global array.
* @param {Event} pushedKey Code of the key pressed.
* @returns True if the key is not registered in the array, false if a event for this key is registered in keyArray array.
* @type Boolean
* @see #obtainKeyCode
*/
function keyControl(pushedKey) {
  try {
    if (keyArray==null || keyArray.length==0) return true;
  } catch (e) {
    return true;
  }
  if (!pushedKey) pushedKey = window.event;
  var thereIsShortcut = false;
  isCtrlPressed = false;
  isAltPressed = false;
  isShiftPressed = false;
  if (pushedKey.ctrlKey) isCtrlPressed = true;
  if (pushedKey.altKey) isAltPressed = true;
  if (pushedKey.shiftKey) isShiftPressed = true;
  pressedKeyCode = pushedKey.keyCode;
  if (isTabPressed == true && isInputFile == true) {
    return true;
  }

  var keyCode = pushedKey.keyCode ? pushedKey.keyCode : pushedKey.which ? pushedKey.which : pushedKey.charCode;
  if (isKeyboardLocked==false) {
    var keyTarget = pushedKey.target ? pushedKey.target: pushedKey.srcElement;
    var total = keyArray.length;
    for (var i=0;i<total;i++) {
      if (keyArray[i] != null && keyArray[i] && keyArray[i].eventShotter != 'onkeyup' && pushedKey.type=='keydown') {
        if (keyCode == obtainKeyCode(keyArray[i].key)) {
          if (keyArray[i].auxKey == null || keyArray[i].auxKey == "" || keyArray[i].auxKey == "null") {
            if (!pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) {
              if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) {
                if (window.event && window.event.keyCode == 116) { //F5 Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
                if (window.event && window.event.keyCode == 121) { //F10 Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
                if (window.event && window.event.keyCode == 27) { //ESC Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
              }
              if (keyArray[i].field==null || (keyTarget!=null && keyTarget.name!=null && isIdenticalField(keyArray[i].field, keyTarget.name))) {
                var evalfuncTrl = replaceEventString(keyArray[i].evalfunc, keyTarget.name, keyArray[i].field);
                try {
                  if (!isWindowInMDIContext || typeof keyArray[i].evalfunc !== "object") {
                    eval(evalfuncTrl);
                  } else {
                    var LayoutMDI = getFrame('LayoutMDI');
                    LayoutMDI.OB.Layout.ClassicOBCompatibility.Keyboard.executeKSFunction(keyArray[i].evalfunc[0], keyArray[i].evalfunc[1]);
                  }
                  thereIsShortcut = true;
                  if (propagateEnter == false && keyArray[i].key == 'ENTER') { // Special ENTER case logic to not propagate if there is default action
                    propagateEnter = true;
                    return false;
                  }
                  if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) {
                    return false;
                  } else {
                    //return true;
                  }
                } catch (e) {
                  return true;
                }
              }
            }
          } else if (keyArray[i].field == null || (keyTarget!=null && keyTarget.name!=null && isIdenticalField(keyArray[i].field, keyTarget.name))) {
            var evalfuncTrl = replaceEventString(keyArray[i].evalfunc, keyTarget.name, keyArray[i].field);
            if ((keyArray[i].auxKey == "ctrlKey" && pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "altKey" && !pushedKey.ctrlKey && pushedKey.altKey && !pushedKey.shiftKey)) {
              if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) { stopKeyPressEvent(); }
            }
            if ((keyArray[i].auxKey == "ctrlKey" && pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "altKey" && !pushedKey.ctrlKey && pushedKey.altKey && !pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "shiftKey" && !pushedKey.ctrlKey && !pushedKey.altKey && pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "ctrlKey+shiftKey" && pushedKey.ctrlKey && !pushedKey.altKey && pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "ctrlKey+altKey" && pushedKey.ctrlKey && pushedKey.altKey && !pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "altKey+shiftKey" && !pushedKey.ctrlKey && pushedKey.altKey && pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "ctrlKey+altKey+shiftKey" && pushedKey.ctrlKey && pushedKey.altKey && pushedKey.shiftKey)) {
              try {
                if (!isWindowInMDIContext || typeof keyArray[i].evalfunc !== "object") {
                  eval(evalfuncTrl);
                } else {
                  var LayoutMDI = getFrame('LayoutMDI');
                  LayoutMDI.OB.Layout.ClassicOBCompatibility.Keyboard.executeKSFunction(keyArray[i].evalfunc[0], keyArray[i].evalfunc[1]);
                }
                thereIsShortcut = true;
                startKeyPressEvent();
                if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) {
                  return false;
                } else {
                  return true;
                }
              } catch (e) {
                startKeyPressEvent();
                return true;
              }
              startKeyPressEvent();
              return true;
            }
          }
        }
      } else if (keyArray[i] != null && keyArray[i] && keyArray[i].eventShotter == 'onkeyup'  && pushedKey.type=='keyup') {
        if (keyCode == obtainKeyCode(keyArray[i].key)) {
          if (keyArray[i].auxKey == null || keyArray[i].auxKey == "" || keyArray[i].auxKey == "null") {
            if (!pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) {
              if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) {
                if (window.event && window.event.keyCode == 116) { //F5 Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
                if (window.event && window.event.keyCode == 121) { //F10 Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
                if (window.event && window.event.keyCode == 27) { //ESC Special case
                  window.event.keyCode = 8;
                  keyCode = 8;
                }
              }
              if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) 
                //stopKeyPressEvent();
              if (keyArray[i].field==null || (keyTarget!=null && keyTarget.name!=null && isIdenticalField(keyArray[i].field, keyTarget.name))) {
                var evalfuncTrl = replaceEventString(keyArray[i].evalfunc, keyTarget.name, keyArray[i].field);
                try {
                  if (!isWindowInMDIContext || typeof keyArray[i].evalfunc !== "object") {
                    eval(evalfuncTrl);
                  } else {
                    var LayoutMDI = getFrame('LayoutMDI');
                    LayoutMDI.OB.Layout.ClassicOBCompatibility.Keyboard.executeKSFunction(keyArray[i].evalfunc[0], keyArray[i].evalfunc[1]);
                  }
                  thereIsShortcut = true;
                  if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) {
                    return false;
                  } else {
                    return true;
                  }
                } catch (e) {
                  startKeyPressEvent();
                  return true;
                }
                startKeyPressEvent();
                return true;
              }
            }
          } else if (keyArray[i].field == null || (keyTarget!=null && keyTarget.name!=null && isIdenticalField(keyArray[i].field, keyTarget.name))) {
            var evalfuncTrl = replaceEventString(keyArray[i].evalfunc, keyTarget.name, keyArray[i].field);
            //if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) stopKeyPressEvent();
            if ((keyArray[i].auxKey == "ctrlKey" && pushedKey.ctrlKey && !pushedKey.altKey && !pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "altKey" && !pushedKey.ctrlKey && pushedKey.altKey && !pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "shiftKey" && !pushedKey.ctrlKey && !pushedKey.altKey && pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "ctrlKey+shiftKey" && pushedKey.ctrlKey && !pushedKey.altKey && pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "ctrlKey+altKey" && pushedKey.ctrlKey && pushedKey.altKey && !pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "altKey+shiftKey" && !pushedKey.ctrlKey && pushedKey.altKey && pushedKey.shiftKey) ||
                (keyArray[i].auxKey == "ctrlKey+altKey+shiftKey" && pushedKey.ctrlKey && pushedKey.altKey && pushedKey.shiftKey)) {
              try {
                if (!isWindowInMDIContext || typeof keyArray[i].evalfunc !== "object") {
                  eval(evalfuncTrl);
                } else {
                  var LayoutMDI = getFrame('LayoutMDI');
                  LayoutMDI.OB.Layout.ClassicOBCompatibility.Keyboard.executeKSFunction(keyArray[i].evalfunc[0], keyArray[i].evalfunc[1]);
                }
                thereIsShortcut = true;
                startKeyPressEvent();
                if ((!keyArray[i].propagateKey || isGridFocused) && !(keyArray[i].key == 'TAB' && isOBTabBehavior == false)) {
                  return false;
                } else {
                  return true;
                }
              } catch (e) {
                startKeyPressEvent();
                return true;
              }
              startKeyPressEvent();
              return true;
            }
          }
        }
      }
    }
  } else {
    return false;
  }
  if (isKeyboardLocked==false && !isCtrlPressed && !isAltPressed && pushedKey.type=='keydown' && pressedKeyCode!='16' && pressedKeyCode!='17' && pressedKeyCode!='18') {
    if (typeof focusedWindowElement != "undefined") {
      if (focusedWindowElement.tagName == 'SELECT') {
        if (focusedWindowElement.getAttribute('onchange') && navigator.userAgent.toUpperCase().indexOf("MSIE") == -1) setTimeout("focusedWindowElement.onchange();",50);
      }
    }
  }
  return true;
}

/**
* Put the focus on the Menu frame
*/
function putFocusOnMenu() {
  if (isWindowInMDIPage) {
    // In a classic window opened in OB 3.0, it avoids the focus go to the hidden menu frame
    // Solves issue https://issues.openbravo.com/view.php?id=17636
    return false;
  } else {
    if (parent && parent.appFrame && parent.appFrame.selectedArea == 'tabs') {
      parent.appFrame.swichSelectedArea();
    }
    if (parent && parent.frameMenu) {
      parent.frameMenu.focus();
    }
    return true;
  }
}

/**
* Put the focus on the Window frame
*/
function putFocusOnWindow() {
  try {
    parent.frameMenu.onBlurMenu();
    parent.appFrame.selectedArea = 'window'
    parent.appFrame.focus();
    parent.appFrame.setWindowElementFocus(parent.appFrame.focusedWindowElement);
  } catch (e) {
    console.log(e);
  }
  return true;
}

/**
* Used to activate the key-press handling. Must be called after set the keys global array <em>keyArray</em>.
*/
function enableShortcuts(type) {
  if (type!=null && type!='null' && type!='') {
    try {
      this.keyArray = new Array();
      if (type=='menu') {
        getShortcuts('applicationCommonKeys');
        getShortcuts('menuSpecificKeys');
      } else if (type=='edition') {
        getShortcuts('applicationCommonKeys');
        getShortcuts('windowCommonKeys');
        getShortcuts('editionSpecificKeys');
        getShortcuts('genericTreeKeys');
        enableDefaultAction();
      } else if (type=='relation') {
        getShortcuts('applicationCommonKeys');
        getShortcuts('windowCommonKeys');
        getShortcuts('relationSpecificKeys');
        getShortcuts('gridKeys');
      } else if (type=='popup') {
        getShortcuts('applicationCommonKeys');
        getShortcuts('windowCommonKeys');
        getShortcuts('editionSpecificKeys');
        getShortcuts('popupSpecificKeys');
        getShortcuts('genericTreeKeys');
        getShortcuts('gridKeys');
        enableDefaultAction();
      }
    } catch (e) {
    }
  }
  keyDownManagement();
  keyUpManagement();
}

function keyDownManagement() {
  if (document.onkeydown) {
    ExternalKeyDownFunction = document.onkeydown;
  }
  document.onkeydown=keyDownExecution;
}

function keyDownExecution(evt) {
  if (!evt) evt = window.event;
  var response = keyControl(evt);
  if (typeof ExternalKeyDownFunction == "function") {
    var responseExternal = ExternalKeyDownFunction(evt);
  }
  return response;
}

function keyUpManagement() {
  if (document.onkeyup) {
    ExternalKeyUpFunction = document.onkeyup;
  }
  document.onkeyup=keyUpExecution;
}

function keyUpExecution(evt) {
  if (!evt) evt = window.event;
  var response = keyControl(evt);
  if (typeof ExternalKeyUpFunction == "function") {
    var responseExternal = ExternalKeyUpFunction(evt);
  }
  return response;
}

/**
* Locks the Keyboard
*/
function lockKeyboard(){
  isKeyboardLocked=true;
  return true;
}

/**
* Unlocks the Keyboard
*/
function unlockKeyboard(){
  isKeyboardLocked=false;
  return true;
}

/**
* Enable or disable the default browser autocomplete feature. By default enable.
* @param {Boolean} state true or false to enable o disable the default browser autocomplete feature
* @returns
* @type Boolean
*/
function setBrowserAutoComplete(state) {
  if (state != false && state != true && state != 'false' && state != 'true') state=true;
  for(var i=0;i<document.forms.length; i++) {
    document.forms[i].setAttribute('autocomplete','off');
  }
  return true;
}

/**
* Validates the name of a Field
* @param {String} arrayName Name of the field to verify
* @param {String} actualName Name of the field to verify
* @returns True, False
* @type Boolean
*/
function isIdenticalField(arrayName, actualName) {
  if (arrayName.substring(arrayName.length-1)=="%") return (actualName.indexOf(arrayName.substring(0, arrayName.length-1))==0);
  else return (arrayName == actualName);
}

/**
* Function Description
* @param {String} eventJS
* @param {String} inputname
* @param {String} arrayName
* @returns
* @type String
* @see #ReplaceText
*/
function replaceEventString(eventJS, inputname, arrayName) {
  eventJS = ReplaceText(eventJS, "@inputname@", inputname);
  if (arrayName!=null && arrayName!="" && arrayName.substring(arrayName.length-1)=="%") {
    var endname = inputname.substring(arrayName.length-1);
    eventJS = ReplaceText(eventJS, "@endinputname@", endname);
  }
  return eventJS;
}

/**
* Allows to set a text in a SPAN, DIV object. Used to dynamically change the text in a section of a HTML page.
* @param {Object} node A reference to a SPAN or DIV object. Or an ID of an object existing in the page.
* @param {String} text The text that we want to change or set.
* @param {Boolean} isId Set if the first parameter is an ID. True for an ID and false for an Object reference.
* @param {Boolean} isAppend Set if we want to append the text to the existing one.
*/
function layer(node, text, isId, isAppend) {
  if (text==null)
    text = "";
  if (isAppend==null) isAppend=false;

  if (document.layers)
  {
    if (isId!=null && isId)
      node = document.layers[node];
    if (node==null) return;
    node.document.write(text);
    node.document.close();
  }
  else if (document.all)
  {
    if (isId!=null && isId)
      node = document.all[node];
    if (node==null) return;
    //node.innerHTML = '';
    try {
      if (isAppend) {
        text = ((node.innerHTML==null)?"":node.innerHTML) + text;
        isAppend = false;
      }
      node.innerHTML = text;
    } catch (e) {
      if (isAppend) {
        text = ((node.outterHTML==null)?"":node.outterHTML) + text;
        isAppend = false;
      }
      node.outterHTML = text;
    }
    node=null;
  }
  else if (document.getElementById) 
  {
    if (isId!=null && isId)
      node = document.getElementById(node);
    if (node==null) return;
    var range = document.createRange();
    range.setStartBefore(node);
    var domfrag = range.createContextualFragment(text);
    while (node.hasChildNodes())
    {
      node.removeChild(node.lastChild);
    }
    node.appendChild(domfrag);
    node=null;
  }
}

/**
* Gets the inner HTML structure of an Layer
* @param {Object} node The Id or reference to the layer.
* @param {Boolean} isId Set if the first parameter is an ID or a reference to an object.
* @returns A inner HTML structure of a given ID
* @type String
* @see #getChildText
*/
function readLayer(node, isId) {
  if (document.layers) {
    if (isId!=null && isId) node = document.layers[node];
    if (node==null) return "";
    return getChildText(node);
  } else if (document.all) {
    if (isId!=null && isId) node = document.all[node];
    if (node==null) return "";
    try {
      return node.innerHTML;
    } catch (e) {
      return node.outterHTML;
    }
  } else if (document.getElementById) {
    if (isId!=null && isId) node = document.getElementById(node);
    if (node==null) return "";
    return getChildText(node);
  }
  return "";
}

/**
* Gets the data of an HTML node. Used for getting the text of a layer, div or span.
* @param {Object} nodo
* @returns The data of a node.
* @type String
*/
function getChildText(nodo) {
  if (nodo==null) return "";
  if (nodo.data) return nodo.data;
  else return getChildText(nodo.firstChild);
}

/**
* Returns the object child of a HTML object
* @param {obj} object
* @returns the object if exist. Else it returns false
* @type Object
*/
function getObjChild(obj) {
  try {
    obj = obj.firstChild;
    for (;;) {
      if (obj.nodeType != '1') {
        obj = obj.nextSibling;
      } else {
        break;
      }
    }
    return obj;
  } catch(e) {
    return false;
  }
}

/**
* Returns the object parent of a HTML object
* @param {obj} object
* @returns the object if exist. Else it returns false
* @type Object
*/
function getObjParent(obj) {
  try {
    obj = obj.parentNode;
    for (;;) {
      if (obj.nodeType != '1') {
        obj = obj.parentNode;
      } else {
        break;
      }
    }
    return obj;
  } catch(e) {
    return false;
  }
}

/**
* Fills a combo with a data from an Array. Allows to set a default selected item, defined as boolean field in the Array.
* @param {Object} combo A reference to the combo object.
* @param {Array} dataArray Array containing the data for the combo. The structure of the array must be value, text, selected. Value is value of the item, text the string that will show the combo, an selected a boolean value to set if the item should appear selected.
* @param {Boolean} bolSelected Sets if the an item will be selected based on the last field of the Array.
* @param {Boolean} withoutBlankOption Set if the first blank element should be removed.
* @returns A string with the new combo structure. An empty string if an error ocurred.
* @type String
*/
function fillCombo(combo, dataArray, bolSelected, withoutBlankOption) {
  var i, value="";
  for (i = combo.options.length;i>=0;i--)
    combo.options[i] = null;
  i=0;
  if (withoutBlankOption==null || !withoutBlankOption)
    combo.options[i++] = new Option("", "");
  if (dataArray==null) return "";

  var total = dataArray.length;
  for (var j=0;j<total;j++) {
    combo.options[i] = new Option(dataArray[j][1], dataArray[j][0]);
    if (bolSelected!=null && bolSelected && dataArray[j][2]=="true") {
      value = dataArray[j][0];
      combo.options[i].selected = true;
    }
    i++;
  }
  return value;
}

/**
* Search for an element in an Array
* @param {Array} dataArray Array of data. The structure of the array is:
*         <ol>
*           <li>key - Element key</li>
*           <li>text - Element text</li>
*           <li>selected - Boolean sets if the element is selected</li>
*         </ol>
* @param {Boolean} bolSelected Set if the element should be selected.
* @returns Returns the key of the founded element. An empty string if was not found or for an empty array.
* @type String
*/
function selectDefaultValueFromArray (dataArray, bolSelected) {
  var value="";
  if (dataArray==null) return "";

  value = dataArray[0][0];
  var total = dataArray.length;
  for (var j=0;j<total;j++) {
    if (bolSelected!=null && bolSelected && dataArray[j][2]=="true") {
      value = dataArray[j][0];
    }
  }
  return value;
}

/**
* Change the sorting direction of a list.
* @param {Object} sourceList A reference to the list that will be sorted.
* @returns True if everything was right, otherwise false.
* @type Boolean
*/
function changeOrderBy(sourceList) {
  if (sourceList == null) return false;
  for (var j=sourceList.length-1;j>=0;j--) {
    if (sourceList.options[j].selected==true) {
      var text = sourceList.options[j].text;
      var value = sourceList.options[j].value;
      if (value.indexOf("-")!=-1) {
        value = value.substring(1);
        text = text.substring(2);
        text = "/\\" + text;
      } else {
        value = "-" + value;
        text = text.substring(2);
        text = "\\/" + text;
      }      
      sourceList.options[j].value = value;
      sourceList.options[j].text = text;
    }
  }
  return true;
}

/**
* Function Description
* @param {Object} sourceList A reference to the source list 
* @param {Object} destinationList A reference to the destination list
* @param {Boolean} withPrefix
* @param {Boolean} selectAll
* @returns Returns false if source or destination list is null.
* @type Boolean
*/
function addListOrderBy(sourceList, destinationList, withPrefix, selectAll) {
  if (sourceList==null || destinationList==null) return false;
  if (selectAll==null) selectAll=false;
  if (withPrefix==null) withPrefix=false;
  var sourceListLength = sourceList.length;
  var i = 0;
  for (var j=0;j<sourceListLength;j++) {
    if (selectAll || sourceList.options[i].selected==true) {
      var text = sourceList.options[i].text;
      var value = sourceList.options[i].value;
      if (withPrefix) {
        if (value.indexOf("-")!=-1) value = value.substring(1);
        if (text.indexOf("/\\")!=-1 || text.indexOf("\\/")!=-1) text = text.substring(2);
      } else {
        text = "/\\" + text;
      }
      destinationList.options[destinationList.length] = new Option(text, value);
      sourceList.options[i]=null;
    } else {
      i = i + 1;
    }
  }
  return true;
}

/**
* Moves elements from one list to another.
* @param {Object} sourceList A reference to the source list, where the items come from.
* @param {Object} destinationList A reference to the destination list, where the items will be copied.
* @param {Boolean} selectAll Sets if we want to copy all the items.
* @returns True is the process was correct, otherwise false.
*/
function addList(sourceList, destinationList, selectAll) {
  if (sourceList==null || destinationList==null) return false;
  if (selectAll==null) selectAll=false;
  var sourceListLength = sourceList.length;
  var i = 0;
  for (var j=0;j<sourceListLength;j++) {
    if (selectAll || sourceList.options[i].selected==true) {
      var text = sourceList.options[i].text;
      var value = sourceList.options[i].value;
      destinationList.options[destinationList.length] = new Option(text, value);
      sourceList.options[i]=null;
    } else {
      i = i + 1;
    }
  }
  return true;
}

/**
* Moves an element or elements selected from a list, incrementing the position
* @param {Object} list A reference to the list where the items are contained.
* @param {Object} incr A integer that sets the number of positions added to the items. If is a negative number, the elements will move up; and if is null a default value of 1 will be used.  
*/
function moveElementInList(list, incr) {
  if (list==null) return false;
  else if (list.length<2) return false;
  if (incr==null) incr=1;
  if (incr>0) {
    for (var i=list.length-2;i>=0;i--) {
      if (list.options[i].selected==true && ((i+incr)>=0 || (i+incr)<list.length)) {
        list.options[i].selected=false;
        var text = list.options[i+incr].text;
        var value = list.options[i+incr].value;
        list.options[i+incr].value = list.options[i].value;
        list.options[i+incr].text = list.options[i].text;
        list.options[i+incr].selected=true;
        list.options[i].value = value;
        list.options[i].text = text;
      }
    }
  } else {
    var total = list.length;
    for (var i=1;i<total;i++) {
      if (list.options[i].selected==true && ((i+incr)>=0 || (i+incr)<list.length)) {
        list.options[i].selected=false;
        var text = list.options[i+incr].text;
        var value = list.options[i+incr].value;
        list.options[i+incr].value = list.options[i].value;
        list.options[i+incr].text = list.options[i].text;
        list.options[i+incr].selected=true;
        list.options[i].value = value;
        list.options[i].text = text;
      }
    }
  }
  return true;
}

// Depecated since 2.50, use searchArray instead
function valorArray(dataArray, searchKey, valueIndex) {
  searchArray(dataArray, searchKey, valueIndex);
}

/**
* Search for a key and returns the value in the {intDevolverPosicion} index position of the Array.
* @param {Array} dataArray Array of elements 
* @param {String} searchKey Key to search for
* @param {Number} valueIndex Index position of the returning value.
* @returns The value of the given index position, or an empty string if not was found.
* @type String
*/
function searchArray(dataArray, searchKey, valueIndex)
{
  if (dataArray == null) return "";
  else if (searchKey==null) return "";
  if (valueIndex==null) valueIndex = 1;

  var total = dataArray.length;
  for (var i=0;i<total;i++) {
    if (dataArray[i][0] == searchKey) {
      return dataArray[i][valueIndex];
    }
  }
  return "";
}


/**
* Gets the value of a radio button element or array.
* @param {Object} radio A reference to the object where we want to get the value.
* @returns The value of the given radio, or null if was not found.
* @type String
*/
function radioValue(radio)
{
  if (!radio) return null;
  else if (!radio.length)
    return ((radio.checked)?radio.value:null);
  var total = radio.length;
  for (var i=0;i<total;i++)
  {
    if (radio[i].checked)
      return radio[i].value;
  }
  return null;
}

/**
* Checks or unchecks all the elements associated to the parameter.
* @param {Object} chk A reference to the check button that will be marked or unmarked.
* @param {Boolean} bolMark Set if we will check or uncheck the element.
* @returns False if the element was not found, otherwise true.
*/
function markAll(chk, bolMark)
{
  if (bolMark==null) bolMark = false;
  if (!chk) return false;
  else if (!chk.length) chk.checked = bolMark;
  else {
    var total = chk.length;
    for (var i=0;i<total;i++) chk[i].checked = bolMark;
  }
  return true;
}

/**
* Changes a combo's selected value based on an Array passed as parameter
* @param {Object} combo A reference to the combo that will be filled with the new values 
* @param {Array} dataArray Array that contains the data for the combo values
* @param {String} key Sets the array's key (index) that will be the value data of our combo
* @param {Boolean} withBlankOption Sets if we will add a blank value to the combo.
*/
function changeComboData(combo, dataArray, key, withBlankOption) {
  var i;
  var n=0;
  if (combo.options.length!=null) {
    for (i = combo.options.length;i>=0;i--)
      combo.options[i] = null;
  }

  if (withBlankOption)
    combo.options[n++] = new Option("", "");
  if (dataArray==null) return false;

  var total = dataArray.length;
  for (i=0;i<total;i++) {
    if (dataArray[i][0]==key)
      combo.options[n++] = new Option(dataArray[i][2], dataArray[i][1]);
  }
}

/**
* Removes all elements from a list
* @param {Object} field A reference to the list that holds all the elements
* @returns True if was processed correctly, otherwise false.
*/
function clearList(field) {
  if (field==null) return false;
  for (var i = field.options.length - 1;i>=0;i--) field.options[i] = null;
  return true;
}

/**
* Removes elements from list. Used when the elements are passed to another list.
* @param {Object} field A reference to the list where the elements are contained.
* @returns True is was processed correctly, otherwise false.
*/
function clearSelectedElements(field) {
  if (field==null) return false;
  for (var i = field.options.length - 1;i>=0;i--) {
    if (field.options[i].selected) field.options[i] = null;
  }
  return true;
}


/**
* Search for a key in a combo elements.
* @param {Object} combo A reference to the combo object.
* @param {String} searchKey The search key to look for in the comobo elements
* @returns True if was found, otherwise false.
*/
function comboContains(combo, searchKey) {
  if (combo==null || searchKey==null) return false;
  var total = combo.options.length;
  for (var i=0;i<total;i++) {
    if (combo.options[i].value == searchKey) return true;
  }
  return false;
}

/**
* Adds new elements to a list based on a data Array.
* @param {Object} destList A reference to the object where the elements will be added.
* @param {Array} arrayNewValues An array with the new data to add.
* @param True if the array was processed correctly, false on any problem.
*/
function addElementsToList(destList, arrayNewValues) {
  if (destList == null || arrayNewValues == null) return false;
  var i = destList.options.length;
  var total = arrayNewValues.length;
  for (var j=0; j<total;j++) {
      if (!comboContains(destList, arrayNewValues[j][0]))
        destList.options[i++] = new Option(arrayNewValues[j][1], arrayNewValues[j][0]);
  }
  return true;
}

/**
* Selects all the elements of a list or combo. Used on multiple selectors where all the values will be selected prior the form submition.
* @param {Object} field A reference to the combo that we want to select.
* @returns True is everything was right, otherwise false.
*/
function markCheckedAllElements(field) {
  if (field==null || field==null) return false;
  var total = field.options.length;
  for (var i=0;i<total;i++) {
    field.options[i].selected = true;
  }
  return true;
}

/**
* Handles the keypress event on textarea fields. Used to control the max length of a field.
* @param {Object} field A reference to the object in the page. Usually use the 'this' reference.
* @param {Number} maxLength Max length of the field.
* @param {Event} evt Event handling object.
* @returns True if is allowed to keep entering text, otherwise false.
*/
function handleFieldMaxLength(field, maxLength, evt) {
  if (field==null || !field) return false;
  if (field.value.length>=maxLength) {
    if (document.layers) keyCode.which=0;
    else {
      if (evt==null) evt = window.event;
      evt.keyCode=0;
      evt.returnValue = false;
      evt.cancelBubble = true 
    }
    showJSMessage(11);
    return false;
  }
  return true;
}

/**
* Function Description
* @param {Object} combo A reference the the combo object
* @param {String} key The element key to select
* @returns True if the element was selected, otherwise false.
* @type Boolean
*/
function selectCombo(combo, key) {
  if (!combo || combo==null) return false;
  var total = combo.length;
  for (var i=0;i<total;i++) {
    combo.options[i].selected = (combo.options[i].value == key);
  }
  return true;
}

/**
* Function Description
* Hides the button to show/hide the menu
* @param {String} id The ID of the element
*/
function hideMenuIcon(id) {
  var imgTag = document.getElementById(id);
  var aTag = getObjParent(imgTag);
  if (parent.frameMenu) {
    getFrame('main').isMenuBlock=true;
  }
  if (aTag.className.indexOf("Main_LeftTabsBar_ButtonLeft_hidden") == -1) {
    aTag.className = "Main_LeftTabsBar_ButtonLeft_hidden";
    imgTag.className = "Main_LeftTabsBar_ButtonLeft_Icon";
    disableAttributeWithFunction(aTag, 'obj', 'onclick');
  }
}

/**
* Function Description
* Shows the button to show/hide the menu
* @param {String} id The ID of the element
*/
function showMenuIcon(id) {
  var imgTag = document.getElementById(id);
  var aTag = getObjParent(imgTag);
  if (parent.frameMenu) {
    getFrame('main').isMenuBlock=false;
  }
  if (aTag.className.indexOf("Main_LeftTabsBar_ButtonLeft_hidden") != -1) {
    aTag.className = "Main_LeftTabsBar_ButtonLeft";
    imgTag.className = "Main_LeftTabsBar_ButtonLeft_Icon Main_LeftTabsBar_ButtonLeft_Icon_arrow_hide";
    enableAttributeWithFunction(aTag, 'obj', 'onclick');
    updateMenuIcon(id);
  }
}

/**
* Function Description
* Shows or hides a window in the application
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function updateMenuIcon(id) {
  if (!parent.frameMenu) {
    hideMenuIcon(id);
    return false;
  }
  else {
    var frameContainer = getFrame('main');
    var framesetMenu = frameContainer.document.getElementById("framesetMenu");
    if (!framesetMenu) return false;
    try {
      if (frameContainer.isMenuBlock==true) {
        menuHide(id, false);
        hideMenuIcon(id);
      } else {
        showMenuIcon(id);
      }
    } catch (ignored) {
    }
    try {
      if (frameContainer.isMenuHide==true && frameContainer.isMenuBlock==false) {
        changeClass(id, "_hide", "_show", true);
      } else {
        changeClass(id, "_show", "_hide", true);
      }
    } catch (ignored) {
    }
    return true;
  }
}

/**
* Function Description
* Shows or hides a window in the application
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuShowHide(id) {
  if (!parent.frameMenu) {
    window.open(baseFrameServlet, "_blank");
  } else {
    var frameContainer = getFrame('main');
    if (frameContainer.isMenuHide == true) {
      menuShow(id);
    } else {
      menuHide(id);
    }
    return true;
  }
}

/**
* Function Description
* Shows the menu in the application
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuShow(id, updateIcon) {
  if (typeof updateIcon === "undefined" || updateIcon === null || updateIcon === "null" || updateIcon === "") {
    updateIcon = true;
  }

  if (!parent.frameMenu) {
    window.open(baseFrameServlet, "_blank");
  } else {
    if (id==null) {
      id = 'buttonMenu';
    }
    var frameContainer = getFrame('main');
    var framesetMenu = frameContainer.document.getElementById("framesetMenu");
    if (!framesetMenu) {
      return false;
    }
    if (frameContainer.isRTL == true) {
      framesetMenu.cols = "*," + frameContainer.menuWidth + ",0%";
    } else {
      framesetMenu.cols = "0%," + frameContainer.menuWidth + ",*";
    }
    frameContainer.isMenuHide = false;
    try {
      putFocusOnMenu();
    } catch(e) {
    }
    if (updateIcon != false) {
      try {
        updateMenuIcon(id);
      } catch (e) {}
    }
    return true;
  }
}

/**
* Function Description
* Hides the menu in the application
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuHide(id, updateIcon) {
  if (typeof updateIcon === "undefined" || updateIcon === null || updateIcon === "null" || updateIcon === "") {
    updateIcon = true;
  }

  if (!parent.frameMenu) {
    window.open(baseFrameServlet, "_blank");
  } else {
    if (id==null) {
      id = 'buttonMenu';
    }
    var frameContainer = getFrame('main');
    var framesetMenu = frameContainer.document.getElementById("framesetMenu");
    if (!framesetMenu) {
      return false;
    }
    if (frameContainer.isRTL == true) {
      framesetMenu.cols = "*,0%,0%";
    } else {
      framesetMenu.cols = "0%,0%,*";
    }
    frameContainer.isMenuHide = true;
    try {
      putFocusOnWindow();
    } catch(e) {
    }
    if (updateIcon != false) {
      try {
        updateMenuIcon(id);
      } catch (e) {}
    }
    return true;
  }
}

/**
* Function Description
* Expands the whole content of the menu
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuExpand() {
  var appUrl = getAppUrl();
  putFocusOnMenu();
  submitCommandForm('ALL', false, null, appUrl + '/utility/VerticalMenu.html', 'frameMenu');
  return false;
}

/**
* Function Description
* Collapse the whole content of the menu
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuCollapse() {
  var appUrl = getAppUrl();
  putFocusOnMenu();
  submitCommandForm('DEFAULT', false, null, appUrl + '/utility/VerticalMenu.html', 'frameMenu');
  return false;
}

/**
* Function Description
* Collapse the whole content of the menu if the menu is expanded
* Expand the whole content of the menu if the menu is collapsed 
* @param {String} id The ID of the element
* @returns True if the operation was made correctly, false if not.
* @see #changeClass
*/
function menuExpandCollapse() {
  var menuExpandCollapse_status = getMenuExpandCollapse_status();
  if (menuExpandCollapse_status == 'expanded') {
    menuCollapse();
  } else if (menuExpandCollapse_status == 'collapsed') {
    menuExpand();
  }
  return false;
}

function getMenuExpandCollapse_status() {
//  alert(getFrame('frameMenu').getElementById('paramfieldDesplegar').getAttribute('id'));
  var menuExpandCollapse_status;
  if (getFrame('frameMenu').document.getElementById('paramfieldDesplegar')) menuExpandCollapse_status = 'collapsed';
  if (getFrame('frameMenu').document.getElementById('paramfieldContraer')) menuExpandCollapse_status = 'expanded';
  return menuExpandCollapse_status;
}

function menuUserOptions() {
  var appUrl = getAppUrl();
  openServletNewWindow('DEFAULT', false, appUrl + '/ad_forms/Role.html', 'ROLE', null, true, '460', '800');
  return true;
}

function menuQuit() {  
  var appUrl = getAppUrl();
  var target;
  try {
    if (parent.frameMenu) {
      target = "_parent";
    } else {
      target = "_self";
    }
  } catch (e) {
    target = "_self";
  }
  submitCommandForm('DEFAULT', false, null, appUrl + '/security/Logout.html', target);
  return false;
}

function menuAlerts() {
  var appUrl = getAppUrl();
  submitCommandForm('DEFAULT', true, getForm(), appUrl + '/ad_forms/AlertManagement.html', 'appFrame', false, true);
  return true;
}

function isVisibleElement(obj, appWindow) {
  if (appWindow == null || appWindow == 'null' || appWindow == '') {
    appWindow = getFrame('main');
  }
  var parentElement = obj;
  try {
    for(;;) {
      if (parentElement.style.display == 'none') {
        return false;
      } else if (parentElement == appWindow.document.getElementsByTagName('BODY')[0]) {
        break;
      }
      parentElement=parentElement.parentNode;
    }
  } catch(e) {
    return false;
  }
  return true;
}

function executeWindowButton(id,focus) {
  if (focus==null) focus=false;
  var appWindow = parent;
  if(parent.frames['appFrame'] || parent.frames['frameMenu']) {
    appWindow = parent.frames['appFrame'];
  } else if (parent.frames['superior']) {
    appWindow = parent.frames['superior'];
  } else if (parent.frames['frameSuperior']) {
    appWindow = parent.frames['frameSuperior'];
  } else if (parent.frames['frameButton']) {
    appWindow = parent.frames['frameButton'];
  } else if (parent.frames['mainframe']) {
    appWindow = parent.frames['mainframe'];
  }
  if (window.location.href.indexOf('ad_forms/Role.html') != -1) { //Exception for "Role" window
    appWindow = parent;
  }
  if (appWindow.document.getElementById(id) && isVisibleElement(appWindow.document.getElementById(id), appWindow)) {
    if (focus==true) appWindow.document.getElementById(id).focus();
    appWindow.document.getElementById(id).onclick();
    if (focus==true) putWindowElementFocus(focusedWindowElement);
  }
}

function executeMenuButton(id) {
  var appWindow = parent;
  if(parent.frames['appFrame'] || parent.frames['frameMenu']) {
    appWindow = parent.frames['frameMenu'];
  } 
  if (appWindow.document.getElementById(id) && isVisibleElement(appWindow.document.getElementById(id), appWindow)) {
    appWindow.document.getElementById(id).onclick();
  }
}

function getAppUrl() {
  var menuFrame = getFrame('frameMenu');
  var LayoutMDI = getFrame('LayoutMDI');
  var appUrl = null;
  if (typeof menuFrame.getAppUrlFromMenu === "function" || typeof menuFrame.getAppUrlFromMenu === "object") {  //"object" clause related to issue https://issues.openbravo.com/view.php?id=14756
    appUrl = menuFrame.getAppUrlFromMenu();
  } else if (LayoutMDI) {
    appUrl = LayoutMDI.OB.Application.contextUrl;
  }
  return appUrl;
}

/**
* Function Description
* Resize a window progressively
* @param {String} id ID of the window
* @param {Number} topSize 
* @param {Number} newSize The new size of the window
* @param {Boolean} grow If the window should 'grow' or set the new size immediately
*/
function progressiveHideMenu(id, topSize, newSize, grow) {
  var frame = parent.document;
  var object = frame.getElementById(id);
  if (newSize==null) {
    var sizes = object.cols.split(",");
    size = sizes[0];
    size = size.replace("%", "");
    size = size.replace("px", "");
    newSize = parseInt(size);
  }
  if (grow==null) grow = !(newSize>0);
  if (grow) {
    newSize += 5;
    if (newSize>=topSize) {
      object.cols = topSize + "%, *";
      return true;
    } else object.cols = newSize + "%, *";
  } else {
    newSize -= 5;
    if (newSize<=0) {
      object.cols = "0%, *";
      return true;
    } else object.cols = newSize + "%, *";
  }
  return setTimeout('progressiveHideMenu("' + id + '", ' + topSize + ', ' + newSize + ', ' + grow + ')', 100);
}

/**
* Function Description
* Change the class of an element on the page
* @param {String} id ID of the element
* @param {String} class1 The class to search for
* @param {String} class2 The class to replace
* @param {Boolean} forced 
* @returns False if the element was not found, otherwise True.
* @type Boolean
*/
function changeClass(id, class1, class2, forced) {
  if (forced==null) forced = false;
  var element = document.getElementById(id);
  if (!element) return false;
  if (element.className.indexOf(class1)!=-1) element.className = element.className.replace(class1, class2);
  else if (!forced && element.className.indexOf(class2)!=-1) element.className = element.className.replace(class2, class1);
  return true;
}

/**
* Function Description
* Change the readonly status of a textbox or a textarea
* @param {String} id ID of the element
* @param {Boolean} forced: it could be "true" or "false"
* @returns False if the element was not found, otherwise True.
* @type Boolean
*/
function changeReadOnly(id, forced) {
  if (forced==null) forced = false;
  var element = document.getElementById(id);
  if (!element) return false;
  if (!forced) {
    if (element.readOnly!=true) element.readOnly=true;
    else element.readOnly=false;
  } else {
//    forced = forced.toLowerCase();
    if (forced=="true") element.readOnly=true;
    else if (forced=="false") element.readOnly=false;
    else return false;
  }
  return true;
}


/**
* Function Description
* Gets a reference to a window
* @param {String} id ID of the element
* @returns A reference to the object, or null if the element was not found.
* @type Object
*/
function getReference(id) {
  if (document.getElementById) return document.getElementById(id);
  else if (document.all) return document.all[id];
  else if (document.layers) return document.layers[id];
  else return null;
}

/**
* Function Description
* Gets the style attribute of an element
* @param {String} id ID of the element
* @returns A reference to the style attribyte of the element or null if the element was not found.
* @type Object
* @see #getReference
*/
function getStyle(id) {
  var ref = getReference(id);
  if (ref==null || !ref) return null;
  return ((document.layers) ? ref : ref.style);
}

/**
* Returns a "modified version" of a name
* @param {String} name A string to modify
* @returns The string modified
*/
function idName(name) {
  return (name.substring(0,9) + name.substring(10));
}

/**
* Returns the position of a element in a form
* @param {Form} form A reference to the form in the page.
* @param {Object} name Name of the element to search.
* @returns If was found returns the position of the element, if not returns null.
*/
function findElementPosition(form, name) {
  var total = form.length;
  for (var i=0;i<total;i++) {
    if (form.elements[i].name==name) return i;
  }
  return null;
}

/**
* Function Description
* @param {Form} form A reference to the form in the page
* @param {Object} field The currect field selected
* @returns The position of the element
* @type Number
* @see #findElementPosition 
* @see #recordSelectExplicit
*/
function deselectActual(form, field) {
  if (field==null || field.value==null || field.value=="") return null;
  var i=findElementPosition(form, "inpRecordW" + field.value);
  if (i==null) return null;
  recordSelectExplicit("inpRecord" + field.value, false);
  field.value="";
  return i;
}

/**
* Returns the first element on a form
* @param {Form} A reference to the form in the page
* @returns The first element on a form
* @type Object
*/
function findFirstElement(form) {
  if (form==null) return null;
  var n=null;
  var total = form.length;
  for (var i=0;i<total;i++) {
    if (form.elements[i].name.indexOf("inpRecordW")==0) {
      n=i;
      break;
    }
  }
  return n;
}

/**
* Returns the last element on a form
* @param {Form} A reference to the form in the page
* @returns The last element on a form
* @type Object
*/
function findLastElement(form) {
  if (form==null) return null;
  var n=null;
  for (var i=form.length-1;i>=0;i--) {
    if (form.elements[i].name.indexOf("inpRecordW")==0) {
      n=i;
      break;
    }
  }
  return n;
}

/**
* Selects the next element on a form
* @param {Form} form A reference to the form in the page
* @param {Object} field The current field selected
* @returns True
* @type Boolean 
* @see #deselectActual 
* @see #findFirstElement 
* @see #findLastElement 
* @see #recordSelectExplicit 
*/
function nextElement(form, field) {
  var i=deselectActual(form, field);
  if (i==null) {
    i=findFirstElement(form);
    if (i==null) return;
  } else if (i<findLastElement(form)) i++;
  field.value = form.elements[i].name.substring(10);
  recordSelectExplicit("inpRecord" + form.elements[i].name.substring(10) , true);
  form.elements[i].focus();
  return true;
}

/**
* Selects the previous element on a form
* @param {Form} form A reference to the form in the page
* @param {Object} field The current field selected
* @returns True
* @type Boolean 
* @see #deselectActual 
* @see #findFirstElement 
* @see #recordSelectExplicit 
*/
function previousElement(form, field) {
  var i=deselectActual(form, field);
  var minor = findFirstElement(form);
  if (minor==null) return;
  else if (i==null) {
    i=minor;
    if (i==null) return;
  } if (i>minor) i--;
  field.value = form.elements[i].name.substring(10);
  recordSelectExplicit("inpRecord" + form.elements[i].name.substring(10) , true);
  form.elements[i].focus();
  return true;
}

/**
* Selects the first element on a form
* @param {Form} form A reference to the form in the page
* @param {Object} field The current field selected
* @returns True
* @type Boolean
* @see #deselectActual 
* @see #findFirstElement 
* @see #recordSelectExplicit
*/
function firstElement(form, field) {
  var i=deselectActual(form, field);
  i=findFirstElement(form);
  if (i==null) return;
  field.value = form.elements[i].name.substring(10);
  recordSelectExplicit("inpRecord" + form.elements[i].name.substring(10) , true);
  form.elements[i].focus();
  return true;
}

/**
* Selects the last element on a form
* @param {Form} form A reference to the form in the page
* @param {Object} field The current field selected
* @returns True
* @type Boolean
* @see #deselectActual 
* @see #findLastElement 
* @see #recordSelectExplicit
*/
function lastElement(form, field) {
  var i=deselectActual(form, field);
  i=findLastElement(form);
  if (i==null) return;
  field.value = form.elements[i].name.substring(10);
  recordSelectExplicit("inpRecord" + form.elements[i].name.substring(10) , true);
  form.elements[i].focus();
  return true;
}

/**
* Highlight an element on the page
* @param {String} name The id of the element on the page 
* @param {Boolean} highlight Sets if we want to highlight an element. 
* @returns The highlighted value
* @type Boolean
* @see #getStyle
*/
function recordSelectExplicit(name, highlight) {
  var obj = getStyle(name);
  if (obj==null) return false;
  if (document.layers) {
    if (highlight) obj.bgColor=gColorSelected;
    else obj.bgColor=gWhiteColor;
  } else {
    if (highlight) obj.backgroundColor = gColorSelected;
    else obj.backgroundColor=gWhiteColor;
  }
  return highlight;
}

/**
* Select an element from a set (array) of radio buttons.
* @param {Object} radio A reference to the radio button
* @param {String} Value The value to select
* @returns * @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function selectRadioButton(radio, Value) {
  if (!radio) return false;
  else if (!radio.length) radio.checked=true;
  else {
    var total = radio.length;
    for (var i=0;i<total;i++) radio[i].checked = (radio[i].value==Value);
  }
  return true;
}
/**
* Selects a value from a set of radio buttons.
* @param {Object} Reference to the radio(s) element(s).
* @param {String} Value of the element that we want to check.
* @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function selectCheckbox(obj, Value) {

  if (!obj) return false;
  else {
    obj.checked = (obj.value==Value);
  }
  return true;
}


/**
* Sets the message to display.  
* @param {Form} form A reference to the form
* @param {String} ElementName The name of the element (INFO, ERROR, SUCCESS, WARNING, EXECUTE, DISPLAY, HIDE, CURSOR_FIELD).
* @param {Value} The value to set
* @returns True if the value was set, otherwise false.
* @type Boolean
* @see #setValues_MessageBox
*/
function formElementValue(form, ElementName, Value) {
  var bolReadOnly=false;
  var onChangeFunction = "";
  if (form==null) {
    form=document.forms[0];
    if (form==null) return false;
  } else if (ElementName==null) return false;
  if (ElementName=="MESSAGE") {
    initialize_MessageBox("messageBoxID");
    try {
      if (Value!=null && Value!="") setValues_MessageBox('messageBoxID', "INFO", "", Value);
    } catch (err) {
      alert(Value);
    }
  } else if (ElementName=="ERROR" || ElementName=="SUCCESS" || ElementName=="WARNING" || ElementName=="INFO") {
    try {
      setValues_MessageBox('messageBoxID', ElementName, "", Value);
    } catch (err) {
      alert(Value);
    }
  } else if (ElementName=="EXECUTE") {
    eval(Value);
  } else if (ElementName=="DISPLAY") {
    displayLogicElement(Value, true);
  } else if (ElementName=="HIDE") {
    displayLogicElement(Value, false);
  } else if (ElementName=="CURSOR_FIELD") {
    var obj = eval("document." + form.name + "." + Value + ";");
    if (obj==null || !obj || !obj.type || obj.type.toUpperCase()=="HIDDEN") return false;
    setWindowElementFocus(obj);
    if (obj.type.toUpperCase().indexOf("SELECT")==-1) obj.select();
    //document.focus();
  } else {
    if (ElementName.toUpperCase().indexOf("_BTN")!=-1) {
      if (Value==null || Value=="null") Value="";
      layer(ElementName, Value, true);
      return true;
    } else if (ElementName.toUpperCase().indexOf("_LBL")!=-1) {
      if (Value==null || Value=="null") Value="";
      document.getElementById(ElementName).innerHTML = Value;
      return true;
    }
    var obj = eval("document." + form.name + "." + ElementName + ";");
    if (obj==null || !obj || !obj.type) return false;
    if (obj.getAttribute("readonly")=="true" || obj.readOnly || (obj.getAttribute("readonly")=="" && navigator.userAgent.toUpperCase().indexOf("MSIE") == -1)) bolReadOnly=true;
    if (bolReadOnly) {
      if (obj.getAttribute("onChange")) {
        onChangeFunction = obj.getAttribute("onChange").toString();
        onChangeFunction = onChangeFunction.replace("function anonymous()\n","");
        onChangeFunction = onChangeFunction.replace("function onchange()\n","");
        onChangeFunction = onChangeFunction.replace("{\n","");
        onChangeFunction = onChangeFunction.replace("\n}","");
      } else {
        onChangeFunction = "";
      }
      obj.setAttribute("onChange", "");
      obj.readOnly = false;
    }
    if (obj.type.toUpperCase().indexOf("SELECT")!=-1) {
      if (Value!=null && typeof Value!="object") {
        var total = obj.length;
        var index = -1;
        var hasMultiSelect = false;
        var selectedOption = false;
        if ((Value==null || Value=="") && total>0) Value = obj.options[0].value;
        for (var i=0;i<total;i++) {
          selectedOption = (obj.options[i].value == Value);
          obj.options[i].selected = selectedOption;
          if (selectedOption) {
            if (index!=-1) hasMultiSelect = true;
            index = i;
          }
        }
        if (!hasMultiSelect) obj.selectedIndex = index;
      } else Value = fillCombo(obj, Value, true, ((obj.className.toUpperCase().indexOf("REQUIRED")!=-1) || obj.className.toUpperCase().indexOf("KEY")!=-1));
    } else if (obj.type.toUpperCase().indexOf("CHECKBOX")!=-1) {
      selectCheckbox(obj, Value);
    } else if (obj.type.toUpperCase().indexOf("RADIO")!=-1 || obj.type.toUpperCase().indexOf("CHECK")!=-1) {
      selectRadioButton(obj, Value);
    } else {

      if (Value==null || Value=="null") {
        Value="";
      }

      if (typeof Value!="object") {

          var decSeparator = getGlobalDecSeparator();
          var groupSeparator = getGlobalGroupSeparator();
          var groupInterval = getGlobalGroupInterval();

          var outputformat = obj.getAttribute("outputformat");

          if(outputformat != null || typeof Value === "number") {
            maskNumeric = formatNameToMask(outputformat);
            var formattedValue = returnCalcToFormatted(Value, maskNumeric, decSeparator, groupSeparator, groupInterval);
            if (focusedWindowElement !== obj || returnFormattedToCalc(formattedValue, decSeparator, groupSeparator) !== returnFormattedToCalc(obj.value, decSeparator, groupSeparator)) {
              obj.value = formattedValue;
            }
          } else {
            obj.value = Value;
          }
      } else {
      //if (obj.className.toUpperCase().indexOf("REQUIRED")!=-1 || obj.className.toUpperCase().indexOf("KEY")!=-1 || obj.className.toUpperCase().indexOf("READONLY")!=-1)
        obj.value = selectDefaultValueFromArray(Value, true);
      }
    }
    if (bolReadOnly && onChangeFunction) {
      var i = onChangeFunction.toString().indexOf("selectCombo(this,");
      var search = "\"";
      if (i!=-1) {
        var first = onChangeFunction.toString().indexOf(search, i+1);
        if (first==-1) {
          search = "'";
          first = onChangeFunction.toString().indexOf(search, i+1);
        }
        if (first!=-1) {
          var end = onChangeFunction.toString().indexOf(search, first+1);
          if (end!=-1) {
            onChangeFunction = onChangeFunction.toString().substring(0, first+1) + Value + onChangeFunction.toString().substring(end);
            onChangeFunction = onChangeFunction.toString().replace("function anonymous()", "");
          }
        }
        obj.setAttribute("onChange", onChangeFunction);
      } else {
        obj.setAttribute("onChange", onChangeFunction);
        //obj.onchange = function anonymous() {selectCombo(this, Value);return true;};
      }
      obj.readOnly = true;
    }
  }
  return true;
}

/**
 * Returns a reference to the frame DOM element
 * @param frameName Name of the frame to get the reference
 * @returns null if not find it, or a reference to the frame DOM element
 */
function getFrame(frameName) {
  var targetFrame;
  if (frameName == 'main') {
    if (mainFrame_windowObj !== "") {  //to avoid go inside the 'main' frame search logic several times in the same html
      targetFrame = mainFrame_windowObj;
    } else {
      var success = false;
      try {  //some typical cases to avoid go into the logic loop. try-catch to avoid security issues when executing Openbravo inside a frame or iframe
        if (parent.frameMenu) {
          targetFrame = window.parent;
          success = true;
        } else if (top.opener.parent.frameMenu) {
          targetFrame = window.top.opener.parent;
          success = true;
        } else if (top.opener.top.opener.parent.frameMenu) {
          targetFrame = window.top.opener.top.opener.parent;
          success = true;
        }
      } catch (e) {
        success = false;
      }

      if (success == false) {
        try {  //some typical cases to avoid go into the logic loop. try-catch to avoid security issues when executing Openbravo inside a frame or iframe
          if (opener && opener.parent && opener.parent.frameMenu) {
            targetFrame = window.opener.parent;
            success = true;
          } else if (parent && parent.opener && parent.opener.parent && parent.opener.parent.frameMenu) {
            targetFrame = window.parent.opener.parent;
            success = true;
          } else  if (opener && opener.opener && opener.opener.parent && opener.opener.parent.frameMenu) {
            targetFrame = window.opener.opener.parent;
            success = true;
          } else  if (opener && opener.opener && opener.opener.opener && opener.opener.opener.parent && opener.opener.opener.parent.frameMenu) {
            targetFrame = window.opener.opener.opener.parent;
            success = true;
          } else  if (opener && opener.opener && opener.opener.opener && opener.opener.opener.opener && opener.opener.opener.opener.parent && opener.opener.opener.opener.parent.frameMenu) {
            targetFrame = window.opener.opener.opener.opener.parent;
            success = true;
          }
        } catch (e) {
          success = false;
        }
      }

      if (success == false) {
        targetFrame = 'window';
        var targetFrame_parent = 'window.parent';
        var targetFrame_opener = 'window.opener';
        var securityEscape = 0;
        var securityEscapeLimit = 50;

        try {  //try-catch to avoid security issues when executing Openbravo inside a frame or iframe
          while (eval(targetFrame) !== eval(targetFrame_opener)) {
            while (eval(targetFrame) !== eval(targetFrame_parent)) {
              if (eval(targetFrame).document.getElementById('paramFrameMenuLoading') || securityEscape > securityEscapeLimit) { //paramFrameMenuLoading is an existing Login_FS.html ID to check if we are aiming at this html
                success = true;
                break;
              }
              targetFrame = targetFrame + '.parent';
              targetFrame_parent = targetFrame + '.parent';
              securityEscape = securityEscape + 1;
            }
            if (eval(targetFrame).document.getElementById('paramFrameMenuLoading') || securityEscape > securityEscapeLimit) { //paramFrameMenuLoading is an existing Login_FS.html ID to check if we are aiming at this html
              success = true;
              break;
            }
            targetFrame = targetFrame + '.opener';
            targetFrame_opener = targetFrame + '.opener';
            securityEscape = securityEscape + 1;
            if (typeof eval(targetFrame) === 'undefined' || eval(targetFrame) === null || eval(targetFrame) === 'null' || eval(targetFrame) === '') {
              break;
            }
          }
        } catch (e) {
        }
        targetFrame = eval(targetFrame);
      }
      if (success == false) {
        targetFrame = null;
      }
      mainFrame_windowObj = targetFrame;
    }
  } else if (frameName === 'mainParent') {
    var main = getFrame('main');
    var check = true;
    try {
      var dummy = main.parent.document;
    } catch (e) {
      check = false;
    }

    if (check) {
      if (main.document === main.parent.document) {
        check = false;
      }
    }

    if (check) {
      targetFrame = main.parent;
    } else {
      targetFrame = null;
    }
  } else if (frameName === 'LayoutMDI') {
    if (LayoutMDI_windowObj !== "") {  //to avoid go inside the 'LayoutMDI' frame search logic several times in the same html
      targetFrame = LayoutMDI_windowObj;
    } else {
      var mainParent = getFrame('mainParent');
      targetFrame = null;
      if (mainParent !== null) {
        if (LayoutMDICheck(mainParent)) {
          targetFrame = mainParent;
        } else {
          targetFrame = null;
        }
      } else {
        if (targetFrame === null) {  // For case of classic ob popups opened from a MDI tab
          try {
            targetFrame = top.opener;
            while (targetFrame !== null && !LayoutMDICheck(targetFrame)) {
              targetFrame = targetFrame.top.opener;
            }
          } catch (e) {
            targetFrame = null;
          }
        }
        if (targetFrame === null) {  // For case of classic ob windows/popups opened inside a MDI modal popup
          try {
            targetFrame = parent;
            while (targetFrame !== null && targetFrame !== targetFrame.parent && !LayoutMDICheck(targetFrame)) {
              targetFrame = targetFrame.parent;
            }
          } catch (e) {
            targetFrame = null;
          }
        }
        if (!LayoutMDICheck(targetFrame)) {
          targetFrame = null;
        }
      }
      LayoutMDI_windowObj = targetFrame;
    }
  } else {
    if (getFrame('main') && getFrame('main').frames[frameName]) {
      targetFrame = getFrame('main').frames[frameName];
    } else {
      targetFrame = null;
    }
  }
  return targetFrame;
}

/**
* Gets Openbravo_ERP css javascript reference
*/
function getOpenbravoERPStyleSheet() {
  var stylesheet;

  for (var i=0; i < document.styleSheets.length; i++) {
    if (document.styleSheets[i].href &&
        document.styleSheets[i].href.indexOf("print")===-1 &&
        document.styleSheets[i].href.indexOf("Openbravo_ERP")!==-1
        ) {
      stylesheet = document.styleSheets[i];
    }
  }

  return stylesheet;
}

/**
* Adds a style definition to Openbravo ERP main CSS in last position
* @param {String} selector
* @param {declaration} declaration
*/
function addStyleRule(selector, declaration) {
  var stylesheet = getOpenbravoERPStyleSheet();

  if (typeof stylesheet === "object") {
    if (navigator.userAgent.toUpperCase().indexOf("MSIE") !== -1) {
      stylesheet.addRule(selector, declaration);
    } else {
      stylesheet.insertRule(selector + ' { ' + declaration + ' }', stylesheet.cssRules.length);
    }
  }
}

/**
* Removes a style definition at given position in Openbravo ERP main CSS
* @param {Ingeter} selectorIndex
*/
function removeStyleRule(selectorIndex) {
  var stylesheet = getOpenbravoERPStyleSheet();

  if (typeof stylesheet === "object") {
    if (navigator.userAgent.toUpperCase().indexOf("MSIE") !== -1) {
      stylesheet.removeRule(selectorIndex);
    } else {
      stylesheet.deleteRule(selectorIndex);
    }
  }
}

/**
* Returns an array with the desired selector positions
* @param {String} selector
* @param {declaration} declaration
*/
function getStyleRulePosition(selector) {
  var stylesheet = getOpenbravoERPStyleSheet();
  var position = new Array();
  var i;

 if (typeof stylesheet === "object") {
    if (navigator.userAgent.toUpperCase().indexOf("MSIE") !== -1) {
      for (i=0; i < stylesheet.rules.length; i++) {
        if (stylesheet.rules[i].selectorText.toLowerCase() === selector.toLowerCase()) {
          position.push(i);
        }
      }
    } else {
      for (i=0; i < stylesheet.cssRules.length; i++) {
        if (typeof stylesheet.cssRules[i].selectorText !== "undefined" && stylesheet.cssRules[i].selectorText.toLowerCase() === selector.toLowerCase()) {
          position.push(i);
        }
      }
    }
  }
  return position;
}

/**
* Small changes in a 2.50 skin to proper view in a MDI tab
*/
function adaptSkinToMDIEnvironment() {
  if (isWindowInMDITab) {
    addStyleRule(".Main_NavBar_bg_left", "height: 1px;");
    addStyleRule(".Main_NavBar_bg_right", "height: 1px;");
    addStyleRule(".Main_ContentPane_LeftTabsBar", "display: none;");
    addStyleRule(".Main_ContentPane_NavBar", "height: 0px;");
    addStyleRule(".Main_ContentPane_NavBar#tdtopNavButtons", "display: none;");
    addStyleRule(".tabTitle_background", "display: none;");
  } else if (isWindowInMDIPopup) {
    addStyleRule(".Popup_ContentPane_NavBar", "display: none;");
    addStyleRule(".Popup_ContentPane_SeparatorBar", "display: none;");
    addStyleRule(".Popup_ContentPane_CircleLogo", "display: none;");
  }
}

/**
* Sets the class attribute of an element
* @param {String} id The ID of the element
* @param {String} selectClass The class to be setted.
* @returns null if the element was not found.
*/
function setClass(id, selectClass) {
  var obj = getReference(id);
  if (obj==null) return null;
  obj.className = selectClass;
}

/**
* Returns the class attibute of an element
* @param {String} id ID of the html element
* @param {String} previousClass Default class to be returned if an error ocurred.
* @returns The class if the element was found, otherwise returns the text of the previousClass parameter. 
* @type String
*/
function getObjectClass(id, previousClass) {
  var obj = getReference(id);
  if (obj==null) return previousClass;
  return(obj.className);
}

/**
* Function Description
* @param {Form} form A reference to the form.
* @param {String} ElementName The name of the element.
* @param {String} callout The CallOut associated.
* @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function formElementEvent(form, ElementName, calloutName) {
  if (form==null) form=document.forms[0].name;
  else if (ElementName==null) return false;
  var isReload=false;
  if (ElementName!="MESSAGE" && ElementName!="CURSOR_FIELD" && ElementName!="EXECUTE" && ElementName!="DISPLAY" && ElementName!="HIDE" && ElementName.indexOf("_BTN")==-1) {
    var obj = eval("document." + form + "." + ElementName + ";");
    if (obj==null || !obj || !obj.type) return false;
    calloutProcessedObj = obj;
    if (obj.type.toUpperCase().indexOf("RADIO")!=-1) {
      if (obj.onclick!=null && obj.onclick.toString().indexOf(calloutName)==-1) {
        if (obj.onclick.toString().indexOf("callout")!=-1 || obj.onclick.toString().indexOf("reload")!=-1) isReload=true;
        obj.onclick();
      }
    } else {
      var bolReadOnly = false;
      if (obj.onchange!=null && obj.onchange.toString().indexOf(calloutName)==-1) {
        if (obj.onchange.toString().indexOf("callout")!=-1 || obj.onchange.toString().indexOf("reload")!=-1) isReload=true;
        if (obj.getAttribute("readonly")=="true" || obj.readOnly==true || (obj.getAttribute("readonly")=="" && navigator.userAgent.toUpperCase().indexOf("MSIE") == -1)) {
          bolReadOnly=true;
          obj.readOnly = false;
        }

        if (obj.className.indexOf("Combo")!=-1) {
          if (obj.getAttribute("onChange")) {
            var onchange_combo = obj.getAttribute("onChange").toString();
            onchange_combo = onchange_combo.replace("function anonymous()\n","");
            onchange_combo = onchange_combo.replace("function onchange()\n","");
            onchange_combo = onchange_combo.replace("{\n","");
            onchange_combo = onchange_combo.replace("\n}","");
          } else {
            var onchange_combo = "";
          }
          if (onchange_combo.indexOf("selectCombo")!=-1) {
            onchange_combo = onchange_combo.substring(0,onchange_combo.indexOf("selectCombo"))+onchange_combo.substring(onchange_combo.indexOf(";",onchange_combo.indexOf("selectCombo"))+1, onchange_combo.length);
            var onchange_combo2 = onchange_combo;
            onchange_combo = onchange_combo.replace("return true; tmp_water_mark; ","");
            onchange_combo = onchange_combo.substring(0,onchange_combo.indexOf("return"))+onchange_combo.substring(onchange_combo.indexOf(";",onchange_combo.indexOf("return"))+1, onchange_combo.length);
            onchange_combo = onchange_combo.replace("(this)","(obj)");
            onchange_combo = onchange_combo.replace("(this,","(obj,");
            onchange_combo = onchange_combo.replace("(this ","(obj ");
            onchange_combo = onchange_combo.replace(",this)",",obj)");
            onchange_combo = onchange_combo.replace(" this)"," obj)");
            eval(onchange_combo);
            obj.setAttribute("onChange", "selectCombo(this, '" + obj.value + "');" + onchange_combo2);
          } else {
            obj.onchange();
          }
        } else {
          if (obj.getAttribute('onchange') != '' && obj.getAttribute('onchange') != null && obj.getAttribute('onchange') != 'null') {
            obj.onchange();
          }
        }
        if (bolReadOnly) obj.readOnly = true;
      }
    }
    calloutProcessedObj = null;
  }
  return (isReload);
}

/**
* Struct to be used on the fillElementsFromArray function
* @param {Form} frm A reference to the form
* @param {String} name 
* @param {String} callout The CallOut to be associated
* @see #fillElementsFromArray
*/
function fillElements(frm, name, callout) {
  this.formName = frm;
  this.name = name;
  this.callout = callout;
}

/**
* Set the value of the variable gWaitingCallOut
* @param {state} boolean: true to set the variable to true, false to set the variable to false.
* @returns True if everything goes right, otherwise false.
*/
function setGWaitingCallOut(state) {
  if (state==true) {
    try {
      setCalloutProcessing(true);
    }
    catch (e) {}
    gWaitingCallOut=true;
  } else if (state==false) {
    try {
      setCalloutProcessing(false);
    }
    catch (e) {}
    gWaitingCallOut=false;
  } else {
    return false;
  }
  return true;
}


/**
* Function Description
* @param {Array} arrElements The array of elements
* @param {String} calloutName The CallOut to be associated.
* @param {Form} form A reference to the form, if is null, the first form in the page will be used.
* @returns True if everything goes right, otherwise false.
* @type Boolean
* @see #formElementEvent
*/
function fillElementsFromArray(arrElements, calloutName, form) {
  if (arrElements==null && arrGeneralChange==null) return false;
  if (form==null || !form) form=document.forms[0];
  if (arrElements!=null) {
    var total = arrElements.length;
    for (var x=0;x<total;x++) {
      formElementValue(form, arrElements[x][0], arrElements[x][1]);
    }
  }
  if (arrGeneralChange==null) arrGeneralChange=new Array();
  if (arrElements!=null) {
    var n=arrGeneralChange.length;
    var total = arrElements.length;
    for (var x=0;x<total;x++) {
        arrGeneralChange[x+n] = new fillElements(form.name , arrElements[x][0], calloutName);
    }
  }
  while (arrGeneralChange!=null && arrGeneralChange.length>0) {
    var obj = arrGeneralChange[0].formName;
    var name = arrGeneralChange[0].name;
    var callout = arrGeneralChange[0].callout;
    {
      if (arrGeneralChange==null || arrGeneralChange.length==0) return true;
      var arrDataNew = new Array();
      var total = arrGeneralChange.length;
      for (var i=1;i<total;i++) {
        arrDataNew[i-1] = new fillElements(arrGeneralChange[i].formName, arrGeneralChange[i].name, arrGeneralChange[i].callout);
      }
      arrGeneralChange=null;
      arrGeneralChange = new Array();
      total = arrDataNew.length;
      for (var i=0;i<total;i++) {
        arrGeneralChange[i] = new fillElements(arrDataNew[i].formName, arrDataNew[i].name, arrDataNew[i].callout);
      }
    }
    if (formElementEvent(obj, name, callout)) return true;
  }
  /*try {
    document.focus();
  } catch (e) {}*/
  return true;
}

/**
* Returns the values of a selected field in a GET format method
* @param {String} name The name of the command
* @param {Object} field A reference to the field where the values will be extracted.
* @returns A string with the extracted values in the form name=value
* @type String
*/
function inputValueForms(name, field) {
  var result = "";
  if (field==null || !field) return "";
  if (!field.type && field.length>1) field = field[0];
  if (field.type) {
    if (field.type.toUpperCase().indexOf("SELECT")!=-1) {
      if (field.selectedIndex==-1) return "";
      else {
        var length = field.options.length;
        for (var fieldsCount=0;fieldsCount<length;fieldsCount++) {
          if (field.options[fieldsCount].selected) {
            if (result!="") result += "&";
            result += name + "=" + encodeURIComponent(field.options[fieldsCount].value);
          }
        }
        return result;
      }
    } else if (field.type.toUpperCase().indexOf("RADIO")!=-1 || field.type.toUpperCase().indexOf("CHECK")!=-1) {
      if (!field.length) {
        if (field.checked) return (name + "=" + encodeURIComponent(field.value));
        else return "";
      } else {
        var total = field.length;
        for (var i=0;i<total;i++) {
          if (field[i].checked) {
            if (result!="") result += "&";
            result += name + "=" + encodeURIComponent(field[i].value);
          }
        }
        return result;
      }
    } else return name + "=" + encodeURIComponent(field.value);
  }

  return "";
}

/**
* Set the focus on the specified field.
* @param {Object} field A reference to the field where the focus will be set.
* @returns An empty string
* @type String
*/
function setFocus(field) {
  if (field==null || !field) return "";
  if (!field.type && field.length>1) field = field[0];
  try {
    field.focus();
  } catch (ignored) {}

  return "";
}

/**
* Gets the value of a field.
* @param {Object} field A reference to the object where the value will be extracted
* @returns An empty string if the field does not exist, or the field's value.
* @type String
*/
function inputValue(field) {
  if (field==null || !field) return "";
  if (!field.type && field.length>1) field = field[0];
  if (field.type) {
    if (field.type.toUpperCase().indexOf("SELECT")!=-1) {
      if (field.selectedIndex==-1) return "";
      else return field.options[field.selectedIndex].value;
    } else if (field.type.toUpperCase().indexOf("RADIO")!=-1 || field.type.toUpperCase().indexOf("CHECK")!=-1) {
      if (!field.length)
      return ((field.checked)?field.value:"N");
      var total = field.length;
      for (var i=0;i<total;i++) {
        if (field[i].checked) return field[i].value;
      }
      return "N";
    } else return field.value;
  }

  return "";
}

/**
* Sets a value to a field
* @param {Object} field A reference to the field
* @param {String} myvalue The value to set.
* @returns True if the value was set, otherwise false.
* @type Boolean
*/
function setInputValue(field, myvalue) {
  if (field==null || field=="") return false;
  var obj = document.forms[0].elements[field];
  if (obj==null) return false;
  if (obj.length>1) {
    var total = obj.length;
    for (var i=0;i<total;i++) obj[i].value = myvalue;
  } else obj.value = myvalue;
  return true;
}

/**
* Shows and hides an element on the screen. Implements the display logic in the window.
* @param {Object} id A reference to the object that will be handled.
* @param {Boolean} display Set if we want to show or hide the element
* @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function displayLogicElement(id, display) {
  var obj = getStyle(id);
  if (obj==null) return false;
  if (id.indexOf("_td")!=-1) {
    obj = getReference(id);
    if (display) obj.className = obj.className.replace("_Nothing","");
    else {
      obj.className = obj.className.replace("_Nothing","");
      obj.className = obj.className + "_Nothing";
    }
  } else {
    if (display) obj.display="";
    else obj.display="none";
  }
  return true;
}

/**
* Sets elements as readonly or not depending on the logic
* @param {Object} id A reference to the object that will be handled.
* @param {Boolean} readonly set readonly or not depending on this field
* @returns True if everything goes right, otherwise false.
* @type Boolean
*/
function readOnlyLogicElement(id, readonly) {
  obj = getStyle(id);
  if (obj==null) return false;

  obj = getReference(id);
  className = obj.className;
  var onchange_combo = null;
  var newOnChange_combo = null;
 
  if (readonly) {
    obj.className = className.replace("ReadOnly","");
    obj.readOnly = true;
    if (obj.setReadOnly) {
    	obj.setReadOnly(true);
    }
    if (obj.getAttribute('type') == "checkbox") {
      var onclickTextA = getObjAttribute(obj, 'onclick');
      var checkPatternA = "^[return false;]";
      var checkRegExpA = new RegExp(checkPatternA);
      if (!onclickTextA.match(checkRegExpA)) {
        onclickTextA = 'return false;' + onclickTextA;
      }
      setObjAttribute(obj, 'onclick', onclickTextA);
    }
    if (className.indexOf("Combo ")!=-1 || className.indexOf("ComboKey ")!=-1) {
      if (className.indexOf("Combo ")!=-1) obj.className = className.replace("Combo","ComboReadOnly");
      else if (className.indexOf("ComboKey ")!=-1) obj.className = className.replace("ComboKey","ComboKeyReadOnly");
      disableAttributeWithFunction(obj, 'obj', 'onChange');
      if (obj.getAttribute("onChange")) {
        onchange_combo = getObjAttribute(obj, 'onChange');
      } else {
        onchange_combo = "";
      }
      newOnChange_combo = "selectCombo(this, '"+obj.value+"');" + onchange_combo;
      setObjAttribute(obj, 'onChange', newOnChange_combo);
    }
    if (className.indexOf("LabelText ")!=-1)
      obj.className = className.replace("LabelText","LabelTextReadOnly");
    if ((className.indexOf("TextBox_")!=-1)||(className.indexOf("TextArea_")!=-1)) {
      if (className.indexOf("readonly")==-1) changeClass(id,'readonly ', '');
      disableFieldButton(getAssociatedFieldButton(obj, 'window'));
    }
  } else { //not readonly
    obj.className = obj.className.replace("ReadOnly","");
    obj.className = obj.className.replace("readonly","");
    setObjAttribute(obj, 'readOnly', "false");
    obj.readOnly = false;
    obj.removeAttribute('readOnly'); // To avoid in Chrome the dropdown arrow be light-greyed although it is not readonly anymore
                                     // The previous statements are already needed since lower IE versions has problems handling "removeAttribute" function
    if (obj.setReadOnly) {
      obj.setReadOnly(false);
    }
    if (obj.getAttribute('type') == "checkbox") {
      var onclickTextB = getObjAttribute(obj, 'onclick');
      var checkPatternB = "^[return false;]";
      var checkRegExpB = new RegExp(checkPatternB);
      if (onclickTextB.match(checkRegExpB)) {
        onclickTextB = onclickTextB.substring(13,onclickTextB.length);
      }
      onclickTextB = onclickTextB.replace('return false', 'return true');
      setObjAttribute(obj, 'onclick', onclickTextB);
    }

    if (obj.className.indexOf("Combo")!=-1) {
      obj.className = obj.className.replace("NoUpdatable","");
      enableAttributeWithFunction(obj, 'obj', 'onChange');
      if (obj.getAttribute("onChange")) {
        onchange_combo = getObjAttribute(obj, 'onChange');
      } else {
        onchange_combo = "";
      }
      if (onchange_combo.indexOf("selectCombo")!=-1) {
        newOnChange_combo = onchange_combo.substring(0,onchange_combo.indexOf("selectCombo"))+onchange_combo.substring(onchange_combo.indexOf(";",onchange_combo.indexOf("selectCombo"))+1, onchange_combo.length);
        setObjAttribute(obj, 'onChange', newOnChange_combo);
      }
    }
    if ((obj.className.indexOf("TextBox_")!=-1)||(obj.className.indexOf("TextArea_")!=-1)) {
      enableFieldButton(getAssociatedFieldButton(obj, 'window'));
    }
  }
  return true;
}

/**
* Handles the onKeyDown and onKeyUp event, for an specific numeric typing control.
* @param {Object} obj Field where the numeric typing will be evaluated.
* @param {Boolean} isFloatAllowed Defines if a float number is allowed.
* @param {Boolean} isNegativeAllowed Defines if a negative number is allowed.
* @param {Event} evt The event handling object associated with the field.
* @returns True if is an allowed number, otherwise false.
* @type Boolean
* @see #obtainKeyCode
*/
function autoCompleteNumber(obj, isFloatAllowed, isNegativeAllowed, evt) {
  if (!evt) evt = window.event;
  var number = evt.keyCode ? evt.keyCode : evt.which ? evt.which : evt.charCode;
  if (number != obtainKeyCode("ENTER") && number != obtainKeyCode("LEFTARROW") && number != obtainKeyCode("RIGHTARROW") && number != obtainKeyCode("UPARROW") && number != obtainKeyCode("DOWNARROW") && number != obtainKeyCode("DELETE") && number != obtainKeyCode("BACKSPACE") && number != obtainKeyCode("END") && number != obtainKeyCode("HOME") && !evt["ctrlKey"]) {
    if (number>95 && number <106) { //Teclado numÃ©rico
      number = number - 96;
      if(isNaN(number)) {
        if (document.all) evt.returnValue = false;
        return false;
      }
    } else if (number!=obtainKeyCode("DECIMAL") && number != obtainKeyCode("NUMBERDECIMAL") && number != obtainKeyCode("NEGATIVE") && number != obtainKeyCode("NUMBERNEGATIVE")) { //No es "-" ni "."
      number = String.fromCharCode(number);
      if(isNaN(number)) {
        if (document.all) evt.returnValue = false;
        return false;
      }
    } else if (number==obtainKeyCode("DECIMAL") || number==obtainKeyCode("NUMBERDECIMAL")) { //Es "."
      if (isFloatAllowed) {
        if (obj.value==null || obj.value=="") return true;
        else {
          var point = obj.value.indexOf(".");
          if (point != -1) {
            point = obj.value.indexOf(".", point+1);
            if (point==-1) return true;
          } else return true;
        }
      }
      if (document.all) evt.returnValue = false;
      return false;
    } else { //Es "-"
      if (isNegativeAllowed && (obj.value==null || obj.value.indexOf("-")==-1)) return true;
      if (document.all) evt.returnValue = false;
      return false;
    }
  }
  return true;
}

/**
* Return the most significant features of an object.
* @param {Object} field Reference to the field that will be inspected.
* @returns The features as a string.
* @type String
*/
function getObjFeatures(obj) {
  if (typeof obj == "string") {
    obj = document.getElementById(obj);
  }
  var objType = ""
  if (obj.tagName.toLowerCase() == 'input') {
    objType += "input";
    objType += " ";
    objType += obj.getAttribute('type');
    objType += " ";
  }
  if (obj.getAttribute('readonly') == 'true' || obj.readOnly) {
    objType += "readonly";
    objType += " ";
  }
  if (obj.getAttribute('disabled') == 'true' || obj.disabled) {
    objType += "disabled";
    objType += " ";
  }
  return objType;
}

/**
* Sets the global variable 'isPageLoading' to true or false
*/
function setPageLoading(status) {
  if (status==false) {
    isPageLoading = false;
  } else {
    isPageLoading = true;
  }
}

/**
* Used on the onChange event for field changes logging. Requires a field named inpLastFieldChanged in the form.
* @param {Object} field Reference to the field that will be logged. 
* @returns True if everything goes right, false if the field does not exist or an error occurred. 
* @type Boolean
* @see #setChangedField
*/
function logChanges(field) {
  // if(!isUserChanges) return;
  if (field==null || !field) return false;
  return setChangedField(field, field.form);
}

/**
* Used on the onKeyDown event for isEditing status.
*/
function changeToEditingMode(special, field) {
  try {
    if (field && field == calloutProcessedObj) return false;
    isContextMenuOpened = false;
    if (special == 'force') {
      setWindowEditing(true);
      return true;
    }
    if (mustBeIgnored(focusedWindowElement)) return false;
    if (special == 'oncut' || special == 'onpaste') {
      checkFieldChange();
    } else if (special == 'oncontextmenu') {
      var elementToCheck = focusedWindowElement;
      isContextMenuOpened = true;
      checkContextMenu(elementToCheck);
    } else if (special == 'onkeydown') { //Special case for Supr and Del keys on IE7 (they can not be catched using onkeypress)
        setTimeout('checkIE7DelSuprKeys();',100);
    } else if (special == 'onkeypress') {
      if (!isTabPressed && focusedWindowElement.tagName.toUpperCase().indexOf("SELECT")!=-1 && focusedWindowElement && !isCtrlPressed && !isAltPressed && isKeyboardLocked==false) { // Keypress on ComboBox
        setWindowEditing(true);
      } else if (!isTabPressed && focusedWindowElement.tagName.toUpperCase().indexOf("SELECT")==-1 && !isCtrlPressed && !isAltPressed && isKeyboardLocked==false && pressedKeyCode!='33' && pressedKeyCode!='34'
       && pressedKeyCode!='35' && pressedKeyCode!='36' && pressedKeyCode!='37' && pressedKeyCode!='38' && pressedKeyCode!='39' && pressedKeyCode!='40') {
        setWindowEditing(true);
  //  } else if (isCtrlPressed && pressedKeyCode=='86' && isKeyboardLocked==false) { // Ctrl + V
  //    setWindowEditing(true);
      } else if (isCtrlPressed && isAltPressed && isKeyboardLocked==false) { // AltGr
        checkFieldChange();
      }
    } else if (special == 'onchange') {
      if (!isTabPressed && focusedWindowElement.tagName.toUpperCase().indexOf("SELECT")!=-1 && focusedWindowElement == field && !isCtrlPressed && !isAltPressed && isKeyboardLocked==false) { // Select item of combo by using mouse
        setWindowEditing(true);
      }
    }
  } catch (e) { }
}

function checkIE7DelSuprKeys() {
  if (focusedWindowElement.tagName.toUpperCase().indexOf("SELECT") == -1 && getBrowserInfo('name').indexOf("Firefox") == -1 && getBrowserInfo('name').indexOf("IceWeasel") == -1) {
    if (pressedKeyCode=='46' || pressedKeyCode=='8') {
      setWindowEditing(true);
    }
  }
}

function checkContextMenu(elementToCheck) {
  if (isContextMenuOpened == true) {
    checkFieldChange(elementToCheck); 
    setTimeout(function() { checkContextMenu(elementToCheck); },50);
  }
}

function checkFieldChange(elementToCheck) {
  if (elementToCheck == null || elementToCheck == 'null' || elementToCheck == '') {
    elementToCheck = focusedWindowElement;
  }
  var beforeShortcutValue = null;
  var afterShortcutValue = null;
  try { beforeShortcutValue = elementToCheck.value; } catch (e) { }
  setTimeout(function() {try { afterShortcutValue = elementToCheck.value; } catch (e) { } if (afterShortcutValue != beforeShortcutValue) { setWindowEditing(true); } },50);
}

/**
* Used on the undo toolbar button
* @param {Object} form Reference to the application form
*/
function windowUndo(form) {
  form.reset();
  for (var i=0; i < form.elements.length; i++) {
    var element = form.elements[i];
    if (element.doReset) {
      element.doReset();
    }
    try{
      //If an element has the onchange method defined, we will execute the selectCombo part
      //so that readonly fields work correctly on undo
      if (element.onchange){
        var onchangecode = element.onchange.toString();
        if(onchangecode.indexOf('selectCombo')!==-1){
          var indSel = onchangecode.indexOf('selectCombo');
          var afterselect = onchangecode.substring(indSel, onchangecode.length);
          var selectComboCode = afterselect.substring(0, afterselect.indexOf(');')+2).replace('this','document.getElementById("'+element.id+'")');
          eval(selectComboCode);
        }
      }
    }catch (e) {
      //do nothing in this case
    }
  }
  form.inpLastFieldChanged.value = '';
  setWindowEditing(false);
  displayLogic();
}

/**
* Opens a pop-up window with a processing message. Used for long wait calls.
* @retunrs A reference ID of the newly opened window 
* @type Object
*/
function processingPopUp() {
  var complementosNS4 = ""

  var strHeight=100, strWidth=200;
  var strTop=parseInt((screen.height - strHeight)/2);
  var strLeft=parseInt((screen.width - strWidth)/2);
  
  if (navigator.appName.indexOf("Netscape"))
    complementosNS4 = "alwaysRaised=1, dependent=1, directories=0, hotkeys=0, menubar=0, ";
  var complementos = complementosNS4 + "height=" + strHeight + ", width=" + strWidth + ", left=" + strLeft + ", top=" + strTop + ", screenX=" + strLeft + ", screenY=" + strTop + ", location=0, resizable=0, status=0, toolbar=0, titlebar=0";
  var winPopUp = window.open("", "_blank", complementos);
  if (winPopUp!=null) {
    document.onunload = function(){winPopUp.close();};
    document.onmousedown = function(){winPopUp.close();};
    winPopUp.document.writeln("<html>\n");
    winPopUp.document.writeln("<head>\n");
    winPopUp.document.writeln("<title>Proceso petici&oacute;n</title>\n");
    winPopUp.document.writeln("<script language=\"javascript\" type=\"text/javascript\">\n");
    winPopUp.document.writeln("function selectTD(name, seleccionar) {\n");
    winPopUp.document.writeln("  var obj = getStyle(name);\n");
    winPopUp.document.writeln("  if (document.layers) {\n");
    winPopUp.document.writeln("    if (seleccionar) obj.bgColor=\"" + gColorSelected + "\";\n");
    winPopUp.document.writeln("    else obj.bgColor=\"" + gWhiteColor + "\";\n");
    winPopUp.document.writeln("  } else {\n");
    winPopUp.document.writeln("    if (seleccionar) obj.backgroundColor = \"" + gColorSelected + "\";\n");
    winPopUp.document.writeln("    else obj.backgroundColor=\"" + gWhiteColor + "\";\n");
    winPopUp.document.writeln("  }\n");
    winPopUp.document.writeln("  return seleccionar;\n");
    winPopUp.document.writeln("}\n");
    winPopUp.document.writeln("function getReference(id) {\n");
    winPopUp.document.writeln("  if (document.getElementById) return document.getElementById(id);\n");
    winPopUp.document.writeln("  else if (document.all) return document.all[id];\n");
    winPopUp.document.writeln("  else if (document.layers) return document.layers[id];\n");
    winPopUp.document.writeln("  else return null;\n");
    winPopUp.document.writeln("}\n");
    winPopUp.document.writeln("function getStyle(id) {\n");
    winPopUp.document.writeln("  var ref = getReference(id);\n");
    winPopUp.document.writeln("  if (ref==null || !ref) return null;\n");
    winPopUp.document.writeln("  return ((document.layers) ? ref : ref.style);\n");
    winPopUp.document.writeln("}\n");
    winPopUp.document.writeln("var total=5;\n");
    winPopUp.document.writeln("function loading(num) {\n");
    winPopUp.document.writeln(" if (num>=total) {\n");
    winPopUp.document.writeln("   for (var i=0;i<total;i++) {\n");
    winPopUp.document.writeln("     selectTD(\"TD\" + i, false);\n");
    winPopUp.document.writeln("   }\n");
    winPopUp.document.writeln("   num=-1;\n");
    winPopUp.document.writeln(" } else {\n");
    winPopUp.document.writeln("   selectTD(\"TD\" + num, true);\n");
    winPopUp.document.writeln(" }\n");
    winPopUp.document.writeln(" setTimeout('loading(' + (++num) + ')', 1000);\n");
    winPopUp.document.writeln(" return true;\n");
    winPopUp.document.writeln("}\n");
    winPopUp.document.writeln("</script>\n");
    winPopUp.document.writeln("</head>\n");
    winPopUp.document.writeln("<body leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" onLoad=\"loading(0);\">\n");
    winPopUp.document.writeln("  <table width=\"80%\" border=\"0\" cellspacing=\"3\" cellpadding=\"0\" align=\"center\">\n");
    winPopUp.document.writeln("    <tr>\n");
    winPopUp.document.writeln("      <td colspan=\"5\" align=\"center\"><font color=\"navy\" size=\"5\">PROCESSING...</font></td>\n");
    winPopUp.document.writeln("    </tr>\n");
    winPopUp.document.writeln("    <tr bgcolor=\"" + gWhiteColor + "\">\n");
    winPopUp.document.writeln("      <td width= \"20%\" id=\"TD0\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("      <td width=\"20%\" id=\"TD1\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("      <td width=\"20%\" id=\"TD2\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("      <td width=\"20%\" id=\"TD3\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("      <td width=\"20%\" id=\"TD4\" bgcolor=\"" + gWhiteColor + "\">&nbsp;</td>\n");
    winPopUp.document.writeln("    </tr>\n");
    winPopUp.document.writeln("  </table>\n");
    winPopUp.document.writeln("</body>\n");
    winPopUp.document.writeln("</html>\n");
    winPopUp.document.close();
    winPopUp.focus();
  }
  return winPopUp;
}

/**
* Returns the rounded value of a number to specified precision (number of digits after the decimal point).
* @param The value to round
* @param The number of decimal digits to round to.
* @return Returns the rounded value.
* @type Number
*/
function round(number,X) {
  X = (!X ? 2 : X);
  if (!number || isNaN(number)) return 0;
  return Math.round(number*Math.pow(10,X))/Math.pow(10,X);
}

/**
* Replace the occurrence of the search string with the replacement string
* @param {String} text The original String.
* @param {String} replaceWhat The search String.
* @param {String} replaceWith The replacement String.
* @returns A String with the replaced text.
* @type String
*/
function ReplaceText(text, replaceWhat, replaceWith) {
  if (text==null || text.length==0) return "";
  text += "";
  var i = text.indexOf(replaceWhat);
  var j = 0;
  while (i!=-1) {
    var partial = text.substring(0, i);
    text = text.substring(i+replaceWhat.length);
    text = partial + replaceWith + text;
    j = i + replaceWith.length;
    i = text.indexOf(replaceWhat, j);
  }
  return text;
}

/**
* Fires the onChange event on the field passed as parameter and updates the inpLastFieldChanged in the form.
* @param {Object} field A reference to the field that will be updated.
* @returns True
* @type Boolean
* @see #setInputValue
*/
function updateOnChange(field) {
  if (field==null) return false;
  try {
    var lastChanged = inputValue(document.forms[0].inpLastFieldChanged);
    //if (field.name!="inpadClientId" && field.name!="inpadOrgId")
    field.onchange();
    setInputValue(document.forms[0].inpLastFieldChanged, lastChanged);
  } catch (e) {}
  return true;
}

/**
* Dummy function. To be substituted in the &lt;script&gt; tag on the page.
* @returns True
* @type Boolean
*/
function xx()
{
  return true;
}

/*
* Calendar compatibility with external elements
*/
/**
* This function gets called when the end-user clicks on some date.
* @param {Object} cal 
* @param {String} date 
* 
*/ 
function selected(cal, date) {
  cal.sel.value = date; // just update the date in the input field.
  if (cal.dateClicked && (cal.sel.id == "sel1" || cal.sel.id == "sel3"))
    // if we add this call we close the calendar on single-click.
    // just to exemplify both cases, we are using this only for the 1st
    // and the 3rd field, while 2nd and 4th will still require double-click.
    cal.callCloseHandler();
}

/**
* And this gets called when the end-user clicks on the _selected_ date,
* or clicks on the "Close" button.  It just hides the calendar without
* destroying it
* @param {Type} cal
*/
// .
function closeHandler(cal) {
  if (typeof (cal.sel.onchange) != "undefined" && cal.sel.onchange != null) {
    cal.sel.onchange();
  }
  cal.sel.focus();
  cal.hide();                        // hide the calendar
//  cal.destroy();
  _dynarch_popupCalendar = null;
}

/**
* Function Description
* @param {String} str_format
* @returns
* @type String
*/
function getDateFormat(str_format) {
  var format = "";
  str_format = str_format.replace("mm","MM").replace("dd","DD").replace("yyyy","YYYY").replace("yy","YY");
  str_format = str_format.replace("%D","%d").replace("%M","%m");
  if (str_format!=null && str_format!="" && str_format!="null") {
    format = str_format;
    format = format.replace("YYYY","%Y");
    format = format.replace("YY","%y");
    format = format.replace("MM","%m");
    format = format.replace("DD","%d");
    format = format.substring(0,8);
  }
  str_format = str_format.replace("hh","HH").replace("HH24","HH").replace("mi","MI").replace("ss","SS");
  str_format = str_format.replace("%H","HH").replace("HH:%m","HH:MI").replace("HH.%m","HH.MI").replace("%S","SS");
  str_format = str_format.replace("HH:mm","HH:MI").replace("HH.mm","HH.MI");
  str_format = str_format.replace("HH:MM","HH:MI").replace("HH.MM","HH.MI");
  if (str_format==null || str_format=="" || str_format=="null") str_format = defaultDateFormat;
  else if (str_format.indexOf(" HH:MI:SS")!=-1) format += " %H:%M:%S";
  else if (str_format.indexOf(" HH:MI")!=-1) format += " %H:%M";
  else if (str_format.indexOf(" HH.MI.SS")!=-1) format += " %H.%M.%S";
  else if (str_format.indexOf(" HH.MI")!=-1) format += " %H.%M";
  return format;
}

/**
* Shows a calendar window
* @param {String} id A reference to the textbox that will hold the returned value
* @param {String} value Starting value of the calendar
* @param {Boolean} debug
* @param {Boolean} format
* @param {Boolean} showsTime
* @param {Boolean} showsOtherMonths
* @returns
* @type Boolean
*/
function showCalendar(id, value, debug, format, showsTime, showsOtherMonths) {
  isTabBlocked = true;
  //var el = document.getElementById(id);
  var el = eval("document." + id);
  if (showsTime==null) showsTime = "";
  if (showsOtherMonths==null) showsOtherMonths = false;
  if (format==null || format=="") format = getDateFormat(el.getAttribute("displayformat"));
  else format = getDateFormat(format);
  if (format.indexOf(" %H:%M")!=-1) showsTime = "24";
  else if (format.indexOf(" %H.%M")!=-1) showsTime = "24";
  
  if (_dynarch_popupCalendar != null) {
    // we already have some calendar created
    _dynarch_popupCalendar.hide();                 // so we hide it first.
  } else {
    // first-time call, create the calendar.
    var cal = new Calendar(1, null, selected, closeHandler);
    // uncomment the following line to hide the week numbers
    cal.weekNumbers = false;
    if (typeof showsTime == "string" && showsTime!="") {
      cal.showsTime = true;
      cal.time24 = (showsTime == "24");
    }
    if (showsOtherMonths) {
      cal.showsOtherMonths = true;
    }
    _dynarch_popupCalendar = cal;                  // remember it in the global var
    cal.setRange(1900, 2070);        // min/max year allowed.
    cal.create();
  }
  dateFormat = format;
  _dynarch_popupCalendar.setDateFormat(format);    // set the specified date format
  _dynarch_popupCalendar.parseDate(el.value);      // try to parse the text in field
  _dynarch_popupCalendar.sel = el;                 // inform it what input field we use

  // the reference element that we pass to showAtElement is the button that
  // triggers the calendar.  In this example we align the calendar bottom-right
  // to the button.
  _dynarch_popupCalendar.showAtElement(el, "Br");        // show the calendar

  return false;
}

/**
* Compares two dates based on the given format.
* @param {String} date1 First date to compare.
* @param {String} date2 Second date to compare.
* @param {String} String format of the date.
* @returns null when a null or an empty String is passed as parameter, returns -1 if date1 < date 2, returns 0 if date 1 = date2 or 1 if date1 > date2
* @type Number
*/
function datecmp(date1, date2, fmt) {
  if (date1==null || date1 == "") return null;
  else if (date2==null || date2 == "") return null;
  //fmt = getDateFormat(fmt);
  var mydate1 = getDate(date1, fmt);
  var mydate2 = getDate(date2, fmt);
  if (mydate1==null || mydate1=="" || mydate2==null || mydate2=="") return null;
  if (mydate1.getFullYear() > mydate2.getFullYear()) return 1;
  else if (mydate1.getFullYear() == mydate2.getFullYear()) {
    if (mydate1.getMonth() > mydate2.getMonth()) return 1;
    else if (mydate1.getMonth() == mydate2.getMonth()) {
      if (mydate1.getDate() > mydate2.getDate()) return 1;
      else if (mydate1.getDate() == mydate2.getDate()) {
        if (mydate1.getHours() > mydate2.getHours()) return 1;
        else if (mydate1.getHours() == mydate2.getHours()) {
          if (mydate1.getMinutes() > mydate2.getMinutes()) return 1;
          else if (mydate1.getMinutes() == mydate2.getMinutes()) return 0;
          else return -1;
        } else return -1;
      } else return -1;
    } else return -1;
  } else return -1;
}

/**
* Returns the number of digtis based on the given parameter (part of a date)
* @param {String} formatType The part of the date to evaluate
* @returns 4 when 'Y', 2 when 'm', 2 when 'd', otherwise 2. 
* @type Number
*/
function checkFormat(formatType) {
  switch (formatType) {
    case 'Y': return 4;
    case 'm': return 2;
    case 'd': return 2;
    default: return 2;
  }
  return 0;
}

/**
* Returns an Array by splitting a String from the % character 
* @param {String} format String to split
* @returns An Array with the splitted elements
* @type Array
*/
function getSeparators(format) {
  if (format==null || format.length==0) return null;
  var result = new Array();
  var pos = format.indexOf("%");
  var last = 0;
  var i=0;
  while (pos!=-1) {
    if (pos>last) {
      result[i++] = format.substring(last, pos);
    }
    last = pos+2;
    pos = format.indexOf("%", last);
  }
  if (last < format.length) result[i] = format.substring(last);
  return result;
}

/**
* Search for a text in an Array
* @param {Array} obj The array of elements
* @param {String} text The text to look for
* @returns True if is found, otherwise false.
* @type Boolean
*/
function isInArray(obj, text) {
  if (obj==null || obj.length==0) return false;
  if (text==null || text.length==0) return false;
  var total = obj.length;
  for (var i = 0;i<total;i++) {
    if (obj[i].toUpperCase()==text.toUpperCase()) return true;
  }
  return false;
}

/**
* Opens the Openbravo's about window
*/
function about() {
  var appUrl = getAppUrl();
  var complementosNS4 = ""

  var strHeight=500;
  var strWidth=600;
  var strTop=parseInt((screen.height - strHeight)/2);
  var strLeft=parseInt((screen.width - strWidth)/2);
  if (navigator.appName.indexOf("Netscape"))
    complementosNS4 = "alwaysRaised=1, dependent=1, directories=0, hotkeys=0, menubar=0, ";
  var complementos = complementosNS4 + "height=" + strHeight + ", width=" + strWidth + ", left=" + strLeft + ", top=" + strTop + ", screenX=" + strLeft + ", screenY=" + strTop + ", location=0, resizable=yes, scrollbars=yes, status=0, toolbar=0, titlebar=0";
  var winPopUp = window.open(appUrl + "/ad_forms/about.html", "ABOUT", complementos);
  if (winPopUp!=null) {
    winPopUp.focus();
    document.onunload = function(){winPopUp.close();};
    document.onmousedown = function(){winPopUp.close();};
  }
  return winPopUp;
}

/**
* Manage button's events
*/
function buttonEvent(event, obj) {
  if (obj.className.indexOf('ButtonLink_disabled') == -1) {
    if (event == "onkeyup") {
      obj.className='ButtonLink_focus';
    } else if (event == "onkeydown") {
    } else if (event == "onkeypress") {
      obj.className='ButtonLink_active';
    } else if (event == "onmouseup") {
      if (obj.className.indexOf('ButtonLink_active') != -1) {
        obj.className = obj.className.replace(' xx','');
        obj.className = obj.className.replace('ButtonLink_active','');
      }
    } else if (event == "onmousedown") {
      if (obj.className.indexOf('ButtonLink_hover') != -1) {
        obj.className = obj.className.replace(' xx','');
        obj.className = obj.className.replace('ButtonLink_hover','');
      }
      if (obj.className.indexOf('ButtonLink_active') == -1) {
        obj.className = 'ButtonLink_active' + ' xx' + obj.className;
      }
    } else if (event == "onmouseover") {
      if (obj.className.indexOf('ButtonLink_hover') == -1) {
        obj.className = 'ButtonLink_hover' + ' xx' + obj.className;
      }
    } else if (event == "onmouseout") {
      if (obj.className.indexOf('ButtonLink_active') != -1) {
        obj.className = obj.className.replace(' xx','');
        obj.className = obj.className.replace('ButtonLink_active','');
      }
      if (obj.className.indexOf('ButtonLink_hover') != -1) {
        obj.className = obj.className.replace(' xx','');
        obj.className = obj.className.replace('ButtonLink_hover','');
      }
      window.status='';
    } else if (event == "onfocus") {
      setWindowElementFocus(obj);
    } else if (event == "onblur") {
      window.status='';
    } else if (event == "onclick") {
    }
  }
  return true;
}

/**
* Returns to previous web
*/
function goToPreviousPage() {
  setMDIEnvironment();
  if (isWindowInMDIPage) {
    var appFrame = getFrame("appFrame");
    appFrame.history.back();
    return;
  }
  
  var appUrl = getAppUrl();
  //if (navigator.userAgent.toUpperCase().indexOf("MSIE") != -1) {
  //  history.back();
  //} else {
    openLink(appUrl + '/secureApp/GoBack.html', 'appFrame');
  //}
}

/**
* Function Description
* @param {Boolean} isOnResize
*/

function resizeArea(isOnResize) {
  if (isOnResize==null) isOnResize = false;
  var mnu = document.getElementById("client");

  var mleft = document.getElementById("tdLeftTabsBars");
  var mleftSeparator = document.getElementById("tdleftSeparator");
  var mright = document.getElementById("tdrightSeparator");
  var mtop = document.getElementById("tdtopNavButtons");
  var mtopToolbar = document.getElementById("tdToolBar");
  var mtopTabs = document.getElementById("tdtopTabs");
  var mbottombut = document.getElementById("tdbottomButtons");
  var mbottom = document.getElementById("tdbottomSeparator");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
  var mnuWidth = w - ((mleft?mleft.clientWidth:0) + (mleftSeparator?mleftSeparator.clientWidth:0) + (mright?mright.clientWidth:0)) - ((name.indexOf("Microsoft")==-1)?2:0);
  var mnuHeight = h -((mtop?mtop.clientHeight:0) + (mtopToolbar?mtopToolbar.clientHeight:0) + (mtopTabs?mtopTabs.clientHeight:0) + (mbottom?mbottom.clientHeight:0) + (mbottombut?mbottombut.clientHeight:0)) - ((name.indexOf("Microsoft")==-1)?1:0);
  if (mnuWidth < 0) { mnuWidth = 0; }
  if (mnuHeight < 0) { mnuHeight = 0; }
  mnu.style.width = mnuWidth;
  mnu.style.height = mnuHeight;
  var mbottomButtons = document.getElementById("tdbottomButtons");
  if (mbottomButtons) mbottomButtons.style.width = w - ((mleft?mleft.clientWidth:0) + (mleftSeparator?mleftSeparator.clientWidth:0) + (mright?mright.clientWidth:0)) - ((name.indexOf("Microsoft")==-1)?2:0);

/*  try {
    dojo.addOnLoad(dijit.byId('grid').onResize);
  } catch (e) {}*/
  try {
    if (isOnResize) dijit.byId('grid').onResize();
  } catch (e) {}
  mnu.style.display = "";
}

/**
* Function Description
*/
function resizeAreaHelp() {
  var mnu = document.getElementById("client");
  var mnuIndex = document.getElementById("clientIndex");
  var mTopSeparator = document.getElementById("tdSeparator");
  var mTopNavigation = document.getElementById("tdNavigation");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
//  var mnuWidth = w - 18 - ((name.indexOf("Microsoft")==-1)?2:0);
  var mnuHeight =  h -(mTopSeparator.clientHeight + mTopNavigation.clientHeight) - 2;
//  if (mnuWidth < 0) { mnuWidth = 0; }
  if (mnuHeight < 0) { mnuHeight = 0; }
//  mnu.style.width = mnuWidth;
  mnu.style.height = mnuHeight;
  mnuIndex.style.height = mnu.style.height;

  mnu.style.display = "";
  mnuIndex.style.display = "";
}

/**
* Function Description
*/
function resizeAreaUserOps() {
  var mnu = document.getElementById("client");
  var mnuIndex = document.getElementById("clientIndex");
  var mTopSeparator = document.getElementById("tdSeparator");
  var mVerSeparator = document.getElementById("tdVerSeparator");
  var mTopNavigation = document.getElementById("tdNavigation");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
//  mnu.style.width = w - 18 - ((name.indexOf("Microsoft")==-1)?2:0);
  mnu.style.height = h -(mTopSeparator.clientHeight + mTopNavigation.clientHeight) - 2;
  mnuIndex.style.height = mnu.style.height;

  mnuIndex.style.display = "";

  mnu.style.width= w - (mVerSeparator.clientWidth + mnuIndex.clientWidth) - 2;

  mnu.style.display = "";
}

/**
* Function Description
*/
function resizeAreaInfo(isOnResize) {
  if (isOnResize==null) isOnResize = false;
  var table_header = document.getElementById("table_header");
  var client_top = document.getElementById("client_top");
  var client_middle = document.getElementById("client_middle");
  var client_bottom = document.getElementById("client_bottom");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
  var client_middleWidth = w;
  var client_middleHeight = h -((table_header?table_header.clientHeight:0) + (client_top?client_top.clientHeight:0) + (client_bottom?client_bottom.clientHeight:0)) - ((name.indexOf("Microsoft")==-1)?1:0);
  if (client_middleWidth < 0) { client_middleWidth = 0; }
  if (client_middleHeight < 170) { client_middleHeight = 170; } // To avoid middle area (usually a grid) disappear completly in small windows.
  client_middle.style.height = client_middleHeight;
  client_middle.style.width = client_middleWidth;

  try {
    if (document.getElementById("grid_toptext")) {
      document.getElementById('grid_toptext').style.width = w - 50;
    }
    if (document.getElementById("grid_bottomtext")) {
      document.getElementById('grid_bottomtext').style.width = w - 50;
    }
    if (isOnResize) dijit.byId('grid').onResize();
  } catch (e) {}
}

/**
* Function Description
*/
function resizeAreaCreateFrom(isOnResize) {
  if (isOnResize==null) isOnResize = false;
  var table_header = document.getElementById("table_header");
  var client_messagebox = document.getElementById("client_messagebox");
  var client_top = document.getElementById("client_top");
  var client_middle = document.getElementById("client_middle");
  var client_bottom = document.getElementById("client_bottom");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
  var client_middleWidth = w - 0;
  var client_middleHeight = h -((table_header?table_header.clientHeight:0) + (client_messagebox?client_messagebox.clientHeight:0) + (client_top?client_top.clientHeight:0) + (client_bottom?client_bottom.clientHeight:0)) - ((name.indexOf("Microsoft")==-1)?1:1);
  if (client_middleWidth < 0) { client_middleWidth = 0; }
  if (client_middleHeight < 80) { client_middleHeight = 80; } // To avoid middle area (usually a grid) disappear completly in small windows.
  client_middle.style.height = client_middleHeight;
  client_middle.style.width = client_middleWidth;
  client_middle.style.display = "";

  try {
    if (isOnResize) dijit.byId('grid').onResize();
  } catch (e) {}
}

/**
* Function Description
*/
function resizeAreaInstallationHistoryGrid(isOnResize) {
  if (isOnResize==null) isOnResize = false;
  var client = document.getElementById("client");
  var client_top = document.getElementById("client_top");
  var installationHistoryGrid = document.getElementById("installationHistoryGrid");
  var name = window.navigator.appName;
  installationHistoryGrid.style.height = client.clientHeight -((client_top?client_top.clientHeight:0) -((name.indexOf("Microsoft")==-1)?1:0) + 8);

/*  try {
    dojo.addOnLoad(dijit.byId('grid').onResize);
  } catch (e) {}*/
  try {
    if (isOnResize) dijit.byId('grid').onResize();
  } catch (e) {}
}

/**
* Function Description
*/
function resizePopup() {
  var mnu = document.getElementById("client");
  var table_header = document.getElementById("table_header");
  var body = document.getElementsByTagName("BODY");
  var h = body[0].clientHeight;
  var w = body[0].clientWidth;
  var name = window.navigator.appName;
  var mnuHeight = h -(table_header?table_header.clientHeight:0);
  var mnuWidth = w;
  if (mnuWidth < 0) { mnuWidth = 0; }
  if (mnuHeight < 0) { mnuHeight = 0; }
  mnu.style.height = mnuHeight;
  mnu.style.width = mnuWidth;
  mnu.style.display = "";
}

/**
* Function Description
*/
function calculateMsgBoxWidth() {
  var client_width = document.getElementById("client").clientWidth;
  var msgbox_table = document.getElementById("messageBoxID");
  msgbox_table.style.width = client_width;
 }
 
/**
* Change the status for show audit in Edition mode, in local javascript variable and in session value (with ajax)
**/
function changeAuditStatus() {
  var appUrl = getAppUrl();
  if (strShowAudit=="Y") strShowAudit="N";
  else strShowAudit="Y";
  displayLogic();
  changeAuditIcon(strShowAudit);
  var paramXMLReq = null;
  submitXmlHttpRequest(xx, null, 'CHANGE', appUrl + "/utility/ChangeAudit", false, null, paramXMLReq);
  return true;
}

/**
* Change the status for show audit in Relation mode, in local javascript variable and in session value (with ajax)
**/
function changeAuditStatusRelation() {
  var appUrl = getAppUrl();
  var paramXMLReq = null;
  submitXmlHttpRequest(document.getElementById("buttonRefresh").onclick, null, 'CHANGE', appUrl + "/utility/ChangeAudit", false, null, paramXMLReq);
  return true;
}

function changeAuditIcon(newStatus) {
  obj = document.getElementById("linkButtonAudit");
  if (obj == null) return false;
  obj.className="Main_ToolBar_Button"+(newStatus=="Y"?"_Selected":"");
  if (newStatus=="Y")
    setTimeout("getDataBaseStandardMessage('hideAudit', changeAuditIconTitle)",100);
  else
    setTimeout("getDataBaseStandardMessage('showAudit', changeAuditIconTitle)",100);
}

function changeSearchIcon(filtered){
  var obj = document.getElementById("buttonSearch") || document.getElementById("buttonSearchFiltered") ;
  if (!obj) {
    return false;
  }
  obj.className = "Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Search"+(filtered?"Filtered":"");
}

 function changeAuditIconTitle(paramXMLParticular, XMLHttpRequestObj) {
   var obj;
   object = document.getElementById("buttonAudit");
   if (getReadyStateHandler(XMLHttpRequestObj)) {
    try {
      if (XMLHttpRequestObj.responseXML) obj = XMLHttpRequestObj.responseXML.documentElement;
    } catch (e) {
    }
    
    
    if (obj && object) {
      var status = obj.getElementsByTagName('status');
      if (status.length>0) {
        object.title = status[0].getElementsByTagName('description')[0].firstChild.nodeValue;
      }
    }
   }
   return false;
 }
 
   function goToDivAnchor(div,elementId){
     div = document.getElementById(div);
     elementId = document.getElementById(elementId);
     div.scrollTop = elementId.offsetTop - (navigator.userAgent.toUpperCase().indexOf("MSIE")!=-1?0:div.offsetTop);
   }

//-->



  // Numeric formatting functions

/**
* Returns the global decimal separator
* @returns The global decimal separator
* @type String
*/
function getGlobalDecSeparator() {
  var m = getFrame('frameMenu');
  return m.decSeparator_global;
}

/**
* Returns the global group separator
* @returns The global group separator
* @type String
*/
function getGlobalGroupSeparator() {
  var m = getFrame('frameMenu');
  return m.groupSeparator_global;
}

/**
* Returns the global group interval
* @returns The global group interval
* @type String
*/
function getGlobalGroupInterval() {
  var m = getFrame('frameMenu');
  return m.groupInterval_global;
}

/**
* Returns a boolean specifing if the returned mask is in java format ("," for group separator and "." for decimal separator)
* @returns True since returned masks are always in java format
* @type Boolean
*/
function isJavaMask() {
  var isJavaMask = true;
  return isJavaMask;
}

/**
* Returns the default mask numeric
* @returns The default mask numeric
* @type String
*/
function getDefaultMaskNumeric() {
  var m = getFrame('frameMenu');
  var maskNumeric_default = m.maskNumeric_default;
  if (isJavaMask()) {
    decSeparator = getGlobalDecSeparator();
    groupSeparator = getGlobalGroupSeparator();
    maskNumeric_default = returnMaskChange(maskNumeric_default, ".", ",", decSeparator, groupSeparator);
  }
  return maskNumeric_default;
}

/**
* Return the mask needed in a given input
* @param {Object} obj The input to obtain the mask
* @returns The mask of the input
* @type String
*/
  function getInputNumberMask(obj) {
    var outputformat = obj.getAttribute('outputformat');
    outputformat = formatNameToMask(outputformat);
    return outputformat;
  }

/**
 * Returns the output format for a given format defined in Format.xml
 * @param (String) formatName One of the defined format names in Format.xml
 * @return The outputFormat for the formatName
 * @type String
*/
function formatNameToMask(formatName) {
  var maskNumeric = "";
  var decSeparator = "";
  var groupSeparator = "";
  var F = getFrame('frameMenu').F;
  if(typeof F === 'undefined') {
      return maskNumeric;
  }
  if(formatName == null || formatName == "" || formatName == "null") {
    formatName = "qtyEdition";
  }
  maskNumeric = F.getFormat(formatName);
  if (isJavaMask()) {
    //decSeparator = F.getDecSeparator(formatName);
    //groupSeparator = F.getGroupSeparator(formatName);
    decSeparator = getGlobalDecSeparator();
    groupSeparator = getGlobalGroupSeparator();
    maskNumeric = returnMaskChange(maskNumeric, ".", ",", decSeparator, groupSeparator);
  }
  return maskNumeric;
}

/**
* Actions to be done when focusing a numeric input
* @param {Object} obj The focused input
* @param {String} maskNumeric The numeric mask of the input
* @param {String} decSeparator The decimal separator of the input
* @param {String} groupSeparator The group separator of the input
* @param {String} groupInterval The group interval of the input
*/
function focusNumberInput(obj, maskNumeric, decSeparator, groupSeparator, groupInterval) {
  if (maskNumeric == null || maskNumeric == "") maskNumeric = getDefaultMaskNumeric();
  if (decSeparator == null || decSeparator == "") decSeparator = getGlobalDecSeparator();
  if (groupSeparator == null || groupSeparator == "") groupSeparator = getGlobalGroupSeparator();
  if (groupInterval == null || groupInterval == "") groupInterval = getGlobalGroupInterval();

  var isTextSelected = false;
  if (obj.value.length > 0) {
    if (getCaretPosition(obj).start !=  getCaretPosition(obj).end && getCaretPosition(obj).end == obj.value.length) {
      isTextSelected = true;
    }
  }

  var oldCaretPosition = getCaretPosition(obj).start;
  var newCaretPosition = returnNewCaretPosition(obj, oldCaretPosition, groupSeparator);

  var number = obj.value;
  var isValid = checkNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
  if (!isValid) {
    return false;
  }
  var plainNumber = returnPlainNumber(number, decSeparator, groupSeparator);
  obj.value = plainNumber;
  setCaretToPos(obj, newCaretPosition);
  if (isTextSelected == true && selectInputTextOnTab) {
    obj.select();
  }
}

/**
* Function to calculate the new caret position when an input is focused and the display format is changed
* @param {Object} obj The input
* @param {String} oldCaretPosition The caret position before the format change
* @param {String} groupSeparator The group separator of the input
* @return The new caret position
* @type Number
*/
function returnNewCaretPosition(obj, oldCaretPosition, groupSeparator) {
  var newCaretPosition = oldCaretPosition;
  for (var i=oldCaretPosition; i>0; i--) {
    if (obj.value.substring(i-1, i) == groupSeparator) {
      newCaretPosition = newCaretPosition - 1;
    }
  }
  return newCaretPosition;
}

/**
* Function that returns a number just with the decimal Separator
* @param {String} number The formatted number
* @param {String} decSeparator The decimal separator of the number
* @param {String} groupSeparator The group separator of the number
* @return The plain number
* @type String
*/
function returnPlainNumber(number, decSeparator, groupSeparator) {
  number = number.toString();
  var plainNumber = number;

  // Remove group separators
  var groupRegExp = new RegExp("\\" + groupSeparator,"g");
  plainNumber = plainNumber.replace(groupRegExp,"");

  // Catch sign
  var numberSign = "";
  if (plainNumber.substring(0, 1) == "+") {
    numberSign = "";
    plainNumber = plainNumber.substring(1, number.length);
  } else if (plainNumber.substring(0, 1) == "-") {
    numberSign = "-";
    plainNumber = plainNumber.substring(1, number.length);
  }

  // Remove ending decimal "0"
  if (plainNumber.indexOf(decSeparator) != -1) {
    while (plainNumber.substring(plainNumber.length-1, plainNumber.length) == "0") {
      plainNumber = plainNumber.substring(0, plainNumber.length-1);
    }
  }

  //Remove starting integer "0"
  while (plainNumber.substring(0, 1) == "0" && plainNumber.substring(1, 2) != decSeparator && plainNumber.length > 1) {
    plainNumber = plainNumber.substring(1, plainNumber.length);
  }

  // Remove decimal separator if is the last character
  if (plainNumber.substring(plainNumber.length-1, plainNumber.length) == decSeparator) {
    plainNumber = plainNumber.substring(0, plainNumber.length-1);
  }

  // Re-set sign
  if (plainNumber != "0") {
    plainNumber = numberSign + plainNumber;
  }

  //Return plain number
  return plainNumber;
}

/**
* Function that returns a number just with the decimal separator which always is ".". It is used for math operations
* @param {String} number The formatted number
* @param {String} decSeparator The decimal separator of the number
* @param {String} groupSeparator The group separator of the number
* @return The converted number
* @type String
*/
function returnFormattedToCalc(number, decSeparator, groupSeparator) {
  if (decSeparator == null || decSeparator == "") decSeparator = getGlobalDecSeparator();
  if (groupSeparator == null || groupSeparator == "") groupSeparator = getGlobalGroupSeparator();

  var calcNumber = number;
  calcNumber = returnPlainNumber(calcNumber, decSeparator, groupSeparator);
  calcNumber = calcNumber.replace(decSeparator, '.');
  return calcNumber;
}

/**
* Function that returns a formatted number given as input a number just with the decimal separator which always is "."
* @param {String} number The formatted number
* @param {String} maskNumeric The numeric mask of the number
* @param {String} decSeparator The decimal separator of the number
* @param {String} groupSeparator The group separator of the number
* @param {String} groupInterval The group interval of the number
* @return The converted number
* @type String
*/
function returnCalcToFormatted(number, maskNumeric, decSeparator, groupSeparator, groupInterval) {
  var formattedNumber = number;
  formattedNumber = formattedNumber.toString();
  formattedNumber = formattedNumber.replace(".", decSeparator);
  formattedNumber = returnFormattedNumber(formattedNumber, maskNumeric, decSeparator, groupSeparator, groupInterval);
  return formattedNumber;
}

/**
* Function that does a change of the decimal and group separators of a mask
* @param {String} maskNumeric The numeric mask
* @param {String} decSeparator_old The old decimal separator
* @param {String} groupSeparator_old The old group separator
* @param {String} decSeparator_new  The new decimal separator
* @param {String} groupSeparator_new The new group separator
* @return The converted mask
* @type String
*/
function returnMaskChange(maskNumeric, decSeparator_old, groupSeparator_old, decSeparator_new, groupSeparator_new) {
  if (decSeparator_new == null || decSeparator_new == "") decSeparator_new = getGlobalDecSeparator();
  if (groupSeparator_new == null || groupSeparator_new == "") groupSeparator_new = getGlobalGroupSeparator();

  var realMask = "";
  for (var i=0; i<maskNumeric.length; i++) {
    if (maskNumeric.substring(i,i+1) == decSeparator_old) {
      realMask = realMask + decSeparator_new;
    } else if (maskNumeric.substring(i,i+1) == groupSeparator_old) {
      realMask = realMask + groupSeparator_new;
    } else {
      realMask = realMask + maskNumeric.substring(i,i+1);
    }
  }
  return realMask;
}

/**
* Actions to be done when blur from a numeric input
* @param {Object} obj The focused input
* @param {String} maskNumeric The numeric mask of the input
* @param {String} decSeparator The decimal separator of the input
* @param {String} groupSeparator The group separator of the input
* @param {String} groupInterval The group interval of the input
*/
function blurNumberInput(obj, maskNumeric, decSeparator, groupSeparator, groupInterval) {
  if (maskNumeric == null || maskNumeric == "") maskNumeric = getDefaultMaskNumeric();
  if (decSeparator == null || decSeparator == "") decSeparator = getGlobalDecSeparator();
  if (groupSeparator == null || groupSeparator == "") groupSeparator = getGlobalGroupSeparator();
  if (groupInterval == null || groupInterval == "") groupInterval = getGlobalGroupInterval();

  var number = obj.value;
  var isValid = checkNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
  if (obj.getAttribute('maxlength')) {
    if (obj.value.length > obj.getAttribute('maxlength')) {
      isValid = false;
    }
  }
  updateNumberMiniMB(obj, isValid); //It doesn't apply in dojo043 inputs since it has its own methods to update it
  if (!isValid) {
    return false;
  }
  var formattedNumber = returnFormattedNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
  obj.value = formattedNumber;
}

/**
* Function called from the input when event occurs
* @param {String} command String containing the event name
* @param {Object} obj The focused input
* @param {Event} evt The event itself (only needed when onkeydown)
*/
function numberInputEvent(command, obj, evt) {
  if (command == "onfocus") {
    focusNumberInput(obj, getInputNumberMask(obj));
    return true;
  } else if (command == "onblur") {
    blurNumberInput(obj, getInputNumberMask(obj));
    return true;
  } else if (command == "onkeydown") {
    manageDecPoint(obj, null, evt);
    return true;
  } else if (command == "onchange") {
    blurNumberInput(obj, getInputNumberMask(obj));
    return true;
  }
}

/**
* Updates the mini message box if the written input format is wrong
* @param {Object} obj The input to check
* @param {Boolean} isValid Boolean to check if the passes obj has valid format or not
*/
function updateNumberMiniMB(obj, isValid) {
  //Invalid check
  if (!document.getElementById(obj.id+"invalidSpan")) {
    return true;
  }
  var miniMessageBox_invalid = document.getElementById(obj.id+"invalidSpan");
  if (!isValid) {
    miniMessageBox_invalid.style.display="";
    return true;
  } else {
    miniMessageBox_invalid.style.display="none";
  }

  //Required check
  if (!document.getElementById(obj.id+"missingSpan")) {
    return true;
  }
  var isRequired = obj.getAttribute("required");
  if (isRequired == "true") isRequired = true;
  else if (isRequired == "false") isRequired = false;
  var isMissing = false;
  if (obj.value.length == 0) {
    isMissing = true;
  }
  var miniMessageBox_missing = document.getElementById(obj.id+"missingSpan");
  if (isRequired && isMissing) {
    miniMessageBox_missing.style.display="";
    return true;
  } else {
    miniMessageBox_missing.style.display="none";
  }

  return true;
}

/**
* Function that transform a plain number into a formatted one
* @param {String} number The number
* @param {String} maskNumeric The numeric mask of the number
* @param {String} decSeparator The decimal separator of the number
* @param {String} groupSeparator The group separator of the number
* @param {String} groupInterval The group interval of the number
* @return The converted number
* @type String
*/
function returnFormattedNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval) {

  if (number == "" || number == null) {
    return number;
  }

  //Management of the mask
  if (maskNumeric.indexOf("+") == 0 || maskNumeric.indexOf("-") == 0) {
    maskNumeric = maskNumeric.substring(1, maskNumeric.length);
  }
  if (maskNumeric.indexOf(groupSeparator) != -1 && maskNumeric.indexOf(decSeparator) != -1 && maskNumeric.indexOf(groupSeparator) > maskNumeric.indexOf(decSeparator)) {
    var fixRegExp = new RegExp("\\" + groupSeparator,"g");
    maskNumeric = maskNumeric.replace(fixRegExp,"");
  }
  var maskLength = maskNumeric.length;
  var decMaskPosition = maskNumeric.indexOf(decSeparator);
  if (decMaskPosition == -1) decMaskPosition = maskLength;
  var intMask = maskNumeric.substring(0,decMaskPosition);
  var decMask = maskNumeric.substring(decMaskPosition+1,maskLength);

  if (decMask.indexOf(groupSeparator) != -1 || decMask.indexOf(decSeparator) != -1) {
    var fixRegExp_1 = new RegExp("\\" + groupSeparator,"g");
    decMask = decMask.replace(fixRegExp_1,"");
    var fixRegExp_2 = new RegExp("\\" + decSeparator,"g");
    decMask = decMask.replace(fixRegExp_2,"");
  }

  //Management of the number
  number = number.toString();
  number = returnPlainNumber(number, decSeparator, groupSeparator);
  var numberSign = "";
  if (number.substring(0, 1) == "+") {
    numberSign = "";
    number = number.substring(1, number.length);
  } else if (number.substring(0, 1) == "-") {
    numberSign = "-";
    number = number.substring(1, number.length);
  }

  ////Splitting the number
  var formattedNumber = "";
  var numberLength = number.length;
  var decPosition = number.indexOf(decSeparator);
  if (decPosition == -1) decPosition = numberLength;
  var intNumber = number.substring(0,decPosition);
  var decNumber = number.substring(decPosition+1,numberLength);

  // //Management of the decimal part
  if (decNumber.length > decMask.length) {
    decNumber = "0." + decNumber;
    decNumber = roundNumber(decNumber, decMask.length);
    decNumber = decNumber.toString();
    if (decNumber.substring(0, 1) == "1") {
      intNumber = parseFloat(intNumber);
      intNumber = intNumber + 1;
      intNumber = intNumber.toString();
    }
    decNumber = decNumber.substring(2, decNumber.length);
  }

  if (decNumber.length < decMask.length) {
    var decNumber_temp = ""
    for (var i=0; i<decMask.length; i++) {
      if (decMask.substring(i,i+1) == "#") {
        if (decNumber.substring(i,i+1) != "") {
          decNumber_temp = decNumber_temp + decNumber.substring(i,i+1);
        }
      } else if (decMask.substring(i,i+1) == "0") {
        if (decNumber.substring(i,i+1) != "") {
          decNumber_temp = decNumber_temp + decNumber.substring(i,i+1);
        } else {
          decNumber_temp = decNumber_temp + "0";
        }
      }
    }
    decNumber = decNumber_temp;
  }

  //Management of the integer part
  var isGroup = false;

  if (intMask.indexOf(groupSeparator) != -1) { isGroup = true; }

  var groupRegExp = new RegExp("\\" + groupSeparator,"g");
  intMask = intMask.replace(groupRegExp,"");

  if (intNumber.length < intMask.length) {
    var intNumber_temp = "";
    var diff = intMask.length - intNumber.length;
    for (var i=intMask.length; i>0; i--) {
      if (intMask.substring(i-1,i) == "#") {
        if (intNumber.substring(i-1-diff,i-diff) != "") {
          intNumber_temp = intNumber.substring(i-1-diff,i-diff) + intNumber_temp;
        }
      } else if (intMask.substring(i-1,i) == "0") {
        if (intNumber.substring(i-1-diff,i-diff) != "") {
          intNumber_temp = intNumber.substring(i-1-diff,i-diff) + intNumber_temp;
        } else {
          intNumber_temp = "0" + intNumber_temp;
        }
      }
    }
    intNumber = intNumber_temp;
  }


  if (isGroup == true) {
    var intNumber_temp = "";
    var groupCounter=0;
    for (var i=intNumber.length; i>0; i--) {
       intNumber_temp = intNumber.substring(i-1, i) + intNumber_temp; 
       groupCounter++;
       if (groupCounter==groupInterval && i!=1) {
        groupCounter=0;
        intNumber_temp = groupSeparator + intNumber_temp;
       }
    }
    intNumber = intNumber_temp;
  }

  //Building the final number
  if (intNumber=="" && decNumber != "") {
    intNumber = "0";
  }
  formattedNumber = numberSign + intNumber;
  if (decNumber != "") {
    formattedNumber += decSeparator + decNumber;
  }
  return formattedNumber;
}

/**
* Function that checks if a number has the right format or not
* @param {String} number The number
* @param {String} maskNumeric The numeric mask of the number
* @param {String} decSeparator The decimal separator of the number
* @param {String} groupSeparator The group separator of the number
* @param {String} groupInterval The group interval of the number
* @return True if is correct or false if it is incorrect
* @type Boolean
*/
function checkNumber(number, maskNumeric, decSeparator, groupSeparator, groupInterval) {
  var bolNegative = true;
  if (maskNumeric.indexOf("+") == 0) {
    bolNegative = false;
    maskNumeric = maskNumeric.substring(1, maskNumeric.length);
  }

  var bolDecimal = true;
  if (maskNumeric.indexOf(decSeparator) == -1) {
    bolDecimal = false;
  }
  var checkPattern = "";
  checkPattern += "^";
  if (bolNegative) { checkPattern += "([+]|[-])?"; }
  checkPattern += "(\\d+)?((\\" + groupSeparator + "\\d{" + groupInterval + "})?)+";
  if (bolDecimal) { checkPattern += "(\\" + decSeparator + "\\d+)?"; }
  checkPattern += "$";
  var checkRegExp = new RegExp(checkPattern);
  if (number.match(checkRegExp) && number.substring(0, 1) != groupSeparator) {
    return true;
  }
  return false;
}

/**
* Function that rounds a number to a given decimal number
* @param {Number} num The number
* @param {Number} dec The number of decimals
* @return True if is correct or false if it is incorrect
* @type Boolean
*/
function roundNumber(num, dec) {
  var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
  return result;
}

/**
* Function to operate with formatted number
* @param {Number} number1 The first operand
* @param {String} operator The operator (+ - * / % < > <= >= ...)
* @param {Number} number2 The second operand
* @param {String} result_maskNumeric The numeric mask of the result
* @param {String} decSeparator The decimal separator of the number
* @param {String} groupSeparator The group separator of the number
* @param {String} groupInterval The group interval of the number
* @return The result of the operation or true or false if the operator is (< > <= >= ...)
* @type String or Boolean
*/
function formattedNumberOp(number1, operator, number2, result_maskNumeric, decSeparator, groupSeparator, groupInterval) {
  if (result_maskNumeric == null || result_maskNumeric == "") result_maskNumeric = getDefaultMaskNumeric();
  if (decSeparator == null || decSeparator == "") decSeparator = getGlobalDecSeparator();
  if (groupSeparator == null || groupSeparator == "") groupSeparator = getGlobalGroupSeparator();
  if (groupInterval == null || groupInterval == "") groupInterval = getGlobalGroupInterval();

  var result;

  number1 = returnFormattedToCalc(number1, decSeparator, groupSeparator);
  number1 = parseFloat(number1);

  number2 = returnFormattedToCalc(number2, decSeparator, groupSeparator);
  number2 = parseFloat(number2);

  if (operator == "sqrt") {
    result = Math.sqrt(number1);
  } else if (operator == "round") {
    result = roundNumber(number1, number2);
  } else {
    result = eval('('+number1+')' + operator + '('+number2+')');
  }
  if (result != true && result != false && result != null && result != "") {
    result = returnCalcToFormatted(result, result_maskNumeric, decSeparator, groupSeparator, groupInterval)
  }
  return result;
}

/**
* Function that returns the reverse of a given text string
* @param {String} text The text
* @return The reversed string
* @type String
*/
function reverseString(text) {
  var reverseText = "";
  for (var i=text.length; i>0 ;i--) {
     reverseText += text.substring(i-1, i);
  }
  return reverseText;
}


/**
* Objet CaretPosition
*/
function CaretPosition() {
 var start = null;
 var end = null;
}

/**
* Function that returns actual position of -1 if we are at last position
*/
function getCaretPosition(oField) {
 var oCaretPos = new CaretPosition();

 // IE support
 if(document.selection) {
  oField.focus();
  var oSel = document.selection.createRange();
  var selectionLength = oSel.text.length;
  oSel.moveStart ('character', -oField.value.length);
  oCaretPos.start = oSel.text.length - selectionLength;
  oCaretPos.end = oSel.text.length;
 }
 // Firefox support
 else if(oField.selectionStart || oField.selectionStart == '0')
 {
  // This is a whole lot easier in Firefox
  oCaretPos.start = oField.selectionStart;
  oCaretPos.end = oField.selectionEnd;
 }

 // Return results
 return (oCaretPos);
}

/**
* Function that selects a text range of an input
* @param {Object} obj The input to do a selection
* @param {Number} selectionStart The start position of the selection
* @param {Number} selectionEnd The start position of the selection
*/
function setSelectionRange(obj, selectionStart, selectionEnd) {
  if (obj.setSelectionRange) {
    obj.focus();
    obj.setSelectionRange(selectionStart, selectionEnd);
  } else if (obj.createTextRange) {
    var range = obj.createTextRange();
    range.collapse(true);
    range.moveEnd('character', selectionEnd);
    range.moveStart('character', selectionStart);
    range.select();
  }
}

/**
* Function that sets the cursor position to the end
* @param {Object} obj The target input
*/
function setCaretToEnd (obj) {
  setSelectionRange(obj, obj.value.length, obj.value.length);
}

/**
* Function that sets the cursor position to the start
* @param {Object} obj The target input
*/
function setCaretToBegin (obj) {
  setSelectionRange(obj, 0, 0);
}

/**
* Function that sets the cursor to an specific position
* @param {Object} obj The target input
* @param {Number} obj The target position
*/
function setCaretToPos (obj, pos) {
  setSelectionRange(obj, pos, pos);
}

/**
* Function that manage the numeric dot write in the input
* @param {Object} obj The input
* @param {String} decSeparator The decimal separator
* @param {Event} evt The event. Usually related to onkeydown
*/
function manageDecPoint(obj, decSeparator, evt) {
  if (decSeparator == null || decSeparator == "") decSeparator = getGlobalDecSeparator();

  if (decSeparator == ".") {
    return true;
  }
  var caretPosition = getCaretPosition(obj).start;
  if (!evt) evt = window.event;
  var keyCode = evt.keyCode ? evt.keyCode : evt.which ? evt.which : evt.charCode;
  /*
  * if(keyCode>=65 && keyCode<=90) { setTimeout(function()
  * {obj.value = replaceAt(obj.value, "", caretPosition);
  * setCaretToPos(obj, caretPosition);},5); }
  */
  var inpMaxlength = obj.getAttribute("maxlength");
  var inpLength = obj.value.length;
  var isInpMaxLength = false;
  if (inpMaxlength === null) {
    isInpMaxLength = false;
  } else if (inpLength >= inpMaxlength) {
    isInpMaxLength = true;
  }

  if (getBrowserInfo('name').toUpperCase().indexOf("OPERA") != -1 && keyCode==78) {
    keyCode = 110;
  }

  if(keyCode==110 && !isInpMaxLength) {
    setTimeout(function() {obj.value = replaceAt(obj.value, decSeparator, caretPosition); setCaretToPos(obj, caretPosition+1);},5);
  }
  return true;
}

/**
* Function that replaces a fixed position of a string with another text
* @param {String} string The complete text
* @param {String} what The replacement text
* @param {Number} ini The initial position to start the change
* @param {Number} end The end position to start the change
* @return The result of the operation or true or false if the operator is (< > <= >= ...)
* @type String
*/
function replaceAt(string, what, ini, end) {
  if (typeof end == "undefined" || end == null || end == "null" || end == "") {
    end = ini;
  }
  if (ini > end) {
    var temp = ini;
    ini = end;
    end = temp;
  }
  var newString = "";
  newString = string.substring(0, ini) + what + string.substring(end+1, string.length);
  return newString;
}

function closePage(okEvent) {
  if (isWindowInMDIPopup) {
    // Timeout is set in order to fix issue https://issues.openbravo.com/view.php?id=20234
    setTimeout(function() { getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup.close(MDIPopupId, !okEvent); }, 10);
  } else if (isWindowInMDITab) {
  } else {
    top.window.close();
  }
  return true;
}

function popupResizeTo(width, height) {
  if (isWindowInMDIPopup) {
    getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup.resize(MDIPopupId, width, height);
  } else if (isWindowInMDITab) {
  } else {
    window.resizeTo(width, height);
  }
  return true;
}

/**
* Start of functions to communicate with 3.0 tabbed interface
*/

function LayoutMDICheck(target) {
  if (target !== null) {
    if (typeof target.OB !== "undefined") {
      if (typeof target.OB.Layout !== "undefined") {
        if (typeof target.OB.Layout.ViewManager === "object") {
          return true;
        }
      }
    }
  }
  return false;
}

function setMDIEnvironment() {
  if (isMDIEnvironmentSet) {
    return;
  }
  isMDIEnvironmentSet = true;

  try {  // To avoid unhandled exceptions suchs a MDI modal popup
    if (isWindowInMDITab && typeof sendWindowInfoToMDI === "function") {
      sendWindowInfoToMDI();
    }
  } catch (e) {
  }
}

/*
 * Function that checks if the rendered html is contained inside a OB 3.0 popup or not.
 */
function checkWindowInMDIPopup(target) {
  var result = true;

  if (!target || target === "null" || target === "") {
    target = window;
  }

  try {
    while ((target.document !== target.parent.document) && (!target.document.getElementById('MDIPopupContainer'))) {
      target = target.parent;
    }
  } catch (e) {
  }

  if (!target.document.getElementById('MDIPopupContainer')) {
    result = false;
  } else {
    MDIPopupId = target.document.getElementById('MDIPopupContainer').name;
  }

  if (window.name !== 'frameMenu' && window.location.href.indexOf('utility/VerticalMenu.html') === -1) {
    if (result === true && 
        MDIPopupId !== null && 
        document.title && 
        getFrame('LayoutMDI') && 
        getFrame('LayoutMDI').OB && 
        getFrame('LayoutMDI').OB.Layout && 
        getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility && 
        getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup && 
        getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup.setTitle) {
      getFrame('LayoutMDI').OB.Layout.ClassicOBCompatibility.Popup.setTitle(MDIPopupId, document.title);
    }
  }

  return result;
}

/*
 * Function that checks if the rendered html is contained inside a OB 3.0 tab or not.
 */
function checkWindowInMDITab(target) {
  var result = true;

  if (isWindowInMDIPopup === true) {
    result = false;
  } else {
    if (!target || target === "null" || target === "") {
      target = window;
    }

    try {
      while ((target.document !== target.parent.document) && (!LayoutMDICheck(target))) {
        target = target.parent;
      }
    } catch (e) {
    }

    if (!LayoutMDICheck(target)) {
      result = false;
    }
  }
  return result;
}

/*
 * Function that checks if the rendered html is contained inside a OB 3.0 page or not.
 */
function checkWindowInMDIPage(target) {
  var result = false;
  if (isWindowInMDIPopup === true || isWindowInMDITab === true) {
    result = true;
  }
  return result;
}

/*
 * Function that checks if the rendered html is contained inside a OB 3.0 context or not.
 */
function checkWindowInMDIContext(target) {
  var result = true;

  if (!target || target === "null" || target === "") {
    target = window;
  }

  if (isWindowInMDIPage) {
    result = true;
  } else {
    var LayoutMDI = getFrame('LayoutMDI');
    if (LayoutMDI !== null) {
      result = true;
    } else {
      result = false;
    }
  }
  return result;
}

function sendWindowInfoToMDI() {
  if (!isWindowInMDITab) {
    return false;
  }
  var LayoutMDI = getFrame('LayoutMDI');

  var windowId = null;
  var tabId = null;
  var keyName = null;
  var recordId = null;
  var title = null;
  var mode = null;
  if (typeof getElementsByName('inpwindowId','input')[0] !== "undefined") {
    windowId = getElementsByName('inpwindowId','input')[0].value;
  }
  if (typeof getElementsByName('inpTabId','input')[0] !== "undefined") {
    tabId = getElementsByName('inpTabId','input')[0].value;
  }
  if (typeof getElementsByName('inpKeyName','input')[0] !== "undefined") {
    keyName = getElementsByName('inpKeyName','input')[0].value;
  }
  if (typeof getElementsByName(keyName,'input')[0] !== "undefined") {
    recordId = getElementsByName(keyName,'input')[0].value;
  }
  if (document.getElementById('tabTitle_text') !== null) {
    title = document.getElementById('tabTitle_text').innerHTML;
  }

  if (tabId === null && !document.getElementById('buttonAbout')) {
    mode = "error";
    title = "Error";
  } else if (tabId === null) {
    mode = "manual";
  } else if (document.getElementById("grid_table_dummy_input")) {
    mode = "grid";
  } else if (recordId === "") {
    mode = "new";
  } else {
    mode = "edit";
  }

  var obManualURL = document.location.href;
  var appUrl = getAppUrl();
  if (!appUrl) {
    return false;
  }

  obManualURL = obManualURL.replace(appUrl, "");
//obManualURL = obManualURL.replace("?hideMenu=true&noprefs=true", "");
  obManualURL = obManualURL.substring(0, obManualURL.indexOf("?"));
  if (mode !== "manual") {
    obManualURL = null;
  } else {
    windowId = null;
    tabId = null;
    recordId = null;
  }

  try {
    if (document.getElementById('buttonBack') || document.getElementById('buttonAbout')) {
      if (typeof LayoutMDI.OB.Layout.ClassicOBCompatibility.setTabInformation === "function") {
        LayoutMDI.OB.Layout.ClassicOBCompatibility.setTabInformation(windowId, tabId, recordId, mode, obManualURL, title);
      }
    }
  } catch (e) { }
}


/**
* End of functions to communicate with 3.0 tabbed interface
*/

/**
* Start of deprecated functions in 2.40
*/

var arrTeclas=new Array();

/**
* Deprecated in 2.40: Builds the keys array on each screen. Each key that we want to use should have this structure.
* @param {Sting} tecla A text version of the handled key.
* @param {String} evento Event that we want to fire when the key is is pressed.
* @param {String} campo Name of the field on the window. If is null, is a global event, for the hole window.
* @param {String} teclaAuxiliar Text defining the auxiliar key. The value could be CTRL for the Control key, ALT for the Alt, null if we don't have to use an auxiliar key.
*/
function Teclas(tecla, evento, campo, teclaAuxiliar) {
  this.tecla = tecla;
  this.evento = evento;
  this.campo = campo;
  this.teclaAuxiliar = teclaAuxiliar;
}

/**
* Deprecated in 2.40: still has one user: multilinea.js 
* Handles the events execution of keys pressed, based on the events registered in the arrTeclas global array.   
* @param {Event} CodigoTecla Code of the key pressed.
* @returns True if the key is not registered in the array, false if a event for this key is registered in arrTeclas array.
* @type Boolean
* @see #obtainKeyCode
*/
function controlTecla(CodigoTecla) {
  if (arrTeclas==null || arrTeclas.length==0) return true;
  if (!CodigoTecla) CodigoTecla = window.event;
  var tecla = window.event ? CodigoTecla.keyCode : CodigoTecla.which;
  var target = (CodigoTecla.target?CodigoTecla.target: CodigoTecla.srcElement);
  //var target = (document.layers) ? CodigoTecla.target : CodigoTecla.srcElement;
  var total = arrTeclas.length;
  for (var i=0;i<total;i++) {
    if (arrTeclas[i]!=null && arrTeclas[i]) {
      if (tecla == obtainKeyCode(arrTeclas[i].tecla)) {
        if (arrTeclas[i].teclaAuxiliar==null || arrTeclas[i].teclaAuxiliar=="" || arrTeclas[i].teclaAuxiliar=="null") {
          if (arrTeclas[i].campo==null || (target!=null && target.name!=null && isIdenticalField(arrTeclas[i].campo, target.name))) {
            var eventoTrl = replaceEventString(arrTeclas[i].evento, target.name, arrTeclas[i].campo);
            eval(eventoTrl);
            return false;
          }
        } else if (arrTeclas[i].campo==null || (target!=null && target.name!=null && isIdenticalField(arrTeclas[i].campo, target.name))) {
          if (arrTeclas[i].teclaAuxiliar=="ctrlKey" && CodigoTecla.ctrlKey && !CodigoTecla.altKey && !CodigoTecla.shiftKey) {
            var eventoTrl = replaceEventString(arrTeclas[i].evento, target.name, arrTeclas[i].campo);
            eval(eventoTrl);
            return false;
          } else if (arrTeclas[i].teclaAuxiliar=="altKey" && !CodigoTecla.ctrlKey && CodigoTecla.altKey && !CodigoTecla.shiftKey) {
            var eventoTrl = replaceEventString(arrTeclas[i].evento, target.name, arrTeclas[i].campo);
            eval(eventoTrl);
            return false;
          } else if (arrTeclas[i].teclaAuxiliar=="shiftKey" && !CodigoTecla.ctrlKey && !CodigoTecla.altKey && CodigoTecla.shiftKey) {
            var eventoTrl = replaceEventString(arrTeclas[i].evento, target.name, arrTeclas[i].campo);
            eval(eventoTrl);
            return false;
          }
        }
      }
    }
  }
  return true;
}

/**
* End of deprecated functions in 2.40
*/

/**
* Start of deprecated functions for 2.50 (will be removed after the 2.50 release)
*/

//Deprecated in 2.50
var gAUXILIAR=0;

//Deprecated in 2.50 use clearForm instead
function limpiar(form) {
  clearForm(form);
}

//Deprecated in 2.50, use validateNumber instead
function esNumero(strValue, isFloatAllowed, isNegativeAllowed) {
  validateNumber(strValue, isFloatAllowed, isNegativeAllowed);
}

//Deprecated in 2.50, use validateNumberField instead
function campoNumerico(field, isFloatAllowed, isNegativeAllowed) {
  validateNumberField(field, isFloatAllowed, isNegativeAllowed);
}

//Deprecated in 2.50, use openNewBrowser instead
function abrirNuevoBrowser(url, _name, height, width, top, left) {
  openNewBrowser(url, _name, height, width, top, left);
}

//Deprecated in 2.50, use openExcel instead
function abrirExcel(url, _name, checkChanges) {
  openExcel(url, _name, checkChanges);
}

//Deprecated in 2.50, use openPDF instead
function abrirPDF(url, _name, checkChanges) {
  openPDF(url, _name, checkChanges);
}

//Deprecated in 2.50, use openPDFFiltered instead
function abrirPDFFiltered(url, _name, checkChanges) {
  openPDFFiltered(url, _name, checkChanges);
}

//Deprecated in 2.50, use openPopUpDefaultSize instead
function abrirPopUp(url, _name, height, width, closeControl, showstatus) {
  openPopUpDefaultSize(url, _name, height, width, closeControl, showstatus);
}

//Deprecated in 2.50, use openPDFSession instead
function abrirPDFSession(strPage, strDirectPrinting, strHiddenKey, strHiddenValue, bolCheckChanges) {
  openPDFSession(strPage, strDirectPrinting, strHiddenKey, strHiddenValue, bolCheckChanges);
}

//Deprecated in 2.50, use openSearchWindow instead
function abrirBusqueda(url, _name, tabId, windowName, windowId, checkChanges) {
  openSearchWindow(url, _name, tabId, windowName, windowId, checkChanges);
}

//Deprecated in 2.50, use autoCompleteNumber instead
function auto_complete_number(obj, isFloatAllowed, isNegativeAllowed, evt) {
  autoCompleteNumber(obj, isFloatAllowed, isNegativeAllowed, evt);
}

//Deprecated in 2.50, use autoCompleteDate instead
function auto_complete_date(field, fmt) {
  autoCompleteDate(field, fmt);
}

//Deprecated in 2.50, use autoCompleteTime instead
function auto_complete_time(field, fmt) {
  autoCompleteTime(field, fmt);
}

/**
* Search an array for a given parent key, and fills a combo with the founded values.
* @param {Object} combo A reference to the combo that will be filled.
* @param {Array} arrayDatos A data array that contents the combo info. Must be in the following format:
*               <ol>
*                 <li>Parent key or grouping element</li>
*                 <li>Element key</li>
*                 <li>Element's string. That will be presented on the screen</li>
*                 <li>Boolean flag to indicate if the element is a selected item</li>
*               </ol>
* @param {String} padre Parent key for search common elements.
* @param {Boolean} bolSelected Sets if the array's last field will be used for select an item. If this parameters is null or false, the last field on the array is no mandatory. 
* @param {Boolean} sinBlanco Set if a blank element should be added to the combo. The default value is false.
* @returns True if everything was right, otherwise false.
* @Type Boolean
* Deprecated in 2.50
*/
function rellenarComboHijo(combo, arrayDatos, padre, bolSelected, sinBlanco) {
  var i, value="";
  for (i = combo.options.length;i>=0;i--)
    combo.options[i] = null;
  i=0;
  if (sinBlanco==null || !sinBlanco)
    combo.options[i++] = new Option("", "");
  if (arrayDatos==null) return false;

  var total = arrayDatos.length;
  for (var j=0;j<total;j++) {
    if (arrayDatos[j][0]==padre) {
      combo.options[i] = new Option(arrayDatos[j][2], arrayDatos[j][1]);
      if (bolSelected!=null && bolSelected && arrayDatos[j][3]=="true") {
        value = arrayDatos[j][1];
        combo.options[i].selected = true;
      }
      else combo.options[i].selected = false;
      i++;
    }
  }
  return value;
}

//Deprecated in 2.50, use fillCombo instead
function rellenarCombo(combo, dataArray, bolSelected, withoutBlankOption) {
  fillCombo(combo, dataArray, bolSelected, withoutBlankOption);
}

//Deprecated since 2.50, use markAll instead
function marcarTodos(chk, bolMark) {
  markAll(chk, bolMark);
}

//Deprecated in 2.50, use changeComboData instead
function cambiarListaCombo(combo, dataArray, key, withBlankOption) {
  changeComboData(combo, dataArray, key, withBlankOption);
}

//Deprecated in 2.50, use clearList instead
function limpiarLista(field) {
  clearList(field);
}

//Deprecated in 2.50, use clearSelectedElements instead
function eliminarElementosList(field) {
  clearSelectedElements(field);
}

/**
* Generates an Array based on a list of checkboxs selected.
* @param {Form} frm A reference to the form where the checkbox are contained.
* @param {Object} check A reference to the checkboxs list
* @param {String} text The textbox that has the name/index of the array. 
* @param {Array} resultado A reference parameter with the modified/generated array
* Deprecated in 2.50
*/
function generarArrayChecks(frm, check, text, resultado) {
  var n=0;
  if (check==null) {
    resultado=null;
    return;
  }
  if (!check.length || check.length<=1) {
    if (check.checked) {
      var texto = eval(frm.name + "." + text + check.value);
      var valor = "";
      if (texto!=null) {
        if (!texto.length || texto.length<=1) valor = texto.value;
        else valor = texto[0].value;
      }
      resultado[0] = new Array(check.value, valor);
    }
  } else {
    for (var i = check.length-1;i>=0;i--) {
      if (check[i].checked) {
        var valor = "";
        var texto = eval(frm.name + "." + text + check[i].value);
        if (texto!=null) {
          if (!texto.length || texto.length<=1) valor = texto.value;
          else valor = texto[0].value;
        }
        resultado[n++] = new Array(check[i].value, valor);
      }
    }
  }
}

//Deprecation in 2.50, use markCheckedAllElements instead
function seleccionarListCompleto(field) {
  markCheckedAllElements(field);
}

//Deprecation in 2.50, use markCheckedAllElements instead
function seleccionarListCompleto(field) {
  markCheckedAllElements(field);
}

/**
* @name menuContextual
* @format function menuContextual()
* @comment Se trata de un funciÃ³n manejadora de eventos que sirve para el control del click con el 
*          botÃ³n derecho sobre la pÃ¡gina. Esta funciÃ³n no permite dicho evento, presentando un mensaje 
*          en tal caso.
* Deprecated in 2.50
*/
function menuContextual(evt) {
  var boton = (evt==null)?event.button:evt.which;
  if (boton == 3 || boton == 2) {
    if (document.all) alert('El boton derecho estÃ¡ deshabilitado por pruebas');
    return false;
  }
  return true;
}

/**
* End of deprecated functions in 2.50
*/

utilsJSDirectExecution();
