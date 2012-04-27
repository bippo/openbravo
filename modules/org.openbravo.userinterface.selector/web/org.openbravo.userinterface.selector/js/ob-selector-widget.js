/*global setWindowEditing, readOnlyLogic, logChanges, setOBTabBehavior, setWindowElementFocus, disableDefaultAction, displayLogic */

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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// = Selector Widget =
// Contains the OBSelector Widget. This widget consists of two main parts:
// 1) a combo box with a picker icon
// 2) a popup window showing a search grid with data
//
// The widget is a compound widget extending the DynamicForm. A number
// of default values are set in the widget for example the width and 
// the columns in the picklist etc. 
isc.ClassFactory.defineClass('OBSelectorWidget', isc.DynamicForm);

// These class properties can be overridden in dependent modules by
// for example doing this in a js file:
// isc.OBSelectorWidget.styling.filterEditorClearIconHeight = 100;
// the dependent modules js file needs to be passes as a static
// resource in the GlobalResources provided by the modules
// componentprovider.
isc.OBSelectorWidget.addClassProperties({
  styling: {

    // ** {{{ selectorFieldTextBoxStyle }}} **
    // Selector input css classname
    selectorFieldTextBoxStyle: 'selectItemText',

    // ** {{{ selectorFieldTextBoxStyleRequired }}} **
    // Selector input css classname
    selectorFieldTextBoxStyleRequired: 'selectItemTextRequired',

    // ** {{{ filterEditorIcon properties }}} **
    // filterEditorIcon is shown in the popup grid next to each field.
    filterEditorClearIconSrc: '[SKINIMG]../../org.openbravo.userinterface.selector/images/filterClear.png',
    filterEditorClearIconWidth: 15,
    filterEditorClearIconHeight: 15,
    filterEditorClearIconHSpace: 0,

    // ** {{{ fieldPicker properties }}} **
    // fieldPicker icon is shown next to the field in the form
    fieldPickerIconSrc: '[SKINIMG]../../org.openbravo.userinterface.selector/images/selectorButton.png',
    fieldPickerIconWidth: 27,
    fieldPickerIconHeight: 17,
    fieldPickerIconHSpace: 0,

    // ** {{{ ListGrid properties }}} **
    // ListGrid shown inside the modal popup after clicking the field picker
    // icon.
    listGridRelativeWidth: '100%',
    listGridRelativeHeight: '100%',

    // ** {{{ Modal popup properties }}} **
    modalPopupAlign: 'center',
    modalPopupRelativeWidth: '85%',
    modalPopupRelativeHeight: '85%',

    // modal popup top left icon
    modalPopupHeaderIconSrc: '[SKINIMG]../../smartclient/images/button/icons/iconSearch.png',
    modalPopupHeaderIconWidth: 16,
    modalPopupHeaderIconHeight: 16,

    // modal popup bottom button group
    modalPopupButtonGroupStyle: 'formLayout',
    modalPopupButtonGroupAlign: 'center',
    modalPopupButtonGroupHeight: 35,
    modalPopupButtonSeparatorWidth: 20,

    // modal popup bottom Ok button
    modalPopupOkButtonSrc: '[SKINIMG]../../smartclient/images/button/icons/iconOk.png',
    modalPopupOkButtonAlign: 'center',
    modalPopupOkButtonWidth: 115,

    // modal popup bottom Cancel button
    modalPopupCancelButtonSrc: '[SKINIMG]../../smartclient/images/button/icons/iconCancel.png',
    modalPopupCancelButtonAlign: 'center',
    modalPopupCancelButtonWidth: 115,

    // Defines the width of the field on the form
    widthDefinition: [135, 280, 425, 570, 715, 860]
  }
});

