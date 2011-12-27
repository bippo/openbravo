/**
* Modified version of scopeleaks by Rui Lopes
* 
* https://github.com/ruidlopes/scopeleaks
* 
* (The MIT License)
*
* Copyright © 2011 Rui Lopes
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of
* this software and associated documentation files (the ‘Software’), to deal in
* the Software without restriction, including without limitation the rights to
* use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
* the Software, and to permit persons to whom the Software is furnished to do so,
* subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED ‘AS IS’, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
* FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
* COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
* IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
* CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

(function() {
	var scope = 
		typeof global !== "undefined" ? global :
		typeof window !== "undefined" ? window :
		this;
	
	var original = undefined;
	
	var inOpera = (typeof scope.opera == 'object') && (scope.opera.toString() == "[object Opera]");
	var inFirebug = (typeof scope.console == 'object') && (scope.console.firebug !== undefined);
	
	var scopeleaks = { 
		snapshot: function () {
			var snapshot = {};

			for (var i in scope)
				snapshot[i] = true;

			original = original || snapshot;

			return snapshot;
		},
			
		leaks: function (snapshot) {
			var ss = snapshot || scopeleaks.snapshot();
			var leaks = [];
			
			for (var i in scope)
				if (!ss[i] && i.indexOf('isc_') === 0 && scope[i] !== null && scope[i] !== undefined)
					leaks.push(i);

			return leaks;
		}
	};
  
	if (typeof window !== "undefined")
		window.scopeleaks = scopeleaks;
  	else if (typeof exports !== "undefined") {
		exports.leaks = scopeleaks.leaks;
		exports.snapshot = scopeleaks.snapshot;
	}
  
	scopeleaks.snapshot();
  
	return scopeleaks;
})();