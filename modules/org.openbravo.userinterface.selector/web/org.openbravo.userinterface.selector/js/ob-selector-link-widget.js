/*global setOBTabBehavior */

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
// = Selector Widget =
// Contains the OBSelectorLink Widget. This widget consists of two main parts:
// 1) a Link
// 2) a popup window showing a search grid with data
//
// The widget is a compound widget extending the OBSelectorWidget.
isc.ClassFactory.defineClass('OBSelectorLinkWidget', isc.OBSelectorWidget);

isc.OBSelectorLinkWidget.addProperties({

  // ** {{{ setSelectorValueFromGrid }}} **
  // Override method
  setSelectorValueFromGrid: function () {
    var changed = false,
        oldValue = this.selector.openbravoField.value,
        selected = this.selector.selectorGrid.getSelectedRecord(),
        newValue;
    if (selected) {
      newValue = selected[this.selector.valueField];
      changed = oldValue !== newValue;
      this.selector.openbravoField.value = newValue;
    } else {
      changed = oldValue !== '';
      this.selector.openbravoField.value = '';
    }
    this.selector.selectorWindow.hide();

    // openbravo specific code
    if (changed) {
      this.selector.openbravoChanged(selected);
    }
  },

  afterDrawDo: function () {
    return;
  },

  // ** {{{ openSelectorWindow }}} **
  // open the popup window and make sure that it has the correct
  // filter set
  openSelectorWindow: function (form) {
    var initialFilter = {};
    setOBTabBehavior(false);
    form.selectorWindow.show();
    form.selectorGrid.setFilterEditorCriteria({});
    form.selectorGrid.filterByEditor();
    form.selectorGrid.focusInFilterEditor();
    form.selectorGrid.selectSingleRecord(null);
  },

  // ** {{{ initWidget }}} **
  // Override initWidget to set the parts of the form. Creates the
  // form, suggestion box and popup modal and grid components.
  initWidget: function () {
    var thisSelector = this;

    // Always call the superclass implementation when overriding
    // initWidget
    this.Super('initWidget', arguments);

    if (this.numCols > 0 && this.numCols <= isc.OBSelectorLinkWidget.styling.widthDefinition.length) {
      this.width = isc.OBSelectorLinkWidget.styling.widthDefinition[this.numCols - 1];
    }
    //        } else {
    // TODO log this error case?
    //        }
    // add the link to the DynamicForm
    this.setFields([{
      type: 'link',
      editorType: 'link',
      linkTitle: 'find',
      target: 'javascript',
      handleClick: function () {
        this.containerWidget.openSelectorWindow(this.containerWidget);
      }
    }]);

    this.selectorGrid = isc.ListGrid.create({
      selector: this,
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
        if (!criteria) {
          criteria = {};
        }

        // also adds the special ORG parameter
        OB.Utilities.addFormInputsToCriteria(criteria);
        criteria[OB.Constants.WHERE_PARAMETER] = this.selector.whereClause;

        // set the default sort option
        criteria[OB.Constants.SORTBY_PARAMETER] = this.selector.displayField;

        criteria[OB.Constants.TEXT_MATCH_PARAMETER_OVERRIDE] = this.selector.popupTextMatchStyle;

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
            this.selectSingleRecord(this.data.find(this.selector.valueField, this.selector.openbravoField.value));
          } else {
            this.selectSingleRecord(null);
          }
        }
      },
      fields: this.selectorGridFields,
      recordDoubleClick: this.setSelectorValueFromGrid
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
      items: [this.selectorGrid, isc.HLayout.create({
        styleName: isc.OBSelectorWidget.styling.modalPopupButtonGroupStyle,
        height: isc.OBSelectorWidget.styling.modalPopupButtonGroupHeight,
        defaultLayoutAlign: isc.OBSelectorWidget.styling.modalPopupButtonGroupAlign,
        members: [isc.LayoutSpacer.create({}), isc.IButton.create({
          selector: this,
          title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
          endRow: false,
          startRow: false,
          align: isc.OBSelectorWidget.styling.modalPopupOkButtonAlign,
          width: isc.OBSelectorWidget.styling.modalPopupOkButtonWidth,
          icon: isc.OBSelectorWidget.styling.modalPopupOkButtonSrc,
          click: this.setSelectorValueFromGrid
        }), isc.LayoutSpacer.create({
          width: isc.OBSelectorWidget.styling.modalPopupButtonSeparatorWidth
        }), isc.IButton.create({
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
        }), isc.LayoutSpacer.create({})]
      })]
    });

    this.afterDrawDoLoop = window.setInterval(function () {
      if (thisSelector.isDrawn() === true) {
        thisSelector.afterDrawDo();
        window.clearInterval(thisSelector.afterDrawDoLoop);
      }
    }, 10);
  }
});