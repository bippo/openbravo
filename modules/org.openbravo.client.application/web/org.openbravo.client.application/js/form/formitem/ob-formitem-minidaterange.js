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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.RelativeDateItem.addProperties({
  showChooserIcon: false,
  timeUnitOptions: ['day', 'week', 'month', 'quarter', 'year'],
  todayTitle: OB.I18N.getLabel('OBUISC_DateChooser.todayButtonTitle'),

  millisecondsAgoTitle: OB.I18N.getLabel('OBUIAPP_milliseconds_ago'),
  secondsAgoTitle: OB.I18N.getLabel('OBUIAPP_seconds_ago'),
  minutesAgoTitle: OB.I18N.getLabel('OBUIAPP_minutes_ago'),
  hoursAgoTitle: OB.I18N.getLabel('OBUIAPP_hours_ago'),
  daysAgoTitle: OB.I18N.getLabel('OBUIAPP_days_ago'),
  weeksAgoTitle: OB.I18N.getLabel('OBUIAPP_weeks_ago'),
  monthsAgoTitle: OB.I18N.getLabel('OBUIAPP_months_ago'),
  quartersAgoTitle: OB.I18N.getLabel('OBUIAPP_quarters_ago'),
  yearsAgoTitle: OB.I18N.getLabel('OBUIAPP_years_ago'),

  millisecondsFromNowTitle: OB.I18N.getLabel('OBUIAPP_milliseconds_from_now'),
  secondsFromNowTitle: OB.I18N.getLabel('OBUIAPP_seconds_from_now'),
  minutesFromNowTitle: OB.I18N.getLabel('OBUIAPP_minutes_from_now'),
  hoursFromNowTitle: OB.I18N.getLabel('OBUIAPP_hours_from_now'),
  daysFromNowTitle: OB.I18N.getLabel('OBUIAPP_days_from_now'),
  weeksFromNowTitle: OB.I18N.getLabel('OBUIAPP_weeks_from_now'),
  monthsFromNowTitle: OB.I18N.getLabel('OBUIAPP_months_from_now'),
  quartersFromNowTitle: OB.I18N.getLabel('OBUIAPP_quarters_from_now'),
  yearsFromNowTitle: OB.I18N.getLabel('OBUIAPP_years_from_now'),

  presetOptions: {
    "$today": OB.I18N.getLabel('OBUISC_DateChooser.todayButtonTitle'),
    "$yesterday": OB.I18N.getLabel('OBUIAPP_Yesterday'),
    "$tomorrow": OB.I18N.getLabel('OBUIAPP_Tomorrow'),
    "-1w": OB.I18N.getLabel('OBUIAPP_Current_day_of_last_week'),
    "+1w": OB.I18N.getLabel('OBUIAPP_Current_day_of_next_week'),
    "-1m": OB.I18N.getLabel('OBUIAPP_Current_day_of_last_month'),
    "+1m": OB.I18N.getLabel('OBUIAPP_Current_day_of_next_month')
  }
});

isc.RelativeDateItem.changeDefaults('quantityFieldDefaults', {
  // max 1000 days/months in the past/future
  max: 1000,
  alwaysTakeSpace: false,

  // after leaving the quantity field the next time the rangeitem is visited the 
  // focus should go to the value field again
  blur: function () {
    if (this.form && this.form._isRedrawing) {
      return;
    }

    this.Super('blur', arguments);
    this.form.setFocusItem(this.form.getItem('valueField'));
  }
});

isc.RelativeDateItem.changeDefaults('valueFieldDefaults', {
  keyPress: function (item, form, keyName, characterValue) {
    if (keyName === 'Enter' && !isc.EventHandler.ctrlKeyDown() && !isc.EventHandler.altKeyDown() && !isc.EventHandler.shiftKeyDown()) {
      // canvasItem is the rangeItem
      form.canvasItem.showPicker();
      return false;
    }
    return true;
  },

  init: function () {
    this.icons = [{
      width: this.calendarIconWidth,
      height: this.calendarIconHeight,
      hspace: this.calendarIconHspace,
      canFocus: false,
      showFocused: false,
      item: this,
      src: this.calendarIconSrc,
      click: function () {
        this.item.form.canvasItem.showPicker();
      }
    }];
    this.Super('init', arguments);
  }
});

