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

isc.defineClass('OBSEIG_AccountAssociationView', isc.Dialog);

isc.OBSEIG_AccountAssociationView.addProperties({
  showMinimizeButton: false,
  showMaximizeButton: false,
  autoSize: true,

  isSameTab: function (viewId, params) {
    return viewId === 'OBSEIG_AccountAssociationView';
  },

  getBookMarkParams: function () {
    var result = {};
    result.viewId = 'OBSEIG_AccountAssociationView';
    return result;
  },

  OK_BUTTON: isc.addProperties({}, isc.Dialog.OK, {
    click: function () {
      this.topElement.cancelClick();
      top.location.href = OB.Application.contextUrl + 'org.openbravo.service.integration.google/auth.html?is_association=true';
    }
  }),

  initWidget: function (args) {
    var community = (OB.Application.licenseType === 'C'),
        label = community ? 'OBSEIG_Activate' : 'OBSEIG_AssociateAccount';

    this.title = args && args.tabTitle ? args.tabTitle : '';

    this.items = isc.Label.create({
      contents: OB.I18N.getLabel(label),
      width: '100%'
    });

    if (community) {
      this.toolbarButtons = [isc.Dialog.OK];
    } else {
      this.toolbarButtons = [this.OK_BUTTON, isc.Dialog.CANCEL];
    }

    this.Super('initWidget', arguments);
  }
});