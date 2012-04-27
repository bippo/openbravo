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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
-->
<@createView data/>      

<#macro createView tabComponent>
    standardWindow: this.standardWindow,
        
    tabTitle: '${tabComponent.tabTitle?js_string}',
    entity:  '${tabComponent.entityName}',
    isDeleteableTable: ${tabComponent.deleteableTable?string},
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
    
    standardProperties:{
<@compress single_line=true>
      inpTabId: '${tabComponent.tabId}',
      inpwindowId: '${tabComponent.windowId}',
      inpTableId: '${tabComponent.tableId?js_string}',
      inpkeyColumnId: '${tabComponent.keyColumnName?js_string}',
      keyProperty: '${tabComponent.keyProperty.name?js_string}',
      inpKeyName: '${tabComponent.keyInpName?js_string}',
      keyColumnName: '${tabComponent.keyColumnName?js_string}',
      keyPropertyType: '${tabComponent.keyPropertyType?js_string}'      
</@compress>
    },
     
    actionToolbarButtons: [
    <#list tabComponent.buttonFields as field>
<@compress single_line=true>
      {id: '${field.id?js_string}', 
       title: '${field.label?js_string}',
       obManualURL: '${field.url?js_string}',
       command: '${field.command?js_string}',
       property: '${field.propertyName?js_string}',
       processId: '${field.processId?js_string}',
       <#if field.newDefinition>newDefinition: ${field.newDefinition?string},</#if>
       <#if field.windowId != "">windowId: '${field.windowId?js_string}',</#if>
       <#if field.windowTitle != "">windowTitle: '${field.windowTitle?js_string}',</#if>
       <#if !field.modal>modal: ${field.modal?string},</#if>
       <#if field.hasLabelValues>
       labelValue: {<#list field.labelValues as value>
           '${value.value?js_string}': '${value.label?js_string}'<#if value_has_next>,</#if>
       </#list>
         },
       </#if>
       <#if field.showIf != "">
       displayIf: function(form, currentValues, context) {
         return (${field.showIf});
       },
       </#if>
       <#if field.readOnlyIf != "">
       readOnlyIf: function(form, currentValues, context) {
         return (${field.readOnlyIf});
       },
       </#if>
       autosave: ${field.autosave?string}
      }<#if field_has_next>,</#if>
</@compress>
    </#list>],
    
    showParentButtons: ${tabComponent.showParentButtons?string},
    
    buttonsHaveSessionLogic: ${tabComponent.buttonSessionLogic?string},
    
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
    
    initialPropertyToColumns:[
    <#list tabComponent.otherFields as field>
<@compress single_line=true>
        {
            property: '${field.propertyName?js_string}',
            inpColumn: '${field.inpColumnName?js_string}', 
            dbColumn: '${field.dbColumnName?js_string}',
            <#if field.session>
                sessionProperty: ${field.session?string},
            </#if>
            type: '${field.type?js_string}'
        }<#if field_has_next>,</#if>
</@compress>
    </#list>
    ],
    
    iconToolbarButtons: [
    <#list tabComponent.iconButtons as button>
<@compress single_line=true>
      {
        action: function(){ ${button.action} },
        buttonType: '${button.type?js_string}',
        prompt: '${button.label?js_string}'
      }<#if button_has_next>,</#if>
</@compress>
    </#list>],
    
    <#if tabComponent.childTabs?has_content>
        hasChildTabs: ${tabComponent.hasChildTabsProperty},
        createViewStructure: function() {
            <#list tabComponent.childTabs as childTabComponent>
            this.addChildView(
                isc.OBStandardView.create({
                    <@createView childTabComponent/>
                })
            );
            </#list>
        },
    </#if>
    initWidget: function() {
        this.prepareFields();
        this.dataSource = ${tabComponent.dataSourceJavaScript};
        this.viewForm = isc.OBViewForm.create(isc.clone(OB.ViewFormProperties), ${tabComponent.viewForm}); 
        this.viewGrid = ${tabComponent.viewGrid};
        this.Super('initWidget', arguments);
    }
</#macro>