/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
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

isc.ClassFactory.defineClass('OBViewForm', isc.DynamicForm);

// = OBViewForm =
// The OBViewForm is the Openbravo specific subclass of the Smartclient
// DynamicForm. The properties of the view form are stored in a separate object
// as they are re-used to create the editor in the grid. The properties are added to the viewform at the bottom
// of this file.
OB.ViewFormProperties = {

  // ** {{{ view }}} **
  // The view member contains the pointer to the composite canvas which
  // handles this form
  // and the grid and other related components.
  view: null,
  auxInputs: {},
  hiddenInputs: {},
  sessionAttributes: {},
  dynamicCols: [],
  width: '100%',
  height: '100%',

  showErrorIcons: false,
  showErrorStyle: true,
  autoComplete: true,
  redrawOnDisable: true,

  // ** {{ Layout Settings }} **
  numCols: 4,
  colWidths: ['24%', '24%', '24%', '24%'],

  titleOrientation: 'top',
  titleSuffix: '</b>',
  titlePrefix: '<b>',
  requiredTitleSuffix: ' *</b>',
  requiredRightTitlePrefix: '<b>* ',
  rightTitlePrefix: '<b>',
  rightTitleSuffix: '</b>',

  fieldsByInpColumnName: null,
  fieldsByColumnName: null,

  isNew: false,
  hasChanged: false,

  // is false for forms used in grid editing
  // true for the main form
  isViewForm: false,

  // Name to the first focused field defined in AD
  firstFocusedField: null,

  // Name of the fields shown in status bar
  statusBarFields: [],

  // is set in the OBNoteSectionItem.initWidget
  noteSection: null,

  // is set in the OBLinkedItemSectionItem.initWidget
  linkedItemSection: null,

  // is set in the OBAttachmentsSectionItem.initWidget
  attachmentsSection: null,

  // is set in the OBAuditSectionItem.init
  auditSection: null,

  selectOnFocus: true,

  initWidget: function () {
    this._preventFocusChanges = true;

    // add the obFormProperties to ourselves, the obFormProperties
    // are re-used for inline grid editing
    isc.addProperties(this, this.obFormProperties);

    this.Super('initWidget', arguments);

    delete this._preventFocusChanges;
  },

  getStatusBarFields: function () {
    var i, item, title, refColumnName, targetEntity, value, displayedValue, length = this.statusBarFields.length,
        sourceWindowId, statusBarFields;

    statusBarFields = [
      [],
      [],
      [],
      [],
      [],
      []
    ];

    for (i = 0; i < length; i++) {
      item = this.getItem(this.statusBarFields[i]);
      title = item.getTitle();
      sourceWindowId = this.view.standardWindow.windowId;
      refColumnName = item.refColumnName;
      targetEntity = item.targetEntity;
      if (item.canvas) {
        if (item.canvas.noTitle) {
          title = null;
        }
        statusBarFields[0].push(title);
        statusBarFields[1].push(item.canvas);
        statusBarFields[2].push(null);
        statusBarFields[3].push(null);
        statusBarFields[4].push(null);
        statusBarFields[5].push(null);
        continue;
      }
      value = item.getValue();
      displayedValue = item.getValue();
      if (displayedValue !== null && displayedValue !== '') {

        if (item.getDisplayValue()) {
          displayedValue = item.getDisplayValue();
        }

        if (displayedValue === title && typeof item.getValue() === 'boolean') { // Checkbox items return the title as display value
          if (item.getValue()) {
            displayedValue = OB.I18N.getLabel('OBUIAPP_Yes');
          } else {
            displayedValue = OB.I18N.getLabel('OBUIAPP_No');
          }
        }

        // if there is a display field or an identifier field accompanying the
        // status bar field and it has a value then always use that
        // one
        if (item.displayField && this.getValue(item.displayField)) {
          displayedValue = this.getValue(item.displayField);
        } else if (this.getValue(item.name + '._identifier')) {
          displayedValue = this.getValue(item.name + '._identifier');
        }

        statusBarFields[0].push(title);
        statusBarFields[1].push(displayedValue);
        statusBarFields[2].push(sourceWindowId);
        statusBarFields[3].push(refColumnName);
        statusBarFields[4].push(targetEntity);
        statusBarFields[5].push(value);
      }
    }
    return statusBarFields;
  },

  setHasChanged: function (value) {
    this.hasChanged = value;
    this.view.updateTabTitle();
    if (value && !this.isNew && this.view.statusBar.mode !== 'EDIT') {
      this.view.statusBar.mode = "EDIT";
      this.view.statusBar.setContentLabel(this.view.statusBar.editIcon, 'OBUIAPP_Editing', this.getStatusBarFields());
    }

    if (value) {
      // signal that autosave is needed after this
      this.view.standardWindow.setDirtyEditForm(this);
      this.validateAfterFicReturn = true;
    } else {
      // signal that no autosave is needed after this
      this.view.standardWindow.setDirtyEditForm(null);
    }
  },

  editRecord: function (record, preventFocus, hasChanges, focusFieldName) {
    this.clearValues();

    var ret = this.Super('editRecord', arguments);

    // used when clicking on a cell in a grid
    if (!preventFocus && focusFieldName) {
      this.forceFocusedField = focusFieldName;
    }

    this.doEditRecordActions(preventFocus, record._new);

    if (hasChanges) {
      this.setHasChanged(true);
    }

    this.view.setTargetRecordInWindow(record.id);

    return ret;
  },

  doEditRecordActions: function (preventFocus, isNew) {
    delete this.contextInfo;

    this.initializing = true;

    delete this.validateAfterFicReturn;

    // will be recomputed after the initial values
    // FIC call, prevents firefox from incorrectly
    // showing focused style in multiple fields
    if (this.getFocusItem()) {
      this.getFocusItem().hasFocus = false;
      this.getFocusItem().elementBlur();
    }
    this.setFocusItem(null);

    // sometimes if an error occured we stay disabled
    // prevent this
    this.setDisabled(false);

    this.setHasChanged(false);

    this.setNewState(isNew);

    // errors are cleared anyway
    delete this.validateAfterFicReturn;

    // focus is done automatically, prevent the focus event if needed
    // the focus event will set the active view
    if (!isNew) {
      // If editing a document set to recent documents
      this.view.setRecentDocument(this.getValues());
    }

    this.ignoreFirstFocusEvent = preventFocus;

    // retrieveinitialvalues does focus and clear of errors
    this.retrieveInitialValues(isNew);

    if (isNew) {
      this.view.statusBar.mode = 'NEW';
      this.view.statusBar.setContentLabel(this.view.statusBar.newIcon, 'OBUIAPP_New');
    }
  },

  editNewRecord: function (preventFocus) {
    this.clearValues();
    var ret = this.Super('editNewRecord', arguments);
    this.doEditRecordActions(preventFocus, true);
    return ret;
  },

  // set parent display info in the record
  setParentDisplayInfo: function () {
    if (this.view.parentProperty) {
      var parentRecord = this.view.getParentRecord();
      if (parentRecord) {
        this.setValue(this.view.parentProperty, parentRecord.id);
        this.setValue(this.view.parentProperty + '.' + OB.Constants.IDENTIFIER, parentRecord[OB.Constants.IDENTIFIER]);
        if (this.getField(this.view.parentProperty) && !this.getField(this.view.parentProperty).valueMap) {
          var valueMap = {};
          this.getField(this.view.parentProperty).valueMap = valueMap;
          valueMap[parentRecord.id] = parentRecord[OB.Constants.IDENTIFIER];
        }
      }
    }
  },

  enableNoteSection: function (enable) {
    if (!this.noteSection) {
      return;
    }
    if (enable) {
      this.noteSection.setRecordInfo(this.view.entity, this.getValue(OB.Constants.ID));
      this.noteSection.collapseSection(true);
      delete this.noteSection.hiddenInForm;
      this.noteSection.refresh();
      this.noteSection.show();
    } else {
      this.noteSection.hiddenInForm = true;
      this.noteSection.hide();
    }
  },

  enableAuditSection: function (enable) {
    var auditSection;
    auditSection = this.auditSection;
    if (!auditSection) {
      return;
    }
    if (enable) {
      delete auditSection.hiddenInForm;
      auditSection.show();
    } else {
      auditSection.collapseSection(false);
      auditSection.hiddenInForm = true;
      auditSection.hide();
    }
  },

  enableLinkedItemSection: function (enable) {
    if (!this.linkedItemSection) {
      return;
    }
    if (enable) {
      this.linkedItemSection.collapseSection(true);
      this.linkedItemSection.setRecordInfo(this.view.entity, this.getValue(OB.Constants.ID));
      delete this.linkedItemSection.hiddenInForm;
      this.linkedItemSection.show();
    } else {
      this.linkedItemSection.hiddenInForm = true;
      this.linkedItemSection.hide();
    }
  },

  enableAttachmentsSection: function (enable) {
    if (!this.attachmentsSection) {
      return;
    }
    if (enable) {
      this.attachmentsSection.collapseSection(true);
      this.attachmentsSection.setRecordInfo(this.view.entity, this.getValue(OB.Constants.ID), this.view.tabId);
      delete this.attachmentsSection.hiddenInForm;
      this.attachmentsSection.show();
    } else {
      this.attachmentsSection.hiddenInForm = true;
      this.attachmentsSection.hide();
    }
  },

  // add the undo buttons to the clickmask so that no save happens when 
  // clicking undo
  showClickMask: function (clickAction, mode, unmaskedTargets) {
    if (!isc.isA.Array(unmaskedTargets)) {
      if (!unmaskedTargets) {
        unmaskedTargets = [];
      } else {
        unmaskedTargets = [unmaskedTargets];
      }
    }
    // the main undo button
    unmaskedTargets.push(this.view.toolBar.getLeftMember('undo'));

    // the row cancel button
    var editRow = this.view.viewGrid.getEditRow();
    if (editRow || editRow === 0) {
      var record = this.view.viewGrid.getRecord(editRow);
      if (record && record.editColumnLayout) {
        unmaskedTargets.push(record.editColumnLayout.cancelButton);
      }
    }
    this.Super('showClickMask', [clickAction, mode, unmaskedTargets]);
  },

  setNewState: function (isNew) {
    // showing the sections will change the focus item
    // restore that
    this.isNew = isNew;
    this.view.statusBar.setNewState(isNew);
    this.view.updateTabTitle();

    this.enableNoteSection(!isNew);
    this.enableLinkedItemSection(!isNew);
    this.enableAttachmentsSection(!isNew);
    this.enableAuditSection(!isNew);

    if (isNew) {
      this.view.statusBar.newIcon.prompt = OB.I18N.getLabel('OBUIAPP_NewIconPrompt');
    } else {
      this.view.statusBar.editIcon.prompt = OB.I18N.getLabel('OBUIAPP_EditIconPrompt');
    }

    // see issue:
    // 16064: Autosave error is triggered when closing a tab, even if the form wasn't touched
    // https://issues.openbravo.com/view.php?id=16064
    // this is inline with current behavior
    // NOTE: changed to reset the edit form when closing the form, so only there
    // so autosave always works except when closing if nothing has changed
    if (isNew) {
      // signal that autosave is needed after this
      this.view.standardWindow.setDirtyEditForm(this);
    }
  },

  computeFocusItem: function (startItem) {
    var items = this.getItems(),
        nextItem, itemsLength = items.length,
        item, i;

    var errorFld = this.getFirstErrorItem();
    if (!startItem && errorFld && errorFld.isFocusable(true)) {
      // get rid of this one, to not set the focus back to this field
      delete this.forceFocusedField;

      this.setFocusItem(errorFld);
      return;
    }

    if (!startItem && this.forceFocusedField) {
      item = this.getItem(this.forceFocusedField);
      delete this.forceFocusedField;
      if (item && item.isFocusable(true)) {
        this.setFocusItem(item);
        return;
      }
    } else {
      delete this.forceFocusedField;
    }

    if (!startItem && this.firstFocusedField) {
      item = this.getItem(this.firstFocusedField);
      if (item && item.isFocusable(true)) {
        this.setFocusItem(item);
        if (this.parentElement) {
          this.parentElement.delayCall('scrollTo', [null, this.getTop()], 100);
        }
        return;
      }
    }

    if (items) {
      if (startItem) {
        for (i = 0; i < itemsLength; i++) {
          item = items[i];
          if (!nextItem && item === startItem) {
            nextItem = true;
          } else if (nextItem && !isc.isA.SectionItem(item) && item && item.isFocusable(true)) {
            this.setFocusItem(item);
            return;
          }
        }
      }

      // not found retry the item we have
      if (startItem && startItem.isFocusable(true)) {
        this.setFocusItem(startItem);
      } else {
        // not found start from new again
        for (i = 0; i < itemsLength; i++) {
          item = items[i];
          if (item.isFocusable(true)) {
            this.setFocusItem(item);
            return;
          }
        }
      }
    }
  },

  // sets the focus in the current focusitem 
  // if it is not focusable then a next item is 
  // searched for
  setFocusInForm: function () {
    if (!this.view || !this.view.isActiveView()) {
      return;
    }

    var focusItem = this.getFocusItem();
    // an edit form in a grid is not
    // drawn it seems...
    if ((!this.grid && !this.isDrawn()) && !this.isVisible()) {
      // autofocus will do it for us
      return;
    }
    if (focusItem && focusItem.isFocusable()) {
      focusItem.focusInItem();
      this.view.lastFocusedItem = focusItem;
      this.selectFocusItemValue(true);
    } else {
      // find a new one
      this.computeFocusItem(focusItem);
      if (this.getFocusItem() !== focusItem && this.getFocusItem()) {
        focusItem.focusInItem();
        this.selectFocusItemValue(true);
        this.view.lastFocusedItem = focusItem;
      }
    }
  },

  selectFocusItemValue: function (delayCall) {
    if (!this.getFocusItem() || !this.view.isActiveView()) {
      return;
    }
    // if not explicitly set to false, select its value
    // or if do initial select on focus
    if (this.getFocusItem().selectOnFocus !== false || this.getFocusItem().doInitialSelectOnFocus) {
      if (delayCall || isc.Browser.isIE) {
        this.getFocusItem().delayCall('selectValue', [], 100);
      } else {
        this.getFocusItem().selectValue();
      }
    }
  },

  getFieldFromInpColumnName: function (inpColumnName) {
    var i, length;

    if (!this.fieldsByInpColumnName) {
      var localResult = [],
          fields = this.getFields();
      length = fields.length;
      for (i = 0; i < length; i++) {
        if (fields[i].inpColumnName) {
          localResult[fields[i].inpColumnName.toLowerCase()] = fields[i];
        }
      }
      this.fieldsByInpColumnName = localResult;
    }
    return this.fieldsByInpColumnName[inpColumnName.toLowerCase()];
  },

  getFieldFromColumnName: function (columnName) {
    var i, length;
    if (!this.fieldsByColumnName) {
      var localResult = [],
          fields = this.getFields();

      length = fields.length;

      for (i = 0; i < fields.length; i++) {
        if (fields[i].columnName) {
          localResult[fields[i].columnName.toLowerCase()] = fields[i];
        }
      }
      this.fieldsByColumnName = localResult;
    }
    return this.fieldsByColumnName[columnName.toLowerCase()];
  },

  setFields: function () {
    var i, item, length;

    // is used in various places, prevent focus and scroll events
    this._preventFocusChanges = true;
    this.Super('setFields', arguments);
    delete this._preventFocusChanges;
    this.fieldsByInpColumnName = null;
    this.fieldsByColumnName = null;

    length = this.getItems().length;
    for (i = 0; i < length; i++) {
      item = this.getItem(i);
      if (item && item.setSectionItemInContent) {
        item.setSectionItemInContent(this);
      }
    }

  },

  retrieveInitialValues: function (isNew) {
    var parentId = this.view.getParentId(),
        i, fldNames = [],
        requestParams, allProperties, parentColumn, me = this,
        mode, length = this.getFields().length;

    this.setParentDisplayInfo();

    // note also in this case initial values are passed in as in case of grid
    // editing the unsaved/error values from a previous edit session are maintained
    allProperties = this.view.getContextInfo(false, true, false, true);

    if (isNew) {
      mode = 'NEW';
    } else {
      mode = 'EDIT';
    }

    requestParams = {
      MODE: mode,
      PARENT_ID: parentId,
      TAB_ID: this.view.tabId,
      ROW_ID: this.getValue(OB.Constants.ID)
    };

    if (parentId && isNew && this.view.parentProperty) {
      parentColumn = this.view.getPropertyDefinition(this.view.parentProperty).inpColumn;
      requestParams[parentColumn] = parentId;
    }

    allProperties._entityName = this.view.entity;

    // only put the visible field names in the call
    for (i = 0; i < length; i++) {
      if (this.getFields()[i].inpColumnName) {
        fldNames.push(this.getFields()[i].inpColumnName);
      }
    }
    allProperties._visibleProperties = fldNames;

    this.setDisabled(true);

    // note that only the fields with errors are validated anyway
    this.validateAfterFicReturn = true;

    // get the editRow before doing the call
    var editRow = me.view.viewGrid.getEditRow();

    this.inFicCall = true;

    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, function (response, data, request) {
      var editValues;
      if (editRow || editRow === 0) {
        editValues = me.view.viewGrid.getEditValues(editRow);
      }

      // no focus item found, focus on the body of the grid
      // this makes sure that keypresses end up in the 
      // bodyKeyPress method
      if (!me.getFocusItem() || !me.getFocusItem().isFocusable()) {
        me.view.viewGrid.body.focus();
      }

      me.processFICReturn(response, data, request, editValues, editRow);

      if (!this.grid || this.grid.getEditRow() !== editRow) {
        // remember the initial values, if we are still editing the same row
        me.rememberValues();
      }
      me.initializing = false;

      // do here because during initial form drawing
      // fields get blurred and will show an error
      me.clearErrors(true);

      // only compute a new focus item if the form is active
      if (me.view.isActiveView()) {
        me.computeFocusItem();
      }

      // if the focus item is not really enabled
      // then find a new one, even if the form is not active
      if (me.getFocusItem() && !me.getFocusItem().isFocusable(true)) {
        me.computeFocusItem(me.getFocusItem());
      }
      // note the focus is set in the field when the FIC call
      // returns
      // at this point select the focused value      
      if (me.getFocusItem()) {
        me.setFocusInForm();
      }
    });
  },

  rememberValues: function () {
    var i, flds = this.getFields(),
        length = flds.length;
    this.Super('rememberValues', arguments);

    // also remember the valuemaps
    for (i = 0; i < length; i++) {
      if (flds[i].valueMap) {
        //This ensures that the valueMap of the createdBy and updatedBy fields will be properly initialized when adding a new record in form view
        //See issue #19843
        if (isc.isA.emptyObject(flds[i].valueMap) && (flds[i].name === 'createdBy' || flds[i].name === 'updatedBy') && flds[i].form.getValue(flds[i].displayField)) {
          flds[i].valueMap = {};
          flds[i].valueMap[flds[i].form.getValue(flds[i].name)] = flds[i].form.getValue(flds[i].displayField);
          flds[i]._rememberedValueMap = flds[i].valueMap;
        } else {
          flds[i]._rememberedValueMap = flds[i].valueMap;
        }
      }
    }
  },

  // used in grid editing, when an edit is discarded then the canFocus needs to be
  // reset
  resetCanFocus: function () {
    var i, length = this.getItems().length;
    for (i = 0; i < length; i++) {
      delete this.getItems()[i].canFocus;
    }
  },

  processFICReturn: function (response, data, request, editValues, editRow) {
    var length, modeIsNew = request.params.MODE === 'NEW',
        noErrors, errorSolved;

    delete this.contextInfo;

    // needs to be recomputed as for grid editing the fields
    // are reset for every edit session
    this.fieldsByColumnName = null;

    // TODO: an error occured, handles this much better...
    if (!data || !data.columnValues) {
      this.setDisabled(false);
      this.validate();
      delete this.inFicCall;
      return;
    }

    if (data._readOnly || this.view.readOnly) {
      this.readOnly = true;
    } else {
      this.readOnly = false;
    }

    var columnValues = data.columnValues,
        calloutMessages = data.calloutMessages,
        auxInputs = data.auxiliaryInputValues,
        prop, value, i, j, dynamicCols = data.dynamicCols,
        sessionAttributes = data.sessionAttributes,
        item, section, retHiddenInputs = data.hiddenInputs;

    // edit row has changed when returning, don't update the form anymore
    if (this.grid && this.grid.getEditRow() !== editRow) {
      if (columnValues) {
        for (prop in columnValues) {
          if (columnValues.hasOwnProperty(prop)) {
            this.setColumnValuesInEditValues(prop, columnValues[prop], editValues);
          }
        }
      }
      if (editValues && editValues.actionAfterFicReturn) {
        OB.Utilities.callAction(editValues.actionAfterFicReturn);
        delete editValues.actionAfterFicReturn;
      }
      return;
    }

    if (columnValues) {
      for (prop in columnValues) {
        if (columnValues.hasOwnProperty(prop)) {
          this.processColumnValue(prop, columnValues[prop], editValues);
        }
      }
    }

    if (modeIsNew || request.params.MODE === 'EDIT') {
      //If a new record is created, or an existing one is opened,
      //the existing hiddenInputs (which correspond to a different record) should be deleted
      this.hiddenInputs = {};
    } else if (retHiddenInputs) {
      for (prop in retHiddenInputs) {
        if (retHiddenInputs.hasOwnProperty(prop)) {
          this.hiddenInputs[prop] = retHiddenInputs[prop];
        }
      }
    }

    if (this.attachmentsSection) {
      this.attachmentsSection.fillAttachments(data.attachments);
    }

    // We will show the note count if it has been calculated and is different from 0
    if (this.noteSection) {
      if (data.noteCount) {
        this.noteSection.setNoteCount(data.noteCount);
      } else if (request.params.MODE === 'EDIT') {
        this.noteSection.setNoteCount(0);
      }
    }

    // apparently sometimes an empty string is returned
    if (calloutMessages && calloutMessages.length > 0 && calloutMessages[calloutMessages.length - 1].text !== '') {
      // TODO: check as what type should call out messages be displayed
      this.view.messageBar.setMessage(isc.OBMessageBar[calloutMessages[calloutMessages.length - 1].severity], null, calloutMessages[calloutMessages.length - 1].text);
    }
    if (auxInputs) {
      for (prop in auxInputs) {
        if (auxInputs.hasOwnProperty(prop)) {
          value = typeof auxInputs[prop].value !== 'undefined' ? auxInputs[prop].value : '';
          this.setValue(prop, value);
          this.auxInputs[prop] = value;
        }
      }
    }

    if (sessionAttributes) {
      this.sessionAttributes = sessionAttributes;
    }

    if (dynamicCols) {
      this.dynamicCols = dynamicCols;
    }

    // grid editing    
    if (this.grid && this.grid.setEditValues && this.grid.getEditRow() === editRow) {
      // keep it as it is overwritten by the setEditValues
      var tmpActionAfterFic = null;
      if (editValues && editValues.actionAfterFicReturn) {
        tmpActionAfterFic = editValues.actionAfterFicReturn;
      }
      this.grid.setEditValues(this.grid.getEditRow(), this.getValues(), true);
      this.grid.storeUpdatedEditorValue(true);
      if (editValues) {
        editValues.actionAfterFicReturn = tmpActionAfterFic;
      }
    }

    // note onFieldChanged uses the form.readOnly set above
    this.onFieldChanged(this);

    // on field changed may have made the focused item non-editable
    // this is handled in setdisabled restore focus item's call
    this.setDisabled(false);

    length = this.getFields().length;

    if (this.validateAfterFicReturn) {
      delete this.validateAfterFicReturn;
      // only validate the fields which have errors or which have changed
      noErrors = true;

      for (i = 0; i < length; i++) {
        if (this.getFields()[i]._changedByFic || this.hasFieldErrors(this.getFields()[i].name)) {
          errorSolved = this.getFields()[i].validate();
          noErrors = noErrors && errorSolved;
          if (errorSolved && this.grid) {
            this.grid.clearFieldError(this.grid.getEditRow(), this.getFields()[i].name);
          }
        }
      }
      if (this.grid && noErrors) {
        this.grid.clearRowErrors(this.grid.getEditRow());
        this.grid.refreshRow(this.grid.getEditRow());
      }
    }

    for (i = 0; i < length; i++) {
      delete this.getFields()[i]._changedByFic;
    }

    // refresh WidgetInForm fields if present (as they might depend on data of current record) 
    for (i = 0; i < length; i++) {
      var locField = this.getFields()[i];
      if (locField.hasOwnProperty("widgetClassId")) {
        locField.refresh();
      }
    }

    this.redraw();

    delete this.inFicCall;
    this.view.toolBar.updateButtonState(true);
    if (request.params.MODE === 'EDIT') {
      this.view.statusBar.mode = 'VIEW';
      this.view.statusBar.setContentLabel(null, null, this.getStatusBarFields());
    }

    if (this.callSaveAfterFICReturn) {
      delete this.callSaveAfterFICReturn;
      this.saveRow(true);
    }
    if (this.expandAttachments) {
      this.getItem('_attachments_').expandSection();
      delete this.expandAttachments;
    }
    if (editValues && editValues.actionAfterFicReturn) {
      OB.Utilities.callAction(editValues.actionAfterFicReturn);
      delete editValues.actionAfterFicReturn;
    }

    if (data.jscode) {
      length = data.jscode.length;
      for (i = 0; i < length; i++) {
        eval(data.jscode[i]);
      }
    }
  },

  setDisabled: function (state) {
    var previousAllItemsDisabled = this.allItemsDisabled || false,
        i, length;
    this.allItemsDisabled = state;

    if (previousAllItemsDisabled !== this.allItemsDisabled) {
      if (this.getFocusItem()) {
        if (this.allItemsDisabled) {
          if (!this.initializing) {
            this.getFocusItem().hasFocus = false;
            this.getFocusItem().elementBlur();
          }
          this.setHandleDisabled(state);
          this.view.viewGrid.refreshEditRow();
        } else {
          this.setHandleDisabled(state);
          this.view.viewGrid.refreshEditRow();
          // reset the canfocus
          length = this.getFields().length;
          for (i = 0; i < length; i++) {
            delete this.getFields()[i].canFocus;
          }
          delete this.ignoreFirstFocusEvent;
        }
      } else {
        this.view.viewGrid.refreshEditRow();
      }
    }
  },

  refresh: function () {
    var criteria = {
      id: this.getValue(OB.Constants.ID)
    };
    this.fetchData(criteria);
  },

  processColumnValue: function (columnName, columnValue, editValues) {
    // Modifications in this method should go also in setColumnValuesInEditValues because both almost do the same
    var typeInstance;
    var assignValue;
    var assignClassicValue;
    var isDate, i, valueMap = {},
        oldValue, field = this.getFieldFromColumnName(columnName),
        entries = columnValue.entries;
    // not a field on the form, probably a datasource field
    var propDef = this.view.getPropertyDefinitionFromDbColumnName(columnName);
    var prop = propDef ? propDef.property : null;
    var id, identifier;
    if (!field) {
      if (!propDef) {
        return;
      }
      field = this.getDataSource().getField(prop);
      if (!field) {
        field = {
          name: propDef.property,
          type: propDef.type
        };
      }
    }

    // ignore the id
    if (prop === OB.Constants.ID) {
      return;
    }

    // note field can be a datasource field, see above, in that case
    // don't set the entries    
    if (field.form && entries && field.setEntries) {
      field.setEntries(entries);
    }

    if (editValues && field.valueMap) {
      // store the valuemap in the edit values so it can be retrieved later
      // when the form is rebuild
      editValues[prop + '._valueMap'] = field.valueMap;
    }

    // Adjust to formatting if exists value and classicValue. 
    oldValue = this.getValue(field.name);
    if (field.typeInstance && field.typeInstance.parseInput && field.typeInstance.editFormatter) {
      assignValue = field.typeInstance.parseInput(field.typeInstance.editFormatter(columnValue.value));
      assignClassicValue = field.typeInstance.editFormatter(field.typeInstance.parseInput(columnValue.classicValue));
    } else {
      assignValue = columnValue.value;
      assignClassicValue = columnValue.classicValue;
    }

    if (columnValue.value && (columnValue.value === 'null' || columnValue.value === '')) {
      // handle the case that the FIC returns a null value as a string
      // should be repaired in the FIC
      // note: do not use clearvalue as this removes the value from the form
      this.setValue(field.name, null);
    } else if (columnValue.value || columnValue.value === 0 || columnValue.value === false) {
      isDate = field.type && (isc.SimpleType.getType(field.type).inheritsFrom === 'date' || isc.SimpleType.getType(field.type).inheritsFrom === 'datetime' || isc.SimpleType.getType(field.type).inheritsFrom === 'time');
      if (isDate) {
        this.setItemValue(field.name, isc.Date.parseSchemaDate(columnValue.value));
      } else if (columnValue.hasDateDefault) {
        this.setItemValue(field.name, columnValue.classicValue);
      } else {

        // set the identifier/display field if the identifier is passed also
        // note that when the field value is changed by the user the setting 
        // of the identifier field is done in the form item
        identifier = columnValue.identifier;
        if (!identifier && field.valueMap) {
          identifier = field.valueMap[columnValue.value];
        }
        if (identifier) {
          if (field.setEntry) {
            field.setEntry(columnValue.value, identifier);
          } else {
            if (!field.valueMap) {
              field.valueMap = {};
            }
            field.valueMap[columnValue.value] = identifier;
          }
          if (field.form) {
            // only set the display field name if the field does not have its own
            // datasource and the field displayfield contains a dot, otherwise 
            // it is a direct field
            if (field.displayField && field.displayField.contains('.') && !this.getField(field.displayField) && !field.optionDataSource && !field.getDataSource()) {
              field.form.setItemValue(field.displayField, identifier);
            } else if (!field.displayField) {
              field.form.setItemValue(field.name + '.' + OB.Constants.IDENTIFIER, identifier);
            }
          }
        }

        this.setItemValue(field.name, assignValue);
      }
    } else {
      // note: do not use clearvalue as this removes the value from the form
      // which results it to not be sent to the server anymore
      this.setValue(field.name, null);
      if (this.getValue(field.name + '.' + OB.Constants.IDENTIFIER)) {
        this.setItemValue(field.name + '.' + OB.Constants.IDENTIFIER, null);
      }
    }

    if (field.compareValues && !field.compareValues(oldValue, this.getValue(field.name))) {
      field._changedByFic = true;
    }

    // store the textualvalue so that it is correctly send back to the server
    typeInstance = isc.SimpleType.getType(field.type);
    if (columnValue.classicValue && typeInstance.decSeparator) {
      this.setTextualValue(field.name, assignClassicValue, typeInstance);
    }
  },

  setColumnValuesInEditValues: function (columnName, columnValue, editValues) {
    // Modifications in this method should go also in processColumnValue because both almost do the same
    var assignClassicValue, typeInstance, length, isDate;

    // no editvalues even anymore, go away
    if (!editValues) {
      return;
    }

    var id, identifier, field = this.getFieldFromColumnName(columnName),
        i, valueMap = {},
        entries = columnValue.entries;
    var prop = this.view.getPropertyFromDBColumnName(columnName);

    // ignore the id
    if (prop === OB.Constants.ID) {
      return;
    }

    if (entries) {
      length = entries.length;
      for (i = 0; i < length; i++) {
        id = entries[i][OB.Constants.ID] || '';
        identifier = entries[i][OB.Constants.IDENTIFIER] || '';
        valueMap[id] = identifier;
      }
      editValues[prop + '._valueMap'] = valueMap;
    }

    if (columnValue.value && (columnValue.value === 'null' || columnValue.value === '')) {
      // handle the case that the FIC returns a null value as a string
      // should be repaired in the FIC
      // note: do not use clearvalue as this removes the value from the form
      editValues[prop] = null;
    } else if (columnValue.value || columnValue.value === 0 || columnValue.value === false) {
      isDate = field && field.type && (isc.SimpleType.getType(field.type).inheritsFrom === 'date' || isc.SimpleType.getType(field.type).inheritsFrom === 'datetime');
      if (isDate) {
        editValues[prop] = isc.Date.parseSchemaDate(columnValue.value);
      } else {

        // set the identifier/display field if the identifier is passed also
        // note that when the field value is changed by the user the setting 
        // of the identifier field is done in the form item
        identifier = columnValue.identifier;
        if (!identifier && valueMap) {
          identifier = valueMap[columnValue.value];
        }
        if (identifier) {
          editValues[prop + '.' + OB.Constants.IDENTIFIER] = identifier;
        }
        editValues[prop] = columnValue.value;
      }
    } else {
      // note: do not use clearvalue as this removes the value from the form
      // which results it to not be sent to the server anymore
      editValues[prop] = null;
    }

    // store the textualvalue so that it is correctly send back to the server
    if (field) {
      // Adjust to formatting if exists value and classicValue.
      assignClassicValue = (field.typeInstance && field.typeInstance.parseInput && field.typeInstance.editFormatter) ? field.typeInstance.editFormatter(field.typeInstance.parseInput(columnValue.classicValue)) : columnValue.classicValue;
      typeInstance = isc.SimpleType.getType(field.type);
      if (columnValue.classicValue && typeInstance.decSeparator) {
        this.setTextualValue(field.name, assignClassicValue, typeInstance, editValues);
      }
    }
  },

  // note textValue is in user format using users decimal and group separator
  setTextualValue: function (fldName, textValue, type, editValues) {
    if (!textValue || textValue.trim() === '') {
      textValue = '';
    } else {
      textValue = OB.Utilities.Number.OBMaskedToOBPlain(textValue, type.decSeparator, type.groupSeparator);
      textValue = textValue.replace(type.decSeparator, '.');
    }
    if (editValues) {
      editValues[fldName + '_textualValue'] = textValue;
    } else if (this.grid && this.grid.getEditForm()) {
      this.grid.getEditValues(this.grid.getEditRow())[fldName + '_textualValue'] = textValue;
    }
    this.setValue(fldName + '_textualValue', textValue);
  },

  // calls setValue and the onchange handling
  setItemValue: function (item, value) {
    var currentValue, view;

    if (isc.isA.String(item)) {

      // not an item, set and bail
      if (!this.getField(item)) {
        this.setValue(item, value);
        return;
      }
      item = this.getField(item);
    }
    currentValue = item.getValue();

    // no change go away
    if (item.compareValues(value, currentValue)) {
      // Force setElemntValue even there is no change to show new possible values
      // in field.valueMap (issue #18957)
      item.setElementValue(item.mapValueToDisplay(value));

      return;
    }

    if (this.grid) {
      this.grid.setEditValue(this.grid.getEditRow(), item.name, value);
    }

    this.setValue(item, value);

    // fire any new callouts
    if (this.view) {
      view = this.view;
    } else if (this.grid && this.grid.view) {
      view = this.grid.view;
    }

    if (view && OB.OnChangeRegistry.hasOnChange(view.tabId, item)) {
      OB.OnChangeRegistry.call(view.tabId, item, view, this, view.viewGrid);
    }
  },

  // called explicitly onblur and when non-editable fields change
  handleItemChange: function (item) {
    var i, length, tabId, view;

    // is used to prevent infinite loops during save
    delete this.saveFocusItemChanged;

    delete this.contextInfo;

    if (item._hasChanged) {
      this.itemChangeActions(item);

      this.onFieldChanged(item.form, item, item.getValue());

      if (this.view) {
        view = this.view;
      } else if (this.grid && this.grid.view) {
        view = this.grid.view;
      }

      if (view && OB.OnChangeRegistry.hasOnChange(view.tabId, item)) {
        OB.OnChangeRegistry.call(view.tabId, item, view, this, view.viewGrid);
      } else {
        // call the classic callout if there
        length = this.dynamicCols.length;
        for (i = 0; i < length; i++) {
          if (this.dynamicCols[i] === item.inpColumnName) {
            item._hasChanged = false;
            this.inFicCall = true;
            this.doChangeFICCall(item);
            return true;
          }
        }
      }

      if (this.getFocusItem() && !this.getFocusItem().isFocusable(true)) {
        this.computeFocusItem(this.getFocusItem());
        this.setFocusInForm();
      }
    }

    item._hasChanged = false;
  },

  setDisabledWhenStillInFIC: function () {
    if (this.inFicCall) {
      this.setDisabled(true);
    }
  },

  // note item can be null, is also called when the form is re-shown
  // to recompute combos
  // if preserveGridEditing is set to true, the isEditingGrid flag will be marked when the FIC processing ends
  // this is used in case the doChangeFICCall is triggered by an action which unwillingly ends the grid editing early
  doChangeFICCall: function (item, preserveGridEditing) {
    var parentId = null,
        me = this,
        requestParams, allProperties = this.view.getContextInfo(false, true, false, true);
    if (this.view.parentProperty) {
      parentId = this.view.getParentId();
    }
    this.setParentDisplayInfo();

    requestParams = {
      MODE: 'CHANGE',
      PARENT_ID: parentId,
      TAB_ID: this.view.tabId,
      ROW_ID: this.getValue(OB.Constants.ID)
    };
    if (item) {
      requestParams.CHANGED_COLUMN = item.inpColumnName;
    }
    allProperties._entityName = this.view.entity;

    // disable with a delay to allow the focus to be moved to a new field
    // before disabling
    // only do this if there is no popup currently
    if (!this.view.standardWindow.inAutoSaveConfirmation) {
      this.delayCall('setDisabledWhenStillInFIC', [true], 10);
    }

    var editRow = this.view.viewGrid.getEditRow();

    OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', allProperties, requestParams, function (response, data, request) {
      var editValues;
      if (editRow || editRow === 0) {
        editValues = me.view.viewGrid.getEditValues(editRow);
      }

      me.processFICReturn(response, data, request, editValues, editRow);

      // compute the focus item after the fic has been done
      // and fields have become visible
      if (me.doFocusInNextItemAfterFic) {
        me.focusInNextItem(me.doFocusInNextItemAfterFic);
        delete me.doFocusInNextItemAfterFic;
      } else if (!me.forceFocusedField) {
        // don't set the focus in this case, this happens
        // when moving to a new row in grid editing
        if (me.getFocusItem()) {
          me.setFocusInForm();
        }
      }
      if (preserveGridEditing) {
        me.view.isEditingGrid = true;
        this.view.toolBar.updateButtonState(true);
      }
    });
    this.view.toolBar.updateButtonState(true);
  },

  itemChanged: function (item, newValue) {
    this.itemChangeActions(item);
  },

  // these actions are done when the user types in a field
  // in contrast to other actions which are done at blur
  // see: handleItemChange
  itemChangeActions: function (item) {
    var i = 0;
    // special case, item change is called when the inline form is being hidden
    if (!this.view.isShowingForm && !this.view.isEditingGrid) {
      return;
    }

    // remove the message
    this.setHasChanged(true);
    this.view.messageBar.hide();
    this.view.toolBar.updateButtonState(true, true);
  },

  // make sure that any field errors also appear in the grid
  setFieldErrors: function (itemName, msg, display) {
    this.Super('setFieldErrors', arguments);
    if (this.grid && this.view.isEditingGrid) {
      this.grid.setFieldError(this.grid.getEditRow(), itemName, msg, !display);
    }
  },

  resetForm: function () {
    this.resetValues();
    this.clearErrors(true);
    this.setHasChanged(false);
  },

  undo: function () {
    var i, flds = this.getFields(),
        length = flds.length,
        doClose = !this.hasChanged;

    if (doClose) {
      this.doClose();
      return;
    }

    // also restore the valuemaps
    for (i = 0; i < length; i++) {
      if (flds[i]._rememberedValueMap) {
        flds[i].valueMap = flds[i]._rememberedValueMap;
      }
    }

    this.view.messageBar.hide();
    this.resetValues();
    this.setHasChanged(false);
    if (this.isNew) {
      this.setNewState(this.isNew);
    } else {
      this.view.statusBar.mode = 'VIEW';
      this.view.statusBar.setContentLabel(null, null, this.getStatusBarFields());
    }
    this.view.toolBar.updateButtonState(true);
  },

  doClose: function () {
    this.view.switchFormGridVisibility();
    this.view.messageBar.hide();
    if (this.isNew) {
      this.view.refreshChildViews();
    }
    this.view.standardWindow.setDirtyEditForm(null);
  },

  autoSave: function () {
    if (this.isViewForm) {
      this.saveRow();
    } else {
      // grid editing, forward to the grid
      this.view.viewGrid.autoSave();
    }
  },

  // always let the saveRow callback handle the error
  saveEditorReply: function (response, data, request) {
    return true;
  },

  // Note: saveRow is not called in case of grid editing
  // there the save call is done through the grid saveEditedValues
  // function
  saveRow: function () {
    var savingNewRecord = this.isNew,
        i, length, flds, form = this,
        ficCallDone, record, recordIndex, callback, viewsNotToRefresh;

    // store the value of the current focus item
    if (this.getFocusItem() && this.saveFocusItemChanged !== this.getFocusItem()) {
      this.getFocusItem().blur(this, this.getFocusItem());
      // prevent infinite loops
      this.saveFocusItemChanged = this.getFocusItem();
    } else {
      delete this.saveFocusItemChanged;
    }

    record = form.view.viewGrid.getSelectedRecord();

    // note record does not have to be set in case new and no
    // previously selected record
    recordIndex = (record ? form.view.viewGrid.getRecordIndex(record) : -1);

    form.isSaving = true;

    // remove the error message if any
    this.view.messageBar.hide();

    callback = function (resp, data, req) {
      var index1, index2, view = form.view,
          localRecord, status = resp.status,
          sessionProperties;

      // if no recordIndex then select explicitly
      if (recordIndex === -1) {
        var id = form.getValue('id');
        record = view.viewGrid.data.find('id', id);
        recordIndex = view.viewGrid.data.indexOf(record);
      }

      // not in the filter, insert the record in the cachedata so it will be made visible
      if (status === isc.RPCResponse.STATUS_SUCCESS && recordIndex === -1) {
        var visibleRows = view.viewGrid.body.getVisibleRows();
        if (visibleRows[0] !== -1) {
          view.viewGrid.data.insertCacheData(data, visibleRows[0]);
          recordIndex = visibleRows[0];
        }
      }

      if (recordIndex || recordIndex === 0) {
        // if this is not done the selection gets lost
        localRecord = view.viewGrid.data.get(recordIndex);
        if (localRecord) {
          localRecord[view.viewGrid.selection.selectionProperty] = true;
        }

        // a new id has been computed use that now    
        if (localRecord && localRecord._newId) {
          localRecord.id = localRecord._newId;
          delete localRecord._newId;
        }

        view.viewGrid.scrollToRow(recordIndex);
      }

      if (status === isc.RPCResponse.STATUS_SUCCESS) {
        // do remember values here to prevent infinite autosave loop
        form.rememberValues();

        //view.messageBar.setMessage(isc.OBMessageBar.TYPE_SUCCESS, null, OB.I18N.getLabel('OBUIAPP_SaveSuccess'));
        this.view.statusBar.mode = 'SAVED';
        view.statusBar.setContentLabel(view.statusBar.savedIcon, 'OBUIAPP_Saved', this.getStatusBarFields());

        view.setRecentDocument(this.getValues());

        if (localRecord && localRecord !== view.viewGrid.getSelectedRecord()) {
          localRecord[view.viewGrid.selection.selectionProperty] = false;
          view.viewGrid.doSelectSingleRecord(localRecord);
        }

        view.updateLastSelectedState();

        // remove any new pointer
        if (this.getValue('_new')) {
          this.clearValue('_new');
        }

        view.viewGrid.markForRedraw();

        if (form.isNew) {
          view.refreshChildViews();
        }

        // success invoke the action, if any there
        view.standardWindow.autoSaveDone(view, true);

        // stop here if the window was getting closed anyway
        if (view.standardWindow.closing) {
          return;
        }

        // do this after doing autoSave as the setHasChanged will clean
        // the autosave info
        form.setHasChanged(false);

        // remove any edit info in the grid
        view.viewGrid.discardEdits(recordIndex, null, false, isc.ListGrid.PROGRAMMATIC, true);

        // change some labels
        form.setNewState(false);

        view.refreshParentRecord();

        // Refreshes the selected record of the views that belong to the same entity
        // as the view being saved
        if (view.standardWindow) {
          viewsNotToRefresh = [];
          // there is no need to refresh the current view...
          viewsNotToRefresh[0] = view.tabId;
          if (view.parentView) {
            //  ... nor the parent view, if any (it would have been refreshed just a few lines ago)
            viewsNotToRefresh[1] = view.parentView.tabId;
          }
          view.standardWindow.refreshViewsWithEntity(this.view.entity, viewsNotToRefresh);
        }

        // We fill attachments in case the record is new, so that components
        // of the attachments section are created
        if (savingNewRecord) {
          this.attachmentsSection.fillAttachments(null);
          // We also do a call to the FIC on SETSESSION mode to set the session variables
          // to fix issue 18453
          sessionProperties = this.view.getContextInfo(true, true, false, true);
          OB.RemoteCallManager.call('org.openbravo.client.application.window.FormInitializationComponent', sessionProperties, {
            MODE: 'SETSESSION',
            TAB_ID: this.view.tabId,
            PARENT_ID: this.view.getParentId(),
            ROW_ID: this.values.id
          }, null);
        }

      } else if (status === isc.RPCResponse.STATUS_VALIDATION_ERROR && resp.errors) {
        form.handleFieldErrors(resp.errors);
        view.standardWindow.autoSaveDone(view, false);
      } else {
        view.setErrorMessageFromResponse(resp, data, req);
        view.standardWindow.autoSaveDone(view, false);
      }

      form.isSaving = false;
      view.toolBar.updateButtonState(true);
      return false;
    };


    if (this.inFicCall) {
      this.callSaveAfterFICReturn = true;
    } else {
      // note validate will also set the formfocus, this is 
      // done by calling showErrors without the third parameter to true
      if (!form.validateForm()) {
        return;
      }
      // last parameter true prevents additional validation
      this.saveData(callback, {
        willHandleError: true,
        formSave: true
      }, true);
    }
  },

  validateForm: function () {
    var form = this;
    if (!form.validate()) {
      form.handleFieldErrors(null);
      form.view.standardWindow.autoSaveDone(form.view, false);
      form.isSaving = false;
      form.view.toolBar.updateButtonState(true);
      return false;
    }
    return true;
  },

  // called when someone picks something from a picklist, the focus should go to the next
  // item
  focusInNextItem: function (currentItemName) {
    // if in the fic then let the fic call us again afterwards
    if (this.inFicCall) {
      this.doFocusInNextItemAfterFic = currentItemName;
      // solve issue https://issues.openbravo.com/view.php?id=19236
      // for firefox we need to recompute focus also during
      // a fic call, for other browsers we only need to do it
      // once, it really seems to be a firefox bug, as firefox
      // does not display the selection of text in an input
      // but in reality it is selected
      if (!isc.Browser.isFirefox) {
        return;
      }
    }

    // wait for the redraw to be finished before moving the focus 
    if (this.isDirty()) {
      this.delayCall('focusInNextItem', [currentItemName], 100);
      return;
    }
    this.computeFocusItem(this.getField(currentItemName));
    if (this.getFocusItem()) {
      this.getFocusItem().focusInItem();
      this.selectFocusItemValue();
    }
  },

  // overridden to prevent focus setting when autoSaving  
  showErrors: function (errors, hiddenErrors, suppressAutoFocus) {
    if (this.view.standardWindow.isAutoSaving) {
      return this.Super('showErrors', [errors, hiddenErrors, true]);
    }
    return this.Super('showErrors', arguments);
  },

  handleFieldErrors: function (errors) {
    var msg, additionalMsg = '',
        err, errorFld;

    if (this.view.isEditingGrid) {
      msg = OB.I18N.getLabel('OBUIAPP_ErrorInFieldsGrid', [this.view.ID]);
    } else {
      msg = OB.I18N.getLabel('OBUIAPP_ErrorInFields');
    }

    if (errors) {
      this.setErrors(errors, true);
      for (err in errors) {
        if (errors.hasOwnProperty(err)) {
          var fld = this.getField(err);
          if (!fld || !fld.visible) {
            if (additionalMsg !== '') {
              additionalMsg = additionalMsg + '<br/>';
            }
            additionalMsg = additionalMsg + errors[err];
          }
        }
      }
      if (additionalMsg) {
        msg = additionalMsg;
      }
    }
    errorFld = this.getFirstErrorItem();
    // special case
    // if there is only an error on the id and no error on any field
    // display that message then
    if (!additionalMsg && errors && errors.id && !errorFld && errors.id.errorMessage) {
      msg = errors.id.errorMessage;
    }

    // set the error message
    this.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, msg);

    // and focus to the first error field
    this.setFocusInErrorField();
  },

  setFocusInErrorField: function () {
    var errorFld = this.getFirstErrorItem();
    if (errorFld) {
      if (this.view.standardWindow.isAutoSaving) {
        // otherwise the focus results in infinite cycles
        // with views getting activated all the time
        this.setFocusItem(errorFld);
      } else if (this.view.isActiveView()) {
        errorFld.focusInItem();
      }
      return;
    }
  },

  getFirstErrorItem: function () {
    var flds = this.getFields(),
        errs = this.getErrors(),
        i;
    if (flds.length) {
      var length = flds.length;
      for (i = 0; i < length; i++) {
        if (flds[i].getErrors() || errs[flds[i].name]) {
          return flds[i];
        }
      }
    }
    return null;
  },

  // overridden to show the error when hovering over items
  titleHoverHTML: function (item) {
    if (!item.isVisible()) {
      return null;
    }
    var errs = item.getErrors();
    if (!errs) {
      return this.Super('titleHoverHTML', arguments);
    }
    return OB.Utilities.getPromptString(errs);
  },

  itemHoverHTML: function (item) {
    if (!item.isVisible()) {
      return null;
    }
    var errs = item.getErrors();
    if (!errs) {
      return this.Super('itemHoverHTML', arguments);
    }
    return OB.Utilities.getPromptString(errs);
  },

  // overridden here to place the link icon after the mandatory sign
  getTitleHTML: function (item, error) {
    var titleHTML = this.Super('getTitleHTML', arguments),
        searchIconObj, imgHTML;

    if (item.showLinkIcon && item.targetEntity && OB.AccessibleEntities[item.targetEntity]) {

      // the parent property does not need a link, as it is shown in the 
      // parent view
      if (item.parentProperty) {
        return titleHTML;
      }

      searchIconObj = {
        src: item.newTabIconSrc,
        height: item.newTabIconSize,
        width: item.newTabIconSize,
        align: 'absmiddle',
        extraStuff: ' id="' + item.ID + this.LINKBUTTONSUFFIX + '" class="OBFormFieldLinkButton" '
      };

      imgHTML = isc.Canvas.imgHTML(searchIconObj);

      // handle a small issue in chrome/firefox that the user agents stylesheet
      // sets a default cursor on labels
      if (titleHTML.contains('LABEL')) {
        titleHTML = titleHTML.replace('<LABEL', '<LABEL style="cursor: pointer"');
      }

      return '<span class="OBFormFieldLinkButton">' + titleHTML + '</span>&nbsp;' + imgHTML;
    }
    //is not a link therefore this property is not needed.
    //if this property is null the click event won't open a new tab
    item.linkButtonClick = null;
    return titleHTML;
  },

  // we are being reshown, get new values for the combos
  visibilityChanged: function (visible) {
    if (visible && (this.view.isShowingForm || this.view.isEditingGrid)) {
      this.doChangeFICCall();
    }
  },

  onFieldChanged: function (form, item, value) {
    // To be implemented dynamically
  },

  disableItem: function (otherName, condition) {
    var otherItem = this.getItem(otherName);
    if (otherItem && otherItem.disable && otherItem.enable) {
      if (this.readOnly) {
        otherItem.disable();
      } else if (condition) {
        otherItem.disable();
      } else {
        otherItem.enable();
      }
    }
  },

  getCachedContextInfo: function () {
    if (!this.contextInfo) {
      this.contextInfo = this.view.getContextInfo(false, true, true);
    }
    return this.contextInfo;
  },

  // overridden to prevent updating of a time value which 
  // has only been edited half, only do this if we are in change
  // handling (to enable buttons etc.)
  updateFocusItemValue: function () {
    var ret, focusItem = this.getFocusSubItem();
    if (this.inChangeHandling && focusItem && !focusItem.changeOnKeypress) {
      return;
    }
    if (this.grid) {
      this.grid._preventDateParsing = true;
      ret = this.Super('updateFocusItemValue', arguments);
      delete this.grid._preventDateParsing;
    } else {
      ret = this.Super('updateFocusItemValue', arguments);
    }
    return ret;
  },

  enableShortcuts: function () {
    var me = this,
        ksAction;

    ksAction = function () {
      if (me.getFocusItem && me.getFocusItem().titleClick) {
        me.getFocusItem().titleClick(me, me.getFocusItem());
      }
      return false; //To avoid keyboard shortcut propagation
    };

    OB.KeyboardManager.Shortcuts.set('ViewForm_OpenLinkOut', 'OBViewForm', ksAction);
  },

  draw: function () {
    this.enableShortcuts();
    this.Super('draw', arguments);
  },

  keyDown: function () {
    var response = OB.KeyboardManager.Shortcuts.monitor('OBViewForm');
    if (response !== false) {
      response = this.Super('keyDown', arguments);
    }
    return response;
  },

  // always suppress focus when showing errors, we do focus handling
  // explicitly
  showFieldErrors: function (fieldName, suppressAutoFocus) {
    // temporary set selectonfocus to false
    // until after the redraw, to prevent this issue
    // https://issues.openbravo.com/view.php?id=18739
    this.previousSelectOnFocus = this.selectOnFocus;
    this.selectOnFocus = false;
    this.selectOnFocusStored = true;
    this.Super('showFieldErrors', [fieldName, true]);
  },

  redraw: function () {
    this._isRedrawing = true;
    this.Super('redraw', arguments);
    delete this._isRedrawing;
    if (this.selectOnFocusStored) {
      this.selectOnFocus = this.previousSelectOnFocus;
      delete this.previousSelectOnFocus;
      delete this.selectOnFocusStored;
    }
  },

  destroy: function () {
    var i, item, items = this.getItems(),
        len = items.length,
        ds, dataSources = [];

    // caching reference to all DS of Items
    for (i = 0; i < len; i++) {
      item = items[i];
      ds = item ? item.dataSource || item.optionDataSource : null;

      if (ds) {
        dataSources.push(ds);
      }
    }

    this.Super('destroy', arguments);
    len = dataSources.length;

    // Destroying DS not managed by DynamicForm.destroy
    for (i = 0; i < len; i++) {
      ds = dataSources[i];
      if (ds) {
        ds.destroy();
        ds = null;
      }
    }
  },

  allRequiredFieldsSet: function () {
    var i, item, length = this.getItems().length,
        value, undef, nullValue = null;
    for (i = 0; i < length; i++) {
      item = this.getItems()[i];
      value = this.getValue(item);
      if (this.isRequired(item) && value !== false && value !== 0 && !value) {
        return false;
      }
    }
    return true;
  }
};