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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview Contains a function returnReponse that is called upon load 
* of a callout template (eg. CallOut.html) and fills specified form fields
* with specified values.
*/

var frameDefault = "appFrame";

function displayLogic() {
  return true;
}

function setgWaitingCallOut(state, frameName) {
  if (frameName==null || frameName=="") frameName=frameDefault;
  objFrame = eval("parent." + frameName);
  if (!objFrame) {
    // working in 3.0 modal window
    objFrame = parent;
  }
  objFrame.setGWaitingCallOut(state);
}

function returnResponse(arrResponse, calloutName, frameName, formName) {
  // Deprecated in 2.50
  // the following allows some backwards-compatibility so that custom callouts which
  // still use the frameAplicacion target, to work with the new name appFrame
  if (frameName == 'frameAplicacion') {
    frameName = 'appFrame';
  }
  setgWaitingCallOut(false, frameName);
  if (arrResponse==null && (calloutName==null || calloutName=="")) return false;
  if (frameName==null || frameName=="") frameName=frameDefault;
  objFrame = eval("parent." + frameName);
  if (!objFrame) {
    // working in 3.0 modal window
    objFrame = parent;
    if (!objFrame.fillElementsFromArray) {
      return;
    }
  }
  if (objFrame) {
    objFrame.fillElementsFromArray(arrResponse, calloutName, formName);
    try {
      objFrame.displayLogic();
    } catch (ignored) {}
  }
}
