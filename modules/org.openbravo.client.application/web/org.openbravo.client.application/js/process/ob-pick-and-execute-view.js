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

  addNewButton: null,

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

    if (this.viewProperties.showSelect) {
      this._addIconField();
    }

    if (this.viewProperties.allowDelete) {
      this._addDeleteField();
    }

    this.dataSource = this.viewProperties.dataSource;
    this.dataSource.view = this;
    this.title = this.windowTitle;

    // the datasource object is defined on viewProperties, do not destroy it
    this.dataSource.potentiallyShared = true;

    this.viewGrid = isc.OBPickAndExecuteGrid.create({
      view: this,
      fields: this.gridFields,
      height: '*',
      cellHeight: OB.Styles.Process.PickAndExecute.gridCellHeight,
      dataSource: this.dataSource,
      gridProperties: this.viewProperties.gridProperties,
      selectionAppearance: (this.viewProperties.showSelect ? 'checkbox' : 'rowStyle'),
      selectionType: 'simple',
      canRemoveRecords: (this.viewProperties.allowDelete ? true : false),
      saveLocally: (this.viewProperties.allowDelete || this.viewProperties.allowAdd ? true : false),
      autoSaveEdits: (this.viewProperties.allowDelete || this.viewProperties.allowAdd ? true : false),
      neverValidate: (this.viewProperties.allowDelete || this.viewProperties.allowAdd ? true : false),
      showGridSummary: this.showGridSummary
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

    if (this.viewProperties.allowAdd) {
      this.addNewButton = isc.OBLinkButtonItem.create({
        title: '[ ' + OB.I18N.getLabel('OBUIAPP_AddNew') + ' ]',
        action: function () {
          var newValues;
          view.viewGrid.endEditing();
          if (view.viewProperties.newFn) {
            newValues = view.viewProperties.newFn(view.viewGrid);
          }
          view.viewGrid.startEditingNew(newValues);
        }
      });
    }
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.pickandexecute.button.addnew', this.addNewButton);

    this.items = [this.viewGrid, isc.HLayout.create({
      height: 1,
      overflow: 'visible',
      align: 'left',
      width: '100%',
      visibility: (this.addNewButton ? 'visible' : 'hidden'),
      members: (this.addNewButton ? [this.addNewButton] : [])
    }), isc.HLayout.create({
      align: 'center',
      width: '100%',
      height: OB.Styles.Process.PickAndExecute.buttonLayoutHeight,
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

    if (this.viewGrid.saveLocally) {
      // Using "disconnected" data to avoid update/remove/add operations to the back-end
      // http://www.smartclient.com/docs/8.1/a/b/c/go.html#method..DataSource.fetchData
      this.dataSource.fetchData(this.viewGrid.getFetchRequestParams(), this.viewGrid.ID + ".setData(data)");
    } else {
      this.viewGrid.fetchData();
    }
  },

  closeClick: function (refresh, message) {
    var window = this.parentWindow;

    window.processLayout.hide();
    window.toolBarLayout.show();
    window.view.show();

    if (message) {
      window.view.messageBar.setMessage(message.severity, message.text);
    }

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

      if (result[i].showGridSummary) {
        if (!this.showGridSummary) {
          this.showGridSummary = true;
        }
      } else {
        result[i].showGridSummary = false;
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
      type: 'boolean',
      title: '&nbsp;',
      canEdit: false,
      canFilter: false,
      canSort: false,
      canReorder: false,
      canHide: false,
      canFreeze: false,
      canDragResize: false,
      canGroupBy: false,
      autoExpand: false,
      width: OB.Styles.Process.PickAndExecute.pinColumnWidth,
      formatCellValue: function (value, record, rowNum, colNum, grid) {
        if (record[grid.selectionProperty]) {
          return '<img src="' + OB.Styles.Process.PickAndExecute.iconPinSrc + '" />';
        }
        return '';
      },
      formatEditorValue: function (value, record, rowNum, colNum, grid) {
        return this.formatCellValue(arguments);
      }
    });
  },

  _addDeleteField: function () {
    if (!this.gridFields) {
      return;
    }
    this.gridFields.unshift({
      name: '_delete',
      type: 'boolean',
      title: '&nbsp;',
      canEdit: false,
      canFilter: false,
      canSort: false,
      canReorder: false,
      canHide: false,
      canFreeze: false,
      canDragResize: false,
      canGroupBy: false,
      autoExpand: false,
      align: 'center',
      cellAlign: 'center',
      isRemoveField: true,
      //width: 32, // No effect
      formatCellValue: function (value, record, rowNum, colNum, grid) {
        var src = OB.Styles.Process.PickAndExecute.iconDeleteSrc,
            srcWithoutExt = src.substring(0, src.lastIndexOf('.')),
            srcExt = src.substring(src.lastIndexOf('.') + 1, src.length),
            onmouseover = 'this.src=\'' + srcWithoutExt + '_Over.' + srcExt + '\'',
            onmousedown = 'this.src=\'' + srcWithoutExt + '_Down.' + srcExt + '\'',
            onmouseout = 'this.src=\'' + src + '\'';
        return '<img style="cursor: pointer;" onmouseover="' + onmouseover + '" onmousedown="' + onmousedown + '" onmouseout="' + onmouseout + '" src="' + src + '" />';
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
        len = selection.length,
        allRows = grid.data.allRows || grid.data;

    allProperties._selection = [];
    allProperties._allRows = [];
    allProperties._buttonValue = btnValue || 'DONE';

    for (i = 0; i < len; i++) {
      tmp = isc.addProperties({}, selection[i], grid.getEditedRecord(selection[i]));
      allProperties._selection.push(tmp);
    }


    len = (allRows && allRows.length) || 0;

    for (i = 0; i < len; i++) {
      tmp = isc.addProperties({}, allRows[i], grid.getEditedRecord(allRows[i]));
      allProperties._allRows.push(tmp);
    }

    OB.RemoteCallManager.call(this.actionHandler, allProperties, {
      processId: this.processId,
      windowId: this.windowId
    }, function (rpcResponse, data, rpcRequest) {
      view.closeClick(true, (data && data.message));
    });
  }
});