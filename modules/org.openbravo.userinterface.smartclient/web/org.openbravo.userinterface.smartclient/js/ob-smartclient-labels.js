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
// jslint
/*
 * The code below sets all standard Smartclient system labels.
 * For more information see the 'Internationalization and Localization (i18n,l10n)'
 * section of the Smartclient reference.
 *
 * Note: Smartclient label properties can be class or instance properties. For instance
 * properties a call to addProperties needs to be done.
 */

// note different locales can have different starting day of the week
// new Date(2000, 0, 2).getDay() is a sunday, so start from there
(function (i18n, isc) {

  var getLabel = i18n.getLabel; // local reference for faster identifier resolution
  isc.Date.shortDayNames = [];
  isc.Date.shortDayNames[new Date(2000, 0, 2).getDay()] = getLabel('OBUISC_Date.shortDayNames.Sun');
  isc.Date.shortDayNames[new Date(2000, 0, 3).getDay()] = getLabel('OBUISC_Date.shortDayNames.Mon');
  isc.Date.shortDayNames[new Date(2000, 0, 4).getDay()] = getLabel('OBUISC_Date.shortDayNames.Tue');
  isc.Date.shortDayNames[new Date(2000, 0, 5).getDay()] = getLabel('OBUISC_Date.shortDayNames.Wed');
  isc.Date.shortDayNames[new Date(2000, 0, 6).getDay()] = getLabel('OBUISC_Date.shortDayNames.Thu');
  isc.Date.shortDayNames[new Date(2000, 0, 7).getDay()] = getLabel('OBUISC_Date.shortDayNames.Fri');
  isc.Date.shortDayNames[new Date(2000, 0, 8).getDay()] = getLabel('OBUISC_Date.shortDayNames.Sat');

  isc.Date.shortMonthNames = [
  getLabel('OBUISC_Date.shortMonthNames.Jan'), getLabel('OBUISC_Date.shortMonthNames.Feb'), getLabel('OBUISC_Date.shortMonthNames.Mar'), getLabel('OBUISC_Date.shortMonthNames.Apr'), getLabel('OBUISC_Date.shortMonthNames.May'), getLabel('OBUISC_Date.shortMonthNames.Jun'), getLabel('OBUISC_Date.shortMonthNames.Jul'), getLabel('OBUISC_Date.shortMonthNames.Aug'), getLabel('OBUISC_Date.shortMonthNames.Sep'), getLabel('OBUISC_Date.shortMonthNames.Oct'), getLabel('OBUISC_Date.shortMonthNames.Nov'), getLabel('OBUISC_Date.shortMonthNames.Dec')];

  isc.Dialog.OK_BUTTON_TITLE = getLabel('OBUISC_Dialog.OK_BUTTON_TITLE');
  isc.Dialog.APPLY_BUTTON_TITLE = getLabel('OBUISC_Dialog.APPLY_BUTTON_TITLE');
  isc.Dialog.YES_BUTTON_TITLE = getLabel('OBUISC_Dialog.YES_BUTTON_TITLE');
  isc.Dialog.NO_BUTTON_TITLE = getLabel('OBUISC_Dialog.NO_BUTTON_TITLE');
  isc.Dialog.CANCEL_BUTTON_TITLE = getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE');
  isc.Dialog.DONE_BUTTON_TITLE = getLabel('OBUISC_Dialog.DONE_BUTTON_TITLE');
  isc.Dialog.CONFIRM_TITLE = getLabel('OBUISC_Dialog.CONFIRM_BUTTON_TITLE');
  isc.Dialog.SAY_TITLE = getLabel('OBUISC_Dialog.SAY_BUTTON_TITLE');
  isc.Dialog.WARN_TITLE = getLabel('OBUISC_Dialog.WARN_BUTTON_TITLE');
  isc.Dialog.ASK_TITLE = getLabel('OBUISC_Dialog.ASK_TITLE');
  isc.Dialog.ASK_FOR_VALUE_TITLE = getLabel('OBUISC_Dialog.ASK_FOR_VALUE_TITLE');
  isc.Dialog.LOGIN_TITLE = getLabel('OBUISC_Dialog.LOGIN_TITLE');
  isc.Dialog.USERNAME_TITLE = getLabel('OBUISC_Dialog.USERNAME_TITLE');
  isc.Dialog.PASSWORD_TITLE = getLabel('OBUISC_Dialog.PASSWORD_TITLE');
  isc.Dialog.LOGIN_BUTTON_TITLE = getLabel('OBUISC_Dialog.LOGIN_BUTTON_TITLE');
  isc.Dialog.LOGIN_ERROR_MESSAGE = getLabel('OBUISC_Dialog.LOGIN_ERROR_MESSAGE');
  isc.RPCManager.defaultPrompt = getLabel('OBUISC_RPCManager.defaultPrompt');
  isc.RPCManager.timeoutErrorMessage = getLabel('OBUISC_RPCManager.timeoutErrorMessage');
  isc.RPCManager.removeDataPrompt = getLabel('OBUISC_RPCManager.removeDataPrompt');
  isc.RPCManager.saveDataPrompt = getLabel('OBUISC_RPCManager.saveDataPrompt');
  isc.RPCManager.fetchDataPrompt = getLabel('OBUISC_RPCManager.fetchDataPrompt');
  isc.Operators.equalsTitle = getLabel('OBUISC_Operators.equalsTitle');
  isc.Operators.notEqualTitle = getLabel('OBUISC_Operators.notEqualTitle');
  isc.Operators.greaterThanTitle = getLabel('OBUISC_Operators.greaterThanTitle');
  isc.Operators.lessThanTitle = getLabel('OBUISC_Operators.lessThanTitle');
  isc.Operators.greaterOrEqualTitle = getLabel('OBUISC_Operators.greaterOrEqualTitle');
  isc.Operators.lessOrEqualTitle = getLabel('OBUISC_Operators.lessOrEqualTitle');
  isc.Operators.betweenTitle = getLabel('OBUISC_Operators.betweenTitle');
  isc.Operators.betweenInclusiveTitle = getLabel('OBUISC_Operators.betweenInclusiveTitle');
  isc.Operators.iContainsTitle = getLabel('OBUISC_Operators.iContainsTitle');
  isc.Operators.iStartsWithTitle = getLabel('OBUISC_Operators.iStartsWithTitle');
  isc.Operators.iEndsWithTitle = getLabel('OBUISC_Operators.iEndsWithTitle');
  isc.Operators.containsTitle = getLabel('OBUISC_Operators.containsTitle');
  isc.Operators.startsWithTitle = getLabel('OBUISC_Operators.startsWithTitle');
  isc.Operators.endsWithTitle = getLabel('OBUISC_Operators.endsWithTitle');
  isc.Operators.iNotContainsTitle = getLabel('OBUISC_Operators.iNotContainsTitle');
  isc.Operators.iNotStartsWithTitle = getLabel('OBUISC_Operators.iNotStartsWithTitle');
  isc.Operators.iNotEndsWithTitle = getLabel('OBUISC_Operators.iNotEndsWithTitle');
  isc.Operators.notContainsTitle = getLabel('OBUISC_Operators.notContainsTitle');
  isc.Operators.notStartsWithTitle = getLabel('OBUISC_Operators.notStartsWithTitle');
  isc.Operators.notEndsWithTitle = getLabel('OBUISC_Operators.notEndsWithTitle');
  isc.Operators.isNullTitle = getLabel('OBUISC_Operators.isNullTitle');
  isc.Operators.notNullTitle = getLabel('OBUISC_Operators.notNullTitle');
  isc.Operators.regexpTitle = getLabel('OBUISC_Operators.regexpTitle');
  isc.Operators.iregexpTitle = getLabel('OBUISC_Operators.iregexpTitle');
  isc.Operators.inSetTitle = getLabel('OBUISC_Operators.inSetTitle');
  isc.Operators.notInSetTitle = getLabel('OBUISC_Operators.notInSetTitle');
  isc.Operators.equalsFieldTitle = getLabel('OBUISC_Operators.equalsFieldTitle');
  isc.Operators.notEqualFieldTitle = getLabel('OBUISC_Operators.notEqualFieldTitle');
  isc.Operators.andTitle = getLabel('OBUISC_Operators.andTitle');
  isc.Operators.notTitle = getLabel('OBUISC_Operators.notTitle');
  isc.Operators.orTitle = getLabel('OBUISC_Operators.orTitle');
  isc.GroupingMessages.upcomingTodayTitle = getLabel('OBUISC_GroupingMessages.upcomingTodayTitle');
  isc.GroupingMessages.upcomingTomorrowTitle = getLabel('OBUISC_GroupingMessages.upcomingTomorrowTitle');
  isc.GroupingMessages.upcomingThisWeekTitle = getLabel('OBUISC_GroupingMessages.upcomingThisWeekTitle');
  isc.GroupingMessages.upcomingNextWeekTitle = getLabel('OBUISC_GroupingMessages.upcomingNextWeekTitle');
  isc.GroupingMessages.upcomingNextMonthTitle = getLabel('OBUISC_GroupingMessages.upcomingNextMonthTitle');
  isc.GroupingMessages.upcomingBeforeTitle = getLabel('OBUISC_GroupingMessages.upcomingBeforeTitle');
  isc.GroupingMessages.upcomingLaterTitle = getLabel('OBUISC_GroupingMessages.upcomingLaterTitle');
  isc.GroupingMessages.byDayTitle = getLabel('OBUISC_GroupingMessages.byDayTitle');
  isc.GroupingMessages.byWeekTitle = getLabel('OBUISC_GroupingMessages.byWeekTitle');
  isc.GroupingMessages.byMonthTitle = getLabel('OBUISC_GroupingMessages.byMonthTitle');
  isc.GroupingMessages.byQuarterTitle = getLabel('OBUISC_GroupingMessages.byQuarterTitle');
  isc.GroupingMessages.byYearTitle = getLabel('OBUISC_GroupingMessages.byYearTitle');
  isc.GroupingMessages.byDayOfMonthTitle = getLabel('OBUISC_GroupingMessages.byDayOfMonthTitle');
  isc.GroupingMessages.byUpcomingTitle = getLabel('OBUISC_GroupingMessages.byUpcomingTitle');
  isc.GroupingMessages.byHoursTitle = getLabel('OBUISC_GroupingMessages.byHoursTitle');
  isc.GroupingMessages.byMinutesTitle = getLabel('OBUISC_GroupingMessages.byMinutesTitle');
  isc.GroupingMessages.bySecondsTitle = getLabel('OBUISC_GroupingMessages.bySecondsTitle');
  isc.GroupingMessages.byMilisecondsTitle = getLabel('OBUISC_GroupingMessages.byMilisecondsTitle');
  isc.Validator.notABoolean = getLabel('OBUISC_Validator.notABoolean');
  isc.Validator.notAString = getLabel('OBUISC_Validator.notAString');
  isc.Validator.notAnInteger = getLabel('OBUISC_Validator.notAnInteger');
  isc.Validator.notADecimal = getLabel('OBUISC_Validator.notADecimal');
  isc.Validator.notADate = getLabel('OBUISC_Validator.notADate');
  isc.Validator.mustBeLessThan = getLabel('OBUISC_Validator.mustBeLessThan');
  isc.Validator.mustBeGreaterThan = getLabel('OBUISC_Validator.mustBeGreaterThan');
  isc.Validator.mustBeLaterThan = getLabel('OBUISC_Validator.mustBeLaterThan');
  isc.Validator.mustBeEarlierThan = getLabel('OBUISC_Validator.mustBeEarlierThan');
  isc.Validator.mustBeShorterThan = getLabel('OBUISC_Validator.mustBeShorterThan');
  isc.Validator.mustBeLongerThan = getLabel('OBUISC_Validator.mustBeLongerThan');
  isc.Validator.mustBeExactLength = getLabel('OBUISC_Validator.mustBeExactLength');
  isc.Validator.requiredField = getLabel('OBUISC_Validator.requiredField');
  isc.Validator.notOneOf = getLabel('OBUISC_Validator.notOneOf');
  isc.Time.AMIndicator = getLabel('OBUISC_Time.AMIndicator');
  isc.Time.PMIndicator = getLabel('OBUISC_Time.PMIndicator');
  isc.Window.title = getLabel('OBUISC_Window.title');
  isc.FilterBuilder.removeButtonPrompt = getLabel('OBUISC_FilterBuilder.removeButtonPrompt');
  isc.FilterBuilder.addButtonPrompt = getLabel('OBUISC_FilterBuilder.addButtonPrompt');
  isc.FilterBuilder.rangeSeparator = getLabel('OBUISC_FilterBuilder.rangeSeparator');
  isc.FilterBuilder.subClauseButtonTitle = getLabel('OBUISC_FilterBuilder.subClauseButtonTitle');
  isc.FilterBuilder.subClauseButtonPrompt = getLabel('OBUISC_FilterBuilder.subClauseButtonPrompt');
  isc.Button.title = getLabel('OBUISC_Button.title');
  isc.DateChooser.todayButtonTitle = getLabel('OBUISC_DateChooser.todayButtonTitle');
  isc.DateChooser.cancelButtonTitle = getLabel('OBUISC_DateChooser.cancelButtonTitle');
  isc.DynamicForm.errorsPreamble = getLabel('OBUISC_DynamicForm.errorsPreamble');
  isc.DynamicForm.unknownErrorMessage = getLabel('OBUISC_DynamicForm.unknownErrorMessage');
  // the following two do not seem to exist
  // isc.SelectOtherItem.otherTitle = getLabel('OBUISC_SelectOtherItem.otherTitle');
  // isc.SelectOtherItem.selectOtherPrompt = getLabel('OBUISC_SelectOtherItem.selectOtherPrompt');
  isc.DateItem.invalidDateStringMessage = getLabel('OBUISC_DateItem.invalidDateStringMessage');
  isc.DateItem.pickerIconPrompt = getLabel('OBUISC_');
  isc.ValuesManager.unknownErrorMessage = getLabel('OBUISC_ValuesManager.unknownErrorMessage');
  isc.DataBoundComponent.addFormulaFieldText = getLabel('OBUISC_DataBoundComponent.addFormulaFieldText');
  isc.DataBoundComponent.editFormulaFieldText = getLabel('OBUISC_DataBoundComponent.editFormulaFieldText');
  isc.DataBoundComponent.addSummaryFieldText = getLabel('OBUISC_DataBoundComponent.addSummaryFieldText');
  isc.DataBoundComponent.editSummaryFieldText = getLabel('OBUISC_DataBoundComponent.editSummaryFieldText');
  isc.Selection.selectionRangeNotLoadedMessage = getLabel('OBUISC_Selection.selectionRangeNotLoadedMessage');
  isc.GridRenderer.emptyMessage = getLabel('OBUISC_GridRenderer.emptyMessage');

  isc.ListGrid.addProperties({
    addFormulaFieldText: getLabel('OBUISC_ListGrid.addFormulaFieldText')
  });
  isc.ListGrid.addProperties({
    editFormulaFieldText: getLabel('OBUISC_ListGrid.editFormulaFieldText')
  });
  isc.ListGrid.addProperties({
    removeFormulaFieldText: getLabel('OBUISC_ListGrid.removeFormulaFieldText')
  });
  isc.ListGrid.addProperties({
    addSummaryFieldText: getLabel('OBUISC_ListGrid.addSummaryFieldText')
  });
  isc.ListGrid.addProperties({
    editSummaryFieldText: getLabel('OBUISC_ListGrid.editSummaryFieldText')
  });
  isc.ListGrid.addProperties({
    removeSummaryFieldText: getLabel('OBUISC_ListGrid.removeSummaryFieldText')
  });
  isc.ListGrid.addProperties({
    emptyMessage: getLabel('OBUISC_ListGrid.emptyMessage')
  });
  isc.ListGrid.addProperties({
    loadingDataMessage: getLabel('OBUISC_ListGrid.loadingDataMessage')
  });
  isc.ListGrid.addProperties({
    loadingMessage: ''
  }); // empty string is fine see description in smartclient reference getLabel('OBUISC_ListGrid.loadingMessage')
  isc.ListGrid.addProperties({
    removeFieldTitle: getLabel('OBUISC_ListGrid.removeFieldTitle')
  });
  isc.ListGrid.addProperties({
    cancelEditingConfirmationMessage: getLabel('OBUISC_ListGrid.cancelEditingConfirmationMessage')
  });
  isc.ListGrid.addProperties({
    confirmDiscardEditsMessage: getLabel('OBUISC_ListGrid.confirmDiscardEditsMessage')
  });
  isc.ListGrid.addProperties({
    discardEditsSaveButtonTitle: getLabel('OBUISC_ListGrid.discardEditsSaveButtonTitle')
  });
  isc.ListGrid.addProperties({
    freezeOnRightText: getLabel('OBUISC_ListGrid.freezeOnRightText')
  });
  isc.ListGrid.addProperties({
    freezeOnLeftText: getLabel('OBUISC_ListGrid.freezeOnLeftText')
  });
  isc.ListGrid.addProperties({
    sortFieldAscendingText: getLabel('OBUISC_ListGrid.sortFieldAscendingText')
  });
  isc.ListGrid.addProperties({
    sortFieldDescendingText: getLabel('OBUISC_ListGrid.sortFieldDescendingText')
  });
  isc.ListGrid.addProperties({
    fieldVisibilitySubmenuTitle: getLabel('OBUISC_ListGrid.fieldVisibilitySubmenuTitle')
  });
  isc.ListGrid.addProperties({
    freezeFieldText: getLabel('OBUISC_ListGrid.freezeFieldText')
  });
  isc.ListGrid.addProperties({
    unfreezeFieldText: getLabel('OBUISC_ListGrid.unfreezeFieldText')
  });
  isc.ListGrid.addProperties({
    groupByText: getLabel('OBUISC_ListGrid.groupByText')
  });
  isc.ListGrid.addProperties({
    ungroupText: getLabel('OBUISC_ListGrid.ungroupText')
  });
  isc.ListGrid.addProperties({
    fieldVisibilitySubmenuTitle: getLabel('OBUISC_ListGrid.fieldVisibilitySubmenuTitle')
  });
  isc.ListGrid.addProperties({
    clearSortFieldText: getLabel('OBUISC_ListGrid.clearSortFieldText')
  });

  isc.TreeGrid.parentAlreadyContainsChildMessage = getLabel('OBUISC_TreeGrid.parentAlreadyContainsChildMessage');
  isc.TreeGrid.cantDragIntoSelfMessage = getLabel('OBUISC_TreeGrid.cantDragIntoSelfMessage');
  isc.TreeGrid.cantDragIntoChildMessage = getLabel('OBUISC_TreeGrid.cantDragIntoChildMessage');
  isc.MenuButton.title = getLabel('OBUISC_MenuButton.title');
  isc.FormulaBuilder.autoHideCheckBoxLabel = getLabel('OBUISC_FormulaBuilder.autoHideCheckBoxLabel');
  isc.FormulaBuilder.helpTextIntro = getLabel('OBUISC_FormulaBuilder.helpTextIntro');
  isc.FormulaBuilder.instructionsTextStart = getLabel('OBUISC_FormulaBuilder.instructionsTextStart');
  isc.FormulaBuilder.samplePrompt = getLabel('OBUISC_FormulaBuilder.samplePrompt');
  isc.SummaryBuilder.autoHideCheckBoxLabel = getLabel('OBUISC_SummaryBuilder.autoHideCheckBoxLabel');
  isc.SummaryBuilder.helpTextIntro = getLabel('OBUISC_SummaryBuilder.helpTextIntro');

  //isc.Calendar is not loaded as a default
  //isc.Calendar.invalidDateMessage = getLabel('OBUISC_Calendar.invalidDateMessage');
  //isc.Calendar.dayViewTitle = getLabel('OBUISC_Calendar.dayViewTitle');
  //isc.Calendar.weekViewTitle = getLabel('OBUISC_Calendar.weekViewTitle');
  //isc.Calendar.monthViewTitle = getLabel('OBUISC_Calendar.monthViewTitle');
  //isc.Calendar.timelineViewTitle = getLabel('OBUISC_Calendar.timelineViewTitle');
  //isc.Calendar.eventNameFieldTitle = getLabel('OBUISC_Calendar.eventNameFieldTitle');
  //isc.Calendar.saveButtonTitle = getLabel('OBUISC_Calendar.saveButtonTitle');
  //isc.Calendar.detailsButtonTitle = getLabel('OBUISC_Calendar.detailsButtonTitle');
  //isc.Calendar.cancelButtonTitle = getLabel('OBUISC_Calendar.cancelButtonTitle');
  //isc.Calendar.previousButtonHoverText = getLabel('OBUISC_Calendar.previousButtonHoverText');
  //isc.Calendar.nextButtonHoverText = getLabel('OBUISC_Calendar.nextButtonHoverText');
  //isc.Calendar.addEventButtonHoverText = getLabel('OBUISC_Calendar.addEventButtonHoverText');
  //isc.Calendar.datePickerHoverText = getLabel('OBUISC_Calendar.datePickerHoverText');
}(OB.I18N, isc));