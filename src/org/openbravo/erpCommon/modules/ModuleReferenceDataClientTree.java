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
package org.openbravo.erpCommon.modules;

import javax.servlet.ServletException;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * Manages the tree of installed modules.
 * 
 * It implements GenericTree, detailed description is in that API doc.
 */
public class ModuleReferenceDataClientTree extends ModuleTree {

  /**
   * Default constructor without parameters. It is needed to be able to create instances by
   * GenericTreeServlet, it must be implemented also by subclases.
   */
  public ModuleReferenceDataClientTree() {
  }

  /**
   * Constructor to generate a root tree
   * 
   * @param base
   * @param bSmall
   *          Normal size or small size (true)
   */
  public ModuleReferenceDataClientTree(HttpBaseServlet base, boolean bSmall) {
    super(base, bSmall);
    setRootTree();
  }

  /**
   * sets to data the root tree
   */
  public void setRootTree() {
    try {
      data = ModuleReferenceDataClientTreeData.select(conn, (lang.equals("") ? "en_US" : lang));
      // addLinks();
      setLevel(0);
      setIcons();
    } catch (ServletException ex) {
      ex.printStackTrace();
      data = null;
    }
  }

  protected void setLevel(int level) {
    super.setLevel(level);

    // set position with the current data, not the one in the Module tree
    for (int pos = 0; pos < data.length; pos++) {
      FieldProviderFactory.setField(data[pos], "position", Integer.toString(pos + 1));
    }
  }

  /**
   * Generates a subtree with nodeId as root node
   * 
   * @param nodeId
   */
  public void setSubTree(String nodeId, String level) {
    setIsSubTree(true);
    try {
      data = ModuleReferenceDataClientTreeData.selectSubTree(conn, (lang.equals("") ? "en_US"
          : lang), nodeId);
      // addLinks();
      setLevel(new Integer(level).intValue());
      setIcons();
    } catch (ServletException ex) {
      ex.printStackTrace();
      data = null;
    }
  }

  /**
   * Returns a HTML with the description for the given node
   * 
   * @param node
   * @return a HTML String with the description for the given node
   */
  public String getHTMLDescription(String node) {
    try {

      ModuleReferenceDataClientTreeData[] data = ModuleReferenceDataClientTreeData
          .selectDescription(conn, lang, node);
      String discard[] = { "" };
      if (data != null && data.length > 0 && data[0].linkname != null
          && !data[0].linkname.equals(""))
        data[0].statusName = "";
      if (data != null && data.length > 0
          && (data[0].updateAvailable == null || data[0].updateAvailable.equals("")))
        discard[0] = "update";

      XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/modules/ModuleTreeDescription", discard).createXmlDocument();
      xmlDocument.setData("structureDesc", data);
      return xmlDocument.print();

    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
}
