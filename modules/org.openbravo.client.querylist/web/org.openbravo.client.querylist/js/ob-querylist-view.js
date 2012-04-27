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
// = OBQueryListView =
//
// Implements the Query / List widget maximized view.
//
isc.defineClass('OBQueryListView', isc.PortalLayout);

isc.OBQueryListView.addProperties({
  widgetInstanceId: null,
  fields: null,
  gridDataSource: null,
  widgetId: null,

  //Set PortalLayout common parameters
  numColumns: 1,
  showColumnMenus: false,
  canDropComponents: false,

  initWidget: function (args) {
    this.Super('initWidget', arguments);

    this.widgetInstanceId = args.widgetInstanceId;
    this.fields = args.fields;
    this.gridDataSource = args.gridDataSource;
    this.widgetId = args.widgetId;

    var widgetInstance = isc['_' + this.widgetId].create(isc.addProperties({
      viewMode: 'maximized',
      fields: this.fields,
      widgetInstanceId: this.widgetInstanceId,
      widgetId: this.widgetId,
      dbInstanceId: this.widgetInstanceId,
      gridDataSource: this.gridDataSource,
      title: args.tabTitle,
      menuItems: args.menuItems,
      parameters: args.parameters,
      fieldDefinitions: args.fieldDefinitions,
      canDelete: false
    }));
    this.addPortlet(widgetInstance);
  },

  isSameTab: function (viewName, params) {
    return this.widgetInstanceId === params.widgetInstanceId;
  }
});