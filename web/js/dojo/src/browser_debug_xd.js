dojo.provide("dojo.browser_debug_xd");

//Override dojo.provide, so we can trigger the next
//script tag for the next local module. We can only add one
//at a time because there are browsers that execute script tags
//in the order that the code is received, and not in the DOM order.
dojo.nonDebugProvide = dojo.provide;

dojo.provide = function(resourceName){
	var dbgQueue = dojo.hostenv["xdDebugQueue"];
	if(dbgQueue && dbgQueue.length > 0 && resourceName == dbgQueue["currentResourceName"]){
		//Set a timeout so the module can be executed into existence. Normally the
		//dojo.provide call in a module is the first line. Don't want to risk attaching
		//another script tag until the current one finishes executing.
		window.setTimeout("dojo.hostenv.xdDebugFileLoaded('" + resourceName + "')", 1);
	}

	dojo.nonDebugProvide.apply(dojo, arguments);
}

dojo.hostenv.xdDebugFileLoaded = function(resourceName){
	var dbgQueue = this.xdDebugQueue;
	
	if(resourceName && resourceName == dbgQueue.currentResourceName){
		dbgQueue.shift();
	}

	if(dbgQueue.length == 0){
		dbgQueue.currentResourceName = null;
		this.xdNotifyLoaded();
	}else{
		dbgQueue.currentResourceName = dbgQueue[0].resourceName;
		var element = document.createElement("script");
		element.type = "text/javascript";
		element.src = dbgQueue[0].resourcePath;
		document.getElementsByTagName("head")[0].appendChild(element);
	}		
}
