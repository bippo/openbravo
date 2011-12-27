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
 * All portions are Copyright (C) 2008-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.xmlEngine.XmlEngine;

/**
 * Manages a generic tree, this class is abstract, for concrete trees subclasses are needed.
 * 
 * It is able to show a HTML component with the tree, to expand nodes Ajax calls are used, these
 * calls are managed by GenericTreeServlet.
 * 
 * <br>
 * Usage</br> This class cannot be directly used, instead a subclass implementing the abstract
 * method must be used.
 * 
 * The purpose is to set the toHml directly in the HTML where the tree is going to be displayed,
 * additionally genericTree.js must be imported in that page. These java classes will show the tree
 * and manage the ajax requests.
 * 
 * <br>
 * To take into account</br>
 * 
 * -data is a FieldProvider that must have the following fields: -nodeID : It is the unique
 * identifier for the node -name : It is the short name for the node (it will be displayed in the
 * tree) -display : It indicates whether the node has child elements in order to show or not the
 * expand/collapse button, the values it can contain are: -block: To show the button -none : Not to
 * show it -linkname : In case this element is passed a link will appear next to the node name,
 * linkname is the text to display in this link. -linkclick: Is the onclick action that will have
 * the link. -levelno : It is the deep level for the current node -icon : It is the css class to
 * display the main icon for the node -icon2 : It is another css class to display another icon over
 * the main icon Additionally to these fields that must be populated by the subclasses, some helper
 * methods that can be passed as empty String and because will be caculated by the super class are
 * needed, they are: -leveltree: It is a set of divs to move the current node depending on its deep.
 * -endline: Is a div at the end of the dotted line -position: It is the node position relative to
 * the tree in order to allow keyboard navigation -It is necessary to implement in subclasses apart
 * of the abstract method a constructor without parameters, this constructor is used by
 * GenericTreeServlet.
 * 
 */
public abstract class GenericTree {
  protected XmlEngine xmlEngine;
  private boolean isSubTree;
  protected ConnectionProvider conn;
  protected FieldProvider[] data;
  protected String lang = "";
  private String HTMLNotifications = "";
  private String genericTreeSize = "";
  private String genericDescriptionSize = "90";
  private boolean showNotifications = false;

  /**
   * This method will assign to data the root nodes for the tree
   */
  protected abstract void setRootTree();

  /**
   * This method will assign to data the child nodes (just one level) for the nodeId
   * 
   * @param nodeId
   *          Root node for the subtree
   */
  protected abstract void setSubTree(String nodeId, String level);

  /**
   * This method returns a String with the description for the node
   * 
   * @param node
   *          Node ID to retrieve description
   * @return The String with the HTML for the description
   */
  public abstract String getHTMLDescription(String node);

  /**
   * Returns true in case the node is the last one at its level
   * 
   * @param nodeID
   * @return true in case the node is the last one at its level
   */
  protected abstract boolean isLastLevelNode(String nodeID);

  /**
   * Returns the position relative to the rest of nodes at the same level
   * 
   * @param nodeID
   * @return the position relative to the rest of nodes at the same level
   */
  protected abstract String getNodePosition(String nodeID);

  /**
   * Returns the node id for the parent of the passed node
   * 
   * @param node
   * @return the node id for the parent of the passed node
   */
  protected abstract String getParent(String node);

  /**
   * Default constructor without parameters. It is needed to be able to create instances by
   * GenericTreeServlet, it must be implemented also by subclases.
   */
  public GenericTree() {
  }

  /**
   * This constructor receives a HttpBaseServlet object to set the infrastructure parameters
   * 
   * @param base
   */
  public GenericTree(HttpBaseServlet base) {
    setParameters(base);
  }

  /**
   * This constructor receives a HttpBaseServlet object to set the infrastructure parameters
   * 
   * @param base
   * @param bSmall
   *          Normal size or small size (true)
   */
  public GenericTree(HttpBaseServlet base, boolean bSmall) {
    setParameters(base);
    if (bSmall) {
      genericTreeSize = "_Small";
      genericDescriptionSize = "45";
    }
  }

