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
import java.util.Collections;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.WindowTrl;
import org.openbravo.model.project.Project;

public class ReferencedLink extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {

      StringBuffer servletURL = new StringBuffer();
      String tabId = getTabId(vars);
      String strKeyReferenceId = vars.getStringParameter("inpKeyReferenceId");

      servletURL.append(Utility.getTabURL(tabId, "E", true));
      servletURL.append("?Command=").append((strKeyReferenceId.equals("") ? "DEFAULT" : "DIRECT"))
          .append("&");
      servletURL.append("inpDirectKey").append("=").append(strKeyReferenceId);

      if (log4j.isDebugEnabled()) {
        log4j.debug(servletURL.toString());
      }

      response.sendRedirect(servletURL.toString());

    } else if (vars.commandIn("JSON")) {
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(getJSON(vars));
      out.close();
    } else
      throw new ServletException();
  }

  private String getJSON(VariablesSecureApp vars) throws ServletException {
    String tabId = getTabId(vars);
    String recordId = vars.getStringParameter("inpKeyReferenceId");
    JSONObject json = null;

    try {
      OBContext.setAdminMode();

      json = new JSONObject();

      Tab tab = OBDal.getInstance().get(Tab.class, tabId);

      json.put("tabId", tabId);
      json.put("windowId", tab.getWindow().getId());

      final Entity entity = ModelProvider.getInstance().getEntity(tab.getTable().getName());

      // special case, find the real recordId for the language case
      if (entity.getName().equals(Language.ENTITY_NAME)) {
        final OBQuery<Language> languages = OBDal.getInstance().createQuery(Language.class,
            Language.PROPERTY_LANGUAGE + "=?");
        languages.setParameters(Collections.singletonList((Object) recordId));
        json.put("recordId", languages.list().get(0).getId());
      } else {
        json.put("recordId", recordId);
      }

      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      String tabTitle = null;
      for (WindowTrl windowTrl : tab.getWindow().getADWindowTrlList()) {
        final String trlLanguageId = (String) DalUtil.getId(windowTrl.getLanguage());
        if (trlLanguageId.equals(userLanguageId)) {
          tabTitle = windowTrl.getName();
        }
      }
      if (tabTitle == null) {
        tabTitle = tab.getWindow().getName();
      }

      json.put("keyParameter",
          "inp" + Sqlc.TransformaNombreColumna(entity.getIdProperties().get(0).getColumnName()));
      json.put("tabTitle", tabTitle);

      // find the model object mapping
      json.put("mappingName", Utility.getTabURL(tabId, "E", false));
    } catch (Exception e) {
      try {
        json.put("error", e.getMessage());
      } catch (JSONException jex) {
        log4j.error("Error trying to generate message: " + jex.getMessage(), jex);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    return json.toString();
  }

  private String getTabId(VariablesSecureApp vars) throws ServletException {
    String strKeyReferenceColumnName = vars.getRequiredStringParameter("inpKeyReferenceColumnName");
    // String strKeyReferenceName =
    // vars.getRequiredStringParameter("inpKeyReferenceName");
    // String strTableId =
    // vars.getRequiredStringParameter("inpTableId");
    String strTableReferenceId;
    if (vars.hasParameter("inpEntityName")) {
      String entityName = vars.getStringParameter("inpEntityName");
      strTableReferenceId = ModelProvider.getInstance().getEntity(entityName).getTableId();
    } else {
      strTableReferenceId = vars.getRequiredStringParameter("inpTableReferenceId");
    }
    String strKeyReferenceId = vars.getStringParameter("inpKeyReferenceId");
    // String strTabId = vars.getStringParameter("inpTabId");
    String strWindowId = vars.getStringParameter("inpwindowId");
    String strTableName = ReferencedLinkData.selectTableName(this, strTableReferenceId);
    boolean isSOTrx = true;

    if (log4j.isDebugEnabled())
      log4j.debug("strKeyReferenceColumnName:" + strKeyReferenceColumnName
          + " strTableReferenceId:" + strTableReferenceId + " strKeyReferenceId:"
          + strKeyReferenceId + " strWindowId:" + strWindowId + " strTableName:" + strTableName);
    {
      ReferencedTables ref = new ReferencedTables(this, strTableReferenceId,
          strKeyReferenceColumnName, strKeyReferenceId);
      if (!ref.hasSOTrx())
        isSOTrx = (Utility.getContext(this, vars, "IsSOTrx", strWindowId).equals("N") ? false
            : true);
      else
        isSOTrx = ref.isSOTrx();
      ref = null;
    }

    // Fixes issue #15723 while the complete implementation defined in #15379 is not ready
    boolean forcedLink;
    try {
      strWindowId = Preferences.getPreferenceValue("ForcedLinkWindow" + strTableName, false,
          vars.getClient(), vars.getOrg(), vars.getUser(), vars.getRole(), strWindowId);
      forcedLink = true;
    } catch (PropertyException e) {
      // Property is not set, follow standard flow
      forcedLink = false;
    }

    if (!forcedLink) {
      String strTableRealReference = strTableReferenceId;
      if (strTableReferenceId.equals("800018")) { // DP
        if (ReferencedTablesData.selectKeyId(this, "C_INVOICE_ID", strTableName,
            strKeyReferenceColumnName, strKeyReferenceId).equals("")) {
          if (!ReferencedTablesData.selectKeyId(this, "C_ORDER_ID", strTableName,
              strKeyReferenceColumnName, strKeyReferenceId).equals("")) {
            strTableRealReference = ReferencedTablesData.selectTableId(this, "C_Order");
          } else {
            strTableRealReference = ReferencedTablesData.selectTableId(this, "C_Settlement");
            strTableReferenceId = "800021";
          }
        }
      }
      if (strTableReferenceId.equals("203")) {
        // Project: select window depending on the project type
        try {
          OBContext.setAdminMode();
          Project referencedProject = OBDal.getInstance().get(Project.class, strKeyReferenceId);
          if (referencedProject != null && referencedProject.getProjectCategory() != null
              && referencedProject.getProjectCategory().equals("S")) {
            // Multiphase project
            strWindowId = "130";
          } else {
            // Service project
            strWindowId = "800001";
          }
        } finally {
          OBContext.restorePreviousMode();
        }

      } else {
        // Standard case, select window based on table definition and isSOTrx
        ReferencedLinkData[] data = ReferencedLinkData.selectWindows(this, strTableRealReference);
        if (data == null || data.length == 0)
          throw new ServletException("Window not found");

        // only in case an adWindowId is returned
        if (!data[0].adWindowId.equals("")) {
          strWindowId = data[0].adWindowId;
        }
        if (!isSOTrx && !data[0].poWindowId.equals("")) {
          strWindowId = data[0].poWindowId;
        }
      }
    }
    ReferencedLinkData[] data = ReferencedLinkData.select(this, strWindowId, strTableReferenceId);
    if (data == null || data.length == 0)
      throw new ServletException("Window not found: " + strWindowId);
    String tabId = data[0].adTabId;
    if (strKeyReferenceId.equals("")) {
      data = ReferencedLinkData.selectParent(this, strWindowId);
      if (data == null || data.length == 0)
        throw new ServletException("Window parent not found: " + strWindowId);
      tabId = data[0].adTabId;
    }
    return tabId;
  }

  public String getServletInfo() {
    return "Servlet that presents the referenced links";
  } // end of getServletInfo() method
}
