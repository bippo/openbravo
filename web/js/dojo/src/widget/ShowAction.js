dojo.provide("dojo.widget.ShowAction");
dojo.require("dojo.widget.*");

dojo.widget.defineWidget(
	"dojo.widget.ShowAction",
	dojo.widget.HtmlWidget,
{
	on: "",
	action: "fade",
	duration: 350,
	from: "",
	to: "",
	auto: "false",
	postMixInProperties: function(){ 
		// fix for very strange Opera 9 bug
		if(dojo.render.html.opera){
			this.action = this.action.split("/").pop();
		}
	}
});
