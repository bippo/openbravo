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
 * All portions are Copyright (C) 2008-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

/**
* @fileoverview Contains methods for manage ajax in generic trees
*/

  var OB = OB || {};
  OB.GenericTree = OB.GenericTree || {};

  //Functions for ajax tree control
  function gt_callback(paramXMLParticular, XMLHttpRequestObj) {
    var strText = "";
    var id = "";
    var imageId = "";
    if (getReadyStateHandler(XMLHttpRequestObj)) {
      try {
        if (XMLHttpRequestObj.responseText) strText = XMLHttpRequestObj.responseText;
      } catch (e) {
      }
      if (paramXMLParticular!=null && paramXMLParticular.length>0) {
        id = paramXMLParticular[0];
        imageId = paramXMLParticular[1];
        folderId = paramXMLParticular[2];
      }
      layer(id, strText, true, false);
      gt_showHideLayer(id, imageId, folderId);
    }
    try {
      gt_adjustTreeWidth();
    } catch (e) {
    }
    return true;
  }

  function gt_showHideLayer(id, imageId, folderId) {
    var obj = getReference(imageId);
    
    var folder = getReference(folderId);
    var spanObj = getStyle(id);
    if (obj!=null) {
      if (obj.className == "Tree_Folder_Opened") {
        obj.className = "Tree_Folder_Closed";
        folder.className = "Tree_Checkbox_Spots_Closed";
        if (spanObj!=null) spanObj.display = "none";
      } else {
        obj.className = "Tree_Folder_Opened";
        folder.className = "Tree_Checkbox_Spots_Opened";
        if (spanObj!=null) spanObj.display = "";
      }
    }
  }
  
  function gt_updateData(CommandValue, id) {
    var frm = document.frmMain;
    frm.inpNodeId.value = id;
    frm.inpLevel.value = document.getElementById("inpLevel"+id).value;
    var dataLayer = readLayer("returnText" + id, true);
    if (dataLayer==null || dataLayer=="") {
      var paramXMLReq = new Array('returnText' + id, 'buttonTree' + id, 'folder'+id);
      return submitXmlHttpRequest(gt_callback, frm, CommandValue, "../utility/GenericTreeServlet.html", false, null, paramXMLReq);
    } else {
      gt_showHideLayer("returnText" + id, "buttonTree" + id, "folder"+id);
      try {
        gt_adjustTreeWidth();
      } catch (e) {
      }
    }
  }
  //----
  
  //Functions to manage descriptions with ajax
   function gt_callbackDescription(paramXMLParticular, XMLHttpRequestObj) {
    var strText = "";
    if (getReadyStateHandler(XMLHttpRequestObj)) {
      try {
        if (XMLHttpRequestObj.responseText) strText = XMLHttpRequestObj.responseText;
      } catch (e) {
      }
      document.getElementById('nodeDescription').innerHTML = strText;
      if (paramXMLParticular!=null && paramXMLParticular.length>0) {
        goToAnchor = paramXMLParticular[0];
        if (goToAnchor!=null && goToAnchor!='') goToDivAnchor('nodeDescription',goToAnchor);
      }
    }
    return true;
  }
  
  function gt_getDescription(id) {
    if (OB.GenericTree.doNotUpdateTreeInfo){
      return;
    }

    var frm = document.frmMain;
    frm.inpNodeId.value = id;
    var paramXMLReq = new Array('begin');
    return submitXmlHttpRequest(gt_callbackDescription, frm, "DESCRIPTION", "../utility/GenericTreeServlet.html", false, null, paramXMLReq);
  }
  
  function gt_getUpdateDescription(evt, id){
    if (!evt) evt = window.event;
    evt.cancelBubble = true;
    var frm = document.frmMain;
    frm.inpNodeId.value = id;
    var paramXMLReq = new Array('anchor');
    return submitXmlHttpRequest(gt_callbackDescription, frm, "DESCRIPTION", "../utility/GenericTreeServlet.html", false, null, paramXMLReq);
  }
  
  function gt_selectAllNodes(value){
    boxes = gt_getElementsByName('inpNodes','input');
    for (i=0; i<boxes.length; i++){
    	if(boxes[i].disabled == true){
    		continue;
    	}
    	boxes[i].checked=value;
    }
    gt_setActiveUninstall('buttonUninstall');
    gt_setActiveUninstall('buttonDisable');
  }

  var gt_focusedNode;
  var gt_focusedTreeContainer;

  function gt_getElementsByName(name,tag) {
    if (tag == null || tag == 'null' || tag == '') { tag='div' }
    var divArray = getElementsByName(name, tag);
    return divArray;
  }

  function gt_getElementByName(name) {
    var divArray = [];
    divArray = gt_getElementsByName(name);
    return divArray[0];
  }

  function gt_returnNodeObject(element, type) {
    try {
      var node;
      if (type=='node') { 
        element = 'node_' + element;
        type = 'name';
      }
      if (type=='id') {
        node = document.getElementById(element);
      } else if (type=='name') {
        node = gt_getElementByName(element);
      } else if (type=='obj' || type==null) {
        node = element;
      }
      return node;
    }
    catch (e) {
      return false;
    }

  }

  function gt_focusTreeContainer(element, type) {
    var tree = gt_returnNodeObject(element, type);
    gt_focusedTreeContainer = tree;
    if (tree.className.indexOf('Tree_Container_focus') == -1 && tree.className.indexOf('Tree_Container') != -1) {
      tree.className = tree.className.replace('Tree_Container','Tree_Container_focus');
    }
    if (gt_focusedNode == null) {
      gt_focusedNode = gt_getElementByName('node_1');
      gt_setFocusNode(gt_focusedNode);
    }
    isGenericTreeFocused = true;
    if (focusedWindowElement.getAttribute('id') != 'genericTree_dummy_input') {
      setWindowElementFocus('genericTree_dummy_input','id');
    }
    isClickOnGenericTree = true;
  }

  function gt_blurTreeContainer(element, type) {
    var tree = gt_returnNodeObject(element, type);
    if (tree.className.indexOf('Tree_Container_focus') != -1) {
      tree.className = tree.className.replace('Tree_Container_focus','Tree_Container');
    }
    gt_blurNode(gt_focusedNode);
    gt_focusedNode = null;
    isGenericTreeFocused = false;
  }

  function gt_focusNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    if (node.className.indexOf('Tree_Row_focus') == -1 && node.className.indexOf('Tree_Row') != -1) {
      if (node.className.indexOf('Tree_Row_hover') != -1) {
        node.className = node.className.replace('Tree_Row_hover','Tree_Row_focus');
      } else {
        node.className = node.className.replace('Tree_Row','Tree_Row_focus');
      }
    }
  }

  function gt_blurNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    if (node.className.indexOf('Tree_Row_focus') != -1) {
      node.className = node.className.replace('Tree_Row_focus','Tree_Row');
    }
  }

  function gt_hoverNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    if (node.className.indexOf('Tree_Row_hover') == -1 && node.className.indexOf('Tree_Row') != -1 && node.className.indexOf('Tree_Row_focus') == -1) {
      node.className = node.className.replace('Tree_Row','Tree_Row_hover');
    }
  }

  function gt_unhoverNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    if (node.className.indexOf('Tree_Row_hover') != -1 && node.className.indexOf('Tree_Row_focus') == -1) {
      node.className = node.className.replace('Tree_Row_hover','Tree_Row');
    }
  }

  function gt_setFocusNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    if (gt_focusedNode != null) {
      gt_blurNode(gt_focusedNode);
    }
    gt_focusedNode = node;
    gt_focusNode(gt_focusedNode);
    gt_updateViewPort();
    gt_executeFocusAction(gt_focusedNode);
  }

  function gt_returnPositionArrayToName(positionArray) {
    var id = 'node_';
    for(var i=0 ;i<positionArray.length; i++) {
     id += positionArray[i];
     if (i != positionArray.length-1) { id += "."; }
    }
    return id;
  }

  function gt_returnNameToPositionArray(id) {
    var positionArray = new Array();
    positionArray = id.replace('node_','').split(".");
    return positionArray;
  }

  function gt_isNodeVisible(element, type) {
    var node = gt_returnNodeObject(element, type);
    if (node == false || node == null) {
      return false
    }
    if (node.clientHeight > 2) {
      return true;
    } else {
      return false;
    }
  }

  function gt_returnNextNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var nextPositionArray = new Array();
    nextPositionArray = gt_returnNameToPositionArray(node.getAttribute('name'));
    nextPositionArray[nextPositionArray.length] = 1;
    if (!gt_isNodeVisible(gt_returnPositionArrayToName(nextPositionArray),'name')) {
      for (;;) {
        if (nextPositionArray.length == 1) {
          nextPositionArray[nextPositionArray.length-1] = 1;
          break;
        }
        nextPositionArray.splice(nextPositionArray.length-1,1);
        nextPositionArray[nextPositionArray.length-1] = Number(nextPositionArray[nextPositionArray.length-1])+1;
        if (gt_isNodeVisible(gt_returnPositionArrayToName(nextPositionArray),'name')) {
          break;
        }
      }
    }
    return gt_getElementByName(gt_returnPositionArrayToName(nextPositionArray));
  }

  function gt_returnPreviousNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var previousPositionArray = new Array();
    previousPositionArray = gt_returnNameToPositionArray(node.getAttribute('name'));
    if (previousPositionArray[previousPositionArray.length-1] == 1 && previousPositionArray.length > 1) { //if the previous node is the parent and not node_1
      previousPositionArray.splice(previousPositionArray.length-1,1);
    } else {
      if (previousPositionArray[previousPositionArray.length-1] == 1) { //if we are in node_1
        previousPositionArray.splice(previousPositionArray.length-1,1);
      } else {
        previousPositionArray[previousPositionArray.length-1] = Number(previousPositionArray[previousPositionArray.length-1])-1;
      }
      for (;;) {
        previousPositionArray[previousPositionArray.length] = '1';
        if (!gt_isNodeVisible(gt_returnPositionArrayToName(previousPositionArray),'name')) {
          previousPositionArray.splice(previousPositionArray.length-1,1);
          break; 
        }
        for (var i=1;;i++) {
          previousPositionArray[previousPositionArray.length-1] = i;
          if (!gt_isNodeVisible(gt_returnPositionArrayToName(previousPositionArray),'name')) {
            previousPositionArray[previousPositionArray.length-1] = i-1;
            if (previousPositionArray[previousPositionArray.length-1] == '0') {
              previousPositionArray.splice(previousPositionArray.length-1,1);
            }
            break;
          }
        }
      }
    }
    return gt_getElementByName(gt_returnPositionArrayToName(previousPositionArray));
  }

  function gt_returnParentNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var parentPositionArray = new Array();
    parentPositionArray = gt_returnNameToPositionArray(node.getAttribute('name'));
    if (parentPositionArray.length > 1) { //if the previous node is the parent and not a main node
      parentPositionArray.splice(parentPositionArray.length-1,1);
    }
    return gt_getElementByName(gt_returnPositionArrayToName(parentPositionArray));
  }

  function gt_updateViewPort() {
    if (gt_focusedTreeContainer != null) {
      var containerHeight = gt_focusedTreeContainer.clientHeight;
      var nodeHeight = gt_focusedNode.clientHeight;
      var containerScrollTop = gt_focusedTreeContainer.scrollTop;
      var nodeOffsetTop = gt_focusedNode.offsetTop;
      var maxPositionToBeVisible = containerHeight + containerScrollTop;
      var nodePosition = nodeHeight + nodeOffsetTop;
      var positionError_bottom = nodePosition - maxPositionToBeVisible;
      var positionError_top = containerScrollTop - nodeOffsetTop;
      if (positionError_bottom>0) {
        while (positionError_bottom>0) {
          gt_focusedTreeContainer.scrollTop += 1;
          containerScrollTop = gt_focusedTreeContainer.scrollTop;
          maxPositionToBeVisible = containerHeight + containerScrollTop;
          nodePosition = nodeHeight + nodeOffsetTop;
          positionError_bottom = nodePosition - maxPositionToBeVisible;
        }
      } else if (positionError_top>-4) {
        while (positionError_top>-4) {
          gt_focusedTreeContainer.scrollTop -= 1;
          containerScrollTop = gt_focusedTreeContainer.scrollTop;
          maxPositionToBeVisible = containerHeight + containerScrollTop;
          nodePosition = nodeHeight + nodeOffsetTop;
          positionError_top = containerScrollTop - nodeOffsetTop;
        }
      }
    }
  }

  function gt_goToNextNode() {
    gt_setFocusNode(gt_returnNextNode(gt_focusedNode));
  }

  function gt_goToPreviousNode() {
    gt_setFocusNode(gt_returnPreviousNode(gt_focusedNode));
  }

  function gt_goToParentNode() {
    gt_setFocusNode(gt_returnParentNode(gt_focusedNode));
  }

  function gt_openNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var nodeId = node.getAttribute('id').replace('node_','');
    if (document.getElementById('buttonTree' + nodeId).className.indexOf('Tree_Folder_Closed') != -1) {
      gt_updateData('OPENNODE', nodeId);
    }
  }

  function gt_closeNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var nodeId = node.getAttribute('id').replace('node_','');
    if (document.getElementById('buttonTree' + nodeId).className.indexOf('Tree_Folder_Opened') != -1) {
      gt_updateData('OPENNODE', nodeId);
    }
  }

  function gt_isClosedNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var nodeId = node.getAttribute('id').replace('node_','');
    if (document.getElementById('buttonTree' + nodeId).className.indexOf('Tree_Folder_Opened') != -1) {
      return false;
    } else {
      return true;
    }
  }

  function gt_checkNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var nodeId = node.getAttribute('id').replace('node_','');
    if (document.getElementById('inpNodes_' + nodeId)) {
      document.getElementById('inpNodes_' + nodeId).checked = true;
    }
  }

  function gt_uncheckNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var nodeId = node.getAttribute('id').replace('node_','');
    if (document.getElementById('inpNodes_' + nodeId)) {
      document.getElementById('inpNodes_' + nodeId).checked = false;
    }
  }

  function gt_checkToggleNode(element, type, isClick) {
    var node = gt_returnNodeObject(element, type);
    if (typeof node != "undefined") {
      var nodeId = node.getAttribute('id').replace('node_','');
      if (document.getElementById('inpNodes_' + nodeId)) {
        if (isClick == true && navigator.userAgent.toUpperCase().indexOf("MSIE") != -1) {
          gt_setActiveUninstall('buttonUninstall');
          gt_setActiveUninstall('buttonDisable');
        } else if (isClick == true) {
          setTimeout(function () {
            document.getElementById('inpNodes_' + nodeId).checked = !document.getElementById('inpNodes_' + nodeId).checked;
            gt_setActiveUninstall('buttonUninstall');
            gt_setActiveUninstall('buttonDisable');
          },10);
        } else {
          document.getElementById('inpNodes_' + nodeId).checked = !document.getElementById('inpNodes_' + nodeId).checked;
          gt_setActiveUninstall('buttonUninstall');
          gt_setActiveUninstall('buttonDisable');
        }
      }
    }
  }

  function gt_showNodeDescription(element, type) {
    var node = gt_returnNodeObject(element, type);
    var nodeId = node.getAttribute('id').replace('node_','');
    try {
      gt_getDescription(nodeId);
    } catch (e) {
    }
  }

  function gt_goToNodeLink(element, type) {
    var node = gt_returnNodeObject(element, type);
    var nodeId = node.getAttribute('id').replace('node_','');
    if (document.getElementById('link_' + nodeId)) {
      document.getElementById('link_' + nodeId).onclick();
    }
  }

  function gt_executeFocusAction(element, type) {
    var node = gt_returnNodeObject(element, type);
    if (node.getAttribute('onfocus')) {
      eval(node.getAttribute('onfocus'));
    }
  }

  function gt_returnParentNode(element, type) {
    var node = gt_returnNodeObject(element, type);
    var parentPositionArray = new Array();
    parentPositionArray = gt_returnNameToPositionArray(node.getAttribute('name'));
    if (parentPositionArray.length > 1) { //if the previous node is the parent and not a main node
      parentPositionArray.splice(parentPositionArray.length-1,1);
    }
    return gt_getElementByName(gt_returnPositionArrayToName(parentPositionArray));
  }
  
  function gt_setActiveUninstall(buttonName) {
    disableButton(buttonName);
    boxes = gt_getElementsByName('inpNodes','input');
    for (i=0; i<boxes.length; i++) {
      if (boxes[i].checked == true) {
        enableButton(buttonName);
        return;
      }
    }
  }

  function gt_adjustTreeWidth() {
    /*if (navigator.userAgent.toUpperCase().indexOf("MSIE")!=-1) {
      return true;
    }*/
    var gt_cont = document.getElementById('genericTreeRowContainer');
    var width_old = document.getElementById('genericTree').clientWidth;
    var height_old = gt_cont.clientHeight;
    var width_new = width_old + 3000;
    gt_cont.style.width = width_new;
    var height_new = gt_cont.clientHeight;
    if (height_old > height_new) {
      gt_cont.style.width = width_old;
      gt_changeTreeWidth();
    } else {
      gt_cont.style.width = width_old - 10;
    }
  }

  function gt_changeTreeWidth() {
    var gt_cont = document.getElementById('genericTreeRowContainer');
    var height_old = null;
    var height_new = null;
    var width_old = gt_cont.clientWidth;
    var width_new = gt_cont.clientWidth;
    for (var i=0; i+=5; i<2000) {
      height_old = gt_cont.clientHeight;
      width_new = width_old + i;
      gt_cont.style.width = width_new;
      height_new = gt_cont.clientHeight;
      if (height_old <= height_new) {
        width_new = width_old + i + 150;
        gt_cont.style.width = width_new;
        break;
      }
    }
  }


  //---
