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

// = OBShowParameterWidget =
//
// A widget which can be used to show parameter values and content.
//
isc.defineClass('OBShowParameterWidget', isc.OBWidget).addProperties({
  setParameters: function (parameters) {
    this.Super('setParameters', arguments);
    var oldForm = this.displayForm;
    this.windowContents.destroyAndRemoveMembers(this.displayForm);
    this.windowContents.addMember(this.createDisplayForm());
    oldForm.destroy();
  },

  createWindowContents: function () {
    var layout = isc.VLayout.create({
      width: '100%',
      height: '100%',
      defaultLayoutAlign: 'center'
    });
    layout.addMember(isc.Label.create({
      contents: OB.I18N.getLabel('OBKMO_ParameterValues'),
      height: 1,
      overflow: 'visible'
    }));
    layout.addMember(isc.LayoutSpacer.create({
      height: 10
    }));
    layout.addMember(this.createDisplayForm());
    return layout;
  },

  customAction: function () {
    isc.say('Custom Action!', {
      isModal: true,
      showModalMask: true
    });
  },

  createDisplayForm: function () {
    var item, i, theForm, items = [],
        values = {};

    theForm = isc.DynamicForm.create({
      width: '100%',
      height: '100%',
      wrapItemTitles: false
    });

    for (i in this.parameters) {
      if (this.parameters.hasOwnProperty(i)) {
        items.push({
          name: i,
          title: i,
          type: 'text',
          width: '100%',
          editorType: 'StaticTextItem'
        });
        // get the display value
        // TODO: handle missing values somehow
        item = this.editFormLayout.editForm.getItem(i);
        if (item) {
          values[i] = item.mapValueToDisplay(this.parameters[i]);
        } else {
          values[i] = this.parameters[i];
        }
      }
    }
    theForm.setItems(items);
    theForm.setValues(values);

    this.displayForm = theForm;

    return theForm;
  }
});