// == OBMiniDateRangeItem ==
// OBMiniDateRangeItem inherits from SmartClient MiniDateRangeItem
// Is used for filtering dates in the grid. Contains the following classes:
// - OBDateRangeDialog: the popup
// - OBMiniDateRangeItem: the filter item itself
isc.ClassFactory.defineClass('OBDateRangeDialog', isc.DateRangeDialog);

isc.OBDateRangeDialog.addProperties({
  initWidget: function () {
    this.Super('initWidget', arguments);
    this.rangeForm.setFocusItem(this.rangeItem);
  },

  show: function () {
    this.Super('show', arguments);
    this.rangeForm.items[0].fromField.calculatedDateField.canFocus = false;
    this.rangeForm.items[0].toField.calculatedDateField.canFocus = false;
    this.rangeForm.items[0].fromField.valueField.focusInItem();
    this.rangeForm.focus();
  },

  // trick: overridden to let the ok and clear button change places
  addAutoChild: function (name, props) {
    if (name === 'okButton') {
      return this.Super('addAutoChild', ['clearButton',
      {
        canFocus: true,
        title: this.clearButtonTitle
      }]);
    } else if (name === 'clearButton') {
      return this.Super('addAutoChild', ['okButton',
      {
        canFocus: true,
        title: this.okButtonTitle
      }]);
    } else {
      return this.Super('addAutoChild', arguments);
    }
  }

});


// == OBMinDateRangeItem ==
// Item used for filtering by dates in the grid. Replaces the normal Smartclient
// MiniDateRangeItem to make it editable.
isc.ClassFactory.defineClass('OBMiniDateRangeItem', isc.OBTextItem);

