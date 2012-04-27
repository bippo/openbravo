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

isc.OBMyOpenbravo.addProperties({
  styleName: 'OBMyOpenbravo'
});

OB.Styles.OBMyOpenbravo = {
  recentViewsLayout: {
    baseStyle: 'OBMyOBRecentViews',
    nodeIcons: {
      Window: OB.Styles.OBApplicationMenu.Icons.window,
      Process: OB.Styles.OBApplicationMenu.Icons.process,
      Report: OB.Styles.OBApplicationMenu.Icons.report,
      Form: OB.Styles.OBApplicationMenu.Icons.form
    },
    Label: {
      baseStyle: 'OBMyOBRecentViewsEntry'
    },
    newIcon: {
      src: OB.Styles.skinsPath + 'Default/org.openbravo.client.myob/images/management/iconCreateNew.png'
    }
  },
  recentDocumentsLayout: {
    baseStyle: 'OBMyOBRecentViews',
    Label: {
      baseStyle: 'OBMyOBRecentViewsEntry',
      icon: OB.Styles.OBApplicationMenu.Icons.document
    }
  },
  actionTitle: {
    baseStyle: 'OBMyOBRecentViews'
  },
  refreshLayout: {
    styleName: 'OBMyOBLeftColumnLink'
  },
  addWidgetLayout: {
    styleName: 'OBMyOBLeftColumnLink'
  },
  adminOtherMyOBLayout: {
    styleName: 'OBMyOBLeftColumnLink'
  },
  leftColumnLayout: {
    styleName: 'OBMyOBLeftColumn'
  },
  portalLayout: {
    styleName: 'OBMyOBPortal'
  }
};

OB.Styles.OBMyOBAddWidgetDialog = {
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldSelectInput',
  pendingTextBoxStyle: null,
  //'OBFormFieldSelectInputPending',
  controlStyle: 'OBFormFieldSelectControl',
  pickListBaseStyle: 'OBFormFieldPickListCell',
  pickListTallBaseStyle: 'OBFormFieldPickListCell',
  pickerIconSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/form/comboBoxPicker.png',
  height: 21,
  pickerIconWidth: 21,
  pickListCellHeight: 22,
  pickListProperties: {
    bodyStyleName: 'OBPickListBody'
  }
};

OB.Styles.OBMyOBAdminModeDialog = {
  cellStyle: 'OBFormField',
  titleStyle: 'OBFormFieldLabel',
  textBoxStyle: 'OBFormFieldSelectInput',
  pendingTextBoxStyle: null,
  //'OBFormFieldSelectInputPending',
  controlStyle: 'OBFormFieldSelectControl',
  pickListBaseStyle: 'OBFormFieldPickListCell',
  pickListTallBaseStyle: 'OBFormFieldPickListCell',
  pickerIconSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/form/comboBoxPicker.png',
  height: 21,
  pickerIconWidth: 21,
  pickListCellHeight: 22,
  pickListProperties: {
    bodyStyleName: 'OBPickListBody'
  }
};

OB.Styles.OBMyOBPublishChangesDialog = {
  form: {
    styleName: 'OBMyOBPublishLegend'
  }
};