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
 * Contributor(s): ___________
 ************************************************************************
 */

// = Personalization Utilities =
// Contains utility methods for window personalization.
//
// Two important methods:
// - getPersonalizationDataFromForm: computes the personalization data structure (the fields
//  from an existing form instance, or if the form has already been personalized returns the
//  personalization data used to personalize the form
// - personalizeWindow/personalizeForm: personalize a form by applying the 
//  personalization data to it. This method is used to update the form preview as well
//  to update the real form. It is called from the standard window
//
OB.Personalization = {
  STATUSBAR_GROUPNAME: '_statusBar',
  MAIN_GROUPNAME: '_main'
};

// ** {{{OB.Personalization.getPersonalizationDataFromForm}}} **
// Creates the data structure used by the form personalizer and stored
// as personalized form information in the UI personalization record.
// If an existing personalization data is passed in then that one is
// used as the basis. This can be used to make sure that the 
// personalizationData used is up-to-date with the current form fields.
OB.Personalization.getPersonalizationDataFromForm = function (form) {
  var i, dataFields = [],
      statusBarFields, length, record, origPersonalizationData = form && form.view ? form.view.getFormPersonalization(true) : null;

  // just use the personalization data which was used on the 
  // form, we can not reconstruct it completely from the form fields
  // as we don't store extra personalization data in the form fields
  // themselves
  if (origPersonalizationData && origPersonalizationData.form) {
    dataFields = origPersonalizationData.form.fields;
  }

  // create the statusbar array so we don't use the one from the form
  if (dataFields && dataFields.length > 0) {
    statusBarFields = [];
    length = dataFields.length;
    for (i = 0; i < length; i++) {
      if (dataFields[i].parentName === OB.Personalization.STATUSBAR_GROUPNAME) {
        statusBarFields.push(dataFields[i].name);
      }
    }
  }

  // update with the form data, new fields may have been added, titles
  // may have changed etc.
  // the content of dataFields will be updated
  this.updatePersonalizationDataFromFields(dataFields, form.getFields(), statusBarFields || form.statusBarFields);

  // set the first focus field
  if (form.firstFocusedField) {
    record = dataFields.find('name', form.firstFocusedField);
    if (record) {
      record.firstFocus = true;
    }
  }

  // if there was already a personalization object, then re-use
  // everything except the fields
  if (origPersonalizationData) {
    return isc.addProperties({}, origPersonalizationData, {
      form: {
        fields: dataFields
      }
    });
  }

  // and return in the expected format
  return {
    form: {
      fields: dataFields
    }
  };
};

