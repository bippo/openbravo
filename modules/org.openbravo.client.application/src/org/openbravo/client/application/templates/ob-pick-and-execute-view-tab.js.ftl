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
<@createView data/>      

<#macro createView tabComponent>    
    tabTitle: '${tabComponent.tabTitle?js_string}',
    entity:  '${tabComponent.entityName}',
    
    <#if tabComponent.parentProperty != ''>
        parentProperty: '${tabComponent.parentProperty?js_string}',
    </#if>
    <#if tabComponent.tabSet>
        tabId: '${tabComponent.tabId}',
        moduleId: '${tabComponent.moduleId}',
    </#if>
    
    <#if tabComponent.defaultEditMode>
    defaultEditMode: ${tabComponent.defaultEditMode?string},
    </#if> 
    mapping250: '${tabComponent.mapping250?js_string}',
    <#if tabComponent.acctTab>
    isAcctTab: ${tabComponent.acctTab?string},
    </#if> 
    <#if tabComponent.trlTab>
    isTrlTab: ${tabComponent.trlTab?string},
    </#if>
    <#if tabComponent.allowAdd>
    allowAdd: true,
    </#if>
    <#if tabComponent.allowDelete>
    allowDelete: true,
    </#if>
    showSelect: ${tabComponent.showSelect?string},
    <#if tabComponent.newFunction != ''>
    newFn: ${tabComponent.newFunction},
    </#if>
    
    standardProperties:{
<@compress single_line=true>
      inpTabId: '${tabComponent.tabId}',
      inpwindowId: '${tabComponent.windowId}',
      inpTableId: '${tabComponent.tableId?js_string}',
      inpkeyColumnId: '${tabComponent.keyProperty.columnId?js_string}',
      keyProperty: '${tabComponent.keyProperty.name?js_string}',
      inpKeyName: '${tabComponent.keyInpName?js_string}',
      keyColumnName: '${tabComponent.keyColumnName?js_string}',
      keyPropertyType: '${tabComponent.keyPropertyType?js_string}'      
</@compress>
    },


    fields: [
    <#list tabComponent.fieldHandler.fields as field>
      <@createField field/><#if field_has_next>,</#if>
    </#list>    
    ],
    
    statusBarFields: [
<@compress single_line=true>
    <#list tabComponent.fieldHandler.statusBarFields as sbf>
      '${sbf?js_string}'<#if sbf_has_next>,</#if>
    </#list>
</@compress>
    ],

    <#if tabComponent.selectionFunction != "">
    selectionFn: ${tabComponent.selectionFunction},
    </#if>

    <#if tabComponent.removeFunction != "">
    removeFn: ${tabComponent.removeFunction},
    </#if>

    gridProperties: ${tabComponent.viewGrid},

    dataSource: ${tabComponent.dataSourceJavaScript}
</#macro>