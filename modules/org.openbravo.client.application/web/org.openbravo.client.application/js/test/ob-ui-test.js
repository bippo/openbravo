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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
module('org.openbravo.client.application');

test('Basic requirements', function () {
  expect(2);
  ok(isc, 'isc object is present');
  ok(document.getElementById, 'getElementById');
});

test('Create Canvas', function () {

  expect(3);

  var canvasID = 'myCanvas',
      canvas, createCanvas;
  createCanvas = function (isc) {
    var c = isc.Canvas.newInstance({
      ID: canvasID,
      width: '100%',
      height: '100%'
    });
    return c;
  };

  canvas = createCanvas(isc);
  canvas.setBackgroundColor('blue');

  ok(typeof canvas !== 'undefined', 'Canvas created');

  ok((function (c) {
    return c.height !== 0 && c.width !== 0;
  }(canvas)), 'Canvas height and width are not zero');

  ok(isc.Log.getStackTrace() !== undefined, 'getStackTrace()');
});