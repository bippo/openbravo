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
//jslint

OB.User = {
        id : '${data.user.id}',
        firstName : '${(data.user.firstName!'')?js_string}',
        lastName : '${(data.user.lastName!'')?js_string}',
        userName : '${(data.user.username!'')?js_string}',
        name : '${(data.user.name!'')?js_string}',
        email : '${(data.user.email!'')?js_string}',
        roleId: '${data.role.id}',
        roleName: '${data.role.name?js_string}',
        clientId: '${data.client.id}',
        clientName: '${data.client.name?js_string}',
        organizationId: '${data.organization.id}',
        organizationName: '${data.organization.name?js_string}'
};

OB.AccessibleEntities = {
    <#list data.accessibleEntities as entity>
    '${entity.name?js_string}':  true<#if entity_has_next>,</#if>
    </#list>
};
