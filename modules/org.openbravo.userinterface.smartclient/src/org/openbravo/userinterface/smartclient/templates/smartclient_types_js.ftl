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
// jslint

// remarks if there is an editor type defined in the simpletype then the validators of the 
// FormItem itself are not executed anymore.
<#list data.definitions as uiDefinition>
isc.SimpleType.create({
    inheritsFrom: '${uiDefinition.parentType?js_string}',
    name: '${uiDefinition.name?js_string}',
    <#if uiDefinition.formEditorType != "">
    editorType: '${uiDefinition.formEditorType?js_string}',
    gridEditorType: '${uiDefinition.gridEditorType?js_string}',
    filterEditorType: '${uiDefinition.filterEditorType?js_string}',
    </#if>
    ${uiDefinition.typeProperties}
    referenceName: '${uiDefinition.reference.name?js_string}'
});
</#list>       

// set the global date format
isc.Date.setShortDisplayFormat(function(useCustomTimeZone) {
    return OB.Utilities.Date.JSToOB(this, OB.Format.date);
});

isc.Date.setNormalDisplayFormat(function(useCustomTimeZone) {
    return OB.Utilities.Date.JSToOB(this, OB.Format.date);
});

isc.Date.inputFormat = function(dateString, format, centuryThreshold, suppressConversion) {
    return OB.Utilities.Date.OBToJS(dateString, OB.Format.date);
};

isc.Date.setInputFormat(function(value) {
    return OB.Utilities.Date.OBToJS(value, OB.Format.date);
});

isc.Date.setShortDatetimeDisplayFormat(function(useCustomTimeZone) {
    return OB.Utilities.Date.JSToOB(this, OB.Format.dateTime);
});

isc.Date.toShortDatetime = function(formatter, isDatetimeField) {
    return OB.Utilities.Date.JSToOB(this, OB.Format.dateTime);
};