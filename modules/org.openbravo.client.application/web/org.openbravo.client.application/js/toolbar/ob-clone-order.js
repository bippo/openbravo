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
 * Contributor(s):   Sreedhar Sirigiri (TDS), Mallikarjun M (TDS)
 ************************************************************************
 */

// Registers a button to clone an order
// put within a function to hide local vars etc.
(function () {
  var cloneButtonProps = isc.addProperties({}, isc.OBToolbar.CLONE_BUTTON_PROPERTIES);

  cloneButtonProps.action = function () {
    var view = this.view,
        callback;

    callback = function (ok) {
      var requestParams;

      if (ok) {
        requestParams = {
          orderId: view.viewGrid.getSelectedRecord().id
        };
        OB.RemoteCallManager.call('org.openbravo.client.application.businesslogic.CloneOrderActionHandler', {}, requestParams, function (rpcResponse, data, rpcRequest) {

          var recordIndex = view.viewGrid.getRecordIndex(view.viewGrid.getSelectedRecord()) + 1;
          // takes care of transforming dates etc.
          data = view.viewGrid.getDataSource().recordsFromObjects(data)[0];
          view.viewGrid.data.insertCacheData(data, recordIndex);
          view.viewGrid.scrollToRow(recordIndex);
          view.viewGrid.markForRedraw();
          var visibleRows = view.viewGrid.body.getVisibleRows();
          view.editRecord(view.viewGrid.getRecord(recordIndex), false);
        });
      }
    };
    isc.ask(OB.I18N.getLabel('OBUIAPP_WantToCloneOrder'), callback);
  };

  // register the button for the sales order tab
  OB.ToolbarRegistry.registerButton(cloneButtonProps.buttonType, isc.OBToolbarIconButton, cloneButtonProps, 100, ['186']);

}());