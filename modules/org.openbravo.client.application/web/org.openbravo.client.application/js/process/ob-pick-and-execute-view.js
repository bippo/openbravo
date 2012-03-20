/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
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

isc.defineClass('OBPickAndExecuteView', isc.OBPopup);


isc.OBPickAndExecuteView.addProperties({

  // Override default properties of OBPopup
  canDragReposition: false,
  canDragResize: false,
  isModal: false,
  showModalMask: false,
  dismissOnEscape: false,
  showMinimizeButton: false,
  showMaximizeButton: false,
  showFooter: false,
  showTitle: true,

  width: '100%',
  height: '100%',
  overflow: 'auto',
  autoSize: false,

  dataSource: null,

  viewGrid: null,

  gridFields: [],

  initWidget: function () {

    var view = this,
        okButton, cancelButton, i, buttonLayout = [];

    function actionClick() {
      if (view.validate()) {
        view.doProcess(this._buttonValue);
      }
    }

    okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUIAPP_Done'),
      _buttonValue: 'DONE',
      click: actionClick
    });

    cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      click: function () {
        view.closeClick();
      }
    });

    this.prepareGridFields(this.viewProperties.fields);

    this._addIconField();

    this.dataSource = this.viewProperties.dataSource;
    this.dataSource.view = this;
    this.title = this.windowTitle;

    // the datasource object is defined on viewProperties, do not destroy it
    this.dataSource.potentiallyShared = true;

    this.viewGrid = isc.OBPickAndExecuteGrid.create({
      view: this,
      fields: this.gridFields,
      height: '80%',
      dataSource: this.dataSource,
      gridProperties: this.viewProperties.gridProperties
    });

    buttonLayout.push(isc.LayoutSpacer.create({}));

    if (this.buttons && !isc.isA.emptyObject(this.buttons)) {
      for (i in this.buttons) {
        if (this.buttons.hasOwnProperty(i)) {

          buttonLayout.push(isc.OBFormButton.create({
            title: this.buttons[i],
            _buttonValue: i,
            click: actionClick
          }));

          // pushing a spacer
          buttonLayout.push(isc.LayoutSpacer.create({
            width: 32
          }));
        }
      }
    } else {
      buttonLayout.push(okButton);
      buttonLayout.push(isc.LayoutSpacer.create({
        width: 32
      }));
    }

    buttonLayout.push(cancelButton);
    buttonLayout.push(isc.LayoutSpacer.create({}));

    this.items = [this.viewGrid, isc.HLayout.create({
      align: 'center',
      width: '100%',
      members: [isc.HLayout.create({
        width: 1,
        overflow: 'visible',
        styleName: this.buttonBarStyleName,
        height: this.buttonBarHeight,
        defaultLayoutAlign: 'center',
        members: buttonLayout
      })]
    })];

    this.Super('initWidget', arguments);
    this.viewGrid.fetchData();
  },

  closeClick: function (refresh) {
    var window = this.parentWindow;

    window.processLayout.hide();
    window.toolBarLayout.show();
    window.view.show();

    if (refresh) {
      window.refresh();
    }

    this.Super('closeClick', arguments);
  },

  prepareGridFields: function (fields) {
    var result = isc.OBStandardView.getPrototype().prepareGridFields.apply(this, arguments),
        i, f, len = result.length;

    for (i = 0; i < len; i++) {
      if (result[i].editorProperties && result[i].editorProperties.disabled) {
        result[i].canEdit = false;
        result[i].readOnlyEditorType = 'OBTextItem';
      } else {
        result[i].validateOnExit = true;
      }
    }

    this.gridFields = result;
  },

  _addIconField: function () {
    if (!this.gridFields) {
      return;
    }

    this.gridFields.unshift({
      name: '_pin',
      type: 'text',
      title: '&nbsp;',
      canEdit: false,
      canFilter: false,
      canSort: false,
      width: 32,
      formatCellValue: function (value, record, rowNum, colNum, grid) {
        if (record[grid.selectionProperty]) {
          return '<img src="web/org.openbravo.client.application/images/iconPin.png" />';
        }
        return '';
      },
      formatEditorValue: function (value, record, rowNum, colNum, grid) {
        return this.formatCellValue(arguments);
      }
    });
  },

  // dummy required by OBStandardView.prepareGridFields
  setFieldFormProperties: function () {},

  validate: function () {
    var viewGrid = this.viewGrid;

    viewGrid.endEditing();
    return !viewGrid.hasErrors();
  },

  doProcess: function (btnValue) {
    var i, tmp, view = this,
        grid = view.viewGrid,
        activeView = view.parentWindow && view.parentWindow.activeView,
        allProperties = activeView.getContextInfo(false, true, false, true) || {},
        selection = grid.getSelectedRecords() || [],
        len = selection.length;

    allProperties._selection = [];
    allProperties._allRows = [];
    allProperties._buttonValue = btnValue || 'DONE';

    for (i = 0; i < len; i++) {
      tmp = isc.addProperties({}, selection[i], grid.getEditedRecord(selection[i]));
      allProperties._selection.push(tmp);
    }

    len = (grid.data.allRows && grid.data.allRows.length) || 0;

    for (i = 0; i < len; i++) {
      tmp = isc.addProperties({}, grid.data.allRows[i], grid.getEditedRecord(grid.data.allRows[i]));
      allProperties._allRows.push(tmp);
    }

    OB.RemoteCallManager.call(this.actionHandler, allProperties, {
      processId: this.processId,
      windowId: this.windowId
    }, function () {
      view.closeClick(true);
    });
  }
});