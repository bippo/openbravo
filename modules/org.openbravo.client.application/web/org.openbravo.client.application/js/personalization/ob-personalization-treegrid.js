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

// = Defines the OBPersonalizationTree =
// Defines the tree shown on the left in the personalization form.
// Uses standard Smartclient features except for:
// - status bar fields which can not be used on the form can not 
//  be dragged out
// - clicking a folder opens/closes it
// - when dragging a normal field on the status bar folder then 
//  a copy is made as a normal field may exist both on the form 
//  as in the status bar
// - added a context menu to make it easy to directly update
//  item properties
// - changed styling of hidden fields 
//
isc.ClassFactory.defineClass('OBPersonalizationTreeGrid', isc.TreeGrid);

isc.OBPersonalizationTreeGrid.addProperties({
  showHeader: false,
  canReorderRecords: true,
  canAcceptDroppedRecords: true,
  leaveScrollbarGap: false,
  showCellContextMenus: true,

  // when an item gets dropped on a closed folder its icon 
  // changes
  showDropIcons: true,
  showOpenIcons: true,
  dropIconSuffix: 'open',
  closedIconSuffix: 'closed',
  openIconSuffix: 'open',

  fields: [{
    name: 'title',
    canHover: true,
    showHover: true,
    treeField: true,
    showTitle: false,
    type: 'text',
    width: '100%',
    canEdit: false
  }
  // disabled for now, it can be an idea to support direct editing in the tree
  //    {name: 'colSpan', title: OB.I18N.getLabel('OBUIAPP_Personalization_Colspan'), type: 'number', editorType: 'TextItem', keyPressFilterNumeric: '[0-9]'}, 
  //    {name: 'rowSpan', title: OB.I18N.getLabel('OBUIAPP_Personalization_Rowspan'),  type: 'number', editorType: 'TextItem', keyPressFilterNumeric: '[0-9]'}, 
  //    {name: 'startRow', title: OB.I18N.getLabel('OBUIAPP_Personalization_Startrow'), type: 'boolean'}, 
  //    {name: 'hiddenInForm', title: OB.I18N.getLabel('OBUIAPP_Personalization_Hidden'), type: 'boolean'}
  ],

  initWidget: function () {
    var length = this.fields.length,
        me = this,
        changedFunction, i;

    // todo: show custom items for different types of fields
    this.nodeIcon = OB.Styles.Personalization.Icons.field;
    this.folderIcon = OB.Styles.Personalization.Icons.fieldGroup;

    // register a change notifier
    changedFunction = function () {
      me.personalizeForm.changed();
    };

    for (i = 0; i < length; i++) {
      this.fields[i].changed = changedFunction;
    }

    // hovering
    this.fields[0].hoverHTML = function (record, value) {
      return me.personalizeForm.getHoverHTML(record, null);
    };

    this.computeNodeIcons(this.fieldData);

    // create the tree, note the modeltype, idField and parentIdField
    // they determine how the tree is build from the list of nodes
    this.data = isc.Tree.create({
      modelType: 'parent',
      idField: 'name',
      parentIdField: 'parentName',
      data: this.fieldData,
      dataChanged: function () {
        me.personalizeForm.changed();
      },
      // note Tree is not a widget, it more like a datasource
      // it has no visual representation, therefore overriding init
      init: function () {
        var mainNode;
        this.Super('init', arguments);

        // open the main folder as a default
        mainNode = this.getAllNodes().find('name', OB.Personalization.MAIN_GROUPNAME);
        this.openFolder(mainNode);
      }
    });

    // does not seem to work for the root, can also
    // be done by overriding the folderDrop method, see below
    //   this.data.getRoot().canAcceptDrop = false;
    // commented, start closed
    //   this.data.openAll();
    this.Super('initWidget', arguments);
  },

  // open the folders and expands form items, needs to be called
  // after the preview form has been build
  openFolders: function () {
    var i, nodes;
    // open the folders which need to be opened
    for (i = 0, nodes = this.data.getAllNodes(); i < nodes.length; i++) {
      if (nodes[i].sectionExpanded) {
        this.openFolder(nodes[i]);
      }
    }
  },

  destroy: function () {
    if (this.data) {
      this.data.destroy();
    }
    this.Super('destroy', arguments);
  },

  // open/close a folder on folder click
  folderClick: function (viewer, folder, recordNum) {
    if (this.data.isOpen(folder)) {
      this.closeFolder(folder);
    } else {
      this.openFolder(folder);
    }
  },

  closeFolder: function (folder) {
    var fld, i, length, flds = this.personalizeForm.previewForm.getFields();

    this.Super('closeFolder', arguments);

    // find the section fld and collapse
    for (i = 0, length = flds.length; i < length; i++) {
      if (flds[i].name === folder.name && flds[i].collapseSection) {
        folder.sectionExpanded = false;
        flds[i].collapseSection();
        this.personalizeForm.changed();
        break;
      }
    }
  },

  openFolder: function (folder) {
    var fld, i, length, flds = this.personalizeForm.previewForm.getFields();

    this.Super('openFolder', arguments);

    // find the section fld and collapse
    for (i = 0, length = flds.length; i < length; i++) {
      if (flds[i].name === folder.name && flds[i].expandSection) {
        folder.sectionExpanded = true;
        flds[i].expandSection();
        this.personalizeForm.changed();
        break;
      }
    }
  },

  // overridden to:
  // - prevent a change event if a node is dropped
  // in the same location (code commented out, seems to prevent move..)
  // - set isStatusBarField flag when moved into the status bar folder
  folderDrop: function (nodes, folder, index, sourceWidget, callback) {
    var i, oldNode, oldValue, newCallback, changed, length;

    if (!nodes) {
      return;
    }

    // can not drop in the root
    if (folder && folder.name === '/') {
      return;
    }

    length = nodes.length;

    // don't allow required fields without default value 
    // to be dropped on the statusbar
    if (folder.name === OB.Personalization.STATUSBAR_GROUPNAME) {
      for (i = 0; i < length; i++) {
        if (!nodes[i].wasOnStatusBarField && nodes[i].required && !nodes[i].hasDefaultValue) {
          return;
        }
      }
    }

    //    
    //    // check if the nodes are all dropped on their current parent
    //    // in the same place they are now (note index + i is done, as
    //    // index is not an array)
    //    // if that's the case then just return to not get a datachanged
    //    // event
    //    changed = false;
    //    for (i = 0; i < nodes.length; i++) {
    //      if (nodes[i].parentName !== folder.name || 
    //          this.data.indexOf(nodes[i]) !== (index + i)) {
    //        changed = true;
    //        break;
    //      }
    //    }
    //    if (!changed) {
    //      return;
    //    }
    // folders can not be dropped outside of the main group
    for (i = 0; i < length; i++) {
      if (nodes[i].isFolder && (!folder || folder.name !== OB.Personalization.MAIN_GROUPNAME)) {
        return;
      }
      nodes[i].isStatusBarField = (folder.name === OB.Personalization.STATUSBAR_GROUPNAME);
    }

    this.Super('folderDrop', arguments);
  },

  // show hidden items in a different style
  getBaseStyle: function (record, rowNum, colNum) {
    if (record.hiddenInForm) {
      return this.baseStyle + 'Hidden';
    }
    return this.baseStyle;
  },

  // no context menu on folders
  folderContextClick: function (me, record, recordNum) {
    return false;
  },

  // overridden to create context menu items specific 
  // for the clicked record
  cellContextClick: function (record, rowNum, colNum) {
    // select when right clicking, this can have some side effects
    // focus and menus appearing/disappearing, check if this happens
    this.deselectAllRecords();
    this.selectRecord(record);

    // create the context items for the clicked record
    this.cellContextItems = this.createCellContextItems(record);
    // continue with normal behavior
    return true;
  },

  // the menu entries when right clicking a field, different menu
  // entries are shown for status bar or normal fields
  createCellContextItems: function (record) {
    var i, menuItems = [],
        updatePropertyFunction, me = this,
        personalizeForm = this.personalizeForm,
        length, allNodes;

    updatePropertyFunction = function (record, property, value) {
      record[property] = value;

      // make sure only one record has first focus
      if (record.firstFocus) {
        allNodes = personalizeForm.fieldsTreeGrid.data.getAllNodes();
        length = allNodes.length;
        for (i = 0; i < length; i++) {
          if (allNodes[i].firstFocus) {
            allNodes[i].firstFocus = false;
          }
        }
        record.firstFocus = true;
      }

      // items may have been hidden, which changes their colour
      personalizeForm.fieldsTreeGrid.markForRedraw();

      // set the value in the properties form also
      if (property === 'hiddenInForm') {
        personalizeForm.propertiesLayout.formLayout.form.setValue('displayed', !value);
      } else {
        personalizeForm.propertiesLayout.formLayout.form.setValue(property, value);
      }

      // this will reset everything
      personalizeForm.changed();
    };

    // status status bar fields can be hidden but not removed (as they
    // do not exist on the rest of the form)
    if (record.isStatusBarField) {
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Displayed'),
        checked: !record.hiddenInForm,
        click: function () {
          updatePropertyFunction(record, 'hiddenInForm', !record.hiddenInForm);
        }
      });
    } else {
      // for normal nodes, show some properties which can be changed
      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Personalization_Startrow'),
        checked: record.startRow,
        click: function () {
          updatePropertyFunction(record, 'startRow', !record.startRow);
        }
      });

      if (record.wasOnStatusBarField || !record.required || record.hasDefaultValue) {
        menuItems.add({
          title: OB.I18N.getLabel('OBUIAPP_Personalization_Displayed'),
          checked: !record.hiddenInForm,
          click: function () {
            updatePropertyFunction(record, 'hiddenInForm', !record.hiddenInForm);
          }
        });
      }

      menuItems.add({
        title: OB.I18N.getLabel('OBUIAPP_Personalization_FirstFocus'),
        checked: record.firstFocus,
        click: function () {
          updatePropertyFunction(record, 'firstFocus', !record.firstFocus);
        }
      });
    }

    return menuItems;
  },

  redraw: function () {
    this.computeNodeIcons();
    this.Super('redraw', arguments);
  },

  computeNodeIcons: function (nodes) {
    var iconSuffix, node, i, data = nodes || this.data.getAllNodes(),
        length = data.length;
    for (i = 0; i < length; i++) {
      node = data[i];
      if (node.isFolder) {
        continue;
      }
      iconSuffix = '';
      if (node.required) {
        iconSuffix = 'Required';
      }
      if (node.hasDisplayLogic) {
        iconSuffix = iconSuffix + 'DisplayLogic';
      }
      if (node.hiddenInForm) {
        iconSuffix = iconSuffix + 'Hidden';
      }
      node.icon = OB.Styles.Personalization.Icons['field' + iconSuffix];
    }
  }
});