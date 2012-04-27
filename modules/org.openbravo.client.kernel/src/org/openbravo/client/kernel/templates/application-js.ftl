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
 * All portions are Copyright (C) 2009-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
//jslint

if (window.isc) {
  // do at the beginning
  isc.setAutoDraw(false);
  // Prevent errors in smartclient for screenreader, is quite new and unstable for now
  isc.screenReader = false;
}

var OB = {
    Application : {
        testEnvironment: ${data.testEnvironment?string},
        language: '${data.languageId?js_string}',
        language_string: '${data.language?js_string}',
        systemVersion : '${data.systemVersion?js_string}', // global version used in all hyperlinks
        contextUrl: '${data.contextUrl}',
        communityBrandingStaticUrl: '${data.communityBrandingStaticUrl?js_string}',
        butlerUtilsUrl: '${data.butlerUtilsUrl?js_string}',
        purpose: '${data.instancePurpose?js_string}',
        licenseType: '${data.licenseType?js_string}',
        isTrial: ${data.trialStringValue},
        isGolden: ${data.goldenStringValue},
        versionDescription: '${data.versionDescription?js_string}'
    },

    Format : {
        defaultGroupingSize: 3,
        defaultGroupingSymbol: '${data.defaultGroupingSymbol}',
        defaultDecimalSymbol: '${data.defaultDecimalSymbol}',
        defaultNumericMask: '${data.defaultNumericMask}',
        date: '${data.dateFormat}',
        dateTime: '${data.dateTimeFormat}',
        formats: {
        <#list data.formats?keys as key>
          '${key}': '${data.formats[key]}'<#if key_has_next>,</#if>
        </#list>
        }
    },

    Constants : {
        IDENTIFIER : '_identifier',
        ID : 'id',
        WHERE_PARAMETER : '_where',
        ORG_PARAMETER : '_org',
        ORDERBY_PARAMETER : '_orderBy',
        FILTER_PARAMETER : '_filter',
        SORTBY_PARAMETER : '_sortBy',
        OR_EXPRESSION: '_OrExpression',
        TEXT_MATCH_PARAMETER_OVERRIDE: '_textMatchStyleOverride',
        SUCCESS : 'success',
        DBL_CLICK_DELAY: 300,
        ERROR : 'error'
    },

    Styles : {
      skinsPath : '${data.contextUrl}' + 'web/org.openbravo.userinterface.smartclient/openbravo/skins/'
    },

    I18N: {}
};