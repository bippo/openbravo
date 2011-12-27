dojo.provide("dojo.style");
dojo.require("dojo.lang.common");
dojo.kwCompoundRequire({
	browser: ["dojo.html.style"]
});
dojo.deprecated("dojo.style", "replaced by dojo.html.style", "0.5");
dojo.lang.mixin(dojo.style, dojo.html);