isc.OBSelectorWidget.addProperties({
  // openbravo specific properties
  selectorDefinitionId: '',

  // ** {{{ Datasource }}} **
  // the datasource is a mandatory field which must be set when creating the
  // widget.
  dataSource: null,

  // ** {{{ Openbravo html field definition }}} **
  // the field used to communicate with the backend
  openbravoFieldId: null,
  openbravoFieldName: null,
  openbravoField: null,

  // ** {{{ showSelectorGrid }}} **
  // enable or disable popup grid
  showSelectorGrid: true,

  // ** {{{ pickListShowing }}} **
  // is set to true when the pick list (suggestion box) is shown.
  pickListShowing: false,

  // ** {{{ disabled }}} **
  disabled: false,

  comboReload: null,

  // ** {{{ enableSelector }}} **
  // call to enable the selector
  enableSelector: function () {
    this.disabled = false;
    this.selectorField.setDisabled(false);
  },

  // ** {{{ disableSelector }}} **
  // call to disable the selector
  disableSelector: function () {
    this.selectorField.setDisabled(true);
    this.disabled = true;
  },

  // ** {{{ required }}} **
  // set to true to make the selector a required field
  required: false,

  // ** {{{ callOut }}} **
  // set to define a callout function to be called
  callOut: function (name) {
    alert(name);
  },

  // ** {{{ displayField and valueField }}} **
  // the fields which are shown in the combo and used as the value.
  displayField: OB.Constants.IDENTIFIER,
  valueField: OB.Constants.ID,
  defaultPopupFilterField: OB.Constants.IDENTIFIER,
  defaultFilter: {},

  // ** {{{ pickListFields }}} **
  // The pick list columns shown, as a default is only the identifier
  pickListFields: [{
    name: OB.Constants.IDENTIFIER
  }],

  // ** {{{ whereClause }}} **
  // The HQL where clause.
  whereClause: '',

  // ** {{{ title }}} **
  // Displayed in the top of the popup window
  title: 'Selector',

  // ** {{{ extraSearchFields }}} **
  // extra fields to search on for the suggestionbox
  extraSearchFields: [],

  // ** {{{ outFields }}} **
  // fields defined as out to compose the returning object
  // If the Object is empty, the whole row object is returned
  outFields: {},

  // ** {{{ outHiddenInputs }}} **
  // An array referencing the hidden inputs generated for 'out' columns
  // Note: Used for 2.50 backward compatibility
  outHiddenInputs: {},

  // ** {{{ outHiddenInputPrefix }}} **
  // Prefix used by all hidden inputs. This prefix concatenated with the out field suffix
  // gets the name for the hidden input, e.g. inpmProductId_PUOM
  // Note: Used for 2.50 backward compatibility
  outHiddenInputPrefix: '',

  // ** {{{ popupTextMatchStyle and suggestionTextMatchStyle }}} **
  // text matching
  popupTextMatchStyle: 'startswith',
  suggestionTextMatchStyle: 'startswith',

  // ** {{{ defaultSelectorGridField }}} **
  // default values are applied to the selector grid fields
  defaultSelectorGridField: {
    canFreeze: true,
    canGroupBy: false,
    filterOnKeypress: true
  },

  // ** {{{ selectorGridFields }}} **
  // the definition of the columns in the popup window
  selectorGridFields: [{
    title: OB.I18N.getLabel('OBUISC_Identifier'),
    name: OB.Constants.IDENTIFIER
  }],

  // ** {{{ Standard form properties }}} **
  writeFormTag: false,
  position: 'relative',
  cellStyle: 'Combo',
  autoDraw: true,
  numCols: 2,
  width: 280,
  // note is set again in the initialization
  // ** {{{ initialValue }}} **
  // used when reseting the form in OB
  initialValue: null,

  // ** {{{ setSelectorValueFromGrid }}} **
  // called when a value is selected in the grid
  // sets the combo and the openbravo field
  setSelectorValueFromGrid: function () {
    var changed = false,
        oldValue = this.selector.openbravoField.value,
        selected = this.selector.selectorGrid.getSelectedRecord(),
        newValue;
    if (selected) {
      newValue = selected[this.selector.valueField];
      changed = oldValue !== newValue;
      this.selector.openbravoField.value = newValue;
      this.selector.selectorField.setValue(newValue);

      // set the value in the valuemap so it shows up correctly without loading
      // all the info again
      if (!this.selector.selectorField.valueMap) {
        this.selector.selectorField.valueMap = {};
      }
      this.selector.selectorField.valueMap[newValue] = selected[this.selector.displayField];
      this.selector.selectorField.updateValueMap();
    } else {
      changed = oldValue !== '';
      this.selector.openbravoField.value = '';
      this.selector.selectorField.setValue(null);
    }
    this.selector.selectorWindow.hide();

    // openbravo specific code
    this.selector.openbravoChanged(selected);

    this.selector.selectorField.focusInItem();
  },

  // ** {{{ setSelectorValueFromField }}} **
  // called when a value is selected in the combo
  // sets the selected value in the openbravo field
  setSelectorValueFromField: function (form, item, value) {
    if (item.selector.pickListShowing) {
      return;
    }

    var oldValue = item.selector.openbravoField.value,
        changed = false,
        selected = item.selector.selectorField.getSelectedRecord();

    if (selected) {
      changed = (oldValue !== item.selector.selectorField.getSelectedRecord()[item.selector.valueField]);
      item.selector.openbravoField.value = item.selector.selectorField.getSelectedRecord()[item.selector.valueField];
    } else {
      changed = (oldValue !== '');
      item.selector.openbravoField.value = '';
    }

    // openbravo specific code
    item.selector.openbravoChanged(selected);

    // this resulted in strange behavior in FF, therefore commented
    // out
    // in FF the focus was placed on the field after each keypress.
    // field.focusInItem();
  },

  // ** {{{ onValueChanged }}} **
  // An stub function executed at the end of openbravoChanged.
  // This is the place to hook an specific functionality.
  // The parameter passed is a JavaScript object containing all the
  // id/value plus all fields defined as 'Out field'
  onValueChanged: function (selected) {
    return;
  },

  // ** {{{ openbravoChanged }}} **
  // calls different Openbravo utils.js methods
  // to control overall form state. Is called when the value
  // changes. Executes onValueChanged function.
  openbravoChanged: function (selected) {
    var selectedObj = {},
        fieldsLength = this.outFields.length,
        i, hiddenInput;

    function changeField(field, value) {
      var inputId;
      if (field) {
        inputId = document.getElementById(field);
        if (inputId) {
          inputId.value = value === undefined || value === null ? '' : value;
          if (typeof inputId.onchange === 'function') {
            inputId.onchange();
          }
        }
      }
    }

    if (!selected) {
      // Cleaning hidden inputs
      for (i in this.outHiddenInputs) {
        if (this.outHiddenInputs.hasOwnProperty(i) && this.outHiddenInputs[i]) {
          this.outHiddenInputs[i].value = '';
        }
      }

      for (i in this.outFields) {
        if (this.outFields.hasOwnProperty(i)) {
          changeField(this.outFields[i].fieldName, '');
        }
      }
    } else {
      for (i in this.outFields) {
        if (this.outFields.hasOwnProperty(i)) {
          selectedObj[i] = selected[i];
          if (!this.outFields[i]) {
            // skip id and _identifier and other columns without
            // associated tab field
            continue;
          }

          if (this.outFields[i].suffix) {
            hiddenInput = this.outHiddenInputs[this.outHiddenInputPrefix + this.outFields[i].suffix];
            if (hiddenInput) {
              hiddenInput.value = selected[i] ? selected[i] : '';
            }
          }

          changeField(this.outFields[i].fieldName, selected[i]);
        }
      }
    }

    if (document.getElementById('linkButtonEdition') && typeof setWindowEditing === 'function') {
      setWindowEditing(true);
    }

    if (this.callOut !== null) {
      this.callOut(this.openbravoField.name);
    }
    if (this.comboReload) {
      this.comboReload(this.openbravoField.name);
    }
    if (typeof readOnlyLogic === 'function') {
      readOnlyLogic();
    }
    if (typeof displayLogic === 'function') {
      displayLogic();
    }
    if (typeof logChanges === 'function') {
      logChanges(this.openbravoField);
    }

    // revalidate after blurring or when the value has been set
    var valueSet = this.openbravoField.value !== null && this.openbravoField.value !== '';
    // reset any warnings
    if (valueSet) {
      this.checkDefaultValidations();
    }

    this.onValueChanged(selectedObj);
  },

  fetchDefaultsCallback: function (rpcResponse, data, rpcRequest) {

    if (data) {
      this.defaultFilter = {}; // Reset filter
      isc.addProperties(this.defaultFilter, data);
    }

    if (this.selectorField.getDisplayValue()) { // Prevents overriding a default
      // value with empty
      this.defaultFilter[this.defaultPopupFilterField] = this.selectorField.getDisplayValue();
    }

    // adds the selector id to filter used to get filter information
    this.defaultFilter._selectorDefinitionId = this.selectorDefinitionId;

    setOBTabBehavior(false);

    // draw now already otherwise the filter does not work the
    // first time
    this.selectorWindow.show();
    if (this.selectorGrid.clearFilter) {
      this.selectorGrid.clearFilter();
    }
    this.selectorGrid.setFilterEditorCriteria(this.defaultFilter);
    this.selectorGrid.filterByEditor();
    this.selectorGrid.focusInFilterEditor();

    if (this.openbravoField.value !== '') {
      this.selectorGrid.selectSingleRecord(this.selectorGrid.data.find(
      this.valueField, this.openbravoField.value));
    } else {
      this.selectorGrid.selectSingleRecord(null);
    }
  },

  // ** {{{ openSelectorWindow }}} **
  // open the popup window and make sure that it has the correct
  // filter set
  openSelectorWindow: function (form, field, icon) {
    var data = {
      '_selectorDefinitionId': this.selectorDefinitionId
    };
    OB.Utilities.addFormInputsToCriteria(data);
    OB.RemoteCallManager.call('org.openbravo.userinterface.selector.SelectorDefaultFilterActionHandler', data, data, this.ID + '.fetchDefaultsCallback(rpcResponse, data, rpcRequest)');
  },

  // ** {{{ checkDefaultValidations }}} **
  // checks mandatory constraint
  checkDefaultValidations: function (form, item) {
    var missingSpan = document.getElementById(this.openbravoField.id + 'missingSpan');
    var invalidSpan = document.getElementById(this.openbravoField.id + 'invalidSpan');
    if (!invalidSpan || (!missingSpan && this.required)) {
      return;
    }
    if (this.selectorField.getDisplayValue().length > 0 && (this.openbravoField.value === null || this.openbravoField.value === '')) {
      if (missingSpan) {
        missingSpan.style.display = 'none';
      }
      invalidSpan.style.display = '';
    } else if (this.required && (this.openbravoField.value === null || this.openbravoField.value === '')) {
      invalidSpan.style.display = 'none';
      missingSpan.style.display = '';
    } else {
      if (missingSpan) {
        missingSpan.style.display = 'none';
      }
      invalidSpan.style.display = 'none';
    }
  },

  // ** {{{ getSelectorContainerHtmlObj }}} **
  // Finds the html container for the selector field
  getSelectorContainerHtmlObj: function () {
    var childObj;
    var selectorContainerHtmlObj;
    childObj = this.selectorField.getDataElement();
    for (;;) {
      for (;;) {
        childObj = childObj.parentNode;
        if ((childObj.tagName.toLowerCase() === 'div' && childObj.getAttribute('id')) || childObj.tagName.toLowerCase() === 'body') {
          break;
        }
      }
      if (childObj.tagName.toLowerCase() === 'body') {
        break;
      } else if (childObj.getAttribute('id').indexOf('isc_') !== -1) {
        selectorContainerHtmlObj = childObj;
      }
    }
    return selectorContainerHtmlObj;
  },

  // ** {{{ setFilterEditorProperties }}} **
  // Called to set the filter editor properties on each grid field.
  setFilterEditorProperties: function (gridFields) {
    var selector = this,
        i, keyPressFunction, clickFunction;

    keyPressFunction = function (item, form, keyName, characterValue) {
      if (keyName === 'Escape') {
        selector.selectorWindow.hide();
        return false;
      }
      return true;
    };

    clickFunction = function (form, item, icon) {
      item.setValue(null);
      selector.selectorGrid.focusInFilterEditor(item);
      selector.selectorGrid.filterByEditor();
    };

    for (i = 0; i < gridFields.length; i++) {
      var gridField = gridFields[i];
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

      gridField.filterEditorProperties.textMatchStyle = this.popupTextMatchStyle;

      // add the icon on the right to the other icons
      var icons = gridField.filterEditorProperties.icons;
      var iconsLength = icons.length;
      icons[iconsLength] = {
        selector: this,
        src: isc.OBSelectorWidget.styling.filterEditorClearIconSrc,
        showDown: true,
        showDownIcon: true,
        showFocused: true,
        showOver: true,
        width: isc.OBSelectorWidget.styling.filterEditorClearIconWidth,
        height: isc.OBSelectorWidget.styling.filterEditorClearIconHeight,
        hspace: isc.OBSelectorWidget.styling.filterEditorClearIconHSpace,
        // note
        // unsupported
        // feature:
        // http://forums.smartclient.com/showthread.php?p=34868
        click: clickFunction
      };
    }
  },

  afterDrawDo: function () {
    var selectorContainerHtmlObj = this.getSelectorContainerHtmlObj();
    if (!selectorContainerHtmlObj.focusLogic) {
      selectorContainerHtmlObj.focusLogic = function (param) {
        if (param === 'mustBeJumped') {
          return true;
        } else if (param === 'mustBeIgnored') {
          return true;
        } else if (param === 'couldHaveFocus') {
          return false;
        }
      };
    }
  },

  // ** {{{ initWidget }}} **
  // Override initWidget to set the parts of the form. Creates the
  // form, suggestion box and popup modal and grid components.
  initWidget: function () {

    var baseTestRegistryName = 'org.openbravo.userinterface.selector.' + this.openbravoField + '.';

    // Do not destroy dataSource after creation
    // https://issues.openbravo.com/view.php?id=18456
    this.dataSource.potentiallyShared = true;

    if (this.numCols > 0 && this.numCols <= isc.OBSelectorWidget.styling.widthDefinition.length) {
      this.width = isc.OBSelectorWidget.styling.widthDefinition[this.numCols - 1];
      // } else {
      // // TODO log this error case?
    }

    // add the combobox to the DynamicForm
    this.fields = [{
      textMatchStyle: this.suggestionTextMatchStyle,
      selector: this,
      selectOnFocus: true,
      autoFetchData: false,
      showTitle: false,
      showPickerIcon: true,
      shouldSaveValue: false,
      validateOnChange: true,
      completeOnTab: true,

      valueMap: {},
      icons: [{
        selector: this,
        showFocused: true,
        showOver: true,
        src: isc.OBSelectorWidget.styling.fieldPickerIconSrc,
        width: isc.OBSelectorWidget.styling.fieldPickerIconWidth,
        height: isc.OBSelectorWidget.styling.fieldPickerIconHeight,
        hspace: isc.OBSelectorWidget.styling.fieldPickerIconHSpace,
        // note
        // unsupported
        // feature:
        // http://forums.smartclient.com/showthread.php?p=34868
        keyPress: function (keyName, character, form, item, icon) {
          if (keyName === 'Enter' && isc.EventHandler.ctrlKeyDown() && !isc.EventHandler.altKeyDown() && !isc.EventHandler.shiftKeyDown()) {
            this.selector.openSelectorWindow(form, item, icon);
            return false;
          }
          return true;
        },
        click: function (form, item, icon) {
          this.selector.selectorField.focus();
          this.selector.openSelectorWindow(form, item, icon);
        }
      }],
      openSelectorWindow: function () {
        this.selector.selectorField.focus();
        this.selector.openSelectorWindow(this.form, this, null);
      },
      width: this.width,
      editorType: 'comboBox',
      displayField: this.displayField,
      focus: function (form, item) {
        var currentWindowElementType = 'custom'; // To
        if (typeof setWindowElementFocus === 'function') {
          // To sync with Openbravo focus logic
          setWindowElementFocus(document.getElementById(this.selector.openbravoField.id), 'obj');
        }
        if (typeof disableDefaultAction === 'function') {
          // To prevent form default action when ENTER is pressed
          disableDefaultAction();
        }
      },

      keyPress: function (item, form, keyName, characterValue) {
        if (keyName === 'Enter' && isc.EventHandler.ctrlKeyDown() && !isc.EventHandler.altKeyDown() && !isc.EventHandler.shiftKeyDown()) {
          this.selector.openSelectorWindow(form, item, null);
          return false;
        }
        return true;
      },
      valueField: this.valueField,
      optionDataSource: this.dataSource,
      pickListWidth: this.width,
      pickListProperties: {
        fetchDelay: 400,
        showHeaderContextMenu: false,
        hide: function () {
          this.Super('hide', arguments);
          this.formItem.selector.pickListShowing = false;
          this.formItem.selector.setSelectorValueFromField(
          this.formItem.form, this.formItem, this.formItem.value);
        },
        show: function () {
          this.Super('show', arguments);
          this.formItem.selector.pickListShowing = true;
        }
      },
      pickListFields: this.pickListFields,
      blur: function (form, item) {
        this.Super('blur', [form, item]);
        // show the openbravo error message
        this.selector.checkDefaultValidations(form, item);
      },
      getPickListFilterCriteria: function () {
        var defValue, i, criteria = {
          operator: 'or',
          _constructor: 'AdvancedCriteria',
          criteria: []
        },
            crit = this.Super('getPickListFilterCriteria');

        criteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());

        // also adds the special ORG parameter
        OB.Utilities.addFormInputsToCriteria(criteria);

        // adds the selector id to filter used to get filter information
        criteria._selectorDefinitionId = this.selector.selectorDefinitionId;

        // only filter if the display field is also passed
        // the displayField filter is not passed when the user clicks the drop-down button
        var displayFieldValue = null;
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
          for (i = 0; i < this.selector.extraSearchFields.length; i++) {
            if (!criteria[this.selector.extraSearchFields[i]]) {
              criteria.criteria.push({
                fieldName: this.selector.extraSearchFields[i],
                operator: 'iContains',
                value: displayFieldValue
              });
            }
          }
          criteria.criteria.push({
            fieldName: this.displayField,
            operator: 'iContains',
            value: displayFieldValue
          });
        }

        // for the suggestion box it is one big or
        criteria[OB.Constants.OR_EXPRESSION] = 'true';

        // the additional where clause
        criteria[OB.Constants.WHERE_PARAMETER] = this.selector.whereClause;

        // and sort according to the display field
        // initially
        criteria[OB.Constants.SORTBY_PARAMETER] = this.selector.displayField;

        return criteria;
      },

      // when changed set the field
      changed: this.setSelectorValueFromField
    }];


    // Always call the superclass implementation when overriding
    // initWidget
    this.Super('initWidget', arguments);

    // store the combo so that it can be refered to directly
    this.selectorField = this.getFields()[0];

    // disable if in read only mode
    this.selectorField.setDisabled(this.disabled);

    // don't show the popup remove the form icon
    if (!this.showSelectorGrid) {
      this.selectorField.icons = [];
    }

    if (this.required) {
      this.selectorField.textBoxStyle = isc.OBSelectorWidget.styling.selectorFieldTextBoxStyleRequired;
    } else {
      this.selectorField.textBoxStyle = isc.OBSelectorWidget.styling.selectorFieldTextBoxStyle;
    }
    this.selectorField.setValue(this.openbravoField.value);
    this.initialValue = this.selectorField.getValue();

    this.openbravoField._selector = this;
    this.openbravoField.doReset = function () {
      if (this._selector.initialValue !== null) {
        this._selector.selectorField.setValue(this._selector.initialValue);
        this._selector.openbravoField.value = this._selector.initialValue;
        this._selector.checkDefaultValidations();
      }
    };

    this.openbravoField.focusLogic = function (param) {
      if (param === 'mustBeJumped') {
        return false;
      } else if (param === 'mustBeIgnored') {
        return this._selector.isDisabled();
      } else if (param === 'couldHaveFocus') {
        return true;
      } else if (param === 'focus') {
        this._selector.focus();
      } else if (param === 'blur') {
        var doNothing = 0; // prevent empty block
      } else {
        return true;
      }
    };

    this.openbravoField.setReadOnly = function (readOnly) {
      // note directly setting disabled on the
      // selectorfield did not work, has to be
      // done through a disable/enable function/call
      if (readOnly) {
        this._selector.disableSelector();
      } else {
        this._selector.enableSelector();
      }
    };

    this.rememberValues();

    this.setFilterEditorProperties(this.selectorGridFields);

    // register ourselves
    OB.TestRegistry.register(baseTestRegistryName + 'selector', this);
    OB.TestRegistry.register(baseTestRegistryName + 'selectorField', this.selectorField);

    // create the selector grid shown in the popup window
    if (this.showSelectorGrid) {

      OB.Utilities.applyDefaultValues(this.selectorGridFields, this.defaultSelectorGridField);

      this.selectorGrid = isc.ListGrid.create({

        selector: this,
        // pointer back to the
        // dynamic form
        dataProperties: {
          useClientFiltering: false,
          useClientSorting: false
        },

        width: isc.OBSelectorWidget.styling.listGridRelativeWidth,
        height: isc.OBSelectorWidget.styling.listGridRelativeHeight,
        alternateRecordStyles: true,
        dataSource: this.dataSource,
        showFilterEditor: true,
        sortField: this.displayField,

        filterData: function (criteria, callback, requestProperties) {
          requestProperties = requestProperties || {};
          requestProperties.params = requestProperties.params | {};
          requestProperties.params._selectorDefinitionId = this.selectorDefinitionId;
          if (!criteria) {
            criteria = {};
          }

          // also adds the special ORG parameter
          OB.Utilities.addFormInputsToCriteria(criteria);
          criteria[OB.Constants.WHERE_PARAMETER] = this.selector.whereClause;

          // set the default sort option
          criteria[OB.Constants.SORTBY_PARAMETER] = this.selector.displayField;

          criteria[OB.Constants.TEXT_MATCH_PARAMETER_OVERRIDE] = this.selector.popupTextMatchStyle;

          criteria._selectorDefinitionId = this.selectorDefinitionId;
          criteria._requestType = 'Window';

          // and call the super
          return this.Super('filterData', [criteria, callback, requestProperties]);
        },

        fetchData: function (criteria, callback, requestProperties) {

          if (!criteria) {
            criteria = {};
          }

          // also adds the special ORG parameter
          OB.Utilities.addFormInputsToCriteria(criteria);
          criteria[OB.Constants.WHERE_PARAMETER] = this.selector.whereClause;

          // set the default sort option
          criteria[OB.Constants.SORTBY_PARAMETER] = this.selector.displayField;
          criteria[OB.Constants.TEXT_MATCH_PARAMETER_OVERRIDE] = this.selector.popupTextMatchStyle;

          criteria._selectorDefinitionId = this.selector.selectorDefinitionId;
          criteria._requestType = 'Window';

          requestProperties = requestProperties || {};
          requestProperties.params = requestProperties.params | {};
          requestProperties.params._selectorDefinitionId = this.selectorDefinitionId;

          // and call the super
          return this.Super('fetchData', [criteria, callback, requestProperties]);
        },

        dataArrived: function () {

          this.Super('dataArrived', arguments);

          // check if a record has been selected, if
          // not take the one
          // from the selectorField
          // by doing this when data arrives the selection
          // will show up
          // when the record shows in view
          if (!this.getSelectedRecord()) {
            if (this.selector.openbravoField.value !== '') {
              this.selectSingleRecord(this.data.find(this.selector, this.selector.openbravoField.value));
            } else {
              this.selectSingleRecord(null);
            }
          }
        },


        filterEditorProperties: {
          actionButtonProperties: {
            visibility: 'hidden'
          }
        },

        fields: this.selectorGridFields,
        recordDoubleClick: this.setSelectorValueFromGrid
      });

      OB.TestRegistry.register(baseTestRegistryName + 'selectorGrid', this.selectorGrid);

      var okButton = isc.IButton.create({
        selector: this,
        title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
        endRow: false,
        startRow: false,
        align: isc.OBSelectorWidget.styling.modalPopupOkButtonAlign,
        width: isc.OBSelectorWidget.styling.modalPopupOkButtonWidth,
        icon: isc.OBSelectorWidget.styling.modalPopupOkButtonSrc,
        click: this.setSelectorValueFromGrid
      });
      var cancelButton = isc.IButton.create({
        selector: this,
        title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
        endRow: false,
        startRow: false,
        align: isc.OBSelectorWidget.styling.modalPopupCancelButtonAlign,
        width: isc.OBSelectorWidget.styling.modalPopupCancelButtonWidth,
        icon: isc.OBSelectorWidget.styling.modalPopupCancelButtonSrc,
        click: function () {
          this.selector.selectorWindow.hide();
        }
      });

      // create the popup window it self
      this.selectorWindow = isc.Window.create({
        title: this.title,
        selector: this,
        autoSize: false,
        width: isc.OBSelectorWidget.styling.modalPopupRelativeWidth,
        height: isc.OBSelectorWidget.styling.modalPopupRelativeHeight,
        align: isc.OBSelectorWidget.styling.modalPopupAlign,
        autoCenter: true,
        isModal: true,
        showModalMask: true,
        canDragReposition: true,
        canDragResize: true,
        dismissOnEscape: true,
        animateMinimize: false,
        showMaximizeButton: true,
        headerControls: ['headerIcon', 'headerLabel', 'minimizeButton', 'maximizeButton', 'closeButton'],
        headerIconProperties: {
          width: isc.OBSelectorWidget.styling.modalPopupHeaderIconWidth,
          height: isc.OBSelectorWidget.styling.modalPopupHeaderIconHeight,
          src: isc.OBSelectorWidget.styling.modalPopupHeaderIconSrc
        },
        // the items are the selector grid and the
        // button bar below it
        hide: function () {
          this.Super('hide', arguments);
          setOBTabBehavior(true);
          this.selector.selectorField.focus();
        },
        items: [
        this.selectorGrid, isc.HLayout.create({
          styleName: isc.OBSelectorWidget.styling.modalPopupButtonGroupStyle,
          height: isc.OBSelectorWidget.styling.modalPopupButtonGroupHeight,
          defaultLayoutAlign: isc.OBSelectorWidget.styling.modalPopupButtonGroupAlign,
          members: [
          isc.LayoutSpacer.create({}), okButton, isc.LayoutSpacer.create({
            width: isc.OBSelectorWidget.styling.modalPopupButtonSeparatorWidth
          }), cancelButton, isc.LayoutSpacer.create({})]
        })]
      });

      OB.TestRegistry.register(baseTestRegistryName + 'selectorWindow', this.selectorWindow);
      OB.TestRegistry.register(
      baseTestRegistryName + 'selectorWindow.okButton', okButton);
      OB.TestRegistry.register(
      baseTestRegistryName + 'selectorWindow.cancelButton', cancelButton);
    }

    var thisSelector = this;
    this.afterDrawDoLoop = window.setInterval(function () {
      if (thisSelector.isDrawn() === true) {
        thisSelector.afterDrawDo();
        window.clearInterval(thisSelector.afterDrawDoLoop);
      }
    }, 10);
  }
});