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

// Styling properties for a generic grid (ob-grid.js)
isc.OBGrid.addProperties({
  editFailedBaseStyle: null,
  // is done through the additional csstext
  editFailedCSSText: 'background-color: red; border-right-color: red; border-bottom-color: red;',
  bodyStyleName: 'OBGridBody',
  baseStyle: 'OBGridCell',
  baseStyleEdit: 'OBGridCellEdit',
  // for use in ob-view-grid.js while editing a cell
  recordStyleError: 'OBGridCellError',
  recordStyleSelectedViewInActive: 'OBGridCellSelectedViewInactive',
  headerBaseStyle: 'OBGridHeaderCell',
  headerBarStyle: 'OBGridHeaderBar',
  headerTitleStyle: 'OBGridHeaderCellTitle',
  cellPadding: 0,
  /* Set in the CSS */
  cellAlign: 'center',
  sortAscendingImage: {
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_sortAscending.png',
    width: 7,
    height: 11
  },
  sortDescendingImage: {
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_sortDescending.png',
    width: 7,
    height: 11
  },
  headerMenuButtonConstructor: 'OBGridHeaderImgButton',
  headerButtonConstructor: 'ImgButton',
  headerMenuButtonWidth: 17,
  headerMenuButtonSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeaderMenuButton.png',
  hoverWidth: 200,
  editLinkColumnWidth: 58,

  summaryRowConstructor: 'OBGridSummary',
  summaryRowDefaults: {
    showRollOver: false
  },
  summaryRowHeight: 22,
  summaryRowStyle: 'OBGridSummaryCell',
  summaryRowStyle_sum: 'OBGridSummaryCell_sum',
  summaryRowStyle_avg: 'OBGridSummaryCell_avg',
  summaryRowStyle_count: 'OBGridSummaryCell_count',

  progressIconDefaults: {
    width: 16,
    height: 16,
    visibility: 'hidden',
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridButton-progress.gif' /* Generated @ http://www.ajaxload.info/ */
    /* Indicator type: 'Snake' - Background color: #FFE1C0 - Transparent background - Foreground color: #333333 */
  }
});

isc.OBGrid.addClassProperties({

  defaultColumnWidths: [50, 100, 200],

  getDefaultColumnWidth: function (length) {
    if (length <= 1) {
      return isc.OBGrid.defaultColumnWidths[0];
    } else if (length <= 30) {
      return isc.OBGrid.defaultColumnWidths[1];
    } else if (length <= 60) {
      return isc.OBGrid.defaultColumnWidths[2];
    }
    return 200;
  }
});

isc.OBGrid.changeDefaults('filterEditorDefaults', {
  height: 22,
  styleName: 'OBGridFilterBase',
  baseStyle: 'OBGridFilterCell'
});

isc.OBGrid.changeDefaults('sorterDefaults', {
  // baseStyle / titleStyle is auto-assigned from headerBaseStyle
  showFocused: false,
  //  src: '[SKIN]ListGrid/header.png',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_bg.png',
  baseStyle: 'OBGridSorterButton'
});

isc.OBGrid.changeDefaults('headerButtonDefaults', {
  showTitle: true,
  showDown: true,
  showFocused: false,
  // baseStyle / titleStyle is auto-assigned from headerBaseStyle
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_bg.png'
});

isc.OBGrid.changeDefaults('headerMenuButtonDefaults', {
  showDown: false,
  showTitle: true
  //src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_bg.png'
});

// Styling properties for the header button of a generic grid (ob-grid.js)
isc.OBGridHeaderImgButton.addProperties({
  showFocused: false,
  showRollOver: false,
  showFocusedAsOver: false,
  showDown: false
});

// Styling properties for the buttons of the grid in 'grid mode' (ob-view-grid.js)
isc.OBGridToolStripIcon.addProperties({
  width: 21,
  height: 19,
  showRollOver: true,
  showDown: true,
  showDisabled: false,
  showFocused: false,
  showFocusedAsOver: true,
  baseStyle: 'OBGridToolStripIcon',
  genericIconSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridButton.png',
  /* Can be: edit - form - cancel - save */
  initWidgetStyle: function () {
    var fileExt = this.genericIconSrc.substring(this.genericIconSrc.lastIndexOf('.'), this.genericIconSrc.length);
    var filePath = this.genericIconSrc.substring(0, this.genericIconSrc.length - fileExt.length) + '-';
    this.setSrc(filePath + this.buttonType + fileExt);
  },
  setErrorState: function (error) {
    var fileExt = this.genericIconSrc.substring(this.genericIconSrc.lastIndexOf('.'), this.genericIconSrc.length);
    var filePath = this.genericIconSrc.substring(0, this.genericIconSrc.length - fileExt.length) + '-';
    if (error) {
      this.setSrc(filePath + this.buttonType + '-error' + fileExt);
    } else {
      this.setSrc(filePath + this.buttonType + fileExt);
    }
  }
});

isc.OBGridToolStripSeparator.addProperties({
  width: 1,
  height: 11,
  imageType: 'normal',
  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridButton-separator.png'
});

isc.OBGridButtonsComponent.addProperties({
  // note this height should be the same as the recordComponentHeight defined above
  height: 21,
  width: '100%',
  overflow: 'hidden',
  align: 'center',
  defaultLayoutAlign: 'center',
  styleName: 'OBGridToolStrip',
  membersMargin: 4
});

isc.OBGridLinkButton.addProperties({
  baseStyle: 'OBGridLinkButton',
  showDown: true,
  showFocused: true,
  showFocusedAsOver: true,
  showRollOver: true,
  autoFit: true,
  height: 1,
  overflow: 'visible'
});

/******************************/

isc.OBViewGrid.addProperties({
  // note should be the same as the height of the OBGridButtonsComponent
  recordComponentHeight: 21,
  cellHeight: 25,
  bodyStyleName: 'OBViewGridBody'
});

isc.OBViewGrid.changeDefaults('editLinkFieldProperties', {
  filterEditorProperties: {
    textBoxStyle: 'OBGridFilterStaticText',
    textAlign: 'center'
  }
});

isc.OBViewGrid.changeDefaults('checkboxFieldDefaults', {
  filterEditorProperties: {
    textBoxStyle: 'OBGridFilterStaticText',
    nonClickableTextBoxStyle: 'OBGridFilterStaticText',
    clickableTextBoxStyle: 'OBGridFilterStaticTextLink',
    textAlign: 'center'
  }
});

/******************************/

isc.OBAlertGrid.addProperties({
  bodyStyleName: 'OBAlertGridBody'
});

/******************************/

isc.OBGridFormButton.addProperties({
  baseStyle: 'OBGridFormButton',
  titleStyle: 'OBFormButtonTitle',

  width: 1,
  height: 21,
  overflow: 'visible'
});