//** {{{OB.Personalization.updatePersonalizationDataFromFields}}} **
// will update the personalization data from a form, this to handle addition 
// of new fields in the AD, changes in required and the title and removal of
// fields.
OB.Personalization.updatePersonalizationDataFromFields = function (dataFields, fields, statusBarFields) {
  var fld, j, record, i, dataField, undef, length;

  // required and title and removal of fields
  // length is recomputed every time as fields can be removed
  // note: not factored out in a separate length attribute,
  // length changes
  for (i = 0; i < dataFields.length; i++) {
    dataField = dataFields[i];
    fld = fields.find('name', dataField.name);
    if (fld) {
      dataField.required = fld.required;
      dataField.hasDisplayLogic = fld.hasShowIf === true || (fld.showIf !== undef && !isc.isA.OBSectionItem(fld));
      // disabled extra * for now, as we have an icon for it
      //      if (false && dataField.required) {
      //        if (isc.Page.isRTL()) {
      //          dataField.title = '* ' + fld.title;       
      //        } else {
      //          dataField.title = fld.title + ' *';       
      //        }
      //      } else {
      dataField.title = fld.title;
      if (fld.sectionExpanded) {
        dataField.sectionExpanded = true;
      }
      //      }
    } else if (!dataField.isSystemFolder) {
      // field has been removed, remove it
      // can be a folder
      if (dataField.isFolder) {
        // clear all the parent Names
        for (j = 0; j < dataFields.length; j++) {
          if (dataField.name === dataFields[j].parentName) {
            delete dataFields[j].parentName;
          }
        }
      }
      dataFields.remove(dataField);
    }
  }

  // always create a status bar group
  if (!dataFields.find('name', OB.Personalization.STATUSBAR_GROUPNAME)) {
    dataFields.push({
      isFolder: true,
      isSystemFolder: true,
      canDrag: false,
      title: OB.I18N.getLabel('OBUIAPP_Personalization_StatusBar_Group'),
      name: OB.Personalization.STATUSBAR_GROUPNAME,
      isSection: true,
      displayed: true,
      _canEdit: false
    });
  }

  if (!dataFields.find('name', OB.Personalization.MAIN_GROUPNAME)) {
    dataFields.push({
      isFolder: true,
      isSystemFolder: true,
      canDrag: false,
      title: OB.I18N.getLabel('OBUIAPP_Personalization_Main_Group'),
      name: OB.Personalization.MAIN_GROUPNAME,
      isSection: true,
      displayed: true,
      _canEdit: false
    });
  }

  // iterate over the fields of the form and handle sections and fields
  length = fields.length;
  for (i = 0; i < length; i++) {
    fld = fields[i];

    if (fld.personalizable === false) {
      continue;
    }

    // already there, continue
    dataField = dataFields.find('name', fld.name);
    if (dataField) {
      // will be corrected in a later loop
      delete dataField.isStatusBarField;
      continue;
    }

    // for each field create a tree record, a tree record is identified
    // by its name
    if (isc.isA.SectionItem(fld)) {
      // section items are shown as folders which can not be dragged
      // or edited
      record = {
        isFolder: true,
        _canEdit: false,
        isSection: true,
        // the childNames are used below to resolve parent names
        childNames: fld.itemIds,
        title: fld.title,
        name: fld.name
      };
    } else {
      record = {
        title: fld.title,
        name: fld.name,
        hiddenInForm: fld.hiddenInForm,
        startRow: fld.startRow,
        colSpan: fld.colSpan,
        required: fld.required,
        hasDefaultValue: fld.hasDefaultValue,
        rowSpan: fld.rowSpan
      };
    }

    // is used below to get rid of non-displayed fields which 
    // are not part of the statusbar, explicit equals to false
    // as it might not be set
    if (fld.displayed === false) {
      record.displayed = false;
    } else {
      record.displayed = true;
    }

    // and keep what we computed
    dataFields.push(record);
  }

  // now resolve the parent names, i.e. in Smartclient a section item
  // has a set of item ids, in the tree we use (as we use 'parent' mode) the items
  // refer to the parent, so the other way
  length = dataFields.length;
  for (i = 0; i < length; i++) {
    if (dataFields[i].childNames) {
      for (j = 0; j < dataFields[i].childNames.length; j++) {
        // find is a smartclient extension
        record = dataFields.find('name', dataFields[i].childNames[j]);
        if (record && !record.parentName) {
          record.parentName = dataFields[i].name;
        }
      }
    }
  }

  // add to the status bar fields
  length = statusBarFields.length;
  for (i = 0; i < length; i++) {
    record = dataFields.find('name', statusBarFields[i]);
    if (record) {
      record.parentName = OB.Personalization.STATUSBAR_GROUPNAME;
      // these items can not be moved from the statusbar
      record.isStatusBarField = true;
      // keep track that this at one point was a 
      // status bar field, allow it to be put back there
      record.wasOnStatusBarField = true;
    }
  }

  length = dataFields.length;
  for (i = length - 1; i >= 0; i--) {
    record = dataFields[i];

    // do not consider the not-displayed ones which are not
    // part of the statusbar, these have item type
    // hidden which means that we can not visualize them, except
    // in the statusbar
    // explicit equals to false as displayed might not be set
    if (!record.isStatusBarField && !record.displayed) {
      dataFields.removeAt(i);
    } else if (!record.parentName && !record.isSystemFolder) {
      // otherwise add to the main group
      record.parentName = OB.Personalization.MAIN_GROUPNAME;
    }
  }
};

// ** {{{OB.Personalization.personalizeWindow}}} **
// Applies the data structure which contains the personalization settings to
// a complete window (an instance of ob-standard-window). 
// Also handles the case that a personalization record is deleted so that the
// form falls back to the default state
OB.Personalization.personalizeWindow = function (data, window) {
  var tabId, personalizationData, undef, form, view, i, viewsToReset = [],
      done, length;

  // no personalization, nothing to do
  if (!data) {
    return;
  }

  // keep track of the tabs which are personalized
  // is used below to de-personalize them
  length = window.views.length;
  for (i = 0; i < length; i++) {
    if (window.getFormPersonalization(window.views[i], true)) {
      viewsToReset.push({
        tabId: window.views[i].tabId
      });
    }
  }

  // iterate over the tabs
  for (tabId in data) {
    if (data.hasOwnProperty(tabId)) {
      personalizationData = data[tabId];
      view = window.getView(tabId);

      done = viewsToReset.find('tabId', tabId);
      if (done) {
        viewsToReset.remove(done);
      }

      // note, the personalization for a tab maybe null
      // view can be null if a personalization setting
      // is not in sync anymore with the window
      if (personalizationData && view) {
        OB.Personalization.personalizeForm(personalizationData, view.viewForm);
      }

      // the personalization button has 2 icons: one 2 show that there is
      // a personalization and one that doesn't
      // this can be changed, update the state
      if (view) {
        view.toolBar.updateButtonState(false);
      }
    }
  }

  // a personalization may have been removed, reset the form
  // to its original state
  length = viewsToReset.length;
  for (i = 0; i < length; i++) {
    view = window.getView(viewsToReset[i].tabId);

    // the personalization button has 2 icons: one 2 show that there is
    // a personalization and one that doesn't
    // this can be changed, update the state
    if (view) {
      view.toolBar.updateButtonState(false);
    }

    if (view.viewForm.originalStatusBarFields) {
      view.viewForm.statusBarFields = view.viewForm.originalStatusBarFields;
    }
    if (view.viewForm.originalFirstFocusedField !== undef) {
      view.viewForm.firstFocusedField = view.viewForm.originalFirstFocusedField;
    }

    // always clone the fields as the setFields changes their content
    // and you can not call setFields with content which has already
    // been passed to it
    view.viewForm.setFields(isc.shallowClone(view.viewForm._originalFields));

    // the status bar fields may have changed
    view.statusBar.updateContentTitle(view.viewForm.getStatusBarFields());

    // redraw the form for the changes
    view.viewForm.markForRedraw();
  }
};