isc.OBMiniDateRangeItem.addProperties(OB.DateItemProperties, {
  validateOnExit: false,
  showPickerIcon: false,
  filterOnKeypress: false,
  operator: 'equals',
  // prevents date formatting using the simple type formatters
  applyStaticTypeFormat: true,

  // note this one needs to be set to let the formatDate be called below
  dateDisplayFormat: OB.Format.date,
  rangeDialogConstructor: isc.OBDateRangeDialog,

  textBoxStyle: 'textItem',
  shouldSaveValue: true,
  rangeDialogDefaults: {
    _constructor: 'DateRangeDialog',
    autoDraw: false,
    destroyOnClose: false,
    clear: function () {
      if (this.destroying) {
        return;
      }
      this.Super('clear', arguments);
    }
  },
  fromDateOnlyPrefix: OB.I18N.getLabel('OBUIAPP_fromDateOnlyPrefix'),
  toDateOnlyPrefix: OB.I18N.getLabel('OBUIAPP_toDateOnlyPrefix'),
  pickerIconPrompt: OB.I18N.getLabel('OBUIAPP_pickerIconPrompt'),
  iconVAlign: 'center',
  pickerIconDefaults: {
    name: 'showDateRange',
    src: '[SKIN]/DynamicForm/DatePicker_icon.gif',
    width: 16,
    height: 16,
    showOver: false,
    showFocused: false,
    showFocusedWithItem: false,
    hspace: 0,
    click: function (form, item, icon) {
      if (!item.disabled) {
        item.showRangeDialog();
      }
    }
  },

  allowRelativeDates: true,

  // if the user enters a date directly
  singleDateMode: false,
  singleDateValue: null,
  singleDateDisplayValue: null,

  init: function () {
    this.addAutoChild('rangeDialog', {
      fromDate: this.fromDate,
      toDate: this.toDate,
      rangeItemProperties: {
        allowRelativeDates: this.allowRelativeDates
      },
      dateDisplayFormat: this.dateDisplayFormat,
      callback: this.getID() + '.rangeDialogCallback(value)'
    });

    this.icons = [isc.addProperties({
      prompt: this.pickerIconPrompt
    }, this.pickerIconDefaults, this.pickerIconProperties)];

    this.rangeItem = this.rangeDialog.rangeItem;
    this.rangeItem.name = this.name;

    // this call super.init
    if (this.doInit) {
      this.doInit();
    }
  },

  blurValue: function () {
    return this.getElementValue();
  },

  expandSingleValue: function () {
    var newValue = this.parseValue(),
        oldValue = this.mapValueToDisplay(),
        dateValue, editRow;

    if (!this.singleDateMode) {
      return;
    }

    if (newValue === oldValue) {
      return false;
    }

    if (this.singleDateMode) {
      dateValue = OB.Utilities.Date.OBToJS(newValue, this.dateFormat);
      if (isc.isA.Date(dateValue)) {
        this.singleDateValue = dateValue;
        this.singleDateDisplayValue = newValue;
        this.singleDateMode = true;
        this.setElementValue(newValue, newValue);
      } else {
        this.singleDateValue = null;
        this.singleDateMode = false;
      }
      return true;
    }
    return false;
  },

  clearDateValues: function () {
    this.singleDateValue = null;
    this.singleDateDisplayValue = '';
    this.singleDateMode = true;
    this.rangeItemValue = null;
    this.rangeItem.setValue(null);
    this.setElementValue('', '');
  },

  setSingleDateValue: function (value) {
    var displayValue = OB.Utilities.Date.JSToOB(value, this.dateFormat);
    this.singleDateValue = value;
    this.singleDateDisplayValue = displayValue;
    this.singleDateMode = true;
    this.setElementValue(displayValue, displayValue);
  },

  blur: function () {
    if (this.form && this.form._isRedrawing) {
      return;
    }

    if (this.expandSingleValue()) {
      this.form.grid.performAction();
    }
    return this.Super('blur', arguments);
  },

  showRangeDialog: function () {
    if (!this.rangeItemValue) {
      this.rangeDialog.clear();
      this.rangeItem.fromField.setValue(null);
      this.rangeItem.fromField.quantityField.hide();
      this.rangeItem.toField.setValue(null);
      this.rangeItem.toField.quantityField.hide();
    }
    this.rangeDialog.show();
  },

  rangeDialogCallback: function (value) {
    var data = value,
        illegalStart = data && data.start && !this.isCorrectRangeValue(data.start);
    var illegalEnd = data && data.end && !this.isCorrectRangeValue(data.end);
    if (illegalStart || illegalEnd) {
      return;
    }
    this.singleDateMode = false;
    this.singleDateValue = null;
    this.rangeItemValue = value;
    this.displayValue();
    this.form.grid.performAction();
  },

  hasAdvancedCriteria: function () {
    return this.singleDateMode || (this.rangeItem !== null && this.rangeItem.hasAdvancedCriteria());
  },

  setCriterion: function (criterion) {
    if (!criterion) {
      return;
    }

    if (criterion.operator === 'isNull') {
      this.setValue('#');
      return;
    }

    if (criterion.operator === 'notNull') {
      this.setValue('!#');
      return;
    }

    if (criterion.operator === 'equals') {
      this.setSingleDateValue(criterion.value);
      return;
    }

    if (this.rangeItem) {
      this.rangeItem.setCriterion(criterion);
      this.singleDateMode = false;
      this.singleDateValue = null;
      this.rangeItemValue = this.rangeItem.getValue();
      this.displayValue();
    }
  },

  getCriterion: function () {
    var value = this.blurValue();
    if (value === '#') {
      return {
        fieldName: this.name,
        operator: 'isNull'
      };
    }
    if (value === '!#') {
      return {
        fieldName: this.name,
        operator: 'notNull'
      };
    }
    if (this.singleDateValue) {
      return {
        fieldName: this.name,
        operator: 'equals',
        value: this.singleDateValue
      };
    }
    var criteria = this.rangeItem ? this.rangeItem.getCriterion() : null;
    return criteria;
  },

  canEditCriterion: function (criterion) {

    if (criterion.fieldName === this.name && (criterion.operator === 'isNull' || criterion.operator === 'notNull')) {
      return true;
    }
    if (this.singleDateMode && criterion.fieldName === this.name) {
      return true;
    }
    return this.rangeItem ? this.rangeItem.canEditCriterion(criterion) : false;
  },

  itemHoverHTML: function (item, form) {
    return this.mapValueToDisplay();
  },

  updateStoredDates: function () {
    var value = this.rangeItemValue,
        i, newValue, length;

    if (value) {
      if (isc.DataSource.isAdvancedCriteria(value)) {
        // value has come back as an AdvancedCriteria!
        newValue = {};
        length = value.criteria.length;

        for (i = 0; i < length; i++) {
          var criterion = value.criteria[i];
          if (criterion.operator === 'greaterThan' || criterion.operator === 'greaterOrEqual') {
            newValue.start = criterion.value;
          } else if (criterion.operator === 'lessThan' || criterion.operator === 'lessOrEqual') {
            newValue.end = criterion.value;
          }
        }
        value = newValue;
      }

      this.fromDate = value.start;
      this.toDate = value.end;
    } else {
      this.fromDate = null;
      this.toDate = null;
    }
  },

  displayValue: function (value) {
    var displayValue = this.mapValueToDisplay(value) || '';
    this.setElementValue(displayValue, value);
  },

  setElementValue: function () {
    return this.Super('setElementValue', arguments);
  },

  mapDisplayToValue: function (display) {
    return display;
  },

  mapValueToDisplay: function (value) {
    if (this.singleDateMode) {
      if (this.singleDateDisplayValue) {
        return this.singleDateDisplayValue;
      }
    }
    if (!this.rangeItemValue) {
      if (!value) {
        return '';
      }
      return value;
    }
    value = this.rangeItemValue;
    var fromDate = value.start,
        toDate = value.end,
        RDI = isc.RelativeDateItem,
        start = (RDI.isRelativeDate(fromDate) ? RDI.getAbsoluteDate(fromDate.value, null, null, 'start') : fromDate),
        end = (RDI.isRelativeDate(toDate) ? RDI.getAbsoluteDate(toDate.value, null, null, 'end') : toDate);

    var prompt;
    if (start || end) {
      if (this.dateDisplayFormat) {
        if (start) {
          prompt = this.formatDate(start);
        }
        if (end) {
          if (prompt) {
            prompt += ' - ' + this.formatDate(end);
          } else {
            prompt = this.formatDate(end);
          }
        }
      } else {
        prompt = Date.getFormattedDateRangeString(start, end);
      }
      if (!start) {
        prompt = this.toDateOnlyPrefix + ' ' + prompt;
      } else if (!end) {
        prompt = this.fromDateOnlyPrefix + ' ' + prompt;
      }
    }
    this.prompt = prompt || '';
    return this.prompt;
  },

  getCriteriaValue: function () {
    return this.getCriterion();
  },

  isCorrectRangeValue: function (value) {
    if (!value) {
      return false;
    }
    if (isc.isA.Date(value)) {
      return true;
    }
    if (value._constructor && value._constructor === 'RelativeDate') {
      return true;
    }
    return false;
  },

  keyPress: function (item, form, keyName, characterValue) {
    if (keyName === 'Enter') {
      if (this.singleDateMode) {
        this.expandSingleValue();
        this.form.grid.performAction();
        return false;
      }
      this.showRangeDialog();
      return false;
    } else if (characterValue || keyName === 'Backspace' || keyName === 'Delete') {
      // only do this if something got really typed in
      this.fromDate = null;
      this.toDate = null;

      // typing, change to single date mode
      this.singleDateMode = true;
      this.singleDateValue = null;
      this.rangeItemValue = null;
      // typing a new value
      this.singleDateDisplayValue = null;
    }
    return true;
  },

  // Explicit destroy of the rangedialog as formitems don't have 
  // an auto delete of autochilds
  destroy: function () {
    this.destroying = true;
    if (this.rangeDialog) {
      this.rangeDialog.rangeForm.destroy();
      this.rangeDialog.mainLayout.destroy();
      this.rangeDialog.destroying = true;
      this.rangeDialog.destroy();
      this.rangeDialog.destroying = false;
    }
    this.Super('destroy', arguments);
    this.destroying = false;
  },

  clear: function () {
    if (this.destroying) {
      return;
    }
    this.Super('clear', arguments);
  },

  formatDate: function (dt) {
    return OB.Utilities.Date.JSToOB(dt, OB.Format.date);
  }
});