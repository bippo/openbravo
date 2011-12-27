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

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.utils.Replace;

/**
 * @author Fernando Iriazabal
 * 
 *         Utility class for the window tree type.
 */
class WindowTreeUtility {
  static Logger log4j = Logger.getLogger(WindowTreeUtility.class);

  /**
   * Gets the tree type.
   * 
   * @param keyColumnName
   *          Name of the column key.
   * @return String with the tree type.
   */
  public static String getTreeType(String keyColumnName) {
    if (log4j.isDebugEnabled())
      log4j.debug("WindowTreeUtility.getTreeID() - key Column: " + keyColumnName);
    if (keyColumnName == null || keyColumnName.length() == 0)
      return "";
    String TreeType = "";
    if (keyColumnName.equals("AD_Menu_ID"))
      TreeType = "MM";
    else if (keyColumnName.equals("C_ElementValue_ID"))
      TreeType = "EV";
    else if (keyColumnName.equals("C_BPartner_ID"))
      TreeType = "BP";
    else if (keyColumnName.equals("AD_Org_ID"))
      TreeType = "OO";
    else if (keyColumnName.equals("C_Project_ID"))
      TreeType = "PJ";
    else if (keyColumnName.equals("M_Product_Category_ID"))
      TreeType = "PC";
    else if (keyColumnName.equals("M_BOM_ID"))
      TreeType = "BB";
    else if (keyColumnName.equals("C_SalesRegion_ID"))
      TreeType = "SR";
    else if (keyColumnName.equals("C_Campaign_ID"))
      TreeType = "MC";
    else if (keyColumnName.equals("C_Activity_ID"))
      TreeType = "AY";
    else if (keyColumnName.equals("AD_Accountingrpt_Element_ID"))
      TreeType = "AR";
    else if (keyColumnName.equals("C_Tax_Report_ID"))
      TreeType = "TR";
    else
      TreeType = "";
    if (TreeType.equals(""))
      log4j.error("WindowTreeUtility.getTreeID() - Could not map " + keyColumnName);
    return TreeType;
  }

  /**
   * Gets the array with the tree elements.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param TreeType
   *          The type of tree.
   * @param TreeID
   *          The id of the tree.
   * @param editable
   *          is editable?
   * @param strParentID
   *          Parent id node (optional).
   * @param strNodeId
   *          The node to search (optional).
   * @param strTabID
   *          Id of the tab.
   * @return Array with the tree elements.
   * @throws ServletException
   */
  public static WindowTreeData[] getTree(ConnectionProvider conn, VariablesSecureApp vars,
      String TreeType, String TreeID, boolean editable, String strParentID, String strNodeId,
      String strTabID) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("WindowTreeUtility.getTree() - TreeID: " + TreeID);
    WindowTreeData[] data = null;
    String strEditable = (editable ? "editable" : "");
    if (TreeType.equals("MM")) {
      data = WindowTreeData.selectTrl(conn, vars.getLanguage(), strEditable, strParentID,
          strNodeId, TreeID);
    } else if (TreeType.equals("OO"))
      data = WindowTreeData.selectOrg(conn, strEditable, strParentID, strNodeId, TreeID);
    else if (TreeType.equals("PC"))
      data = WindowTreeData
          .selectProductCategory(conn, strEditable, strParentID, strNodeId, TreeID);
    else if (TreeType.equals("BB"))
      data = WindowTreeData.selectBOM(conn, strEditable, strParentID, strNodeId, TreeID);
    else if (TreeType.equals("EV")) {
      String strElementId = vars.getSessionValue(Utility.getWindowID(conn, strTabID)
          + "|C_Element_ID");
      data = WindowTreeData.selectElementValue(conn, vars.getLanguage(), strEditable, strParentID,
          strNodeId, TreeID, strElementId);
    } else if (TreeType.equals("MC"))
      data = WindowTreeData.selectCampaign(conn, strEditable, strParentID, strNodeId, TreeID);
    else if (TreeType.equals("PJ"))
      data = WindowTreeData.selectProject(conn, strEditable, strParentID, strNodeId, TreeID);
    else if (TreeType.equals("AY"))
      data = WindowTreeData.selectActivity(conn, strEditable, strParentID, strNodeId, TreeID);
    else if (TreeType.equals("SR"))
      data = WindowTreeData.selectSalesRegion(conn, strEditable, strParentID, strNodeId, TreeID);
    else if (TreeType.equals("AR"))
      data = WindowTreeData.selectAccountingReport(conn, strEditable, strParentID, strNodeId,
          TreeID);
    else if (TreeType.equals("TR"))
      data = WindowTreeData.selectTaxReport(conn, strEditable, strParentID, strNodeId, TreeID);

