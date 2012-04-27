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
 * Contributor(s): Valery Lezhebokov.
 ************************************************************************
 */

// = OBNotesItems =
//
// Represents the notes section shown in the bottom of the form.
// Note is not shown for new records.
//
isc.ClassFactory.defineClass('OBNoteSectionItem', isc.OBSectionItem);

isc.OBNoteSectionItem.addProperties({
  // as the name is always the same there should be at most
  // one note section per form
  name: '_notes_',

  overflow: 'hidden',

  // some defaults, note if this changes then also the 
  // field generation logic needs to be checked
  colSpan: 4,
  startRow: true,
  endRow: true,

  canFocus: true,

  prompt: OB.I18N.getLabel('OBUIAPP_NotesPrompt'),

  noteCanvasItem: null,

  visible: true,

  noteCount: 0,

  // this field group does not participate in personalization
  personalizable: false,

  itemIds: ['_notes_Canvas'],

  // note formitems don't have an initWidget but an init method
  init: function () {
    // override the one passed in
    this.defaultValue = OB.I18N.getLabel('OBUIAPP_NotesTitle');

    /* tell the form who we are */
    this.form.noteSection = this;

    this.Super('init', arguments);
  },

  setNoteCount: function (lNoteCount) {
    lNoteCount = parseInt(lNoteCount, 10);
    this.noteCount = lNoteCount;
    if (lNoteCount !== 0) {
      if (!this.getNotePart().noteListGrid.isVisible()) {
        this.getNotePart().noteListGrid.show();
      }
      this.setValue(OB.I18N.getLabel('OBUIAPP_NotesTitle') + ' (' + lNoteCount + ')');
    } else {
      if (this.getNotePart().noteListGrid.isVisible()) {
        this.getNotePart().noteListGrid.hide();
      }
      this.setValue(OB.I18N.getLabel('OBUIAPP_NotesTitle'));
    }
  },

  getNotePart: function () {
    if (!this.noteCanvasItem) {
      this.noteCanvasItem = this.form.getField(this.itemIds[0]);
    }
    return this.noteCanvasItem.canvas;
  },

  setRecordInfo: function (entity, id) {
    this.getNotePart().setRecordInfo(entity, id);
  },

  refresh: function () {
    this.getNotePart().refresh();
  },

  expandSection: function () {
    this.Super('expandSection', arguments);
    this.form.noteSection.refresh();
  },

  hide: function () {
    this.Super('hide', arguments);
    if (this.noteCanvasItem) {
      // Solves issue #16663: Forcing call to canvas hide. 
      // Shouldn't this be invoked by SmartClient 
      this.noteCanvasItem.hide();
    }
  }

});

isc.ClassFactory.defineClass('OBNoteLayout', isc.VLayout);

