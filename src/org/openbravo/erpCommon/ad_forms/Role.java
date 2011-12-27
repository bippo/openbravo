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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.LicenseRestriction;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class Role extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars, false);
    } else if (vars.commandIn("CHANGE")) {
      String strClaveOld = vars.getRequiredStringParameter("inpClaveOld");
      String strClaveNew = vars.getRequiredStringParameter("inpClaveNew");

      OBError myMessage = null;
      try {
        changePassword(vars, strClaveOld, strClaveNew);
      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        if (!myMessage.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        } else {
          vars.setMessage("Role", myMessage);
        }
      }
      if (myMessage == null) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=PasswordChanged");
        myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
        vars.setMessage("Role", myMessage);
      }
      bdErrorAjax(response, myMessage.getType(), myMessage.getTitle(), myMessage.getMessage());
      // response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("SAVE")) {
      String strSetDefault = vars.getStringParameter("setasdefault");

      if (saveDefaultOptions(vars, strSetDefault, request)) {
        vars = new VariablesSecureApp(request); // refresh
        printPage(response, vars, true);
      } else {
        OBError myMessage = new OBError();
        myMessage.setType("Error");
        myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        myMessage.setMessage(Utility.messageBD(this, "LoginError", vars.getLanguage()));
        vars.setMessage("Role", myMessage);
        response.sendRedirect(strDireccion + request.getServletPath());
      }
    } else if (vars.commandIn("WAREHOUSES")) {
      printWarehousesJSON(response, vars);
    } else
      pageErrorPopUp(response);
  }

  private void printWarehousesJSON(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    String role = vars.getRequiredStringParameter("role");
    String client = vars.getRequiredStringParameter("client");

    WarehouseData[] data = WarehouseData.selectByRoleAndClient(this, role, client);

    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    if (data == null) {
      out.println(JSONObject.NULL.toString());
    } else {
      JSONArray list = new JSONArray();

      for (int i = 0; i < data.length; i++) {
        JSONArray item = new JSONArray();
        item.put(data[i].padre);
        item.put(data[i].id);
        item.put(data[i].name);
        list.put(item);
      }
      out.println(list.toString());
    }
    out.close();
  }

  private void changePassword(VariablesSecureApp vars, String strClaveOld, String strClaveNew)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Login change process change");
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Old password: " + strClaveOld + ", "
          + FormatUtilities.sha1Base64(strClaveOld) + " - user:" + vars.getUser());
    if (log4j.isDebugEnabled())
      log4j.debug("pwd: " + RoleData.getPassword(this, vars.getUser()) + " - encr: "
          + FormatUtilities.sha1Base64(strClaveOld));

    if (!RoleData.getPassword(this, vars.getUser()).equals(FormatUtilities.sha1Base64(strClaveOld))) {
      log4j.error("Invalid password");
      throw new ServletException("@CODE=PasswordIncorrect");
    }
    if (RoleData.update(this, FormatUtilities.sha1Base64(strClaveNew), vars.getUser()) == 0)
      throw new ServletException("@CODE=ProcessError");
  }

  private boolean saveDefaultOptions(VariablesSecureApp vars, String strSetDefault,
      HttpServletRequest req) throws ServletException {

    String strUserAuth = vars.getUser();
    String strLanguage = vars.getStringParameter("language");
    String strRol = vars.getStringParameter("role");
    String strClient = vars.getStringParameter("client");
    String strOrg = vars.getStringParameter("organization");
    String strWarehouse = vars.getStringParameter("warehouse");

    final String strIsRTL = RoleData.getIsRTL(this, strLanguage);
    if (strIsRTL.equals("Y")) {
      vars.setSessionValue("#TextDirection", "RTL");
    } else {
      vars.setSessionValue("#TextDirection", "LTR");
    }

    if (strSetDefault.equals("Y"))
      RoleData.saveDefaultOptions(this, strLanguage, strRol, strClient, strOrg, strWarehouse,
          strUserAuth);

    if (strClient.equals("") || strOrg.equals("") || strRol.equals(""))
      return false;

    // Clear session variables maintaining session and user
    String sessionID = vars.getSessionValue("#AD_Session_ID");
    String sessionUser = (String) req.getSession(true).getAttribute("#Authenticated_user");
    vars.clearSession(false);
    vars.setSessionValue("#AD_Session_ID", sessionID);
    req.getSession(true).setAttribute("#Authenticated_user", sessionUser);

    boolean result = LoginUtils.fillSessionArguments(this, vars, strUserAuth, strLanguage,
        strIsRTL, strRol, strClient, strOrg, strWarehouse);
    if (!result)
      return false;
    readProperties(vars, globalParameters.getOpenbravoPropertiesPath());
    readNumberFormat(vars, globalParameters.getFormatPath());
    return true;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, boolean bSaveOK)
      throws IOException, ServletException {

    final String strIsRTL = RoleData.getIsRTL(this, vars.getLanguage());

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/Role")
        .createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("TextDirection", ((strIsRTL.equals("Y") ? "RTL" : "LTR")));
    xmlDocument.setParameter("client", Utility.messageBD(this, "AD_Client_ID", vars.getLanguage()));
    xmlDocument.setParameter("org", Utility.messageBD(this, "AD_Org_ID", vars.getLanguage()));
    xmlDocument.setParameter("user", RoleData.nombreUsuario(this, vars.getUser()));

    // Input data
    xmlDocument.setParameter("inputLanguage", vars.getLanguage());
    xmlDocument.setParameter("inputRole", vars.getRole());
    xmlDocument.setParameter("inputEntity", vars.getClient());
    xmlDocument.setParameter("inputOrg", vars.getOrg());
    xmlDocument.setParameter("inputWarehouse", vars.getWarehouse());

    // fields

    xmlDocument.setParameter("saveok", Boolean.toString(bSaveOK));

    xmlDocument.setData("structureLang", LanguageComboData.select(this));

    // Role
    OBContext.setAdminMode();
    RoleComboData[] datarole = null;
    try {
      // We check if there is a Openbravo Professional Subscription restriction in the license,
      // or if the last rebuild didn't go well. If any of these are true, then the user is
      // allowed to login only as system administrator
      SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");
      boolean correctSystemStatus = sysInfo.getSystemStatus() == null
          || this.globalParameters.getOBProperty("safe.mode", "false").equalsIgnoreCase("false")
          || sysInfo.getSystemStatus().equals("RB70");
      LicenseRestriction limitation = ActivationKey.getInstance().checkOPSLimitations(
          vars.getDBSession());
      if (limitation == LicenseRestriction.OPS_INSTANCE_NOT_ACTIVE
          || limitation == LicenseRestriction.NUMBER_OF_CONCURRENT_USERS_REACHED
          || limitation == LicenseRestriction.MODULE_EXPIRED
          || limitation == LicenseRestriction.NOT_MATCHED_INSTANCE
          || limitation == LicenseRestriction.HB_NOT_ACTIVE || !correctSystemStatus) {
        // allow only system login
        datarole = RoleComboData.selectSystem(this, vars.getUser());
      } else {

        datarole = RoleComboData.select(this, vars.getUser());
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    // Client
    List<ClientData> vecClients = new ArrayList<ClientData>();
    ClientData[] plaindataclient = ClientData.select(this);
    if (plaindataclient != null) {

      for (int i = 0; i < datarole.length; i++) {

        String clientlist = LoginUtils.buildClientList(LoginUtils
            .loadRoleOrganization(datarole[i].adRoleId));
        StringTokenizer st = new StringTokenizer(clientlist, ",", false);
        while (st.hasMoreTokens()) {
          String token = st.nextToken().trim();
          token = token.substring(1, token.length() - 1); // remove quotes
          ClientData auxClient = new ClientData();
          auxClient.padre = datarole[i].adRoleId;
          auxClient.id = token;
          auxClient.name = getDescriptionFromArray(plaindataclient, token);
          vecClients.add(auxClient);
        }
      }
    }

    xmlDocument.setData("structureRol", datarole);
    xmlDocument.setParameter(
        "clientes",
        Utility.arrayDobleEntrada("arrClientes",
            vecClients.toArray(new ClientData[vecClients.size()])));
    xmlDocument.setParameter("organizaciones",
        Utility.arrayDobleEntrada("arrOrgs", OrganizationData.selectLogin(this)));

    {
      OBError myMessage = vars.getMessage("Role");
      vars.removeMessage("Role");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet for the media reports generation";
  } // end of getServletInfo() method

  private String getDescriptionFromArray(ClientData[] data, String clave) {
    if (data == null || data.length == 0)
      return "";
    for (int i = 0; i < data.length; i++) {
      if (data[i].id.equalsIgnoreCase(clave))
        return data[i].name;
    }
    return "";
  }
}
