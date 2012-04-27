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
// it is often nicer to create a subclass than just changing 
// the properties of the ListGrid. A subclass can be re-used.
// OBTestSelectionGrid: The first type is the listgrid shown in the top
// OBTestSelectionLayout: is the layout showing the grid and the button below it
isc.ClassFactory.defineClass('OBTestSelectionGrid', isc.ListGrid);

isc.OBTestSelectionGrid.addProperties({
  width: '100%',
  height: '90%',
  // give the button some space
  // some common settings
  showFilterEditor: true,
  filterOnKeypress: true,

  canEdit: false,
  alternateRecordStyles: true,
  canReorderFields: true,
  canFreezeFields: true,
  canGroupBy: false,
  selectionAppearance: 'checkbox',
  canAutoFitFields: false,

  autoFitFieldWidths: true,
  autoFitWidthApproach: 'title',

  // as all the data is loaded on the client 
  // let's do these things here
  dataProperties: {
    useClientFiltering: true,
    useClientSorting: true
  },

  // property set by creator or when instantiated
  // is the server side actionhandler providing the data
  // and executing the action
  actionHandler: null,

  initWidget: function () {
    var me = this,
        callBack;

    // the data request calls this method, which sets the data in the grid
    callBack = function (response, data, request) {
      me.dataSource = isc.DataSource.create({
        clientOnly: true,
        fields: me.fields,
        testData: data.list
      });
      me.filterData();
    };

    // request the data, note the _command value
    OB.RemoteCallManager.call(this.actionHandler, {}, {
      '_command': 'data'
    }, callBack);

    var result = this.Super("initWidget", arguments);
  },

  // enable/disable the button on selection
  selectionChanged: function () {
    this.selectionLayout.selectionUpdated(this.getSelection().getLength());
  }
});

// a VLayout which combines a selection grid with a button
isc.ClassFactory.defineClass('OBTestSelectionLayout', isc.VLayout);

isc.OBTestSelectionLayout.addProperties({
  top: 100,
  left: 100,
  width: 600,
  height: 300,
  defaultLayoutAlign: 'center',

  gridProperties: null,
  buttonProperties: null,

  actionHandler: null,

  selectionGrid: null,
  selectionButton: null,
  initWidget: function () {
    var buttonClickCallback;

    // note the actionHandler needs to be set in the grid when it gets created
    // read the SC documentation on what isc.addProperties does    
    this.selectionGrid = isc.OBTestSelectionGrid.create(isc.addProperties(this.gridProperties, {
      actionHandler: this.actionHandler
    }));

    // tell the selection grid who is its owner
    this.selectionGrid.selectionLayout = this;
    this.addMembers(this.selectionGrid);

    // make some vertical space
    this.addMembers(isc.LayoutSpacer.create({
      height: 10
    }));

    // create the action button    
    this.selectionButton = isc.Button.create(this.buttonProperties);
    this.selectionButton.selectionLayout = this;
    this.selectionButton.setDisabled(true);
    this.addMembers(this.selectionButton);

    // this function is called after the data has been processed
    // the message is set on the server
    buttonClickCallback = function (resp, data, req) {
      isc.say(data.message);
    };

    // note it can make sense to make a process button class
    // which gets the name of the action handler to call
    this.selectionButton.click = function () {
      OB.RemoteCallManager.call(this.actionHandler, {
        selectedRecords: this.selectionLayout.selectionGrid.getSelection()
      }, {
        // the _command parameter determines on the server side what 
        // happens
        '_command': 'execute'
      }, buttonClickCallback);
    };
    this.selectionButton.actionHandler = this.actionHandler;
    this.Super('initWidget', arguments);

    this.selectionGrid.focusInFilterEditor();
  },

  // disable or enable the button
  selectionUpdated: function (selectedCount) {
    this.selectionButton.setDisabled(selectedCount === 0);
  }
});