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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

Note, the below template contains many if statements to minimize the output if
the outputted value is already covered by a default.
-->

<#macro createField field>
{
<@compress single_line=true>
    name: '${field.name?js_string}',
    <#if field.label != ''>
        title: '${field.label?js_string}',
    </#if>
    <#if field.required>
        required: ${field.required?string},
    </#if>
    <#if field.readOnly>
        disabled: true,
    </#if>
    <#if !field.updatable>
        updatable: false,
    </#if>
    <#if field.onChangeFunction??>
        onChangeFunction: ${field.onChangeFunction?js_string},
    </#if>
    <#if field.sessionProperty>
        sessionProperty: true,
    </#if>
    <#if field.parentProperty>
        parentProperty: true,
    </#if>
    <#if field.showColSpan>
        colSpan: ${field.colSpan},
    </#if>
    <#if field.rowSpan != 1>
        rowSpan: ${field.rowSpan},
    </#if>
    <#if field.showStartRow>
        startRow: ${field.startRow?string},
    </#if>
    <#if field.showEndRow>
        endRow: ${field.endRow?string},
    </#if>
    <#if !field.personalizable>
        personalizable: false,
    </#if>
    <#if field.hasDefaultValue>
        hasDefaultValue: true,
    </#if>
</@compress>
    <#if field.standardField>
<@compress single_line=true>
        <#if field.clientClass != ''>
            clientClass: '${field.clientClass?string}',
        </#if>
        <#if field.columnName != ''>
            columnName: '${field.columnName?string}',
        </#if>
        <#if field.inpColumnName != ''>
            inpColumnName: '${field.inpColumnName?string}',
        </#if>
        <#if field.referencedKeyColumnName != ''>
            refColumnName: '${field.referencedKeyColumnName?string}',
        </#if>
        <#if field.targetEntity != ''>
            targetEntity: '${field.targetEntity?string}',
        </#if>
        <#if !field.displayed>
            displayed: false,
        </#if>
        <#if field.redrawOnChange && field.displayed>
            redrawOnChange: true,
        </#if>
        <#if field.showIf != "" && field.displayed>
            showIf: function(item, value, form, currentValues, context) {
                return (${field.showIf});          
            },          
        </#if>
        <#if field.searchField>
            displayField: '${field.name?js_string}._identifier',
            <#if field.parentProperty>
                showPickerIcon: ${(!field.parentProperty)?string},
            </#if>
        </#if>
        <#if field.firstFocusedField>
            firstFocusedField: true,
        </#if>
        <#if field.validationFunction != "">
            validationFn: ${field.validationFunction},
        </#if>
        <#if field.showSummary>
            showGridSummary: true,
            <#if field.summaryFunction != "">
              summaryFunction: ${field.summaryFunction},
            <#else>
              summaryFunction: 'sum',
            </#if>
        </#if>
</@compress>
    </#if>
    <#if field.type = "OBSectionItem" || field.type = "OBNoteSectionItem" || field.type = "OBLinkedItemSectionItem"  || field.type = "OBAttachmentsSectionItem" || field.type = "OBAuditSectionItem">
<@compress single_line=true>
        <#if !field.displayed>
            displayed: false,
        </#if>
        <#if field.expanded>
            sectionExpanded: ${field.expanded?string},
        </#if>
        <#if field.label != ''>
            defaultValue: '${field.label?js_string}',
        </#if>
        <#if field.hasChildren>
            itemIds: [
            <#list field.children as childField>
            '${childField.name?js_string}'<#if childField_has_next>,</#if>
            </#list>
            ],
        </#if>
</@compress>
    </#if>
    ${field.fieldProperties}
    <#if field.isGridProperty>
<@compress single_line=true>
        gridProps: {
            sort: ${field.gridSort?string}
            <#if field.autoExpand>
                , autoExpand: ${field.autoExpand?string}
            </#if>
            <#if field.cellAlign??>
                , cellAlign: '${field.cellAlign?js_string}'
            </#if>
            <#if !field.showInitiallyInGrid>
                , showIf: '${field.showInitiallyInGrid?string}'
            </#if>            
            <#if field.gridEditorFieldProperties != "">
                , editorProps: {
                  ${field.gridEditorFieldProperties}
                }
            </#if>
            ${field.gridFieldProperties}
            ${field.filterEditorProperties}
        },
</@compress>
    </#if>
    type: '${field.type}'
}
</#macro>