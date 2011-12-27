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
package org.openbravo.erpCommon.utility;

class ToolBar_Image implements HTMLElement {
  String name = "";
  String description = "";
  String base_direction;
  String imageClass = "";

  public ToolBar_Image(String _base_direction, String _name, String _description) {
    this(_base_direction, _name, _description, _name);
  }

  public ToolBar_Image(String _base_direction, String _name, String _description, String _imageClass) {
    this.name = _name;
    this.base_direction = _base_direction;
    this.description = _description;
    this.imageClass = _imageClass;
  }

  public String getWidth() {
    return "16";
  }

  public String elementType() {
    return "IMAGE";
  }

  public String toString() {
    StringBuffer toolbar = new StringBuffer();
    toolbar.append("<img class=\"Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_")
        .append(imageClass).append("\" src=\"").append(base_direction)
        .append("/images/blank.gif\" ");
    toolbar.append("title=\"").append(description);
    // Needed to build the HTML tag id as refresh has in classic layout
    if (name.equals("Refresh") || name.equals("Edition") || name.equals("Relation")) {
      toolbar.append("\" border=\"0\" id=\"linkButton").append(name).append("\"");
    } else {
      toolbar.append("\" border=\"0\" id=\"button").append(name).append("\"");
    }
    toolbar.append(">");
    return toolbar.toString();
  }
}
