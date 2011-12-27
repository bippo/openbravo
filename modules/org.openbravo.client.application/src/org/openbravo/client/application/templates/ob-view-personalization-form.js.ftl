<#--
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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
-->
{
    fields: [
    <#list data.fieldHandler.fields as field>
      {
        name: '${field.name?js_string}',
        title: '${field.label?js_string}',
        type: '${field.type}',
        <#if field.clientClass != "">
            clientClass: '${field.clientClass}',
        </#if>
        <#if field.type = "text">
            editorType: 'OBTextItem',
        </#if>
        colSpan: ${field.colSpan},
        rowSpan: ${field.rowSpan},
        startRow: ${field.startRow?string},
        endRow: ${field.endRow?string},
        personalizable: ${field.personalizable?string},
        isPreviewFormItem: true,
        disabled: true,
        showDisabled: false,
        <#if !field.displayed>
        width: '',
        <#else>
        width: '*',
        </#if>
        <#if field.showIf != "" && field.displayed>
          hasShowIf: true,            
        </#if>
        <#if field.standardField>
            <#if !field.displayed>
                alwaysTakeSpace: false,
                displayed: false,
            </#if>
            required: ${field.required?string},
            hasDefaultValue: ${field.hasDefaultValue?string},
            <#if field.searchField>
                showPickerIcon: ${(!field.parentProperty)?string},
            </#if>
        </#if>
        <#if field.type = "OBSectionItem" || field.type = "OBNoteSectionItem" || field.type = "OBLinkedItemSectionItem"  || field.type = "OBAttachmentsSectionItem" || field.type = "OBAuditSectionItem">
          <#if !field.displayed>
          visible: false,
          </#if>
          <#if field.hasChildren>
          itemIds: [
            <#list field.children as childField>
                '${childField.name?js_string}'<#if childField_has_next>,</#if>
            </#list>
            ],
          </#if>
          defaultValue: '${field.label?js_string}'
        </#if>
    }
      <#if field_has_next>,</#if>
    </#list>    
    ],

    statusBarFields: [
    <#list data.fieldHandler.statusBarFields as sbf>
      '${sbf?js_string}'<#if sbf_has_next>,</#if>
    </#list>
    ]
}
