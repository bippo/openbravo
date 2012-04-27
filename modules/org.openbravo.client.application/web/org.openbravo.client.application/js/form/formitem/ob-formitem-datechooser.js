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

// == OBDateChooser ==
// OBDateChooser inherits from SmartClient DateChooser
// extends standard DateChooser implementation to be used in OBDateItem
isc.ClassFactory.defineClass('OBDateChooser', isc.DateChooser);

isc.OBDateChooser.addProperties({
  firstDayOfWeek: 1,
  autoHide: true,
  showCancelButton: true,
  todayButtonTitle: OB.I18N.getLabel('OBUISC_DateChooser.todayButtonTitle'),
  cancelButtonTitle: OB.I18N.getLabel('OBUISC_DateChooser.cancelButtonTitle'),

  initWidget: function () {
    this.Super('initWidget', arguments);

    // Force associated date text box to have the same enable status as the picker has
    if (this.callingFormItem) {
      this.callingFormItem.disabled = this.disabled;
    }
  }
});

if (isc.OBDateChooser) { // To force SC to load OBDateChooser instead of DateChooser
  isc.DateChooser.getSharedDateChooser = function (properties) {
    return isc.OBDateChooser.create(properties);
  };
}