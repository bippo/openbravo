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
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
// = OBAboutPopup =
//
// Implements the About Popup
//
isc.defineClass('OBAboutPopupWindow', isc.OBPopup).addProperties({
  canDragReposition: true,
  canDragResize: true,
  dismissOnEscape: true,
  showMaximizeButton: false,
  showMinimizeButton: false,
  autoCenter: true,
  isModal: true,
  showModalMask: true,
  autoDraw: false,

  aboutFieldDefinitions: [],

  initWidget: function (args) {

    var aboutFieldDefinitions = args.aboutFieldDefinitions;
    this.aboutPopupWindowLayout = this.createPopupWindowLayout();
    this.items = [this.aboutPopupWindowLayout];

    this.Super('initWidget', arguments);
  },

  createPopupWindowLayout: function () {
    var verticalLayout, htmlContentsWidget, htmlContentsModule, urlRegex, url, theSections, htmlFlowWidget, htmlFlowModule;

    verticalLayout = isc.VLayout.create({
      defaultLayoutAlign: 'center',
      overflow: 'visible',
      height: 500,
      width: '100%'
    });

    if (this.aboutFieldDefinitions.authorUrl) {
      urlRegex = /((\http?:\/\/)|(\https?:\/\/))/;
      url = this.aboutFieldDefinitions.authorUrl.match(urlRegex);
      if (!url) {
        this.aboutFieldDefinitions.authorUrl = 'http://' + this.aboutFieldDefinitions.authorUrl;
      }
    }

    htmlContentsWidget = '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Widget_Title") + '</td>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Widget_Superclass") + '</td>  ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.title + '</td> ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.superclassTitle + '</td> ' + '</tr> ';
    htmlContentsWidget += '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Author_Message") + '</td>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Data_Access_Level") + '</td>  ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.authorMsg + '</td> ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.dataAccessLevel + '</td> ' + '</tr> ';
    htmlContentsWidget += '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Author_URL") + '</td>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Enabled_For_All_Users") + '</td> ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1"><a href=' + this.aboutFieldDefinitions.authorUrl + ' target="_blank">' + this.aboutFieldDefinitions.authorUrl + '</a></td> ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.enabledAllUsers + '</td> ' + '</tr> ';
    htmlContentsWidget += '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Widget_Description") + '</td> ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.description + '</td> ' + '</tr> ';

    htmlContentsModule = '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Module_Name") + '</td>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_License_Type") + '</td>  ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleName + '</td> ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleLicenseType + '</td> ' + '</tr> ';
    htmlContentsModule += '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Module_Version") + '</td>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Update_Information") + '</td>  ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleVersion + '</td> ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleUpdateInfo + '</td> ' + '</tr> ';
    htmlContentsModule += '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Maturity_Status") + '</td>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_License_Text") + '</td>  ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1" style="vertical-align:top">' + this.aboutFieldDefinitions.moduleStatus + '</td> ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleLicenseText + '</td> ' + '</tr> ';
    htmlContentsModule += '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Java_Package") + '</td>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Author") + '</td>  ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleJavaPackage + '</td> ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleAuthor + '</td> ' + '</tr> ';
    htmlContentsModule += '<tr>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_Type") + '</td>  ' + '<td class="OBWidgetAboutLabels" colspan="1">' + OB.I18N.getLabel("OBKMO_DB_Prefix") + '</td>  ' + '</tr> ' + '<tr>  ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleType + '</td> ' + '<td class="OBWidgetAboutFields" colspan="1">' + this.aboutFieldDefinitions.moduleDBPrefix + '</td> ' + '</tr> ';


    htmlFlowWidget = isc.HTMLFlow.create({
      contents: '<html> ' + ' <table width="100%" style="table-layout:fixed"> ' + ' <col width="50%"> ' + ' <col width="50%"> ' + '  <body> ' + htmlContentsWidget + ' </body> ' + ' </table> ' + '</html> '
    });

    htmlFlowModule = isc.HTMLFlow.create({
      overflow: 'auto',
      contents: '<html> ' + ' <table width="100%" style="table-layout:fixed"> ' + ' <col width="50%"> ' + ' <col width="50%""> ' + '  <body> ' + htmlContentsModule + ' </body> ' + ' </table> ' + '</html> '
    });

    theSections = isc.SectionStack.create({
      visibilityMode: 'multiple'
    });

    isc.SectionStack.addProperties({
      sectionHeaderClass: 'OBSectionItemButton',
      headerHeight: 22
    });

    theSections.addSection({
      title: OB.I18N.getLabel('OBKMO_WidgetSection'),
      expanded: true,
      items: [htmlFlowWidget]
    });

    theSections.addSection({
      title: OB.I18N.getLabel('OBKMO_ParentModuleSection'),
      expanded: false,
      items: [htmlFlowModule]
    });

    verticalLayout.addMember(theSections);

    return verticalLayout;
  }
});