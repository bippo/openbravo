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
// = OBSelectorPopupWindow =
// The selector popup window shown when clicking the picker icon. Contains 
// a selection grid and cancel/ok buttons.
//
isc.ClassFactory.defineClass('OBSelectorPopupWindow', isc.OBPopup);

isc.OBSelectorPopupWindow.addProperties({
  canDragReposition: true,
  canDragResize: true,
  dismissOnEscape: true,
  showMaximizeButton: true,

  defaultSelectorGridField: {
    canFreeze: true,
    canGroupBy: false
  },

  initWidget: function () {
    var selectorWindow = this,
        okButton, cancelButton, operator, i;

    this.setFilterEditorProperties(this.selectorGridFields);

    okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
      click: function () {
        selectorWindow.setValueInField();
      }
    });
    cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      click: function () {
        selectorWindow.hide();
        selectorWindow.selector.focusInItem();
      }
    });

    OB.Utilities.applyDefaultValues(this.selectorGridFields, this.defaultSelectorGridField);

    if (this.selector.popupTextMatchStyle === 'substring') {
      operator = 'iContains';
    } else {
      operator = 'iStartsWith';
    }

    for (i = 0; i < this.selectorGridFields.length; i++) {
      this.selectorGridFields[i].canSort = (this.selectorGridFields[i].canSort === false ? false : true);
      if (this.selectorGridFields[i].disableFilter) {
        this.selectorGridFields[i].canFilter = false;
      } else {
        this.selectorGridFields[i].canFilter = true;
      }
    }
    if (!this.dataSource.fields || !this.dataSource.fields.length || this.dataSource.fields.length === 0) {
      this.dataSource.fields = this.selectorGridFields;
      this.dataSource.init();
    }
    this.selectorGrid = isc.OBGrid.create({

      selector: this.selector,

      dataProperties: {
        useClientFiltering: false,
        useClientSorting: false
      },

      width: this.selectorGridProperties.width,
      height: this.selectorGridProperties.height,
      alternateRecordStyles: this.selectorGridProperties.alternateRecordStyles,
      dataSource: this.dataSource,
      showFilterEditor: true,
      sortField: this.displayField,

      onFetchData: function (criteria, requestProperties) {
        requestProperties = requestProperties || {};
        requestProperties.params = this.getFetchRequestParams(requestProperties.params);
      },

      getFetchRequestParams: function (params) {
        params = params || {};
        // on purpose not sending the third boolean param
        isc.addProperties(params, this.selector.form.view.getContextInfo(false, true));

        // also adds the special ORG parameter
        if (params.inpadOrgId) {
          params[OB.Constants.ORG_PARAMETER] = params.inpadOrgId;
        }
        params[OB.Constants.WHERE_PARAMETER] = this.selector.whereClause;

        // set the default sort option
        params[OB.Constants.SORTBY_PARAMETER] = this.displayField;

        params._selectorDefinitionId = this.selector.selectorDefinitionId;
        params._requestType = 'Window';

        // add field's default filter expressions
        params.filterClass = 'org.openbravo.userinterface.selector.SelectorDataSourceFilter';

        if (this.getSelectedRecord()) {
          params._targetRecordId = this.targetRecordId;
        }
        return params;
      },

      dataArrived: function () {
        var record, rowNum;
        this.Super('dataArrived', arguments);
        // check if a record has been selected, if
        // not take the one
        // from the selectorField
        // by doing this when data arrives the selection
        // will show up
        // when the record shows in view
        if (this.targetRecordId) {
          record = this.data.find(this.selector.valueField, this.targetRecordId);
          rowNum = this.getRecordIndex(record);
          this.scrollToRow(rowNum);
          this.selectSingleRecord(record);
          delete this.targetRecordId;
        } else {
          this.selectSingleRecord(null);
        }
      },
      fields: this.selectorGridFields,
      recordDoubleClick: function () {
        selectorWindow.setValueInField();
      }
    });

    this.items = [this.selectorGrid, isc.HLayout.create({
      styleName: this.buttonBarStyleName,
      height: this.buttonBarHeight,
      defaultLayoutAlign: 'center',
      members: [isc.LayoutSpacer.create({}), okButton, isc.LayoutSpacer.create({
        width: this.buttonBarSpace
      }), cancelButton, isc.LayoutSpacer.create({})]
    })];
    this.Super('initWidget', arguments);
  },

  setFilterEditorProperties: function (gridFields) {
    var type, selectorWindow = this,
        keyPressFunction, clickFunction, i, gridField;

    keyPressFunction = function (item, form, keyName, characterValue) {
      if (keyName === 'Escape') {
        selectorWindow.hide();
        return false;
      }
      return true;
    };

    clickFunction = function (form, item, icon) {
      item.setValue(null);
      selectorWindow.selectorGrid.focusInFilterEditor(item);
      selectorWindow.selectorGrid.filterByEditor();
    };

    for (i = 0; i < gridFields.length; i++) {
      gridField = gridFields[i];

      type = isc.SimpleType.getType(gridField.type);

      if (type.filterEditorType && !gridField.filterEditorType) {
        gridField.filterEditorType = type.filterEditorType;
      }

      gridField.canFilter = (gridField.canFilter === false ? false : true);
      gridField.filterOnKeypress = (gridField.filterOnKeypress === false ? false : true);

      if (!gridField.filterEditorProperties) {
        gridField.filterEditorProperties = {
          required: false
        };
      } else {
        gridField.filterEditorProperties.required = false;
      }

      gridField.filterEditorProperties.keyPress = keyPressFunction;

      if (!gridField.filterEditorProperties.icons) {
        gridField.filterEditorProperties.icons = [];
      }

      gridField.filterEditorProperties.showLabel = false;
      gridField.filterEditorProperties.showTitle = false;
      gridField.filterEditorProperties.selectorWindow = selectorWindow;
      gridField.filterEditorProperties.textMatchStyle = selectorWindow.selector.popupTextMatchStyle;
    }
  },

  closeClick: function () {
    this.hide(arguments);
  },

  hide: function () {
    this.Super('hide', arguments);
    //focus is now moved to the next item in the form automatically
    if (!this.selector.form.getFocusItem()) {
      this.selector.focusInItem();
    }
  },

  show: function (applyDefaultFilter) {
    // draw now already otherwise the filter does not work the
    // first time    
    var ret = this.Super('show', arguments);
    if (applyDefaultFilter) {
      this.selectorGrid.setFilterEditorCriteria(this.defaultFilter);
      this.selectorGrid.filterByEditor();
    }
    if (this.selectorGrid.isDrawn()) {
      this.selectorGrid.focusInFilterEditor();
    } else {
      isc.Page.setEvent(isc.EH.IDLE, this.selectorGrid, isc.Page.FIRE_ONCE, 'focusInFilterEditor');
    }

    if (this.selector.getValue()) {
      this.selectorGrid.selectSingleRecord(this.selectorGrid.data.find(this.valueField, this.selector.getValue()));
    } else {
      this.selectorGrid.selectSingleRecord(null);
    }

    return ret;
  },

  open: function () {
    var selectorWindow = this,
        callback, data;

    data = {
      '_selectorDefinitionId': this.selectorDefinitionId || this.selector.selectorDefinitionId
    };

    // purposely not passing the third boolean param
    isc.addProperties(data, this.selector.form.view.getContextInfo(false, true));

    callback = function (resp, data, req) {
      selectorWindow.fetchDefaultsCallback(resp, data, req);
    };
    OB.RemoteCallManager.call('org.openbravo.userinterface.selector.SelectorDefaultFilterActionHandler', data, data, callback);
  },

  fetchDefaultsCallback: function (rpcResponse, data, rpcRequest) {
    var defaultFilter = {};
    if (data) {
      defaultFilter = {}; // Reset filter
      isc.addProperties(defaultFilter, data);
    }

    // adds the selector id to filter used to get filter information
    defaultFilter._selectorDefinitionId = this.selector.selectorDefinitionId;
    this.defaultFilter = defaultFilter;
    this.selectorGrid.targetRecordId = this.selector.getValue();
    this.show(true);
  },

  setValueInField: function () {
    this.selector.setValueFromRecord(this.selectorGrid.getSelectedRecord(), true);
    this.hide();
  }
});

