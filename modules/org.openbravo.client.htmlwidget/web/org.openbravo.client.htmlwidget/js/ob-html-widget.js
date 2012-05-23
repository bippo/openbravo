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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = OBHTMLWidget =
//
// Implements the HTML widget superclass.
//
isc.defineClass('OBHTMLWidget', isc.OBWidget).addProperties({
  contentSource: null,

  initWidget: function () {
    this.Super('initWidget', arguments);
    this.setWidgetHeight();
  },

  createWindowContents: function () {
    if (!this.contentSource && this.parameters.htmlcode) {
      this.contentSource = this.evaluateContents(this.parameters.htmlcode);
    }
    if (this.parameters.widgetTitle) {
      this.setTitle(this.parameters.widgetTitle);
    }
    return isc.HTMLFlow.create({
      contents: this.contentSource,
      height: '100%',
      width: '100%'
    });
  },

  refresh: function () {
    this.setWidgetHeight();
    if (this.parameters.widgetTitle) {
      this.setTitle(this.parameters.widgetTitle);
    }
    if (this.parameters.htmlcode) {
      this.contentSource = this.evaluateContents(this.parameters.htmlcode);
    }
    this.windowContents.setContents(this.contentSource);
  },

  setWidgetHeight: function () {
    var currentHeight = this.getHeight(),
        contentHeight = this.parameters.widgetHeight,
        edgeTop = this.edgeTop,
        edgeBottom = this.edgeBottom,
        newHeight = contentHeight + edgeTop + edgeBottom;

    if (!isc.isA.Number(this.parameters.widgetHeight)) {
      return;
    }

    this.setHeight(newHeight);
    if (this.parentElement) {
      var heightDiff = newHeight - currentHeight,
          parentHeight = this.parentElement.getHeight();
      this.parentElement.setHeight(parentHeight + heightDiff);
    }
  }
});