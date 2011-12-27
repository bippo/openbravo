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
 * All portions are Copyright (C) 2001-2010 Openbravo S.L.U.
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.xmlEngine.XmlDocument;

public class ShowSessionVariables extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("REMOVE")) {
      String preferences = vars.getRequestGlobalVariable("inpPreference",
          "ShowSessionVariables|preferences");
      String global = vars.getRequestGlobalVariable("inpGlobal", "ShowSessionVariables|global");
      String accounting = vars.getRequestGlobalVariable("inpAccounting",
          "ShowSessionVariables|accounting");
      String windowG = vars.getRequestGlobalVariable("inpWindowGlobal",
          "ShowSessionVariables|windowGlobal");
      String window = vars.getRequestGlobalVariable("inpWindow", "ShowSessionVariables|window");
      String strSessionValue = vars.getRequiredStringParameter("inpSessionValue");
      vars.removeSessionValue(strSessionValue);
      printPageDataSheet(request, response, vars, preferences, global, accounting, windowG, window);
    } else if (vars.commandIn("FIND")) {
      String preferences = vars.getRequestGlobalVariable("inpPreference",
          "ShowSessionVariables|preferences");
      String global = vars.getRequestGlobalVariable("inpGlobal", "ShowSessionVariables|global");
      String accounting = vars.getRequestGlobalVariable("inpAccounting",
          "ShowSessionVariables|accounting");
      String windowG = vars.getRequestGlobalVariable("inpWindowGlobal",
          "ShowSessionVariables|windowGlobal");
      String window = vars.getRequestGlobalVariable("inpWindow", "ShowSessionVariables|window");
      printPageDataSheet(request, response, vars, preferences, global, accounting, windowG, window);
    } else if (vars.commandIn("SAVE_NEW")) {
      String strNombre = vars.getRequiredStringParameter("inpNombreVariable");
      String strValor = vars.getStringParameter("inpValorVariable");
      vars.setSessionValue(strNombre, strValor);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else if (vars.commandIn("SESSION")) {
      String preferences = vars.getGlobalVariable("inpPreference",
          "ShowSessionVariables|preferences", "Y");
      String global = vars.getGlobalVariable("inpGlobal", "ShowSessionVariables|global", "Y");
      String accounting = vars.getGlobalVariable("inpAccounting",
          "ShowSessionVariables|accounting", "Y");
      String windowG = vars.getGlobalVariable("inpWindowGlobal",
          "ShowSessionVariables|windowGlobal", "Y");
      String window = vars.getGlobalVariable("inpWindow", "ShowSessionVariables|window", "0");
      printPageDataSheet(request, response, vars, preferences, global, accounting, windowG, window);
    } else {
      String preferences = vars.getGlobalVariable("inpPreference",
          "ShowSessionVariables|preferences", "Y");
      String global = vars.getGlobalVariable("inpGlobal", "ShowSessionVariables|global", "Y");
      String accounting = vars.getGlobalVariable("inpAccounting",
          "ShowSessionVariables|accounting", "Y");
      String windowG = vars.getGlobalVariable("inpWindowGlobal",
          "ShowSessionVariables|windowGlobal", "Y");
      String window = vars.getGlobalVariable("inpWindow", "ShowSessionVariables|window", "0");
      printPageDataSheet(request, response, vars, preferences, global, accounting, windowG, window);
    }
  }

  private boolean existsWindow(Vector<Object> windows, String windowId) {
    if (windows.size() == 0)
      return false;
    for (int i = 0; i < windows.size(); i++) {
      String aux = (String) windows.elementAt(i);
      if (aux.equals(windowId))
        return true;
    }
    return false;
  }

  private String windowName(ShowSessionVariablesData[] windows, String windowId) {
    if (windows == null || windowId == null || windowId.equals(""))
      return "";
    for (int i = 0; i < windows.length; i++) {
      if (windows[i].id.equals(windowId))
        return windows[i].name;
    }
    return "";
  }

  private ShowSessionVariablesStructureData[] orderStructure(
      ShowSessionVariablesStructureData[] data, ShowSessionVariablesData[] windows,
      boolean preferences, boolean global, boolean accounting, boolean windowGlobal, String window) {
    ShowSessionVariablesStructureData[] resData = null;
    try {
      Vector<Object> vecPreferences = new Vector<Object>();
      if (preferences && (window.equals("") || window.equals("0"))) {
        for (int i = 0; i < data.length; i++) {
          if (data[i].isPreference && (data[i].window == null || data[i].window.equals(""))) {
            boolean insertado = false;
            data[i].window = "";
            data[i].windowName = "";
            for (int j = 0; j < vecPreferences.size() && !insertado; j++) {
              ShowSessionVariablesStructureData element = (ShowSessionVariablesStructureData) vecPreferences
                  .elementAt(j);
              if (element.name.compareTo(data[i].name) >= 0) {
                vecPreferences.insertElementAt(data[i], j);
                insertado = true;
              }
            }
            if (!insertado) {
              vecPreferences.addElement(data[i]);
            }
          }
        }
      }
      Vector<Object> vecPreferencesW = new Vector<Object>();
      if (preferences && !window.equals("")) {
        for (int i = 0; i < data.length; i++) {
          if (data[i].isPreference
              && (data[i].window != null && !data[i].window.equals("") && (data[i].window
                  .equals(window) || window.equals("0")))) {
            boolean insertado = false;
            data[i].windowName = windowName(windows, data[i].window);
            for (int j = 0; j < vecPreferencesW.size() && !insertado; j++) {
              ShowSessionVariablesStructureData element = (ShowSessionVariablesStructureData) vecPreferencesW
                  .elementAt(j);
              if (element.windowName.compareTo(data[i].windowName) > 0) {
                vecPreferencesW.insertElementAt(data[i], j);
                insertado = true;
              } else if (element.windowName.compareTo(data[i].windowName) == 0) {
                if (element.name.compareTo(data[i].name) >= 0) {
                  vecPreferencesW.insertElementAt(data[i], j);
                  insertado = true;
                }
              }
            }
            if (!insertado) {
              vecPreferencesW.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecGlobal = new Vector<Object>();
      if (global) {
        for (int i = 0; i < data.length; i++) {
          if (data[i].isGlobal) {
            boolean insertado = false;
            data[i].window = "";
            data[i].windowName = "";
            for (int j = 0; j < vecGlobal.size() && !insertado; j++) {
              ShowSessionVariablesStructureData element = (ShowSessionVariablesStructureData) vecGlobal
                  .elementAt(j);
              if (element.name.compareTo(data[i].name) >= 0) {
                vecGlobal.insertElementAt(data[i], j);
                insertado = true;
              }
            }
            if (!insertado) {
              vecGlobal.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecAccounting = new Vector<Object>();
      if (accounting) {
        for (int i = 0; i < data.length; i++) {
          if (data[i].isAccounting) {
            boolean insertado = false;
            data[i].window = "";
            data[i].windowName = "";
            for (int j = 0; j < vecAccounting.size() && !insertado; j++) {
              ShowSessionVariablesStructureData element = (ShowSessionVariablesStructureData) vecAccounting
                  .elementAt(j);
              if (element.name.compareTo(data[i].name) >= 0) {
                vecAccounting.insertElementAt(data[i], j);
                insertado = true;
              }
            }
            if (!insertado) {
              vecAccounting.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecWindowG = new Vector<Object>();
      if (windowGlobal) {
        for (int i = 0; i < data.length; i++) {
          if (!data[i].isAccounting && !data[i].isGlobal && !data[i].isPreference
              && (data[i].window == null || data[i].window.equals(""))) {
            boolean insertado = false;
            data[i].window = "";
            data[i].windowName = "";
            for (int j = 0; j < vecWindowG.size() && !insertado; j++) {
              ShowSessionVariablesStructureData element = (ShowSessionVariablesStructureData) vecWindowG
                  .elementAt(j);
              if (element.name.compareTo(data[i].name) >= 0) {
                vecWindowG.insertElementAt(data[i], j);
                insertado = true;
              }
            }
            if (!insertado) {
              vecWindowG.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecWindow = new Vector<Object>();
      if (!window.equals("")) {
        for (int i = 0; i < data.length; i++) {
          if (!data[i].isAccounting
              && !data[i].isGlobal
              && !data[i].isPreference
              && (data[i].window != null && !data[i].window.equals("") && (data[i].window
                  .equals(window) || window.equals("0")))) {
            boolean insertado = false;
            data[i].windowName = windowName(windows, data[i].window);
            for (int j = 0; j < vecWindow.size() && !insertado; j++) {
              ShowSessionVariablesStructureData element = (ShowSessionVariablesStructureData) vecWindow
                  .elementAt(j);
              if (element.windowName.compareTo(data[i].windowName) > 0) {
                vecWindow.insertElementAt(data[i], j);
                insertado = true;
              } else if (element.windowName.compareTo(data[i].windowName) == 0) {
                if (element.name.compareTo(data[i].name) >= 0) {
                  vecWindow.insertElementAt(data[i], j);
                  insertado = true;
                }
              }
            }
            if (!insertado) {
              vecWindow.addElement(data[i]);
            }
          }
        }
      }

      Vector<Object> vecCompleto = new Vector<Object>();
      for (int i = 0; i < vecPreferences.size(); i++) {
        vecCompleto.addElement(vecPreferences.elementAt(i));
      }
      for (int i = 0; i < vecPreferencesW.size(); i++) {
        vecCompleto.addElement(vecPreferencesW.elementAt(i));
      }
      for (int i = 0; i < vecGlobal.size(); i++) {
        vecCompleto.addElement(vecGlobal.elementAt(i));
      }
      for (int i = 0; i < vecAccounting.size(); i++) {
        vecCompleto.addElement(vecAccounting.elementAt(i));
      }
      for (int i = 0; i < vecWindowG.size(); i++) {
        vecCompleto.addElement(vecWindowG.elementAt(i));
      }
      for (int i = 0; i < vecWindow.size(); i++) {
        vecCompleto.addElement(vecWindow.elementAt(i));
      }
      resData = new ShowSessionVariablesStructureData[vecCompleto.size()];
      vecCompleto.copyInto(resData);

      for (int i = 0; i < resData.length; i++) {
        resData[i].rownum = "" + i;
      }

      if (log4j.isDebugEnabled())
        log4j.debug("ShowSession - orderStructure - Total: " + resData.length + "-"
            + resData[0].name);
    } catch (Exception e) {
      log4j.error("ShowSession - orderStructure - Ordering Session variables error " + e);
    }
    return resData;
  }

  private ShowSessionVariablesStructureData[] compoundSession(HttpServletRequest request,
      VariablesSecureApp vars, Vector<Object> windows) {
    if (log4j.isDebugEnabled())
      log4j.debug("ShowSession - compoundSession - view session");
    ShowSessionVariablesStructureData[] data = null;
    HttpSession session = request.getSession(true);
    Vector<Object> texto = new Vector<Object>();
    try {
      String sessionName;
      Enumeration<?> e = session.getAttributeNames();
      while (e.hasMoreElements()) {
        sessionName = (String) e.nextElement();
        if (log4j.isDebugEnabled())
          log4j.debug("ShowSession - compoundSession - session name: " + sessionName);
        String realName = sessionName;
        ShowSessionVariablesStructureData data1 = new ShowSessionVariablesStructureData();
        if (realName.startsWith("P|")) {
          data1.isPreference = true;
          realName = realName.substring(2);
        }
        if (realName.startsWith("$")) {
          data1.isAccounting = true;
          realName = realName.substring(1);
        }
        if (realName.startsWith("#")) {
          data1.isGlobal = true;
          realName = realName.substring(1);
        }
        int pos = realName.indexOf("|");
        if (pos != -1) {
          data1.window = realName.substring(0, pos);
          if (!existsWindow(windows, data1.window))
            windows.addElement(data1.window);
          realName = realName.substring(pos + 1);
        }

        data1.completeName = sessionName;
        data1.name = realName;
        data1.value = vars.getSessionValue(sessionName);
        texto.addElement(data1);
      }
      data = new ShowSessionVariablesStructureData[texto.size()];
      texto.copyInto(data);
    } catch (Exception e) {
      log4j.error("ShowSession - compoundSession - Session variables error " + e);
    }
    return data;
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String preferences, String global, String accounting,
      String windowG, String window) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("ShowSession - printPageDataSheet - Output: data sheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    Vector<Object> windows = new Vector<Object>();
    ShowSessionVariablesStructureData[] data = compoundSession(request, vars, windows);
    XmlDocument xmlDocument;
    if (data == null || data.length == 0) {
      String[] discard = { "sectionDetail" };
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/ShowSessionVariables", discard).createXmlDocument();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/ShowSessionVariables").createXmlDocument();
    }
    StringBuffer strWindows = new StringBuffer();
    Vector<Object> vecWindows = new Vector<Object>();
    if (windows.size() != 0) {
      strWindows.append("(");
      for (int i = 0; i < windows.size(); i++) {
        String aux = (String) windows.elementAt(i);
        try {
          if (i > 0)
            strWindows.append(", ");
          strWindows.append("'").append(aux).append("'");
        } catch (Exception e) {
          ShowSessionVariablesData d = new ShowSessionVariablesData();
          d.id = aux;
          d.name = aux;
          vecWindows.addElement(d);
        }
      }
      strWindows.append(")");
    }
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ShowSessionVariables", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("preference", preferences);
    xmlDocument.setParameter("accounting", accounting);
    xmlDocument.setParameter("global", global);
    xmlDocument.setParameter("windowGlobal", windowG);
    xmlDocument.setParameter("window", window);
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.ShowSessionVariables");
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ShowSessionVariables.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ShowSessionVariables.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ShowSessionVariables");
      vars.removeMessage("ShowSessionVariables");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    ShowSessionVariablesData[] windowsData = (vars.getLanguage().equals("en_US") ? ShowSessionVariablesData
        .select(this, strWindows.toString()) : ShowSessionVariablesData.selectTrl(this,
        strWindows.toString(), vars.getLanguage()));
    {
      Vector<Object> v = new Vector<Object>();
      ShowSessionVariablesData d = new ShowSessionVariablesData();
      d.id = "0";
      d.name = "All";
      v.addElement(d);
      for (int i = 0; i < windowsData.length; i++) {
        v.addElement(windowsData[i]);
      }
      for (int i = 0; i < vecWindows.size(); i++) {
        v.addElement(vecWindows.elementAt(i));
      }
      windowsData = new ShowSessionVariablesData[v.size()];
      v.copyInto(windowsData);
    }
    data = orderStructure(data, windowsData, preferences.equals("Y"), global.equals("Y"),
        accounting.equals("Y"), windowG.equals("Y"), window);
    xmlDocument.setData("windows", windowsData);
    xmlDocument.setData("structure1", data);

    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet ShowSession. This Servlet was made by Wad constructor";
  } // end of getServletInfo() method
}