// = Selector Item =
// Contains the OBSelector Item. This widget consists of two main parts:
// 1) a combo box with a picker icon
// 2) a popup window showing a search grid with data
//
isc.ClassFactory.defineClass('OBSelectorItem', isc.ComboBoxItem);

isc.ClassFactory.mixInInterface('OBSelectorItem', 'OBLinkTitleItem');

isc.OBSelectorItem.addProperties({
  hasPickList: true,
  popupTextMatchStyle: 'startswith',
  suggestionTextMatchStyle: 'startswith',
  showOptionsFromDataSource: true,

  // https://issues.openbravo.com/view.php?id=18739
  selectOnFocus: false,
  // still do select on focus initially
  doInitialSelectOnFocus: true,

  // Setting this to false results in the picklist to be shown 
  // on focus, specific SC logic
  //  addUnknownValues: false,
  // ** {{{ selectorGridFields }}} **
  // the definition of the columns in the popup window
  selectorGridFields: [{
    title: OB.I18N.getLabel('OBUISC_Identifier'),
    name: OB.Constants.IDENTIFIER
  }],

  // Do not fetch data upon creation
  // http://www.smartclient.com/docs/8.1/a/b/c/go.html#attr..ComboBoxItem.optionDataSource
  fetchMissingValues: false,

  autoFetchData: false,
  showPickerIcon: true,
  //  selectors should not be validated on change, only after its content has been deleted
  //  and after an option of the combo has been selected
  //  see issue 19956 (https://issues.openbravo.com/view.php?id=19956)
  validateOnChange: false,
  completeOnTab: true,
  // note validateonexit does not work when completeOnTab is true
  // setting it anyway, the this.validate() is called in the blur
  validateOnExit: true,

  pickListProperties: {
    fetchDelay: 400,
    showHeaderContextMenu: false,
    dataProperties: {
      useClientFiltering: false
    }
  },

  hidePickListOnBlur: function () {
    // when the form gets redrawn the focus may not be in
    // the item but it is still the item which gets the focus
    // after redrawing
    if (this.form && this.form._isRedrawing && this.form.getFocusItem() === this) {
      return;
    }

    this.Super('hidePickListOnBlur', arguments);
  },

  setUpPickList: function (show, queueFetches, request) {
    this.pickListProperties.canResizeFields = true;
    // Set the pickListWidth just before being shown.
    this.setPickListWidth();
    this.Super('setUpPickList', arguments);
  },

  // don't do update value in all cases, updatevalue results in a data source request
  // to the server, so only do updatevalue when the user changes information
  // https://issues.openbravo.com/view.php?id=16611
  updateValue: function () {
    if (this.form && this.form.grid && (this.form.grid._storingUpdatedEditorValue || this.form.grid._showingEditor || this.form.grid._hidingInlineEditor)) {
      // prevent updatevalue while the form is being shown or hidden
      return;
    }
    this.Super('updateValue', arguments);
  },

  setValue: function (val) {
    var i, displayedVal;

    if (val && this.valueMap) {
      displayedVal = this.valueMap[val];
      for (i in this.valueMap) {
        if (this.valueMap.hasOwnProperty(i)) {
          if (this.valueMap[i] === displayedVal && i !== val) {
            // cleaning up valueMap: there are 2 values that display the same info, keep just the one for
            // the current value
            delete this.valueMap[i];
            break;
          }
        }
      }
    }

    this.Super('setValue', arguments);
  },

  // changed handles the case that the user removes the value using the keyboard
  // this should do the same things as setting the value through the pickvalue
  changed: function (form, item, newValue) {
    // only do the identifier actions when clearing
    // in all other cases pickValue is called
    if (!newValue) {
      this.setValueFromRecord(null);
    }
  },

  setPickListWidth: function () {
    var extraWidth = 0,
        fieldWidth = this.getVisibleWidth();
    if (this.pickListFields.length > 1) {
      extraWidth = 150 * (this.pickListFields.length - 1);
    }

    this.pickListWidth = (fieldWidth < 150 ? 150 : fieldWidth) + extraWidth;
  },

  enableShortcuts: function () {
    var me = this,
        ksAction_ShowPopup;

    ksAction_ShowPopup = function () {
      me.openSelectorWindow();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('Selector_ShowPopup', ['OBSelectorItem', 'OBSelectorItem.icon'], ksAction_ShowPopup);
  },

  init: function () {
    this.enableShortcuts();
    this.icons = [{
      src: this.popupIconSrc,
      width: this.popupIconWidth,
      height: this.popupIconHeight,
      hspace: this.popupIconHspace,
      keyPress: function (keyName, character, form, item, icon) {
        var response = OB.KeyboardManager.Shortcuts.monitor('OBSelectorItem.icon');
        if (response !== false) {
          response = this.Super('keyPress', arguments);
        }
        return response;
      },
      click: function (form, item, icon) {
        item.openSelectorWindow();
      }
    }];

    if (this.disabled) {
      // TODO: disable, remove icons
      this.icons = null;
    }
    if (!this.showSelectorGrid) {
      this.icons = null;
    }

    if (this.showSelectorGrid && !this.form.isPreviewForm) {
      this.selectorWindow = isc.OBSelectorPopupWindow.create({
        // solves issue: https://issues.openbravo.com/view.php?id=17268
        title: (this.form && this.form.grid ? this.form.grid.getField(this.name).title : this.title),
        dataSource: this.optionDataSource,
        selector: this,
        valueField: this.valueField,
        displayField: this.displayField,
        selectorGridFields: isc.shallowClone(this.selectorGridFields)
      });
    }

    this.optionCriteria = {
      _selectorDefinitionId: this.selectorDefinitionId
    };

    return this.Super('init', arguments);
  },

  setValueFromRecord: function (record, fromPopup) {
    var currentValue = this.getValue(),
        identifierFieldName = this.name + '.' + OB.Constants.IDENTIFIER,
        i;
    if (!record) {
      this.storeValue(null);
      this.form.setValue(this.name + '.' + this.displayField, null);
      this.form.setValue(identifierFieldName, null);

      // make sure that the grid does not display the old identifier
      if (this.form.grid && this.form.grid.getEditForm()) {
        this.form.grid.setEditValue(this.form.grid.getEditRow(), this.name, null);
        this.form.grid.setEditValue(this.form.grid.getEditRow(), identifierFieldName, '');
        this.form.grid.setEditValue(this.form.grid.getEditRow(), this.name + '.' + this.displayField, '');
      }
    } else {
      this.handleOutFields(record);
      this.storeValue(record[this.valueField]);
      this.form.setValue(this.name + '.' + this.displayField, record[this.displayField]);
      this.form.setValue(identifierFieldName, record[OB.Constants.IDENTIFIER]);
      if (!this.valueMap) {
        this.valueMap = {};
      }

      this.valueMap[record[this.valueField]] = record[this.displayField];
      this.updateValueMap();
    }

    if (this.form && this.form.handleItemChange) {
      this._hasChanged = true;
      this.form.handleItemChange(this);
    }

    // only jump to the next field if the value has really been set
    if (currentValue && this.form.focusInNextItem) {
      this.form.focusInNextItem(this.name);
    }
  },

  // override blur to not do any change handling
  blur: function (form, item) {},

  handleOutFields: function (record) {
    var i, j, outFields = this.outFields,
        form = this.form,
        grid = this.grid,
        item, value, fields = form.fields || grid.fields,
        numberFormat;
    for (i in outFields) {
      if (outFields.hasOwnProperty(i)) {
        if (outFields[i].suffix) {
          // when it has a suffix
          if (record) {
            value = record[i];
            if (typeof value === 'undefined') {
              continue;
            }
            if (isc.isA.Number(value)) {
              if (outFields[i].formatType && outFields[i].formatType !== '') {
                value = OB.Utilities.Number.JSToOBMasked(value, OB.Format.formats[outFields[i].formatType], OB.Format.defaultDecimalSymbol, OB.Format.defaultGroupingSymbol, OB.Format.defaultGroupingSize);
              } else {
                value = value.toString().replace('.', OB.Format.defaultDecimalSymbol);
              }
            }
            form.hiddenInputs[this.outHiddenInputPrefix + outFields[i].suffix] = value;
            item = form.getItem(outFields[i].fieldName);
            if (item && item.valueMap) {
              item.valueMap[value] = record[outFields[i].fieldName + '._identifier'];
            }
          } else {
            form.hiddenInputs[this.outHiddenInputPrefix + outFields[i].suffix] = null;
          }
        } else {
          // it does not have a suffix
          for (j = 0; j < fields.length; j++) {
            if (fields[j].name !== '' && fields[j].name === outFields[i].fieldName) {
              if (record) {
                value = record[i];
                if (typeof value === 'undefined') {
                  continue;
                }
              } else {
                value = null;
              }
              fields[j].setValue(value);
            }
          }
        }
      }
    }
  },

  openSelectorWindow: function () {
    // always refresh the content of the grid to force a reload
    // if the organization has changed
    if (this.selectorWindow.selectorGrid) {
      this.selectorWindow.selectorGrid.invalidateCache();
    }
    this.selectorWindow.open();
  },

  keyPress: function (item, form, keyName, characterValue) {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBSelectorItem');
    if (response !== false) {
      response = this.Super('keyPress', arguments);
    }
    return response;
  },

  pickValue: function (value) {
    // get the selected record before calling the super, as this super call
    // will deselect the record
    var selectedRecord = this.pickList.getSelectedRecord(),
        ret = this.Super('pickValue', arguments);
    this.setValueFromRecord(selectedRecord);
    return ret;
  },

  filterDataBoundPickList: function (requestProperties, dropCache) {
    requestProperties = requestProperties || {};
    requestProperties.params = requestProperties.params || {};

    // sometimes the value is passed as a filter criteria remove it
    if (this.getValueFieldName() && requestProperties.params[this.getValueFieldName()]) {
      requestProperties.params[this.getValueFieldName()] = null;
    }

    // do not prevent the count operation
    requestProperties.params[isc.OBViewGrid.NO_COUNT_PARAMETER] = 'true';

    // on purpose not passing the third boolean param
    isc.addProperties(requestProperties.params, this.form.view.getContextInfo(false, true));

    // also add the special ORG parameter
    if (requestProperties.params.inpadOrgId) {
      requestProperties.params[OB.Constants.ORG_PARAMETER] = requestProperties.params.inpadOrgId;
    }

    var criteria = this.getPickListFilterCriteria(),
        i;
    for (i = 0; i < criteria.criteria.length; i++) {
      if (criteria.criteria[i].fieldName === this.displayField) {
        // for the suggestion box it is one big or
        requestProperties.params[OB.Constants.OR_EXPRESSION] = 'true';
      }
    }

    // adds the selector id to filter used to get filter information
    requestProperties.params._selectorDefinitionId = this.selectorDefinitionId;

    // add field's default filter expressions
    requestProperties.params.filterClass = 'org.openbravo.userinterface.selector.SelectorDataSourceFilter';

    // the additional where clause
    requestProperties.params[OB.Constants.WHERE_PARAMETER] = this.whereClause;

    // and sort according to the display field
    // initially
    requestProperties.params[OB.Constants.SORTBY_PARAMETER] = this.displayField;

    return this.Super('filterDataBoundPickList', [requestProperties, dropCache]);
  },

  getPickListFilterCriteria: function () {
    var crit = this.Super('getPickListFilterCriteria', arguments);
    this.pickList.data.useClientFiltering = false;
    var criteria = {
      operator: 'or',
      _constructor: 'AdvancedCriteria',
      criteria: []
    };

    // add a dummy criteria to force a fetch
    criteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());

    // only filter if the display field is also passed
    // the displayField filter is not passed when the user clicks the drop-down button
    // display field is passed on the criteria.
    var displayFieldValue = null,
        i;
    if (crit.criteria) {
      for (i = 0; i < crit.criteria.length; i++) {
        if (crit.criteria[i].fieldName === this.displayField) {
          displayFieldValue = crit.criteria[i].value;
        }
      }
    } else if (crit[this.displayField]) {
      displayFieldValue = crit[this.displayField];
    }
    if (displayFieldValue !== null) {
      for (i = 0; i < this.extraSearchFields.length; i++) {
        criteria.criteria.push({
          fieldName: this.extraSearchFields[i],
          operator: 'iContains',
          value: displayFieldValue
        });
      }
      criteria.criteria.push({
        fieldName: this.displayField,
        operator: 'iContains',
        value: displayFieldValue
      });
    }
    return criteria;
  },

  mapValueToDisplay: function (value) {
    var ret = this.Super('mapValueToDisplay', arguments);
    if (ret === value && this.isDisabled()) {
      return '';
    }
    if (ret === value) {
      if (!this.valueMap) {
        this.valueMap = {};
        this.valueMap[value] = '';
        return '';
      } else if (!this.valueMap[value] && OB.Utilities.isUUID(value)) {
        return '';
      }
    }
    return ret;
  },

  destroy: function () {
    // Explicitly destroy the selector window to avoid memory leaks
    if (this.selectorWindow) {
      this.selectorWindow.destroy();
      this.selectorWindow = null;
    }
    this.Super('destroy', arguments);
  }
});