  /**
   * Sets the infrastructure parameters from the HttpBaseServlet object passed.
   * 
   * @param base
   *          HttpBaseServlet object to obtain the parameters from
   */
  public void setParameters(HttpBaseServlet base) {
    conn = base;
    xmlEngine = base.xmlEngine;
    isSubTree = false;
  }

  /**
   * Sets the isSubTree variable to the passed value. Subtrees have different treatment in
   * interface.
   * 
   * @param value
   */
  protected void setIsSubTree(boolean value) {
    isSubTree = value;
  }

  /**
   * @return a String with the HTML with all the structure for the tree.
   */
  public String toHtml() {
    if (data == null || data.length == 0)
      return "";

    XmlDocument xmlDocument;
    if (!isSubTree) {
      String[] discard = { "" };
      if (!showNotifications)
        discard[0] = "notifArea";
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/GenericTreeHeader",
          discard).createXmlDocument();
      XmlDocument xmlDocumentTree = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/utility/GenericTree").createXmlDocument();
      xmlDocumentTree.setData("structureTree", data);
      xmlDocument.setParameter("inpTreeClass", this.getClass().getName());
      xmlDocument.setParameter("tree", xmlDocumentTree.print());
      xmlDocument.setParameter("notifications", HTMLNotifications);
      xmlDocument.setParameter("genericTree", genericTreeSize);
    } else {
      String[] discard = { "inpNodes_xx" }; // remove check-box for
      // subtree nodes
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/GenericTree",
          discard).createXmlDocument();
      xmlDocument.setData("structureTree", data);
    }
    return xmlDocument.print();
  }

  /**
   * Sets the language
   * 
   * @param lang
   */
  public void setLanguage(String lang) {
    this.lang = lang;
  }

  public String descriptionToHtml() {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/utility/GenericTreeDescription").createXmlDocument();
    xmlDocument.setParameter("genericDescriptionSize", genericDescriptionSize);
    return xmlDocument.print();
  }

  /**
   * Set the deep level for the current set of nodes
   * 
   * @param l
   */
  protected void setLevel(int l) {
    if (data == null || data.length == 0)
      return;
    for (int i = 0; i < data.length; i++) {
      // endline: set the spotted right line with angles
      if (!isSubTree && i == 0) {
        FieldProviderFactory.setField((FieldProvider) data[i], "endline", "Tree_FirstNode_Spots");
      } else if (i == (data.length - 1)) {
        FieldProviderFactory.setField((FieldProvider) data[i], "endline", "Tree_LastNode_Spots");
      } else {
        FieldProviderFactory.setField((FieldProvider) data[i], "endline", "Tree_MiddleNode_Spots");
      }

      // Level tree: sets the spotted left lines
      FieldProviderFactory.setField(data[i], "leveltree", "");
      FieldProviderFactory.setField(data[i], "levelno", (new Integer(l).toString()));
      String parentID = data[i].getField("nodeId"); // set the current
      // node to look for
      // its parents
      for (int j = l - 1; j >= 0; j--) {
        parentID = getParent(parentID);
        String divClass = "Tree_" + (isLastLevelNode(parentID) ? "LastParent" : "MiddleParent")
            + "_Spots";
        FieldProviderFactory.setField(data[i], "leveltree", "<div class=\"" + divClass
            + "\"></div>" + data[i].getField("leveltree"));
      }

      // Position: this is in order to be able to use keyboard to navigate
      // in the tree UI
      String position = "";
      parentID = data[i].getField("nodeId"); // set the current node to
      // look for its parents
      for (int j = l; j >= 0; j--) {

        position = getNodePosition(parentID) + (position.length() == 0 ? "" : ".") + position;
        parentID = getParent(parentID);
      }
      FieldProviderFactory.setField(data[i], "position", position);
    }
  }

  /**
   * Sets information for the notifications area.
   * 
   * @param notifications
   *          HTML string with the text and link
   */
  public void setNotifications(String notifications) {
    HTMLNotifications = notifications;
  }

  /**
   * @return the data
   */
  public FieldProvider[] getData() {
    return data;
  }

  public void showNotifications(boolean notif) {
    showNotifications = notif;
  }

}
