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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.Sqlc;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * @author Fernando Iriazabal
 * 
 *         Manage the composition of the tree data for the tree window types.
 */
public class WindowTree extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final String CHILD_SHEETS = "frameWindowTreeF3";

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // Checking the window invoking the tree is accessible
    if (!hasGeneralAccess(vars, "W", vars.getStringParameter("inpTabId"))) {
      bdErrorGeneralPopUp(request, response, Utility.messageBD(this, "Error", vars.getLanguage()),
          Utility.messageBD(this, "AccessTableNoView", vars.getLanguage()));
    }

    if (vars.commandIn("DEFAULT", "TAB")) {
      String strTabId = vars.getGlobalVariable("inpTabId", "WindowTree|tabId");
      String strTreeID = "";
      String key = WindowTreeData.selectKey(this, strTabId);
      {
        String TreeType = WindowTreeUtility.getTreeType(key);
        WindowTreeData[] data = WindowTreeData.selectTreeID(this,
            Utility.getContext(this, vars, "#User_Client", ""), TreeType);
        if (data != null && data.length > 0)
          strTreeID = data[0].id;
      }
      if (strTreeID.equals(""))
        advisePopUp(request, response, "ERROR",
            Utility.messageBD(this, "Error", vars.getLanguage()),
            Utility.messageBD(this, "AccessTableNoView", vars.getLanguage()));
      else
        printPageDataSheet(response, vars, strTabId);
    } else if (vars.commandIn("ASSIGN")) {
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strTop = vars.getRequiredStringParameter("inpTop");
      String strLink = vars.getRequiredStringParameter("inpLink");
      String strChild = vars.getStringParameter("inpChild", "N");
      String strResult = WindowTreeChecks.checkChanges(this, vars, strTabId, strTop, strLink,
          strChild.equals("Y"));
      if (strResult.equals(""))
        changeNode(vars, strTabId, strTop, strLink, strChild);
      else {
        vars.setSessionValue("WindowTree|message", strResult);
      }
      vars.setSessionValue("WindowTree|tabId", strTabId);
      PrintWriter out = response.getWriter();

      if (strResult != "") {
        // create OBError and serizalize it using JSON
        OBError error = new OBError();
        error.setType("Error");
        error.setTitle("Error");
        error.setMessage(strResult);
        XStream xs = new XStream(new JettisonMappedXmlDriver());
        xs.alias("OBError", OBError.class);
        strResult = xs.toXML(error);
      }

      out.print(strResult);
      out.close();
    } else
      throw new ServletException();
  }

  /**
   * Main method to build the html for the tree.
   * 
   * @param vars
   *          Handler for the session info.
   * @param key
   *          key column name.
   * @param editable
   *          is editable?
   * @param strTabId
   *          id of the tab.
   * @return String html with the tree.
   * @throws ServletException
   */
  private String loadNodes(VariablesSecureApp vars, String key, boolean editable, String strTabId)
      throws ServletException {
    String TreeType = WindowTreeUtility.getTreeType(key);
    String TreeID = "";
    String TreeName = "";
    String TreeDescription = "";
    WindowTreeData[] data = WindowTreeData.selectTreeID(this,
        Utility.getContext(this, vars, "#User_Client", ""), TreeType);
    StringBuffer menu = new StringBuffer();
    if (data == null || data.length == 0) {
      log4j.error("WindowTree.loadNodes() - Unknown TreeNode: TreeType " + TreeType + " - TreeKey "
          + key);
      throw new ServletException("WindowTree.loadNodes() - Unknown TreeNode");
    } else {
      TreeID = data[0].id;
      TreeName = data[0].name;
      TreeDescription = data[0].description;
    }
    if (log4j.isDebugEnabled())
      log4j.debug("WindowTree.loadNodes() - TreeType: " + TreeType + " || TreeID: " + TreeID);
    menu.append("\n<ul class=\"dhtmlgoodies_tree\">\n");
    menu.append(WindowTreeUtility.addNodeElement(TreeName, TreeDescription, CHILD_SHEETS, true, "",
        strDireccion, "clickItem(0, '" + Replace.replace(TreeName, "'", "\\'") + "', 'N');",
        "dblClickItem(0);", true, "0", ""));
    WindowTreeData[] wtd = WindowTreeUtility.getTree(this, vars, TreeType, TreeID, editable, "",
        "", strTabId);
    Map<String, List<WindowTreeData>> wtdTree = buildTree(wtd);
    menu.append(generateTree(wtd, wtdTree, strDireccion, "0", true));
    menu.append("\n</ul>\n");
    return menu.toString();
  }

  /**
   * Generates the tree for the html.
   * 
   * @param data
   *          Array with the tree elements.
   * @param strDireccion
   *          String with the path for the urls.
   * @param indice
   *          String with the index.
   * @param isFirst
   *          Indicates if is the first or not.
   * @return String html with the tree.
   */
  private String generateTree(WindowTreeData[] data, Map<String, List<WindowTreeData>> wtdTree,
      String strDireccion, String indice, boolean isFirst) {
    if (data == null || data.length == 0)
      return "";
    if (log4j.isDebugEnabled())
      log4j.debug("WindowTree.generateTree() - data: " + data.length);
    if (indice == null)
      indice = "0";
    boolean hayDatos = false;
    StringBuffer strResultado = new StringBuffer();
    strResultado.append("<ul>");
    isFirst = false;
    List<WindowTreeData> subList = wtdTree.get(indice);
    if (subList != null) {
      for (WindowTreeData elem : subList) {
        hayDatos = true;
        String strHijos = generateTree(data, wtdTree, strDireccion, elem.nodeId, isFirst);
        strResultado.append(WindowTreeUtility.addNodeElement(elem.name, elem.description,
            CHILD_SHEETS, elem.issummary.equals("Y"), WindowTreeUtility.windowType(elem.action),
            strDireccion,
            "clickItem('" + elem.nodeId + "', '" + Replace.replace(elem.name, "'", "\\'") + "', '"
                + elem.issummary + "');", "dblClickItem('" + elem.nodeId + "');",
            !strHijos.equals(""), elem.nodeId, elem.action));
        strResultado.append(strHijos);
      }
    }
    strResultado.append("</li></ul>");
    return (hayDatos ? strResultado.toString() : "");
  }

  private static Map<String, List<WindowTreeData>> buildTree(WindowTreeData[] input) {
    Map<String, List<WindowTreeData>> resMap = new HashMap<String, List<WindowTreeData>>();

    for (WindowTreeData elem : input) {
      List<WindowTreeData> list = resMap.get(elem.parentId);
      if (list == null) {
        list = new ArrayList<WindowTreeData>();
      }
      list.add(elem);
      resMap.put(elem.parentId, list);
    }

    return resMap;
  }

  /**
   * Prints the tree page.
   * 
   * @param response
   *          Handler to the response.
   * @param vars
   *          Handler for the session info.
   * @param TabId
   *          Tab id.
   * @throws IOException
   * @throws ServletException
   */
  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String TabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Tree's screen for the tab: " + TabId);
    OBError defaultInfo = new OBError();
    defaultInfo.setType("INFO");
    defaultInfo.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
    defaultInfo.setMessage(Utility.messageBD(this, "TreeInfo", vars.getLanguage()));
    vars.setMessage("WindowTree", defaultInfo);

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/WindowTree").createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    String strTreeID = "";
    String key = WindowTreeData.selectKey(this, TabId);
    {
      String TreeType = WindowTreeUtility.getTreeType(key);
      WindowTreeData[] data = WindowTreeData.selectTreeID(this,
          Utility.getContext(this, vars, "#User_Client", ""), TreeType);
      if (data != null && data.length > 0)
        strTreeID = data[0].id;
    }
    WindowTreeData[] data = WindowTreeData.selectTabName(this, TabId);

    xmlDocument.setParameter("description", data[0].name);
    xmlDocument.setParameter("page", Utility.getTabURL(TabId, "E", true));
    xmlDocument.setParameter("menu",
        loadNodes(vars, key, WindowTreeData.selectEditable(this, TabId).equals("Y"), TabId));
    xmlDocument.setParameter("treeID", strTreeID);
    xmlDocument.setParameter("tabID", TabId);
    key = "inp" + Sqlc.TransformaNombreColumna(key);
    xmlDocument.setParameter("keyField", key);
    xmlDocument.setParameter("keyFieldScript",
        "function getKeyField() {\n return document.frmMain." + key + ";\n}\n");

    try {
      OBError myMessage = vars.getMessage("WindowTree");
      vars.removeMessage("WindowTree");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Makes the change of position of the elements in the tree.
   * 
   * It positions the node and moves down all the nodes after this one.
   * 
   * For menu tree it looks whether the current node and the ones after that are within a module in
   * development. It searchs the first node after the current one that is not in development and
   * modifies the seqno for the current one (regardless it is in development or not) and the ones
   * bellow it that are in development.
   * 
   * @param vars
   *          Handler for the session info.
   * @param strTabId
   *          Tab id.
   * @param strTop
   *          Parent node id.
   * @param strLink
   *          Id of the node to change.
   * @param strChild
   *          String indicating if is a child or not of the parent node (Y|N).
   * @throws ServletException
   */
  private void changeNode(VariablesSecureApp vars, String strTabId, String strTop, String strLink,
      String strChild) throws ServletException {
    String key = WindowTreeData.selectKey(this, strTabId);
    String TreeType = WindowTreeUtility.getTreeType(key);
    String TreeID = "";
    String strParent = strTop;
    boolean editable = WindowTreeData.selectEditable(this, strTabId).equals("Y");
    // Calculating the TreeID
    {
      WindowTreeData[] data = WindowTreeData.selectTreeID(this,
          Utility.getContext(this, vars, "#User_Client", ""), TreeType);
      if (data == null || data.length == 0) {
        log4j.error("WindowTree.loadNodes() - Unknown TreeNode");
        throw new ServletException("WindowTree.loadNodes() - Unknown TreeNode");
      } else {
        TreeID = data[0].id;
      }
    }
    // Calculating the parent
    if (!strTop.equals("0")) {
      WindowTreeData[] data = WindowTreeUtility.getTree(this, vars, TreeType, TreeID, editable, "",
          strTop, strTabId);
      if (data == null || data.length == 0) {
        log4j.error("WindowTree.loadNodes() - Unknown Top Node");
        throw new ServletException("WindowTree.loadNodes() - Unknown Top Node");
      }

      if (!data[0].issummary.equals("Y") || !strChild.equals("Y")) {
        strParent = data[0].parentId;
      }
    } else
      strParent = strTop;
    WindowTreeData[] data = WindowTreeUtility.getTree(this, vars, TreeType, TreeID, editable,
        strParent, "", strTabId);
    int seqNo = 0;
    int add = 10;
    try {
      if (data == null || data.length == 0) {
        WindowTreeUtility.setNode(this, vars, TreeType, TreeID, strParent, strLink,
            Integer.toString(seqNo));
      } else {
        boolean updated = false;
        boolean finish = false;
        if (strParent.equals(strTop)) {
          seqNo += add;
          WindowTreeUtility.setNode(this, vars, TreeType, TreeID, strParent, strLink,
              Integer.toString(seqNo));
          updated = true;
        }
        for (int i = 0; !finish && i < data.length; i++) {
          if (!data[i].nodeId.equals(strLink)) {

            if (updated && !finish) { // update only elements after
              // the current one
              if (data[i].isindevelopment == null || data[i].isindevelopment.equals("")
                  || data[i].isindevelopment.equals("Y")) {
                seqNo += add;
                WindowTreeUtility.setNode(this, vars, TreeType, TreeID, data[i].parentId,
                    data[i].nodeId, Integer.toString(seqNo));
              } else {
                finish = true; // update elements till one is
                // not in developement, then
                // finish
              }
            }

            if (!updated && data[i].nodeId.equals(strTop)) {

              // Calculate the addition for the range of modules
              // in development
              int j = 0;

              for (j = i + 1; j < data.length
                  && (data[j].isindevelopment == null || data[j].isindevelopment.equals("") || data[j].isindevelopment
                      .equals("Y")); j++)
                ;
              if (j == data.length)
                add = 10; // it is at the end it can be expanded
              // without problem
              else
                add = new Float(
                    ((new Integer(data[j].seqno) - new Integer(data[i].seqno)) / (j - i + 1)))
                    .intValue();

              // Set the current node in its posisiton
              if (i == 0)
                seqNo = 10;
              else
                seqNo = new Integer(data[i].seqno).intValue() + add;
              WindowTreeUtility.setNode(this, vars, TreeType, TreeID, strParent, strLink,
                  Integer.toString(seqNo));
              updated = true;
            }
          }
        }
        if (!updated)
          WindowTreeUtility.setNode(this, vars, TreeType, TreeID, strParent, strLink,
              Integer.toString(seqNo));
      }
    } catch (ServletException e) {
      log4j.error("WindowTree.changeNode() - Couldn't change the node: " + strLink);
      log4j.error("WindowTree.setNode() - error: " + e);
      throw new ServletException(e);
    }
  }

  public String getServletInfo() {
    return "Servlet that presents the tree of a TreeNode windo windoww";
  } // end of getServletInfo() method
}
