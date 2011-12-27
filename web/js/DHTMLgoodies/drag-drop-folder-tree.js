/************************************************************************************************************
Drag and drop folder tree
Copyright (C) 2006  DTHMLGoodies.com, Alf Magne Kalleland

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

Dhtmlgoodies.com., hereby disclaims all copyright interest in this script
written by Alf Magne Kalleland.

Alf Magne Kalleland, 2006
Owner of DHTMLgoodies.com

************************************************************************************************************

This file was modified on 2007-Aug-10 by David Baz Fayos
All modifications © 2007 Openbravo, S.L.U.
You may contact the rights holder at www.openbravo.com

New features added:

* Now with each drop submitDraggingInfo() function is called (to raise an ajax event in the web which save the current change)
* All images now are loaded via CSS
* lockTree() and unlockTree() functions added to avoid accidental dragging
* The Tree now has to be defined into DIV so you can define de DIV size and properties (instead of be linked to the whole window)
* Added folder image status: opened and closed deppending if the node is expanded or not
* isElement property added to show an element image instead of a folder image
* noSiblings and noChildren properties now can be declared simultaniously to avoid any kind of drop in a node
* clearDestination() function added linked to onmouseout event. Now if you are not on a node, you won't be able to drop the dragged element
* isScrolling variable added to scroll the DIV containing the Tree while you are dragging a node by moving over the top or bottom area of the DIV
* A mechanism to expand a node if you are dragging by putting the cursor over a plus image
* The element is not dragged until you move the mouse

************************************************************************************************************/	
		
	var JSTreeObj;
	var treeUlCounter = 0;
	var nodeId = 1;

	/* Constructor */
	function JSDragDropTree()
	{
		var idOfTree;
		var imageFolder;
		var folderClosedClass;
		var folderOpenedClass;
		var plusClass;
		var minusClass;
		var maximumDepth;
		var dragNode_source;
		var dragNode_parent;
		var dragNode_sourceNextSib;
		var dragNode_noSiblings;
		var ajaxObjects;

		var dragNode_destination;
		var floatingContainer;
		var dragDropTimer;
		var dropTargetIndicator;
		var insertAsSub;
		var indicator_offsetX;
		var indicator_offsetX_sub;
		var indicator_offsetY;
    var dragStarPos_X;
    var dragStarPos_Y;

		this.imageFolder = 'images/';
    this.blankImage = 'blank.gif';
		this.folderClosedClass = 'DragDropTree_folderClosed_icon';
		this.folderOpenedClass = 'DragDropTree_folderOpened_icon';
		this.elementClass = 'DragDropTree_element_icon';
		this.plusClass = 'DragDropTree_plus_icon';
		this.minusClass = 'DragDropTree_minus_icon';
		this.dragDrop_ind1 = 'DragDropTree_sibling_pos_icon';
		this.dragDrop_ind2 = 'DragDropTree_child_pos_icon';
		this.maximumDepth = 6;
		var messageMaximumDepthReached;
		var filePathRenameItem;
		var filePathDeleteItem;
		var additionalRenameRequestParameters = {};
		var additionalDeleteRequestParameters = {};

    var isDragging;
    var isOnPlusMinus;
    var isLocked;

    var isInitializingDrag;

		var renameAllowed;
		var deleteAllowed;
		var currentlyActiveItem;
		var contextMenu;
		var currentItemToEdit;		// Reference to item currently being edited(example: renamed)
		var helpObj;

		this.contextMenu = false;
		this.floatingContainer = document.createElement('UL');
		this.floatingContainer.style.position = 'absolute';
		this.floatingContainer.style.display='none';
		this.floatingContainer.id = 'floatingContainer';
		this.insertAsSub = false;
		document.body.appendChild(this.floatingContainer);
		this.dragDropTimer = -1;
		this.dragNode_noSiblings = false;
		this.currentItemToEdit = false;
		if(document.all){
			this.indicator_offsetX = 2;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 4;
			this.indicator_offsetY = 8;
		}else{
			this.indicator_offsetX = 0;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 2;
			this.indicator_offsetY = 11;
		}
		if(navigator.userAgent.indexOf('MSIE')>=0){
			this.indicator_offsetX = 1;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 3;
			this.indicator_offsetY = 2;	
		}
		if(navigator.userAgent.indexOf('Opera')>=0){
			this.indicator_offsetX = 2;	// Offset position of small black lines indicating where nodes would be dropped.
			this.indicator_offsetX_sub = 3;
			this.indicator_offsetY = -7;
		}

		this.messageMaximumDepthReached = ''; // Use '' if you don't want to display a message 

		this.renameAllowed = false;
		this.deleteAllowed = false;
		this.currentlyActiveItem = false;
		this.filePathRenameItem = 'folderTree_updateItem.php';
		this.filePathDeleteItem = 'folderTree_updateItem.php';
		this.ajaxObjects = new Array();
		this.helpObj = false;

		this.RENAME_STATE_BEGIN = 1;
		this.RENAME_STATE_CANCELED = 2;
		this.RENAME_STATE_REQUEST_SENDED = 3;
		this.renameState = null;
	}


	/* JSDragDropTree class */
	JSDragDropTree.prototype = {
		// {{{ addEvent()
	    /**
	     *
	     *  This function adds an event listener to an element on the page.
	     *
	     *	@param Object whichObject = Reference to HTML element(Which object to assigne the event)
	     *	@param String eventType = Which type of event, example "mousemove" or "mouseup"
	     *	@param functionName = Name of function to execute. 
	     * 
	     * @public
	     */	
		addEvent : function(whichObject,eventType,functionName)
		{ 
		  if(whichObject.attachEvent){ 
		    whichObject['e'+eventType+functionName] = functionName; 
		    whichObject[eventType+functionName] = function(){whichObject['e'+eventType+functionName]( window.event );} 
		    whichObject.attachEvent( 'on'+eventType, whichObject[eventType+functionName] ); 
		  } else 
		    whichObject.addEventListener(eventType,functionName,false); 	    
		} 
		// }}}
		,
		// {{{ removeEvent()
	    /**
	     *
	     *  This function removes an event listener from an element on the page.
	     *
	     *	@param Object whichObject = Reference to HTML element(Which object to assigne the event)
	     *	@param String eventType = Which type of event, example "mousemove" or "mouseup"
	     *	@param functionName = Name of function to execute. 
	     * 
	     * @public
	     */		
		removeEvent : function(whichObject,eventType,functionName) {
		  if(whichObject.detachEvent){ 
		    whichObject.detachEvent('on'+eventType, whichObject[eventType+functionName]); 
		    whichObject[eventType+functionName] = null; 
		  } else 
		    whichObject.removeEventListener(eventType,functionName,false); 
		}
		,
		Get_Cookie : function(name) { 
		   var start = document.cookie.indexOf(name+"="); 
		   var len = start+name.length+1; 
		   if ((!start) && (name != document.cookie.substring(0,name.length))) return null; 
		   if (start == -1) return null; 
		   var end = document.cookie.indexOf(";",len); 
		   if (end == -1) end = document.cookie.length; 
		   return unescape(document.cookie.substring(len,end)); 
		}
		,
		// This function has been slightly modified
		Set_Cookie : function(name,value,expires,path,domain,secure) { 
			expires = expires * 60*60*24*1000;
			var today = new Date();
			var expires_date = new Date( today.getTime() + (expires) );
		    var cookieString = name + "=" +escape(value) + 
		       ( (expires) ? ";expires=" + expires_date.toGMTString() : "") + 
		       ( (path) ? ";path=" + path : "") + 
		       ( (domain) ? ";domain=" + domain : "") + 
		       ( (secure) ? ";secure" : ""); 
		    document.cookie = cookieString; 
		} 
		,
		setFileNameRename : function(newFileName)
		{
			this.filePathRenameItem = newFileName;
		}
		,
		setFileNameDelete : function(newFileName)
		{
			this.filePathDeleteItem = newFileName;
		}
		,
		setAdditionalRenameRequestParameters : function(requestParameters)
		{
			this.additionalRenameRequestParameters = requestParameters;
		}
		,
		setAdditionalDeleteRequestParameters : function(requestParameters)
		{
			this.additionalDeleteRequestParameters = requestParameters;
		}
		,setRenameAllowed : function(renameAllowed)
		{
			this.renameAllowed = renameAllowed;			
		}
		,
		setDeleteAllowed : function(deleteAllowed)
		{
			this.deleteAllowed = deleteAllowed;	
		}
		,setMaximumDepth : function(maxDepth)
		{
			this.maximumDepth = maxDepth;	
		}
		,setMessageMaximumDepthReached : function(newMessage)
		{
			this.messageMaximumDepthReached = newMessage;
		}
		,setBlankImage : function(imagePath)
		{
			this.blankImage = imagePath;	
		}
		,
		setImageFolder : function(path)
		{
			this.imageFolder = path;	
		}
		,
		setfolderClosedClass : function(imageClass)
		{
			this.folderClosedClass = imageClass;			
		}
		,
		setfolderOpenedClass : function(imageClass)
		{
			this.folderOpenedClass = imageClass;			
		}
		,
		setElementClass : function(imageClass)
		{
			this.elementClass = imageClass;			
		}
		,
		setPlusClass : function(imageClass)
		{
			this.plusClass = imageClass;				
		}
		,
		setMinusClass : function(imageClass)
		{
			this.minusClass = imageClass;			
		}
		,		
		setTreeId : function(idOfTree)
		{
			this.idOfTree = idOfTree;			
		}	
		,
		lockTree : function()
		{
      JSTreeObj.isLocked = true;
      return true;
		}	
		,
		unlockTree : function()
		{
      JSTreeObj.isLocked = false;
      return true;
		}	
		,
		expandAll : function()
		{
			var menuItems = document.getElementById(this.idOfTree).getElementsByTagName('LI');
			for(var no=0;no<menuItems.length;no++){
				var subItems = menuItems[no].getElementsByTagName('UL');
				if(subItems.length>0 && subItems[0].style.display!='block'){
					JSTreeObj.showHideNode(false,menuItems[no].id);
				}			
			}
		}	
		,
		collapseAll : function()
		{
			var menuItems = document.getElementById(this.idOfTree).getElementsByTagName('LI');
			for(var no=0;no<menuItems.length;no++){
				var subItems = menuItems[no].getElementsByTagName('UL');
				if(subItems.length>0 && subItems[0].style.display=='block'){
					JSTreeObj.showHideNode(false,menuItems[no].id);
				}			
			}		
		}	
		,
		expandNode : function(node)
		{
			var no = node;
			var menuItems = document.getElementById(this.idOfTree).getElementsByTagName('LI');
			if(no<menuItems.length) {
				var subItems = menuItems[no].getElementsByTagName('UL');
				if(subItems.length>0 && subItems[0].style.display!='block'){
					JSTreeObj.showHideNode(false,menuItems[no].id);
				}
			}
		}	
		,
		/*
		Find top pos of a tree node
		*/
		getTopPos : function(obj){
      var menuDiv = document.getElementById(this.idOfTree);
			var top = (obj.offsetTop - menuDiv.scrollTop)/1;
			while((obj = obj.offsetParent) != null){
				if(obj.tagName!='HTML')top += obj.offsetTop;
			}			
			if(document.all)top = top/1 + 13; else top = top/1 + 4;
			return top;
		}
		,
		/*
		Find left pos of a tree node
		*/
		getLeftPos : function(obj){
			var left = obj.offsetLeft/1 + 1;
			while((obj = obj.offsetParent) != null){
				if(obj.tagName!='HTML')left += obj.offsetLeft;
			}

			if(document.all)left = left/1 - 2;
			return left;
		}

		,
		showHideNode : function(e,inputId)
		{
			if(inputId){
				if(!document.getElementById(inputId))return;
				thisNode = document.getElementById(inputId).getElementsByTagName('IMG')[0];
        thisNode2 = document.getElementById(inputId).getElementsByTagName('IMG')[1];
			} else {
				thisNode = this.parentNode.getElementsByTagName('IMG')[0];
				thisNode2 = this.parentNode.getElementsByTagName('IMG')[1];
			}
			if(thisNode.style.visibility=='hidden')return;
			var parentNode = thisNode.parentNode;
			inputId = parentNode.id.replace(/[^0-9]/g,'');
			if(thisNode.className.indexOf(JSTreeObj.plusClass)>=0 || thisNode2.className.indexOf(JSTreeObj.folderClosedClass)>=0){
				thisNode.className = thisNode.className.replace(JSTreeObj.plusClass,JSTreeObj.minusClass);
        thisNode2.className = thisNode2.className.replace(JSTreeObj.folderClosedClass,JSTreeObj.folderOpenedClass);
				var ul = parentNode.getElementsByTagName('UL')[0];
				ul.style.display='block';
				if(!initExpandedNodes)initExpandedNodes = ',';
				if(initExpandedNodes.indexOf(',' + inputId + ',')<0) initExpandedNodes = initExpandedNodes + inputId + ',';
			}else{
				thisNode.className = thisNode.className.replace(JSTreeObj.minusClass,JSTreeObj.plusClass);
        thisNode2.className = thisNode2.className.replace(JSTreeObj.folderOpenedClass,JSTreeObj.folderClosedClass);
				parentNode.getElementsByTagName('UL')[0].style.display='none';
				initExpandedNodes = initExpandedNodes.replace(',' + inputId,'');
			}
			JSTreeObj.Set_Cookie('dhtmlgoodies_expandedNodes',initExpandedNodes,500);
			return false;
		}
		,
		showNode : function(thisNode, thisNode2)
		{
      if(JSTreeObj.isDragging == true && JSTreeObj.isOnPlusMinus == true) {
        if(thisNode.style.visibility=='hidden')return;
        var parentNode = thisNode.parentNode;
        inputId = parentNode.id.replace(/[^0-9]/g,'');
        if(thisNode.className.indexOf(JSTreeObj.plusClass)>=0 || thisNode2.className.indexOf(JSTreeObj.folderClosedClass)>=0){
          thisNode.className = thisNode.className.replace(JSTreeObj.plusClass,JSTreeObj.minusClass);
          thisNode2.className = thisNode2.className.replace(JSTreeObj.folderClosedClass,JSTreeObj.folderOpenedClass);
          var ul = parentNode.getElementsByTagName('UL')[0];
          ul.style.display='block';
          if(!initExpandedNodes)initExpandedNodes = ',';
          if(initExpandedNodes.indexOf(',' + inputId + ',')<0) initExpandedNodes = initExpandedNodes + inputId + ',';
        }
        JSTreeObj.Set_Cookie('dhtmlgoodies_expandedNodes',initExpandedNodes,500);
        return false;
      }
		}
		,
    showNodeToDrag : function(e)
    {
      JSTreeObj.isOnPlusMinus = true;
      thisNode = this.parentNode.getElementsByTagName('IMG')[0];
      thisNode2 = this.parentNode.getElementsByTagName('IMG')[1];
      setTimeout("JSTreeObj.showNode(thisNode, thisNode2)",750);
    }
    ,
    PlusMinusOut : function ()
    {
      JSTreeObj.isOnPlusMinus = false;
    }
    ,
		/* Initialize drag */
		initDrag : function(e)
		{
      if(JSTreeObj.isLocked != true) {
        if(document.all)e = event;
        var subs = JSTreeObj.floatingContainer.getElementsByTagName('LI');
        if(subs.length>0){
          if(JSTreeObj.dragNode_sourceNextSib){
            JSTreeObj.dragNode_parent.insertBefore(JSTreeObj.dragNode_source,JSTreeObj.dragNode_sourceNextSib);
          }else{
            JSTreeObj.dragNode_parent.appendChild(JSTreeObj.dragNode_source);
          }
        }

        JSTreeObj.dragNode_source = this.parentNode;
        JSTreeObj.dragNode_parent = this.parentNode.parentNode;
        JSTreeObj.dragNode_sourceNextSib = false;


        if(JSTreeObj.dragNode_source.nextSibling)JSTreeObj.dragNode_sourceNextSib = JSTreeObj.dragNode_source.nextSibling;
        JSTreeObj.dragStarPos_X = e.clientX/1;
        JSTreeObj.dragStarPos_Y = e.clientY/1;
        JSTreeObj.dragNode_destination = false;
        JSTreeObj.dragDropTimer = 0;
        JSTreeObj.isDragging = false;
        JSTreeObj.timerDrag();
        return false;
      }

		}
		,
		timerDrag : function()
		{
			if(this.dragDropTimer>=0 && this.dragDropTimer<10){
				this.dragDropTimer = this.dragDropTimer + 1;
				setTimeout('JSTreeObj.timerDrag()',20);
				return;
			}
			if(this.dragDropTimer==10)
			{
        JSTreeObj.isInitializingDrag = true;
			}
		}
		,
		moveDragableNodes : function(e)
		{
			if(JSTreeObj.dragDropTimer<10)return;
			if(document.all)e = event;
      if(e.clientY/1 > (JSTreeObj.dragStarPos_Y - 10) && 
        e.clientY/1 < (JSTreeObj.dragStarPos_Y + 10) && 
        e.clientX/1 > (JSTreeObj.dragStarPos_X - 15) && 
        e.clientX/1 < (JSTreeObj.dragStarPos_X + 15) && 
        JSTreeObj.isDragging == false)return;
      if(JSTreeObj.isInitializingDrag == true) {
			  JSTreeObj.floatingContainer.style.display='block';
  		  JSTreeObj.floatingContainer.appendChild(JSTreeObj.dragNode_source);
        JSTreeObj.isInitializingDrag = false;
      }
      enableScroll(true);
      JSTreeObj.isDragging = true;
			dragDrop_x = e.clientX/1 + 5 + document.body.scrollLeft;
			dragDrop_y = e.clientY/1 + 5 + document.body.scrollTop;

			JSTreeObj.floatingContainer.style.left = dragDrop_x + 'px';
			JSTreeObj.floatingContainer.style.top = dragDrop_y + 'px';

			var thisObj = this;
			if(thisObj.tagName=='A' || thisObj.tagName=='IMG')thisObj = thisObj.parentNode;

			JSTreeObj.dragNode_noSiblings = false;
			var tmpVar = thisObj.getAttribute('noSiblings');
			if(!tmpVar)tmpVar = thisObj.noSiblings;
			if(tmpVar=='true')JSTreeObj.dragNode_noSiblings=true;

			if(thisObj && thisObj.id)
			{
				JSTreeObj.dragNode_destination = thisObj;
				var img = thisObj.getElementsByTagName('IMG')[1];
				var tmpObj= JSTreeObj.dropTargetIndicator;
				tmpObj.style.display='block';

				var eventSourceObj = this;
				if(JSTreeObj.dragNode_noSiblings && eventSourceObj.tagName=='IMG')eventSourceObj = eventSourceObj.nextSibling;

				var tmpImg = tmpObj.getElementsByTagName('IMG')[0];
				if(this.tagName=='A' || JSTreeObj.dragNode_noSiblings){
					tmpImg.className = JSTreeObj.dragDrop_ind2;
					JSTreeObj.insertAsSub = true;
					tmpObj.style.left = (JSTreeObj.getLeftPos(eventSourceObj) + JSTreeObj.indicator_offsetX_sub) + 'px';
				}else{
					tmpImg.className = JSTreeObj.dragDrop_ind1;
					JSTreeObj.insertAsSub = false;
					tmpObj.style.left = (JSTreeObj.getLeftPos(eventSourceObj) + JSTreeObj.indicator_offsetX) + 'px';
				}


				tmpObj.style.top = (JSTreeObj.getTopPos(thisObj) + JSTreeObj.indicator_offsetY) + 'px';
			}

			return false;

		}
		,
		dropDragableNodes:function()
		{
      var strChild;
      var strTop;
      var strLink;
			if(JSTreeObj.dragDropTimer<10){
				JSTreeObj.dragDropTimer = -1;
				return;
			}
			var showMessage = false;
			if(JSTreeObj.dragNode_destination){	// Check depth
				var countUp = JSTreeObj.dragDropCountLevels(JSTreeObj.dragNode_destination,'up');
				var countDown = JSTreeObj.dragDropCountLevels(JSTreeObj.dragNode_source,'down');
				var countLevels = countUp/1 + countDown/1 + (JSTreeObj.insertAsSub?1:0);

				if(countLevels>JSTreeObj.maximumDepth){
					JSTreeObj.dragNode_destination = false;
					showMessage = true; 	// Used later down in this function
				}
			}


			if(JSTreeObj.dragNode_destination){
        strTop = JSTreeObj.dragNode_destination.id
        strLink = JSTreeObj.dragNode_source.id;
				if(JSTreeObj.insertAsSub){
          strChild = 'Y';
					var uls = JSTreeObj.dragNode_destination.getElementsByTagName('UL');
					if(uls.length>0){
						ul = uls[0];
						ul.style.display='block';

						var lis = ul.getElementsByTagName('LI');

						if(lis.length>0){	// Sub elements exists - drop dragable node before the first one
							ul.insertBefore(JSTreeObj.dragNode_source,lis[0]);
						}else {	// No sub exists - use the appendChild method - This line should not be executed unless there's something wrong in the HTML, i.e empty <ul>
							ul.appendChild(JSTreeObj.dragNode_source);
						}
					}else{

						var ul = document.createElement('UL');
						ul.style.display='block';
						JSTreeObj.dragNode_destination.appendChild(ul);
						ul.appendChild(JSTreeObj.dragNode_source);
					}
					var img = JSTreeObj.dragNode_destination.getElementsByTagName('IMG')[0];
          var img2 = JSTreeObj.dragNode_destination.getElementsByTagName('IMG')[1];
					img.style.visibility='visible';
					img.className = img.className.replace(JSTreeObj.plusClass,JSTreeObj.minusClass);
          img2.className = img2.className.replace(JSTreeObj.folderClosedClass,JSTreeObj.folderOpenedClass);


				}else{
          strChild = 'N';
					if(JSTreeObj.dragNode_destination.nextSibling){
						var nextSib = JSTreeObj.dragNode_destination.nextSibling;
						nextSib.parentNode.insertBefore(JSTreeObj.dragNode_source,nextSib);
					}else{
						JSTreeObj.dragNode_destination.parentNode.appendChild(JSTreeObj.dragNode_source);
					}
				}
				/* Clear parent object */
				var tmpObj = JSTreeObj.dragNode_parent;
				var lis = tmpObj.getElementsByTagName('LI');
				if(lis.length==0){
					var img = tmpObj.parentNode.getElementsByTagName('IMG')[0];
          var img2 = tmpObj.parentNode.getElementsByTagName('IMG')[1];
					img.style.visibility='hidden';	// Hide [+],[-] icon
          img2.className = img2.className.replace(JSTreeObj.folderOpenedClass,JSTreeObj.folderClosedClass);
					tmpObj.parentNode.removeChild(tmpObj);
				}
        submitDraggingInfo(strTop, strLink, strChild);



			}else{
				// Putting the item back to it's original location

				if(JSTreeObj.dragNode_sourceNextSib){
					JSTreeObj.dragNode_parent.insertBefore(JSTreeObj.dragNode_source,JSTreeObj.dragNode_sourceNextSib);
				}else{
					JSTreeObj.dragNode_parent.appendChild(JSTreeObj.dragNode_source);
				}

			}
      enableScroll(false);
      JSTreeObj.isDragging = false;
			JSTreeObj.dropTargetIndicator.style.display='none';
			JSTreeObj.dragDropTimer = -1;
			if(showMessage && JSTreeObj.messageMaximumDepthReached)alert(JSTreeObj.messageMaximumDepthReached);
		}
		,
		createDropIndicator : function()
		{
			this.dropTargetIndicator = document.createElement('DIV');
			this.dropTargetIndicator.style.position = 'absolute';
			this.dropTargetIndicator.style.display='none';
			var img = document.createElement('IMG');
			img.src = this.imageFolder + this.blankImage;
      img.className = this.dragDrop_ind1;
			img.id = 'dragDropIndicatorImage';
			this.dropTargetIndicator.appendChild(img);
			document.body.appendChild(this.dropTargetIndicator);

		}
		,
		dragDropCountLevels : function(obj,direction,stopAtObject){
			var countLevels = 0;
			if(direction=='up'){
				while(obj.parentNode && obj.parentNode!=stopAtObject){
					obj = obj.parentNode;
					if(obj.tagName=='UL')countLevels = countLevels/1 +1;
				}
				return countLevels;
			}

			if(direction=='down'){ 
				var subObjects = obj.getElementsByTagName('LI');
				for(var no=0;no<subObjects.length;no++){
					countLevels = Math.max(countLevels,JSTreeObj.dragDropCountLevels(subObjects[no],"up",obj));
				}
				return countLevels;
			}
		}
		,
		cancelEvent : function()
		{
			return false;	
		}
		,
		cancelSelectionEvent : function()
		{

			if(JSTreeObj.dragDropTimer<10)return true;
			return false;
		}
		,getNodeOrders : function(initObj,saveString)
		{

			if(!saveString)var saveString = '';
			if(!initObj){
				initObj = document.getElementById(this.idOfTree);

			}
			var lis = initObj.getElementsByTagName('LI');

			if(lis.length>0){
				var li = lis[0];
				while(li){
					if(li.id){
						if(saveString.length>0)saveString = saveString + ',';
						var numericID = li.id.replace(/[^0-9]/gi,'');
						if(numericID.length==0)numericID='A';
						var numericParentID = li.parentNode.parentNode.id.replace(/[^0-9]/gi,'');
						if(numericID!='0'){
							saveString = saveString + numericID;
							saveString = saveString + '-';


							if(li.parentNode.id!=this.idOfTree)saveString = saveString + numericParentID; else saveString = saveString + '0';
						}
						var ul = li.getElementsByTagName('UL');
						if(ul.length>0){
							saveString = this.getNodeOrders(ul[0],saveString);
						}
					}
					li = li.nextSibling;
				}
			}

			if(initObj.id == this.idOfTree){
				return saveString;

			}
			return saveString;
		}
		,highlightItem : function(inputObj,e)
		{
			if(JSTreeObj.currentlyActiveItem)JSTreeObj.currentlyActiveItem.className = '';
			this.className = 'highlightedNodeItem';
			JSTreeObj.currentlyActiveItem = this;
		}
		,
		removeHighlight : function()
		{
			if(JSTreeObj.currentlyActiveItem)JSTreeObj.currentlyActiveItem.className = '';
			JSTreeObj.currentlyActiveItem = false;
		}
		,
		hasSubNodes : function(obj)
		{
			var subs = obj.getElementsByTagName('LI');
			if(subs.length>0)return true;
			return false;	
		}
		,
		deleteItem : function(obj1,obj2)
		{
			var message = 'Click OK to delete item ' + obj2.innerHTML;
			if(this.hasSubNodes(obj2.parentNode)) message = message + ' and it\'s sub nodes';
			if(confirm(message)){
				this.__deleteItem_step2(obj2.parentNode);	// Sending <LI> tag to the __deleteItem_step2 method
			}

		}
		,
		__refreshDisplay : function(obj)
		{
			if(this.hasSubNodes(obj))return;

			var img = obj.getElementsByTagName('IMG')[0];
			img.style.visibility = 'hidden';	
		}
		,
		__deleteItem_step2 : function(obj)
		{

			var saveString = obj.id.replace(/[^0-9]/gi,'');
			
			var lis = obj.getElementsByTagName('LI');
			for(var no=0;no<lis.length;no++){
				saveString = saveString + ',' + lis[no].id.replace(/[^0-9]/gi,'');
			}

			// Creating ajax object and send items
			var ajaxIndex = JSTreeObj.ajaxObjects.length;
			JSTreeObj.ajaxObjects[ajaxIndex] = new sack();
			JSTreeObj.ajaxObjects[ajaxIndex].method = "GET";
			JSTreeObj.ajaxObjects[ajaxIndex].setVar("deleteIds", saveString);
			JSTreeObj.__addAdditionalRequestParameters(JSTreeObj.ajaxObjects[ajaxIndex], JSTreeObj.additionalDeleteRequestParameters);
			JSTreeObj.ajaxObjects[ajaxIndex].requestFile = JSTreeObj.filePathDeleteItem;	// Specifying which file to get
			JSTreeObj.ajaxObjects[ajaxIndex].onCompletion = function() { JSTreeObj.__deleteComplete(ajaxIndex,obj); } ;	// Specify function that will be executed after file has been found
			JSTreeObj.ajaxObjects[ajaxIndex].runAJAX();		// Execute AJAX function


		}
		,
		__deleteComplete : function(ajaxIndex,obj)
		{
			if(this.ajaxObjects[ajaxIndex].response!='OK'){
				alert('ERROR WHEN TRYING TO DELETE NODE: ' + this.ajaxObjects[ajaxIndex].response); 	// Rename failed
			}else{
				var parentRef = obj.parentNode.parentNode;
				obj.parentNode.removeChild(obj);
				this.__refreshDisplay(parentRef);

			}
			
		}
		,
		__renameComplete : function(ajaxIndex)
		{
			if(this.ajaxObjects[ajaxIndex].response!='OK'){
				alert('ERROR WHEN TRYING TO RENAME NODE: ' + this.ajaxObjects[ajaxIndex].response); 	// Rename failed
			}
		}
		,
		__saveTextBoxChanges : function(e,inputObj)
		{
			if(!inputObj && this)inputObj = this;
			if(document.all)e = event;
			if(e.keyCode && e.keyCode==27){
				JSTreeObj.__cancelRename(e,inputObj);
				return;
			}
			inputObj.style.display='none';
			inputObj.nextSibling.style.visibility='visible';
			if(inputObj.value.length>0){
				inputObj.nextSibling.innerHTML = inputObj.value;
				// Send changes to the server.
				if (JSTreeObj.renameState != JSTreeObj.RENAME_STATE_BEGIN) {
					return;
				}
				JSTreeObj.renameState = JSTreeObj.RENAME_STATE_REQUEST_SENDED;

				var ajaxIndex = JSTreeObj.ajaxObjects.length;
				JSTreeObj.ajaxObjects[ajaxIndex] = new sack();
				JSTreeObj.ajaxObjects[ajaxIndex].method = "GET";
				JSTreeObj.ajaxObjects[ajaxIndex].setVar("renameId", inputObj.parentNode.id.replace(/[^0-9]/gi,''));
				JSTreeObj.ajaxObjects[ajaxIndex].setVar("newName", inputObj.value);
				JSTreeObj.__addAdditionalRequestParameters(JSTreeObj.ajaxObjects[ajaxIndex], JSTreeObj.additionalRenameRequestParameters);
				JSTreeObj.ajaxObjects[ajaxIndex].requestFile = JSTreeObj.filePathRenameItem;	// Specifying which file to get
				JSTreeObj.ajaxObjects[ajaxIndex].onCompletion = function() { JSTreeObj.__renameComplete(ajaxIndex); } ;	// Specify function that will be executed after file has been found
				JSTreeObj.ajaxObjects[ajaxIndex].runAJAX();		// Execute AJAX function		



			}
		}
		,
		__cancelRename : function(e,inputObj)
		{
			JSTreeObj.renameState = JSTreeObj.RENAME_STATE_CANCELD;
			if(!inputObj && this)inputObj = this;
			inputObj.value = JSTreeObj.helpObj.innerHTML;
			inputObj.nextSibling.innerHTML = JSTreeObj.helpObj.innerHTML;
			inputObj.style.display = 'none';
			inputObj.nextSibling.style.visibility = 'visible';
		}
		,
		__renameCheckKeyCode : function(e)
		{
			if(document.all)e = event;
			if(e.keyCode==13){	// Enter pressed
				JSTreeObj.__saveTextBoxChanges(false,this);
			}	
			if(e.keyCode==27){	// ESC pressed
				JSTreeObj.__cancelRename(false,this);
			}
		}
		,
		__createTextBox : function(obj)
		{
			var textBox = document.createElement('INPUT');
			textBox.className = 'folderTreeTextBox';
			textBox.value = obj.innerHTML;
			obj.parentNode.insertBefore(textBox,obj);
			textBox.id = 'textBox' + obj.parentNode.id.replace(/[^0-9]/gi,'');
			textBox.onblur = this.__saveTextBoxChanges;
			textBox.onkeydown = this.__renameCheckKeyCode;
			this.__renameEnableTextBox(obj);
		}
		,
		__renameEnableTextBox : function(obj)
		{
			JSTreeObj.renameState = JSTreeObj.RENAME_STATE_BEGIN;
			obj.style.visibility = 'hidden';
			obj.previousSibling.value = obj.innerHTML;
			obj.previousSibling.style.display = 'inline';
			obj.previousSibling.select();
		}
		,
		renameItem : function(obj1,obj2)
		{
			currentItemToEdit = obj2.parentNode;	// Reference to the <li> tag.
			if(!obj2.previousSibling || obj2.previousSibling.tagName.toLowerCase()!='input'){
				this.__createTextBox(obj2);
			}else{
				this.__renameEnableTextBox(obj2);
			}
			this.helpObj.innerHTML = obj2.innerHTML;

		}
		,
		clearDestination: function(e)
		{
      JSTreeObj.dropTargetIndicator.style.display = 'none';
      JSTreeObj.dragNode_destination = false;
		}
		,
		initTree : function()
		{
			JSTreeObj = this;
			JSTreeObj.createDropIndicator();
			document.documentElement.onselectstart = JSTreeObj.cancelSelectionEvent;
			document.documentElement.ondragstart = JSTreeObj.cancelEvent;
			document.documentElement.onmousedown = JSTreeObj.removeHighlight;

			/* Creating help object for storage of values */
			this.helpObj = document.createElement('DIV');
			this.helpObj.style.display = 'none';
			document.body.appendChild(this.helpObj);

			/* Create context menu */
			if(this.deleteAllowed || this.renameAllowed){
				try{
					/* Creating menu model for the context menu, i.e. the datasource */
					var menuModel = new DHTMLGoodies_menuModel();
					if(this.deleteAllowed)menuModel.addItem(1,'Delete','','',false,'JSTreeObj.deleteItem');
					if(this.renameAllowed)menuModel.addItem(2,'Rename','','',false,'JSTreeObj.renameItem');
					menuModel.init();

					var menuModelRenameOnly = new DHTMLGoodies_menuModel();
					if(this.renameAllowed)menuModelRenameOnly.addItem(3,'Rename','','',false,'JSTreeObj.renameItem');
					menuModelRenameOnly.init();

					var menuModelDeleteOnly = new DHTMLGoodies_menuModel();
					if(this.deleteAllowed)menuModelDeleteOnly.addItem(4,'Delete','','',false,'JSTreeObj.deleteItem');
					menuModelDeleteOnly.init();

					window.refToDragDropTree = this;

					this.contextMenu = new DHTMLGoodies_contextMenu();
					this.contextMenu.setWidth(120);
					referenceToDHTMLSuiteContextMenu = this.contextMenu;
				}catch(e){

				}
			}

			var nodeId = 0;
			var dhtmlgoodies_tree = document.getElementById(this.idOfTree);
			var menuItems = dhtmlgoodies_tree.getElementsByTagName('LI');	// Get an array of all menu items
			var menuItemsLen = menuItems.length;
			for(var no=0;no<menuItemsLen;no++){
				var currItem = menuItems[no];
				// Is child var set ?
        var isElement = false;
				var tmpVar = currItem.getAttribute('isElement');
				if(!tmpVar)tmpVar = currItem.isElement;
				if(tmpVar=='true')isElement=true;
				// No children var set ?
				var noChildren = false;
				var tmpVar = currItem.getAttribute('noChildren');
				if(!tmpVar)tmpVar = currItem.noChildren;
				if(tmpVar=='true')noChildren=true;
				// No drag var set ?
				var noDrag = false;
				var tmpVar = currItem.getAttribute('noDrag');
				if(!tmpVar)tmpVar = currItem.noDrag;
				if(tmpVar=='true')noDrag=true;
				// No siblings var set ?
				var noSiblings = false;
				var tmpVar = currItem.getAttribute('noSiblings');
				if(!tmpVar)tmpVar = currItem.noSiblings;
				if(tmpVar=='true')noSiblings=true;

				nodeId++;
				var subItems = currItem.getElementsByTagName('UL');
				var img = document.createElement('IMG');
				img.src = this.imageFolder + this.blankImage;
        img.className = this.plusClass;
				img.onmouseover = JSTreeObj.showNodeToDrag;
        img.onmouseout = JSTreeObj.PlusMinusOut;
				img.onclick = JSTreeObj.showHideNode;

				if(subItems.length==0)img.style.visibility='hidden';else{
					subItems[0].id = 'tree_ul_' + treeUlCounter;
					treeUlCounter++;
				}
				var aTag = currItem.getElementsByTagName('A')[0];
				aTag.id = 'nodeATag' + currItem.id.replace(/[^0-9]/gi,'');
				aTag.ondblclick = JSTreeObj.showHideNode;
				if(!noDrag)aTag.onmousedown = JSTreeObj.initDrag;
				if(!noChildren)aTag.onmousemove = JSTreeObj.moveDragableNodes;
        aTag.onmouseout = JSTreeObj.clearDestination;
        currItem.insertBefore(img,aTag);
				//menuItems[no].id = 'dhtmlgoodies_treeNode' + nodeId;
				var folderImg = document.createElement('IMG');
				if(!noDrag)folderImg.onmousedown = JSTreeObj.initDrag;
				if(!noSiblings)folderImg.onmousemove = JSTreeObj.moveDragableNodes;
        folderImg.onmouseout = JSTreeObj.clearDestination;
				if(currItem.className){
					folderImg.src = this.imageFolder + currItem.className;
				}else{
          folderImg.src = this.imageFolder + this.blankImage;
          if(isElement) { 
            folderImg.className = this.elementClass;
          } else {
            folderImg.className = this.folderClosedClass;
          }
				}
        //folderImg.id = 'nodefolderImg' + menuItems[no].id.replace(/[^0-9]/gi,'');
				folderImg.ondblclick = JSTreeObj.showHideNode;
				currItem.insertBefore(folderImg,aTag);

				if(this.contextMenu){
					var noDelete = currItem.getAttribute('noDelete');
					if(!noDelete)noDelete = currItem.noDelete;
					var noRename = currItem.getAttribute('noRename');
					if(!noRename)noRename = currItem.noRename;

					if(noRename=='true' && noDelete=='true'){}else{
						if(noDelete == 'true')this.contextMenu.attachToElement(aTag,false,menuModelRenameOnly);
						else if(noRename == 'true')this.contextMenu.attachToElement(aTag,false,menuModelDeleteOnly);
						else this.contextMenu.attachToElement(aTag,false,menuModel);

					}
				}
				this.addEvent(aTag,'contextmenu',this.highlightItem);



			} // end for all items



			initExpandedNodes = this.Get_Cookie('dhtmlgoodies_expandedNodes');
			if(initExpandedNodes){
				var nodes = initExpandedNodes.split(',');
				for(var no=0;no<nodes.length;no++){
					if(nodes[no])this.showHideNode(false,nodes[no]);	
				}
			}


			document.documentElement.onmousemove = JSTreeObj.moveDragableNodes;
			document.documentElement.onmouseup = JSTreeObj.dropDragableNodes;
		}
		,
		__addAdditionalRequestParameters : function(ajax, parameters)
		{
			for (var parameter in parameters) {
				ajax.setVar(parameter, parameters[parameter]);
			}
		}
	}
