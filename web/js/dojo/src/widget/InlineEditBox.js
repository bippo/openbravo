dojo.provide("dojo.widget.InlineEditBox");

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.lfx.*");
dojo.require("dojo.gfx.color");
dojo.require("dojo.string");
dojo.require("dojo.html.*");
dojo.require("dojo.html.layout");

dojo.widget.defineWidget(
	"dojo.widget.InlineEditBox",
	dojo.widget.HtmlWidget,
	function(){
		// summary
		//		Given node is displayed as-is (for example, an <h1 dojoType="InlineEditBox">
		//		is displayed as an <h1>, but when you click on it, it turns into an
		//		<input> or <textarea>, and the user can edit the value.
		
		// Available Callbacks:
		//		onSave()  -- When the save button is pushed.
		//		onCancel()  -- when the cancel button is pushed.
		//		onChange()  -- when the value is actually changed.

		// mutable objects need to be in constructor to give each instance its own copy
		this.history = [];
	},
{
	templatePath: dojo.uri.moduleUri("dojo.widget", "templates/InlineEditBox.html"),
	templateCssPath: dojo.uri.moduleUri("dojo.widget", "templates/InlineEditBox.css"),

	// mode: String
	//		"text" is the default, and means that the node will convert it into a (single-line) <input>
	//		when you click on it;
	//		"textarea" means that the node will be converted into a multi-line <textarea> for editing.
	mode: "text",
	
	// name: String
	//		This is passed as the third argument to onSave().
	name: "",

	// minWidth:  Integer
	//		Pixel minimum width of edit box
	minWidth: 100,

	// minHeight: Integer
	//		Pixel minimum height of edit box, if it's a <textarea>
	minHeight: 200,

	// editing: Boolean
	//		Is the node currently in edit mode?
	editing: false,

	// value: String
	//		The text string displayed or edited.
	//		Initial value can also be specified inline, like
	//		<h1 dojoType="InlineEditBox">Hello world</h1>
	value: "",

	// deprecated
	textValue: "",
	defaultText: "",
	
	postMixInProperties: function(){
		if(this.textValue){
			dojo.deprecated("InlineEditBox: Use value parameter instead of textValue; will be removed in 0.5");
			this.value=this.textValue;
		}
		if(this.defaultText){
			dojo.deprecated("InlineEditBox: Use value parameter instead of defaultText; will be removed in 0.5");
			this.value=this.defaultText;
		}
	},


	postCreate: function(args, frag){
		// put original node back in the document, and attach handlers
		// which hide it and display the editor
		// TODO: this has a number of issues including breaking programatic creation
		this.editable = this.getFragNodeRef(frag);
		dojo.html.insertAfter(this.editable, this.form);
		dojo.event.connect(this.editable, "onmouseover", this, "onMouseOver");
		dojo.event.connect(this.editable, "onmouseout", this, "onMouseOut");
		dojo.event.connect(this.editable, "onclick", this, "_beginEdit");
		
		// get value and display it
		if(this.value){
			this.editable.innerHTML = this.value;
			return;
		} else {
			this.value = dojo.string.trim(this.editable.innerHTML);
			this.editable.innerHTML = this.value;
		}
	},
	
	onMouseOver: function(){
		if(!this.editing){
			if (this.disabled){
				dojo.html.addClass(this.editable, "editableRegionDisabled");
			} else {
				dojo.html.addClass(this.editable, "editableRegion");
				if(this.mode == "textarea"){
					dojo.html.addClass(this.editable, "editableTextareaRegion");
				}
			}
		}
	},
	
	onMouseOut: function(){
		if(!this.editing){
			dojo.html.removeClass(this.editable, "editableRegion");
			dojo.html.removeClass(this.editable, "editableTextareaRegion");
			dojo.html.removeClass(this.editable, "editableRegionDisabled");
		}
	},

	_beginEdit: function(e){
		// summary
		// 		When user clicks the text, then start editing.
		// 		Hide the text and display the form instead.

		if(this.editing || this.disabled){ return; }
		this.onMouseOut();
		this.editing = true;

		// setup the form's <input> or <textarea> field, as specified by mode
		var ee = this[this.mode.toLowerCase()];
		ee.value = dojo.string.trim(this.value);
		ee.style.fontSize = dojo.html.getStyle(this.editable, "font-size");
		ee.style.fontWeight = dojo.html.getStyle(this.editable, "font-weight");
		ee.style.fontStyle = dojo.html.getStyle(this.editable, "font-style");
		var bb = dojo.html.getBorderBox(this.editable);
		ee.style.width = Math.max(bb.width, this.minWidth) + "px";
		if(this.mode.toLowerCase()=="textarea"){
			ee.style.display = "block";
			ee.style.height = Math.max(bb.height, this.minHeight) + "px";
		} else {
			ee.style.display = "";
		}

		// show the edit form and hide the read only version of the text
		this.form.style.display = "";
		this.editable.style.display = "none";

		ee.focus();
		ee.select();
		this.submitButton.disabled = true;
	},

	saveEdit: function(e){
		// summary: Callback when user presses "Save" button
		e.preventDefault();
		e.stopPropagation();
		var ee = this[this.mode.toLowerCase()];
		if((this.value != ee.value)&&
			(dojo.string.trim(ee.value) != "")){
			this.doFade = true;
			this.history.push(this.value);
			this.onSave(ee.value, this.value, this.name);
			this.value = ee.value;
			this.editable.innerHTML = "";
			var textNode = document.createTextNode( this.value );
			this.editable.appendChild( textNode );
		}else{
			this.doFade = false;
		}
		this._finishEdit(e);
	},
	
	_stopEditing: function(){
		//summary: makes the edit box go away.
		this.editing = false;
		this.form.style.display="none";
		this.editable.style.display = "";
		return true;
	},

	cancelEdit: function(e){
		// summary: Callback when user presses "Cancel" button
		this._stopEditing();
		this.onCancel();
		return true;
	},

	_finishEdit: function(e){
		//this line serves no purpose, and never just returns.
		//if(!this._cancelEdit(e)){ return; }
		this._stopEditing();
		if(this.doFade) {
			dojo.lfx.highlight(this.editable, dojo.gfx.color.hex2rgb("#ffc"), 700).play(300);
		}
		this.doFade = false;
	},
	
	setText: function(txt){
		dojo.deprecated("setText() is deprecated, call setValue() instead, will be removed in 0.5");
		this.setValue(txt);
	},
	
	setValue: function(/*String*/ txt){
		// sets the text without informing the server
		txt = "" + txt;
		var tt = dojo.string.trim(txt);
		this.value = tt
		this.editable.innerHTML = tt;
	},

	undo: function(){
		// summary: Revert to previous value in history list.
		//  This seems to have no use.. can we erase? --birlcathy
		if(this.history.length > 0){
			var curValue = this.value;
			var value = this.history.pop();
			this.editable.innerHTML = value;
			this.value = value;
			this.onUndo(value);
			this.onSave(value, curValue, this.name);
		}
	},

	onChange: function(newValue,oldValue){
		//Summary: User Callback for when a value has changed.
	},
	onSave: function(newValue, oldValue, name){
		// summary: Callback for when value is changed.
	},
	onCancel: function(){
	 //Summary: Boring callback for when user pushes the cancel button	
	},

	checkForValueChange: function(){
		// summary
		//		Callback when user changes input value.
		//		Enable save button if the text value is different than the original value.
		var ee = this[this.mode.toLowerCase()];
		if((this.value != ee.value)&&
			(dojo.string.trim(ee.value) != "")){
			this.submitButton.disabled = false;
		}
		this.onChange(this.value,ee.value);
	},
	
	disable: function(){
		this.submitButton.disabled = true;
		this.cancelButton.disabled = true;
		var ee = this[this.mode.toLowerCase()];
		ee.disabled = true;
		dojo.widget.InlineEditBox.superclass.disable.apply(this, arguments);
	},
	
	enable: function(){
		this.checkForValueChange();
		this.cancelButton.disabled = false;
		var ee = this[this.mode.toLowerCase()];
		ee.disabled = false;
		
		dojo.widget.InlineEditBox.superclass.enable.apply(this, arguments);
	}
});