isc.ClassFactory.defineClass('OBSelectorLinkItem', isc.StaticTextItem);

isc.ClassFactory.mixInInterface('OBSelectorLinkItem', 'OBLinkTitleItem');

isc.OBSelectorLinkItem.addProperties({
  canFocus: true,
  showFocused: true,
  wrap: false,
  clipValue: true,

  // show the complete displayed value, handy when the display value got clipped
  itemHoverHTML: function (item, form) {
    return this.getDisplayValue(this.getValue());
  },

  setValue: function (value) {
    var ret = this.Super('setValue', arguments);
    // in this case the clearIcon needs to be shown or hidden
    if (!this.disabled && !this.required) {
      if (value) {
        this.showIcon(this.instanceClearIcon);
      } else {
        this.hideIcon(this.instanceClearIcon);
      }
    }
    return ret;
  },

  click: function () {
    this.showPicker();
    return false;
  },

  keyPress: function (item, form, keyName, characterValue) {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBSelectorLinkItem');
    if (response !== false) {
      response = this.Super('keyPress', arguments);
    }
    return response;
  },

  showPicker: function () {
    if (this.isFocusable()) {
      this.focusInItem();
    }
    this.selectorWindow.open();
  },

  setValueFromRecord: function (record) {
    // note this.displayfield already contains the prefix of the property name
    if (!record) {
      this.setValue(null);
      this.form.setValue(this.displayField, null);
    } else {
      this.setValue(record[this.gridValueField]);
      this.form.setValue(this.displayField, record[this.gridDisplayField]);
      if (!this.valueMap) {
        this.valueMap = {};
      }
      this.valueMap[record[this.gridValueField]] = record[this.gridDisplayField];
      this.updateValueMap();
    }
    this.handleOutFields(record);
    if (this.form && this.form.handleItemChange) {
      this._hasChanged = true;
      this.form.handleItemChange(this);
    }
  },

  handleOutFields: function (record) {
    var i, j, outFields = this.outFields,
        form = this.form,
        grid = this.grid,
        item, value, fields = form.fields || grid.fields;
    for (i in outFields) {
      if (outFields.hasOwnProperty(i)) {
        if (outFields[i].suffix) {
          if (record) {
            value = record[i];
            if (isc.isA.Number(value)) {
              value = OB.Utilities.Number.JSToOBMasked(value, OB.Format.defaultNumericMask, OB.Format.defaultDecimalSymbol, OB.Format.defaultGroupingSymbol, OB.Format.defaultGroupingSize);
            }
            form.hiddenInputs[this.outHiddenInputPrefix + outFields[i].suffix] = value;
          } else {
            form.hiddenInputs[this.outHiddenInputPrefix + outFields[i].suffix] = null;
          }
        } else {
          // it does not have a suffix
          for (j = 0; j < fields.length; j++) {
            if (fields[j].name !== "" && fields[j].name === outFields[i].fieldName) {
              value = record ? record[i] : null;
              fields[j].setValue(value);
            }
          }
        }
      }
    }
  },

  enableShortcuts: function () {
    var me = this,
        ksAction_ShowPopup;
    ksAction_ShowPopup = function () {
      me.showPicker();
      return false; //To avoid keyboard shortcut propagation
    };
    OB.KeyboardManager.Shortcuts.set('SelectorLink_ShowPopup', 'OBSelectorLinkItem', ksAction_ShowPopup);
  },

  init: function () {
    this.enableShortcuts();
    if (this.disabled) {
      this.showPickerIcon = false;
    }

    this.instanceClearIcon = isc.shallowClone(this.clearIcon);
    this.instanceClearIcon.showIf = function (form, item) {
      if (item.disabled) {
        return false;
      }
      if (item.required) {
        return false;
      }
      if (form && form.view && form.view.readOnly) {
        return false;
      }
      if (item.getValue()) {
        return true;
      }
      return false;
    };

    this.instanceClearIcon.click = function () {
      this.formItem.setValue(null);
      this.formItem.form.itemChangeActions();
    };

    this.icons = [this.instanceClearIcon];
    this.icons[0].formItem = this;

    if (this.disabled) {
      // TODO: disable, remove icons
      this.icons = null;
    }

    if (!this.form.isPreviewForm) {
      this.selectorWindow = isc.OBSelectorPopupWindow.create({
        // solves issue: https://issues.openbravo.com/view.php?id=17268
        title: (this.form && this.form.grid ? this.form.grid.getField(this.name).title : this.title),
        dataSource: this.dataSource,
        selector: this,
        valueField: this.gridValueField,
        displayField: this.gridDisplayField,
        selectorGridFields: isc.shallowClone(this.selectorGridFields)
      });
    }

    return this.Super('init', arguments);
  },

  changed: function () {
    var ret = this.Super('changed', arguments);
    this._hasChanged = true;
    this._doFICCall = true;
    if (this.form && this.form.handleItemChange) {
      this.form.handleItemChange(this);
    }
    return ret;
  },

  destroy: function () {
    // Explicitly destroy the selector window to avoid memory leaks
    if (this.selectorWindow) {
      this.selectorWindow.destroy();
      this.selectorWindow = null;
    }
    this.Super('destroy', arguments);
  }
});