    return data;
  }

  /**
   * Auxiliar method to transform a FieldProvider into a WindowTreeData object.
   * 
   * @param data
   *          FieldProvider to transform.
   * @return WindowTreeData object.
   * @throws ServletException
   */
  public static WindowTreeData transformFieldProvider(FieldProvider data) throws ServletException {
    WindowTreeData aux = new WindowTreeData();
    aux.nodeId = data.getField("nodeId");
    aux.parentId = data.getField("parentId");
    aux.seqno = data.getField("seqno");
    aux.id = data.getField("id");
    aux.name = data.getField("name");
    aux.description = data.getField("description");
    aux.issummary = data.getField("issummary");
    aux.action = data.getField("action");
    aux.adWindowId = data.getField("adWindowId");
    aux.adProcessId = data.getField("adProcessId");
    aux.adFormId = data.getField("adFormId");
    return aux;
  }

  /**
   * Adds an html node structure. Used by the WindowTree java to build the html tree.
   * 
   * @param name
   *          Text to display.
   * @param description
   *          Description.
   * @param target
   *          Target to open link (deprecated).
   * @param isSummary
   *          If is a folder.
   * @param windowType
   *          Type of window.
   * @param strDirection
   *          Base path for the urls.
   * @param strOnClick
   *          Command for the onclick event.
   * @param strOnDblClick
   *          Command for the onDblClick event.
   * @param hasChilds
   *          Boolean to know if has any child.
   * @param nodeId
   *          Id of the node.
   * @param action
   *          Type of element.
   * @return String html with the node.
   */
  public static String addNodeElement(String name, String description, String target,
      boolean isSummary, String windowType, String strDirection, String strOnClick,
      String strOnDblClick, boolean hasChilds, String nodeId, String action) {
    if (log4j.isDebugEnabled())
      log4j.debug("WindowTreeUtility.addNodeElement() - name: " + name);
    StringBuffer element = new StringBuffer();
    strOnClick = Replace.replace(strOnClick, "\"", "&quot;");
    strOnDblClick = Replace.replace(strOnDblClick, "\"", "&quot;");

    if (isSummary) {
      element
          .append("<li id=\"folder")
          .append((hasChilds ? "" : "NoChilds"))
          .append("__")
          .append(nodeId)
          .append("\"")
          .append(
              (nodeId.equals("0") ? " noDrag=\"true\" noSiblings=\"true\" noDelete=\"true\" noRename=\"true\" "
                  : "")).append(">\n");
      element.append("<a href=\"#\" id=\"folderHref").append((hasChilds ? "" : "NoChilds"))
          .append("__").append(nodeId).append("\"");

      if (strOnDblClick != null && !strOnDblClick.equals("")) {
        element.append(" onclick=\"").append(strOnDblClick).append("return true;\"");
      }
      element.append(">").append(name).append("</a>\n");
      // element.append("</li>\n");
    } else {
      element.append("<li id=\"").append(windowTypeNico(action)).append("__").append(nodeId)
          .append("\" isElement=\"true\"").append(" noChildren=\"true\"").append(">\n");
      element.append("<a href=\"#\" id=\"child").append(windowTypeNico(action)).append("__")
          .append(nodeId).append("\"");

      if (strOnDblClick != null && !strOnDblClick.equals("")) {
        element.append(" onclick=\"").append(strOnDblClick).append("return true;\"");
      }
      element.append(">").append(name).append("</a>\n");
      element.append("</li>\n");
    }
    return element.toString();
  }

  /**
   * Type for the class of the html element.
   * 
   * @param type
   *          Database type.
   * @return String with the css type.
   */
  public static String windowType(String type) {
    if (log4j.isDebugEnabled())
      log4j.debug("WindowTreeUtility.windowType() - type: " + type);
    if (type == null || type.equals(""))
      return "";
    else if (type.equals("W"))
      return "Ventanas";
    else if (type.equals("X"))
      return "Formularios";
    else if (type.equals("P"))
      return "Procesos";
    else if (type.equals("T"))
      return "Tareas";
    else if (type.equals("R"))
      return "Informes";
    else if (type.equals("F"))
      return "FlujoTrabajo";
    else if (type.equals("B"))
      return "BancoTrabajo";
    else
      return "";
  }

  /**
   * Type for the id of the html element. Used in the test application.
   * 
   * @param tipo
   *          Database type.
   * @return String with the id type.
   */
  public static String windowTypeNico(String tipo) {
    if (log4j.isDebugEnabled())
      log4j.debug("WindowTreeUtility.windowTypeNico() - type: " + tipo);
    if (tipo == null)
      return "";
    else if (tipo.equals("W"))
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
    else
      return "";
  }

  /**
   * Sets the node in the specified position.
   * 
   * @param conn
   *          Handler for the database connection.
   * @param vars
   *          Handler for the session info.
   * @param TreeType
   *          Type of tree.
   * @param TreeID
   *          Id of the tree.
   * @param strParentID
   *          Parent node id.
   * @param strLink
   *          Actual node id.
   * @param strSeqNo
   *          Sequence number.
   * @throws ServletException
   */
  public static void setNode(ConnectionProvider conn, VariablesSecureApp vars, String TreeType,
      String TreeID, String strParentID, String strLink, String strSeqNo) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("WindowTreeUtility.setNode() - TreeID: " + TreeID);

    WindowTreeData.update(conn, vars.getUser(), strParentID, strSeqNo, TreeID, strLink);
  }

}
