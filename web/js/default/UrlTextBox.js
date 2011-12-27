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

// Function to build the validation for text box
validateUrlTextBox= function(/*String*/ id){
  isValidUrlTextBox(id);
  var required = document.getElementById(id).getAttribute("required");
  if (required == "true") isMissingUrlTextBox(id);
}

isValidUrlTextBox= function(/*String*/ id){
  var isValid = this.isValidUrl(document.getElementById(id).value);
  var element = document.getElementById(id+"invalidSpan");
  if (isValid)
    element.style.display="none";
  else
    element.style.display="";
}

isMissingUrlTextBox= function(/*String*/ id){
  var isMissing = document.getElementById(id).value.length == 0;
  var element = document.getElementById(id+"missingSpan");
  if (isMissing)
    element.style.display="";
  else
    element.style.display="none";
}

isValidUrl = function(/*String*/str_url) {
  if (str_url.length == 0) return true;
// url parsing and formatting routimes. modify them if you wish other url format
  var re_date = /^([https?|ftp|file])+\:\/\/\/?(([A-Za-z0-9]+)(\.)?(\-)?)+((\/)([A-Za-z0-9\-\_]*(\.)?[A-Za-z0-9\-\_]*))*$/;
  if (!re_date.exec(str_url))
    return false;
  return (true);
}
