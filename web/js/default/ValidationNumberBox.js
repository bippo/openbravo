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
 * All portions are Copyright (C) 2001-2007 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

// Function to build the validation for text box
validateNumberBox= function(/*String*/ id){
  isValidNumberBox(id);
  var required = document.getElementById(id).getAttribute("required");
  if (required == "true") isMissingNumberBox(id);
}

isValidNumberBox= function(/*String*/ id){
  var isValid = isNumber(document.getElementById(id).value, true, true);
  var element = document.getElementById(id+"invalidSpan");
  if (isValid)
    element.style.display="none";
  else
    element.style.display="";
}

isMissingNumberBox= function(/*String*/ id){
  var isMissing = document.getElementById(id).value.length == 0;
  var element = document.getElementById(id+"missingSpan");
  if (isMissing)
    element.style.display="";
  else
    element.style.display="none";
}

isNumber= function(strValorNumerico, bolDecimales, bolNegativo) {
  var bolComa = false;
  var esNegativo = false;
  var i=0;
  if (strValorNumerico == null || strValorNumerico=="") return true;
  if (strValorNumerico.substring(i, i+1)=="-") {
    if (bolNegativo !=null && bolNegativo) {
      esNegativo = true;
      i++;
    } else {
      return false;
    }
  } else if (strValorNumerico.substring(i, i+1)=="+")
    i++;
  var total = strValorNumerico.length;
  for (i=i;i<total;i++) {
    if (isNaN(strValorNumerico.substring(i,i+1))) {
      if (bolDecimales && strValorNumerico.substring(i,i+1)=="." && !bolComa) 
        bolComa = true;
      else
        return false;
    }
  }
  return true;
}
