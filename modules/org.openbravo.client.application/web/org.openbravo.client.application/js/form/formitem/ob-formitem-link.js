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

//== OBLinkItem ==
//Input for normal strings (links) with an added icon to navigate to the link  
isc.ClassFactory.defineClass('OBLinkItem', isc.TextItem);

isc.OBLinkItem.addProperties({
  validateOnExit: true,
  init: function () {
    this.icons = [{
      src: this.pickerIconSrc,
      width: this.pickerIconWidth,
      height: this.pickerIconHeight,
      hspace: this.pickerIconHspace,
      click: function (form, item) {
        var url = item.getValue();
        if (!url) {
          return;
        }
        if (url.indexOf('://') === -1) {
          url = 'http://' + url;
        }
        window.open(url);
      }
    }];
    return this.Super('init', arguments);
  },
  validate: function () {
    var url = this.getValue();
    if (!url) {
      return true;
    }
    return OB.Utilities.isValidURL(url);
  }
});