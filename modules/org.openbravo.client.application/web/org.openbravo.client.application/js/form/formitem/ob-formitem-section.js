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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBSectionItem ==
// Form sections, used for notes, more information, attachment etc.

isc.ClassFactory.defineClass('OBSectionItem', isc.SectionItem);

isc.OBSectionItem.addProperties({
  sectionExpanded: false,
  
  // revisit when/if we allow disabling of section items
  // visual state of disabled or non-disabled stays the same now
  showDisabled: false,

  // some defaults, note if this changes then also the 
  // field generation logic needs to be checked
  colSpan: 4, 
  startRow: true, 
  endRow: true,
  
  canTabToHeader: true,
  
  alwaysTakeSpace: false,

  click: function() {
    this.focusInItem();
    
    var ret = this.Super('click', arguments);
    return ret;
  },

  setSectionItemInContent: function(form) {
    var i = 0, length = this.itemIds.length;
    for (i = 0; i < length; i++) {
      if (form.getItem(this.itemIds[i])) {
        form.getItem(this.itemIds[i]).section = this;
      }
    }
  },
  
  // never disable a section item
  isDisabled: function(){
    return false;
  },

  // Update the property alwaysTakeSpace when collapsing/expanding a section
  // Note: The hidden fields are not updated, they always have alwaysTakeSpace to false
  updateAlwaysTakeSpace: function(flag) {
    var i, f = this.form, item, length = this.itemIds.length;

    for(i = 0; i < length; i++) {
      item = f.getItem(this.itemIds[i]);
      if (item) {
        // note different cases can occur, these properties may be set, maybe
        // undefined, false or true, undefined is not always false, as a field
        // is not always processed through all logic in the system which sets
        // these properties
        if (item.hiddenInForm) {
          continue;
        }
        if (item.displayed !== false) {
          item.alwaysTakeSpace = flag;
        }
      }
    }
  },
  
  collapseSection: function(preventFocusChange) {
    this.updateAlwaysTakeSpace(false);

    var ret = this.Super('collapseSection', arguments);
    return ret;
  },
  
  expandSection: function() {
    this.updateAlwaysTakeSpace(true);

    var ret = this.Super('expandSection', arguments);
    return ret;
  },
    
  showIf: function(item, value, form, values) {
    var i, field, length;
    
    if (!this.itemIds || item.hiddenInForm === true || item.displayed === false ) {
      this.updateAlwaysTakeSpace(false); // To avoid an empty space if the section is not shown
      return false;
    }
    
    length = this.itemIds.length;
    
    for (i = 0; i < length; i++) {
      field = form.getItem(this.itemIds[i]);

      if(!field || field.displayed === false || field.hiddenInForm === true) {
        continue;
      }

      if (field.obShowIf) {
        if(field.obShowIf(field, value, form)) {
          return true;
        }
      } else {
        return true;
      }
    }
    
    this.updateAlwaysTakeSpace(false); // To avoid an empty space if the section is not shown
    return false;
  }
});
