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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess;
import org.openbravo.erpCommon.ad_process.HeartbeatProcess.HeartBeatOrRegistration;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.obps.ActivationKey.FeatureRestriction;
import org.openbravo.model.ad.access.Session;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class VerticalMenu extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private String target = "appFrame";

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPageDataSheet(response, vars, "0", false);
    } else if (vars.commandIn("ALL")) {
      printPageDataSheet(response, vars, "0", true);
    } else if (vars.commandIn("ALERT")) {
      updateSessionLastActivity(vars);
      printPageAlert(response, vars);
    } else if (vars.commandIn("LOADING")) {
      printPageLoadingMenu(response, vars);
    } else if (vars.commandIn("HIDE")) {
      printPageHideMenu(response, vars);
    } else
      throw new ServletException();
  }

  /**
   * Updates last time the browser checked for alerts (ping) this info is later used (in
   * {@link org.openbravo.erpCommon.obps.ActivationKey}) to find no longer used sessions because the
   * browser was closed without the user explicitly closing the session.
   */
  private void updateSessionLastActivity(VariablesSecureApp vars) {
    String sessionId = vars.getDBSession();
    Date now = new Date();
    log4j.debug("ping session:" + sessionId + " - time" + now);
    if (sessionId != null && !sessionId.isEmpty()) {
      OBContext.setAdminMode();
      try {
        Session session = OBDal.getInstance().get(Session.class, sessionId);
        session.setLastPing(now);
        // flush to force commit in admin mode
        OBDal.getInstance().flush();
      } catch (Exception e) {
        log4j.error("Error in session ping", e);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }

  private void printPageAlert(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    Integer alertCount = 0;
    final VerticalMenuData[] data = VerticalMenuData.selectAlertRules(this, vars.getUser(),
        vars.getRole());
    if (data != null && data.length != 0) {
      for (int i = 0; i < data.length; i++) {
        final String strWhere = new UsedByLink().getWhereClause(vars, "", data[i].filterclause);
        try {
          final Integer count = new Integer(VerticalMenuData.selectCountActiveAlerts(this,
              Utility.getContext(this, vars, "#User_Client", ""),
              Utility.getContext(this, vars, "#AccessibleOrgTree", ""), data[i].adAlertruleId,
              strWhere)).intValue();
          alertCount += count;
        } catch (final Exception ex) {
          log4j.error("Error in Alert Query, alertRule=" + data[i].adAlertruleId + " error:"
              + ex.toString());
        }
      }
    }

    response.setContentType("text/plain; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(alertCount.toString());
    out.close();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strCliente, boolean open) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Vertical Menu's screen");
    final String[] discard = new String[1];
    if (open)
      discard[0] = new String("buttonExpand");
    else
      discard[0] = new String("buttonCollapse");
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/VerticalMenu", discard).createXmlDocument();

    final MenuData[] data = MenuData.selectIdentificacion(this, strCliente);
    MenuData[] dataMenu;
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    dataMenu = MenuData.select(this, vars.getLanguage(), vars.getRole(), data[0].parentId);
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";");
    xmlDocument.setParameter("autosave", "var autosave = "
        + (vars.getSessionValue("#Autosave").equals("")
            || vars.getSessionValue("#Autosave").equalsIgnoreCase("N") ? "false" : "true") + ";");
    final StringBuffer menu = new StringBuffer();
    menu.append(generarMenuVertical(dataMenu, strDireccion, "0", open));
    menu.append(generateMenuSearchs(vars, open));

    xmlDocument.setParameter("menu", menu.toString());
    xmlDocument.setParameter("userName", MenuData.getUserName(this, vars.getUser()));

    decidePopups(xmlDocument, vars);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageHideMenu(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Vertical Menu's screen");

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/VerticalMenu").createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";");
    xmlDocument.setParameter("autosave", "var autosave = "
        + (vars.getSessionValue("#Autosave").equals("")
            || vars.getSessionValue("#Autosave").equalsIgnoreCase("N") ? "false" : "true") + ";");

    xmlDocument.setParameter("menu", "");
    xmlDocument.setParameter("userName", MenuData.getUserName(this, vars.getUser()));
    xmlDocument.setParameter("popup", "");

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageLoadingMenu(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/VerticalMenuLoading").createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String tipoVentanaNico(String tipo) {
    if (tipo.equals("W"))
      return "window";
    else if (tipo.equals("X"))
      return "form";
    else if (tipo.equals("P"))
      return "process";
    else if (tipo.equals("R"))
      return "report";
    else if (tipo.equals("F"))
      return "wf";
    else if (tipo.equals("B"))
      return "wb";
    else if (tipo.equals("L"))
      return "el";
    else if (tipo.equals("I"))
      return "il";
    else
      return "";
  }

  private String generarMenuVertical(MenuData[] menuData, String strDireccion, String indice,
      boolean open) {
    if (menuData == null || menuData.length == 0)
      return "";
    if (indice == null)
      indice = "0";
    // boolean haveData=false;
    final StringBuffer strText = new StringBuffer();
    for (int i = 0; i < menuData.length; i++) {
      if (menuData[i].parentId.equals(indice)) {
        // haveData=true;
        final String strHijos = generarMenuVertical(menuData, strDireccion, menuData[i].nodeId,
            open);
        String strID = "";

        // Hide non accessible entries because of license
        String menuAction = menuData[i].action;
        if (menuAction.equals("W")) {
          menuAction = "MW";
        }
        String artifactId = "";
        if (menuAction.equals("MW")) {
          artifactId = menuData[i].adWindowId;
        } else if (menuAction.equals("P") || menuAction.equals("R")) {
          artifactId = menuData[i].adProcessId;
        } else if (menuAction.equals("X")) {
          artifactId = menuData[i].adFormId;
        }
        boolean hasLicenseAccess = ActivationKey.getInstance().hasLicenseAccess(menuAction,
            artifactId) == FeatureRestriction.NO_RESTRICTION;

        if (hasLicenseAccess && (!strHijos.equals("") || menuData[i].issummary.equals("N"))) {
          strText.append("<tr>\n");
          strText.append("  <td>\n");
          strText.append("    <table cellspacing=\"0\" cellpadding=\"0\"");
          if (menuData[i].issummary.equals("N")) {
            strText.append(" id=\"").append(tipoVentanaNico(menuData[i].action))
                .append(menuData[i].nodeId).append("\"");
            strID = tipoVentanaNico(menuData[i].action) + menuData[i].nodeId;
          } else {
            strText.append(" id=\"folder").append(menuData[i].nodeId).append("\"");
          }
          strText.append(" onmouseover=\"window.status='");
          strText.append(FormatUtilities.replaceJS(menuData[i].description));
          strText.append("';return true;\"");
          strText.append(" onmouseout=\"window.status='';return true;\">\n");
          strText.append("      <tr");
          strText.append(" class=\"Normal ");
          if (!open || !menuData[i].issummary.equals("Y"))
            strText.append("NOT_");
          strText.append("Opened NOT_Hover NOT_Selected NOT_Pressed NOT_Focused");
          strText.append("\"");
          if (menuData[i].issummary.equals("N")) {
            strText.append(" id=\"child").append(strID).append("\"");
            strText
                .append(" onclick=\"checkSelected('child")
                .append(strID)
                .append(
                    "');submitCommandForm('DEFAULT', "
                        + (menuData[i].action.equals("F") ? "false" : "true") + ", getForm(),'");
            if (menuData[i].action.equals("L") || menuData[i].action.equals("I"))
              strText.append(menuData[i].url);
            else {
              strText.append(getUrlString(strDireccion, menuData[i]));
            }
            strText.append("', '");
            if (menuData[i].action.equals("F")
                || (menuData[i].action.equals("P") && menuData[i].mappingname.equals("") && !(menuData[i].isexternalservice
                    .equals("Y") && menuData[i].serviceType.equals("PS"))))
              strText.append("hiddenFrame");
            else if (menuData[i].action.equals("L"))
              strText.append("_blank");
            else
              strText.append(target);
            strText.append("'");
            strText.append(", false, " + (menuData[i].action.equals("F") ? "false" : "true")
                + ");return false;\"");
          } else {
            strText.append(" id=\"child").append(menuData[i].nodeId).append("\"");
          }
          strText
              .append(" onmouseover=\"setMouseOver(this);return true;\" onmouseout=\"setMouseOut(this); return true;\"");
          strText
              .append(" onmousedown=\"setMouseDown(this);return true;\" onmouseup=\"setMouseUp(this);return true;\">\n");
          strText.append("        <td width=\"5px\"");
          if (menuData[i].issummary.equals("Y"))
            strText.append(" id=\"folderCell1_").append(menuData[i].nodeId).append("\"");
          strText.append(">");
          strText.append("<img src=\"").append(strReplaceWith)
              .append("/images/blank.gif\" class=\"Menu_Client_Button_")
              .append((indice.equals("0") ? "Big" : "")).append("Icon");
          if (menuData[i].issummary.equals("N")) {
            if (menuData[i].action.equals("B"))
              strText.append(" Menu_Client_Button_Icon_childWorkBench");
            else if (menuData[i].action.equals("P"))
              strText.append(" Menu_Client_Button_Icon_childProcesses");
            else if (menuData[i].action.equals("R"))
              strText.append(" Menu_Client_Button_Icon_childReports");
            else if (menuData[i].action.equals("X"))
              strText.append(" Menu_Client_Button_Icon_childForms");
            else if (menuData[i].action.equals("L") || menuData[i].action.equals("I"))
              strText.append(" Menu_Client_Button_Icon_childExternalLink");
            else
              strText.append(" Menu_Client_Button_Icon_childWindows");
          } else {
            strText.append(" Menu_Client_Button_");
            if (indice.equals("0"))
              strText.append("Big");
            strText.append("Icon_folder");
            strText.append((open ? "Opened" : "Closed"));
          }
          strText.append("\"");
          if (menuData[i].issummary.equals("Y"))
            strText.append(" id=\"folderImg").append(menuData[i].nodeId).append("\"");
          strText.append(">");
          strText.append("</td>\n");
          strText.append("        <td nowrap=\"\"");
          if (menuData[i].issummary.equals("Y"))
            strText.append(" id=\"folderCell2_").append(menuData[i].nodeId).append("\"");
          strText.append(">");
          strText.append(menuData[i].name);
          strText.append("</td>\n");
          strText.append("      </tr>\n");
          strText.append("    </table>\n");
          strText.append("  </td>\n");
          strText.append("</tr>\n");
          strText.append("<tr>\n");
          strText.append("  <td");
          if (strHijos.equals("")) {
            strText.append(" style=\"").append("display: none;").append("\" id=\"parent")
                .append(menuData[i].nodeId).append("\">\n");
          } else {
            strText.append(" style=\"").append((!open ? "display: none;" : ""))
                .append("\" id=\"parent").append(menuData[i].nodeId).append("\">\n");
          }
          strText
              .append("    <table cellspacing=\"0\" cellpadding=\"0\" class=\"Menu_Client_child_bg\">\n");
          strText.append(strHijos);
          strText.append("    </table>\n");
          strText.append("  </td>\n");
          strText.append("</tr>\n");
        }
      }
    }
    return (strText.toString());
  }

  public static String getUrlStringStatic(String strDireccionBase, String name, String action,
      String classname, String mappingname, String adProcessId, String isExternalService,
      String externalType) {
    MenuData menuItem = new MenuData();
    menuItem.name = name;
    menuItem.action = action;
    menuItem.classname = classname;
    menuItem.mappingname = mappingname;
    menuItem.adProcessId = adProcessId;
    menuItem.isexternalservice = isExternalService;
    menuItem.serviceType = externalType;
    return new VerticalMenu().getUrlString(strDireccionBase, menuItem);
  }

  private String getUrlString(String strDireccionBase, MenuData menuItem) {
    final StringBuffer strResultado = new StringBuffer();
    strResultado.append(strDireccionBase);
    if (menuItem.mappingname.equals("")) {
      if (menuItem.action.equals("W")) {
        strResultado.append("/" + ("0".equals(menuItem.windowmodule) ? "" : menuItem.windowpackage)
            + menuItem.windowname + "/" + menuItem.tabname
            + ("0".equals(menuItem.tabmodule) ? "" : menuItem.adTabId) + "_Relation.html");
      } else if (menuItem.action.equals("P")) {
        if (menuItem.isexternalservice.equals("Y") && menuItem.serviceType.equals("PS"))
          strResultado.append("/utility/OpenPentaho.html?inpadProcessId=").append(
              menuItem.adProcessId);
        else {
          try {
            if (MenuData.isGenericJavaProcess(this, menuItem.adProcessId))
              strResultado.append(
                  "/ad_actionButton/ActionButtonJava_Responser.html?inpadProcessId=").append(
                  menuItem.adProcessId);
            else
              strResultado.append("/ad_actionButton/ActionButton_Responser.html?inpadProcessId=")
                  .append(menuItem.adProcessId);
          } catch (final Exception e) {
            e.printStackTrace();
            strResultado.append("/ad_actionButton/ActionButton_Responser.html?inpadProcessId=")
                .append(menuItem.adProcessId);
          }
        }
      } else if (menuItem.action.equals("X")) {
        strResultado.append("/ad_forms/").append(FormatUtilities.replace(menuItem.name))
            .append(".html");
      } else if (menuItem.action.equals("R")) {
        strResultado.append("ad_reports/").append(FormatUtilities.replace(menuItem.name))
            .append(".html");
      }
    } else {
      strResultado.append(menuItem.mappingname);
    }
    return strResultado.toString();
  }

  /**
   * Generates Search folder and entries in case there is at least one accessible entrie, other case
   * it returns an empty string
   * 
   * @param vars
   * @param open
   * @return the search folder and entries in case there is at least one accessible entrie, other
   *         case it returns an empty string
   * @throws ServletException
   */
  private String generateMenuSearchs(VariablesSecureApp vars, boolean open) throws ServletException {
    final StringBuffer menu = new StringBuffer();
    final MenuData[] data = MenuData.selectSearchs(this, vars.getLanguage());
    if (data != null && data.length > 0) {
      final String entries = generateMenuSearchEntries(vars, strDireccion, open, data);
      if (entries.length() > 0) {
        menu.append("<tr>\n");
        menu.append("  <td>\n");
        menu.append("    <table cellspacing=\"0\" cellpadding=\"0\" onmouseover=\"window.status='");
        menu.append(Utility.messageBD(this, "Information", vars.getLanguage()));
        menu.append("';return true;\"");
        menu.append(" onmouseout=\"window.status='';return true;\"");
        menu.append(" id=\"folderInformation\">\n");
        menu.append("      <tr class=\"Normal ");
        if (!open)
          menu.append("NOT_");
        menu.append("Opened NOT_Hover NOT_Selected NOT_Pressed NOT_Focused");
        menu.append("\" id=\"childInformation\" onmouseover=\"setMouseOver(this);return true;\" onmouseout=\"setMouseOut(this); return true;\"");
        menu.append(" onmousedown=\"setMouseDown(this);return true;\" onmouseup=\"setMouseUp(this);return true;\">\n");
        menu.append("        <td width=\"5px\" id=\"folderCell1_Information\"><img src=\"")
            .append(strReplaceWith)
            .append(
                "/images/blank.gif\" class=\"Menu_Client_Button_BigIcon Menu_Client_Button_BigIcon_folder");
        menu.append((open ? "Opened" : "Closed"));
        menu.append("\" id=\"folderImgInformation\"></td>\n");
        menu.append("        <td nowrap=\"\" id=\"folderCell2_Information\">");
        menu.append(Utility.messageBD(this, "Information", vars.getLanguage()));
        menu.append("        </td>\n");
        menu.append("      </tr>\n");
        menu.append("    </table>\n");
        menu.append("  </td>\n");
        menu.append("</tr>\n");
        menu.append("<tr>\n");
        menu.append("  <td");
        menu.append(" style=\"").append((!open ? "display: none;" : ""))
            .append("\" id=\"parentInformation\">\n");
        menu.append("    <table cellspacing=\"0\" cellpadding=\"0\">\n");
        menu.append(entries);
        menu.append("    </table>\n");
        menu.append("  </td>\n");
        menu.append("</tr>\n");
      }
    }
    return menu.toString();
  }

  /**
   * Generates a table of entries for all the accessible search entries.
   * 
   * @param vars
   * @param direccion
   * @param open
   * @param data
   * @return the table of entries for all accessible search entries
   * @throws ServletException
   */
  private String generateMenuSearchEntries(VariablesSecureApp vars, String direccion, boolean open,
      MenuData[] data) throws ServletException {
    final StringBuffer result = new StringBuffer();
    if (data == null || data.length == 0)
      return "";

    final AccessData[] accessData = AccessData.selectAccessSearchMultiple(this, vars.getRole());
    final HashMap<String, String> accessMap = new HashMap<String, String>();
    for (final AccessData a : accessData) {
      accessMap.put(a.adReferenceValueId, a.total);
    }
    for (int i = 0; i < data.length; i++) {
      final String res = accessMap.get(data[i].nodeId);
      if ((res != null) && (!res.equals("0"))) {
        result.append("<tr>\n");
        result.append("  <td>\n");
        result
            .append("    <table cellspacing=\"0\" cellpadding=\"0\" onmouseover=\"window.status='");
        result.append(FormatUtilities.replaceJS(data[i].description));
        result.append("';return true;\"");
        result.append(" onmouseout=\"window.status='';return true;\"");
        result.append(" id=\"info").append(FormatUtilities.replace(data[i].name)).append("\"");
        result.append(">\n");
        result.append("      <tr");
        result
            .append(" class=\"Normal NOT_Opened NOT_Hover NOT_Selected NOT_Pressed NOT_Focused\"");
        result.append(" id=\"childinfo").append(FormatUtilities.replace(data[i].name)).append("\"");
        result.append(" onclick=\"checkSelected('childinfo")
            .append(FormatUtilities.replace(data[i].name)).append("');openSearch(null, null, '");
        final String javaClassName = data[i].classname.trim();
        result.append(direccion).append(javaClassName);
        result
            .append("', null, false);return false;\" onmouseover=\"setMouseOver(this);return true;\" onmouseout=\"setMouseOut(this); return true;\"");
        result
            .append(" onmousedown=\"setMouseDown(this);return true;\" onmouseup=\"setMouseUp(this);return true;\">\n");
        result
            .append("        <td width=\"5px\"><img src=\"")
            .append(strReplaceWith)
            .append(
                "/images/blank.gif\" class=\"Menu_Client_Button_Icon Menu_Client_Button_Icon_childInfo\"></td>\n");
        result.append("        <td nowrap=\"\">");
        result.append(data[i].name);
        result.append("        </td>\n");
        result.append("      </tr>\n");
        result.append("    </table>\n");
        result.append("  </td>\n");
        result.append("</tr>\n");
      }
    }
    return result.toString();
  }

  private void decidePopups(XmlDocument xmlDocument, VariablesSecureApp vars)
      throws ServletException {

    final HeartBeatOrRegistration showHeartBeatOrRegistration = HeartbeatProcess
        .isLoginPopupRequired(vars, myPool);

    switch (showHeartBeatOrRegistration) {
    case InstancePurpose:
      xmlDocument.setParameter("popup", "openInstancePurpose();");
      return;
    case HeartBeat:
      xmlDocument.setParameter("popup", "openHeartbeat();");
      return;
    case Registration:
      xmlDocument.setParameter("popup", "openRegistration();");
      return;
    case None:
      xmlDocument.setParameter("popup", "");
      return;
    default:
      throw new IllegalStateException("HeartbeatOrRegistration " + showHeartBeatOrRegistration
          + " not supported");
    }
  }

  @Override
  public String getServletInfo() {
    return "Servlet that presents application's vertical menu";
  } // end of getServletInfo() method
}
