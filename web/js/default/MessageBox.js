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

// Available levels for the Message Box: HIDDEN, INFO, WARNING, ERROR

// Function to build the message body
//   Messages are built as a Html table with one line for each message.
setMessage_MessageBox= function(/*String*/ id, /*String*/ title, /*String*/ message){
    var span = document.getElementById(id+"Content").getElementsByTagName("span")[0];
    var div1 = document.createElement("div");
    div1.appendChild(document.createTextNode(title));
    div1.className = "MessageBox_TextTitle";
    div1.id = "messageBoxIDTitle";
    var div2 = document.createElement("div");
    div2.innerHTML = message;
    div2.className = "MessageBox_TextDescription";
    div2.id = "messageBoxIDMessage";
    var div3 = document.createElement("div");
    div3.className = "MessageBox_TextSeparator";
    span.appendChild(div1);
    span.appendChild(div2);
    span.appendChild(div3);
  }

// Function to reset the array of messages, hidding the box and removing all the messages
initialize_MessageBox= function (/*String*/ id){
    setType_MessageBox(id,"HIDDEN");
    var label=document.getElementById(id+"Content").getElementsByTagName("span")[0];
    while( label.hasChildNodes() ) { label.removeChild( label.lastChild ); }
}

// Function to add a value to the array of messages
setValues_MessageBox= function(/*String*/ id,/*String*/ type, /*String*/ title, /*String*/ message){
  var maxLevel_MessageBox = document.getElementById(id).className.toUpperCase().replace("MESSAGEBOX","");
  var strType = type.toUpperCase();
  if (maxLevel_MessageBox == "ERROR"){
  }
  else if (maxLevel_MessageBox == "WARNING"){
    if (strType == "ERROR"){
      maxLevel_MessageBox = strType;
    }
  }
  else if (maxLevel_MessageBox == "INFO"){
    if (strType == "ERROR" || strType == "WARNING"){
      maxLevel_MessageBox = strType;
    }
  }
  else if (maxLevel_MessageBox == "SUCCESS"){
    if (strType == "ERROR" || strType == "WARNING" || strType == "INFO"){
      maxLevel_MessageBox = strType;
    }
  }
  else if (maxLevel_MessageBox == "HIDDEN"){
    if (strType == "ERROR" || strType == "WARNING" || strType == "INFO" || strType == "SUCCESS"){
      maxLevel_MessageBox = strType;
    }
  }
  setType_MessageBox(id,maxLevel_MessageBox);
  setMessage_MessageBox(id, title, message);
}

// Function to set the class type
setType_MessageBox= function(/*String*/ id, /*String*/ type){
  document.getElementById(id).className = "MessageBox"+type.toUpperCase();
}