isc.OBNoteLayout.addProperties({

  entity: null,

  recordId: null,

  layoutMargin: 0,

  membersMargin: 10,

  noteTextAreaItem: null,

  noteDynamicForm: null,

  saveNoteButton: null,

  noteDSId: '090A37D22E61FE94012E621729090048',

  noteListGrid: null,

  /**
   * Saves the note to the DB.
   */
  saveNote: function () {
    var note = this.noteDynamicForm.getField('noteOBTextAreaItem').getValue();

    if (!note) {
      return;
    }

    this.noteDynamicForm.validate();

    var noteDS = this.getNoteDataSource();

    var currentTime = new Date();

    noteDS.addData({
      'client': OB.User.clientId,
      'organization': OB.User.organizationId,
      'table': this.getForm().view.standardProperties.inpTableId,
      'record': this.getForm().view.viewGrid.getSelectedRecord().id,
      'note': note
    });

    // clean text area
    this.noteDynamicForm.getItem('noteOBTextAreaItem').clearValue();
    this.saveNoteButton.setDisabled(true);
    this.noteDynamicForm.focusInItem('noteOBTextAreaItem');

    this.parentElement.noteSection.setNoteCount(this.parentElement.noteSection.noteCount + 1);
  },

  /**
   * Deletes the note from the DB.
   */
  deleteNote: function ( /* note id to delete */ id) {
    var noteDS = this.getNoteDataSource();
    var noteSection = this.parentElement.noteSection;
    isc.confirm(OB.I18N.getLabel('OBUIAPP_ConfirmRemoveNote'), function (clickedOK) {
      if (clickedOK) {
        noteDS.removeData({
          'id': id
        });
        noteSection.setNoteCount(noteSection.noteCount - 1);
      }
    }, {
      title: OB.I18N.getLabel('OBUIAPP_DialogTitle_DeleteNote')
    });
  },

  /**
   * Returns Notes data source.
   */
  getNoteDataSource: function () {
    return this.noteListGrid.dataSource;
  },

  /**
   * Initializes the widget.
   */
  initWidget: function () {
    this.Super('initWidget', arguments);

    var hLayout = isc.HLayout.create({
      width: '50%',
      height: '100%',
      layoutMargin: 0,
      layoutTopMargin: 10,
      membersMargin: 10
    });
    hLayout.setLayoutMargin();

    this.noteDynamicForm = isc.DynamicForm.create({
      numCols: 1,
      width: '100%',
      fields: [{
        name: 'noteOBTextAreaItem',
        type: 'OBTextAreaItem',
        showTitle: false,
        layout: this,
        width: '*',
        length: 2000,
        change: function (form, item, value, oldValue) {
          if (value) {
            this.layout.saveNoteButton.setDisabled(false);
          } else {
            this.layout.saveNoteButton.setDisabled(true);
          }
          return this.Super('change', arguments);
        },
        validators: [{
          type: 'required'
        }]
      }]
    });

    this.saveNoteButton = isc.OBFormButton.create({
      layout: this,
      margin: 4,
      //hLayout layoutTopMargin is not affecting completly the button, so this magin tries to balance it
      title: OB.I18N.getLabel('OBUIAPP_SaveNoteButtonTitle'),
      click: function () {
        this.layout.saveNote();
        return false;
      },
      canFocus: true,
      draw: function () {
        this.setDisabled(true);
        return this.Super('draw', arguments);
      }
    });

    hLayout.addMember(this.noteDynamicForm);
    hLayout.addMember(this.saveNoteButton);
    // add the grids to the vertical layout
    this.addMember(hLayout);

    this.noteListGrid = isc.OBGrid.create({
      width: '50%',
      autoFitData: 'vertical',
      fields: [{
        name: 'colorBar',
        width: '5'
      }, {
        name: 'note'
      }],
      alternateRecordStyles: false,
      autoFetchData: true,
      baseStyle: 'OBNoteListGridCell',
      fixedRecordHeights: false,
      filterOnKeypress: true,
      headerHeight: 0,
      hoverStyle: 'OBNoteListGridCellOver',
      layout: this,
      height: 1,
      //Due to issue 16695. Only with this, the visualization is strange when no records are shown. The noteListGrid visibility management is needed too.
      visibility: 'hidden',
      //Due to issue 16695. The noteListGrid is automatically shown/hidden each time the note count (set using setNoteCount) is > 0
      selectionType: 'none',
      showEmptyMessage: false,
      styleName: 'OBNoteListGrid',
      wrapCells: true,

      setDataSource: function (dataSource, fields) {
        this.Super('setDataSource', [dataSource, this.fields]);
      },

      fetchData: function (criteria, callback, requestProperties) {
        if (this.layout.getForm() && this.layout.getForm().noteSection && this.layout.getForm().noteSection.visible && this.layout.getForm().noteSection.isExpanded()) {
          return this.Super('fetchData', [this.convertCriteria(criteria), callback, requestProperties]);
        }
      },

      filterData: function (criteria, callback, requestProperties) {
        return this.Super('filterData', [
        this.convertCriteria(criteria), callback, requestProperties]);
      },

      getCriteria: function () {
        var criteria = this.Super('getCriteria', arguments) || {};
        criteria = this.convertCriteria(criteria);
        return criteria;
      },

      convertCriteria: function (criteria) {
        criteria = isc.addProperties({}, criteria || {});

        if (!criteria.criteria) {
          criteria.criteria = [];
        }

        var view = this.layout.getForm().view;
        if (view && view.viewGrid.getSelectedRecord()) {
          criteria.criteria.push({
            fieldName: 'table',
            operator: 'equals',
            value: view.standardProperties.inpTableId
          });

          criteria.criteria.push({
            fieldName: 'record',
            operator: 'equals',
            value: view.viewGrid.getSelectedRecord().id
          });

          criteria[OB.Constants.ORDERBY_PARAMETER] = '-updated';
        }
        return criteria;

      },

      formatCellValue: function (value, record, rowNum, colNum) {

        if (this.getFieldName(colNum) !== 'note') {
          return value;
        }

        value = value + ' <span class="OBNoteListGridAuthor">' + OB.Utilities.getTimePassedInterval(record.recordTime - record.creationDate.getTime()) + ' ' + OB.I18N.getLabel('OBUIAPP_by') + ' ' + record['createdBy._identifier'] + '</span>';

        // show delete link if the note was created by
        // the current user
        if (record.createdBy === OB.User.id) {
          value = value + ' <nobr><span class="OBNoteListGridDelete" ><a class="OBNoteListGridDelete" href="#" onclick="' + this.layout.ID + '.deleteNote(\'' + record.id + '\')">[ ' + OB.I18N.getLabel('OBUIAPP_delete') + ' ]</a></span></nobr>';
        }
        return value;
      },

      getBaseStyle: function (record, rowNum, colNum) {
        if (this.getFieldName(colNum) !== 'colorBar') {
          return this.baseStyle;
        }

        if (record.createdBy === OB.User.id) {
          return 'OBNoteListGridCurrentUserNoteCell';
        } else {
          return 'OBNoteListGridOtherUserNoteCell';
        }
      }

    });

    this.noteListGrid.addSort({
      direction: 'desc',
      property: 'updated'
    });

    this.addMember(this.noteListGrid);

    // register note DS
    OB.Datasource.get(this.noteDSId, this.noteListGrid, null, true);
  },

  /**
   * Sets record information.
   */
  setRecordInfo: function (entity, id) {
    this.entity = entity;
    this.recordId = id;
  },

  refresh: function () {
    this.noteDynamicForm.getItem('noteOBTextAreaItem').clearValue();
    this.noteListGrid.fetchData();
  },

  getForm: function () {
    return this.canvasItem.form;
  }

});

isc.ClassFactory.defineClass('OBNoteCanvasItem', isc.CanvasItem);

isc.OBNoteCanvasItem.addProperties({

  // some defaults, note if this changes then also the 
  // field generation logic needs to be checked
  colSpan: 4,
  startRow: true,
  endRow: true,

  canFocus: true,

  // setting width/height makes the canvasitem to be hidden after a few
  // clicks on the section item, so don't do that for now
  showTitle: false,

  // note that explicitly setting the canvas gives an error as not
  // all props are set correctly on the canvas (for example the
  // pointer back to this item: canvasItem
  // for setting more properties use canvasProperties, etc. see
  // the docs
  canvasConstructor: 'OBNoteLayout',

  // never disable this one
  isDisabled: function () {
    return false;
  }

});