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

// ** {{{Personalization Toolbar Buttons}}} **
// Registers buttons to open the form layout manager from a normal form/grid
// view and from the Window Personalization view.
(function () {
  var personalizationButtonProperties, windowPersonalizationTabButtonProperties;

  personalizationButtonProperties = {
    action: function () {
      var tabIdentifier, personalizeForm;

      if (!OB.Utilities.checkProfessionalLicense(
      OB.I18N.getLabel('OBUIAPP_ActivateMessagePersonalization'))) {
        return;
      }

      if (this.view === this.view.standardWindow.view) {
        tabIdentifier = this.view.tabTitle;
      } else {
        tabIdentifier = this.view.standardWindow.tabTitle + ' - ' + this.view.tabTitle;
      }

      personalizeForm = isc.OBPersonalizeFormLayout.create({
        form: this.view.viewForm,
        openedInForm: true,
        tabIdentifier: tabIdentifier,
        tabId: this.view.tabId
      });
      personalizeForm.doOpen();
    },
    disabled: false,
    buttonType: 'personalization',
    prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_Button'),
    updateState: function () {
      var propValue, undef;

      // set it 
      if (this.userWindowPersonalizationAllowed === undef) {
        propValue = OB.PropertyStore.get('OBUIAPP_WindowPersonalization_Override', this.view.standardWindow ? this.view.standardWindow.windowId : null);
        if (propValue === 'false' || propValue === 'N') {
          this.userWindowPersonalizationAllowed = false;
        } else {
          this.userWindowPersonalizationAllowed = true;
        }
      }
      if (!this.userWindowPersonalizationAllowed) {
        this.setDisabled(true);
        return;
      }

      this.show();
    },
    keyboardShortcutId: 'ToolBar_Personalization'
  };

  OB.ToolbarRegistry.registerButton(personalizationButtonProperties.buttonType, isc.OBToolbarIconButton, personalizationButtonProperties, 310, null);

  // and register the toolbar button the window personalization tab  
  windowPersonalizationTabButtonProperties = {
    action: function () {
      var personalizationData = {},
          personalizeForm, view = this.view,
          grid = view.viewGrid,
          record = grid.getSelectedRecord();
      if (record.value) {
        personalizationData = isc.JSON.decode(record.value);
      }
      personalizationData.personalizationId = record.id;
      personalizationData.canDelete = false;

      personalizeForm = isc.OBPersonalizeFormLayout.create({
        personalizationData: personalizationData,

        maintenanceView: view,
        openedFromMaintenanceWindow: true,

        tabId: record.tab,
        tabIdentifier: record['tab._identifier'],
        clientId: record.visibleAtClient,
        clientIdentifier: record['visibleAtClient._identifier'],
        orgId: record.visibleAtOrganization,
        orgIdentifier: record['visibleAtOrganization._identifier'],
        roleId: record.visibleAtRole,
        roleIdentifier: record['visibleAtRole._identifier'],
        userId: record.user,
        userIdentifier: record['user._identifier']
      });
      personalizeForm.doOpen();
    },
    disabled: false,
    buttonType: 'edit_personalization',
    prompt: OB.I18N.getLabel('OBUIAPP_Personalization_Toolbar_Edit_Button'),
    updateState: function () {
      var view = this.view,
          form = view.viewForm,
          grid = view.viewGrid,
          length, selectedRecords = grid.getSelectedRecords(),
          i;

      // only show for records which can be edited
      if (selectedRecords.length !== 1) {
        this.setDisabled(true);
        return;
      }

      if (selectedRecords[0].type && selectedRecords[0].type !== 'Form') {
        this.setDisabled(true);
        return;
      }

      length = selectedRecords.length;
      for (i = 0; i < length; i++) {
        if (!grid.isWritable(selectedRecords[i])) {
          this.setDisabled(true);
          return;
        }
        if (selectedRecords[i]._new) {
          this.setDisabled(true);
          return;
        }
      }
      if (view.isShowingForm) {
        this.setDisabled(form.isSaving || form.readOnly || view.singleRecord || !view.hasValidState() || form.isNew);
      } else {
        this.setDisabled(view.readOnly || view.singleRecord || !view.hasValidState() || !grid.getSelectedRecords() || grid.getSelectedRecords().length !== 1);
      }
    },
    keyboardShortcutId: 'ToolBar_Personalization_Edit'
  };

  // register only for the window personalization tab
  OB.ToolbarRegistry.registerButton(windowPersonalizationTabButtonProperties.buttonType, isc.OBToolbarIconButton, windowPersonalizationTabButtonProperties, 320, 'FF8081813157AED2013157BF6D810023');

}());