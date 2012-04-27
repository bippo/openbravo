/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
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

// == OBWidgetInFormItem ==
// Allows placing of workspace widget into a generated form
isc.ClassFactory.defineClass('OBWidgetInFormItem', isc.CanvasItem);

isc.OBWidgetInFormItem.addProperties({
  widgetInstance: null,
  autoDestroy: true,

  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  widgetProperties: {
    height: '0px',
    inWidgetInFormMode: true
  },

  createCanvas: function () {
    var widgetProperties, i, w, widgetClass, widgetParameters, locAvailWidgetClasses;

    locAvailWidgetClasses = OB.MyOB.availableWidgetClasses;
    for (i = 0; i < locAvailWidgetClasses.length; i++) {
      w = locAvailWidgetClasses[i];
      if (w.widgetClassId === this.widgetClassId) {
        widgetClass = w.widgetClassName;
        widgetParameters = w.parameters;
      }
    }

    // make a local copy which can be changed
    widgetProperties = isc.addProperties({}, this.widgetProperties);

    // add link to form so widget can possibly use it
    widgetProperties.viewForm = this.form;

    widgetProperties.parameters = widgetParameters;

    if (!widgetClass && this.isPreviewFormItem) {
      widgetClass = isc.OBWidget;
      widgetProperties.createWindowContents = function () {
        return isc.Label.create({
          width: 1,
          height: 1,
          contents: '&nbsp;'
        });
      };
    }

    this.widgetInstance = isc.ClassFactory.newInstance(widgetClass, widgetProperties);
    return this.widgetInstance;
  },

  // called via processFICReturn
  refresh: function () {
    // refresh widget, passing special parameter which is link to formValues for currently displayed record
    if (this.widgetInstance) {
      this.widgetInstance.parameters.formValues = this.form.values;
      this.widgetInstance.refresh();
    }
  }
});