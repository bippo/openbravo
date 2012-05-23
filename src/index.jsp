<%@ page import="java.util.Properties" %>
<%@ page import="org.openbravo.base.HttpBaseServlet" %>
<%@ page import="org.openbravo.dal.core.OBContext"%>
<%@ page import="org.openbravo.base.util.OBClassLoader" %>
<%@ page import="org.openbravo.authentication.AuthenticationManager" %>
<%@ page import="org.openbravo.client.kernel.KernelUtils" %>
<%@ page import="org.openbravo.dal.core.OBContext" %>
<%@ page import="org.openbravo.model.ad.module.Module" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
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

Logger log = Logger.getLogger(org.openbravo.authentication.AuthenticationManager.class); 

HttpBaseServlet s = new HttpBaseServlet(); // required for ConnectionProvider
s.init(getServletConfig());
s.initialize(request, response);

AuthenticationManager authManager = AuthenticationManager.getAuthenticationManager(s);
    
authManager.init(s);

String userId = authManager.authenticate(request, response);
if(userId == null){
  return;
}

boolean uncompSC = false;
String scDevModulePackage = "org.openbravo.userinterface.smartclient.dev";
OBContext.setAdminMode();
try {
  if (KernelUtils.getInstance().isModulePresent(scDevModulePackage)) {
    uncompSC = KernelUtils.getInstance().getModule(scDevModulePackage).isInDevelopment();
  }
} catch (Exception e) {
  log.error("Error trying to acquire module \"" + scDevModulePackage + "\": " + e.getMessage(), e);
} finally {
  OBContext.restorePreviousMode();
}

String ua = request.getHeader( "User-Agent" );
boolean isMSIE = ( ua != null && ua.indexOf( "MSIE" ) != -1 );
int verMSIE = 0;
String verMSIEtmp = "";
if (isMSIE) {
  verMSIEtmp = ua.substring(ua.indexOf("MSIE") + 5);
  verMSIEtmp = verMSIEtmp.substring(0, verMSIEtmp.indexOf("."));
  if (ua.indexOf("MSIE 7.0") != -1 && ua.indexOf("Trident/4") != -1) {
    //In case IE8 runs in "IE8 Compatibility mode, look for Trident/4.0 to know that is IE8 although MSIE string is MSIE 7.0
    verMSIEtmp = "8";
  } else if (ua.indexOf("MSIE 7.0") != -1 && ua.indexOf("Trident/5") != -1) {
    // In case IE9 runs in "IE8 Compatibility mode, look for Trident/5.0 to know that is IE9 although MSIE string is MSIE 7.0
    verMSIEtmp = "9";
  } else if (ua.indexOf("MSIE 7.0") != -1 && ua.indexOf("Trident/") != -1) {
    // For hypothetic future IE versions in case IEX runs in "IEX Compatibility mode, look for Trident/ to know that is IEX although MSIE string is MSIE 7.0
    verMSIEtmp = "10"; //If this 'if' statement is not updated, could be 10 or 11 or anything... but set 10 just to ensure it is not in IE7
  }
  verMSIE = Integer.parseInt(verMSIEtmp);
}
response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
response.addHeader("Pragma", "no-cache");
response.addHeader("Expires", "0");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<meta http-equiv="Expires" content="Tue, 24 Apr 1979 00:00:01 GMT"/>
<meta http-equiv="Content-type" content="text/html;charset=utf-8"/>
<meta http-equiv="Cache-Control" content="no-cache no-store must-revalidate" >
<meta http-equiv="Pragma" content="no-cache" >
<meta name="author" content="Openbravo S.L.U.">
<meta name="keywords" content="openbravo">
<meta name="description" content="Openbravo S.L.U.">
<link rel="shortcut icon" href="./web/images/favicon.ico" />
<link rel="stylesheet" type="text/css" href="./org.openbravo.client.kernel/OBCLKER_Kernel/StyleSheetResources?_mode=3.00&_skinVersion=Default&_cssDataUri=<%=(!isMSIE || (isMSIE && verMSIE >=8))%>"/>

<title>Openbravo</title>
<script type="text/javascript" src="./web/org.openbravo.client.kernel/js/scopeleaks.min.js"></script>
<script type="text/javascript" src="./web/org.openbravo.client.kernel/js/LAB.min.js"></script>
<script type="text/javascript" src="./web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.1.min.js"></script>

<!-- styles used during loading -->
<style type="text/css">
  html, body {
      height: 100%;
      width: 100%;
  }

  .OBCenteredBox {
      position: fixed;
      z-index: 1000000;
      top: 50%;
      left: 50%;
      margin: -25px 0 0 -150px;
      width: 300px;
      height: 50px;
  }

  .OBLoadingPromptLabel {
      font-family: 'Arial';
      font-size: 12px;
      color: #ccd0d4;
  }

  .OBLoadingPromptModalMask {
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      background-color: #7f7f7f;
  }
</style>

</head>
<body dir="<%=(OBContext.isRightToLeft() ? "RTL" : "LTR")%>">

<!-- shows the loading div -->
<div class="OBLoadingPromptModalMask" id="OBLoadingDiv">
    <div class="OBCenteredBox">
        <table>
            <tr>
                <td>
                    <span class="OBLoadingPromptLabel">LOADING...</span>
                </td>
                <td>
                    <img width="220" height="16" src="./web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/org.openbravo.client.application/images/system/windowLoading.gif"/>
                </td>
            </tr>
        </table>
    </div>
</div>
<!-- load the rest -->
<script type="text/javascript">
$LAB.setGlobalDefaults({AppendTo: 'body'});

var isomorphicDir='./web/org.openbravo.userinterface.smartclient/isomorphic/';

// starts the application is called as the last statement in the StaticResources part
function OBStartApplication() {
  OB.Layout.initialize();
  OB.Layout.draw();
  OB.Layout.ViewManager.createAddStartTab();
  // get rid of the loading stuff
  document.body.removeChild(document.getElementById('OBLoadingDiv'));
  OB.GlobalHiddenForm = document.forms.OBGlobalHiddenForm;
<%
  if (session.getAttribute("STARTUP-MESSAGE") != null) {
    String text = (String) session.getAttribute("STARTUP-MESSAGE");
    String title = (String) session.getAttribute("STARTUP-MESSAGE-TITLE");
    session.removeAttribute("STARTUP-MESSAGE");
    session.removeAttribute("STARTUP-MESSAGE-TITLE");
%>
  isc.say('<%=text%>', null, {title: '<%=title%>'});
<%
  }
%>
}
</script>
<script type="text/javascript" src="./web/org.openbravo.userinterface.smartclient/isomorphic/ISC_Combined<%=(uncompSC ? ".uncompressed" : "")%>.js"></script>
<script type="text/javascript" src="./web/org.openbravo.userinterface.smartclient/isomorphic/ISC_History<%=(uncompSC ? ".uncompressed" : "")%>.js"></script>
<script type="text/javascript" src="./org.openbravo.client.kernel/OBCLKER_Kernel/StaticResources?_mode=3.00&_skinVersion=Default"></script>
<iframe name="background_target" id="background_target" height="0" width="0" style="display:none;"></iframe>
<form name="OBGlobalHiddenForm" method="post" action="blank.html" target="background_target">
</form>
</body>
</html>