// ** {{{OB.Personalization.personalizeForm}}} **
// Applies the data structure which contains the personalization settings to a
// form.
OB.Personalization.personalizeForm = function (data, form) {
  var persId, i, j, fld, undef, fldDef, childFld, newField, newFields = [],
      record, length, allChildFieldsHidden, statusBarFields = [];

  // work further with the fields themselves
  data = data.form.fields;

  // keep some stuff to be able to reset when the personalization settings
  // get deleted
  // explicit check on null, as it may not have been set
  if (form.originalFirstFocusedField === undef) {
    if (!form.firstFocusedField) {
      form.originalFirstFocusedField = null;
    } else {
      form.originalFirstFocusedField = form.firstFocusedField;
    }
  }
  if (!form.originalStatusBarFields) {
    form.originalStatusBarFields = isc.shallowClone(form.statusBarFields);
  }

  // iterate over the personalized data, this ensures that we follow
  // the order defined by the user
  length = data.length;
  for (i = 0; i < length; i++) {
    record = data[i];

    // original name is used when a field is visible in the status bar
    // and also on the form
    fld = form.getField(record.name);

    // use the originalFields as we are then sure
    // that we do not get ready build form items
    // but just the original simple objects
    // with properties
    fldDef = form._originalFields.find('name', record.name);
    if (!fld || !fldDef) {
      // a folder for example
      continue;
    }

    // for the preview form get rid of all non-personalizable stuff
    if (form.isPreviewForm && !fldDef.personalizable) {
      continue;
    }

    // set the first focused field
    if (record.firstFocus) {
      form.firstFocusedField = record.name;
    }

    // work with a clone
    newField = isc.shallowClone(fldDef);

    if (record.isSection) {
      newField.itemIds = [];
      // find the child items and set them
      // if all fields are hidden then don't show the section item either
      allChildFieldsHidden = true;
      for (j = 0; j < data.length; j++) {
        if (!data[j].isStatusBarField && data[j].parentName && data[j].parentName === newField.name) {
          newField.itemIds.push(data[j].name);
          allChildFieldsHidden = allChildFieldsHidden && data[j].hiddenInForm;
        }
      }

      // if all fields are hidden then don't show the section item either
      if (allChildFieldsHidden) {
        newField.hiddenInForm = true;
        newField.visible = false;
        newField.alwaysTakeSpace = false;
      } else {
        if (record.sectionExpanded) {
          newField.sectionExpanded = true;
        }
        newField.alwaysTakeSpace = true;
        delete newField.hiddenInForm;
        delete newField.visible;
      }
    } else if (record.isStatusBarField) {
      // we encountered a status bar field, if it is not hidden then
      // put it on the status bar
      if (!record.hiddenInForm) {
        statusBarFields.push(record.name);
      }
      // always hide
      newField.visible = false;
      newField.alwaysTakeSpace = false;
    } else {
      // only copy the things we want to copy
      newField.startRow = record.startRow;
      newField.colSpan = record.colSpan;
      newField.rowSpan = record.rowSpan;

      if (record.hiddenInForm) {
        newField.hiddenInForm = true;
        newField.visible = false;
        newField.alwaysTakeSpace = false;
      } else {
        newField.alwaysTakeSpace = true;
        delete newField.hiddenInForm;
        delete newField.visible;
      }
    }

    newFields.push(newField);
  }

  // now add the ones we did not know about, these maybe new 
  // fields or hidden fields
  if (!form.isPreviewForm) {
    length = form.getFields().length;
    for (i = 0; i < length; i++) {
      record = data.find('name', form.getFields()[i].name);
      // use the original.fields as we are then sure
      // that we do not get ready build form items
      // but just the original simple objects
      // with properties
      fldDef = form._originalFields.find('name', form.getFields()[i].name);
      if (!record && fldDef) {
        // clone the fieldDef
        newFields.push(isc.shallowClone(fldDef));
      }
    }
  }

  // set the fields
  form.statusBarFields = statusBarFields;
  form.setFields(newFields);

  // and show me the stuff!
  form.markForRedraw();

  if (form.statusBar) {
    // the preview form has a direct reference to the statusbar
    form.statusBar.updateContentTitle(form.getStatusBarFields());
  } else if (form.view && form.view.statusBar) {
    // when opened directly from a form
    form.view.statusBar.updateContentTitle(form.getStatusBarFields());
  }
};