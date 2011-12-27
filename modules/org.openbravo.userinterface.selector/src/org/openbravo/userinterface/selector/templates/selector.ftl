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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
-->
<#if data.selectorItem>
    selectorDefinitionId: '${data.id}',
    popupTextMatchStyle: '${data.selector.popuptextmatchstyle}',
    textMatchStyle: '${data.selector.suggestiontextmatchstyle}',
    defaultPopupFilterField : '${data.defaultPopupFilterField}',
    displayField: '${data.displayField?js_string}',
    valueField: '${data.valueField?js_string}',
    pickListFields: [
    <#list data.pickListFields as pickListField>
<@compress single_line=true>
        {<#list pickListField.properties as property>
        ${property.name}: ${property.value}<#if property_has_next>,</#if>
         </#list>       
        }<#if pickListField_has_next>,</#if>
</@compress>
    </#list>
    ],
    showSelectorGrid: ${data.showSelectorGrid},
    selectorGridFields : [
    <#list data.selectorGridFields as selectorGridField>
<@compress single_line=true>
        {
        <#list selectorGridField.properties as property>
        ${property.name}: ${property.value}<#if property_has_next>,</#if>
         </#list>         
         ${selectorGridField.filterEditorProperties}         
        }<#if selectorGridField_has_next>,</#if>
</@compress>
    </#list>
    ],
    outFields : {
<@compress single_line=true>
    <#list data.outFields as selectorOutField>
    <#if selectorOutField.tabFieldName != "">
    '${selectorOutField.outFieldName}': {'fieldName':'${selectorOutField.tabFieldName}', 'suffix': '${selectorOutField.outSuffix}', 'formatType': '${selectorOutField.formatType}'}<#if selectorOutField_has_next>,</#if>
    </#if>
    </#list>
</@compress>
    },
    extraSearchFields: [${data.extraSearchFields}],
<#--
    // create the datasource in the init method, this
    // prevents too early creation, it is created when the
    // fields on the form are actually created
-->
    init: function() {
        this.optionDataSource = ${data.dataSourceJavascript};
        this.Super('init', arguments);
    },
    <#if data.whereClause != "">
        whereClause: '${data.whereClause?js_string}',
    </#if>
    <#if data.outHiddenInputPrefix != "">
        outHiddenInputPrefix: '${data.outHiddenInputPrefix}'
    </#if>
<#else>
/* jslint */
<#list data.hiddenInputs?keys as key>
document.write('<input type="hidden" value="" id="${key}" name="${key}" />');
</#list>
sc_${data.columnName} = isc.OBSelectorWidget.create({
    selectorDefinitionId: '${data.id}',
    popupTextMatchStyle: '${data.selector.popuptextmatchstyle}',
    suggestionTextMatchStyle: '${data.selector.suggestiontextmatchstyle}',
    openbravoField : document.getElementById("${data.columnName}"),
    outHiddenInputPrefix: '${data.outHiddenInputPrefix}',
    outHiddenInputs : {
      <#list data.hiddenInputs?keys as key>
      '${key}': ${data.hiddenInputs[key]}<#if key_has_next>,</#if>
      </#list>
    },
    defaultPopupFilterField : '${data.defaultPopupFilterField}',
    disabled: ${data.disabled},
    required: ${data.required},
    numCols : ${data.numCols},
    displayField: '${data.displayField?js_string}',
    valueField: '${data.valueField?js_string}',
    pickListFields: [
    <#list data.pickListFields as pickListField>
        {<#list pickListField.properties as property>
        ${property.name}: ${property.value}<#if property_has_next>,</#if>
         </#list>       
        }<#if pickListField_has_next>,</#if>
    </#list>
    ],
    showSelectorGrid: ${data.showSelectorGrid},
    selectorGridFields : [
    <#list data.selectorGridFields as selectorGridField>
        {<#list selectorGridField.properties as property>
        ${property.name}: ${property.value}<#if property_has_next>,</#if>
         </#list>
         ${selectorGridField.filterEditorProperties}         
        }<#if selectorGridField_has_next>,</#if>
    </#list>
    ],
    outFields : {
    <#list data.outFields as selectorOutField>
    <#if selectorOutField.tabFieldName != "">
    '${selectorOutField.outFieldName}': {'fieldName':'${selectorOutField.tabFieldName}', 'suffix': '${selectorOutField.outSuffix}'}<#if selectorOutField_has_next>,</#if>
    </#if>
    </#list>
    },
    extraSearchFields: [${data.extraSearchFields}],
    dataSource: ${data.dataSourceJavascript},
    <#if data.whereClause != "">
        whereClause: '${data.whereClause?js_string}',
    </#if>
    callOut: ${data.callOut},
    title : '${data.title}',
    comboReload: ${data.comboReload}
});
</#if>